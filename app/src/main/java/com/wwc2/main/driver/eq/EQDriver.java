package com.wwc2.main.driver.eq;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.eq.driver.STM32EQDriver;

/**
 * the EQ Driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class EQDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "EQ";

    @Override
    public BaseDriver newDriver() {
        return new STM32EQDriver();
    }

    /**
     * the driver interface.
     */
    public static EQDriverable Driver() {
        EQDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof EQDriverable) {
                ret = (EQDriverable) driver;
            }
        }
        return ret;
    }
}
