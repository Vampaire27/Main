package com.wwc2.main.driver.tptouch;

/**
 * the tp touch driver interface.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public interface TPTouchDriverable {

    /**
     * bind tp position listener.
     *
     * @param tpPositionListener the tp position listener.
     */
    void bindTPPositionListener(TPTouchListener.TPPositionListener tpPositionListener);

    /**
     * unbind tp position listener.
     *
     * @param tpPositionListener the tp position listener.
     */
    void unbindTPPositionListener(TPTouchListener.TPPositionListener tpPositionListener);

    /**
     * reset the tp learn info.
     */
    void reset();

    /**
     * tp code.
     *
     * @param x the tp x position.
     * @param y the tp y position.
     * @param code  the tp code.
     * @param longCode the tp long code.
     * @param lastCode the tp last code.
     */
    boolean TPCode(int x, int y, int code, int longCode, int lastCode);

    /**
     * enter learn the tp touch.
     */
    void enterLearn();

    /**
     * leave learn the tp touch.
     */
    void leaveLearn();

    void setTpCoordinateSwitch(int t);

    void reloadConfig();
}
