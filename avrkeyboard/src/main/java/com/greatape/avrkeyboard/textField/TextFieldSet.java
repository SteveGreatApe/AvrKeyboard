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

import com.greatape.avrkeyboard.model.AvrKeyboard;
import com.greatape.avrkeyboard.model.KeyboardEventListener;
import com.greatape.avrkeyboard.styles.KeyStyle;
import com.greatape.avrkeyboard.util.AvrUtil;
import com.greatape.avrutils.objects.LayoutLink;
import com.greatape.avrutils.objects.LinkedObjects;
import com.greatape.avrutils.objects.VertexBox;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Steve Townsend
 */
public class TextFieldSet extends GVRSceneObject implements KeyboardEventListener, TextField.Listener,
        LayoutLink.LinkOwner, LinkedObjects.Linkable, LayoutLink.LinkParent {
    private Listener mListener;
    private List<TextField> mTextFields;
    private TextField mActiveTextField;
    private LinkedObjects mLinkedObjects;
    private int mRenderingOrder;
    private KeyStyle mStyle;
    private GVRTexture mMainTexture;
    private GVRTexture mHoverTexture;
    private boolean mHover;
    private float mCurrentWidth;
    private float mCurrentHeight;
    private Vector3f mCurrentMidPoint;
    private BackgroundDetails mBackgroundDetails;

    public interface Listener {
        void onComplete();
        void onCancel();
    }

    public static class BackgroundDetails {
        float minimumWidth;
        float minimumHeight;
        float xMargin;
        float yMargin;

        public BackgroundDetails(float minimumWidth, float minimumHeight, float xMargin, float yMargin) {
            this.minimumWidth = minimumWidth;
            this.minimumHeight = minimumHeight;
            this.xMargin = xMargin;
            this.yMargin = yMargin;
        }
    }

    public TextFieldSet(GVRContext gvrContext, int renderingOrder) {
        super(gvrContext);
        mTextFields = new ArrayList<>();
        mLinkedObjects = new LinkedObjects(this);
        mLinkedObjects.positionUpdated();
        mRenderingOrder = renderingOrder;
    }

    public TextFieldSet(AvrKeyboard avrKeyboard, Listener listener, int renderingOrder) {
        this(avrKeyboard.getGVRContext(), renderingOrder);
        avrKeyboard.setOnKeyboardEventListener(this);
        mListener = listener;
    }

    public void add(final TextField textField) {
        textField.setParentLink(this);
        addChildObject(textField);
        textField.setListener(this);
        mTextFields.add(textField);
        if (mTextFields.size() == 1 && !textField.isReadOnly()) {
            textField.addBackButton();
            switchActiveTextField(textField);
        }
    }

    public void setScale(float scale) {
        getTransform().setScale(scale, scale, scale);
    }

    public void close() {
        for(TextField textField : mTextFields) {
            textField.close();
        }
    }

    public List<TextField> getTextFields() {
        return mTextFields;
    }

    public void setHover(boolean hover) {
        if (hover != mHover) {
            mHover = hover;
            GVRRenderData renderData = getRenderData();
            if (renderData != null) {
                GVRMaterial material = renderData.getMaterial();
                if (material != null) {
                    material.setMainTexture(hover ? mHoverTexture : mMainTexture);
                }
            }
            for(TextField textField : mTextFields) {
                textField.setHover(mHover);
            }
        }
    }

    public void addBackground(KeyStyle style, BackgroundDetails backgroundDetails) {
        mStyle = style;
        mBackgroundDetails = backgroundDetails;
        doAddBackground();
    }

    private void doAddBackground() {
        if (mStyle == null || mStyle.background == null) {
            return;
        }
        VertexBox ownerBoundingBox = new VertexBox(this, getBoundsObjects());
        float width = Math.max(mBackgroundDetails.minimumWidth, ownerBoundingBox.getWidth() + mBackgroundDetails.xMargin * 2);
        float height = Math.max(mBackgroundDetails.minimumHeight, ownerBoundingBox.getHeight() + mBackgroundDetails.yMargin * 2);
        if (width == 0f || height == 0f) {
            return;
        }
        Vector3f midPoint = ownerBoundingBox.getMidPoint();
        if (width == mCurrentWidth && height == mCurrentHeight && midPoint.equals(mCurrentMidPoint)) {
            return;
        }
        mCurrentWidth = width;
        mCurrentHeight = height;
        mCurrentMidPoint = midPoint;
        GVRContext gvrContext = getGVRContext();
        GVRMesh mesh = gvrContext.createQuad(width, height);
        AvrUtil.offsetQuad(mesh, midPoint.x, midPoint.y, midPoint.z - 0.001f);
        GVRRenderData renderData = getRenderData();
        if (renderData == null) {
            renderData = new GVRRenderData(gvrContext);
            attachRenderData(renderData);
        }
        renderData.setAlphaBlend(true);
        renderData.setRenderingOrder(mRenderingOrder);
        renderData.setMesh(mesh);
        mMainTexture = mStyle.gvrTexture(gvrContext, width, height, mStyle.background);
        mHoverTexture = mStyle.gvrTexture(gvrContext, width, height, mStyle.hover_background);
        GVRMaterial material = new GVRMaterial(gvrContext);
        renderData.setMaterial(material);
        material.setMainTexture(mHover ? mHoverTexture : mMainTexture);
        attachCollider(new GVRMeshCollider(gvrContext, true));
    }

    private void switchActiveTextField(TextField textField) {
        if (textField != mActiveTextField) {
            if (mActiveTextField != null) {
                mActiveTextField.setFocused(false);
            }
            mActiveTextField = textField;
            if (mActiveTextField != null) {
                mActiveTextField.setFocused(true);
            }
        }
    }

    @Override
    public void onKeyPressed(char keyChar) {
        mActiveTextField.append(keyChar);
    }

    @Override
    public void onKeyDelete() {
        mActiveTextField.removeCharacter();
    }

    @Override
    public void onKeyConfirm() {
        int activeTextFieldIndex = getTextFieldIndex(mActiveTextField);
        if (activeTextFieldIndex < (mTextFields.size() - 1)) {
            switchActiveTextField(mTextFields.get(activeTextFieldIndex + 1));
        } else {
            mListener.onComplete();
        }
    }

    @Override
    public void onClaimFocus(TextField textField) {
        switchActiveTextField(textField);
    }

    @Override
    public void onBack(TextField textField) {
        doBack(textField);
    }

    private void doBack(TextField textField) {
        int activeTextFieldIndex = getTextFieldIndex(textField);
        if (activeTextFieldIndex > 0) {
            switchActiveTextField(mTextFields.get(activeTextFieldIndex - 1));
        } else {
            mListener.onCancel();
        }
    }

    private int getTextFieldIndex(TextField textField) {
        for (int index = 0; index < mTextFields.size(); index++) {
            if (mTextFields.get(index) == textField) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public GVRSceneObject getSceneObject() {
        return this;
    }

    @Override
    public List<GVRSceneObject> getLinkToBoundsObjects() {
        return getBoundsObjects();
    }

    @Override
    public List<GVRSceneObject> getBoundsObjects() {
        List<GVRSceneObject> objects = null;
        if (mTextFields.size() > 0) {
            List<GVRSceneObject> childObjects = new ArrayList<>();
            for (TextField textField : mTextFields) {
                Vector3f offset = AvrUtil.vectorFromPosition(textField.getTransform());
                textField.getBoundsObjects(childObjects, offset);
            }
            VertexBox bounds = new VertexBox(this, childObjects);
            GVRMesh mesh = getGVRContext().createQuad(bounds.getWidth(), bounds.getHeight());
            Vector3f offset = bounds.getMidPoint();
            AvrUtil.offsetQuad(mesh, offset.x, offset.y, offset.z);
            objects = Collections.singletonList(new GVRSceneObject(getGVRContext(), mesh));
        }
        return objects;
    }

    @Override
    public void addLink(LayoutLink layoutLink) {
        mLinkedObjects.addLink(layoutLink);
    }

    @Override
    public void positionUpdated() {
        mLinkedObjects.positionUpdated();
        doAddBackground();
    }

    @Override
    public void sizeUpdated() {
        mLinkedObjects.positionUpdated();
        doAddBackground();
    }
}
