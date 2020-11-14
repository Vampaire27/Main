package com.wwc2.main.driver.audio;

import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.StringII;

/**
 * the audio listener.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public class AudioListener extends BaseListener {

    @Override
    public String getClassName() {
        return AudioListener.class.getName();
    }

    /**声音开始监听器，see {@link AudioDefine.AudioStream}和{@link AudioDefine.AudioFocus}*/
    public void AudioStartListener(StringII val) {

    }

    /**声音停止监听器，see {@link AudioDefine.AudioStream}和{@link AudioDefine.AudioFocus}*/
    public void AudioStopListener(StringII val) {

    }

    /**忽略音频处理包名列表监听器*/
    public void IgnoreAudioPackagesListener(String[] oldVal, String[] newVal) {

    }

    /**
     * 导航出声监听器
     */
    public void NaviAudioActiveListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * ARM出声监听器
     */
    public void ArmAudioActivieListener(Boolean oldVal, Boolean newVal) {

    }
}
