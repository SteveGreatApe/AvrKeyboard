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

import com.greatape.avrkeyboard.util.AvrUtil;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import java.io.IOException;

import static android.view.MotionEvent.BUTTON_SECONDARY;
import static org.gearvrf.GVREventListeners.TouchEvents;

/**
 * @author Steve Townsend
 */
public class FloorObject extends ObjectBase {
    private int mFloorIndex;
    private String[] mFloorTextureAssets;

    public FloorObject(GVRContext gvrContext, float floorSize, String[] textureAssets) {
        super();
        mFloorTextureAssets = textureAssets;
        mSceneObject = new GVRSceneObject(gvrContext, gvrContext.createQuad(floorSize, floorSize), AvrUtil.createTextureMaterial(gvrContext));
        GVRRenderData renderData = mSceneObject.getRenderData();
        renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND + 10);
        setFloorTexture();
        mSceneObject.setName("FloorObject");
        mSceneObject.getEventReceiver().addListener(new TouchEvents() {
            @Override
            public void onTouchEnd(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
                if (gvrPickedObject.motionEvent != null && gvrPickedObject.motionEvent.getButtonState() == BUTTON_SECONDARY) {
                    cycleFloor();
                }
            }
        });
        mSceneObject.attachCollider(new GVRMeshCollider(gvrContext, true));
    }

    private void cycleFloor() {
        mFloorIndex = (mFloorIndex + 1) % mFloorTextureAssets.length;
        setFloorTexture();
    }

    private void setFloorTexture() {
        try {
            GVRContext gvrContext = mSceneObject.getGVRContext();
            GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, mFloorTextureAssets[mFloorIndex]));
            AvrUtil.setDiffuseAndAmbientTextures(mSceneObject.getRenderData().getMaterial(), texture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
