package com.wwc2.main.accoff.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.DspManager;
import com.wwc2.main.manager.GpsDataManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.TaxiManager;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

/**
 * the base system delay sleep memory acc off driver.
 *
 * @author wwc2
 * @date 2017/1/21
 */
public abstract class BaseSystemDelaySleepMemoryAccoffDriver extends BaseDelaySleepMemoryAccoffDriver {

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean gotoDeepSleepSystem() {
        // 进入系统深度休眠
        if (mWakeupRealTime) {
            LogUtils.d(TAG, "goToDeepSleep##interrupt, do not execute [framework]gotoDeepSleep any more.");
        } else {
            LogUtils.d(TAG, "goToDeepSleep##[framework]kill process, enter fly mode, and system deep sleep start.");
            Packet packet = new Packet();
            if (null != mKeepProcessPackageFilter) {
                final int size = mKeepProcessPackageFilter.size();
                if (size > 0) {
                    String[] packages = new String[size];
                    for (int i = 0;i < size;i++) {
                        packages[i] = mKeepProcessPackageFilter.get(i);
                    }
                    packet.putStringArray("KeepPackages", packages);
                }
            }

            BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
            if (null != logic) {
                logic.Notify(SystemPermissionInterface.MAIN_TO_APK.GOTO_DEEP_SLEEP, packet);
            }
            LogUtils.d(TAG, "goToDeepSleep##[framework]kill process, enter fly mode, and system deep sleep over.");
        }

        return true;
    }

    @Override
    protected boolean wakeupFromDeepSleepSystem() {
        LogUtils.d(TAG, "wakeupFromDeepSleep##[framework]close fly mode and wakeup system start.");
        BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
        if (null != logic) {
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.WAKEUP_FROM_DEEP_SLEEP, null);
        }
        LogUtils.d(TAG, "wakeupFromDeepSleep##[framework]close fly mode and wakeup system over.");

        return true;
    }

    @Override
    protected boolean goToDeepSleep() {
        LogUtils.d(TAG, "goToDeepSleep...");

        // 休眠操作
        deepSleepAction();

        gotoDeepSleepSystem();

        return true;
    }

    @Override
    protected boolean wakeupFromDeepSleep() {
        LogUtils.d(TAG, "wakeupFromDeepSleep...");

        // 从系统深度休眠唤醒
        wakeupFromDeepSleepSystem();

        // 唤醒操作
        wakeupAction();

        return true;
    }

    @Override
    protected boolean wakeupSystemFromDeepSleepAccOff() {
        LogUtils.d(TAG, "wakeupSystemFromDeepSleepAccOff...");

        // 从系统深度休眠唤醒
        wakeupFromDeepSleepSystem();
        return true;
    }

    /**休眠操作*/
    private void deepSleepAction() {
        mDeepSleepAction  = true;

        //关闭MCU串口
        if (PowerManager.isPortProject() && !PowerManager.isRuiPai_SP()) {
            DspManager.getInstance().onDestroy();
        }
        if (PowerManager.isRuiPai()) {
            GpsDataManager.getInstance().onDestroy();
        }
        if (PowerManager.isTaxiClient()) {
            TaxiManager.getInstance().onDestroy();
        }
        LogUtils.d(TAG, "deepSleepAction##close mcu port start.");
        McuManager.onDestroy();
        LogUtils.d(TAG, "deepSleepAction##close mcu port over.");
        //销毁部分逻辑对象
        LogUtils.d(TAG, "deepSleepAction##destroy logics start.");
        ModuleManager.onDestroyLogics(mWithoutHasslesLogics);
        LogUtils.d(TAG, "deepSleepAction##destroy logics over.");
        //销毁部分驱动对象
        LogUtils.d(TAG, "deepSleepAction##destroy drivers start.");
        ModuleManager.onDestroyDrivers(mWithoutHasslesDrivers);
        LogUtils.d(TAG, "deepSleepAction##destroy drivers over.");
    }

    /**唤醒操作*/
    private void wakeupAction() {
        if (mDeepSleepAction) {
            Packet packet = new Packet();
            //创建部分驱动对象
            LogUtils.d(TAG, "wakeupAction##create drivers start.");
            ModuleManager.onCreateDrivers(packet, mWithoutHasslesDrivers);
            LogUtils.d(TAG, "wakeupAction##create drivers over.");
            //创建部分逻辑对象
            LogUtils.d(TAG, "wakeupAction##create logics start.");
            ModuleManager.onCreateLogics(packet, mWithoutHasslesLogics);
            LogUtils.d(TAG, "wakeupAction##create logics over.");
            //打开串口
            LogUtils.d(TAG, "wakeupAction##open mcu port start.");
            McuManager.onCreate(null);
            LogUtils.d(TAG, "wakeupAction##open mcu port over.");
            if (PowerManager.isPortProject() && !PowerManager.isRuiPai_SP()) {
                DspManager.getInstance().onCreate(null);
            }
            if (PowerManager.isRuiPai()) {
                GpsDataManager.getInstance().onCreate(null);
            }
            if (PowerManager.isTaxiClient()) {
                TaxiManager.getInstance().onCreate(null);
            }

            mDeepSleepAction = false;
        } else {
            LogUtils.e("wakeupAction return mDeepSleepAction = false!");
        }
    }
}
