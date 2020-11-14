package com.wwc2.main.driver.ime;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the ime listener.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class IMEListener extends BaseListener {

    @Override
    public String getClassName() {
        return IMEListener.class.getName();
    }

    /**
     * input method id listener.
     */
    public void InputMethodIDListener(String oldVal, String newVal) {

    }
}
