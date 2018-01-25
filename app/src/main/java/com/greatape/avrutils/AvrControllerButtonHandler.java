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

import org.gearvrf.GVRCursorController;

import java.util.WeakHashMap;

/**
 * @author Steve Townsend
 */
public class AvrControllerButtonHandler implements GVRCursorController.IControllerEvent {
    private SparseIntArray mKeysDown = new SparseIntArray();
    private WeakHashMap<AvrControllerButton, Void> mControllerButtons;

    public AvrControllerButtonHandler() {
        mControllerButtons = new WeakHashMap<>();
    }

    void addControllerButton(AvrControllerButton controllerButton) {
        mControllerButtons.put(controllerButton, null);
    }

    void removeControllerButton(AvrControllerButton avrControllerButton) {
        mControllerButtons.remove(avrControllerButton);
    }

    public boolean isDown(int keyCode) {
        return mKeysDown.get(keyCode) != 0;
    }

    private void keyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        boolean isDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
        mKeysDown.put(keyCode, isDown ? 1 : 0);
        for(AvrControllerButton button : mControllerButtons.keySet()) {
            if (button.matches(keyCode)) {
                button.clicked(isDown);
            }
        }
    }

    @Override
    public void onEvent(GVRCursorController gvrCursorController, boolean isActive) {
        for(KeyEvent keyEvent : gvrCursorController.getKeyEvents()) {
            keyEvent(keyEvent);
        }
//        for(MotionEvent motionEvent : gvrCursorController.getMotionEvents()) {
//        }
    }
}
