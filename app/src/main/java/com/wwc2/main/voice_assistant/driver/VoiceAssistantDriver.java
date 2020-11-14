package com.wwc2.main.voice_assistant.driver;


import android.os.Handler;
import android.os.Message;

import com.txznet.sdk.TXZConfigManager;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.common.driver.BaseCommonDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.radio.driver.CommandUtil;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;

/**
 * Created by huwei on 2017/1/28.
 */
public class VoiceAssistantDriver extends BaseVoiceAssistantDriver {
    private String TAG = VoiceAssistantDriver.class.getSimpleName();
    public static final byte VOICE_CMD = (byte) McuDefine.ARM_TO_MCU.RPT_CPU_SEND_SOUND;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            /*-begin-20180504-ydinggen-modify-完善语音启动逻辑，解决语音有时无法启动-*/
            stopServiceSafety();
            /*-end-20180504-ydinggen-modify-完善语音启动逻辑，解决语音有时无法启动-*/
            if(!BaseCommonDriver.isGWVersion() || (BaseCommonDriver.isGWVersion() && FactoryDriver.Driver().getVoiceEnable())) {
                //国内版本开启语音服务，国外版本根据配置是否开启语音服务
                startServiceSafety();
            }
        }
    };
    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        mHandler.sendEmptyMessageDelayed(1,5000);
        LogUtils.d(TAG, "onCreate of the TXZ");
    }

    @Override
    public void onDestroy() {
        stopServiceSafety();
        LogUtils.d(TAG, "onDestroy of the TXZ");
        super.onDestroy();
    }

    @Override
    public void triggerRecordButton() {
        LogUtils.d(TAG, "The TXZ did not initial success!triggerRecordButton Failed!");
    }

    @Override
    public void recordShow() {
        writeCmd(VOICE_CMD, new byte[]{0x01});
    }

    @Override
    public void recordDismiss() {
        writeCmd(VOICE_CMD, new byte[]{0x00});
    }

    @Override
    public void setWakeupKeywords(String[] kws) {
        if (!TXZConfigManager.getInstance().isInitedSuccess()) {
            LogUtils.d(TAG, "The TXZ did not initial success!setWakeupKeywords Failed!");
            return;
        }
        TXZConfigManager.getInstance().setWakeupKeywordsNew(kws);
    }

    @Override
    public void setFilterNoiseType(boolean enable) {
        stopServiceSafety();
        Model().getFilterNoiseType().setVal(enable);
        startServiceSafety();
    }

    public void writeCmd(byte head, byte[] radioCmds) {
        int result = McuManager.sendMcu(head, radioCmds, radioCmds.length);
        LogUtils.d(TAG, " -------- write:" + CommandUtil.printHexString(radioCmds) + " result:" + result);
    }

    public void startServiceSafety(){
        LogUtils.d(TAG,"startServiceSafety");
        ApkUtils.startServiceSafety(getMainContext(), VoiceAssistantDefine.VOICE_SERVICE_AIOS_NAME,
                com.wwc2.voiceassistant_interface.VoiceAssistantDefine.VOICE_SERVICE_PACKET_NAME,
                VoiceAssistantDefine.VOICE_SERVICE_CLASS_AIOS_NAME);
    }
    public void stopServiceSafety(){
        if(mHandler.hasMessages(1)){
            mHandler.removeMessages(1);
        }
        LogUtils.d(TAG,"stopServiceSafety");
        ApkUtils.stopServiceSafety(getMainContext(), com.wwc2.voiceassistant_interface.VoiceAssistantDefine.VOICE_SERVICE_AIOS_NAME,
                com.wwc2.voiceassistant_interface.VoiceAssistantDefine.VOICE_SERVICE_PACKET_NAME,
                com.wwc2.voiceassistant_interface.VoiceAssistantDefine.VOICE_SERVICE_CLASS_AIOS_NAME);
    }
}
