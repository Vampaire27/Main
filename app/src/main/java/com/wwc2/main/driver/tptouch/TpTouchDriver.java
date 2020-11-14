package com.wwc2.main.driver.tptouch;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.tptouch.driver.MTK6737TPTouchDriver;

/**
 * the tp touch driver.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public class TpTouchDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "TpTouch";

    /**
     * the driver interface.
     */
    public static TPTouchDriverable Driver() {
        TPTouchDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof TPTouchDriverable) {
                ret = (TPTouchDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new MTK6737TPTouchDriver();
    }
}
