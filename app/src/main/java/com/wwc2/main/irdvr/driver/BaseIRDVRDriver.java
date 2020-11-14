package com.wwc2.main.irdvr.driver;

import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.memory.BaseMemoryDriver;

/**
 * the base ir dvr driver.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public abstract class BaseIRDVRDriver extends BaseMemoryDriver implements IRDVRDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseIRDVRDriver";

    /**
     * the model data.
     */
    protected static class IRDVRModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString("Version", getVersion().getVal());
            return packet;
        }

        /**
         * 版本号
         */
        private MString mVersion = new MString(this, "VersionListener", null);

        public MString getVersion() {
            return mVersion;
        }
    }

    @Override
    public BaseModel newModel() {
        return new IRDVRModel();
    }

    /**
     * get the model object.
     */
    protected IRDVRModel Model() {
        IRDVRModel ret = null;
        BaseModel model = getModel();
        if (model instanceof IRDVRModel) {
            ret = (IRDVRModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "IRDVRDataConfig.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (mMemory != null) {
            Object object = mMemory.get("IRDVR", "Version");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getVersion().setVal(string);
                    ret = true;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final String version = Model().getVersion().getVal();
            if (!TextUtils.isEmpty(version)) {
                mMemory.set("IRDVR", "Version", version);
            }
        }
        LogUtils.d(TAG, "writeData ret:" + ret);
        return ret;
    }
}
