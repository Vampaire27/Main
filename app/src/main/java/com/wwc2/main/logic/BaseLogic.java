package com.wwc2.main.logic;

import android.content.Context;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.utils.apk.ApkUtils;

import java.util.List;

/**
 * the base logic.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public abstract class BaseLogic extends CoreLogic implements Logicable {

    @Override
    public String getMessageType() {
        return null;
    }

    @Override
    public BaseDriver newDriver() {
        return null;
    }

    @Override
    public boolean funcModule() {
        return true;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public int firstBoot() {
        return Define.FirstBoot.DEFAULT;
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    public boolean isStackSource() {
        return isSource();
    }

    @Override
    public boolean isPoweroffSource() {
        return false;
    }

    @Override
    public boolean isScreenoffSource() {
        return false;
    }

    @Override
    public boolean isFullScreenSource() {
        return isPoweroffSource();
    }

    @Override
    public boolean isVolumeHideSource() {
        return isPoweroffSource();
    }

    @Override
    public boolean isVoiceHideSource() {
        return isPoweroffSource();
    }

    @Override
    public boolean isHFPFloatHideSource() {
        return isPoweroffSource();
    }

    @Override
    public boolean handleBacklightOn() {
        return false;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_NONE;
    }

    @Override
    public boolean enable() {
        return funcModule() && isAPKExist();
    }

    @Override
    public boolean available() {
        return (enable() && isReady());
    }

    @Override
    public boolean passive() {
        return false;
    }

    @Override
    public boolean runApk() {
        return false;
    }

    @Override
    public String getAPKPacketName() {
        return null;
    }

    @Override
    public String getAPKClassName() {
        return null;
    }

    @Override
    public List<String> getAPKPacketList() {
        return null;
    }

    @Override
    public boolean isEnterAlwaysPackage() {
        return false;
    }

    @Override
    public Packet onModuleEvent(int id, Packet packet) {
        return null;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        return false;
    }

    @Override
    public boolean onStatusEvent(int type, boolean status, Packet packet) {
        return false;
    }

    /**
     * 得到该模块的APK是否存在
     *
     * @return 该模块的APK是否存在
     */
    public final boolean isAPKExist() {
        Context context = getMainContext();
        boolean exist = ApkUtils.isAPKExist(context, getAPKPacketName());

        if (!exist) {
            List<String> list = getAPKPacketList();
            if (null != list) {
                for (int i = 0; i < list.size(); i++) {
                    String string = list.get(i);
                    if (ApkUtils.isAPKExist(context, string)) {
                        exist = true;
                        break;
                    }
                }
            }
        }
        return exist;
    }
}
