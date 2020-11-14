package com.wwc2.main.phonelink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.bluetooth_interface.BluetoothInterface;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.message.MessageDefine;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

/**
 * the phone link logic.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public class PhonelinkLogic extends BaseLogic {

    private final static String TAG = PhonelinkLogic.class.getSimpleName();

    private static final String AUDIO_FOCUS_PACKAGENAME = "com.wwc2.framework.action.audiofocus";

    /**
     * 亿连
     */
    public static final String EASY_CONN_PKGNAME = "net.easyconn";
    //接收
    public static final String ACTION_EC_A2DP_ACQUIRE      = "net.easyconn.a2dp.acquire";//获取蓝牙声道(进入双屏互动时)
    public static final String ACTION_EC_A2DP_RELEASE      = "net.easyconn.a2dp.release";//释放蓝牙声道(退出双屏互动时)
    public static final String ACTION_EC_OPEN              = "net.easyconn.app.open";//启动亿连
    public static final String ACTION_EC_PAUSE             = "net.easyconn.app.pause";//亿连切换到后台
    public static final String ACTION_EC_RESUME            = "net.easyconn.app.resume";//亿连切换到前台(Deprecated)
    public static final String ACTION_EC_QUIT              = "net.easyconn.app.quit";//退出亿连
    public static final String ACTION_EC_REQUEST_BT        = "net.easyconn.bt.checkstatus"; //检查蓝牙状态
    public static final String ACTION_EC_SCREEN_RESUME     = "net.easyconn.screen.resume"; //安卓投屏启动、切前台
    public static final String ACTION_EC_SCREEN_PAUSE      = "net.easyconn.screen.pause"; //安卓投屏退出、切后台
    public static final String ACTION_EC_MIRROR_IN         = "net.easyconn.android.mirror.in"; //安卓连接进入投屏
    public static final String ACTION_EC_MIRROR_OUT        = "net.easyconn.android.mirror.out"; //安卓连接退出投屏
    public static final String ACTION_EC_IOS_MIRROR_IN     = "net.easyconn.iphone.mirror.in"; //IOS 连接进入投屏
    public static final String ACTION_EC_IOS_MIRROR_OUT    = "net.easyconn.iphone.mirror.out"; //IOS 连接退出投屏
    public static final String ACTION_EC_BT_CONNECT        = "net.easyconn.bt.connect";//(预留,暂时忽略) EC请求车机启动匹配过程
    //发送
    public static final String ACTION_BT_OPEN              = "net.easyconn.bt.opened"; //蓝牙开启 接收
                                                            //"net.easyconn.bt.opened @name=xxx @pin=1234"
    public static final String ACTION_BT_CONNECTED         = "net.easyconn.bt.connected"; //蓝牙连接成功
    public static final String ACTION_BT_DISCONNECTED      = "net.easyconn.bt.notconnected"; //蓝牙断开
    public static final String ACTION_A2DP_ACQUIRE_OK      = "net.easyconn.a2dp.acquire.ok";//A2DP获取成功
    public static final String ACTION_A2DP_ACQUIRE_FAIL    = "net.easyconn.a2dp.acquire.fail";//A2DP获取失败
    public static final String ACTION_A2DP_RELEASE_OK      = "net.easyconn.a2dp.release.ok";//A2DP释放成功
    public static final String ACTION_A2DP_RELEASE_FAIL    = "net.easyconn.a2dp.release.fail";//A2DP释放失败
    public static final String ACTION_EXIT_AIRPLAY         = "net.easyconn.action.EXIT_AIRPLAY"; //外部停止airplay解码

    /**
     * 钛马星
     */
    public static final String ACTION_TIMA_A2DP_ACQUIRE     = "com.carnet.vt.a2dp.start";//申请蓝牙声道
    public static final String ACTION_TIMA_A2DP_RELEASE     = "com.carnet.vt.a2dp.stop";//释放蓝牙声道

    /**
     * CarPlay
     */
    public static final String ACTION_CARPLAY_CONNECT       = "com.zjinnova.zlink";
    public static final String CARPLAY_STATUS               = "status";//CONNECTED, DISCONNECT
    public static final String CARPLAY_TYPE                 = "phoneType";//ios_carplay ios_carplay/android_auto_release
    public static final String CARPLAY_MODE                 = "phoneMode";//carplay_wired

    private boolean mBTDisconnect = true;
    private String mLocalName = "";
    private String mPinCode = "";
    private static boolean mEasyConn = false;

    private boolean enableBefore = false;
    private boolean btSwitchBefore = false;

    private static boolean mCarplayPhone = false;

    @Override
    public String getTypeName() {
        return "Phonelink";
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_PHONELINK;
    }

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public String getAPKPacketName() {
        return "net.easyconn";//"com.wwc2.phonelink";
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        LogUtils.d(TAG, "PhonelinkLogic onCreate----");

//        Packet mPacket = new Packet(Notify(true, BluetoothDefine.MODULE, MessageDefine.APK_TO_MAIN_ID_GET_INFO, null));
//        EasyConnHelper.getEasyConnHelper().onCreate(getMainContext(), mPacket);
        initBroadcast();

        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        LogUtils.d(TAG, "onResume----");
        // 申请音频
//        AudioDriver.Driver().request(null,
//                AudioDefine.AudioStream.STREAM_MUSIC,
//                AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
    }

    @Override
    public void onStop() {
        super.onStop();

//        sendBroadcastToEasyConn("net.easyconn.action.EXIT_AIRPLAY");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);
        try {
            getMainContext().unregisterReceiver(mEasyConnReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        EasyConnHelper.getEasyConnHelper().onDestroy();
    }

    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            if (mBTDisconnect != BluetoothDefine.HFPStatus.isDisConnect(newVal)) {
                mBTDisconnect = BluetoothDefine.HFPStatus.isDisConnect(newVal);
                if (mBTDisconnect) {
                    sendBroadcastToEasyConn(ACTION_BT_DISCONNECTED);
                } else {
                    sendBroadcastToEasyConn(ACTION_BT_CONNECTED);
                }
            }
        }

        @Override
        public void LocalNameListener(String oldVal, String newVal) {
            mLocalName = newVal;
        }

        @Override
        public void LocalMacAddrListener(String oldVal, String newVal) {
        }

        @Override
        public void PinCodeListener(String oldVal, String newVal) {
            mPinCode = newVal;
        }
    };

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        //亿连
//        filter.addAction(ACTION_EC_A2DP_ACQUIRE);
//        filter.addAction(ACTION_EC_A2DP_RELEASE);
        filter.addAction(ACTION_EC_OPEN);
        filter.addAction(ACTION_EC_PAUSE);
        filter.addAction(ACTION_EC_RESUME);
        filter.addAction(ACTION_EC_QUIT);
        filter.addAction(ACTION_EC_REQUEST_BT);
        filter.addAction(ACTION_EC_SCREEN_RESUME);
        filter.addAction(ACTION_EC_SCREEN_PAUSE);
        filter.addAction(ACTION_EC_MIRROR_IN);
        filter.addAction(ACTION_EC_MIRROR_OUT);
        filter.addAction(ACTION_EC_IOS_MIRROR_IN);
        filter.addAction(ACTION_EC_IOS_MIRROR_OUT);
        filter.addAction(ACTION_EC_BT_CONNECT);
        //钛马星
        filter.addAction(ACTION_TIMA_A2DP_ACQUIRE);
        filter.addAction(ACTION_TIMA_A2DP_RELEASE);
        //CarPlay
        filter.addAction(ACTION_CARPLAY_CONNECT);
        getMainContext().registerReceiver(mEasyConnReceiver, filter);
    }

    private BroadcastReceiver mEasyConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.i(TAG, "EasyConnReceiver---action="+action);
            /*if (action.equals(ACTION_EC_A2DP_ACQUIRE) *//*|| action.equals(ACTION_TIMA_A2DP_ACQUIRE)*//*) {
                AudioDriver.Driver().request(null,
                        AudioDefine.AudioStream.STREAM_MUSIC,
                        AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
                requestAudioFocus(true);
            } else if (action.equals(ACTION_EC_A2DP_RELEASE) *//*|| action.equals(ACTION_TIMA_A2DP_RELEASE)*//*) {
//                AudioDriver.Driver().abandon();
                requestAudioFocus(false);
            } else */if (action.equals(ACTION_EC_OPEN)) {
            } else if (action.equals(ACTION_EC_PAUSE)) {
            } else if (action.equals(ACTION_EC_RESUME)) {
            } else if (action.equals(ACTION_EC_QUIT)) {
                SourceManager.onExitPackage(EASY_CONN_PKGNAME);
            } else if (action.equals(ACTION_EC_REQUEST_BT)) {
                Packet mPacket = new Packet(Notify(true, BluetoothDefine.MODULE, MessageDefine.APK_TO_MAIN_ID_GET_INFO, null));
                mLocalName = mPacket.getString("LocalName");
                mPinCode = mPacket.getString("PinCode");
                if (mBTDisconnect) {
                    String strAction = ACTION_BT_OPEN;
                    if (mLocalName != null && mPinCode != null) {
                        strAction = ACTION_BT_OPEN + "[@name=" + mLocalName + "][" + "@PIN=" + mPinCode + "]";
                    } else {
                        LogUtils.e(TAG, "LocalName == null");
                    }
                    sendBroadcastToEasyConn(strAction);
                } else {
                    sendBroadcastToEasyConn(ACTION_BT_CONNECTED);
                }
            } else if (action.equals(ACTION_EC_SCREEN_RESUME)) {
            } else if (action.equals(ACTION_EC_SCREEN_PAUSE)) {
            } else if (action.equals(ACTION_EC_MIRROR_IN)) {
            } else if (action.equals(ACTION_EC_MIRROR_OUT)) {
            } else if (action.equals(ACTION_EC_IOS_MIRROR_IN)) {
            } else if (action.equals(ACTION_EC_IOS_MIRROR_OUT)) {
            } else if (action.equals(ACTION_EC_BT_CONNECT)) {
            } else if (action.equals(ACTION_CARPLAY_CONNECT)) {
                String status = intent.getStringExtra(CARPLAY_STATUS);
                if (!TextUtils.isEmpty(status)) {
//                    ToastUtil.show(getMainContext(), "CarPlay:" + status);
                    LogUtils.e(TAG, "ACTION_CARPLAY_CONNECT----status=" + status);
                    if (status.equals("CONNECTED")) {
                        enableBefore = false;
                        mCarplayPhone = false;
                        Packet voice = ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).getModel().getInfo();
                        if (null != voice) {
                            boolean enableWakeup = voice.getBoolean("EnableWakeup");
                            if (enableWakeup) {
                                enableBefore = true;
                                Packet packet = new Packet();
                                packet.putBoolean("isEnableWakeup", false);
                                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.ENABLE_WAKEUP, packet);
                            }
                        }

                        BaseLogic logic = ModuleManager.getLogicByName(BluetoothDefine.MODULE);
                        if (null != logic) {
                            Packet data = logic.getInfo();
                            if (null != data) {
                                boolean open = data.getBoolean("OpenOrClose");
                                LogUtils.e(TAG, "ACTION_CARPLAY_CONNECT----OpenOrClose=" + open);
                                if (open) {
                                    btSwitchBefore = true;

                                    Packet packet = new Packet();
                                    packet.putBoolean("carplay", true);
                                    logic.dispatch(BluetoothInterface.APK_TO_MAIN.BT_CLOSE, packet);
                                }
                            }
                        }
                    } else if (status.equals("DISCONNECT")) {
                        if (enableBefore) {
                            Packet packet = new Packet();
                            packet.putBoolean("isEnableWakeup", true);
                            ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.ENABLE_WAKEUP, packet);
                        }
                        enableBefore = false;
                        mCarplayPhone = false;
                        AudioDriver.Driver().setBlueCallActive(mCarplayPhone);

                        if (btSwitchBefore) {
                            ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.BT_OPEN, null);
                            btSwitchBefore = false;
                        }
                    } else if (status.equals("PHONE_CALL_ON")) {//电话
                        mCarplayPhone = true;
                        AudioDriver.Driver().setBlueCallActive(mCarplayPhone);

                        LogUtils.e(TAG, "ACTION_CARPLAY_CONNECT----PHONE_CALL_ON=" + status);
                        BaseLogic logic = ModuleManager.getLogicByName(BluetoothDefine.MODULE);
                        if (null != logic) {
                            Packet data = logic.getInfo();
                            if (null != data) {
                                boolean open = data.getBoolean("OpenOrClose");
                                LogUtils.e(TAG, "ACTION_CARPLAY_CONNECT----OpenOrClose=" + open);
                                if (open) {
                                    btSwitchBefore = true;

                                    Packet packet = new Packet();
                                    packet.putBoolean("carplay", true);
                                    logic.dispatch(BluetoothInterface.APK_TO_MAIN.BT_CLOSE, packet);
                                }
                            }
                        }
                    } else if (status.equals("PHONE_CALL_OFF")) {//挂断
                        mCarplayPhone = false;
                        AudioDriver.Driver().setBlueCallActive(mCarplayPhone);

//                        if (btSwitchBefore) {
//                            ModuleManager.getLogicByName(BluetoothDefine.MODULE).dispatch(BluetoothInterface.APK_TO_MAIN.BT_OPEN, null);
//                            btSwitchBefore = false;
//                        }
                    }
                } else {
                    LogUtils.e(TAG, "ACTION_CARPLAY_CONNECT----null!");
                }
            }
        }
    };

    private void sendBroadcastToEasyConn(String action) {
        LogUtils.i(TAG, "sendBroadcastToEasyConn---action="+action);
        Intent intentConnect = new Intent();
        intentConnect.setAction(action);
        getMainContext().sendBroadcast(intentConnect);
    }

    public static void requestAudioFocus(boolean request) {
        mEasyConn = request;
//        Intent intent = new Intent(AUDIO_FOCUS_PACKAGENAME);
//        intent.putExtra("CODE", 401);
//        intent.putExtra("PKGNAME", EASY_CONN_PKGNAME);
//        intent.putExtra("streamType", AudioManager.STREAM_MUSIC);
//        intent.putExtra("durationHint", request ? AudioManager.AUDIOFOCUS_GAIN : AudioManager.AUDIOFOCUS_LOSS);
//        intent.putExtra("DATA", request ? 1 : 0);
//        getMainContext().sendBroadcast(intent);
    }

    public static boolean getEasyConnA2dp() {
        return mEasyConn;
    }

    public static boolean getCarplayPhone() {
        LogUtils.e(TAG, "ACTION_CARPLAY_CONNECT----mCarplayPhone=" + mCarplayPhone);
        return mCarplayPhone;
    }
}
