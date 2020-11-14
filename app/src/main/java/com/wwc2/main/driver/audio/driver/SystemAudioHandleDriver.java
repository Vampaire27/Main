package com.wwc2.main.driver.audio.driver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.wwc2.common_interface.Provider;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.bluetooth.EcarHelper;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.manager.CPUThermalManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.navi.NaviListener;
import com.wwc2.main.navi.driver.NaviDriverable;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.settings_interface.SettingsDefine;

import java.io.FileOutputStream;

/**
 * the system audio handle driver.
 * 主要处理AudioManager相关
 * @author wwc2
 * @date 2017/1/17
 */
public class SystemAudioHandleDriver extends SystemAudioDriver {

    protected static final String TAG = "SystemAudioHandleDriver";

    /**
     * the audio manager.
     */
//    private AudioManager mAudioManager = null;

    /**
     * the audio focus change.
     */
    private AudioChangeListener mAudioChangeListener = null;

    /**
     * the current volume.
     */

    private  int defealutValue  =7;//默认音量大小

    private final int DEFAULT_MUSIC_VOLUME_12  = 12;


    private final int DELAY_CHECK_GPS       = 1;
    private final int DELAY_TIME            = 100;
    private long gpsSoundTime               = 0;

    /**
     * audio focus change listener.
     */
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (null != mAudioChangeListener) {
                final int focus = AudioDefine.getAudioFocus(focusChange);
                mAudioChangeListener.onAudioFocusChange(focus);
            }
        }
    };

    private void initAudioManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        /**导航混音 YDG 2017-03-30 Begin**/
//        context.getContentResolver().registerContentObserver(Provider.ProviderColumns.CONTENT_URI, true,
//                new pkgObserver(new Handler()));
        mStrGpsProcessName = query_pkg_name(context);
        mAudioManager.setGpsProcessName(mStrGpsProcessName);
        //begin zhongyang.hu_oom add
        sendNaviPackageToFW(mStrGpsProcessName);
        //end
        mAudioManager.setGpsBegin(false);
        mCheckGpsOn.sendEmptyMessageDelayed(DELAY_CHECK_GPS, DELAY_TIME);
        // 导航包名变化
        ModuleManager.getLogicByName(NaviDefine.MODULE).getModel().bindListener(mNaviListener);
        /**导航混音 YDG 2017-03-30 end**/

        //一开机语音没有声音。2017-06-07
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);

//        if("mt6739".equals(CPUThermalManager.mPlatfromID)){
//            defealutValue = DEFAULT_MUSIC_VOLUME_12;
//        }

        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, defealutValue, AudioManager.FLAG_PLAY_SOUND);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defealutValue, AudioManager.FLAG_PLAY_SOUND);
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, defealutValue, AudioManager.FLAG_PLAY_SOUND);

        mNaviSoundActive = false;
        mCallSoundActive = false;
        mVoiceAssistantActive = false;
        mECarActive = false;
        EcarHelper.setEcarState(false);
        mTpmsWarn = false;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        LogUtils.d(TAG, "onCreate");
        Context context = getMainContext();
        if (null != context) {
            initAudioManager(context);
        }
    }

    @Override
    public void onDestroy() {
        mCheckGpsOn.removeMessages(DELAY_CHECK_GPS);
        ModuleManager.getLogicByName(NaviDefine.MODULE).getModel().unbindListener(mNaviListener);

        super.onDestroy();
    }

    @Override
    protected int request(AudioChangeListener listener, int stream, int focus) {
        int ret = AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_DEFAULT;
        if (null != mAudioManager) {
            mAudioChangeListener = listener;
            final int status = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                    AudioDefine.getSystemAudioStream(stream),
                    AudioDefine.getSystemAudioFocus(focus));
            ret = AudioDefine.getAudioStatus(status);
        }
        return ret;
    }

    @Override
    protected int abandon(AudioChangeListener listener) {
        int ret = AudioDefine.AudioStatus.AUDIOFOCUS_REQUEST_DEFAULT;
        if (null != mAudioManager) {
            mAudioChangeListener = listener;
            final int status = mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
            ret = AudioDefine.getAudioStatus(status);
        }
        return ret;
    }

//    private final class pkgObserver extends ContentObserver {
//        public pkgObserver(Handler handler) {
//            super(handler);
//        }
//
//        @Override
//        public void onChange(boolean selfChange, Uri uri) {
//            String gpsPkgName = query_pkg_name(getMainContext());
//            if (!mStrGpsProcessName.equals(gpsPkgName)) {
//                mStrGpsProcessName = gpsPkgName;
//                mAudioManager.setGpsProcessName(mStrGpsProcessName);
//                //begin zhongyang.hu_oom add
//                sendNaviPackageToFW(mStrGpsProcessName);
//                //end
//                LogUtils.w(TAG, "onChange pakName=" + mStrGpsProcessName);
//            }
//        }
//    }

    private String query_pkg_name(Context context) {
        String ret = null;
//        ContentResolver resolver = context.getContentResolver();
//        if (null != resolver) {
//            Cursor cursor = resolver.query(com.wwc2.common_interface.Provider.ProviderColumns.CONTENT_URI, null, null, null, null);
//            if (cursor != null) {
//                if (cursor.moveToFirst()) {
//                    ret = cursor.getString(cursor.getColumnIndex(com.wwc2.navi_interface.Provider.NAVI_PKG_NAME()));
//                }
//                cursor.close();
//            }
//        }
        BaseDriver driver = ModuleManager.getLogicByName(NaviDefine.MODULE).getDriver();
        if (driver instanceof NaviDriverable) {
            NaviDriverable naviDriverable = (NaviDriverable) driver;
            ret = naviDriverable.getNavigationPacketName();
        }
        LogUtils.d(TAG, "navi packet name ="+ret);
        return ret;
    }

    private Handler mCheckGpsOn = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DELAY_CHECK_GPS:
                    mCheckGpsOn.removeMessages(DELAY_CHECK_GPS);
                    if (mAudioManager.getGpsBegin()) {
                        mAudioManager.setGpsBegin(false);
                        if (mAudioManager.getGpsOn()) {
                            gpsSoundTime = System.currentTimeMillis();
                        } else {
                            gpsSoundTime = 0;
                            //报点完成
                            handleGpsSound(false);
                        }
                    }
                    if (gpsSoundTime > 0 && System.currentTimeMillis() - gpsSoundTime > 100) {
                        //报点开始
                        handleGpsSound(true);
                    }
                    mCheckGpsOn.sendEmptyMessageDelayed(DELAY_CHECK_GPS, DELAY_TIME);
                    break;
                default:
                    break;
            }
        }
    };

    NaviListener mNaviListener = new NaviListener() {
        /**
         * 导航包名发生变化
         */
        @Override
        public void NaviPacketNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "NaviPacketNameListener--oldVal=" + oldVal + ", newVal=" + newVal);
            mStrGpsProcessName = newVal;
            mAudioManager.setGpsProcessName(newVal);
            //begin zhongyang.hu_oom add
            sendNaviPackageToFW(mStrGpsProcessName);
            //end
        }
    };

    @Override
    protected void handleGpsSound(boolean active) {
        super.handleGpsSound(active);
//        if (on) {
//            write('1');
//        } else {
//            write('0');
//        }
        if (mVoiceAssistantActive || mCallSoundActive || mECarActive) {
            return;//在语音播报或通话时，不对音乐流处理，解决在语音或通话会出现音乐的声音。2017-10-19 YDG
        }
        //报点开始
        final Packet common = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getInfo();
        final boolean gpsMix = common.getBoolean(SettingsDefine.Common.Switch.GPS_MIX.value());
        if (active) {
            final int gpsMixRate = common.getInt(SettingsDefine.Common.Switch.GPS_MIX_RATIO.value());
            LogUtils.d(TAG, "handleGpsSound gpsMix:" + gpsMix + ", gpsMixRate:" + gpsMixRate);
            if (gpsMix) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (defealutValue * gpsMixRate) / SettingsDefine.Common.MIXRATIO_MAX, AudioManager.FLAG_PLAY_SOUND);
            } else {
                //导航混音关时，不暂停音乐，而是mute。YDG 2017-05-09
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            }
        } else {
            LogUtils.d(TAG, "handleGpsSound Before GPS MusicVolume Cur:" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            if (!gpsMix) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defealutValue, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    @Override
    protected void handleMusicStream(boolean active) {
        super.handleMusicStream(active);
        LogUtils.d(TAG, "handleMusicStream---active="+active);
        if (active) {
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
        } else {
            if (!mCallSoundActive && !mVoiceAssistantActive && !mECarActive) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
                if (mNaviSoundActive) {
                    final Packet common = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getInfo();
                    final boolean gpsMix = common.getBoolean(SettingsDefine.Common.Switch.GPS_MIX.value());
                    final int gpsMixRate = common.getInt(SettingsDefine.Common.Switch.GPS_MIX_RATIO.value());
                    LogUtils.d(TAG, "handleMusicStream  gpsMix:" + gpsMix + ", gpsMixRate:" + gpsMixRate + ", mCurMusicVolume=" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                    if (gpsMix) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                (defealutValue * gpsMixRate) / SettingsDefine.Common.MIXRATIO_MAX, AudioManager.FLAG_PLAY_SOUND);
                    } else {
                        //导航混音关时，不暂停音乐，而是mute。YDG 2017-05-09
                        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    }
                } else {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defealutValue, AudioManager.FLAG_PLAY_SOUND);
                }
            }
        }
    }

    //begin zhongyang.hu_oom
    void sendNaviPackageToFW(String name) {
        Intent intent = new Intent("android.intent.action.SYSTEM_NAVI_NAME");
        intent.putExtra("package-name", name);
        getMainContext().sendBroadcast(intent);
    }
    //end zhongyang.hu_oom

    /**
     * 导航音gpio通知接口, 导航出声音 写入字符 0 gpio75拉低, 导航出声音结束 写入字符 1 gpio75拉高
     */
    private void write(Character n) {
        try {
            FileOutputStream fbp = new FileOutputStream(
                    "/proc/navi_message");
            fbp.write(n);
            fbp.flush();
            fbp.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}