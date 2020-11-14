package com.wwc2.main.canbus;

import android.net.Uri;
import android.os.Bundle;

import com.wwc2.audio_interface.AudioDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.canbus_interface.CarSettingsDefine;
import com.wwc2.canbus_interface.Provider;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.SwitchStatus;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.canbus.driver.CanBusDriver;
import com.wwc2.main.canbus.driver.CanBusDriverable;
import com.wwc2.main.driver.language.LanguageDriver;
import com.wwc2.main.driver.language.LanguageListener;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.driver.volume.VolumeListener;
import com.wwc2.main.eventinput.EventInputListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.manager.VolumeManager;
import com.wwc2.main.media.MediaListener;
import com.wwc2.main.provider.LogicProvider;
import com.wwc2.main.provider.LogicProviderHelper;
import com.wwc2.main.radio.RadioListener;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.video_interface.VideoDefine;

import java.util.Locale;

/**
 * the can bus logic.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public class CanBusLogic extends BaseLogic {

    /**
     * TAG
     */
    private static final String TAG = "CanBusLogic";

    private int radioBand       = 0;
    private int radioFreq       = 8750;
    private int radioSaveOn     = 0;
    private int mediaCurTime    = 0;
    private int mediaCurIndex   = 0;
    private int mediaTotal      = 0;
    private int mediaCurTime_video    = 0;
    private int mediaCurIndex_video   = 0;
    private int mediaTotal_video      = 0;
    private int volume          = 0;
    private boolean muteState   = false;

    /**
     * ContentProvider.
     */
    /*-begin-20180426-ydinggen-add-修改Can信息获取方式，不写数据库，防止数据库异常问题-*/
//    static {
//        LogicProviderHelper.Provider(Provider.CAN_ENABLE(), "" + true);
//        LogicProviderHelper.Provider(Provider.CAN_SWITCH(), "" + false);
//        LogicProviderHelper.Provider(Provider.CAN_SWITCH_STATUS(), "" + SwitchStatus.DEFAULT);
//        LogicProviderHelper.Provider(Provider.CAN_SERIES(), "" + CanBusDefine.CanSeries.RuiZhiCheng.VOLKSWAGEN_GENERAL);
//        LogicProviderHelper.Provider(Provider.IMPLEMENAATION(), "" + CanBusDefine.Implementation.DEFAULT);
//        LogicProviderHelper.Provider(Provider.SUPPORT_FUNCTION(), "" + getSupportFunctionString(CanBusDefine.Function.DEFAULT_SUPPORT));
//        LogicProviderHelper.Provider(Provider.CANBUS_VERSION(), "" + "0.0");
//        LogicProviderHelper.Provider(Provider.CANBUS_CONNECTED(), "" + false);
//        LogicProviderHelper.Provider(Provider.SERIAL_PORT(), "" + "/dev/ttyMT2");
//    }
    /*-begin-20180426-ydinggen-add-修改Can信息获取方式，不写数据库，防止数据库异常问题-*/

    /**the can bus listener.*/
    private CanBusListener mCanBusListener = new CanBusListener() {
        @Override
        public void CanEnableListener(Boolean oldVal, Boolean newVal) {
            LogUtils.i(TAG, "CanEnableListener---newVal="+newVal);
//            LogicProviderHelper.getInstance().update(Provider.CAN_ENABLE(), "" + newVal);
        }

        @Override
        public void CanSwitchListener(Boolean oldVal, Boolean newVal) {
            LogUtils.i(TAG, "CanSwitchListener---newVal="+newVal);
//            LogicProviderHelper.getInstance().update(Provider.CAN_SWITCH(), "" + newVal);

            Packet packet = new Packet();
            packet.putBoolean("CanSwitch", newVal);
            Notify(CanBusDefine.MainDefine.CANSWITCH, packet);
        }

        @Override
        public void CanSwitchStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.i(TAG, "CanSwitchStatusListener---newVal="+newVal);
//            LogicProviderHelper.getInstance().update(Provider.CAN_SWITCH_STATUS(), "" + newVal);
            Uri uri_acc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.CUR_CAN_SWITCH);
            getMainContext().getContentResolver().notifyChange(uri_acc, null);

            Packet packet = new Packet();
            packet.putInt("CanSwitchStatus", newVal);
            Notify(CanBusDefine.MainDefine.CANBUS_SWITCH_STATUS, packet);
        }

        @Override
        public void CanCompanyListener(String oldVal, String newVal) {
//            LogicProviderHelper.getInstance().update(Provider.CAN_COMPANY(), "" + newVal);

            Packet packet = new Packet();
            packet.putString("CanCompany", newVal);
            Notify(CanBusDefine.MainDefine.CANCOMPANY, packet);
        }

        @Override
        public void CarBrandListener(String oldVal, String newVal) {
//            LogicProviderHelper.getInstance().update(Provider.CAR_BRAND(), "" + newVal);

            Packet packet = new Packet();
            packet.putString("CarBrand", newVal);
            Notify(CanBusDefine.MainDefine.CARBRAND, packet);
        }

        @Override
        public void CarTypeListener(String oldVal, String newVal) {
//            LogicProviderHelper.getInstance().update(Provider.CAR_TYPE(), "" + newVal);

            Packet packet = new Packet();
            packet.putString("CarType", newVal);
            Notify(CanBusDefine.MainDefine.CARTYPE, packet);
        }

        @Override
        public void CanSeriesListener(Integer oldVal, Integer newVal) {
//            LogicProviderHelper.getInstance().update(Provider.CAN_SERIES(),""+newVal);
            LogUtils.i(TAG, "CanSeriesListener---"+CanBusDefine.CanSeries.toString(newVal));
            Uri uri_acc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.CUR_CANSERIES);
            getMainContext().getContentResolver().notifyChange(uri_acc, null);
        }

        @Override
        public void ImplementationListener(String oldVal, String newVal) {
//            LogicProviderHelper.getInstance().update(Provider.IMPLEMENAATION(), "" + newVal);

            Packet packet = new Packet();
            packet.putString("Implementation", newVal);
            Notify(CanBusDefine.MainDefine.IMPLEMENTATION, packet);
        }

        @Override
        public void CanBusVersionListener(String oldVal, String newVal) {
//            LogicProviderHelper.getInstance().update(Provider.CANBUS_VERSION(), "" + newVal);
            Uri uri_acc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.VERSION_CANBUS);
            getMainContext().getContentResolver().notifyChange(uri_acc, null);

            LogUtils.d(TAG,"CanBusVersion:" + newVal);
            Packet packet = new Packet();
            packet.putString("CanBusVersion", newVal);
            Notify(CanBusDefine.MainDefine.CANBUS_VERSION, packet);
        }

        @Override
        public void CanBusConnectedListener(Boolean oldVal, Boolean newVal) {
//            LogicProviderHelper.getInstance().update(Provider.CANBUS_CONNECTED(), "" + newVal);
            Uri uri_acc = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.CUR_CAN_CONNECT);
            getMainContext().getContentResolver().notifyChange(uri_acc, null);

            LogUtils.d(TAG,"CanBusConnected:" + newVal);
            Packet packet = new Packet();
            packet.putBoolean("CanBusConnected", newVal);
            Notify(CanBusDefine.MainDefine.CANBUS_CONNECT, packet);
        }

        @Override
        public void SupportFunctionListener(String[] oldVal, String[] newVal) {
//            LogicProviderHelper.getInstance().update(Provider.SUPPORT_FUNCTION(), "" + getSupportFunctionString(newVal));
            LogUtils.d(TAG,"SupportFunction:" + getSupportFunctionString(newVal));
        }

        @Override
        public void CanBusSerialPortListener(String oldVal, String newVal) {
//            LogicProviderHelper.getInstance().update(Provider.SERIAL_PORT(), "" + newVal);
        }
    };

    @Override
    public String getTypeName() {
        return "CanBus";
    }

    @Override
    public String getMessageType() {
        return CanBusDefine.MODULE;
    }

    @Override
    public BaseDriver newDriver() {
        LogUtils.d(TAG, "newDriver()");
        return new CanBusDriver();
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        LogUtils.d(TAG, "onCreate()");

        /*-begin-20180426-ydinggen-add-修改Can信息获取方式，不写数据库，防止数据库异常问题-*/
//        LogicProviderHelper.getInstance().update(Provider.CAN_ENABLE(), "" + getInfo().getBoolean("CanEnable"));
//        LogicProviderHelper.getInstance().update(Provider.CAN_SWITCH(), "" + getInfo().getBoolean("CanSwitch"));
//        LogicProviderHelper.getInstance().update(Provider.CAN_SWITCH_STATUS(), "" + getInfo().getInt("CanSwitchStatus"));
//        LogicProviderHelper.getInstance().update(Provider.CAN_SERIES(), "" + getInfo().getInt("CanSeries"));
//        LogicProviderHelper.getInstance().update(Provider.IMPLEMENAATION(), "" + getInfo().getString("Implementation"));
//        LogicProviderHelper.getInstance().update(Provider.SUPPORT_FUNCTION(), "" + getSupportFunctionString(getInfo().getStringArray("SupportFunction")));
//        LogicProviderHelper.getInstance().update(Provider.CANBUS_VERSION(), "" + getInfo().getString("CanBusVersion"));
//        LogicProviderHelper.getInstance().update(Provider.CANBUS_CONNECTED(), "" + getInfo().getBoolean("CanBusConnected"));
//        LogicProviderHelper.getInstance().update(Provider.SERIAL_PORT(), "" + getInfo().getString("SerialPort"));
        /*-begin-20180426-ydinggen-add-修改Can信息获取方式，不写数据库，防止数据库异常问题-*/

        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }

        getModel().bindListener(mCanBusListener);
//        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().bindListener(mEventInputListener);
        SourceManager.getModel().bindListener(mSourceListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);
        ModuleManager.getLogicByName(RadioDefine.MODULE).getModel().bindListener(mRadioListener);
        ModuleManager.getLogicByName(VideoDefine.MODULE).getModel().bindListener(mVideoListener);
        ModuleManager.getLogicByName(AudioDefine.MODULE).getModel().bindListener(mAudioListener);
        DriverManager.getDriverByName(VolumeDriver.DRIVER_NAME).getModel().bindListener(mVolumeListener);
        DriverManager.getDriverByName(LanguageDriver.DRIVER_NAME).getModel().bindListener(mLanguageListener);
        volume = VolumeManager.getValue();
        muteState = VolumeManager.getMute();
    }

    @Override
    public void onDestroy() {
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }
        getModel().unbindListener(mCanBusListener);
        DriverManager.getDriverByName(LanguageDriver.DRIVER_NAME).getModel().unbindListener(mLanguageListener);
//        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().unbindListener(mEventInputListener);
        SourceManager.getModel().unbindListener(mSourceListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);
        ModuleManager.getLogicByName(RadioDefine.MODULE).getModel().unbindListener(mRadioListener);
        ModuleManager.getLogicByName(VideoDefine.MODULE).getModel().unbindListener(mVideoListener);
        ModuleManager.getLogicByName(AudioDefine.MODULE).getModel().unbindListener(mAudioListener);
        DriverManager.getDriverByName(VolumeDriver.DRIVER_NAME).getModel().unbindListener(mVolumeListener);

        super.onDestroy();
    }

    /**driver*/
    protected CanBusDriverable Driver() {
//        LogUtils.d(TAG, "Driver()");
        CanBusDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof CanBusDriverable) {
            ret = (CanBusDriverable) driver;
        }
        if (ret == null) {
            ret = (CanBusDriverable) newDriver();
        }
        LogUtils.d(TAG, "Driver()----ret=" + ret);
        return ret;
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
//        LogUtils.d(TAG, "dispatch()");
        Packet ret = null;
        switch (nId) {
            case CanBusDefine.MainDefine.CANBUS_SETTINGS:
                if (null != packet) {
                    final String impl = packet.getString("Implementation");
                    final boolean open = packet.getBoolean("CanSwitch");
                    final int canSeries = packet.getInt("CanSeries");
                    Driver().initCanbus(impl, open, canSeries, true);
                }
                break;
            case CanBusDefine.MainDefine.CANBUS_ENABLE:
                if (null != packet) {
                    final boolean enable = packet.getBoolean("enable");
                    Driver().enableCanbus(enable);
                }
                break;
            default:
                ret = super.dispatch(nId, packet);
                break;
        }
        return ret;
    }

    /**get supprt function string.*/
    private static String getSupportFunctionString(String[] array) {
        LogUtils.d(TAG, "getSupportFunctionString()");
        String ret = "";
        if (null != array) {
            for (int i = 0;i < array.length;i++) {
                ret += (array[i] + ",");
            }
        }
        return ret;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.canbusapk";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.canbusapk.ui.MainActivity";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_CANBUS;
    }

    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
//            LogUtils.d(TAG,"CurSourceListener " +" oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
//            sendHostInfoToSlave(newVal, true);
            int camera = -1;
            if (newVal == Define.Source.SOURCE_CAMERA && oldVal != Define.Source.SOURCE_CAMERA) {
                camera = 1;
            } else if (newVal != Define.Source.SOURCE_CAMERA && oldVal == Define.Source.SOURCE_CAMERA) {
                camera = 0;
            }
            if (camera != -1) {
                LogUtils.e(TAG, "CameraListener----camera="+camera+", getCamera()="+EventInputManager.getCamera());
                packHostInfo(CanBusDefine.HostInfo.BACKCAMMER_STATE.value(), camera);
            }
        }

        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG,"CurBackSourceListener " +" oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            sendHostInfoToSlave(newVal, true);
        }
    };

    private EventInputListener mEventInputListener = new EventInputListener() {
        @Override
        public void CameraListener(Boolean oldVal, Boolean newVal) {
            LogUtils.e(TAG, "CameraListener----newVal="+newVal+", getCamera()="+EventInputManager.getCamera());
            packHostInfo(CanBusDefine.HostInfo.BACKCAMMER_STATE.value(), EventInputManager.getCamera()?1:0);
        }
    };

    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            packHostInfo(CanBusDefine.HostInfo.PHONE_STATUS.value(), newVal);
        }

        @Override
        public void CallNumberListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "CallNumberListener---newVal="+newVal);
            packHostInfo(CanBusDefine.HostInfo.PHONE_NUMBER.value(), newVal);
        }
    };

    private RadioListener mRadioListener = new RadioListener() {
        @Override
        public void BandListener(Integer oldVal, Integer newVal) {//band(FM1=0 FM2=1 FM3=2 AM1=3 AM2=4)
            LogUtils.d(TAG, "band change, oldVal = " + oldVal + ", newVal = " + newVal);
            radioBand = newVal;
            packHostInfo(CanBusDefine.HostInfo.RADIO_INFO_BAND.value(), newVal);
        }
        @Override
        public void FreqListener(Integer oldVal, Integer newVal) {
//            LogUtils.d(TAG, "freq change, oldVal = " + oldVal + ", newVal = " + newVal+", BackSource="+SourceManager.getCurBackSource()+", cur="+SourceManager.getCurSource());
            radioFreq = newVal;
            packHostInfo(CanBusDefine.HostInfo.RADIO_INFO_FREQ.value(), newVal);
        }
        @Override
        public void FrequenceSaveOnListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "index change, oldVal = " + oldVal + ", newVal = " + newVal);
            radioSaveOn = newVal;
            packHostInfo(CanBusDefine.HostInfo.RADIO_INFO_CH.value(), newVal);
        }
    };

    private MediaListener mVideoListener = new MediaListener() {
        /**当前播放时间监听器*/
        @Override
        public void CurrentTimeListener(Integer oldVal, Integer newVal) {
//            LogUtils.d(TAG,"CurTime:" + newVal);
            mediaCurTime_video = newVal;
            packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURTIME.value(), newVal/1000);
        }
        /**播放下标监听器, 从0开始*/
        @Override
        public void IndexListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Index:" + newVal);
            mediaCurIndex_video = newVal;
            packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURINDEX.value(), newVal);
        }
        /**媒体列表总数监听器*/
        @Override
        public void TotalListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Total:" + newVal);
            mediaTotal_video = newVal;
            packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_TOTAL.value(), newVal);
        }
    };

    private MediaListener mAudioListener = new MediaListener() {
        /**当前播放时间监听器*/
        @Override
        public void CurrentTimeListener(Integer oldVal, Integer newVal) {
//            LogUtils.d(TAG,"CurTime:" + newVal);
            if (SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                mediaCurTime = newVal;
            } else {
                mediaCurTime_video = newVal;
            }
            packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURTIME.value(), newVal/1000);
        }
        /**播放下标监听器, 从0开始*/
        @Override
        public void IndexListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Index:" + newVal);
            if (SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                mediaCurIndex = newVal;
            } else if (SourceManager.getCurSource() == Define.Source.SOURCE_VIDEO) {
                mediaCurIndex_video = newVal;
            }
            packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURINDEX.value(), newVal);
        }
        /**媒体列表总数监听器*/
        @Override
        public void TotalListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Total:" + newVal);
            if (SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                mediaTotal = newVal;
            } else {
                mediaTotal_video = newVal;
            }
            packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_TOTAL.value(), newVal);
        }
    };

    private VolumeListener mVolumeListener = new VolumeListener() {
        /**音量变化监听器*/
        @Override
        public void VolumeValueListener(Integer[] oldVal, Integer[] newVal) {
            volume = VolumeManager.getValue();
            int value = (muteState?0x01:0x00)<<7 | (volume &0x7F);
            packHostInfo(CanBusDefine.HostInfo.HOST_VOLUME_INFO.value(), value);
        }

        /**音量静音状态变化监听器*/
        @Override
        public void VolumeMuteListener(Boolean oldVal, Boolean newVal) {
            muteState = newVal;
            int value = (muteState?0x01:0x00)<<7 | (volume &0x7F);
            packHostInfo(CanBusDefine.HostInfo.HOST_VOLUME_INFO.value(), value);
        }
    };

    /**
     * language listener
     */
    private LanguageListener mLanguageListener = new LanguageListener() {
        @Override
        public void LocaleListener(String oldLgg, String newLgg) {
            LogUtils.d(TAG, "LocaleListener----new="+newLgg+", language="+Locale.getDefault().getLanguage());
            if (Driver() != null) {
                packCarSettingInfo(CarSettingsDefine.Spinner.LANGUAGE.value(), Driver().getLanuage(newLgg));
            }
        }
    };

    private void sendHostInfoToSlave(int curSource, boolean sendsource) {
        if (sendsource) {
            packHostInfo(CanBusDefine.HostInfo.CURRENT_SOURCE.value(), curSource);
        }

        switch (curSource) {
            case Define.Source.SOURCE_RADIO:
                packHostInfo(CanBusDefine.HostInfo.RADIO_INFO_BAND.value(), radioBand);
                packHostInfo(CanBusDefine.HostInfo.RADIO_INFO_FREQ.value(), radioFreq);
                packHostInfo(CanBusDefine.HostInfo.RADIO_INFO_CH.value(), radioSaveOn);
                break;
            case Define.Source.SOURCE_AUDIO:
                packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURTIME.value(), mediaCurTime/1000);
                packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURINDEX.value(), mediaCurIndex);
                packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_TOTAL.value(), mediaTotal);
                break;
            case Define.Source.SOURCE_VIDEO:
                packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURTIME.value(), mediaCurTime_video/1000);
                packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_CURINDEX.value(), mediaCurIndex_video);
                packHostInfo(CanBusDefine.HostInfo.MEDIA_INFO_TOTAL.value(), mediaTotal_video);
                break;
            default:
                break;
        }
    }

    private void packHostInfo(String key, int value) {
        if (Driver() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(CanBusDefine.Parameter.PARAMETER_INT.value(), key);
            bundle.putInt(key, value);
            Driver().sendHostInfoToCan(key, CanBusDefine.PARAMETER_SPINNER, bundle);
        }
    }

    private void packHostInfo(String key, String strValue) {
        if (Driver() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(CanBusDefine.Parameter.PARAMETER_STRING.value(), key);
            bundle.putString(key, strValue);
            Driver().sendHostInfoToCan(key, CanBusDefine.PARAMETER_STRING, bundle);
        }
    }

    private void packCarSettingInfo(String key, int value) {
        if (Driver() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(CanBusDefine.Parameter.PARAMETER_INT.value(), key);
            bundle.putInt(key, value);
            Driver().sendHostInfoToCan(key, CanBusDefine.PARAMETER_SPINNER, bundle);
        }
    }

    public int getCanSeries() {
        int ret = CanBusDefine.CanSeries.RuiZhiCheng.VOLKSWAGEN_GENERAL;
        Packet packet = getInfo();
        if (packet != null) {
            ret = packet.getInt("CanSeries");
        } else {
            LogUtils.e(TAG, "getInfo = null!");
        }
        return ret;
    }

    public int getCanSwitchStatus() {
        int ret = 0;
        Packet packet = getInfo();
        if (packet != null) {
            ret = packet.getInt("CanSwitchStatus");
        } else {
            LogUtils.e(TAG, "getInfo = null!");
        }
        return ret;
    }

    public int getCanBusConnected() {
        int ret = 0;
        Packet packet = getInfo();
        if (packet != null) {
            ret = packet.getBoolean("CanBusConnected") ? 1 : 0;
        } else {
            LogUtils.e(TAG, "getInfo = null!");
        }
        return ret;
    }

    public boolean getCanBusSwitch() {
        boolean ret = true;
        Packet packet = getInfo();
        if (packet != null) {
            ret = packet.getBoolean("CanSwitch");
        } else {
            LogUtils.e(TAG, "getInfo = null!");
        }
        return ret;
    }
}
