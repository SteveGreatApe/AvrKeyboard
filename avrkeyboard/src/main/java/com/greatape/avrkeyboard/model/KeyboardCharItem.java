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
package com.greatape.avrkeyboard.model;

import android.util.SparseArray;

import com.android.inputmethod.annotations.UsedForTesting;
import com.android.inputmethod.keyboard.Key;
import com.greatape.avrkeyboard.styles.KeyboardStyle;

import static com.android.inputmethod.latin.common.Constants.CODE_ACTION_NEXT;
import static com.android.inputmethod.latin.common.Constants.CODE_ACTION_PREVIOUS;
import static com.android.inputmethod.latin.common.Constants.CODE_ALPHA_FROM_EMOJI;
import static com.android.inputmethod.latin.common.Constants.CODE_CAPSLOCK;
import static com.android.inputmethod.latin.common.Constants.CODE_DELETE;
import static com.android.inputmethod.latin.common.Constants.CODE_EMOJI;
import static com.android.inputmethod.latin.common.Constants.CODE_ENTER;
import static com.android.inputmethod.latin.common.Constants.CODE_LANGUAGE_SWITCH;
import static com.android.inputmethod.latin.common.Constants.CODE_OUTPUT_TEXT;
import static com.android.inputmethod.latin.common.Constants.CODE_SETTINGS;
import static com.android.inputmethod.latin.common.Constants.CODE_SHIFT;
import static com.android.inputmethod.latin.common.Constants.CODE_SHIFT_ENTER;
import static com.android.inputmethod.latin.common.Constants.CODE_SHORTCUT;
import static com.android.inputmethod.latin.common.Constants.CODE_SPACE;
import static com.android.inputmethod.latin.common.Constants.CODE_SWITCH_ALPHA_SYMBOL;
import static com.android.inputmethod.latin.common.Constants.CODE_TAB;
import static com.android.inputmethod.latin.common.Constants.CODE_UNSPECIFIED;

/**
 * @author Steve Townsend
 */
public class KeyboardCharItem {
    private SparseArray<ModeDetails> mCharacters;
    private SparseArray<Object> mMoreKeys;

    private final static ModeDetails defaultDetails = new ModeDetails(CODE_UNSPECIFIED, null);

    KeyboardCharItem() {
        super();
        mCharacters = new SparseArray<>();
        mMoreKeys = new SparseArray<>();
    }

    void setKeyCode(int mode, Key key, KeyboardStyle keyboardStyle) {
        ModeDetails modeDetails = new ModeDetails(key.getCode(), keyboardStyle);
        mCharacters.put(mode, modeDetails);
    }

    public void setKeyCode(int mode, int keyCode, int resId) {
        ModeDetails modeDetails = new ModeDetails(keyCode, null);
        modeDetails.imageResId = resId;
        mCharacters.put(mode, modeDetails);
    }

    boolean isShiftKey(int mode) {
        return mCharacters.get(mode).code == CODE_SHIFT;
    }

    boolean isSpecialKey(int mode) {
        return mCharacters.get(mode).code == CODE_SWITCH_ALPHA_SYMBOL;
    }

    public int getCharacter(int mode) {
        return mCharacters.get(mode, defaultDetails).code;
    }

    boolean isValidKey(int mode) {
        return mCharacters.get(mode, defaultDetails).code != CODE_UNSPECIFIED;
    }

    String getDescription(int mode) {
        final int code = mCharacters.get(mode, defaultDetails).code;
        switch (code) {
            case CODE_SHIFT: return "shift";
            case CODE_CAPSLOCK: return "capslock";
            case CODE_SWITCH_ALPHA_SYMBOL: return "?123";
            case CODE_OUTPUT_TEXT: return "text";
            case CODE_DELETE: return "delete";
            case CODE_SETTINGS: return "settings";
            case CODE_SHORTCUT: return "shortcut";
            case CODE_ACTION_NEXT: return "actionNext";
            case CODE_ACTION_PREVIOUS: return "actionPrevious";
            case CODE_LANGUAGE_SWITCH: return "languageSwitch";
            case CODE_EMOJI: return "emoji";
            case CODE_SHIFT_ENTER: return "shiftEnter";
            case CODE_ALPHA_FROM_EMOJI: return "alpha";
            case CODE_UNSPECIFIED: return "";
            case CODE_TAB: return "tab";
            case CODE_ENTER: return "enter";
            case CODE_SPACE: return "space";
            default:
                return Character.isValidCodePoint(code) ? String.format("%c", code) : "";
        }
    }

    @UsedForTesting
    public int getImageResId(int mode) {
        return mCharacters.get(mode, defaultDetails).imageResId;
    }

    @UsedForTesting
    public boolean isValid(int mode) {
        return mCharacters.get(mode, null) != null;
    }

    int getSpecialFunction(int mode) {
        return AvrKeyboard.mapCodeToSpecialFunction(mCharacters.get(mode).code);
    }

    void setMoreKeys(int mode, Object moreKeys) {
        mMoreKeys.put(mode, moreKeys);
    }

    public Object getMoreKeys(int mode) {
        return mMoreKeys.get(mode);
    }

    private static class ModeDetails {
        int code;
        int imageResId;

        ModeDetails(int code, KeyboardStyle keyboardStyle) {
            this.code = code;
            if (keyboardStyle != null) {
                switch (code) {
                    case CODE_SHIFT:
                        imageResId = keyboardStyle.shift_drawable;
                        break;
                    case CODE_ENTER:
                        imageResId = keyboardStyle.return_drawable;
                        break;
                    case CODE_DELETE:
                        imageResId = keyboardStyle.delete_drawable;
                        break;
                    case CODE_SPACE: // TODO: Do we want this? Or just write "space"
                        imageResId = keyboardStyle.space_drawable;
                        break;
                    case 0x200C: // TODO:
                        imageResId = keyboardStyle.zwnj_drawable;
                        break;
                }
            }
        }
    }
}
