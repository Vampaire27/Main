package com.wwc2.main.driver.common;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.common.driver.STM32CommonDriver;

/**
 * the version drvier.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class CommonDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "SettingsCommon";

    @Override
    public BaseDriver newDriver() {
        return new STM32CommonDriver();
    }

    /**
     * the driver interface.
     */
    public static CommonDriverable Driver() {
        CommonDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof CommonDriverable) {
                ret = (CommonDriverable) driver;
            }
        }
        return ret;
    }
}
