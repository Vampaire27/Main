package com.wwc2.main.settings.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;

/**
 * the base settings driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class SettingsDriver extends BaseDriver implements SettingsDriverable {

    /**
     * the model data.
     */
    protected static class SettingsModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet ret = new Packet();
            ret.putBoolean("ImportState",mImportState.getVal());
            return ret;
        }

        private MBoolean mImportState = new MBoolean(this, "ImportStatusListener", false);

        /**检测到是否要导入EQ*/
        private MBoolean mImportEQState = new MBoolean(this, "ImportEQStatusListener", false);

        public MBoolean getImportState() {
            return mImportState;
        }
        public MBoolean getImportEQState() {
            return mImportEQState;
        }
    }

    @Override
    public BaseModel newModel() {
        return new SettingsModel();
    }

    /**
     * get the model object.
     */
    protected SettingsModel Model() {
        SettingsModel ret = null;
        BaseModel model = getModel();
        if (model instanceof SettingsModel) {
            ret = (SettingsModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }

    public void setImportStatus(boolean status) {
        Model().getImportState().setVal(status);
    }

    public void setImportEQState(boolean status) {
        Model().getImportEQState().setVal(status);
    }

}
