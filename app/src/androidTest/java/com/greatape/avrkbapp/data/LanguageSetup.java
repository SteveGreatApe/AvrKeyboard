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
package com.greatape.avrkbapp.data;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.view.inputmethod.InputMethodSubtype;

import java.util.Locale;

/**
 * @author Steve Townsend
 */
public class LanguageSetup {
    // See http://androidxref.com/7.1.1_r6/xref/packages/inputmethods/LatinIME/java/res/xml/method.xml#125
    // for example subtypes
    private final static LanguageSetup[] sLanguages = {
            new LanguageSetup("en", "GB", null),
            new LanguageSetup("fr", "FR", null),
            new LanguageSetup("de", "DE", null),
            new LanguageSetup("nl", "BE", "KeyboardLayoutSet=azerty,AsciiCapable,EmojiCapable"),
            new LanguageSetup("sr", "ZZ", "KeyboardLayoutSet=serbian_qwertz,AsciiCapable,EmojiCapable"),
            new LanguageSetup("ta", "IN", "KeyboardLayoutSet=tamil,AsciiCapable,EmojiCapable"),
            new LanguageSetup("te", "IN", "KeyboardLayoutSet=telugu,EmojiCapable"),
            new LanguageSetup("te", "IN", "KeyboardLayoutSet=telugu,EmojiCapable"),
            new LanguageSetup("th", null, "KeyboardLayoutSet=thai,EmojiCapable"),
            new LanguageSetup("es", null, "KeyboardLayoutSet=spanish,AsciiCapable,EmojiCapable"),
            new LanguageSetup("uk", null, "KeyboardLayoutSet=east_slavic,AsciiCapable,EmojiCapable"),
            new LanguageSetup("uz", "UZ", "KeyboardLayoutSet=uzbek,AsciiCapable,EmojiCapable"),
            new LanguageSetup("bg", null, "KeyboardLayoutSet=bulgarian,EmojiCapable"),
            new LanguageSetup("bg", null, "KeyboardLayoutSet=bulgarian_bds,EmojiCapable"),
            new LanguageSetup("bn", "BD", "KeyboardLayoutSet=bengali_akkhor,EmojiCapable"),
            new LanguageSetup("km", "KH", "KeyboardLayoutSet=khmer,EmojiCapable"),
            new LanguageSetup("hi", "IN", "KeyboardLayoutSet=hindi,EmojiCapable"),
    };

    private String mLanguage;
    private String mCountry;
    private String mSubTypeExtraValue;

    private LanguageSetup(String language, String country, String subTypeExtraValue) {
        this.mLanguage = language;
        this.mCountry = country;
        this.mSubTypeExtraValue = subTypeExtraValue;
    }

    public Context getLocaleContext() {
        Locale locale = getLocale();
        Locale.setDefault(locale);
        Context appContext = InstrumentationRegistry.getTargetContext();
        Resources res = appContext.getResources();
        Configuration configuration = res.getConfiguration();
        configuration.setLocale(locale);
        return appContext.createConfigurationContext(configuration);
    }

    public Locale getLocale() {
        return mCountry != null ? new Locale(mLanguage, mCountry) : new Locale(mLanguage);
    }

    public String getSubtypeExtraValue() {
        return mSubTypeExtraValue;
    }

    public static LanguageSetup[] getTestLanguages() {
        return sLanguages;
    }

    public InputMethodSubtype getInputMethodSubtype() {
        Locale locale = getLocale();
        InputMethodSubtype.InputMethodSubtypeBuilder builder = new InputMethodSubtype.InputMethodSubtypeBuilder();
        builder.setSubtypeLocale(locale.toString());
        builder.setSubtypeExtraValue(getSubtypeExtraValue());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setLanguageTag(locale.toLanguageTag());
        }
        return builder.build();
    }


    @Override
    public String toString() {
        return getLocale().toString();
    }
}
