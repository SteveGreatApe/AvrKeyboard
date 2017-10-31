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
package com.greatape.avrutils.anim;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRScaleAnimation;

/**
 * @author Steve Townsend
 */
public class AvrScaleAnim extends AvrAnim {
    private float mScaleX;
    private float mScaleY;
    private float mScaleZ;

    public AvrScaleAnim(float duration, float scaleX, float scaleY, float scaleZ) {
        super(duration);
        mScaleX = scaleX;
        mScaleY = scaleY;
        mScaleZ = scaleZ;
    }

    GVRAnimation createAnimation(GVRSceneObject sceneObject) {
        return new GVRScaleAnimation(sceneObject.getTransform(), mDuration, mScaleX, mScaleY, mScaleZ);
    }

    void setState(GVRSceneObject sceneObject) {
        sceneObject.getTransform().setScale(mScaleX, mScaleY, mScaleZ);
    }
}
