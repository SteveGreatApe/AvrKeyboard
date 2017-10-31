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
public class TextFieldStyle {
    public KeyStyle textStyle;
    public float font_height;

    public int space_drawable;
    public float space_width;
    public float space_height;

    public Button back_button;
    public Button delete_all_button;

    public float cursor_width;
    public int cursor_color;
    public float cursor_flash_on_time;
    public float cursor_flash_off_time;

    public class Button {
        public KeyStyle keyStyle;
        public int icon_drawable;
        public float icon_scale;
        public float width;
        public float height;
        public float margin;

        public Button(int icon_drawable, float icon_scale, KeyStyle keyStyle, float width, float height, float margin) {
            this.icon_drawable = icon_drawable;
            this.icon_scale = icon_scale;
            this.keyStyle = keyStyle;
            this.width = width;
            this.height = height;
            this.margin = margin;
        }

        private Button(Button source, float scale) {
            icon_drawable = source.icon_drawable;
            icon_scale = source.icon_scale;
            keyStyle = new KeyStyle(source.keyStyle, scale);
            width = source.width * scale;
            height = source.height * scale;
            margin = source.margin * scale;
        }
    }

    protected TextFieldStyle() {
        textStyle = new KeyStyle();
    }

    public TextFieldStyle(TextFieldStyle source, float scale) {
        textStyle = new KeyStyle(source.textStyle, scale);
        font_height = source.font_height * scale;

        space_drawable = source.space_drawable;
        space_width = source.space_width;
        space_height = source.space_height;

        back_button = new Button(source.back_button, scale);
        delete_all_button = new Button(source.delete_all_button, scale);

        cursor_width = source.cursor_width * scale;
        cursor_color = source.cursor_color;
        cursor_flash_on_time = source.cursor_flash_on_time;
        cursor_flash_off_time = source.cursor_flash_off_time;
    }

    public TextFieldStyle scaled(float scale) {
        return new TextFieldStyle(this, scale);
    }
}
