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

/**
 * @author Steve Townsend
 */
public class DrawableStyle {
    public final static int DefaultPixelQualityRatio = 400;

    public int pixelQualityRatio;
    public int drawable;
    public int tint;

    DrawableStyle() {
        this.pixelQualityRatio = DefaultPixelQualityRatio;
    }

    public DrawableStyle(DrawableStyle other) {
        pixelQualityRatio = other.pixelQualityRatio;
        drawable = other.drawable;
        tint = other.tint;
    }
}
