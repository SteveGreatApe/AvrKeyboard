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
package com.greatape.avrutils;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * @author Steve Townsend
 */
public class VrMath {
    public static Vector2f fromPitchAndYaw2(double pitch, double yaw) {
        float sinYaw = (float) Math.sin(yaw);
        float cosYaw = (float) Math.cos(yaw);
        float cosPitch = (float) Math.cos(pitch);
        return new Vector2f(cosPitch * cosYaw, sinYaw);
    }

    public static Vector3f fromPitchAndYaw3(double pitch, double yaw) {
        float sinYaw = (float) Math.sin(yaw);
        float cosYaw = (float) Math.cos(yaw);
        float sinPitch = (float) Math.sin(pitch);
        float cosPitch = (float) Math.cos(pitch);
        return new Vector3f(cosPitch * cosYaw, sinYaw, sinPitch * cosYaw);
    }

    public static float angleFromPitchAndYaw(double pitch, double yaw) {
        float sinYaw = (float) Math.sin(yaw);
        float cosYaw = (float) Math.cos(yaw);
        float cosPitch = (float) Math.cos(pitch);
        float x = cosPitch * cosYaw;
        float y = sinYaw;
        return (float) Math.atan2(y, x);
    }

    public static double normalisedAngle(double angle) {
        while(angle >= Math.PI) {
            angle -= Math.PI * 2;
        }
        while(angle < -Math.PI) {
            angle += Math.PI * 2;
        }
        return angle;
    }
}
