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

import android.graphics.Color;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Colors;

import static android.view.MotionEvent.BUTTON_SECONDARY;

/**
 * @author Steve Townsend
 */
public class OuterSphere extends ObjectBase implements ITouchEvents {
    private int mColorIndex;
    private int[] mColors;
    private int[] mAmbientLights;
    private int[] mDiffuseLights;
    private GVRSceneObject mLightObject;
    private GVRPointLight mLight;

    public OuterSphere(GVRContext gvrContext, float radius) {
        mSceneObject = new GVRSphereSceneObject(gvrContext, false, radius);
        GVRRenderData renderData = mSceneObject.getRenderData();
        mMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
        renderData.setMaterial(mMaterial);
        renderData.setAlphaBlend(true);
        renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        mSceneObject.setName("OuterSphere");
        mSceneObject.getEventReceiver().addListener(this);
        mSceneObject.attachCollider(new GVRMeshCollider(gvrContext, true));

        mLightObject = new GVRSceneObject(gvrContext);
        mLightObject.getTransform().setPositionY(radius / 2f);
        mSceneObject.addChildObject(mLightObject);
    }

    public void setColors(int[] colors) {
        mColors = colors;
        setColor(mColors[mColorIndex]);
    }

    public void setLights(int[] ambientLights, int[] diffuseLights) {
        mAmbientLights = ambientLights;
        mDiffuseLights = diffuseLights;
        setLights(mAmbientLights[mColorIndex], mDiffuseLights[mColorIndex]);
    }

    private void setLights(int ambientLight, int diffuseLight) {
        if (ambientLight == 0 && diffuseLight == 0) {
            if (mLight != null) {
                mLightObject.detachLight();
                mLight = null;
            }
        } else {
            if (mLight == null) {
                mLight = new GVRPointLight(mLightObject.getGVRContext());
                mLightObject.attachLight(mLight);
            }
            mLight.setAmbientIntensity(
                    Colors.byteToGl(Color.red(ambientLight)),
                    Colors.byteToGl(Color.green(ambientLight)),
                    Colors.byteToGl(Color.blue(ambientLight)),
                    Colors.byteToGl(Color.alpha(ambientLight)));
            mLight.setDiffuseIntensity(
                    Colors.byteToGl(Color.red(diffuseLight)),
                    Colors.byteToGl(Color.green(diffuseLight)),
                    Colors.byteToGl(Color.blue(diffuseLight)),
                    Colors.byteToGl(Color.alpha(diffuseLight)));
        }
    }

    public void setColor(int color) {
        mMaterial.setDiffuseColor(
                Colors.byteToGl(Color.red(color)),
                Colors.byteToGl(Color.green(color)),
                Colors.byteToGl(Color.blue(color)),
                Colors.byteToGl(Color.alpha(color)));
    }

    private void cycleColor() {
        mColorIndex = (mColorIndex + 1) % mColors.length;
        setColor(mColors[mColorIndex]);
        setLights(mAmbientLights[mColorIndex], mDiffuseLights[mColorIndex]);
    }

    @Override
    public void onEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onTouchEnd(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        if (gvrPickedObject.motionEvent != null && gvrPickedObject.motionEvent.getButtonState() == BUTTON_SECONDARY) {
            cycleColor();
        }
    }

    @Override
    public void onInside(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {
    }
}
