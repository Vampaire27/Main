package com.wwc2.main.tpms.driver;

/**
 * the TPMS driver interface.
 *
 * @author wwc2
 * @date 2017/1/8
 */
public interface TPMSDriverable {
    /**
     * 设置气压上限
     */
    void setMaxPressure(Integer pressure);
    /**
     * 设置气压下限
     */
    void setMinPressure(Integer pressure);
    /**
     * 设置温度上限
     */
    void setMaxTemperature(Integer temperature);

    /**
     * 设置温度上限
     */
    void setTireSensorID(int tireLocation, int tireSensorID);

    /**
     * 打开Tpms
     * @param open
     */
    void openTpms(boolean open);

    void activityMain();

    void activitySetting();


    interface DataListener{

        /**
         * 设置气压上限
         */
        void updateMaxPressure(Integer pressure);
        /**
         * 设置气压下限
         */
        void updateMinPressure(Integer pressure);
        /**
         * 设置温度上限
         */
        void updateMaxTemperature(Integer temperature);

        /**
         * 设置版本
         */
        void updateVersion(String version);
        /**
         * 设置传感器ID
         */
        void updateTireSensorID(int index, Integer sensorID);
        /**
         * 设置气压
         */
        void updateTirePressure(int index, Integer pressure);
        /**
         * 设置温度
         */
        void updateTireTemperature(int index, Integer temperature);
        /**
         * 设置低压警告 value:1 警告;value:0 正常;
         */
        void updateLowPressure(int index, Integer value);
        /**
         * 设置高压警告 value:1 警告;value:0 正常;
         */
        void updateHighPressure(int index, Integer value);
        /**
         * 设置高温警告 value:1 警告;value:0 正常;
         */
        void updateHighTemperature(int index, Integer value);
        /**
         * 设置漏气警告 value:1 警告;value:0 正常;
         */
        void updateTireLeakageStatus(int index, Integer value);
        /**
         * 设置低电压警告 value:1 警告;value:0 正常;
         */
        void updateLowVoltage(int index, Integer value);
        /**
         * 设置无信号警告 value:1 警告;value:0 正常;
         */
        void updateTireStatus(int index, Integer value);
    }
}
