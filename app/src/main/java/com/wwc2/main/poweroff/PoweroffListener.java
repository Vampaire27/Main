package com.wwc2.main.poweroff;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the power off listener.
 *
 * @author wwc2
 * @date 2017/1/11
 */
public class PoweroffListener extends BaseListener {

    @Override
    public String getClassName() {
        return PoweroffListener.class.getName();
    }

    /**关机状态监听器*/
    public void PowerOffListener(Boolean oldVal, Boolean newVal) {

    }
}
