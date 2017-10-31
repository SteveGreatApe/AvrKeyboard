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
package com.greatape.avrutils.objects;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author Steve Townsend
 */
public class LayoutLink {
    private WeakReference<LinkOwner> mOwner;
    private Vector3f mSelfRelative;
    private Vector3f mLinkRelative;
    private Vector3f mExtraOffset;

    public interface LinkOwner {
        GVRSceneObject getSceneObject();
        List<GVRSceneObject> getBoundsObjects();
        void positionUpdated();
    }

    public interface LinkParent {
        void sizeUpdated();
    }

    public LayoutLink(LinkOwner owner) {
        mOwner = new WeakReference<>(owner);
    }

    void updateLinkPosition(VertexBox boundingBox) {
        Vector3f pos = boundingBox.getRelative(mLinkRelative);
        setRelativePos(pos);
    }

    private void setRelativePos(Vector3f pos) {
        LinkOwner owner = mOwner.get();
        if (owner == null) {
            return;
        }
        GVRSceneObject ownerSceneObject = owner.getSceneObject();
        VertexBox ownerBoundingBox = new VertexBox(ownerSceneObject, owner.getBoundsObjects());
        Vector3f selfAttachPos = ownerBoundingBox.getRelative(mSelfRelative);
        pos.sub(selfAttachPos);
        if (mExtraOffset != null) {
            pos.add(mExtraOffset);
        }
        GVRTransform transform = ownerSceneObject.getTransform();
        if (pos.x != transform.getPositionX() || pos.y != transform.getPositionY() || pos.z != transform.getPositionZ()) {
            transform.setPosition(pos.x, pos.y, pos.z);
            owner.positionUpdated();
        }
    }

    public void setSelfRelative(Vector3f selfRelative) {
        mSelfRelative = selfRelative;
    }

    public void setLinkRelative(Vector3f linkRelative) {
        mLinkRelative = linkRelative;
    }

    public void setExtraOffset(Vector3f extraOffset) {
        mExtraOffset = extraOffset;
    }

    GVRSceneObject getOwner() {
        LinkOwner owner = mOwner.get();
        return owner != null ? owner.getSceneObject() : null;
    }
}
