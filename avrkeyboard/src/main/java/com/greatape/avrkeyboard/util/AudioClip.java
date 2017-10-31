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
package com.greatape.avrkeyboard.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseArray;

/**
 * @author Steve Townsend
 */
public class AudioClip {
    private static SoundPool sSoundPool;
    private static SparseArray<AudioClip> sLoadingArray = new SparseArray<>();

    private int mSoundID;
    private boolean mLoaded;

    private static synchronized SoundPool getSoundPool() {
        if (sSoundPool == null) {
            sSoundPool = createSoundPool();
        }
        return sSoundPool;
    }

    private static SoundPool createSoundPool() {
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(2);
        AudioAttributes.Builder aaBuilder = new AudioAttributes.Builder();
        aaBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        builder.setAudioAttributes(aaBuilder.build());
        SoundPool soundPool = builder.build();
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                AudioClip clip = sLoadingArray.get(sampleId);
                if (clip != null) {
                    sLoadingArray.remove(sampleId);
                    clip.mLoaded = true;
                }
            }
        });
        return soundPool;
    }

    public AudioClip(Context context, int resId) {
        mSoundID = getSoundPool().load(context, resId, 1);
        sLoadingArray.put(mSoundID, this);
    }


    public void play() {
        if (mLoaded) {
            sSoundPool.play(mSoundID, 1.0f, 1.0f, 1, 0, 1f);
        }
    }
}
