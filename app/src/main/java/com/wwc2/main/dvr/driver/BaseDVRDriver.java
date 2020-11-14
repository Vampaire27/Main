package com.wwc2.main.dvr.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr_interface.DVRDefine;
import com.wwc2.main.driver.memory.BaseMemoryDriver;

/**
 * the base dvr driver.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public abstract class BaseDVRDriver extends BaseMemoryDriver implements DVRDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseDVRDriver";

    /**
     * the model data.
     */
    protected static class DVRModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString(DVRDefine.AUTO_SAVE_TIME, mAutoSaveTime.getVal());
            packet.putString(DVRDefine.VIDEO_QUALITY, mVideoQuality.getVal());
            packet.putBoolean(DVRDefine.MUTE_RECORD, mMuteRecord.getVal());
            packet.putBoolean(DVRDefine.AUTO_RECORD, mAutoRecord.getVal());
            packet.putBoolean(DVRDefine.WATERMARK, mWatermark.getVal());
            packet.putString(DVRDefine.LOCALTION, mLocation.getVal());
            return packet;
        }

        private MString mAutoSaveTime = new MString(this, "AutoSaveTimeListener", "5min");
        public MString getAutoSaveTime() {
            return mAutoSaveTime;
        }

        private MString mVideoQuality = new MString(this, "VideoQualityListener", "low");
        public MString getVideoQuality() {
            return mVideoQuality;
        }

        private MBoolean mMuteRecord = new MBoolean(this, "MuteRecordListener", false);
        public MBoolean getMuteRecord() {
            return mMuteRecord;
        }

        private MBoolean mAutoRecord = new MBoolean(this, "AutoRecordListener", false);
        public MBoolean getAutoRecord() {
            return mAutoRecord;
        }

        private MBoolean mWatermark = new MBoolean(this, "WatermarkListener", true);
        public MBoolean getWatermark() {
            return mWatermark;
        }

        private MString mLocation = new MString(this, "LocationListener", "sd");
        public MString getLocation() {
            return mLocation;
        }
    }

    @Override
    public BaseModel newModel() {
        return new DVRModel();
    }

    /**
     * get the model object.
     */
    protected DVRModel Model() {
        DVRModel ret = null;
        BaseModel model = getModel();
        if (model instanceof DVRModel) {
            ret = (DVRModel) model;
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
        return "DVRDataConfig.ini";
    }

    @Override
    public void setAutoSaveTime(String selection) {
        Model().getAutoSaveTime().setVal(selection);
    }

    @Override
    public void setVideoQualiqy(String selection) {
        Model().getVideoQuality().setVal(selection);
    }

    @Override
    public void setMuteRecord(boolean selection) {
        Model().getMuteRecord().setVal(selection);
    }

    @Override
    public void setAutoRecord(boolean selection) {
        Model().getAutoRecord().setVal(selection);
    }

    @Override
    public void setWatermark(boolean selection) {
        Model().getWatermark().setVal(selection);
    }

    @Override
    public void setLocation(String selection) {
        Model().getLocation().setVal(selection);
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (mMemory != null) {
            Object autoSavetime = mMemory.get("DVR", DVRDefine.AUTO_SAVE_TIME);
            if (null != autoSavetime) {
                Model().getAutoSaveTime().setVal((String) autoSavetime);
                LogUtils.d(TAG, "readData:" + "autoSavetime:" + autoSavetime);
                ret = true;
            }
            Object videoQuality = mMemory.get("DVR", DVRDefine.VIDEO_QUALITY);
            if (null != videoQuality) {
                Model().getVideoQuality().setVal((String) videoQuality);
                LogUtils.d(TAG, "readData:" + "videoQuality:" + videoQuality);
                ret = true;
            }
            Object muteRecord = mMemory.get("DVR", DVRDefine.MUTE_RECORD);
            if (null != muteRecord) {
                Model().getMuteRecord().setVal(Boolean.parseBoolean((String) muteRecord));
                LogUtils.d(TAG, "readData:" + "muteRecord:" + muteRecord);
                ret = true;
            }
            Object autoRecord = mMemory.get("DVR", DVRDefine.AUTO_RECORD);
            if (null != autoRecord) {
                Model().getAutoRecord().setVal(Boolean.parseBoolean((String) autoRecord));
                LogUtils.d(TAG, "readData:" + "autoRecord:" + autoRecord);
                ret = true;
            }
            Object watermark = mMemory.get("DVR", DVRDefine.WATERMARK);
            if (null != watermark) {
                Model().getWatermark().setVal(Boolean.parseBoolean((String) watermark));
                LogUtils.d(TAG, "readData:" + "watermark:" + watermark);
                ret = true;
            }
            Object location = mMemory.get("DVR", DVRDefine.LOCALTION);
            if (null != location) {
                Model().getLocation().setVal((String) location);
                LogUtils.d(TAG, "readData:" + "localtion:" + location);
                ret = true;
            }

        }
        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final String autoSaveTime = Model().getAutoSaveTime().getVal();
            mMemory.set("DVR", DVRDefine.AUTO_SAVE_TIME, autoSaveTime);
            final String videoQuality = Model().getVideoQuality().getVal();
            mMemory.set("DVR", DVRDefine.VIDEO_QUALITY, videoQuality);
            final boolean muteRecord = Model().getMuteRecord().getVal();
            mMemory.set("DVR", DVRDefine.MUTE_RECORD, muteRecord+"");
            final boolean autoRecord = Model().getAutoRecord().getVal();
            mMemory.set("DVR", DVRDefine.AUTO_RECORD, autoRecord+"");
            final boolean watermark = Model().getWatermark().getVal();
            mMemory.set("DVR", DVRDefine.WATERMARK, watermark+"");
            final String location = Model().getLocation().getVal();
            mMemory.set("DVR", DVRDefine.LOCALTION, location);
            ret = true;
            LogUtils.d(TAG,"autoSaveTime:" + autoSaveTime + "\tvideoQuality:" + videoQuality + "\tmuteRecord:" + muteRecord + "\tautoRecord:" + autoRecord + "\twatermark:" + watermark + "\tlocation:" + location);
        }
        LogUtils.d(TAG, "writeData ret:" + ret);
        return ret;
    }
}
