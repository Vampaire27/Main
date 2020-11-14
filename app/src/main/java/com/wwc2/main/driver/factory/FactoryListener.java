package com.wwc2.main.driver.factory;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the version listener.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class FactoryListener extends BaseListener {

    @Override
    public String getClassName() {
        return FactoryListener.class.getName();
    }

    /**
     * 导航
     * @param oldValue
     * @param newValue
     */
    public void NavigationListener(Boolean oldValue, Boolean newValue){};

    /**
     * 收音机
     * @param oldValue
     * @param newValue
     */
    public void RadioListener(Boolean oldValue, Boolean newValue){};

    /**
     * 蓝牙
     * @param oldValue
     * @param newValue
     */
    public void BluetoothListener(Boolean oldValue, Boolean newValue){};

    /**
     * 音频
     * @param oldValue
     * @param newValue
     */
    public void AudioFrequencyListener(Boolean oldValue, Boolean newValue){};

    /**
     * 视频
     * @param oldValue
     * @param newValue
     */
    public void VideoListener(Boolean oldValue, Boolean newValue){};

    /**
     * 图片
     * @param oldValue
     * @param newValue
     */
    public void PictureListener(Boolean oldValue, Boolean newValue){};

    /**
     * AUX
     * @param oldValue
     * @param newValue
     */
    public void AuxListener(Boolean oldValue, Boolean newValue){};

    /**
     * 语音助手
     * @param oldValue
     * @param newValue
     */
    public void VoiceAssistantListener(Boolean oldValue, Boolean newValue){};

    /**
     * 收音机区域
     * @param oldValue
     * @param newValue
     */
    public void RadioRegionListener(String oldValue, String newValue){};

    /**
     * 工厂设置密码
     * @param oldValue
     * @param newValue
     */
    public void FactoryPasswdListener(String oldValue, String newValue){};

    /**
     * 收音有源天线开关设置
     * @param oldValue
     * @param newValue
     */
    public void RadioAerialListener(boolean oldValue, boolean newValue){};

    /**
     * 收音模块类型设置
     * @param oldValue
     * @param newValue
     */
    public void RadioModuelListener(Integer oldValue, Integer newValue){};

    /**
     * RDS开关
     */
    public void RDSEnableListener(Boolean oldValue, Boolean newValue){};

    /**
     * 屏幕尺寸设置
     * @param oldValue
     * @param newValue
     */
    public void ScreenSizeListener(Integer oldValue, Integer newValue){};

    /**
     * 方向盘AD值
     */
    public void SteerADValueListener(Integer oldValue, Integer newValue) {};

    /**
     * MCU调试信息
     * @param oldValue
     * @param newValue
     */
    public void McuDebugInfoListener(Integer[] oldValue, Integer[] newValue){};  /**

     * LK logo index
     * @param oldValue
     * @param newValue
     */
    public void lkLogoIndexListener(Integer oldValue, Integer newValue){};

    /**
     * VCOM设置
     */
    public void setVComListener(Integer oldValue, Integer newValue) {

    }

    public void cameraSwitchListener(Boolean oldValue, Boolean newValue) {

    }

    public void panoramicSwitchListener(Boolean oldValue, Boolean newValue) {

    }

    public void panoramicConnTypeListener(Integer oldValue, Integer newValue) {

    }

    public void panoramicVideoTypeListener(Integer oldValue, Integer newValue) {

    }

    public void panoramicTypeListener(Integer oldValue, Integer newValue) {

    }

    public void dvrEnableListener(Boolean oldValue, Boolean newValue) {

    }

    public void uiStyleListener(Integer oldValue, Integer newValue) {

    }

    public void uiStyleDateListener(String oldValue, String newValue) {

    }

    public void uiStyleShowListener(Boolean oldValue, Boolean newValue) {

    }

    public void uiStyleNumberListener(Integer[] oldValue, Integer[] newValue) {

    }

    public void uiStyleDateArrayListener(String[] oldValue, String[] newValue) {

    }

    public void uiStyleNumberListener(String[] oldValue, String[] newValue) {

    }

    public void uiStyleNameListener(String oldValue, String newValue) {

    }
    public void avddListener(Integer oldValue, Integer newValue) {

    }

    public void screenBrightnessListener(Integer oldValue, Integer newValue) {

    }
    public void screenColourtempListener(Integer oldValue, Integer newValue) {

    }
    public void screenSaturationListener(Integer oldValue, Integer newValue) {

    }
    public void screenContrastListener(Integer oldValue, Integer newValue) {

    }
    public void screenSharpnessListener(Integer oldValue, Integer newValue) {

    }
    public void screenDynamiccontrastListener(Integer oldValue, Integer newValue) {

    }
    public void screenVcomListener(Integer oldValue, Integer newValue) {

    }
    public void screenAvddListener(Integer oldValue, Integer newValue) {

    }
    public void rotateVoltageListener(Integer oldValue, Integer newValue) {

    }
    public void rotateTimeListener(Integer oldValue, Integer newValue) {

    }

    public void panelKeyListener(Byte[] oldValue, Byte[] newValue) {

    }

    public void blueMicGainListener(Integer oldValue, Integer newValue) {

    }
}
