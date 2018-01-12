package com.greatape.avrutils;

public class AvrEventHandler {
    private static AvrEventHandler sInstance;
    private AvrControllerButtonHandler mAvrControllerButtonHandler;

    public static void init() {
        sInstance = new AvrEventHandler();
    }

    private AvrEventHandler() {
        mAvrControllerButtonHandler = new AvrControllerButtonHandler();
    }

    public static AvrControllerButtonHandler controllerButtonHandler() {
        return sInstance.mAvrControllerButtonHandler;
    }

    static boolean keyDown(int keycode) {
        return sInstance.mAvrControllerButtonHandler.isDown(keycode);
    }

    static void addControllerButton(AvrControllerButton avrControllerButton) {
        sInstance.mAvrControllerButtonHandler.addControllerButton(avrControllerButton);
    }

    static void removeControllerButton(AvrControllerButton avrControllerButton) {
        sInstance.mAvrControllerButtonHandler.removeControllerButton(avrControllerButton);
    }
}
