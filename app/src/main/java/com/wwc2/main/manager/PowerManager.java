package com.wwc2.main.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.accoff.driver.WakeupManager;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.client.driver.BaseClientDriver;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.driver.version.VersionDriverable;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.provider.LogicProviderHelper;


import java.io.File;
import java.lang.reflect.Method;



/**
 * the power manager.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class PowerManager {

    /**
     * TAG
     */
    public static final String TAG = "PowerManager";

    /**
     * boot timer.
     */
    private static final int BOOT_TIMER = 100;

    /**
     * boot time out.
     */
    private static final int BOOT_TIMEOUT = 30;

    /**
     * the main service context.
     */
    private static Context mContext = null;

    /**
     * get the main service context.
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * the power start time
     */
    private static long mPowerStartTime = 0;

    /**
     * the power time out
     */
    private static int mPowerTimeOut = 0;

    /**
     * the power on packet.
     */
    private static Packet mPoweronPacket = null;

    /**
     * the power timer id.
     */
    private static int mPowerTimerId = 0;


    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";

    public static boolean mFirstBoot = false;

    //begin  zhongyang.hu add for upload error state info.  20180333
    public static boolean feedDog = true;
    public static Object mLockDog = new Object();
    //end

    /**
     * the power step.
     */
    public static class PowerStep {
        /**
         * unknown status.
         */
        public static final int POWER_UNKNOWN = 0;

        /**
         * power on start, ready to prepare module manager.
         */
        public static final int POWER_ON_START = 1;

        /**
         * power on, module manager prepared, ready to create managers.
         */
        public static final int POWER_ON_MODULE_PREPARED = 2;

        /**
         * power on, managers created, ready to create modules.
         */
        public static final int POWER_ON_MANAGERS_CREATED = 3;

        /**
         * power on, module manager created, ready to wait some of logic and driver created.
         */
        public static final int POWER_ON_MODULE_CREATED = 4;

        /**
         * power on, some of logic and driver created, ready to create mcu manager.
         */
        public static final int POWER_ON_CREATE_OVERED = 5;

        /**
         * power on, mcu manager created, ready to wait some of manager/logic/driver first boot source.
         */
        public static final int POWER_ON_MCU_CREATED = 6;

        /**
         * power on, first boot source changed, APP power over.
         */
        public static final int POWER_ON_OVER = 7;

        /**
         * power off start, ready to destroy mcu manager.
         */
        public static final int POWER_OFF_START = 100;

        /**
         * power off, mcu manager destroyed, ready to destroy modules.
         */
        public static final int POWER_OFF_MCU_DESTROYED = 101;

        /**
         * power off, modules destroyed, ready to wait some of logic and driver destroyed.
         */
        public static final int POWER_OFF_MODULE_DESTROYED = 102;

        /**
         * power off, some of logic and driver destroyed, ready to destroy mcu manager.
         */
        public static final int POWER_OFF_MODULE_OVERED = 103;

        /**
         * power off, managers destroyed, ready to destroy config manager.
         */
        public static final int POWER_OFF_MANAGERS_DESTROYED = 104;

        /**
         * power off, config manager destroyed, APP
         */
        public static final int POWER_OFF_OVER = 106;

        /**
         * power unknown.
         */
        public static boolean isPowerUnknown(int step) {
            return (POWER_UNKNOWN == step);
        }

        /**
         * is power on start.
         */
        public static boolean isPoweronStart(int step) {
            return (POWER_ON_START == step);
        }

        /**
         * is power on module prepared.
         */
        public static boolean isPoweronModulePrepared(int step) {
            return (POWER_ON_MODULE_PREPARED == step);
        }

        /**
         * is power on managers created.
         */
        public static boolean isPoweronManagersCreated(int step) {
            return (POWER_ON_MANAGERS_CREATED == step);
        }

        /**
         * is power on module created.
         */
        public static boolean isPoweronModuleCreated(int step) {
            return (POWER_ON_MODULE_CREATED == step);
        }

        /**
         * is power on boot.
         */
        public static boolean isPoweronCreateOvered(int step) {
            return (POWER_ON_CREATE_OVERED == step);
        }

        /**
         * is power on mcu created.
         */
        public static boolean isPoweronMcuCreated(int step) {
            return (POWER_ON_MCU_CREATED == step);
        }

        /**
         * is power on over.
         */
        public static boolean isPoweronOvered(int step) {
            return (POWER_ON_OVER == step);
        }

        /**
         * is power off start.
         */
        public static boolean isPoweroffStart(int step) {
            return (POWER_OFF_START == step);
        }

        /**
         * is power off mcu destroyed.
         */
        public static boolean isPoweroffMcuDestroyed(int step) {
            return (POWER_OFF_MCU_DESTROYED == step);
        }

        /**
         * is power off module destroyed.
         */
        public static boolean isPoweroffModuleDestroyed(int step) {
            return (POWER_OFF_MODULE_DESTROYED == step);
        }

        /**
         * is power off module overed.
         */
        public static boolean isPoweroffModuleOvered(int step) {
            return (POWER_OFF_MODULE_OVERED == step);
        }

        /**
         * is power off managers destroyed.
         */
        public static boolean isPoweroffManagersDestroyed(int step) {
            return (POWER_OFF_MANAGERS_DESTROYED == step);
        }

        /**
         * is power off overed.
         */
        public static boolean isPoweroffOvered(int step) {
            return (POWER_OFF_OVER == step);
        }

        /**
         * to string.
         */
        public static String toString(int step) {
            String ret = null;
            switch (step) {
                case POWER_ON_START:
                    ret = "POWER_ON_START";
                    break;
                case POWER_ON_MODULE_PREPARED:
                    ret = "POWER_ON_MODULE_PREPARED";
                    break;
                case POWER_ON_MANAGERS_CREATED:
                    ret = "POWER_ON_MANAGERS_CREATED";
                    break;
                case POWER_ON_MODULE_CREATED:
                    ret = "POWER_ON_MODULE_CREATED";
                    break;
                case POWER_ON_CREATE_OVERED:
                    ret = "POWER_ON_CREATE_OVERED";
                    break;
                case POWER_ON_MCU_CREATED:
                    ret = "POWER_ON_MCU_CREATED";
                    break;
                case POWER_ON_OVER:
                    ret = "POWER_ON_OVER";
                    break;
                case POWER_OFF_START:
                    ret = "POWER_OFF_START";
                    break;
                case POWER_OFF_MCU_DESTROYED:
                    ret = "POWER_OFF_MCU_DESTROYED";
                    break;
                case POWER_OFF_MODULE_DESTROYED:
                    ret = "POWER_OFF_MODULE_DESTROYED";
                    break;
                case POWER_OFF_MODULE_OVERED:
                    ret = "POWER_OFF_MODULE_OVERED";
                    break;
                case POWER_OFF_MANAGERS_DESTROYED:
                    ret = "POWER_OFF_MANAGERS_DESTROYED";
                    break;
                case POWER_OFF_OVER:
                    ret = "POWER_OFF_OVER";
                    break;
                default:
                    break;
            }
            return ret;
        }
    }

    /**
     * 开机监听器
     */
    public static class PowerListener extends BaseListener {

        @Override
        public String getClassName() {
            return PowerListener.class.getName();
        }

        /**
         * 电源管理阶段变化监听器
         */
        public void PowerStepListener(Integer oldVal, Integer newVal) {

        }
    }

    /**
     * 模式Model
     */
    protected static class PowerModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putInt("PowerStep", mPowerStep.getVal());
            return packet;
        }

        /**
         * 电源管理阶段
         */
        private MInteger mPowerStep = new MInteger(this, "PowerStepListener", PowerStep.POWER_ON_START);

        public MInteger getPowerStep() {
            return mPowerStep;
        }
    }

    /**
     * Model对象
     */
    private static PowerModel mModel = new PowerModel();

    /**
     * 获取Model对象
     */
    public static BaseModel getModel() {
        return mModel;
    }

    /**
     * 自身监听器
     */
    private static PowerListener mPowerListener = new PowerListener() {

        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "power change, oldVal = " + PowerStep.toString(oldVal) + ", newVal = " + PowerStep.toString(newVal));
        }
    };

    /**
     * module manager create method.
     */
    public static void onCreate(final Packet packet) {
        // first, get the context.
        if (null != packet) {
            mPoweronPacket = new Packet(packet);
            mContext = (Context) packet.getObject("context");
        }

        // 绑定自身模式监听器
        mModel.bindListener(mPowerListener);
        if (0 != mPowerTimerId) {
            TimerUtils.killTimer(mPowerTimerId);
        }
        mPowerTimeOut = 0;
        mPowerStartTime = System.currentTimeMillis();
        mModel.getPowerStep().setVal(PowerStep.POWER_ON_START);
        mPowerTimerId = TimerUtils.setTimer(mContext, TimerUtils.TimerType.HANDLER, BOOT_TIMER, BOOT_TIMER, new Timerable.TimerListener() {
            @Override
            public void onTimer(int timerId) {
                powerOnStep();
            }
        });
    }

    /**
     * module manager destroy method.
     */
    public static void onDestroy() {
        powerOver();
        mPowerStartTime = System.currentTimeMillis();
        mModel.getPowerStep().setVal(PowerStep.POWER_OFF_START);
        mPowerTimerId = TimerUtils.setTimer(mContext, TimerUtils.TimerType.HANDLER, BOOT_TIMER, BOOT_TIMER, new Timerable.TimerListener() {
            @Override
            public void onTimer(int timerId) {
                powerOffStep();
            }
        });
    }

    /**
     * 获取电源阶段
     */
    public static int getPowerStep() {
        return mModel.getPowerStep().getVal();
    }


    public static  String findFile(String path, String findNameRegular) {
        String results=null;
        if ((path != null)  && (findNameRegular != null)) {
            File parent = new File(path);
            if (parent.exists()) {
                File[] childs = parent.listFiles();
                if (childs != null) {
                    for (File child : childs) {
                        String name = child.getName();
                        if (name != null) {
                            if (name.matches(findNameRegular)) {
                                results=name;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }


    public static void MCUAutoUpdate() {
        String SYSTEM_ACTION = "com.wwc2.mcuupdate.systemupdate";
        String SYSTEM_ACTION_EXTRA = "need_user_check";
        String updateFinish= SystemProperties.get("persist.sys.mcu_update_finish","false");
        String UPDATE_PATH = "/system/mcu_update_bin/";

        LogUtils.d(TAG, "MCUAutoUpdate  persist.sys.mcu_update_finish = " +  updateFinish);

        if(!("true".equals(updateFinish))) {
            // get update File
            String updateFile = findFile(UPDATE_PATH, "^CM_([0-9]{2}.){2}[0-9]{2}_[0-9]{2}.[0-9]{2}.bin$");
            if (updateFile != null) {
                String updateVersion = updateFile.substring(0, updateFile.length() - 4);
                VersionDriverable driverable = VersionDriver.Driver();
                String CurrentVersion = driverable.getMcuVersion();
                if (!updateVersion.equals(CurrentVersion)) {
                    Intent it = new Intent(SYSTEM_ACTION);
                    it.putExtra(SYSTEM_ACTION_EXTRA, false);
                    mContext.sendBroadcast(it);
                }else{
                    LogUtils.d(TAG, "MCUAutoUpdate  first boot CurrentVersion " + CurrentVersion );
                }
            }
            SystemProperties.set("persist.sys.mcu_update_finish", "true");
        }
//        else{
//            String updateFile = findFile(UPDATE_PATH, "^CM_([0-9]{2}.){2}[0-9]{2}_[0-9]{2}.[0-9]{2}.bin$");
//            if (updateFile != null) {
//                String updateVersion = updateFile.substring(0, updateFile.length() - 4);
//                VersionDriverable driverable = VersionDriver.Driver();
//                String CurrentVersion = driverable.getMcuVersion();
//                if (!"Unknown".equals(CurrentVersion) && !updateVersion.equals(CurrentVersion)) {
//                    Intent it = new Intent(SYSTEM_ACTION);
//                    it.putExtra(SYSTEM_ACTION_EXTRA, true);
//                    mContext.sendBroadcast(it);
//                }else{
//                    LogUtils.d(TAG, "MCUAutoUpdate,  second boot CurrentVersion " + CurrentVersion );
//                }
//            }
//        }

    }

    /**
     * 开机阶段
     */
    private static void powerOnStep() {
        final int step = getPowerStep();
        // power on start, prepare module manager
        if (PowerStep.isPoweronStart(step)) {
            ModuleManager.onPrepare();
            // Provider被开始
            LogicProviderHelper.getInstance().start();
            mModel.getPowerStep().setVal(PowerStep.POWER_ON_MODULE_PREPARED);
            return;
        }
        // power on, create managers
        if (PowerStep.isPoweronModulePrepared(step)) {
            // 源管理类被创建
            SourceManager.onCreate(mPoweronPacket);

            // 事件输入管理类被创建
            EventInputManager.onCreate(mPoweronPacket);

            // 声音管理类被创建
            VolumeManager.onCreate(mPoweronPacket);
            mModel.getPowerStep().setVal(PowerStep.POWER_ON_MANAGERS_CREATED);
            return;
        }
        // power on, create module manager
        if (PowerStep.isPoweronManagersCreated(step)) {
            ModuleManager.onCreate(mPoweronPacket);
            mModel.getPowerStep().setVal(PowerStep.POWER_ON_MODULE_CREATED);
            return;
        }
        // power on, wait module manager create over
        if (PowerStep.isPoweronModuleCreated(step)) {
            if (ModuleManager.isCreateOver()) {
                mModel.getPowerStep().setVal(PowerStep.POWER_ON_CREATE_OVERED);
            } else {
                LogUtils.d(TAG, "Power on##" + "need waiting module onCreate.");
                mPowerTimeOut++;
                if (mPowerTimeOut > BOOT_TIMEOUT) {
                    LogUtils.e(TAG, "Power on##" + "wait module onCreate timeout");
                    mModel.getPowerStep().setVal(PowerStep.POWER_ON_CREATE_OVERED);
                }
            }
            return;
        }
        // power on, create mcu manager
        if (PowerStep.isPoweronCreateOvered(step)) {
            WakeupManager.getInstance().onCreate(getContext());

            McuManager.onCreate(mPoweronPacket);
            mModel.getPowerStep().setVal(PowerStep.POWER_ON_MCU_CREATED);
            mPowerTimeOut = 0;//当没收到MCU的源信息时，放在下面会造成死循环，一起死等MCU。2018-01-12

            PanoramicManager.getInstance().onCreate(mPoweronPacket);

            if (PowerManager.isPortProject() && !PowerManager.isRuiPai_SP()) {
                DspManager.getInstance().onCreate(mPoweronPacket);
            }
            if (isRuiPai()) {
                GpsDataManager.getInstance().onCreate(mPoweronPacket);
            }
            if (isTaxiClient()) {
                TaxiManager.getInstance().onCreate(mPoweronPacket);
            }

            if (ApkUtils.isAPKExist(mContext, "com.baony.avm360")) {
                Avm360Manager.openCameraBack(getContext(), true);
            }
            return;
        }
        // power on, wait first boot source.
        if (PowerStep.isPoweronMcuCreated(step)) {
            boolean wait = false;
            int source = Define.Source.SOURCE_INVALID;
            // get mcu manager first boot status.
            int boot = McuManager.firstBoot();
            if (Define.FirstBoot.isFirstBoot(boot)) {
                source = McuManager.getMemorySource();
                LogUtils.d(TAG, "Power on##" + "McuManager need first boot, source = " + Define.Source.toString(source));
            } else if (Define.FirstBoot.isWait(boot)) {
                wait = true;
                LogUtils.d(TAG, "Power on##" + "McuManager need waiting first boot.");
            } else {
                // get module manager first boot source.
                BaseLogic logic = ModuleManager.firstBoot();
                if (null == logic) {
                    // default page.
                    LogUtils.d(TAG, "Power on##" + "SourceManager default handle.");
                } else {
                    boot = logic.firstBoot();
                    if (Define.FirstBoot.isFirstBoot(boot)) {
                        // need first boot
                        LogUtils.d(TAG, "Power on##" + Define.Source.toString(logic.source()) + " need first boot.");
                        source = logic.source();
                    } else if (Define.FirstBoot.isWait(boot)) {
                        // wait
                        wait = true;
                        LogUtils.d(TAG, "Power on##" + Define.Source.toString(logic.source()) + " need waiting first boot.");
                    } else {
                        // default page.
                        LogUtils.d(TAG, "Power on##" + "SourceManager default handle.");
                    }
                }
            }

            boolean go = true;
            if (wait) {
                go = false;
                mPowerTimeOut++;
                if (mPowerTimeOut > BOOT_TIMEOUT) {
                    LogUtils.e(TAG, "Power on##" + "wait module onCreate timeout");
                    go = true;
                } else if (mPowerTimeOut % 10 == 0) {
                    McuManager.sendInitOkToMcu();
                }
            }

            if (go) {
                if (Define.Source.SOURCE_INVALID == source) {
                    SourceManager.firstBoot();
                } else {
                    final int current = SourceManager.getCurSource();
                    BaseLogic logic = ModuleManager.getLogicBySource(current);
                    if (null != logic) {
                        if (logic.passive()) {
                            LogUtils.w(TAG, "Power on##" + "current source[" + Define.Source.toString(current) + "] not allow leave this source.");
                            SourceManager.firstBoot();
                        } else {
                            final boolean result = SourceManager.onChangeSource(source);
                            if (!result) {
                                LogUtils.e(TAG, "Power on##" + Define.Source.toString(source) + " change failed, SourceManager default handle.");
                                SourceManager.firstBoot();
                            }
                        }
                    } else {
                        LogUtils.w(TAG, "Power on##" + "current source " + Define.Source.toString(current) +
                                " null exception, try change to " + Define.Source.toString(source));
                        /*-begin-20180417-ydinggen-modify-名称为persist断电会有保存，解决在没收到广播时马上断ARM电，导致下次起来时直接切源-*/
                        String launcherState = SystemProperties.get("user.launcher.start");
                        /*-end-20180417-ydinggen-modify-名称为persist断电会有保存，解决在没收到广播时马上断ARM电，导致下次起来时直接切源-*/
                        LogUtils.w(TAG, "Power on##" + "current source " + Define.Source.toString(current) +
                                " laucher.start state  " + launcherState);
                        if (launcherState.equals("true")) {
                            SystemProperties.set("user.launcher.start", "false");
                            final boolean result = SourceManager.onChangeSource(source);
                            if (!result) {
                                LogUtils.e(TAG, "Power on##" + Define.Source.toString(source) + " change failed, SourceManager default handle.");
                                SourceManager.firstBoot();
                            }
                        } else {
                            mFirstBoot = true;
                        }
                    }
                }
                mModel.getPowerStep().setVal(PowerStep.POWER_ON_OVER);
            }
            return;
        }
        // power on over, kill power timer.
        if (PowerStep.isPoweronOvered(step)) {
            powerOver();
            LogUtils.d(TAG, "Power on##" + "spend time = " + (System.currentTimeMillis() - mPowerStartTime));
            //begin zhongyang.hu add for mcu auto update 20171211
            MCUAutoUpdate();
            //end zhongyang.hu add for mcu auto update 20171211
            //begin zhongyang.hu add for open  mobile data 20180104
            //if(isKSProject()) {
                registerSimCard();
            //}
            //end

            //在此处启动DVR有可能出现打开摄像头失败。2019-11-13
//            LogUtils.d(TAG, "Power test add start dvr server!");
//            if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
//                    !FactoryDriver.Driver().getDvrEnable()) {
//
//            } else {
//                //test add dvr
//                ApkUtils.startServiceSafety(mContext,
//                        "com.wwc2.dvr.RecordService",
//                        "com.wwc2.dvr",
//                        "com.wwc2.dvr.RecordService");
//            }
            mPowerStartTime = 0;
            return;
        }
    }

    /**
     * 关机阶段
     */
    private static void powerOffStep() {
        final int step = getPowerStep();
        // power off start ,destroy mcu manager.
        if (PowerStep.isPoweroffStart(step)) {
            mPowerTimeOut = 0;
            if (PowerManager.isPortProject() && !PowerManager.isRuiPai_SP()) {
                DspManager.getInstance().onDestroy();
            }
            if (isRuiPai()) {
                GpsDataManager.getInstance().onDestroy();
            }
            if (isTaxiClient()) {
                TaxiManager.getInstance().onDestroy();
            }
            McuManager.onDestroy();
            mModel.getPowerStep().setVal(PowerStep.POWER_OFF_MCU_DESTROYED);
            return;
        }
        // destroy module manager.
        if (PowerStep.isPoweroffMcuDestroyed(step)) {
            ModuleManager.onDestroy();
            mModel.getPowerStep().setVal(PowerStep.POWER_OFF_MODULE_DESTROYED);
            return;
        }
        // wait module destroyed.
        if (PowerStep.isPoweroffModuleDestroyed(step)) {
            if (ModuleManager.isDestroyOver()) {
                mModel.getPowerStep().setVal(PowerStep.POWER_OFF_MODULE_OVERED);
            } else {
                LogUtils.d(TAG, "Power off##" + "need waiting module onDestroy.");
                mPowerTimeOut++;
                if (mPowerTimeOut > BOOT_TIMEOUT) {
                    LogUtils.e(TAG, "Power on##" + "wait module onCreate timeout");
                    mModel.getPowerStep().setVal(PowerStep.POWER_OFF_MODULE_OVERED);
                }
            }
            return;
        }
        // destroy managers.
        if (PowerStep.isPoweroffModuleOvered(step)) {
            VolumeManager.onDestroy();

            EventInputManager.onDestroy();

            SourceManager.onDestroy();

            mModel.getPowerStep().setVal(PowerStep.POWER_OFF_MANAGERS_DESTROYED);

            return;
        }
        // destroy config manager.
        if (PowerStep.isPoweroffManagersDestroyed(step)) {
            mModel.getPowerStep().setVal(PowerStep.POWER_OFF_OVER);
            return;
        }
        // power off over, kill power timer.
        if (PowerStep.isPoweroffOvered(step)) {
            powerOver();
            mModel.unbindListener(mPowerListener);
            mContext = null;
            LogUtils.d(TAG, "Power off##" + "spend time = " + (System.currentTimeMillis() - mPowerStartTime));
            mPowerStartTime = 0;
            return;
        }
    }

    /**
     * 动作结束
     */
    private static void powerOver() {
        if (0 != mPowerTimerId) {
            TimerUtils.killTimer(mPowerTimerId);
            mPowerTimerId = 0;
        }
    }

    //begin zhongyang.hu modify for sim_swtich 20180529
    public static int[] getSubId(int slotId) {
        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(mContext);
        Method declaredMethod;
        int[] subArr = null;
        try {
            declaredMethod = Class.forName("android.telephony.SubscriptionManager").getDeclaredMethod("getSubId", new Class[]{Integer.TYPE});
            declaredMethod.setAccessible(true);
            subArr = (int[]) declaredMethod.invoke(mSubscriptionManager, slotId);
        } catch (Exception e) {
            e.printStackTrace();
            declaredMethod = null;
        }
        if (declaredMethod == null) {
            subArr = null;
        }
        return subArr;
    }


    public static void setDefaultDataSubId(int subId) {
        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(mContext);
        Method declaredMethod;
        try {
            declaredMethod = Class.forName("android.telephony.SubscriptionManager").getDeclaredMethod("setDefaultDataSubId", new Class[]{Integer.TYPE});
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(mSubscriptionManager, subId);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return ;
    }
 //end

    //zhongyang.hu add for open  mobile data 20180104
    public static void setMobileDataEnabled(boolean isEnable) throws Exception {
        if(mContext!=null) {
            //后续全部采用下面设置方式2020-08-19

//            //begin zhongyang.hu modify for sim_swtich 20180529
//            int slot;
//            String simswich= SystemProperties.get("persist.sys.simswich","1");
//            if(SystemProperties.get("persist.sys.simswich","1")) {
//                 if (simswich.equals("1")) {
//                     slot = 1;
//                 } else {
//                     slot = 0;
//                 }
//             }else{
//                 if (simswich.equals("1")) {
//                     slot = 0;
//                 } else {
//                     slot = 1;
//                 }
//             }
//
//            int subId[]=getSubId(slot);
//
//            setDefaultDataSubId(subId[0]);
//            Log.d("setMobileDataEnabled", "slot =" +slot +", subId= "+subId[0]);
//            //end

            TelephonyManager mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> cmClass = mTelephonyManager.getClass();
            Class<?>[] argClasses = new Class[1];
            argClasses[0] = boolean.class;
            // 反射TelephonyManager中hide的方法setDataEnabled，可以开启和关闭GPRS网络
            Method method = cmClass.getMethod("setDataEnabled", argClasses);
            method.invoke(mTelephonyManager,isEnable);
        }
    }


    public static boolean isCH009Project(){
        boolean ret =false;
        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        String clientProject;
        if (client != null) {
            clientProject = client.getString("ClientProject");
            if (clientProject.contains("ch009")) {
                ret =true;
            }
        }
        return ret;
    }

    public static boolean isKSProject(){
        boolean ret =false;
        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        String clientProject;
        if (client != null) {
            clientProject = client.getString("ClientProject");
            if (BaseClientDriver.CLIENT_KS.equals(clientProject) ||
                    BaseClientDriver.CLIENT_KS4.equals(clientProject)) {
                ret =true;
            }
        }
        return ret;
    }

    public static boolean isPortProject() {
        boolean ret = false;
        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        String clientProject;
        if (client != null) {
            clientProject = client.getString("ClientProject");
            if (clientProject.contains("sp") ||
                    clientProject.contains("hs")) {
                ret = true;
            }
            LogUtils.d(TAG, "isPortProject---ret=" + ret + ", clientProject=" + clientProject);
        }
        return ret;
    }

    public static boolean isRuiPai() {
        String client = VersionDriver.Driver().getClientProject();
        LogUtils.d(TAG, "isRuiPai--- client=" + client);
        if (client.equals("ch007_25") || client.equals("ch007_sp_33")) {
            return true;
        }
        return false;
    }

    public static boolean isRuiPai_SP() {
        String client = VersionDriver.Driver().getClientProject();
        LogUtils.d(TAG, "isRuiPai_SP--- client=" + client);
        if (client.equals("ch007_sp_33")) {
            return true;
        }
        return false;
    }

    public static boolean isTaxiClient() {
        String client = VersionDriver.Driver().getClientProject();
        LogUtils.d(TAG, "isTaxiClient--- client=" + client);
        if (client.equals("ch010_29")) {
            return true;
        }
        return false;
    }

    public static void openMobileData(){
        //if(isKSProject()){
            SystemProperties.set("ecar.sim.support", "true");
            try {
                setMobileDataEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
       // }
    }

    public static void registerSimCard() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_SIM_STATE_CHANGED);
        mContext.registerReceiver(mSimReceiver, myIntentFilter);
    }





    private static BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(context.TELEPHONY_SERVICE);
                int state = tm.getSimState();
                if(state == TelephonyManager.SIM_STATE_READY){
                    openMobileData();
                    mContext.unregisterReceiver(mSimReceiver);
                }
            }
        }
    };
    //end



    public static void disableWifi() {
        if(mContext!=null) {
            WifiManager wifiManager = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            boolean enabled = wifiManager.isWifiEnabled();
            if (enabled) {
                wifiManager.setWifiEnabled(false);
                LogUtils.d(TAG, "setWifiEnabled  false when acc off");
            }
        }
    }

    //begin  zhongyang.hu add for upload error state info.  20180333
    public static void  feedDogFlag(){
        synchronized (mLockDog){
             feedDog = true;
        }
    }

    public static void resetDogFlag() {
        synchronized (mLockDog) {
            feedDog = false;
        }
    }


    public static boolean  isOutOfDog() {
        boolean ret;
        synchronized (mLockDog) {
            ret=!feedDog;
        }
        return  ret;
    }
    //end

}
