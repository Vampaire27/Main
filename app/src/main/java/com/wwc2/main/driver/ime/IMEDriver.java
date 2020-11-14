package com.wwc2.main.driver.ime;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.ime.driver.SystemIMEDriver;

/**
 * the ime driver.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class IMEDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "IME";

    @Override
    public BaseDriver newDriver() {
        return new SystemIMEDriver();
    }

    /**
     * the driver interface.
     */
    public static IMEDriverable Driver() {
        IMEDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof IMEDriverable) {
                ret = (IMEDriverable) driver;
            }
        }
        return ret;
    }
}
