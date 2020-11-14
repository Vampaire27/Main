package com.wwc2.main.driver.steer;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the steer listener.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class SteerListener extends BaseListener {
    @Override
    public String getClassName() {
        return SteerListener.class.getName();
    }
    public void ADKeyStatusListener(Byte oldState, Byte newState){}
    public void ADKeyPressedListener(Byte oldCode, Byte newCode){}
    public void ADKeyImpedanceListener(Byte[] oldImpedance, Byte[] newImpedance){}
    public void ADKeyInfoListener(Byte[] oldInfo, Byte[] newInfo){}

    public void PanelKeyStatusListener(Byte oldState, Byte newState){}
    public void PanelKeyInfoListener(Byte[] oldInfo, Byte[] newInfo){}
}
