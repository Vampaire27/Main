package com.wwc2.main.driver.backlight.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.jni.backlight.BacklightNative;

/**
 * the gpio back light driver.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public class MTK6737BacklightDriver extends BaseBacklightDriver {

    /**
     * TAG
     */
    private static final String TAG = "MTK6737BacklightDriver";

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        // 初始化
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean open(boolean open) {
        boolean ret = false;//YDG
        if (open) {
            LogUtils.d(TAG, "APP open back light action.");
            ret = (1 == BacklightNative.BACKLIGHT_open());
        } else {
            LogUtils.d(TAG, "APP close back light action.");
            ret = (1 == BacklightNative.BACKLIGHT_close());
        }
        return ret;
    }

    @Override
    protected boolean auto(boolean auto) {
        return false;
    }

    @Override
    protected boolean getBacklightOpenOrClose() {
        boolean ret = super.getBacklightOpenOrClose();

        //final boolean real =  BacklightNative.BACKLIGHT_read();

       // boolean real =false;

       // int backlightValue = BacklightNative.BACKLIGHT_read();

        //if(backlightValue > 0 ){
         //   real = true;
       // }

        //if (ret != real) {
          //  LogUtils.w(TAG, "getBacklightOpenOrClose result does not match real Native value, ret = " + ret + ", real = " + real);
         //   ret = real;
        //}
        return ret;
    }
}
