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

import android.graphics.Color;
import android.graphics.Typeface;

import com.greatape.avrkeyboard.R;

/**
 * @author Steve Townsend
 */
public class TextFieldStyleBig extends TextFieldStyle {
    private static final int MainColor = 0xC080E4FF;
    private static final int HoverTextColor = 0xFF000000;
    private static final float TEXT_HEIGHT = 1.0f;
    private static final float CURSOR_WIDTH = 0.05f;
    private static final float BUTTON_SIZE = TEXT_HEIGHT * 0.75f;
    private static final float BUTTON_MARGIN = 0.2f;

    public TextFieldStyleBig() {
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
        keyStyle.text_color_hover = HoverTextColor;

        back_button = new Button(R.drawable.avrk_text_field_back, 0.8f, keyStyle,
                BUTTON_SIZE, BUTTON_SIZE, BUTTON_MARGIN);
        delete_all_button = new Button(R.drawable.avrk_text_field_delete, 0.75f, keyStyle,
                BUTTON_SIZE, BUTTON_SIZE, BUTTON_MARGIN);

        cursor_width = CURSOR_WIDTH;
        cursor_color = MainColor;
        cursor_flash_on_time = 0.6f;
        cursor_flash_off_time = 0.4f;

        textStyle.text_color = Color.WHITE;
        textStyle.font_typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        textStyle.text_color_hover = HoverTextColor;
        font_height = TEXT_HEIGHT;
   }
}
