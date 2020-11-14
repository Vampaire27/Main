package com.wwc2.main.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.bluetooth_interface.BluetoothInterface;
import com.wwc2.bluetooth_interface.Provider;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.custom.IntegerSS;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerSSS;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.bluetooth.driver.BluetoothDriverable;
import com.wwc2.main.bluetooth.driver.goc.GocBluetoothDriver;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.audio.driver.BaseAudioDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.manager.VolumeManager;
import com.wwc2.main.phonelink.PhonelinkLogic;
import com.wwc2.main.provider.LogicProviderHelper;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the bluetooth logic.
 *
 * @author wwc2
 * @date 2017/1/5
 */
public class BluetoothLogic extends BaseLogic {

    /**
     * log.s tag.
     */
    public static final String TAG = "BluetoothLogic";

    /**
     * 是否切到蓝牙音乐
     */
    private boolean mSwitchToMusic = false;

    /**
     * Delay pause the bluetooth music.
     */
    private int mDelayNumberTimerID = 0;

    private boolean mHideByEcar = false;

    private final int SEND_LEN   = 800;
    private int deviceListIndex = 0;

    /**
     * ContentProvider.
     */
    static {
        LogicProviderHelper.Provider(Provider.HFP_STATUS(), "" + BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT);
    }


    @Override
    public String getTypeName() {
        return "Bluetooth";
    }

    @Override
    public String getMessageType() {
        return BluetoothDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.bluetooth";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.bluetooth.ui.MainActivity";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_BLUETOOTH;
    }

    @Override
    public boolean isSource() {
        return true;
    }


    @Override
    public BaseDriver newDriver() {
        return new GocBluetoothDriver();
    }

    /**
     * the driver interface.
     */
    protected BluetoothDriverable Driver() {
        BluetoothDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof BluetoothDriverable) {
            ret = (BluetoothDriverable) driver;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }

        getModel().bindListener(mBluetoothListener);

        SourceManager.getModel().bindListener(mSourceListener);


        LogicProviderHelper.getInstance().update(Provider.HFP_STATUS(), "" + getInfo().getInt("HFPStatus"));

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.wwc2.bluetooth.open");
        filter.addAction("com.wwc2.bluetooth.close");
        getMainContext().registerReceiver(mIntentReceiver, filter);

        //注册翼卡接收广播
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(EcarHelper.ACTION_ECAR_SEND);
        filter1.addAction(EcarHelper.ACTION_CALL_IDLE);
        filter1.addAction(EcarHelper.ACTION_CALL_INCOMING);
        filter1.addAction(EcarHelper.ACTION_CALL_OFFHOOK);
        filter1.addAction(EcarHelper.E_CAR_OCCUPY_SCREEN);
        filter1.addAction(EcarHelper.ACTION_OCCUPY_SCREEN);
        filter1.addAction(PhonelinkLogic.ACTION_EC_A2DP_ACQUIRE);
        filter1.addAction(PhonelinkLogic.ACTION_EC_A2DP_RELEASE);
        getMainContext().registerReceiver(ecarBroadcastReceiver, filter1);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        SourceManager.getModel().unbindListener(mSourceListener);

        getModel().unbindListener(mBluetoothListener);

        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }

        try {
            getMainContext().unregisterReceiver(mIntentReceiver);
            getMainContext().unregisterReceiver(ecarBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    /**
     * 模式监听器
     */
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            Packet packet = BluetoothLogic.this.getInfo();
            if (null != packet) {
                final int status = packet.getInt("HFPStatus", -1);
                if (-1 != status) {

                    if (BluetoothDefine.HFPStatus.isCalling(status)) {
                        // 在蓝牙界面，隐藏通话悬浮框；不在蓝牙界面，显示通话悬浮框
                        boolean open = true;
                        BaseLogic logic = ModuleManager.getLogicBySource(newVal);
                        if (null != logic) {
                            open = !logic.isHFPFloatHideSource();
                        }
                        if (open) {
                            openFloatWindow();
                        } else {
                            closeFloatWindow();
                        }
                    }
                }
            }
        }

        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG,"CurBackSourceListener " +" oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            if (newVal == Define.Source.SOURCE_PHONELINK) {
                Driver().musicUnmute();
            } else if (oldVal == Define.Source.SOURCE_PHONELINK) {
                if (newVal != source()) {
                    if (isMusicPlay()) {
                        Driver().musicPause();
                    }
                    Driver().musicMute();
                }
            }
        }
    };


    /**
     * 蓝牙本身监听器
     */
    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void ModuleStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "ModuleStatusListener, oldVal = " + BluetoothDefine.ModuleStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.ModuleStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("ModuleStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MODULE_STATUS, packet);
        }

        @Override
        public void MusicPlaytimeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MusicPlaytimeListener, oldVal =" + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putInt("PlayTime", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MUSIC_PLAY_TIME, packet);
        }

        @Override
        public void MusicIndexListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MusicIndexListener, o ldVal =" + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putInt("Index", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MUSIC_INDEX, packet);
        }

        @Override
        public void MusicTotalListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MusicTotalListener, oldVal =" + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putInt("Total", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MUSIC_TOTAL, packet);
        }

        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "HFPStatusListener, oldVal = " + BluetoothDefine.HFPStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.HFPStatus.toString(newVal));

            if (EcarHelper.getEcarState()) {//避免刚来电切换没作用
                if (!EcarHelper.getEcarPhoneState()) {
                    Packet packet = getModel().getInfo();
                    if (null != packet) {
                        if (!packet.getBoolean("VoiceInPhone")) {
                            Driver().phoneTransfer();
                            LogUtils.d(TAG, "VoiceInPhone false");
                        } else {
                            LogUtils.d(TAG, "VoiceInPhone true");
                        }
                    }
                }
            }

            // 进入通话状态，弹出通话悬浮框,解除静音状态
            if (!BluetoothDefine.HFPStatus.isCalling(oldVal) && BluetoothDefine.HFPStatus.isCalling(newVal)) {
                // add by hwei 170929;fix bug 9831
                if (VolumeManager.getMute()) {
                    VolumeDriver.Driver().mute(false);
                }
                //end add
                boolean open = true;
                BaseLogic logic = ModuleManager.getFrontLogic();
                if (null != logic) {
                    open = !logic.isHFPFloatHideSource();
                }

                if (EcarHelper.getEcarState() || EcarHelper.getEcarPhoneState()) {
                    mHideByEcar = true;
                    open = false;
                }

                if (open) {
                    openFloatWindow();
                }
            }
            if (BluetoothDefine.HFPStatus.isTalking(newVal)) {
                //发送通话中状态给翼卡
                EcarHelper.sendMsgToEcar(getMainContext(), EcarHelper.CMDToEcar.CallState.name(),
                        String.valueOf(EcarHelper.BT_CALL_OFFHOOK));
            } else {
                EcarHelper.sendMsgToEcar(getMainContext(), EcarHelper.CMDToEcar.CallState.name(),
                        String.valueOf(EcarHelper.BT_CALL_RINGING));
            }
            // 退出通话状态，隐藏通话悬浮框,重置mic状态为打开
            if (BluetoothDefine.HFPStatus.isCalling(oldVal) && !BluetoothDefine.HFPStatus.isCalling(newVal)
                    || BluetoothDefine.HFPStatus.isHangup(newVal)) {
                closeFloatWindow();
                Driver().MicSwitch('1');

                //发送通话结束状态给翼卡
                EcarHelper.sendMsgToEcar(getMainContext(), EcarHelper.CMDToEcar.CallState.name(),
                        String.valueOf(EcarHelper.BT_CALL_IDLE));

                m_callNumber = null;
                m_callTime = 0;
            }
            //蓝牙连接状态变化发送给翼卡
            if (BluetoothDefine.HFPStatus.isDisConnect(oldVal) && !BluetoothDefine.HFPStatus.isDisConnect(newVal)) {
                EcarHelper.sendMsgToEcar(getMainContext(),EcarHelper.CMDToEcar.BluetoothState.name(),
                        String.valueOf(EcarHelper.BT_CONNECTED));
            } else if (BluetoothDefine.HFPStatus.isDisConnect(newVal)) {
                EcarHelper.sendMsgToEcar(getMainContext(),EcarHelper.CMDToEcar.BluetoothState.name(),
                        String.valueOf(EcarHelper.BT_DISCONNECT));

                m_callNumber = null;
                m_callTime = 0;
            }
            // 发送给APK
            Packet packet = new Packet();
            packet.putInt("HFPStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.HFP_STATUS, packet);

            // 保存状态
            LogicProviderHelper.getInstance().update(Provider.HFP_STATUS(), "" + newVal);

            mHfpStatus = newVal;
            sendCallInfoToMcu();
        }

        @Override
        public void LocalNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "LocalNameListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("LocalName", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.LOCAL_NAME, packet);
        }

        @Override
        public void LocalMacAddrListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "LocalMacAddrListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("LocalMacAddr", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.LOCAL_MACADDR, packet);
        }

        @Override
        public void PinCodeListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "PinCodeListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("PinCode", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.PIN_CODE, packet);
        }

        @Override
        public void AutoConnectListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "AutoConnectListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putBoolean("AutoConnect", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.AUTO_CONNECT, packet);
        }

        @Override
        public void AutoAnswerListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "AutoAnswerListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putBoolean("AutoAnswer", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.AUTO_ANSWER, packet);
        }

        @Override
        public void PairingNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "PairingNameListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("PairingName", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.PAIRING_NAME, packet);
        }

        @Override
        public void PairingMacAddrListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "PairingMacAddrListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("PairingMacAddr", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.PAIRING_MACADDR, packet);
        }

        @Override
        public void CallTimeListener(Long oldVal, Long newVal) {
//            LogUtils.d(TAG, "CallTimeListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putLong("CallTime", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.CALL_TIME, packet);

            m_callTime = newVal;
            sendCallInfoToMcu();
        }

        @Override
        public void CallNumberListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "CallNumberListner,oldVal = " + oldVal + ", newVal = " + newVal);
            final Packet packet = new Packet();
            packet.putString("CallNumber", newVal);

            m_callNumber = newVal;
            sendCallInfoToMcu();

            //通过号码获取联系人姓名
            Packet infoPacket = getInfo();
            Parcelable[] phonebookList = infoPacket.getParcelableArray("PhonebookList");
            if (phonebookList != null) {
                for (int i = 0; i < phonebookList.length; i++) {
                    if (phonebookList[i] instanceof IntegerSSBoolean) {
                        IntegerSSBoolean integerSSBoolean = (IntegerSSBoolean) phonebookList[i];
                        String number = integerSSBoolean.getString2();
                        if (pickNum(number) != null &&
                                pickNum(number).equals(pickNum(newVal))) {
                            String callName = integerSSBoolean.getString1();
                            packet.putString("CallName", callName);
                            LogUtils.d(TAG, "CallName = " + callName);
                            break;
                        }
                    }
                }
            }
            if (mDelayNumberTimerID != 0) {
                TimerUtils.killTimer(mDelayNumberTimerID);
            }
            mDelayNumberTimerID = TimerUtils.setTimer(getMainContext(), 200, 200, new Timerable.TimerListener() {
                @Override
                public void onTimer(int paramInt) {
                    if (!EcarHelper.getEcarState()) {//防止语音播报
                        Notify(BluetoothInterface.MAIN_TO_APK.CALL_NUMBER, packet);
                    }
                    TimerUtils.killTimer(mDelayNumberTimerID);
                }
            });
        }

        @Override
        public void MusicStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MusicStatusListener, oldVal = " + BluetoothDefine.MusicStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.MusicStatus.toString(newVal));

            LogUtils.d(TAG, "MusicStatusListener...getEasyConnA2dp="+PhonelinkLogic.getEasyConnA2dp()+
                    ", backsource="+SourceManager.getCurBackSource());
            Packet packet = new Packet();
            packet.putInt("MusicStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MUSIC_STATUS, packet);

            // 蓝牙音乐操作
            if (!BluetoothDefine.MusicStatus.isPlay(oldVal) && BluetoothDefine.MusicStatus.isPlay(newVal)) {
                if (source() != SourceManager.getCurBackSource() &&
                        (!PhonelinkLogic.getEasyConnA2dp() && SourceManager.getCurBackSource() != Define.Source.SOURCE_PHONELINK)) {//处理一键上网投屏声音问题。2019-01-18
                    // 不在蓝牙后台，则Mute掉蓝牙音乐
                    LogUtils.e(TAG, "不在蓝牙后台，不允许播放蓝牙音乐"+"当前后台："+Define.Source.toString(SourceManager.getCurBackSource()));
                    Driver().musicMute();
                }
            }
        }

        @Override
        public void MusicArtistNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "MusicArtistNameListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("MusicArtistName", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MUSIC_ARTISTNAME, packet);
        }

        @Override
        public void MusicSongNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "MusicSongNameListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("MusicSongName", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MUSIC_SONGNAME, packet);
        }

        @Override
        public void MusicAlbumNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "MusicAlbumNameListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("MusicAlbumName", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MUSIC_ALBUMNAME, packet);
        }

        @Override
        public void VoiceInPhoneListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "VoiceInPhoneListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putBoolean("VoiceInPhone", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.VOICE_IN_PHONE, packet);
        }

        @Override
        public void MicMuteListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "MicMuteListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putBoolean("MicMute", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MIC_MUTE, packet);
        }

        @Override
        public void VersionListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "VersionListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putString("Version", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.VERSION, packet);
        }

        @Override
        public void A2DPConnectStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "A2DPConnectStatusListener, oldVal = " + BluetoothDefine.A2DPConnectStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.A2DPConnectStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("A2DPConnectStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.A2DP_CONNECT_STATUS, packet);
        }

        @Override
        public void OpenOrCloseListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "OpenOrCloseListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putBoolean("OpenOrClose", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.OPEN_OR_CLOSE, packet);
        }

        @Override
        public void PhonebookListListener(IntegerSSBoolean[] oldVal, IntegerSSBoolean[] newVal) {
            LogUtils.d(TAG, "PhonebookListListener newVal = " + (newVal == null ? "null" : newVal.length));

            if (newVal == null) {
                Packet packet = new Packet();
                packet.putParcelableArray("PhonebookList", newVal);
                Notify(BluetoothInterface.MAIN_TO_APK.PHONEBOOK_LIST, packet);
            } else {
                deviceListIndex = 0;
                int total = newVal.length / SEND_LEN;
                if (newVal.length % SEND_LEN != 0) {
                    total += 1;
                }

                LogUtils.d(TAG, "PhonebookListListener total = " + total);

                for (int i = 0; i < total; i++) {
                    deviceListIndex++;
                    Packet packet = new Packet();
                    packet.putInt("listInfoIndex", deviceListIndex);
                    packet.putInt("listInfoTotal", newVal.length);
                    if (i == (total - 1)) {
                        packet.putParcelableArray("PhonebookList", Arrays.copyOfRange(newVal, i * SEND_LEN, newVal.length));
                    } else {
                        packet.putParcelableArray("PhonebookList", Arrays.copyOfRange(newVal, i * SEND_LEN, (i * SEND_LEN) + SEND_LEN));
                    }
                    Notify(BluetoothInterface.MAIN_TO_APK.PHONEBOOK_LIST, packet);
                    SystemClock.sleep(30);
                }
            }
        }

        @Override
        public void PhonebookDownloadStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "PhonebookDownloadStatusListener, oldVal = " + BluetoothDefine.DownloadStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.DownloadStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("PhonebookDownloadStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.PHONEBOOK_DOWNLOAD_STATUS, packet);
        }

        @Override
        public void PhoneDownloadStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "PhoneDownloadStatusListener, oldVal = " + BluetoothDefine.DownloadStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.DownloadStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("PhoneDownloadStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.PHONE_DOWNLOAD_STATUS, packet);
        }

        @Override
        public void SIMDownloadStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "SIMDownloadStatusListener, oldVal = " + BluetoothDefine.DownloadStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.DownloadStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("SIMDownloadStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.SIM_DOWNLOAD_STATUS, packet);
        }

        @Override
        public void TalkListListener(IntegerSSS[] oldVal, IntegerSSS[] newVal) {
            LogUtils.d(TAG, "TalkListListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putParcelableArray("TalkList", getInfo().getParcelableArray("TalkList"));
            Notify(BluetoothInterface.MAIN_TO_APK.TALK_LIST, packet);
        }

        @Override
        public void DialTalkListDownloadStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "DialTalkListDownloadStatusListener, oldVal = " + BluetoothDefine.DownloadStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.DownloadStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("DialTalkListDownloadStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.DIAL_TALKLIST_DOWNLOAD_STATUS, packet);
        }

        @Override
        public void IncomingTalkListDownloadStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "IncomingTalkListDownloadStatusListener, oldVal = " + BluetoothDefine.DownloadStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.DownloadStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("IncommingTalkListDownloadStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.INCOMMING_TALKLIST_DOWNLOAD_STATUS, packet);
        }

        @Override
        public void MissedTalkListDownloadStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "MissedTalkListDownloadStatusListener, oldVal = " + BluetoothDefine.DownloadStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.DownloadStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("MissedTalkListDownloadStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.MISSED_TALKLIST_DOWNLOAD_STATUS, packet);
        }

        @Override
        public void TalkListDownloadStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "TalkListDownloadStatusListener, oldVal = " + BluetoothDefine.DownloadStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.DownloadStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("TalkListDownloadStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.TALKLIST_DOWNLOAD_STATUS, packet);
        }

        @Override
        public void SearchListListener(IntegerSS[] oldVal, IntegerSS[] newVal) {
            LogUtils.d(TAG, "SearchListListener, oldVal = " + oldVal + ", newVal = " + newVal);

            Packet packet = new Packet();
            packet.putParcelableArray("SearchList", getInfo().getParcelableArray("SearchList"));
            Notify(BluetoothInterface.MAIN_TO_APK.SEARCH_LIST, packet);
        }

        @Override
        public void SearchListStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "SearchListStatusListener, oldVal = " + BluetoothDefine.SearchStatus.toString(oldVal) + ", newVal = " + BluetoothDefine.SearchStatus.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("SearchStatus", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.SEARCH_STATUS, packet);
        }

        @Override
        public void AudioSoftListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "AudioSoftListener, oldVal = " + BluetoothDefine.AudioSoft.toString(oldVal) + ", newVal = " + BluetoothDefine.AudioSoft.toString(newVal));

            Packet packet = new Packet();
            packet.putInt("type", newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.AUDIO_SOFT, packet);
        }

        @Override
        public void ContactsNumberListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "ContactsNumberListener:" + newVal);
            Packet packet = new Packet();
            packet.putInt("ContactsNumber",newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.CONTACTS_NUMBER, packet);
        }

        @Override
        public void PairListListener(IntegerSS[] oldVal, IntegerSS[] newVal) {
            LogUtils.d(TAG, "PairListListener:" + newVal);
            Packet packet = new Packet();
            packet.putParcelableArray("PairList",newVal);
            Notify(BluetoothInterface.MAIN_TO_APK.PAIR_LIST, packet);
        }
    };

    @Override
    public Packet getInfo() {
        Packet packet = super.getInfo();
        if (null == packet) {
            packet = new Packet();
        }
        packet.putBoolean("SwitchToMusic", mSwitchToMusic);
        if (mSwitchToMusic) {
            mSwitchToMusic = false;
        }
        return packet;
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        //LogUtils.d(TAG, "dispatch, apk operate id = " + nId + ", packet = " + packet);
        switch (nId) {
            case BluetoothInterface.APK_TO_MAIN.START_DISCOVERY:
                Driver().clearSearchList();
                Driver().startDiscovery();
                break;
            case BluetoothInterface.APK_TO_MAIN.DISCONNECT_DEVICE:
                Driver().disconnect();
                break;
            case BluetoothInterface.APK_TO_MAIN.CONNECT_LAST_DEVICE:
                Driver().connectLast();
                break;
            case BluetoothInterface.APK_TO_MAIN.MUSIC_PLAY_PAUSE:
                if (isMusicPlay()) {
                    Driver().musicPause();
                } else {
                    Driver().musicPlay();
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.MUSIC_PREV:
                Driver().musicPrevious();
                break;
            case BluetoothInterface.APK_TO_MAIN.MUSIC_NEXT:
                Driver().musicNext();
                break;
            case BluetoothInterface.APK_TO_MAIN.CALL_OUT:
                if (null != packet) {
                    String strPhoneNumber = packet.getString("strPhoneNumber");
                    if (null != strPhoneNumber) {
                        if (TextUtils.isGraphic(strPhoneNumber)) {
                            Driver().phoneDail(strPhoneNumber);
                        }
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.CALL_HANGUP:
                Driver().phoneHangUp();
                break;
            case BluetoothInterface.APK_TO_MAIN.CALL_REDIAL:
                Notify(false, BluetoothInterface.MAIN_TO_APK.CALL_NUMBER, getInfo());
//                Driver().ReDail();
                break;
            case BluetoothInterface.APK_TO_MAIN.SEND_DTMF_CODE:
                if (null != packet) {
                    char chDTMFCode = packet.getChar("chDTMFCode");
                    if (0 != chDTMFCode) {
                        Driver().phoneTransmitDTMFCode(chDTMFCode);
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.DOWNLOAD_BOOK_LIST:
                Driver().phoneBookStartUpdate();
                break;
            case BluetoothInterface.APK_TO_MAIN.STOP_DISCOVERY:
                Driver().stopDiscovery();
                break;
            case BluetoothInterface.APK_TO_MAIN.CONNECT_HFP:
                if (null != packet) {
                    String strAddress = packet.getString("strAddress");
                    if (null != strAddress) {
                        if (!strAddress.isEmpty()) {
                            Driver().connectHFP(strAddress);
                        }
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.SET_AUTO_CONNECT:
                if (null != packet) {
                    boolean isAutoConnect = packet.getBoolean("isAutoConnect");
                    if (isAutoConnect) {
                        Driver().setAutoConnect();
                    } else {
                        Driver().cancelAutoConnect();
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.SET_AUTO_RESPONSE:
                if (null != packet) {
                    boolean isAutoResponse = packet.getBoolean("isAutoResponse");
                    LogUtils.d(TAG, "AudioSoft: " + isAutoResponse);
                    if (isAutoResponse) {
                        Driver().setAutoAccept();
                    } else {
                        Driver().cancelAutoAccept();
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.SET_LOCAL_NAME:
                if (null != packet) {
                    String strLocalName = packet.getString("strLocalName");
                    if (null != strLocalName) {
                        if (!strLocalName.isEmpty()) {
                            Driver().setLocalName(strLocalName);
                        }
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.SET_PIN_CODE:
                if (null != packet) {
                    String strPinCode = packet.getString("strPinCode");
                    if (null != strPinCode) {
                        if (!strPinCode.isEmpty()) {
                            Driver().setPinCode(strPinCode);
                        }
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.ANSWER:
                //huwei 17.12.11  根据控件按键需求修改接听键功能如下
                int hfpstatus = getHFPStatus();
                //来电时为接听功能
                if (BluetoothDefine.HFPStatus.isCallIn(hfpstatus)) {
                    Driver().phoneAnswer();
                } else {
                    if (!BluetoothDefine.HFPStatus.isCalling(hfpstatus)) {
                        //非电话状态且不在蓝牙模式,则为切到蓝牙模式
                        if (SourceManager.getCurSource() != source()) {
                            SourceManager.onChangeSource(source());
                        } else {
                            //非电话状态且在蓝牙模式,则为重拨作用
                            Driver().ReDail();
                        }

                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.BT_VOL_ADD:
                // host volume add.
                break;
            case BluetoothInterface.APK_TO_MAIN.BT_VOL_SUB:
                // host volume sub.
                break;
            case BluetoothInterface.APK_TO_MAIN.MIC_PHONE_ON:
                Driver().MicSwitch('1');
                break;
            case BluetoothInterface.APK_TO_MAIN.MIC_PHONE_OFF:
                Driver().MicSwitch('0');
                break;
            case BluetoothInterface.APK_TO_MAIN.SPEAKER_PHONE_ON:
                // host volume unmute.
                break;
            case BluetoothInterface.APK_TO_MAIN.SPEAKER_PHONE_OFF:
                // host volume mute.
                break;
            case BluetoothInterface.APK_TO_MAIN.DECLINE_CALL:
                Driver().phoneHangUp();
                break;
            case BluetoothInterface.APK_TO_MAIN.CLEAR_PHONE_BOOK:
                // clear phone book.
                Driver().clearPhoneBook();
                break;
            case BluetoothInterface.APK_TO_MAIN.CLEAR_TALK_LIST:
                // clear phone book.
                Driver().clearTalkList();
                break;
            case BluetoothInterface.APK_TO_MAIN.CLEAR_SEARCH_LIST:
                // clear phone book.
                if (null != packet) {
                    String macAddr = packet.getString("macAddr");
                    Driver().deletePairList(macAddr);
                    Driver().deletePair(macAddr);
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.CLEAR_CALLNUMBER:
                Driver().clearCallNumber();
                break;
            case BluetoothInterface.APK_TO_MAIN.SET_VOL_CHANNEL_PHONE:
                Driver().phoneTransfer();
                break;
            case BluetoothInterface.APK_TO_MAIN.SET_VOL_CHANNEL_MEDCHINE:
                Driver().phoneTransfer();
                break;
            case BluetoothInterface.APK_TO_MAIN.SET_INC_SEARCH:
                if (null != packet) {
                    boolean bIncSearch = packet.getBoolean("bIncSearch");
                    if (bIncSearch) {
                        Driver().TestMode('1');
                    } else {
                        Driver().TestMode('0');
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.MUSIC_STOP:
                Driver().musicStop();
                break;
            case BluetoothInterface.APK_TO_MAIN.MUSIC_PLAY:
                if (!isMusicPlay()) {
                    Driver().musicPlay();
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.BT_OPEN:
                Driver().OpenBt();
                break;
            case BluetoothInterface.APK_TO_MAIN.BT_CLOSE:
                if (packet != null) {
                    boolean carPlay = packet.getBoolean("carplay", false);
                    if (carPlay) {
                        Driver().CloseBt(BluetoothDefine.BluetoothClosedBy.NORMAL);
                        return ret;
                    }
                }
                Driver().CloseBt(BluetoothDefine.BluetoothClosedBy.USER);
                break;
            case BluetoothInterface.APK_TO_MAIN.SWITCH_TO_MUSIC:
                //避免蓝牙后台模式时切不进蓝牙的问题
                if (source() == SourceManager.getCurSource()) {
                    // 加个消息，让APK切到音乐子界面
                    Notify(false, BluetoothInterface.MAIN_TO_APK.SWITCH_TO_MUSIC, null);
                } else {
                    mSwitchToMusic = true;
                    SourceManager.onChangeSource(source());
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.MULTI_FUNCTION_KEY:
                Packet packet1 = getInfo();
                if (null != packet1) {
                    int hfp = packet1.getInt("HFPStatus", BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT);
                    if (BluetoothDefine.HFPStatus.isIdle(hfp)) {
                        if (SourceManager.getCurBackSource() == Define.Source.SOURCE_BLUETOOTH) {
                            String strPhoneNumber = packet1.getString("CallNumber");
                            if (null != strPhoneNumber) {
                                if (TextUtils.isGraphic(strPhoneNumber)) {
                                    Notify(false, BluetoothInterface.MAIN_TO_APK.CALL_NUMBER, packet1);
                                    Driver().phoneDail(strPhoneNumber);
                                    break;
                                }
                            }
                        }
                        SourceManager.onChangeSource(source());
                    } else if (BluetoothDefine.HFPStatus.isCallIn(hfp)) {
                        Driver().phoneAnswer();
                    } else if (BluetoothDefine.HFPStatus.isCalling(hfp)) {
                        Driver().phoneHangUp();
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.AUDIO_SOFT:
                if (null != packet) {
                    int type = packet.getInt("type", -1);
                    if (0 == type) {
                        Driver().setAudioSoft('0');
                    } else if (1 == type) {
                        Driver().setAudioSoft('1');
                    }
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.DELETE_CONTACT_ITEM:
                if (packet != null) {
                    Driver().deleteContactItem(packet.getString("Number"));
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.ADD_TO_FAVORITE:
                if (packet != null) {
                    Driver().addToFavorite(packet.getString("Number"));
                }
                break;
            case BluetoothInterface.APK_TO_MAIN.REMOVE_FROM_FAVORITE:
                if (packet != null) {
                    Driver().removeFromFavorite(packet.getString("Number"));
                }
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;
        switch (key) {
            case Define.Key.KEY_CH_INC:
            case Define.Key.KEY_NEXT:
            case Define.Key.KEY_FF:
            case Define.Key.KEY_SCAN_INC:
            case Define.Key.KEY_DIRECT_RIGHT:
                Driver().musicNext();
                break;
            case Define.Key.KEY_CH_DEC:
            case Define.Key.KEY_PREV:
            case Define.Key.KEY_FB:
            case Define.Key.KEY_SCAN_DEC:
            case Define.Key.KEY_DIRECT_LEFT:
                Driver().musicPrevious();
                break;
            case Define.Key.KEY_PLAY:
                Driver().musicPlay();
                break;
            case Define.Key.KEY_PAUSE:
                BaseAudioDriver.pauseByVoice(true);
                Driver().musicPause();
                break;
            case Define.Key.KEY_PLAYPAUSE:
                if (isMusicPlay()) {
                    Driver().musicPause();
                } else {
                    Driver().musicPlay();
                }
                break;
            case Define.Key.KEY_PICKUP:
                //接听键功能走commonlogic统一处理
                ret = false;
//                Driver().phoneAnswer();
                break;
            case Define.Key.KEY_HANGUP:
                Driver().phoneHangUp();
                break;
            case Define.Key.KEY_NUM_0:
                Driver().phoneTransmitDTMFCode('0');
                break;
            case Define.Key.KEY_NUM_1:
                Driver().phoneTransmitDTMFCode('1');
                break;
            case Define.Key.KEY_NUM_2:
                Driver().phoneTransmitDTMFCode('2');
                break;
            case Define.Key.KEY_NUM_3:
                Driver().phoneTransmitDTMFCode('3');
                break;
            case Define.Key.KEY_NUM_4:
                Driver().phoneTransmitDTMFCode('4');
                break;
            case Define.Key.KEY_NUM_5:
                Driver().phoneTransmitDTMFCode('5');
                break;
            case Define.Key.KEY_NUM_6:
                Driver().phoneTransmitDTMFCode('6');
                break;
            case Define.Key.KEY_NUM_7:
                Driver().phoneTransmitDTMFCode('7');
                break;
            case Define.Key.KEY_NUM_8:
                Driver().phoneTransmitDTMFCode('8');
                break;
            case Define.Key.KEY_NUM_9:
                Driver().phoneTransmitDTMFCode('9');
                break;
            case Define.Key.KEY_NUM_XING:
                Driver().phoneTransmitDTMFCode('*');
                break;
            case Define.Key.KEY_NUM_JING:
                Driver().phoneTransmitDTMFCode('#');
                break;
            case Define.Key.KEY_POWER:
            case Define.Key.KEY_LONG_POWEROFF:
                final int hfp = getHFPStatus();
                if (!BluetoothDefine.HFPStatus.isCalling(hfp)) {
                    ret = false;
                }
                break;
            case Define.Key.KEY_BT_DISCONNECT:
                Driver().disconnect();
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }

    /**
     * 打开蓝牙悬浮框
     */
    private void openFloatWindow() {
        ApkUtils.startServiceSafety(getMainContext(), BluetoothDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME,
                BluetoothDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME, BluetoothDefine.FLOAT_WINDOW_SERVICE_NAME);
    }

    /**
     * 关闭蓝牙悬浮框
     */
    private void closeFloatWindow() {
        ApkUtils.stopServiceSafety(getMainContext(), BluetoothDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME,
                BluetoothDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME, BluetoothDefine.FLOAT_WINDOW_SERVICE_NAME);
    }

    /**
     * 蓝牙音乐是否在播放
     */
    protected boolean isMusicPlay() {
        boolean ret = false;
        Packet packet = getModel().getInfo();
        if (null != packet) {
            ret = BluetoothDefine.MusicStatus.isPlay(packet.getInt("MusicStatus"));
        }
        return ret;
    }

    /**
     * 获取蓝牙HFP状态
     */
    protected int getHFPStatus() {
        int ret = BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT;
        Packet packet = getModel().getInfo();
        if (null != packet) {
            ret = packet.getInt("HFPStatus", BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT);
        }
        return ret;
    }

    /**
     * 获取蓝牙开关状态
     */
    protected boolean getOpenOrClose() {
        boolean ret = false;
        Packet packet = getModel().getInfo();
        if (null != packet) {
            ret = packet.getBoolean("OpenOrClose");
        }
        return ret;
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context paramContext, Intent paramIntent) {
            String action = paramIntent.getAction();
            if(action.equals("com.wwc2.bluetooth.open")){
                Driver().OpenBt();
            }else if(action.equals("com.wwc2.bluetooth.close")){
                Driver().CloseBt(BluetoothDefine.BluetoothClosedBy.USER);
            }
        }
    };

    private BroadcastReceiver ecarBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            LogUtils.e(TAG, intent.getAction());
            switch (intent.getAction()) {
                case EcarHelper.ACTION_ECAR_SEND:
                    String cmd = intent.getStringExtra(EcarHelper._CMD_);
                    String type = intent.getStringExtra(EcarHelper._TYPE_);
                    String key = intent.getStringExtra(EcarHelper._KEYS_);
                    EcarHelper.CMDFormEcar cmdFormEcar = null;
                    try {
                        cmdFormEcar = EcarHelper.CMDFormEcar.valueOf(cmd);
                    } catch (IllegalArgumentException e) {
                        LogUtils.w(TAG, e.getMessage());
                    }
                    if (cmdFormEcar == null) {
                        LogUtils.e(TAG, "cmd unhandled:" + cmd);
                        return;
                    }
                    switch (cmdFormEcar) {
                        case BluetoothQueryState:
                            int hfpstatus = getHFPStatus();
                            String state;
                            if (!getOpenOrClose()) {
                                state = String.valueOf(EcarHelper.BT_OFF);
                            } else {
                                if (BluetoothDefine.HFPStatus.isDisConnect(hfpstatus)) {
                                    state = String.valueOf(EcarHelper.BT_DISCONNECT);
                                } else {
                                    state = String.valueOf(EcarHelper.BT_CONNECTED);
                                }
                            }
                            EcarHelper.sendMsgToEcar(getMainContext(), EcarHelper.CMDToEcar.BluetoothState.name(), state);
                            break;
                        case BluetoothConnect:
                            SourceManager.onChangeSource(Define.Source.SOURCE_BLUETOOTH);
                            break;
                        case BluetoothMakeCall:
                            String name = intent.getStringExtra("name");
                            String number = intent.getStringExtra("number");
                            LogUtils.i(TAG, "ecar name:" + name + "\tecar number:" + number);
                            if (number != null) {
                                if (TextUtils.isGraphic(number)) {
                                    Driver().phoneDail(number);
                                }
                            }
                            break;
                        case HideCallUI:
                            String value = intent.getStringExtra(key);
                            if ("show".equals(value)) {
                                LogUtils.d(TAG, "show CallUI");
                                EcarHelper.setEcarPhoneState(false);
                                if (mHideByEcar) {//解决：翼云和手机同时拨打电话，界面在翼云通话，当结束翼云电话后，手机通话还在继续，操作退出和主页图标无作用。
                                    mHideByEcar = false;
                                    Packet packet = getModel().getInfo();
                                    if (null != packet) {
                                        final int status = packet.getInt("HFPStatus", -1);
                                        if (-1 != status) {
                                            if (BluetoothDefine.HFPStatus.isCalling(status)) {
                                                if (packet.getBoolean("VoiceInPhone")) {
                                                    Driver().phoneTransfer();
                                                    LogUtils.d(TAG, "VoiceInPhone false");
                                                } else {
                                                    LogUtils.d(TAG, "VoiceInPhone true");
                                                }

                                                // 在蓝牙界面，隐藏通话悬浮框；不在蓝牙界面，显示通话悬浮框
                                                boolean open = true;
                                                BaseLogic logic = ModuleManager.getFrontLogic();
                                                if (null != logic) {
                                                    open = !logic.isHFPFloatHideSource();
                                                }
                                                if (open) {
                                                    openFloatWindow();
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if ("hide".equals(value)) {
                                LogUtils.d(TAG, "hide CallUI");
                                EcarHelper.setEcarPhoneState(true);
                            }
                            break;
                        case BluetoothEndCall:
                            Driver().phoneHangUp();
                            break;
                    }
                    break;
                case EcarHelper.ACTION_CALL_IDLE:
                    LogUtils.d(TAG, "ecar call idle");
                    if (mHideByEcar) {//解决：翼云和手机同时拨打电话，界面在翼云通话，当结束翼云电话后，手机通话还在继续，操作退出和主页图标无作用。
                        mHideByEcar = false;
                        Packet packet = getModel().getInfo();
                        if (null != packet) {
                            final int status = packet.getInt("HFPStatus", -1);
                            if (-1 != status) {
                                if (BluetoothDefine.HFPStatus.isCalling(status)) {
                                    if (packet.getBoolean("VoiceInPhone")) {
                                        Driver().phoneTransfer();
                                        LogUtils.d(TAG, "VoiceInPhone false");
                                    } else {
                                        LogUtils.d(TAG, "VoiceInPhone true");
                                    }

                                    // 在蓝牙界面，隐藏通话悬浮框；不在蓝牙界面，显示通话悬浮框
                                    boolean open = true;
                                    BaseLogic logic = ModuleManager.getFrontLogic();
                                    if (null != logic) {
                                        open = !logic.isHFPFloatHideSource();
                                    }
                                    if (open) {
                                        openFloatWindow();
                                    }
                                }
                            }
                        }
                    }
                    break;
                case EcarHelper.ACTION_CALL_INCOMING:
                    LogUtils.d(TAG, "ecar call incoming");
                    break;
                case EcarHelper.ACTION_CALL_OFFHOOK:
                    LogUtils.d(TAG, "ecar call offhook");
                    break;
                case EcarHelper.E_CAR_OCCUPY_SCREEN:
                    int registState = intent.getIntExtra("registState", 0);
                    LogUtils.d(TAG, "E_CAR_OCCUPY_SCREEN   state="+registState);
                    EcarHelper.setEcarRegistState(registState==1);
                    break;
                /*-begin-20180511-ydinggen-add-增加SIM卡认证失败，禁掉按键功能-*/
                case EcarHelper.ACTION_OCCUPY_SCREEN:
                    LogUtils.d(TAG, "wwc2_sim_auth_fail");
                    EcarHelper.setEcarRegistState(false);
                    break;
                /*-end-20180511-ydinggen-add-增加SIM卡认证失败，禁掉按键功能-*/
                //处理一键上网投屏声音问题。2019-01-18
                case PhonelinkLogic.ACTION_EC_A2DP_ACQUIRE:
                    SourceManager.onOpenBackgroundSource(Define.Source.SOURCE_SILENT);
                    Driver().musicUnmute();
                    AudioDriver.Driver().request(null,
                            AudioDefine.AudioStream.STREAM_MUSIC,
                            AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
                    PhonelinkLogic.requestAudioFocus(true);
                    break;
                case PhonelinkLogic.ACTION_EC_A2DP_RELEASE:
                    if (SourceManager.getCurBackSource() != Define.Source.SOURCE_BLUETOOTH &&
                            SourceManager.getCurBackSource() != Define.Source.SOURCE_PHONELINK) {
                        Driver().musicMute();
                    }
                    PhonelinkLogic.requestAudioFocus(false);
                    break;
            }

        }
    };

    private Pattern pattern = Pattern.compile("\\d+");

    private String pickNum(String str) {
        Matcher matcher = pattern.matcher(str);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            stringBuffer.append(matcher.group());
        }
        return stringBuffer.toString();
    }

    String m_callNumber = null;
    long m_callTime = 0;
    int mHfpStatus = 0;
    private void sendCallInfoToMcu() {
        int length = 3;
        if (m_callNumber != null) {
            length += m_callNumber.length();
        }

        byte[] data = new byte[length + 1];
        data[0] = (byte) getMcuHfpStatus();
        data[1] = (byte) (m_callTime / 60);
        data[2] = (byte) (m_callTime % 60);
        if (null != m_callNumber) {
            byte[] number = m_callNumber.getBytes();
            for (int i = 0;i < number.length;i++) {
                data[3 + i] = (byte) (number[i]&0xFF);
            }
        }
        data[length] = 0;

//        LogUtils.d("sendCallInfoToMcu----time=" + m_callTime + ", number=" + m_callNumber +
//                ", data=" + FormatData.formatHexBufToString(data, data.length));
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_BTCALL_INFO, data, data.length);
    }

    private int getMcuHfpStatus() {
        int ret = 0;
        switch (mHfpStatus) {
            case BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT:
                ret = 1;
                break;
            case BluetoothDefine.HFPStatus.HFP_STATUS_OUT_CALLING:
                ret = 3;
                break;
            case BluetoothDefine.HFPStatus.HFP_STATUS_IN_CALLING:
                ret = 2;
                break;
            case BluetoothDefine.HFPStatus.HFP_STATUS_TALKING:
                ret = 4;
                break;
        }
        return ret;
    }
}
