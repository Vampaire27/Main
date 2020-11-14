package com.wwc2.main.upgrade.system.driver;

import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

/**
 * Created by huwei on 2017/1/5.
 */
public abstract class BaseSystemDriver extends BaseMemoryDriver implements SystemDriverable{
    private final String TAG = BaseSystemDriver.class.getSimpleName();

    /**
     * the model data.
     */
    protected static class SystemUpdateModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString("UpdatePath",getUpdatePath().getVal());
            packet.putString("UpdateMode",getUpdateMode().getVal());
            packet.putBoolean("UpdateAuto", getUpdateAuto().getVal());
            return packet;
        }

        /**升级路径*/
        private MString mUpdatePath= new MString(this, "UpdatePathListener", "/storage/usbotg/");
        public MString getUpdatePath() {
            return mUpdatePath;
        }
        /**升级目录升级路径类型*/
        private MString mUpdateMode= new MString(this, "UpdateModeListener", "path");
        public MString getUpdateMode() {
            return mUpdateMode;
        }
        /**自动升级*/
        private MBoolean mUpdateAuto= new MBoolean(this, "UpdateAutoListener", false);
        public MBoolean getUpdateAuto() {
            return mUpdateAuto;
        }
    }

    @Override
    public BaseModel newModel() {
        return new SystemUpdateModel();
    }

    /**
     * get the model object.
     */
    protected SystemUpdateModel Model() {
        SystemUpdateModel ret = null;
        BaseModel model = getModel();
        if (model instanceof SystemUpdateModel) {
            ret = (SystemUpdateModel) model;
        }
        return ret;
    }

    @Override
    public String filePath() {
        return "SystemUpdate.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if(mMemory != null){
            Object object = mMemory.get("SYSTEMUPDATE", "FilePath");
            if (null != object) {
                String string = (String)object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getUpdatePath().setVal(string);
                    LogUtils.d(TAG," FilePath:" + string);
                    ret = true;
                }
            }
            Object objectMode = mMemory.get("SYSTEMUPDATE", "PathMode");
            if (null != objectMode) {
                String string = (String)objectMode;
                if (!TextUtils.isEmpty(string)) {
                    Model().getUpdateMode().setVal(string);
                    LogUtils.d(TAG, " PathMode:" + string);
                    ret = true;
                }
            }

            Object objectAuto = mMemory.get("SYSTEMUPDATE", "Auto");
            if (null != objectAuto) {
                String auto = (String)objectAuto;
                Model().getUpdateAuto().setVal(Boolean.parseBoolean(auto));
                LogUtils.d(TAG, " auto:" + auto);
                ret = true;
            }
        }

        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final String path = Model().getUpdatePath().getVal();//
            if (!TextUtils.isEmpty(path)) {
                mMemory.set("SYSTEMUPDATE", "FilePath", path);
                ret = true;
            }
            final String pathMode = Model().getUpdateMode().getVal();//默认为USB
            if (!TextUtils.isEmpty(path)) {
                mMemory.set("SYSTEMUPDATE", "PathMode", pathMode);
                ret = true;
            }
            final boolean auto = Model().getUpdateAuto().getVal();//默认为USB
            mMemory.set("SYSTEMUPDATE", "Auto", auto+"");
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        LogUtils.d(TAG, "onCreate start.");
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean osUpdate() {
        BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
        if (null != logic) {
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.OS_UPDATE, null);
        }
        return true;
    }
}
