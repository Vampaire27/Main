package com.wwc2.jni.micswitch;

/**
 * JNI提供的方法
 * @author wwc2
 * @date 2017/1/3
 */
public class MicSwitchNative {

	static {
		System.loadLibrary("jni_micswitch");
	}

	public final static native int MICSWITCH_ARM();
	public final static native int MICSWITCH_BT();
}
