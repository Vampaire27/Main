package com.wwc2.main.weather.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MString;
import com.wwc2.weather_interface.WeatherDefine;

/**
 * the base weather driver.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public abstract class BaseWeatherDriver extends BaseDriver implements WeatherDriverable {

    /**
     * TAG
     */
    protected static final String TAG = "BaseAccoffDriver";

    /**
     * the model data.
     */
    protected static class WeatherModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString(WeatherDefine.WEATHER_CITY, mCity.getVal());
            packet.putString(WeatherDefine.WEATHER_TEMP, mTemper.getVal());
            packet.putString(WeatherDefine.WEATHER_TEMP_H, mHighTemper.getVal());
            packet.putString(WeatherDefine.WEATHER_TEMP_L, mLowTemper.getVal());
            packet.putString(WeatherDefine.WEATHER_TEXT, mText.getVal());
            packet.putBoolean(WeatherDefine.WEATHER_UNIT, mUnit.getVal());
            return packet;
        }

        private MString mCity = new MString(this, "CityListener", "");

        public MString getCity() {
            return mCity;
        }

        private MString mTemper = new MString(this, "TemperListener", "");

        public MString getTemper() {
            return mTemper;
        }

        private MString mHighTemper = new MString(this, "HighTemperListener", "");

        public MString getHighTemper() {
            return mHighTemper;
        }


        private MString mLowTemper = new MString(this, "LowTemperListener", "");

        public MString getLowTemper() {
            return mLowTemper;
        }

        private MString mText = new MString(this, "TextListener", "");

        public MString getText() {
            return mText;
        }

        private MBoolean mUnit = new MBoolean(this, "UnitListener", false);

        public MBoolean getUnit() {
            return mUnit;
        }
    }

    @Override
    public BaseModel newModel() {
        return new WeatherModel();
    }

    /**
     * get the model object.
     */
    protected WeatherModel Model() {
        WeatherModel ret = null;
        BaseModel model = getModel();
        if (model instanceof WeatherModel) {
            ret = (WeatherModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void setCity(String value) {
        Model().getCity().setVal(value);
    }

    @Override
    public void setTemper(String value) {
        Model().getTemper().setVal(value);
    }

    @Override
    public void setHighTemper(String value) {
        Model().getHighTemper().setVal(value);
    }

    @Override
    public void setLowTemper(String value) {
        Model().getLowTemper().setVal(value);
    }

    @Override
    public void setText(String value) {
        Model().getText().setVal(value);
    }

    @Override
    public void setUnit(Boolean value) {
        Model().getUnit().setVal(value);
    }
}
