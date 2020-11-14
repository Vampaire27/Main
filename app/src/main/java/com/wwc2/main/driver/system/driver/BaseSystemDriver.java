package com.wwc2.main.driver.system.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;

import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.custom.MIntegerInteger;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.reflex.ReflexUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.driver.system.SystemDriverable;
import com.wwc2.main.driver.system.SystemListener;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

import java.lang.reflect.Method;
import java.util.List;

/**
 * the base system driver.
 *
 * @author wwc2
 * @date 2017/1/23
 */
public abstract class BaseSystemDriver extends BaseDriver implements SystemDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseSystemDriver";

    /**
     * the top apk timer queue.
     */
    private TimerQueue mTopApkTimerQueue = new TimerQueue();

    /**
     * 数据Model
     */
    protected static class SystemModel extends BaseModel {
        @Override
        public Packet getInfo() {
            return null;
        }

        /**
         * 信号强度, 0:Dbm 1:Asu
         */
        private MIntegerInteger mSignalStrength = new MIntegerInteger(this, "", null);

        public MIntegerInteger getSignalStrength() {
            return mSignalStrength;
        }
        /**
         * 重启状态
         */
        private MBoolean rebootState = new MBoolean(this, "rebootStateListener", false);
        public MBoolean getRebootState() {
            return rebootState;
        }
    }

    @Override
    public BaseModel newModel() {
        return new SystemModel();
    }

    /**
     * get the model object.
     */
    protected SystemModel Model() {
        SystemModel ret = null;
        BaseModel model = getModel();
        if (model instanceof SystemModel) {
            ret = (SystemModel) model;
        }
        return ret;
    }

    // 广播监听
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packageName = intent.getDataString();
            if (!TextUtils.isEmpty(packageName)) {
                packageName = packageName.replace("package:", "");
            }
            if (!TextUtils.isEmpty(packageName)) {
                List<BaseListener> listeners = Model().getListeners();
                if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    LogUtils.d(TAG, "Installed:" + packageName + " Package name program");
                    if (null != listeners) {
                        for (int i = 0; i < listeners.size(); i++) {
                            if (listeners.get(i) instanceof SystemListener) {
                                SystemListener listener = (SystemListener) listeners.get(i);
                                listener.AppInstallListener(packageName);
                            }
                        }
                    }
                } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                    LogUtils.d(TAG, "Uninstall:" + packageName + " Package name program");
                    if (null != listeners) {
                        for (int i = 0; i < listeners.size(); i++) {
                            if (listeners.get(i) instanceof SystemListener) {
                                SystemListener listener = (SystemListener) listeners.get(i);
                                listener.AppUninstallListener(packageName);
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     * 模式监听器
     */
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            collapseStatusBar();
        }
    };

    @Override
    public void onCreate(Packet packet) {
        Context context = getMainContext();
        if (null != context) {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            myIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            myIntentFilter.addDataScheme("package");
            context.registerReceiver(mBroadcastReceiver, myIntentFilter);
        }

        SourceManager.getModel().bindListener(mSourceListener);
    }

    @Override
    public void onDestroy() {
        mTopApkTimerQueue.stop();
        SourceManager.getModel().unbindListener(mSourceListener);
        Context context = getMainContext();
        if (null != context) {
            try {
                context.unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean collapseStatusBar() {
        boolean ret = false;
        Context context = getMainContext();
        if (null != context) {
            try {
                Object statusBarManager = null;
                Method collapse;

                Class classMethod = ReflexUtils.getClassByName(Context.class.getName());
                if (null != classMethod) {
                    Method method = ReflexUtils.getMethodByName(classMethod, "getSystemService", String.class);
                    if (null != method) {
                        statusBarManager = ReflexUtils.invokeMethod(method, context, "statusbar");
                    }
                }

                if (null != statusBarManager) {
                    if (Build.VERSION.SDK_INT <= 16) {
                        collapse = statusBarManager.getClass().getMethod("collapse");
                    } else {
                        collapse = statusBarManager.getClass().getMethod("collapsePanels");
                    }
                    if (null != collapse) {
                        ret = true;
                        collapse.invoke(statusBarManager);
                        LogUtils.d(TAG, "collapseStatusBar success.");
                    } else {
                        LogUtils.d(TAG, "collapseStatusBar failed, because the collapse is null.");
                    }
                } else {
                    LogUtils.d(TAG, "collapseStatusBar failed, because the statusBarManager is null.");
                }
            } catch (Exception localException) {
                LogUtils.d(TAG, "collapseStatusBar failed, because the exception.");
                localException.printStackTrace();
            }
        } else {
            LogUtils.w(TAG, "collapseStatusBar failed, because the context is null.");
        }
        return ret;
    }

    @Override
    public boolean reboot() {
        Model().getRebootState().setVal(true);
        boolean ret = false;
        CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
        if (logic != null) {
            ret = true;
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.REBOOT, null);
        }
        return ret;
    }

    @Override
    public boolean restoreFactorySettings() {
        boolean ret = false;
        CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
        if (logic != null) {
            ret = true;
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.RESTOR_FACTORY_SETTINGS, null);
        }
        return ret;
    }

    @Override
    public boolean wipeUserData() {
        boolean ret = false;
        CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
        if (logic != null) {
            ret = true;
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.WIPE_USER_DATA, null);
        }
        return ret;
    }

    @Override
    public boolean wipeCache() {
        boolean ret = false;
        CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
        if (logic != null) {
            ret = true;
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.WIPE_CACHE, null);
        }
        return ret;
    }

    /**
     * 底层包名和类名发送变化
     */
    protected void topPackageClassChanged(String pkgName, String clsName) {
        if (!TextUtils.isEmpty(pkgName)) {
            if (mTopApkTimerQueue.isOver()) {
                // 对比模式
                LogUtils.d(TAG, "topPackageClassChanged##onCompareSource isOver.");
                SourceManager.onCompareSource(pkgName, clsName);
            } else {
                mTopApkTimerQueue.stop();
                Packet packet = new Packet();
                packet.putString("pkgName", pkgName);
                packet.putString("clsName", clsName);
                mTopApkTimerQueue.add(1000, packet, new BaseCallback() {
                    @Override
                    public void onCallback(int nId, Packet packet) {
                        if (null != packet) {
                            final String pkgName = packet.getString("pkgName");
                            final String clsName = packet.getString("clsName");
                            // 对比模式
                            LogUtils.d(TAG, "topPackageClassChanged##onCompareSource onCallback");
                            SourceManager.onCompareSource(pkgName, clsName);
                        }
                    }
                });
                mTopApkTimerQueue.start();
                LogUtils.d(TAG, "topPackageClassChanged##onCompareSource ignore.");
            }
        }
    }

    @Override
    public boolean getRebootState() {
        return Model().getRebootState().getVal();
    }
}
