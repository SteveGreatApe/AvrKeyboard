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

package com.greatape.avrutils;

/**
 * @author Steve Townsend
 */
public class AvrEventHandler {
    private static AvrEventHandler sInstance;
    private AvrControllerButtonHandler mAvrControllerButtonHandler;

    public static void init() {
        sInstance = new AvrEventHandler();
    }

    private AvrEventHandler() {
        mAvrControllerButtonHandler = new AvrControllerButtonHandler();
    }

    public static AvrControllerButtonHandler controllerButtonHandler() {
        return sInstance.mAvrControllerButtonHandler;
    }

    static boolean keyDown(int keycode) {
        return sInstance.mAvrControllerButtonHandler.isDown(keycode);
    }

    static void addControllerButton(AvrControllerButton avrControllerButton) {
        sInstance.mAvrControllerButtonHandler.addControllerButton(avrControllerButton);
    }

    static void removeControllerButton(AvrControllerButton avrControllerButton) {
        sInstance.mAvrControllerButtonHandler.removeControllerButton(avrControllerButton);
    }
}
