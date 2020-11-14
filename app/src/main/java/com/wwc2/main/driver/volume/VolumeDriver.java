package com.wwc2.main.driver.volume;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.volume.driver.STM32VolumeDriver;

/**
 * the volume driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class VolumeDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Volume";

    /**
     * the driver interface.
     */
    public static VolumeDriverable Driver() {
        VolumeDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof VolumeDriverable) {
                ret = (VolumeDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new STM32VolumeDriver();
    }
}
