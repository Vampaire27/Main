package com.wwc2.main.driver.storage.driver;

import android.content.Context;
import android.content.Intent;

import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.storage.driver.serial.MountManager;
import com.wwc2.main.driver.storage.driver.serial.SystemSerialStorageDriver;

/**
 * the mtk6737 storage driver.
 *
 * @author wwc2
 * @date 2017/1/30
 */
public class MTK6737StorageDriver extends SystemSerialStorageDriver {
    private final String TAG = "MTK6737StorageDriver";

    private static final String ACTION_STORAGE_NOTIFICATION_ENABLE = "com.android.storage.notification.enable";

    private final String CID0_PATH    = "/sys/class/mmc_host/mmc0/mmc0:*/cid";//实际上为mmc*:*/
    private final String CID1_PATH    = "/sys/class/mmc_host/mmc1/mmc1:*/cid";
    private final String PID0_SERIAL_PATH = "/sys/devices/platform/mt_usb/musb-hdrc.0.auto/usb1/1-1/serial";//usb1
    private final String PID1_SERIAL_PATH = "/sys/devices/platform/mt_usb/musb-hdrc.0/usb1/1-1/1-1.3/serial";//usb2
    private final String PID2_SERIAL_PATH = "/sys/devices/platform/mt_usb/musb-hdrc.0/usb1/1-1/1-1.2/serial";//usb3
    private final String PID3_SERIAL_PATH = "/sys/devices/platform/mt_usb/musb-hdrc.0/usb1/1-1/1-1.1/serial";//usb4
    private MountManager mountManager = null;

    @Override
    protected void regStorages() {
        regStorage(StorageDevice.MEDIA_CARD, "SD");
        regStorage(StorageDevice.USB, "USB");
        regStorage(StorageDevice.USB1, "USB1");
        regStorage(StorageDevice.USB2, "USB2");
        regStorage(StorageDevice.USB3, "USB3");
    }

    @Override
    public void onCreate(Packet packet) {
        LogUtils.d(TAG, "onCreate!");
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy!");
        super.onDestroy();
        if(mountManager != null){
            mountManager.destroy();//退出20s挂载检测
            mountManager = null;
        }
    }

//    @Override
//    protected int enablePlugDelayMS() {
//        return 10 *1000;
//    }

    @Override
    protected void enablePlug(boolean enable) {
        super.enablePlug(enable);
        Context context = getMainContext();
        if (null != context) {
            Intent intent = new Intent(ACTION_STORAGE_NOTIFICATION_ENABLE);
            intent.putExtra("enable", enable);
            context.sendBroadcast(intent);
        }
    }

    @Override
    public String readSerialCid0() {
        return readId(readCidPath(CID0_PATH));
    }

    @Override
    public String readSerialCid1() {
        return readId(readCidPath(CID1_PATH));
    }

    @Override
    public String readSerialPid0() {
        return readId(PID0_SERIAL_PATH);
    }

    @Override
    public String readSerialPid1() {
        return readId(PID1_SERIAL_PATH);
    }

    @Override
    public String readSerialPid2() {
        return readId(PID2_SERIAL_PATH);
    }

    @Override
    public String readSerialPid3() {
        return readId(PID3_SERIAL_PATH);
    }
}
