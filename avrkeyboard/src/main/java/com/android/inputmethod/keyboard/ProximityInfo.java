/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.keyboard;

import com.android.inputmethod.keyboard.internal.TouchPositionCorrection;
import com.android.inputmethod.latin.common.Constants;

import java.util.List;

import javax.annotation.Nonnull;

// Great Ape: Replaced with minimal version as functionality not required and we can avoid pulling
// in excessive dependencies.
public class ProximityInfo {
    ProximityInfo(final int gridWidth, final int gridHeight, final int minWidth, final int height,
            final int mostCommonKeyWidth, final int mostCommonKeyHeight,
            @Nonnull
            final List<Key> sortedKeys,
            @Nonnull final TouchPositionCorrection touchPositionCorrection) {
    }

    public List<Key> getNearestKeys(int adjustedX, int adjustedY) {
        return null;
    }

    static boolean needsProximityInfo(final Key key) {
        // Don't include special keys into ProximityInfo.
        return key.getCode() >= Constants.CODE_SPACE;
    }
}
