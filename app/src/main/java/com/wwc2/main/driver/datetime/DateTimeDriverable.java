package com.wwc2.main.driver.datetime;

/**
 * the date time driver interface.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public interface DateTimeDriverable {
    /**
     * set time source.
     */
    boolean setTimeSource(String src, int year, int month, int day, int hour, int minute, int second);

    /**
     * set time format.
     */
    boolean setTimeFormat(String format);
}
