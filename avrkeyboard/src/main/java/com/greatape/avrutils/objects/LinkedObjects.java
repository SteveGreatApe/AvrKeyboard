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

import com.greatape.avrkeyboard.util.AvrUtil;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Steve Townsend
 */
public class LinkedObjects {
    private List<LayoutLink> mLinks = new ArrayList<>();
    private Linkable mOwner;
    private VertexBox mBoundingBox;

    public interface Linkable {
        void addLink(LayoutLink layoutLink);
        GVRSceneObject getSceneObject();
        List<GVRSceneObject> getLinkToBoundsObjects();
    }

    public LinkedObjects(Linkable owner) {
        mOwner = owner;
    }

    public void addLink(LayoutLink layoutLink) {
        mLinks.add(layoutLink);
        if (mBoundingBox != null) {
            VertexBox offsetBounds = adjustBoundsForCommonParent(layoutLink);
            layoutLink.updateLinkPosition(offsetBounds);
        }
    }

    private GVRSceneObject commonParent(LayoutLink layoutLink) {
        GVRSceneObject linkObject = layoutLink.getOwner();
        GVRSceneObject ownerParentObject = mOwner.getSceneObject();
        do {
            GVRSceneObject linkParentObject = linkObject;
            do {
                if (linkParentObject == ownerParentObject) {
                    return linkParentObject;
                }
                linkParentObject = linkParentObject.getParent();
            } while(linkParentObject != null);
            ownerParentObject = ownerParentObject.getParent();
        } while(ownerParentObject != null);
        return null;
    }

    public void positionUpdated() {
        mBoundingBox = new VertexBox(mOwner.getSceneObject(), mOwner.getLinkToBoundsObjects());
        Iterator<LayoutLink> iter = mLinks.iterator();
        while(iter.hasNext()) {
            LayoutLink layoutLink = iter.next();
            if (layoutLink.getOwner() == null) {
                iter.remove();
            } else {
                VertexBox offsetBounds = adjustBoundsForCommonParent(layoutLink);
                layoutLink.updateLinkPosition(offsetBounds);
            }
        }
    }

    private VertexBox adjustBoundsForCommonParent(LayoutLink layoutLink) {
        VertexBox offsetBounds = new VertexBox(mBoundingBox);
        GVRSceneObject commonParent = commonParent(layoutLink);
        GVRSceneObject parentObject = mOwner.getSceneObject();
        while(parentObject != commonParent) {
            GVRTransform transform = parentObject.getTransform();
            offsetBounds.add(AvrUtil.vectorFromPosition(transform));
            parentObject = parentObject.getParent();
        }
        return offsetBounds;
    }
}
