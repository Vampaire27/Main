package com.wwc2.main.tpms;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.tpms.driver.TPMSDriverable;
import com.wwc2.tpms_interface.TPMSDefine;
import com.wwc2.tpms_interface.TPMSInterface;

/**
 * the tpms logic.
 *
 * @author wwc2
 * @date 2017/1/8
 */
public class BaseTPMSLogic extends BaseLogic {

    /**
     * TAG
     */
    private static final String TAG = "BaseTPMSLogic";

    @Override
    public String getTypeName() {
        return "TPMS";
    }

    @Override
    public String getMessageType() {
        return TPMSDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.tpms";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.tpms.MainActivity";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_TPMS;
    }

    protected  TPMSListener mListener = new TPMSListener(){
        @Override
        public void VersionListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "Version change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putString("Version", newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TPMS_VERSION, packet);
        }

        @Override
        public void TireLocationListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "TireLocation change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("TireLocation",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TPMS_TIRE_LOCATION, packet);
        }

        @Override
        public void TireSensorIDListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "TireSensorID change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("TireSensorID",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TPMS_TIRESENSOR_ID, packet);
        }

        @Override
        public void TirePressureListener(Float[] oldVal, Float[] newVal) {
            LogUtils.d(TAG, "TirePressure change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putFloatObjectArray("TirePressure",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TIRE_PRESSURE, packet);
        }

        @Override
        public void TireTemperatureListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "TireTemperature change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("TireTemperature",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TIRE_TEMPERATURE, packet);
        }

        @Override
        public void TireStatusListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "TireStatus change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("TireTemperature",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TIRE_TEMPERATURE, packet);
        }

        @Override
        public void TireLeakageStatusListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "TireLeakageStatus change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("TireLeakageStatus",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TIRE_LEAKAGE_STATUS, packet);
        }

        @Override
        public void LowPressureListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "LowPressure change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("LowPressure",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.LOW_PRESSURE, packet);
        }

        @Override
        public void HighPressureListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "HighPressure change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("HighPressure",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.HIGH_PRESSURE, packet);
        }

        @Override
        public void HighTemperatureListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "HighTemperature change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("HighTemperature",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.HIGH_TEMPERATURE, packet);
        }

        @Override
        public void LowVoltageListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "LowVoltage change, oldVal = " + oldVal + ", newVal = " + (newVal == null ? "null": newVal.toString()));
            Packet packet = new Packet();
            packet.putIntegerObjectArray("LowPressure",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.LOW_VOLTAGE, packet);
        }


        @Override
        public void MaxPressureListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MaxPressure change, oldVal = " + oldVal + ", newVal = " + newVal.toString());
            Packet packet = new Packet();
            packet.putInt("MaxPressure",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TPMS_MAX_PRESSURE, packet);
        }

        @Override
        public void MinPressureListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MinPressure change, oldVal = " + oldVal + ", newVal = " + newVal.toString());
            Packet packet = new Packet();
            packet.putInt("MinPressure",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TPMS_MIN_PRESSURE, packet);
        }

        @Override
        public void MaxTemperatureListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MaxTemperature change, oldVal = " + oldVal + ", newVal = " + newVal.toString());
            Packet packet = new Packet();
            packet.putInt("MaxTemperature",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TTPMS_MAX_TEMPERATURE, packet);
        }

        @Override
        public void TpmsSwtichListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "TpmsSwtich change, oldVal = " + oldVal + ", newVal = " + newVal.toString());
            Packet packet = new Packet();
            packet.putBoolean("TpmsSwtich",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TPMS_SWTICH, packet);
        }

        @Override
        public void TpmsConnectedListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "TpmsConnected change, oldVal = " + oldVal + ", newVal = " + newVal.toString());
            Packet packet = new Packet();
            packet.putBoolean("TpmsConnected",newVal);
            Notify(false, TPMSInterface.MAIN_TO_APK.TPMS_CONNECTED, packet);
        }
    };

    @Override
    public Packet dispatch(int nId, Packet packet) {
        switch(nId){
            case TPMSInterface.APK_TO_MAIN.TPMS_MAX_PRESSURE:
                int maxPressure = packet.getInt("MaxPressure");
                LogUtils.d(TAG, "set maxPressure:" + maxPressure);
                Driver().setMaxPressure(maxPressure);
                break;
            case TPMSInterface.APK_TO_MAIN.TPMS_MIN_PRESSURE:
                int minPressure = packet.getInt("MinPressure");
                LogUtils.d(TAG, "set minPressure:" + minPressure);
                Driver().setMinPressure(minPressure);
                break;
            case TPMSInterface.APK_TO_MAIN.TPMS_MAX_TEMPERATURE:
                int maxTemperature = packet.getInt("MaxTemperature");
                LogUtils.d(TAG, "set maxTemperature:" + maxTemperature);
                Driver().setMaxTemperature(maxTemperature);
                break;
            case TPMSInterface.APK_TO_MAIN.TPMS_NOTIFY_TIRESENSORID:
                int tireLocation = packet.getInt("TireLocation");
                int tireSensorID = packet.getInt("TireSensorID");
                LogUtils.d(TAG, "set tireLocation:" + tireLocation + " tireSensorID:" + tireSensorID);
                Driver().setTireSensorID(tireLocation,tireSensorID);
                break;
            case TPMSInterface.APK_TO_MAIN.ACITVITY_QUERY:
                Object obj = packet.getSerializable(TPMSDefine.STRING_TAG);
                if (obj instanceof TPMSDefine.StringTag) {
                    switch ((TPMSDefine.StringTag)obj) {
                        case ACTIVITY_MAIN:
                            Driver().activityMain();
                            LogUtils.d(TAG,"activityMain");
                            break;
                        case ACTIVITY_SETTING:
                            Driver().activitySetting();
                            LogUtils.d(TAG,"activitySetting");
                            break;
                    }
                }

                break;
        }
        return super.dispatch(nId, packet);
    }

    /**
     * the driver interface.
     */
    protected TPMSDriverable Driver() {
        TPMSDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof TPMSDriverable) {
            ret = (TPMSDriverable) drive;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        getModel().bindListener(mListener);
    }

    @Override
    public void onDestroy() {
        getModel().unbindListener(mListener);
        super.onDestroy();
    }
}
