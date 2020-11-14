package com.wwc2.main.driver.sdk;

import android.os.IInterface;

/**
 * the sdk interface.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public interface SDKable {

    /**
     * 是否需要MCU交互
     */
    boolean needMCU();

    /**
     * 发送数据，主要是组装数据
     */
    boolean sendData(byte[] val);

    /**
     * 接收数据，主要是去除包装，获取数据
     */
    void recvData(byte[] val);

    /**
     * 开关状态发生变化,见{@link com.wwc2.common_interface.utils.SwitchStatus}
     */
    void switchStatusChanged(int oldVal, int newVal);

    /**
     * SDK连接上了.
     */
    void onSDKConnected(IInterface driver);

    /**
     * SDK断开连接了.
     */
    void onSDKDisconnected(IInterface driver);
}
