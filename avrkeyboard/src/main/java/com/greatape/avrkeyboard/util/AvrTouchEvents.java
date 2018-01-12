package com.greatape.avrkeyboard.util;

import android.view.MotionEvent;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;

public interface AvrTouchEvents {
    void onAvrTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject);
    void onAvrTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject gvrPickedObject);
    default void onAvrEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {};
    default void onAvrExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {};
    default void onAvrInside(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {};
    default void onAvrMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {};
}
