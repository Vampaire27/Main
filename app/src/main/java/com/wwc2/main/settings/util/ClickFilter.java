package com.wwc2.main.settings.util;

import android.os.SystemClock;

import com.wwc2.corelib.utils.log.LogUtils;

/**
 * Created by Administrator on 2016/4/27 0027.
 */
public class ClickFilter {
    //    public static final long INTERVAL = 1000L; //防止连续点击的时间间隔
    private static long lastClickTime = 0L; //上一次点击的时间

    public static boolean filter(Long minTime) {

        long time = SystemClock.uptimeMillis();

        if (lastClickTime == 0L) {
            lastClickTime = time;
            return false;
        } else {
            if ((time - lastClickTime) > minTime) {
                lastClickTime = time;
                return false;
            } else {
                LogUtils.d("ClickFilter","click too fast");
                return true;
            }

        }
    }
}