package com.wwc2.main.poweroff.driver;

import com.wwc2.corelib.db.Packet;

/**
 * mtk6737 power off driver.
 *
 * @author wwc2
 * @date 2017/1/19
 */
public class SystemPoweroffDriver extends BasePoweroffDriver {

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean powerOffAction() {
        return true;
    }

    @Override
    protected boolean powerOnAction() {
        return true;
    }
}
