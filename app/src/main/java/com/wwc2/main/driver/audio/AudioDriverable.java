package com.wwc2.main.driver.audio;

/**
 * the audio driver interface.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public interface AudioDriverable {

    /**音频控制接口*/
    public interface AudioControlable {
        /**暂停*/
        void pause();

        /**播放*/
        void play();

        /**是否正在播放*/
        boolean isPlay();
    }

    /**
     * 申请音频焦点
     * @param control 音频操作接口
     * @param stream 音频流类型，see {@link AudioDefine.AudioStream}
     * @param focus 音频焦点控制，see {@link AudioDefine.AudioFocus}
     * @return see {@link AudioDefine.AudioStatus}
     */
    int request(AudioControlable control, int stream, int focus);

    /**
     * 是否音频焦点
     * @return see {@link AudioDefine.AudioStatus}
     */
    int abandon();

    /**
     * 设置导航报点状态
     */
    void setGpsSoundActive(boolean active);

    /**
     * 设置通话状态
     */
    void setBlueCallActive(boolean active);
}
