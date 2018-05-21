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
package com.greatape.avrkeyboard.util;

import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;

/**
 * @author Steve Townsend
 */
public class AvrTouchHandler implements ITouchEvents {
    private final static boolean verboseLog = true;
    private final static String TAG = "AvrTouchHandler";

    private AvrTouchEvents mListener;

    private static AvrControllerMonitor sAvrControllerMonitor;

    public AvrTouchHandler(AvrTouchEvents listener) {
        mListener = listener;
    }

    public static void init(GVRContext gvrContext) {
        sAvrControllerMonitor = new AvrControllerMonitor(gvrContext);
    }

    public static GVRCursorController controller() {
        return sAvrControllerMonitor.controller;
    }

    private boolean isMainButton(GVRPicker.GVRPickedObject gvrPickedObject) {
        return true;
//        boolean ret = false;
//        switch(sAvrControllerMonitor.controllerType) {
//            case CONTROLLER:
//                if (gvrPickedObject.motionEvent != null) {
//                    int buttonState = gvrPickedObject.motionEvent.getButtonState();
//                    ret = buttonState == BUTTON_SECONDARY;
//                }
//                break;
//            case GAZE:
//                ret = true;
//                break;
//            default:
//                break;
//        }
//        if (verboseLog) Log.d(TAG, "isMainButton=" + ret + " " + sAvrControllerMonitor.controllerName() + " motionEvent=" + gvrPickedObject.motionEvent);
//        return ret;
    }

    @Override
    public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject) {
        if (verboseLog) Log.d(TAG, "onEnter: motionEvent=" + gvrPickedObject.motionEvent);
        mListener.onAvrEnter(sceneObj, gvrPickedObject);
    }

    @Override
    public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject) {
        if (verboseLog) Log.d(TAG, "onExit: motionEvent=" + gvrPickedObject.motionEvent);
        mListener.onAvrExit(sceneObj, gvrPickedObject);
    }

    @Override
    public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject) {
        if (verboseLog) Log.d(TAG, "onTouchStart: motionEvent=" + gvrPickedObject.motionEvent);
        if (isMainButton(gvrPickedObject)) {
            mListener.onAvrTouchStart(sceneObj, gvrPickedObject);
        }
    }

    @Override
    public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject) {
        if (verboseLog) Log.d(TAG, "onTouchEnd: motionEvent=" + gvrPickedObject.motionEvent);
        if (isMainButton(gvrPickedObject)) {
            mListener.onAvrTouchEnd(sceneObj, gvrPickedObject);
        }
    }

    @Override
    public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject) {
        if (verboseLog) Log.d(TAG, "onInside: motionEvent=" + gvrPickedObject.motionEvent);
        mListener.onAvrInside(sceneObj, gvrPickedObject);
    }

    @Override
    public void onMotionOutside(GVRPicker picker, MotionEvent motionEvent) {
        if (verboseLog) Log.d(TAG, "onMotionOutside: motionEvent=" + motionEvent);
        mListener.onAvrMotionOutside(picker, motionEvent);
    }

    private static class AvrControllerMonitor {
        private GVRCursorController controller;
        private GVRControllerType controllerType;

        AvrControllerMonitor(GVRContext gvrContext) {
            GVRInputManager inputManager = gvrContext.getInputManager();
            inputManager.getEventReceiver().addListener(new GVRInputManager.ICursorControllerSelectListener() {
                public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                    controller = newController;
                    controllerType = newController.getControllerType();
                    if (verboseLog) Log.d(TAG, "Set controller type=" + controllerType);
                }
            });
        }

        private String controllerName() {
            switch(controllerType) {
                case CONTROLLER:
                    return "CONTROLLER";
                case GAZE:
                    return "GAZE";
                default:
                    return String.valueOf(sAvrControllerMonitor.controllerType);
            }
        }
    }
}
