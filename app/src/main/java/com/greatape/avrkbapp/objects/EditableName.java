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
import com.greatape.avrkeyboard.styles.KeyboardStyleSolid;
import com.greatape.avrkeyboard.styles.TextFieldStyle;
import com.greatape.avrkeyboard.styles.TextFieldStyleSolid;
import com.greatape.avrkeyboard.textField.TextField;

import org.gearvrf.GVRContext;

/**
 * @author Steve Townsend
 */
public class EditableName extends EditableTextObject {

    public EditableName(GVRContext gvrContext) {
        super(gvrContext);
        setKeyboardStyle(new KeyboardStyleSolid());
        TextFieldSpec[] textFieldSpecs = new TextFieldSpec[]{
                new TextFieldSpec(true, true),
                new TextFieldSpec(true, true)
        };
        TextFieldStyle textFieldStyle = new TextFieldStyleSolid().scaled(1.2f);
        setTextFields(textFieldSpecs, textFieldStyle);

        TextFieldStyle resultStyle = addBackgroundToStyle(textFieldStyle);
        mResult = new TextResult(gvrContext, resultStyle, textFieldSpecs.length, 4f, 0.2f, new TextResult.OnPickListener() {
            @Override
            public void onPick() {
                edit();
            }
        });
        mResult.setEmptyString(R.string.click_to_edit_name);
        mResult.setPosition(0.0f, 1.0f, 0.0f);
        mSceneObject.addChildObject(mResult.sceneObject());
        setAnimParams(0.5f, 0.0f, 0.0f, 0.0f, 1.0f, AvrKeyboard.ALIGN_TOP);
    }

    @Override
    public void editSuccess() {
        for(int index = 0; index < mTextFieldSet.getTextFields().size(); index++) {
            TextField textField = mTextFieldSet.getTextFields().get(index);
            mResult.setText(index, textField.getCurrentText());
        }
    }

    private TextFieldStyle addBackgroundToStyle(TextFieldStyle textFieldStyle) {
        TextFieldStyle ret = new TextFieldStyle(textFieldStyle, 1f);
        ret.textStyle.background = new DrawableStyle(textFieldStyle.back_button.keyStyle.background);
        ret.textStyle.hover_background = new DrawableStyle(textFieldStyle.back_button.keyStyle.hover_background);
        return ret;
    }
}
