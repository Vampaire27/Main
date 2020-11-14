package com.wwc2.main.canbus;

import com.wwc2.corelib.listener.BaseListener;

/**
 * Created by unique on 2017/1/27.
 */
public class CanBusListener extends BaseListener {

    @Override
    public String getClassName() {
        return CanBusListener.class.getName();
    }

    /**
     * can使能
     */
    public void CanEnableListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * can开关
     */
    public void CanSwitchListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * can开关状态
     */
    public void CanSwitchStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * can厂商，see{@link com.wwc2.canbus_interface.CanBusDefine.CanCompany}
     */
    public void CanCompanyListener(String oldVal, String newVal) {

    }

    /**
     * 汽车品牌(车系)，see{@link com.wwc2.canbus_interface.CanBusDefine.CarBrand}
     */
    public void CarBrandListener(String oldVal, String newVal) {

    }

    /**
     * 汽车车型，see{@link com.wwc2.canbus_interface.CanBusDefine.CarType}
     */
    public void CarTypeListener(String oldVal, String newVal) {

    }

    /**
     * can协议类型,see{@link com.wwc2.canbus_interface.CanBusDefine.CanSeries}
     */
    public void CanSeriesListener(Integer oldVal, Integer newVal) {

    }
    /**
     * 实现方式，see{@link com.wwc2.canbus_interface.CanBusDefine.Implementation}
     */
    public void ImplementationListener(String oldVal, String newVal) {

    }

    /**
     * 具体车型支持列表，see {@link com.wwc2.canbus_interface.CanBusDefine.Function}
     */
    public void SupportFunctionListener(String[] oldVal, String[] newVal) {

    }

    /**
     * CanBus 版本信息
     */
    public void CanBusVersionListener(String oldVal, String newVal) {

    }

    /**
     * CanBus 连接状态
     */
    public void CanBusConnectedListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * CAN串口号监听器
     */
    public void CanBusSerialPortListener(String oldVal, String newVal) {

    }
}
