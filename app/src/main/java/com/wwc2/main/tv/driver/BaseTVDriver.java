package com.wwc2.main.tv.driver;

import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.memory.BaseMemoryDriver;

/**
 * the base tv driver.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public abstract class BaseTVDriver extends BaseMemoryDriver implements TVDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseTVDriver";

    /**
     * the model data.
     */
    protected static class TVModel extends BaseModel {

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
        return new TVModel();
    }

    /**
     * get the model object.
     */
    protected TVModel Model() {
        TVModel ret = null;
        BaseModel model = getModel();
        if (model instanceof TVModel) {
            ret = (TVModel) model;
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
        return "TVDataConfig.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (mMemory != null) {
            Object object = mMemory.get("TV", "Version");
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
                mMemory.set("TV", "Version", version);
            }
        }
        LogUtils.d(TAG, "writeData ret:" + ret);
        return ret;
    }
}
