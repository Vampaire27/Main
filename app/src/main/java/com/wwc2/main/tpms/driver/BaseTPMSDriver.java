package com.wwc2.main.tpms.driver;

import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MFloatArray;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.memory.BaseMemoryDriver;

/**
 * the base TPMS driver.
 *
 * @author wwc2
 * @date 2017/1/8
 */
public abstract class BaseTPMSDriver extends BaseMemoryDriver {

    /**
     * TAG
     */
    private static final String TAG = "BaseTPMSDriver";

    /**
     * the model data.
     */
    protected static class TPMSModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString("Version", getVersion().getVal());
            packet.putIntegerObjectArray("TireLocation", getTireLocation().getVal());
            packet.putIntegerObjectArray("TireSensorID", getTireSensorID().getVal());
            packet.putFloatObjectArray("TirePressure", getTirePressure().getVal());
            packet.putIntegerObjectArray("TireTemperature", getTireTemperature().getVal());
            packet.putIntegerObjectArray("TireStatus", getTireStatus().getVal());
            packet.putIntegerObjectArray("TireLeakageStatus", getTireLeakageStatus().getVal());
            packet.putIntegerObjectArray("LowPressure", getLowPressure().getVal());
            packet.putIntegerObjectArray("HighPressure", getHighPressure().getVal());
            packet.putIntegerObjectArray("HighTemperature", getHighTemperature().getVal());
            packet.putIntegerObjectArray("LowVoltage", getLowVoltage().getVal());
            packet.putInt("MaxPressure", getMaxPressure().getVal());
            packet.putInt("MinPressure", getMinPressure().getVal());
            packet.putInt("MaxTemperature", getMaxTemperature().getVal());
            packet.putBoolean("TpmsSwitch", getTpmsSwtich().getVal());
            packet.putBoolean("TpmsConnected", getTpmsConnected().getVal());
            return packet;
        }

        /**
         * TPMS版本号
         */
        private MString mVersion = new MString(this, "VersionListener", null);

        public MString getVersion() {
            return mVersion;
        }

        /**
         * 轮胎位置
         */
        private MIntegerArray mTireLocation = new MIntegerArray(this, "TireLocationListener", new Integer[]{0,1,2,3});

        public MIntegerArray getTireLocation() {
            return mTireLocation;
        }

        /**
         * 轮胎传感器ID
         */
        private MIntegerArray mTireSensorID = new MIntegerArray(this, "TireSensorIDListener", new Integer[]{4660,22136,39612,57072});

        public MIntegerArray getTireSensorID() {
            return mTireSensorID;
        }

        /**
         * 压力值
         */
        private MFloatArray mTirePressure = new MFloatArray(this, "TirePressureListener", new Float[]{240f,240f,240f,240f});

        public MFloatArray getTirePressure() {
            return mTirePressure;
        }

        /**
         * 温度值
         */
        private MIntegerArray mTireTemperature = new MIntegerArray(this, "TireTemperatureListener", new Integer[]{25,25,25,25});

        public MIntegerArray getTireTemperature() {
            return mTireTemperature;
        }

        /**
         * 轮胎状态(信号)
         */
        private MIntegerArray mTireStatus = new MIntegerArray(this, "TireStatusListener", new Integer[]{0,0,0,0});

        public MIntegerArray getTireStatus() {
            return mTireStatus;
        }

        /**
         * 漏气报警
         */
        private MIntegerArray mTireLeakageStatus = new MIntegerArray(this, "TireLeakageStatusListener", new Integer[]{0,0,0,0});

        public MIntegerArray getTireLeakageStatus() {
            return mTireLeakageStatus;
        }
        /**
         * 低压报警
         */
        private MIntegerArray mLowPressure = new MIntegerArray(this, "LowPressureListener", new Integer[]{0,0,0,0});

        public MIntegerArray getLowPressure() {
            return mLowPressure;
        }
        /**
         * 高压报警
         */
        private MIntegerArray mHighPressure = new MIntegerArray(this, "HighPressureListener", new Integer[]{0,0,0,0});

        public MIntegerArray getHighPressure() {
            return mHighPressure;
        }
        /**
         * 高温报警
         */
        private MIntegerArray mHighTemperature = new MIntegerArray(this, "HighTemperatureListener", new Integer[]{0,0,0,0});

        public MIntegerArray getHighTemperature() {
            return mHighTemperature;
        }
        /**
         * 低电量报警
         */
        private MIntegerArray mLowVoltage = new MIntegerArray(this, "LowVoltageListener", new Integer[]{0,0,0,0});

        public MIntegerArray getLowVoltage() {
            return mLowVoltage;
        }
        /**
         * 气压上限
         */
        private MInteger mMaxPressure = new MInteger(this, "MaxPressureListener", 320);

        public MInteger getMaxPressure() {
            return mMaxPressure;
        }
        /**
         * 气压下限
         */
        private MInteger mMinPressure = new MInteger(this, "MinPressureListener", 180);

        public MInteger getMinPressure() {
            return mMinPressure;
        }
        /**
         * 温度上限
         */
        private MInteger mMaxTemperature = new MInteger(this, "MaxTemperatureListener", 65);

        public MInteger getMaxTemperature() {
            return mMaxTemperature;
        }
        /**
         * tpms 开关
         */
        private MBoolean mTpmsSwtich = new MBoolean(this, "TpmsSwtichListener", false);

        public MBoolean getTpmsSwtich() {
            return mTpmsSwtich;
        }
        /**
         * tpms连接状态
         */
        private MBoolean mTpmsConnected = new MBoolean(this, "TpmsConnectedListener", false);

        public MBoolean getTpmsConnected() {
            return mTpmsConnected;
        }


    }

    @Override
    public BaseModel newModel() {
        return new TPMSModel();
    }

    /**
     * get the model object.
     */
    protected TPMSModel Model() {
        TPMSModel ret = null;
        BaseModel model = getModel();
        if (model instanceof TPMSModel) {
            ret = (TPMSModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "TPMSDataConfig.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (mMemory != null) {
            Object object = mMemory.get("TPMS", "Version");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getVersion().setVal(string);
                    ret = true;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final String version = Model().getVersion().getVal();
            if (!TextUtils.isEmpty(version)) {
                mMemory.set("TPMS", "Version", version);
            }
        }
        LogUtils.d(TAG, "writeData ret:" + ret);
        return ret;
    }
}
