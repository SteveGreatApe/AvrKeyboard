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

import android.view.MotionEvent;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;

/**
 * @author Steve Townsend
 */
public interface AvrTouchEvents {
    void onAvrTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject);
    void onAvrTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject);
    default void onAvrEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {};
    default void onAvrExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {};
    default void onAvrInside(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {};
    default void onAvrMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {};
}
