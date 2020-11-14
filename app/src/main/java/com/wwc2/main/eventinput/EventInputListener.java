package com.wwc2.main.eventinput;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the event input listener.
 *
 * @author wwc2
 * @date 2017/1/13
 */
public class EventInputListener extends BaseListener {

    @Override
    public String getClassName() {
        return EventInputListener.class.getName();
    }

    /**刹车状态监听器*/
    public void BrakeListener(Boolean oldVal, Boolean newVal) {

    }

    /**倒车状态监听器*/
    public void CameraListener(Boolean oldVal, Boolean newVal) {

    }

    /**ACC状态监听器*/
    public void AccListener(Boolean oldVal, Boolean newVal) {

    }

    /**ILL状态监听器*/
    public void IllListener(Boolean oldVal, Boolean newVal) {

    }

    /**左转向灯状态监听器*/
    public void LeftLightListener(Boolean oldVal, Boolean newVal) {

    }

    /**右转向灯监听器*/
    public void RightLightListener(Boolean oldVal, Boolean newVal) {

    }

    /**按键监听器*/
    public void KeyListener(int key, int origin) {

    }

    public void TurnLightListener(Integer oldVal, Integer newVal) {

    }
}
