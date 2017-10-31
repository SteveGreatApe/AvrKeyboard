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

import com.greatape.avrutils.objects.VertexBox;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRPositionAnimation;
import org.joml.Vector3f;

/**
 * @author Steve Townsend
 */
public class AvrRelativePositionAnim extends AvrAnim {
    private Vector3f mTargetPosition;
    private Vector3f mSelfRelativePosition;

    public AvrRelativePositionAnim(float duration, Vector3f targetPosition, Vector3f selfRelativePosition) {
        super(duration);
        mTargetPosition = targetPosition;
        mSelfRelativePosition = selfRelativePosition;
    }

    @Override
    GVRAnimation createAnimation(GVRSceneObject sceneObject) {
        Vector3f pos = calcTargetPosition(sceneObject);
        return new GVRPositionAnimation(sceneObject.getTransform(), mDuration, pos.x, pos.y, pos.z);
    }

    @Override
    void setState(GVRSceneObject sceneObject) {
        Vector3f pos = calcTargetPosition(sceneObject);
        sceneObject.getTransform().setPosition(pos.x, pos.y, pos.z);
    }

    Vector3f calcTargetPosition(GVRSceneObject sceneObject) {
        VertexBox bounds = new VertexBox(sceneObject, sceneObject.getChildren());
        Vector3f relativePos = bounds.getRelative(mSelfRelativePosition);
        return new Vector3f(mTargetPosition).sub(relativePos);
    }
}
