package com.wwc2.main.accoff;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.HandlerTimer;
import com.wwc2.main.accoff.driver.AccoffDriverable;
import com.wwc2.main.accoff.driver.MTK6737AccoffDriver;
import com.wwc2.main.bluetooth.EcarHelper;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.navi.driver.NaviDriverable;
import com.wwc2.navi_interface.NaviDefine;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * the acc off logic.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class AccoffLogic extends BaseLogic {

    /**TAG*/
    private static final String TAG = "AccoffLogic";


    private static final String CUSTOM_ACC_PROP= "ro.custom_acc";

    /**进入ACC OFF前的source*/
    private int mEnterAccoffSource = Define.Source.SOURCE_NONE;

    /**进入ACC OFF前的包名*/
    protected String mEnterAccoffPkgName = null;

    /**进入ACC OFF前的类名*/
    protected String mEnterAccoffClsName = null;

    /**light sleep lock object.*/
    protected static final Lock mLightSleepLock = new ReentrantLock();

    void sendCustomAccOnBroadCast(String Action,String value){
        if(!"Null".equals(Action)) {
            Intent intent = new Intent(Action);
            intent.putExtra("state", value);
            getMainContext().sendBroadcast(intent);
        }

        //通知至简Carplay状态
        Intent intentCarplay = new Intent("com.zjinnova.zlink");
        if (value.equals("ACC_OFF")) {
            intentCarplay.putExtra("command", "ACTION_EXIT");
        } else {
            intentCarplay.putExtra("command", "ACTION_ENTER");
        }
        getMainContext().sendBroadcast(intentCarplay);
    }

    /**acc off listener.*/
    //zhongyang.hu add for delay acc on event 20180326...
    Handler mHander =new Handler();
    Runnable mRunanle =new Runnable() {
        @Override
        public void run() {
            EcarHelper.sendMsgToEcar_2(getMainContext(),EcarHelper.ACCON);
            sendCustomAccOnBroadCast(SystemProperties.get(CUSTOM_ACC_PROP,"Null"),"ACC_ON");

            String startName = CommonDriver.Driver().getStartApkName();
            LogUtils.d(TAG, "getStartApkName---" + startName);
            if (!TextUtils.isEmpty(startName)) {
                String[] name = startName.split(",");
                if (name != null && name.length > 0) {
                    if (ApkUtils.isAPKExist(getMainContext(), name[0])) {
                        try {
                            if (name.length == 3 && !TextUtils.isEmpty(name[2])) {
                                ApkUtils.startServiceSafety(getMainContext(), name[2], name[0], name[2]);
                            } else if (name.length == 2 && !TextUtils.isEmpty(name[1])) {
                                ApkUtils.runApk(getMainContext(), name[0], name[1], null, true);
                            } else {
                                ApkUtils.runApk(getMainContext(), name[0], null, true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };
    //end

    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "AccoffStepListener changed, oldVal = " + AccoffListener.AccoffStep.toString(oldVal) +
                        ", newVal = " + AccoffListener.AccoffStep.toString(newVal));

            if (!AccoffStep.isAccoff(oldVal) && AccoffStep.isAccoff(newVal)) {

                mHander.removeCallbacks(mRunanle);
                EcarHelper.sendMsgToEcar_2(getMainContext(),EcarHelper.ACCOFF);
                sendCustomAccOnBroadCast(SystemProperties.get(CUSTOM_ACC_PROP,"Null"),"ACC_OFF");
                //zhongyang.hu add for diable wifi,when acc off 20180102
                if(PowerManager.isKSProject()) {
                    PowerManager.disableWifi();
                }
                //end
            }else if (AccoffStep.isAccoff(oldVal) && !AccoffStep.isAccoff(newVal)){
                PowerManager.openMobileData();
                mHander.postDelayed(mRunanle,6000);
            }
        }

        @Override
        public void FastCameraListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "FastCameraListener changed, oldVal = " + oldVal +
                    ", newVal = " + newVal);
        }
    };

    @Override
    public String getTypeName() {
        return "Accoff";
    }

    @Override
    public String getMessageType() {
        return AccoffDefine.MODULE;
    }

    @Override
    public boolean handleBacklightOn() {
        return true;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public boolean isPoweroffSource() {
        return true;
    }

    @Override
    public boolean isScreenoffSource() {
        return true;
    }

    @Override
    public boolean passive() {
        return true;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_ACCOFF;
    }

    @Override
    public String getAPKPacketName() {
        /*-begin-20180503-ydinggen-modify-关ACC增加黑屏界面,解决关ACC还会有界面图标显示-*/
        return "com.wwc2.black";
    }

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public boolean runApk() {
        BacklightDriver.Driver().close();
        /*-begin-20180503-ydinggen-modify-关ACC增加黑屏界面,解决关ACC还会有界面图标显示-*/
        return false;
    }

    @Override
    public BaseDriver newDriver() {
        return new MTK6737AccoffDriver();
    }

    /**
     * the driver interface.
     */
    protected AccoffDriverable Driver() {
        AccoffDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof AccoffDriverable) {
            ret = (AccoffDriverable) driver;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }

        getModel().bindListener(mAccoffListener);
        PowerManager.getModel().bindListener(mPowerListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        LogUtils.d(TAG, "onStart accoff().");
        LogUtils.d(TAG, "accOff##mLightSleepLock.lock() start.");
        mLightSleepLock.lock();
        LogUtils.d(TAG, "accOff##mLightSleepLock.lock() over.");
        try {
            Driver().accOff();
        } finally {
            LogUtils.d(TAG, "accOff##mLightSleepLock.unlock() start.");
            mLightSleepLock.unlock();
            LogUtils.d(TAG, "accOff##mLightSleepLock.unlock() over.");
        }

        /**记忆source*/
        int curSource = SourceManager.getCurSource();
        int oldSource = SourceManager.getOldSource();
        String curPkgName = SourceManager.getCurPkgName();
        String curClsName = SourceManager.getCurClsName();
        LogUtils.d(TAG, "Enter acc off, curSource = " + Define.Source.toString(curSource) +
                ", oldSource = " + Define.Source.toString(oldSource) +
                ", curPkgName = " + curPkgName +
                ", curClsName = " + curClsName);

        //去掉第三方源记忆。2018-11-19
//        if (oldSource == Define.Source.SOURCE_THIRDPARTY) {
//
//        } else {
            //ACC OFF后ON，需进入ACC OFF前的真源界面。YDG 2017-04-17
            // zhongyang.hu modify  for will need not to save  SOURCE_POWEROFF,when Accoff 2017.6.20
            BaseLogic oldLogic = ModuleManager.getLogicBySource(oldSource);
            if ((null != oldLogic && !oldLogic.isSource()) || (oldSource == Define.Source.SOURCE_POWEROFF && !PowerManager.isRuiPai())) {//锐派需要记忆POWEROFF状态
                oldSource = SourceManager.getLastNoPoweroffRealSource();
//            oldLogic = ModuleManager.getLogicBySource(oldSource);
//            if (null != oldLogic ) {
//                curPkgName = oldLogic.getAPKPacketName();
//                curClsName = oldLogic.getAPKClassName();
//            }
            }
//        }

        mEnterAccoffSource = oldSource;
        mEnterAccoffPkgName = curPkgName;
        mEnterAccoffClsName = curClsName;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!EventInputManager.getAcc()) {
            LogUtils.e("AccoffLogic onStop return acc off!");
            return;
        }
        LogUtils.d(TAG, "accOn() start.");
        Driver().accOn();
        LogUtils.d(TAG, "accOn() over.");

        /*-begin-20180503-ydinggen-modify-关ACC增加黑屏界面,解决关ACC还会有界面图标显示-*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "AccoffLogic kill apk:"+getAPKPacketName());
                STM32MCUDriver.killProcess(getMainContext(), getAPKPacketName());
            }
        }).start();
        /*-end-20180503-ydinggen-modify-关ACC增加黑屏界面,解决关ACC还会有界面图标显示-*/
    }

    @Override
    public void onDestroy() {
        PowerManager.getModel().unbindListener(mPowerListener);
        getModel().unbindListener(mAccoffListener);

        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public boolean onStatusEvent(int type, boolean status, Packet packet) {
        boolean ret = true;
        switch (type) {
            case EventInputDefine.Status.TYPE_ACC:
                if (status) {
                    final int step = getAccoffStep();
                    // 在ACC OFF的情况下执行
                    if (AccoffListener.AccoffStep.isAccoff(step)) {
                        // 在没有深度睡眠情况下执行
                        if (!AccoffListener.AccoffStep.isDeepSleep(step)) {
                            // 上ACC
                            LogUtils.d(TAG, "abortAccOff start.");
                            Driver().abortAccOff();
                            LogUtils.d(TAG, "abortAccOff over.");

                            if (!EventInputManager.getAcc()) {
                                LogUtils.e("EventInputDefine.Status.TYPE_ACC---return acc off!");
                                return ret;
                            }
                            LogUtils.d(TAG, "accOn##mLightSleepLock.lock() start.");
                            mLightSleepLock.lock();
                            LogUtils.d(TAG, "accOn##mLightSleepLock.lock() over.");
                            try {
                                final boolean camera = EventInputManager.getCamera();
                                boolean result = false;
                                boolean poweroff = false;
                                boolean available = false;
                                LogUtils.d(TAG, "acc on, step = " + AccoffListener.AccoffStep.toString(step) + ", camera = " + camera);
//                                if (camera) {
//                                    result = SourceManager.onRemoveChangeSource(Define.Source.SOURCE_CAMERA, Define.Source.SOURCE_ACCOFF);
//                                    if (result) {
//                                        SourceManager.onOpenBackgroundSource();
//                                    }
//                                }
                                if (!result) {
                                    if (!PowerManager.isRuiPai()) {//锐派需要记忆POWEROFF状态
                                        if (mEnterAccoffSource == Define.Source.SOURCE_SILENT) {
                                            int mcuSource = McuManager.getMemorySource();
                                            if (mcuSource != Define.Source.SOURCE_INVALID &&
                                                    mcuSource != Define.Source.SOURCE_NONE) {
                                                mEnterAccoffSource = mcuSource;
                                            }
                                            LogUtils.d(TAG, "Acc on, mcuSource =" + mcuSource);
                                        }
                                    }
                                    BaseLogic logic = ModuleManager.getLogicBySource(mEnterAccoffSource);
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
                                    LogUtils.d(TAG, "Acc on, mEnterAccoffSource = " + Define.Source.toString(mEnterAccoffSource) +
                                            ", mEnterAccoffPkgName = " + mEnterAccoffPkgName +
                                            ", mEnterAccoffClsName = " + mEnterAccoffClsName +
                                            ", poweroff = " + poweroff + ", available = " + available + ", result = " + result);
                                    if (!result) {
                                        if (mEnterAccoffPkgName.equals(query_pkg_name(getMainContext()))/* ||//去掉第三方源记忆。2018-11-19
                                                mEnterAccoffSource == Define.Source.SOURCE_THIRDPARTY*/) {
                                            result = SourceManager.onRemoveChangePackage(mEnterAccoffPkgName, null , source());
                                        } else {
                                            result = SourceManager.onRemoveChangeSource(mEnterAccoffSource, source());
                                        }
                                        if (!result) {
                                            SourceManager.onPopSourceNoPoweroff();
                                        }
                                        SourceManager.onOpenBackgroundSource();
                                    }
                                }

                                //修改bug9311，原因：在浅睡眠时会暂停视频播放，再上ACC时，未启动视频APK不会自动播放视频。而在深度睡眠时，倒车检测慢，所以会先启动视频APK。
                                if (camera) {
                                    result = SourceManager.onRemoveChangeSource(Define.Source.SOURCE_CAMERA, Define.Source.SOURCE_ACCOFF);
                                    if (result) {
                                        SourceManager.onOpenBackgroundSource();
                                    }
                                }
                            } finally {
                                LogUtils.d(TAG, "accOn##mLightSleepLock.unlock() start.");
                                mLightSleepLock.unlock();
                                LogUtils.d(TAG, "accOn##mLightSleepLock.unlock() over.");
                            }
                        } else {
                            LogUtils.d(TAG, "ACC ON, not handle it, is in the deep sleep status, step = " + AccoffListener.AccoffStep.toString(step));
                        }
                    } else {
                        LogUtils.d(TAG, "ACC ON, not handle it, is already in acc on, step = " + AccoffListener.AccoffStep.toString(step));
                    }
                } else {
                    LogUtils.d(TAG, "accOff##mLightSleepLock.lock() start 1.");
                    mLightSleepLock.lock();
                    LogUtils.d(TAG, "accOff##mLightSleepLock.lock() over 1.");
                    try {
                        Driver().accOff();
                    } finally {
                        LogUtils.d(TAG, "accOff##mLightSleepLock.unlock() start 1.");
                        mLightSleepLock.unlock();
                        LogUtils.d(TAG, "accOff##mLightSleepLock.unlock() over 1.");
                    }

                }
                break;
            case EventInputDefine.Status.TYPE_BRAKE:
                break;
            case EventInputDefine.Status.TYPE_ILL:
                break;
            case EventInputDefine.Status.TYPE_CAMERA:
                //zhongyang.hu add for acc off logic is not exit,but is not accoff mode. 20170509
                final int step = getAccoffStep();
                if (!AccoffListener.AccoffStep.isAccoff(step)) {
                    ret=false;
                }
                //end 20170509
                break;
            case EventInputDefine.Status.TYPE_BEEP:
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        return true;
    }

    /**获取ACC阶段*/
    private int getAccoffStep() {
        int ret = AccoffListener.AccoffStep.STEP_WORK;
        Packet packet = getInfo();
        if (null != packet) {
            ret = packet.getInt("AccoffStep");
        }
        return ret;
    }

    private String query_pkg_name(Context context) {
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

    private PowerManager.PowerListener mPowerListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            if (PowerManager.PowerStep.isPoweronOvered(newVal)) {
                LogUtils.d(TAG, "Power on end, mInitAcc = ");
                //通知至简Carplay状态
                Intent intentCarplay = new Intent("com.zjinnova.zlink");
                intentCarplay.putExtra("command", "ACTION_ENTER");
                getMainContext().sendBroadcast(intentCarplay);
            }
        }
    };
}
