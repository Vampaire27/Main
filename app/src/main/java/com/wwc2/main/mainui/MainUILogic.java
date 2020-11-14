package com.wwc2.main.mainui;

import android.net.Uri;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.message.MessageDefine;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.language.LanguageDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.driver.volume.VolumeListener;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.eventinput.EventInputListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.VolumeManager;
import com.wwc2.main.provider.LogicProvider;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.mainui_interface.MainUIInterface;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

/**
 * the main UI.
 *
 * @author wwc2
 * @date 2017/1/15
 */
public class MainUILogic extends BaseLogic {

    /**
     * TAG
     */
    private static final String TAG = "MainUILogic";

    /**
     * 声音Model
     */
    private BaseModel mVolumeModel = null;

    /**
     * 声音值发生变化
     */
    private int mVolumeValueTimerId = 0;

    /**
     * logo定时器ID
     **/
    private int logoTimerID;

    /**
     * 声音监听器
     */
    private VolumeListener mVolumeListener = new VolumeListener() {
        @Override
        public void VolumeTypeListener(Integer oldVal, Integer newVal) {
            Packet packet = mVolumeModel.getInfo();
            if (null != packet) {
                packet.putInt("VolumeValue", VolumeManager.getValue());
            }
            Notify(MainUIInterface.MAIN_TO_APK.VOLUME_TYPE, packet);
        }

        @Override
        public void VolumeValueListener(Integer[] oldVal, Integer[] newVal) {
            Packet packet = mVolumeModel.getInfo();
            if (null != packet) {
                packet.putInt("VolumeValue", VolumeManager.getValue());
            }
            Notify(MainUIInterface.MAIN_TO_APK.VOLUME_VALUE, packet);

            Uri uri_vol = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.VOLUME_VALUE);
            getMainContext().getContentResolver().notifyChange(uri_vol, null);
        }

        @Override
        public void VolumeMuteListener(Boolean oldVal, Boolean newVal) {
            Packet packet = mVolumeModel.getInfo();
            if (null != packet) {
                packet.putInt("VolumeValue", VolumeManager.getValue());
            }
            Notify(MainUIInterface.MAIN_TO_APK.VOLUME_MUTE, packet);
        }

        @Override
        public void VolumeMaxListener(Integer oldVal, Integer newVal) {
            Packet packet = mVolumeModel.getInfo();
            if (null != packet) {
                packet.putInt("VolumeValue", VolumeManager.getValue());
            }
            Notify(MainUIInterface.MAIN_TO_APK.VOLUME_MAX, packet);
        }
    };

    /**
     * 事件输入监听器
     */
    private EventInputListener mEventInputListener = new EventInputListener() {

        @Override
        public void CameraListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("camera", newVal);
            Notify(false, MainUIInterface.MAIN_TO_APK.CAMERA_STATUS, packet);
        }
    };

    /**
     * mcu串口状态监听器
     */
//
//    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {
//        @Override
//        public void OpenListener(int status) {
//            if (isInlogo) {
////                VolumeDriver.Driver().mute(true);
//                McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.OP_LOGO_STATE, new byte[]{0x01}, 1);
//                LogUtils.i(TAG, "logo mute");
//            } else {
//                McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.OP_LOGO_STATE, new byte[]{0x00}, 1);
//            }
//        }
//    };

    @Override
    public String getTypeName() {
        return "MainUI";
    }

    @Override
    public String getMessageType() {
        return com.wwc2.mainui_interface.MainUIDefine.MODULE;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        ApkUtils.stopServiceSafety(getMainContext(), com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_NAME,
                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
        ApkUtils.startServiceSafety(getMainContext(), com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_NAME,
                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);

        mVolumeModel = DriverManager.getDriverByName(VolumeDriver.DRIVER_NAME).getModel();
        if (null != mVolumeModel) {
            mVolumeModel.bindListener(mVolumeListener);
        }
        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().bindListener(mEventInputListener);
//        McuManager.getModel().bindListener(mMcuListener);
    }

    @Override
    public void onDestroy() {
        if (null != mVolumeModel) {
            mVolumeModel.unbindListener(mVolumeListener);
        }

        ApkUtils.stopServiceSafety(getMainContext(), com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_NAME,
                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);

        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        switch (nId) {
            case MessageDefine.APK_TO_MAIN_ID_CREATE:
                LogUtils.e(TAG, "dispatch----APK_TO_MAIN_ID_CREATE");
                String lang = LanguageDriver.Driver().get();
                if (null == lang) {
                    lang = Define.Language.Default.name();
                }
                CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
                if (logic != null) {
                    Packet pack = new Packet();
                    pack.putString(com.wwc2.systempermission_interface.SystemPermissionDefine.ParseKey.LOCALE, lang);
                    logic.Notify(SystemPermissionInterface.MAIN_TO_APK.LOCALE, pack);
                }
                break;
            case MainUIInterface.APK_TO_MAIN.SET_VOLUME:
                if (null != packet) {
                    int value = packet.getInt("value", -1);
                    if (-1 != value) {
                        VolumeDriver.Driver().set(value);
                    }
                }
                break;
            case MainUIInterface.APK_TO_MAIN.SET_MUTE:
                if (null != packet) {
                    boolean mute = packet.getBoolean("mute", false);
                    VolumeDriver.Driver().mute(mute);
                }
                break;
            case MainUIInterface.APK_TO_MAIN.INCREASE_VOLUME:
                if (null != packet) {
                    int value = packet.getInt("value", -1);
                    if (-1 != value) {
                        VolumeDriver.Driver().increase(value);
                    }
                }
                break;
            case MainUIInterface.APK_TO_MAIN.DECREASE_VOLUME:
                if (null != packet) {
                    int value = packet.getInt("value", -1);
                    if (-1 != value) {
                        VolumeDriver.Driver().decrease(value);
                    }
                }
                break;
            case MainUIInterface.APK_TO_MAIN.UI_SHOW:
                if (null != packet) {
                    boolean show = packet.getBoolean("show");
                    VolumeDriver.Driver().adjustShow(show);
                    final int type = packet.getInt("type");
                    if (type == MainUIDefine.FloatType.FLOAT_TYPE_LOGO && !show) {
                        LogUtils.d(TAG, "FLOAT_TYPE_LOGO");
                        McuManager.setLogoStatus(getMainContext(), false);
                    }
                }
                break;
            case MainUIInterface.APK_TO_MAIN.BACKLIGHT_CLICK:
                if (null != packet) {
                    int state = packet.getInt("operate", -1);
                    if (state != -1) {
                        ApkUtils.startServiceSafety(getMainContext(), com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_NAME,
                                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
                        Notify(MainUIInterface.MAIN_TO_APK.BACKLIGHT_SWITCH, packet);
                        return ret;
                    }
                }
                BacklightDriver.Driver().open();
                break;
            case MainUIInterface.APK_TO_MAIN.OPERATE_VOLUME:
                if (null != packet) {
                    int operate = packet.getInt("operate", -1);
                    if (-1 != operate) {
                        ApkUtils.startServiceSafety(getMainContext(), com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_NAME,
                                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
                                com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
                        switch (operate) {
                            case Define.VolumeOperate.SHOW:
                            case Define.VolumeOperate.HIDE:
                                if (null != mVolumeModel) {
                                    packet = mVolumeModel.getInfo();
                                    final int value = VolumeManager.getValue();
                                    final int type = com.wwc2.mainui_interface.MainUIDefine.FloatType.FLOAT_TYPE_VOLUME;
                                    if (null != packet) {
                                        packet.putInt("VolumeValue", VolumeManager.getValue());
                                        packet.putInt("type", com.wwc2.mainui_interface.MainUIDefine.FloatType.FLOAT_TYPE_VOLUME);
                                        packet.putInt("operate", operate);
                                    }
                                    LogUtils.d(TAG, "type = " + com.wwc2.mainui_interface.MainUIDefine.FloatType.toString(type) +
                                            ", operate = " + Define.VolumeOperate.toString(operate) +
                                            ", value = " + value +
                                            ", service running = " + ApkUtils.isServiceRunning(getMainContext(),
                                            com.wwc2.mainui_interface.MainUIDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME));
                                    Notify(MainUIInterface.MAIN_TO_APK.OPERATE_VOLUME, packet);
                                } else {
                                    LogUtils.e(TAG, "volume model is null?");
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return ret;
    }

    public void showLogoWhileLeaveDeepSleep() {

        //记住初始静音状态
        final boolean muteStatus = VolumeManager.getMute();
        //通知显示ｌｏｇｏ
        Notify(false, MainUIInterface.MAIN_TO_APK.LOGO_SWITCH, null);
        McuManager.setLogoStatus(getMainContext(), true);

        //3秒后ｌｏｇｏ退出，恢复声音
//        logoTimerID = TimerUtils.setTimer(getMainContext(), 3000, 3000, new Timerable.TimerListener() {
//            @Override
//            public void onTimer(int paramInt) {
//                isInlogo = false;
////                if (!muteStatus) {//ACC ON后应该是要解MUTE，而浅睡眠是由MCU解。所以AP不需要记忆MUTE的状态。2017-09-27
//                    VolumeDriver.Driver().mute(false);
//                    LogUtils.d(TAG, "logo unmute");
////                }
//                TimerUtils.killTimer(logoTimerID);
//            }
//        });

        logoTimerID = TimerUtils.setTimer(getMainContext(), 5000, 5000, new Timerable.TimerListener() {
            @Override
            public void onTimer(int paramInt) {
                if (McuManager.getLogoStatus()) {
                    LogUtils.e(TAG, "logo unmute 5s");
                    McuManager.setLogoStatus(getMainContext(), false);
                }
                TimerUtils.killTimer(logoTimerID);
            }
        });
    }



}
