package com.wwc2.jni.camera;

/**
 * JNI提供的方法
 *
 * @author wwc2
 * @date 2016/06/02
 */
public class CameraNative {

	static {
		System.loadLibrary("jni_camera");
	}

	/**0倒车，1不在倒车，-1错误*/
	public final static native int CAMERA_read();
}
