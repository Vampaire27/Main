package com.wwc2.main.driver.vrkey;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.vrkey.driver.MTK6737VrKeyDriver;

/**
 * the vr key driver.
 *
 * @author wwc2
 * @date 2017/1/21
 */
public class VrKeyDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "VrKey";

    /**
     * the driver interface.
     */
    public static VrKeyDriverable Driver() {
        VrKeyDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof VrKeyDriverable) {
                ret = (VrKeyDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new MTK6737VrKeyDriver();
    }
}
