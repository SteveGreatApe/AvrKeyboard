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

import android.support.annotation.CallSuper;

import org.gearvrf.GVRContext;
import org.gearvrf.io.GVRInputManager;

/**
 * @author Steve Townsend
 */
public abstract class AvrSceneBase {
    protected GVRContext mGVRContext;
    protected AvrPicker mAvrPicker;
    private boolean mInitComplete;

    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
    }

    public abstract void close();

    public abstract String sceneName();
    public abstract void createPicker();

    @CallSuper
    public void hibernate() {
        if (mAvrPicker != null) {
            GVRInputManager inputManager = mGVRContext.getInputManager();
            inputManager.removeCursorControllerListener(mAvrPicker.cursorControllerListener());
            mAvrPicker.removeControllers(mGVRContext.getInputManager());
            mAvrPicker = null;
        }
    }

    @CallSuper
    public void resume(GVRContext gvrContext) {
        if (!mInitComplete) {
            onInit(gvrContext);
            mInitComplete = true;
        }
        createPicker();
        GVRInputManager inputManager = mGVRContext.getInputManager();
        mAvrPicker.addControllers(inputManager);
        inputManager.addCursorControllerListener(mAvrPicker.cursorControllerListener());
    }

    void pickerStateChanged() {
    }

    public void activeCursorControllerChanged() {
    }

    public boolean isInitComplete() {
        return mInitComplete;
    }
}
