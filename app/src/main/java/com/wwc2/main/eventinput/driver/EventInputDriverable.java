package com.wwc2.main.eventinput.driver;

/**
 * the event input driver interface.
 *
 * @author wwc2
 * @date 2017/1/13
 */
public interface EventInputDriverable {

    /**设置刹车状态*/
    void setBrake(boolean on);

    /**设置倒车状态*/
    void setCamera(boolean on);

    /**设置ACC状态*/
    void setAcc(boolean on, boolean any);

    /**设置ILL状态*/
    void setIll(boolean on, boolean init);

    /**设置左转向灯状态*/
    void setLeftLight(boolean open);

    /**设置右转向灯状态*/
    void setRightLight(boolean open);

    /**设置转向灯状态*/
    void setTurnLight(int value);
}
