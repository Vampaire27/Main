package com.wwc2.main.manager;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.main.canbus.CanBusManager;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.datetime.DateTimeDriver;
import com.wwc2.main.driver.ime.IMEDriver;
import com.wwc2.main.driver.info.InfoDriver;
import com.wwc2.main.driver.language.LanguageDriver;
import com.wwc2.main.driver.network.NetworkDriver;
import com.wwc2.main.silent.SilentDefine;

/**
 * the config manager.
 *
 * @author wwc2
 * @date 2017/1/5
 */
public class ConfigManager {

    /**
     * 系统配置路径
     */
    private static String mSystemConfigDir = "/custom/";

    /**
     * 获取系统配置路径
     */
    public static String getSystemConfigDir() {
        return mSystemConfigDir;
    }

    /**
     * 设置系统配置路径
     */
    public static void setSystemConfigDir(String dir) {
        mSystemConfigDir = dir;
    }

    /**
     * onCreate
     */
    public static void onCreate(Packet packet) {
        // register the driver.
        DriverManager.regDriver(DateTimeDriver.DRIVER_NAME, new DateTimeDriver());
        DriverManager.regDriver(LanguageDriver.DRIVER_NAME, new LanguageDriver());
        DriverManager.regDriver(NetworkDriver.DRIVER_NAME, new NetworkDriver());
        DriverManager.regDriver(InfoDriver.DRIVER_NAME, new InfoDriver());
        DriverManager.regDriver(IMEDriver.DRIVER_NAME, new IMEDriver());
        DriverManager.regDriver(ClientDriver.DRIVER_NAME, new ClientDriver());
//        DriverManager.regDriver(BacklightDriver.DRIVER_NAME, new BacklightDriver());
//        DriverManager.regDriver(VolumeDriver.DRIVER_NAME, new VolumeDriver());
//        DriverManager.regDriver(EQDriver.DRIVER_NAME, new EQDriver());
//        DriverManager.regDriver(SteerDriver.DRIVER_NAME, new SteerDriver());
//        DriverManager.regDriver(VersionDriver.DRIVER_NAME, new VersionDriver());
//        DriverManager.regDriver(VrKeyDriver.DRIVER_NAME, new VrKeyDriver());
//        DriverManager.regDriver(McuDriver.DRIVER_NAME, new McuDriver());
//        DriverManager.regDriver(TpTouchDriver.DRIVER_NAME, new TpTouchDriver());
//        DriverManager.regDriver(StorageDriver.DRIVER_NAME, new StorageDriver());
//        DriverManager.regDriver(FactoryDriver.DRIVER_NAME, new FactoryDriver());
//        DriverManager.regDriver(CommonDriver.DRIVER_NAME, new CommonDriver());
//        DriverManager.regDriver(AudioDriver.DRIVER_NAME, new AudioDriver());
//        DriverManager.regDriver(SystemDriver.DRIVER_NAME, new SystemDriver());
//        DriverManager.regDriver(DebugDriver.DRIVER_NAME, new DebugDriver());

        // register the logic.
        LogicManager.regLogic(SilentDefine.MODULE);
        CanBusManager.register();

//        LogicManager.regLogic(Define.MODULE);
//        LogicManager.regLogic(EventInputDefine.MODULE);
//        LogicManager.regLogic(NaviDefine.MODULE);
//        LogicManager.regLogic(ThirdpartyDefine.MODULE);
//        LogicManager.regLogic(LauncherDefine.MODULE);
//        LogicManager.regLogic(SettingsDefine.MODULE);
//        LogicManager.regLogic(PoweroffDefine.MODULE);
//        LogicManager.regLogic(AccoffDefine.MODULE);
//        LogicManager.regLogic(PhonelinkDefine.MODULE);
//        LogicManager.regLogic(RadioDefine.MODULE);
//        LogicManager.regLogic(BluetoothDefine.MODULE);
//        LogicManager.regLogic(AuxDefine.MODULE);
//        LogicManager.regLogic(CameraDefine.MODULE);
//        LogicManager.regLogic(StandbyDefine.MODULE);
//        LogicManager.regLogic(VideoDefine.MODULE);
//        LogicManager.regLogic(AudioDefine.MODULE);
//        LogicManager.regLogic(McuDefine.MODULE);
//        LogicManager.regLogic(SystemDefine.MODULE);
//        LogicManager.regLogic(MainUIDefine.MODULE);
//        LogicManager.regLogic(SystemPermissionDefine.MODULE);
//        LogicManager.regLogic(SettingsDefine.MODULE);
//        LogicManager.regLogic(VoiceAssistantDefine.MODULE);
//        LogicManager.regLogic(WeatherDefine.MODULE);
//        LogicManager.regLogic(DVRDefine.MODULE);
//        LogicManager.regLogic(TPMSDefine.MODULE);
//        LogicManager.regLogic(TVDefine.MODULE);
//        LogicManager.regLogic(IRDVRDefine.MODULE);
    }

    /**
     * onDestroy
     */
    public static void onDestroy() {
        // unregister the logic.
        LogicManager.unregLogics();

        // unregister the driver.
        DriverManager.unregDrivers();
    }
}
