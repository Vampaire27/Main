package com.wwc2.main.driver.info;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.info.driver.BaseInfoSystemDriver;

/**
 * the device info driver.
 *
 * @author wwc2
 * @date 2017/1/26
 */
public class InfoDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Info";

    @Override
    public BaseDriver newDriver() {
        return new BaseInfoSystemDriver();
    }

    /**
     * the driver interface.
     */
    public static InfoDriverable Driver() {
        InfoDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof InfoDriverable) {
                ret = (InfoDriverable) driver;
            }
        }
        return ret;
    }
}
