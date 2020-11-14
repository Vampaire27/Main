package com.wwc2.jni.mcu_serial;

import com.wwc2.main.manager.McuManager;

/**
 * JNI调用JAVA方法
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class JniToJava {

    /**通信数据接收超时*/
    public static final int ERROR_RECV_DATA_TIMEOUT = 1;

    // 需要发送应答给MCU
    public static void dispatch(byte[] buf, int len) {
        McuManager.dispatch(buf, len);
    }

    // 产生错误
    public void error(int no) {
        McuManager.error(no);
    }
}
