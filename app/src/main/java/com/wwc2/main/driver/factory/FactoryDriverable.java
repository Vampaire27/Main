package com.wwc2.main.driver.factory;

/**
 * the version driver interface.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public interface FactoryDriverable {
    void setModuleEnable(String module, boolean enable);
    void setRadioRegion(String region);

    void setFactoryPasswd(String passwd);

    void setVoiceChannel(String channel);

    void reloadConfig();

    void setRadioAerial(boolean open);

    void setRadioModule(int module);

    void setRDSEnable(boolean enable);
    boolean getRDSEnable();

    void setScreenSize(int screenSize);

    void requestMcuDebugInfo(int value);

    void setLKLogoIndex(int lkLogoIndex);

    void setVComValue(int vComValue,boolean save);

    void setUISytle(String uiSytle);

    int getUiStyle();

    boolean getUiStyleShow();

//    int[] getUiStyleNumber();

    void setCameraSwitch(boolean sw);
    boolean getCameraSwitch();

    void setPanoramicSwitch(boolean sw);
    boolean getPanoramicSwitch();

    void setPanoramicConnType(int type);
    int getPanoramicConnType();

    void setPanoramicVideoType(int type);
    int getPanoramicVideoType();

    void setPanoramicType(int type);
    int getPanoramicType();

    void setDvrEnable(boolean enable);
    boolean getDvrEnable();

    void setAvdd(int avdd);

    void setScreenBrightness(int brightness);//亮度
    void setScreenColourtemp(int colourtemp);//色温
    void setScreenSaturation(int saturation);//饱和度
    void setScreenContrast(int contrast);//对比度
    void setScreenSharpness(int sharpness);//锐度
    void setScreenDynamiccontrast(int dynamiccontrast);//动态对比
    void setScreenVcom(int vcom);//VCOM
    void setScreenAvdd(int avdd);//AVDD
    void resetScreenParam();
    void saveScreenParam();

    int getScreenBrightness();//亮度
    int getScreenColourtemp();//色温
    int getScreenSaturation();//饱和度
    int getScreenContrast();//对比度
    int getScreenSharpness();//锐度
    int getScreenDynamiccontrast();//动态对比
    int getScreenVcom();//VCOM
    int getScreenAvdd();//AVDD

    boolean getCloseScreen();//获取待机时是否关屏

    boolean getSupportOpenCamera();//获取是否支持打开后视功能

    boolean getIsNoTouchKey();//是否支持触摸按键，false：支持，true：不支持（竖屏）
    boolean getSupportPort();//是否支持横竖屏自动旋转

    void setCameraPower(boolean power);
    boolean getCameraPower();

    byte[] getFanControlData();//获取风扇配置

    String getFactoryPassword();

    //旋转参数设置
    void setRotateVoltage(int voltage);
    int getRotateVoltage();
    void setRotateTime(int time);
    int getRotateTime();

    boolean getSupportPoweroff();//获取是否支持长按太阳图标关机
    boolean getSupportRightCamera();//获取是否支持MCU右视

    int getAuxVideoType();//获取AUX配置的制式
    int getRightVideoType();//获取右视配置的制式

    boolean getSupportFrontCamera();//获取是否支持前视
    void setSupportFrontCamera(boolean supportFrontCamera);

    int getFrontCameraTime();//倒车显示前视的时间
    void setFrontCameraTime(int time);

    boolean getShowOnlineUpgrade();//获取是否显示在线升级
    void setShowOnlineUpgrade(boolean showOnlineUpgrade);

    int getBlueMicGain();//获取蓝牙增益
    void setBlueMicGain(int gain);

    boolean getWakeupPower();
    boolean getSupportAngle();
    boolean getVoiceEnable();

    void setMonitorInfo(int type, int value);//停车监控开关
    int getMonitorInfo(int type);

    void setZhiNengTong(boolean zhiNengTong);
    boolean getZhiNengTong();
    void setVideoDemo(boolean videoDemo);
    boolean getVideoDemo();
}
