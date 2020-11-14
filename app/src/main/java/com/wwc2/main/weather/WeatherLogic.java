package com.wwc2.main.weather;

import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.weather.driver.SystemWeatherDriver;
import com.wwc2.main.weather.driver.WeatherDriverable;
import com.wwc2.weather_interface.WeatherDefine;
import com.wwc2.weather_interface.WeatherInterface;

/**
 * the weather logic.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public class WeatherLogic extends BaseLogic {

    private static final String TAG = "WeatherLogic";
    private WeatherListener mWeatherListener = new WeatherListener() {
        /**
         * 天气城市
         */
        @Override
        public void CityListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "CityListener " + "oldVal:" + oldVal + "\t newVal:" + newVal);
            Packet packet = new Packet();
            packet.putString(WeatherDefine.WEATHER_CITY, newVal);
            Notify(WeatherInterface.MAIN_TO_APK.WEATHER_CITY, packet);
        }

        /**
         * 天气温度
         */
        @Override
        public void TemperListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "TemperListener " + "oldVal:" + oldVal + "\t newVal:" + newVal);
            Packet packet = new Packet();
            packet.putString(WeatherDefine.WEATHER_TEMP, newVal);
            Notify(WeatherInterface.MAIN_TO_APK.WEATHER_TEMP, packet);
        }

        /**
         * 天气最高温度
         */
        @Override
        public void HighTemperListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "HighTemperListener " + "oldVal:" + oldVal + "\t newVal:" + newVal);
            Packet packet = new Packet();
            packet.putString(WeatherDefine.WEATHER_TEMP_H, newVal);
            Notify(WeatherInterface.MAIN_TO_APK.WEATHER_TEMP_H, packet);
        }

        /**
         * 天气最低温度
         */
        @Override
        public void LowTemperListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "LowTemperListener " + "oldVal:" + oldVal + "\t newVal:" + newVal);
            Packet packet = new Packet();
            packet.putString(WeatherDefine.WEATHER_TEMP_L, newVal);
            Notify(WeatherInterface.MAIN_TO_APK.WEATHER_TEMP_L, packet);
        }

        /**
         * 天气内容
         */
        @Override
        public void TextListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "TextListener " + "oldVal:" + oldVal + "\t newVal:" + newVal);
            Packet packet = new Packet();
            packet.putString(WeatherDefine.WEATHER_TEXT, newVal);
            Notify(WeatherInterface.MAIN_TO_APK.WEATHER_TEXT, packet);
        }

        /**
         * 天气单位
         */
        @Override
        public void UnitListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "UnitListener " + "oldVal:" + oldVal + "\t newVal:" + newVal);
            Packet packet = new Packet();
            packet.putBoolean(WeatherDefine.WEATHER_UNIT, newVal);
            Notify(WeatherInterface.MAIN_TO_APK.WEATHER_UNIT, packet);
        }
    };

    @Override
    public String getTypeName() {
        return "Weather";
    }

    @Override
    public String getMessageType() {
        return WeatherDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.weather";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.weather.WeatherActivity";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_WEATHER;
    }


    @Override
    public BaseDriver newDriver() {
        return new SystemWeatherDriver();
    }

    /**
     * the driver interface.
     */
    protected WeatherDriverable Driver() {
        WeatherDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof WeatherDriverable) {
            ret = (WeatherDriverable) driver;
        }

        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }
        getModel().bindListener(mWeatherListener);
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        getModel().unbindListener(mWeatherListener);
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        switch (nId) {
            case WeatherInterface.APK_TO_MAIN.WEATHER_RESULT:
                String city = packet.getString(WeatherDefine.WEATHER_CITY);
                String temper = packet.getString(WeatherDefine.WEATHER_TEMP);
                String highTemper = packet.getString(WeatherDefine.WEATHER_TEMP_H);
                String lowTemper = packet.getString(WeatherDefine.WEATHER_TEMP_L);
                String text = packet.getString(WeatherDefine.WEATHER_TEXT);
                boolean isFahrenheit = packet.getBoolean(WeatherDefine.WEATHER_UNIT);

                //天气数据正常时
                if (null != city && temper != null && highTemper != null && lowTemper != null && text != null) {

                    Driver().setCity(city);
                    Driver().setText(text);
                    if (isFahrenheit) {
                        Driver().setTemper(c2f(Integer.valueOf(temper)) + "");
                        Driver().setHighTemper(c2f(Integer.valueOf(highTemper)) + "");
                        Driver().setLowTemper(c2f(Integer.valueOf(lowTemper)) + "");
                    } else {
                        Driver().setTemper(temper);
                        Driver().setHighTemper(highTemper);
                        Driver().setLowTemper(lowTemper);
                    }

                } else {   //天气数据不正常或只有温度单位时

                    //温度单位改变时
                    String temper1 = getInfo().getString(WeatherDefine.WEATHER_TEMP);
                    String highTemper1 = getInfo().getString(WeatherDefine.WEATHER_TEMP_H);
                    String lowTemper1 = getInfo().getString(WeatherDefine.WEATHER_TEMP_L);
                    boolean unit1 = getInfo().getBoolean(WeatherDefine.WEATHER_UNIT);
                    if(TextUtils.isEmpty(temper1)) {
                        temper1 = WeatherDefine.DEFAULT_WEATHER_TEMP;
                    }
                    if(TextUtils.isEmpty(highTemper1)) {
                        highTemper1 = WeatherDefine.DEFAULT_WEATHER_TEMP_H;
                    }
                    if(TextUtils.isEmpty(lowTemper1)) {
                        lowTemper1 = WeatherDefine.DEFAULT_WEATHER_TEMP_L;
                    }
                    //摄氏度转华氏度
                    if (isFahrenheit && (!unit1)) {
                        Driver().setTemper(c2f(Integer.valueOf(temper1)) + "");
                        Driver().setHighTemper(c2f(Integer.valueOf(highTemper1)) + "");
                        Driver().setLowTemper(c2f(Integer.valueOf(lowTemper1)) + "");
                    }
                    //华氏度转摄氏度
                    if (!isFahrenheit && unit1) {
                        Driver().setTemper(f2c(Integer.valueOf(temper1)) + "");
                        Driver().setHighTemper(f2c(Integer.valueOf(highTemper1)) + "");
                        Driver().setLowTemper(f2c(Integer.valueOf(lowTemper1)) + "");
                    }
                }

                Driver().setUnit(isFahrenheit);
                break;
            default:
                break;
        }
        return super.dispatch(nId, packet);
    }

    /**
     * 摄氏温度转华氏温度
     */
    private int c2f(int tC) {
        return (int) (9 * tC / 5+ + 32.5f);
    }

    /**
     * 华氏温度转摄氏温度
     */
    private int f2c(int tW) {
        return (int) ((tW - 32) * 5 / 9+0.5);
    }


}
