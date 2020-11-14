package com.wwc2.jni.tp_touch;

/**
 * JNI提供的方法
 * @author wwc2
 * @date 2017/1/5
 */
public class TPTouchNative {

	static {
		System.loadLibrary("jni_tp_touch");
	}

	public final static native int TP_positionX();
	public final static native int TP_positionY();
	public final static native int TP_positionRect(int[] rects);
}
