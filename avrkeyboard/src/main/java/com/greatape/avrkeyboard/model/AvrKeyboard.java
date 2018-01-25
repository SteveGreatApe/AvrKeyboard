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

import android.util.Log;

import com.android.inputmethod.annotations.UsedForTesting;
import com.android.inputmethod.keyboard.AndroidKeyboardLayout;
import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardId;
import com.android.inputmethod.keyboard.internal.MoreKeySpec;
import com.greatape.avrkeyboard.R;
import com.greatape.avrkeyboard.styles.KeyboardStyle;
import com.greatape.avrkeyboard.styles.KeyboardStyleDefault;
import com.greatape.avrkeyboard.util.AudioClip;
import com.greatape.avrkeyboard.util.AvrUtil;
import com.greatape.avrutils.objects.LayoutLink;
import com.greatape.avrutils.objects.LinkedObjects;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.utility.Threads;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.android.inputmethod.latin.common.Constants.CODE_DELETE;
import static com.android.inputmethod.latin.common.Constants.CODE_ENTER;
import static com.android.inputmethod.latin.common.Constants.CODE_SHIFT;
import static com.android.inputmethod.latin.common.Constants.CODE_SWITCH_ALPHA_SYMBOL;

/**
 * @author Steve Townsend
 */
public class AvrKeyboard extends GVRSceneObject implements KeyItem.KeyListener, LinkedObjects.Linkable {
    private static final int SPECIAL_FUNCTION_NONE = 0;
    private static final int SPECIAL_FUNCTION_DEL = 1;
    private static final int SPECIAL_FUNCTION_OK = 2;
    private static final int SPECIAL_FUNCTION_SHIFT = 3;
    private static final int SPECIAL_FUNCTION_TOGGLE = 4;

    public static final int SHIFT_LOWERCASE = 0;
    public static final int SHIFT_UPPERCASE = 1;
    public static final int SHIFT_UPPERCASE_LOCK = 2;
    public static final int SHIFT_FIRST_LETTER_UPPERCASE = 3;

    private static final float FADE_IN_TIME = 1.0f;
    private static final String TAG = "AvrKeyboard";

    public static final int MODE_SHIFTED = 0;
    public static final int MODE_NORMAL = 1;
    public static final int MODE_SPECIAL = 2;
    public static final int MODE_SHIFTED_SPECIAL = 3;

    public static final int ALIGN_CENTER = 0;
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_TOP = 4;
    public static final int ALIGN_BOTTOM = 8;

    private int mAlignment;
    private float mPosX;
    private float mPosY;
    private float mPosZ;
    private float mScale;
    private float mVrWidth;
    private float mVrHeight;
    private int mRenderingOrder;
    private boolean mInitialised;
    private boolean mDimmed;
    private Exception mConfigException;

    public enum KeyboardType {
        NUMERIC, ALPHA, EMOJI
    }

    private KeyboardEventListener mKeyboardEventListener;
    private AudioClip mKeyEnterSound;
    private KeyboardType mKeyboardType;
    private final KeyboardStyle mStyle;
    private List<KeyItem> mKeyItems;
    private Semaphore mKeyConfigSemaphore;

    private LinkedObjects mLinkedObjects;

    private int mShiftExtra;
    private int mShiftMode;

    public AvrKeyboard(GVRContext gvrContext, KeyboardType keyboardType, KeyboardStyle style) {
        super(gvrContext);
        setName("AvrKeyboard");
        setEnable(false); // Not enabled until explicitly shown
        mStyle = style ==  null ? new KeyboardStyleDefault() : style;
        mKeyboardType = keyboardType;
        mKeyEnterSound = new AudioClip(gvrContext.getContext(), R.raw.key_enter_sound);
        mRenderingOrder = GVRRenderData.GVRRenderingOrder.TRANSPARENT;
        mLinkedObjects = new LinkedObjects(this);
    }

    public void setPositionAndScale(float posX, float posY, float posZ, float scale, int alignment) {
        mPosX = posX;
        mPosY = posY;
        mPosZ = posZ;
        mScale = scale;
        mAlignment = alignment;
        doSetPosition();
    }

    @UsedForTesting
    public List<KeyItem> getKeyItems() {
        return mKeyItems;
    }

    public void setShiftMode(int shiftMode, int shiftExtra) {
        if (mShiftMode != shiftMode || mShiftExtra != shiftExtra) {
            mShiftExtra = shiftExtra;
            setMode(shiftMode);
        }
    }

    public void cancelAutoShift() {
        if (mShiftMode == MODE_SHIFTED || mShiftExtra == SHIFT_FIRST_LETTER_UPPERCASE) {
            setShiftMode(MODE_NORMAL, SHIFT_LOWERCASE);
        }
    }

    private void createLatinIME() {
        int[] elementIds = {
                KeyboardId.ELEMENT_ALPHABET,
                KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED,
                KeyboardId.ELEMENT_SYMBOLS,
                KeyboardId.ELEMENT_SYMBOLS_SHIFTED,
        };
        int[] modes = {
                MODE_NORMAL, MODE_SHIFTED, MODE_SPECIAL, MODE_SHIFTED_SPECIAL
        };
        initKeyboard(elementIds, modes);
        changeToLowercase();
    }

    private void createNumericIME() {
        int[] elementIds = { KeyboardId.ELEMENT_NUMBER };
        int[] modes = { MODE_NORMAL };
        initKeyboard(elementIds, modes);
        changeToLowercase();
    }

    private void createEmojiIME() {
        int[] elementIds = {
                KeyboardId.ELEMENT_EMOJI_CATEGORY1,
                KeyboardId.ELEMENT_EMOJI_CATEGORY2,
                KeyboardId.ELEMENT_EMOJI_CATEGORY3,
                KeyboardId.ELEMENT_EMOJI_CATEGORY4
        };
        int[] modes = {
                MODE_NORMAL, MODE_SHIFTED, MODE_SPECIAL, MODE_SHIFTED_SPECIAL
        };
        initKeyboard(elementIds, modes);
        changeToLowercase();
    }

    public void setOnKeyboardEventListener(KeyboardEventListener keyboardEventListener) {
        this.mKeyboardEventListener = keyboardEventListener;
    }

    public void setDimmed(boolean dimmed) {
        if (mDimmed != dimmed) {
            mDimmed = dimmed;
            for (KeyItem keyItem : mKeyItems) {
                keyItem.setDimmed(mDimmed);
            }
        }
    }

    @Override
    public void onLongPress(KeyItem keyItem) {
        // TODO: More keys
    }

    @Override
    public void onKeyTapped(KeyItem keyItem) {
        mKeyEnterSound.play();
        if (mKeyboardEventListener != null) {
            KeyboardCharItem currentItem = keyItem.getKeyboardCharItem();
            switch(currentItem.getSpecialFunction(mShiftMode)) {
                case SPECIAL_FUNCTION_DEL:
                    mKeyboardEventListener.onKeyDelete();
                    break;
                case SPECIAL_FUNCTION_OK:
                    mKeyboardEventListener.onKeyConfirm();
                    break;
                case SPECIAL_FUNCTION_SHIFT:
                    if (mShiftMode == MODE_SPECIAL) {
                        changeToShiftedSpecialCharacter();
                    } else if (mShiftMode == MODE_SHIFTED_SPECIAL) {
                        changeToSpecialCharacter();
                    } else {
                        shiftKeys();
                    }
                    break;
                case SPECIAL_FUNCTION_TOGGLE:
                    if (mShiftMode == MODE_SPECIAL || mShiftMode == MODE_SHIFTED_SPECIAL) {
                        changeToLowercase();
                    } else {
                        changeToSpecialCharacter();
                    }
                    break;
                default:
                    if (currentItem.isValidKey(mShiftMode)) {
                        mKeyboardEventListener.onKeyPressed((char) currentItem.getCharacter(mShiftMode));
                        if (mShiftMode == MODE_SHIFTED && mShiftExtra != SHIFT_UPPERCASE_LOCK) {
                            changeToLowercase();
                        }
                    }
                    break;
            }
        }
    }

    public void setBaseRenderingOrder(int renderingOrder) {
        mRenderingOrder = renderingOrder;
    }

    public void initKeyboard() {
        if (!mInitialised) {
            switch (mKeyboardType) {
                case ALPHA:
                    createLatinIME();
                    break;
                case NUMERIC:
                    createNumericIME();
                    break;
                case EMOJI:
                    createEmojiIME();
                    break;
            }
            mInitialised = true;
        }
    }

    public void showKeyboard() {
        setEnable(true);
        fadeInKeys();
    }

    public float getScale() {
        return mScale;
    }

    public void hideKeyboard() {
        setEnable(false);
    }

    public Vector3f getShowPosition() {
        Vector3f position = alignmentOffset(mAlignment, mScale);
        position.add(mPosX, mPosY, mPosZ);
        return position;
    }

    public Vector3f getHidePosition(float posX, float posY, float posZ, float scale, int alignment) {
        return new Vector3f(posX, posY, posZ).add(alignmentOffset(alignment, scale));
    }

    private void shiftKeys() {
        switch (mShiftExtra) {
            case SHIFT_LOWERCASE:
                changeToUppercase();
                break;
            case SHIFT_UPPERCASE:
                changeToShiftLock();
                break;
            case SHIFT_UPPERCASE_LOCK:
            case SHIFT_FIRST_LETTER_UPPERCASE:
                changeToLowercase();
                break;
            default:
                changeToLowercase();
                break;
        }
    }

    private void setMode(int mode) {
        mShiftMode = mode;
        switchMaterialState(mShiftMode, mShiftExtra);
    }

    private void changeToLowercase() {
        mShiftExtra = SHIFT_LOWERCASE;
        setMode(MODE_NORMAL);
    }

    private void changeToUppercase() {
        mShiftExtra = SHIFT_UPPERCASE;
        setMode(MODE_SHIFTED);
    }

    private void changeToShiftLock() {
        mShiftExtra = SHIFT_UPPERCASE_LOCK;
        setMode(MODE_SHIFTED);
    }

    private void changeToSpecialCharacter() {
        setMode(MODE_SPECIAL);
    }

    private void changeToShiftedSpecialCharacter() {
        setMode(MODE_SHIFTED_SPECIAL);
    }

    static int mapCodeToSpecialFunction(int code) {
        int specialFunction;
        switch(code) {
            case CODE_DELETE:
                specialFunction = AvrKeyboard.SPECIAL_FUNCTION_DEL;
                break;
            case CODE_ENTER:
                specialFunction = AvrKeyboard.SPECIAL_FUNCTION_OK;
                break;
            case CODE_SWITCH_ALPHA_SYMBOL:
                specialFunction = AvrKeyboard.SPECIAL_FUNCTION_TOGGLE;
                break;
            case CODE_SHIFT:
                specialFunction = AvrKeyboard.SPECIAL_FUNCTION_SHIFT;
                break;
            default:
                specialFunction = AvrKeyboard.SPECIAL_FUNCTION_NONE;
                break;
        }
        return specialFunction;
    }

    private void initKeyboard(int elementIds[], final int modes[]) {
        GVRContext gvrContext = getGVRContext();
        mKeyItems = new ArrayList<>();
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = 0f;
        float maxY = 0f;
        final HashMap<String, KeyItem> keysByLocation = new HashMap<>();
        Keyboard[] keyboards = AndroidKeyboardLayout.getKeyboards(gvrContext.getContext(), elementIds);
        for(int listIndex = 0; listIndex < keyboards.length; listIndex++) {
            Keyboard keyboard = keyboards[listIndex];
            List<Key> keys = keyboard.getSortedKeys();
            int mode = modes[listIndex];
            for(Key key : keys) {
                KeyItem item = keysByLocation.get(locationKey(key));
                if (item == null) {
                    int xPos = scaledX(key.getX());
                    int yPos = scaledY(key.getY());
                    int width = scaledX(key.getWidth());
                    int height = scaledY(key.getHeight());
                    minX = Math.min(minX, xPos);
                    minY = Math.min(minY, yPos);
                    maxX = Math.max(maxX, xPos + width);
                    maxY = Math.max(maxY, yPos + height);
                    item = new KeyItem(gvrContext, this, mStyle.keyStyle, width, height, mRenderingOrder);
                    item.setPosition(xPos + width / 2, -(yPos + height / 2));
                    addChildObject(item);
                    keysByLocation.put(locationKey(key), item);
                    mKeyItems.add(item);
                }
                KeyboardCharItem charItem = item.getKeyboardCharItem();
                charItem.setKeyCode(mode, key, mStyle);
                final MoreKeySpec[] moreKeys = key.getMoreKeys();
                if (moreKeys != null) {
                    charItem.setMoreKeys(mode, moreKeys);
                }
            }
        }
        float vrMinX = mStyle.keyStyle.convertPixelToVRFloatValue(minX);
        float vrMinY = mStyle.keyStyle.convertPixelToVRFloatValue(minY);
        mVrWidth = mStyle.keyStyle.convertPixelToVRFloatValue(maxX) - vrMinX + mStyle.left_margin + mStyle.right_margin;
        mVrHeight = mStyle.keyStyle.convertPixelToVRFloatValue(maxY) - vrMinY + mStyle.top_margin + mStyle.bottom_margin;
        createBackground(mVrWidth / 2 - mStyle.left_margin + vrMinX, mStyle.top_margin - vrMinY - mVrHeight / 2, mVrWidth, mVrHeight);
        doSetPosition();
        mKeyConfigSemaphore = new Semaphore(0);
        Threads.spawnIdle(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int keyMode : modes) {
                        // Do the none hover version first to get the basic keyboard up ASAP
                        configureKeyTextures(keyMode, false);
                        configureKeyTextures(keyMode, true);
                    }
                } catch (Exception e) {
                    mConfigException = e;
                }
                Log.d(TAG, "Char: " + KeyItem.getBitmapCacheStats());
                Log.d(TAG, "Resource: " + KeyItem.getResourceCacheStats());
                mKeyConfigSemaphore.release();
            }

            private void configureKeyTextures(int keyMode, boolean hover) {
                for (KeyItem key : keysByLocation.values()) {
                    key.configureKeyTextures(keyMode, hover);
                }
            }
        });
    }

    public void waitForConfigToComplete() throws Exception {
        if (mKeyConfigSemaphore != null) {
            try {
                mKeyConfigSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mKeyConfigSemaphore = null;
            if (mConfigException != null) {
                throw mConfigException;
            }
        }
    }

    private Vector3f alignmentOffset(int alignment, float scale) {
        float offsetX;
        if ((alignment & ALIGN_LEFT) != 0) {
            offsetX = 0;
        } else if ((alignment & ALIGN_RIGHT) != 0) {
            offsetX = -mVrWidth;
        } else {
            offsetX = -mVrWidth / 2;
        }
        float offsetY;
        if ((alignment & ALIGN_TOP) != 0) {
            offsetY = 0;
        } else if ((alignment & ALIGN_BOTTOM) != 0) {
            offsetY = mVrHeight;
        } else {
            offsetY = mVrHeight / 2;
        }
        return new Vector3f(offsetX * scale, offsetY * scale, 0f);
    }

    private void doSetPosition() {
        Vector3f alignment = alignmentOffset(mAlignment, mScale);
        GVRTransform transform = getTransform();
        transform.setPosition(mPosX + alignment.x, mPosY + alignment.y, mPosZ);
        transform.setScale(mScale, mScale, mScale);
        if (getRenderData() != null) {
            mLinkedObjects.positionUpdated();
        }
    }

    @Override
    public GVRSceneObject getSceneObject() {
        return this;
    }

    @Override
    public List<GVRSceneObject> getLinkToBoundsObjects() {
        return null;
    }

    @Override
    public void addLink(LayoutLink layoutLink) {
        mLinkedObjects.addLink(layoutLink);
    }

    private void createBackground(float xOffset, float yOffset, float width, float height) {
        GVRContext gvrContext = getGVRContext();
        GVRMesh mesh = gvrContext.createQuad(width, height);
        AvrUtil.offsetMesh(mesh, xOffset, yOffset, -0.001f);
        GVRMaterial material = AvrUtil.createTextureMaterial(gvrContext);
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setAlphaBlend(true);
        renderData.setRenderingOrder(mRenderingOrder);
        renderData.setMesh(mesh);
        GVRTexture texture = mStyle.keyStyle.gvrTexture(gvrContext, width, height, mStyle.background);
        AvrUtil.setDiffuseAndAmbientTextures(material, texture);
        renderData.setMaterial(material);
        attachRenderData(renderData);
        attachCollider(new GVRMeshCollider(gvrContext, true));
    }

    private int scaledX(int value) {
        return (int)(value * mStyle.button_scale_x);
    }

    private int scaledY(int value) {
        return (int)(value * mStyle.button_scale_y);
    }

    private String locationKey(Key key) {
        return key.getX() + ":" + key.getY() + ":" + key.getWidth() + ":" + key.getHeight();
    }

    private void switchMaterialState(int shiftMode, int shiftExtra) {
        for (KeyItem keyItem : mKeyItems) {
            boolean isValid = keyItem.getKeyboardCharItem().isValid(shiftMode);
            if (isValid) {
                keyItem.switchMaterialState(shiftMode, shiftExtra, null);
            }
            keyItem.setEnable(isValid);
        }
    }

    private void fadeInKeys() {
        GVRAnimationEngine animationEngine = getGVRContext().getAnimationEngine();
        for (KeyItem item : mKeyItems) {
            GVROpacityAnimation anim3 = new GVROpacityAnimation(item, FADE_IN_TIME, 1);
            anim3.start(animationEngine);
        }
    }
}
