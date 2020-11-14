package com.wwc2.main.radio;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the model data listener, Open to the public.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public class RadioListener extends BaseListener {

    @Override
    public String getClassName() {
        return RadioListener.class.getName();
    }

    /**
     * 当前波段改变监听器.
     */
    public void BandListener(Integer oldVal, Integer newVal) {
    }

    /**
     * PS信息改变监听器
     */
    public void PSInfoListener(String oldVal, String newVal) {

    }

    /**
     * 立体声状态
     */
    public void STStateListener(Boolean oldVal, Boolean newVal) {

    }
    /**
     * 立体声使能开关
     */
    public void STSwitchListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * 近远程使能开关
     */
    public void LOCEnableListener(Boolean oldVal, Boolean newVal) {

    }

    /**RDS使能开关.*/
    public void RDSEnableListener(Boolean oldVal, Boolean newVal) {

    }

    /**FM保存频率数组.*/
    public void FMFreqArrayListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**FM保存频率数组.*/
    public void AMFreqArrayListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**频点.*/
    public void FreqListener(Integer oldVal, Integer newVal) {

    }

    /**搜索状态.*/
    public void WorkListener(Integer oldVal, Integer newVal) {

    }
    /**收音开关.*/
    public void SwitchEnableListener(Boolean oldVal, Boolean newVal) {

    }

    /**频点.*/
    public void FrequenceSaveOnListener(Integer oldVal, Integer newVal) {

    }
    /**FM区域协议.*/
    public void FMFreqRegionListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**AM区域协议.*/
    public void AMFreqRegionListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * Fm发射开关
     */
    public void FmSendSwitchListener(Boolean oldVal, Boolean newVal) {

    }

    /**Fm发射频率ID.*/
    public void FmSendIndexListener(Integer oldVal, Integer newVal) {

    }

    /**Fm发射频率数组.*/
    public void FmSendFreqArrayListener(Integer[] oldVal, Integer[] newVal) {

    }

    public void RDSTaSwitchListener(Boolean oldVal, Boolean newVal) {

    }

    /** AF */
    public void RDSAfSwitchListener(Boolean oldVal, Boolean newVal) {

    }

    /** RDS电台名 */
    public void RDSStationListener(String oldVal, String newVal) {

    }

    /** RDS节目类型 */
    public void RDSStationTypeListener(Integer oldVal, Integer newVal) {

    }

    public void RDSTPSwitchListener(Boolean oldVal, Boolean newVal) {

    }

    public void RDSTrafficListener(Boolean oldVal, Boolean newVal) {

    }

    public void RDSSignalListener(Boolean oldVal, Boolean newVal) {

    }

    public void RDSNoPtyListener(Boolean oldVal, Boolean newVal) {

    }

    public void RDSSeekStatusListener(Integer oldVal, Integer newVal) {

    }
    public void AngleListener(Integer oldVal, Integer newVal) {

    }
}
