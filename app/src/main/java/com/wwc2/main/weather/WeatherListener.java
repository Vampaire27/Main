package com.wwc2.main.weather;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the weather listener.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public class WeatherListener extends BaseListener {
    @Override
    public String getClassName() {
        return WeatherListener.class.getName();
    }

    /**
     * 天气城市
     */
    public void CityListener(String oldVal, String newVal) {

    }

    /**
     * 天气温度
     */
    public void TemperListener(String oldVal, String newVal) {

    }

    /**
     * 天气最高温度
     */
    public void HighTemperListener(String oldVal, String newVal) {

    }

    /**
     * 天气最低温度
     */
    public void LowTemperListener(String oldVal, String newVal) {

    }

    /**
     * 天气内容
     */
    public void TextListener(String oldVal, String newVal) {

    }

    /**
     * 天气单位
     */
    public void UnitListener(Boolean oldVal, Boolean newVal) {

    }
}
