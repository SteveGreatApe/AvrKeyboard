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

import android.text.TextUtils;
import android.view.MotionEvent;

import com.greatape.avrkeyboard.styles.TextFieldStyle;
import com.greatape.avrkeyboard.textField.TextField;
import com.greatape.avrkeyboard.textField.TextFieldSet;
import com.greatape.avrkeyboard.util.AvrTouchEvents;
import com.greatape.avrkeyboard.util.AvrTouchHandler;
import com.greatape.avrutils.anim.AvrAnim;
import com.greatape.avrutils.anim.AvrRelativePositionAnim;
import com.greatape.avrutils.anim.AvrScaleAnim;
import com.greatape.avrutils.anim.AvrShowHideAnimation;
import com.greatape.avrutils.objects.LayoutLink;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.ITouchEvents;
import org.joml.Vector3f;

import static android.view.MotionEvent.BUTTON_SECONDARY;

/**
 * @author Steve Townsend
 */
public class TextResult extends ObjectBase implements AvrTouchEvents {
    private static final float ZPOS = 0.001f;

    private OnPickListener mOnPickListener;
    private AvrShowHideAnimation mAnimation;
    private TextField[] mResultTextFields;
    private TextFieldSet mResultTextFieldSet;
    private String mEmptyString;

    public interface OnPickListener {
        void onPick();
    }

    TextResult(GVRContext gvrContext, TextFieldStyle textFieldStyle, int numTextFields, float minimumWidth, float minimumHeight, OnPickListener onPickListener) {
        mOnPickListener = onPickListener;
        mSceneObject = new GVRSceneObject(gvrContext);

        int renderingOrder = GVRRenderData.GVRRenderingOrder.TRANSPARENT + 10;
        mResultTextFieldSet = new TextFieldSet(gvrContext, renderingOrder);
        mResultTextFieldSet.getEventReceiver().addListener(new AvrTouchHandler(this));
        mResultTextFields = new TextField[numTextFields];
        mSceneObject.addChildObject(mResultTextFieldSet);
        LayoutLink.LinkOwner previousTextField = null;
        Vector3f midLowerEdge = new Vector3f(0.5f, 0.0f, 0.0f);
        Vector3f midUpperEdge = new Vector3f(0.5f, 1.0f, 1.0f);
        for(int textFieldIndex = 0; textFieldIndex < numTextFields; textFieldIndex++) {
            TextField textField = new TextField(gvrContext, textFieldStyle, true, null);
            textField.setRenderingOrder(renderingOrder + 10);
            textField.setProportional(true);
            mResultTextFieldSet.add(textField);
            mResultTextFields[textFieldIndex] = textField;
            if (previousTextField != null) {
                LayoutLink nameLink = new LayoutLink(previousTextField);
                nameLink.setSelfRelative(midLowerEdge);
                nameLink.setLinkRelative(midUpperEdge);
                nameLink.setExtraOffset(new Vector3f(0f, 0.25f, ZPOS));
                textField.addLink(nameLink);
            }
            previousTextField = textField;
        }
        if (previousTextField != null) {
            LayoutLink layoutLink = new LayoutLink(previousTextField);
            layoutLink.setSelfRelative(midLowerEdge);
            layoutLink.setLinkRelative(midLowerEdge);
            layoutLink.setExtraOffset(new Vector3f(0f, 0.0f, ZPOS));
            mResultTextFieldSet.addLink(layoutLink);
        }
        TextFieldSet.BackgroundDetails backgroundDetails =
                new TextFieldSet.BackgroundDetails(minimumWidth, minimumHeight,
                        textFieldStyle.font_height / 4,
                        textFieldStyle.font_height / 4);
        mResultTextFieldSet.addBackground(textFieldStyle.textStyle, backgroundDetails);
    }

    private void onSelected(boolean selected) {
        mResultTextFieldSet.setHover(selected);
    }

    public void setText(String text) {
        setText(0, text);
    }

    public void setText(int index, String text) {
        mResultTextFields[index].setText(index == 0 && TextUtils.isEmpty(text) ? mEmptyString : text);
    }

    void setEmptyString(int resId) {
        mEmptyString = mSceneObject.getGVRContext().getContext().getResources().getString(resId);
        if (TextUtils.isEmpty(mResultTextFields[0].getCurrentText())) {
            mResultTextFields[0].setText(mEmptyString);
        }
    }

    void show() {
        if (mAnimation != null) {
            mAnimation.show();
        } else {
            mSceneObject.setEnable(true);
        }
    }

    void hide() {
        if (mAnimation != null) {
            mAnimation.hide();
        } else {
            mSceneObject.setEnable(false);
        }
    }

    void setAnimParams(float animDuration, float scale) {
        mAnimation = new AvrShowHideAnimation(mSceneObject);
        GVRTransform transform = mSceneObject.getTransform();
        // Set up show animations
        mAnimation.addShowAnimation(mAnimation.createPositionToCurrentAnimation(transform, animDuration));
        mAnimation.addShowAnimation(mAnimation.createScaleToCurrentAnimation(transform, animDuration));
        // Set up hide animations
        AvrAnim hidePositionAnimation = new AvrRelativePositionAnim(animDuration,
                new Vector3f(transform.getPositionX(), 0, transform.getPositionZ()),
                new Vector3f(0.5f, 1f, 0.5f));
        mAnimation.addHideAnimation(hidePositionAnimation);
        final AvrAnim hideScaleAnimation = new AvrScaleAnim(animDuration, scale, scale, scale);
        mAnimation.addHideAnimation(hideScaleAnimation);
    }

    @Override
    public void onAvrEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        onSelected(true);
    }

    @Override
    public void onAvrExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        onSelected(false);
    }

    @Override
    public void onAvrTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onAvrTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject) {
        if (gvrPickedObject.hitLocation != null) {
            mOnPickListener.onPick();
        }
    }
}
