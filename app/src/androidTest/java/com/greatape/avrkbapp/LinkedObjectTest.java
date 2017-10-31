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
package com.greatape.avrkbapp;

import android.support.test.runner.AndroidJUnit4;

import com.greatape.avrutils.objects.LayoutLink;
import com.greatape.avrutils.objects.LinkedObjects;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.joml.Vector3f;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DecimalFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Steve Townsend
 */
@RunWith(AndroidJUnit4.class)
public class LinkedObjectTest extends AvrTestBase {
    private final static Vector3f sV3f123 = new Vector3f(0.1f, 0.2f, 0.3f);
    private final static Vector3f sV3f642 = new Vector3f(0.6f, 0.4f, 0.2f);
    private final static Vector3f sV3f000 = new Vector3f(0.0f, 0.0f, 0.0f);
    private final static Vector3f sV3f555 = new Vector3f(0.5f, 0.5f, 0.5f);
    private final static Vector3f sV3f111 = new Vector3f(1.0f, 1.0f, 1.0f);

    @Test
    public void test01SimpleTest() throws Exception {
        // Context of the app under test.
        GVRSceneObject parent = new GVRSceneObject(mGVRContext);
        LinkTestInstance linkTestInstance = new LinkTestInstance(parent);
        LinkTestObject linkToTarget = new LinkTestObject(parent, 1.0f, 2.0f);
        LinkTestObject linkedObject = new LinkTestObject(parent, 3.0f, 5.0f, sV3f123, sV3f642);
        linkToTarget.setPosition(10.0f, 12.0f, 0.0f);
        linkToTarget.addLink(linkedObject.fieldSetLink());
        linkTestInstance.verify(linkToTarget, linkedObject);
        linkToTarget.setPosition(-5.0f, 1.0f, -3.0f);
        linkTestInstance.verify(linkToTarget, linkedObject);
    }

    @Test
    public void test02DoubleLevelLink() throws Exception {
        GVRSceneObject parent = new GVRSceneObject(mGVRContext);
        LinkTestInstance linkTestInstance = new LinkTestInstance(parent);
        LinkTestObject linkToTarget = new LinkTestObject(parent, 1.0f, 2.0f);
        LinkTestObject extraLink = new LinkTestObject(parent, 0.2f, 0.4f, sV3f000, sV3f111);
        LinkTestObject linkedObject = (new LinkTestObject(extraLink.getSceneObject(), 3.0f, 5.0f, sV3f123, sV3f642));
        linkToTarget.setPosition(10.0f, 12.0f, 0.0f);
        extraLink.addLink(linkedObject.fieldSetLink());
        linkToTarget.addLink(extraLink.fieldSetLink());
        linkTestInstance.verify(linkToTarget, extraLink);
        linkTestInstance.verify(extraLink, linkedObject);
        linkToTarget.setPosition(-5.0f, 1.0f, -3.0f);
        linkTestInstance.verify(linkToTarget, extraLink);
        linkTestInstance.verify(extraLink, linkedObject);
    }

    @Test
    public void test00HandCalculated() throws Exception {
        GVRSceneObject parent = new GVRSceneObject(mGVRContext);
        LinkTestInstance linkTestInstance = new LinkTestInstance(parent);
        LinkTestObject linkToTarget = new LinkTestObject(parent, 4.0f, 4.0f);
        LinkTestObject extraLink = new LinkTestObject(parent, 2.0f, 2.0f, sV3f000, sV3f111);
        LinkTestObject linkedObject = (new LinkTestObject(extraLink.getSceneObject(), 1.0f, 1.0f, sV3f000, sV3f111));
        Vector3f basePosition = new Vector3f(10.0f, 12.0f, 0.0f);
        linkToTarget.setPosition(basePosition);
        extraLink.addLink(linkedObject.fieldSetLink());
        linkToTarget.addLink(extraLink.fieldSetLink());
        Vector3f expected = new Vector3f(2f + 1f, 2f + 1f, 0f).add(basePosition);
        linkTestInstance.assertPosition(extraLink, expected);
        Vector3f expected2 = new Vector3f(1 + 0.5f, 1 + 0.5f, 0f).add(expected);
        linkTestInstance.assertPosition(linkedObject, expected2);
        // Also check with auto verification calculations
        linkTestInstance.verify(linkToTarget, extraLink);
        linkTestInstance.verify(extraLink, linkedObject);

        // Move the base position and check all updates okay
//        basePosition = new Vector3f(-5.0f, 1.0f, -3.0f);
        linkToTarget.setPosition(basePosition);
        expected = new Vector3f(2f + 1f, 2f + 1f, 0f).add(basePosition);
        linkTestInstance.assertPosition(extraLink, expected);
        expected2 = new Vector3f(1 + 0.5f, 1 + 0.5f, 0f).add(expected);
        linkTestInstance.assertPosition(linkedObject, expected2);
        // Also check with auto verification calculations
        linkTestInstance.verify(linkToTarget, extraLink);
        linkTestInstance.verify(extraLink, linkedObject);
    }

    @Test
    public void test03SiblingLink() throws Exception {
        GVRSceneObject parent = new GVRSceneObject(mGVRContext);
        LinkTestInstance linkTestInstance = new LinkTestInstance(parent);
        LinkTestObject linkToTarget = new LinkTestObject(parent, 1.0f, 2.0f);
        LinkTestObject linkedObject = new LinkTestObject(parent, 3.0f, 5.0f, sV3f123, sV3f642);
        LinkTestObject siblingLinkedObject = new LinkTestObject(parent, 3.0f, 5.0f, sV3f123, sV3f642);
        linkToTarget.setPosition(10.0f, 12.0f, 0.0f);
        linkToTarget.addLink(linkedObject.fieldSetLink());
        linkedObject.addLink(siblingLinkedObject.fieldSetLink());
        linkTestInstance.verify(linkToTarget, linkedObject);
        linkTestInstance.verify(linkedObject, siblingLinkedObject);
        linkToTarget.setPosition(-5.0f, 1.0f, -3.0f);
        linkTestInstance.verify(linkToTarget, linkedObject);
        linkTestInstance.verify(linkedObject, siblingLinkedObject);
    }

    // @Test TODO: Disabled for now, dealing with scaled objects needs more investigation
    public void test04ScaleTest() throws Exception {
        GVRSceneObject parent = new GVRSceneObject(mGVRContext);
        LinkTestInstance linkTestInstance = new LinkTestInstance(parent);
        LinkTestObject linkToTarget = new LinkTestObject(parent, 1.0f, 2.0f);
        LinkTestObject linkedObject = new LinkTestObject(parent, 3.0f, 5.0f, sV3f111, sV3f111);
        Vector3f basePosition = new Vector3f(1.0f, 2.0f, 3.0f);
        linkToTarget.setPosition(basePosition);
        linkToTarget.addLink(linkedObject.fieldSetLink());
        Vector3f expected = new Vector3f(1f/2 - 3f/2, 2f/2 - 5f/2, 0f).add(basePosition);
        linkTestInstance.assertPosition(linkedObject, expected);
        // Now tweak the scale of the object we are attaching
        linkedObject.getSceneObject().getTransform().setScaleX(0.5f);
        linkToTarget.setPosition(basePosition);
        expected.x = basePosition.x + 1f/2 - 3f/4;
        linkTestInstance.assertPosition(linkedObject, expected);
        // Now tweak the scale of the object we are attaching to
        linkToTarget.getSceneObject().getTransform().setScaleX(0.5f);
        linkToTarget.setPosition(basePosition);
        expected.x = basePosition.x + 1f/4 - 3f/4;
        linkTestInstance.assertPosition(linkedObject, expected);
    }

    private class LinkTestInstance {
        private GVRSceneObject mCommonParent;

        LinkTestInstance(GVRSceneObject commonParent) {
            mCommonParent = commonParent;
        }

        void verify(LinkTestObject linkToTarget, LinkTestObject linkedSceneObject) {
            Vector3f expected = linkToTarget.getRelativePosition(mCommonParent);
            expected = expected.add(linkToTarget.relativeOffset(linkedSceneObject.mLinkRelative));
            expected = expected.sub(linkedSceneObject.relativeOffset(linkedSceneObject.mSelfRelative));
            assertPosition(linkedSceneObject, expected);
        }

        private void assertPosition(LinkTestObject linkTestObject, Vector3f expected) {
            Vector3f actual = linkTestObject.getRelativePosition(mCommonParent);
            DecimalFormat formatter = new DecimalFormat(" 0.000");
            String expectedString = expected.toString(formatter);
            String actualString = actual.toString(formatter);
            assertEquals(expectedString, actualString);
        }
    }

    private class LinkTestObject implements LinkedObjects.Linkable, LayoutLink.LinkOwner {
        private GVRSceneObject mSceneObject;
        private LinkedObjects mLinkedObjects;
        private Vector3f mSelfRelative;
        private Vector3f mLinkRelative;
        float width;
        float height;
        float depth;

        private LinkTestObject(GVRSceneObject parent, float width, float height) {
            this.width = width;
            this.height = height;
            // TODO: depth
            mSceneObject = new GVRSceneObject(mGVRContext, width, height);
            mLinkedObjects = new LinkedObjects(this);
            parent.addChildObject(mSceneObject);
        }

        private LinkTestObject(GVRSceneObject parent, float width, float height, Vector3f selfRelative, Vector3f linkRelative) {
            this(parent, width, height);
            mSelfRelative = selfRelative;
            mLinkRelative = linkRelative;
        }

        void setPosition(float x, float y, float z) {
            GVRTransform transform = mSceneObject.getTransform();
            transform.setPosition(x, y, z);
            mLinkedObjects.positionUpdated();
        }

        void setPosition(Vector3f pos) {
            setPosition(pos.x, pos.y, pos.z);
        }

        Vector3f getRelativePosition(GVRSceneObject root) {
            Vector3f relativePos = new Vector3f();
            GVRSceneObject sceneObject = mSceneObject;
            do {
                GVRTransform transform = sceneObject.getTransform();
                relativePos.x += transform.getPositionX();
                relativePos.y += transform.getPositionY();
                relativePos.z += transform.getPositionZ();
                sceneObject = sceneObject.getParent();
            } while(sceneObject != root && sceneObject != null);
            return relativePos;
        }

        Vector3f relativeOffset(Vector3f relativeOffset) {
            return new Vector3f(width * (relativeOffset.x - 0.5f),
                    height * (relativeOffset.y - 0.5f),
                    depth * (relativeOffset.y - 0.5f));
        }

        @Override
        public void addLink(LayoutLink layoutLink) {
            mLinkedObjects.addLink(layoutLink);
        }

        @Override
        public GVRSceneObject getSceneObject() {
            return mSceneObject;
        }

        @Override
        public List<GVRSceneObject> getBoundsObjects() {
            return null;
        }

        @Override
        public void positionUpdated() {
            mLinkedObjects.positionUpdated();
        }

        @Override
        public List<GVRSceneObject> getLinkToBoundsObjects() {
            return null;
        }

        LayoutLink fieldSetLink() {
            LayoutLink fieldSetLink = new LayoutLink(this);
            fieldSetLink.setSelfRelative(mSelfRelative);
            fieldSetLink.setLinkRelative(mLinkRelative);
            return fieldSetLink;
        }
    }
}
