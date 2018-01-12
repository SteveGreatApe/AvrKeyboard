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

import android.view.KeyEvent;

import com.greatape.avrkbapp.objects.EditWithGreySquareKeyboard;
import com.greatape.avrkbapp.objects.EditableName;
import com.greatape.avrkbapp.objects.EditableNumber;
import com.greatape.avrkbapp.objects.EditableTextObject;
import com.greatape.avrkbapp.objects.FloorObject;
import com.greatape.avrkbapp.objects.LotsOfTextFields;
import com.greatape.avrkbapp.objects.OuterSphere;
import com.greatape.avrkbapp.objects.SmallScaleAndRedKeyboard;
import com.greatape.avrkeyboard.util.ClickLocation;
import com.greatape.avrutils.AvrControllerButton;
import com.greatape.avrutils.AvrPlayer;
import com.greatape.avrutils.AvrSceneBase;
import com.greatape.avrutils.VrMath;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTransform;
import org.joml.Vector3f;

/**
 * @author Steve Townsend
 */
public class AvrKbAppScene extends AvrSceneBase implements AvrPlayer.BoundsCheck {
    private final static float FloorSize = 60f;
    private final static float Radius = FloorSize / 2;
    private final static int[] sSphereColours = {0xFF2060A0, 0xFF000000, 0xFFFFFFFF, 0xFF801010, 0xFF108020};
    private final static String[] sFloorTextures = {
            "textures/Floor2.png",
            "textures/Floor3.png",
            "textures/Floor4.png",
            "textures/Floor5.png"
    };

    private final static float KeyboardSpacingAngle = 60;
    private final static float CameraYPos = 3f;
    private final static float MoveSpeed = 0.2f;

    private AvrKbAppMain mGvrMain;
    private GVRScene mMainScene;
    private EditableTextObject mEditableName;
    private EditableTextObject mEditableNumber;
    private EditableTextObject mSmallScaleAndRedKeyboard;
    private EditableTextObject mSquareGreyKeys;
    private EditableTextObject mLotsOfTextFields;
    private OuterSphere mOuterSphere;
    private AvrPlayer mAvrPlayer;

    private final static AvrPlayer.MoveConfig sMoveConfig = new AvrPlayer.MoveConfig();
    static {
        sMoveConfig.xSpeed = 12.5f;
        sMoveConfig.xMode = AvrPlayer.RotateMode.WhenPressed;
        sMoveConfig.zSpeed = 25f;
        sMoveConfig.zMode = AvrPlayer.RotateMode.WhenPressed;
        sMoveConfig.rotateSpeed = 0.666f;
        sMoveConfig.rotateMode = AvrPlayer.RotateMode.Swipe;
    }


    AvrKbAppScene(AvrKbAppMain gvrMain) {
        mGvrMain = gvrMain;
    }

    public void onInit(GVRContext gvrContext) {
        super.onInit(gvrContext);
        mMainScene = mGVRContext.getMainScene();
        GVRCameraRig cameraRig = mMainScene.getMainCameraRig();
        cameraRig.getTransform().setPositionY(CameraYPos);
        addSceneObjects(gvrContext);
        mAvrPlayer = new AvrPlayer(mGVRContext.getMainScene(), new Vector3f(0f, 2f, 0f));
        mAvrPlayer.setMoveConfig(sMoveConfig);
        mAvrPlayer.setBoundsCheck(this);
    }

    private void addSceneObjects(GVRContext gvrContext) {
        FloorObject floorModel = new FloorObject(gvrContext, FloorSize, sFloorTextures);
        GVRTransform transform = floorModel.sceneObject().getTransform();
        transform.rotateByAxis(-90f, 1.0f, 0.0f, 0.0f);
        floorModel.addToMainScene();

        mOuterSphere = new OuterSphere(gvrContext, Radius);
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
    public void checkMove(Vector3f oldPos, Vector3f newPos) {
        double newRadius = Math.sqrt(newPos.x * newPos.x + newPos.z * newPos.z);
        if (newRadius > Radius) {
            double oldRadius = Math.sqrt(oldPos.x * oldPos.x + oldPos.z * oldPos.z);
            double relativePos = 0f;
            if (oldRadius < Radius) {
                relativePos = getRelativeMove(oldRadius, newRadius, Radius);
            }
            newPos.x = (float)(oldPos.x + relativePos * (newPos.x - oldPos.x));
            newPos.z = (float)(oldPos.z + relativePos * (newPos.z - oldPos.z));
        }
    }

    private double getRelativeMove(double oldPos, double newPos, double limit) {
        return (limit - oldPos) / (newPos - oldPos);
    }
}
