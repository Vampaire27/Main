package com.wwc2.main.radio.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.model.MString;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.radio_interface.RadioDefine;

/**
 * the base radio module.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public abstract class BaseRadioDriver extends BaseDriver implements RadioDriverable {

    /**
     * the model data.
     */
    protected static class RadioModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_BAND, mBand.getVal());
            packet.putInt(RadioDefine.KeyMain.KEY_FREQ, mFreq.getVal());
            packet.putInt(RadioDefine.KeyMain.KEY_WORK, mWork.getVal());
//            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_FREQ_SAVE_ON, mFreqSaveOn.getVal());
            packet.putInt(RadioDefine.KeyMain.KEY_FREQ_SAVE_ON, mFreqSaveOn.getVal());
            packet.putString(RadioDefine.KeyMain.KEY_PS_INFO, mPSInfo.getVal());
            packet.putBoolean(RadioDefine.KeyMain.KEY_RADIO_SWITCH, mSwitchEnable.getVal());
            packet.putBoolean(RadioDefine.KeyMain.KEY_STEREO_SWITCH, mSTSwitch.getVal());
            packet.putBoolean(RadioDefine.KeyMain.KEY_ST_ENABLE, mSTState.getVal());
            packet.putBoolean(RadioDefine.KeyMain.KEY_LOC_ENABLE, mLOCEnable.getVal());
            packet.putBoolean(RadioDefine.KeyMain.KEY_RDS_ENABLE, mRDSEnable.getVal());
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_FM_FREQ_ARRAY, mFMFreqArray.getVal());
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_AM_FREQ_ARRAY, mAMFreqArray.getVal());
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_FM_REGION_ARRAY, mFMFreqRegionArray.getVal());
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_AM_REGION_ARRAY, mAMFreqRegionArray.getVal());

            //fm send
            packet.putBoolean(RadioDefine.KeyMain.KEY_FMSEND_SWITCH, mFmSendSwitch.getVal());
            packet.putInt(RadioDefine.KeyMain.KEY_FMSEND_INDEX, mFmSendIndex.getVal());
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_FMSEND_FREQ, mFmSendFreqArray.getVal());

            //RDS
            if (FactoryDriver.Driver() != null) {
                packet.putBoolean(RadioDefine.KeyMain.KEY_RDS_ENABLE, FactoryDriver.Driver().getRDSEnable());
            }
            packet.putBoolean(RadioDefine.KeyMain.KEY_RDS_TA, mRDSTaSwitch.getVal());
            packet.putBoolean(RadioDefine.KeyMain.KEY_RDS_AF, mRDSAfSwitch.getVal());
            packet.putString(RadioDefine.KeyMain.KEY_RDS_STATION, mRDSStation.getVal());
            packet.putInt(RadioDefine.KeyMain.KEY_RDS_STATION_PTY, mRDSStationType.getVal());
            packet.putBoolean("rds_tp_enable", mRDSTPSwitch.getVal());
            packet.putBoolean("rds_traffic", mRDSTraffic.getVal());
            packet.putBoolean("rds_signal", mRDSSignal.getVal());
            packet.putBoolean("rds_no_pty", mRDSNoPty.getVal());
            packet.putInt("rds_seek_status", mRDSSeekStatus.getVal());
            return packet;
        }

        /**
         * 当前波段.
         */
        private MInteger mBand = new MInteger(this, "BandListener", RadioDefine.Band.DEFAULT);

        public MInteger getBand() {
            return mBand;
        }

        /**
         * 当前频率.
         */
        private MInteger mFreq = new MInteger(this, "FreqListener", 8750);

        public MInteger getFreq() {
            return mFreq;
        }

        /**
         * 工作状态,正常0，搜索1，浏览2
         */
        private MInteger mWork = new MInteger(this, "WorkListener", RadioDefine.Band.DEFAULT);

        public MInteger getWork() {
            return mWork;
        }


        /*
        static Integer[] FREQSAVEON_ARRAY = new Integer[5];
        static {
            for (int i = 0;i < FREQSAVEON_ARRAY.length;i++) {
                FREQSAVEON_ARRAY[i] = 0;
            }
        }
       private MIntegerArray mFreqSaveOn = new MIntegerArray(this, "FrequenceSaveOnListener", FREQSAVEON_ARRAY);
        public MIntegerArray getFreqSaveOn() {
            return mFreqSaveOn;
        }*/

        /**
         * 选中的.
         */
        private MInteger mFreqSaveOn = new MInteger(this, "FrequenceSaveOnListener", 0);

        public MInteger getFreqSaveOn() {
            return mFreqSaveOn;
        }

        /**
         * PS信息.
         */
        private MString mPSInfo = new MString(this, "PSInfoListener", "");

        public MString getPSInfo() {
            return mPSInfo;
        }

        /**
         * 立体声状态.
         */
        private MBoolean mSTState = new MBoolean(this, "STStateListener", false);

        public MBoolean getSTState() {
            return mSTState;
        }

        /**
         * 立体声功能开关.
         */
        private MBoolean mSTSwitch = new MBoolean(this, "STSwitchListener", false);

        public MBoolean getmSTSwitch() {
            return mSTSwitch;
        }

        /**
         * 收音功能开关.
         */
        private MBoolean mSwitchEnable = new MBoolean(this, "SwitchEnableListener", true);

        public MBoolean getSwitchEnable() {
            return mSwitchEnable;
        }

        /**
         * 远近程功能开关.
         */
        private MBoolean mLOCEnable = new MBoolean(this, "LOCEnableListener", false);

        public MBoolean getLOCEnable() {
            return mLOCEnable;
        }

        /**
         * RDS开关.
         */
        private MBoolean mRDSEnable = new MBoolean(this, "RDSEnableListener", false);

        public MBoolean getRDSEnable() {
            return mRDSEnable;
        }

        /**
         * FM保存频率数组.
         */
        static Integer[] FM_FREQ_ARRAY = new Integer[RadioDefine.FM_MAX];

        static {
            for (int i = 0; i < RadioDefine.FM_MAX; i++) {
                FM_FREQ_ARRAY[i] = 8750;
            }
        }

        /**
         * FM电台.
         */
        private MIntegerArray mFMFreqArray = new MIntegerArray(this, "FMFreqArrayListener", RadioDefine.fmDefaultReq);

        public MIntegerArray getFMFreqArray() {
            return mFMFreqArray;
        }

        /**
         * AM电台.
         */
        private MIntegerArray mAMFreqArray = new MIntegerArray(this, "AMFreqArrayListener", RadioDefine.amDefaultReq);

        public MIntegerArray getAMFreqArray() {
            return mAMFreqArray;
        }

        /**
         * FM电台区域频率.
         */
        private MIntegerArray mFMFreqRegionArray = new MIntegerArray(this, "FMFreqRegionListener", RadioDefine.FM_REGION);

        public MIntegerArray getFMFreqRegionArray() {
            return mFMFreqRegionArray;
        }

        /**
         * AMFMFreqRegionArray.
         */
        private MIntegerArray mAMFreqRegionArray = new MIntegerArray(this, "AMFreqRegionListener", RadioDefine.AM_REGION);

        public MIntegerArray getAMFreqRegionArray() {
            return mAMFreqRegionArray;
        }

        /**
         * Fm发射开关.
         */
        private MBoolean mFmSendSwitch = new MBoolean(this, "FmSendSwitchListener", false);

        public MBoolean getFmSendSwitch() {
            return mFmSendSwitch;
        }

        /**
         * Fm发射频率ID.
         */
        private MInteger mFmSendIndex = new MInteger(this, "FmSendIndexListener", 0);

        public MInteger getFmSendIndex() {
            return mFmSendIndex;
        }

        /**
         * Fm发射频率点.
         */
        private MIntegerArray mFmSendFreqArray = new MIntegerArray(this, "FmSendFreqArrayListener", null);

        public MIntegerArray getFmSendFreqArray() {
            return mFmSendFreqArray;
        }

        /**
         * TA
         */
        private MBoolean mRDSTaSwitch = new MBoolean(this, "RDSTaSwitchListener", false);

        public MBoolean getRDSTaSwitch() {
            return mRDSTaSwitch;
        }

        /**
         * AF
         */
        private MBoolean mRDSAfSwitch = new MBoolean(this, "RDSAfSwitchListener", false);

        public MBoolean getRDSAfSwitch() {
            return mRDSAfSwitch;
        }

        /**
         * RDS电台名
         */
        private MString mRDSStation = new MString(this, "RDSStationListener", "");

        public MString getRDSStationInfo() {
            return mRDSStation;
        }

        /**
         * RDS节目类型
         */
        private MInteger mRDSStationType = new MInteger(this, "RDSStationTypeListener", 0);

        public MInteger getRDSStationType() {
            return mRDSStationType;
        }

        /**
         * TP
         */
        private MBoolean mRDSTPSwitch = new MBoolean(this, "RDSTPSwitchListener", false);

        public MBoolean getRDSTPSwitch() {
            return mRDSTPSwitch;
        }

        /**
         * traffic
         */
        private MBoolean mRDSTraffic = new MBoolean(this, "RDSTrafficListener", false);

        public MBoolean getRDSTraffic() {
            return mRDSTraffic;
        }

        /**
         * RDS signal
         */
        private MBoolean mRDSSignal = new MBoolean(this, "RDSSignalListener", false);

        public MBoolean getRDSSignal() {
            return mRDSSignal;
        }

        /**
         * RDS NO PTY
         */
        private MBoolean mRDSNoPty = new MBoolean(this, "RDSNoPtyListener", false);

        public MBoolean getRDSNoPty() {
            return mRDSNoPty;
        }

        /**
         * RDS seek status
         */
        private MInteger mRDSSeekStatus = new MInteger(this, "RDSSeekStatusListener", 0);

        public MInteger getRDSSeekStatus() {
            return mRDSSeekStatus;
        }

        private MInteger mAngle = new MInteger(this,"AngleListener",0);

        public MInteger getAngle() {
            return mAngle;
        }
    }

    @Override
    public BaseModel newModel() {
        return new RadioModel();
    }

    /**
     * get the model object.
     */
    protected RadioModel Model() {
        RadioModel ret = null;
        BaseModel model = getModel();
        if (model instanceof RadioModel) {
            ret = (RadioModel) model;
        }
        return ret;
    }
}
