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

import android.graphics.Bitmap;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.GVRVertexBuffer;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

/**
 * @author Steve Townsend
 */
public class AvrUtil {
    public static void offsetQuad(GVRMesh mesh, float xOffset, float yOffset, float zOffset) {
        float[] vertices = getMeshVertices(mesh);
        for(int index = 0; index < vertices.length; index += 3) {
            vertices[index] += xOffset;
            vertices[index + 1] += yOffset;
            vertices[index + 2] += zOffset;
        }
        mesh.setVertices(vertices);
    }

    public static Vector3f vectorFromPosition(GVRTransform transform) {
        return new Vector3f(transform.getPositionX(), transform.getPositionY(), transform.getPositionZ());
    }

    public static float[] getMeshVertices(GVRMesh mesh) {
        // TODO: For 4.0 use this alternative, delete once onto an official build with a fix
        GVRVertexBuffer vertexBuffer = mesh.getVertexBuffer();
        FloatBuffer floatBuffer = vertexBuffer.getFloatVec("a_position");
        int count = floatBuffer.remaining();
        float[] vertices = new float[count];
        floatBuffer.get(vertices);
        // For 3.3 this still works, should also be fixed post 4.0
//        float[] vertices = mesh.getVertices(); // Throws UnsupportedOperationException in 4.0
        return vertices;
    }

    public static GVRTexture bitmapTexture(GVRContext gvrContext, Bitmap bitmap) {
        GVRTexture gvrTexture = null;
        if (bitmap != null) {
            gvrTexture = new GVRTexture(gvrContext);
            gvrTexture.setImage(new GVRBitmapTexture(gvrContext, bitmap));
        }
        return gvrTexture;
    }
}
