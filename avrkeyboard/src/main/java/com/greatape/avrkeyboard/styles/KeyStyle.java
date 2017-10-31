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
package com.greatape.avrkeyboard.styles;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.util.SparseArray;

import com.greatape.avrkeyboard.util.AvrUtil;
import com.greatape.avrkeyboard.util.NinePatch;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;

/**
 * @author Steve Townsend
 */
public class KeyStyle {
    public DrawableStyle background;
    public DrawableStyle hover_background;
    public float depressed_depth;

    public int font_size;
    public Typeface font_typeface;
    public int text_color;
    public int text_color_hover;

    public int vrToPixelRatio = 272;

    private SparseArray<GVRTexture> mKeyTextures = new SparseArray<>();
    private SparseArray<NinePatchDrawable> mDrawables = new SparseArray<>();

    public KeyStyle() {
        background = new DrawableStyle();
        hover_background = new DrawableStyle();
    }

    public KeyStyle(KeyStyle other, float scale) {
        background = new DrawableStyle(other.background);
        hover_background = new DrawableStyle(other.hover_background);
        depressed_depth = other.depressed_depth * scale;
        font_size = (int)(other.font_size * scale);
        font_typeface = other.font_typeface;
        text_color = other.text_color;
        text_color_hover = other.text_color_hover;
    }

    private NinePatchDrawable ninePatchDrawable(GVRContext gvrContext, int resId) {
        NinePatchDrawable drawable = mDrawables.get(resId);
        if (drawable == null) {
            drawable = (NinePatchDrawable) gvrContext.getContext().getResources().getDrawable(resId, null);
            mDrawables.put(resId, drawable);
        }
        return drawable;
    }

    public GVRTexture gvrTexture(GVRContext gvrContext, float vrWidth, float vrHeight, DrawableStyle drawableStyle) {
        int bitmapWidth = (int)(vrWidth * drawableStyle.pixelQualityRatio);
        int bitmapHeight = (int)(vrHeight * drawableStyle.pixelQualityRatio);
        int hashKey = ((((bitmapWidth * 31) + bitmapHeight) * 31) + drawableStyle.drawable) * 31 + drawableStyle.tint;
        GVRTexture texture = mKeyTextures.get(hashKey);
        if (texture == null) {
            Bitmap bitmap = NinePatch.createBitmap(ninePatchDrawable(gvrContext, drawableStyle.drawable), bitmapWidth, bitmapHeight, drawableStyle.tint);
            texture = AvrUtil.bitmapTexture(gvrContext, bitmap);
            mKeyTextures.put(hashKey, texture);
        }
        return texture;
    }

    public float convertPixelToVRFloatValue(float pixel) {
        return pixel / vrToPixelRatio;
    }

    public int convertVRFloatToPixelValue(float vrFloat) {
        return (int)(vrFloat * vrToPixelRatio);
    }
}
