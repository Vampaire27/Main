package com.wwc2.main.driver.version;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.version.driver.MTK6737VersionDriver;

/**
 * the version drvier.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class VersionDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Version";

    /**
     * the driver interface.
     */
    public static VersionDriverable Driver() {
        VersionDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof VersionDriverable) {
                ret = (VersionDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new MTK6737VersionDriver();
    }
}
