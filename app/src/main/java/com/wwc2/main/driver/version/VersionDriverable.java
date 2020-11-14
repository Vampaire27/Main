package com.wwc2.main.driver.version;

/**
 * the version driver interface.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public interface VersionDriverable {

    /**获取未知字符串*/
    String getUnknownString();

    /**android型号*/
    String getModelNumber();

    /**硬件版本*/
    String getHardVersion();

    /**android版本*/
    String getFirewareVersion();

    /**基带版本*/
    String getBasebandVersion();

    /**内核版本*/
    String getKernelVersion();

    /**系统版本号*/
    String getSystemVersion();

    /**MCU版本号*/
    String getMcuVersion();

    /**蓝牙版本号*/
    String getBluetoothVersion();

    /**客户项目号*/
    String getClientProject();

    /**APP版本号*/
    String getAPPVersion();

    /**APP版本号ID*/
    long getAPPVersionID();

    /**APP MAIN版本号*/
    String getAPPMainVersion();

    /**APP SDK版本号*/
    String getAPPSDKVersion();

    /**APK版本号, name指模块名字，比如蓝牙为{@link com.wwc2.bluetooth_interface.BluetoothDefine#MODULE}*/
    String getApkVersion(String name);

    /**can盒版本号*/
    String getCanBusVersion();
}
