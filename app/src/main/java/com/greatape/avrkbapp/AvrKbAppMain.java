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

import com.greatape.avrkeyboard.util.AvrTouchHandler;
import com.greatape.avrutils.AvrEventHandler;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;

import java.util.EnumSet;

import static android.view.MotionEvent.BUTTON_PRIMARY;
import static android.view.MotionEvent.BUTTON_SECONDARY;

/**
 * @author Steve Townsend
 */
public class AvrKbAppMain extends GVRMain {
    private GVRContext mGVRContext;
    private GVRScene mMainScene;
    private AvrKbAppScene mAvrKbAppScene;
    private GVRSceneObject cursor;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);
        mGVRContext = gvrContext;
        mMainScene = gvrContext.getMainScene();

        // Launch the main Scene
        mAvrKbAppScene = new AvrKbAppScene(this);
        mAvrKbAppScene.resume(gvrContext);

        // Needs to be called before setting up the controller
        AvrTouchHandler.init(gvrContext);
        AvrEventHandler.init();

        // Create a cursor
        GVRAssetLoader assetLoader = new GVRAssetLoader(gvrContext);
        GVRAndroidResource trackerResource = new GVRAndroidResource(gvrContext, R.raw.cursor);
        GVRTexture trackerTexture = assetLoader.loadTexture(trackerResource);
        GVRInputManager inputManager = mGVRContext.getInputManager();
        cursor = new GVRSceneObject(mGVRContext, mGVRContext.createQuad(1f, 1f), trackerTexture);
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().disableLight();
        cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        // Set up the controller types supported.
        // TODO: Add and test Mouse and Gamepad controllers
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            private static final float DEPTH = -7.0f;
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removeControllerEventListener(AvrEventHandler.controllerButtonHandler());
                }
                GVRPicker picker = newController.getPicker();
                EnumSet<GVRPicker.EventOptions> eventOptions = picker.getEventOptions();
                eventOptions.add(GVRPicker.EventOptions.SEND_TO_HIT_OBJECT);
                int touchButtons = BUTTON_PRIMARY;
                switch(newController.getControllerType()) {
                    case CONTROLLER:
                        touchButtons = BUTTON_SECONDARY;
                        break;
                    case MOUSE:
                        break;
                }
                newController.setTouchButtons(touchButtons);

                newController.addControllerEventListener(AvrEventHandler.controllerButtonHandler());
                newController.setCursor(cursor);
                newController.setCursorDepth(DEPTH);
                newController.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
            }
        });
    }
}
