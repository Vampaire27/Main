package com.wwc2.main.manager;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.camera.CameraLogic;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.client.driver.BaseClientDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * all of sources manager.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class SourceManager {

    /**
     * TAG
     */
    public static final String TAG = "SourceManager";

    /**
     * the main service context.
     */
    private static Context mContext = null;

    /**
     * lock object.
     */
    private static final Lock mLock = new ReentrantLock();

    protected static String[] needKillThirdPartApk = {
            "com.coagent.voip",
            "com.ecar.AppManager",
            "ecar.DeviceManagerService"
    };

    private static TimerQueue mStopSourceTimerQueue = new TimerQueue();

    /**
     * 模式监听器
     */
    public static class SourceListener extends BaseListener {

        @Override
        public String getClassName() {
            return SourceListener.class.getName();
        }

        /**
         * 当前Source监听器
         */
        public void CurSourceListener(Integer oldVal, Integer newVal) {

        }

        /**
         * 之前Source监听器
         */
        public void OldSourceListener(Integer oldVal, Integer newVal) {

        }

        /**
         * 当前后台Source监听器
         */
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {

        }

        /**
         * 之前后台Source监听器
         */
        public void OldBackSourceListener(Integer oldVal, Integer newVal) {

        }

        /**
         * 当前包名监听器
         */
        public void CurPkgNameListener(String oldVal, String newVal) {

        }

        /**
         * 之前包名监听器
         */
        public void OldPkgNameListener(String oldVal, String newVal) {

        }

        /**
         * 当前类名监听器
         */
        public void CurClsNameListener(String oldVal, String newVal) {

        }

        /**
         * 之前类名监听器
         */
        public void OldClsNameListener(String oldVal, String newVal) {

        }

        /**
         * 实时包名监听器
         */
        public void RealTimePkgNameListener(String oldVal, String newVal) {

        }

        /**
         * 实时类名监听器
         */
        public void RealTimeClsNameListener(String oldVal, String newVal) {

        }
    }

    /**
     * 模式Model
     */
    protected static class SourceModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putInt("CurSource", mCurSource.getVal());
            packet.putInt("OldSource", mOldSource.getVal());
            packet.putInt("CurBackSource", mCurBackSource.getVal());
            packet.putInt("OldBackSource", mOldBackSource.getVal());
            packet.putString("CurPkgName", mCurPkgName.getVal());
            packet.putString("OldPkgName", mOldPkgName.getVal());
            packet.putString("CurClsName", mCurClsName.getVal());
            packet.putString("OldClsName", mOldClsName.getVal());
            packet.putString("RealTimePkgName", mRealTimePkgName.getVal());
            packet.putString("RealTimeClsName", mRealTimeClsName.getVal());
            return packet;
        }

        /**
         * 当前Source
         */
        private MInteger mCurSource = new MInteger(this, "CurSourceListener", Define.Source.DEFAULT);

        public MInteger getCurSource() {
            return mCurSource;
        }

        /**
         * 之前Source
         */
        private MInteger mOldSource = new MInteger(this, "OldSourceListener", Define.Source.DEFAULT);

        public MInteger getOldSource() {
            return mOldSource;
        }

        /**
         * 当前后台source
         */
        private MInteger mCurBackSource = new MInteger(this, "CurBackSourceListener", Define.Source.DEFAULT);

        public MInteger getCurBackSource() {
            return mCurBackSource;
        }

        /**
         * 之前后台Source
         */
        private MInteger mOldBackSource = new MInteger(this, "OldBackSourceListener", Define.Source.DEFAULT);

        public MInteger getOldBackSource() {
            return mOldBackSource;
        }

        /**
         * 当前包名
         */
        private MString mCurPkgName = new MString(this, "CurPkgNameListener", "com.wwc2.launcher");

        public MString getCurPkgName() {
            return mCurPkgName;
        }

        /**
         * 之前包名
         */
        private MString mOldPkgName = new MString(this, "OldPkgNameListener", null);

        public MString getOldPkgName() {
            return mOldPkgName;
        }

        /**
         * 当前类名
         */
        private MString mCurClsName = new MString(this, "CurClsNameListener", null);

        public MString getCurClsName() {
            return mCurClsName;
        }

        /**
         * 之前类名
         */
        private MString mOldClsName = new MString(this, "OldClsNameListener", null);

        public MString getOldClsName() {
            return mOldClsName;
        }

        /**
         * 实时包名
         */
        private MString mRealTimePkgName = new MString(this, "RealTimePkgNameListener", null);

        public MString getRealTimePkgName() {
            return mRealTimePkgName;
        }

        /**
         * 实时类名
         */
        private MString mRealTimeClsName = new MString(this, "RealTimeClsNameListener", null);

        public MString getRealTimeClsName() {
            return mRealTimeClsName;
        }
    }

    /**
     * Model对象
     */
    private static SourceModel mModel = new SourceModel();

    /**
     * 获取Model对象
     */
    public static BaseModel getModel() {
        return mModel;
    }




    /**
     * 是否为需要kill三方apk
     */
    private static boolean isNeedKillPackage(String[] killPackages, String packageName) {
        boolean ret = false;
        if (null != killPackages && null != packageName) {
            final int length = killPackages.length;
            for (int i = 0; i < length; i++) {
                if (packageName.equals(killPackages[i])) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }



    /**
     * 自身监听器
     */
    private static SourceListener mListener = new SourceListener() {

        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "cur source change, oldVal = " + Define.Source.toString(oldVal) + ", newVal = " + Define.Source.toString(newVal));
        }

        @Override
        public void OldSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "old source change, oldVal = " + Define.Source.toString(oldVal) + ", newVal = " + Define.Source.toString(newVal));
        }

        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            // 后台source变化，记忆的退出source清空
            Intent intent = new Intent("ACTION_MAIN_SOURCE");
            intent.putExtra("VALUE", String.valueOf(newVal.intValue()));
            mContext.sendBroadcast(intent);
            LogUtils.d(TAG, "cur back source change, oldVal = " + Define.Source.toString(oldVal) + ", newVal = " + Define.Source.toString(newVal));
        }

        @Override
        public void OldBackSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "old back source change, oldVal = " + Define.Source.toString(oldVal) + ", newVal = " + Define.Source.toString(newVal));
        }

        @Override
        public void CurPkgNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "cur package name change, oldVal = " + oldVal + ", newVal = " + newVal);
        }

        @Override
        public void OldPkgNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "old package name change, oldVal = " + oldVal + ", newVal = " + newVal);
        }

        @Override
        public void CurClsNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "cur package class name change, oldVal = " + oldVal + ", newVal = " + newVal);
        }

        @Override
        public void OldClsNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "old package class name change, oldVal = " + oldVal + ", newVal = " + newVal);
        }

        @Override
        public void RealTimePkgNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, " real time package name change, oldVal = " + oldVal + ", newVal = " + newVal);
            //zhongyang.hu add kill_thirdpart apk

            if(isNeedKillPackage(needKillThirdPartApk,oldVal) &&
                    newVal.equals("com.wwc2.launcher") &&
                    getCurSource()!= Define.Source.SOURCE_ACCOFF){
                LogUtils.d(TAG, "kill the thirdpart apk =" + oldVal + ", newVal = " + newVal);
                Packet packet = new Packet();
                packet.putString("pkgName", oldVal);
                packet.putStringArray("pkgNameList",needKillThirdPartApk);
                BaseLogic l = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                if (null != l) {
                    l.Notify(SystemPermissionInterface.MAIN_TO_APK.KILL_PROCCESS, packet);
                }

            }
         }

        @Override
        public void RealTimeClsNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "real time class name change, oldVal = " + oldVal + ", newVal = " + newVal);
        }
    };

    /**
     * 模式切换列表
     */
    private static List<Integer> mSourceList = new ArrayList<>();

    /**
     * 模式列表最大数
     */
    protected static int getSourceListMax() {
        return 10;
    }

    /**
     * 创建
     */
    public static void onCreate(Packet packet) {
        if (null != packet) {
            mContext = (Context) packet.getObject("context");
        }
        // 创建查询置顶窗口服务
//        QueryTopWindowService.start(mContext);
        // 绑定自身模式监听器
        mModel.bindListener(mListener);
    }

    /**
     * 销毁
     */
    public static void onDestroy() {
        // 解绑自身模式监听器
        mModel.unbindListener(mListener);
        // 销毁查询置顶窗口服务
//        QueryTopWindowService.stop(mContext);
    }

    /**
     * 第一次上电开机的模式，暂时为无声模式
     */
    public static int getFirstPoweronDefaultSource() {
        return Define.Source.SOURCE_SILENT;
    }

    /**
     * 获取当前Source的值
     */
    public static int getCurSource() {
        int ret = Define.Source.SOURCE_NONE;
        if (null != mModel) {
            ret = mModel.getCurSource().getVal();
        }
        return ret;
    }

    /**
     * 获取之前Source的值
     */
    public static int getOldSource() {
        int ret = Define.Source.SOURCE_NONE;
        if (null != mModel) {
            ret = mModel.getOldSource().getVal();
        }
        return ret;
    }

    /**
     * 获取当前后台Source的值
     */
    public static int getCurBackSource() {
        int ret = Define.Source.SOURCE_NONE;
        if (null != mModel) {
            ret = mModel.getCurBackSource().getVal();
        }
        return ret;
    }

    /**
     * 获取之前后台Source的值
     */
    public static int getOldBackSource() {
        int ret = Define.Source.SOURCE_NONE;
        if (null != mModel) {
            ret = mModel.getOldBackSource().getVal();
        }
        return ret;
    }

    /**
     * 获取当前包名
     */
    public static String getCurPkgName() {
        String ret = null;
        if (null != mModel) {
            ret = mModel.getCurPkgName().getVal();
        }
        return ret;
    }

    /**
     * 获取之前包名
     */
    public static String getOldPkgName() {
        String ret = null;
        if (null != mModel) {
            ret = mModel.getOldPkgName().getVal();
        }
        return ret;
    }

    /**
     * 获取当前类名
     */
    public static String getCurClsName() {
        String ret = null;
        if (null != mModel) {
            ret = mModel.getCurClsName().getVal();
        }
        return ret;
    }

    /**
     * 获取之前类名
     */
    public static String getOldClsName() {
        String ret = null;
        if (null != mModel) {
            ret = mModel.getOldClsName().getVal();
        }
        return ret;
    }

    /**
     * 获取实时包名
     */
    public static String getRealTimePkgName() {
        String ret = null;
        if (null != mModel) {
            ret = mModel.getRealTimePkgName().getVal();
        }
        return ret;
    }

    /**
     * 获取实时类名
     */
    public static String getRealTimeClsName() {
        String ret = null;
        if (null != mModel) {
            ret = mModel.getRealTimeClsName().getVal();
        }
        return ret;
    }

    /**
     * 打印source信息
     */
    public static void printLog(String prefix) {
        // 打印source信息
        prefix = (null == prefix) ? "" : prefix + ", ";
        LogUtils.d(TAG, prefix + "Source Info" +
                ", CurSource = " + Define.Source.toString(getCurSource()) +
                ", OldSource = " + Define.Source.toString(getOldSource()) +
                ", CurBackSource = " + Define.Source.toString(getCurBackSource()) +
                ", OldBackSource = " + Define.Source.toString(getOldBackSource()) +
                ", CurPkgName = " + getCurPkgName() +
                ", OldPkgName = " + getOldPkgName() +
                ", CurClsName = " + getCurClsName() +
                ", OldClsName = " + getOldClsName() +
                ", RealTimePkgName = " + getRealTimePkgName() +
                ", RealTimeClsName = " + getRealTimeClsName());
    }

    /**
     * first boot
     */
    public static void firstBoot() {
        mLock.lock();
        try {
            final int curSource = getCurSource();
            final int backSource = getCurBackSource();
            int defSource = getFirstPoweronDefaultSource();
            int mcuSource = McuManager.getMemorySource();
            if (mcuSource != Define.Source.SOURCE_INVALID &&
                    mcuSource != Define.Source.SOURCE_NONE) {
                defSource = mcuSource;
            }
            LogUtils.d(TAG, "first boot, curSource = " + Define.Source.toString(curSource) +
                    ", backSource = " + Define.Source.toString(backSource) +
                    ", defSource = " + Define.Source.toString(defSource));
            if (Define.Source.SOURCE_NONE == curSource) {
                LogUtils.d(TAG, "first boot, enter default source = " + Define.Source.toString(defSource));
                onChangeSource(defSource);
            } else {
                if (Define.Source.SOURCE_NONE == backSource) {
                    boolean source = false;
                    BaseLogic defLogic = ModuleManager.getLogicBySource(defSource);
                    if (null != defLogic) {
                        source = defLogic.isSource();
                    }
                    if (source) {
                        LogUtils.d(TAG, "first boot, open background defSource = " + Define.Source.toString(defSource));
                        SourceManager.onOpenBackgroundSource(defSource);
                    } else {
                        final int spSource = Define.Source.SOURCE_SILENT;
                        LogUtils.d(TAG, "first boot, open background specify source = " + Define.Source.toString(spSource));
                        SourceManager.onOpenBackgroundSource(spSource);
                    }
                } else {
                    LogUtils.d(TAG, "first boot, APP already in the power on source.");
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 启动APK
     *
     * @param pkgName    指定包名
     * @param clsName    指定类名
     * @param packetInfo 启动信息
     * @param animation  是否有动画
     * @return true 表示启动成功，false表示切换失败
     */
    public static boolean runApk(String pkgName, String clsName, Packet packetInfo, boolean animation) {
        boolean ret = true;

        BaseLogic logic = ModuleManager.getLogicByPacketName(pkgName);
        if (null != logic) {
            if (logic.isEnterAlwaysPackage()) {
                pkgName = logic.getAPKPacketName();
                clsName = null;
            }
        }

        //ret = ApkUtils.runApk(context, pkgName, baseLogic.getInfo(), false);

        Packet packet = new Packet();
        packet.putString("pkgName", pkgName);
        packet.putString("clsName", clsName);
        packet.putPacket("packet", packetInfo);
        packet.putBoolean("animation", animation);
        logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
        if (null != logic) {
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.RUN_APK, packet);
        }
        return ret;
    }

    /**
     * 切入指定的模式
     *
     * @param source 指定模式
     * @return true表示切换成功，false表示切换失败
     */
    public static boolean onChangeSource(int source) {
        final String string = Define.Source.toString(source);
        LogUtils.d(TAG, "onChangeSource source=" + string+", curSource="+mModel.getCurSource().getVal());
        boolean ret = false;
        mLock.lock();
        try {
            String debug = null;
            if (source != mModel.getCurSource().getVal() || PowerManager.isPortProject()) {
                BaseLogic baseLogic = ModuleManager.getLogicBySource(source);
                if (null != baseLogic) {
                    if (baseLogic.available()) {
                        // start the apk.
                        final String pkgName = baseLogic.getAPKPacketName();
                        if (!baseLogic.runApk()) {
                            ret = runApk(pkgName, null, baseLogic.getInfo(), false);
                        } else {
                            ret = true;
                        }
                        LogUtils.d(TAG, "onChangeSource[" + string + "]#runApk = " + pkgName + (ret ? " success." : " failed."));
                        // source action.
                        ret &= onSourceAction(source);
                    } else {
                        debug = "the module is not available";
                    }
                } else {
                    debug = "the event control null point exception";
                }
            } else {
                debug = "the module source is the same";
            }

            if (null != debug) {
                LogUtils.w(TAG, "changed the source ignore, " + debug + ", ignore: " +
                        "source = " + Define.Source.toString(source) +
                        ", current source = " + Define.Source.toString(getCurSource()));
            }
        } finally {
            mLock.unlock();
        }

        return ret;
    }

    /**
     * 根据指定的source进行对比
     *
     * @param packetName 需要对比Source的包名
     * @param className  需要对比Source的类名
     */
    public static boolean onCompareSource(String packetName, String className) {
        boolean ret = false;
        // 进行当前SOURCE补偿
        mLock.lock();
        try {
            if (!TextUtils.isEmpty(packetName)) {
                if (null != mModel) {
                    mModel.getRealTimePkgName().setVal(packetName);
                    mModel.getRealTimeClsName().setVal(className);
                }

                LogUtils.d(TAG, "onCompareSource, packetName = " + packetName + ", className = " + className);
                BaseLogic logic = ModuleManager.getLogicByPacketName(packetName);
                if (null != logic) {
                    final int source = logic.source();
                    boolean compare = true;
                    // 目标source为SOURCE_NONE
                    if (Define.Source.SOURCE_NONE == source) {
                        compare = false;
                        LogUtils.d(TAG, packetName +
                                " is bypass, ignore to compare source[" + Define.Source.toString(source) + "].");
                    }

                    // 前台对于source对比是否通过
                    if (compare) {
                        logic = ModuleManager.getFrontLogic();
                        if (null != logic) {
                            if (logic.passive()) {
                                compare = false;
                                LogUtils.d(TAG, Define.Source.toString(logic.source()) +
                                        " is passive, ignore to compare source[" + Define.Source.toString(source) + "].");
                                if (logic.source() == Define.Source.SOURCE_CAMERA) {
                                    //在视频播放时，语音进入酷我音乐，马上进倒车，有时酷我会被杀掉，但进入倒车前的源是第三方源，导致在
                                    //退出倒车仍会切到第三方源，而界面停留在视频，视频无法播放。2018-03-21
                                    CameraLogic.setEnterCameraSource(source, true);
                                }
                            }
                        }
                    }

                    if (compare) {
                        if (null != mModel) {
                            // 包名变化
                            final String pkgName = getCurPkgName();
                            if (!packetName.equals(pkgName)) {
                                mModel.getCurPkgName().setVal(packetName);
                                mModel.getOldPkgName().setVal(pkgName);
                            }

                            // 类名变化
                            final String clsName = getCurClsName();
                            if (!TextUtils.isEmpty(className)) {
                                if (!className.equals(clsName)) {
                                    mModel.getCurClsName().setVal(className);
                                    mModel.getOldClsName().setVal(clsName);
                                }
                            }
                        }
                        ret = onSourceAction(source);
                    }
                }
            }
        } finally {
            mLock.unlock();
        }
        return ret;
    }

    /**
     * 打开后台模式
     */
    public static void onOpenBackgroundSource() {
        final int source = getLastNoPoweroffRealSource();
        if (Define.Source.SOURCE_NONE != source) {
            onOpenBackgroundSource(source);
        }
    }

    /**
     * 打开后台模式
     */
    public static void onOpenBackgroundSource(int source) {
        // 弹出有效的模式
        mLock.lock();
        try {
            if (source == getCurBackSource()) {
                LogUtils.d(TAG, "Open to background source = " + Define.Source.toString(source) + " the same.");
            } else {
                BaseLogic curLogic = ModuleManager.getFrontLogic();
                if (null != curLogic) {
                    //hzy add for bug ruipai tack when entry FM from Canbus,cann go back to FM 20200106
                    //modify by huwei 171202;后台为silent时允许打开后台;
                    // fix 首次开机后台是silent模式,点击音乐挂件播放无法打开音乐后台;
                    if (PowerManager.isRuiPai()) {
                        if (curLogic.isSource() && mModel.getCurBackSource().getVal() != Define.Source.SOURCE_SILENT
                                && !"com.wwc2.canbusapk".equals(getOldPkgName())) {
                            LogUtils.d(TAG, "Open  to background source failed, curSource = " + Define.Source.toString(curLogic.source()) + ", is mode source.");
                            return;
                        }
                    } else {
                        if (curLogic.isSource() && mModel.getCurBackSource().getVal() != Define.Source.SOURCE_SILENT) {
                            LogUtils.d(TAG, "Open  to background source failed, curSource = " + Define.Source.toString(curLogic.source()) + ", is mode source.");
                            return;
                        }
                    }
                }

                BaseLogic logic = ModuleManager.getLogicBySource(source);
                if (null == logic) {
                    LogUtils.d(TAG, "Open to background source = " + Define.Source.toString(source) + " null exception.");
                } else {
                    if (logic.isPoweroffSource()) {
                        final int _source = source;
                        source = getLastNoPoweroffRealSource();
                        LogUtils.d(TAG, "Open to background source = " + Define.Source.toString(_source) +
                                " is poweroff source, and change to last not poweroff background source = " + Define.Source.toString(source));
                        logic = ModuleManager.getLogicBySource(source);
                    }
                    if (null != logic) {
                        if (logic.isSource()) {
                            // 开始打开后台的操作
                            LogUtils.d(TAG, "Open to background source = " + Define.Source.toString(source) +
                                    ", current source = " + Define.Source.toString(getCurSource()) +
                                    ", current background source = " + Define.Source.toString(getCurBackSource()));

                            // 压入Source列表中
                            pushSource(source);

                            // 销毁后台资源
                            BaseLogic curBackLogic = ModuleManager.getBackLogic();
                            if (null != curBackLogic) {
                                curBackLogic.onStop();
                            }

                            // 修改后台
                            mModel.getOldBackSource().setVal(getCurBackSource());
                            mModel.getCurBackSource().setVal(source);

                            // 打开后台
                            if (null != logic) {
                                logic.onStart();
                                logic.onResume();
                            }
                        } else {
                            LogUtils.d(TAG, "Open to background source = " + Define.Source.toString(source) + " is not source.");
                        }
                    }
                }

            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 移除指定后source， 对比指定source
     */
    public static boolean onRemoveCompareSource(int source, int remove) {
        boolean ret = false;
        mLock.lock();
        try {
            if (Define.Source.SOURCE_NONE != source) {
                removeSource(remove);
                ret = onSourceAction(source);
            } else {
                LogUtils.w(TAG, "onRemoveCompareSource##failed, source = " + Define.Source.toString(source) +
                        ", remove = " + Define.Source.toString(remove));
            }
        } finally {
            mLock.unlock();
        }
        return ret;
    }

    /**
     * 移除指定后source，进入指定source
     */
    public static boolean onRemoveChangeSource(int source, int remove) {
        boolean ret = false;
        mLock.lock();
        try {
            if (Define.Source.SOURCE_NONE != source) {
                removeSource(remove);
                ret = onChangeSource(source);
            } else {
                LogUtils.w(TAG, "onRemoveChangeSource##failed, source = " + Define.Source.toString(source) +
                        ", remove = " + Define.Source.toString(remove));
            }
        } finally {
            mLock.unlock();
        }
        return ret;
    }

    /**
     * 移除指定后source，进入指定包名程序
     */
    public static boolean onRemoveChangePackage(String pkgName, String clsName, int remove) {
        boolean ret = false;
        LogUtils.d(TAG, "onRemoveChangePackage##mLock.lock() before.");
        mLock.lock();
        LogUtils.d(TAG, "onRemoveChangePackage##mLock.lock() after.");
        try {
            if (ApkUtils.isAPKExist(mContext, pkgName)) {
                LogUtils.d(TAG, pkgName + " exist, and remove " + Define.Source.toString(remove) + " start.");
                removeSource(remove);
                LogUtils.d(TAG, "onRemoveChangePackage##remove " + Define.Source.toString(remove) + " success, and run " + pkgName + ".");
                ret = runApk(pkgName, clsName, null, false);
                LogUtils.d(TAG, "onRemoveChangePackage##run " + pkgName + " over.");
                if (ret) {
                    BaseLogic logic = ModuleManager.getLogicByPacketName(pkgName);
                    if (null != logic) {
                        int source = logic.source();
                        if (Define.Source.SOURCE_NONE == source) {
                            source = Define.Source.SOURCE_THIRDPARTY;
                        }
                        LogUtils.d(TAG, "onRemoveChangePackage##onSourceAction start " + Define.Source.toString(source) + ".");
                        ret = onSourceAction(source);
                        LogUtils.d(TAG, "onRemoveChangePackage##onSourceAction end " + Define.Source.toString(source) + ".");
                    } else {
                        LogUtils.d(TAG, "onRemoveChangePackage##package's logic is null exception, pkgName = " + pkgName +
                                ", remove = " + Define.Source.toString(remove));
                    }
                } else {
                    LogUtils.d(TAG, "onRemoveChangePackage##run apk failed, pkgName = " + pkgName +
                            ", remove = " + Define.Source.toString(remove));
                }
            } else {
                LogUtils.d(TAG, "onRemoveChangePackage##apk not exist, pkgName = " + pkgName +
                        ", remove = " + Define.Source.toString(remove));
            }
        } finally {
            LogUtils.d(TAG, "onRemoveChangePackage##mLock.unlock() before.");
            mLock.unlock();
            LogUtils.d(TAG, "onRemoveChangePackage##mLock.unlock() after.");
        }
        LogUtils.d(TAG, "onRemoveChangePackage##ret = " + ret);
        return ret;
    }

    /**
     * 返回上一Source
     *
     * @param exceptArray 弹出Source，直到不是exceptArray为止
     * @return true 成功， false 失败
     */
    public static boolean onPopSource(int... exceptArray) {
        boolean ret = false;
        mLock.lock();
        try {
            int source = popSourceExceptArray(exceptArray);
            if (Define.Source.SOURCE_NONE == source) {
                source = getFirstPoweronDefaultSource();
            }
            ret = onChangeSource(source);
        } finally {
            mLock.unlock();
        }
        return ret;
    }

    /**
     * 返回上一不是关机的source
     *
     * @param exceptArray 弹出开机Source，直到不是exceptArray为止
     * @return 上一不是关机的source
     */
    public static boolean onPopSourceNoPoweroff(int... exceptArray) {
        boolean ret = false;
        mLock.lock();
        try {
            int source = popSourceNoPoweroffExceptArray(exceptArray);
            if (Define.Source.SOURCE_NONE == source) {
                source = getFirstPoweronDefaultSource();
            }
            ret = onChangeSource(source);
        } finally {
            mLock.unlock();
        }
        return ret;
    }

    /**
     * 换顺序切换source
     */
    static long lastModeTime = 0;
    public static boolean onChangeMode() {
        boolean ret = false;
        long curModeTime = SystemClock.uptimeMillis();
        if (curModeTime - lastModeTime < 1000) {
            return ret;//防止快速切源
        }
        lastModeTime = curModeTime;

        mLock.lock();
        try {
            int source = Define.Source.SOURCE_NONE;
            List<Integer> list = getChangeSourceList();
            if (null != list) {
                final int size = list.size();
                if (size > 0) {

                } else {
                    LogUtils.d(TAG, "list's size is 0.");
                    return ret;
                }
            } else {
                LogUtils.d(TAG, "list is null exception.");
                return ret;
            }

            boolean contains = false;
            Integer curSource = getCurSource();
            if (Define.Source.SOURCE_NONE != curSource) {
                if (list.contains(curSource)) {
                    contains = true;
                } else {
                    curSource = getLastSource();
                    if (list.contains(curSource)) {
                        contains = true;
                    } else {
                        LogUtils.d(TAG, "current source[" + Define.Source.toString(curSource) + "] is not on the list.");
                    }
                }
            } else {
                LogUtils.d(TAG, "current source is " + Define.Source.toString(curSource));
            }

            if (contains) {
                // 找到当前source下标
                int curIndex = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (curSource == list.get(i)) {
                        curIndex = i;
                        break;
                    }
                }

                // 调整列表
                List<Integer> listMode = new ArrayList<Integer>();
                if (-1 != curIndex) {
                    // 调整当前source下标
                    curIndex++;
                    if (curIndex >= list.size()) {
                        curIndex = 0;
                    }

                    for (int i = curIndex; i < list.size(); i++) {
                        listMode.add(list.get(i));
                    }
                    for (int i = 0; i < curIndex; i++) {
                        listMode.add(list.get(i));
                    }
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        listMode.add(list.get(i));
                    }
                }

                // 遍历列表，得到有效的目的source
                for (int i = 0; i < listMode.size(); i++) {
                    int sourceID = listMode.get(i);
                    BaseLogic logic = ModuleManager.getLogicBySource(sourceID);
                    if (null != logic) {
                        if (logic.available()) {
                            source = sourceID;
                            break;
                        }
                    }
                }
            }

            if (Define.Source.SOURCE_NONE == source) {
                source = list.get(0);
            }

            ret = onChangeSource(source);
        } finally {
            mLock.unlock();
        }
        return ret;
    }

    /**
     * 退出指定包名的APK
     *
     * @param pkgName 退出的指定包名
     * @return 成功返回true，失败返回false
     */
    public static boolean onExitPackage(String pkgName) {
        boolean ret = false;
        mLock.lock();
        try {
            if (ApkUtils.isAPKExist(mContext, pkgName)) {
                BaseLogic logic = ModuleManager.getLogicByPacketName(pkgName);
                if (null != logic) {
                    if (logic.isSource()) {
                        final int source = logic.source();
                        final int curSource = getCurSource();
                        final int backSource = getCurBackSource();

                        //hzy add for bug ruipai tack when entry FM from Canbus,cann go back to FM 20200106
                        if ("com.wwc2.canbusapk".equals(getOldPkgName())
                                && curSource == source && PowerManager.isRuiPai()) {
                            onOpenBackgroundSource(Define.Source.SOURCE_SILENT);
                        } else if (curSource == source) {
                            LogUtils.d(TAG, "onExitPackage##exit package = " + pkgName +
                                    ", source = " + Define.Source.toString(source) +
                                    ", curSource = " + Define.Source.toString(curSource) +
                                    ", backSource = " + Define.Source.toString(backSource));
                            onChangeSource(Define.Source.SOURCE_SILENT);
                        } else if (backSource == source) {
                            onOpenBackgroundSource(Define.Source.SOURCE_SILENT);
                        } else {
                            //logic.onStop();//快速连续操作在文件夹点歌，返回后 到了文件管理器还能继续播放，下面这行是相关log
                            //onExitPackage package = com.wwc2.audio, source = SOURCE_AUDIO, curSource = SOURCE_THIRDPARTY, backSource = SOURCE_SILENT
                            LogUtils.d(TAG, "onExitPackage##not any exit package = " + pkgName +
                                    ", source = " + Define.Source.toString(source) +
                                    ", curSource = " + Define.Source.toString(curSource) +
                                    ", backSource = " + Define.Source.toString(backSource));
                        }
                    } else {
                        LogUtils.d(TAG, "onExitPackage##" + pkgName + " is not source.");
                    }
                } else {
                    LogUtils.d(TAG, "onExitPackage##" + pkgName + "'s logic is null.");
                }

                ret = true;
                Packet packet = new Packet();
                packet.putString("pkgName", pkgName);
                BaseLogic l = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                if (null != l) {
                    l.Notify(SystemPermissionInterface.MAIN_TO_APK.KILL_PROCCESS, packet);
                }
            } else {
                LogUtils.w(TAG, "onExitPackage## kill process failed, " + pkgName + " is not exist.");
            }
        } finally {
            mLock.unlock();
        }
        return ret;
    }

    /**
     * 得到模式切换列表，提供可配置模式切换优先级的能力
     *
     * @return 模式切换列表
     */
    private static List<Integer> getChangeSourceList() {
        List<Integer> list = new ArrayList<>();

        list.add(Define.Source.SOURCE_NAVI);
        list.add(Define.Source.SOURCE_RADIO);
        list.add(Define.Source.SOURCE_BLUETOOTH);
        list.add(Define.Source.SOURCE_AUDIO);
        list.add(Define.Source.SOURCE_VIDEO);
        list.add(Define.Source.SOURCE_DVD);
        list.add(Define.Source.SOURCE_AUX);
        list.add(Define.Source.SOURCE_TV);
        list.add(Define.Source.SOURCE_IPOD);
        list.add(Define.Source.SOURCE_PHONELINK);

        return list;
    }

    /**
     * 得到使能的模式切换
     *
     * @return 模式切换列表
     */
    public static List<Integer> getEnableChangeSourceList() {
        List<Integer> list = getChangeSourceList();
        List<Integer> ret = new ArrayList<Integer>();

        // mode
        if (null != list) {
            for (int i = 0; i < list.size(); i++) {
                final Integer source = list.get(i);
                BaseLogic logic = ModuleManager.getLogicBySource(source);
                if (null != logic) {
                    if (logic.enable()) {
                        ret.add(source);
                    }
                }
            }
        }

        return ret;
    }

    /**
     * 模式的动作
     */
    protected static boolean onSourceAction(int source) {
        boolean ret = false;

        // 判断当前已处于该SOURCE，则忽略该请求
        if (source == mModel.getCurSource().getVal()) {
            LogUtils.w(TAG, "adjust source ignore, the same source = " + Define.Source.toString(source));
            return ret;
        }

        // 判断当前SOURCE如果是SOURCE_NONE, 则忽略该请求
        if (Define.Source.SOURCE_NONE == source) {
            LogUtils.w(TAG, "adjust source ignore, the none source = " + Define.Source.toString(source));
            return ret;
        }

        // 如果切入的模块不在有效状态，则忽略该请求
        BaseLogic curLogic = ModuleManager.getLogicBySource(source);
        if (null != curLogic) {
            if (!curLogic.available()) {
                LogUtils.w(TAG, "adjust source ignore, is not available, ingore: " + Define.Source.toString(source));
                return ret;
            }
        } else {
            LogUtils.e(TAG, "adjust source ignore, the event control null point exception, ingore: " + Define.Source.toString(source));
            return ret;
        }

        // 打印source信息
        printLog("Source changed before");

        // 设置当前SOURCE
        final int curSource = getCurSource();
        final int curBackSource = getCurBackSource();

        // 设置当前source
        mModel.getCurSource().setVal(source);
        if (curLogic.isSource()) {
            mModel.getCurBackSource().setVal(source);
        }

        // 设置之前source
        mModel.getOldSource().setVal(curSource);
        if (curLogic.isSource()) {
            mModel.getOldBackSource().setVal(curBackSource);
        }

        // 打印source信息
        printLog("Source changed after");

        // 压入Source列表中

        pushSource(source);

        // 执行逻辑生命周期判断
        if (PowerManager.isPortProject()) {
            if (getCurSource() != Define.Source.SOURCE_NAVI) {//竖屏、旋转屏项目导航时不切源，避免分屏时会导致视频退出。
                onLifeAction(getCurSource(), getOldSource(), getOldBackSource());
            }
        } else {
            onLifeAction(getCurSource(), getOldSource(), getOldBackSource());
        }

        String soureName = Define.Source.toString(getCurBackSource());
        SystemProperties.set("user.current.source", soureName);

        // 执行开背光
        onOpenBacklight(getOldSource(), getCurSource());

        ret = true;
        return ret;
    }

    /**
     * 根据传入的当前SOURCE以及上一SOURCE，进行模式的生命动作
     *
     * @param curSource     当前SOURCE
     * @param oldSource     上一SOURCE
     * @param oldBackSource 后台SOURCE
     */
    protected static void onLifeAction(final int curSource, final int oldSource, final int oldBackSource) {
        // 得到旧SOURCE对应的Logic对象
        BaseLogic curBaseLogic = ModuleManager.getLogicBySource(curSource);
        BaseLogic oldBaseLogic = ModuleManager.getLogicBySource(oldSource);
        BaseLogic oldBackBaseLogic = ModuleManager.getLogicBySource(oldBackSource);

        // 注意：要保证传入当前SOURCE，之前SOURCE，以及之前后台SOURCE是对的
        String str = "enter logic: ";
        str += "curSource = " + Define.Source.toString(curSource) + ", oldSource = "
                + Define.Source.toString(oldSource) + ", oldBackSource = " + Define.Source.toString(oldBackSource);
        LogUtils.d(TAG, str);

        // 打印调用堆栈信息
        RuntimeException e = new RuntimeException("Log: stack info");
        e.fillInStackTrace();
        LogUtils.i(TAG, "onLifeAction stack, ", e);

        if (null == oldBaseLogic) {
            if (null == curBaseLogic) {
                // 找不到事件对象，不做处理
                Log.d(TAG, "OldSource event control object is null, oldSource = " + Define.Source.toString(oldSource));
            } else {
                // 从无源切入到指定的源，执行指定的源的onStart、onResume操作
                curBaseLogic.onStart();
                curBaseLogic.onResume();
            }
        } else {
            if (null == curBaseLogic) {
                // 从旧源切入到空的事件对象，执行旧源的onPause, onStop操作
                oldBaseLogic.onPause();
                oldBaseLogic.onStop();
            } else {
                if (oldBaseLogic.isSource()) {
                    if (curBaseLogic.isSource()) {
                        // 从真源切入真源，执行旧源的onPause、onStop操作
                        oldBaseLogic.onPause();
                        oldBaseLogic.onStop();

                        if (oldSource == Define.Source.SOURCE_VIDEO && !curBaseLogic.isPoweroffSource()) {
                            /*-begin-20180424-ydinggen-modify-倒车时SystemPermission被LowMemmorykiller杀掉时，会导致出现两个声音-*/
                            // 由于视频播放是在APK中处理，Main无法控制，当从视频切到其他源时，直接杀掉。防止出现快速切源时，会出现在其它源时视频仍在播放。
                            final String pkgName = oldBaseLogic.getAPKPacketName();
//                            Packet packet = new Packet();
//                            packet.putString("pkgName", pkgName);
//                            BaseLogic l = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
//                            if (null != l) {
//                                LogUtils.d(TAG, "killvideo apk");
//                                l.Notify(SystemPermissionInterface.MAIN_TO_APK.KILL_PROCCESS, packet);
//                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtils.d(TAG, "killvideo apk");
                                    STM32MCUDriver.killProcess(mContext, pkgName);
                                }
                            }).start();
                            /*-end-20180424-ydinggen-modify-倒车时SystemPermission被LowMemmorykiller杀掉时，会导致出现两个声音-*/
                        }

                        // 执行当前源的onStart、onResume操作
                        curBaseLogic.onStart();
                        curBaseLogic.onResume();
                    } else {
                        // 从真源切入伪源，执行旧源的onPause操作
                        oldBaseLogic.onPause();
                        if (oldBaseLogic.isPoweroffSource() && curSource == Define.Source.SOURCE_THIRDPARTY) {
                            //休眠再上ACC，快速按方控上的开关机键，会出现主界面操作任何按键无作用，源是关机，但界面是第三方。2018-03-20
                            LogUtils.e(TAG, "old source is poweroff source!");
                            oldBaseLogic.onStop();
                        }

                        // 执行当前源的onStart、onResume操作
                        curBaseLogic.onStart();
                        curBaseLogic.onResume();

                        /*-begin-20180424-ydinggen-modify-上ACC，在视频快速倒车时，会出现两个声音-*/
                        if (oldSource == Define.Source.SOURCE_VIDEO && (curSource == Define.Source.SOURCE_CAMERA
                                || curSource == Define.Source.SOURCE_LAUNCHER)) {//修改bug14432。2018-12-27（不改播放器）风险：在列表按home再进视频会直接进播放界面。
                            final String pkgName = oldBaseLogic.getAPKPacketName();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtils.d(TAG, "killvideo apk 2");
                                    STM32MCUDriver.killProcess(mContext, pkgName);
                                }
                            }).start();
                        } else {
                            LogUtils.d(TAG, "killvideo apk 3");
                        }
                        /*-end-20180424-ydinggen-modify-上ACC，在视频快速倒车时，会出现两个声音-*/
                    }
                } else {
                    if (curBaseLogic.isSource()) {
                        // 从伪源切入真源，执行伪源的onPause、onStop操作
                        oldBaseLogic.onPause();
                        oldBaseLogic.onStop();

                        // 判断后台源是否发生变化
                        if (null != oldBackBaseLogic) {
                            if (oldBackBaseLogic != curBaseLogic) {
                                // 如果后台源存在，则需要执行后台源的onStop操作、当前源的onStart操作
                                oldBackBaseLogic.onStop();

                                //在酷我音乐，语音打开收音机，会出现收音机无声音。原因：语音结束后，酷我音乐会在切源后才会抢点焦点。
                                //延时切源，另外需MCU收到切源时mute大概1s时间。2018-01-02
                                if (!mStopSourceTimerQueue.isOver()) {
                                    mStopSourceTimerQueue.stop();
                                }

                                if (true || curBaseLogic.isPoweroffSource() || oldBaseLogic.isPoweroffSource()) {
                                    curBaseLogic.onStart();
                                    // 执行当前源的onResume操作
                                    curBaseLogic.onResume();
                                } else {
                                    Packet packet = new Packet();
                                    packet.putInt("curSource", curSource);
                                    mStopSourceTimerQueue.add(1000, packet, new BaseCallback() {
                                        @Override
                                        public void onCallback(int nId, Packet packet) {
                                            if (null != packet) {
                                                LogUtils.d(TAG, "oldBackSource----curSource=" + curSource);
                                                BaseLogic curLogic = ModuleManager.getLogicBySource(packet.getInt("curSource"));
                                                // 执行当前源的onStart、onResume操作
                                                curLogic.onStart();
                                                curLogic.onResume();
                                            }
                                        }
                                    });
                                    mStopSourceTimerQueue.start();
                                }
                            }
                        } else {
                            // 如果之前后台源不存在，则需要执行当前源的onStart操作
                            curBaseLogic.onStart();
                            // 执行当前源的onResume操作
                            curBaseLogic.onResume();
                        }
                    } else {
                        // 从伪源切入伪源，执行伪源的onPause、onStop操作
                        // 从伪源切入倒车，不执行伪源的操作，否则在设置时进倒车会切到主页。2017-11-15
                        // 原因：SettingsActivity继承BaseActivity会finish。
                        if (curSource != Define.Source.SOURCE_CAMERA) {
                            oldBaseLogic.onPause();
//                            oldBaseLogic.onStop();//设置->第三方源
                        }
                        // 执行当前源的onStart、onPause操作
                        curBaseLogic.onStart();
                        curBaseLogic.onResume();
                    }
                }
            }
        }
    }

    /**
     * 模式切换开背光
     */
    protected static void onOpenBacklight(int oldVal, int newVal) {
        boolean open = false;
        BaseLogic oldLogic = ModuleManager.getLogicBySource(oldVal);
        BaseLogic newLogic = ModuleManager.getLogicBySource(newVal);
        if (null == oldLogic) {
            if (null == newLogic) {
                // 异常调用或者异常情况
                LogUtils.e(TAG, Define.Source.toString(oldVal) + " object is null exception(oldVal), " +
                        Define.Source.toString(newVal) + " object is null exception(newVal).");
                return;
            } else {
                // 能否打开背光判断
                if (newLogic.isScreenoffSource()) {
                    // 切入是关屏的源
                    LogUtils.i(TAG, Define.Source.toString(newVal) + " is screen off source, not need open back light.");
                } else {
                    // 切入不是关屏的源
                    open = true;
                }
            }
        } else {
            if (null == newLogic) {
                // 异常调用或者异常情况
                LogUtils.e(TAG, Define.Source.toString(newVal) + " object is null exception(newVal).");
                return;
            } else {
                // 能否打开背光判断
                if (newLogic.isScreenoffSource()) {
                    // 切入是关屏的源
                    LogUtils.i(TAG, Define.Source.toString(newVal) + " is screen off source, not need open back light.");
                } else {
                    // 切入不是关屏的源
                    if (oldLogic.handleBacklightOn()) {
                        // 旧源自己处理开背光
                        LogUtils.i(TAG, Define.Source.toString(oldVal) + " handle back light on self.");
                        return;//关屏后，ACC深度休眠再ACC ON会出现关屏。
                    } else {
                        // 切源要打开背光
                        if (oldLogic.isScreenoffSource() || newVal == Define.Source.SOURCE_CAMERA) {//关屏状态下倒车需开屏
                            open = true;
                        } else {
                            //如果之前源是开屏的，则不处理。在关屏状态下，按MENU和BACK不用开屏。
                            LogUtils.i(TAG, Define.Source.toString(oldVal) + " handle back return light.");
                            return;
                        }
                    }
                }
            }
        }

        // 打开背光
        if (open) {
            LogUtils.d(TAG, Define.Source.toString(oldVal) + " to " + Define.Source.toString(newVal) + ", should open the back light.");
            BacklightDriver.Driver().open();
        } else {//进关机界面，增加关屏功能。
            String client = VersionDriver.Driver().getClientProject();
            LogUtils.d(TAG, "SourceManager---client="+client);
            if (client.equals(BaseClientDriver.CLIENT_AM) || FactoryDriver.Driver().getCloseScreen()) {//只有爱民的需要关闭时钟
                BacklightDriver.Driver().close();//YDG
            }
        }
    }

    /**
     * 把指定的Source压入到列表中
     */
    protected static boolean pushSource(int source) {
        boolean ret = false;
        int size = mSourceList.size();
        LogUtils.d(TAG, "pushSource##source = " + Define.Source.toString(source) + ", size = " + size + " start.");
        BaseLogic logic = ModuleManager.getLogicBySource(source);
        if (null != logic) {
            if (logic.isStackSource()) {
                /**相同源不用压入*/
                if (size > 0) {
                    final int index = size - 1;
                    final Integer temp = mSourceList.get(index);
                    if (source != temp) {
                        mSourceList.add(source);
                    }
                } else {
                    mSourceList.add(source);
                }

                /**保证切换列表中最大不超过{@link #getSourceListMax()} */
                size = mSourceList.size();
                final int max = getSourceListMax();
                if (size > max) {
                    final int index = size - max;
                    for (int i = 0; i < index; i++) {
                        mSourceList.remove(0);
                    }
                }
                ret = true;

                /**debug out.*/
                String string = "push source[" + Define.Source.toString(source) + "], mSourceList:";
                for (int i = 0; i < mSourceList.size(); i++) {
                    if (0 == i) {
                        string += Define.Source.toString(mSourceList.get(i));
                    } else {
                        string += ", " + Define.Source.toString(mSourceList.get(i));
                    }
                }
                LogUtils.d(TAG, string);
            } else {
                LogUtils.w(TAG, "push source, " + Define.Source.toString(source) + " is not stack source.");
            }
        } else {
            LogUtils.e(TAG, "push source, " + Define.Source.toString(source) + " is null object exception.");
        }
        LogUtils.d(TAG, "pushSource##source = " + Define.Source.toString(source) + ", size = " + mSourceList.size() + " end.");
        return ret;
    }

    /**
     * 获取最近的源(列表中的都是真源）
     */
    protected static int getLastSource() {
        int ret = Define.Source.SOURCE_NONE;
        final int size = mSourceList.size();
        if (size > 0) {
            ret = mSourceList.get(size - 1);
        }
        return ret;
    }

    /**
     * 获取最近的不是关机的真源，默认为{@link com.wwc2.common_interface.Define.Source#SOURCE_SILENT}
     */
    public static int getLastNoPoweroffRealSource() {
        int ret = Define.Source.SOURCE_SILENT;
        final int size = mSourceList.size();
        if (size > 0) {
            for (int i = (size - 1); i >= 0; i--) {
                final int source = mSourceList.get(i);
                BaseLogic logic = ModuleManager.getLogicBySource(source);
                if (null != logic) {
                    if (!logic.isPoweroffSource()) {
                        ret = source;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 移除保存的source
     *
     * @param source 需要移除的source
     * @return 成功返回true，失败返回false
     */
    protected static boolean removeSource(int source) {
        boolean ret = false;
        Integer object = source;
        final int max = mSourceList.size();
        int loop = 0;
        if (Define.Source.SOURCE_NONE != source) {
            while (mSourceList.contains(object)) {
                ret = true;
                mSourceList.remove(object);
                if ((++loop) > max) {
                    LogUtils.d(TAG, "removeSource##use List.contains(object) and remove error.");
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 弹出Source
     */
    protected static int popSource() {
        int ret = Define.Source.SOURCE_NONE;
        final int size = mSourceList.size();
        if (size > 0) {
            final int location = size - 1;
            ret = mSourceList.get(location);
            mSourceList.remove(location);
        }
        return ret;
    }

    /**
     * 弹出Source，直到不是{@link #popSourceExceptArray(int...)}为止
     */
    protected static int popSourceExceptArray(int... exceptArray) {
        int ret = Define.Source.SOURCE_NONE;

        int size = mSourceList.size();
        if (size > 0) {
            final int last = mSourceList.get(size - 1);
            for (int i : exceptArray) {
                removeSource(i);
            }
            size = mSourceList.size();
            if (size > 0) {
                final int temp = mSourceList.get(size - 1);
                if (temp == last) {
                    ret = popSource();
                } else {
                    ret = temp;
                }
            }
        }
        return ret;
    }

    /**
     * 弹出不是关机的source，也就是获取开机source
     */
    public static int popSourceNoPoweroffExceptArray(int... exceptArray) {
        int ret = Define.Source.SOURCE_NONE;
        List<BaseLogic> list = ModuleManager.getPoweroffLogic();
        if (null != list) {
            final int size = list.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    BaseLogic logic = list.get(i);
                    if (null != logic) {
                        final int source = logic.source();
                        removeSource(source);
                    }
                }
                ret = popSourceExceptArray(exceptArray);
            }
        }
        return ret;
    }

    public static boolean isVideoTop() {
        boolean ret = false;
        if (PowerManager.isRuiPai()) {//针对锐派竖屏视频与导航分屏，进设置，再进视频无法播放的问题。bug21485，原因：系统在此操作下未发送activity变化的消息
            String curPacket = ApkUtils.getTopPackage(mContext);
            if ("com.wwc2.video".equals(curPacket)) {//锐派竖屏的才作此处理。2020-09-02
                ret = true;
            }
        }
        return ret;
    }
}
