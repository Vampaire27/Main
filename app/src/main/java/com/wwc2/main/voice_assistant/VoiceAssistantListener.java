package com.wwc2.main.voice_assistant;

import com.wwc2.corelib.listener.BaseListener;

/**
 * Created by unique on 2017/1/27.
 */
public class VoiceAssistantListener extends BaseListener {

    @Override
    public String getClassName() {
        return VoiceAssistantListener.class.getName();
    }

    /**
     * 语音助手显示隐藏监听.
     */
    public void ShowRecordButtonListener(Boolean oldVal, Boolean newVal) {
    }

    /**
     * 语音控制音频监听
     */
    public void AudioTypeListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 确认后的语音包名列表监听器
     */
    public void VoiceRealPacketListListener(String[] oldVal, String[] newVal) {

    }
    /**
     * 语音是否允许唤醒
     */
    public void EnableWakeupListener(Boolean oldVal, Boolean newVal) {

    }
    /**
     * 语音全局指令开关
     */
    public void EnableWholeCmdLstener(Boolean oldVal, Boolean newVal) {

    }
}
