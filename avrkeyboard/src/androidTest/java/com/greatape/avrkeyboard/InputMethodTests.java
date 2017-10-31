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
package com.greatape.avrkeyboard;

import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

/**
 * @author Steve Townsend
 */
@RunWith(AndroidJUnit4.class)
public class InputMethodTests {
    private static final String TAG = "InputMethodTests";
    // Currently this code is just being used to understand available InputMethodSubtype's
    // TODO: Either expand to a more useful test or delete.

    @Test
    public void getKeyListBySubType() {
        Context context = InstrumentationRegistry.getTargetContext();
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledList = imm.getInputMethodList();
        for (InputMethodInfo enabledInfo : enabledList) {
            List<InputMethodSubtype> subtypeListList = imm.getEnabledInputMethodSubtypeList(enabledInfo, true);
            LogInputMethodSubTypes(enabledInfo, subtypeListList);
        }
        Map<InputMethodInfo, List<InputMethodSubtype>> shortcutInputMethods = imm.getShortcutInputMethodsAndSubtypes();
        for(InputMethodInfo inputMethodInfo : shortcutInputMethods.keySet()) {
            List<InputMethodSubtype> subtypeListList = shortcutInputMethods.get(inputMethodInfo);
            LogInputMethodSubTypes(inputMethodInfo, subtypeListList);
        }
    }

    private void LogInputMethodSubTypes(InputMethodInfo inputMethodInfo, List<InputMethodSubtype> subtypeListList) {
        for(InputMethodSubtype inputMethodSubtype : subtypeListList) {
            String tag = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tag = inputMethodSubtype.getLanguageTag();
            }
            String mode = inputMethodSubtype.getMode();
            Log.d(TAG, inputMethodInfo + ": " + mode + ": " + tag);
        }
    }
}
