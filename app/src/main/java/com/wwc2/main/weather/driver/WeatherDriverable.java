package com.wwc2.main.weather.driver;

/**
 * the weather driver interface.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public interface WeatherDriverable {
    /**
     * 天气城市
     */
    public void setCity(String value);

    /**
     * 天气温度
     */
    public void setTemper(String value);

    /**
     * 天气最高温度
     */
    public void setHighTemper(String value);

    /**
     * 天气最低温度
     */
    public void setLowTemper(String value);

    /**
     * 天气内容
     */
    public void setText(String value);

    /**
     * 天气单位
     */
    public void setUnit(Boolean value);
}
