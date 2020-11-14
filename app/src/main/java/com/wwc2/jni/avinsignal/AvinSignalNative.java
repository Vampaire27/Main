package com.wwc2.jni.avinsignal;

/**
 * JNI提供的方法
 * @author wwc2
 * @date 2017/1/13
 */
public class AvinSignalNative {

	static {
		System.loadLibrary("jni_avinsignal");
	}

	public final static native int AVINSIGNAL_read();
}
