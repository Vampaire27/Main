package com.wwc2.main.driver.system.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.camera.CameraLogic;
import com.wwc2.main.common.CommonLogic;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.driver.volume.VolumeListener;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.navi.driver.NaviDriverable;
import com.wwc2.main.settings.util.FileUtil;
import com.wwc2.main.settings.util.XmlUtil;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.mainui_interface.MainUIInterface;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * the mtk6737 system driver.
 *
 * @author wwc2
 * @date 2017/1/23
 */
public class MTK6737SystemDriver extends BaseSystemDriver {

    /**TAG*/
    private static final String TAG = "MTK6737SystemDriver";

    /**current package name.*/
    public static final String CURRENT_PACKAGENAME = "com.wwc2.framework.action.CURRENT_PACKAGENAME";

    /**simulate key.*/
    public static final String SIMULATE_KEY = "com.wwc2.simulate.key";

    /**touch event.*/
    public static final String TOUCH_EVENT = "com.wwc2.touch.event";

    /**operate the system ui status.*/
    private static final String ACTION_OPT_VISIBLE = "android.intent.action.ACTION_OPT_VISIBLE";

    /**operate the volume window to show*/
    public static final String ACTION_SHOW_VOLUME = "com.wwc2.show.volume.window";
    public static final String KEY_SHOW_VOLUME = "show_volume";//true:show, false:hide

    public static final String ACTION_SHOW_BRIGHTNESS = "com.wwc2.show.brightness.window";
    public static final String KEY_SHOW_BRIGHTNESS = "show_brightness";

    /**reboot system for SystemUI*/
    public static final String ACTION_REBOOT_SYSTEM = "com.wwc2.reboot.system";

    public static final String ACTION_START_LAUNCHER = "com.wwc2.start.launcher";

    public static final String ACTION_MCU_ROTATE_SCREEN = "com.wwc2.mcu.rotate.screen";

    //通知MCU切换摄像头，锐派货车项目
    public static final String ACTION_SWITCH_CAMERA = "com.wwc2.switch.camera";

    private boolean mFirstRunLauncher = false;

    private boolean mIsPortProject      = false;

    // 蓝牙监听
    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            Context context = getMainContext();
            if (null != context) {
                // 发送广播
                Intent intent = new Intent("com.wwc2.bluetooth.hfpstatus");
                intent.putExtra("HFPStatus", newVal);
                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.sendStickyBroadcast(intent);
                LogUtils.d(TAG,"send hfpStatus" +newVal);
            }
        }
        @Override
        public void OpenOrCloseListener(Boolean oldVal, Boolean newVal) {
            // 发送广播
            Context context = getMainContext();
            if (null != context) {
                Intent intent = new Intent("com.wwc2.bluetooth.openorclose");
                intent.putExtra("OpenOrClose", newVal);
                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.sendStickyBroadcast(intent);
                LogUtils.d(TAG,"send openOrClose" +newVal);
            }
        }
    };

    // 声音监听
    private VolumeListener mVolumeListener = new VolumeListener() {
        @Override
        public void VolumeMuteListener(Boolean oldVal, Boolean newVal) {
            // 发送广播
            Context context = getMainContext();
            if (null != context) {
                Intent intent = new Intent("com.wwc2.volume.mute");
                intent.putExtra("mute", newVal);
                context.sendBroadcast(intent);
            }
        }
    };

    // 开机状态监听
    private PowerManager.PowerListener mPowerListener = new PowerManager.PowerListener() {
        private static final String ACTION_READ_CONFIG_FINISH = "com.wwc2.action.read.config.finish";

        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            Context context = getMainContext();
            if (PowerManager.PowerStep.isPoweronMcuCreated(newVal)) {
                if (null != context) {
                    LogUtils.d(TAG, " register  mBroadcastReceiver&mKeyBroadcastReceiver broadcast!! " );
                    IntentFilter myIntentFilter = new IntentFilter();
                    myIntentFilter.addAction(CURRENT_PACKAGENAME);
                    myIntentFilter.addAction(ACTION_SHOW_VOLUME);
                    myIntentFilter.addAction(ACTION_SHOW_BRIGHTNESS);
                    myIntentFilter.addAction(ACTION_REBOOT_SYSTEM);
                    myIntentFilter.addAction(ACTION_START_LAUNCHER);
                    myIntentFilter.addAction(ACTION_MCU_ROTATE_SCREEN);
                    myIntentFilter.addAction(ACTION_SWITCH_CAMERA);
                    context.registerReceiver(mBroadcastReceiver, myIntentFilter);

                    IntentFilter myKeyIntentFilter = new IntentFilter();
                    myKeyIntentFilter.addAction(SIMULATE_KEY);
                    myKeyIntentFilter.addAction(TOUCH_EVENT);
                    context.registerReceiver(mKeyBroadcastReceiver, myKeyIntentFilter);

                    mIsPortProject = PowerManager.isPortProject();
                }
            } else if (PowerManager.PowerStep.isPoweroffStart(newVal)) {
                if (null != context) {
                    LogUtils.d(TAG, " mBroadcastReceiver mKeyBroadcastReceiver  have been unregister !");
                    context.unregisterReceiver(mBroadcastReceiver);
                    context.unregisterReceiver(mKeyBroadcastReceiver);
                }
                //add by huwei 180120;send broadcast to notify read ini finish;
            } else if (PowerManager.PowerStep.isPoweronModulePrepared(newVal)) {
                Intent intent = new Intent(ACTION_READ_CONFIG_FINISH);
//                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.sendBroadcast(intent);
            }
        }
    };

    // 模式监听
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            BaseLogic oldLogic = ModuleManager.getLogicBySource(oldVal);
            BaseLogic newLogic = ModuleManager.getLogicBySource(newVal);
            if (null != oldLogic && null != newLogic) {
                if (oldLogic.isFullScreenSource() != newLogic.isFullScreenSource()) {
                    Context context = getMainContext();
                    if (null != context) {
                        if (!oldLogic.isFullScreenSource() && newLogic.isFullScreenSource()) {
                            //全屏
                            context.sendBroadcast(new Intent("ACTION_STATUS_BAR_HIDE"));
                        } else {
                            //退出全屏
                            context.sendBroadcast(new Intent("ACTION_STATUS_BAR_SHOW"));
                        }
                    }
                }
            } else if (null == oldLogic && null != newLogic) {
                Context context = getMainContext();
                if (newLogic.isFullScreenSource()) {
                    if (null != context) {
                        context.sendBroadcast(new Intent("ACTION_STATUS_BAR_HIDE"));
                    }
                } else {
                    context.sendBroadcast(new Intent("ACTION_STATUS_BAR_SHOW"));
                }
            }
        }
    };

    //com.wwc2.mcuupdate
    //com.adups.fota
    //com.wwc2.systemupdate_apk
    //com.wwc2.settings
    String oldPkgName = "";
    private boolean isUpdateMenu(String oldPkgName) {
        boolean ret = false;
        if (oldPkgName.equals("com.wwc2.mcuupdate") ||
                oldPkgName.equals("com.adups.fota") ||
                oldPkgName.equals("com.wwc2.systemupdate_apk")) {
            ret = true;
        }
        return ret;
    }

    private void sendMcuUpdateState(String packetName) {
        if (isUpdateMenu(packetName) && !isUpdateMenu(oldPkgName)) {
            byte[] data = new byte[1];
            data[0] = (byte) 0x02;
            McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
        } else if (isUpdateMenu(oldPkgName) && !isUpdateMenu(packetName)) {
            byte[] data = new byte[1];
            data[0] = (byte) 0x04;
            McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
        }
        oldPkgName = packetName;
    }

    // 广播监听
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "system broadcast receiver action = " + action);
            if(CURRENT_PACKAGENAME.equals(action)){
                // 包名发生变化
                Bundle bundle = intent.getExtras();
                if (null != bundle) {
                    String packetName = bundle.getString("packagename");
                    String className = bundle.getString("classname");
                    LogUtils.d(TAG, "top package name = " + packetName + ", className = " + className);
                    
                    if (packetName.equals("com.coagent.ecar")) {
                        LogUtils.e(TAG, "not to change source when ecar start!");
                        return;
                    }

                    if (!oldPkgName.equals(packetName)) {
                        sendMcuUpdateState(packetName);
                    }

                    /*-begin-20180423-ydinggen-modify-Acc深睡和ARM断电起来，由于收到的包名和实际显示的不一致，不处理Launcher的切源，避免出现切源导致视频不播放-*/
                    if (mFirstRunLauncher || SourceManager.getCurSource() == Define.Source.SOURCE_POWEROFF) {
                        BaseLogic logic = ModuleManager.getLogicBySource(Define.Source.SOURCE_LAUNCHER);
                        if (logic != null) {
                            if (packetName.equals(logic.getAPKPacketName())) {
                                if (SourceManager.getCurSource() != Define.Source.SOURCE_POWEROFF) {
                                    mFirstRunLauncher = false;
                                }
                                LogUtils.e(TAG, "topPackageClassChanged return Launcher First!");
                                return;
                            }
                        }
                    }
                    /*-end-20180423-ydinggen-modify-Acc深睡和ARM断电起来，由于收到的包名和实际显示的不一致，不处理Launcher的切源，避免出现切源导致视频不播放-*/

                    if (mIsPortProject) {//竖屏、旋转屏项目导航时不切源，避免分屏时会导致视频退出。
                        String mNaviPkgName = query_pkg_name(getMainContext());
                        if (!TextUtils.isEmpty(mNaviPkgName) && mNaviPkgName.equals(packetName)) {
                            LogUtils.e(TAG, "topPackageClassChanged return Navi!");
                            //修改bug17132
//                            return;
                        }
                    }

                    topPackageClassChanged(packetName, className);

//                    if (PowerManager.mFirstBoot) {
//                        BaseLogic logic = ModuleManager.getLogicBySource(Define.Source.SOURCE_SILENT);
//                        if (logic != null) {
//                            LogUtils.d(TAG, "top package getAPKPacketName="+logic.getAPKPacketName()+", packetName="+packetName+", mcusource="+McuManager.getMemorySource());
//                            if (packetName.equals(logic.getAPKPacketName())) {
//                                PowerManager.mFirstBoot = false;
//                                SourceManager.onChangeSource(McuManager.getMemorySource());
//                            }
//                        }
//                    }
                }
            } else if (ACTION_SHOW_VOLUME.equals(action)) {
                boolean state = intent.getBooleanExtra(KEY_SHOW_VOLUME, false);
                if (state) {
                    VolumeDriver.Driver().operate(Define.VolumeOperate.SHOW);
                } else {
                    VolumeDriver.Driver().operate(Define.VolumeOperate.HIDE);
                }
            } else if (ACTION_SHOW_BRIGHTNESS.equals(action)) {
                boolean state = intent.getBooleanExtra(KEY_SHOW_BRIGHTNESS, false);
                Packet packet = new Packet();
                packet.putInt("operate", state ? 1 : 0);
                packet.putInt("backlight", BacklightDriver.Driver().getBacklightness());
                BaseLogic logic = ModuleManager.getLogicByName(MainUIDefine.MODULE);
                if (null != logic) {
                    logic.dispatch(MainUIInterface.APK_TO_MAIN.BACKLIGHT_CLICK, packet);

                }
            } else if (ACTION_REBOOT_SYSTEM.equals(action)) {
                LogUtils.d(TAG, "Broadcast to reboot system");
                reboot();
            } else if (ACTION_START_LAUNCHER.equals(action)) {
                LogUtils.d(TAG, "action_start_launcher");
                /*-begin-20180417-ydinggen-modify-名称为persist断电会有保存，解决在没收到广播时马上断ARM电，导致下次起来时直接切源-*/
                SystemProperties.set("user.launcher.start", "false");
                /*-end-20180417-ydinggen-modify-名称为persist断电会有保存，解决在没收到广播时马上断ARM电，导致下次起来时直接切源-*/
                if (PowerManager.mFirstBoot) {
                    PowerManager.mFirstBoot = false;
                    boolean result = false;
                    /*if (SourceManager.getCurSource() == Define.Source.SOURCE_CAMERA) {
                        LogUtils.e(TAG, "onChangeSource source=" + McuManager.getMemorySource() + " when is Camera");
                        CameraLogic.setEnterCameraSource(McuManager.getMemorySource());
                        SourceManager.onOpenBackgroundSource(McuManager.getMemorySource());
                        result = true;
                    } else */if (EventInputManager.getCamera()) {
                        LogUtils.e(TAG, "onChangeSource 1 source=" + McuManager.getMemorySource() + " when is Camera");
                        CameraLogic.setEnterCameraSource(McuManager.getMemorySource(), false);
                        result = true;
                    } else {
                        result = SourceManager.onChangeSource(McuManager.getMemorySource());
                    }

                    if (!result) {
                        LogUtils.e(TAG, "Power on##" + Define.Source.toString(McuManager.getMemorySource()) + " change failed, SourceManager default handle.");
                        SourceManager.firstBoot();
                    }
                }
            } else if (ACTION_MCU_ROTATE_SCREEN.equals(action)) {
                McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.OP_ROTATE_SCREEN, new byte[]{0x01}, 1);

                //旋转屏幕关掉语音。解决bug16594。
                Packet packet1 = new Packet();
                packet1.putBoolean("open", false);
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.KEY_TRIGGER_VOICE, packet1);
            } else if (ACTION_SWITCH_CAMERA.equals(action)) {
//                String value = intent.getStringExtra("camera_id");//0：后; 1：前
//                LogUtils.d("ACTION_SWITCH_CAMERA----value=" + value);
//                if (value.equals("0")) {
//                    //后摄像头
//                    McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_CAMERA360_SWITCH, new byte[]{0x00}, 1);
//                } else if (value.equals("1")) {
//                    //前摄像头
//                    McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_CAMERA360_SWITCH, new byte[]{0x01}, 1);
//                }
                if (!FactoryDriver.Driver().getCameraPower()) {
                    boolean status = intent.getBooleanExtra("camera_open", false);
                    LogUtils.d(TAG, "ACTION_SWITCH_CAMERA---status=" + status);
                    if (status) {
                        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_CARMERA_POWER, new byte[]{0x01}, 1);
                    } else {
                        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_CARMERA_POWER, new byte[]{0x00}, 1);
                    }
                } else {
                    LogUtils.e(TAG, "ACTION_SWITCH_CAMERA---power open");
                }
            }
        }
    };

    // 广播监听
    private BroadcastReceiver mKeyBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "system mKeyBroadcastReceiver receiver action = " + action);
           if (SIMULATE_KEY.equals(action)) {
                // 模拟key
                Bundle bundle = intent.getExtras();
                if (null != bundle) {
                    int key = bundle.getInt("key", -1);
                    int mainKey = getMainHandleKey(key);
                    if (mainKey != Define.Key.KEY_NONE) {
                        EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.OS, mainKey, null);
                    }
                }
            } else if (TOUCH_EVENT.equals(action)) {
                // 发送按键音
                EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_BEEP, true, null);
            }
        }
    };




    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        mFirstRunLauncher = true;
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);
        DriverManager.getDriverByName(VolumeDriver.DRIVER_NAME).getModel().bindListener(mVolumeListener);
        PowerManager.getModel().bindListener(mPowerListener);
        SourceManager.getModel().bindListener(mSourceListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);
    }

    @Override
    public void onDestroy() {
        mFirstRunLauncher = false;
        mIsPortProject = false;
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().unbindListener(mAccoffListener);
        SourceManager.getModel().unbindListener(mSourceListener);
        PowerManager.getModel().unbindListener(mPowerListener);
        DriverManager.getDriverByName(VolumeDriver.DRIVER_NAME).getModel().unbindListener(mVolumeListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);

        super.onDestroy();
    }

    @Override
    public boolean reboot() {
        byte[] data = new byte[1];
        data[0] = (byte) 0x03;
        McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
        return super.reboot();
    }

    @Override
    public boolean restoreFactorySettings() {
        deleteCustomConfig();
        byte[] data = new byte[1];
        data[0] = (byte) 0x01;
        McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
        return super.restoreFactorySettings();
    }

    private final String resetDelList = "/system/etc/factoryResetDelList.xml";
    private final String CONFIG_DIR = "/custom/";

    private void deleteCustomConfig() {
        try {
            List<String> list = XmlUtil.getXmlList(new FileInputStream(resetDelList), "FileName");
            for (String path : list) {
                FileUtil.deleteFromName(CONFIG_DIR + path);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean wipeUserData() {
        byte[] data = new byte[1];
        data[0] = (byte) 0x00;
        McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
        return super.wipeUserData();
    }

    @Override
    public boolean wipeCache() {
        byte[] data = new byte[1];
        data[0] = (byte) 0x00;
        McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
        return super.wipeUserData();
    }

    /// HuangZeming add HOME BACK key for bug6404 - 20170407
    final int _KEYCODE_HOME = 19;//3;//HOME和BACK由Main接管，按面板按键系统改发19和20。2017-05-23 YDG
    final int _KEYCODE_BACK = 20;//4;
//    final int _KEYCODE_NAVI = 21;
//    final int _KEYCODE_VOLUME_UP = 24;
//    final int _KEYCODE_VOLUME_DOWN = 25;
//    final int _KEYCODE_POWER = 26;
    private int getMainHandleKey(int key){
        int keyCode = Define.Key.KEY_NONE;
        switch(key){
//            case _KEYCODE_NAVI:
//                keyCode = Define.Key.KEY_NAVI;
//                break;
//            case _KEYCODE_VOLUME_UP:
//                keyCode = Define.Key.KEY_VOL_INC;
//                break;
//            case _KEYCODE_VOLUME_DOWN:
//                keyCode = Define.Key.KEY_VOL_DEC;
//                break;
//            case _KEYCODE_POWER:
//                keyCode = Define.Key.KEY_POWER;
//                break;
            case _KEYCODE_HOME:
                keyCode = Define.Key.KEY_HOME;
                break;
            case _KEYCODE_BACK:
                keyCode = Define.Key.KEY_BACK;
                break;
            default:
                break;
        }

        return keyCode;
    }

    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            if (!AccoffStep.isDeepSleep(oldVal) && AccoffStep.isDeepSleep(newVal) ||
                    AccoffStep.isDeepSleep(oldVal) && !AccoffStep.isDeepSleep(newVal)) {
                mFirstRunLauncher = true;
            }
        }
    };

    private String query_pkg_name(Context context) {
        String ret = null;
        try {
            BaseDriver driver = ModuleManager.getLogicByName(NaviDefine.MODULE).getDriver();
            if (driver instanceof NaviDriverable) {
                NaviDriverable naviDriverable = (NaviDriverable) driver;
                ret = naviDriverable.getNavigationPacketName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private Handler mHandler;
    boolean bNaviFore = false;
    private class SystemHandler extends Handler {
        public SystemHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mHandler.removeMessages(1);

                LogUtils.e(TAG, "handleMessage---bNaviFore=" + bNaviFore);
                if (bNaviFore) {
                    bNaviFore = false;

                    if (SourceManager.getCurSource() != Define.Source.SOURCE_NAVI) {
                        SourceManager.onChangeSource(Define.Source.SOURCE_NAVI);
                    } else {
                        SourceManager.onPopSourceNoPoweroff(Define.Source.SOURCE_NAVI);
                    }
                } else {
                    bNaviFore = true;
                    mHandler.sendEmptyMessageDelayed(1, 200);

                    CommonLogic.KeyInstSystem(KeyEvent.KEYCODE_HOME);
                }
            }
        }
    }
}
