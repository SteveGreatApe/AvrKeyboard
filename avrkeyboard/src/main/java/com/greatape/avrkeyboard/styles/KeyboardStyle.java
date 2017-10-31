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
public abstract class KeyboardStyle {
    public String name;

    public KeyStyle keyStyle = new KeyStyle();

    public DrawableStyle background = new DrawableStyle();

    public float button_scale_x;
    public float button_scale_y;

    public int shift_drawable;
    public int return_drawable;
    public int space_drawable;
    public int delete_drawable;
    public int zwnj_drawable;
    public float left_margin = 0.25f;
    public float right_margin = 0.25f;
    public float top_margin = 0.1f;
    public float bottom_margin = 0.1f;
}