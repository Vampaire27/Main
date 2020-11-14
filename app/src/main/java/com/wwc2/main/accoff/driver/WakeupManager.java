package com.wwc2.main.accoff.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.common_interface.Provider;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.mcu.McuDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.provider.LogicProvider;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by swd1 on 19-1-25.
 */

public class WakeupManager {

    /**后台唤醒广播*/
    private static final String ACTION_CARNET_WAKEUP    = "com.android.wwc2.carnet.wakeup";
    private static final String KEY_CARNET_WAKEUP       = "photo.state";//1开始 2结束
    private static final String KEY_DELAY_TIME          = "time";//超时时间
    /**MCU准备完成，通知后台可以开始抓拍*/
    private static final String ACTION_MAIN_WAKEUP      = "com.android.wwc2.main.wakeup";
    //电压低的广播
    private static final String ACTION_MAIN_SLEEP_VOLTAGE   = "com.wwc2.main.low.voltage";

    private static final String RESET_USB_GPIO          = "/sys/class/gpiodrv/gpio_ctrl/hub_set";

    private static final String Logic_Name = "com.wwc2.main.accoff.AccoffLogic";
    private static final int MSG_PHOTO_TO_MCU   = 1;
    private static final int MSG_DELAY_SLEEP    = 2;

    private static WakeupManager wakeupManager = null;
    private Context mContext = null;
    BaseModel mcuModel = null;

    private int mPhotoState = 0;
    private int mDelayTime = -1;

    private int voltage         = 120;
    private boolean lowVoltage  = false;

    int sendCount = 0;
    private Handler mHandler = null;

    public static WakeupManager getInstance() {
        if (null == wakeupManager) {
            wakeupManager = new WakeupManager();
        }
        return wakeupManager;
    }

    public void onCreate(Context context) {
        mContext = context;
        mHandler = new subHandler();

        LogUtils.d("onCreate!");

        if (mContext != null) {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(ACTION_CARNET_WAKEUP);
            mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);
        }

        mcuModel = McuManager.getModel();
        if (mcuModel != null) {
            mcuModel.bindListener(mMCUListener);
        }
    }

    public void onDestroy() {
        if (mContext != null) {
            try {
                mContext.unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mcuModel != null) {
            mcuModel.unbindListener(mMCUListener);
        }
    }

    public boolean getPhotoStatus(boolean acc) {
        boolean ret = false;
        LogUtils.d("getPhotoStatus-----acc=" + acc + ", mPhotoState=" + mPhotoState);
        if (mPhotoState == 1 && !acc) {
            ret = true;
        }
        if (acc && mPhotoState != 0) {
            mPhotoState = 0;
            if (mHandler != null) {
                if (mHandler.hasMessages(MSG_PHOTO_TO_MCU)) {
                    mHandler.removeMessages(MSG_PHOTO_TO_MCU);
                }
                if (mHandler.hasMessages(MSG_DELAY_SLEEP)) {
                    mHandler.removeMessages(MSG_DELAY_SLEEP);
                }
            }
        }
        return ret;
    }

    class  subHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PHOTO_TO_MCU:
                    mHandler.removeMessages(MSG_PHOTO_TO_MCU);
                    if (mPhotoState == 1 || mPhotoState == 2) {
                        mHandler.sendEmptyMessageDelayed(MSG_PHOTO_TO_MCU, 500);
                        sendCount ++;
                        LogUtils.d("MSG_PHOTO_TO_MCU---sendCount=" + sendCount + ", mPhotoState=" + mPhotoState);
                        McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_PHOTO_STATUS, new byte[]{(byte) mPhotoState}, 1);
                    }
                    break;
                case MSG_DELAY_SLEEP:
                    mHandler.removeMessages(MSG_DELAY_SLEEP);
                    if (mPhotoState == 1) {
                        mPhotoState = 2;
                        sendCount = 0;
                        mHandler.removeMessages(MSG_PHOTO_TO_MCU);
                        mHandler.sendEmptyMessageDelayed(MSG_PHOTO_TO_MCU, 500);
                        McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_PHOTO_STATUS, new byte[]{(byte) mPhotoState}, 1);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**MCU数据监听器*/
    private McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            LogUtils.d("OpenListener---");
            if (mPhotoState != 0) {
                sendCount = 0;
                if (mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(MSG_PHOTO_TO_MCU, 50);
                }
            }
        }

        @Override
        public void DataListener(byte[] val) {
            if (null != val) {
                final int length = val.length;
                if (length > 1) {
                    byte cmd = val[0];
                    switch (cmd) {
                        case (byte) McuDefine.MCU_TO_ARM.MRPT_PHOTO_STATUS:
                            if ((val[1] & 0xFF) == 1) {
                                if (mContext != null) {
                                    if (mHandler != null) {
                                        mHandler.removeMessages(MSG_PHOTO_TO_MCU);
                                    }
                                    LogUtils.d("MRPT_PHOTO_STATUS---context!=null!---1----");

                                    if (mPhotoState != 0) {
                                        //由底层处理，避免ACC OFF状态远程唤醒后一直反复挂载U盘的问题。2020-08-28
//                                        writeTextFile("1", RESET_USB_GPIO);
//                                        writeTextFile("0", RESET_USB_GPIO);//上电

                                        BaseLogic logicAccoff = ModuleManager.getLogicByName(AccoffDefine.MODULE);
                                        if (logicAccoff == null) {
                                            return;
                                        }
                                        Packet packet = logicAccoff.getInfo();
                                        if (packet != null) {
                                            int accStep = packet.getInt("AccoffStep");

                                            if (AccoffListener.AccoffStep.isDeepSleep(accStep)) {
                                                BaseLogic logic = ModuleManager.getLogicByName(Logic_Name);
                                                MTK6737AccoffDriver Driver = (MTK6737AccoffDriver) logic.getDriver();
                                                if (Driver != null) {
                                                    Driver.wakeupFromDeepSleepAccOff();
                                                }
                                            } else {
                                                EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_ACC, true, null);
                                            }
                                        }

                                        Intent intent = new Intent();
                                        intent.setAction(ACTION_MAIN_WAKEUP);
                                        mContext.sendBroadcast(intent);
                                    }
                                } else {
                                    LogUtils.d("MRPT_PHOTO_STATUS---context==null!");
                                }
                            } else if ((val[1] & 0xFF) == 2) {
                                if (mHandler != null) {
                                    mHandler.removeMessages(MSG_PHOTO_TO_MCU);
                                }

                                LogUtils.d("MRPT_PHOTO_STATUS---(val[1] & 0xFF) == 2---mPhotoState=" + mPhotoState);
                                mPhotoState = 0;

                                if (EventInputManager.getAcc()) {
                                    LogUtils.d("MRPT_PHOTO_STATUS---(val[1] & 0xFF) == 2---return acc on");
                                    //在ACC ON后仍收到MCU的2时会导致机器重启。2019-11-25
                                    return;
                                }

                                //远程唤醒完成，继续ACC OFF流程
                                BaseLogic logicAccoff = ModuleManager.getLogicByName(AccoffDefine.MODULE);
                                if (logicAccoff == null) {
                                    return;
                                }
                                Packet packet = logicAccoff.getInfo();
                                if (packet != null) {
                                    int accStep = packet.getInt("AccoffStep");
                                    if (AccoffListener.AccoffStep.isDeepSleep(accStep)) {
                                        McuManager.onDestroy();
                                    }
                                }
                                EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_ACC, false, null);
                            }
                            break;
                        case (byte) McuDefine.MCU_TO_ARM.MPRT_VOLTAGE:
                            if (length > 3) {
                                int voltageTemp = ((val[1] & 0xFF) << 8) + (val[2] & 0xFF);
                                boolean low = (val[3] == 1);
                                if (voltageTemp != voltage) {
                                    voltage = voltageTemp;
                                    LogUtils.d("MPRT_VOLTAGE----voltage=" + voltage + ", low=" + low);
                                    if (mContext !=null) {
                                        Uri uri = Uri.parse("content://" + Provider.AUTHORITY + "/" + LogicProvider.CUR_VOLTAGE);
                                        mContext.getContentResolver().notifyChange(uri, null);
                                    }
                                }
                                if (lowVoltage != low) {
                                    lowVoltage = low;
                                    LogUtils.e("MPRT_VOLTAGE----voltage=" + voltage + ", low=" + low + ", acc=" + EventInputManager.getAcc());
                                    if (!EventInputManager.getAcc() && lowVoltage) {
                                        Intent intent = new Intent();
                                        intent.setAction(ACTION_MAIN_SLEEP_VOLTAGE);
                                        mContext.sendBroadcast(intent);

                                        mHandler.removeMessages(MSG_PHOTO_TO_MCU);
                                        if (mPhotoState == 1) {
                                            mPhotoState = 2;
                                            sendCount = 0;
                                            mHandler.sendEmptyMessageDelayed(MSG_PHOTO_TO_MCU, 500);
                                            McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_PHOTO_STATUS, new byte[]{(byte) mPhotoState}, 1);
                                        } else if (mPhotoState == 2) {
                                            mPhotoState = 0;
                                            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_ACC, false, null);
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        @Override
        public void CloseListener(int status) {
            LogUtils.d("CloseListener---");
            if (mHandler != null) {
                mHandler.removeMessages(MSG_PHOTO_TO_MCU);
            }
        }
    };

    // 广播监听
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (mHandler == null) {
                return;
            }
            if (ACTION_CARNET_WAKEUP.equals(action)) {
                int state = intent.getIntExtra(KEY_CARNET_WAKEUP, 0);
                mDelayTime = intent.getIntExtra(KEY_DELAY_TIME, -1);
                LogUtils.d("mBroadcastReceiver receiver action = " + action + ", mPhotoState=" + mPhotoState + ",　state=" + state);

                if (EventInputManager.getAcc()) {
                    mHandler.removeMessages(MSG_DELAY_SLEEP);
                    mHandler.removeMessages(MSG_PHOTO_TO_MCU);
                    mPhotoState = 0;
                    sendCount = 0;
                    LogUtils.d("mBroadcastReceiver receiver return acc off!");
                    return;
                }
                if (state == 1 && mDelayTime > 0) {
                    mHandler.removeMessages(MSG_DELAY_SLEEP);
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_SLEEP, mDelayTime);
                }
                if (mPhotoState == state) {
                    return;
                }
                mPhotoState = state;
                sendCount = 0;
                if (mPhotoState == 1) {
                    ModuleManager.onCreateDriver(new Packet(), McuDriver.DRIVER_NAME);
                    McuManager.onCreate(null);

//                    BaseLogic logicAccoff = ModuleManager.getLogicByName(AccoffDefine.MODULE);
//                    if (logicAccoff == null) {
//                        return;
//                    }
//                    Packet packet = logicAccoff.getInfo();
//                    if (packet != null) {
//                        int accStep = packet.getInt("AccoffStep");
//
//                        //深度睡眠唤醒才执行以下操作
//                        if (AccoffListener.AccoffStep.isDeepSleep(accStep)) {
////                            //通知ｍａｉｎＵＩ显示ｌｏｇｏ
////                            BaseLogic mLogic = ModuleManager.getLogicByName(MainUIDefine.MODULE);
////                            if (mLogic instanceof MainUILogic) {
////                                MainUILogic mainUILogic = (MainUILogic) mLogic;
////                                mainUILogic.showLogoWhileLeaveDeepSleep();
////                            }
//                        }
//                        BaseLogic logic = ModuleManager.getLogicByName(Logic_Name);
//                        MTK6737AccoffDriver Driver = (MTK6737AccoffDriver) logic.getDriver();
//                        if (Driver != null) {
//                            Driver.wakeupFromDeepSleepComming();
//                        }
//                    }
                } else if (mPhotoState == 2) {
                    McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_PHOTO_STATUS, new byte[]{(byte) mPhotoState}, 1);
                    mHandler.sendEmptyMessageDelayed(MSG_PHOTO_TO_MCU, 500);
                }
            }
        }
    };

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

    public int getCurVoltage() {
        return voltage;
    }
}
