package com.wwc2.main.driver.ime.driver;

import android.text.TextUtils;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.ime.IMEListener;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

/**
 * th base ime driver.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public abstract class BaseIMEDriver extends BaseMemoryDriver {

    /**
     * TAG
     */
    private static final String TAG = "BaseIMEDriver";

    /**
     * the power on listener.
     */
    private PowerManager.PowerListener mPowerListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            if (PowerManager.PowerStep.isPoweronCreateOvered(newVal)) {
                final String id = Model().getInputMethodID().getVal();
                if (!TextUtils.isEmpty(id)) {
                    LogUtils.d(TAG, "PoweronStepListener, set input method, id = " + id + ", start.");
                    Packet packet = new Packet();
                    packet.putString("inputMethodID", id);
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.SET_INPUT_METHOD, packet);
                    }
                    LogUtils.d(TAG, "PoweronStepListener, set input method end.");
                }
            }
        }
    };

    /**
     * the acc listener.
     */
    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            if (AccoffStep.isAccoff(oldVal) && !AccoffStep.isAccoff(newVal)) {
                // 从深度睡眠中开机
                final String id = Model().getInputMethodID().getVal();
                if (!TextUtils.isEmpty(id)) {
                    LogUtils.d(TAG, "mAccoffListener, set input method, id = " + id + ", start.");
                    Packet packet = new Packet();
                    packet.putString("inputMethodID", id);
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.SET_INPUT_METHOD, packet);
                    }
                    LogUtils.d(TAG, "PoweronStepListener, set input method end.");
                }
            }
        }
    };

    /**
     * the ime listener.
     */
    private IMEListener mIMEListener = new IMEListener() {
        @Override
        public void InputMethodIDListener(String oldVal, String newVal) {
            if (!TextUtils.isEmpty(newVal)) {
                if (null != mMemory) {
                    mMemory.save();
                }
            }
        }
    };

    /**
     * 数据Model
     */
    protected static class IMEModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString("InputMethodID", mInputMethodID.getVal());
            return packet;
        }

        /**
         * 输入ID
         */
        private MString mInputMethodID = new MString(this, "InputMethodIDListener", "com.google.android.inputmethod.pinyin/.PinyinIME"); //com.android.inputmethod.latin/.LatinIME

        public MString getInputMethodID() {
            return mInputMethodID;
        }
    }

    @Override
    public BaseModel newModel() {
        return new IMEModel();
    }

    /**
     * get the model object.
     */
    protected IMEModel Model() {
        IMEModel ret = null;
        BaseModel model = getModel();
        if (model instanceof IMEModel) {
            ret = (IMEModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        getModel().bindListener(mIMEListener);
        PowerManager.getModel().bindListener(mPowerListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);
    }

    @Override
    public void onDestroy() {
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().unbindListener(mAccoffListener);
        PowerManager.getModel().unbindListener(mPowerListener);
        getModel().unbindListener(mIMEListener);
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "IMEConfig.ini";
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final String id = Model().getInputMethodID().getVal();
            if (!TextUtils.isEmpty(id)) {
                mMemory.set("InputMethod", "ID", id);
            }
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            // 读取输入方法ID
            Object object = mMemory.get("InputMethod", "ID");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getInputMethodID().setVal(string);
                    ret = true;
                }
            }
        }
        return ret;
    }
}
