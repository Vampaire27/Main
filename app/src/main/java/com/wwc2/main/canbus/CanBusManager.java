package com.wwc2.main.canbus;

import android.text.TextUtils;

import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.sdk.BaseSDKMemoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * the can bus manager.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public class CanBusManager {

    private static final String TAG = "CanBusManager";

    /**
     * the can bus logics.
     */
    private static List<String> mCanbusLogics = new ArrayList<>();

    /**register the logics.*/
    static {
        mCanbusLogics.add(CanBusDefine.MODULE);
//        mCanbusLogics.add(MainDefine.MODULE);
    }

    /**
     * register the logic.注册CanbusLogic
     */
    public static void register() {
        LogUtils.i(TAG, "register()");
        if (null != mCanbusLogics) {
            for (String string : mCanbusLogics) {
                if (!TextUtils.isEmpty(string)) {
                    LogicManager.regLogic(string);
                }
            }
        }
    }

    /**
     * connect sdk.启动Canbus服务
     */
    public static boolean connectSDK() {
        LogUtils.i(TAG, "connectSDK()");
        boolean ret = false;
        BaseSDKMemoryDriver driver = getDriver();
        if (null != driver) {
            BaseLogic logic = ModuleManager.getLogicByName(CanBusDefine.MODULE);
            if (null != logic) {
                Packet packet = logic.getInfo();
                ret = driver.connectSDK(CanBusDefine.CANBUS_SERVICE_NAME,
                        CanBusDefine.CANBUS_SERVICE_PACKET_NAME,
                        CanBusDefine.CANBUS_SERVICE_CLASS_NAME, packet);
            }
        }
        return ret;
    }

    /**
     * disconnect sdk.断开Canbus服务
     */
    public static boolean disconnectSDK() {
        boolean ret = false;
        BaseSDKMemoryDriver driver = getDriver();
        if (null != driver) {
            ret = driver.disconnectSDK();
        }
        return ret;
    }

    /**
     * get driver.
     */
    protected static BaseSDKMemoryDriver getDriver() {
        LogUtils.i(TAG, "getDriver()");
        BaseSDKMemoryDriver ret = null;
        BaseLogic logic = ModuleManager.getLogicByName(CanBusDefine.MODULE);
        if (null != logic) {
            BaseDriver driver = logic.getDriver();
            if (driver instanceof BaseSDKMemoryDriver) {
                ret = (BaseSDKMemoryDriver) driver;
            }
        }
        return ret;
    }
}
