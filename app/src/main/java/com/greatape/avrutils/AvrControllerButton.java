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
package com.greatape.avrutils;

import android.graphics.PointF;
import android.os.Handler;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVREventReceiver;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * @author Steve Townsend
 */
public class AvrControllerButton {
    private final static long MaxClickToTouchTime = 100L;

    private final Listener mListener;
    private final GVREventListeners.ActivityEvents mEventListener;
    private final GVREventReceiver mEventReceiver;
    private final Handler mHandler;
    private PointF mControllerPoint;
    private long mRepeatTime;
    private Runnable mRunnable;
    private long mPendingClickTime;

    public interface Listener {
        void click(float x, float y, boolean isRepeat);
    }

    void clicked(boolean isDown) {
        if (isDown) {
            if (mControllerPoint != null) {
                processClick();
            } else {
                mPendingClickTime = System.currentTimeMillis();
            }
        } else {
            mPendingClickTime = 0L;
            if (mRunnable != null) {
                mHandler.removeCallbacks(mRunnable);
                mRunnable = null;
            }
        }
    }

    private void processClick() {
        mListener.click(mControllerPoint.x, mControllerPoint.y, false);
        queueRepeat();
    }

    private void queueRepeat() {
        if (mRepeatTime > 0L) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mRunnable != null) {
                        mListener.click(mControllerPoint.x, mControllerPoint.y, true);
                        queueRepeat();
                    }
                }
            };
            mHandler.postDelayed(mRunnable, mRepeatTime);
        }
    }

    public AvrControllerButton(GVRContext gvrContext, AvrPicker avrPicker, Listener listener) {
        mListener = listener;
        avrPicker.setControllerButton(this);
        mEventListener = new GVREventListeners.ActivityEvents() {
            @Override
            public void onControllerEvent(Vector3f position, Quaternionf orientation, PointF touchPadPosition) {
                if (touchPadPosition.x == 0f && touchPadPosition.y == 0f) {
                    mControllerPoint = null;
                } else {
                    final float MaxValue = 320f;
                    mControllerPoint = new PointF(touchPadPosition.x / MaxValue, touchPadPosition.y / MaxValue);
                    if (mPendingClickTime != 0L) {
                        long expired =  System.currentTimeMillis() - mPendingClickTime;
                        if (expired < MaxClickToTouchTime) {
                            processClick();
                        }
                        mPendingClickTime = 0L;
                    }
                }
            }
        };
        mEventReceiver = gvrContext.getActivity().getEventReceiver();
        mEventReceiver.addListener(mEventListener);
        mHandler = new Handler();
    }

    public void setRepeatTime(long repeatTime) {
        mRepeatTime = repeatTime;
    }

    public void close() {
        mEventReceiver.removeListener(mEventListener);
    }
}
