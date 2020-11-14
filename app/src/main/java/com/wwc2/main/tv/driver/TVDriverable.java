package com.wwc2.main.tv.driver;

/**
 * the tv driver interface.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public interface TVDriverable {

    /**
     * 发送XY坐标
     */
    void postXY(float x, float y);

    /**
     * power命令
     */
    void power();

    /**
     * menu命令
     */
    void menu();

    /**
     * exit命令
     */
    void exit();

    /**
     * up命令
     */
    void up();

    /**
     * down命令
     */
    void down();

    /**
     * left命令
     */
    void left();

    /**
     * right命令
     */
    void right();

    /**
     * ok命令
     */
    void ok();

    /**
     * scan命令
     */
    void scan();

    /**
     * pvr命令
     */
    void pvr();
}
