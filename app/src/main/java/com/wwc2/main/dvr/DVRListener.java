package com.wwc2.main.dvr;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the dvr listener.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public class DVRListener extends BaseListener {

    @Override
    public String getClassName() {
        return DVRListener.class.getName();
    }

    public void AutoSaveTimeListener(String oldVal, String newVal) {

    }
    public void VideoQualityListener(String oldVal, String newVal) {

    }
    public void MuteRecordListener(Boolean oldVal, Boolean newVal) {

    }
    public void AutoRecordListener(Boolean oldVal, Boolean newVal) {

    }
    public void WatermarkListener(Boolean oldVal, Boolean newVal) {

    }
    public void LocationListener(String oldVal, String newVal) {

    }
}
