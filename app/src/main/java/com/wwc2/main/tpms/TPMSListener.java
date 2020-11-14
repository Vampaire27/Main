package com.wwc2.main.tpms;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the tpms listener.
 *
 * @author wwc2
 * @date 2017/1/8
 */
public class TPMSListener extends BaseListener {

    @Override
    public String getClassName() {
        return TPMSListener.class.getName();
    }

    /**
     * TPMS版本号变化监听器
     */
    public void VersionListener(String oldVal, String newVal) {

    }

    /**
     * 轮胎位置变化监听器
     */
    public void TireLocationListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * 轮胎传感器ID变化监听器
     */
    public void TireSensorIDListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * 压力值变化监听器
     */
    public void TirePressureListener(Float[] oldVal, Float[] newVal) {

    }

    /**
     * 温度值变化监听器
     */
    public void TireTemperatureListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * 轮胎状态变化监听器
     */
    public void TireStatusListener(Integer[] oldVal, Integer[] newVal) {

    }
    /**
     * 漏气状态变化监听器
     */
    public void TireLeakageStatusListener(Integer[] oldVal, Integer[] newVal) {

    }
    /**
     * 低气压状态变化监听器
     */
    public void LowPressureListener(Integer[] oldVal, Integer[] newVal) {

    }
    /**
     * 高气压状态变化监听器
     */
    public void HighPressureListener(Integer[] oldVal, Integer[] newVal) {

    }
    /**
     * 高温状态变化监听器
     */
    public void HighTemperatureListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * 低电压状态变化监听器
     */
    public void LowVoltageListener(Integer[] oldVal, Integer[] newVal) {

    }
    /**
     * 气压上限
     */
    public void MaxPressureListener(Integer oldVal, Integer newVal) {

    }
    /**
     * 气压下限
     */
    public void MinPressureListener(Integer oldVal, Integer newVal) {

    }
    /**
     * 温度上限
     */
    public void MaxTemperatureListener(Integer oldVal, Integer newVal) {

    }
    /**
     * tpms开关
     */
    public void TpmsSwtichListener(Boolean oldVal, Boolean newVal) {

    }
    /**
     * tpms连接状态
     */
    public void TpmsConnectedListener(Boolean oldVal, Boolean newVal) {

    }
}
