package com.wwc2.main.dvr;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr_interface.DVRDefine;
import com.wwc2.dvr_interface.DVRInterface;
import com.wwc2.main.dvr.driver.DVRDriver;
import com.wwc2.main.dvr.driver.DVRDriverable;
import com.wwc2.main.logic.BaseLogic;

/**
 * the dvr logic.
 *
 * @author wwc2
 * @date 2017/1/8
 */
public class DVRLogic extends BaseLogic {

    /**TAG*/
    private static final String TAG = "DVRLogic";

    /**DVR listener.*/
    private BaseListener mDVRListener = new DVRListener() {

        @Override
        public void AutoSaveTimeListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "oleVal = " + oldVal + " newVal = " + newVal);
            Packet packet = new Packet();
            packet.putString(DVRDefine.AUTO_SAVE_TIME, newVal);
            Notify(DVRInterface.MAIN_TO_APK.AUTO_SAVE_TIME, packet);
        }

        @Override
        public void VideoQualityListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "oleVal = " + oldVal + " newVal = " + newVal);
            Packet packet = new Packet();
            packet.putString(DVRDefine.VIDEO_QUALITY, newVal);
            Notify(DVRInterface.MAIN_TO_APK.VIDEO_QUALITY, packet);
        }

        @Override
        public void MuteRecordListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "oleVal = " + oldVal + " newVal = " + newVal);
            Packet packet = new Packet();
            packet.putBoolean(DVRDefine.MUTE_RECORD, newVal);
            Notify(DVRInterface.MAIN_TO_APK.MUTE_RECORD, packet);
        }

        @Override
        public void AutoRecordListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "oleVal = " + oldVal + " newVal = " + newVal);
            Packet packet = new Packet();
            packet.putBoolean(DVRDefine.AUTO_RECORD, newVal);
            Notify(DVRInterface.MAIN_TO_APK.AUTO_RECORD, packet);
        }

        @Override
        public void WatermarkListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "oleVal = " + oldVal + " newVal = " + newVal);
            Packet packet = new Packet();
            packet.putBoolean(DVRDefine.WATERMARK, newVal);
            Notify(DVRInterface.MAIN_TO_APK.WATERMARK, packet);
        }

        @Override
        public void LocationListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "oleVal = " + oldVal + " newVal = " + newVal);
            Packet packet = new Packet();
            packet.putString(DVRDefine.LOCALTION, newVal);
            Notify(DVRInterface.MAIN_TO_APK.LOCALTION, packet);
        }
    };

    @Override
    public String getTypeName() {
        return "DVR";
    }

    @Override
    public String getMessageType() {
        return DVRDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.dvr";
    }

    @Override
    public int firstBoot() {
        int ret = Define.FirstBoot.NO;
        Packet packet = getInfo();
        if (null != packet) {
            boolean auto = packet.getBoolean(DVRDefine.AUTO_RECORD);
            if (auto) {
                ret = Define.FirstBoot.YES;
            }
        }
        return ret;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_DVR;
    }

    @Override
    public BaseDriver newDriver() {
        return new DVRDriver();
    }

    /**
     * the driver interface.
     */
    protected DVRDriverable Driver() {
        DVRDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof DVRDriverable) {
            ret = (DVRDriverable) drive;
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
        getModel().bindListener(mDVRListener);
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        getModel().unbindListener(mDVRListener);
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        if (null != packet)
            switch (nId) {
                case DVRInterface.APK_TO_MAIN.AUTO_SAVE_TIME:
                    String autoSaveTime = packet.getString(DVRDefine.AUTO_SAVE_TIME);
                    LogUtils.d(TAG, "autoSaveTime:" + autoSaveTime);
                    Driver().setAutoSaveTime(autoSaveTime);
                    break;
                case DVRInterface.APK_TO_MAIN.VIDEO_QUALITY:
                    String videoQuality = packet.getString(DVRDefine.VIDEO_QUALITY);
                    LogUtils.d(TAG, "videoQuality:" + videoQuality);
                    Driver().setVideoQualiqy(videoQuality);
                    break;
                case DVRInterface.APK_TO_MAIN.MUTE_RECORD:
                    boolean muteRecord = packet.getBoolean(DVRDefine.MUTE_RECORD);
                    LogUtils.d(TAG, "muteRecord:" + muteRecord);
                    Driver().setMuteRecord(muteRecord);
                    break;
                case DVRInterface.APK_TO_MAIN.AUTO_RECORD:
                    boolean autoRecord = packet.getBoolean(DVRDefine.AUTO_RECORD);
                    LogUtils.d(TAG, "autoRecord:" + autoRecord);
                    Driver().setAutoRecord(autoRecord);
                    break;
                case DVRInterface.APK_TO_MAIN.WATERMARK:
                    boolean watermark = packet.getBoolean(DVRDefine.WATERMARK);
                    LogUtils.d(TAG, "watermark:" + watermark);
                    Driver().setWatermark(watermark);
                    break;
                case DVRInterface.APK_TO_MAIN.LOCALTION:
                    String location = packet.getString(DVRDefine.LOCALTION);
                    LogUtils.d(TAG, "localtion:" + location);
                    Driver().setLocation(location);
                    break;
            }
        return super.dispatch(nId, packet);
    }
}
