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
package com.greatape.avrkeyboard.util;

/**
 * @author Steve Townsend
 */
public enum ClickLocation {
    Left,
    Right,
    Top,
    Bottom,
    Mid;

    private static final String TAG = "ClickLocation";

    public static ClickLocation classifyClick(float x, float y) {
        final float MinStrength = 0.6f;
        ClickLocation clickLocation;
        float horizStrength = Math.max(x, 1.0f - x);
        float vertStrength = Math.max(y, 1.0f - y);
        if (Math.max(horizStrength, vertStrength) < MinStrength) {
            clickLocation = Mid;
        } else if (horizStrength > vertStrength) {
            clickLocation = x < 0.5f ? Left : Right;
        } else {
            clickLocation = y < 0.5f ? Top : Bottom;
        }
//        Log.d(TAG, "clickLocation:" + clickLocation + " horizStrength:" + horizStrength + " vertStrength:" + vertStrength + " {" + x + "," + y + "}");
        return clickLocation;
    }
}
