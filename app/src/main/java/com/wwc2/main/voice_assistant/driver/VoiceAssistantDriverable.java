package com.wwc2.main.voice_assistant.driver;

/**
 * the radio module interface.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public interface VoiceAssistantDriverable {
    /**
     * 启动语音识别界面
     */
    void triggerRecordButton();

    /**
     * 显示/隐藏语音助手
     */
    void showRecordButton(Boolean isShow);

    /**
     * 语音助手管理音频
     */
    void chooseAudio(int audioType);

    /**
     * 打开导航
     */
    void openNavi();

    /**
     * 退出导航
     */
    void exitNavi();

    /**
     * 语音启动
     */
    void recordShow();

    /**
     * 语音退出
     */
    void recordDismiss();

    /**
     * 修改唤醒词
     */
    void setWakeupKeywords(String[] kws);

    void memorySave();

    void setEnableWakeup(boolean enable);

    void setFilterNoiseType(boolean enable);

    void setEnableWholeCmd(boolean enable);
}
