package com.wwc2.main.accoff.driver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.navi.driver.NaviDriverable;
import com.wwc2.main.silent.SilentDefine;
import com.wwc2.main.upgrade.mcu.McuUpdateLogic;
import com.wwc2.navi_interface.NaviDefine;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * the base acc off driver.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public abstract class BaseAccoffDriver extends BaseMemoryDriver implements AccoffDriverable {

    /**TAG*/
    protected static final String TAG = "BaseAccoffDriver";

    /**deep sleep lock object.*/
    protected static final Lock mDeepSleepLock = new ReentrantLock();

    /**进入模式延时开背光*/
    private TimerQueue mDelayOpenBacklightTimerQueue = new TimerQueue();

    /**进入ACC OFF的包名*/
    protected String mEnterAccoffPkgName = null;

    /**进入ACC OFF的类名*/
    protected String mEnterAccoffClsName = null;

    /**进入ACC OFF的后台*/
    protected int mEnterAccoffBackSource = Define.Source.SOURCE_NONE;

    /**
     * the model data.
     */
    protected static class AccoffModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putInt("AccoffStep", mAccoffStep.getVal());
            packet.putBoolean("FastCamera", mFastCamera.getVal());
            return packet;
        }

        /**ACC OFF 阶段*/
        private MInteger mAccoffStep = new MInteger(this, "AccoffStepListener", AccoffListener.AccoffStep.STEP_WORK);
        public MInteger getAccoffStep() {
            return mAccoffStep;
        }

        /**快速倒车状态*/
        private MBoolean mFastCamera = new MBoolean(this, "FastCameraListener", false);
        public MBoolean getFastCamera() {
            return mFastCamera;
        }
    }

    @Override
    public BaseModel newModel() {
        return new AccoffModel();
    }

    /**进入深度睡眠*/
    protected abstract boolean goToDeepSleep();

    /**从深度睡眠状态唤醒*/
    protected abstract boolean wakeupFromDeepSleep();

    /**从深度睡眠状态远程唤醒*/
    protected abstract boolean wakeupSystemFromDeepSleepAccOff();

    /**获取倒车状态，用于快速倒车*/
    protected abstract boolean getCameraStatus();

    /**进入深度睡眠接口*/
    protected abstract boolean gotoDeepSleepSystem();

    /**从深度睡眠状态唤醒接口*/
    protected abstract boolean wakeupFromDeepSleepSystem();

    protected boolean mDeepSleepAction = false;

    /**
     * get the model object.
     */
    protected AccoffModel Model() {
        AccoffModel ret = null;
        BaseModel model = getModel();
        if (model instanceof AccoffModel) {
            ret = (AccoffModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**快速倒车下休眠了*/
    protected void gotoDeepSleepComming() {
        final boolean fastCamera = Model().getFastCamera().getVal();
        if (fastCamera) {
            LogUtils.d(TAG, "gotoDeepSleepComming##Fast camera go to deep sleep start.");
            LogUtils.d(TAG, "gotoDeepSleepComming##close back light start.");
            BacklightDriver.Driver().close();
            LogUtils.d(TAG, "gotoDeepSleepComming##close back light over.");
            LogUtils.d(TAG, "gotoDeepSleepComming##change source accoff start.");
            SourceManager.onChangeSource(Define.Source.SOURCE_ACCOFF);
            LogUtils.d(TAG, "gotoDeepSleepComming##change source accoff over.");
            LogUtils.d(TAG, "gotoDeepSleepComming##gotoDeepSleepSystem start.");
            gotoDeepSleepSystem();
            LogUtils.d(TAG, "gotoDeepSleepComming##gotoDeepSleepSystem over.");
            LogUtils.d(TAG, "gotoDeepSleepComming##Fast camera go to deep sleep over.");
        } else {
            LogUtils.d(TAG, "gotoDeepSleepComming##ignore the sleep message, FastCamera = " + fastCamera);
        }
    }

    /**从深度休眠来唤醒了*/
    protected void wakeupFromDeepSleepComming() {
        final int source = SourceManager.getCurSource();
        final int step = Model().getAccoffStep().getVal();
        LogUtils.d(TAG, "ACC on, source = " + Define.Source.toString(source) + ", step = " + AccoffListener.AccoffStep.toString(step));
        // 解决没有深度睡眠，就来了唤醒消息的问题
        //begin zhongyang.hu add for uart open 20170508  add McuManager.isUartOpen
        if (AccoffListener.AccoffStep.isDeepSleep(step) || (!McuManager.isUartOpen() && !McuUpdateLogic.getMCUupdateState())) {
         //end
            // 解决会来多次消息造成多次开机的问题
            if (Define.Source.SOURCE_ACCOFF == source || !McuManager.isUartOpen()) {
                long time = System.currentTimeMillis();
                LogUtils.d(TAG, "###wakeup comming, time = " + (System.currentTimeMillis() - time));

                LogUtils.d(TAG, "wakeupFromDeepSleepComming##mDeepSleepLock.lock() start.");
                mDeepSleepLock.lock();
                LogUtils.d(TAG, "wakeupFromDeepSleepComming##mDeepSleepLock.lock() over.");
                try {
                    //begin zhongyang.hu remove the fast CameraStatus  20170503
                    // 进入唤醒状态
                    /*
                    final boolean camera = getCameraStatus();
                    if (camera) {
                        wakeupFromDeepSleepSystem();
                        Model().getFastCamera().setVal(true);
                        SourceManager.onRemoveChangeSource(Define.Source.SOURCE_CAMERA, Define.Source.SOURCE_ACCOFF);
                        openBacklight(1000);
                        LogUtils.d(TAG, "fast camera, CurSource = " + Define.Source.toString(SourceManager.getCurSource()) +
                                ", BackgroundSource = " + Define.Source.toString(SourceManager.getCurBackSource()));
                    } else
                    */
                    //end zhongyang.hu remove the fast CameraStatus  20170503
                    {
                        accPowerOn("Acc on");
                    }
                    LogUtils.d(TAG, "wakeup action over, time = " + (System.currentTimeMillis() - time));
                } finally {
                    LogUtils.d(TAG, "wakeupFromDeepSleepComming##mDeepSleepLock.unlock() start.");
                    mDeepSleepLock.unlock();
                    LogUtils.d(TAG, "wakeupFromDeepSleepComming##mDeepSleepLock.unlock() over.");
                }
            } else {
                LogUtils.d(TAG, "ignore the wakeup message, source = " + Define.Source.toString(source));
            }
        } else {
            LogUtils.d(TAG, "ignore the wakeup message, step = " + AccoffListener.AccoffStep.toString(step));
        }
    }

    /**从深度休眠来远程唤醒了*/
    protected void wakeupFromDeepSleepAccOff() {
        final int source = SourceManager.getCurSource();
        final int step = Model().getAccoffStep().getVal();
        LogUtils.d(TAG, "ACC on, source = " + Define.Source.toString(source) + ", step = " + AccoffListener.AccoffStep.toString(step));
        // 解决没有深度睡眠，就来了唤醒消息的问题
        if (AccoffListener.AccoffStep.isDeepSleep(step) || (!McuManager.isUartOpen() && !McuUpdateLogic.getMCUupdateState())) {
            // 解决会来多次消息造成多次开机的问题
            if (Define.Source.SOURCE_ACCOFF == source || !McuManager.isUartOpen()) {
                long time = System.currentTimeMillis();
                LogUtils.d(TAG, "###wakeup comming, time = " + (System.currentTimeMillis() - time));

                LogUtils.d(TAG, "wakeupFromDeepSleepAccOff##mDeepSleepLock.lock() start.");
                mDeepSleepLock.lock();
                LogUtils.d(TAG, "wakeupFromDeepSleepAccOff##mDeepSleepLock.lock() over.");
                try {
                    if (EventInputManager.getAcc()) {
                        accPowerOn("Acc on");
                    } else {
                        LogUtils.e("accPowerOn return acc off!");
                        wakeupSystemFromDeepSleepAccOff();
                    }
                    LogUtils.d(TAG, "wakeup action over, time = " + (System.currentTimeMillis() - time));
                } finally {
                    LogUtils.d(TAG, "wakeupFromDeepSleepAccOff##mDeepSleepLock.unlock() start.");
                    mDeepSleepLock.unlock();
                    LogUtils.d(TAG, "wakeupFromDeepSleepAccOff##mDeepSleepLock.unlock() over.");
                }
            } else {
                LogUtils.d(TAG, "ignore the wakeup message, source = " + Define.Source.toString(source));
            }
        } else {
            LogUtils.d(TAG, "ignore the wakeup message, step = " + AccoffListener.AccoffStep.toString(step));
        }
    }

    /**倒车状态来了*/
    protected void CameraComming(boolean camera) {
        // 退出倒车了
        final int source = SourceManager.getCurSource();
        final int step = Model().getAccoffStep().getVal();
        LogUtils.d(TAG, "ACC on, source = " + Define.Source.toString(source) + ", step = " + AccoffListener.AccoffStep.toString(step));
        // 倒车状态只在ACC ON下处理
        if (Define.Source.SOURCE_ACCOFF != source) {
            if (Model().getFastCamera().getVal()) {
                if (!camera) {
                    // 在快速倒车状态下
                    LogUtils.d(TAG, "ExitFastCamera##mDeepSleepLock.lock() start.");
                    mDeepSleepLock.lock();
                    LogUtils.d(TAG, "ExitFastCamera##mDeepSleepLock.lock() over.");
                    try {
                        accPowerOn("Fast camera out");
                    } finally {
                        LogUtils.d(TAG, "ExitFastCamera##mDeepSleepLock.unlock() start.");
                        mDeepSleepLock.unlock();
                        LogUtils.d(TAG, "ExitFastCamera##mDeepSleepLock.unlock() over.");
                    }
                } else {
                    LogUtils.d(TAG, "Fast camera, not handle camera on message");
                }
            } else {
                LogUtils.d(TAG, "APP camera, not handle system camera message, camera = " + camera);
            }
        } else {
            LogUtils.d(TAG, "ignore the camera message, source = " + Define.Source.toString(source));
        }
    }

    /**接上ACC，开机*/
    private void accPowerOn(String prefix) {
        Model().getFastCamera().setVal(false);
        wakeupFromDeepSleep();
        openBacklight(200);
        LogUtils.d(TAG, prefix + ", mEnterAccoffPkgName = " + mEnterAccoffPkgName +
                ", mEnterAccoffClsName = " + mEnterAccoffClsName +
                ", mEnterAccoffBackSource = " + Define.Source.toString(mEnterAccoffBackSource));

        boolean result = false;
        //修改：倒车－关ACC－20s左右再开ACC不进倒车。原因：在正好进入深睡眠将倒车设置为false，而串口未关前又收到串口的倒车。
        final boolean camera = EventInputManager.getCamera();
        if (camera) {
            result = SourceManager.onRemoveChangeSource(Define.Source.SOURCE_CAMERA, Define.Source.SOURCE_ACCOFF);
            if (result) {
                SourceManager.onOpenBackgroundSource();
            }
        }

        boolean poweroff = false;
        boolean available = false;
        if (!result) {
            if (!PowerManager.isRuiPai()) {//锐派需要记忆POWEROFF状态
                if (mEnterAccoffBackSource == Define.Source.SOURCE_SILENT) {
                    int mcuSource = McuManager.getMemorySource();
                    if (mcuSource != Define.Source.SOURCE_INVALID &&
                            mcuSource != Define.Source.SOURCE_NONE) {
                        mEnterAccoffBackSource = mcuSource;
                    }
                    LogUtils.d(TAG, "Acc on, mcuSource =" + mcuSource);
                }
            }

            if (ApkUtils.isAPKExist(getMainContext(), "com.baony.avm360")) {
                mEnterAccoffBackSource = Define.Source.SOURCE_SILENT;
            } else {
                Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
                if (client != null) {
                    String clientProject = client.getString("ClientProject");
                    if (clientProject != null && clientProject.equals("ch010_23")) {//马自达不作记忆
                        mEnterAccoffBackSource = Define.Source.SOURCE_SILENT;
                        mEnterAccoffPkgName = "";
                    }
                }
            }

            BaseLogic logic = ModuleManager.getLogicBySource(mEnterAccoffBackSource);
            if (null != logic) {
                poweroff = logic.isPoweroffSource();
                available = logic.available();
            }
            if ((poweroff && !PowerManager.isRuiPai()) || !available) {//锐派需要记忆POWEROFF状态
                // 开关ACC，要开机
                result = SourceManager.onPopSourceNoPoweroff();
                if (result) {
                    SourceManager.onOpenBackgroundSource();
                }
            }
            LogUtils.d(TAG, "Acc on, mEnterAccoffSource = " + Define.Source.toString(mEnterAccoffBackSource) +
                    ", mEnterAccoffPkgName = " + mEnterAccoffPkgName +
                    ", mEnterAccoffClsName = " + mEnterAccoffClsName +
                    ", poweroff = " + poweroff + ", available = " + available + ", result = " + result);
            if (!result) {
                if (mEnterAccoffPkgName != null && mEnterAccoffPkgName.equals(query_pkg_name(getMainContext()))/* ||//去掉第三方源记忆。2018-11-19
                        isThirdSource(mEnterAccoffBackSource, mEnterAccoffPkgName)*/) {
                    result = SourceManager.onRemoveChangePackage(mEnterAccoffPkgName, null, Define.Source.SOURCE_ACCOFF);
                }
                else {
                    result = SourceManager.onRemoveChangeSource(mEnterAccoffBackSource, Define.Source.SOURCE_ACCOFF);
                }
                if (!result) {
                    SourceManager.onPopSourceNoPoweroff();
                }
                SourceManager.onOpenBackgroundSource();
            }
        }
    }

    private boolean isThirdSource(int source, String pkgName) {
        boolean ret = false;
        if (source == Define.Source.SOURCE_THIRDPARTY) {
            ret = true;
            /*-begin-20180418-ydinggen-modify-第三方源记忆条件判断错误，导致的视频时深睡起来不会播放，当前源不是视频-*/
        } else if (source == Define.Source.SOURCE_SILENT &&
                !pkgName.equals(ModuleManager.getLogicByName(SilentDefine.MODULE).getAPKPacketName())) {
            ret = true;
        }
        /*-end-20180418-ydinggen-modify-第三方源记忆条件判断错误，导致的视频时深睡起来不会播放，当前源不是视频-*/
        return ret;
    }

    /**关闭背光*/
    protected void closeBacklight() {
        mDelayOpenBacklightTimerQueue.stop();
        BacklightDriver.Driver().close();
    }

    /**打开背光*/
    protected void openBacklight(int time) {
        mDelayOpenBacklightTimerQueue.stop();
        mDelayOpenBacklightTimerQueue.add(time, null, new BaseCallback() {
            @Override
            public void onCallback(int nId, Packet packet) {
                BacklightDriver.Driver().open();
            }
        });
        mDelayOpenBacklightTimerQueue.start();
    }

    public String query_pkg_name(Context context) {
        String ret = null;
//        ContentResolver resolver = context.getContentResolver();
//        if (null != resolver) {
//            Cursor cursor = resolver.query(com.wwc2.common_interface.Provider.ProviderColumns.CONTENT_URI, null, null, null, null);
//            if(cursor != null){
//                if(cursor.moveToFirst()){
//                    ret = cursor.getString(cursor.getColumnIndex(com.wwc2.navi_interface.Provider.NAVI_PKG_NAME()));
//                }
//                cursor.close();
//            }
//        }
        BaseDriver driver = ModuleManager.getLogicByName(NaviDefine.MODULE).getDriver();
        if (driver instanceof NaviDriverable) {
            NaviDriverable naviDriverable = (NaviDriverable) driver;
            ret = naviDriverable.getNavigationPacketName();
        }
        return ret;
    }
}
