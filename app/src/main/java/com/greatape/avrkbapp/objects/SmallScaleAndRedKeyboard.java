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
import com.greatape.avrkbapp.style.RedStyle;
import com.greatape.avrkeyboard.model.AvrKeyboard;
import com.greatape.avrkeyboard.styles.TextFieldStyle;
import com.greatape.avrkeyboard.textField.TextField;

import org.gearvrf.GVRContext;
import org.joml.Vector3f;

/**
 * @author Steve Townsend
 */
public class SmallScaleAndRedKeyboard extends EditableTextObject {

    public SmallScaleAndRedKeyboard(GVRContext gvrContext) {
        super(gvrContext);
        setKeyboardStyle(new RedStyle.RedKeyboard());
        setKeyboardOffset(new Vector3f(0f, 0.5f, 0f));
        // TODO: Investigate picker problems when using smaller scale
//        mScale = 0.5f;
        TextFieldStyle textFieldStyle = new RedStyle.RedTextField();
        TextFieldSpec[] fieldSpecs = new TextFieldSpec[]{new TextFieldSpec(16, true, false)};
        setTextFields(fieldSpecs, textFieldStyle);
        mResult = new TextResult(gvrContext, textFieldStyle, 1, 3f, 0.2f, new TextResult.OnPickListener() {
            @Override
            public void onPick() {
                edit();
            }
        });
        mResult.setEmptyString(R.string.click_to_edit);
        mResult.setPosition(0.0f, 1.0f, 0.0f);
        mSceneObject.addChildObject(mResult.sceneObject());
        setAnimParams(0.5f, 0.0f, 0.0f, -20.0f, 1.0f, AvrKeyboard.ALIGN_TOP);
    }


    protected void seedTextFields() {
        getTextField(0).setText("Seeded text");
    }

    @Override
    public void editSuccess() {
        TextField textField = mTextFieldSet.getTextFields().get(0);
        mResult.setText(textField.getCurrentText());
    }

}
