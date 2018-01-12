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
package com.greatape.avrkbapp.objects;

import com.greatape.avrkbapp.R;
import com.greatape.avrkeyboard.model.AvrKeyboard;
import com.greatape.avrkeyboard.styles.DrawableStyle;
import com.greatape.avrkeyboard.styles.KeyboardStyleDefault;
import com.greatape.avrkeyboard.styles.TextFieldStyle;
import com.greatape.avrkeyboard.styles.TextFieldStyleDefault;
import com.greatape.avrkeyboard.textField.TextField;

import org.gearvrf.GVRContext;

/**
 * @author Steve Townsend
 */
public class EditableNumber extends EditableTextObject {
    public EditableNumber(GVRContext gvrContext) {
        super(gvrContext);
        setKeyboardStyle(new KeyboardStyleDefault());
        setKeyboardType(AvrKeyboard.KeyboardType.NUMERIC);
        TextFieldStyle textFieldStyle = new TextFieldStyleDefault().scaled(1.4f);
        TextFieldSpec[] fieldSpecs = new TextFieldSpec[]{new TextFieldSpec(true, false)};
        setTextFields(fieldSpecs, textFieldStyle);
        mResult = new TextResult(gvrContext, addBackgroundToStyle(textFieldStyle), 1, 2f, 0.2f, new TextResult.OnPickListener() {
            @Override
            public void onPick() {
                edit();
            }
        });
        mResult.setEmptyString(R.string.click_to_edit_number);
        mResult.setPosition(0.0f, 1.0f, 0.0f);
        mSceneObject.addChildObject(mResult.sceneObject());
        setAnimParams(0.5f, 0.0f, 0.0f, 0.0f, 1.0f, AvrKeyboard.ALIGN_TOP);
    }

    @Override
    public void editSuccess() {
        TextField textField = mTextFieldSet.getTextFields().get(0);
        mResult.setText(textField.getCurrentText());
    }

    private TextFieldStyle addBackgroundToStyle(TextFieldStyle textFieldStyle) {
        TextFieldStyle ret = new TextFieldStyle(textFieldStyle, 1f);
        ret.textStyle.background = new DrawableStyle(textFieldStyle.back_button.keyStyle.background);
        ret.textStyle.hover_background = new DrawableStyle(textFieldStyle.back_button.keyStyle.hover_background);
        return ret;
    }
}
