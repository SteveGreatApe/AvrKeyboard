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

import android.os.Debug;
import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import java.io.File;
import java.io.IOException;

/**
 * @author Steve Townsend
 */
public class AvrTestBase {
    private static final String TAG = "AvrTestBase";

    static GVRActivity mActivity;
    static GVRContext mGVRContext;

    @ClassRule
    public static ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void setUpClass() {
        mActivity = mActivityRule.getActivity();
        mGVRContext = mActivity.getGVRContext();
        try {
            while(mGVRContext.getMainScene() == null) {
                Thread.sleep(1000);
                Log.d(TAG, "Waiting for surface to be created, please make sure headset is awake");
            }
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void HeapDump() {
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
