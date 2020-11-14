package com.wwc2.main.voice_assistant;

import android.content.Intent;
import android.text.TextUtils;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.audio_interface.AudioDefine;
import com.wwc2.audio_interface.AudioInterface;
import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.camera_interface.CameraDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PanoramicManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.manager.TBoxDataManager;
import com.wwc2.main.manager.VolumeManager;
import com.wwc2.main.navi.NaviListener;
import com.wwc2.main.poweroff.PoweroffListener;
import com.wwc2.main.voice_assistant.driver.VoiceAssistantDriver;
import com.wwc2.main.voice_assistant.driver.VoiceAssistantDriverable;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.poweroff_interface.PoweroffDefine;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

/**
 * Created by huwei on 2017/1/28.
 */
public class VoiceAssistantLogic extends BaseLogic {
    private final String TAG = VoiceAssistantLogic.class.getSimpleName();
    BaseLogic accoffLogic, poweroffLogic, cameraLogic, audioLogic, radioLogic, bluetoothLogic, auxLogic, naviLogic;
    private boolean isCamera = false; //def false
    private boolean isPower = true; //语音apk中true为显示

    @Override
    public String getTypeName() {
        return "voice_assistant";
    }

    @Override
    public String getMessageType() {
        return VoiceAssistantDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.voice_assistant";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_VOICE_ASSISTANT;
    }

    private VoiceAssistantListener mVoiceAssistantListener = new VoiceAssistantListener() {
        @Override
        public void ShowRecordButtonListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "ShowRecordButtonListener: newVal:" + newVal);
            if (newVal) {
                Notify(false, VoiceAssistantInterface.MainToApk.KEY_SHOW_BUTTON, null);
            } else {
                Notify(false, VoiceAssistantInterface.MainToApk.KEY_HIDE_BUTTON, null);
            }
        }

        @Override
        public void AudioTypeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "AudioTypeListener: newVal:" + com.wwc2.voiceassistant_interface.VoiceAssistantDefine.AudioType.toString(newVal));
            Driver().memorySave();
            Packet packet = new Packet();
            packet.putInt("AudioType", newVal);
            Notify(false, VoiceAssistantInterface.MainToApk.CHOOSE_AUDIO_TYPE, packet);
            //txz
//            BaseDriver driver = getDriver();
//            if (null != driver) {
//                driver.onDestroy();
//                Packet packet1 = new Packet();
//                packet1.putObject("context", getMainContext());
//                driver.onCreate(packet1);
//            }
        }

        @Override
        public void EnableWakeupListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("EnableWakeup", newVal);
            Notify(false, VoiceAssistantInterface.MainToApk.ENABLE_WAKEUP, packet);
        }

        @Override
        public void EnableWholeCmdLstener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("EnableWholeCmd", newVal);
            Notify(false, VoiceAssistantInterface.MainToApk.ENABLE_WHOLE_CMD, packet);
        }
    };

    @Override
    public BaseDriver newDriver() {
        return new VoiceAssistantDriver();
    }

    /**
     * the driver interface.
     */
    protected VoiceAssistantDriverable Driver() {
        VoiceAssistantDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof VoiceAssistantDriverable) {
            ret = (VoiceAssistantDriverable) drive;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        // logic create.
        super.onCreate(packet);

        // create driver.
        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }
        getModel().bindListener(mVoiceAssistantListener);
        accoffLogic = ModuleManager.getLogicByName(AccoffDefine.MODULE);
        bluetoothLogic = ModuleManager.getLogicByName(BluetoothDefine.MODULE);
        auxLogic = ModuleManager.getLogicByName(AuxDefine.MODULE);
        naviLogic = ModuleManager.getLogicByName(NaviDefine.MODULE);
        if (accoffLogic != null) {
            accoffLogic.getModel().bindListener(mAccoffListener);
        }
        poweroffLogic = ModuleManager.getLogicByName(PoweroffDefine.MODULE);

        if (poweroffLogic != null) {
            poweroffLogic.getModel().bindListener(mPoweroffListener);
        }
        if (naviLogic != null) {
            naviLogic.getModel().bindListener(mNaviListener);
        }
        audioLogic = ModuleManager.getLogicByName(AudioDefine.MODULE);
        radioLogic = ModuleManager.getLogicByName(RadioDefine.MODULE);
        SourceManager.getModel().bindListener(mSourceListener);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        // destroy driver.
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }
        // logic destroy.
        super.onDestroy();
        getModel().unbindListener(mVoiceAssistantListener);
        if (accoffLogic != null) {
            accoffLogic.getModel().unbindListener(mAccoffListener);
        }
        if (poweroffLogic != null) {
            poweroffLogic.getModel().unbindListener(mPoweroffListener);
        }
        if (naviLogic != null) {
            naviLogic.getModel().unbindListener(mNaviListener);
        }
        SourceManager.getModel().unbindListener(mSourceListener);
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        LogUtils.d(TAG, "dispatch:" + nId);
        switch (nId) {
            case VoiceAssistantInterface.ApkToMain.KEY_TRIGGER_VOICE:
                Packet packet1 = new Packet();
                packet1.putBoolean("open",packet.getBoolean("open"));
                Notify(VoiceAssistantInterface.MainToApk.KEY_TRIGGER_VOICE, packet1);
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_SHOW_BUTTON:
                Notify(VoiceAssistantInterface.MainToApk.KEY_SHOW_BUTTON, null);
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_HIDE_BUTTON:
                Notify(VoiceAssistantInterface.MainToApk.KEY_HIDE_BUTTON, null);
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_OPEN_NAVI:
                Driver().openNavi();
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_EXIT_NAVI:
                Driver().exitNavi();
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_CLOSE_PKGNAME:
                String ePkgName = packet.getString("pkgName");
                if (ePkgName.equals("com.wwc2.dvr")) {
                    getMainContext().sendBroadcast(new Intent("com.wwc2.dvr.exit.broadcast"));
                } else {
                    SourceManager.onExitPackage(ePkgName);
                }
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_OPEN_PKGNAME:
                String oPkgName = packet.getString("pkgName");
                LogUtils.d(TAG, "open pkgName:" + oPkgName);
                SourceManager.runApk(oPkgName, null, null, false);
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_CHANGE_SOURCE:
                int source = packet.getInt("source");
                LogUtils.d(TAG, " source:" + source);
                if (source != Define.Source.SOURCE_NONE) {
                    SourceManager.onChangeSource(source);
                }
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_VOLUME_MUTE:
                Boolean mute = packet.getBoolean("mute");
                LogUtils.d(TAG, " mute:" + mute);
                VolumeDriver.Driver().mute(mute);
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_RECORD_SHOW:
                Driver().recordShow();
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_RECORD_DISMISS:
                Driver().recordDismiss();
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_AUDIO_TITLE:
                ModuleManager.getLogicByName(AudioDefine.MODULE).dispatch(AudioInterface.APK_TO_MAIN.ENTER_TITLE, packet);
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_AUDIO_ARTIST:
                ModuleManager.getLogicByName(AudioDefine.MODULE).dispatch(AudioInterface.APK_TO_MAIN.ENTER_ARTIST, packet);
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_AUDIO_ALBUM:
                ModuleManager.getLogicByName(AudioDefine.MODULE).dispatch(AudioInterface.APK_TO_MAIN.ENTER_ALBUM, packet);
                break;
            case VoiceAssistantInterface.ApkToMain.HIDE_SHOW_RECORD_BUTTON:
                Boolean isShowButton = packet.getBoolean("isShowButton");
                LogUtils.d(TAG, "choose system audio! isShowButton:" + isShowButton);
                Driver().showRecordButton(isShowButton);
                break;
            case VoiceAssistantInterface.ApkToMain.CHOOSE_SYSTEM_AUDIO:
                int warnInfo = packet.getInt("sound_info", -1);
                if (warnInfo != -1) {
                    Notify(false, VoiceAssistantInterface.MainToApk.CHOOSE_AUDIO_TYPE, packet);
                } else {
                    int audioType = packet.getInt("audioType");
                    LogUtils.d(TAG, "choose system audio! isChooseAudio:" + com.wwc2.voiceassistant_interface.VoiceAssistantDefine.AudioType.toString(audioType));
                    Driver().chooseAudio(audioType);
                }
                break;
            case VoiceAssistantInterface.ApkToMain.ENABLE_WAKEUP:
                Boolean isEnableWakeup = packet.getBoolean("isEnableWakeup");
                LogUtils.d(TAG, " isEnableWakeup:" + isEnableWakeup);
                Driver().setEnableWakeup(isEnableWakeup);
                break;
            case VoiceAssistantInterface.ApkToMain.FILTER_NOISE_TYPE:
                Boolean filterNoiseType = packet.getBoolean("FilterNoiseType");
                LogUtils.d(TAG, " filterNoiseType:" + filterNoiseType);
                Driver().setFilterNoiseType(filterNoiseType);
                break;
            case VoiceAssistantInterface.ApkToMain.VOICEASSISTANT_SETTING:
                String name = packet.getString(VoiceAssistantDefine.Common.SWITCH);
                if (!TextUtils.isEmpty(name)) {
                    try {
                        VoiceAssistantDefine.Common.Switch sw = VoiceAssistantDefine.Common.Switch.valueOf(name);
                        switch (sw) {
                            case SHOW_RECORD_BUTTON:
                                Driver().showRecordButton(packet.getBoolean(sw.value()));
                                LogUtils.d(TAG, " showRecordButton:" + packet.getBoolean(sw.value()));
                                break;
                            case ENABLE_WAKEUP:
                                Driver().setEnableWakeup(packet.getBoolean(sw.value()));
                                LogUtils.d(TAG, " setEnableWakeup:" + packet.getBoolean(sw.value()));
                                break;
                            case FILTER_NOISE_TYPE:
                                Driver().setFilterNoiseType(packet.getBoolean(sw.value()));
                                LogUtils.d(TAG, " setFilterNoiseType:" + packet.getBoolean(sw.value()));
                                break;
                            case ENABLE_WHOLE_CMD:
                                Driver().setEnableWholeCmd(packet.getBoolean(sw.value()));
                                LogUtils.d(TAG, " setEnableWholeCmd:" + packet.getBoolean(sw.value()));
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        LogUtils.d(TAG, "");
                    }
                }
                break;
            case VoiceAssistantInterface.ApkToMain.KEY_KWS:
                LogUtils.d(TAG, "---512---init packet---");
                Packet packet2 = getInfo();
                packet2.putBoolean("VOICE_INIT", true);
                Notify(VoiceAssistantInterface.MainToApk.KEY_SHOW_BUTTON, packet2);

                String[] kws = packet.getStringArray("kws");
                if (kws != null) {
                    if (kws.length > 0) {
                        LogUtils.d(TAG, "Set the wake up word!");
                        Driver().setWakeupKeywords(kws);
                    }
                }
                break;
            case VoiceAssistantInterface.ApkToMain.ENABLE_WHOLE_CMD:
                Boolean isEnableWholeCmd = packet.getBoolean("isEnableWholeCmd");
                LogUtils.d(TAG, " isEnableWholeCmd:" + isEnableWholeCmd);
                Driver().setEnableWholeCmd(isEnableWholeCmd);
                break;
            case VoiceAssistantInterface.ApkToMain.PANORAMIC_CMD:
                int cmd = packet.getInt("panoramic_cmd", -1);
                if (cmd == -1) {
                    cmd = packet.getInt("voice_car", -1);
                    if (cmd != -1) {
                        TBoxDataManager.getTBoxManager().sendDataToTBox(false, (byte) cmd, null);//暂不处理重发
                    }
                } else {
                    PanoramicManager.getInstance().sendCMDToPanoramic(cmd, true);
                }
                break;
        }
        return ret;
    }

    @Override
    public Packet getInfo() {
        Packet mPacket = super.getInfo();
        if (mPacket == null) {
            mPacket = new Packet();
        }
        mPacket.putBoolean("Camera", EventInputManager.getCamera());
        CoreLogic logic = LogicManager.getLogicByName(VoiceAssistantDefine.MODULE);
        if (logic != null && logic.getModel() != null) {
            Packet voice = logic.getModel().getInfo();
            if (null != voice) {
                mPacket.putBoolean("EnableWakeup", voice.getBoolean("EnableWakeup"));
                mPacket.putBoolean("EnableWholeCmd", voice.getBoolean("EnableWholeCmd"));
            }
        }
        mPacket.putInt("BackSource", SourceManager.getCurBackSource());
        mPacket.putInt("CurrentSource", SourceManager.getCurSource());
        mPacket.putInt("MaxVolume", VolumeManager.getMax());
        mPacket.putInt("VolumeValue", VolumeManager.getValue());
        mPacket.putBoolean("VolumeMute", VolumeManager.getMute());
        if (audioLogic != null) {
            mPacket.putString("PkgAudio", audioLogic.getAPKPacketName());
        }
        if (radioLogic != null) {
            mPacket.putString("PkgRadio", radioLogic.getAPKPacketName());
        }
        if (bluetoothLogic != null) {
            mPacket.putString("PkgBluetooth", bluetoothLogic.getAPKPacketName());
        }
        if (auxLogic != null) {
            mPacket.putString("PkgAux", auxLogic.getAPKPacketName());
        }
        if (naviLogic != null) {
            mPacket.putString(com.wwc2.main.navi.NaviDefine.SELECTION, naviLogic.getInfo().getString(com.wwc2.main.navi.NaviDefine.SELECTION));
        }
        mPacket.putBoolean("power", isPower);
        return mPacket;
    }

    /**
     * 倒车监听器
     */
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            cameraLogic = ModuleManager.getLogicByName(CameraDefine.MODULE);
            if (cameraLogic != null) {
                if (cameraLogic.source() == newVal) {
                    isCamera = true;
                    packet.putBoolean("reverse", true);
                    Notify(VoiceAssistantInterface.MainToApk.KEY_REVERSE_STATE, packet);//倒车也使用开关机接口
                    LogUtils.d(TAG, " SourceListener voice is in reverse!");
                } else if (cameraLogic.source() == oldVal) {
                    isCamera = false;
                    packet.putBoolean("reverse", false);
                    Notify(VoiceAssistantInterface.MainToApk.KEY_REVERSE_STATE, packet);
                    LogUtils.d(TAG, " SourceListener voice out of reverse!");
                }
            }
            LogUtils.d(TAG, "CurSourceListener " + " oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            //开机acc处理

//            if (Define.Source.SOURCE_POWEROFF == newVal || Define.Source.SOURCE_ACCOFF == newVal) {
////                mCallToolHandler.configControl(true);
//            } else if (Define.Source.SOURCE_POWEROFF == oldVal || Define.Source.SOURCE_ACCOFF == oldVal) {
//
//            }
            BaseLogic curBassLogic = ModuleManager.getLogicBySource(newVal);
            if (curBassLogic != null) {
                if (Define.Source.SOURCE_RADIO == newVal || Define.Source.SOURCE_POWEROFF == newVal ||
                        curBassLogic.isSource()) {//切源退出语音（卡仕达）
                    Packet packet1 = new Packet();
                    packet1.putBoolean("open", false);
                    Notify(false, VoiceAssistantInterface.MainToApk.KEY_TRIGGER_VOICE, packet1);
                }
            }
        }

        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt("CurBackSource", newVal);
            Notify(VoiceAssistantInterface.MainToApk.CUR_BACK_SOURCE, packet);
        }
    };

    /**
     * 休眠唤醒
     */
    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            if (AccoffStep.isLightSleep(oldVal) && !AccoffStep.isAccoff(newVal)) {
                packet.putBoolean("acc", true);
                Notify(VoiceAssistantInterface.MainToApk.KEY_ACC_STATE, packet);
                LogUtils.d(TAG, "acc on from light sleep");
            } else if (!AccoffStep.isAccoff(oldVal) && AccoffStep.isLightSleep(newVal)) {
                packet.putBoolean("acc", false);
                Notify(VoiceAssistantInterface.MainToApk.KEY_ACC_STATE, packet);
                LogUtils.d(TAG, " Acc off into light sleep!");
            } else if (AccoffStep.isDeepSleep(newVal) && AccoffStep.isLightSleep(oldVal)) {
                packet.putBoolean("deepsleep", true);
                Notify(VoiceAssistantInterface.MainToApk.KEY_DEEPSLEEP_STATE, packet);
                LogUtils.d(TAG, " will goto deep sleep!");
            } else if (AccoffStep.isDeepSleep(oldVal) && !AccoffStep.isAccoff(newVal)) {
                packet.putBoolean("deepsleep", false);
                Notify(VoiceAssistantInterface.MainToApk.KEY_DEEPSLEEP_STATE, packet);
                LogUtils.d(TAG, " acc on from deep sleep!");
            }
        }
    };

    /**
     * 开关机监听器
     */
    private PoweroffListener mPoweroffListener = new PoweroffListener() {
        Packet packet = new Packet();

        @Override
        public void PowerOffListener(Boolean oldVal, Boolean newVal) {
            isPower = !newVal;
            if(isCamera)  //仅限语音
                return;
            if (newVal) {
                packet.putBoolean("power", false);
                Notify(VoiceAssistantInterface.MainToApk.KEY_POWER_STATE, packet);
                LogUtils.d(TAG, " Poweroff is voice sleep!");
            } else {
                LogUtils.d(TAG, " Poweroff re voice sleep! logo=" + McuManager.getLogoStatus());
                if (!McuManager.getLogoStatus()) {
                    packet.putBoolean("power", true);
                    Notify(VoiceAssistantInterface.MainToApk.KEY_POWER_STATE, packet);
                    // 开机
                    LogUtils.d(TAG, " Poweroff Re initialization of the Voice!");
                } else {
                    LogUtils.e(TAG, "Poweroff uninitialization of the Voice LOGO!");
                }
            }
        }
    };

    private NaviListener mNaviListener = new NaviListener() {
        @Override
        public void NaviPacketNameListener(String oldVal, String newVal) {
            Packet packet = new Packet();
            packet.putString(com.wwc2.main.navi.NaviDefine.SELECTION, newVal);
            Notify(VoiceAssistantInterface.MainToApk.KEY_NAVI_PACKETNAME, packet);
        }
    };
}
