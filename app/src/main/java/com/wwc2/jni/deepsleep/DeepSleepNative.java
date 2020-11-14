package com.wwc2.jni.deepsleep;

/**
 * JNI提供的方法
 * @author wwc2
 * @date 2017/1/18
 */
public class DeepSleepNative {

	static {
		System.loadLibrary("jni_deepsleep");
	}

	public final static native int DEEPSLEEP_read();
}
