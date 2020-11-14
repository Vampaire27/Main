package com.wwc2.main.driver.factory;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.factory.driver.STM32FactoryDriver;

/**
 * the version drvier.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class FactoryDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "FactoryDriver";

    /**
     * the driver interface.
     */
    public static FactoryDriverable Driver() {
        FactoryDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof FactoryDriverable) {
                ret = (FactoryDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new STM32FactoryDriver();
    }
}
