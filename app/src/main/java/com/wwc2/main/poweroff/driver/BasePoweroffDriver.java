package com.wwc2.main.poweroff.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;

/**
 * the base power off driver.
 *
 * @author wwc2
 * @date 2017/1/19
 */
public abstract class BasePoweroffDriver extends BaseDriver implements PoweroffDriverable {

    /**
     * the model data.
     */
    protected static class PoweroffModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putBoolean("PowerOff", mPowerOff.getVal());
            return packet;
        }

        /**关机状态*/
        private MBoolean mPowerOff = new MBoolean(this, "PowerOffListener", false);
        public MBoolean getPowerOff() {
            return mPowerOff;
        }
    }

    @Override
    public BaseModel newModel() {
        return new PoweroffModel();
    }

    /**
     * get the model object.
     */
    protected PoweroffModel Model() {
        PoweroffModel ret = null;
        BaseModel model = getModel();
        if (model instanceof PoweroffModel) {
            ret = (PoweroffModel) model;
        }
        return ret;
    }

    /**关机操作*/
    protected abstract boolean powerOffAction();

    /**开机操作*/
    protected abstract boolean powerOnAction();

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean powerOff() {
        boolean ret = false;
        if (powerOffAction()) {
            Model().getPowerOff().setVal(true);
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean powerOn() {
        boolean ret = false;
        if (powerOnAction()) {
            Model().getPowerOff().setVal(false);
            ret = true;
        }
        return ret;
    }
}
