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
package com.greatape.avrutils.anim;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVROnFinish;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Townsend
 */
public class AvrShowHideAnimation {
    private List<AvrAnim> mShowAnimations;
    private List<AvrAnim> mHideAnimations;
    private List<GVRAnimation> mActiveAnimations;
    private GVRSceneObject mSceneObject;
    private GVRAnimationEngine mAnimationEngine;

    public AvrShowHideAnimation(GVRSceneObject sceneObject) {
        mSceneObject = sceneObject;
        mAnimationEngine = sceneObject.getGVRContext().getAnimationEngine();
        mShowAnimations = new ArrayList<>();
        mHideAnimations = new ArrayList<>();
    }

    public void addShowAnimation(AvrAnim animation) {
        mShowAnimations.add(animation);
    }

    public void addHideAnimation(AvrAnim animation) {
        mHideAnimations.add(animation);
    }

    public AvrAnim createPositionToCurrentAnimation(GVRTransform transform, float duration) {
        return new AvrPositionAnim(duration, transform.getPositionX(), transform.getPositionY(), transform.getPositionZ());
    }

    public AvrAnim createScaleToCurrentAnimation(GVRTransform transform, float duration) {
        return new AvrScaleAnim(duration, transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());
    }

    public void show() {
        stopActive();
        mSceneObject.setEnable(true);
        startAnimations(mShowAnimations, false);
    }

    public void hide() {
        stopActive();
        startAnimations(mHideAnimations, true);
    }

    private void startAnimations(List<AvrAnim> animations, final boolean hideOnFinish) {
        mActiveAnimations = new ArrayList<>();
        for(AvrAnim avrAnim : animations) {
            GVRAnimation animation = avrAnim.createAnimation(mSceneObject);
            animation.setOnFinish(new GVROnFinish() {
                @Override
                public void finished(GVRAnimation gvrAnimation) {
                    mActiveAnimations.remove(gvrAnimation);
                    if (mActiveAnimations.isEmpty()) {
                        mActiveAnimations = null;
                        if (hideOnFinish) {
                            mSceneObject.setEnable(false);
                        }
                    }
                }
            });
            mActiveAnimations.add(animation);
            mAnimationEngine.start(animation);
        }
    }

    private void stopActive() {
        if (mActiveAnimations != null) {
            for(GVRAnimation animation : mActiveAnimations) {
//                animation.getElapsedTime() TODO: If partially animated, use elapsed time to adjust reverse animation time
                mAnimationEngine.stop(animation);
            }
            mActiveAnimations = null;
        }
    }

    public void setToShownState() {
        setToState(mShowAnimations);
    }

    public void setToHiddenState() {
        setToState(mHideAnimations);
    }

    private void setToState(List<AvrAnim> animations) {
        stopActive();
        for(AvrAnim avrAnim : animations) {
            avrAnim.setState(mSceneObject);
        }
    }
}
