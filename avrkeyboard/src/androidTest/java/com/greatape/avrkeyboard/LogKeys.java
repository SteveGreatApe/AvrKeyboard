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

import android.util.Log;
import android.util.SparseArray;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.latin.common.Constants;

import java.util.List;
import java.util.TreeMap;

/**
 * @author Steve Townsend
 */
class LogKeys {
    private static final String TAG = "LogKeys";

    static void logKeys(String title, List<Key> keys, int logLevel) {
        Log.d(TAG, title + ": " + keys.size() + " keys");
        if (logLevel == 1) {
            logLayout(keys);
        } else if (logLevel == 2) {
            logFullDetails(keys);
        }
    }

    private static void logLayout(List<Key> keys) {
        SparseArray<TreeMap<Integer, Integer>> keyRows = new SparseArray<>();
        for (Key key : keys) {
            int keyY = key.getY();
            TreeMap<Integer, Integer> rowDetails = keyRows.get(keyY);
            if (rowDetails == null) {
                rowDetails = new TreeMap<>();
                keyRows.put(keyY, rowDetails);
            }
            rowDetails.put(key.getX(), key.getCode());
        }
        for(int index = 0; index < keyRows.size(); index++) {
            int yPos = keyRows.keyAt(index);
            TreeMap<Integer, Integer> rowDetails = keyRows.get(yPos);
            StringBuilder stringBuilder = new StringBuilder("Row{");
            stringBuilder.append(yPos);
            stringBuilder.append("} ");
            boolean isFirst = true;
            for(int keyCode : rowDetails.values()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    stringBuilder.append(' ');
                }
                if (keyCode < 0x100) {
                    stringBuilder.append(Constants.printableCode(keyCode));
                } else {
                    stringBuilder.append(String.format("%c", keyCode));
                }
            }
            Log.d(TAG, stringBuilder.toString());
        }
    }

    private static void logFullDetails(List<Key> keys) {
        for (Key key : keys) {
            int keyCode = key.getCode();
            StringBuilder stringBuilder = new StringBuilder("Key: ");
            if (keyCode == Constants.CODE_OUTPUT_TEXT) {
                stringBuilder.append(key.getOutputText());
            } else {
                stringBuilder.append(Constants.printableCode(keyCode));
            }
            stringBuilder.append('{');
            stringBuilder.append(key.getX());
            stringBuilder.append(',');
            stringBuilder.append(key.getY());
            stringBuilder.append('}');
            stringBuilder.append('{');
            stringBuilder.append(key.getWidth());
            stringBuilder.append(',');
            stringBuilder.append(key.getHeight());
            stringBuilder.append('}');
            int iconId = key.getIconId();
            if (iconId != 0) {
                stringBuilder.append('[');
                stringBuilder.append(iconId);
                stringBuilder.append(']');
            }
            Log.d(TAG, stringBuilder.toString());
        }
    }
}
