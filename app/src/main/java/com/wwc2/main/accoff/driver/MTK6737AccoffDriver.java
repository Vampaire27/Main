package com.wwc2.main.accoff.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.jni.camera.CameraNative;
import com.wwc2.jni.deepsleep.DeepSleepNative;
import com.wwc2.main.manager.EventInputManager;

/**
 * the mtk6737 acc off driver.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class MTK6737AccoffDriver extends BaseSystemDelaySleepMemoryAccoffDriver {

    /**ACC OFF message.*/
    //private static final String ACC_OFF = "com.android.wwc2.sleep";

    /**ACC ON message.*/
   // private static final String ACC_ON = "com.android.wwc2.wakeup";

    /**CAMERA message.*/
    //private static final String CAMERA = "com.android.wwc2.camera";

    /**广播监听*/
//    private BroadcastReceiver mBroadcastReceivermBroadcastReceiver = new BroadcastReceiver(){
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            LogUtils.d(TAG, "ACC " + action + " receive");
//            if (Intent.ACTION_SCREEN_ON.equals(action)) {
//
//
//            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
//
//            } else if (ACC_OFF.equals(action)) {
//                gotoDeepSleepComming();
//            } else if (ACC_ON.equals(action)) {
//                wakeupFromDeepSleepComming();
//            }
//            //begin zhongyang.hu remove the fast CameraStatus  20170503
//            /*else if (CAMERA.equals(action)) {
//                LogUtils.d(TAG, "System camera start.");
//                final boolean camera = intent.getBooleanExtra("camera", false);
//                LogUtils.d(TAG, "System camera = " + camera);
//                CameraComming(camera);
//                LogUtils.d(TAG, "System camera over.");
//            }*/
//            //end  zhongyang.hu remove the fast CameraStatus 20170503
//        }
//    };

    @Override
    protected boolean getCameraStatus() {
        final int data = CameraNative.CAMERA_read();
        final boolean camera = (0 == data);
        LogUtils.d(TAG, "camera GPIO status, data = " + data + ", camera = " + camera + ", CameraVar = " + EventInputManager.getCamera());
        return camera;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

//        Context context = getMainContext();
//        if (null != context) {
//            IntentFilter myIntentFilter = new IntentFilter();
//            myIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
//            myIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
//            myIntentFilter.addAction(ACC_OFF);
//            myIntentFilter.addAction(ACC_ON);
//            myIntentFilter.addAction(CAMERA);
//            context.registerReceiver(mBroadcastReceiver, myIntentFilter);
//
//            LogUtils.d(TAG, "register screen message.");
//        }
    }

    @Override
    public void onDestroy() {
//        Context context = getMainContext();
//        if (null != context) {
//            LogUtils.d(TAG, "unregister screen message.");
//            context.unregisterReceiver(mBroadcastReceiver);
//        }

        super.onDestroy();
    }

    /**读取深度睡眠GPIO状态，返回true则进入深度睡眠
     * @hide output模式，GPIO不可用 20161018 by wwc2.*/
    private boolean readDeepSleep() {
//        int data = -1;
//        try {
//            FileInputStream fbp = new FileInputStream("/dev/mcustatus");
//            data = fbp.read();
//            fbp.close();
//        } catch (Exception e) {
//            LogUtils.e(TAG, e.getMessage());
//        }
        int data = DeepSleepNative.DEEPSLEEP_read();

        // 判断是否要进入深度睡眠
        boolean ret = false;
        if (0 == data) {
            // MCU工作，上ACC
        } else if (1 == data) {
            // MCU休眠，无ACC
            ret = true;
        } else if (-1 == data) {
            // 错误
        }

        LogUtils.d(TAG, "DeepSleep GPIO status, data = " + data + ", DeepSleep = " + ret);
        return ret;
    }
}
