package com.wwc2.main.driver.network;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.network.driver.BaseNetworkDriver;

/**
 * the network driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class NetworkDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "Network";

    @Override
    public BaseDriver newDriver() {
        return new BaseNetworkDriver();
    }

    /**
     * the driver interface.
     */
    public static NetworkDriverable Driver() {
        NetworkDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof NetworkDriverable) {
                ret = (NetworkDriverable) driver;
            }
        }
        return ret;
    }
}
