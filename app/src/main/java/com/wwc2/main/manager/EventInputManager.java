package com.wwc2.main.manager;

import com.wwc2.corelib.db.Packet;
import com.wwc2.main.canbus.driver.CanBusDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.logic.BaseLogic;

/**
 * 管理事件的输入，比如：模块事件、屏幕按键、面板按键、设备状态、输入状态等。
 *
 * @author wwc2
 * @date 2017/1/25
 */
public class EventInputManager {

    /**
     * 事件输入logic对象
     */
    private static BaseLogic mEventInputLogic = null;

    /**
     * 创建
     */
    public static void onCreate(Packet packet) {
        mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
    }

    /**
     * 销毁
     */
    public static void onDestroy() {
        mEventInputLogic = null;
    }

    /**
     * 指定模块发送事件接口
     *
     * @param direct         true同步方式，false异步方式
     * @param logicClassName 逻辑类名
     * @param id             模块事件ID
     * @param packet         模块数据packet
     * @return 返回packet
     */
    public static Packet NotifyModuleEvent(boolean direct, String logicClassName, int id, Packet packet) {
        Packet ret = null;
        if (null != logicClassName) {
            if (null == packet) {
                packet = new Packet();
            }
            packet.putInt("id", id);
            packet.putString("logicClassName", logicClassName);
            // 直接给EventInput处理
            if (null == mEventInputLogic) {
                mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
            }
            if (null != mEventInputLogic) {
                ret = mEventInputLogic.NotifyModule(direct, EventInputDefine.EVENT_INPUT_MODULE, packet);
            }
        }
        return ret;
    }

    /**
     * 发送按键事件接口
     *
     * @param direct    true同步方式，false异步方式
     * @param keyOrigin 按键来源, {@link com.wwc2.common_interface.Define.KeyOrigin}
     * @param key       按键值，{@link com.wwc2.common_interface.Define.Key}
     * @param packet    按键packet
     * @return 返回packet
     */
    public static Packet NotifyKeyEvent(boolean direct, int keyOrigin, int key, Packet packet) {
        Packet ret = null;
        if (null == packet) {
            packet = new Packet();
        }
        packet.putInt("keyOrigin", keyOrigin);
        packet.putInt("key", key);

        // 直接给EventInput处理
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            ret = mEventInputLogic.NotifyModule(direct, EventInputDefine.EVENT_INPUT_KEY, packet);
        }
        return ret;
    }

    /**
     * 发送状态事件接口
     *
     * @param direct true同步方式，false异步方式
     * @param type   状态类型，{@link EventInputDefine.Status}
     * @param status 状态
     * @param packet 状态packet
     * @return 返回packet
     */
    public static Packet NotifyStatusEvent(boolean direct, int type, boolean status, Packet packet) {
        Packet ret = null;
        if (null == packet) {
            packet = new Packet();
        }
        packet.putInt("type", type);
        packet.putBoolean("status", status);

        // 直接给EventInput处理
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            ret = mEventInputLogic.NotifyModule(direct, EventInputDefine.EVENT_INPUT_STATUS, packet);
        }
        return ret;
    }

    /**
     * 获取ACC状态
     */
    public static boolean getAcc() {
        boolean acc = false;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                acc = packet.getBoolean("Acc", false);
            }
        }
        return acc;
    }
    public static boolean getRealAcc() {
        boolean acc = false;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                acc = packet.getBoolean("RealAcc", false);
            }
        }
        return acc;
    }

    /**
     * 获取刹车状态
     */
    public static boolean getBrake() {
        boolean acc = false;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                acc = packet.getBoolean("Brake", false);
            }
        }

        if (CanBusDriver.getSpeedLimit() != -1) {//收到过Can数据，以速度为准。目前适用进业智车项目
            acc = (CanBusDriver.getSpeedLimit() == 0);
        }

        return acc;
    }

    /**
     * 获取倒车状态
     */
    public static boolean getCamera() {
        boolean acc = false;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                acc = packet.getBoolean("Camera", false);
            }
        }
        /*-begin-20180426-ydinggen-add-非倒车状态下，全景切入需要进入倒车界面，防止倒车apk自动退出-*/
        if (!acc) {
            acc = CanBusDriver.getPanoramicView();
        }
        /*-end-20180426-ydinggen-add-非倒车状态下，全景切入需要进入倒车界面，防止倒车apk自动退出-*/
        return acc;
    }

    /**
     * 获取大灯状态
     */
    public static boolean getIll() {
        boolean acc = false;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                acc = packet.getBoolean("Ill", false);
            }
        }
        return acc;
    }

    /**
     * 获取左转向灯状态
     */
    public static boolean getLeftLight() {
        boolean acc = false;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                acc = packet.getBoolean("LeftLight", false);
            }
        }
        return acc;
    }

    /**
     * 获取右转向灯状态
     */
    public static boolean getRightLight() {
        boolean acc = false;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                acc = packet.getBoolean("RightLight", false);
            }
        }
        return acc;
    }

    public static int getTurnLight() {
        int light = 0;
        if (null == mEventInputLogic) {
            mEventInputLogic = ModuleManager.getLogicByName(EventInputDefine.MODULE);
        }
        if (null != mEventInputLogic) {
            Packet packet = mEventInputLogic.getInfo();
            if (null != packet) {
                light = packet.getInt("TurnLight", 0);
            }
        }
        return light;
    }
}
