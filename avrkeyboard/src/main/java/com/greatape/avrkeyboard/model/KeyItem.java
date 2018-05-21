/* Copyright 2017 Great Ape Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greatape.avrkeyboard.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.SparseArray;

import com.android.inputmethod.annotations.UsedForTesting;
import com.greatape.avrkeyboard.styles.KeyStyle;
import com.greatape.avrkeyboard.util.AvrTouchEvents;
import com.greatape.avrkeyboard.util.AvrTouchHandler;
import com.greatape.avrkeyboard.util.AvrUtil;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;

import java.lang.ref.SoftReference;

/**
 * @author Steve Townsend
 */
public class KeyItem extends GVRSceneObject implements AvrTouchEvents {
    // Defines How much of the button area should a bitmap resource take up
    private final static float RESOURCE_BITMAP_FILL_FRACTION = 0.5f;
    private final static long LONG_PRESS_TIME = 2000L;

    private final int mBitmapWidth;
    private final int mBitmapHeight;
    private final float mMeshWidth;
    private final float mMeshHeight;
    private final KeyListener mKeyListener;
    private KeyboardCharItem mKeyboardCharItem;
    private Paint mPaint;
    private float mTextBaselineOffset;
    private KeyStyle mKeyStyle;

    private final static BitmapCache<Bitmap> sResourceCache = new BitmapCache<>();
    private final static BitmapCache<RectTexture> sBitmapCache = new BitmapCache<>();
    private GVRSceneObject mDetailSceneObject;
    private SparseArray<RectTexture> mRectTextures;
    private int mCurrentModeKey;
    private GVRTexture mMainTexture;
    private GVRTexture mHoverTexture;
    private boolean mHover;
    private int mCurrentMode;
    private int mCurrentShiftMode;
    private boolean mIsDepressed;
    private float mDepressedDepth;
    private boolean mDimmed;
    private Long mDownTime;

    public interface KeyListener {
        void onLongPress(KeyItem keyItem);
        void onKeyTapped(KeyItem keyItem);
    }

    KeyItem(GVRContext gvrContext, KeyListener keyListener, KeyStyle keyStyle, int width, int height, int renderingOrder) {
        this(gvrContext, keyListener, keyStyle, width, height,
                keyStyle.convertPixelToVRFloatValue(width),
                keyStyle.convertPixelToVRFloatValue(height),
                renderingOrder);
    }

    public KeyItem(GVRContext gvrContext, KeyListener keyListener, KeyStyle keyStyle, float vrWidth, float vrHeight, int renderingOrder) {
        this(gvrContext, keyListener, keyStyle,
                keyStyle.convertVRFloatToPixelValue(vrWidth),
                keyStyle.convertVRFloatToPixelValue(vrHeight),
                vrWidth, vrHeight, renderingOrder);
    }

    private KeyItem(GVRContext gvrContext, KeyListener keyListener, KeyStyle keyStyle, int width, int height, float vrWidth, float vrHeight, int renderingOrder) {
        super(gvrContext);
        setName("KeyItem");
        getEventReceiver().addListener(new AvrTouchHandler(this));
        mKeyListener = keyListener;
        mKeyStyle = keyStyle;
        mKeyboardCharItem = new KeyboardCharItem();
        mBitmapWidth = width;
        mBitmapHeight = height;
        mMeshWidth = vrWidth;
        mMeshHeight = vrHeight;
        mDepressedDepth = keyStyle.depressed_depth;
        mRectTextures = new SparseArray<>();
        mMainTexture = keyStyle.gvrTexture(gvrContext, vrWidth, vrHeight, keyStyle.background);
        mHoverTexture = keyStyle.gvrTexture(gvrContext, vrWidth, vrHeight, keyStyle.hover_background);
        initPaint(keyStyle);
        GVRRenderData renderData = createMesh(gvrContext);
        attachRenderData(renderData);
        AvrUtil.setDiffuseAndAmbientTextures(renderData.getMaterial(), mMainTexture);
        renderData.setAlphaBlend(true);
        renderData.setRenderingOrder(renderingOrder + 1);
        mDetailSceneObject = new GVRSceneObject(gvrContext);
        GVRRenderData detailRenderData = createMesh(gvrContext);
        mDetailSceneObject.attachRenderData(detailRenderData);
        detailRenderData.setRenderingOrder(renderingOrder + 2);
        detailRenderData.setAlphaBlend(true);
        addChildObject(mDetailSceneObject);
        if (keyStyle.backlight) {
            renderData.disableLight();
            detailRenderData.disableLight();
        }
        setDimmed(false);
        setTag(this);
        attachCollider(new GVRMeshCollider(gvrContext, true));
    }

    void setPosition(int x, int y) {
        getTransform().setPosition(mKeyStyle.convertPixelToVRFloatValue(x), mKeyStyle.convertPixelToVRFloatValue(y), getZPos());
    }

    private float getZPos() {
        return mIsDepressed ? 0f : mDepressedDepth;
    }

    protected void configureKeyTextures(int keyMode, boolean hover) {
        int imageResId = mKeyboardCharItem.getImageResId(keyMode);
        int keyHash;
        String character = null;
        if (imageResId != 0) {
            keyHash = imageResId;
        } else {
            character = mKeyboardCharItem.getDescription(keyMode);
            keyHash = character.hashCode();
        }
        int textColor = hover ? mKeyStyle.text_color_hover : mKeyStyle.text_color;
        keyHash = addToHash(keyHash, textColor);
        keyHash = addToHash(keyHash, mBitmapWidth, mBitmapHeight);
        RectTexture rectTexture = sBitmapCache.get(keyHash);
        if (rectTexture == null) {
            Bitmap textureBitmap;
            rectTexture = new RectTexture();
            if (imageResId != 0) {
                textureBitmap = createResourceBitmap(imageResId, textColor, rectTexture);
            } else if (character.length() > 0) {
                textureBitmap = createTextBitmap(character, textColor, rectTexture);
            } else {
                textureBitmap = null;
            }
            if (textureBitmap != null) {
                rectTexture.gvrTexture = AvrUtil.bitmapTexture(getGVRContext(), textureBitmap);
            }
            sBitmapCache.put(keyHash, rectTexture);
        }
        int modeKey = modeKey(keyMode, hover);
        mRectTextures.put(modeKey, rectTexture);
        if (modeKey == mCurrentModeKey) {
            setRectTexture(rectTexture);
        }
    }

    private int addToHash(int hash, int ... values) {
        for(int value : values) {
            hash = hash * 31 + value;
        }
        return hash;
    }

    private Bitmap createTextBitmap(String character, int textColor, RectTexture rectTexture) {
        Rect textRect = new Rect();
        mPaint.getTextBounds(character, 0, character.length(), textRect);
        Bitmap bitmap = Bitmap.createBitmap(textRect.width(), textRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0x00000000);
        mPaint.setColor(textColor);
        canvas.drawText(character, -textRect.left, -textRect.top, mPaint);
        rectTexture.xPos = 0;
        float midY = (textRect.top + textRect.bottom) / 2;
        rectTexture.yPos = mKeyStyle.convertPixelToVRFloatValue(mTextBaselineOffset - midY);
        rectTexture.xScale = ((float)textRect.width()) / mBitmapWidth;
        rectTexture.yScale = ((float)textRect.height()) / mBitmapHeight;
        return bitmap;
    }

    private Bitmap createResourceBitmap(int resId, int filterColor, RectTexture rectTexture) {
        Resources resources = getGVRContext().getContext().getResources();
        return sCreateResourceBitmap(resources, resId, filterColor, rectTexture, mBitmapWidth, mBitmapHeight);
    }

    public static Bitmap sCreateResourceBitmap(GVRContext gvrContext, int resId, int filterColor) {
        Resources resources = gvrContext.getContext().getResources();
        KeyItem.RectTexture rectTexture = new KeyItem.RectTexture();
        return sCreateResourceBitmap(resources, resId, filterColor, rectTexture, 0, 0);
    }

    private static Bitmap sCreateResourceBitmap(Resources resources, int resId, int filterColor, RectTexture rectTexture, int bitmapWidth, int bitmapHeight) {
        Bitmap srcBitmap = sResourceCache.get(resId);
        if (srcBitmap == null) {
            srcBitmap = BitmapFactory.decodeResource(resources, resId);
            sResourceCache.put(resId, srcBitmap);
        }
        if (bitmapWidth == 0) {
            bitmapWidth = srcBitmap.getWidth();
        }
        if (bitmapHeight == 0) {
            bitmapHeight = srcBitmap.getHeight();
        }
        float fillWidth = bitmapWidth * RESOURCE_BITMAP_FILL_FRACTION;
        float fillHeight = bitmapHeight * RESOURCE_BITMAP_FILL_FRACTION;
        float drawableWidthHeightRatio = ((float) srcBitmap.getWidth()) / srcBitmap.getHeight();
        float targetWidthHeightRatio = fillWidth / fillHeight;
        int drawWidth;
        int drawHeight;
        if (drawableWidthHeightRatio > targetWidthHeightRatio) {
            drawWidth = (int)fillWidth;
            drawHeight = (int)(fillWidth / targetWidthHeightRatio);
        } else {
            drawHeight = (int)fillHeight;
            drawWidth = (int)(fillHeight * targetWidthHeightRatio);
        }
        rectTexture.xPos = 0;
        rectTexture.yPos = 0;
        rectTexture.xScale = (float)drawWidth / bitmapWidth;
        rectTexture.yScale = (float)drawHeight / bitmapHeight;

        Bitmap bitmap = Bitmap.createBitmap(drawWidth, drawHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setFilterBitmap(true);
        canvas.drawColor(0x00000000);
        ColorFilter filter = new PorterDuffColorFilter(filterColor, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);
        RectF targetRect = new RectF(0, 0, drawWidth, drawHeight);
        canvas.drawBitmap(srcBitmap, null, targetRect, paint);
        return bitmap;
    }

    void setDimmed(boolean dimmed) {
        mDimmed = dimmed;
        int color = dimmed ? 0xFF808080 : 0xFFFFFFFF;
        AvrUtil.setDiffuseAndAmbientColors(this, color);
        AvrUtil.setDiffuseAndAmbientColors(mDetailSceneObject, color);
    }

    protected void switchMaterialState(int shiftMode, int shiftExtra, Boolean hover) {
        mCurrentMode = shiftMode;
        mCurrentShiftMode = shiftExtra;
        if (mKeyboardCharItem.isShiftKey(shiftMode)) {
            hover = (hover != null && hover) ||
                    (shiftMode == AvrKeyboard.MODE_SHIFTED && shiftExtra == AvrKeyboard.SHIFT_UPPERCASE_LOCK) ||
                    (shiftMode == AvrKeyboard.MODE_SHIFTED_SPECIAL);
        }
        if (mKeyboardCharItem.isSpecialKey(shiftMode)) {
            hover = (hover != null && hover) ||
                    (shiftMode == AvrKeyboard.MODE_SPECIAL || shiftMode == AvrKeyboard.MODE_SHIFTED_SPECIAL);
        }
        mCurrentModeKey = modeKey(shiftMode, hover != null ? hover : mHover);

        RectTexture rectTexture = mRectTextures.get(mCurrentModeKey);
        setRectTexture(rectTexture);
        if (hover != null && hover != mHover) {
            mHover = hover;
            AvrUtil.setDiffuseAndAmbientTextures(getRenderData().getMaterial(), hover ? mHoverTexture : mMainTexture);
        }
    }

    private void setRectTexture(RectTexture rectTexture) {
        if (rectTexture != null && rectTexture.gvrTexture != null) {
            GVRTransform transform = mDetailSceneObject.getTransform();
            transform.setScale(rectTexture.xScale, rectTexture.yScale, 1f);
            transform.setPosition(rectTexture.xPos, rectTexture.yPos, 0.001f);
            AvrUtil.setDiffuseAndAmbientTextures(mDetailSceneObject.getRenderData().getMaterial(), rectTexture.gvrTexture);
            mDetailSceneObject.setEnable(true);
        } else {
            mDetailSceneObject.setEnable(false);
        }
    }

    private int modeKey(int mode, boolean hover) {
        return mode * 2 + (hover ? 1 : 0);
    }

    private GVRRenderData createMesh(GVRContext gvrContext) {
        GVRRenderData renderData = new GVRRenderData(gvrContext, AvrUtil.createTextureMaterial(gvrContext));
        GVRMesh mesh = gvrContext.createQuad(mMeshWidth, mMeshHeight);
        renderData.setAlphaBlend(true);
        renderData.setMesh(mesh);
        return renderData;
    }

    @UsedForTesting
    public KeyboardCharItem getKeyboardCharItem() {
        return mKeyboardCharItem;
    }

    private void initPaint(KeyStyle keyStyle) {
        mPaint = new Paint();
        mPaint.setTypeface(keyStyle.font_typeface);
        mPaint.setTextSize(keyStyle.font_size);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setFakeBoldText(true);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mTextBaselineOffset = (fontMetrics.bottom + fontMetrics.top) / 2;
    }

    private void setSelected(boolean selected) {
        switchMaterialState(mCurrentMode, mCurrentShiftMode, selected);
    }

    private void onPickDown() {
        if (!mDimmed) {
            setDepressed(true);
            mDownTime = System.currentTimeMillis();
        }
    }

    private void onPickUp(long downTime) {
        setDepressed(false);
        if (!mDimmed) {
            if (downTime > LONG_PRESS_TIME) {
                mKeyListener.onLongPress(this);
            } else if (downTime >= 0) {
                mKeyListener.onKeyTapped(this);
            }
        }
    }

    @Override
    public void onAvrEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        setSelected(true);
        if (mDownTime != null) {
            onPickDown();
        }
    }

    @Override
    public void onAvrExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        setSelected(false);
    }

    @Override
    public void onAvrTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        onPickDown();
    }

    @Override
    public void onAvrTouchEnd(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        long downTime = -1;
        // TODO: Check out if we still need the hitLocation != null check
        if (mDownTime != null && gvrPickedObject.hitLocation != null) {
            downTime = System.currentTimeMillis() - mDownTime;
        }
        onPickUp(downTime);
        mDownTime = null;
    }

    protected void setDepressed(boolean depressed) {
        mIsDepressed = depressed;
        getTransform().setPositionZ(getZPos());
    }

    static String getBitmapCacheStats() {
        return sBitmapCache.toString();
    }

    static String getResourceCacheStats() {
        return sResourceCache.toString();
    }

    private static class RectTexture {
        float xPos;
        float yPos;
        float xScale;
        float yScale;
        GVRTexture gvrTexture;
    }

    private static class BitmapCache<T> {
        private SparseArray<SoftReference<T>> mSparseArray = new SparseArray<>();
        private int mHitCount;
        private int mMissCount;
        private int mPutCount;

        T get(int key) {
            SoftReference<T> ref = mSparseArray.get(key);
            T ret = ref != null ? ref.get() : null;
            if (ret != null) {
                mHitCount++;
            } else {
                mMissCount++;
            }
            return ret;
        }

        void put(int key, T bitmap) {
            mPutCount++;
            mSparseArray.put(key, new SoftReference<>(bitmap));
        }

        @Override
        public String toString() {
            return "Size=" + mSparseArray.size() + " Hit/Miss: " + mHitCount + "/" + mMissCount + " put() count=" + mPutCount;
        }
    }
}