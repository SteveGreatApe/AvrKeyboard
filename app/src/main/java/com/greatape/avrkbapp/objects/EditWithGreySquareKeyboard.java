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
import com.greatape.avrkbapp.style.GreySquareStyle;
import com.greatape.avrkeyboard.model.AvrKeyboard;
import com.greatape.avrkeyboard.styles.TextFieldStyle;

import org.gearvrf.GVRContext;

/**
 * @author Steve Townsend
 */
public class EditWithGreySquareKeyboard extends EditableTextObject {

    public EditWithGreySquareKeyboard(GVRContext gvrContext) {
        super(gvrContext);
        setKeyboardStyle(new GreySquareStyle.Keyboard());
        setKeyboardType(AvrKeyboard.KeyboardType.ALPHA);
        TextFieldStyle textFieldStyle = new GreySquareStyle.TextField();
        TextFieldSpec[] fieldSpecs = new TextFieldSpec[]{new TextFieldSpec(true, true)};
        setTextFields(fieldSpecs, textFieldStyle);
        mResult = new TextResult(gvrContext, textFieldStyle, 1, 4f, 0.2f, new TextResult.OnPickListener() {
            @Override
            public void onPick() {
                edit();
            }
        });
        mResult.setEmptyString(R.string.click_to_edit);
        mResult.setPosition(0.0f, 1.0f, 0.0f);
        mSceneObject.addChildObject(mResult.sceneObject());
        setAnimParams(0.25f, 0.0f, 0.0f, 0.0f, 0.0f, AvrKeyboard.ALIGN_TOP);
    }

    @Override
    public void editSuccess() {
        com.greatape.avrkeyboard.textField.TextField textField = mTextFieldSet.getTextFields().get(0);
        mResult.setText(textField.getCurrentText());
    }
}
