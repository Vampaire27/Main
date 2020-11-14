package com.wwc2.main.driver.common;

/**
 * the version driver interface.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public interface CommonDriverable {
    void setBrakeWarning(boolean checked);
    void setReversingVolume(boolean checked);
    void setAnyKey(boolean checked);
    void setKeyTone(boolean checked);
    void setReverseImage(boolean checked);
    void setGpsMonitor(boolean checked);
    void setGpsMix(boolean checked);
    void setGpsMixRatio(int ratio);
    void setNoretain3Party(boolean checked);
    void setMediaJump(boolean checked);
    void beep();

    void getGpsAudioInfo();

    void setColorfulLightSwitch(boolean checked);
    void setSmallLightSwitch(boolean checked);
    void setFlicherSwitch(boolean checked);
    void setFlicherRate(int rate);
    void setColorfulLightColor(int color);
    void setReverseGuideLine(boolean checked);

    void setDefSystemVolume(int volume);
    void setDefCallVolume(int volume);

    String getGprsApkName();
    void setGprsApkName(String apkName);

    String getTpmsApkName();
    void setTpmsApkName(String apkName);

    String getDvrApkName();
    void setDvrApkName(String apkName);

    boolean getAutoLandPort();
    void setAutoLandPort(boolean checked);

    String getStartApkName();
    void setStartApkName(String apkName);

    void setColorfulLightColor3Party(int color);

    void setKeyShake(boolean keyShake);
    void setLightSensitive(boolean lightSensitive);

    void setCameraSwitchTruck(boolean open);

    void setTurnLightSwitch(int type, boolean open);//type:1:左转向 2:右转向
    boolean getTurnLightSwitch(int type);
}
