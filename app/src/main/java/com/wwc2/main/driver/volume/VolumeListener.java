package com.wwc2.main.driver.volume;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the volume listener.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class VolumeListener extends BaseListener {

    @Override
    public String getClassName() {
        return VolumeListener.class.getName();
    }

    /**音量类型变化监听器*/
    public void VolumeTypeListener(Integer oldVal, Integer newVal) {

    }

    /**音量的源:来自Mcu的为1,来自App的为0*/
    public void VolumeResourceListener(Integer oldVal, Integer newVal) {

    }

    /**音量变化监听器*/
    public void VolumeValueListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**音量静音状态变化监听器*/
    public void VolumeMuteListener(Boolean oldVal, Boolean newVal) {

    }

    /**音量最大值变化监听器*/
    public void VolumeMaxListener(Integer oldVal, Integer newVal) {

    }

    /**声音面板是否显示监听器*/
    public void VolumeShowListener(Boolean oldVal, Boolean newVal) {

    }

    /**ARM是否出声监听器*/
    public void ArmActiveListener(Boolean oldVal, Boolean newVal) {

    }

    /**GPS是否出声监听器*/
    public void GpsActiveListener(Boolean oldVal, Boolean newVal) {

    }

    /**Gps是否混音监听器*/
    public void GpsMixFlagListener(Boolean oldVal, Boolean newVal) {

    }

    /**Gps监听状态监听器*/
    public void GpsMonitorListener(Boolean oldVal, Boolean newVal) {

    }

    /**Gps混音比例监听器*/
    public void GpsMixScaleListener(Integer oldVal, Integer newVal) {

    }
}
