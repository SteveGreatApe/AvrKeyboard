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
package com.greatape.avrutils;

import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRPickerInput;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRGearControllerSceneObject;

import java.lang.ref.WeakReference;

/**
 * @author Steve Townsend
 */
public class AvrPicker {
    private final static String TAG = "AvrPicker";

    private final static int EyeTrackerRenderOrder = 100000;

    private GVRSceneObject mHeadTracker;
    private boolean mEyePickerEnabled;
    private boolean mControllerPickerEnabled;
    private boolean mControllerPickerAvailable;
    private boolean mControllerPickerActive;
    private GVRPickerInput mControllerPicker;
    private GVRScene mScene;
    private AvrSceneBase mAvrSceneBase;
    private PlayCursorControllerListener mCursorControllerListener;
    private PlayControllerEventListener mPlayControllerEventListener;
    private GVRCursorController mActiveCursorController;
//    private AvrPlayer mAvrPlayer;
    private Object mAvrPlayer;
    private SparseIntArray mKeysDown;
    private WeakReference<AvrControllerButton> mControllerButton;

    public AvrPicker(AvrSceneBase avrSceneBase, GVRScene scene) {
        mAvrSceneBase = avrSceneBase;
        mScene = scene;
        GVRContext gvrContext = mScene.getGVRContext();
        mKeysDown = new SparseIntArray();
        mControllerPickerEnabled = true;
        mControllerPicker = new GVRPickerInput(gvrContext, mScene);

        GVRAssetLoader assetLoader = new GVRAssetLoader(gvrContext);
        GVRAndroidResource trackerResource = new GVRAndroidResource(gvrContext, com.greatape.avrkbapp.R.drawable.headtrackingpointer);
        GVRTexture trackerTexture = assetLoader.loadTexture(trackerResource);
        mHeadTracker = new GVRSceneObject(gvrContext, gvrContext.createQuad(0.1f, 0.1f), trackerTexture);
        mHeadTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        mHeadTracker.getRenderData().setDepthTest(false);
        mHeadTracker.getRenderData().setRenderingOrder(EyeTrackerRenderOrder);

        mCursorControllerListener = new PlayCursorControllerListener();
        mPlayControllerEventListener = new PlayControllerEventListener();
    }

    private void setEyePickerEnabled(boolean enabled) {
        if (enabled != mEyePickerEnabled) {
            mEyePickerEnabled = enabled;
            GVRCameraRig cameraRig = mScene.getMainCameraRig();
            if (enabled) {
                cameraRig.addChildObject(mHeadTracker);
            } else {
                cameraRig.removeChildObject(mHeadTracker);
            }
            mAvrSceneBase.pickerStateChanged();
        }
    }

//    public void setAttachedToPlayer(AvrPlayer player) {
//        if (mAvrPlayer != player) {
//            setControllerPickerActive(false);
//            mAvrPlayer = player;
//            if (mControllerPickerAvailable) {
//                setControllerPickerActive(true);
//            }
//        }
//    }

    private void setControllerPickerActive(boolean active) {
        if (active != mControllerPickerActive) {
            mControllerPickerActive = active;
            mControllerPicker.setEnable(active);
            if (active) {
                mControllerPicker.setPickRay(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1f);
                GVRGearControllerSceneObject controllerSceneObject = new GVRGearControllerSceneObject(mControllerPicker.getGVRContext());
                controllerSceneObject.attachComponent(mControllerPicker);
                controllerSceneObject.setRayDepth(100f);
                if (mAvrPlayer == null) {
                    mActiveCursorController.setSceneObject(controllerSceneObject);
                }
                mActiveCursorController.addControllerEventListener(mPlayControllerEventListener);
                setEyePickerEnabled(false);
            } else {
                mActiveCursorController.resetSceneObject();
                mActiveCursorController.removeControllerEventListener(mPlayControllerEventListener);
            }
            mAvrSceneBase.pickerStateChanged();
        }
    }

    public boolean eyePickerEnabled() {
        return mEyePickerEnabled;
    }

    public boolean controllerPickerActive() {
        return mControllerPickerActive;
    }

    boolean controllerPickerAvailable() {
        return mControllerPickerAvailable;
    }

    public void setPickerEnabled(boolean enabled) {
        mControllerPickerEnabled = enabled;
        setControllerPickerActive(mControllerPickerEnabled && mControllerPickerAvailable);
        if (!enabled) {
            setEyePickerEnabled(true);
        }
    }

    void removeControllers(GVRInputManager inputManager) {
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            mCursorControllerListener.onCursorControllerRemoved(cursor);
        }
    }

    void addControllers(GVRInputManager inputManager) {
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            mCursorControllerListener.onCursorControllerAdded(cursor);
        }
        setEyePickerEnabled(!mControllerPickerAvailable);
    }

    private void keyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        boolean isDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
        mKeysDown.put(keyCode, isDown ? 1 : 0);
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mControllerButton != null) {
                AvrControllerButton button = mControllerButton.get();
                if (button == null) {
                    mControllerButton = null;
                } else {
                    button.clicked(isDown);
                }
            }
        }
    }

    public boolean isDown(int keyCode) {
        return mKeysDown.get(keyCode) != 0;
    }

    public CursorControllerListener cursorControllerListener() {
        return mCursorControllerListener;
    }

    public GVRCursorController activeCursorController() {
        return mActiveCursorController;
    }

    public boolean isEnabled() {
        return mControllerPicker.isEnabled();
    }

    public void setControllerButton(AvrControllerButton controllerButton) {
        mControllerButton = new WeakReference<>(controllerButton);
    }

    private class PlayCursorControllerListener implements CursorControllerListener {

        @Override
        public void onCursorControllerAdded(final GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                mControllerPickerAvailable = true;
                mActiveCursorController = gvrCursorController;
                if (mControllerPickerEnabled) {
                    setControllerPickerActive(true);
                }
                mAvrSceneBase.activeCursorControllerChanged();
            } else {
                gvrCursorController.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                mControllerPickerAvailable = false;
                setControllerPickerActive(false);
                mActiveCursorController = null;
                mAvrSceneBase.activeCursorControllerChanged();
            }
        }
    }

    private class PlayControllerEventListener implements GVRCursorController.ControllerEventListener {
        @Override
        public void onEvent(GVRCursorController gvrCursorController) {
            MotionEvent motionEvent = gvrCursorController.getMotionEvent();
            if (motionEvent != null) {
//                Log.d(TAG, "PlayControllerEventListener.onEvent: motionEvent=" + motionEvent);
            } else {
                KeyEvent keyEvent = gvrCursorController.getKeyEvent();
                if (keyEvent != null) {
//                    Log.d(TAG, "PlayControllerEventListener.onEvent: keyEvent=" + keyEvent);
                    keyEvent(keyEvent);
                }
            }
        }
    }
}
