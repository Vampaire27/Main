package com.wwc2.jni.backlight;

/**
 * JNI提供的方法
 *
 * @author wwc2
 * @date 2017/1/12
 */
public class BacklightNative {

    static {
        System.loadLibrary("jni_backlight");
    }

    public final static native int BACKLIGHT_open();
    public final static native int BACKLIGHT_read();
    public final static native int BACKLIGHT_close();
}
