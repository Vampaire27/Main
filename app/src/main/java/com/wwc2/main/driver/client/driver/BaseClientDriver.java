package com.wwc2.main.driver.client.driver;

import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MLong;
import com.wwc2.corelib.model.MString;
import com.wwc2.main.driver.client.ClientDriverable;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.provider.LogicProviderHelper;

/**
 * the base client driver.
 *
 * @author wwc2
 * @date 2017/1/19
 */
public abstract class BaseClientDriver extends BaseMemoryDriver implements ClientDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseClientDriver";


    public static final String CLIENT_PROJECT = "ClientProject";
    public static final String CLIENT_BS = "ch001_bs";
    public static final String CLIENT_RP = "ch001_rp";
    public static final String CLIENT_ST = "ch001_st";
    public static final String CLIENT_WT = "ch001_wt";
    public static final String CLIENT_AM = "ch001_am";
    public static final String CLIENT_KS = "ch001_ks";
    public static final String CLIENT_JS = "ch004_js";
    public static final String CLIENT_SJ = "ch004_sj";
    public static final String CLIENT_KS4 = "ch004_ks";

    /**
     * 数据Model
     */
    protected static class ClientModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString("ClientProject", mClientProject.getVal());
            packet.putString("ClientVersion", mClientVersion.getVal());
            packet.putLong("ClientVersionID", mClientVersionID.getVal());
            return packet;
        }

        /**客户项目号*/
        private MString mClientProject = new MString(this, "ClientProjectListener", CLIENT_BS);
        public MString getClientProject() {
            return mClientProject;
        }

        /**
         * 客户版本号
         */
        private MString mClientVersion = new MString(this, "ClientVersionListener", "0.8.16");

        public MString getClientVersion() {
            return mClientVersion;
        }

        /**客户ID版本号*/
        private MLong mClientVersionID = new MLong(this, "ClientIDVersionListener", 0L);
        public MLong getClientVersionID() {
            return mClientVersionID;
        }
    }

    @Override
    public BaseModel newModel() {
        return new ClientModel();
    }

    /**
     * get the model object.
     */
    protected ClientModel Model() {
        ClientModel ret = null;
        BaseModel model = getModel();
        if (model instanceof ClientModel) {
            ret = (ClientModel) model;
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
    public boolean autoSave() {
        return false;
    }

    @Override
    public String filePath() {
        return "ClientDataConfig.ini";
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            mMemory.set("CLIENT", "Project", Model().getClientProject().getVal());

            mMemory.set("CLIENT", "Version", Model().getClientVersion().getVal());

            mMemory.set("CLIENT", "VersionID", Model().getClientVersionID().getVal()+"");

            ret = true;
        }
        return ret;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            Object object = mMemory.get("CLIENT", "Project");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getClientProject().setVal(string);
                    //客户ID不需要保存到数据库
//                    LogicProviderHelper.getInstance().update(BaseClientDriver.CLIENT_PROJECT,string);
                    ret = true;
                }
            }

            object = mMemory.get("CLIENT", "Version");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getClientVersion().setVal(string);
                    ret = true;
                }
            }

            object = mMemory.get("CLIENT", "VersionID");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    long id = 0L;
                    try {
                        id = Long.parseLong(string);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    Model().getClientVersionID().setVal(id);
                    ret = true;
                }
            }
        }
        return ret;
    }
}
