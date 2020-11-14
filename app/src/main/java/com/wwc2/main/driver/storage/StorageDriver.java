package com.wwc2.main.driver.storage;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.storage.driver.MTK6737StorageDriver;

/**
 * the storage driver.
 *
 * @author wwc2
 * @date 2017/1/29
 */
public class StorageDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Storage";

    /**
     * the driver interface.
     */
    public static StorageDriverable Driver() {
        StorageDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof StorageDriverable) {
                ret = (StorageDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new MTK6737StorageDriver();
    }
}
