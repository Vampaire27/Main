package com.wwc2.main.driver.eq;

import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.FourInteger;

/**
 * the EQ listener.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class EQListener extends BaseListener {

    @Override
    public String getClassName() {
        return EQListener.class.getName();
    }

    /**EQ的风格*/
    public void StyleListener(Integer oldType, Integer newType){}

    /**等响度*/
    public void LoudnessListener(Boolean oldType, Boolean newType){}

    /**超重音*/
    public void SubwooferListener(Integer oldType, Integer newType){}
    /**重音*/
    public void BassListener(Integer oldType, Integer newType){}
    /**中音*/
    public void MiddleListener(Integer oldType, Integer newType){}
    /**高音*/
    public void TrebleListener(Integer oldType, Integer newType){}
    /**超重音开关*/
    public void SubwooferSwitchListener(Boolean oldType,Boolean newType){}
    /**超重音类型*/
    public void SubwooferFreqListener(Integer oldType, Integer newType){}

    /**声场设置*/
    public void XListener(Integer oldX, Integer newX) {}
    public void YListener(Integer oldY, Integer newY) {}
    public void SoundFiledListener(Byte[] oldValue, Byte[] newValue){}
    public void SoundFieldHiddenListener(Boolean newValue){}
    /**
     * DSP
     */
    public void DspSoundEffectListener(FourInteger[] oldValue, FourInteger[] newValue) {}
    public void DspParamListener(Integer[] oldValue, Integer[] newValue) {}
    public void DspSoundFiledListener(Integer[] oldValue, Integer[] newValue) {}
    public void ThreeeDSwitchListener(Boolean oldValue, Boolean newValue){}
    public void EQSendMcuFinishedListener(Boolean oldValue, Boolean newValue){}
    public void DspHpfLpfListener(Integer[] oldValue, Integer[] newValue) {}
    public void QValueListener(Integer oldValue, Integer newValue) {}

}
