package com.wwc2.main.driver.steer;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.steer.driver.STM32SteerDriver;

/**
 * the steer driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class SteerDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Steer";

    /**
     * the driver interface.
     */
    public static SteerDriverable Driver() {
        SteerDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof SteerDriverable) {
                ret = (SteerDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new STM32SteerDriver();
    }
}
