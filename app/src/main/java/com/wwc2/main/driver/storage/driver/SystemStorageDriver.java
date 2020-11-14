package com.wwc2.main.driver.storage.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.camera.CameraLogic;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.driver.android.AndroidMediaDriver;
import com.wwc2.main.settings.SettingsLogic;
import com.wwc2.main.upgrade.system.SystemUpdateLogic;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.system_interface.SystemDefine;

import java.util.HashMap;

/**
 * the system storage driver.
 *
 * @author wwc2
 * @date 2017/1/29
 */
public abstract class SystemStorageDriver extends BaseStorageDriver {

    /**
     * TAG
     */
    private static final String TAG = "SystemStorageDriver";

    private  final String UPDATE_FILE= "update_wwc2_local.zip";

    private  final String ID_DIRTY= "/cache/id_dirty.txt";
    private  final String ID_CHECK= "/cache/id_check.txt";

    /**
     * the enable plug timer queue.
     */
    private TimerQueue mEnablePlugTimerQueue = new TimerQueue();
    static boolean deepSleepFlag = false;
    static int memeryDevice = StorageDevice.UNKNOWN;

    boolean[] storageState = {false, false, false, false};

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LogUtils.e("Delay Time is arrived, to set eject device!");
                    removeMessages(1);
                    if (EventInputManager.getAcc() && AndroidMediaDriver.mCloseUsb != 2) {//在acc off和MCU断电的情况下不清除USB信息。
                        for (int i = 0; i < 4; i++) {
                            if (!storageState[i]) {
                                setMounted(StorageDevice.USB + i, false);
                            }
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(1, 6000);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void setStorageState(int id, boolean mounted) {
        for (int i=0; i<4; i++) {
            if (id == i) {
                storageState[i] = mounted;
                break;
            }
        }
    }

    // 广播监听
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String path = intent.getData().getPath() + "/";

            // FIXME: 17-1-19 后续增加usb此处需要修改

            int type=StorageDevice.parseFileOrDirName(context,path);
            LogUtils.d(TAG, "storage: action = " + action + ", path = " + path + ", storage = " + StorageDevice.toString(type)+
                    ", deepSleepFlag="+deepSleepFlag);

            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                LogUtils.e(TAG, "Mounted when mCloseUsb="+AndroidMediaDriver.mCloseUsb+", deepSleepFlag="+deepSleepFlag);
                setStorageState(type-4, true);
                if (deepSleepFlag) {
                    deepSleepFlag = false;
                    mHandler.removeMessages(1);
                }
                /*-begin-20180214-ydinggen-add-在未收到MCU给USB上电的消息或还在ACC OFF时收到加载消息，导致不会记忆源停留在主界面-*/
                if (AndroidMediaDriver.mCloseUsb == 2 || !EventInputManager.getAcc()) {
                    if (AndroidMediaDriver.playStorage == type) {
                        memeryDevice = type;
                    }
                }
                /*-end-20180214-ydinggen-add-在未收到MCU给USB上电的消息或还在ACC OFF时收到加载消息，导致不会记忆源停留在主界面-*/
            } else {
                if (Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
                    setStorageState(type-4, false);
                    if (AndroidMediaDriver.mCloseUsb == 2 || deepSleepFlag) {
                        LogUtils.e(TAG, "Eject return when mCloseUsb=" + AndroidMediaDriver.mCloseUsb +
                                ", deepSleepFlag=" + deepSleepFlag);
                        return;
                    } else {
                        LogUtils.e(TAG, "Eject not return!");
                    }
                }
            }

            if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
                LogUtils.e(TAG, "storage: action  return when acc off!");
                return;
            }

            if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                // 拔出
                setMounted(type, false);
                AndroidMediaDriver.setMountedAfterOpen(type, false);
                /*-begin-20180424-ydinggen-delete-USB拔出只需处理EJECT消息，避免ACC ON后收到BAD REMOVAL消息引起切源-*/
//            } else if (Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
//                // 错误拔出
//                if (AndroidMediaDriver.mCloseUsb == 0) {
//                    setMounted(type, false);
//                }
                /*-end-20180424-ydinggen-delete-USB拔出只需处理EJECT消息，避免ACC ON后收到BAD REMOVAL消息引起切源-*/
            } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                // 已挂载
                setMounted(type, true);
                if (AndroidMediaDriver.setMountedAfterOpen(type, true)) {
                    memeryDevice = StorageDevice.UNKNOWN;
                }

                if (type == StorageDevice.USB || type == StorageDevice.USB1 ||
                        type == StorageDevice.USB2 || type == StorageDevice.USB3
                        || type == StorageDevice.MEDIA_CARD) {
                    CoreLogic coreLogic = LogicManager.getLogicByName(SettingsDefine.MODULE);
                    if (coreLogic instanceof SettingsLogic) {
                        ((SettingsLogic) coreLogic).importConfig(path);
                    }

                }
            } else if (Intent.ACTION_MEDIA_CHECKING.equals(action)) {
                // 正在检测
            }
        }
    };

    private BroadcastReceiver mUSBDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // FIXME: 17-1-19 后续增加usb此处需要修改

            LogUtils.d(TAG, "11storage: action = " + action);

            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
//                Toast.makeText(getMainContext(), "ACTION_USB_DEVICE_ATTACHED 1", Toast.LENGTH_SHORT).show();
                UsbManager usbManager = (UsbManager) getMainContext().getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> map = usbManager.getDeviceList();
                for(UsbDevice device : map.values()){
                    String name = device.getDeviceName()+", "+device.getProductId()+", "+device.getVendorId();
//                    Toast.makeText(getMainContext(), "ACTION_USB_DEVICE_ATTACHED 1"+name, Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
//                Toast.makeText(getMainContext(), "ACTION_USB_DEVICE_DETACHED 2", Toast.LENGTH_SHORT).show();
            } else if (action.equals("com.wwc2.usb.device.attached")) {
//                Toast.makeText(getMainContext(), "com.wwc2.usb.device.attached 2", Toast.LENGTH_SHORT).show();
            }
        }
    };


    void updata_form_USB(String updatePath){

        CoreLogic coreLogic1 = LogicManager.getLogicByName(SystemDefine.MODULE);
        if (coreLogic1 instanceof SystemUpdateLogic) {
            ((SystemUpdateLogic) coreLogic1).updateUSB(updatePath);
        }
    }

    /**
     * plug delay tick.
     */
    protected int enablePlugDelayMS() {
        return 0;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

//        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().bindListener(mEventInputListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);
        McuManager.getModel().bindListener(mMcuListener);
        mEnablePlugTimerQueue.stop();
        final int delay = enablePlugDelayMS();
        if (delay > 0) {
            // 延时使能插拔
            mEnablePlugTimerQueue.add(delay, null, new BaseCallback() {
                @Override
                public void onCallback(int nId, Packet packet) {
                    enablePlug(true);
                }
            });
            mEnablePlugTimerQueue.start();
        } else {
            // 立即使能插拔
            enablePlug(true);
        }
    }

    @Override
    public void onDestroy() {
        mEnablePlugTimerQueue.stop();

        deepSleepFlag = false;
        mHandler.removeMessages(1);

        // 禁用插拔
        enablePlug(false);
        McuManager.getModel().unbindListener(mMcuListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().unbindListener(mAccoffListener);
//        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().unbindListener(mEventInputListener);

        super.onDestroy();
    }

    /**
     * the enable plug.
     */
    protected void enablePlug(boolean enable) {
        if (enable) {
            Context context = getMainContext();
            if (null != context) {
                IntentFilter myIntentFilter = new IntentFilter();
                myIntentFilter.addDataScheme("file");
                myIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
                myIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
                myIntentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
                myIntentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
                context.registerReceiver(mBroadcastReceiver, myIntentFilter);

                IntentFilter inFilter = new IntentFilter();
                inFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                inFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                context.registerReceiver(mUSBDeviceReceiver, inFilter);
            }
        } else {
            Context context = getMainContext();
            if (null != context) {
                try {
                    context.unregisterReceiver(mBroadcastReceiver);
                    context.unregisterReceiver(mUSBDeviceReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    private EventInputListener mEventInputListener = new EventInputListener() {
//
//        @Override
//        public void AccListener(Boolean oldVal, Boolean newVal) {
//            LogUtils.d(TAG, "AccListener, oldVal = " + oldVal + ", newVal = " + newVal);
//            deepSleepFlag = true;
//            mHandler.removeMessages(1);
//            mHandler.sendEmptyMessageDelayed(1, 6000);
//        }
//    };

    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            if (AccoffStep.isDeepSleep(oldVal) && AccoffStep.isLightSleep(newVal)) {
                LogUtils.d(TAG, "Will from deep sleep,reset USB devices");
                deepSleepFlag = true;
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1, 20000);
            }
        }
    };

    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {
        @Override
        public void DataListener(byte[] val) {
            //LogUtils.e("MCU_TO_ARM.MRPT_MEDIA_INFO---val[0]:" + FormatData.formatHexBufToString(val, 1));
            if (null != val) {
                byte cmd = (byte) (val[0] & 0xFF);
                if (cmd == (byte) McuDefine.MCU_TO_ARM.MRPT_CLOSE_USB) {
                    LogUtils.e("MCU_TO_ARM.MRPT_CLOSE_USB---val[1]:" + (val[1] & 0xFF) + ", curSource=" + SourceManager.getCurSource());
                    if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
                        AndroidMediaDriver.mCloseUsb = 0;
                        LogUtils.e("MCU_TO_ARM.MRPT_CLOSE_USB-return-when acc off");
                        deepSleepFlag = true;
                        mHandler.removeMessages(1);
                        mHandler.sendEmptyMessageDelayed(1, 6000);
                        return;
                    }
                    if (val.length > 1) {
                        if ((val[1] & 0xFF) == 1) {
                            AndroidMediaDriver.mCloseUsb = (val[1] & 0xFF) + 1;
                            deepSleepFlag = true;
                            mHandler.removeMessages(1);
                            mHandler.sendEmptyMessageDelayed(1, 30000);

                            if (SourceManager.getCurSource() == Define.Source.SOURCE_VIDEO) {
                                if (AndroidMediaDriver.playStorage != 1) {
                                    AndroidMediaDriver.pauseFlag = 1;
                                }
                            } else if (SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                                if (AndroidMediaDriver.playStorage != 1) {
                                    AndroidMediaDriver.pauseFlag = 2;
//                                    pause();
                                }
                            }
                            if (AndroidMediaDriver.pauseFlag == 1 || AndroidMediaDriver.pauseFlag == 2) {
                                if (EventInputManager.getCamera()) {
                                    CameraLogic.setEnterCameraSource(Define.Source.SOURCE_SILENT, false);
                                    SourceManager.onOpenBackgroundSource(Define.Source.SOURCE_SILENT);
                                } else {
                                    SourceManager.onChangeSource(Define.Source.SOURCE_SILENT);
                                }
                            }
                        } else {
                            if (AndroidMediaDriver.mCloseUsb != 0 ) {
                                AndroidMediaDriver.mCloseUsb = (val[1] & 0xFF) + 1;
                                mHandler.removeMessages(1);

                                /*-begin-20180214-ydinggen-add-在未收到MCU给USB上电的消息或还在ACC OFF时收到加载消息，导致不会记忆源停留在主界面-*/
                                if (memeryDevice != StorageDevice.UNKNOWN) {
                                    AndroidMediaDriver.setMountedAfterOpen(memeryDevice, true);
                                    memeryDevice = StorageDevice.UNKNOWN;
                                    /*-end-20180214-ydinggen-add-在未收到MCU给USB上电的消息或还在ACC OFF时收到加载消息，导致不会记忆源停留在主界面-*/
                                } else {
                                    //针对MCU断USB电的情况等待时间可以长一点。2018-03-27
                                    deepSleepFlag = true;
                                    mHandler.sendEmptyMessageDelayed(1, 30000);
                                }
                            } else {
                                AndroidMediaDriver.mCloseUsb = 0;
                            }
                        }
                        LogUtils.e("MCU_TO_ARM.MRPT_CLOSE_USB---mCloseUsb:" + AndroidMediaDriver.mCloseUsb + ", val=" + (val[1] & 0xFF + 1));
                    }
                }
            }
        }
    };
}
