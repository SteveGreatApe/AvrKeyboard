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
package com.greatape.avrutils.objects;

import android.util.Log;

import com.greatape.avrkeyboard.util.AvrUtil;

import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.joml.Vector3f;

import java.util.List;

/**
 * @author Steve Townsend
 */
public class VertexBox {
    private final static Vector3f sDefaultRelative = new Vector3f(0.5f, 0.5f, 0.5f);
    private static final String TAG = "VertexBox";

    // TODO: Investigatge how to handle scaled objects
    private final static boolean applyScale = false;

    private Vector3f min = new Vector3f();
    private Vector3f max = new Vector3f();

    public VertexBox(GVRSceneObject object, List<GVRSceneObject> objects) {
        min.x = Float.MAX_VALUE;
        min.y = Float.MAX_VALUE;
        min.z = Float.MAX_VALUE;
        max.x = -Float.MAX_VALUE;
        max.y = -Float.MAX_VALUE;
        max.z = -Float.MAX_VALUE;
        if (objects == null) {
            updateBounds(object, null);
        } else {
            for(GVRSceneObject sceneObject : objects) {
                Vector3f offset = calcOffset(object, sceneObject);
                updateBounds(sceneObject, offset);
            }
        }
    }

    VertexBox(VertexBox vertexBox) {
        min.set(vertexBox.min);
        max.set(vertexBox.max);
    }

    public float getWidth() {
        return max.x - min.x;
    }

    public float getHeight() {
        return max.y - min.y;
    }

    public Vector3f getRelative(Vector3f relativePos) {
        if (relativePos == null) {
            relativePos = sDefaultRelative;
        }
        return new Vector3f(
                getRelative(min.x, max.x, relativePos.x),
                getRelative(min.y, max.y, relativePos.y),
                getRelative(min.z, max.z, relativePos.z));
    }

    private float getRelative(float min, float max, float relative) {
        return min + (max - min) * relative;
    }

    public void scale(float scaleX, float scaleY, float scaleZ) {
        min.x *= scaleX;
        min.y *= scaleY;
        min.z *= scaleZ;
        max.x *= scaleX;
        max.y *= scaleY;
        max.z *= scaleZ;
    }

    public void add(Vector3f pos) {
        min.add(pos);
        max.add(pos);
    }

    public String toString() {
        return "{" + min.x + ", " + min.y + ", " + min.z + "}{" + max.x + ", " + max.y + ", " + max.z + "}";
    }

    public Vector3f getMidPoint() {
        return new Vector3f((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2);
    }

    private static Vector3f calcOffset(GVRSceneObject root, GVRSceneObject object) {
        Vector3f offset = new Vector3f();
        do {
            GVRTransform transform = object.getTransform();
            offset.x += transform.getPositionX();
            offset.y += transform.getPositionY();
            offset.z += transform.getPositionZ();
            if (applyScale) {
                offset.mul(transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());
            }
            object = object.getParent();
        } while(object != root && object != null);
        return offset;
    }

    private void updateBounds(GVRSceneObject object, Vector3f offset) {
        GVRRenderData renderData = object.getRenderData();
        Vector3f[] vertices = renderData != null ? verticesFromMesh(renderData.getMesh()) : new Vector3f[]{new Vector3f()};
        for(Vector3f vertex : vertices) {
            if (applyScale) {
                GVRTransform transform = object.getTransform();
                vertex.mul(transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());
            }
            if (offset != null) {
                vertex.add(offset);
            }
            if (vertex.x < min.x) {
                min.x = vertex.x;
            }
            if (vertex.y < min.y) {
                min.y = vertex.y;
            }
            if (vertex.z < min.z) {
                min.z = vertex.z;
            }
            if (vertex.x > max.x) {
                max.x = vertex.x;
            }
            if (vertex.y > max.y) {
                max.y = vertex.y;
            }
            if (vertex.z > max.z) {
                max.z = vertex.z;
            }
        }
    }

    private static Vector3f[] verticesFromMesh(GVRMesh mesh) {
        Vector3f[] vertexArray = null;
        float[] vertices = AvrUtil.getMeshVertices(mesh);
        if (vertices.length % 3 == 0) {
            vertexArray = new Vector3f[vertices.length / 3];
            int vaIndex = 0;
            for(int index = 0; index < vertices.length; index += 3, vaIndex++) {
                Vector3f vector3f = new Vector3f();
                vector3f.x = vertices[index];
                vector3f.y = vertices[index + 1];
                vector3f.z = vertices[index + 2];
                vertexArray[vaIndex] = vector3f;
            }
        } else {
            Log.e(TAG, "Error: vertex not multiple of 3");
        }
        return vertexArray;
    }
}
