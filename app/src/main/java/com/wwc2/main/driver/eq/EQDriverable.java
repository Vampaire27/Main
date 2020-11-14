package com.wwc2.main.driver.eq;

/**
 * the eq driver interface.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public interface EQDriverable {
    void enter();
    /**设置等响度*/
    void setLoudness(boolean enable);
    boolean getLoudness();

    /**设置风格*/
    void setStyle(int style);

    /**设置频率值*/
    void setTypeValue(int type, int value);

    /**声场设置*/
    void setX(int x);
    void setY(int y);
    void setField(int[] data);

    /**DSP设置*/
    void setDspSoundEffects(int index, int type, int value);
    void setDspParam(int type, int value);
    void setDspSoundField(int type, int value);
    void resetDsp(int value);
    void set3D(boolean enable);
    void outputDsp();
    void setDspHpfLpf(int type, int value);
    void setQValue(int value);

    void setSubwoofer(boolean enable, int subwooferFreq);
}
