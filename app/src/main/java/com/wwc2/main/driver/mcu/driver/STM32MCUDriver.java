package com.wwc2.main.driver.mcu.driver;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.custom.IntegerInteger;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.jni.mcu_serial.McuSerialNative;
import com.wwc2.main.MainService;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.bluetooth.EcarHelper;
import com.wwc2.main.camera.CameraLogic;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.audio.AudioListener;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.client.driver.BaseClientDriver;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.driver.mcu.McuStatusDefine;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.eventinput.EventInputListener;
import com.wwc2.main.eventinput.driver.BaseEventInputDriver;
import com.wwc2.main.eventinput.driver.STM32EventInputDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.CPUThermalManager;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.manager.TBoxDataManager;
import com.wwc2.main.manager.VolumeManager;
import com.wwc2.main.media.driver.android.AndroidMediaDriver;
import com.wwc2.main.settings.SettingsLogic;
import com.wwc2.poweroff_interface.PoweroffDefine;
import com.wwc2.settings_interface.SettingsDefine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the stm32 mcu driver.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public class STM32MCUDriver extends BaseMcuDriver {

    /**TAG*/
    private static final String TAG = "STM32MCUDriver";

    /**need mcu memory source.*/
    private static final boolean MEMORY_SOURCE = true;//2018-01-08

    /**open*/
    private boolean mOpen = false;

    /**MCU记忆source*/
    private int mMemorySource = Define.Source.SOURCE_INVALID;

    /**polling data timer id.*/
    private int mPollingTimerId = 0;

    private boolean mIsCalling              = false;
//    public static boolean mIsInLogo = false;

   // private int mCpuLoadCnt = 0;

    private static int KILL_PRO_NUM = 3;

    //
    private int mCpuTempThermalCnt = 0;

    //
    private int mcuWatchDogCnt = 0;

    private static final  int OUT_DOG_MAGIC =31415;

   //begin zhongyang.hu add for AccOff check. 201804026
    private static final int ACC_ON =0;
    private static final int ACC_OFF =1;
    public static final String ACC_ONOFF_STATE = "/sys/devices/virtual/switch/acc_signal/state";
    //end

    /**不用杀死的包名过滤列表*/
    protected  String[] mKeepProcessPackageFilter = {
            "com.wwc2.main",
            "com.wwc2.systempermission",
            "com.wwc2.camera",
            "com.wwc2.mainui",
            "com.wwc2.launcher",
            "com.wwc2.voice_assistant",
            "com.android.externalstorage",
            "com.mediatek.mtklogger",
            "com.goodocom.gocsdk",
            "com.aispeech.aios.adapter",
            "com.aispeech.aios",
            "com.wwc2.voice_assistant",
            "com.android.providers.telephony",
            "com.android.stk",
            "com.android.phone",
            "com.mediatek.flp.em",
            "android",
            "com.android.systemui",
            "com.android.providers.settings",
            "com.android.server.telecom",
            "com.android.location.fused",
            "com.wwc2.launcher",
            "com.wwc2.canbussdk",
            "com.android.providers.applications",
            "com.android.providers.userdictionary",
            "com.android.providers.media",
            "com.wwc2.canbusapk",
            "com.wwc2.audio",
            "com.wwc2.radio_apk",
            "com.autonavi.amapauto",
            "com.wwc2.tpmservice",
            "com.wwc2.bluetooth",
            "com.wwc2.video",
            "com.android.providers.downloads",
            "com.android.providers.contacts",
            /**在上ACC时马上关机，快速倒车，退出倒车会出现按其他键没作用。*/
            "com.wwc2.poweroff",
//            "com.baony.avm360"  //灵动飞扬360
    };

    /**开机监听器*/
    PowerManager.PowerListener mPoweronListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            if (PowerManager.PowerStep.isPoweronMcuCreated(newVal)) {
                // 发送开机命令
                McuManager.sendInitOkToMcu();
                LogUtils.d(TAG, "first boot, send MCU RPT_SysInitOK");
            } else if (PowerManager.PowerStep.isPoweronOvered(newVal)) {
                // 解静
                VolumeDriver.Driver().mute(false);
            }
        }
    };

    /**源监听器*/
    SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            final int source = McuAdapter.getUIMode(newVal);
            byte[] data = new byte[1];
            data[0] = (byte) source;
            McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.RPT_SysUImode, data, 1);
        }

        int id;
        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            final int source = McuAdapter.getMediaMode(newVal);

            if (0xFF != source) {
                byte[] data = new byte[1];
                data[0] = (byte) source;
                LogUtils.d(TAG, "RPT_SysMediamode source="+source);
                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_SysMediamode, data, 1);

                //百盛的需要下面命令，否则会无法切换声音通道。
                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_MediaInfo, data, 1);
            }
        }
    };

    /**MCU监听器*/
    McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            // 启动MCU监听
            startMCUMonitor();

            if (totalTime != -1 && hourTime != -1 && accoffTimer != -1) {
                byte[] data = new byte[4];
                data[0] = (byte) ((totalTime >> 8) & 0xFF);
                data[1] = (byte) (totalTime & 0xFF);
                data[2] = (byte) (hourTime & 0xFF);
                data[3] = (byte) (accoffTimer & 0xFF);
                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_SEND_TIME_TEST, data, data.length);
            }

            if (isHasTboxProject()) {
                TBoxDataManager.getTBoxManager().sendSimToTBox(getMainContext());
            }
        }

        @Override
        public void CloseListener(int status) {
            // 停止MCU监听
            stopMCUMonitor();

            if (isHasTboxProject()) {
                TBoxDataManager.getTBoxManager().sendSimToTBox(null);
            }
        }

        @Override
        public void DataListener(byte[] val) {
            if (null != val) {
                final int length = val.length;
                if (length > 0) {
                    // 打印MCU数据
                    //LogUtils.d(TAG, "MCU --> ARM:" + FormatData.formatHexBufToString(val, length));

                    final int cmd = val[0] & 0xff;
                    switch (cmd) {
                        case McuDefine.MCU_TO_ARM.MACK_SysInitdata:
                            if (length > 1) {
                                // 启动方式
                                final int start = (int) val[1];
                                if (length > 2) {
                                    // 上次关机源
                                    final int source = (int) val[2] & 0xff;
                                    mMemorySource = McuAdapter.getMcuMediaSource(source);
                                    LogUtils.d(TAG, "MCU power on source = " + Define.Source.toString(mMemorySource) + ", data = " + source);

                                    if (ApkUtils.isAPKExist(getMainContext(), "com.baony.avm360")) {
                                        mMemorySource = Define.Source.SOURCE_SILENT;
                                    } else {
                                        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
                                        if (client != null) {
                                            String clientProject = client.getString("ClientProject");
                                            if (clientProject != null && clientProject.equals("ch010_23")) {//马自达不作记忆
                                                mMemorySource = Define.Source.SOURCE_SILENT;
                                            }
                                        }
                                    }

                                    if (SourceManager.getCurBackSource() == Define.Source.SOURCE_SILENT &&
                                            mMemorySource != Define.Source.SOURCE_NONE) {
                                        //当MCU记忆的是关机源时，默认到主页。2020-03-17
                                        if (mMemorySource == Define.Source.SOURCE_POWEROFF) {
                                            mMemorySource = Define.Source.SOURCE_SILENT;
                                        }
                                        AndroidMediaDriver.bFirstboot = true;
                                        //启动后，快速倒车，会将倒车界面切掉。2019-01-15
                                        if (EventInputManager.getCamera()) {
                                            CameraLogic.setEnterCameraSource(mMemorySource, false);
                                            SourceManager.onOpenBackgroundSource(mMemorySource);
                                        } else {
                                            SourceManager.onChangeSource(mMemorySource);
                                        }
                                    }
                                }

                                final int source = McuAdapter.getMediaMode(SourceManager.getCurBackSource());
                                LogUtils.d(TAG, "MACK_SysInitdata RPT_SysMediamode source=" + source + ", backsource=" + SourceManager.getCurBackSource());
                                if (0xFF != source) {
                                    byte[] data = new byte[1];
                                    data[0] = (byte) source;
                                    McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_SysMediamode, data, 1);

                                    //百盛的需要下面命令，否则会无法切换声音通道。
                                    McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_MediaInfo, data, 1);
                                }
                            }
                            break;
                        case McuDefine.MCU_TO_ARM.MOP_UserKEY:
                            if (length > 1) {
                                if (val[1] == 0x58) {//LEFT
                                    processSystemKey(KeyEvent.KEYCODE_DPAD_UP);
                                } else if (val[1] == 0x59) {//RIGHT
                                    processSystemKey(KeyEvent.KEYCODE_DPAD_DOWN);
                                } else if (val[1] == 0x5A) {//OK
                                    processSystemKey(KeyEvent.KEYCODE_ENTER);
                                } else {
                                    int key = McuAdapter.getKey(val[1]);
                                    if (key != Define.Key.KEY_NONE) {
                                        EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.MCU, key, null);
                                        LogUtils.d(TAG, "MCU key = " + Define.Key.toString(key));
                                    }
                                }
                            }
                            break;
                        case McuDefine.MCU_TO_ARM.MRPT_LAND_PORT_STATUS:
                            if (length > 1) {
                                int state = val[1];//1：现在是横屏，2：现在是竖屏
                                int mCurrentOrientation = getMainContext().getResources().getConfiguration().orientation;
                                if ((mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT && state != 2) ||
                                        (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE && state != 1)) {
                                    LogUtils.e(TAG, "MRPT_LAND_PORT_STATUS---mCurrentOrientation=" + mCurrentOrientation + ", state=" + state);
                                    //通知SystemUI旋转屏幕，统一由SystemUI调用系统旋转接口
                                    Intent intent = new Intent("com.wwc2.rotate.screen");
                                    intent.putExtra("correct", true);
                                    getMainContext().sendBroadcast(intent);
                                }
                            }
                            break;
                        case McuDefine.MCU_TO_ARM.MPRT_REV_TBOX_DATA:
                            if (length > 1) {
                                TBoxDataManager.getTBoxManager().revDataFromTBox(Arrays.copyOfRange(val, 1, val.length));
                            }
                            break;
                        case McuDefine.MCU_TO_ARM.MPRT_CAR_STATUS:
                            if (length > 1) {
                                int value = val[1]; //1尾箱关闭，2尾箱开启
                                Intent intent = new Intent("com.wwc2.trunk.status");
                                intent.putExtra("status", value);
                                getMainContext().sendBroadcast(intent);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };

    protected void processSystemKey(int systemKey) {
        if (systemKey != KeyEvent.KEYCODE_UNKNOWN) {
            final int finalSystemKey = systemKey;
            LogUtils.d(TAG, "processSystemKey----finalSystemKey:" + finalSystemKey);

            //解决有些界面无beep的问题。2020-03-27
            EventInputManager.NotifyStatusEvent(true, EventInputDefine.Status.TYPE_BEEP, true, null);

            new Thread() {
                public void run() {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(finalSystemKey);
                }
            }.start();
        }
    }

    /**蓝牙监听器*/
    BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void CallNumberListener(String oldVal, String newVal) {
//            sendBluetoothStatus();
        }

        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            final boolean oldCall = BluetoothDefine.HFPStatus.isCalling(oldVal);
            final boolean newCall = BluetoothDefine.HFPStatus.isCalling(newVal);

            if (oldCall && !newCall) {
                mIsCalling = false;
                sendBluetoothStatus();//只在通话状态变化时通知MCU，否则会出现bug9809关机状态下也会有导航声音输出2017-09-27
            } else if (!oldCall && newCall) {
                mIsCalling = true;

                byte[] data = new byte[2];
                data[0] = 0x0d;
                data[1] = (byte)McuAdapter.getHFPStatus(newVal);
                McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.RPT_BtInfo, data, data.length);
                sendBluetoothStatus();//只在通话状态变化时通知MCU，否则会出现bug9809关机状态下也会有导航声音输出2017-09-27
            }

//            if (!BluetoothDefine.HFPStatus.isCalling(oldVal) && BluetoothDefine.HFPStatus.isCalling(newVal)) {
//                LogUtils.d("MIC", "BT");
//                MicSwitchNative.MICSWITCH_BT();
//            } else if (BluetoothDefine.HFPStatus.isCalling(oldVal) && !BluetoothDefine.HFPStatus.isCalling(newVal)) {
//                LogUtils.d("MIC", "ARM");
//                MicSwitchNative.MICSWITCH_ARM();
//            }

//            final boolean oldChannel = BluetoothDefine.HFPStatus.isCallOut(oldVal) || BluetoothDefine.HFPStatus.isTalking(oldVal);
//            final boolean newChannel = BluetoothDefine.HFPStatus.isCallOut(newVal) || BluetoothDefine.HFPStatus.isTalking(newVal);
//            if (!oldChannel && newChannel) {
//                // bluetooth
//                LogUtils.d(TAG, "HFPStatusListener set volume channel to bluetooth.");
//                byte[] data = new byte[1];
//                data[0] = (byte)0x04;
//                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_VolumeChannel, data, 1);
//            } else if (oldChannel && !newChannel) {
//                // arm
//                LogUtils.d(TAG, "HFPStatusListener set volume channel to arm.");
//                byte[] data = new byte[1];
//                data[0] = (byte)0x01;
//                McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.OP_VolumeChannel, data, 1);
//            }
        }

        @Override
        public void MusicStatusListener(Integer oldVal, Integer newVal) {
//            if (Define.Source.SOURCE_BLUETOOTH == SourceManager.getCurBackSource()) {
//                if (BluetoothDefine.MusicStatus.isPlay(newVal)) {
//                    // 蓝牙在播放，认为是A2DP模式
//                    LogUtils.d(TAG, "bluetooth music play, send MCU A2DP source.");
//                    byte[] data = new byte[1];
//                    data[0] = (byte)0x0a;
//                    McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.RPT_SysMediamode, data, 1);
//                } else {
//                    // 蓝牙暂停，认为是蓝牙模式
//                    LogUtils.d(TAG, "bluetooth music pause, send MCU BT source.");
//                    byte[] data = new byte[1];
//                    data[0] = (byte)0x05;
//                    McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.RPT_SysMediamode, data, 1);
//                }
//            }
        }
    };

    /**ACC OFF监听器*/
    AccoffListener mAccoffListener = new AccoffListener() {

        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            if (AccoffStep.isAccoff(oldVal) && !AccoffStep.isAccoff(newVal)) {
                // 从深度睡眠中开机
                McuManager.sendInitOkToMcu();
                LogUtils.d(TAG, "acc off -- on, send MCU RPT_SysInitOK");
            } else if (!AccoffStep.isDeepSleep(oldVal) && AccoffStep.isDeepSleep(newVal)) {
                // 进入深度睡眠
                McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.OP_LOGO_STATE, new byte[]{0x01}, 1);
                // zhongyang.hu remove the no use cmd, uart will close in the next line so  . 20180413
               //  byte[] data = new byte[1];
              //  data[0] = 0;
              //  McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_SysPoweroff, data, 1);
              //end

            }
        }
    };

    /**导航出声监听器*/
    boolean mNaviSoundActive = false;
    AudioListener mAudioListener = new AudioListener() {
        @Override
        public void NaviAudioActiveListener(Boolean oldVal, Boolean newVal) {
            if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF && newVal) {
                return;
            }
            mNaviSoundActive= newVal;

            if (isCalling() || EcarHelper.getEcarState()) {
                LogUtils.d(TAG, "Navi audio change return to MCU.");
                return;//解决通话过程中报点MCU会对声音进行处理，出现通话声音会变大变小。暂时AP处理。YDG 2017-04-21
            }
            if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF && newVal) {
                return;
            }
            //不管在哪个源，都通知MCU，由MCU处理。2018-01-06
//            if (SourceManager.getCurBackSource() != Define.Source.SOURCE_RADIO &&
//                    SourceManager.getCurBackSource() != Define.Source.SOURCE_AUX/* &&
//                    SourceManager.getCurBackSource() != Define.Source.SOURCE_SILENT*/) {
//                LogUtils.d(TAG, "Navi audio change return to MCU because the Source is:"+SourceManager.getCurBackSource());
//                return;
//            }
            if (newVal) {
                // 导航声音开始
                mNaviSoundActive = true;
                LogUtils.d(TAG, "Navi audio start.");
                byte[] data = new byte[1];
                data[0] = 1;
                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_GPS_SPEAKER, data, 1);
            } else {
                // 导航声音结束
                mNaviSoundActive = false;
                LogUtils.d(TAG, "Navi audio stop.");
                byte[] data = new byte[1];
                data[0] = 0;
                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_GPS_SPEAKER, data, 1);
            }
        }

        @Override
        public void ArmAudioActivieListener(Boolean oldVal, Boolean newVal) {
            if (isCalling() || EcarHelper.getEcarState() ||//修改：收音界面来电话，接听蓝牙无声音。
                    mNaviSoundActive) {//在报点时不通知MCU切换声音通道，避免MCU会切回收音，解决bug13874。2018-11-23
                LogUtils.e(TAG, "ArmAudioActivieListener =" + newVal + ", mIsCalling=" + mIsCalling +
                        ", mNaviSoundActive=" + mNaviSoundActive);
                return;
            }
            if (newVal) {
                // ARM声音开始
                LogUtils.d(TAG, "ARM audio start.");
                McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.RPT_CPU_SEND_SOUND, new byte[]{0x01}, 1);
            } else {
                // ARM声音结束
                LogUtils.d(TAG, "ARM audio stop.");
                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_CPU_SEND_SOUND, new byte[]{0x00}, 1);
            }
        }
    };

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

    /**
     * 事件输入监听器
     */
    private EventInputListener mEventInputListener = new EventInputListener() {

        @Override
        public void AccListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "AccListener, oldVal = " + oldVal + ", newVal = " + newVal);
            if (!newVal) {
                Calendar mCalendar = Calendar.getInstance();
                if (null != mCalendar) {
                    byte[] data = new byte[7];
                    data[0] = (byte) ((mCalendar.get(Calendar.YEAR) >> 8) & 0xFF);
                    data[1] = (byte) (mCalendar.get(Calendar.YEAR) & 0xFF);
                    data[2] = (byte) ((mCalendar.get(Calendar.MONTH) + 1) & 0xFF);
                    data[3] = (byte) (mCalendar.get(Calendar.DAY_OF_MONTH) & 0xFF);
                    data[4] = (byte) (mCalendar.get(Calendar.HOUR_OF_DAY) & 0xFF);
                    data[5] = (byte) (mCalendar.get(Calendar.MINUTE) & 0xFF);
                    data[6] = (byte) (mCalendar.get(Calendar.SECOND) & 0xFF);
                    McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_SEND_TIME, data, data.length);
                }
            }
        }
    };

    static int totalTime = -1;
    static int hourTime = -1;
    static int accoffTimer = -1;
    public static void sendTestTimeToMcu(int total, int hour, int accoffTime) {
        totalTime = total;
        hourTime = hour;
        accoffTimer = accoffTime;

        byte[] data = new byte[4];
        data[0] = (byte) ((total >> 8) & 0xFF);
        data[1] = (byte) (total & 0xFF);
        data[2] = (byte) (hour & 0xFF);
        data[3] = (byte) (accoffTime & 0xFF);
        McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_SEND_TIME_TEST, data, data.length);
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        BaseLogic logic = null;
        BaseModel model = null;

        // 绑定开机管理监听器
        model = PowerManager.getModel();
        model.bindListener(mPoweronListener);

        // 绑定源管理监听
        model = SourceManager.getModel();
        model.bindListener(mSourceListener);

        // 绑定MCU管理监听
        model = McuManager.getModel();
        model.bindListener(mMCUListener);

        // 绑定蓝牙监听
        model = ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel();
        model.bindListener(mBluetoothListener);

        // 绑定关机监听
        model = ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel();
        model.bindListener(mAccoffListener);

        // 导航监听
        model = DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel();
        model.bindListener(mAudioListener);

        // 倒车监听
        model = ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel();
        model.bindListener(mEventInputListener);
    }

    @Override
    public void onDestroy() {
        BaseLogic logic = null;
        BaseModel model = null;

        // 解绑倒车监听
        model = ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel();
        model.unbindListener(mEventInputListener);

        // 解绑导航监听
        model = DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel();
        model.unbindListener(mAudioListener);

        // 解绑关机监听
        model = ModuleManager.getLogicByName(PoweroffDefine.MODULE).getModel();
        model.unbindListener(mAccoffListener);

        // 解绑蓝牙监听
        model = ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel();
        model.unbindListener(mBluetoothListener);

        // 解绑源管理监听
        model = SourceManager.getModel();
        model.unbindListener(mSourceListener);

        // 解绑MCU管理监听
        model = McuManager.getModel();
        model.unbindListener(mMCUListener);

        // 解绑开机管理监听器
        model = PowerManager.getModel();
        model.unbindListener(mPoweronListener);

        super.onDestroy();
    }

    @Override
    public int open() {
        int ret = -1;
        //int open = McuSerialNative.DEVICE_open("/dev/ttyHS0", 115200, McuSerialNative.O_CLSTTY);
        int open = McuSerialNative.DEVICE_open(""/*"/dev/ttyMT2"*/, 115200, McuSerialNative.O_CLSTTY);
        if (0 == open) {
            mOpen = true;
            ret = OPEN_RET_REPEAT;
            LogUtils.w(TAG, "MCU serial port has been opened, no need to open!");
        } else if (1 == open) {
            mOpen = true;
            ret = OPEN_RET_SUCCESS;
            LogUtils.d(TAG, "MCU serial open success!");
        } else {
            mOpen = false;
            LogUtils.w(TAG, "MCU serial port open failed!");
        }
        return ret;
    }

    @Override
    public int close() {
        int ret = -1;
        mOpen = false;
        int close = McuSerialNative.DEVICE_close();
        if (0 == close) {
            ret = McuStatusDefine.CloseStatus.NONEED;
            LogUtils.w(TAG, "MCU serial port is closed, no need to close!");
        } else if (1 == close) {
            ret = McuStatusDefine.CloseStatus.SUCCESS;
            LogUtils.d(TAG, "MCU serial port off successfully!");
        } else {
            LogUtils.w(TAG, "MCU serial port off failed!");
        }
        return ret;
    }

    @Override
    public int firstBoot() {
        int ret = Define.FirstBoot.NO;
        if (MEMORY_SOURCE) {
            if (Define.Source.SOURCE_INVALID == mMemorySource) {
                ret = Define.FirstBoot.WAIT;
            } else if (Define.Source.SOURCE_NONE != mMemorySource){
                ret = Define.FirstBoot.YES;
            }
        }
        return ret;
    }

    @Override
    public int getMemorySource() {
        return mMemorySource;
    }

    @Override
    public int sendMcu(boolean needAck,int priority, boolean check, byte head, byte[] buf, int len) {
        int ret = -1;
        byte[] _head = new byte[1];
        _head[0] = head;

        final String string = "ARM --> MCU: needAck=" + needAck + ", priority = " + priority + ", check = " + check +
                ", head = " + FormatData.formatHexBufToString(_head, _head.length) +
                ", buf = " + FormatData.formatHexBufToString(buf, len);

        if (mOpen) {
            // 打印MCU数据
            //LogUtils.d(TAG,  string);

            // 发送MCU数据
            ret = McuSerialNative.DEVICE_write(needAck,priority, check, head, buf, len);
        } else {
            LogUtils.w(TAG, "warning, sendMcu failed, because the port is close, " + string);
        }
        return ret;
    }

    /**发送蓝牙状态*/
    private void sendBluetoothStatus() {
        BaseModel model = ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel();
        if (null != model) {
            Packet packet = model.getInfo();
            if (null != packet) {
                final int status = packet.getInt("HFPStatus");
                final String number = packet.getString("CallNumber");

                int length = 2;
                if (null != number) {
                    length += number.length();
                }
                byte[] data = new byte[length];
                data[0] = 0x0d;
                data[1] = (byte)McuAdapter.getHFPStatus(status);
                if (null != number) {
                    for (int i = 0;i < number.length();i++) {
                        char ch = number.charAt(i);
                        data[2+i] = (byte)(ch - '0');
                    }
                }
                McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.RPT_BtInfo, data, length);
            }
        }
    }

//    public static double getCPURateDesc(){
//        String path = "/proc/stat";
//        long totalJiffies[]=new long[2];
//        long totalIdle[]=new long[2];
//        int firstCPUNum=0;
//        FileReader fileReader = null;
//        BufferedReader bufferedReader = null;
//        Pattern pattern=Pattern.compile(" [0-9]+");
//        for(int i=0;i<2;i++) {
//            totalJiffies[i]=0;
//            totalIdle[i]=0;
//            try {
//                fileReader = new FileReader(path);
//                bufferedReader = new BufferedReader(fileReader, 8192);
//                int currentCPUNum=0;
//                String str;
//                while ((str = bufferedReader.readLine()) != null&&(i==0||currentCPUNum<firstCPUNum)) {
//                    if (str.toLowerCase().startsWith("cpu")) {
//                        currentCPUNum++;
//                        int index = 0;
//                        Matcher matcher = pattern.matcher(str);
//                        while (matcher.find()) {
//                            try {
//                                long tempJiffies = Long.parseLong(matcher.group(0).trim());
//                                totalJiffies[i] += tempJiffies;
//                                if (index == 3) {
//                                    totalIdle[i] += tempJiffies;
//                                }
//                                index++;
//                            } catch (NumberFormatException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    if(i==0){
//                        firstCPUNum=currentCPUNum;
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } finally {
//                if (bufferedReader != null) {
//                    try {
//                        bufferedReader.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        double rate=-1;
//        if (totalJiffies[0]>0&&totalJiffies[1]>0&&totalJiffies[0]!=totalJiffies[1]){
//            rate=1.0*((totalJiffies[1]-totalIdle[1])-(totalJiffies[0]-totalIdle[0]))/(totalJiffies[1]-totalJiffies[0]);
//        }
////        LogUtils.w(TAG, "Current times " + String.format("cpu:%.2f",rate));
//        return rate;
//    }


    /**
     * 杀死指定包名的进程
     */
    public static boolean killProcess(Context context, String packageName) {
        boolean ret = false;
        if (null == context) {
            LogUtils.w(TAG, "#killProcess failed, because the context is null.");
            return ret;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (null != am) {
            try {
                LogUtils.d(TAG, "#killProcess packageName = " + packageName);
                Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
                forceStopPackage.setAccessible(true);
                forceStopPackage.invoke(am, packageName);
                ret = true;
            } catch (NoSuchMethodException e) {
                LogUtils.w(TAG, "#killProcess failed, because NoSuchMethodException.");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                LogUtils.w(TAG, "#killProcess failed, because IllegalAccessException.");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                LogUtils.w(TAG, "#killProcess failed, because InvocationTargetException.");
                e.printStackTrace();
            } catch (Exception e) {
                LogUtils.w(TAG, "#killProcess failed, because Exception.");
                e.printStackTrace();
            }
        } else {
            LogUtils.w(TAG, "#killProcess failed, because the activity manager is null.");
        }
        return ret;
    }



    /**
     * 是否为保留的包名
     */
    private static boolean isKeepPackage(String[] keepPackages, String packageName) {
        boolean ret = false;
        if (null != keepPackages && null != packageName) {
            final int length = keepPackages.length;
            for (int i = 0; i < length; i++) {
                if (packageName.equals(keepPackages[i])) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 指定进程是否正在运行
     */
    public static boolean isProcessRunning(Context context, String packageName) {
        return ApkUtils.isProcessRunning(context, packageName);
    }


    /**
     * 杀死进程
     */
    public static void killProcess(Context context, String[] keepPackages,int killNum) {
        int cnt =killNum;
        if (null == context) {
            return;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (null != am) {
            List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
            if (null != list) {
                final int size = list.size();
                for (int i = 0; i < size && cnt>0; i++) {
                    ActivityManager.RunningAppProcessInfo temp = list.get(i);
                    if (null != temp) {
                        String[] pkgList = temp.pkgList;
                        if (null != pkgList) {
                            for (int j = 0; j < pkgList.length; j++) {
                                final String p = pkgList[j];
                                if (!isKeepPackage(keepPackages, p)) {
                                    cnt--;
                                   killProcess(context, p);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**MCU监测ARM状态依据，用source指令实现*/
    private void startMCUMonitor() {
        // MCU监测ARM状态依据，用source指令实现
        TimerUtils.killTimer(mPollingTimerId);

        mPollingTimerId = TimerUtils.setTimer(getMainContext(), 2000, 2000, new Timerable.TimerListener() {
            @Override
            public void onTimer(int timerId) {
                final int cur_source = SourceManager.getCurSource();
                final int back_source = SourceManager.getCurBackSource();
                final boolean volume_mute = VolumeManager.getMute();
                final int volume_type = VolumeManager.getType();
                final int volume_value = VolumeManager.getValue();
                LogUtils.d(TAG, "send mcu heartbeat data, cur_source = " + Define.Source.toString(cur_source) +
                        ", back_source = " + Define.Source.toString(back_source) +
                        ", volume_mute = " + volume_mute +
                        ", volume_type = " + Define.VolumeType.toString(volume_type) +
                        ", volume_value = " + volume_value +
                        ", logo = " + McuManager.getLogoStatus());

                // send heartbeat package
                byte[] data = new byte[6];
                data[0] = (byte) McuAdapter.getUIMode(cur_source);
                data[1] = (byte) McuAdapter.getMediaMode(back_source);
                data[2] = (byte) (volume_mute ? 0x00 : 0x01);
                data[3] = (byte)McuAdapter.getMCUVolumeType(volume_type);
                data[4] = (byte) volume_value;
                data[5] = (byte) (McuManager.getLogoStatus() ? 0x01 : 0x00);
                McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.REQ_HeartBeat, data, 6);

                // query camera status
                data[0] = 0;
                McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.RPT_RequestReverse, data, 1);

                // query acc status
                data[0] = 0;
                McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.RPT_RequestAcc, data, 1);

                //begin zhongyang.hu add
                CoreLogic coreLogic = LogicManager.getLogicByName(SettingsDefine.MODULE);
                if (coreLogic instanceof SettingsLogic) {
                    if(((SettingsLogic) coreLogic).IsImport()){
                        ((SettingsLogic) coreLogic).sendImportState();
                    }
                }
                //end
                //zhongyang.hu remove when the cpu full rate,kill apk.
//                double rate = getCPURateDesc();
//                if(rate >= 0.97){
//                   mCpuLoadCnt ++;
//                } else if(rate < 0.91){
//                   mCpuLoadCnt =0;
//                }
//
//               if(mCpuLoadCnt > 3 && cur_source==Define.Source.SOURCE_CAMERA ){
//                    LogUtils.w(TAG, "CpuLoad is full, need to kill the font process for SOURCE_CAMERA");
//                    Context context = getMainContext();
//                    killProcess(context,mKeepProcessPackageFilter,KILL_PRO_NUM);
//                    mCpuLoadCnt=0;
//                }
                //end

                if(mCpuTempThermalCnt++ >= 5) {
                    CPUThermalManager.updateCPUState();
                    CPUThermalManager.SetThermalPolicy(getMainContext());
                    mCpuTempThermalCnt=0;
                }

                //begin  zhongyang.hu add for upload error state info.  20180333
                if(mcuWatchDogCnt != OUT_DOG_MAGIC) {
                    if (mcuWatchDogCnt++ >= 20) {
                        if (PowerManager.isOutOfDog()) {
                            if(!checkAccOffState()) {
                                mcuWatchDogCnt = OUT_DOG_MAGIC;
                                LogUtils.w(TAG, "isOutOfDog!!!!!!");
                                Intent mIntent = new Intent(MainService.INTENT_ERROR);
                                mIntent.putExtra(MainService.ERROR_TYPE, MainService.MCU_UART_ERROR);
                                getMainContext().sendBroadcast(mIntent);
                            }else{
                                mcuWatchDogCnt =0;
                            }
                            //add for acc off .
                        }else{
                            mcuWatchDogCnt = 0;
                        }
                        LogUtils.w(TAG, "resetDogFlag!!!!!!");
                        PowerManager.resetDogFlag();
                    }
                    //end
                }
                //end

            }
        });
    }
    //begin zhongyang.hu add for AccOff check. 201804026
    private boolean checkAccOffState(){
        boolean ret =false;
        String sstate =getContentFromFile(ACC_ONOFF_STATE);
        int istate;
        try {
             istate = Integer.parseInt(sstate);
        }catch (NumberFormatException e){
            Log.w(TAG,"acc state fail!");
            return ret;
        }
        if(istate == ACC_OFF && SourceManager.getCurSource() != Define.Source.SOURCE_ACCOFF){
            //zhongyang.hu modify for cannot acc on, when acc but not set uart acc value to false,2018
            //old SourceManager.onChangeSource(Define.Source.SOURCE_ACCOFF);
            BaseDriver driver = ModuleManager.getLogicByName(EventInputDefine.MODULE).getDriver();
            if (driver instanceof BaseEventInputDriver) {
                BaseEventInputDriver mEventInputDriver = (BaseEventInputDriver) driver;
                mEventInputDriver.setAcc(false, false);

            }
            //end
            ret = true;
        }
        return ret;
    }

    public static String getContentFromFile(String filePath) {
        char[] buffer = new char[128];
        FileReader reader = null;
        String content = null;
        try {
            reader = new FileReader(filePath);
            int len = reader.read(buffer, 0, buffer.length);
            content = String.valueOf(buffer, 0, len).trim();
            Log.d(TAG, filePath + " content is " + content);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "can't find file " + filePath);
        } catch (IOException e) {
            Log.w(TAG, "IO exception when read file " + filePath);
        } catch (IndexOutOfBoundsException e) {
            Log.w(TAG, "index exception: " + e.getMessage());
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "close reader fail: " + e.getMessage());
                }
            }
        }
        return content;
    }
    //end
    /**销毁MCU串口监测*/
    private void stopMCUMonitor() {
        // 销毁MCU串口监测
        if (0 != mPollingTimerId) {
            TimerUtils.killTimer(mPollingTimerId);
            mPollingTimerId = 0;
        }
        //begin zhongyang.hu add for AccOff check. 201804026
        mcuWatchDogCnt =0;
        //end
    }

    private boolean isHasTboxProject() {
        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        String clientProject = BaseClientDriver.CLIENT_JS;
        if (client != null) {
            clientProject = client.getString("ClientProject");
            if (clientProject != null) {
                if (clientProject.equals("ch010_23") || clientProject.equals("ch010_24")) {
                    return true;
                }
            }
        }
        return false;
    }
}
