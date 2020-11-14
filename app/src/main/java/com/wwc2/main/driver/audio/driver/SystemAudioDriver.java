package com.wwc2.main.driver.audio.driver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.StringII;
import com.wwc2.corelib.model.custom.StringIIBoolean;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.bluetooth.EcarHelper;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioListener;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.MediaListener;
import com.wwc2.media_interface.MediaDefine;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;

import java.util.List;

/**
 * the system audio driver.
 * 音频主要逻辑处理
 * @author wwc2
 * @date 2017/1/14
 */
public abstract class SystemAudioDriver extends BaseAudioDriver {

    /**TAG*/
    protected static final String TAG = "SystemAudioDriver";

    /**
     * audio focus package name.
     */
    private static final String AUDIO_FOCUS_PACKAGENAME = "com.wwc2.framework.action.audiofocus";

    /**凯立德包名*/
    public static final String KLD_NAVI_PACKAGE = "cld.navi.c2739.mainframe";//cld.navi.kaimap.h4139.mainframe
    /**凯立德消息*/
    public static final String KLD_NAVI_MSG = "android.NaviOne.voiceprotocol";
    /***/
    private static final String TPMS_PACKAGE = "com.tpms3";

    /**
     * navigation logic object.
     */
    private BaseLogic mNaviLogic    = null;

    /**voice logic object.*/
    private BaseLogic mVoiceLogic   = null;

    private Context mContext        = null;

    protected AudioManager mAudioManager = null;

    /**
     * the audio package.
     */
    private String mThirdAudioPkgName = null;

    private TimerQueue mDelayStopGps    = new TimerQueue();
    private TimerQueue mDelayResumePlay = new TimerQueue();
    private TimerQueue mDelayTpms       = new TimerQueue();
    private TimerQueue mDelayArmAudio   = new TimerQueue();

    int mTimerId = 0;
    boolean mMusicactive = false;
    long mFirstBootTime = -1;

    /**
     * 应用包名出声的广播
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (AUDIO_FOCUS_PACKAGENAME.equals(action)) {
                //如果导航正在报点时被异常退出，重新设置报点状态。解决语音“关闭地图”出现音乐声音小的问题。2017-11-29
                if (mNaviSoundActive) {
                    if (!ApkUtils.isProcessRunning(mContext, mStrGpsProcessName)) {
                        LogUtils.d(TAG, "AUDIO_FOCUS_PACKAGENAME-----Naiv is killed");
                        mNaviSoundActive = false;
                        setAudioActive(mStrGpsProcessName, AudioDefine.AudioStream.STREAM_ALARM, AudioDefine.AudioFocus.AUDIOFOCUS_LOSS, false);
                    }
                }
                // 应用包名出声
                final int id = intent.getIntExtra("CODE", -1);
                if (401 == id) {
                    Bundle bundle = intent.getExtras();
                    if (null != bundle) {
                        final int INVALID = 0xffffffff;
                        final String packetName = bundle.getString("PKGNAME");
                        final int streamType = bundle.getInt("streamType", INVALID);
                        final int durationHint = bundle.getInt("durationHint", INVALID);
                        final int data = bundle.getInt("DATA", -1);
                        if (-1 != data) {
                            if (!TextUtils.isEmpty(packetName)) {
                                final String pkgName = packetName;
                                int stream = AudioDefine.getAudioStream(streamType);
                                int focus = AudioDefine.getAudioFocus(durationHint);
                                final boolean active = (1 == data);
                                LogUtils.d(TAG, "audio out, pkgName = " + pkgName +
                                        ", stream = " + AudioDefine.AudioStream.toString(stream) + "[" + stream + "]" +
                                        ", focus = " + AudioDefine.AudioFocus.toString(focus) + "[" + focus + "]" +
                                        ", active = " + active);
                                // 设置音频
                                if (active) {
                                    // 声音开始
                                    setAudioActive(pkgName, stream, focus, true);
                                } else if (0 == data) {
                                    // 声音结束
                                    List<StringIIBoolean> list = getAudiosByPkgName(pkgName);
                                    if (null != list) {
                                        if (list.size() > 0) {
                                            StringIIBoolean audio = list.get(0);
                                            if (null != audio) {
                                                stream = audio.getInteger1();
                                                focus = -1 * audio.getInteger2();
                                                setAudioActive(pkgName, stream, focus, false);
                                            }
                                        }
                                    }
                                }
                            } else {
                                LogUtils.w(TAG, "audio out, can't find package name, data = " + data);
                            }
                        } else {
                            LogUtils.w(TAG, "audio out, can't find data.");
                        }
                    }
                }
            } else if (KLD_NAVI_MSG.equals(action)) {
                // 凯立德出声
                String navi = intent.getStringExtra("VOICEPROTOCOL");
                String pkgName = KLD_NAVI_PACKAGE;
                int stream = AudioDefine.AudioStream.STREAM_MUSIC;
                final boolean active = "play".equals(navi);
                int focus = active ? AudioDefine.AudioFocus.AUDIOFOCUS_GAIN : AudioDefine.AudioFocus.AUDIOFOCUS_LOSS;
                ComponentName component = intent.getComponent();
                if (component != null) {
                    LogUtils.d(TAG, "onReceive---pkgName="+component.getPackageName()+", clsName="+component.getClassName());
                }
                if (mStrGpsProcessName.contains("cld.navi")) {//暂时无法获取到apk的包名，先用固定字符串代替。2017-08-05
                    pkgName = mStrGpsProcessName;
                    stream = AudioDefine.AudioStream.STREAM_ALARM;

                    //修改凯立德货车版会退出的问题。2019-11-22
                    LogUtils.d(TAG, "audio out, pkgName = " + pkgName +
                            ", stream = " + AudioDefine.AudioStream.toString(stream) + "[" + stream + "]" +
                            ", focus = " + AudioDefine.AudioFocus.toString(focus) + "[" + focus + "]" +
                            ", active = " + active);
                    setAudioActive(pkgName, stream, focus, active);
                }

//                LogUtils.d(TAG, "audio out, pkgName = " + pkgName +
//                        ", stream = " + AudioDefine.AudioStream.toString(stream) + "[" + stream + "]" +
//                        ", focus = " + AudioDefine.AudioFocus.toString(focus) + "[" + focus + "]" +
//                        ", active = " + active);
//                setAudioActive(pkgName, stream, focus, active);
            } else if (action.equals(EcarHelper.ACTION_CALL_IDLE)) {
                LogUtils.d(TAG, "ecar call idle");
                mECarActive = false;
                EcarHelper.setEcarState(mECarActive);
                handleMusicStream(false);
            } else if (action.equals(EcarHelper.ACTION_CALL_INCOMING)) {
                LogUtils.d(TAG, "ecar call incoming");
            } else if (action.equals(EcarHelper.ACTION_CALL_OFFHOOK)) {
                LogUtils.d(TAG, "ecar call offhook");
                mECarActive = true;
                EcarHelper.setEcarState(mECarActive);
                handleMusicStream(true);
            }
        }
    };

    /**
     * audio listener.
     */
    private AudioListener mAudioListener = new AudioListener() {
        @Override
        public void AudioStartListener(StringII val) {
            if (null != val) {
                LogUtils.d(TAG, "AudioStartListener start curPkgName=" + val.getString() +
                        ", stream=" + AudioDefine.AudioStream.toString(val.getInteger1()) + "[" + val.getInteger1() + "]" +
                        ", focus=" + AudioDefine.AudioFocus.toString(val.getInteger2()) + "[" + val.getInteger2() + "]" +
                        ", active=" + true + ", mThirdAudioPkgName=" + mThirdAudioPkgName);

                final String curPkgName = val.getString();
                if (!TextUtils.isEmpty(curPkgName)) {
                    boolean handle = true;
                    // 判断指定忽略处理的音频
                    if (handle) {
                        String[] ignore = Model().getIgnoreAudioPackages().getVal();
                        if (null != ignore) {
                            for (int i = 0; i < ignore.length; i++) {
                                final String temp = ignore[i];
                                if (curPkgName.equals(temp)) {
                                    handle = false;
                                    if (mThirdAppSoundActive) {
                                        // 处理除了Main和导航之外的所有音频流
                                        List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                        if (null != listeners) {
                                            for (int j = 0; j < listeners.size(); j++) {
                                                BaseListener tempListener = listeners.get(j);
                                                if (null != tempListener) {
                                                    if (tempListener instanceof AudioListener) {
                                                        AudioListener listener = (AudioListener) tempListener;
                                                        // arm start.
                                                        listener.ArmAudioActivieListener(true, false);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (!isBlueToothPause) {//通话结束后，还原之前第三方播放状态。
                                        mThirdAppSoundActive = false;
                                    }
                                    LogUtils.d(TAG, "AudioStartListener, specify ignore success!");
                                    break;
                                }
                            }
                        }
                    }
                    // 判断指定为导航的包名
                    if (handle) {
//                        if (null != mNaviLogic) {
//                            Packet naviPacket = mNaviLogic.getInfo();
//                            if (null != naviPacket) {
//                                String current = naviPacket.getString(com.wwc2.main.navi.NaviDefine.SELECTION);
//                                if (pkgName.equals(current)) {
//                                    handle = false;
//                                }
//                                if (handle) {
//                                    String[] real = naviPacket.getStringArray(com.wwc2.main.navi.NaviDefine.REAL);
//                                    if (null != real) {
//                                        for (int i = 0; i < real.length; i++) {
//                                            final String temp = real[i];
//                                            if (pkgName.equals(temp)) {
//                                                handle = false;
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
                        if (curPkgName.equals(mStrGpsProcessName) || curPkgName.contains(mStrGpsProcessName)) {
                            // navi start.
                            mDelayStopGps.stop();
                            handleGpsSound(true);
                            handle = false;
                            if (!mVoiceAssistantActive && !mECarActive) {
                                List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                if (null != listeners) {
                                    for (int i = 0; i < listeners.size(); i++) {
                                        BaseListener temp = listeners.get(i);
                                        if (null != temp) {
                                            if (temp instanceof AudioListener) {
                                                AudioListener listener = (AudioListener) temp;
                                                listener.NaviAudioActiveListener(false, true);
                                            }
                                        }
                                    }
                                }
                            }
                            LogUtils.d(TAG, "AudioStartListener, specify navigation success!");
                        }
                    }

                    if (handle) {
                        // 判断指定为行车记录仪的包名
                        if (curPkgName.equals("com.ankai.cardvr") ||
                                curPkgName.equals("com.sicadroid.ucam_car") ||
                                curPkgName.equals("com.fvsm.camera") ||
                                /*curPkgName.equals("com.wwc2.dvr") ||*/ //解决在DVR播放没有声音的问题。
                                curPkgName.equals("com.baony.avm360")) {//灵动飞扬360
                            handle = false;
                            LogUtils.d(TAG, "AudioStartListener, specify dvr success!");
                        }
                        if (handle) {
                            String dvrName = CommonDriver.Driver().getDvrApkName();
                            if (!TextUtils.isEmpty(dvrName)) {
                                if (curPkgName.equals(dvrName)) {
                                    handle = false;
                                }
                            }
                        }
                        // 判断指定为胎压的包名
                        if (handle) {
                            if (curPkgName.equals(TPMS_PACKAGE)) {
                                mTpmsWarn = true;
                                handle = false;
                                LogUtils.d(TAG, "AudioStartListener, specify tpms success!");
                            } else {
                                String tpmsName = CommonDriver.Driver().getTpmsApkName();
                                if (!TextUtils.isEmpty(tpmsName)) {
                                    if (curPkgName.equals(tpmsName)) {
                                        mTpmsWarn = true;
                                        handle = false;
                                    }
                                }
                            }
                        }
                        // 判断指定为语音的包名
                        if (handle) {
                            if (isVoiceAssistantActive(curPkgName)) {
                                handle = false;
                                mVoiceAssistantActive = true;

                                if (mNaviSoundActive) {
                                    handleGpsSound(false);
                                    List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                    if (null != listeners) {
                                        final int size = listeners.size();
                                        for (int i = 0; i < size; i++) {
                                            BaseListener temp = listeners.get(i);
                                            if (null != temp) {
                                                if (temp instanceof AudioListener) {
                                                    AudioListener listener = (AudioListener) temp;
                                                    listener.NaviAudioActiveListener(true, false);
                                                }
                                            }
                                        }
                                    }
                                }

                                handleMusicStream(true);
                                LogUtils.d(TAG, "AudioStartListener, specify voice assistant success!");
                            }
                        }
                        if (handle) {
                            if ((curPkgName.equals(EcarHelper.E_CAR_VOIP) || curPkgName.equals(EcarHelper.E_CAR_CALL)) /*&&
                                    val.getInteger1() == AudioDefine.getSystemAudioStream(AudioDefine.AudioStream.STREAM_VOICE_CALL)*/) {
                                handle = false;
                                mECarActive = true;
                                EcarHelper.setEcarState(true);
                                handleMusicStream(true);
                            }
                        }
                        // 第三方应用
                        if (handle) {
                            mThirdAppSoundActive = true;
                            LogUtils.d(TAG, "AudioStartListener, specify third app success! curPkgName=" + curPkgName);
                            // 获取声音开始的logic对象
                            BaseLogic logic = ModuleManager.getLogicByPacketName(curPkgName);
                            if (null != logic) {
                                final int source = logic.source();
                                if (Define.Source.SOURCE_THIRDPARTY == source) {
                                    // 切换为无声模式
                                    SourceManager.onOpenBackgroundSource(Define.Source.SOURCE_SILENT);
                                    // 杀掉之前的进程
                                    if (!curPkgName.equals(mThirdAudioPkgName)) {
                                        if (ApkUtils.isAPKExist(mContext, mThirdAudioPkgName) &&
                                                !(isVoiceAssistantActive(mThirdAudioPkgName) ||
                                                        mThirdAudioPkgName.equals("com.zjinnova.zlink") ||
                                                        mThirdAudioPkgName.equals("com.wwc2.dvr"))) {
                                            LogUtils.d(TAG, "AudioStartListener kill process = " + mThirdAudioPkgName);
                                            Packet packet = new Packet();
                                            packet.putString("pkgName", mThirdAudioPkgName);
                                            BaseLogic l = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                                            if (null != l) {
                                                l.Notify(SystemPermissionInterface.MAIN_TO_APK.KILL_PROCCESS, packet);
                                            }
                                        }
                                        mThirdAudioPkgName = curPkgName;
                                    }
                                } else {
                                    LogUtils.d(TAG, "AudioStartListener ignore handle source = " + Define.Source.toString(source) +
                                            "logic = " + logic.toString());
                                }
                            } else {
                                LogUtils.w(TAG, "AudioStartListener get the logic object is null exception, package = " + curPkgName);
                            }
                        }

                        // 处理除了Main和导航之外的所有音频流
                        List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                        if (null != listeners) {
                            for (int i = 0; i < listeners.size(); i++) {
                                BaseListener temp = listeners.get(i);
                                if (null != temp) {
                                    if (temp instanceof AudioListener) {
                                        AudioListener listener = (AudioListener) temp;
                                        // arm start.
                                        listener.ArmAudioActivieListener(false, true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void AudioStopListener(StringII val) {
            if (null != val) {
                LogUtils.d(TAG, "SystemAudioHandleDriver AudioStopListener, pkgName=" + val.getString() +
                        ", stream=" + AudioDefine.AudioStream.toString(val.getInteger1()) + "[" + val.getInteger1() + "]" +
                        ", focus=" + AudioDefine.AudioFocus.toString(val.getInteger2()) + "[" + val.getInteger2() + "]" +
                        ", active=" + false);
            }
            boolean bVoiceStopWhenThird = false;
            if (null != val && null != mContext) {
                final String curAudioPkg = val.getString();
                if (!TextUtils.isEmpty(curAudioPkg)) {
                    boolean handle = true;
                    // 判断是否是过滤的应用
                    if (handle) {
                        String[] ignore = Model().getIgnoreAudioPackages().getVal();
                        if (null != ignore) {
                            for (int i = 0; i < ignore.length; i++) {
                                final String temp = ignore[i];
                                if (curAudioPkg.equals(temp)) {
                                    handle = false;
                                    LogUtils.d(TAG, "AudioStopListener, specify ignore success!");
                                    break;
                                }
                            }
                        }
                    }
                    // 判断是否是导航
                    if (handle) {
                        if (curAudioPkg.equals(mStrGpsProcessName) || curAudioPkg.contains(mStrGpsProcessName)) {
                            handle = false;
                            // navi stop.
                            mDelayStopGps.stop();
                            mDelayStopGps.add(150, null, new BaseCallback() {
                                @Override
                                public void onCallback(int nId, Packet packet) {
                                    LogUtils.d(TAG, "GpsSound mVoiceAssistantActive=" + mVoiceAssistantActive);
                                    if (mVoiceAssistantActive) {
                                        mVoiceWhenNavi = true;
                                    } else {
                                        handleGpsSound(false);
                                        if (!mVoiceAssistantActive) {
                                            List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                            if (null != listeners) {
                                                final int size = listeners.size();
                                                for (int i = 0; i < size; i++) {
                                                    BaseListener temp = listeners.get(i);
                                                    if (null != temp) {
                                                        if (temp instanceof AudioListener) {
                                                            AudioListener listener = (AudioListener) temp;
                                                            listener.NaviAudioActiveListener(true, false);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            mDelayStopGps.start();
                        }
                    }
                    if (handle) {
                        // 判断指定为语音的包名
                        boolean bSendNaviState = false;
                        if (isVoiceAssistantActive(curAudioPkg)) {
                            handle = false;
                            mVoiceAssistantActive = false;
                            handleMusicStream(false);
                            bSendNaviState = true;
                            if (mThirdAppSoundActive) {
                                bVoiceStopWhenThird = true;
                            }

                            if (mVoiceWhenNavi) {
                                mVoiceWhenNavi = false;
                                handleGpsSound(false);
                                List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                if (null != listeners) {
                                    final int size = listeners.size();
                                    for (int i = 0; i < size; i++) {
                                        BaseListener temp = listeners.get(i);
                                        if (null != temp) {
                                            if (temp instanceof AudioListener) {
                                                AudioListener listener = (AudioListener) temp;
                                                listener.NaviAudioActiveListener(true, false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // 判断指定为胎压的包名
                        if (handle) {
                            if (curAudioPkg.equals(TPMS_PACKAGE)) {
                                mTpmsWarn = false;
                                handle = false;
                                LogUtils.d(TAG, "AudioStopListener, specify tpms success!");
                            }
                        }
                        if (handle) {
                            if ((curAudioPkg.equals(EcarHelper.E_CAR_VOIP) || curAudioPkg.equals(EcarHelper.E_CAR_CALL)) /*&&
                                    val.getInteger1() == AudioDefine.getSystemAudioStream(AudioDefine.AudioStream.STREAM_VOICE_CALL)*/) {
                                handle = false;
                                mECarActive = false;
                                EcarHelper.setEcarState(false);
                                handleMusicStream(false);
                                bSendNaviState = true;
                            }
                        }
                        if (bSendNaviState) {
                            if (mNaviSoundActive) {
                                List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                if (null != listeners) {
                                    for (int i = 0; i < listeners.size(); i++) {
                                        BaseListener temp = listeners.get(i);
                                        if (null != temp) {
                                            if (temp instanceof AudioListener) {
                                                AudioListener listener = (AudioListener) temp;
                                                listener.NaviAudioActiveListener(false, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // 第三方应用
                        if (handle) {
//                            if (mThirdAudioPkgName.equals(curAudioPkg)) {
                            handle = false;
                            mThirdAppSoundActive = false;
                        }
                        if (!mTpmsWarn && !mVoiceAssistantActive && //在有语音或胎压报警时不通知MCU切回声音通道
                                !bVoiceStopWhenThird) { //在第三方播放时，语音结束后不通知MCU切回声音通道，解决威益德出现无声音问题。ydgingen2018-05-21
                            // 语音和第三方应用都需要切回声音通道
                            List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                            if (null != listeners) {
                                final int size = listeners.size();
                                for (int i = 0; i < size; i++) {
                                    BaseListener temp = listeners.get(i);
                                    if (null != temp) {
                                        if (temp instanceof AudioListener) {
                                            AudioListener listener = (AudioListener) temp;
                                            // arm stop.//一直执行？？？？
                                            listener.ArmAudioActivieListener(true, false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    // 判断是否为语音的包名
    private boolean isVoiceAssistantActive(String pkgName) {
        boolean ret = false;
        if (null != mVoiceLogic) {
            Packet voicePacket = mVoiceLogic.getInfo();
            if (null != voicePacket) {
                String[] real = voicePacket.getStringArray("VoiceRealPacketList");
                if (null != real) {
                    for (int i = 0; i < real.length; i++) {
                        final String temp = real[i];
                        if (pkgName.equals(temp)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * the source listener.
     */
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            if (mTpmsWarn) {
                if (!ApkUtils.isProcessRunning(mContext, TPMS_PACKAGE)) {
                    mTpmsWarn = false;
                }
                LogUtils.d(TAG, "cur source change, mTpmsWarn = " + mTpmsWarn);
            }
            //模拟导航，播放视频，快速多次倒车，导航被杀掉，导致音效设置无作用，音量大小异常2018-04-03
            if (oldVal == Define.Source.SOURCE_CAMERA) {
                if (mNaviSoundActive) {
                    if (!ApkUtils.isProcessRunning(mContext, mStrGpsProcessName)) {
                        LogUtils.e(TAG, "cur source change, mNaviSoundActive = " + mNaviSoundActive);
                        handleGpsSound(false);
                        if (!mVoiceAssistantActive && !mTpmsWarn) {
                            List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                            if (null != listeners) {
                                final int size = listeners.size();
                                for (int i = 0; i < size; i++) {
                                    BaseListener temp = listeners.get(i);
                                    if (null != temp) {
                                        if (temp instanceof AudioListener) {
                                            AudioListener listener = (AudioListener) temp;
                                            listener.NaviAudioActiveListener(true, false);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LogUtils.d(TAG, "cur source change, mNaviSoundActive = " + mNaviSoundActive);
                    }
                }
            }
        }

        private boolean isMcuSource(int source) {
            if (source == Define.Source.SOURCE_RADIO ||
                    source == Define.Source.SOURCE_AUX ||
                    source == Define.Source.SOURCE_SILENT) {
                return true;
            }
            return false;
        }
        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            if (mFirstBootTime == -1) {
                mTimerId = 0;
                mFirstBootTime = System.currentTimeMillis();
            }
             if (isMcuSource(newVal) && !isMcuSource(oldVal)) {
                 if (mTimerId == 0) {
                     long currentTime = System.currentTimeMillis();
                     if ((currentTime - mFirstBootTime) > 30000) {
                         mTimerId = -1;
                     }
                 }
                 if (mTimerId == 0) {
                     mTimerId = TimerUtils.setTimer(getMainContext(), 800, 800, new Timerable.TimerListener() {//处理语音没声音问题。
                         @Override
                         public void onTimer(int paramInt) {
                             if (mMusicactive != mAudioManager.isMusicActive()) {
                                 LogUtils.e(TAG, "cur source change, isMusicActive = " + mAudioManager.isMusicActive());
                                 mMusicactive = mAudioManager.isMusicActive();
                                 List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                 if (null != listeners) {
                                     final int size = listeners.size();
                                     for (int i = 0; i < size; i++) {
                                         BaseListener temp = listeners.get(i);
                                         if (null != temp) {
                                             if (temp instanceof AudioListener) {
                                                 AudioListener listener = (AudioListener) temp;
                                                 listener.ArmAudioActivieListener(!mMusicactive, mMusicactive);
                                             }
                                         }
                                     }
                                 }
                             }

                             long currentTime = System.currentTimeMillis();
                             if ((currentTime - mFirstBootTime) > 30000) {//开机第一次切源40s后不检测isMusicActive()，避免在亿连切到收音会出现无声音的问题。2020-05-18
                                 LogUtils.e(TAG, "CurBackSourceListener--0--mFirstBootTime=" + mFirstBootTime + ", currentTime=" + currentTime + ", mTimerId=" + mTimerId);
                                 if (mTimerId > 0) {
                                     TimerUtils.killTimer(mTimerId);
                                 }
                                 mTimerId = -1;
                                 mMusicactive = false;
                             } else {
//                                 LogUtils.e(TAG, "CurBackSourceListener--1--mFirstBootTime=" + mFirstBootTime + ", currentTime=" + currentTime + ", mTimerId=" + mTimerId);
                             }
                         }
                     });
                 }
             } else if (isMcuSource(newVal)) {
                 LogUtils.d(TAG, "cur source change,11 isMusicActive:" + mAudioManager.isMusicActive() + ", mMusicactive:" + mMusicactive);
                 if (mMusicactive) {
                     mDelayArmAudio.stop();
                     mDelayArmAudio.add(3000, null, new BaseCallback() {
                         @Override
                         public void onCallback(int nId, Packet packet) {
                             if (mMusicactive) {
                                 List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                 if (null != listeners) {
                                     final int size = listeners.size();
                                     for (int i = 0; i < size; i++) {
                                         BaseListener temp = listeners.get(i);
                                         if (null != temp) {
                                             if (temp instanceof AudioListener) {
                                                 AudioListener listener = (AudioListener) temp;
                                                 listener.ArmAudioActivieListener(!mMusicactive, mMusicactive);
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     });
                     mDelayArmAudio.start();
                 }
             } else {
                 if (!isMcuSource(newVal)) {
                     if (mTimerId > 0) {
                         TimerUtils.killTimer(mTimerId);
                     }
                     mTimerId = 0;
                     mMusicactive = false;
                 }
             }

            if (Define.Source.SOURCE_SILENT == oldVal && Define.Source.SOURCE_SILENT != newVal) {
                //从第三方应用切换到Main的应用，不杀掉进程，只是去抢占焦点。只有两个第三方进程才不能共存。2017-10-21
                // 杀掉之前的进程
                if (ApkUtils.isAPKExist(mContext, mThirdAudioPkgName) &&
                        !("com.txznet.music".equals(mThirdAudioPkgName) ||
                                "com.zjinnova.zlink".equals(mThirdAudioPkgName) ||
                                "com.wwc2.dvr".equals(mThirdAudioPkgName))) {
                    LogUtils.d(TAG, "SourceListener kill process = " + mThirdAudioPkgName +
                            ", oldVal = " + Define.Source.toString(oldVal) +
                            ", newVal = " + Define.Source.toString(newVal));
                    // begin zhongyang.hu kill one apk may fail .
                    // because SystemPermission is not running or kill by LowMemmorykiller
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            STM32MCUDriver.killProcess(mContext,mThirdAudioPkgName);
                            mThirdAudioPkgName = null;
                        }
                    }).start();
//                    Packet packet = new Packet();
//                    packet.putString("pkgName", mThirdAudioPkgName);
//                    BaseLogic l = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
//                    if (null != l) {
//                        l.Notify(SystemPermissionInterface.MAIN_TO_APK.KILL_PROCCESS, packet);
//                    }
//                 mThirdAudioPkgName = null;
                  //end
                }
            }

            if (mTpmsWarn) {
                if (ApkUtils.isProcessRunning(mContext, TPMS_PACKAGE)) {
                    LogUtils.d(TAG, "SourceListener TMPS WARNING");
                    mDelayTpms.stop();
                    mDelayTpms.add(2000, null, new BaseCallback() {
                        @Override
                        public void onCallback(int nId, Packet packet) {
                            if (ApkUtils.isProcessRunning(mContext, TPMS_PACKAGE)) {
                                LogUtils.d(TAG, "SourceListener TMPS WARNING to MCU");
                                List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                                if (null != listeners) {
                                    final int size = listeners.size();
                                    for (int i = 0; i < size; i++) {
                                        BaseListener temp = listeners.get(i);
                                        if (null != temp) {
                                            if (temp instanceof AudioListener) {
                                                AudioListener listener = (AudioListener) temp;
                                                listener.ArmAudioActivieListener(false, true);
                                            }
                                        }
                                    }
                                }
                            } else {
                                mTpmsWarn = false;
                            }
                        }
                    });
                    mDelayTpms.start();
                } else {
                    mTpmsWarn = false;
                }
                LogUtils.d(TAG, "SourceListener mTpmsWarn="+mTpmsWarn);
            }

            //其他模式，叠加模拟导航，当内存紧张而导致导航被杀掉时会出现没声音的问题。
            if (mNaviSoundActive) {
//                if (!ApkUtils.isProcessRunning(mContext, mStrGpsProcessName)) {
                    LogUtils.e(TAG, "cur back source change, mNaviSoundActive = " + mNaviSoundActive);
                    handleGpsSound(false);
                    if (!mVoiceAssistantActive && !mTpmsWarn) {
                        List<BaseListener> listeners = SystemAudioDriver.this.getModel().getListeners();
                        if (null != listeners) {
                            final int size = listeners.size();
                            for (int i = 0; i < size; i++) {
                                BaseListener temp = listeners.get(i);
                                if (null != temp) {
                                    if (temp instanceof AudioListener) {
                                        AudioListener listener = (AudioListener) temp;
                                        listener.NaviAudioActiveListener(true, false);
                                    }
                                }
                            }
                        }
                    }
//                } else {
//                    LogUtils.d(TAG, "cur back source change, mNaviSoundActive = " + mNaviSoundActive);
//                }
            }
        }
    };

    /**
     * the bluetooth listener
     */
    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "HFPStatusListener bt="+newVal+", ThirdAppSoundActive="+mThirdAppSoundActive);
            final boolean oldCall = BluetoothDefine.HFPStatus.isCalling(oldVal);
            final boolean newCall = BluetoothDefine.HFPStatus.isCalling(newVal);
            if (oldCall && !newCall) {
                // 不在电话状态
                mDelayResumePlay.stop();
                mDelayResumePlay.add(1000, null, new BaseCallback() {
                    @Override
                    public void onCallback(int nId, Packet packet) {
                        mCallSoundActive = false;
                        if (isBlueToothPause) {
                            isBlueToothPause = false;
                            abandon(mAudioChangeListener);
                        }
                        handleMusicStream(false);
                    }
                });
                mDelayResumePlay.start();
            } else if (/*!oldCall && */newCall) {
                // 在电话状态
                mDelayResumePlay.stop();
                mCallSoundActive = true;
                //第三方播放时来电，抢占焦点使播放器暂停。而本地播放时会暂停。统一做法。
                //存在风险：在导航报点时结束通话，第三方还是暂停状态，只有报点结束才恢复播放。
                if (mThirdAppSoundActive) {
                    isBlueToothPause = true;
                    request(mAudioControl,//null
                            AudioDefine.AudioStream.STREAM_RING,
                            AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);//bug13452播放酷我音乐用手机拨打电话，再挂掉电话，酷我音乐不会继续播放，显示在暂停状态
                }
                handleMusicStream(true);
            }
        }
    };

    private MediaListener mMediaListener = new MediaListener() {
        /**播放状态监听器, see {@link com.wwc2.media_interface.MediaDefine.PlayStatus}*/
        public void PlayStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "PlayStatus:" + newVal + ", mIsCalling:" + mCallSoundActive+", mVoiceAssistantActive="+mVoiceAssistantActive);

            //修改：在音乐播放时快速点击下一曲，来电，通话状态时没暂停音乐。YDG 2017-04-13
            if (MediaDefine.PlayStatus.isPlay(newVal) &&
                    (mCallSoundActive || mVoiceAssistantActive)) {
                pause();
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        // init ignore audio packages.
        String[] string = new String[]{
                "com.wwc2.main",//Main
                "com.android.systemui",//SystemUI
                "com.android.deskclock",//DeskClock
                "com.goodocom.gocsdk",//gocsdk
//                "com.wwc2.dvr",
        };
        Model().getIgnoreAudioPackages().setVal(string);

        // super create.
        super.onCreate(packet);
        LogUtils.d(TAG, "onCreate");
        mFirstBootTime = -1;

        mContext = getMainContext();
        if (null != mContext) {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(AUDIO_FOCUS_PACKAGENAME);
            myIntentFilter.addAction(KLD_NAVI_MSG);
            //注册翼卡接收广播
            myIntentFilter.addAction(EcarHelper.ACTION_CALL_IDLE);
            myIntentFilter.addAction(EcarHelper.ACTION_CALL_INCOMING);
            myIntentFilter.addAction(EcarHelper.ACTION_CALL_OFFHOOK);
            mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);
        }

        // bind.
        getModel().bindListener(mAudioListener);
        SourceManager.getModel().bindListener(mSourceListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);
        ModuleManager.getLogicByName(com.wwc2.audio_interface.AudioDefine.MODULE).getModel().bindListener(mMediaListener);
        // get
        mNaviLogic = ModuleManager.getLogicByName(NaviDefine.MODULE);
        mVoiceLogic = ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE);
    }

    @Override
    public void onDestroy() {
        // get.
        mVoiceLogic = null;
        mNaviLogic = null;

        // unbind.
        SourceManager.getModel().unbindListener(mSourceListener);
        getModel().unbindListener(mAudioListener);
        ModuleManager.getLogicByName(com.wwc2.audio_interface.AudioDefine.MODULE).getModel().bindListener(mMediaListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);

        if (null != mContext) {
            try {
                mContext.unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
