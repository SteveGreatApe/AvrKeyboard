package com.greatape.avrkeyboard.debug;

import android.graphics.PointF;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTransform;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

public class AvrDebugUtils {
    private static final String TAG = "AvrDebugUtils";

    private static int sHeapDumpIndex = 1;

    public static void HeapDump() {
        Log.d(TAG, "Dumping heap");
//        System.gc();
        try {
            File external = Environment.getExternalStorageDirectory();
            File folder = new File(external, "Documents");
            String fileName = String.format(Locale.ENGLISH, "oom%02d.hprof", sHeapDumpIndex++);
            File heapDumpFile1 = new File(folder, fileName);
            Debug.dumpHprofData(heapDumpFile1.getPath());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "Finished heap dump");
    }

    public static String format(float coord) {
        return Float.toString(coord);
    }

    public static String format(float[] coords) {
        if (coords == null) {
            return "null";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(float coord : coords) {
            stringBuilder.append(stringBuilder.length() == 0 ? "{" : ", ");
            stringBuilder.append(format(coord));
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static String format(PointF pointF) {
        if (pointF == null) {
            return "null";
        }
        return format2f(pointF.x, pointF.y);
    }

    public static String format(Vector2f vector2f) {
        if (vector2f == null) {
            return "null";
        }
        return format2f(vector2f.x, vector2f.y);
    }

    private static String format2f(float x, float y) {
        return "{" +
                format(x) +
                ", " +
                format(y) +
                '}';
    }

    public static String format(Vector3f vector3f) {
        if (vector3f == null) {
            return "null";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');
        stringBuilder.append(format(vector3f.x));
        stringBuilder.append(", ");
        stringBuilder.append(format(vector3f.y));
        stringBuilder.append(", ");
        stringBuilder.append(format(vector3f.z));
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public static String formatPos(GVRTransform transform) {
        if (transform == null) {
            return "null";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append(format(transform.getPositionX()));
        stringBuilder.append(", ");
        stringBuilder.append(format(transform.getPositionY()));
        stringBuilder.append(", ");
        stringBuilder.append(format(transform.getPositionZ()));
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static void logMaterial(String tag, GVRMaterial material) {
        StringBuilder stringBuilder = new StringBuilder();
        String ud = material.getUniformDescriptor();
        String[] descs = ud.split("; ");
        for(String desc : descs) {
            String[] typeValue = desc.split(" ");
            if (typeValue.length == 2) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("; ");
                }
                String name = typeValue[1];
                stringBuilder.append(name);
                stringBuilder.append('=');
                try {
                    float[] floatArray = null;
                    switch (typeValue[0]) {
                        case "float2":
                        case "float3":
                        case "float4":
                            floatArray = material.getFloatVec(name);
                            break;
                        case "float":
                            floatArray = new float[]{material.getFloat(name)};
                            break;
                        default:
                            Log.w(tag, "Error unknown uniform type: " + typeValue[0]);
                            break;
                    }
                    if (floatArray != null) {
                        stringBuilder.append(AvrDebugUtils.format(floatArray));
                    }
                } catch (IllegalArgumentException e) {
                    stringBuilder.append("<Empty>");
                }
            }
        }
        Log.d(tag, stringBuilder.toString());
    }

    public static void assertEquals(GVRShaderId shader1, GVRShaderId shader2) {
        try {
            Field idField = GVRShaderId.class.getDeclaredField("ID");
            idField.setAccessible(true);
            Object id1 = idField.get(shader1);
            Object id2 = idField.get(shader2);
            if (!id1.equals(id2)) {
                String error = "Error wrong shader type, expecting: " + id1 + ", Found: " + id2;
                Log.e(TAG, error);
                throw new RuntimeException(error);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
