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
public class RedStyle {
    private static final int MainColor = 0xFFC04040;
    private static final int HoverTextColor = 0xFF000000;
    private static final int TextFieldHoverTextColor = 0xFF800000;

    public static class RedKeyboard extends KeyboardStyle {
        public RedKeyboard() {
            name = "RedStyle";
            keyStyle.font_size = 120;
            keyStyle.font_typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            keyStyle.text_color = MainColor;
            keyStyle.text_color_hover = HoverTextColor;

            keyStyle.background.drawable = R.drawable.avrk_key;
            keyStyle.background.tint = MainColor;
            keyStyle.hover_background.drawable = R.drawable.avrk_keyhover;
            keyStyle.hover_background.tint = MainColor & 0xA0FF8080;
            keyStyle.depressed_depth = 0.2f;

            background.drawable = R.drawable.avrk_custom_background;
            background.tint = 0xFF000000;

            button_scale_x = 1.0f;
            button_scale_y = 1.0f;

            shift_drawable = R.drawable.avr_keyboard_shift;
            return_drawable = R.drawable.avr_keyboard_return;
            space_drawable = R.drawable.avr_keyboard_space;
            delete_drawable = R.drawable.avr_keyboard_delete;
            zwnj_drawable = R.drawable.avr_keyboard_zwnj;
        }
    }

    public static class RedTextField extends TextFieldStyle {
        private static final float TEXT_HEIGHT = 0.75f;
        private static final float CURSOR_WIDTH = 0.05f;
        private static final float BUTTON_SIZE = TEXT_HEIGHT * 1.25f;
        private static final float BUTTON_MARGIN = 0.25f;

        public RedTextField() {
            space_drawable = R.drawable.avrk_text_field_space;
            space_width = 0.5f;
            space_height = 0.04f;

            KeyStyle keyStyle = new KeyStyle();
            keyStyle.background.drawable = R.drawable.avrk_text_field_button;
            keyStyle.background.tint = MainColor;
            keyStyle.hover_background.drawable = R.drawable.avrk_text_field_button_hover;
            keyStyle.hover_background.tint = MainColor;
            keyStyle.depressed_depth = 0.15f;
            keyStyle.text_color = MainColor;
            keyStyle.text_color_hover = TextFieldHoverTextColor;

            back_button = new Button(R.drawable.avrk_text_field_back, 0.9f, keyStyle,
                    BUTTON_SIZE, BUTTON_SIZE, BUTTON_MARGIN);
            delete_all_button = new Button(R.drawable.avrk_text_field_delete, 0.8f, keyStyle,
                    BUTTON_SIZE, BUTTON_SIZE, BUTTON_MARGIN);

            cursor_width = CURSOR_WIDTH;
            cursor_color = MainColor & 0xC0FFFFFF;
            cursor_flash_on_time = 0.6f;
            cursor_flash_off_time = 0.4f;

            textStyle.text_color = MainColor;
            textStyle.text_color_hover = Color.BLACK;
            textStyle.background.drawable = R.drawable.avrk_custom_background;
            textStyle.background.tint = Color.BLACK;
            textStyle.hover_background.drawable = R.drawable.avrk_custom_background;
            textStyle.hover_background.tint = Color.RED;
            textStyle.font_typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            font_height = TEXT_HEIGHT;
        }
    }
}
