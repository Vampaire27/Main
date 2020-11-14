package com.wwc2.main.driver.storage;

import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.IntegerSS;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerSSS;

/**
 * the storage listener.
 *
 * @author wwc2
 * @date 2017/1/29
 */
public class StorageListener extends BaseListener {

    @Override
    public String getClassName() {
        return StorageListener.class.getName();
    }

    /**存储设备发生变化, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
    public void StorageInfoListener(IntegerSSBoolean oldVal, IntegerSSBoolean newVal) {

    }

    /**存储设备状态, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
    public void StorageListListener(IntegerSSBoolean[] oldVal, IntegerSSBoolean[] newVal) {

    }
    /**存储设备序列号发生变化*/
    public void StorageInfoListListener(IntegerSS[] oldVal, IntegerSS[] newVal) {

    }
    /**存储设备序列号发生变化*/
    public void StorageSerialNoListener(IntegerSSS oldVal, IntegerSSS newVal) {

    }

}
