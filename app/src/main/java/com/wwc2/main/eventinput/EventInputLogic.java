package com.wwc2.main.eventinput;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.Provider;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.canbus.driver.CanBusDriver;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.eventinput.driver.EventInputDriverable;
import com.wwc2.main.eventinput.driver.STM32EventInputDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PanoramicManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.provider.LogicProvider;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * the event input logic.
 *
 * @author wwc2
 * @date 2017/1/25
 */
public class EventInputLogic extends BaseLogic {

    /**
     * TAG
     */
    private static final String TAG = "EventInputListener";

    /**
     * lock object.
     */
    private static final Lock mLock = new ReentrantLock();

    @Override
    public String getTypeName() {
        return "EventInput";
    }

    @Override
    public String getMessageType() {
        return EventInputDefine.MODULE;
    }

    @Override
    public BaseDriver newDriver() {
        return new STM32EventInputDriver();
    }

    /**CAMERA message.*/
    private static final String CAMERA = "com.android.wwc2.camera";

    /**
     * the driver interface.
     */
    protected EventInputDriverable Driver() {
        EventInputDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof EventInputDriverable) {
            ret = (EventInputDriverable) driver;
        }
        return ret;
    }

    /**
     * 自身监听器
     */
    private EventInputListener mListener = new EventInputListener() {
        @Override
        public void BrakeListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "BrakeListener, oldVal = " + oldVal + ", newVal = " + newVal);
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_BRAKE, newVal, null);
        }

        @Override
        public void AccListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "AccListener, oldVal = " + oldVal + ", newVal = " + newVal);
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_ACC, newVal, null);

            //由于休眠唤醒时切源导致此监听与MCU的状态不同步。20190617
//            Uri uri_acc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.ACC_STATUS);
//            getMainContext().getContentResolver().notifyChange(uri_acc, null);

            PanoramicManager.getInstance().openPanoramic(false, true);

            if (ApkUtils.isAPKExist(getMainContext(), "com.baony.avm360")) {
                if (newVal) {
//                Avm360Manager.openCameraBack(getMainContext());
                } else {
//                Avm360Manager.closeBackCamera();
                }
            }
        }

        @Override
        public void CameraListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "CameraListener, oldVal = " + oldVal + ", newVal = " + newVal);
            //倒车时退出语音
            if (newVal) {
                Packet packet1 = new Packet();
                packet1.putBoolean("open", false);
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.KEY_TRIGGER_VOICE, packet1);
            }

            //aux应用接收此广播，处理aux和倒车交互
            if (!CanBusDriver.getPanoramicView()) {
                Uri uri = Uri.parse("content://" + Provider.AUTHORITY + "/" + LogicProvider.CAMERA_STATUS);
                getMainContext().getContentResolver().notifyChange(uri, null);

                Intent intent = new Intent(CAMERA);
                intent.putExtra("camera", newVal);
                getMainContext().sendBroadcast(intent);
                EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_CAMERA, newVal, null);
            }
        }

        @Override
        public void IllListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "IllListener, oldVal = " + oldVal + ", newVal = " + newVal);
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_ILL, newVal, null);
        }

        @Override
        public void LeftLightListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "LeftLightListener, oldVal = " + oldVal + ", newVal = " + newVal);
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_LEFT_LIGHT, newVal, null);
        }

        @Override
        public void RightLightListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "RightLightListener, oldVal = " + oldVal + ", newVal = " + newVal);
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_RIGHT_LIGHT, newVal, null);
        }

        @Override
        public void TurnLightListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "TurnLightListener, oldVal = " + oldVal + ", newVal = " + newVal);

            if (newVal == 1) {//左转
                boolean sw = CommonDriver.Driver().getTurnLightSwitch(1);
                if (!sw) return;//开关关闭时不处理左转
            } else if (newVal == 2) {//右转
                boolean sw = CommonDriver.Driver().getTurnLightSwitch(2);
                if (!sw) return;//开关关闭时不处理右转
            }
            Uri uri = Uri.parse("content://" + Provider.AUTHORITY + "/" + LogicProvider.TURN_LIGHT);
            getMainContext().getContentResolver().notifyChange(uri, null);
        }
    };

    /**
     * 延时触发按键
     */
    private static final int DELAY_HANGDLE_KEY = 1000;
    /**
     * 延时对象
     */
    private static TimerQueue mDelayHandleKeyTimerExQueue = new TimerQueue();

    /**
     * 获取延时触发按键列表
     */
    private static List<Integer> getDelayHandleKeyList() {
        List<Integer> listDelayHandleKey = new ArrayList<Integer>();

        /*HOME和BACK由Main接管，不作延时处理，否则会出现1s内再按没有作用。2017-05-23
        listDelayHandleKey.add(Define.Key.KEY_HOME);
        listDelayHandleKey.add(Define.Key.KEY_BACK);*/
        listDelayHandleKey.add(Define.Key.KEY_POWER);
        listDelayHandleKey.add(Define.Key.KEY_MODE);
        listDelayHandleKey.add(Define.Key.KEY_BAND);
        listDelayHandleKey.add(Define.Key.KEY_NAVI);
        listDelayHandleKey.add(Define.Key.KEY_EQ);
        listDelayHandleKey.add(Define.Key.KEY_RADIO);
        listDelayHandleKey.add(Define.Key.KEY_BLUETOOTH);
        listDelayHandleKey.add(Define.Key.KEY_DVD);
        listDelayHandleKey.add(Define.Key.KEY_AUDIO);
        listDelayHandleKey.add(Define.Key.KEY_VIDEO);
        listDelayHandleKey.add(Define.Key.KEY_PHOTO);
        listDelayHandleKey.add(Define.Key.KEY_CARD);
        listDelayHandleKey.add(Define.Key.KEY_USB);
        listDelayHandleKey.add(Define.Key.KEY_IPOD);
        listDelayHandleKey.add(Define.Key.KEY_AUX);
        listDelayHandleKey.add(Define.Key.KEY_SETTINGS);
        listDelayHandleKey.add(Define.Key.KEY_TV);
        listDelayHandleKey.add(Define.Key.KEY_CAN);
        listDelayHandleKey.add(Define.Key.KEY_FCAM);
        listDelayHandleKey.add(Define.Key.KEY_DVR);
        listDelayHandleKey.add(Define.Key.KEY_PHONELINK);
        listDelayHandleKey.add(Define.Key.KEY_STANDBY);

        return listDelayHandleKey;
    }

    /**
     * 是否为延时按键
     */
    private static boolean isDelayHandleKey(int key) {
        boolean ret = false;
        List<Integer> listDelayHandleKey = getDelayHandleKeyList();
        for (int i = 0; i < listDelayHandleKey.size(); i++) {
            if (key == listDelayHandleKey.get(i)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onCreate(null);
        }

        getModel().bindListener(mListener);
    }

    @Override
    public void onDestroy() {
        getModel().unbindListener(mListener);

        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public Packet onModule(int nId, Packet packet) {
        Packet ret = null;

        mLock.lock();
        try {
            ret = super.onModule(nId, packet);
            if (null == ret) {
                switch (nId) {
                    case EventInputDefine.EVENT_INPUT_MODULE:
                        ret = handleModuleEvent(packet);
                        break;
                    case EventInputDefine.EVENT_INPUT_KEY:
                        ret = handleKeyEvent(packet);
                        break;
                    case EventInputDefine.EVENT_INPUT_STATUS:
                        ret = handleStatusEvent(packet);
                        break;
                    default:
                        break;
                }
            }
        } finally {
            mLock.unlock();
        }

        return ret;
    }

    @Override
    public Packet onModuleEvent(int id, Packet packet) {
        Packet ret = super.onModuleEvent(id, packet);
        switch (id) {
            case EventInputDefine.MODULE_ID_MCU_ERROR:
                if (null != packet) {
                    int error = packet.getInt("error", -1);
                    if (-1 != error) {
                        LogUtils.e("MCU-->ARM communication error, error code:" + error);
                        if (McuDriverable.ERROR_RECV_DATA_TIMEOUT == error) {
                            Toast.makeText(getMainContext(), "MCU数据接收超时！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getMainContext(), "MCU串口通信错误！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case EventInputDefine.MODULE_ID_SOURCE_CHANGE:
                if (null != packet) {
                    int source = packet.getInt("source", -1);
                    if (-1 != source) {
                        SourceManager.onChangeSource(source);
                    }
                }
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * 处理模块事件
     */
    protected Packet handleModuleEvent(Packet packet) {
        Packet ret = null;
        if (null != packet) {
            int id = packet.getInt("id", -1);
            if (-1 != id) {
                String logicClassName = packet.getString("logicClassName");
                if (null != logicClassName) {
                    BaseLogic logic = ModuleManager.getLogicByName(logicClassName);
                    if (null != logic) {
                        ret = logic.onModuleEvent(id, packet);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 处理按键事件
     */
    protected Packet handleKeyEvent(Packet packet) {
        Packet ret = null;
        if (null != packet) {
            int key = packet.getInt("key", -1);
            int keyOrigin = packet.getInt("keyOrigin", Define.KeyOrigin.DEFAULT);
            if (-1 != key) {
                boolean handle = false;
                BaseLogic logic = null;
                // 按键入口 延时处理部分按键
                if (isDelayHandleKey(key)) {
                    if (mDelayHandleKeyTimerExQueue.isOver()) {
                        mDelayHandleKeyTimerExQueue.add(DELAY_HANGDLE_KEY, null, null);
                        mDelayHandleKeyTimerExQueue.start();
                    } else {
                        String string = "lost the key[" + Define.Key.toString(key) + "],please wait(" + DELAY_HANGDLE_KEY + "ms) the key handle over.";
                        LogUtils.d(TAG, string);
                        return ret;
                    }
                }

                // 发送按键音
                if (Define.KeyOrigin.DEFAULT != keyOrigin) {
                    EventInputManager.NotifyStatusEvent(true, EventInputDefine.Status.TYPE_BEEP, true, null);
                }

                if (Define.KeyOrigin.OS == keyOrigin) {//解决关屏状态下按左边按键不开屏。2020-03-16
                    Driver driver = DriverManager.getDriverByName(BacklightDriver.DRIVER_NAME);
                    if (null != driver) {
                        Packet packet1 = driver.getInfo();
                        if (null != packet1) {
                            final boolean open = packet1.getBoolean("BacklightOpenOrClose", false);
                            if (!open ) {
                                BaseLogic newLogic = ModuleManager.getLogicBySource(SourceManager.getCurSource());
                                if (null != newLogic) {
                                    if (!newLogic.isPoweroffSource()) {
                                        BacklightDriver.Driver().open();
                                        return ret;
                                    }
                                }
                            }
                        }
                    }
                }

                // 按键监听器
                List<BaseListener> list = getModel().getListeners();
                if (null != list) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) instanceof EventInputListener) {
                            EventInputListener temp = (EventInputListener) list.get(i);
                            if (null != temp) {
                                temp.KeyListener(key, keyOrigin);
                            }
                        }
                    }
                }

                // 按键开背光
                //zhongyang.hu add for remove the KEY_SCREEN_ONOFF key
                /*
                if (Define.Key.KEY_SCREEN_ONOFF != key && Define.Key.KEY_SCREENOFF != key) {
                    BacklightDriverable driverable = BacklightDriver.Driver();
                    if (null != driverable) {
                        final boolean status = driverable.open();
                        if (status) {
                            // 开背光成功，则屏蔽该按键功能
                            handle = true;
                        }
                    }
                }
                 */
                // 按键转换部分

                // 给前台处理
                if (!handle) {
                    logic = ModuleManager.getFrontLogic();
                    if (null != logic) {
                        handle = logic.onKeyEvent(keyOrigin, key, packet);
                    }
                }

                // 给后台处理
                if (!handle) {
                    logic = ModuleManager.getBackLogic();
                    if (null != logic) {
                        handle = logic.onKeyEvent(keyOrigin, key, packet);
                    }
                }

                // 默认处理
                if (!handle) {
                    logic = ModuleManager.getCommonLogic();
                    if (null != logic) {
                        handle = logic.onKeyEvent(keyOrigin, key, packet);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 处理状态事件
     */
    protected Packet handleStatusEvent(Packet packet) {
        Packet ret = null;
        if (null != packet) {
            int type = packet.getInt("type", -1);
            if (-1 != type) {
                boolean status = packet.getBoolean("status");
                boolean handle = false;
                BaseLogic logic = null;

                // 给前台处理
                if (!handle) {
                    logic = ModuleManager.getFrontLogic();
                    if (null != logic) {
                        handle = logic.onStatusEvent(type, status, packet);
                    }
                }

                // 给后台处理
                if (!handle) {
                    logic = ModuleManager.getBackLogic();
                    if (null != logic) {
                        handle = logic.onStatusEvent(type, status, packet);
                    }
                }

                // 默认处理
                if (!handle) {
                    logic = ModuleManager.getCommonLogic();
                    if (null != logic) {
                        handle = logic.onStatusEvent(type, status, packet);
                    }
                }
            }
        }
        return ret;
    }
}
