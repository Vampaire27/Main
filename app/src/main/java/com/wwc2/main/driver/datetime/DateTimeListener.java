package com.wwc2.main.driver.datetime;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the date time listener.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class DateTimeListener extends BaseListener {

    @Override
    public String getClassName() {
        return DateTimeListener.class.getName();
    }

    public void TimeSource(String oldLgg, String newLgg){}
    public void TimeFormat(String oldLgg, String newLgg){}
}
