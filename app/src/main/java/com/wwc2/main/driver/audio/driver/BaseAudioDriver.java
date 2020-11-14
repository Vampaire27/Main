package com.wwc2.main.driver.audio.driver;

import android.os.Handler;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MStringArray;
import com.wwc2.corelib.model.custom.MStringIIBooleanArray;
import com.wwc2.corelib.model.custom.StringII;
import com.wwc2.corelib.model.custom.StringIIBoolean;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriverable;
import com.wwc2.main.driver.audio.AudioListener;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.manager.SourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * the base audio driver.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public abstract class BaseAudioDriver extends BaseMemoryDriver implements AudioDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseAudioDriver";

    /**
     * 音频焦点发生变化
     */
    public interface AudioChangeListener {
        /**
         * 音频焦点变化
         *
         * @param focusChange see {@link AudioDefine.AudioFocus}
         */
        void onAudioFocusChange(int focusChange);
    }

    /**
     * the audio array.
     */
    private MStringIIBooleanArray mAudios           = null;

    /**
     * the audio control interface.
     */
    protected static AudioControlable mAudioControl   = null;

    /**
     * the audio focus pause.
     */
    protected static boolean mAudioFocusPause       = false;

    /**
     * navigation sound active.
     */
    protected static boolean mNaviSoundActive       = false;

    protected static String mStrGpsProcessName      = "";

    /**
     * third app sound active.
     */
    protected static boolean mThirdAppSoundActive   = false;

    /**
     * call sound active.
     */
    protected static boolean mCallSoundActive       = false;

    protected static boolean mVoiceAssistantActive  = false;

    protected static boolean isBlueToothPause       = false;//打开后视镜 来电或者接电话 后会请求request方法

    protected static boolean isVoicePauseAudio      = false;

    protected static boolean mECarActive            = false;

    protected static boolean mTpmsWarn              = false;

    protected static boolean mVoiceWhenNavi         = false;

    /**
     * audio control lock object.
     */
    private static final Lock mAudioControlLock = new ReentrantLock();

    protected void handleGpsSound(boolean active) {
        mNaviSoundActive = active;
    }

    protected void handleMusicStream(boolean active) {
        LogUtils.d(TAG, "handleMusicStream---active="+active+", third="+mThirdAppSoundActive+
                ", voice="+mVoiceAssistantActive+", call="+mCallSoundActive+", navi="+mNaviSoundActive);
        if (active) {
            if (mAudioControl != null) {
                if (mAudioControl.isPlay()) {
                    pause();
                }
            }
        } else {
            if (isVoicePauseAudio) {
                isVoicePauseAudio = false;
            } else {
                play();//在音乐叠加导航时，语音->导航报点->结束语音，不会播放音乐。2017-08-07 YDG
            }
//            new Handler(getMainContext().getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (isVoicePauseAudio) {
//                        isVoicePauseAudio = false;
//                    } else {
//                        play();//在音乐叠加导航时，语音->导航报点->结束语音，不会播放音乐。2017-08-07 YDG
//                    }
//                }
//            }, 500);
        }
    }
    /**
     * the audio change listener.
     */
    //焦点变化只接收framework的广播进行处理，系统监听暂时保留。
    protected AudioChangeListener mAudioChangeListener = new AudioChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

            if (null != mAudioControl) {
                LogUtils.d(TAG, Define.Source.toString(SourceManager.getCurBackSource()) + " audio, focus = " + AudioDefine.AudioFocus.toString(focusChange));
                switch (focusChange) {
                    case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN:
                    case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT:
                    case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    case AudioDefine.AudioFocus.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
//                        mThirdAppSoundActive = false;
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (isVoicePauseAudio) {
//                                    isVoicePauseAudio = false;
//                                } else {
//                                    play();
//                                }
//                            }
//                        }, 300);
                        break;
                    case AudioDefine.AudioFocus.AUDIOFOCUS_LOSS:
                    case AudioDefine.AudioFocus.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioDefine.AudioFocus.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                        mThirdAppSoundActive = true;
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mNaviSoundActive /*&& gpsMix*/) {
//
//                                } else {
//                                    pause();
//                                }
//                            }
//                        }, 100);
                        break;
                    default:
                        break;
                }
            } else {
                LogUtils.d("mAudioControl==null!!!!!!!!!!");
            }
        }
    };

    /**
     * 数据Model
     */
    protected static class AudioModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet ret = new Packet();
            ret.putStringArray("IgnoreAudioPackages", mIgnoreAudioPackages.getVal());
            return ret;
        }

        /**
         * 忽略音频处理包名列表
         */
        private MStringArray mIgnoreAudioPackages = new MStringArray(this, "IgnoreAudioPackagesListener", null);

        public MStringArray getIgnoreAudioPackages() {
            return mIgnoreAudioPackages;
        }
    }

    @Override
    public BaseModel newModel() {
        return new AudioModel();
    }

    /**
     * get the model object.
     */
    protected AudioModel Model() {
        AudioModel ret = null;
        BaseModel model = getModel();
        if (model instanceof AudioModel) {
            ret = (AudioModel) model;
        }
        return ret;
    }

    /**
     * 申请音频
     */
    protected abstract int request(AudioChangeListener listener, int stream, int focus);

    /**
     * 释放音频
     */
    protected abstract int abandon(AudioChangeListener listener);

    @Override
    public String filePath() {
        return "AudioDataConfig.ini";
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        LogUtils.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        unregAudios();
        super.onDestroy();
    }

    @Override
    public int request(AudioControlable control, int stream, int focus) {
        LogUtils.d("request  mAudioFocusPause:" + mAudioFocusPause + ",isBlueToothPause:" + isBlueToothPause);
        if (control == null) {
            LogUtils.d("request  control = null");
        }
        mAudioControl = control;
        return request(mAudioChangeListener, stream, focus);
    }

    @Override
    public int abandon() {
        LogUtils.d("abandon  AudioControl = null");
        mAudioControl = null;
        if (true) {
            LogUtils.d(TAG, "main abandon focus return");
            return AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_DEFAULT;
        }
        return abandon(mAudioChangeListener);
    }

    @Override
    public void setGpsSoundActive(boolean active) {
        LogUtils.d(TAG, "setGpsSoundActive---active="+active+", navi="+mNaviSoundActive);
        if (!active && mNaviSoundActive) {
            mNaviSoundActive = false;
            setAudioActive(mStrGpsProcessName, AudioDefine.AudioStream.STREAM_ALARM, AudioDefine.AudioFocus.AUDIOFOCUS_LOSS, false);
        }
    }

    @Override
    public void setBlueCallActive(boolean active) {
        LogUtils.d("setBlueCallActive---active=" + active + ", mCallSoundActive=" + mCallSoundActive);
        //Carplay通话时会有导航报点的声音。
        //由于Carplay通话也是走的ALARM音频流，所以下面暂不处理，否则会出现通话无声音的问题。2019-12-07
//        mCallSoundActive = active;
//        handleMusicStream(active);
    }

    @Override
    public boolean autoSave() {
        return false;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            // 读取忽略音频处理包名列表
            int i = 1;
            while (true) {
                Object object = mMemory.get("IGNORE", "package" + i);
                String temp = null;
                if (object instanceof String) {
                    temp = (String) object;
                    if (!TextUtils.isEmpty(temp)) {
                        Model().getIgnoreAudioPackages().addVal(-1, temp.trim());
                    }
                }

                if (TextUtils.isEmpty(temp)) {
                    break;
                } else {
                    ret = true;
                    i++;
                }
            }
        }
        return ret;
    }

    /**
     * register the audio.
     */
    protected void regAudio(StringIIBoolean audio) {
        if (null == mAudios) {
            mAudios = new MStringIIBooleanArray(null, null, null);
        }
        if (null != audio) {
            if (!TextUtils.isEmpty(audio.getString())) {
                mAudios.addVal(-1, audio);
            }
        }
    }

    /**
     * unregister the audios.
     */
    protected void unregAudios() {
        mAudios = null;
    }

    /**
     * 获取音频下标
     */
    protected int getAudioIndex(String pkgName, int stream) {
        int ret = -1;
        if (!TextUtils.isEmpty(pkgName)) {
            if (null != mAudios) {
                StringIIBoolean[] audios = mAudios.getVal();
                if (null != audios) {
                    for (int i = 0; i < audios.length; i++) {
                        final StringIIBoolean temp = audios[i];
                        if (null != temp) {
                            if (pkgName.equals(temp.getString())) {
                                if (stream == temp.getInteger1()) {
                                    ret = i;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 获取音频长度
     */
    protected int getAudioLength() {
        int ret = 0;
        if (null != mAudios) {
            ret = mAudios.getVal().length;
        }
        return ret;
    }

    /**
     * 通过包名获取音频列表
     */
    protected List<StringIIBoolean> getAudiosByPkgName(String pkgName) {
        List<StringIIBoolean> ret = null;
        if (!TextUtils.isEmpty(pkgName)) {
            if (null != mAudios) {
                final int length = mAudios.getVal().length;
                for (int i = 0; i < length; i++) {
                    final StringIIBoolean temp = mAudios.getVal()[i];
                    if (null != temp) {
                        if (pkgName.equals(temp.getString())) {
                            if (null == ret) {
                                ret = new ArrayList<>();
                            }
                            ret.add(temp);
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 设置音频活动
     */
    protected void setAudioActive(String pkgName, int stream, int focus, boolean active) {
        if (!TextUtils.isEmpty(pkgName)) {
            StringIIBoolean audio = new StringIIBoolean(pkgName, stream, focus, active);
            final int index = getAudioIndex(pkgName, stream);
            final int length = getAudioLength();
            if (index >= 0 && index < length) {
                // exist
                notify(pkgName, stream, focus, active);
            } else {
                // not exist
                if (active) {
                    notify(pkgName, stream, focus, true);
                }

                regAudio(audio);
            }
        }
    }

    /**
     * notify listener.
     */
    private void notify(String pkgName, int stream, int focus, boolean active) {
        // set the listener.
        StringII status = new StringII(pkgName, stream, focus);
        List<BaseListener> listeners = getModel().getListeners();
        if (null != listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                BaseListener temp = listeners.get(i);
                if (null != temp) {
                    if (temp instanceof AudioListener) {
                        AudioListener listener = (AudioListener) temp;
                        if (active) {
                            listener.AudioStartListener(status);
                        } else {
                            listener.AudioStopListener(status);
                        }
                    }
                }
            }
        }
    }

    /**
     * 继续播放
     */
    protected synchronized void play() {
        mAudioControlLock.lock();
        LogUtils.d(TAG, Define.Source.toString(SourceManager.getCurBackSource()));
        try {
            // 三者满足，则继续播放
            if (null != mAudioControl) {
                if ((!mThirdAppSoundActive || (mThirdAppSoundActive && mNaviSoundActive)) && !mCallSoundActive) {
                    final boolean play = mAudioControl.isPlay();
                    LogUtils.d(TAG, Define.Source.toString(SourceManager.getCurBackSource()) + " audio focus play, play = " + play + ", mAudioFocusPause = " + mAudioFocusPause);
                    if (mAudioFocusPause) {
                        mAudioFocusPause = false;
                        if (null != mAudioControl) {
                            LogUtils.d(TAG, "mAudioControl.play()----11");
                            mAudioControl.play();
                        }
                    }
                } else {
                    LogUtils.w(TAG, "audio focus play failed, mNaviSoundActive = " + mNaviSoundActive +
                            ", mThirdAppSoundActive = " + mThirdAppSoundActive +
                            ", mCallSoundActives = " + mCallSoundActive);
                }
            }
        } finally {
            mAudioControlLock.unlock();
        }
    }

    /**
     * 暂停
     */
    protected synchronized void pause() {
        mAudioControlLock.lock();
        try {
            if (null != mAudioControl) {
                final boolean play = mAudioControl.isPlay();
                LogUtils.d(TAG, Define.Source.toString(SourceManager.getCurBackSource()) + " audio focus pause, play = " + play);
                if (play) {
                    mAudioFocusPause = true;
                    if (null != mAudioControl) {
                        mAudioControl.pause();
                    }
                }
            }
        } finally {
            mAudioControlLock.unlock();
        }
    }

    public static void pauseByVoice(boolean pause) {
        if (pause) {
            isVoicePauseAudio = true;
            mAudioFocusPause = false;
        }
    }

    public static boolean getVoiceAssistantActive() {
        return mVoiceAssistantActive;
    }
}
