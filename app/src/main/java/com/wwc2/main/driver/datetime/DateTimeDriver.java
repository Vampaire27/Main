package com.wwc2.main.driver.datetime;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.datetime.driver.SystemDateTimeDriver;

/**
 * the date time driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class DateTimeDriver extends Driver {

    /**the driver name.*/
    public static final String DRIVER_NAME = "DateTime";

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

    }

    @Override
    public BaseDriver newDriver() {
        return new SystemDateTimeDriver();
    }

    /**
     * the driver interface.
     */
    public static DateTimeDriverable Driver() {
        DateTimeDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof DateTimeDriverable) {
                ret = (DateTimeDriverable) driver;
            }
        }
        return ret;
    }
}
