package com.greatape.avrutils;

import android.graphics.PointF;
import android.view.KeyEvent;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AvrPlayer implements GVRDrawFrameListener {
    private final static String TAG = "AvrPlayer";

    private final static float MoveMaxRange = 314;
    private final static float HalfMaxRange = MoveMaxRange / 2f;
    private final static float QuarterMaxRange = MoveMaxRange / 4f;
    private final static double ZoomAngleResetMinSpeed = 0.02;
    private final static double ZoomAngleResetDiv = 10.0;

    private final GVRCameraRig mCameraRig;
    private final GVRScene mScene;
    private Vector3f mCameraRigPos;
    private GVRSceneObject mDirectionPointer;
    private double mPlayerAngle;
    private double mPlayerAngleRadians;
    private double mSinPlayerAngle;
    private double mCosPlayerAngle;
    private double mZoomAngleAdjust;
    private PointF mPrevPos;
    private MoveDirection mMoveDirection;
    private GVREventListeners.ActivityEvents mEventListener;
    private PointF mControllerPoint;
    private Quaternionf mControllerOrientation;
    private BoundsCheck mBoundsCheck;
    private ZoomMode mZoomMode;
    private MoveConfig mMoveConfig;

    public static class MoveConfig {
        public double xSpeed;
        public RotateMode xMode;
        public double zSpeed;
        public RotateMode zMode;
        public double rotateSpeed;
        public RotateMode rotateMode;
    }

    public enum MoveDirection {
        TowardsFront,
        TowardsLook,
    }

    public enum RotateMode {
        Always,
        Swipe,
        WhenPressed,
        WhenNotPressed,
        None,
    }

    public interface BoundsCheck {
        void checkMove(Vector3f oldPos, Vector3f newPos);
    }

    public AvrPlayer(GVRScene scene, Vector3f cameraPos) {
        mScene = scene;
        mCameraRig = scene.getMainCameraRig();
        setPosition(cameraPos, 0.0f);
        mMoveDirection = MoveDirection.TowardsLook;
        mCosPlayerAngle = 1.0f;
//        ICameraEvents cameraEvents = new ICameraEvents() {
//            public void onViewChange(GVRCameraRig rig) {
//                Log.d(TAG, "onViewChange:");
//                updateCameraRig(rig);
//            }
//        };
//        scene.getGVRContext().getEventReceiver().addListener(cameraEvents);
    }

    public void updateCameraRig(GVRCameraRig rig) {
        rig.getTransform().setPosition(mCameraRigPos.x, mCameraRigPos.y, mCameraRigPos.z);
        rig.getTransform().setRotationByAxis((float)mPlayerAngle, 0.0f, 1.0f, 0.0f);
        if (mDirectionPointer != null) {
            mDirectionPointer.getTransform().setRotationByAxis((float)mPlayerAngle, 0f, 1f, 0f);
            mDirectionPointer.getTransform().setPosition(mCameraRigPos.x, mCameraRigPos.y, mCameraRigPos.z);
        }
    }

    private void enableListeners() {
        if (mEventListener == null) {
            mEventListener = new GVREventListeners.ActivityEvents() {
                @Override
                public void onControllerEvent(Vector3f position, Quaternionf orientation, PointF touchPadPosition, boolean touched) {
                    handleControllerEvent(orientation, touchPadPosition, touched);
                }
            };
            mScene.getGVRContext().getActivity().getEventReceiver().addListener(mEventListener);
        }
        mScene.getGVRContext().registerDrawFrameListener(this);
    }

    private void disableListeners() {
        if (mEventListener != null) {
            mScene.getGVRContext().getActivity().getEventReceiver().removeListener(mEventListener);
            mEventListener = null;
        }
        mScene.getGVRContext().unregisterDrawFrameListener(this);
    }

    public void setMoveConfig(MoveConfig moveConfig) {
        mMoveConfig = moveConfig;
        updateListenerStates();
    }

    private void updateListenerStates() {
        if (mMoveConfig.xMode != RotateMode.None ||
                mMoveConfig.zMode != RotateMode.None ||
                mMoveConfig.rotateMode != RotateMode.None ) {
            enableListeners();
        } else {
            disableListeners();
        }
    }

    public void setPosition(Vector3f position, double angle) {
        mCameraRigPos = new Vector3f(position);
        mCameraRig.getTransform().setPosition(mCameraRigPos.x, mCameraRigPos.y, mCameraRigPos.z);
        setPlayerAngle(angle);
        mCameraRig.getTransform().setRotationByAxis((float)mPlayerAngle, 0.0f, 1.0f, 0.0f);
    }

    private void setPlayerAngle(double angle) {
        mPlayerAngleRadians = VrMath.normalisedAngle(angle);
        mPlayerAngle = Math.toDegrees(mPlayerAngleRadians);
        mSinPlayerAngle = Math.sin(mPlayerAngleRadians);
        mCosPlayerAngle = Math.cos(mPlayerAngleRadians);
    }

    public void setBoundsCheck(BoundsCheck boundsCheck) {
        mBoundsCheck = boundsCheck;
    }

    public double getAngle() {
        return mPlayerAngle;
    }

    public ZoomMode getZoomMode() {
        return mZoomMode;
    }

    public void setZoomMode(ZoomMode zoomMode) {
        if (zoomMode.mZoomOutPos == null) {
            zoomMode.mZoomOutPos = new Vector3f(mCameraRigPos);
        }
        if (zoomMode.mZoomOutAngle == null) {
            zoomMode.mZoomOutAngle = mPlayerAngleRadians;
        }
        mZoomMode = zoomMode;
        updateListenerStates();
        mZoomAngleAdjust = 0;
        setPosition(mZoomMode.positionForOffset(), mZoomMode.mZoomAngle + Math.PI);
    }

    public void exitZoomMode() {
        if (mZoomMode != null) {
            setPosition(mZoomMode.mZoomOutPos, mZoomMode.mZoomOutAngle + Math.PI);
            mZoomMode = null;
            updateListenerStates();
        }
    }

    public boolean isInZoomMode() {
        return mZoomMode != null;
    }

    private synchronized void handleControllerEvent(Quaternionf orientation, PointF touchPadPosition, boolean touched) {
        if (!touched) {
            mControllerPoint = null;
        } else {
            mControllerPoint = new PointF(touchPadPosition.x, touchPadPosition.y);
        }
        mControllerOrientation = new Quaternionf(orientation);
    }

    public void close() {
        disableListeners();
    }

    public void setMoveDirection(MoveDirection moveDirection) {
        mMoveDirection = moveDirection;
    }

    private boolean modeActive(RotateMode rotateMode) {
        boolean ret = false;
        switch(rotateMode) {
            case Always:
                 ret = true;
                 break;
            case WhenPressed:
                ret = AvrEventHandler.keyDown(KeyEvent.KEYCODE_ENTER);
                break;
            case WhenNotPressed:
            case Swipe:
                ret = !AvrEventHandler.keyDown(KeyEvent.KEYCODE_ENTER);
                break;
        }
        return ret;
    }

    private void processNoTouch() {
        if (isInZoomMode() && mZoomAngleAdjust != 0) {
            double absZoomAdjust = Math.abs(mZoomAngleAdjust);
            double change = Math.min(Math.max(absZoomAdjust / ZoomAngleResetDiv, ZoomAngleResetMinSpeed), absZoomAdjust);
            if (mZoomAngleAdjust > 0) {
                change = -change;
            }
            setPlayerAngle(mPlayerAngleRadians + change);
            mZoomAngleAdjust += change;
        }
    }

    private void processMove(float frameTime) {
        Vector3f newPos = new Vector3f(mCameraRigPos);
        processMoveX(newPos, frameTime);
        processMoveZ(newPos, frameTime);
        if (mBoundsCheck != null) {
            mBoundsCheck.checkMove(mCameraRigPos, newPos);
        }
        mCameraRigPos = newPos;
    }

    private float relativeMove(RotateMode mode, float current, Float previous, float frameTime) {
        float move;
        if (mode == RotateMode.Swipe) {
            move = previous != null ? (current - previous) / HalfMaxRange : 0;
        } else {
            move = (current - HalfMaxRange) * frameTime / HalfMaxRange;
        }
        return move;
    }

    private MoveConfig activeMoveConfig() {
        return isInZoomMode() ? mZoomMode.mMoveConfig : mMoveConfig;
    }

    private void processMoveX(Vector3f pos, float frameTime) {
        MoveConfig moveConfig = activeMoveConfig();
        if (modeActive(moveConfig.rotateMode)) {
            float rotate = relativeMove(moveConfig.rotateMode, mControllerPoint.x, mPrevPos != null ? mPrevPos.x : null, frameTime);
            double adjustAngle = rotate * -moveConfig.rotateSpeed;
            if (isInZoomMode()) {
                mZoomAngleAdjust += adjustAngle;
            }
            setPlayerAngle(mPlayerAngleRadians + adjustAngle);
        }
        if (modeActive(moveConfig.xMode)) {
            // If we are only doing 1D movement then filter out touches too far to the right or left
            if (!modeActive(moveConfig.zMode) && !isInMidRange(mControllerPoint.y)) {
                return;
            }
            float moveX = relativeMove(moveConfig.xMode, mControllerPoint.x, mPrevPos != null ? mPrevPos.x : null, frameTime);
            moveX *= moveConfig.xSpeed;
            double moveSin;
            double moveCos;
            if (mMoveDirection == MoveDirection.TowardsFront) {
                moveSin = mSinPlayerAngle;
                moveCos = mCosPlayerAngle;
            } else {
                double moveAngle = playerPlusLookAngle();
                moveSin = Math.sin(moveAngle);
                moveCos = Math.cos(moveAngle);
            }
            pos.x += moveCos * moveX;
            pos.z += moveSin * -moveX;
        }
    }

    private void processMoveZ(Vector3f pos, float elapsedF) {
        MoveConfig moveConfig = activeMoveConfig();
        if (modeActive(moveConfig.zMode)) {
            // If we are only doing 1D movement then filter out touches too far to the right or left
            if (!modeActive(moveConfig.xMode) && !isInMidRange(mControllerPoint.x)) {
                return;
            }
            float moveZ = relativeMove(moveConfig.zMode, mControllerPoint.y, mPrevPos != null ? mPrevPos.y : null, elapsedF);
            moveZ *= moveConfig.zSpeed;
            if (isInZoomMode()) {
                mZoomMode.mOffset = Math.max(Math.min(mZoomMode.mOffset + moveZ, mZoomMode.mMaxOffset), mZoomMode.mMinOffset);
                Vector3f zoomPos = mZoomMode.positionForOffset();
                pos.x = zoomPos.x;
                pos.z = zoomPos.z;
            } else {
                double moveSin;
                double moveCos;
                if (mMoveDirection == MoveDirection.TowardsFront) {
                    moveSin = mSinPlayerAngle;
                    moveCos = mCosPlayerAngle;
                } else {
                    double moveAngle = playerPlusLookAngle();
                    moveSin = Math.sin(moveAngle);
                    moveCos = Math.cos(moveAngle);
                }
                pos.x += moveSin * moveZ;
                pos.z += moveCos * moveZ;
            }
        }
    }

    private boolean isInMidRange(float position) {
        return position > QuarterMaxRange && position < (MoveMaxRange - QuarterMaxRange);
    }

    private double playerPlusLookAngle() {
        GVRTransform headTransform = mCameraRig.getHeadTransform();
        double yaw = Math.toRadians(headTransform.getRotationYaw());
        double pitch = Math.toRadians(headTransform.getRotationPitch());
        double headAngle = VrMath.angleFromPitchAndYaw(pitch, yaw);
        return mPlayerAngleRadians + headAngle;
    }

    public void setDirectionPointer(GVRSceneObject sceneObject) {
        mDirectionPointer = sceneObject;
        mScene.addSceneObject(sceneObject);
    }

    @Override
    public synchronized void onDrawFrame(float frameTime) {
        if (mControllerOrientation != null) {
            if (mControllerPoint != null) {
                processMove(frameTime);
            } else {
                processNoTouch();
            }
            mPrevPos = mControllerPoint;
            updateCameraRig(mCameraRig);
        }
    }

    public static class ZoomMode {
        private MoveConfig mMoveConfig;
        private Vector3f mZoomPos;
        public double mZoomAngle;
        public float mOffset;
        private double mZoomSin;
        private double mZoomCos;
        private float mMinOffset;
        private float mMaxOffset;
        private Vector3f mZoomOutPos;
        private Double mZoomOutAngle;

        public ZoomMode(Vector3f zoomPos, double zoomAngle, float initialOffset, float minOffset, float maxOffset, MoveConfig moveConfig) {
            mZoomPos = zoomPos;
            mZoomAngle = zoomAngle;
            mZoomSin = Math.sin(mZoomAngle);
            mZoomCos = Math.cos(mZoomAngle);
            mOffset = initialOffset;
            mMinOffset = minOffset;
            mMaxOffset = maxOffset;
            mMoveConfig = moveConfig;
        }

        public void setZoomOutPos(Vector3f zoomOutPos, double zoomOutAngle) {
            mZoomOutPos = zoomOutPos;
            mZoomOutAngle = zoomOutAngle;
        }

        public Vector3f positionForOffset() {
            float xOffset = (float)(mZoomSin * mOffset);
            float zOffset = (float)(mZoomCos * mOffset);
            return new Vector3f(mZoomPos.x - xOffset, mZoomPos.y, mZoomPos.z - zOffset);
        }
    }
}
