package com.wwc2.main.accoff.driver;

import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * the base delay sleep acc off driver.
 *
 * @author wwc2
 * @date 2017/1/18
 */
public abstract class BaseDelaySleepAccoffDriver extends BaseAccoffDriver {

    /**延迟调用停止GPS时间*/ 
    private static final int DELAY_STOP_TIME_SECOND = 20;

    /**延迟调用睡眠时间*/ //zhongyang.hu modify old 25s
    private static final int DELAY_SLEEP_TIME_SECOND = 35;

    /**延时调用睡眠延时器*/
    private int mDelaySleepTimerID = 0;

    /**延时调用睡眠计数器*/
    private int mDelaySleepTimerCnt = 0;

    /**记录实时的唤醒状态*/
    protected boolean mWakeupRealTime = true;

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean accOff() {
        boolean ret = true;

        mEnterAccoffPkgName = SourceManager.getCurPkgName();
        mEnterAccoffClsName = SourceManager.getCurClsName();
        mEnterAccoffBackSource = SourceManager.getOldBackSource();
        BaseLogic oldLogic = ModuleManager.getLogicBySource(mEnterAccoffBackSource);
        //zhongyang.hu modify for oldLogic null object reference, cannot sleep 20171207
        if(null != oldLogic) {
            if ( !oldLogic.isSource() || (oldLogic.isPoweroffSource() && !PowerManager.isRuiPai())) {//锐派需要记忆POWEROFF状态
                mEnterAccoffBackSource = SourceManager.getLastNoPoweroffRealSource();
             }
        }
        //end
        LogUtils.d(TAG, "accoff(), mEnterAccoffPkgName = " + mEnterAccoffPkgName +
                ", CurSource = " + Define.Source.toString(SourceManager.getCurSource()) +
                ", getOldPkgName = " + SourceManager.getOldPkgName());
        //modify:launcher start when acc on from deep sleep,navi not save when deep sleep 2017-12-11
        String naviPkgName = query_pkg_name(getMainContext());
        if (!mEnterAccoffPkgName.equals(naviPkgName) && SourceManager.getOldPkgName() != null) {
            BaseLogic curLogic = ModuleManager.getLogicBySource(SourceManager.getCurSource());
            if (curLogic != null) {
                if ((curLogic.isPoweroffSource()  && !PowerManager.isRuiPai()) || !curLogic.isSource()) {//锐派需要记忆POWEROFF状态
                    if (SourceManager.getOldPkgName().equals(naviPkgName) &&
                            /*-begin-20180424-ydinggen-modify-退出导航，Acc深睡会自动启动导航-*/
                            ApkUtils.isProcessRunning(getMainContext(), naviPkgName) &&
                            naviPkgName.equals(ApkUtils.getTopPackage(getMainContext()))) {
                            /*-end-20180424-ydinggen-modify-退出导航，Acc深睡会自动启动导航-*/
                        mEnterAccoffPkgName = naviPkgName;
                    }
                }
            }
            LogUtils.d(TAG, "accoff(), query_pkg_name = " + naviPkgName +
                    ", getOldPkgName = " + SourceManager.getOldPkgName() +
                    ", getCurSource = " + Define.Source.toString(SourceManager.getCurSource()) +
                    ", naviExit = " + ApkUtils.isProcessRunning(getMainContext(), naviPkgName));
        }

        LogUtils.d(TAG, "accoff(), mEnterAccoffPkgName = " + mEnterAccoffPkgName +
                ", mEnterAccoffClsName = " + mEnterAccoffClsName +
                ", mEnterAccoffBackSource = " + Define.Source.toString(mEnterAccoffBackSource));
        closeBacklight();

        LogUtils.e("accoff()----mDeepSleepAction=" + mDeepSleepAction);
        if (!mDeepSleepAction) {
            //手机视频预览-深休眠后，碰撞-手机预览-开启ACC，会出现状态不对导致没有唤醒。（主要是状态不对）2020-05-19
            Model().getAccoffStep().setVal(AccoffListener.AccoffStep.STEP_ACCOFF_PAUSE);
        }
        TimerUtils.killTimer(mDelaySleepTimerID);
        mDelaySleepTimerID = TimerUtils.setTimer(getMainContext(), 1000, 1000, new Timerable.TimerListener() {
            @Override
            public void onTimer(int timerId) {
                mDelaySleepTimerCnt++;
                LogUtils.d(TAG, "accoff()##Ready to goto deep sleep, mDelaySleepTimerCnt = " + mDelaySleepTimerCnt);
                if (DELAY_SLEEP_TIME_SECOND < mDelaySleepTimerCnt) {
                    if (!getUsbMountedStatus() ||
                            (DELAY_SLEEP_TIME_SECOND + DELAY_STOP_TIME_SECOND) < mDelaySleepTimerCnt) {
                        TimerUtils.killTimer(mDelaySleepTimerID);
                        mDelaySleepTimerID = 0;
                        mDelaySleepTimerCnt = 0;

                        LogUtils.d(TAG, "accoff()##mDeepSleepLock.lock() start.");
                        mDeepSleepLock.lock();
                        LogUtils.d(TAG, "accoff()##mDeepSleepLock.lock() over.");
                        try {
                            mWakeupRealTime = false;
                            Model().getAccoffStep().setVal(AccoffListener.AccoffStep.STEP_ACCOFF_STOP);
                            goToDeepSleep();
                        } finally {
                            LogUtils.d(TAG, "accoff()##mDeepSleepLock.unlock() start.");
                            mDeepSleepLock.unlock();
                            LogUtils.d(TAG, "accoff()##mDeepSleepLock.unlock() over.");
                        }
                    }
                } else if (DELAY_SLEEP_TIME_SECOND == mDelaySleepTimerCnt) {
                    if (getUsbMountedStatus() && //解决360录像时关ACC会出现ACC ON时无法识别U盘的问题。
                            ApkUtils.isAPKExist(getMainContext(), "com.baony.avm360")) {//目前只针对360作处理。
                        writeTextFile("1", "/sys/class/gpiodrv/gpio_ctrl/hub_set");
                    } else {
                        TimerUtils.killTimer(mDelaySleepTimerID);
                        mDelaySleepTimerID = 0;
                        mDelaySleepTimerCnt = 0;

                        LogUtils.d(TAG, "accoff()##mDeepSleepLock.lock() start.");
                        mDeepSleepLock.lock();
                        LogUtils.d(TAG, "accoff()##mDeepSleepLock.lock() over.");
                        try {
                            mWakeupRealTime = false;
                            Model().getAccoffStep().setVal(AccoffListener.AccoffStep.STEP_ACCOFF_STOP);
                            goToDeepSleep();
                        } finally {
                            LogUtils.d(TAG, "accoff()##mDeepSleepLock.unlock() start.");
                            mDeepSleepLock.unlock();
                            LogUtils.d(TAG, "accoff()##mDeepSleepLock.unlock() over.");
                        }
                    }
                } else {
                    if (DELAY_STOP_TIME_SECOND == mDelaySleepTimerCnt) {
                        //LogUtils.d(TAG, "accoff()##Stop gps start zhongyang.");
                        //zhongyang.hu remove donnot close GPS,when acc ON ,GPS cannot dingwei ok.
//                        BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
//                        if (null != logic) {
//                            Packet packet = new Packet();
//                            packet.putInt("resume", 0);
//                            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.GPS_MODE, packet);
//                        }
                        LogUtils.d(TAG, "just do nothing ..");
                    }
                }
            }
        });

        return ret;
    }

    @Override
    public boolean accOn() {
        boolean ret = true;
        abortAccOff();
        final int step = Model().getAccoffStep().getVal();
        LogUtils.d(TAG, "accOn(), step = " + AccoffListener.AccoffStep.toString(step));
        if (AccoffListener.AccoffStep.isLightSleep(step)) {
            Model().getAccoffStep().setVal(AccoffListener.AccoffStep.STEP_ACCOFF_RESUME);
            openBacklight(600);
        } else {
            Model().getAccoffStep().setVal(AccoffListener.AccoffStep.STEP_ACCOFF_RESUME);
            Model().getAccoffStep().setVal(AccoffListener.AccoffStep.STEP_ACCOFF_START);
            Model().getAccoffStep().setVal(AccoffListener.AccoffStep.STEP_WORK);
        }
        //zhongyang.hu remove donnot close GPS,when acc ON ,GPS cannot dingwei ok.
        //LogUtils.d(TAG, "accOn()##Resume gps start zhongyang.");
//        BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
//        if (null != logic) {
//            Packet packet = new Packet();
//            packet.putInt("resume", 1);
//            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.GPS_MODE, packet);
//        }
       // LogUtils.d(TAG, "accOn()##Resume gps over.");
        //end
        return ret;
    }

    @Override
    public boolean abortAccOff() {
        boolean ret = false;
        LogUtils.d(TAG, "abort acc off...");
        if (!mWakeupRealTime) {
            ret = true;
            mWakeupRealTime = true;
        }
        if (0 != mDelaySleepTimerID) {
            ret = true;
            TimerUtils.killTimer(mDelaySleepTimerID);
            mDelaySleepTimerID = 0;
            mDelaySleepTimerCnt = 0;
        }

        LogUtils.d(TAG, "abort acc off...acc=" + EventInputManager.getAcc() + ", mDeepSleepAction=" +
                mDeepSleepAction + ", accStep=" + Model().getAccoffStep().getVal());
        if (EventInputManager.getAcc() && mDeepSleepAction) {//此处可不作处理。暂保留。
            //手机视频预览-深休眠后，碰撞-手机预览-开启ACC，会出现状态不对导致没有唤醒。（主要是没有收到系统唤醒广播）2020-05-19
            wakeupFromDeepSleep();
//            openBacklight(200);
        }
        return ret;
    }

    @Override
    protected void gotoDeepSleepComming() {
        final boolean fastCamera = Model().getFastCamera().getVal();
        if (fastCamera) {
            mWakeupRealTime = false;
        }
        super.gotoDeepSleepComming();
    }

    @Override
    protected void wakeupFromDeepSleepComming() {
        // 轻度休眠不处理系统wakeup消息
        final int source = SourceManager.getCurSource();
//        final int step = Model().getAccoffStep().getVal();
//        LogUtils.d(TAG, "ACC on, source = " + Define.Source.toString(source) + ", step = " + AccoffListener.AccoffStep.toString(step));
//        // 解决没有深度睡眠，就来了唤醒消息的问题
//        if (AccoffListener.AccoffStep.isDeepSleep(step)) {
            // 解决会来多次消息造成多次开机的问题
        if (Define.Source.SOURCE_ACCOFF == source) {
            LogUtils.d(TAG, "wakeupFromDeepSleepComming start, mWakeupRealTime = " + mWakeupRealTime);
            abortAccOff();
            LogUtils.d(TAG, "wakeupFromDeepSleepComming over, mWakeupRealTime = " + mWakeupRealTime);
        }
//        }

        super.wakeupFromDeepSleepComming();
    }

    @Override
    protected void wakeupFromDeepSleepAccOff() {
        // 轻度休眠不处理系统wakeup消息
        final int source = SourceManager.getCurSource();
        // 解决会来多次消息造成多次开机的问题
        if (Define.Source.SOURCE_ACCOFF == source) {
            LogUtils.d(TAG, "wakeupFromDeepSleepAccOff start, mWakeupRealTime = " + mWakeupRealTime);
            abortAccOff();
            LogUtils.d(TAG, "wakeupFromDeepSleepAccOff over, mWakeupRealTime = " + mWakeupRealTime);
        }

        super.wakeupFromDeepSleepAccOff();
    }

    private boolean getUsbMountedStatus() {
        boolean ret = false;

        for (int i = StorageDevice.USB; i < StorageDevice.USB3; i++) {
            if (StorageDevice.isDiskMounted(getMainContext(), i)) {
                LogUtils.d(TAG, "getUsbMountedStatus----deviceId=" + i);
                ret = true;
            }
        }
        LogUtils.d(TAG, "getUsbMountedStatus----ret=" + ret);
        return ret;
    }

    public void writeTextFile(String tivoliMsg, String fileName) {
        try {
            byte[] bMsg = tivoliMsg.getBytes();
            FileOutputStream fOut = new FileOutputStream(fileName);
            fOut.write(bMsg);
            fOut.getFD().sync();
            fOut.close();
        } catch (IOException e) {
            //throw the exception
        }
    }
}
