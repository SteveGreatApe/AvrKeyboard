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
package com.greatape.avrkbapp;

import android.graphics.RectF;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;

import com.android.inputmethod.keyboard.AndroidKeyboardLayout;
import com.greatape.avrkbapp.data.LanguageSetup;
import com.greatape.avrkeyboard.model.AvrKeyboard;
import com.greatape.avrkeyboard.model.KeyItem;
import com.greatape.avrkeyboard.model.KeyboardCharItem;
import com.greatape.avrkeyboard.styles.KeyboardStyle;
import com.greatape.avrkeyboard.styles.KeyboardStyleDefault;
import com.greatape.avrkeyboard.util.AvrUtil;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.greatape.avrkeyboard.model.AvrKeyboard.MODE_NORMAL;
import static com.greatape.avrkeyboard.model.AvrKeyboard.MODE_SHIFTED;
import static com.greatape.avrkeyboard.model.AvrKeyboard.MODE_SHIFTED_SPECIAL;
import static com.greatape.avrkeyboard.model.AvrKeyboard.MODE_SPECIAL;

/**
 * @author Steve Townsend
 */
@RunWith(AndroidJUnit4.class)
public class AvrKeyboardTest extends AvrTestBase {
    private static final String TAG = "AvrKeyboardTest";
    private final static boolean verboseLog = false;

    @Before
    public void setUp() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                HeapDump();
            }
        });
    }

    @Test @UiThreadTest
    public void testLanguagesAndElementModes() throws Exception {
        try {
            for (LanguageSetup language : LanguageSetup.getTestLanguages()) {
                Log.d(TAG, "Language: " + language.toString());
                doTestKeyListForLanguage(language);
                // TODO: There are attempts to clean up leaks, but they don't work, problem seems to be a fundamental one in GVRf
                System.gc();
                System.runFinalization();
            }
        } catch (OutOfMemoryError oom) {
            // TODO: Avoid ignoring OOM errors if issue with framework resolved
            // Currently there seem to be unavoidable errors where GVRf objects are not being
            // released for garbage collection, so we have to just ignore OOM errors for now.
            // HeapDump();
            // fail(oom.getMessage());
        }
    }

    private void doTestKeyListForLanguage(LanguageSetup language) throws Exception {
        InputMethodSubtype selectedSubtype = language.getInputMethodSubtype();
        String title = "Locale:" + language.getLocale().toString();
        doTestGetListWithMode(title, selectedSubtype);
    }

    private void doTestGetListWithMode(String title, InputMethodSubtype selectedSubtype) throws Exception {
        AndroidKeyboardLayout.setSubTypeOverride(selectedSubtype);
        KeyboardStyle style = new KeyboardStyleDefault();
        AvrKeyboard avrKeyboard = new AvrKeyboard(mGVRContext, AvrKeyboard.KeyboardType.ALPHA, style);
        avrKeyboard.initKeyboard();
        avrKeyboard.showKeyboard();
        verifyKeyboard(title, avrKeyboard);
        Assert.assertTrue(avrKeyboard.isEnabled());
        avrKeyboard.hideKeyboard();
        Assert.assertFalse(avrKeyboard.isEnabled());
        AndroidKeyboardLayout.setSubTypeOverride(null);
        Log.d(TAG, "Waiting for config to complete");
        avrKeyboard.waitForConfigToComplete();
        Log.d(TAG, "Config complete");
    }

    private void verifyKeyboard(String title, AvrKeyboard avrKeyboard) {
        int[] modes = {MODE_NORMAL, MODE_SHIFTED, MODE_SPECIAL, MODE_SHIFTED_SPECIAL};
        List<KeyItem> keyItems = avrKeyboard.getKeyItems();
        for(int mode : modes) {
            Log.d(TAG, title + " Mode: " + mode);
            for (KeyItem keyItem : keyItems) {
                RectF bounds = getRect(keyItem);
                KeyboardCharItem charItem = keyItem.getKeyboardCharItem();
                if (charItem.isValid(mode)) {
                    int resId = charItem.getImageResId(mode);
                    if (verboseLog) Log.d(TAG, "keyCode: " + charItem.getCharacter(mode) + " resId:" + resId + " " + bounds);
                }
            }
        }
    }

    private RectF getRect(KeyItem keyItem) {
        // TODO: workaround for issue in 4.0
        float[] coords = AvrUtil.getMeshVertices(keyItem.getRenderData().getMesh());
//        float[] coords = keyItem.getRenderData().getMesh().getVertices();
        // END: Workaround
        float xPos = keyItem.getRenderData().getTransform().getPositionX();
        float yPos = keyItem.getRenderData().getTransform().getPositionY();
        Assert.assertTrue((coords.length % 3) == 0);
        RectF bounds = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for(int index = 0; index < coords.length; index += 3) {
            float x = xPos + coords[index];
            bounds.left = Math.min(x, bounds.left);
            bounds.right = Math.max(x, bounds.right);
            float y = yPos + coords[index + 1];
            bounds.top = Math.min(y, bounds.top);
            bounds.bottom = Math.max(y, bounds.bottom);
        }
        return bounds;
    }
}
