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
package com.greatape.avrkbapp;

import com.greatape.avrkbapp.objects.EditWithGreySquareKeyboard;
import com.greatape.avrkbapp.objects.EditableName;
import com.greatape.avrkbapp.objects.EditableNumber;
import com.greatape.avrkbapp.objects.EditableTextObject;
import com.greatape.avrkbapp.objects.FloorObject;
import com.greatape.avrkbapp.objects.LotsOfTextFields;
import com.greatape.avrkbapp.objects.OuterSphere;
import com.greatape.avrkbapp.objects.SmallScaleAndRedKeyboard;
import com.greatape.avrutils.AvrPicker;
import com.greatape.avrutils.AvrSceneBase;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTransform;

/**
 * @author Steve Townsend
 */
public class AvrKbAppScene extends AvrSceneBase {
    private final static float FloorSize = 60f;
    private final static int[] sSphereColours = {0xFF2060A0, 0xFF000000, 0xFFFFFFFF, 0xFF801010, 0xFF108020};
    private final static String[] sFloorTextures = {
            "textures/Floor2.png",
            "textures/Floor3.png",
            "textures/Floor4.png",
            "textures/Floor5.png"
    };

    private final static float KeyboardSpacingAngle = 60;

    private AvrKbAppMain mGvrMain;
    private GVRScene mMainScene;
    private EditableTextObject mEditableName;
    private EditableTextObject mEditableNumber;
    private EditableTextObject mSmallScaleAndRedKeyboard;
    private EditableTextObject mSquareGreyKeys;
    private EditableTextObject mLotsOfTextFields;
    private OuterSphere mOuterSphere;

    AvrKbAppScene(AvrKbAppMain gvrMain) {
        mGvrMain = gvrMain;
    }

    public void onInit(GVRContext gvrContext) {
        super.onInit(gvrContext);
        mMainScene = mGVRContext.getMainScene();
        GVRCameraRig cameraRig = mMainScene.getMainCameraRig();
        cameraRig.getTransform().setPositionY(3f);
        addSceneObjects(gvrContext);
    }

    private void addSceneObjects(GVRContext gvrContext) {
        FloorObject floorModel = new FloorObject(gvrContext, FloorSize, sFloorTextures);
        GVRTransform transform = floorModel.sceneObject().getTransform();
        transform.rotateByAxis(-90f, 1.0f, 0.0f, 0.0f);
        floorModel.addToMainScene();

        mOuterSphere = new OuterSphere(gvrContext, 30f);
        mOuterSphere.setColors(sSphereColours);
        mOuterSphere.addToMainScene();

        mEditableName = new EditableName(gvrContext);
        mEditableName.setOnArc(0f, 10f, 0.1f);
        mMainScene.addSceneObject(mEditableName.sceneObject());

        mEditableNumber = new EditableNumber(gvrContext);
        mEditableNumber.setOnArc(KeyboardSpacingAngle, 10f, 0.1f);
        mMainScene.addSceneObject(mEditableNumber.sceneObject());

        mSmallScaleAndRedKeyboard = new SmallScaleAndRedKeyboard(gvrContext);
        mSmallScaleAndRedKeyboard.setOnArc(-KeyboardSpacingAngle, 10f, 0.1f);
        mMainScene.addSceneObject(mSmallScaleAndRedKeyboard.sceneObject());

        mSquareGreyKeys = new EditWithGreySquareKeyboard(gvrContext);
        mSquareGreyKeys.setOnArc(KeyboardSpacingAngle * 2, 10f, 0.1f);
        mMainScene.addSceneObject(mSquareGreyKeys.sceneObject());

        mLotsOfTextFields = new LotsOfTextFields(gvrContext);
        mLotsOfTextFields.setOnArc(KeyboardSpacingAngle * -2, 10f, 0.1f);
        mMainScene.addSceneObject(mLotsOfTextFields.sceneObject());
    }

    @Override
    public void close() {
        mMainScene.removeSceneObject(mEditableName.sceneObject());
        mMainScene.removeSceneObject(mEditableNumber.sceneObject());
    }

    @Override
    public String sceneName() {
        return "AVR Keyboard App";
    }

    @Override
    public void createPicker() {
        mAvrPicker = new AvrPicker(this, mMainScene);
    }
}
