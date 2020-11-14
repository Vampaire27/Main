package com.wwc2.main.common;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.avin_interface.AvinInterface;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.bluetooth_interface.BluetoothInterface;
import com.wwc2.camera_interface.CameraInterface;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.Interface;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.bluetooth.EcarHelper;
import com.wwc2.main.driver.audio.driver.BaseAudioDriver;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.eq.EQDriver;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PanoramicManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.manager.VolumeManager;
import com.wwc2.main.phonelink.PhonelinkLogic;
import com.wwc2.main.settings.util.ClickFilter;
import com.wwc2.main.settings.util.ToastUtil;
import com.wwc2.main.upgrade.mcu.McuUpdateLogic;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.radio_interface.RadioInterface;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * the common logic.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public class CommonLogic extends BaseLogic {

    private static final String TAG = "CommonLogic";

    private TimerQueue mPowerKeyTimerQueue = new TimerQueue();


    private final int MES_LOUD = 1001;

    @Override
    public String getTypeName() {
        return "Common";
    }

    @Override
    public String getMessageType() {
        return Define.MODULE;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_NONE;
    }

    @Override
    public List<String> getAPKPacketList() {
        // 主服务不做任何动作的白名单
        List<String> list = new ArrayList<String>();
        list.add("com.android.systemui");
        list.add("android");
        list.add("com.android.inputmethod.pinyin");
        list.add("com.wwc2.black");
        list.add("com.android.stk");
        return list;
    }

    // 当前模块监听模式的变化
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt("CurSource", newVal);
            Notify(Interface.MAIN_TO_APK.CUR_SOURCE, packet);
        }

        @Override
        public void OldSourceListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt("OldSource", newVal);
            Notify(Interface.MAIN_TO_APK.OLD_SOURCE, packet);
        }

        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt("CurBackSource", newVal);
            Notify(Interface.MAIN_TO_APK.CUR_BACK_SOURCE, packet);
        }

        @Override
        public void OldBackSourceListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt("OldBackSource", newVal);
            Notify(Interface.MAIN_TO_APK.OLD_BACK_SOURCE, packet);
        }

        @Override
        public void CurPkgNameListener(String oldVal, String newVal) {
            Packet packet = new Packet();
            packet.putString("CurPkgName", newVal);
            Notify(Interface.MAIN_TO_APK.CUR_PKG_NAME, packet);
        }

        @Override
        public void OldPkgNameListener(String oldVal, String newVal) {
            Packet packet = new Packet();
            packet.putString("OldPkgName", newVal);
            Notify(Interface.MAIN_TO_APK.OLD_PKG_NAME, packet);
        }

        @Override
        public void CurClsNameListener(String oldVal, String newVal) {
            Packet packet = new Packet();
            packet.putString("CurClsName", newVal);
            Notify(Interface.MAIN_TO_APK.CUR_CLS_NAME, packet);
        }

        @Override
        public void OldClsNameListener(String oldVal, String newVal) {
            Packet packet = new Packet();
            packet.putString("OldClsName", newVal);
            Notify(Interface.MAIN_TO_APK.OLD_CLS_NAME, packet);
        }
    };

    @Override
    public Packet getInfo() {
        Packet ret = super.getInfo();
        if (null == ret) {
            ret = new Packet();
        }
        ret.putInt("CurSource", SourceManager.getCurSource());
        ret.putInt("OldSource", SourceManager.getOldSource());
        ret.putInt("CurBackSource", SourceManager.getCurBackSource());
        ret.putInt("OldBackSource", SourceManager.getOldBackSource());
        ret.putString("CurPkgName", SourceManager.getCurPkgName());
        ret.putString("OldPkgName", SourceManager.getOldPkgName());
        ret.putString("CurClsName", SourceManager.getCurClsName());
        ret.putString("OldClsName", SourceManager.getOldClsName());
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        // 获取源管理的Model对象
        BaseModel model = SourceManager.getModel();
        // 绑定源管理监听器
        if (null != model) {
            model.bindListener(mSourceListener);
        }
    }

    @Override
    public void onDestroy() {
        // 获取源管理的Model对象
        BaseModel model = SourceManager.getModel();
        if (null != model) {
            model.unbindListener(mSourceListener);
        }

        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        if (null == ret) {
            switch (nId) {
                case Interface.APK_TO_MAIN.CHANGE_SOURCE:
                    if (null != packet) {
                        int source = packet.getInt("source", -1);
                        if (-1 != source) {
                            LogUtils.d(TAG, "onChangeSource source = " + Define.Source.toString(source));
                            SourceManager.onChangeSource(source);
                        }
                    }
                    break;
                case Interface.APK_TO_MAIN.SIMULATE_KEY:
                    if (null != packet) {
                        int key = packet.getInt("key", -1);
                        if (-1 != key) {
                            LogUtils.d(TAG, "NotifyKeyEvent key = " + Define.Key.toString(key));
                            EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.SOFTWARE, key, packet);
                        }
                    }
                    break;
                case Interface.APK_TO_MAIN.EXIT_APP:
                    if (null != packet) {
                        String pkgName = packet.getString("pkgName");
                        if (!TextUtils.isEmpty(pkgName)) {
                            LogUtils.d(TAG, "onExitPackage pkgName = " + pkgName);
                            SourceManager.onExitPackage(pkgName);
                        }
                    }
                    break;
                case Interface.APK_TO_MAIN.GET_INFO:
                    if (null != packet) {
                        final String type = packet.getString("type", null);
                        final int source = packet.getInt("source", Define.Source.SOURCE_NONE);
                        final String pkgName = packet.getString("pkgName", null);
                        BaseLogic logic = null;
                        if (!TextUtils.isEmpty(type)) {
                            logic = ModuleManager.getLogicByName(type);
                        }
                        if (null == logic) {
                            if (Define.Source.SOURCE_NONE != source) {
                                logic = ModuleManager.getLogicBySource(source);
                            }
                        }
                        if (null == logic) {
                            if (!TextUtils.isEmpty(pkgName)) {
                                logic = ModuleManager.getLogicByPacketName(pkgName);
                            }
                        }
                        if (null != logic) {
                            ret = new Packet();
                            final String _type = logic.getMessageType();
                            if (!TextUtils.isEmpty(type)) {
                                ret.putString("type", _type);
                            }
                            final int _source = logic.source();
                            if (Define.Source.SOURCE_NONE != _source) {
                                ret.putInt("source", _source);
                            }
                            final String _pkgName = logic.getAPKPacketName();
                            if (!TextUtils.isEmpty(_pkgName)) {
                                ret.putString("pkgName", _pkgName);
                            }
                            final boolean _isSource = logic.isSource();
                            ret.putBoolean("isSource", _isSource);
                        }
                    }
                    break;
                case Interface.APK_TO_MAIN.OPEN_BACK_SOURCE:
                    if (null != packet) {
                        int source = packet.getInt("source");
                        SourceManager.onOpenBackgroundSource(source);
                    }
                    break;
                case CameraInterface.APK_TO_MAIN.TOUCH_RECT:
                    int action = packet.getInt("TOUCH_STATUS", 0);
                    int x = packet.getInt("TOUCH_X", 0);
                    int y = packet.getInt("TOUCH_Y", 0);
//                    PanoramicManager.getInstance().sendTouchXY(getMainContext(), x, y);
                    LogUtils.d(TAG, "TOUCH---0--x=" + x + ", y=" + y);
                    PanoramicManager.getInstance().sendTouchXY(action, x, y);
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    @Override
    public Packet onModuleEvent(int id, Packet packet) {
        return super.onModuleEvent(id, packet);
    }

    private static Thread mThread = null;
    private static int CurrentKey = KeyEvent.KEYCODE_UNKNOWN ;
    public static void KeyInstSystem(int keyValue){
        CurrentKey= keyValue;
        if (mThread == null) {
            mThread= new Thread() {
                public void run() {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(CurrentKey);
                    try {
                        Thread.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        mThread = null;
                    }
                }
            };
            mThread.start();
        }
    }

    private void sendBackKey() {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intent.putExtra("reason", "backKey");
        getMainContext().sendBroadcast(intent);
    }

    @Override
    // HuangZeming rm VR key event handle, these key event will be handle in framework - for bug6404
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;
        LogUtils.d(TAG, "Common onKeyEvent, keyOrigin = " + Define.KeyOrigin.toString(keyOrigin) +
                ", key = " + Define.Key.toString(key) + ", packet = " + packet);
        BaseLogic logic = null;
        switch (key) {
            case Define.Key.KEY_HOME:
                //VrKeyDriver.Driver().simulate(Define.Key.KEY_HOME);
                //通话中不响应home键
                if (isCalling() && !EventInputManager.getCamera() && PanoramicManager.getInstance().openPanoramic(false, false)) {
                    //解决bug14128，进360后来电，无法挂断。
                    KeyInstSystem(KeyEvent.KEYCODE_HOME);
                } else {
                    if (hanleKeyEvent()) {
                        if (!PanoramicManager.getInstance().openPanoramic(false, false)) {
                            KeyInstSystem(KeyEvent.KEYCODE_HOME);
//                        new Thread() {
//                            public void run() {
//                                Instrumentation inst = new Instrumentation();
//                                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
//                            }
//                        }.start();
                        }
                    }
                }
                break;
            case Define.Key.KEY_MENU:
                //VrKeyDriver.Driver().simulate(Define.Key.KEY_MENU);
                break;
            case Define.Key.KEY_BACK:
                //VrKeyDriver.Driver().simulate(Define.K ey.KEY_BACK);
                if (isCalling() && !EventInputManager.getCamera() && PanoramicManager.getInstance().openPanoramic(false, false)) {
                    sendBackKey();
                    KeyInstSystem(KeyEvent.KEYCODE_BACK);
                } else {
                    if (hanleKeyEvent()) {
                        if (!PanoramicManager.getInstance().openPanoramic(false, false)) {
                            sendBackKey();
                            KeyInstSystem(KeyEvent.KEYCODE_BACK);
//                        new Thread() {
//                            public void run() {
//                                Instrumentation inst = new Instrumentation();
//                                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
//                            }
//                        }.start();
                        }
                    }
                }
                break;
            case Define.Key.KEY_ANDROID_RECENT:
                //VrKeyDriver.Driver().simulate(Define.Key.KEY_ANDROID_RECENT);
                break;
            case Define.Key.KEY_VOL_INC:
                VolumeDriver.Driver().increase(1);
                VolumeDriver.Driver().operate(Define.VolumeOperate.SHOW);
                break;
            case Define.Key.KEY_VOL_DEC:
                VolumeDriver.Driver().decrease(1);
                VolumeDriver.Driver().operate(Define.VolumeOperate.SHOW);
                break;
            case Define.Key.KEY_VOL_MUTE:
                VolumeDriver.Driver().mute(!VolumeManager.getMute());
                break;
            case Define.Key.KEY_SHOW_VOLUME:
                VolumeDriver.Driver().operate(Define.VolumeOperate.SHOW);
                break;
            case Define.Key.KEY_POWER:
            case Define.Key.KEY_LONG_POWEROFF:
                if (hanleKeyEvent()) {
                    mPowerKeyTimerQueue.stop();
                    mPowerKeyTimerQueue.add(800, null, new BaseCallback() {//过滤方控按键，避免不断按POWER键出现待机时界面消失。
                        @Override
                        public void onCallback(int nId, Packet packet) {
                            if (!EventInputManager.getCamera()) {//修改按POWER键，马上倒车，会出现倒车界面退出。2019-05-08
                                SourceManager.onChangeSource(Define.Source.SOURCE_POWEROFF);
                            }
                        }
                    });
                    mPowerKeyTimerQueue.start();

                    PanoramicManager.getInstance().openPanoramic(false, false);
                }
                break;
            case Define.Key.KEY_SHORT_POWEROFF:
                if (ClickFilter.filter(100L)) {
                    break;
                }
                TimerUtils.setTimer(getMainContext(), 100, new Timerable.TimerListener() {
                    @Override
                    public void onTimer(int paramInt) {
                        VolumeDriver.Driver().mute(!VolumeManager.getMute());
                    }
                });
                break;
            case Define.Key.KEY_STANDBY:
                SourceManager.onChangeSource(Define.Source.SOURCE_STANDBY);
                break;
            case Define.Key.KEY_DIM:
                BacklightDriver.Driver().changeBacklightMode();
                break;
            case Define.Key.KEY_SCREENOFF:
                BacklightDriver.Driver().close();
                break;
            case Define.Key.KEY_SCREENON:
                BacklightDriver.Driver().open();
                break;
            case Define.Key.KEY_SCREEN_ONOFF:
                Driver driver = DriverManager.getDriverByName(BacklightDriver.DRIVER_NAME);
                if (null != driver) {
                    Packet packet1 = driver.getInfo();
                    if (null != packet1) {
                        final boolean open = packet1.getBoolean("BacklightOpenOrClose", false);
                        if (open) {
                            BacklightDriver.Driver().close();
                        } else {
                            BacklightDriver.Driver().open();
                        }
                    }
                }
                break;
            case Define.Key.KEY_MODE:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeMode();
                }
                break;
            case Define.Key.KEY_NAVI:
                //通话中不响应导航键
                if (hanleKeyEvent()) {
                    if (SourceManager.getCurSource() != Define.Source.SOURCE_NAVI) {
                        SourceManager.onChangeSource(Define.Source.SOURCE_NAVI);
                    } else {
                        SourceManager.onPopSourceNoPoweroff(Define.Source.SOURCE_NAVI);
                    }
                }
                break;
            case Define.Key.KEY_RADIO:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_RADIO);
                }
                break;
            case Define.Key.KEY_BLUETOOTH:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_BLUETOOTH);
                }
                break;
            case Define.Key.KEY_AUDIO:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_AUDIO);
                }
                break;
            case Define.Key.KEY_VIDEO:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_VIDEO);
                }
                break;
            case Define.Key.KEY_AUX:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_AUX);
                }
                break;
            case Define.Key.KEY_TV:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_TV);
                }
                break;
            case Define.Key.KEY_BLUETOOTH_MULTI_FUNCTION:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.MULTI_FUNCTION_KEY, null);
                break;
            case Define.Key.KEY_PICKUP:
                /*-begin-20180522-ydinggen-modifly-增加功能，康大提出-*/
//                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.ANSWER, null);
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.MULTI_FUNCTION_KEY, null);
                /*-end-20180522-ydinggen-modifly-增加功能，康大提出-*/
                break;
            case Define.Key.KEY_HANGUP:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.CALL_HANGUP, null);
                break;
            case Define.Key.KEY_EQ:
                EQDriver.Driver().enter();
                break;
            case Define.Key.KEY_BLUETOOTH_MUSIC:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.SWITCH_TO_MUSIC, null);
                break;
            case Define.Key.KEY_VOICE_ASSISTANT:
                Packet packet1 = new Packet();
                packet1.putBoolean("open", !BaseAudioDriver.getVoiceAssistantActive());
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.KEY_TRIGGER_VOICE, packet1);
                break;
            case Define.Key.KEY_BLUETOOTH_ON:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.BT_OPEN, null);
                break;
            case Define.Key.KEY_BLUETOOTH_OFF:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.BT_CLOSE, null);
                break;
            case Define.Key.KEY_BLUETOOTH_ONOFF:
                logic = ModuleManager.getLogicByName(BluetoothDefine.MODULE);
                if (null != logic) {
                    Packet data = logic.getInfo();
                    if (null != data) {
                        boolean open = data.getBoolean("OpenOrClose");
                        if (open) {
                            logic.dispatch(BluetoothInterface.APK_TO_MAIN.BT_CLOSE, null);
                        } else {
                            logic.dispatch(BluetoothInterface.APK_TO_MAIN.BT_OPEN, null);
                        }
                    }
                }
                break;
            case Define.Key.KEY_EXIT_CURRENT_APP:
                SourceManager.onExitPackage(SourceManager.getCurPkgName());
                break;

            case Define.Key.KEY_SETTINGS:
                SourceManager.onChangeSource(Define.Source.SOURCE_SETTINGS);
                break;

            case Define.Key.KEY_SCAN:
            case Define.Key.KEY_PS:
            case Define.Key.KEY_BAND:
            case Define.Key.KEY_AM:
            case Define.Key.KEY_FM:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_RADIO);
                }
                break;
            case Define.Key.KEY_PHONELINK:
                if (hanleKeyEvent()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_PHONELINK);
                }
                break;
            case Define.Key.KEY_AIRCONDITIONON:
//                ModuleManager.getLogicByName(AirConditionDefine.MODULE).dispatch(AirConditionInterface.APK_TO_MAIN.KEY_AIRCONDITIONON, null);
                break;
            case Define.Key.KEY_AIRCONDITIONOFF:
//                ModuleManager.getLogicByName(AirConditionDefine.MODULE).dispatch(AirConditionInterface.APK_TO_MAIN.KEY_AIRCONDITIONOFF, null);
                break;
            case Define.Key.KEY_AIRCONDITION_ONOFF:
//                ModuleManager.getLogicByName(AirConditionDefine.MODULE).dispatch(AirConditionInterface.APK_TO_MAIN.KEY_AIRCONDITION_ONOFF, null);
                break;
            case Define.Key.KEY_RADARON:
                EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_CAMERA, true, null);
//                ModuleManager.getLogicByName(RadarDefine.MODULE).dispatch(RadarInterface.APK_TO_MAIN.OPEN_RADAR, null);
                break;
            case Define.Key.KEY_RADAROFF:
                EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_CAMERA, false, null);
//                ModuleManager.getLogicByName(RadarDefine.MODULE).dispatch(RadarInterface.APK_TO_MAIN.CLOSE_RADAR, null);
                break;
            case Define.Key.KEY_RADAR_ONOFF:
                ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.RIGHT_CAMERA, null);
//                ModuleManager.getLogicByName(RadarDefine.MODULE).dispatch(RadarInterface.APK_TO_MAIN.OPEN_CLOSE_RADAR, null);
                break;
            case Define.Key.KEY_MIC_ON:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.MIC_PHONE_ON, null);
                break;
            case Define.Key.KEY_MIC_OFF:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.MIC_PHONE_OFF, null);
                break;
            case Define.Key.KEY_SWITCH_PHONE:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.SET_VOL_CHANNEL_PHONE, null);
                break;
            case Define.Key.KEY_SWITCH_DEVICE:
                ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.SET_VOL_CHANNEL_MEDCHINE, null);
                break;
            case Define.Key.KEY_ANGLE:
                getMainContext().sendBroadcast(new Intent("com.wwc2.rotate.screen"));
                break;

            case Define.Key.KEY_CONTROL_RIGHT:
                KeyInstSystem(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case Define.Key.KEY_CONTROL_LEFT:
                KeyInstSystem(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case Define.Key.KEY_CONTROL_ENTER:
                KeyInstSystem(KeyEvent.KEYCODE_ENTER);
                break;
            case Define.Key.KEY_LOUD:

                //zhonngyang.hu  add for 20190103 only need English .
                Message msg =MegHandler.obtainMessage(MES_LOUD);
                if(EQDriver.Driver().getLoudness()){
                    EQDriver.Driver().setLoudness(false);
                    msg.obj ="LOUD OFF";
                }else{
                    EQDriver.Driver().setLoudness(true);
                    msg.obj ="LOUD ON";
                }
                MegHandler.sendMessage(msg);
                //end
                break;
            case Define.Key.KEY_TA:
                ModuleManager.getLogicByName(RadioDefine.MODULE).dispatch(RadioInterface.ApkToMain.RDS_TA, null);
                break;
            case Define.Key.KEY_AF:
                ModuleManager.getLogicByName(RadioDefine.MODULE).dispatch(RadioInterface.ApkToMain.RDS_AF, null);
                break;
            case Define.Key.KEY_FRONT_CAMERA:
                Packet packet2 = new Packet();
                packet2.putBoolean("frontCamera", true);
                ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.FRONT_CAMERA, packet2);
                break;
            case Define.Key.KEY_RIGHT_CAMERA:
                Packet packet3 = new Packet();
                packet3.putBoolean("rightCamera", true);
                ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.RIGHT_CAMERA, packet3);
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }

    private boolean hanleKeyEvent() {
        boolean ret = true;
        if (isCalling() ||
                McuUpdateLogic.getMCUupdateState() ||
                !EcarHelper.getEcarRegistState() ||
                EventInputManager.getCamera() ||
                PhonelinkLogic.getCarplayPhone()) {
            ret = false;
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

        if (EcarHelper.getEcarState()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onStatusEvent(int type, boolean status, Packet packet) {
        boolean ret = true;
        switch (type) {
            case EventInputDefine.Status.TYPE_ACC:
                if (!status) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_ACCOFF);
                }
                break;
            case EventInputDefine.Status.TYPE_CAMERA:
                if (status) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_CAMERA);
                }
                break;
            case EventInputDefine.Status.TYPE_BRAKE:
                break;
            case EventInputDefine.Status.TYPE_ILL:
                BacklightDriver.Driver().setIllState(status);
                break;
            case EventInputDefine.Status.TYPE_LEFT_LIGHT:
                break;
            case EventInputDefine.Status.TYPE_RIGHT_LIGHT:
                break;
            case EventInputDefine.Status.TYPE_BEEP:
                if (status) {
                    //begin zhongyang.hu add for bug7522 20170511
                    if (!VolumeDriver.Driver().getMuteState() && (0 != VolumeDriver.Driver().getVolumeValue())) {
                        CommonDriver.Driver().beep();
                    }
                    //end
                }
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }


    private Handler MegHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MES_LOUD:
                    String value = (String) msg.obj;
                    ToastUtil.show(getMainContext(), value);
                    break;
                default:
                    break;
            }
        }
    };
}
