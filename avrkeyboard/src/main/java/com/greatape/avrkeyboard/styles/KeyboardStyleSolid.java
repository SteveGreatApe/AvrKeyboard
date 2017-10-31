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

import android.graphics.Typeface;

import com.greatape.avrkeyboard.R;

/**
 * @author Steve Townsend
 */
public class KeyboardStyleSolid extends KeyboardStyle {
    public KeyboardStyleSolid() {
        name = "Solid";
        keyStyle.font_size = 120;
        keyStyle.font_typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        keyStyle.text_color = 0xFF00CCFF;
        keyStyle.text_color_hover = 0xFF012E39;
        keyStyle.background.drawable = R.drawable.avrk_key_solid_alt;
        keyStyle.background.tint = 0xFF293a93;
        keyStyle.hover_background.drawable = R.drawable.avrk_key_solid_alt;
        keyStyle.hover_background.tint = 0xFF3274FF;
        keyStyle.depressed_depth = 0.2f;

        background.drawable = R.drawable.avrk_key;// TODO: Seperate background drawable?
        background.tint = 0xFF00C0FF;

        button_scale_x = 1;
        button_scale_y = 1;

        shift_drawable = R.drawable.avr_keyboard_shift;
        return_drawable = R.drawable.avr_keyboard_return;
        space_drawable = R.drawable.avr_keyboard_space;
        delete_drawable = R.drawable.avr_keyboard_delete;
        zwnj_drawable = R.drawable.avr_keyboard_zwnj;
    }
}
