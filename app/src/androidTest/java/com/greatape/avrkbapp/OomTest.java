/* Copyright 2018 Great Ape Software Ltd
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

import android.graphics.Bitmap;
import android.os.Debug;
import android.os.Environment;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.fail;

public class OomTest {
    private static final String TAG = "OomTest";

    private GVRActivity mActivity;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
        GVRContext gvrContext = mActivity.getGVRContext();
        while(gvrContext.getMainScene() == null) {
            try {
                Thread.sleep(1000);
                Log.d(TAG, "Waiting for surface to be created, please make sure headset is awake");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @UiThreadTest
    public void gcOomTest() throws Exception {
        //  Change this between true & false to either trigger an OutOfMemoryError exception or a
        // "global reference table overflow" crash.
        final boolean createBitmap = true;
        try {
            final int MaxInstances = 100000;
            GVRContext gvrContext = mActivity.getGVRContext();
            for (int count = 0; count < MaxInstances; count++) {
                Log.d(TAG, "Count: " + count);
                GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, gvrContext.createQuad(10f, 10f));
                GVRRenderData renderData = sceneObject.getRenderData();
                GVRTexture texture;
                if (createBitmap) {
                    Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
                    texture = new GVRTexture(gvrContext);
                    texture.setImage(new GVRBitmapTexture(gvrContext, bitmap));
                } else {
                    texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, "textures/Floor2.png"));
                }
                renderData.getMaterial().setMainTexture(texture);
                renderData.setAlphaBlend(true);
                if ((count % 100) == 99) {
                    System.gc();
                    System.runFinalization();
                }
            }
        } catch (OutOfMemoryError oom) {
            HeapDump();
            fail(oom.getMessage());
        }
    }

    private void HeapDump() {
        Log.d(TAG, "Dumping heap");
        try {
            File external = Environment.getExternalStorageDirectory();
            File folder = new File(external, "Documents");
            File heapDumpFile1 = new File(folder, "oom.hprof");
            Debug.dumpHprofData(heapDumpFile1.getPath());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "Finished heap dump");
    }
}
