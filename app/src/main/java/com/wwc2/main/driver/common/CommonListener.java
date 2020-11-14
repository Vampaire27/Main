package com.wwc2.main.driver.common;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the version listener.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class CommonListener extends BaseListener {

    @Override
    public String getClassName() {
        return CommonListener.class.getName();
    }

    /**
     * 刹车警告
     * @param oldValue
     * @param newValue
     */
    public void BrakeWarningListener(Boolean oldValue, Boolean newValue){};

    /**
     * 倒车音量
     * @param oldValue
     * @param newValue
     */
    public void ReversingVolumeListener(Boolean oldValue, Boolean newValue){};

    /**
     * 任意按键
     * @param oldValue
     * @param newValue
     */
    public void AnyKeyListener(Boolean oldValue, Boolean newValue){};

    /**
     * 按键音
     * @param oldValue
     * @param newValue
     */
    public void KeyToneListener(Boolean oldValue, Boolean newValue){};

    /**
     * 倒车镜像
     * @param oldValue
     * @param newValue
     */
    public void ReverseImageListener(Boolean oldValue, Boolean newValue){};

    /**
     * GPS监听
     * @param oldValue
     * @param newValue
     */
    public void GpsMonitorListener(Boolean oldValue, Boolean newValue){};

    /**
     * GPS混音
     * @param oldValue
     * @param newValue
     */
    public void GpsMixListener(Boolean oldValue, Boolean newValue){};

    /**
     * GPS混音比例
     * @param oldValue
     * @param newValue
     */
    public void GpsMixRatioListener(Integer oldValue, Integer newValue){};

    /**
     * 不保留第三方
     * @param oldValue
     * @param newValue
     */
    public void Noretain3PartyListener(Boolean oldValue, Boolean newValue){}
    /**
     * 不保留第三方
     * @param oldValue
     * @param newValue
     */
    public void MediaJumpListenerListener(Boolean oldValue, Boolean newValue){}

    /**
     *七彩灯开关
     */
    public void ColorfulLightSwitchListener(Boolean oldValue, Boolean newValue){}

    /**
     * 常开小灯开关
     */
    public void SmallLightSwitchListener(Boolean oldValue, Boolean newValue){}

    /**
     * 闪烁开关
     */
    public void FlicherSwitchListener(Boolean oldValue, Boolean newValue){}

    /**
     * 闪烁频率
     */
    public void FlicherRateListener(Integer oldValue, Integer newValue){}

    /**
     * 七彩灯颜色
     */
    public void ColorfulLightColorListener(Integer oldValue, Integer newValue){}

    /**
     * 七彩灯颜色
     */
    public void ColorfulLightColorListener3Party(Integer oldValue, Integer newValue){}
    /**
     * 倒车引导线开关
     */
    public void reversingGuideLineListener(Boolean oldValue, Boolean newValue) {

    }

    /**
     * 默认系统音量
     */
    public void defSystemVolumeListener(Integer oldValue, Integer newValue) {

    }
    /**
     * 默认通话音量
     */
    public void defCallVolumeListener(Integer oldValue, Integer newValue) {

    }
    /**
     * GPS混音是否支持调节
     * @param oldValue
     * @param newValue
     */
    public void GpsMixSupportListener(Boolean oldValue, Boolean newValue){};

    public void GprsApkNameListener(String oldValue, String newValue) {};

    public void TpmsApkNameListener(String oldValue, String newValue) {};

    public void DvrApkNameListener(String oldValue, String newValue) {};

    /**
     * 自动椣竖屏
     * @param oldValue
     * @param newValue
     */
    public void AutoLandPortListener(Boolean oldValue, Boolean newValue){};

    public void monitorSwitchListener(Integer oldValue, Integer newValue) {};

    public void deviceVoltaleListener(Integer oldValue, Integer newValue) {};

    /**
     * 按键震动
     * @param oldValue
     * @param newValue
     */
    public void KeyShakeListener(Boolean oldValue, Boolean newValue){};

    /**
     * 光感开关
     * @param oldValue
     * @param newValue
     */
    public void LightSensitiveListener(Boolean oldValue, Boolean newValue){};

    /**
     * 倒车开关
     */
    public void cameraSwitchTruckListener(Boolean oldValue, Boolean newValue){};
}
