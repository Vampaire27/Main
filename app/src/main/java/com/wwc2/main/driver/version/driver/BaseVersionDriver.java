package com.wwc2.main.driver.version.driver;

import android.content.Context;
import android.text.TextUtils;

import com.wwc2.audio_interface.AudioDefine;
import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.camera_interface.CameraDefine;
import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.launcher_interface.LauncherDefine;
import com.wwc2.main.SDKVersion;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.common.CommonDriverable;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.factory.FactoryDriverable;
import com.wwc2.main.driver.version.VersionDriverable;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.video_interface.VideoDefine;

import java.util.List;

/**
 * the base version driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class BaseVersionDriver extends BaseDriver implements VersionDriverable {

    /**数据Model*/
    protected class VersionModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet ret = new Packet();
            ret.putString(SettingsDefine.Version.HARDWARE   , getHardVersion());
            ret.putString(SettingsDefine.Version.SYSTEM     , getSystemVersion());
            ret.putString(SettingsDefine.Version.MODULE_BLUETOOTH, getBluetoothVersion());
            ret.putString(SettingsDefine.Version.KERNEL     , getKernelVersion());
            ret.putString(SettingsDefine.Version.MCU        , getMcuVersion());
            ret.putString(SettingsDefine.Version.APP        , getAPPVersion());
            ret.putString(SettingsDefine.Version.CLINET     , getClientProject());
            ret.putString(SettingsDefine.Version.CANBUS     , getCanBusVersion());

            ret.putString(SettingsDefine.Version.MODELNUMBER, getModelNumber());
            ret.putString(SettingsDefine.Version.UNKNOWN    , getUnknownString());
            ret.putString(SettingsDefine.Version.FIREWARE   , getFirewareVersion());
            ret.putString(SettingsDefine.Version.BASEBAND   , getBasebandVersion());

            ret.putString(SettingsDefine.Version.MAIN       , getAPPMainVersion());
//            ret.putString(SettingsDefine.Version.MAINSDK    , getAPPSDKVersion());
            ret.putString(SettingsDefine.Version.NAVIGATION , getApkVersion(NaviDefine.MODULE));
            ret.putString(SettingsDefine.Version.LAUNCHER   , getApkVersion(LauncherDefine.MODULE));
            ret.putString(SettingsDefine.Version.RADIO      , getApkVersion(RadioDefine.MODULE));
            ret.putString(SettingsDefine.Version.APP_BLUETOOTH, getApkVersion(BluetoothDefine.MODULE));
            ret.putString(SettingsDefine.Version.AUDIO_FREQ , getApkVersion(AudioDefine.MODULE));
            ret.putString(SettingsDefine.Version.VIDEO      , getApkVersion(VideoDefine.MODULE));
            ret.putString(SettingsDefine.Version.IMAGE      , getUnknownString());
            ret.putString(SettingsDefine.Version.AUX        , getApkVersion(AuxDefine.MODULE));
            ret.putString(SettingsDefine.Version.ASTERN     , getApkVersion(CameraDefine.MODULE));
            ret.putString(SettingsDefine.Version.SETTINGS   , getApkVersion(SettingsDefine.MODULE));
            return ret;
        }
    }

    @Override
    public BaseModel newModel() {
        return new VersionModel();
    }

    /**
     * get the model object.
     */
    protected VersionModel Model() {
        VersionModel ret = null;
        BaseModel model = getModel();
        if (model instanceof VersionModel) {
            ret = (VersionModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public String getUnknownString() {
        return "Unknown";
    }

    @Override
    public String getBluetoothVersion() {
        String ret = null;
        Packet packet = ModuleManager.getLogicByName(BluetoothDefine.MODULE).getInfo();
        if (null != packet) {
            ret = packet.getString("Version");
        }
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }
        return ret;
    }

    @Override
    public String getClientProject() {
        String ret = null;
        Packet packet = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        if (null != packet) {
            ret = packet.getString("ClientProject");
        }
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }
        return ret;
    }

    @Override
    public String getAPPVersion() {
        String ret = null;
        Packet packet = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        if (null != packet) {
            ret = packet.getString("ClientVersion");
        }
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }

        FactoryDriverable driverable = FactoryDriver.Driver();
        if (driverable != null) {
            if (driverable.getMonitorInfo(2) == 1) {//配置为24V时，默认在APP版本号上显示"B"
                ret += "B";
            }
        }
        return ret;
    }

    @Override
    public long getAPPVersionID() {
        long ret = 0L;
        Packet packet = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        if (null != packet) {
            ret = packet.getLong("ClientVersionID");
        }
        return ret;
    }

    @Override
    public String getAPPMainVersion() {
        String ret = null;
        Context context = getMainContext();
        if (null != context) {
            String packet = context.getPackageName();
            ret = ApkUtils.getApkVersion(context, packet);
        }
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }
        return ret;
    }

    @Override
    public String getAPPSDKVersion() {
        return SDKVersion.getSDKVersion();
    }

    @Override
    public String getApkVersion(String name) {
        String ret = null;
        BaseLogic logic = ModuleManager.getLogicByName(name);
        if (null != logic) {
            try {
                String packet = logic.getAPKPacketName();
                ret = ApkUtils.getApkVersion(getMainContext(), packet);
                if (TextUtils.isEmpty(ret)) {
                    List<String> packets = logic.getAPKPacketList();
                    if (null != packets) {
                        for (int i = 0; i < packets.size(); i++) {
                            ret = ApkUtils.getApkVersion(getMainContext(), packets.get(i));
                            if (!TextUtils.isEmpty(ret)) {
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }
        return ret;
    }

    @Override
    public String getCanBusVersion() {
        String ret = null;
        Packet packet = ModuleManager.getLogicByName(CanBusDefine.MODULE).getInfo();
        if (null != packet) {
            ret = packet.getString("CanBusVersion");
        }
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }
        return ret;
    }
}
