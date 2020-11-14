package com.wwc2.main.driver.mcu;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;

/**
 * the McuDriver.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public class McuDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "MCU";

    /**
     * the driver interface.
     */
    public static McuDriverable Driver() {
        McuDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof McuDriverable) {
                ret = (McuDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new STM32MCUDriver();
    }
}
