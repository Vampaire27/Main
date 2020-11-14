package com.wwc2.jni.simulate_key;

/**
 * JNI提供的方法
 * @author wwc2
 * @date 2017/1/5
 */
public class SimulateKeyNative {

	static {
		System.loadLibrary("jni_simulate_key");
	}

	public static final int VR_KEY_CODE_HOME = 102;
	public static final int VR_KEY_CODE_MENU = 139;
	public static final int VR_KEY_CODE_BACK = 158;
	public static final int VR_KEY_CODE_NEXT = 163;
	public static final int VR_KEY_CODE_PREV = 165;
	public static final int VR_KEY_CODE_PLAY_PAUSE = 164;

	public final static native int SIMULATE_key(int key);
}
