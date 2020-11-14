package com.wwc2.main.driver.backlight;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the backlight listener.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public class BacklightListener extends BaseListener {

    @Override
    public String getClassName() {
        return BacklightListener.class.getName();
    }

    /**背光开关状态监听器*/
    public void BacklightOpenOrCloseListener(Boolean oldVal, Boolean newVal) {

    }

    /**自动背光开关监听器*/
    public void BacklightAutoSwitchListener(Boolean oldVal, Boolean newVal) {

    }

    /**背光值监听器，0~100(适用于视频和设置界面只有一个SEEKBAR，类似爱民)*/
    public void BacklightValueListener(Integer oldVal, Integer newVal) {

    }
    /**白天模式背光值监听器，0~100*/
    public void BacklightValueDayListener(Integer oldVal, Integer newVal) {

    }

    /**黑夜模式背光值监听器，0~100*/
    public void BacklightValueNightListener(Integer oldVal, Integer newVal) {

    }

    /**背光模式变化监听器*/
    public void BacklightModeListener(Integer oldVal, Integer newVal) {

    }

    /**背光是否允许调节*/
    public void BacklightAdjustEnableListener(Boolean oldVal, Boolean newVal) {

    }
}
