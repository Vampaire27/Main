package com.wwc2.main.irdvr;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.irdvr_interface.IRDVRDefine;
import com.wwc2.main.irdvr.driver.IRDVRDriverable;
import com.wwc2.main.irdvr.driver.STM32IRDVRDriver;
import com.wwc2.main.logic.BaseLogic;

/**
 * the ir dvr logic.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public class IRDVRLogic extends BaseLogic {

    @Override
    public String getTypeName() {
        return "IRDVR";
    }

    @Override
    public String getMessageType() {
        return IRDVRDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.irdvr";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_IR_DVR;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public BaseDriver newDriver() {
        return new STM32IRDVRDriver();
    }

    /**
     * the driver interface.
     */
    protected IRDVRDriverable Driver() {
        IRDVRDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof IRDVRDriverable) {
            ret = (IRDVRDriverable) drive;
        }
        return ret;
    }
}
