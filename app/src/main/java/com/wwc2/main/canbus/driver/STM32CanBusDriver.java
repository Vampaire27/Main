package com.wwc2.main.canbus.driver;

import com.wwc2.corelib.db.Packet;

/**
 * the stm32 can bus driver.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public class STM32CanBusDriver extends CanBusDriver {

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean sendData(byte[] val) {
        // 拼装MCU数据
        boolean ret = false;
        //ret = McuManager.sendMcu(0xff, val, val.length);
        return ret;
    }

    @Override
    public void recvData(byte[] val) {
        // 解析MCU透传的CAN数据
        if (null != mCanbusAIDLDriver) {
            byte[] can = null;

//            try {
//                mCanbusAIDLDriver.canbusDataFromMCU(can);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }
    }
}
