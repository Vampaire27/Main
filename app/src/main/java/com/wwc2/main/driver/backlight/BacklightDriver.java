package com.wwc2.main.driver.backlight;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.backlight.driver.MTK6737BacklightDriver;

/**
 * the backlight utils.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class BacklightDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Backlight";

    /**
     * the driver interface.
     */
    public static BacklightDriverable Driver() {
        BacklightDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof BacklightDriverable) {
                ret = (BacklightDriverable) driver;
            }
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new MTK6737BacklightDriver();
    }
}
