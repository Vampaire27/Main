package com.wwc2.main.media;

import android.content.ComponentName;
import android.content.Intent;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.audio.AudioDriverable;
import com.wwc2.main.driver.audio.driver.BaseAudioDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.storage.StorageDriver;
import com.wwc2.main.driver.storage.StorageDriverable;
import com.wwc2.main.driver.storage.StorageListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.media.driver.MediaDriverable;
import com.wwc2.main.settings.util.ClickFilter;
import com.wwc2.media_interface.MediaDefine;
import com.wwc2.main.manager.SourceManager;

/**
 * the media abstract logic.
 *
 * @author wwc2
 * @date 2017/1/12
 */
public abstract class MediaLogic extends BaseLogic {

    /**
     * TAG
     */
    private static final String TAG = "MediaLogic";

    private Driver mStorageDriver;

    /**
     * main to apk id
     */
    protected abstract int getStorageMethod();

    /**
     * the audio control interface.
     */
    private AudioDriverable.AudioControlable mAudioControl = new AudioDriverable.AudioControlable() {
        @Override
        public void pause() {
            Driver().pause();
        }

        @Override
        public void play() {
            Driver().play();
        }

        @Override
        public boolean isPlay() {
            return isMusicPlay();
        }
    };
    private StorageListener mStorageListener = new StorageListener() {
        @Override
        public void StorageListListener(IntegerSSBoolean[] oldVal, IntegerSSBoolean[] newVal) {
            LogUtils.d(TAG, "StoragesMountListener:" + "newVal:" + (newVal == null ? "null" : newVal.length));
            sendStorageArray(newVal);
        }
    };

    private void sendStorageArray(IntegerSSBoolean[] newVal) {
        Packet mPacket = new Packet();
        mPacket.putParcelableArray("Storages", newVal);
        Notify(getStorageMethod(), mPacket);
    }


    @Override
    public boolean isSource() {
        return true;
    }

    /**
     * the driver interface.
     */
    protected MediaDriverable Driver() {
        MediaDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof MediaDriverable) {
            ret = (MediaDriverable) drive;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        LogUtils.d(TAG, "MediaLogic onCreate!");
        super.onCreate(packet);
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.setMainContext(getMainContext());
            driver.onCreate(packet);

        }
        mStorageDriver = DriverManager.getDriverByName(StorageDriver.DRIVER_NAME);
        if (mStorageDriver != null) {
            mStorageDriver.getModel().bindListener(mStorageListener);
        }
        sendStorageArray(StorageDriver.Driver().getStoragesInfo());
    }

    @Override
    public Packet getInfo() {
        Packet ret = super.getInfo();
        if (null == ret) {
            ret = new Packet();
        }
        StorageDriverable storageDriverable = StorageDriver.Driver();
        if (storageDriverable != null) {
            ret.putParcelableArray("Storages", storageDriverable.getStoragesInfo());
            IntegerSSBoolean[] issb = storageDriverable.getStoragesInfo();
            if (issb != null) {
                for (int i = 0; i < issb.length; i++) {
                    LogUtils.d("MediaLogic getInfo[" + i + "]newVal Integer:" + issb[i].getInteger() +
                            ",String1:" + issb[i].getString1() + ",String2:" + issb[i].getString2() +
                            ",Boolean:" + issb[i].getBoolean());
                }
            } else {
                LogUtils.d("issb is null");
            }
        } else {
            LogUtils.d("storageDriverable is null");
        }
        return ret;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 申请音频
        AudioDriver.Driver().request(mAudioControl,
                AudioDefine.AudioStream.STREAM_MUSIC,
                AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        AudioDriver.Driver().abandon();
        Driver().pause();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }
        super.onDestroy();
        if (mStorageDriver != null) {
            mStorageDriver.getModel().unbindListener(mStorageListener);
        }
    }

    /**
     * 播放或者暂停
     */
    private boolean isMusicPlay() {
        boolean ret = false;
        Packet packet = getInfo();
        if (null != packet) {
            ret = MediaDefine.PlayStatus.isPlay(packet.getInt("PlayStatus"));
        }
        return ret;
    }

    private boolean isCalling() {
        BaseLogic logic = ModuleManager.getLogicByName(BluetoothDefine.MODULE);
        if (null != logic) {
            Packet data = logic.getInfo();
            if (null != data) {
                int hfp = data.getInt("HFPStatus");
                if (BluetoothDefine.HFPStatus.isCalling(hfp)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {

        boolean ret = true;
        LogUtils.d(TAG, "key:" + Define.Key.toString(key));
        if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
            LogUtils.d("media logic onKeyEvent diabale for acc off mode.");
            return ret;
        }

        switch (key) {
            case Define.Key.KEY_PREV:
            case Define.Key.KEY_CH_DEC:
            case Define.Key.KEY_FB:
            case Define.Key.KEY_SCAN_DEC:
            case Define.Key.KEY_DIRECT_LEFT:
                if (!ClickFilter.filter(300L) && !isCalling()) {//bug13454 通话时不处理方控按键。2018-10-24
                    Driver().prev();
                }
                break;
            case Define.Key.KEY_NEXT:
            case Define.Key.KEY_CH_INC:
            case Define.Key.KEY_FF:
            case Define.Key.KEY_SCAN_INC:
            case Define.Key.KEY_DIRECT_RIGHT:
                if (!ClickFilter.filter(300L) && !isCalling()) {//bug13454 通话时不处理方控按键。2018-10-24
                    Driver().next();
                }
                break;
            case Define.Key.KEY_PAUSE:
                if (!isCalling()) {//bug13454 通话时不处理方控按键。2018-10-24
                    BaseAudioDriver.pauseByVoice(true);
                    Driver().pause();
                }
                break;
            case Define.Key.KEY_PLAY:
                if (!isCalling()) {//bug13454 通话时不处理方控按键。2018-10-24
                    Driver().play();
                }
                break;
            case Define.Key.KEY_PLAYPAUSE:
                if (ClickFilter.filter(300L) || isCalling()) {//bug13454 通话时不处理方控按键。2018-10-24
                    return false;
                }
                int playMode = getInfo().getInt("PlayStatus");
                if (MediaDefine.PlayStatus.isPause(playMode)) {
                    Driver().play();
                } else {
                    Driver().pause();
                }
                break;
            case Define.Key.KEY_STOP:
                Driver().stop();
                break;
            case Define.Key.KEY_AUDIO_RPT:
                Driver().playMode(MediaDefine.PlayMode.changePlayModeWithoutRand(getInfo().getInt("PlayMode")));
                break;
            case Define.Key.KEY_AUDIO_RDM:
                Driver().playMode(MediaDefine.PlayMode.changePlayModeByRand(getInfo().getInt("PlayMode")));
                break;
            default:
                ret = false;
                break;
        }
        // }
        return ret;
    }

    @Override
    public boolean runApk() {
        if (ApkUtils.isAPKExist(getMainContext(), getAPKPacketName())) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            final String strClassName = ApkUtils.getAPKLaunchClassName(getMainContext(), getAPKPacketName());
            if (null != strClassName) {
                ComponentName comp = new ComponentName(getAPKPacketName(), strClassName);
                intent.setComponent(comp);
            }
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            getMainContext().startActivity(intent);
            LogUtils.d(TAG, "runApk Logic by:" + source());
            return true;
        }
        return super.runApk();
    }

    protected void sendTimeInfoToMcu(int time) {
        long hour = 0;
        long minute = 0;
        long second = 0;

        time = time / 1000;

        if (time > 0) {
            minute = time / 60;
            hour = minute / 60;
            minute = minute % 60;
            second = time - hour * 3600 - minute * 60;

//            LogUtils.d("sendTimeInfoToMcu---hour=" + hour + ", minute=" + minute + ", second=" + second);
            byte[] data = new byte[3];
            data[0] = (byte) (hour & 0xFF);
            data[1] = (byte) (minute & 0xFF);
            data[2] = (byte) (second & 0xFF);
            McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.OP_MEDIA_TIME_INFO, data, data.length);
        }
    }
}









