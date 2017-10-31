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
package com.greatape.avrkeyboard.textField;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.greatape.avrkeyboard.model.AvrKeyboard;
import com.greatape.avrkeyboard.model.KeyItem;
import com.greatape.avrkeyboard.styles.TextFieldStyle;
import com.greatape.avrkeyboard.styles.TextFieldStyleDefault;
import com.greatape.avrkeyboard.util.AvrUtil;
import com.greatape.avrutils.objects.LayoutLink;
import com.greatape.avrutils.objects.LinkedObjects;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.IHoverEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.utility.Colors;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Steve Townsend
 */
public class TextField extends GVRSceneObject implements GVRDrawFrameListener, KeyItem.KeyListener, LayoutLink.LinkOwner, LinkedObjects.Linkable {
    private static final int TEXT_PIXEL_HEIGHT = 75;
    private static final float CURSOR_ZPOS = 0.0001f;

    private final static int CursorRenderingOrderBoost = 3;
    private final static int CharacterRenderingOrderBoost = 2;
    private final static int SpaceRenderingOrderBoost = 1;

    private static final int KEY_CODE_DELETE_ALL = 1;
    private static final int KEY_CODE_BACK = 2;

    private final TextFieldStyle mStyle;
    private final boolean mReadOnly;
    private final AvrKeyboard mAvrKeyboard;
    private GVRTexture mSpaceTexture;

    private int mBitmapHeight;
    private float mTextDrawPosY;
    private float mFontWidth;

    private List<TextFieldItem> mTextFieldItems = new ArrayList<>();
    private List<GVRSceneObject> mTextMarkers = new ArrayList<>();

    private int mMaxNumberOfCharacters;
    private boolean mExpandable;
    private TextFieldKey mClearButton;
    private TextFieldKey mBackButton;
    private Paint mPaint;
    private int mCursorIndex;
    private float mBaseLinePos;
    private boolean mFocused;
    private Listener mListener;

    private float mCursorFlashCountdown;
    private boolean mCursorFlashOn;

    private GVRSceneObject mCursor;
    private float mCursorHeight;
    private float mCursorBaseLinePos;
    private float mPixelsToSizeRatio;

    private GVRSceneObject mPhantomCursor;
    private int mActivePhantomCursorIndex;

    private boolean mProportional;
    private ArrayList<Float> mCharacterWidths;

    private int mBaseRenderingOrder;
    private LinkedObjects mLinkedObjects;
    private LayoutLink.LinkParent mParentLink;

    private boolean mAutoShiftOnEmptyFocus;

    interface Listener {
        void onClaimFocus(TextField textField);

        void onBack(TextField textField);
    }

    public TextField(GVRContext gvrContext, boolean readOnly, AvrKeyboard avrKeyboard) {
        this(gvrContext, new TextFieldStyleDefault(), readOnly, avrKeyboard);
    }

    public TextField(GVRContext gvrContext, TextFieldStyle style, boolean readOnly, AvrKeyboard avrKeyboard) {
        super(gvrContext);
        setName("TextField");
        mReadOnly = readOnly;
        mStyle = style;
        mAvrKeyboard = avrKeyboard;
        mBaseRenderingOrder = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
        initPaint();
        mTextFieldItems = new ArrayList<>();
        if (!mReadOnly) {
            mCursor = createCursor(gvrContext, false);
            mPhantomCursor = createCursor(gvrContext, true);
            mActivePhantomCursorIndex = -1;
            updateCursorIndex(0);

            Bitmap spaceBitmap = KeyItem.sCreateResourceBitmap(gvrContext, mStyle.space_drawable, style.cursor_color);
            mSpaceTexture = AvrUtil.bitmapTexture(gvrContext, spaceBitmap);
        } else {
            mExpandable = true;
        }
        mLinkedObjects = new LinkedObjects(this);
        mCharacterWidths = new ArrayList<>();
    }

    public void setProportional(boolean proportional) {
        if (!mReadOnly) {
            throw new RuntimeException("Only read only text fields can be proportional.");
        }
        mProportional = proportional;
    }

    boolean isReadOnly() {
        return mReadOnly;
    }

    public void setRenderingOrder(int renderingOrder) {
        mBaseRenderingOrder = renderingOrder;
    }

    public void setParentLink(LayoutLink.LinkParent parentLink) {
        mParentLink = parentLink;
    }

    private GVRSceneObject createCursor(GVRContext gvrContext, boolean isPhantom) {
        GVRSceneObject cursor = new GVRSceneObject(gvrContext, mStyle.cursor_width, mCursorHeight);
        GVRRenderData renderData = cursor.getRenderData();
        renderData.setRenderingOrder(mBaseRenderingOrder + CursorRenderingOrderBoost);
        // TODO: Latest Post 3.3 requires this change
        GVRMaterial material = new GVRMaterial(gvrContext, new GVRShaderId(GVRPhongShader.class));
        // else for 3.3
//        GVRMaterial material = new GVRMaterial(gvrContext);
//        renderData.setShaderTemplate(GVRPhongShader.class);
        // END 3.3
        int alpha = Color.alpha(mStyle.cursor_color);
        material.setDiffuseColor(
                Colors.byteToGl(Color.red(mStyle.cursor_color)),
                Colors.byteToGl(Color.green(mStyle.cursor_color)),
                Colors.byteToGl(Color.blue(mStyle.cursor_color)),
                Colors.byteToGl(isPhantom ? alpha / 2 : alpha));
        renderData.setAlphaBlend(true);
        renderData.setMaterial(material);
        cursor.setEnable(false);
        addChildObject(cursor);
        gvrContext.getMainScene().bindShaders(cursor);
        return cursor;
    }

    public void setFocused() {
        characterSelected(mTextFieldItems.size());
    }

    public void setExpandable(boolean expandable) {
        mExpandable = mReadOnly || expandable;
        if (mExpandable) {
            doSetNumberOfCharacters(mTextFieldItems.size());
        }
    }

    void setListener(Listener listener) {
        mListener = listener;
    }

    public void setNumberOfCharacters(int maxNumberOfCharacters) {
        mMaxNumberOfCharacters = maxNumberOfCharacters;
        mExpandable = false;
        while (mTextFieldItems.size() > mMaxNumberOfCharacters) {
            removeCharacter(mMaxNumberOfCharacters);
        }
        doSetNumberOfCharacters(mMaxNumberOfCharacters);
    }

    public String getCurrentText() {
        StringBuilder result = new StringBuilder();
        for (TextFieldItem textFieldItem : mTextFieldItems) {
            result.append(textFieldItem.getCharacter());
        }
        return result.toString();
    }

    public TextFieldStyle getStyle() {
        return mStyle;
    }

    public void close() {
        // To ensure we remove the listener
        setFocused(false);
    }

    void setHover(boolean hover) {
        for (TextFieldItem textFieldItem : mTextFieldItems) {
            textFieldItem.setHover(hover);
        }
    }

    public void setAutoShiftOnEmptyFocus(boolean enabled) {
        if (mAutoShiftOnEmptyFocus != enabled) {
            mAutoShiftOnEmptyFocus = enabled;
        }
    }

    void setFocused(boolean focused) {
        if (!mReadOnly && focused != mFocused) {
            mFocused = focused;
            if (mFocused) {
                checkAutoShift();
                getGVRContext().registerDrawFrameListener(this);
                updateCursorIndex(mCursorIndex);
                restartFlash();
            } else {
                getGVRContext().unregisterDrawFrameListener(this);
                mCursor.setEnable(false);
            }
        }
    }

    private void checkAutoShift() {
        if (mAutoShiftOnEmptyFocus) {
            if (mTextFieldItems.size() == 0) {
                mAvrKeyboard.setShiftMode(AvrKeyboard.MODE_SHIFTED, AvrKeyboard.SHIFT_FIRST_LETTER_UPPERCASE);
            } else {
                mAvrKeyboard.cancelAutoShift();
            }
        }
    }

    @Override
    public void onDrawFrame(float frameTime) {
        mCursorFlashCountdown -= frameTime;
        if (mCursorFlashCountdown <= 0) {
            mCursorFlashOn = !mCursorFlashOn;
            mCursorFlashCountdown = mCursorFlashOn ? mStyle.cursor_flash_on_time : mStyle.cursor_flash_off_time;
            mCursor.setEnable(mCursorFlashOn);
        }
    }

    void addBackButton() {
        mBackButton = addButton(mStyle.back_button, KEY_CODE_BACK);
        updateLayout();
    }

    public void addClearButton() {
        mClearButton = addButton(mStyle.delete_all_button, KEY_CODE_DELETE_ALL);
        updateLayout();
    }

    private TextFieldKey addButton(TextFieldStyle.Button style, int keyCode) {
        TextFieldKey textFieldKey = TextFieldKey.create(getGVRContext(), style, keyCode, this, mBaseRenderingOrder);
        addChildObject(textFieldKey);
        return textFieldKey;
    }

    @Override
    public void onLongPress(KeyItem keyItem) {
        onKeyTapped(keyItem);
    }

    @Override
    public void onKeyTapped(KeyItem keyItem) {
        if (keyItem instanceof TextFieldKey) {
            TextFieldKey textFieldKey = (TextFieldKey) keyItem;
            switch (textFieldKey.getCode()) {
                case KEY_CODE_DELETE_ALL:
                    deleteAll();
                    break;
                case KEY_CODE_BACK:
                    if (mListener != null) {
                        mListener.onBack(this);
                    }
                    break;
            }
        }
    }

    private void setPhantomCursor(int index) {
        index = Math.min(index, mTextFieldItems.size());
        if (index != mActivePhantomCursorIndex) {
            if (index >= 0) {
                if (mActivePhantomCursorIndex < 0) {
                    mPhantomCursor.setEnable(true);
                }
                float xPos = xPos(index, true);
                mPhantomCursor.getTransform().setPosition(xPos, mCursorBaseLinePos, CURSOR_ZPOS + 0.1f);
            } else {
                mPhantomCursor.setEnable(false);
            }
            mActivePhantomCursorIndex = index;
        }
    }

    private GVRSceneObject newSpaceLine(int position) {
        GVRContext gvrContext = getGVRContext();
        GVRSceneObject space = new SpaceObject(gvrContext, mSpaceTexture, position);
        space.getTransform().setPosition(position * mFontWidth, mBaseLinePos, 0.0001f);
        return space;
    }

    void removeCharacter() {
        if (mCursorIndex > 0) {
            removeCharacter(mCursorIndex - 1);
            if (mExpandable) {
                doSetNumberOfCharacters(mTextFieldItems.size());
            } else {
                updateLayout();
            }
        }
    }

    private void removeCharacter(int index) {
        TextFieldItem character = mTextFieldItems.remove(index);
        mCharacterWidths.remove(index);
        removeChildObject(character);
        if (mCursorIndex > index) {
            updateCursorIndex(mCursorIndex - 1);
        }
    }

    public void setText(final String text) {
        while (mCursorIndex > 0) {
            removeCharacter(0);
        }
        for (int index = 0; index < text.length(); index++) {
            addCharacter(text.charAt(index));
        }
        updateAfterCharactersAdded();
    }

    void append(final char keyChar) {
        this.getGVRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (mExpandable || mTextFieldItems.size() < mMaxNumberOfCharacters) {
                    int newCharIndex = mCursorIndex;
                    TextFieldItem character = addCharacter(keyChar);
                    character.getTransform().setPosition(xPos(newCharIndex), 0, 0);
                    updateAfterCharactersAdded();
                }
            }
        });
    }

    private void updateAfterCharactersAdded() {
        if (mExpandable) {
            doSetNumberOfCharacters(mTextFieldItems.size());
        }
        mLinkedObjects.positionUpdated();
        if (mParentLink != null) {
            mParentLink.sizeUpdated();
        }
    }

    private TextFieldItem addCharacter(char keyChar) {
        float[] widthArray = new float[1];
        Bitmap bitmap = createBitmap(keyChar, widthArray);
        float width = widthArray[0] * mPixelsToSizeRatio;
        TextFieldItem character = new TextFieldItem(getGVRContext(), width, mStyle.font_height, bitmap, keyChar, TextField.this);
        mCharacterWidths.add(mCursorIndex, width);
        character.getRenderData().setRenderingOrder(mBaseRenderingOrder + CharacterRenderingOrderBoost);
        mTextFieldItems.add(mCursorIndex, character);
        addChildObject(character);
        updateCursorIndex(mCursorIndex + 1);
        for (int updateIndex = mCursorIndex - 1; updateIndex < mTextFieldItems.size(); updateIndex++) {
            mTextFieldItems.get(updateIndex).getTransform().setPositionX(xPos(updateIndex));
        }
        return character;
    }

    private void updateCursorIndex(int index) {
        mCursorIndex = index;
        if (mFocused) {
            if (mCursorIndex == 0) {
                checkAutoShift();
            }
            mCursor.getTransform().setPosition(xPos(index, true), mCursorBaseLinePos, CURSOR_ZPOS);
            restartFlash();
        }
    }

    private void restartFlash() {
        mCursorFlashOn = true;
        mCursorFlashCountdown = mStyle.cursor_flash_on_time;
        mCursor.setEnable(true);
    }

    private float xPos(int index) {
        return xPos(index, false);
    }

    private float xPos(int index, boolean beforeCharPos) {
        float xPos;
        if (mProportional) {
            float totalWidth = 0;
            float offset = 0;
            for (int chr = 0; chr < mCharacterWidths.size(); chr++) {
                float chrWidth = mCharacterWidths.get(chr);
                totalWidth += chrWidth;
                if (chr < index) {
                    offset += chrWidth;
                } else if (!beforeCharPos && chr == index) {
                    offset += chrWidth / 2;
                }
            }
            xPos = offset - totalWidth / 2;
        } else {
            xPos = mFontWidth * (((float) index) - ((float) mTextMarkers.size()) / 2);
            if (!beforeCharPos) {
                xPos += mFontWidth / 2;
            }
        }
        return xPos;
    }

    private void updateLayout() {
        int maxCount = Math.max(mTextMarkers.size(), mTextFieldItems.size());
        for (int index = 0; index < maxCount; index++) {
            float xPos = xPos(index);
            if (index < mTextMarkers.size()) {
                mTextMarkers.get(index).getTransform().setPositionX(xPos);
            }
            if (index < mTextFieldItems.size()) {
                mTextFieldItems.get(index).getTransform().setPositionX(xPos);
            }
        }
        if (mBackButton != null) {
            mBackButton.getTransform().setPositionX(xPos(-1) - mStyle.back_button.margin);
        }
        if (mClearButton != null) {
            mClearButton.getTransform().setPositionX(xPos(maxCount) + mStyle.delete_all_button.margin);
        }
        updateCursorIndex(mCursorIndex);
    }

    private void deleteAll() {
        for (TextFieldItem textFieldItem : mTextFieldItems) {
            removeChildObject(textFieldItem);
        }
        mTextFieldItems.clear();
        mCharacterWidths.clear();
        if (mExpandable) {
            doSetNumberOfCharacters(0);
        }
        updateCursorIndex(0);
    }

    private void doSetNumberOfCharacters(int numberOfCharacters) {
        if (numberOfCharacters == 0) {
            numberOfCharacters = 1;
        }
        if (!mReadOnly) {
            if (mTextMarkers.size() > numberOfCharacters) {
                removeToMatch(numberOfCharacters);
            } else {
                addToMatch(numberOfCharacters);
            }
        }
        updateLayout();
        mLinkedObjects.positionUpdated();
        if (mParentLink != null) {
            mParentLink.sizeUpdated();
        }
    }

    private void addToMatch(int numberOfCharacters) {
        int oldSize = mTextMarkers.size();
        for (int position = oldSize; position < numberOfCharacters; position++) {
            GVRSceneObject space = newSpaceLine(position);
            mTextMarkers.add(position, space);
            addChildObject(space);
        }
    }

    private void removeToMatch(int numberOfCharacters) {
        for (int i = mTextMarkers.size() - 1; i >= numberOfCharacters; i--) {
            GVRSceneObject subLine = mTextMarkers.get(i);
            removeChildObject(subLine);
            mTextMarkers.remove(i);
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setTypeface(mStyle.textStyle.font_typeface);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(TEXT_PIXEL_HEIGHT);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float totalHeight = fontMetrics.bottom - fontMetrics.top;
        mPixelsToSizeRatio = mStyle.font_height / totalHeight;
        mFontWidth = Math.max(mPaint.measureText("M"), mPaint.measureText("W")) * mPixelsToSizeRatio;
        mCursorHeight = mStyle.font_height * (fontMetrics.descent - fontMetrics.ascent) / totalHeight;
        mBitmapHeight = (int) totalHeight;
        mTextDrawPosY = -fontMetrics.top;
        float totalMid = (fontMetrics.bottom + fontMetrics.top) / 2f;
        mBaseLinePos = mStyle.font_height * totalMid / totalHeight;
        float ascDescMid = (fontMetrics.descent + fontMetrics.ascent) / 2f;
        mCursorBaseLinePos = mStyle.font_height * (totalMid - ascDescMid) / totalHeight;
        mPaint.setFakeBoldText(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setFilterBitmap(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    private Bitmap createBitmap(char keyChar, float[] widthArray) {
        char[] charArray = new char[]{keyChar};
        mPaint.getTextWidths(charArray, 0, 1, widthArray);
        int width = (int) widthArray[0];
        Bitmap bitmap = Bitmap.createBitmap(width > 0 ? width : 1, mBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0);
        canvas.drawText(charArray, 0, 1, ((float) width) / 2, mTextDrawPosY, mPaint);
        return bitmap;
    }

    private void characterSelected(int index) {
        updateCursorIndex(Math.min(index, mTextFieldItems.size()));
        if (mListener != null) {
            mListener.onClaimFocus(this);
        }
    }

    void getBoundsObjects(List<GVRSceneObject> objects, Vector3f offset) {
        objects.add(createBoundsObject(offset));
    }

    private GVRSceneObject createBoundsObject(Vector3f offset) {
        float width = 0;
        if (mProportional) {
            for (int chr = 0; chr < mCharacterWidths.size(); chr++) {
                width += mCharacterWidths.get(chr);
            }
        } else {
            width = mFontWidth * mTextMarkers.size();
        }
        float extraXOffset = 0;
        if (mBackButton != null) {
            float extraWidth = mFontWidth + mStyle.back_button.margin;
            width += extraWidth;
            extraXOffset -= extraWidth / 2;
        }
        if (mClearButton != null) {
            float extraWidth = mFontWidth + mStyle.delete_all_button.margin;
            width += extraWidth;
            extraXOffset += extraWidth / 2;
        }
        GVRMesh mesh = getGVRContext().createQuad(width, mCursorHeight);
        if (extraXOffset != 0) {
            AvrUtil.offsetQuad(mesh, extraXOffset, 0, 0);
        }
        if (offset != null) {
            AvrUtil.offsetQuad(mesh, offset.x, offset.y, offset.z);
        }
        return new GVRSceneObject(getGVRContext(), mesh);
    }

    @Override
    public void addLink(LayoutLink layoutLink) {
        mLinkedObjects.addLink(layoutLink);
    }

    @Override
    public GVRSceneObject getSceneObject() {
        return this;
    }

    @Override
    public List<GVRSceneObject> getLinkToBoundsObjects() {
        return Collections.singletonList(createBoundsObject(null));
    }

    @Override
    public List<GVRSceneObject> getBoundsObjects() {
        return Collections.singletonList(createBoundsObject(null));
    }

    @Override
    public void positionUpdated() {
        mLinkedObjects.positionUpdated();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getCurrentText());
        stringBuilder.append(" ");
        stringBuilder.append(getTransform());
        return stringBuilder.toString();
    }

    public class SpaceObject extends GVRSceneObject implements ITouchEvents, IHoverEvents {
        private int mIndex;
        private boolean mClickLeft;
        private boolean mIsSelected;

        SpaceObject(GVRContext gvrContext, GVRTexture texture, int index) {
            super(gvrContext, mStyle.space_width * mFontWidth, mStyle.space_height * mStyle.font_height, texture);
            this.mIndex = index;
            getRenderData().setRenderingOrder(mBaseRenderingOrder + SpaceRenderingOrderBoost);
            //
            GVRMesh colliderMesh = gvrContext.createQuad(mFontWidth, mStyle.font_height);
            final float[] textureCoords = colliderMesh.getTexCoords();
            for (int coord = 1; coord < textureCoords.length; coord += 2) {
                textureCoords[coord] = textureCoords[coord] - mBaseLinePos;
            }
            colliderMesh.setTexCoords(textureCoords);
            attachCollider(new GVRMeshCollider(colliderMesh));
        }

        void onClickChar(float[] hitLocation) {
            // TODO: Resurrect Phantom cursor for hover. Problem is GVRPickerInput doesn't deliver hover moves or location with move events.
            if (hitLocation != null) {
                mClickLeft = hitLocation[0] < 0f;
                characterSelected(selectedIndex());
            }
        }

        void setSelected(boolean selected) {
            if (mIsSelected && !selected) {
                setPhantomCursor(-1);
            }
            mIsSelected = selected;
        }

        int selectedIndex() {
            return mClickLeft || mIndex >= mTextFieldItems.size() ? mIndex : mIndex + 1;
        }

        @Override
        public void onHoverEnter(GVRSceneObject sceneObject) {
            setSelected(true);
        }

        @Override
        public void onHoverExit(GVRSceneObject sceneObject) {
            setSelected(false);
        }

        @Override
        public void onTouch(GVRSceneObject sceneObject, MotionEvent motionEvent, float[] hitLocation) {
            final int action = motionEvent.getAction();
            StringBuilder location = new StringBuilder();
            if (hitLocation != null) {
                location.append(hitLocation[0]).append(",").append(hitLocation[1]).append(",").append(hitLocation[2]);
            } else {
                location.append("null");
            }
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
                    onClickChar(hitLocation);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    break;
            }
        }
    }
}
