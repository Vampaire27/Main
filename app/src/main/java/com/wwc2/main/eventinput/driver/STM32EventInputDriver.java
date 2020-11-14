package com.wwc2.main.eventinput.driver;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.accoff.driver.WakeupManager;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.provider.LogicProvider;
import com.wwc2.main.upgrade.system.NetworkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

/**
 * the stm32 event input driver.
 *
 * @author wwc2
 * @date 2017/1/18
 */
public class STM32EventInputDriver extends BaseEventInputDriver {

    /**TAG*/
    private static final String TAG = "STM32EventInputDriver";

    /**开机的ACC状态，需要完成APP启动后再判断，用于B+起来后深度休眠判断*/
    private boolean mInitAcc = true;

    /**倒车防抖Timer queue.*/
    private TimerQueue mCameraTimerQueue = new TimerQueue();

    private int accState = 1;


    /**MCU数据监听器*/
    private McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            byte[] data = new byte[1];

            // query camera status
            data[0] = 0;
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.RPT_RequestReverse, data, 1);

            // query acc status
            data[0] = 0;
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.RPT_RequestAcc, data, 1);

            /*add by huwei 180412 ro fix bug 11136; 威益德mcu没有返回正确的按键音状态，还需要mcu修改*/
            //query reversingVolume and keytone
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.RPT_REQ_OTHERSET_INFO, data, 1);
        }

        @Override
        public void DataListener(byte[] val) {
            if (null != val) {
                final int length = val.length;
                if (length >= 1) {
                    byte cmd = val[0];
                    switch (cmd) {
                        case (byte)McuDefine.MCU_TO_ARM.MACK_SysInitdata:
                            if (length > 10) {
                                // 倒车状态
                                setCamera(1 == val[10]);
                                if (length > 11) {
                                    // 手刹状态
                                    setBrake(1 == val[11]);
                                    if (length > 12) {
                                        // 大灯状态
                                        LogUtils.e(TAG, "MACK_SysInitdata----" + val[12]);
                                        setIll(1 == val[12], true);
                                        if (length > 22) {
                                            // ACC状态
                                            final boolean acc = (1 == val[22]);
                                            // 保存ACC状态，用于未完成开机
                                            mInitAcc = acc;
                                            if (WakeupManager.getInstance().getPhotoStatus(acc)) {
                                                LogUtils.d(TAG, "mInitAcc return by Photo Taking!");
                                            }
                                            if (PowerManager.PowerStep.isPoweronOvered(PowerManager.getPowerStep())) {
                                                LogUtils.d(TAG, "mInitAcc=" + mInitAcc + ", acc=" + acc);
                                                setAcc(acc, false);
                                            } else {
                                                LogUtils.e(TAG, "mInitAcc=" + mInitAcc + ", acc=" + acc);
                                            }
                                            if (mInitAcc) {
                                                Uri uri_acc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.ACC_STATUS);
                                                getMainContext().getContentResolver().notifyChange(uri_acc, null);
                                            }

                                            LogUtils.d(TAG, "init, oldAcc = " + Model().getAcc().getVal() + ", McuAcc = " + acc +
                                                    ", PowerStep = " + PowerManager.getPowerStep() + ", mInitAcc = " + mInitAcc);
                                        }
                                    }
                                }
                            }
                            break;
                        case (byte) McuDefine.MCU_TO_ARM.MRPT_RDS_TIME_ACC_STATUS://RTC时间发送给ARM（在无网络和无GPS信号时，收到RDS更新系统时间）
                            //由于命令定义有冲突，通过长度判断类型。
                            if (length > 7) {//RDS时间　威益德带RDS
//                                LogUtils.d("setCalendarDate---data=" + FormatData.formatHexBufToString(val, val.length));
                                if (!isNetworkGpsAcivite()) {
                                    int year = (val[1] & 0xFF) * 256 + (val[2] & 0xFF);
                                    int month = val[3] & 0xFF;
                                    int day = val[4] & 0xFF;
                                    int hour = val[5] & 0xFF;
                                    int minute = val[6] & 0xFF;
                                    int second = val[7] & 0xFF;
                                    setCalendarDate(year, month, day, hour, minute, second);
                                }
                            } else {//实际ACC状态，针对停车监控项目
                                boolean acc = (val[1]==1);
                                if (realAccState != acc) {
                                    LogUtils.d(TAG, "MRPT_ACC_STATUS = " + (1 == val[1]) + ", realAccState = " + realAccState);
                                    realAccState = acc;

                                    Uri uri_realacc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.REAL_ACC_STATUS);
                                    getMainContext().getContentResolver().notifyChange(uri_realacc, null);
                                }
                            }
                            break;
                        case (byte) McuDefine.MCU_TO_ARM.MRPT_CarAcc:
                            PowerManager.feedDogFlag();
                            LogUtils.d(TAG, "McuAcc = " + (1 == val[1]) + ", AccVar = " + Model().getAcc().getVal());
                            if (val[1] == 0) {
                                PowerManager.mFirstBoot = false;
                            }
                            setAcc(1 == val[1], false);
                            if (accState != val[1]) {
                                accState = val[1];
                                Uri uri_acc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.ACC_STATUS);
                                getMainContext().getContentResolver().notifyChange(uri_acc, null);

                                if (WakeupManager.getInstance().getPhotoStatus(accState == 1)) {
                                    LogUtils.d(TAG, "McuAcc = return by Photo Taking!");
                                }
                            }
                            break;
                        case (byte)McuDefine.MCU_TO_ARM.MRPT_CarReverse:
                            boolean camera = 1 == val[1];
                            LogUtils.d(TAG, "McuCamera = " + camera + ", CameraVar = " + Model().getCamera().getVal());
                            mCameraTimerQueue.stop();
                            if (camera) {
                                mCameraTimerQueue.add(20, null, new BaseCallback() {//MCU有作200ms的防抖，此处先保留20ms
                                    @Override
                                    public void onCallback(int nId, Packet packet) {
                                        setCamera(true);
                                    }
                                });
                                mCameraTimerQueue.start();
                            } else {
                                setCamera(false);
                            }
                            break;
                        case (byte)McuDefine.MCU_TO_ARM.MRPT_CarBrake:
                            LogUtils.d(TAG, "McuBrake = " + (1 == val[1]) + ", BrakeVar = " + Model().getBrake().getVal());
                            setBrake(1 == val[1]);
                            break;
                        case (byte)McuDefine.MCU_TO_ARM.MRPT_CarILL:
                            LogUtils.d(TAG, "McuILL = " + (1 == val[1]) + ", ILLVar = " + Model().getIll().getVal());
                            setIll(1 == val[1], false);
                            break;
                        case (byte)McuDefine.MCU_TO_ARM.MPRT_LEFT_RIGHT_ST:
                            setLeftLight(1 == val[1]);
                            setRightLight(3 == val[1]);
                            break;
                        case (byte) McuDefine.MCU_TO_ARM.MRPT_CarTurnSW:
                            setTurnLight(val[1] & 0xFF);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };

    /**开机监听器*/
    private PowerManager.PowerListener mPowerListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            if (PowerManager.PowerStep.isPoweronOvered(newVal)) {
                LogUtils.d(TAG, "Power on end, mInitAcc = " + mInitAcc);
                setAcc(mInitAcc, true);

                //不接ACC直接上电，需进入休眠，未测试，先保留。
//                if (readFile("/sys/class/switch/acc_signal/state").equals("1")) {
//                    LogUtils.d(TAG, "Power on end, acc_signal = 1");
//                    setAcc(false);
//                }
            }
        }
    };

    /**ACC监听器*/
    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            if (!AccoffStep.isDeepSleep(oldVal) && AccoffStep.isDeepSleep(newVal)) {
                LogUtils.d(TAG, "Sleep old = " + AccoffStep.toString(oldVal) + ", new = " + AccoffStep.toString(newVal) + ", start.");
                mCameraTimerQueue.stop();
                setCamera(false);
                LogUtils.d(TAG, "Sleep old = " + AccoffStep.toString(oldVal) + ", new = " + AccoffStep.toString(newVal) + ", over.");
            } else if (AccoffStep.isDeepSleep(oldVal) && !AccoffStep.isDeepSleep(newVal)) {
                LogUtils.d(TAG, "Wakeup old = " + AccoffStep.toString(oldVal) + ", new = " + AccoffStep.toString(newVal) + ", start.");
//                setAcc(true);
                //1: have set Camera false in ACC off. 2: this may be cause Die LOCK  UART thread &  Acc on Receiver. hzy20180327  bug11034
                //setCamera(false);//2017-07-12；避免acc on后无法进入倒车。
                LogUtils.d(TAG, "Wakeup old = " + AccoffStep.toString(oldVal) + ", new = " + AccoffStep.toString(newVal) + ", over.");
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        McuManager.registerEventInputDriver(this);
        McuManager.getModel().bindListener(mMCUListener);
        PowerManager.getModel().bindListener(mPowerListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);

        mInitAcc = getSystemAccStatus();//默认值获取系统的acc状态，避免ACC OFF状态重启会打开摄像头。
        accState = (mInitAcc ? 1 : 0);
//        setAcc(mInitAcc);
    }

    @Override
    public void onDestroy() {
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().unbindListener(mAccoffListener);
        PowerManager.getModel().unbindListener(mPowerListener);
        McuManager.getModel().unbindListener(mMCUListener);
        super.onDestroy();
    }

    public static String readFile(String path) {
        BufferedReader readbuffer = null;
        String bufferValue = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                readbuffer = new BufferedReader(new FileReader(file));
                bufferValue = readbuffer.readLine();
                LogUtils.i(TAG, "File node value is " + bufferValue);
            }
        } catch (IOException e) {
            LogUtils.i(TAG, "readFileNote() throws IOException: " + e);
        } finally {
            try {
                if (readbuffer != null)
                    readbuffer.close();
            } catch (IOException e) {
                LogUtils.i(TAG, "readFileNote() throws IOException: " + e);
            }
        }
        return bufferValue;
    }

    private boolean getSystemAccStatus() {
        String sstate = STM32MCUDriver.getContentFromFile(STM32MCUDriver.ACC_ONOFF_STATE);
        int istate = 0;
        try {
            istate = Integer.parseInt(sstate);
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, "acc state fail!");
            return false;
        }
        LogUtils.d("getSystemAccStatus----acc=" + (istate == 0));
        return (istate == 0);//0:acc on
    }

    private boolean isNetworkGpsAcivite() {
        boolean ret = false;
        LocationManager locationManager = (LocationManager)
                getMainContext().getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        boolean netWork = NetworkUtils.checkNetworkAvailable(getMainContext());
        if (netWork || location != null) {
            ret = true;
        }
        LogUtils.d(TAG, "isNetworkGpsAcivite----network=" + netWork + ", location=" + location);
//        if (location != null) {
//            double longitude = location.getLongitude(); // 纬度
//            double latitude = location.getLatitude(); // 经度
//        }
        return ret;
    }

    private void setCalendarDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar mCalendar = Calendar.getInstance();
        if ((year >= mCalendar.getMinimum(Calendar.YEAR)) && (year <= mCalendar.getMaximum(Calendar.YEAR))) {
            mCalendar.set(Calendar.YEAR, year);
        }

        if ((month >= (mCalendar.getMinimum(Calendar.MONTH)+1)) && (month <= (mCalendar.getMaximum(Calendar.MONTH)+1))) {
            mCalendar.set(Calendar.MONTH, month-1);
        }

        if ((day >= mCalendar.getMinimum(Calendar.DAY_OF_MONTH)) && (day <= mCalendar.getMaximum(Calendar.DAY_OF_MONTH))) {
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
        }

        ContentResolver cv = getMainContext().getContentResolver();
        String strTimeFormat = Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
//        LogUtils.d("setCalendarDate---format=" + strTimeFormat + ", min=" + mCalendar.getMinimum(Calendar.HOUR_OF_DAY) +
//                ", max=" + mCalendar.getMaximum(Calendar.HOUR_OF_DAY) + ", hour=" + hour + ", minute=" + minute);
        if (strTimeFormat.equals("24")) {
            if ((hour >= mCalendar.getMinimum(Calendar.HOUR_OF_DAY)) && (hour <= mCalendar.getMaximum(Calendar.HOUR_OF_DAY))) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hour);
            }
        } else/* if ((timeFormat == TIME_FORMAT_AM) || (timeFormat == TIME_FORMAT_PM))*/ {
            if ((hour >= mCalendar.getMinimum(Calendar.HOUR)) && (hour <= mCalendar.getMaximum(Calendar.HOUR))) {
                mCalendar.set(Calendar.HOUR, hour);
            }
        }

        if ((minute >= mCalendar.getMinimum(Calendar.MINUTE)) && (minute <= mCalendar.getMaximum(Calendar.MINUTE))) {
            mCalendar.set(Calendar.MINUTE, minute);
        }

        if ((second >= mCalendar.getMinimum(Calendar.SECOND)) && (second <= mCalendar.getMaximum(Calendar.SECOND))) {
            mCalendar.set(Calendar.SECOND, second);
        }

        long when = mCalendar.getTimeInMillis();
        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) getMainContext().getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }
}
