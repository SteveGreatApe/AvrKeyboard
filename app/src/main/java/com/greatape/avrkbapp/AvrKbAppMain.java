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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;

/**
 * @author Steve Townsend
 */
public class AvrKbAppMain extends GVRMain {
    private GVRContext mGVRContext;
    private GVRScene mMainScene;
    private AvrKbAppScene mAvrKbAppScene;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);
        mGVRContext = gvrContext;
        mMainScene = gvrContext.getMainScene();
        mAvrKbAppScene = new AvrKbAppScene(this);
        mAvrKbAppScene.resume(gvrContext);
    }
}
