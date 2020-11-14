package com.wwc2.main.canbus.driver;

import android.os.Bundle;

/**
 * the navigation driver interface.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public interface CanBusDriverable {

    /**
     * 使能CANBUS
     */
    boolean enableCanbus(boolean enable);

    /**
     * 初始化CANBUS
     */
    boolean initCanbus(String impl, boolean open, /*String canCompany, String carBrand, String carType,*/int canSeries, boolean kill);

    /**
     * 设置协议类型
     */
    void setCanbusType(int nId, Bundle bundle);

    void sendHostInfoToCan(String key, int nId, Bundle bundle);

    int getLanuage(String lan);

    void reloadConfig();
}
