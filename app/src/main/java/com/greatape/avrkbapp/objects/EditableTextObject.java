/* Copyright 2018 Great Ape Software Ltd
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

import com.greatape.avrkeyboard.model.AvrKeyboard;
import com.greatape.avrkeyboard.styles.KeyboardStyle;
import com.greatape.avrkeyboard.styles.TextFieldStyle;
import com.greatape.avrkeyboard.textField.TextField;
import com.greatape.avrkeyboard.textField.TextFieldSet;
import com.greatape.avrutils.anim.AvrPositionAnim;
import com.greatape.avrutils.anim.AvrScaleAnim;
import com.greatape.avrutils.anim.AvrShowHideAnimation;
import com.greatape.avrutils.objects.LayoutLink;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.joml.Vector3f;

import java.util.List;

/**
 * @author Steve Townsend
 */
public abstract class EditableTextObject extends ObjectBase {
    final GVRContext mGVRContext;
    TextFieldSet mTextFieldSet;
    AvrKeyboard mAvrKeyboard;
    KeyboardStyle mKeyboardStyle;
    private AvrKeyboard.KeyboardType mKeyboardType;
    float mScale;
    private AvrShowHideAnimation mKeyboardAnimation;
    Vector3f mKeyboardOffset = new Vector3f();

    TextResult mResult;

    class TextFieldSpec {
        boolean expandable;
        int maxChars;
        boolean hasClearButton;
        boolean autoShiftOnEmptyFocus;

        TextFieldSpec(boolean hasClearButton, boolean autoShiftOnEmptyFocus) {
            this.expandable = true;
            this.hasClearButton = hasClearButton;
            this.autoShiftOnEmptyFocus = autoShiftOnEmptyFocus;
        }

        TextFieldSpec(int maxChars, boolean hasClearButton, boolean autoShiftOnEmptyFocus) {
            this.maxChars = maxChars;
            this.hasClearButton = hasClearButton;
            this.autoShiftOnEmptyFocus = autoShiftOnEmptyFocus;
        }
    }

    public EditableTextObject(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mSceneObject = new GVRSceneObject(gvrContext);
        mKeyboardType = AvrKeyboard.KeyboardType.ALPHA;
        mScale = 1.0f;
    }

    public void setKeyboardStyle(KeyboardStyle keyboardStyle) {
        mKeyboardStyle = keyboardStyle;
    }

    public void setKeyboardType(AvrKeyboard.KeyboardType keyboardType) {
        mKeyboardType = keyboardType;
    }

    public void setKeyboardOffset(Vector3f offset) {
        mKeyboardOffset = offset;
    }

    public void setTextFields(TextFieldSpec[] fieldSpecs, TextFieldStyle textFieldStyle) {
        mAvrKeyboard = new AvrKeyboard(mGVRContext, mKeyboardType, mKeyboardStyle);
        mAvrKeyboard.setBaseRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT + 10);
        mSceneObject.addChildObject(mAvrKeyboard);
        mAvrKeyboard.setPositionAndScale(mKeyboardOffset.x, mKeyboardOffset.y, mKeyboardOffset.z, mScale, AvrKeyboard.ALIGN_BOTTOM);
        mAvrKeyboard.initKeyboard();
        addTextFields(fieldSpecs, textFieldStyle);
    }

    void showResult() {
        mResult.show();
    }

    abstract void editSuccess();

    public void editFinish() {
        showResult();
    }

    private void focusFirstField() {
        List<TextField> textFields = mTextFieldSet.getTextFields();
        if (textFields.size() > 0) {
            textFields.get(0).setFocused();
        }
    }

    void edit() {
        mResult.hide();
        seedTextFields();
        focusFirstField();
        showKeyboard();
    }

    protected void seedTextFields() {
    }

    void setAnimParams(float animDuration, float posX, float posY, float posZ, float scale, int alignment) {
        mKeyboardAnimation = new AvrShowHideAnimation(mAvrKeyboard);
        // Add show animations
        Vector3f keyboardPosition = mAvrKeyboard.getShowPosition();
        mKeyboardAnimation.addShowAnimation(new AvrPositionAnim(animDuration, keyboardPosition.x, keyboardPosition.y, keyboardPosition.z));
        float keyboardScale = mAvrKeyboard.getScale();
        mKeyboardAnimation.addShowAnimation(new AvrScaleAnim(animDuration, keyboardScale, keyboardScale, keyboardScale));
        // Add hide animations
        Vector3f hidePos = mAvrKeyboard.getHidePosition(posX, posY, posZ, scale, alignment);
        mKeyboardAnimation.addHideAnimation(new AvrPositionAnim(animDuration, hidePos.x, hidePos.y, hidePos.z));
        mKeyboardAnimation.addHideAnimation(new AvrScaleAnim(animDuration, scale, scale, scale));
        mKeyboardAnimation.setToHiddenState();
        mResult.setAnimParams(animDuration, scale);
    }

    void showKeyboard() {
        if (mKeyboardAnimation != null) {
            mKeyboardAnimation.show();
        } else {
            mAvrKeyboard.showKeyboard();
        }
    }

    protected void hideKeyboard() {
        if (mKeyboardAnimation != null) {
            mKeyboardAnimation.hide();
        } else {
            mAvrKeyboard.hideKeyboard();
        }
    }

    void addTextFields(TextFieldSpec[] fieldSpecs, TextFieldStyle textFieldStyle) {
        int renderingOrder = GVRRenderData.GVRRenderingOrder.TRANSPARENT + 10;
        mTextFieldSet = new TextFieldSet(mAvrKeyboard, new TextFieldSet.Listener() {
            @Override
            public void onComplete() {
                editSuccess();
                onCancel();
            }

            @Override
            public void onCancel() {
                hideKeyboard();
                editFinish();
            }
        }, renderingOrder);
        final Vector3f selfRelativeAttach = new Vector3f(0.5f, 0.0f, 0.0f);
        final Vector3f linkRelativeAttach = new Vector3f(0.5f, 1.0f, 1.0f);
        final Vector3f fieldExtraOffset = new Vector3f(0f, 0.2f, 0f);
        LayoutLink fieldSetLink = new LayoutLink(mTextFieldSet);
        fieldSetLink.setSelfRelative(selfRelativeAttach);
        fieldSetLink.setLinkRelative(linkRelativeAttach);
        final Vector3f fieldSetExtraOffset = new Vector3f(0f, 0.25f, 0f);
        fieldSetLink.setExtraOffset(fieldSetExtraOffset);
        mTextFieldSet.setScale(1.0f);
        // TODO: Investigate why if I call addChildObject later the layout goes wrong
        mAvrKeyboard.addChildObject(mTextFieldSet);
        mAvrKeyboard.addLink(fieldSetLink);

        LayoutLink.LinkOwner previousTextField = null;
        for(TextFieldSpec fieldSpec : fieldSpecs) {
            TextField textField = new TextField(mGVRContext, textFieldStyle, false, mAvrKeyboard);
            textField.setRenderingOrder(renderingOrder + 10);
            if (fieldSpec.hasClearButton) {
                textField.addClearButton();
            }
            if (fieldSpec.expandable) {
                textField.setExpandable(true);
            } else {
                textField.setNumberOfCharacters(fieldSpec.maxChars);
            }
            textField.setAutoShiftOnEmptyFocus(fieldSpec.autoShiftOnEmptyFocus);
            mTextFieldSet.add(textField);

            if (previousTextField != null) {
                LayoutLink nameLink = new LayoutLink(previousTextField);
                nameLink.setSelfRelative(selfRelativeAttach);
                nameLink.setLinkRelative(linkRelativeAttach);
                nameLink.setExtraOffset(fieldExtraOffset);
                textField.addLink(nameLink);
            }
            previousTextField = textField;
        }
        if (previousTextField != null) {
            LayoutLink nameLink = new LayoutLink(previousTextField);
            nameLink.setSelfRelative(selfRelativeAttach);
            nameLink.setLinkRelative(selfRelativeAttach);
            mTextFieldSet.addLink(nameLink);
        }
        if (textFieldStyle.textStyle.background.drawable != 0) {
            TextFieldSet.BackgroundDetails backgroundDetails =
                    new TextFieldSet.BackgroundDetails(0, 0,
                            textFieldStyle.font_height / 4,
                            textFieldStyle.font_height / 4);
            mTextFieldSet.addBackground(textFieldStyle.textStyle, backgroundDetails);
        }
    }

    TextField getTextField(int index) {
        return mTextFieldSet.getTextFields().get(index);
    }
}
