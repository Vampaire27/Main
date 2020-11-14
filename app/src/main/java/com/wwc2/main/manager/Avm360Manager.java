package com.wwc2.main.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.SystemProperties;

import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author wwc2
 * @date 2017/1/19
 */
public class Avm360Manager {

    private static final String ACTION_AVM360_READYOK = "com.baios.avm360.readyok";
    private static final String KEY_AVM360 = "msg";

    private static Camera mCameraBack = null;
    private static Context mContext = null;

    private static BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context paramContext, Intent intent) {
            String action = intent.getAction();
            LogUtils.d("ACTION_AVM360_READYOK----action=" + action);
            if (action.equals(ACTION_AVM360_READYOK)) {
                String value = intent.getStringExtra(KEY_AVM360);
                LogUtils.d("ACTION_AVM360_READYOK----value=" + value);
                if (value.equals("360ready")) {//360启动后，获取camera数据后，发送至系统。

                } else if (value.equals("360roundover")) {//360 3D环视完毕,发送至系统。

                }
            }
        }
    };

    public static boolean openCameraBack(Context context, boolean start) {
        if (context != null) {
            mContext = context;
        }

        if (!start) {
            SystemProperties.set("wwc2.camera.running", "0");
            String value = 0 + " " + 7;
            writeTextFile(value, "/sys/bus/platform/devices/wwc2_camera_combine/camera_action");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String ret1 = SystemProperties.get("wwc2.camera.running");
            String ret2 = SystemProperties.get("wwc2.avm360cb.running");
            if ("1".equals(ret1) && "1".equals(ret2)) {
                return false;
            } else {
                LogUtils.d("---openCameraBack.------ret1=" + ret1 + ", ret2=" + ret2);
                return false;//暂不拉360
            }
        }

        if (mContext != null) {
            if (ApkUtils.isAPKExist(mContext, "com.baony.avm360")) {
                ComponentName online = new ComponentName("com.baony.avm360", "com.baony.ui.activity.AVMBVActivity");
                Intent mIntent = new Intent();
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.setComponent(online);
                mContext.startActivity(mIntent);

                SystemProperties.set("wwc2.avm360.enable", "1");
                SystemProperties.set("persist.wwc2camera.platformtype", "quart_360");//AVM360版本需要写一下此属性，解决在DVR版本升级360版本需要恢复出厂设置问题。
            }
        }

        //由AVM360打开Camera
//        try {
//            if (mCameraBack == null) {
//                LogUtils.d("---openCameraBack.------");
//                IntentFilter filter = new IntentFilter();
//                filter.addAction(ACTION_AVM360_READYOK);
//                mContext.registerReceiver(mIntentReceiver, filter);
//
//                mCameraBack = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//                if (mCameraBack != null) {
//                    startPreview();
//                }
//            }
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        }

        return true;
    }

    public static void closeBackCamera(){//直接杀掉AVM360
        if (null != mCameraBack) {
            mCameraBack.stopPreview();
            mCameraBack.release();
            mCameraBack = null;
            LogUtils.d("---close Camera----后camera---close over.------");
            try {
                mContext.unregisterReceiver(mIntentReceiver);
            } catch (Exception e) {
                LogUtils.e("---close Camera----后camera---close error.------");
                e.printStackTrace();
            }
        }
    }

    private static void startPreview() {
        LogUtils.d("####startPreview");

        try {
            Camera.Parameters params= mCameraBack.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            params.setPictureFormat(ImageFormat.JPEG);
            params.set("first-preview-frame-black", 1);
            params.setPreviewSize(1280, 720);
            mCameraBack.setParameters(params);
            mCameraBack.setPreviewDisplay(null);
            mCameraBack.startPreview();

            String value = 0 + " " + 0;
            writeTextFile(value, "/sys/devices/platform/wwc2_camera_combine/camera_action");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeTextFile(String tivoliMsg, String fileName) {
        try {
            byte[] bMsg = tivoliMsg.getBytes();
            FileOutputStream fOut = new FileOutputStream(fileName);
            fOut.write(bMsg);
            fOut.getFD().sync();
            fOut.close();
        } catch (IOException e) {
            //throw the exception
        }
    }
}
