package com.wwc2.main.driver.client;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.client.driver.SystemClientDriver;

/**
 * the client driver.
 *
 * @author wwc2
 * @date 2017/1/19
 */
public class ClientDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "CLIENT";

    @Override
    public BaseDriver newDriver() {
        return new SystemClientDriver();
    }

    /**
     * the driver interface.
     */
    public static ClientDriverable Driver() {
        ClientDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof ClientDriverable) {
                ret = (ClientDriverable) driver;
            }
        }
        return ret;
    }
}
