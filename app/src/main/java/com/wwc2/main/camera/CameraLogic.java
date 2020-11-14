package com.wwc2.main.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.avin_interface.AvinInterface;
import com.wwc2.camera_interface.CameraDefine;
import com.wwc2.camera_interface.CameraInterface;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.message.MessageDefine;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.avin.AvinLogic;
import com.wwc2.main.canbus.driver.CanBusDriver;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.backlight.driver.BaseBacklightDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.Avm360Manager;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PanoramicManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * the camera logic.
 *
 * @author wwc2
 * @date 2017/1/19
 */
public class CameraLogic extends AvinLogic {

    /**
     * TAG
     */
    private static final String TAG = "CameraLogic";

    /**
     * 进入倒车前的source
     */
    private static int mEnterCameraSource = Define.Source.SOURCE_NONE;

    /**
     * camera lock object.
     */
    protected static final Lock mCameraLock = new ReentrantLock();

    private static boolean mIllStatus   = false;
    private static int mBacklightMode = BaseBacklightDriver.DAY;
    private static Context mContext = null;

    /**CAMERA message.*/
    private static final String CAMERA = "com.android.wwc2.camera";

    private static final String PANORAMIC_PKG_NAME  = "com.sjs.vrbackcarapp";
    private static final String PANORAMIC_SERVICE_NAME  = "com.sjs.vrbackcar.AutoStartService";

    public static final String FLOAT_WINDOW_SERVICE_NAME = "com.wwc2.monitor.MainService";
    public static final String FLOAT_WINDOW_SERVICE_PACKET_NAME = "com.wwc2.monitor";
    public static final String FLOAT_WINDOW_SERVICE_CLASS_NAME = "com.wwc2.monitor.MainService";

    @Override
    public String getTypeName() {
        return "Camera";
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.camera";
    }

    @Override
    public String getMessageType() {
        return CameraDefine.MODULE;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_CAMERA;
    }

    @Override
    public boolean isHFPFloatHideSource() {
        //解决通话时无法进入倒车的问题。
        if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                !FactoryDriver.Driver().getDvrEnable()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean available() {
        //修改倒车的处理，解决倒车时语音不隐藏和视频不暂停的问题。2019-10-11
        if (ApkUtils.isAPKExist(getMainContext(), "com.baony.avm360")) {
            return true;
        }

        if (ApkUtils.isAPKExist(getMainContext(), FLOAT_WINDOW_SERVICE_PACKET_NAME) && PowerManager.isRuiPai()) {
            LogUtils.d("PowerManager.isRuiPai!");
            return true;
        } else {
            LogUtils.e("PowerManager.isRuiPai!isRuiPai=" + PowerManager.isRuiPai());
        }

        if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                !FactoryDriver.Driver().getDvrEnable()) {
            return (enable() && isReady());
        }
        return true;
    }

    @Override
    public boolean isReady() {
        final boolean camera = EventInputManager.getCamera();
        final boolean fast_camera = ModuleManager.getLogicByName(AccoffDefine.MODULE).getInfo().getBoolean("FastCamera");
        final boolean ready = (camera | fast_camera | CanBusDriver.getPanoramicView());
        final boolean service_running = ApkUtils.isServiceRunning(getMainContext(), com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
        LogUtils.d(TAG, "camera = " + camera + ", fast_camera = " + fast_camera +
                ", ready = " + ready + ", service_running = " + service_running);
        return ready;
    }

    @Override
    public boolean passive() {
        return true;
    }

    @Override
    public boolean isFullScreenSource() {
        return true;
    }

    @Override
    public boolean isVolumeHideSource() {
        return true;
    }

    @Override
    public boolean isVoiceHideSource() {
        return true;
    }

   /* @Override
    public boolean isHFPFloatHideSource() {
        return true;
    }*/

    @Override
    public boolean runApk() {
        //ApkUtils.runApk(getMainContext(), getAPKPacketName(), null, true);
        return true;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        // enter the camera, start camera service.
        LogUtils.d(TAG, "start camera service start.");
        if (CanBusDriver.getPanoramicView()) {
            if (ApkUtils.isServiceRunning(getMainContext(), com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_NAME)) {
                LogUtils.e(TAG, "start camera service retrun Panoramic.");
                return;
            }
        }
        mContext = getMainContext();

        if (ApkUtils.isAPKExist(getMainContext(), PANORAMIC_PKG_NAME) && FactoryDriver.Driver().getPanoramicType() == 1) {
//            Intent intent = new Intent(PANORAMIC_SERVICE_NAME);
//            ComponentName component = new ComponentName(PANORAMIC_PKG_NAME, PANORAMIC_SERVICE_NAME);
//            intent.setComponent(component);

            Intent intent = new Intent();
            intent.setAction("com.sjs.vrbackcar.ACTION_AUTOSTARTSERVICE");
            intent.setPackage(PANORAMIC_PKG_NAME);
            intent.putExtra("status", 1);
            getMainContext().startService(intent);
        } else if (ApkUtils.isAPKExist(getMainContext(), FLOAT_WINDOW_SERVICE_PACKET_NAME)) {
            LogUtils.e(TAG, "start camera service panoramic isExist=" + ApkUtils.isServiceRunning(getMainContext(), FLOAT_WINDOW_SERVICE_NAME));
            ApkUtils.startServiceSafety(mContext, FLOAT_WINDOW_SERVICE_NAME,
                    FLOAT_WINDOW_SERVICE_PACKET_NAME,
                    FLOAT_WINDOW_SERVICE_CLASS_NAME);
        } else {
            if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                    !FactoryDriver.Driver().getDvrEnable()) {
                LogUtils.e(TAG, "start camera service Panoramic." + ApkUtils.isAPKExist(getMainContext(), PANORAMIC_PKG_NAME) +
                        ", switch=" + FactoryDriver.Driver().getPanoramicType());
                ApkUtils.stopServiceSafety(getMainContext(), com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_NAME,
                        com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                        com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
                ApkUtils.startServiceSafety(getMainContext(), com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_NAME,
                        com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                        com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
            }
        }
        LogUtils.d(TAG, "start camera service over.");

        //打开360
        PanoramicManager.getInstance().sendCMDToPanoramic(PanoramicManager.CMD_OPEN, false);

        if (ApkUtils.isAPKExist(mContext, "com.baony.avm360")) {
            Avm360Manager.openCameraBack(getMainContext(), false);
        }
        final int curSource = SourceManager.getCurSource();
        final int oldSource = SourceManager.getOldSource();
        LogUtils.d(TAG, "Enter camera, curSource = " + Define.Source.toString(curSource) + ", oldSource = " + Define.Source.toString(oldSource));

        //begin zhongyang.hu add will need not  save accoff source. 20170609
        // 20171027 add SOURCE_NONE for   SOURCE_NONE to SOURCE_CAMERA ,cannot exit SOURCE_CAMERA.
        if(oldSource == Define.Source.SOURCE_ACCOFF || oldSource == Define.Source.SOURCE_NONE)  {
            mEnterCameraSource = SourceManager.getLastNoPoweroffRealSource();
        }else{
            mEnterCameraSource = oldSource;
        }

        //end zhongyang.hu add will need not  save accoff source. 20170609

        mBacklightMode = BacklightDriver.Driver().getBacklightMode();
//        if (EventInputManager.getIll()) {
        if (mBacklightMode == BaseBacklightDriver.NIGHT) {
            BacklightDriver.Driver().setBacklightDay();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (CanBusDriver.getPanoramicView()) {
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_CAMERA, true, null);
            LogUtils.e(TAG, "stop camera service return Panoramic.");
            return;
        }
        // leave the camera, stop camera service.
        LogUtils.d(TAG, "stop camera service start.");
        if (ApkUtils.isAPKExist(getMainContext(), PANORAMIC_PKG_NAME) && FactoryDriver.Driver().getPanoramicType() == 1) {
            if (ApkUtils.isAPKExist(getMainContext(), PANORAMIC_PKG_NAME) && FactoryDriver.Driver().getPanoramicType() == 1) {
                Intent intent = new Intent();
                intent.setAction("com.sjs.vrbackcar.ACTION_AUTOSTARTSERVICE");
                intent.setPackage(PANORAMIC_PKG_NAME);
                intent.putExtra("status", 0);
                getMainContext().startService(intent);
            }
        } else if (ApkUtils.isAPKExist(getMainContext(), FLOAT_WINDOW_SERVICE_PACKET_NAME)) {
//            ApkUtils.stopServiceSafety(getMainContext(), PanoramicManager.FLOAT_WINDOW_SERVICE_CLASS_NAME,
//                    PanoramicManager.FLOAT_WINDOW_SERVICE_PACKET_NAME,
//                    PanoramicManager.FLOAT_WINDOW_SERVICE_CLASS_NAME);
        }

        if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                !FactoryDriver.Driver().getDvrEnable()) {
            ApkUtils.stopServiceSafety(getMainContext(), com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_NAME,
                    com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                    com.wwc2.camera_interface.CameraDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
        }
        LogUtils.d(TAG, "stop camera service over. mIllStatus="+mIllStatus+", ill="+EventInputManager.getIll());

        //关闭360
        PanoramicManager.getInstance().sendCMDToPanoramic(PanoramicManager.CMD_CLOSE, false);

        //修改bug13226屏幕设置黑夜模式，倒车，关ACC浅睡再上ACC，取消倒车，黑夜模式无记忆。2018-10-09
        if (mBacklightMode == BaseBacklightDriver.NIGHT) {
            BacklightDriver.Driver().setBacklightNight();
        }
        if (mIllStatus/* || EventInputManager.getIll()*/) {//倒车，接大灯，退出倒车，背光无变化
            mIllStatus = false;
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_ILL, EventInputManager.getIll(), null);
        } else {
            if (mBacklightMode == BaseBacklightDriver.NIGHT) {
                BacklightDriver.Driver().setBacklightNight();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        switch (nId) {
            case MessageDefine.APK_TO_MAIN_ID_CREATE:
                LogUtils.d(TAG, "Camera APK_TO_MAIN_ID_CREATE.");
                break;
            case MessageDefine.APK_TO_MAIN_ID_DESTROY:
                LogUtils.d(TAG, "Camera APK_TO_MAIN_ID_DESTROY.");
                break;
            case CameraInterface.APK_TO_MAIN.TOUCH_RECT:
                if (null != packet) {
                    int action = packet.getInt("TOUCH_STATUS", 0);
                    int x = packet.getInt("TOUCH_X", 0);
                    int y = packet.getInt("TOUCH_Y", 0);
                    LogUtils.d(TAG, "TOUCH-----x=" + x + ", y=" + y);
//                    PanoramicManager.getInstance().sendTouchXY(getMainContext(), x, y);
                    PanoramicManager.getInstance().sendTouchXY(action, x, y);
                }
                break;
            case CameraInterface.APK_TO_MAIN.VIDEO_TYPE:
                if (null != packet) {
                    int type = packet.getInt("VIDEO_TYPE");
                    LogUtils.d(TAG, "VIDEO_TYPE-----=" + type);
                    FactoryDriver.Driver().setPanoramicVideoType(type);
                }
                break;
            default:
                break;
        }
        return ret;
    }

    private void exitFontCamera(){
        LogUtils.e("Delay Time is arrived, to before camera menu!");
        mHandler.removeMessages(1);
        Packet packet2 = new Packet();
        packet2.putBoolean("frontCamera", false);
        ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.FRONT_CAMERA, packet2);

        BaseLogic logic = ModuleManager.getLogicBySource(mEnterCameraSource);
        LogUtils.e("Delay Time is arrived, mEnterCameraSource=" + mEnterCameraSource + ", issource=" + logic.isSource());
        if (logic != null && logic.isSource()) {
            if (mEnterCameraSource == Define.Source.SOURCE_VIDEO) {
                mHandler.sendEmptyMessageDelayed(2, 800);
            } else {
                SourceManager.onRemoveChangeSource(mEnterCameraSource, source());
            }
        } else {
            SourceManager.onRemoveCompareSource(mEnterCameraSource, source());
            SourceManager.onOpenBackgroundSource();
        }

    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    exitFontCamera();
                    break;
                case 2:
                    LogUtils.d(TAG, "handleMessage-----curSource=" + SourceManager.getCurSource() +
                            ", backSource=" + SourceManager.getCurBackSource());
                    if (SourceManager.getCurSource() != Define.Source.SOURCE_VIDEO &&
                            SourceManager.getCurBackSource() == Define.Source.SOURCE_VIDEO) {
                        SourceManager.onRemoveChangeSource(mEnterCameraSource, source());
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onStatusEvent(int type, boolean status, Packet packet) {
        boolean ret = true;
        switch (type) {
            case EventInputDefine.Status.TYPE_CAMERA:
                if (!status) {
                    Intent intent = new Intent(CAMERA);
                    intent.putExtra("camera", false);
                    getMainContext().sendBroadcast(intent);

                    mCameraLock.lock();
                    try {
                        boolean result = false;
                        boolean poweroff = false;
                        boolean available = false;
                        BaseLogic logic = ModuleManager.getLogicBySource(mEnterCameraSource);
//                        if (null != logic) {
//                            poweroff = logic.isPoweroffSource();
//                            available = logic.available();
//                        }
//                        if (poweroff || !available) {
//                            // 开关倒车，要开机
//                            result = SourceManager.onPopAvailableSource();
////                            if (result) {
////                                SourceManager.onOpenBackgroundSource();
////                            }
//                        }
                        LogUtils.d(TAG, "Exit camera, mEnterCameraSource = " + Define.Source.toString(mEnterCameraSource) +
                                ", poweroff = " + poweroff + ", available = " + available);
                        if (logic != null) {
                            LogUtils.d(TAG, "Exit camera, isSource() = " + logic.isSource());
                        }
                        if (!result) {
                            int time = FactoryDriver.Driver().getFrontCameraTime();
                            Packet auxPacket = LogicManager.getLogicByName(AuxDefine.MODULE).getInfo();
                            if (FactoryDriver.Driver().getSupportFrontCamera() &&
                                    auxPacket != null && !auxPacket.getBoolean("frontCamera") &&
                                    time > 0) {//飞音的才有前视功能。
                                LogUtils.e("auxPacket---" + (auxPacket == null ? "null" : auxPacket.getBoolean("frontCamera")));
                                Packet packet2 = new Packet();
                                packet2.putBoolean("frontCamera", true);
                                ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.FRONT_CAMERA, packet2);
                                mHandler.sendEmptyMessageDelayed(1, time * 1000);
                            } else {
                                if (logic != null && logic.isSource()) {
                                    SourceManager.onRemoveChangeSource(mEnterCameraSource, source());
                                } else {
                                    SourceManager.onRemoveCompareSource(mEnterCameraSource, source());
                                    SourceManager.onOpenBackgroundSource();
                                }
                            }
                        }
                    } finally {
                        mCameraLock.unlock();
                    }
                }else{ //zhongyang.hu add for when in front camera,cannot entry into Back Camera  20200102
                    if (FactoryDriver.Driver().getUiStyle() == 97 || //飞音的才有前视功能。
                            FactoryDriver.Driver().getSupportFrontCamera()) {
                        exitFontCamera();
                        ret =false;
                    } //end
                }
                break;
            case EventInputDefine.Status.TYPE_ILL:
//                if (!mIllStatus && !status) {

//                } else {
                    mIllStatus = !mIllStatus;
//                }
                break;
            case EventInputDefine.Status.TYPE_ACC:
                ret = false;
                LogUtils.d("EventInputDefine.Status.TYPE_ACC----" + status);
                if (!status && mHandler.hasMessages(1)) {
                    LogUtils.d("EventInputDefine.Status.TYPE_ACC--11--" + status);
                    mHandler.removeMessages(1);
                    Packet packet2 = new Packet();
                    packet2.putBoolean("frontCamera", false);
                    ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.FRONT_CAMERA, packet2);
                }
                break;
            case EventInputDefine.Status.TYPE_BRAKE:
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;
        switch (key) {
            case Define.Key.KEY_POWER:
                // 倒车优先，POWER按键要无作用
                //SourceManager.onChangeSource(Define.Source.SOURCE_POWEROFF);
                break;
            case Define.Key.KEY_RADAROFF:
                if (EventInputManager.getCamera() == false) {
                    EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_CAMERA, false, null);
                }
                break;
            default:
                break;
        }
        return ret;
    }

    public static void setEnterCameraSource(int source, boolean force) {
        LogUtils.d(TAG, "setEnterCameraSource----source="+source+", force="+force);
        if (force) {
            if (mEnterCameraSource == Define.Source.SOURCE_THIRDPARTY) {
                //在视频播放时，语音进入酷我音乐，马上进倒车，有时酷我会被杀掉，但进入倒车前的源是第三方源，导致在
                //退出倒车仍会切到第三方源，而界面停留在视频，视频无法播放。2018-03-21
                BaseLogic logic = ModuleManager.getLogicBySource(source);
                if (logic != null) {
                    if (logic.isSource() && !logic.isPoweroffSource()) {
                        mEnterCameraSource = source;
                        return;
                    }
                }
            } else {
                //在主界面点击源按钮，马上进倒车，退出倒车会出现在点击的源界面，而声音还是之前的源。2018-04-02
                BaseLogic logic = ModuleManager.getLogicBySource(source);
                if (logic != null) {
                    if (logic.isSource()) {
                        mEnterCameraSource = source;
                        /*-begin-20180424-ydinggen-modify-上ACC，在视频快速倒车时，会出现两个声音-*/
                        if (source == Define.Source.SOURCE_VIDEO) {
                            final String pkgName = logic.getAPKPacketName();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtils.d(TAG, "killvideo apk 4");
                                    STM32MCUDriver.killProcess(mContext, pkgName);
                                }
                            }).start();
                        } else {
                            LogUtils.d(TAG, "killvideo apk source="+source);
                        }
                        /*-end-20180424-ydinggen-modify-上ACC，在视频快速倒车时，会出现两个声音-*/
                        /*-begin-20180424-ydinggen-modify-在后台播音乐，打倒车，马上点视频，退倒车后在视频界面播放音乐-*/
                        SourceManager.onOpenBackgroundSource(source);
                        /*-end-20180424-ydinggen-modify-在后台播音乐，打倒车，马上点视频，退倒车后在视频界面播放音乐-*/
                    }
                }
            }
        } else {
            mEnterCameraSource = source;
        }
    }
}
