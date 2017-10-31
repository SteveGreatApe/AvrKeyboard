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

package com.greatape.avrkbapp.objects;

import android.util.Log;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;

/**
 * @author Steve Townsend
 */
public abstract class ObjectBase {
    private final static String TAG = "ObjectBase";

    GVRSceneObject mSceneObject;
    protected GVRMaterial mMaterial;
    protected float mScale;

    public GVRSceneObject sceneObject() {
        return mSceneObject;
    }

    float zeroHeightAdjust() {
        return 0f;
    }

    public void setPosition(float x, float y, float z) {
        mSceneObject.getTransform().setPosition(x, y + zeroHeightAdjust(), z);
    }

    public void setOnArc(float angle, float distance, float height) {
        setOnArc(mSceneObject, angle, distance, height + zeroHeightAdjust());
    }

    public static void setOnArc(GVRSceneObject sceneObject, float angle, float distance, float height) {
        double radians = angle * Math.PI / 180.0;
        double x = Math.sin(radians) * distance;
        double z = Math.cos(radians) * -distance;
        sceneObject.getTransform().setPosition((float)x, height, (float)z);
        sceneObject.getTransform().rotateByAxis(-angle, 0.0f, 1.0f, 0.0f);
    }

    public void setScale(float scale) {
        mScale = scale;
        mSceneObject.getTransform().setScale(scale, scale, scale);
    }

    public void addToMainScene() {
        mSceneObject.getGVRContext().getMainScene().addSceneObject(mSceneObject);
        Log.d(TAG, "addToMainScene: " + mSceneObject);
    }

    public void removeFromScene() {
        mSceneObject.getGVRContext().getMainScene().removeSceneObject(mSceneObject);
    }
}
