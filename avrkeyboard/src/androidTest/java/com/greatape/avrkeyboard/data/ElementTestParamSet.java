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
package com.greatape.avrkeyboard.data;

import com.android.inputmethod.keyboard.KeyboardId;

/**
 * @author Steve Townsend
 */
public class ElementTestParamSet {
    private final static int MaxKeys = 53;

    private final static ElementTestParams[] alphabeticElements = {
            new ElementTestParams("ELEMENT_ALPHABET", KeyboardId.ELEMENT_ALPHABET),
            new ElementTestParams("ELEMENT_ALPHABET_MANUAL_SHIFTED", KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED),
            new ElementTestParams("ELEMENT_ALPHABET_AUTOMATIC_SHIFTED", KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED),
            new ElementTestParams("ELEMENT_ALPHABET_SHIFT_LOCKED", KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED),
            new ElementTestParams("ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED", KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED),
    };

    private final static ElementTestParams[] symbolElements = {
            new ElementTestParams("ELEMENT_SYMBOLS", KeyboardId.ELEMENT_SYMBOLS),
            new ElementTestParams("ELEMENT_SYMBOLS_SHIFTED", KeyboardId.ELEMENT_SYMBOLS_SHIFTED),
    };

    private final static ElementTestParams[] phoneElements = {
            new ElementTestParams("ELEMENT_PHONE", KeyboardId.ELEMENT_PHONE),
            new ElementTestParams("ELEMENT_PHONE_SYMBOLS", KeyboardId.ELEMENT_PHONE_SYMBOLS),
    };

    private final static ElementTestParams[] numberElements = {
            new ElementTestParams("ELEMENT_NUMBER", KeyboardId.ELEMENT_NUMBER),
    };

    private final static ElementTestParamSet[] elementTestSets = {
            new ElementTestParamSet("Alphabetic", alphabeticElements, 30, MaxKeys),
            new ElementTestParamSet("Symbol", symbolElements, 30, MaxKeys),
            new ElementTestParamSet("Phone", phoneElements, 16, 16),
            new ElementTestParamSet("Number", numberElements, 16, 16),
    };

    private String title;
    public ElementTestParams[] elementArray;
    public int min;
    public int max;

    private ElementTestParamSet(String title, ElementTestParams[] elementArray, int min, int max) {
        this.title = title;
        this.elementArray = elementArray;
        this.min = min;
        this.max = max;
    }

    public static class ElementTestParams {
        ElementTestParams(String title, int elementId) {
            this.title = title;
            this.elementId = elementId;
        }
        public String title;
        public int elementId;
    }

    public static ElementTestParamSet[] getElementTestSets() {
        return elementTestSets;
    }

    @Override
    public String toString() {
        return title;
    }
}
