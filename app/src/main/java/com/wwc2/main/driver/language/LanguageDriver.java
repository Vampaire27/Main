package com.wwc2.main.driver.language;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.language.driver.SystemLanguageDriver;

/**
 * the language driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class LanguageDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Language";

    @Override
    public BaseDriver newDriver() {
        return new SystemLanguageDriver();
    }

    /**
     * the driver interface.
     */
    public static LanguageDriverable Driver() {
        LanguageDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof LanguageDriverable) {
                ret = (LanguageDriverable) driver;
            }
        }
        return ret;
    }
}
