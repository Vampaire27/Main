package com.wwc2.main.driver.tptouch.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.util.Pair;
import android.view.MotionEvent;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * the mtk6737 tp driver.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public class MTK6737TPTouchDriver extends BaseTpTouchDriver {

    private static final String TAG = "MTK6737TPTouchDriver";

    public static final String TOUCH_KEYCODE = "com.wwc2.keycode.event";


    private static final String POSITION_X_37T = "/sys/bus/platform/drivers/mtk-tpd/soc:touch/tpd_position_X";
    private static final String POSITION_Y_37T = "/sys/bus/platform/drivers/mtk-tpd/soc:touch/tpd_position_Y";
    private static final String POSITION_X_37M = "/sys/bus/platform/drivers/mtk-tpd/bus:touch@/tpd_position_X";
    private static final String POSITION_Y_37M = "/sys/bus/platform/drivers/mtk-tpd/bus:touch@/tpd_position_Y";
    private static final String POSITION_X_6580 = "/sys/bus/platform/drivers/mtk-tpd/bus:touch@0/tpd_position_X";
    private static final String POSITION_Y_6580 = "/sys/bus/platform/drivers/mtk-tpd/bus:touch@0/tpd_position_Y";
    private static final String POSITION_X_39 = "/sys/devices/platform/touch/tpd_position_X";
    private static final String POSITION_Y_39 = "/sys/devices/platform/touch/tpd_position_Y";

    private static final String POSITION_X_6763 = "/sys/devices/platform/touch/tpd_position_X";
    private static final String POSITION_Y_6763 = "/sys/devices/platform/touch/tpd_position_Y";


    private int mQueryReleaseTimerId = 0;

    private String platformID = null;

    // 广播监听
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TOUCH_KEYCODE.equals(action)) {
                // TP按键
                int motion = intent.getIntExtra("action", -1);
                if (-1 != motion) {
                    if (MotionEvent.ACTION_DOWN == motion) {
                        // 点击
                        tpDown();
                    } else if (MotionEvent.ACTION_UP == motion) {
                        // 弹起
                        tpUp();
                    }
                }
            }
        }

    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        Context context = getMainContext();
        if (null != context) {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(TOUCH_KEYCODE);
            context.registerReceiver(mBroadcastReceiver, myIntentFilter);
        }
        platformID = getPlatformID();
    }


    @Override
    public void onDestroy() {
        Context context = getMainContext();
        if (null != context) {
            try {
                context.unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

    private void tpDown() {
        // 点击
        if (mbUpComming) {
            mbUpComming = false;
            Pair<Integer, Integer> pair = getTPPosition();
            TPDown(pair.first, pair.second);
            LogUtils.d(TAG, "TP down, position xy = " + pair.first + ", " + pair.second);

            TimerUtils.killTimer(mQueryReleaseTimerId);
            mQueryReleaseTimerId = TimerUtils.setTimer(getMainContext(), 100, 100, new Timerable.TimerListener() {
                @Override
                public void onTimer(int timerId) {
                    if (isRelease()) {
                        LogUtils.w(TAG, "TP release, simulate tp up action.");
                        tpUp();
                    }
                }
            });
        }
    }

    private void tpUp() {
        TimerUtils.killTimer(mQueryReleaseTimerId);
        mQueryReleaseTimerId = 0;

        // 弹起
        mbUpComming = true;
        Pair<Integer, Integer> pair = getTPPosition();
        TPUp(pair.first, pair.second);
        LogUtils.d(TAG, "TP up , position xy = " + pair.first + ", " + pair.second);

    }

    private boolean isRelease() {
        boolean ret = false;
        Pair<Integer, Integer> pair = getTPPosition();

        if (0 == pair.first && 0 == pair.second) {
            ret = true;
        }
        return ret;
    }

    private Pair<Integer, Integer> getTPPosition() {
        Pair<Integer, Integer> pair = new Pair<>(-1, -1);
        String xpath = null, ypath = null;
        BufferedReader bufferedReaderx = null;
        BufferedReader bufferedReadery = null;
        if (platformID.contains("mt6737t")) {
            xpath = POSITION_X_37T;
            ypath = POSITION_Y_37T;
        } else if(platformID.contains("mt6580")){
            xpath = POSITION_X_6580;
            ypath = POSITION_Y_6580;
        } else if (platformID.contains("mt6763")){
            xpath = POSITION_X_6763;
            ypath = POSITION_Y_6763;
        } else if (platformID.contains("mt6737m")){ // zhongyang.hu  mt6753 mt6737m mt6580 is some,use this as a default value 20181015
            xpath = POSITION_X_37M;
            ypath = POSITION_Y_37M;
        } else {//后续由驱动改成下面默认"mt6739"的节点。
            xpath = POSITION_X_39;
            ypath = POSITION_Y_39;
        }
        try {
            bufferedReaderx = new BufferedReader(new FileReader(xpath));
            String strx = bufferedReaderx.readLine();
            bufferedReadery = new BufferedReader(new FileReader(ypath));
            String stry = bufferedReadery.readLine();
            LogUtils.d(TAG, "x:" + strx + "\ty:" + stry);
            pair = new Pair<>(Integer.valueOf(strx), Integer.valueOf(stry));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReaderx != null) {
                try {
                    bufferedReaderx.close();
                    bufferedReadery.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return pair;
    }
    private String getPlatformID() {
        return SystemProperties.get("ro.board.platform", "");
    }
}
