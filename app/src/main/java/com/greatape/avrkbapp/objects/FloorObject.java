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

import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.ITouchEvents;

import java.io.IOException;

/**
 * @author Steve Townsend
 */
public class FloorObject extends ObjectBase implements ITouchEvents {
    private int mFloorIndex;
    private String[] mFloorTextureAssets;

    public FloorObject(GVRContext gvrContext, float floorSize, String[] textureAssets) {
        super();
        mFloorTextureAssets = textureAssets;
        mSceneObject = new GVRSceneObject(gvrContext, floorSize, floorSize);
        GVRRenderData renderData = mSceneObject.getRenderData();
        renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND + 10);
        setFloorTexture();
        mSceneObject.getEventReceiver().addListener(this);
        mSceneObject.attachCollider(new GVRMeshCollider(gvrContext, true));
    }

    @Override
    public void onTouch(GVRSceneObject sceneObject, MotionEvent motionEvent, float[] hitLocation) {
        final int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                cycleFloor();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                break;
        }
    }

    private void cycleFloor() {
        mFloorIndex = (mFloorIndex + 1) % mFloorTextureAssets.length;
        setFloorTexture();
    }

    private void setFloorTexture() {
        try {
            GVRContext gvrContext = mSceneObject.getGVRContext();
            GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, mFloorTextureAssets[mFloorIndex]));
            mSceneObject.getRenderData().getMaterial().setMainTexture(texture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
