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
import android.graphics.Color;
import android.os.Handler;

import com.greatape.avrkeyboard.debug.AvrDebugUtils;

import org.gearvrf.BuildConfig;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.utility.Colors;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

/**
 * @author Steve Townsend
 */
public class AvrUtil {
    private static final String TAG = "AvrUtil";
    // Can't get this working with ambient lighting
    private static final boolean sUseTextureShader = false;
    private static Handler sHandler;

    public static void init() {
        sHandler = new Handler();
    }

    public static void offsetMesh(GVRSceneObject sceneObject, float xOffset, float yOffset, float zOffset) {
        offsetMesh(sceneObject.getRenderData().getMesh(), xOffset, yOffset, zOffset);
    }

    public static void offsetMesh(GVRMesh mesh, float xOffset, float yOffset, float zOffset) {
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

    private static GVRMaterial getOrCreateRenderDataAndMaterial(GVRSceneObject sceneObject, GVRShaderId shaderId) {
        GVRMaterial material;
        GVRRenderData renderData = sceneObject.getRenderData();
        if (renderData == null) {
            material = new GVRMaterial(sceneObject.getGVRContext(), shaderId);
            renderData = new GVRRenderData(sceneObject.getGVRContext(), material);
            sceneObject.attachRenderData(renderData);
        } else {
            material = renderData.getMaterial();
            if (material == null) {
                material = new GVRMaterial(sceneObject.getGVRContext(), shaderId);
                renderData.setMaterial(material);
            } else if (BuildConfig.DEBUG) {
                AvrDebugUtils.assertEquals(shaderId, material.getShaderType());
            }
        }
        return material;
    }

    public static void setDiffuseAndAmbientColors(GVRSceneObject sceneObject, int color) {
        setDiffuseColor(sceneObject, color);
        setAmbientColor(sceneObject, color);
    }

    public static void setDiffuseAndAmbientColors(GVRSceneObject sceneObject, int color, GVRShaderId shaderId) {
        setDiffuseColor(sceneObject, color, shaderId);
        setAmbientColor(sceneObject, color, shaderId);
    }

    public static void setDiffuseColor(GVRSceneObject sceneObject, int color) {
        setDiffuseColor(sceneObject, color, GVRMaterial.GVRShaderType.Phong.ID);
    }

    public static void setAmbientColor(GVRSceneObject sceneObject, int color) {
        setAmbientColor(sceneObject, color, GVRMaterial.GVRShaderType.Phong.ID);
    }

    public static void setDiffuseColor(GVRSceneObject sceneObject, int color, GVRShaderId shaderId) {
        setDiffuseColor(getOrCreateRenderDataAndMaterial(sceneObject, shaderId), color);
    }

    public static void setDiffuseColor(GVRMaterial material, int color) {
        material.setDiffuseColor(
                Colors.byteToGl(Color.red(color)),
                Colors.byteToGl(Color.green(color)),
                Colors.byteToGl(Color.blue(color)),
                Colors.byteToGl(Color.alpha(color)));
    }

    public static void setAmbientColor(GVRSceneObject sceneObject, int color, GVRShaderId shaderId) {
        GVRMaterial material = getOrCreateRenderDataAndMaterial(sceneObject, shaderId);
        material.setAmbientColor(
                Colors.byteToGl(Color.red(color)),
                Colors.byteToGl(Color.green(color)),
                Colors.byteToGl(Color.blue(color)),
                Colors.byteToGl(Color.alpha(color)));
    }

    public static void setSpecularColor(GVRSceneObject sceneObject, int color) {
        GVRMaterial material = getOrCreateRenderDataAndMaterial(sceneObject, GVRMaterial.GVRShaderType.Phong.ID);
        material.setSpecularColor(
                Colors.byteToGl(Color.red(color)),
                Colors.byteToGl(Color.green(color)),
                Colors.byteToGl(Color.blue(color)),
                Colors.byteToGl(Color.alpha(color)));
    }

    public static void setEmissiveColor(GVRSceneObject sceneObject, int color) {
        GVRMaterial material = getOrCreateRenderDataAndMaterial(sceneObject, GVRMaterial.GVRShaderType.Phong.ID);
        material.setVec4("emissive_color",
                Colors.byteToGl(Color.red(color)),
                Colors.byteToGl(Color.green(color)),
                Colors.byteToGl(Color.blue(color)),
                Colors.byteToGl(Color.alpha(color)));
    }

    public static GVRMaterial createColorMaterial(GVRContext gvrContext) {
        return new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
    }

    public static GVRMaterial createTextureMaterial(GVRContext gvrContext) {
        GVRMaterial material;
        if (sUseTextureShader) {
            material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Texture.ID);
            material.setVec4("u_color", 1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
            material.setVec4("ambient_color", 1.0f, 1.0f, 1.0f, 1.0f);
        }
        material.setVec4("diffuse_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("specular_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        return material;
    }

    public static void setDiffuseAndAmbientTextures(GVRMaterial material, GVRTexture texture) {
        material.setTexture("diffuseTexture", texture);
        if (sUseTextureShader) {
            material.setTexture("u_texture", texture);
        } else {
            material.setTexture("ambientTexture", texture);
        }
    }

    public static void setLightingEnabled(GVRSceneObject sceneObject, boolean enabled) {
        GVRRenderData renderData = sceneObject.getRenderData();
        if (enabled) {
            renderData.enableLight();
        } else {
            renderData.disableLight();
        }
    }

    public static Handler Handler() {
        return sHandler;
    }
}
