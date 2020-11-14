package com.wwc2.main.driver.storage;

import com.wwc2.corelib.model.custom.IntegerSSBoolean;

/**
 * the storage driver interface.
 *
 * @author wwc2
 * @date 2017/1/29
 */
public interface StorageDriverable {

    /**
     * 根据存储设备类型获取存储设备信息,
     * type, describe, path, mounted,
     * see {@link com.wwc2.common_interface.utils.StorageDevice}
     */
    IntegerSSBoolean getStorageInfo(int type);

    IntegerSSBoolean[] getStoragesInfo();

    /**
     * 更新USB设备挂载状态
     * @param type
     * @param mount
     */
    void updateMountedStatus(Integer type, boolean mount);
}
