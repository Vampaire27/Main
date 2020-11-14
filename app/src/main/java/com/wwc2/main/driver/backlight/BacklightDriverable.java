package com.wwc2.main.driver.backlight;

/**
 * the back light interface.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public interface BacklightDriverable {

    /**open back light.*/
    boolean open();

    /**close back light.*/
    boolean close();

    /**set back light auto brightness.*/
    boolean setAuto(boolean auto);

    /**set temp value and the {@link BacklightListener#BacklightValueListener(Integer, Integer)} not changed.*/
    boolean setBacklightNight();

    /**resume temp value, appear in pairs with {@link #setBacklightNight()}.*/
    boolean setBacklightDay();

    /**
     * 设置背光亮度 YDG 2017－11－27
     */
    void setBacklightness(int value, int type);

    /**
     * 改变背光模式
     */
    void changeBacklightMode();

    void setIllState(boolean on);

    int getBacklightMode();

    int getBacklightness();
}
