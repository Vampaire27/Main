package com.wwc2.main.weather.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.weather_interface.WeatherDefine;

/**
 * the system weather driver.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public class SystemWeatherDriver extends BaseWeatherDriver {
    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        // start weather service.
        LogUtils.d(TAG, "start weather service start.");
        ApkUtils.stopServiceSafety(getMainContext(), WeatherDefine.FLOAT_WINDOW_SERVICE_NAME,
                WeatherDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME, WeatherDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
        ApkUtils.startServiceSafety(getMainContext(), WeatherDefine.FLOAT_WINDOW_SERVICE_NAME,
                WeatherDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME, WeatherDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //zhongyang.hu remove  weather service ,need merge with carnetworker later. 20180907
        // stop weather service.
        LogUtils.d(TAG, "stop weather service start.");
        ApkUtils.stopServiceSafety(getMainContext(), WeatherDefine.FLOAT_WINDOW_SERVICE_NAME,
                WeatherDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME, WeatherDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
    }
}
