package com.wwc2.main.driver.vrkey.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.main.driver.vrkey.VrKeyDriverable;

/**
 * the base vr key driver.
 *
 * @author wwc2
 * @date 2017/1/21
 */
public abstract class BaseVrKeyDriver extends BaseDriver implements VrKeyDriverable {

    /**
     * 数据Model
     */
    protected static class VrKeyModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            return packet;
        }
    }

    @Override
    public BaseModel newModel() {
        return new VrKeyModel();
    }

    /**
     * get the model object.
     */
    protected VrKeyModel Model() {
        VrKeyModel ret = null;
        BaseModel model = getModel();
        if (model instanceof VrKeyModel) {
            ret = (VrKeyModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }
}