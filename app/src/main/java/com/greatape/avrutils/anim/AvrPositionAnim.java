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
import org.gearvrf.animation.GVRPositionAnimation;

/**
 * @author Steve Townsend
 */
public class AvrPositionAnim extends AvrAnim {
    private float mPosX;
    private float mPosY;
    private float mPosZ;

    public AvrPositionAnim(float duration, float posX, float posY, float posZ) {
        super(duration);
        mPosX = posX;
        mPosY = posY;
        mPosZ = posZ;
    }

    GVRAnimation createAnimation(GVRSceneObject sceneObject) {
        return new GVRPositionAnimation(sceneObject.getTransform(), mDuration, mPosX, mPosY, mPosZ);
    }

    void setState(GVRSceneObject sceneObject) {
        sceneObject.getTransform().setPosition(mPosX, mPosY, mPosZ);
    }
}
