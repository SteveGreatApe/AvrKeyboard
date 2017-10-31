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
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;

import com.android.inputmethod.keyboard.AndroidKeyboardLayout;
import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.greatape.avrkeyboard.data.ElementTestParamSet;
import com.greatape.avrkeyboard.data.LanguageSetup;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Steve Townsend
 */
@RunWith(AndroidJUnit4.class)
public class GetKeyListTest {

    private int mLogLevel = 1;
    private static final String TAG = "GetKeyList";

    @Test
    public void testElementModes() throws Exception {
        mLogLevel = 2;
        Context context = InstrumentationRegistry.getTargetContext();
        for(ElementTestParamSet testParamSet: ElementTestParamSet.getElementTestSets()) {
            ElementTestInfo elementTestInfo = new ElementTestInfo(testParamSet);
            for (ElementTestParamSet.ElementTestParams testParams : testParamSet.elementArray) {
                doTestGetListWithMode(context, "ElementId: " + testParams.title, elementTestInfo, testParams);
            }
        }
    }

    @Test
    public void testLanguages() throws Exception {
        mLogLevel = 1;
        for(LanguageSetup language : LanguageSetup.getTestLanguages()) {
            ElementTestInfo elementTestInfo = new ElementTestInfo(ElementTestParamSet.getElementTestSets()[0]);
            doTestKeyListForLanguage(language, elementTestInfo);
        }
    }

    @Test
    public void testLanguagesAndElementModes() throws Exception {
        mLogLevel = 1;
        for (LanguageSetup language : LanguageSetup.getTestLanguages()) {
            Log.d(TAG, "Language: " + language.toString());
            for(ElementTestParamSet testParamSet: ElementTestParamSet.getElementTestSets()) {
                ElementTestInfo elementTestInfo = new ElementTestInfo(testParamSet);
                Log.d(TAG, "ElementSet: " + testParamSet.toString());
                for (ElementTestParamSet.ElementTestParams testParams : testParamSet.elementArray) {
                    doTestKeyListForLanguage(language, elementTestInfo, testParams);
                }
            }
        }
    }

    private void doTestKeyListForLanguage(LanguageSetup language, ElementTestInfo elementTestInfo) {
        ElementTestParamSet.ElementTestParams testParams = elementTestInfo.testParamSet.elementArray[0];
        doTestKeyListForLanguage(language, elementTestInfo, testParams);
    }

    private void doTestKeyListForLanguage(LanguageSetup language, ElementTestInfo elementTestInfo, ElementTestParamSet.ElementTestParams testParams) {
        InputMethodSubtype selectedSubtype = language.getInputMethodSubtype();
        String title = "Locale:" + language.getLocale().toString() + " ElementId: " + testParams.title;
        Context localeContext = language.getLocaleContext();
        doTestGetListWithMode(localeContext, title, selectedSubtype, elementTestInfo, testParams);
    }

    private void doTestGetListWithMode(Context context, String title, InputMethodSubtype selectedSubtype, ElementTestInfo elementTestInfo, ElementTestParamSet.ElementTestParams testParams) {
        Keyboard keyboard = AndroidKeyboardLayout.getKeyList(context, selectedSubtype, testParams.elementId);
        assertKeyList(title, keyboard, elementTestInfo);
    }

    private void doTestGetListWithMode(Context context, String title, ElementTestInfo elementTestInfo, ElementTestParamSet.ElementTestParams testParams) {
        int[] elementIds = {testParams.elementId};
        Keyboard keyboards[] = AndroidKeyboardLayout.getKeyboards(context, elementIds);
        assertKeyList(title, keyboards[0], elementTestInfo);
    }

    private void assertKeyList(String title, Keyboard keyboard, ElementTestInfo elementTestInfo) {
        List<Key> keys = keyboard.getSortedKeys();
        assertNotNull(keys);
        LogKeys.logKeys(title, keys, mLogLevel);
        if (elementTestInfo.keys ==  null) {
            elementTestInfo.keys = keys;
            Assert.assertTrue("com.greatape.avrkeyboard.test", keys.size() >= elementTestInfo.testParamSet.min);
            Assert.assertTrue("com.greatape.avrkeyboard.test", keys.size() <= elementTestInfo.testParamSet.max);
        } else {
            assertEquals(elementTestInfo.keys.size(), keys.size());
            for(int index = 0; index < keys.size(); index++) {
                Key baseKey = elementTestInfo.keys.get(index);
                Key otherKey = keys.get(index);
                assertEquals(baseKey.getX(), otherKey.getX());
                assertEquals(baseKey.getY(), otherKey.getY());
                assertEquals(baseKey.getWidth(), otherKey.getWidth());
                assertEquals(baseKey.getHeight(), otherKey.getHeight());
            }
        }
    }

    public static class ElementTestInfo {
        List<Key> keys;
        public ElementTestParamSet testParamSet;

        public ElementTestInfo(ElementTestParamSet testParamSet) {
            this.testParamSet = testParamSet;
        }
    }
}
