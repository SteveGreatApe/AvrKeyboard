package com.android.inputmethod.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;

import com.android.inputmethod.annotations.UsedForTesting;
import com.android.inputmethod.compat.InputMethodSubtypeCompatUtils;
import com.android.inputmethod.latin.RichInputMethodManager;
import com.android.inputmethod.latin.RichInputMethodSubtype;
import com.android.inputmethod.latin.utils.ResourceUtils;
import com.android.inputmethod.latin.utils.SubtypeLocaleUtils;
import com.greatape.avrkeyboard.R;

import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.text.InputType.TYPE_CLASS_TEXT;

public class AndroidKeyboardLayout {
    // Used for testing to over-ride standard behaviour.
    private static InputMethodSubtype sSubTypeOverride;

    public static Keyboard[] getKeyboards(Context context, final int[] elementIds) {
        SubtypeLocaleUtils.init(context);
        RichInputMethodManager.init(context);
        Keyboard[] keyboards = null;
        InputMethodSubtype subtype = getCurrentSubType();
        if (subtype != null) {
            keyboards = new Keyboard[elementIds.length];
            for(int index = 0; index < elementIds.length; index++) {
                keyboards[index] = getKeyList(context, subtype, elementIds[index]);
            }
        }
        return keyboards;
    }

    @UsedForTesting
    public static void setSubTypeOverride(InputMethodSubtype subTypeOverride) {
        sSubTypeOverride = subTypeOverride;
    }

    private static InputMethodSubtype getCurrentSubType() {
        RichInputMethodManager rimm = RichInputMethodManager.getInstance();
        InputMethodSubtype basicCurrentSubType;
        if (sSubTypeOverride != null) {
            basicCurrentSubType = sSubTypeOverride;
        } else {
            basicCurrentSubType = rimm.getCurrentSubtype().getRawSubtype();
        }
        // Find matching subtype, the value returned from RichInputMethodManager doesn't
        // contain the essential "KeyboardLayoutSet" extra data, so look up and switch to
        // the version from getInputMethodInfoOfThisIme that does have it.
        final Locale basicCurrentSubTypeLocale = InputMethodSubtypeCompatUtils.getLocaleObject(basicCurrentSubType);
        InputMethodSubtype selectedSubtype = null;
        InputMethodSubtype fallbackSelection = null;
        InputMethodInfo imi = rimm.getInputMethodInfoOfThisIme();
        for(int index = 0; index < imi.getSubtypeCount(); index++) {
            InputMethodSubtype subtype = imi.getSubtypeAt(index);
            final Locale subtypeLocale = InputMethodSubtypeCompatUtils.getLocaleObject(subtype);
            if (subtypeLocale.equals(basicCurrentSubTypeLocale)) {
                selectedSubtype = subtype;
                break;
            }
            if (fallbackSelection == null && basicCurrentSubTypeLocale.getLanguage().equals(subtypeLocale.getLanguage())) {
                fallbackSelection = subtype;
            }
        }
        return selectedSubtype != null ? selectedSubtype : fallbackSelection;
    }

    public static Keyboard getKeyList(Context context, InputMethodSubtype selectedSubtype, final int baseKeyboardLayoutSetElementId) {
        EditorInfo editorInfo = new EditorInfo();
        editorInfo.inputType = TYPE_CLASS_TEXT;// TODO: Test different values here
        editorInfo.label = "";
        editorInfo.hintText = "";
        KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(context, editorInfo);
        builder.setSubtype(new RichInputMethodSubtype(selectedSubtype));
        final Resources resources = context.getResources();
        builder.setKeyboardGeometry(ResourceUtils.getDefaultKeyboardWidth(resources), ResourceUtils.getDefaultKeyboardHeight(resources));
        KeyboardLayoutSet keyboardLayoutSet = builder.build();
        return keyboardLayoutSet.getKeyboard(baseKeyboardLayoutSetElementId);
    }
}
