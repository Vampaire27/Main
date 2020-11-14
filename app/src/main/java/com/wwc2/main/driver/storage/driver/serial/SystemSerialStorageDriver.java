package com.wwc2.main.driver.storage.driver.serial;

import android.text.TextUtils;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerSSS;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.storage.driver.SystemStorageDriver;
import com.wwc2.main.manager.ModuleManager;


import java.io.File;
import java.io.FileFilter;

/**
 * the system serial storage driver.
 *
 * @author wwc2
 * @date 2017/1/1
 */
public abstract class SystemSerialStorageDriver extends SystemStorageDriver implements MountDriverable {
    private final String TAG = "SystemSerialStorageDriver";

    private MountManager mountManager = null;

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        startCheckMount();//启动20s挂载检测
        //hzy_remove no use acc listener
        //ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);
    }

    @Override
    public void onDestroy() {
       // ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().unbindListener(mAccoffListener);
        super.onDestroy();
        if (mountManager != null) {
            mountManager.destroy();//退出20s挂载检测
            mountManager = null;
        }
    }

    /**
     * 获取sd卡cip路径
     *
     * @param path
     * @return
     */
    public String readCidPath(String path) {
        final String prefix = "mmc";
        String cidName = "cid";
        String result = null;
        if (!TextUtils.isEmpty(path)) {
            File cidParentPath = new File(path.substring(0, path.lastIndexOf(prefix)));
            if (cidParentPath.exists()) {
                File[] cidParent = cidParentPath.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.getName().startsWith(prefix)) {
                            return true;
                        }
                        return false;
                    }
                });
                if (cidParent != null && cidParent.length >= 1) {
                    result = cidParent[0].getAbsolutePath() + File.separator + cidName;
                }
            }
//            Log.e(TAG, "readCid2 result:" + result);
        }
        return result;
    }

    /**
     * 挂载设备检查
     *
     * @param storageId
     * @param mounted
     */
    @Override
    public void updateMountState(int storageId, boolean mounted) {/*检查*/
        mountFilterMap.put(storageId,false);
        if (mounted) {
            String serialId = getSerialFromArrayById(storageId);/*已保存的设备编号*/
            String mountSerialId = getSerialByStorageId(storageId);/*已挂载的设备编号*/
            LogUtils.d(TAG, " path:" + StorageDevice.toString(storageId) + " mountSerialId:" + mountSerialId + " serialId:" + serialId);
            LogUtils.d(TAG, StorageDevice.toString(storageId) + "mount success between 0 and 20 s!");
            if (!TextUtils.isEmpty(mountSerialId)) {
                if (!mountSerialId.equals(serialId) && !TextUtils.isEmpty(serialId)) {
                    updateMountSerialByType(storageId, mounted);/*更新设备序列号*/

                    final int index = getStorageIndex(storageId);
                    LogUtils.d(TAG, " updateMountState:" + index);
                    if (-1 != index) {
                        IntegerSSBoolean storage = getStorage(storageId);
                        IntegerSSBoolean temp = new IntegerSSBoolean(storage.getInteger(), storage.getString1(), storage.getString2(), mounted);
                        Model().getStorageList().setVal(index, temp);
                        if (storage != null) {
                            LogUtils.d(TAG, " updateMountState StorageArray merroy type = " + storage.getInteger() +
                                    ", describe = " + storage.getString1() +
                                    ", path = " + storage.getString2() +
                                    ", mount:" + storage.getBoolean());
                            Model().getStorageSerialNo().setVal(new IntegerSSS(storageId, storage.getString1(), storage.getString2(), mountSerialId));
                        } else {
                            LogUtils.d(TAG, "storage: is null!");
                        }
                    }
                } else if (!TextUtils.isEmpty(mountSerialId)) {
                    final int index = getStorageIndex(storageId);
                    LogUtils.d(TAG, " updateMountState:" + index);
                    if (-1 != index) {
                        IntegerSSBoolean storage = getStorage(storageId);
                        if (storage != null) {
                            IntegerSSBoolean temp = new IntegerSSBoolean(storage.getInteger(), storage.getString1(), storage.getString2(), mounted);
                            LogUtils.d(TAG, " updateMountState StorageArray newVal type = " + storage.getInteger() +
                                    ", describe = " + storage.getString1() +
                                    ", path = " + storage.getString2() +
                                    ", mount:" + storage.getBoolean());
                            Model().getStorageList().setVal(index, temp);
                        } else {
                            LogUtils.e(TAG, "getStorage ERROR:  getStorage(storageId):" + storage);
                        }

                    }
                }
            }
        } else {
            /*update serial array*/
            /*update regStorage array*/
            updateMountedByType(storageId, mounted, false);
            LogUtils.w(TAG, StorageDevice.toString(storageId) + "mount fail!");
            updateMountSerialByType(storageId, mounted);/*acc on  Model().getStorages()是未挂载状态不用处理*/
        }
    }

    /**
     * 获取序列号
     *
     * @param storageId 设备编号
     * @return 序列号
     */
    @Override
    public String getSerialByStorageId(int storageId) {
        String result = null;
        switch (storageId) {
            case StorageDevice.NAND_FLASH:
                result = readSerialCid0();
                break;
            case StorageDevice.MEDIA_CARD:
                result = readSerialCid1();
                break;
            case StorageDevice.USB:
                result = readSerialPid0();
                break;
            case StorageDevice.USB1:
                result = readSerialPid1();
                break;
            case StorageDevice.USB2:
                result = readSerialPid2();
                break;
            case StorageDevice.USB3:
                result = readSerialPid3();
                break;
        }
        return result;
    }

    /**
     * 启动检查挂载设备
     */
    private void startCheckMount() {
        mountManager = new MountManager(getMainContext(), this);
        IntegerSSBoolean[] mIntegerSSBoolean = getStorageArray();/**/
        if (mIntegerSSBoolean != null) {
            for (int i = 0; i < mIntegerSSBoolean.length; i++) {
                mountManager.addMountTimer(mIntegerSSBoolean[i].getInteger());
            }
        }
    }


}
