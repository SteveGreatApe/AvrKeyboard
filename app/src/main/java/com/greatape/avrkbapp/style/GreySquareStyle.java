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
package com.greatape.avrkbapp.style;

import android.graphics.Color;
import android.graphics.Typeface;

import com.greatape.avrkbapp.R;
import com.greatape.avrkeyboard.styles.KeyStyle;
import com.greatape.avrkeyboard.styles.KeyboardStyle;
import com.greatape.avrkeyboard.styles.TextFieldStyle;

/**
 * @author Steve Townsend
 */
public class GreySquareStyle {
    private final static int MainColor = 0xFF808080;
    private final static int HoverTextColor = 0xFF000000;

    public static class Keyboard extends KeyboardStyle {

        public Keyboard() {
            name = "GreySquareStyle";
            keyStyle.font_size = 120;
            keyStyle.font_typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD);
            keyStyle.text_color = MainColor;
            keyStyle.text_color_hover = HoverTextColor;

            keyStyle.background.drawable = R.drawable.avrk_square_key;
            keyStyle.background.tint = MainColor;
            keyStyle.hover_background.drawable = R.drawable.avrk_square_key_hover;
            keyStyle.hover_background.tint = 0x80FFFFFF;
            keyStyle.depressed_depth = 0.2f;

            background.drawable = R.drawable.avrk_custom_background;
            background.tint = Color.BLACK;

            left_margin = 0.5f;
            right_margin = 0.5f;
            top_margin = 0.25f;
            bottom_margin = 0.25f;

            button_scale_x = 1.0f;
            button_scale_y = 1.0f;

            shift_drawable = R.drawable.avr_keyboard_shift;
            return_drawable = R.drawable.avr_keyboard_return;
            space_drawable = R.drawable.avr_keyboard_space;
            delete_drawable = R.drawable.avr_keyboard_delete;
            zwnj_drawable = R.drawable.avr_keyboard_zwnj;
        }
    }

    public static class TextField extends TextFieldStyle {
        private static final float TEXT_HEIGHT = 0.5f;
        private static final float CURSOR_WIDTH = 0.03f;
        private static final float BUTTON_SIZE = TEXT_HEIGHT * 1.25f;
        private static final float BUTTON_MARGIN = 0.2f;

        public TextField() {
            space_drawable = R.drawable.avrk_text_field_space;
            space_width = 0.5f;
            space_height = 0.04f;

            KeyStyle keyStyle = new KeyStyle();
            keyStyle.background.drawable = R.drawable.avrk_square_text_field_button;
            keyStyle.background.tint = MainColor;
            keyStyle.hover_background.drawable = R.drawable.avrk_square_text_field_button_hover;
            keyStyle.hover_background.tint = MainColor;
            keyStyle.depressed_depth = 0.15f;
            keyStyle.text_color = MainColor;
            keyStyle.text_color_hover = 0xFF000000;

            back_button = new Button(R.drawable.avrk_text_field_back, 0.9f, keyStyle,
                    BUTTON_SIZE, BUTTON_SIZE, BUTTON_MARGIN);
            delete_all_button = new Button(R.drawable.avrk_text_field_delete, 0.8f, keyStyle,
                    BUTTON_SIZE, BUTTON_SIZE, BUTTON_MARGIN);

            cursor_width = CURSOR_WIDTH;
            cursor_color = MainColor & 0xC0FFFFFF;
            cursor_flash_on_time = 0.6f;
            cursor_flash_off_time = 0.4f;

            font_height = TEXT_HEIGHT;

            textStyle.text_color = MainColor;
            textStyle.text_color_hover = HoverTextColor;
            textStyle.font_typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD);
            textStyle.background.drawable = R.drawable.avrk_custom_background;
            textStyle.background.tint = Color.BLACK;
            textStyle.hover_background.drawable = R.drawable.avrk_custom_background;
            textStyle.hover_background.tint = 0xFFC0C0C0;
        }
    }
}
