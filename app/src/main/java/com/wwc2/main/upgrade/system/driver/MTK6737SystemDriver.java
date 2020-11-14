package com.wwc2.main.upgrade.system.driver;


import com.wwc2.corelib.db.Packet;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.McuManager;

/**
 * Created by huwei on 2017/1/5.
 */
public class MTK6737SystemDriver extends BaseSystemDriver {
    private final String TAG = MTK6737SystemDriver.class.toString();

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean osUpdate() {
        byte[] data = new byte[1];
        data[0] = (byte) 0x02;
        McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
        return super.osUpdate();
    }
}
