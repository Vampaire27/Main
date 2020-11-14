package com.wwc2.main.voice_assistant.driver;

import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MStringArray;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;

/**
 * Created by huwei on 2017/1/28.
 */
public abstract class BaseVoiceAssistantDriver extends BaseMemoryDriver implements VoiceAssistantDriverable {
    private String TAG = BaseVoiceAssistantDriver.class.getSimpleName();
    BaseLogic naviLogic;
    /**
     * the model data.
     */
    protected static class VoiceAssistantModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putBoolean("ShowRecordButton",getShowRecordButton().getVal());
            packet.putBoolean("EnableWakeup",getEnableWakeup().getVal());
            packet.putInt("AudioType", getAudioType().getVal());
            packet.putStringArray("VoiceRealPacketList", getVoiceRealPacketList().getVal());
            packet.putBoolean("FilterNoiseType", getFilterNoiseType().getVal());
            packet.putBoolean("EnableWholeCmd", getEnableWholeCmd().getVal());
            return packet;
        }

        /**显示语言按钮*/
        private MBoolean mShowRecordButton= new MBoolean(this, "ShowRecordButtonListener",true);
        public MBoolean getShowRecordButton() {
            return mShowRecordButton;
        }
        /**升级目录升级路径类型*/
        private MInteger mAudioType = new MInteger(this, "AudioTypeListener", VoiceAssistantDefine.AudioType.KUWO_AUDIO);
        public MInteger getAudioType() {
            return mAudioType;
        }
        /**确认后的语音包名列表*/
        private MStringArray mVoiceRealPacketList = new MStringArray(this, "VoiceRealPacketListListener", null);
        public MStringArray getVoiceRealPacketList() {
            return mVoiceRealPacketList;

        }
        /**启动唤醒词*/
        private MBoolean mEnableWakeup = new MBoolean(this, "EnableWakeupListener", true);
        public MBoolean getEnableWakeup() {
            return mEnableWakeup;
        }
        /**滤波器的噪声类型*/
        private MBoolean mFilterNoiseType = new MBoolean(this, null,false);
        public MBoolean getFilterNoiseType() {
            return mFilterNoiseType;
        }
        /**语音全局指令开关*/
        private MBoolean mEnableWholeCmd = new MBoolean(this, "EnableWholeCmdLstener", false);
        public MBoolean getEnableWholeCmd() {
            return mEnableWholeCmd;
        }
    }


    @Override
    public BaseModel newModel() {
        return new VoiceAssistantModel();
    }

    /**
     * get the model object.
     */
    protected VoiceAssistantModel Model() {
        VoiceAssistantModel ret = null;
        BaseModel model = getModel();
        if (model instanceof VoiceAssistantModel) {
            ret = (VoiceAssistantModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        naviLogic = ModuleManager.getLogicByName(NaviDefine.MODULE);

        // 添加确认后的语音包名列表
        String[] realArray = new String[]{
                "com.aispeech.aios",//思必驰
                "com.txznet.txz",//同行者
        };
        Model().getVoiceRealPacketList().setVal(realArray);
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
    }

    @Override
    public String filePath() {
        return "VoiceDataConfig.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if(mMemory != null) {
            Object objectButton = mMemory.get("VOICE", "ShowRecordButton");
            if (null != objectButton) {
                String string = (String) objectButton;
                if (!TextUtils.isEmpty(string)) {
                    LogUtils.d(TAG, " ShowRecordButton:" + string + "Boolean.parseBoolean(string):" + Boolean.parseBoolean(string));
                    Model().getShowRecordButton().setVal(Boolean.parseBoolean(string));
                    ret = true;
                }
            }
            Object objectAudioType = mMemory.get("VOICE", "AudioType");
            if (null != objectAudioType) {
                String string = (String) objectAudioType;
                if (!TextUtils.isEmpty(string)) {
                    try {
                        LogUtils.d(TAG, " AudioType:" + string);
                        Model().getAudioType().setVal(Integer.parseInt(string));
                        ret = true;
                    }catch (NumberFormatException e){
                    }
                }
            }
            Object objectEnableWakeup = mMemory.get("VOICE", "EnableWakeup");
            if (null != objectEnableWakeup) {
                String string = (String) objectEnableWakeup;
                if (!TextUtils.isEmpty(string)) {
                    try {
                        LogUtils.d(TAG, " EnableWakeup:" + string + " Boolean.valueOf(string):" + Boolean.valueOf(string));
                        Model().getEnableWakeup().setVal(Boolean.valueOf(string));
                        ret = true;
                    }catch (NumberFormatException e){
                    }
                }
            }
            Object objectFilterNoiseType = mMemory.get("VOICE", "FilterNoiseType");
            if (null != objectFilterNoiseType) {
                String string = (String) objectFilterNoiseType;
                if (!TextUtils.isEmpty(string)) {
                    try {
                        LogUtils.d(TAG, " FilterNoiseType:" + string +  " Boolean.valueOf(string):" + Boolean.valueOf(string));
                        Model().getFilterNoiseType().setVal(Boolean.valueOf(string));
                        ret = true;
                    }catch (NumberFormatException e){
                    }
                }
            }
            Object objectEnableWholeCmd = mMemory.get("VOICE", "EnableWholeCmd");
            if (null != objectEnableWholeCmd) {
                String string = (String) objectEnableWholeCmd;
                if (!TextUtils.isEmpty(string)) {
                    try {
                        LogUtils.d(TAG, " EnableWholeCmd:" + string + " Boolean.valueOf(string):" + Boolean.valueOf(string));
                        Model().getEnableWholeCmd().setVal(Boolean.valueOf(string));
                        ret = true;
                    }catch (NumberFormatException e){
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final boolean showButton = Model().getShowRecordButton().getVal();
            mMemory.set("VOICE", "ShowRecordButton", showButton+"");
            final int audioType = Model().getAudioType().getVal();//
            mMemory.set("VOICE", "AudioType", audioType+"");
            final boolean enableWakeup = Model().getEnableWakeup().getVal();//
            mMemory.set("VOICE", "EnableWakeup", enableWakeup+"");
            final boolean filterNoiseType = Model().getFilterNoiseType().getVal();//
            mMemory.set("VOICE", "FilterNoiseType", filterNoiseType+"");
            final boolean enableWholeCmd = Model().getEnableWholeCmd().getVal();//
            mMemory.set("VOICE", "EnableWholeCmd", enableWholeCmd+"");
            ret = true;
        }
        LogUtils.d(TAG,"writeData ret:" + ret);
        return ret;
    }

    @Override
    public void openNavi() {
        if(naviLogic != null){
            if(naviLogic != null){
                String pkgName = naviLogic.getInfo().getString(com.wwc2.main.navi.NaviDefine.SELECTION);
                LogUtils.d(TAG,"openNavi pkgName:" + pkgName);
                SourceManager.runApk(pkgName,null, null,false);
            }
        }
    }

    @Override
    public void exitNavi() {
        if(naviLogic != null){
            String pkgName = naviLogic.getInfo().getString(com.wwc2.main.navi.NaviDefine.SELECTION);
            LogUtils.d(TAG, "openNavi  pkgName:" + pkgName);
            /*-begin-20180511-ydinggen-add-模拟导航，语音关闭导航，播音乐，声音异常-*/
            AudioDriver.Driver().setGpsSoundActive(false);
            /*-end-20180511-ydinggen-add-模拟导航，语音关闭导航，播音乐，声音异常-*/
            SourceManager.onExitPackage(pkgName);
        }
    }

    @Override
    public void showRecordButton(Boolean isShow) {
        Model().getShowRecordButton().setVal(isShow);
        memorySave();
    }

    @Override
    public void chooseAudio(int audioType) {
        Model().getAudioType().setVal(audioType);
        memorySave();
    }

    @Override
    public void setEnableWakeup(boolean enable) {
        Model().getEnableWakeup().setVal(enable);
        memorySave();
    }

    @Override
    public void setEnableWholeCmd(boolean enable) {
        Model().getEnableWholeCmd().setVal(enable);
        memorySave();
    }

    @Override
    public void memorySave() {
        if(mMemory != null){
            mMemory.save();
        }
    }
}
