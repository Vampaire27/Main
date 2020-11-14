package com.wwc2.main.driver.audio;

import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.audio.driver.SystemAudioHandleDriver;

/**
 * the audio driver.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public class AudioDriver extends Driver {

    /**
     * the driver name.
     */
    public static final String DRIVER_NAME = "Audio";

    @Override
    public BaseDriver newDriver() {
        return new SystemAudioHandleDriver();
    }

    /**
     * the driver interface.
     */
    public static AudioDriverable Driver() {
        AudioDriverable ret = null;
        Driver ins = DriverManager.getDriverByName(DRIVER_NAME);
        if (null != ins) {
            BaseDriver driver = ins.getDriver();
            if (driver instanceof AudioDriverable) {
                ret = (AudioDriverable) driver;
            }
        }
        return ret;
    }
}
