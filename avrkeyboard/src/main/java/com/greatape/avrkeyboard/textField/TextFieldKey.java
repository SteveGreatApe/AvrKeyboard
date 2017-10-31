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

import com.greatape.avrkeyboard.model.KeyItem;
import com.greatape.avrkeyboard.styles.KeyStyle;
import com.greatape.avrkeyboard.styles.TextFieldStyle;

import org.gearvrf.GVRContext;

/**
 * @author Steve Townsend
 */
public class TextFieldKey extends KeyItem {
    private final static int TEXT_FIELD_KEY_MODE = 0;

    private TextFieldKey(GVRContext gvrContext, KeyItem.KeyListener listener, KeyStyle keyStyle, float vrWidth, float vrHeight, int renderingOrder) {
        super(gvrContext, listener, keyStyle, vrWidth, vrHeight, renderingOrder);
    }

    static TextFieldKey create(GVRContext gvrContext, TextFieldStyle.Button style, int code, KeyItem.KeyListener listener, int renderingOrder) {
        TextFieldKey textFieldKey = new TextFieldKey(gvrContext, listener, style.keyStyle, style.width, style.height, renderingOrder);
        textFieldKey.getKeyboardCharItem().setKeyCode(TEXT_FIELD_KEY_MODE, code, style.icon_drawable);
        textFieldKey.switchMaterialState(TEXT_FIELD_KEY_MODE, 0, false);
        textFieldKey.configureKeyTextures(TEXT_FIELD_KEY_MODE, false);
        textFieldKey.configureKeyTextures(TEXT_FIELD_KEY_MODE, true);
        textFieldKey.setDepressed(false);
        return textFieldKey;
    }

    public int getCode() {
        return getKeyboardCharItem().getCharacter(TEXT_FIELD_KEY_MODE);
    }
}
