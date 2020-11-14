package com.wwc2.main.accoff.driver;

/**
 * the acc off driver interface.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public interface AccoffDriverable {

    /**acc off.*/
    boolean accOff();

    /**acc on.*/
    boolean accOn();

    /**abort acc off.*/
    boolean abortAccOff();
}
