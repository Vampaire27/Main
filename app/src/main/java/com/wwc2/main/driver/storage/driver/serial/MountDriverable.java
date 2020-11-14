package com.wwc2.main.driver.storage.driver.serial;

/**
 * Created by huwei on 2017/1/13.
 */
public interface MountDriverable {
    String readSerialCid0();
    String readSerialCid1();
    String readSerialPid0();
    String readSerialPid1();
    String readSerialPid2();
    String readSerialPid3();
    void updateMountState(int storageId, boolean mounted);

    public interface MountTimerListener{
        void updateMountState(int storageId, boolean mounted);
    }
}
