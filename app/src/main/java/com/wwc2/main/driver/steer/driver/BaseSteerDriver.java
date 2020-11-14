package com.wwc2.main.driver.steer.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MByte;
import com.wwc2.corelib.model.MByteArray;
import com.wwc2.main.driver.steer.SteerDriverable;

/**
 * the steer driver base.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class BaseSteerDriver extends BaseDriver implements SteerDriverable {

    /**数据Model*/
    protected static class SteerModel extends BaseModel {
        @Override
        public Packet getInfo() {
           return null;
        }

        private MByte mADKeyStatus = new MByte(this, "ADKeyStatusListener", (byte)0);
        public MByte getADKeyStatus() {
            return mADKeyStatus;
        }

        private MByteArray mADKeyInfo = new MByteArray(this, "ADKeyInfoListener", null);
        public MByteArray getADKeyInfo() {
            return mADKeyInfo;
        }

        private MByte mPanelKeyStatus = new MByte(this, "PanelKeyStatusListener", (byte)0);
        public MByte getPanelKeyStatus() {
            return mPanelKeyStatus;
        }

        private MByteArray mPanelKeyInfo = new MByteArray(this, "PanelKeyInfoListener", null);
        public MByteArray getPanelKeyInfo() {
            return mPanelKeyInfo;
        }
    }

    @Override
    public BaseModel newModel() {
        return new SteerModel();
    }

    /**
     * get the model object.
     */
    protected SteerModel Model() {
        SteerModel ret = null;
        BaseModel model = getModel();
        if (model instanceof SteerModel) {
            ret = (SteerModel) model;
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
