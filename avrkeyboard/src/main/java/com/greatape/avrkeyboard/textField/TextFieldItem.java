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

import com.greatape.avrkeyboard.styles.KeyStyle;
import com.greatape.avrkeyboard.util.AvrUtil;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;

/**
 * @author Steve Townsend
 */
public class TextFieldItem extends GVRSceneObject {
    private final boolean mSupportsHover;
    private char mKeyChar;
    private boolean mHover;
    private KeyStyle mTextKeyStyle;

    TextFieldItem(GVRContext gvrContext, float sceneObjectWidth, float sceneObjectHeight, Bitmap bitmap, char keyChar, TextField textField) {
        super(gvrContext, sceneObjectWidth, sceneObjectHeight, AvrUtil.bitmapTexture(gvrContext, bitmap));
        setName("TextFieldItem");
        mKeyChar = keyChar;
        GVRRenderData renderData = getRenderData();
        mTextKeyStyle = textField.getStyle().textStyle;
        renderData.getMaterial().setColor(mTextKeyStyle.text_color);
        renderData.setAlphaBlend(true);
        mSupportsHover = mTextKeyStyle.text_color_hover != 0;
    }

    void setHover(boolean hover) {
        if (mSupportsHover && hover != mHover) {
            mHover = hover;
            GVRRenderData renderData = getRenderData();
            renderData.getMaterial().setColor(mHover ? mTextKeyStyle.text_color_hover : mTextKeyStyle.text_color);
        }
    }

    public char getCharacter() {
        return mKeyChar;
    }

    @Override
    public String toString() {
        return "TextFieldItem: '" + mKeyChar + "'";
    }
}
