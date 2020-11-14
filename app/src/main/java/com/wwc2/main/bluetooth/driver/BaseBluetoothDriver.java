package com.wwc2.main.bluetooth.driver;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MLong;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerSSS;
import com.wwc2.corelib.model.custom.MIntegerSSArray;
import com.wwc2.corelib.model.custom.MIntegerSSBooleanArray;
import com.wwc2.corelib.model.custom.MIntegerSSSArray;
import com.wwc2.corelib.utils.format.TimeFormat;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.camera.CameraLogic;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.audio.AudioDriverable;
import com.wwc2.main.driver.memory.BaseMemoryContentDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;

/**
 * 蓝牙驱动基类.
 *
 * @author wwc2
 * @date 2017/1/5
 */
public abstract class BaseBluetoothDriver extends BaseMemoryContentDriver implements BluetoothDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseBluetoothDriver";

    /**
     * 是否由用户关闭蓝牙标志
     */
    public boolean isClosedByUser = false;

    /**
     * resume the connect.
     */
    private boolean mResumeConnect = false;

    /**
     * 是否要恢复蓝牙音乐标志
     */
    private boolean mResumeMusicPlay = false;
    /**
     * 开机延时连接蓝牙
     */
    private TimerQueue mPoweronConnectTimerQueue = new TimerQueue(), mDelayPlayMusic = new TimerQueue();

    protected int mLatestCallType = BluetoothDefine.CallType.CALL_TYPE_UNKNOWN;

    /**
     * 进入蓝牙时延时播放蓝牙音乐的定时器
     */
    protected int delayMusicTimerID = -1;

    /**
     * the model data.
     */
    protected static class BluetoothModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putInt("ModuleStatus", mModuleStatus.getVal());
            packet.putInt("HFPStatus", mHFPStatus.getVal());
            packet.putString("LocalName", mLocalName.getVal());
            packet.putString("LocalMacAddr", mLocalMacAddr.getVal());
            packet.putString("PinCode", mPinCode.getVal());
            packet.putBoolean("AutoConnect", mAutoConnect.getVal());
            packet.putBoolean("AutoAnswer", mAutoAnswer.getVal());
            packet.putString("PairingName", mPairingName.getVal());
            packet.putString("PairingMacAddr", mPairingMacAddr.getVal());
            packet.putLong("CallTime", mCallTime.getVal());
            packet.putString("CallNumber", mCallNumber.getVal());
            packet.putInt("MusicStatus", mMusicStatus.getVal());
            packet.putString("MusicArtistName", mMusicArtistName.getVal());
            packet.putString("MusicSongName", mMusicSongName.getVal());
            packet.putString("MusicAlbumName", mMusicAlbumName.getVal());
            packet.putBoolean("VoiceInPhone", mVoiceInPhone.getVal());
            packet.putBoolean("MicMute", mMicMute.getVal());
            packet.putString("Version", mVersion.getVal());
            packet.putInt("A2DPConnectStatus", mA2DPConnectStatus.getVal());
            packet.putBoolean("OpenOrClose", mOpenOrClose.getVal());
            packet.putParcelableArray("PhonebookList", mPhonebookList.getVal());
            packet.putInt("PhonebookDownloadStatus", mPhonebookDownloadStatus.getVal());
            packet.putInt("PhoneDownloadStatus", mPhoneDownloadStatus.getVal());
            packet.putInt("SIMDownloadStatus", mSIMDownloadStatus.getVal());
            packet.putParcelableArray("TalkList", mTalkList.getVal());
            packet.putInt("DialTalkListDownloadStatus", mDialTalkListDownloadStatus.getVal());
            packet.putInt("IncommingTalkListDownloadStatus", mIncommingTalkListDownloadStatus.getVal());
            packet.putInt("MissedTalkListDownloadStatus", mMissedTalkListDownloadStatus.getVal());
            packet.putInt("TalkListDownloadStatus", mTalkListDownloadStatus.getVal());
            packet.putParcelableArray("SearchList", mSearchList.getVal());
            packet.putParcelableArray("PairList", mPairList.getVal());
            packet.putInt("SearchStatus", mSearchStatus.getVal());
            return packet;
        }

        /**
         * 当前模块连接状态
         */
        private MInteger mModuleStatus = new MInteger(this, "ModuleStatusListener", BluetoothDefine.ModuleStatus.MODULE_STATUS_NONE);

        public MInteger getModuleStatus() {
            return mModuleStatus;
        }

        /**
         * 当前HFP状态.
         */
        private MInteger mHFPStatus = new MInteger(this, "HFPStatusListener", BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT);

        public MInteger getHFPStatus() {
            return mHFPStatus;
        }

        /**
         * 本地名字
         */
        private MString mLocalName = new MString(this, "LocalNameListener", "wwc2-BT");

        public MString getLocalName() {
            return mLocalName;
        }

        /**
         * 本地MAC地址
         */
        private MString mLocalMacAddr = new MString(this, "LocalMacAddrListener", null);

        public MString getLocalMacAddr() {
            return mLocalMacAddr;
        }

        /**
         * PIN码
         */
        private MString mPinCode = new MString(this, "PinCodeListener", "0000");

        public MString getPinCode() {
            return mPinCode;
        }

        /**
         * 自动连接
         */
        private MBoolean mAutoConnect = new MBoolean(this, "AutoConnectListener", false);

        public MBoolean getAutoConnect() {
            return mAutoConnect;
        }

        /**
         * 自动应答
         */
        private MBoolean mAutoAnswer = new MBoolean(this, "AutoAnswerListener", false);

        public MBoolean getAutoAnswer() {
            return mAutoAnswer;
        }

        /**
         * 蓝牙配对名字
         */
        private MString mPairingName = new MString(this, "PairingNameListener", null);

        public MString getPairingName() {
            return mPairingName;
        }

        /**
         * 蓝牙配对MAC地址
         */
        private MString mPairingMacAddr = new MString(this, "PairingMacAddrListener", null);

        public MString getPairingMacAddr() {
            return mPairingMacAddr;
        }

        /**
         * 通话时间
         */
        private MLong mCallTime = new MLong(this, "CallTimeListener", 0L);

        public MLong getCallTime() {
            return mCallTime;
        }

        /**
         * 通话号码
         */
        private MString mCallNumber = new MString(this, "CallNumberListener", null);

        public MString getCallNumber() {
            return mCallNumber;
        }

        /**
         * 蓝牙音乐状态
         */
        private MInteger mMusicStatus = new MInteger(this, "MusicStatusListener", BluetoothDefine.MusicStatus.MUSIC_STATUS_STOP);

        public MInteger getMusicStatus() {
            return mMusicStatus;
        }

        /**
         * 蓝牙音乐艺术家名称
         */
        private MString mMusicArtistName = new MString(this, "MusicArtistNameListener", null);

        public MString getMusicArtistName() {
            return mMusicArtistName;
        }

        /**
         * 蓝牙音乐歌曲名称
         */
        private MString mMusicSongName = new MString(this, "MusicSongNameListener", null);

        public MString getMusicSongName() {
            return mMusicSongName;
        }

        /**
         * 蓝牙音乐专辑名称
         */
        private MString mMusicAlbumName = new MString(this, "MusicAlbumNameListener", null);

        public MString getMusicAlbumName() {
            return mMusicAlbumName;
        }

        /**
         * 蓝牙音乐playtime
         */
        private MInteger mMusicPlaytime = new MInteger(this, "MusicPlaytimeListener", null);

        public MInteger getmMusicPlaytime() {
            return mMusicPlaytime;
        }

        /**
         * 蓝牙音乐index
         */
        private MInteger mMusicIndex = new MInteger(this, "MusicIndexListener", null);

        public MInteger getMusicIndex() {
            return mMusicIndex;
        }

        /**
         * 蓝牙音乐total
         */
        private MInteger mMusicTotal = new MInteger(this, "MusicTotalListener", null);

        public MInteger getMusicTotal() {
            return mMusicTotal;
        }

        /**
         * 蓝牙通话声音是否在手机
         */
        private MBoolean mVoiceInPhone = new MBoolean(this, "VoiceInPhoneListener", false);

        public MBoolean getVoiceInPhone() {
            return mVoiceInPhone;
        }

        /**
         * 蓝牙MIC是否静音
         */
        private MBoolean mMicMute = new MBoolean(this, "MicMuteListener", false);

        public MBoolean getMicMute() {
            return mMicMute;
        }

        /**
         * 蓝牙版本
         */
        private MString mVersion = new MString(this, "VersionListener", null);

        public MString getVersion() {
            return mVersion;
        }

        /**
         * A2DP连接状态
         */
        private MInteger mA2DPConnectStatus = new MInteger(this, "A2DPConnectStatusListener", BluetoothDefine.A2DPConnectStatus.DISCONNECT);

        public MInteger getA2DPConnectStatus() {
            return mA2DPConnectStatus;
        }


        /**
         * 蓝牙模块开关状态
         */
        private MBoolean mOpenOrClose = new MBoolean(this, "OpenOrCloseListener", true);

        public MBoolean getOpenOrClose() {
            return mOpenOrClose;
        }

        /**
         * 蓝牙电话本列表
         */
        private MIntegerSSBooleanArray mPhonebookList = new MIntegerSSBooleanArray(this, "PhonebookListListener", null);

        public MIntegerSSBooleanArray getPhonebookList() {
            return mPhonebookList;
        }

        /**
         * 蓝牙电话本下载状态
         */
        private MInteger mPhonebookDownloadStatus = new MInteger(this, "PhonebookDownloadStatusListener", BluetoothDefine.DownloadStatus.NONE);

        public MInteger getPhonebookDownloadStatus() {
            return mPhonebookDownloadStatus;
        }

        /**
         * 蓝牙手机电话本下载状态
         */
        private MInteger mPhoneDownloadStatus = new MInteger(this, "PhoneDownloadStatusListener", BluetoothDefine.DownloadStatus.NONE);

        public MInteger getPhoneDownloadStatus() {
            return mPhoneDownloadStatus;
        }

        /**
         * 蓝牙SIM电话本下载状态
         */
        private MInteger mSIMDownloadStatus = new MInteger(this, "SIMDownloadStatusListener", BluetoothDefine.DownloadStatus.NONE);

        public MInteger getSIMDownloadStatus() {
            return mSIMDownloadStatus;
        }

        /**
         * 蓝牙通话列表
         */
        private MIntegerSSSArray mTalkList = new MIntegerSSSArray(this, "TalkListListener", null);

        public MIntegerSSSArray getTalkList() {
            return mTalkList;
        }

        /**
         * 蓝牙已拨通话列表下载状态
         */
        private MInteger mDialTalkListDownloadStatus = new MInteger(this, "DialTalkListDownloadStatusListener", BluetoothDefine.DownloadStatus.NONE);

        public MInteger getDialTalkListDownloadStatus() {
            return mDialTalkListDownloadStatus;
        }

        /**
         * 蓝牙已接通话列表下载状态
         */
        private MInteger mIncommingTalkListDownloadStatus = new MInteger(this, "IncommingTalkListDownloadStatusListener", BluetoothDefine.DownloadStatus.NONE);

        public MInteger getIncommingTalkListDownloadStatus() {
            return mIncommingTalkListDownloadStatus;
        }

        /**
         * 蓝牙未接通话列表下载状态
         */
        private MInteger mMissedTalkListDownloadStatus = new MInteger(this, "MissedTalkListDownloadStatusListener", BluetoothDefine.DownloadStatus.NONE);

        public MInteger getMissedTalkListDownloadStatus() {
            return mMissedTalkListDownloadStatus;
        }

        /**
         * 蓝牙通话列表下载状态
         */
        private MInteger mTalkListDownloadStatus = new MInteger(this, "TalkListDownloadStatusListener", BluetoothDefine.DownloadStatus.NONE);

        public MInteger getTalkListDownloadStatus() {
            return mTalkListDownloadStatus;
        }

        /**
         * 蓝牙搜索列表
         */
        private MIntegerSSArray mSearchList = new MIntegerSSArray(this, "SearchListListener", null);

        public MIntegerSSArray getSearchList() {
            return mSearchList;
        }
        /**
         * 蓝牙配对列表
         */
        private MIntegerSSArray mPairList = new MIntegerSSArray(this, "PairListListener", null);

        public MIntegerSSArray getPairList() {
            return mPairList;
        }

        /**
         * 蓝牙搜索状态
         */
        private MInteger mSearchStatus = new MInteger(this, "SearchListStatusListener", BluetoothDefine.SearchStatus.SEARCH_STATUS_NONE);

        public MInteger getSearchStatus() {
            return mSearchStatus;
        }

        /**
         * 蓝牙通话声音通道
         */
        private MInteger mAudioSoft = new MInteger(this, "AudioSoftListener", BluetoothDefine.AudioSoft.DEFAULT);

        public MInteger getAudioSoft() {
            return mAudioSoft;
        }

        /**
         * 蓝牙已下载电话本数目
         */
        private MInteger downloadedContacts = new MInteger(this, "ContactsNumberListener", 0);
        public MInteger getDownloadedContacts(){
            return downloadedContacts;
        }

    }

    /**
     * talking timer id
     */
    private int mTalkingTimer = 0;

    /**
     * the listener
     */
    private BluetoothListener mListener = new BluetoothListener() {

        @Override
        public void ModuleStatusListener(Integer oldVal, Integer newVal) {
            if (BluetoothDefine.ModuleStatus.MODULE_STATUS_CONNECT == newVal) {
                LogUtils.d(TAG, "bluetooth connect, mResumeConnect = " + mResumeConnect);
//                if (mResumeConnect) {
//                    mResumeConnect = false;
//                    connectLast();
//                }
            }
        }

        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            // 计算通话时间
            LogUtils.d(TAG, "HFPStatusListener: oldVal = " + BluetoothDefine.HFPStatus.toString(oldVal) + " newVal = " + BluetoothDefine.HFPStatus.toString(newVal));
            if (BluetoothDefine.HFPStatus.isTalking(newVal) && !BluetoothDefine.HFPStatus.isTalking(oldVal)) {
                // start call timer.
                if (mTalkingTimer == 0) {
                    mTalkingTimer = TimerUtils.setTimer(getMainContext(), 1000, 1000, new Timerable.TimerListener() {
                         Long time = 1L;
                        @Override
                        public void onTimer(int timerId) {
                            Model().getCallTime().setVal(time++);
                        }
                    });
                }

            } else /*if (0 != mTalkingTimer) */ {
                TimerUtils.killTimer(mTalkingTimer);
                mTalkingTimer = 0;

//                Model().getCallTime().setVal(0L);
                if (BluetoothDefine.HFPStatus.isHangup(newVal)) {
                    savePhoneRecord();
                }
            }

            // 根据连接状态进行数据保存
            if (!BluetoothDefine.HFPStatus.isDisConnect(oldVal) && BluetoothDefine.HFPStatus.isDisConnect(newVal)) {

                if (BluetoothDefine.SearchStatus.searching(Model().getSearchStatus().getVal())) {
                    Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_FAILED);
                }
                if (BluetoothDefine.DownloadStatus.START == Model().getPhonebookDownloadStatus().getVal()
                       || BluetoothDefine.DownloadStatus.DOWNLOADING == Model().getPhonebookDownloadStatus().getVal()) {
                    Model().getPhonebookDownloadStatus().setVal(BluetoothDefine.DownloadStatus.FAILED);
                    Model().getPhoneDownloadStatus().setVal(BluetoothDefine.DownloadStatus.FAILED);
                    clearPhoneBook();
                }
            }

            //蓝牙连接后恢复蓝牙音乐播放
            if (mResumeMusicPlay && !BluetoothDefine.HFPStatus.isDisConnect(newVal)) {
                if (SourceManager.getCurBackSource() != Define.Source.SOURCE_BLUETOOTH) {
                    return;
                }
                //延时播放
                mDelayPlayMusic.add(5000, null, new BaseCallback() {//播放蓝牙音乐，TA切到收音，通话，切回蓝牙，挂断不会自动播放，必须延时5s。2020-04-08
                    @Override
                    public void onCallback(int nId, Packet packet) {
                        // TODO Auto-generated method stub
                        musicUnmute();
                        musicPlay();
                        LogUtils.d(TAG, "musicPlay() is called");
                    }
                });
                mDelayPlayMusic.start();

                mResumeMusicPlay = false;
            }
        }

        @Override
        public void PairingMacAddrListener(String oldVal, String newVal) {
            // 配对设备发生变化，则清空保存数据
            if (null != oldVal && null != newVal && !newVal.equals(oldVal)) {
                Model().getPhonebookList().setVal(null);
                Model().getTalkList().setVal(null);
                Model().getCallNumber().setVal(null);
                Model().getMusicSongName().setVal(null);
                Model().getMusicArtistName().setVal(null);
                if (null != mContentMemory) {
                    mContentMemory.clear();
                }
            }
        }

        @Override
        public void PinCodeListener(String oldVal, String newVal) {
            if (null != mMemory) {
                mMemory.save();
            }
        }

        @Override
        public void LocalNameListener(String oldVal, String newVal) {
            if (null != mMemory) {
                mMemory.save();
            }
        }

        @Override
        public void OpenOrCloseListener(Boolean oldVal, Boolean newVal) {
            if (!newVal) {
                if (BluetoothDefine.SearchStatus.searching(Model().getSearchStatus().getVal())) {
                    stopDiscovery();
                }
            }

            if (null != mMemory) {
                mMemory.save();
            }
        }
    };

    /**
     * the audio control.
     */
    private AudioDriverable.AudioControlable mAudioControl = new AudioDriverable.AudioControlable() {
        @Override
        public void pause() {
            musicMute();
//            musicPause();
        }

        @Override
        public void play() {
//            musicPlay();
            musicUnmute();
        }

        @Override
        public boolean isPlay() {
            boolean ret = false;

            final int status = Model().getMusicStatus().getVal();
            ret = BluetoothDefine.MusicStatus.isPlay(status);
            return ret;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        // 在蓝牙后台，允许播放蓝牙音乐，把静音关闭
        musicUnmute();

        // 进入蓝牙模式，恢复蓝牙音乐播放
       final int hfpStatus = Model().getHFPStatus().getVal();

        delayMusicTimerID = TimerUtils.setTimer(getMainContext(), 100, new Timerable.TimerListener() {
            @Override
            public void onTimer(int i) {
                delayMusicTimerID = -1;
                if (mResumeMusicPlay && !BluetoothDefine.HFPStatus.isDisConnect(hfpStatus)) {
                    if (!BluetoothDefine.HFPStatus.isCalling(hfpStatus)) {
                        mResumeMusicPlay = false;
                        LogUtils.d(TAG, "resume Music play");
                        if (!BluetoothDefine.MusicStatus.isPlay(Model().getMusicStatus().getVal())) {
                            musicPlay();
                        }
                    }
                }
            }
        });

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
    public void onStop() {
        super.onStop();
        // 不在蓝牙后台，则不允许播放蓝牙音乐，把静音打开
        musicMute();
        // 退出蓝牙模式，要暂停蓝牙播放
        if (BluetoothDefine.MusicStatus.isPlay(Model().getMusicStatus().getVal())) {
            LogUtils.d(TAG, "mResumeMusicPlay: true");
            musicPause();
            mResumeMusicPlay = true;
        }

        // 释放音频
        AudioDriver.Driver().abandon();
    }

    private BaseListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {

            //深度睡眠醒来需要复位蓝牙（会自动打开蓝牙）
            if (AccoffStep.isDeepSleep(oldVal) && !AccoffStep.isAccoff(newVal)) {
                ResetBluetooth();
            }
        }
    };

    /**
     * power off source listener.
     */
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {

            boolean oldPoweroff = false;
            BaseLogic oldLogic = ModuleManager.getLogicBySource(oldVal);
            if (null != oldLogic) {
                oldPoweroff = oldLogic.isPoweroffSource();
            }

            boolean newPoweroff = false;
            BaseLogic newLogic = ModuleManager.getLogicBySource(newVal);
            if (null != newLogic) {
                newPoweroff = newLogic.isPoweroffSource();
            }

            if (oldPoweroff && !newPoweroff && !(newLogic instanceof CameraLogic)) {
                // 开机
                final int status = Model().getModuleStatus().getVal();
                LogUtils.d(TAG, "power on, resume connect? module status = " + BluetoothDefine.ModuleStatus.toString(status) + ", mResumeConnect = " + mResumeConnect);
                if (mResumeConnect) {
                        mResumeConnect = false;
                        mPoweronConnectTimerQueue.add(1600, null, new BaseCallback() {
                            @Override
                            public void onCallback(int nId, Packet packet) {
                                if (!isClosedByUser) {
                                    LogUtils.d(TAG, "open bt when poweron");
                                    OpenBt();
                                }
                            }
                        });
                        mPoweronConnectTimerQueue.start();
                }
            } else if (!oldPoweroff && newPoweroff) {
                // 关机  保存蓝牙状态  蓝牙为开启则关闭蓝牙,恢复状态置为true
                if (Model().getOpenOrClose().getVal()) {
                    mResumeConnect = true;
                    LogUtils.d(TAG, "power off, isBluetoothOn = " + Model().getOpenOrClose().getVal() + ", mResumeConnect = " + mResumeConnect);
                    if (BluetoothDefine.MusicStatus.isPlay(Model().getMusicStatus().getVal())) {
                        mResumeMusicPlay = true;
                        LogUtils.d(TAG, "mResumeMusicPlay:" + mResumeMusicPlay);
                        mPoweronConnectTimerQueue.stop();
                    }

                    CloseBt(BluetoothDefine.BluetoothClosedBy.NORMAL);
                }
            }
        }
    };

    @Override
    public BaseModel newModel() {
        return new BluetoothModel();
    }

    /**
     * get the model object.
     */
    protected BluetoothModel Model() {
        BluetoothModel ret = null;
        BaseModel model = getModel();
        if (model instanceof BluetoothModel) {
            ret = (BluetoothModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        getModel().bindListener(mListener);
        SourceManager.getModel().bindListener(mSourceListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);

        // 发送广播
        Context context = getMainContext();
        if (null != context) {
            Intent intent = new Intent("com.wwc2.bluetooth.openorclose");
            intent.putExtra("OpenOrClose", Model().getOpenOrClose().getVal());
            context.sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        SourceManager.getModel().unbindListener(mSourceListener);
        getModel().unbindListener(mListener);
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "BluetoothConfig.ini";
    }

    @Override
    public String contentFilePath() {
        return "BluetoothData.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {

            ret = true;

            final String localName = (String) mMemory.get("BLUETOOTH", "LocalName");
            if (localName != null)
                Model().getLocalName().setVal(localName);
            final String localPin = (String) mMemory.get("BLUETOOTH", "LocalPin");
            if (localPin != null)
                Model().getPinCode().setVal(localPin);

            try {
                Object object = mMemory.get("BLUETOOTH", "AudioSoft");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getAudioSoft().setVal(Integer.parseInt(string));
                    }
                }

                object = mMemory.get("BLUETOOTH", "BTSwitch");
                if (null != object) {
                    String string = (String) object;
                    LogUtils.e("readData----" + Model().getOpenOrClose().getVal() + ", string=" + string);
                    if (!TextUtils.isEmpty(string)) {
                        Model().getOpenOrClose().setVal(Boolean.parseBoolean(string));
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public boolean contentReadData() {
        boolean ret = false;
        if (null != mContentMemory) {

            ret = true;

            final String macAddr = (String) mContentMemory.get("MAC", "DATA");
            if (!TextUtils.isEmpty(macAddr)) {
                Model().getPairingMacAddr().setVal(macAddr);

                Object object = mContentMemory.get("Phonebook", "length");
                if (null != object) {
                    int length = Integer.parseInt((String) object);
                    if (length > 0) {
                        IntegerSSBoolean[] phonebook = new IntegerSSBoolean[length];
                        for (int i = 0; i < length; i++) {
                            final String string = (String) mContentMemory.get("Phonebook", ("item" + i));
                            if (!TextUtils.isEmpty(string)) {
                                String[] array = string.split(",");
                                if (null != array) {
                                    if (array.length > 3) {
                                        final int type = Integer.valueOf(array[0]);
                                        final String name = array[1];
                                        final String number = array[2];
                                        boolean favorite = Boolean.parseBoolean(array[3]);
                                        if (!TextUtils.isEmpty(number)) {
                                            phonebook[i] = new IntegerSSBoolean(type, name, number, favorite);
                                        }
                                    }
                                }
                            }
                        }
                        Model().getPhonebookList().setVal(phonebook);
                    }
                }

                object = mContentMemory.get("Talklist", "length");
                if (null != object) {
                    int length = Integer.parseInt((String) object);
                    if (length > 0) {
                        IntegerSSS[] talklist = new IntegerSSS[length];
                        for (int i = 0; i < length; i++) {
                            final String string = (String) mContentMemory.get("Talklist", ("item" + i));
                            if (!TextUtils.isEmpty(string)) {
                                String[] array = string.split(",");
                                if (null != array) {
                                    if (array.length > 3) {
                                        final int type = Integer.valueOf(array[0]);
                                        final String name = array[1];
                                        final String number = array[2];
                                        final String time = array[3];
                                        if (!TextUtils.isEmpty(number)) {
                                            talklist[i] = new IntegerSSS(type, name, number, time);
                                        }
                                    }
                                }
                            }
                        }
                        Model().getTalkList().setVal(talklist);
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            ret = true;
            final String locatName = Model().getLocalName().getVal();
            if (locatName != null) {
                mMemory.set("BLUETOOTH", "LocalName", locatName);
            }
            final String localPin = Model().getPinCode().getVal();
            if (locatName != null) {
                mMemory.set("BLUETOOTH", "LocalPin", localPin);
            }
            mMemory.set("BLUETOOTH", "AudioSoft", Model().getAudioSoft().getVal()+"");

            LogUtils.e("writeData----" + Model().getOpenOrClose().getVal());
            mMemory.set("BLUETOOTH", "BTSwitch", Model().getOpenOrClose().getVal() + "");
        }
        return ret;
    }

    @Override
    public boolean contentWriteData() {
        boolean ret = false;
        if (null != mContentMemory) {
            ret = true;
            final String macAddr = Model().getPairingMacAddr().getVal();
            if (!TextUtils.isEmpty(macAddr)) {
                mContentMemory.set("MAC", "DATA", macAddr);
                IntegerSSBoolean[] phonebook = Model().getPhonebookList().getVal();
                if (null != phonebook) {
                    final int length = phonebook.length;
                    mContentMemory.set("Phonebook", "length", length);
                    for (int i = 0; i < length; i++) {
                        IntegerSSBoolean temp = phonebook[i];
                        if (null != temp) {
                            final String string = temp.getInteger() + "," + temp.getString1() + "," + temp.getString2() + "," + temp.getBoolean();
                            mContentMemory.set("Phonebook", ("item" + i), string);
                        }
                    }
                }
                IntegerSSS[] talklist = Model().getTalkList().getVal();
                if (null != talklist) {
                    final int length = talklist.length;
                    mContentMemory.set("Talklist", "length", length);
                    for (int i = 0; i < talklist.length; i++) {
                        IntegerSSS temp = talklist[i];
                        if (null != temp) {
                            final String string = temp.getInteger() + "," + temp.getString1() + "," + temp.getString2() + "," + temp.getString3();
                            mContentMemory.set("Talklist", ("item" + i), string);
                        }
                    }
                }
            }
        }
        return ret;
    }

    protected void savePhoneRecord() {
        if (BluetoothDefine.CallType.CALL_TYPE_UNKNOWN != mLatestCallType) {
            final String number = Model().getCallNumber().getVal();
            String name = null;
            IntegerSSBoolean book = Model().getPhonebookList().getValByString2(number);
            if (null != book) {
                name = book.getString1();
            }
            // yyyy-MM-dd HH:mm:ss
            String time = TimeFormat.getStringDate();
            IntegerSSS talk = new IntegerSSS(mLatestCallType, name, number, time);
            Model().getTalkList().addVal(-1, talk);
        }
    }
}
