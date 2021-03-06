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
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.ITouchEvents;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Colors;

/**
 * @author Steve Townsend
 */
public class OuterSphere extends ObjectBase implements ITouchEvents {
    private int mColorIndex;
    private int[] mColors;

    public OuterSphere(GVRContext gvrContext, float radius) {
        mSceneObject = new GVRSphereSceneObject(gvrContext, false, radius);
        GVRRenderData renderData = mSceneObject.getRenderData();
        mMaterial = new GVRMaterial(gvrContext, new GVRShaderId(GVRPhongShader.class));
        renderData.setMaterial(mMaterial);
        renderData.setAlphaBlend(true);
        renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        mSceneObject.getEventReceiver().addListener(this);
        mSceneObject.attachCollider(new GVRMeshCollider(gvrContext, true));
    }

    public void setColors(int[] colors) {
        mColors = colors;
        setColor(mColors[mColorIndex]);
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
    }

    @Override
    public void onTouch(GVRSceneObject sceneObject, MotionEvent motionEvent, float[] hitLocation) {
        final int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                cycleColor();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                break;
        }
    }
}
