package com.wwc2.main.media.driver.search;

import android.os.SystemClock;

import com.wwc2.audio_interface.AudioInterface;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.video.VideoLogic;
import com.wwc2.video_interface.VideoInterface;

import java.util.Arrays;

public class FilesSendRunnable extends Thread {

    private SuperMediaDriverable.SearchPresenter mSearchPresenter;

    private final String TAG = FilesSendRunnable.class.getSimpleName();
    private int currentStorage = StorageDevice.MEDIA_CARD;
    private FourString[] fileList = null;

    private BaseLogic baseLogic = null;

    private static final int SEND_LEN   = 800;
    int deviceListIndex = 0;

    private boolean isEnable = false;

    public int getCurrentStorage() {
        return currentStorage;
    }

    public FilesSendRunnable(int currentStorage, FourString[] lists, BaseLogic logic) {
        this.currentStorage = currentStorage;
        this.fileList = lists;
        this.baseLogic = logic;
    }

    public FilesSendRunnable(SuperMediaDriverable.SearchPresenter mSearchPresenter, int currentStorage, FourString[] lists, BaseLogic logic) {
        this(currentStorage, lists, logic);
        this.mSearchPresenter = mSearchPresenter;
    }

    @Override
    public void run() {
        String mDeviceKey = "";
        int mDeviceId = 0;
        if (baseLogic == null) {
            return;
        }
        switch (currentStorage) {
            case StorageDevice.NAND_FLASH:
                mDeviceKey = "FlashInfo";
                mDeviceId = AudioInterface.MAIN_TO_APK.FLASH_LIST_INFO;
                if (baseLogic instanceof VideoLogic) {
                    mDeviceId = VideoInterface.MAIN_TO_APK.FLASH_LIST_INFO;
                }
                break;
            case StorageDevice.MEDIA_CARD:
                mDeviceKey = "SdInfo";
                mDeviceId = AudioInterface.MAIN_TO_APK.SD_LIST_INFO;
                if (baseLogic instanceof VideoLogic) {
                    mDeviceId = VideoInterface.MAIN_TO_APK.SD_LIST_INFO;
                }
                break;
            case StorageDevice.USB:
                mDeviceKey = "UsbInfo";
                mDeviceId = AudioInterface.MAIN_TO_APK.USB_LIST_INFO;
                if (baseLogic instanceof VideoLogic) {
                    mDeviceId = VideoInterface.MAIN_TO_APK.USB_LIST_INFO;
                }
                break;
            case StorageDevice.USB1:
                mDeviceKey = "Usb1Info";
                mDeviceId = AudioInterface.MAIN_TO_APK.USB1_LIST_INFO;
                if (baseLogic instanceof VideoLogic) {
                    mDeviceId = VideoInterface.MAIN_TO_APK.USB1_LIST_INFO;
                }
                break;
            case StorageDevice.USB2:
                mDeviceKey = "Usb2Info";
                mDeviceId = AudioInterface.MAIN_TO_APK.USB2_LIST_INFO;
                if (baseLogic instanceof VideoLogic) {
                    mDeviceId = VideoInterface.MAIN_TO_APK.USB2_LIST_INFO;
                }
                break;
            case StorageDevice.USB3:
                mDeviceKey = "Usb3Info";
                mDeviceId = AudioInterface.MAIN_TO_APK.USB3_LIST_INFO;
                if (baseLogic instanceof VideoLogic) {
                    mDeviceId = VideoInterface.MAIN_TO_APK.USB3_LIST_INFO;
                }
                break;
            default:
                return;
        }

        try {
            if (null == fileList) {
                if (baseLogic != null && !isEnable) {
                    baseLogic.Notify(mDeviceId, null);
                }
                return;
            }

            deviceListIndex = 0;
            int total = (fileList.length / SEND_LEN) + 1;

            for (int i = 0; i < total; i++) {
                if (isEnable) {
                    break;
                }
                deviceListIndex++;
                Packet mPacket1 = new Packet();
                mPacket1.putInt("listInfoIndex", deviceListIndex);
                mPacket1.putInt("listInfoTotal", total);
                if (i == (total - 1)) {
                    mPacket1.putParcelableArray(mDeviceKey, Arrays.copyOfRange(fileList, i * SEND_LEN, fileList.length));
                } else {
                    mPacket1.putParcelableArray(mDeviceKey, Arrays.copyOfRange(fileList, i * SEND_LEN, (i * SEND_LEN) + SEND_LEN));
                }
                if (baseLogic == null) {
                    break;
                } else {
                    baseLogic.Notify(mDeviceId, mPacket1);
                }

                LogUtils.d(TAG, "updateDeviceListInfo i=" + i + ", deviceListIndex=" + deviceListIndex + ", storageId=" + currentStorage);
                SystemClock.sleep(200);
            }
            LogUtils.e(TAG, "FilesSendRunnable run!!!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void needCancel() {
        LogUtils.e(TAG, "needCancel");
        isEnable = true;
        interrupt();
    }
}
