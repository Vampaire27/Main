package com.wwc2.main.settings;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.mediatek.miravision.setting.MiraVisionJni;
import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.shell.ShellUtils;
import com.wwc2.main.R;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.backlight.BacklightListener;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.client.ClientListener;
import com.wwc2.main.driver.client.driver.BaseClientDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.common.CommonListener;
import com.wwc2.main.driver.datetime.DateTimeDriver;
import com.wwc2.main.driver.eq.EQDriver;
import com.wwc2.main.driver.eq.EQListener;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.factory.FactoryListener;
import com.wwc2.main.driver.info.InfoDriver;
import com.wwc2.main.driver.info.InfoDriverable;
import com.wwc2.main.driver.language.LanguageDriver;
import com.wwc2.main.driver.language.LanguageListener;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.driver.steer.SteerDriver;
import com.wwc2.main.driver.steer.SteerListener;
import com.wwc2.main.driver.system.SystemDriver;
import com.wwc2.main.driver.tptouch.TPTouchListener;
import com.wwc2.main.driver.tptouch.TpTouchDriver;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.driver.version.VersionDriverable;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PanoramicManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.navi.driver.NaviDriverable;
import com.wwc2.main.provider.LogicProvider;
import com.wwc2.main.provider.LogicProviderHelper;
import com.wwc2.main.settings.driver.SettingsDriver;
import com.wwc2.main.settings.util.FileUtil;
import com.wwc2.main.settings.util.ToastUtil;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.mainui_interface.MainUIInterface;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.settings_interface.SettingsInterface;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * the settings logic.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public class SettingsLogic extends BaseLogic {
    private static final String TAG = "SettingsLogic";

    public static final String TP_COORDINATE_SWITCH_NODE = "/sys/class/gpiodrv/gpio_ctrl/ctp_switch";
    private static final String OUTPUT_CONFIG_LIST_XML = "/system/etc/outputConfigList.xml";

    private static final String RES_CONFIG_XML = "ResConfigList.xml";

    private static final String USB_PROPERTY = "persist.sys.usbspeed";
    private static final String USB_2_0 = "1";
    private static final String USB_1_1 = "0";
    private static final String USB_NODE = "sys/class/gpiodrv/gpio_ctrl/usb_speed";

    public static final String TP_UPDATE_NODE = "/sys/class/gpiodrv/gpio_ctrl/gtp_update";
    private static final String ACTION_SCREEN_SIZE = "wwc2.setraw.data";
    private static final String ACTION_AVDD_SIZE = "wwc2.setraw.data";

    public final static String VCOM_FILE = "/sys/class/gpiodrv/gpio_ctrl/vcom_pwm_duty";
    public final static String AVDD_FILE = "/sys/class/gpiodrv/gpio_ctrl/avdd_pwm_duty";
    private int mContrastMax = 10, mSaturationMax = 10, mBrightnessMax = 10, mGammaMax = 10, mSharpness = 10, mDynamiccontrastMax = 10;

    private List<String> ResConfigList;

    private List<String> inputConfigList;
    private final String FILE_NAME = "FileName";
    private static final String SRC_PATH = "SrcPath";
    private static final String DES_PATH = "DesPath";


    private static final String LKLOGO_CMDS = "jbset:2 ";
    private static final String LKLOGO_PROPERTIES = "ctl.start";

    private static final String LogoSet_exe = "/system/bin/jbset 2 ";

    //begin zhongyang.hu add for Vsync apk
    private static final String ACTION_CONFIG_FINISH ="vsync.intent.config_finish";
    private static final String ACTION_IMPORT_CONFIG_SUCCESSED = "com.wwc2.action.import.config.success";
    private static final String VSYNC_PATH = "VsyncRes/";
   //zhongyang_usb
    private static final String usbStrArray[]={"/storage/usbotg/",
                                               "/storage/usbotg1/",
                                               "/storage/usbotg2/",
                                               "/storage/usbotg3/",
                                               "/storage/sdcard1/"};
    private Context mContext = null;
    //end

    private static final String STREAM_NAME = "/sys/class/gpiodrv/gpio_ctrl/360_camtype";

    //DVR水印
    private static final String RECORD_WATERMASK = "wwc2.video.record.watermask";
    //DVR开关
    private static final String RECORD_ENABLE = "wwc2.video.record.enable";

    static {
        LogicProviderHelper.Provider(BaseClientDriver.CLIENT_PROJECT, BaseClientDriver.CLIENT_BS);
    }

    private static final int MAX_SCREEN_HEIGHT = 600 + 20;
    private static int MAX_SCREEN_WIDTH = 1024 + 20;

    private final int IMPORT_SUCCESS = 9999;
    private final int IMPORT_ERROR = 9998;
    private final int SAME_KEY = 8888;

    private final int UPDATE_OS = 8777;

    private final int MCU_NOT_MATCH = 8666;

    private int mTpDownX = -1;
    private int mTpDownY = -1;
//    private ToastHandler toastHandler = null;

    private static final int MCU_STAT_FINISH = 0;
    private static final int MCU_STAT_UNKNOWN = 1;
    private static final int MCU_STAT_UNFINISH = 2;

    private EQListener mEQListener = new EQListener() {
        @Override
        public void LoudnessListener(Boolean oldType, Boolean newType) {
            Packet packet = new Packet();
            packet.putBoolean(SettingsDefine.EQ.LOUDNESS, newType);
            Notify(SettingsInterface.MAIN_TO_APK.EQ_LOUDNESS, packet);
        }

        @Override
        public void SubwooferListener(Integer oldType, Integer newType) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.EQ.TYPE, SettingsDefine.EQ.Type.SUBWOOFER);
            packet.putInt(SettingsDefine.EQ.VALUE, newType);
            Notify(SettingsInterface.MAIN_TO_APK.EQ_TYPE, packet);
        }

        @Override
        public void BassListener(Integer oldType, Integer newType) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.EQ.TYPE, SettingsDefine.EQ.Type.BASS);
            packet.putInt(SettingsDefine.EQ.VALUE, newType);
            Notify(SettingsInterface.MAIN_TO_APK.EQ_TYPE, packet);
        }

        @Override
        public void MiddleListener(Integer oldType, Integer newType) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.EQ.TYPE, SettingsDefine.EQ.Type.MIDDLE);
            packet.putInt(SettingsDefine.EQ.VALUE, newType);
            Notify(SettingsInterface.MAIN_TO_APK.EQ_TYPE, packet);
        }

        @Override
        public void TrebleListener(Integer oldType, Integer newType) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.EQ.TYPE, SettingsDefine.EQ.Type.TREBLE);
            packet.putInt(SettingsDefine.EQ.VALUE, newType);
            Notify(SettingsInterface.MAIN_TO_APK.EQ_TYPE, packet);
        }

        @Override
        public void SubwooferSwitchListener(Boolean oldType, Boolean newType) {
            Packet packet = new Packet();
            packet.putBoolean("SUBWOOFER_SWITCH", newType);
            Notify(SettingsInterface.MAIN_TO_APK.SUBWOOFER_SWITCH, packet);
        }

        @Override
        public void SubwooferFreqListener(Integer oldType, Integer newType) {
            Packet packet = new Packet();
            packet.putInt("SUBWOOFER_FREQ", newType);
            Notify(SettingsInterface.MAIN_TO_APK.SUBWOOFER_FREQ, packet);
        }

        @Override
        public void StyleListener(Integer oldType, Integer newType) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.EQ.STYLE, newType);
            Notify(SettingsInterface.MAIN_TO_APK.EQ_STYLE, packet);
        }

        @Override
        public void XListener(Integer oldX, Integer newX) {
//            super.XListener(x);
        }

        @Override
        public void YListener(Integer oldY, Integer newY) {
//            super.YListener(y);
        }

        @Override
        public void SoundFieldHiddenListener(Boolean newValue) {
//            super.SoundFieldHiddenListener(newValue);
        }

        @Override
        public void DspSoundEffectListener(FourInteger[] oldValue, FourInteger[] newValue) {
            LogUtils.d("STM32EQDriver", "DspSoundEffectListener");
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("DspSoundEffect", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.DSP_SOUNDEFFECT, mPacket);
        }

        @Override
        public void DspParamListener(Integer[] oldValue, Integer[] newValue) {
            Packet packet = new Packet();
            packet.putIntegerObjectArray("DspParam", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.DSP_PARAM, packet);
        }

        @Override
        public void DspSoundFiledListener(Integer[] oldValue, Integer[] newValue) {
            Packet packet = new Packet();
            packet.putIntegerObjectArray("DspSoundFiled", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.DSP_SOUNDFILED, packet);
        }

        @Override
        public void ThreeeDSwitchListener(Boolean oldValue, Boolean newValue){
            Packet packet = new Packet();
            packet.putBoolean("3DSWITCH",newValue);
            Notify(SettingsInterface.MAIN_TO_APK.DSP_3DSWITCH, packet);//58
        }

        @Override
        public void EQSendMcuFinishedListener(Boolean oldValue, Boolean newValue) {
            LogUtils.d(TAG,"EQSendMcuFinishedListener---"+newValue);
            if(newValue) {
                synchronized (eqSendFinished) {
                     eqSendFinished.notify();
                }
            }
        }

        @Override
        public void DspHpfLpfListener(Integer[] oldValue, Integer[] newValue) {
            Packet packet = new Packet();
            packet.putIntegerObjectArray("DspHpfLpf", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.DSP_HPFLPF, packet);
        }

        @Override
        public void QValueListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putInt("QVALUE", newValue);
            Notify(0x3C, packet);
        }
    };

    /**
     * 通用设置变量监听
     */
    private CommonListener mCommonListener = new CommonListener() {
        /**
         * 刹车警告
         * @param oldValue
         * @param newValue
         */
        @Override
        public void BrakeWarningListener(Boolean oldValue, Boolean newValue) {
        }


        /**
         * 倒车音量
         * @param oldValue
         * @param newValue
         */
        @Override
        public void ReversingVolumeListener(Boolean oldValue, Boolean newValue) {
        }


        /**
         * 任意按键
         * @param oldValue
         * @param newValue
         */
        @Override
        public void AnyKeyListener(Boolean oldValue, Boolean newValue) {
        }


        /**
         * 按键音
         * @param oldValue
         * @param newValue
         */
        @Override
        public void KeyToneListener(Boolean oldValue, Boolean newValue) {
        }


        /**
         * 倒车镜像
         * @param oldValue
         * @param newValue
         */
        @Override
        public void ReverseImageListener(Boolean oldValue, Boolean newValue) {
        }


        /**
         * GPS监听
         * @param oldValue
         * @param newValue
         */
        @Override
        public void GpsMonitorListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            String name = SettingsDefine.Common.Switch.GPS_MONITOR.value();
            packet.putString(SettingsDefine.Common.SWITCH, name);
            packet.putBoolean(name, newValue);
        }


        /**
         * GPS混音
         * @param oldValue
         * @param newValue
         */
        @Override
        public void GpsMixListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            String name = SettingsDefine.Common.Switch.GPS_MIX.value();
            packet.putString(SettingsDefine.Common.SWITCH, name);
            packet.putBoolean(name, newValue);
        }


        /**
         * GPS混音比例
         * @param oldValue
         * @param newValue
         */
        @Override
        public void GpsMixRatioListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            String name = SettingsDefine.Common.Switch.GPS_MIX_RATIO.value();
            packet.putString(SettingsDefine.Common.SWITCH, name);
            packet.putInt(name, newValue);
            Notify(false, SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);

        }

        /**
         * 不保留第三方
         * @param oldValue
         * @param newValue
         */
        @Override
        public void Noretain3PartyListener(Boolean oldValue, Boolean newValue) {
        }

        /**
         *七彩灯开关
         */
        @Override
        public void ColorfulLightSwitchListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(SettingsDefine.Common.Switch.COLORFUL_LIGHT_SWITCH.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        /**
         * 常开小灯开关
         */
        public void SmallLightSwitchListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(SettingsDefine.Common.Switch.SMALL_LIGHT_SWITCH.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        /**
         * 闪烁开关
         */
        public void FlicherSwitchListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(SettingsDefine.Common.Switch.FLICHER_SWITCH.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        /**
         * 闪烁频率
         */
        public void FlicherRateListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.Common.Switch.FLICHER_RATE.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        /**
         * 七彩灯颜色
         */
        public void ColorfulLightColorListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.Common.Switch.COLORFUL_LIGHT_COLOR.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        /**
         * 威益德七彩灯颜色
         */
        public void ColorfulLightColorListener3Party(Integer oldValue, Integer newValue) {
            LogUtils.d("ColorfulLightColorListener3Party = "+ newValue);
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH,"COLORFUL_LIGHT_COLOR_3PARTY");
            packet.putInt("COLORFUL_LIGHT_COLOR_3PARTY", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        @Override
        public void defSystemVolumeListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.DEFAULT_SYSTEM_VOLUME.value());
            packet.putInt(SettingsDefine.Common.Switch.DEFAULT_SYSTEM_VOLUME.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        @Override
        public void defCallVolumeListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.DEFAULT_CALL_VOLUME.value());
            packet.putInt(SettingsDefine.Common.Switch.DEFAULT_CALL_VOLUME.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_SET, packet);
        }

        @Override
        public void GpsMixSupportListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.GPS_MIX_SUPPORT.value());
            packet.putBoolean(SettingsDefine.Common.Switch.GPS_MIX_SUPPORT.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.GPS_MIX_SUPPORT, packet);
        }

        @Override
        public void GprsApkNameListener(String oldValue, String newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.GPRS_APK_NAME.value());
            packet.putString(SettingsDefine.Common.Switch.GPRS_APK_NAME.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.GPRS_APK_NAME, packet);
        }

        @Override
        public void TpmsApkNameListener(String oldValue, String newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.TMPS_APK_NAME.value());
            packet.putString(SettingsDefine.Common.Switch.TMPS_APK_NAME.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.TMPS_APK_NAME, packet);
        }

        @Override
        public void DvrApkNameListener(String oldValue, String newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.DVR_APK_NAME.value());
            packet.putString(SettingsDefine.Common.Switch.DVR_APK_NAME.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.DVR_APK_NAME, packet);
        }

        @Override
        public void AutoLandPortListener(Boolean oldValue, Boolean newValue) {

            Uri uri_land_port = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY + "/" + LogicProvider.AUTO_LAND_PORT);
            getMainContext().getContentResolver().notifyChange(uri_land_port, null);
        }

        @Override
        public void monitorSwitchListener(Integer oldValue, Integer newValue) {

        }

        @Override
        public void deviceVoltaleListener(Integer oldValue, Integer newValue) {

        }
    };

    /**
     * 工厂设置监听器
     */
    private FactoryListener mFactoryListener = new FactoryListener() {
        @Override
        public void AuxListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.AUX.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }

        @Override
        public void NavigationListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.NAVIGATION.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }


        @Override
        public void RadioListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.RADIO.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }

        @Override
        public void BluetoothListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.BLUETOOTH.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }

        @Override
        public void AudioFrequencyListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.AUDIO_FREQUENCY.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }

        @Override
        public void VideoListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.VIDEO.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }

        @Override
        public void PictureListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.PICTURE.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }

        @Override
        public void VoiceAssistantListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.Modules.VOICE_ASSISTANT.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.REDIO_REGION, packet);
        }

        @Override
        public void RadioRegionListener(String oldValue, String newValue) {
            Packet packet = new Packet();
            packet.putString(Define.Factory.REGION, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.FACTORY_MODULES, packet);
        }

        @Override
        public void RadioAerialListener(boolean oldValue, boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean(Define.Factory.RADIO_AERIAL, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.RADIO_AERIAL, packet);
        }

        @Override
        public void RadioModuelListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putInt(Define.Factory.RADIO_MODULE, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.RADIO_MODULE, packet);
        }

        @Override
        public void RDSEnableListener(Boolean oldValue, Boolean newValue) {
            LogUtils.d(TAG, "RDSEnableListener---" + newValue);
            Packet packet = new Packet();
            packet.putBoolean("rds_enable", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.RDS_ENABLE, packet);
        }

        @Override
        public void FactoryPasswdListener(String oldValue, String newValue) {
//            Packet packet = new Packet();
//            packet.putString(Define.Factory.REGION, newValue);
//            Notify(SettingsInterface.MAIN_TO_APK.FACTORY_MODULES, packet);
        }

        @Override
        public void ScreenSizeListener(Integer oldValue, Integer newValue) {
            Intent intent = new Intent(ACTION_SCREEN_SIZE);
            intent.putExtra("type", "screen");
            intent.putExtra("para", newValue);
            getMainContext().sendBroadcast(intent);
        }

        @Override
        public void SteerADValueListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putInt(Define.Factory.STEER_AD_VALUE, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.STEER_AD_VALUE, packet);
        }

        @Override
        public void McuDebugInfoListener(Integer[] oldValue, Integer[] newValue) {
            Packet packet = new Packet();
            packet.putIntegerObjectArray(Define.Factory.MCU_DEBUG_INFO, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.MCU_DEBUG_INFO, packet);
        }

        @Override
        public void lkLogoIndexListener(Integer oldValue, Integer newValue) {
            //zhongyang.hu add for
            //SystemProperties.set(LKLOGO_PROPERTIES, LKLOGO_CMDS + newValue);
            String cmds = LogoSet_exe +  newValue;
            Log.d("setRawData", "cmds="+cmds);
            ShellUtils.CommandResult sc = ShellUtils.execCommand(cmds, false, true);
        }

        @Override
        public void setVComListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.VCOM.value());
            packet.putInt(SettingsDefine.Common.Switch.VCOM.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.VCOM_SET, packet);

//            CommonDriver.Driver().setVComValue(newValue,false);
        }

        @Override
        public void avddListener(Integer oldValue, Integer newValue) {
           // super.avddListener(oldValue, newValue);
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.AVDD.value());
            packet.putInt(SettingsDefine.Common.Switch.AVDD.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.AVDD_SET, packet);

            Intent intent = new Intent();
            intent.setAction(ACTION_AVDD_SIZE);
            intent.putExtra("type", "avdd");
            intent.putExtra("para", newValue);
            getMainContext().sendBroadcast(intent);
        }

        @Override
        public void cameraSwitchListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.CAMERA_SET_SWITCH.value());
            packet.putBoolean(SettingsDefine.Common.Switch.CAMERA_SET_SWITCH.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.CAMERA_SET_SWITCH, packet);

            LogUtils.d(TAG, "PANORAMIC_VIDEO_TYPE--00--" + FactoryDriver.Driver().getPanoramicVideoType());
            if (newValue) {
                if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                        !FactoryDriver.Driver().getDvrEnable()) {
                    //由DVR处理，不再写节点。2019-10-23
                    FileUtil.write(FactoryDriver.Driver().getPanoramicVideoType() + "", STREAM_NAME);
                }
            } else {
//                FileUtil.write(1 + "", STREAM_NAME);//经讨论，去掉驱动的自动检测，默认AHD25fps。2019-01-09
                FactoryDriver.Driver().setPanoramicVideoType(1);

                //360全景必须设置制式。
                FactoryDriver.Driver().setPanoramicSwitch(false);
            }
        }

        @Override
        public void panoramicSwitchListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.PANORAMIC_SUPPORT.value());
            packet.putBoolean(SettingsDefine.Common.Switch.PANORAMIC_SUPPORT.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.PANORAMIC_SUPPORT, packet);

            if (newValue) {
                Packet packet1 = new Packet();
                packet1.putObject("context", getMainContext());
                PanoramicManager.getInstance().onCreate(packet1);
            } else {
                PanoramicManager.getInstance().onDestroy();
            }
        }

        @Override
        public void panoramicConnTypeListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.PANORAMIC_CONN_TYPE.value());
            packet.putInt(SettingsDefine.Common.Switch.PANORAMIC_CONN_TYPE.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.PANORAMIC_CONN_TYPE, packet);

            if (newValue == 0) {
                Packet packet1 = new Packet();
                packet1.putObject("context", getMainContext());
                PanoramicManager.getInstance().onCreate(packet1);
            } else {
                PanoramicManager.getInstance().onDestroy();
            }
        }

        @Override
        public void panoramicVideoTypeListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.PANORAMIC_VIDEO_TYPE.value());
            packet.putInt(SettingsDefine.Common.Switch.PANORAMIC_VIDEO_TYPE.value(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.PANORAMIC_VIDEO_TYPE, packet);

            LogUtils.d(TAG, "PANORAMIC_VIDEO_TYPE--11--" + newValue);
            /*8: default value user not select 360,
            1:AHD25fps,
            2:AHD30fps,
            3:CVBS_NTSC,
            4:CVBS_PAL,
            5:1080P25fps,
            6:1080P30fps,
            7:TVI,
            8:CVI,
            9:PVI*/
            if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                    !FactoryDriver.Driver().getDvrEnable()) {
                //由DVR处理，不再写节点。2019-10-23
                FileUtil.write(newValue + "", STREAM_NAME);
            }
        }

        @Override
        public void panoramicTypeListener(Integer oldValue, Integer newValue) {
            LogUtils.d(TAG, "panoramicTypeListener----" + newValue);
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, "ONE_PANORAMIC");
            packet.putInt("ONE_PANORAMIC", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.COMMON_SWITCH_GET, packet);
        }

        @Override
        public void dvrEnableListener(Boolean oldValue, Boolean newValue) {
            LogUtils.d(TAG, "dvrEnableListener----" + newValue);

            Uri uri_dvr = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY +
                    "/" + LogicProvider.DVR_SUPPORT);
            getMainContext().getContentResolver().notifyChange(uri_dvr, null);

            SystemProperties.set("persist.sys.dvr_enable", newValue + "");

            Packet packet = new Packet();
            packet.putString(SettingsDefine.Common.SWITCH, SettingsDefine.Common.Switch.DVR_SUPPORT.name());
            packet.putBoolean(SettingsDefine.Common.Switch.DVR_SUPPORT.name(), newValue);
            Notify(SettingsInterface.MAIN_TO_APK.DVR_SUPPORT, packet);

            //目前做法：设置DVR开关后，会自动重启。因此下面可不作处理。
            STM32MCUDriver.killProcess(getMainContext(), "com.wwc2.launcher");

            if (!newValue) {
                SystemProperties.set(RECORD_WATERMASK, "0");
                SystemProperties.set(RECORD_ENABLE, "0");
            } else {
                if (ApkUtils.isAPKExist(mContext, "com.wwc2.dvr")) {
                    ApkUtils.stopServiceSafety(mContext, "com.wwc2.dvr.RecordService",
                            "com.wwc2.dvr",
                            "com.wwc2.dvr.RecordService");
                    ApkUtils.startServiceSafety(mContext,
                            "com.wwc2.dvr.RecordService",
                            "com.wwc2.dvr",
                            "com.wwc2.dvr.RecordService");
                }
            }
        }

        public void uiStyleNameListener(String oldValue, String newValue) {

        }

        @Override
        public void uiStyleListener(Integer oldValue, Integer newValue) {
            Uri uri_uiStyle = Uri.parse("content://" + com.wwc2.common_interface.Provider.AUTHORITY +
                    "/" + LogicProvider.UI_STYLE);
            getMainContext().getContentResolver().notifyChange(uri_uiStyle, null);

            LogUtils.d(TAG, "uiStyleListener----old=" + oldValue + ", new=" + newValue);
            SystemProperties.set("persist.sys.ui_style", newValue + "");
        }

        public void screenBrightnessListener(Integer oldValue, Integer newValue) {
            MiraVisionJni.setPicBrightnessIndex(newValue);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_BRIGHTNESS, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_BRIGHTNESS, packet);
        }
        public void screenColourtempListener(Integer oldValue, Integer newValue) {
            MiraVisionJni.setGammaIndex(newValue);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_COLOUR_TEMP, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_COLOUR_TEMP, packet);
        }
        public void screenSaturationListener(Integer oldValue, Integer newValue) {
            MiraVisionJni.setSaturationIndex(newValue);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_SATURATION, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_SATURATION, packet);
        }
        public void screenContrastListener(Integer oldValue, Integer newValue) {
            MiraVisionJni.setContrastIndex(newValue);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_CONTRAST, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_CONTRAST, packet);
        }
        public void screenSharpnessListener(Integer oldValue, Integer newValue) {
            MiraVisionJni.setSharpnessIndex(newValue);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_SHARPNESS, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_SHARPNESS, packet);
        }
        public void screenDynamiccontrastListener(Integer oldValue, Integer newValue) {
            MiraVisionJni.setDynamicContrastIndex(newValue);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_DYNAMICCONTRAST, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_DYNAMICCONTRAST, packet);
        }
        public void screenVcomListener(Integer oldValue, Integer newValue) {
            //不能一直发广播，否则会一直写flash。只在保存时发广播。2018-10-30
//            Intent intent = new Intent();
//            intent.setAction(ACTION_AVDD_SIZE);
//            intent.putExtra("type", "vcom");
//            intent.putExtra("para", newValue);
//            getMainContext().sendBroadcast(intent);

            FileUtil.write(newValue + "", VCOM_FILE);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_VCOM, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_VCOM, packet);
        }
        public void screenAvddListener(Integer oldValue, Integer newValue) {
            //不能一直发广播，否则会一直写flash。只在保存时发广播。2018-10-30
//            Intent intent = new Intent();
//            intent.setAction(ACTION_AVDD_SIZE);
//            intent.putExtra("type", "avdd");
//            intent.putExtra("para", newValue);
//            getMainContext().sendBroadcast(intent);

            FileUtil.write(newValue + "", AVDD_FILE);

            Packet packet = new Packet();
            packet.putInt(SettingsDefine.ScreenParam.TYPE_AVDD, newValue);
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_AVDD, packet);
        }

        public void rotateVoltageListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putInt("rotate_voltage", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.ROTATE_VOLATGE, packet);
        }
        public void rotateTimeListener(Integer oldValue, Integer newValue) {
            Packet packet = new Packet();
            packet.putInt("rotate_time", newValue);
            Notify(SettingsInterface.MAIN_TO_APK.ROTATE_TIME, packet);
        }
    };

    /**
     * 背光亮度监听器
     */
    private BacklightListener mBacklightListner = new BacklightListener() {
        @Override
        public void BacklightValueListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.Brightness.BRIGHTNESS, newVal);
            Notify(SettingsInterface.MAIN_TO_APK.BACKLIGHT_VALUE, packet);
        }

        @Override
        public void BacklightValueDayListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.Brightness.BRIGHTNESS_DAY, newVal);
            Notify(SettingsInterface.MAIN_TO_APK.BACKLIGHT_DAY_VALUE, packet);
        }

        @Override
        public void BacklightValueNightListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.Brightness.BRIGHTNESS_NIGHT, newVal);
            Notify(SettingsInterface.MAIN_TO_APK.BACKLIGHT_NIGHT_VALUE, packet);
            LogUtils.d(TAG, "BacklightValueNightListener---old=" + oldVal + ", new=" + newVal);
        }

        @Override
        public void BacklightModeListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.Brightness.BRIGHTNESS_MODE, newVal);
            Notify(SettingsInterface.MAIN_TO_APK.BACKLIGHT_MODE, packet);
        }

        @Override
        public void BacklightAdjustEnableListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean(SettingsDefine.Brightness.AOTUADJUST_EN, newVal);
            Notify(SettingsInterface.MAIN_TO_APK.BACKLIGHT_ENABLE, packet);
        }

        @Override
        public void BacklightAutoSwitchListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean(SettingsDefine.Brightness.AOTUADJUST, newVal);
            Notify(SettingsInterface.MAIN_TO_APK.BACKLIGHT_AUTO_ADJUST, packet);
        }
    };

    /**
     * Steer study monitor
     */
    private SteerListener mSteerListener = new SteerListener() {
        @Override
        public void ADKeyStatusListener(Byte oldState, Byte newState) {
            Packet packet = new Packet();
            packet.putByte(SettingsDefine.SteerStudy.Extra.keyStatus, newState);
            Notify(SettingsInterface.MAIN_TO_APK.STEER_KEY_STATUS, packet);
            super.ADKeyStatusListener(oldState, newState);
        }

        @Override
        public void ADKeyInfoListener(Byte[] oldInfo, Byte[] newInfo) {
            Packet packet = new Packet();
            packet.putByteObjectArray(SettingsDefine.SteerStudy.Extra.keyInfo, newInfo);
            Notify(SettingsInterface.MAIN_TO_APK.STEER_KEY_INFO, packet);
            super.ADKeyInfoListener(oldInfo, newInfo);
        }

        @Override
        public void PanelKeyStatusListener(Byte oldState, Byte newState) {
            Packet packet = new Packet();
            packet.putByte(SettingsDefine.PanelStudy.Extra.keyStatus, newState);
            Notify(SettingsInterface.MAIN_TO_APK.PANEL_KEY_STATUS, packet);
            super.PanelKeyStatusListener(oldState, newState);
        }

        @Override
        public void PanelKeyInfoListener(Byte[] oldInfo, Byte[] newInfo) {
            Packet packet = new Packet();
            packet.putByteObjectArray(SettingsDefine.PanelStudy.Extra.keyInfo, newInfo);
            Notify(SettingsInterface.MAIN_TO_APK.PANEL_KEY_INFO, packet);
            super.PanelKeyInfoListener(oldInfo, newInfo);
        }
    };

    /**
     * panel key study
     */
    private TPTouchListener mTPTouchListener = new TPTouchListener() {
        /**get tp data packet.*/
        private Packet getPacket() {
            Packet ret = null;
            Driver driver = DriverManager.getDriverByName(TpTouchDriver.DRIVER_NAME);
            if (null != driver) {
                Packet info = driver.getInfo();
                if (null != info) {
                    ret = new Packet();
                    final Integer[] codes = info.getIntObjectArray("Codes");
                    if (null != codes) {
                        ret.putIntegerObjectArray("Codes", codes);
                    }
                    final Integer[] longCodes = info.getIntObjectArray("LongCodes");
                    if (null != longCodes) {
                        ret.putIntegerObjectArray("LongCodes", longCodes);
                    }
                    final Integer[] lastCodes = info.getIntObjectArray("LastCodes");
                    if (null != lastCodes) {
                        ret.putIntegerObjectArray("ContinueCodes", lastCodes);
                    }
                }
            }
            return ret;
        }

        @Override
        public void InitListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "tptouch init codes");
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_KEYS_STATE, getPacket());
        }

        @Override
        public void CodesListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "tptouch update codes");
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_KEYS_STATE, getPacket());
        }

        @Override
        public void LongCodesListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "tptouch update longcodes");
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_KEYS_STATE, getPacket());
        }

        @Override
        public void LastCodesListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "tptouch update lastcodes");
            Notify(SettingsInterface.MAIN_TO_APK.SCREEN_KEYS_STATE, getPacket());
        }

        @Override
        public void TPCoordinateSwitchListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "TPCoordinateSwitch:" + newVal);
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.TPCoordinate.SWITCH, newVal);
            Notify(false, SettingsInterface.MAIN_TO_APK.TP_COORDINATE_SWITCH, packet);
            FileUtil.write(newVal + "", TP_COORDINATE_SWITCH_NODE);
        }
    };
    private TPTouchListener.TPPositionListener mTPPositionListener = new TPTouchListener.TPPositionListener() {
        @Override
        public boolean TPDown(int x, int y) {
            LogUtils.d(TAG, "tptouch TPDown");
            mTpDownX = x;
            mTpDownY = y;

            //判断是屏幕之外的区域点击才学习
            if (x >= MAX_SCREEN_WIDTH || y >= MAX_SCREEN_HEIGHT) {
                Packet packet = new Packet();
                Notify(SettingsInterface.MAIN_TO_APK.STEER_KEY_PRESSED, packet);
                return true;
            } else return false;
        }

        @Override
        public boolean TPUp(int x, int y) {
            LogUtils.d(TAG, "tptouch TPUp");
            return false;
        }
    };

    /**
     * language listener
     */
    private LanguageListener mLanguageListener = new LanguageListener() {
        @Override
        public void LocaleListener(String oldLgg, String newLgg) {
            if (null != newLgg) {
                Packet packet = new Packet();
                packet.putString(SettingsDefine.Language.LANGUAGE, newLgg);
                Notify(SettingsInterface.MAIN_TO_APK.LANGUAGE, packet);
            }
            super.LocaleListener(oldLgg, newLgg);
        }
    };

    /**
     * client listener
     */
    private ClientListener mClientListener = new ClientListener() {
        @Override
        public void ClientProjectListener(String oldVal, String newVal) {
//            LogicProviderHelper.getInstance().update(BaseClientDriver.CLIENT_PROJECT, newVal);
        }
    };

    @Override
    public String getTypeName() {
        return "Settings";
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.settings";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.settings.MainActivity";
    }

    @Override
    public boolean isEnterAlwaysPackage() {
        return true;
    }

    @Override
    public String getMessageType() {
        return SettingsDefine.MODULE;
    }

    @Override
    public List<String> getAPKPacketList() {
        // 主服务需要挂起模块的白名单
        List<String> list = new ArrayList<String>();
        list.add("com.android.settings");
        return list;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_SETTINGS;
    }

    @Override
    public void onPrepare() {
        String usb_speed = SystemProperties.get(USB_PROPERTY, USB_2_0);
        if (USB_1_1.equals(usb_speed)) {
            FileUtil.write(USB_1_1, USB_NODE);
        } else {
            FileUtil.write(USB_2_0, USB_NODE);
        }
        super.onPrepare();
    }

    @Override
    public Packet getInfo() {
        Packet ret = super.getInfo();
        if (null == ret) {
            ret = new Packet();
        }

        Packet eq = DriverManager.getDriverByName(EQDriver.DRIVER_NAME).getInfo();
        if (null != eq) {
            ret.putInt(SettingsDefine.EQ.STYLE, eq.getInt(SettingsDefine.EQ.STYLE));
            ret.putBoolean(SettingsDefine.EQ.LOUDNESS, eq.getBoolean(SettingsDefine.EQ.LOUDNESS));
            ret.putIntegerObjectArray(SettingsDefine.EQ.VALUE, eq.getIntObjectArray(SettingsDefine.EQ.VALUE));
            ret.putIntArray(SettingsDefine.SoundField.COORDS, eq.getIntArray(SettingsDefine.SoundField.COORDS));
            ret.putBoolean("SoundFieldHidden",eq.getBoolean("SoundFieldHidden"));
            ret.putBoolean("SUBWOOFER_SWITCH",eq.getBoolean("SUBWOOFER_SWITCH"));
            ret.putInt("SUBWOOFER_FREQ",eq.getInt("SUBWOOFER_FREQ"));
            ret.putParcelableArray("DspSoundEffect", eq.getParcelableArray("DspSoundEffect"));
            ret.putIntegerObjectArray("DspParam", eq.getIntObjectArray("DspParam"));
            ret.putIntegerObjectArray("DspSoundFiled", eq.getIntObjectArray("DspSoundFiled"));
            ret.putBoolean("3DSWITCH",eq.getBoolean("3DSWITCH"));
            ret.putIntegerObjectArray("DspHpfLpf", eq.getIntObjectArray("DspHpfLpf"));
            ret.putInt("QVALUE", eq.getInt("QVALUE"));
        }

        Packet brightness = DriverManager.getDriverByName(BacklightDriver.DRIVER_NAME).getInfo();
        if (brightness != null) {
            ret.putInt(SettingsDefine.Brightness.BRIGHTNESS, brightness.getInt(SettingsDefine.Brightness.BRIGHTNESS));
            ret.putBoolean(SettingsDefine.Brightness.AOTUADJUST, brightness.getBoolean(SettingsDefine.Brightness.AOTUADJUST));

            ret.putInt(SettingsDefine.Brightness.BRIGHTNESS_MODE, brightness.getInt(SettingsDefine.Brightness.BRIGHTNESS_MODE));
            ret.putInt(SettingsDefine.Brightness.BRIGHTNESS_DAY, brightness.getInt(SettingsDefine.Brightness.BRIGHTNESS_DAY));
            ret.putInt(SettingsDefine.Brightness.BRIGHTNESS_NIGHT, brightness.getInt(SettingsDefine.Brightness.BRIGHTNESS_NIGHT));
        }

        Packet language = DriverManager.getDriverByName(LanguageDriver.DRIVER_NAME).getInfo();
        if (language != null) {
            String lang = language.getString(SettingsDefine.Language.LANGUAGE);
            if (null == lang) {
                lang = Define.Language.Default.name();
            }

            //获取默认值
            if (lang.equals(Define.Language.Default.name())) {
                CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
                if (logic != null) {
                    Packet pack = new Packet();
                    pack.putString(com.wwc2.systempermission_interface.SystemPermissionDefine.ParseKey.LOCALE, lang);
                    logic.Notify(SystemPermissionInterface.MAIN_TO_APK.LOCALE, pack);
                }
            }

            //未初始化设置初始值
            ret.putString(SettingsDefine.Language.LANGUAGE, lang);
        }

        ret.putBoolean(SettingsDefine.Debug.APP_DEBUG, false);
        ret.putBoolean(SettingsDefine.Debug.MCU_DEBUG, false);

        Packet version = DriverManager.getDriverByName(VersionDriver.DRIVER_NAME).getInfo();
        if (version != null) {
            ret.putString(SettingsDefine.Version.CLINET, version.getString(SettingsDefine.Version.CLINET));
            ret.putString(SettingsDefine.Version.SYSTEM, version.getString(SettingsDefine.Version.SYSTEM));
            ret.putString(SettingsDefine.Version.MCU, version.getString(SettingsDefine.Version.MCU));
            ret.putString(SettingsDefine.Version.KERNEL, version.getString(SettingsDefine.Version.KERNEL));
            ret.putString(SettingsDefine.Version.HARDWARE, version.getString(SettingsDefine.Version.HARDWARE));
            ret.putString(SettingsDefine.Version.APP, version.getString(SettingsDefine.Version.APP));
            ret.putString(SettingsDefine.Version.MODELNUMBER, version.getString(SettingsDefine.Version.MODELNUMBER));
            ret.putString(SettingsDefine.Version.UNKNOWN, version.getString(SettingsDefine.Version.UNKNOWN));
            ret.putString(SettingsDefine.Version.FIREWARE, version.getString(SettingsDefine.Version.FIREWARE));
            ret.putString(SettingsDefine.Version.BASEBAND, version.getString(SettingsDefine.Version.BASEBAND));
            ret.putString(SettingsDefine.Version.RADIO, version.getString(SettingsDefine.Version.RADIO));
            ret.putString(SettingsDefine.Version.MODULE_BLUETOOTH, version.getString(SettingsDefine.Version.MODULE_BLUETOOTH));
            ret.putString(SettingsDefine.Version.APP_BLUETOOTH, version.getString(SettingsDefine.Version.APP_BLUETOOTH));
            ret.putString(SettingsDefine.Version.LAUNCHER, version.getString(SettingsDefine.Version.LAUNCHER));
            ret.putString(SettingsDefine.Version.SETTINGS, version.getString(SettingsDefine.Version.SETTINGS));
            ret.putString(SettingsDefine.Version.AUDIO_FREQ, version.getString(SettingsDefine.Version.AUDIO_FREQ));
            ret.putString(SettingsDefine.Version.VIDEO, version.getString(SettingsDefine.Version.VIDEO));
            String aux = version.getString(SettingsDefine.Version.AUX);
            ret.putString(SettingsDefine.Version.AUX, aux);
            ret.putString(SettingsDefine.Version.IMAGE, version.getString(SettingsDefine.Version.IMAGE));
            ret.putString(SettingsDefine.Version.NAVIGATION, version.getString(SettingsDefine.Version.NAVIGATION));
            ret.putString(SettingsDefine.Version.ASTERN, version.getString(SettingsDefine.Version.ASTERN));
            ret.putString(SettingsDefine.Version.MAIN, version.getString(SettingsDefine.Version.MAIN));
            ret.putString(SettingsDefine.Version.MAINSDK, version.getString(SettingsDefine.Version.MAINSDK));

            ret.putString(SettingsDefine.Version.CANBUS, version.getString(SettingsDefine.Version.CANBUS));
        }
        Packet time = DriverManager.getDriverByName(DateTimeDriver.DRIVER_NAME).getInfo();
        if (time != null) {
            ret.putString(Define.Time.FORMAT, time.getString(Define.Time.FORMAT));
            ret.putString(Define.Time.RESOURCE, time.getString(Define.Time.RESOURCE));
        }

        Packet factory = DriverManager.getDriverByName(FactoryDriver.DRIVER_NAME).getInfo();
        if (factory != null) {
            /**初始化工厂模块的初始化*/
            ret.putBoolean(Define.Factory.Modules.NAVIGATION.value(), factory.getBoolean(Define.Factory.Modules.NAVIGATION.value()));
            ret.putBoolean(Define.Factory.Modules.RADIO.value(), factory.getBoolean(Define.Factory.Modules.RADIO.value()));
            ret.putBoolean(Define.Factory.Modules.BLUETOOTH.value(), factory.getBoolean(Define.Factory.Modules.BLUETOOTH.value()));
            ret.putBoolean(Define.Factory.Modules.AUDIO_FREQUENCY.value(), factory.getBoolean(Define.Factory.Modules.AUDIO_FREQUENCY.value()));
            ret.putBoolean(Define.Factory.Modules.VIDEO.value(), factory.getBoolean(Define.Factory.Modules.VIDEO.value()));
            ret.putBoolean(Define.Factory.Modules.PICTURE.value(), factory.getBoolean(Define.Factory.Modules.PICTURE.value()));
            ret.putBoolean(Define.Factory.Modules.AUX.value(), factory.getBoolean(Define.Factory.Modules.AUX.value()));
            ret.putBoolean(Define.Factory.Modules.VOICE_ASSISTANT.value(), factory.getBoolean(Define.Factory.Modules.VOICE_ASSISTANT.value()));
            ret.putString(Define.Factory.PASSWD, factory.getString(Define.Factory.PASSWD));
            /** 收音机区域初始化 */
            ret.putString(Define.Factory.REGION, factory.getString(Define.Factory.REGION));
            ret.putBoolean("rds_enable", factory.getBoolean("rds_enable"));
            ret.putString("VoiceChannel", factory.getString("VoiceChannel"));
            ret.putBoolean(Define.Factory.RADIO_AERIAL, factory.getBoolean(Define.Factory.RADIO_AERIAL));
            ret.putInt(Define.Factory.RADIO_MODULE, factory.getInt(Define.Factory.RADIO_MODULE));
            ret.putInt("ScreenSize",factory.getInt("ScreenSize"));
            ret.putInt("LKLogoIndex", factory.getInt("LKLogoIndex"));
            ret.putString(Define.Factory.RESET_PASSWD, factory.getString(Define.Factory.RESET_PASSWD));
            ret.putInt(SettingsDefine.Common.Switch.VCOM.value(), factory.getInt(SettingsDefine.Common.Switch.VCOM.value()));
            ret.putInt(SettingsDefine.Common.Switch.UI_STYLE.name(), factory.getInt(SettingsDefine.Common.Switch.UI_STYLE.name()));
            ret.putBoolean(SettingsDefine.Common.Switch.UI_STYLE_SHOW.name(), factory.getBoolean(SettingsDefine.Common.Switch.UI_STYLE_SHOW.name()));
            ret.putStringArray(SettingsDefine.Common.Switch.UI_NAME_ARRAY.name(), factory.getStringArray(SettingsDefine.Common.Switch.UI_NAME_ARRAY.name()));
            ret.putString(SettingsDefine.Common.Switch.UI_STYLE_NAME.name(), factory.getString(SettingsDefine.Common.Switch.UI_STYLE_NAME.name()));

            ret.putBoolean(SettingsDefine.Common.Switch.CAMERA_SET_SWITCH.name(), factory.getBoolean(SettingsDefine.Common.Switch.CAMERA_SET_SWITCH.name()));
            ret.putBoolean(SettingsDefine.Common.Switch.PANORAMIC_SUPPORT.name(), factory.getBoolean(SettingsDefine.Common.Switch.PANORAMIC_SUPPORT.name()));
            ret.putInt(SettingsDefine.Common.Switch.PANORAMIC_CONN_TYPE.name(), factory.getInt(SettingsDefine.Common.Switch.PANORAMIC_CONN_TYPE.name()));
            ret.putInt(SettingsDefine.Common.Switch.PANORAMIC_VIDEO_TYPE.name(), factory.getInt(SettingsDefine.Common.Switch.PANORAMIC_VIDEO_TYPE.name()));
            ret.putInt("ONE_PANORAMIC", factory.getInt("ONE_PANORAMIC"));
            ret.putInt("avdd", factory.getInt("avdd"));

            ret.putBoolean(SettingsDefine.Common.Switch.DVR_SUPPORT.name(), factory.getBoolean(SettingsDefine.Common.Switch.DVR_SUPPORT.name()));

            ret.putInt(SettingsDefine.ScreenParam.TYPE_BRIGHTNESS, converData(mBrightnessMax, factory.getInt(SettingsDefine.ScreenParam.TYPE_BRIGHTNESS)));
            ret.putInt(SettingsDefine.ScreenParam.TYPE_COLOUR_TEMP, converData(mGammaMax, factory.getInt(SettingsDefine.ScreenParam.TYPE_COLOUR_TEMP)));
            ret.putInt(SettingsDefine.ScreenParam.TYPE_CONTRAST, converData(mContrastMax, factory.getInt(SettingsDefine.ScreenParam.TYPE_CONTRAST)));
            ret.putInt(SettingsDefine.ScreenParam.TYPE_SATURATION, converData(mSaturationMax, factory.getInt(SettingsDefine.ScreenParam.TYPE_SATURATION)));
            ret.putInt(SettingsDefine.ScreenParam.TYPE_DYNAMICCONTRAST, converData(mDynamiccontrastMax, factory.getInt(SettingsDefine.ScreenParam.TYPE_DYNAMICCONTRAST)));
            ret.putInt(SettingsDefine.ScreenParam.TYPE_SHARPNESS, converData(mSharpness, factory.getInt(SettingsDefine.ScreenParam.TYPE_SHARPNESS)));
            ret.putInt(SettingsDefine.ScreenParam.TYPE_AVDD, factory.getInt(SettingsDefine.ScreenParam.TYPE_AVDD));
            ret.putInt(SettingsDefine.ScreenParam.TYPE_VCOM, factory.getInt(SettingsDefine.ScreenParam.TYPE_VCOM));

            ret.putBoolean(SettingsDefine.Common.Switch.SUPPORT_PORT.name(), FactoryDriver.Driver().getSupportPort());

            ret.putInt("rotate_voltage", factory.getInt("rotate_voltage"));
            ret.putInt("rotate_time", factory.getInt("rotate_time"));

            ret.putBoolean("support_frong_camera", factory.getBoolean("support_frong_camera"));
            ret.putInt("front_camera_time", factory.getInt("front_camera_time"));
            ret.putBoolean("show_online_upgrade", factory.getBoolean("show_online_upgrade"));
            ret.putInt("blue_mic_gain", factory.getInt("blue_mic_gain"));

            ret.putInt("MONITOR_SWITCH", factory.getInt("MONITOR_SWITCH"));

            ret.putBoolean("zhi_neng_tong", factory.getBoolean("zhi_neng_tong"));//锐派竖屏智能通开关
            ret.putBoolean("video_demo_icon", factory.getBoolean("video_demo_icon"));//锐派竖屏国六视频开关
        }

        Packet common = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getInfo();
        if (common != null) {
            /** 收音机区域初始化 */
            ret.putBoolean(SettingsDefine.Common.Switch.AUTO_LAND_PORT.value(), common.getBoolean(SettingsDefine.Common.Switch.AUTO_LAND_PORT.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.BRAKE_WARNING.value(), common.getBoolean(SettingsDefine.Common.Switch.BRAKE_WARNING.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.REVERSING_VOLUME.value(), common.getBoolean(SettingsDefine.Common.Switch.REVERSING_VOLUME.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.ANY_KEY.value(), common.getBoolean(SettingsDefine.Common.Switch.ANY_KEY.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.KEY_TONE.value(), common.getBoolean(SettingsDefine.Common.Switch.KEY_TONE.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.REVERSE_IMAGE.value(), common.getBoolean(SettingsDefine.Common.Switch.REVERSE_IMAGE.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.GPS_MONITOR.value(), common.getBoolean(SettingsDefine.Common.Switch.GPS_MONITOR.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.GPS_MIX.value(), common.getBoolean(SettingsDefine.Common.Switch.GPS_MIX.value()));
            ret.putInt(SettingsDefine.Common.Switch.GPS_MIX_RATIO.value(), common.getInt(SettingsDefine.Common.Switch.GPS_MIX_RATIO.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.SWITCH_MODE_NORETAIN_3PARTY.value(), common.getBoolean(SettingsDefine.Common.Switch.SWITCH_MODE_NORETAIN_3PARTY.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.MEDIA_JUMP.value(), common.getBoolean(SettingsDefine.Common.Switch.MEDIA_JUMP.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.COLORFUL_LIGHT_SWITCH.value(), common.getBoolean(SettingsDefine.Common.Switch.COLORFUL_LIGHT_SWITCH.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.SMALL_LIGHT_SWITCH.value(), common.getBoolean(SettingsDefine.Common.Switch.SMALL_LIGHT_SWITCH.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.FLICHER_SWITCH.value(), common.getBoolean(SettingsDefine.Common.Switch.FLICHER_SWITCH.value()));
            ret.putInt(SettingsDefine.Common.Switch.FLICHER_RATE.value(), common.getInt(SettingsDefine.Common.Switch.FLICHER_RATE.value()));
            ret.putInt(SettingsDefine.Common.Switch.COLORFUL_LIGHT_COLOR.value(), common.getInt(SettingsDefine.Common.Switch.COLORFUL_LIGHT_COLOR.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.REVERSE_GUIDE_LINE.value(), common.getBoolean(SettingsDefine.Common.Switch.REVERSE_GUIDE_LINE.value()));
            ret.putInt(SettingsDefine.Common.Switch.DEFAULT_SYSTEM_VOLUME.value(), common.getInt(SettingsDefine.Common.Switch.DEFAULT_SYSTEM_VOLUME.value()));
            ret.putInt(SettingsDefine.Common.Switch.DEFAULT_CALL_VOLUME.value(), common.getInt(SettingsDefine.Common.Switch.DEFAULT_CALL_VOLUME.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.GPS_MIX_SUPPORT.value(), common.getBoolean(SettingsDefine.Common.Switch.GPS_MIX_SUPPORT.value()));
            ret.putString(SettingsDefine.Common.Switch.GPRS_APK_NAME.name(), common.getString(SettingsDefine.Common.Switch.GPRS_APK_NAME.name()));
            ret.putString(SettingsDefine.Common.Switch.TMPS_APK_NAME.name(), common.getString(SettingsDefine.Common.Switch.TMPS_APK_NAME.name()));
            ret.putString(SettingsDefine.Common.Switch.DVR_APK_NAME.name(), common.getString(SettingsDefine.Common.Switch.DVR_APK_NAME.name()));
            ret.putInt("COLORFUL_LIGHT_COLOR_3PARTY", common.getInt("COLORFUL_LIGHT_COLOR_3PARTY"));
            ret.putBoolean(SettingsDefine.Common.Switch.KEY_SHAKE.value(), common.getBoolean(SettingsDefine.Common.Switch.KEY_SHAKE.value()));
            ret.putBoolean(SettingsDefine.Common.Switch.LIGHT_SENSITIVE.value(), common.getBoolean(SettingsDefine.Common.Switch.LIGHT_SENSITIVE.value()));
            ret.putBoolean("CAMERA_SWITCH_TRUCK", common.getBoolean("CAMERA_SWITCH_TRUCK"));
            ret.putBoolean("TURNLIGHT_SWITCH_LEFT", common.getBoolean("TURNLIGHT_SWITCH_LEFT"));
            ret.putBoolean("TURNLIGHT_SWITCH_RIGHT", common.getBoolean("TURNLIGHT_SWITCH_RIGHT"));
        }

        Packet navig = ModuleManager.getLogicByName(NaviDefine.MODULE).getModel().getInfo();
        if (navig != null) {
            /** 收音机区域初始化 */
            ret.putString(SettingsDefine.Navig.SELECTION, navig.getString(SettingsDefine.Navig.SELECTION));
            ret.putStringArray(SettingsDefine.Navig.SOURCES, navig.getStringArray(SettingsDefine.Navig.SOURCES));
        }

        InfoDriverable infoDriverable = InfoDriver.Driver();
        if (null != infoDriverable) {
            ret.putString("Info.IMEI", infoDriverable.getIMEI(getMainContext()));
            ret.putString("Info.IMEISV", infoDriverable.getIMEISV(getMainContext()));
            ret.putString("Info.IpAddress", infoDriverable.getIpAddress(getMainContext()));
            ret.putString("Info.Line1Number", infoDriverable.getLine1Number(getMainContext()));
            ret.putString("Info.SerialNumber", infoDriverable.getSerialNumber(getMainContext()));
            ret.putString("Info.SimCountryIso", infoDriverable.getSimCountryIso(getMainContext()));
            ret.putString("Info.SimOperator", infoDriverable.getSimOperator(getMainContext()));
            ret.putString("Info.SimSerialNumber", infoDriverable.getSimSerialNumber(getMainContext()));
            ret.putString("Info.SubscriberId", infoDriverable.getSubscriberId(getMainContext()));
            ret.putString("Info.VoiceMailAlphaTag", infoDriverable.getVoiceMailAlphaTag(getMainContext()));
            ret.putString("Info.VoiceMailNumber", infoDriverable.getVoiceMailNumber(getMainContext()));
            ret.putString("Info.WifiIpAddresses", infoDriverable.getWifiIpAddresses(getMainContext()));
            ret.putInt("Info.NetworkType", infoDriverable.getNetworkType(getMainContext()));
            ret.putInt("Info.PhoneType", infoDriverable.getPhoneType(getMainContext()));
            ret.putInt("Info.SimState", infoDriverable.getSimState(getMainContext()));
        }

        Packet voice = ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).getModel().getInfo();
        if (null != voice) {
            ret.putBoolean("ShowRecordButton", voice.getBoolean("ShowRecordButton"));
            ret.putInt("AudioType", voice.getInt("AudioType"));
            ret.putBoolean("isEnableWakeup", voice.getBoolean("EnableWakeup"));
            ret.putBoolean("FilterNoiseType", voice.getBoolean("FilterNoiseType"));
            ret.putBoolean("isEnableWholeCmd", voice.getBoolean("EnableWholeCmd"));
        }

        Packet volume = DriverManager.getDriverByName(VolumeDriver.DRIVER_NAME).getModel().getInfo();
        if (null != volume) {
            ret.putInt("VolumeType", volume.getInt("VolumeType"));
            ret.putIntegerObjectArray("VolumeValue", volume.getIntObjectArray("VolumeValue"));
            ret.putBoolean("VolumeMute", volume.getBoolean("VolumeMute"));
            ret.putInt("VolumeMax", volume.getInt("VolumeMax"));
            ret.putBoolean("VolumeShow", volume.getBoolean("VolumeShow"));
        }

        Packet tpTouch = DriverManager.getDriverByName(TpTouchDriver.DRIVER_NAME).getInfo();
        if (tpTouch != null) {
            ret.putInt(SettingsDefine.TPCoordinate.SWITCH, tpTouch.getInt(SettingsDefine.TPCoordinate.SWITCH));
        }

        return ret;
    }

    private int converData(int high, int low) {
        int ret = low;
        ret = ((high & 0xFF) << 16) | (low & 0xFFFF);
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new SettingsDriver();
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getModel().bindListener(mClientListener);
        DriverManager.getDriverByName(EQDriver.DRIVER_NAME).getModel().bindListener(mEQListener);
        DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getModel().bindListener(mCommonListener);
        DriverManager.getDriverByName(FactoryDriver.DRIVER_NAME).getModel().bindListener(mFactoryListener);
        DriverManager.getDriverByName(LanguageDriver.DRIVER_NAME).getModel().bindListener(mLanguageListener);
        DriverManager.getDriverByName(TpTouchDriver.DRIVER_NAME).getModel().bindListener(mTPTouchListener);
        DriverManager.getDriverByName(SteerDriver.DRIVER_NAME).getModel().bindListener(mSteerListener);
        DriverManager.getDriverByName(BacklightDriver.DRIVER_NAME).getModel().bindListener(mBacklightListner);

        // FIXME: 17-8-5 //每次启动尝试从外部存储设备导入配置
//        toastHandler = new ToastHandler();
        //  inputConfigList = loadConfigList();
        //  importConfig(5000L);
        //begin zhongyang.hu add for Vsync apk
        mContext =getMainContext();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_CONFIG_FINISH);
        mContext.registerReceiver(mVsyncBroadcastReceiver, myIntentFilter);
        //end

        MiraVisionJni.nativeSetPictureMode(MiraVisionJni.PIC_MODE_USER_DEF);
        //对比度
        mContrastMax = MiraVisionJni.getContrastIndexRange().max;
        //饱和度
        mSaturationMax = MiraVisionJni.getSaturationIndexRange().max;
        //亮度
        mBrightnessMax = MiraVisionJni.getPicBrightnessIndexRange().max;
        //色温
        mGammaMax = MiraVisionJni.getGammaIndexRange().max;
        //动态对比
        mDynamiccontrastMax = MiraVisionJni.getDynamicContrastIndexRange().max;
        //锐度
        mSharpness = MiraVisionJni.getSharpnessIndexRange().max;
        LogUtils.i(TAG, "mContrastMax=" + mContrastMax + ", mSaturationMax=" + mSaturationMax + ", mBrightnessMax=" + mBrightnessMax +
                ", mGammaMax=" + mGammaMax + ", mDynamiccontrastMax=" + mDynamiccontrastMax + ", mSharpness=" + mSharpness);
        initPQParam();
    }

    //begin zhongyang.hu add for Vsync apk
    private BroadcastReceiver mVsyncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "mVsyncBroadcastReceiver action = " + action);
            if (ACTION_CONFIG_FINISH.equals(action)) {
                TpTouchDriver.Driver().reloadConfig();
                FactoryDriver.Driver().reloadConfig();
                setTouchStudyState(FINISH);
                Driver().setImportStatus(true);
                //begin zhongyang.hu add for vsync 20190426
                Packet packet = new Packet();
                packet.putString("message", getMainContext().getString(R.string.input_vsync_success));
                Notify(false, MainUIDefine.MODULE, MainUIInterface.MAIN_TO_APK.SHOW_DIALOG, packet);
                //end
                mContext.sendBroadcast(new Intent(ACTION_IMPORT_CONFIG_SUCCESSED));
            }
        }
    };
    //end


//begin zhongyang_usb for mcu cannot auto update,when OSupdate.20180814
    public void checkUsbConfig(){

        if(hasImportOne){
            LogUtils.e(TAG, "checkUsbConfig need not to do import work.." );
            return;
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LogUtils.e(TAG, "checkUsbConfig  boot_complete..10s is comoeimg." );

                if(hasImportOne){
                    LogUtils.e(TAG, "checkUsbConfig need not to do import work..10s late" );
                    return;
                }
                for(int i = 0;i < usbStrArray.length; i++){
                    File tmp =new File(usbStrArray[i]+IMPORT_DIR);
                    if(tmp.exists()){
                        importConfig(usbStrArray[i]);
                        break;
                    }
                    //begin zhongyang.hu add for vsync 20190426
                    tmp =new File(usbStrArray[i]+ SettingsLogic.VSYNC_PATH);
                    if(tmp.exists()){
                        importConfig(usbStrArray[i]);
                        break;
                    }
                    //end
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task,20000);

    }
//end
    public void importConfig(String path) {
        hasImportOne = true;
        if (AccoffListener.AccoffStep.isAccoff(getAccoffStep())) {
            LogUtils.e(TAG, "is accoff state, no need importConfig");
            return;
        }
        LogUtils.i(TAG, "import config start");
        if (!isImporting) {
            importThread = new Thread(new ConfigImportRunnable(path));
            importThread.start();
        }
    }

    private Thread importThread;
    private static boolean isImporting = false;

    private static boolean importingwork = false;

    //user for, has receiver a media mounted broad cast,
    private static boolean hasImportOne = false;

    private final String IMPORT_DIR = "import_CustomConfig/";
    private final String IMPORT_DES = "/custom/";
    private final String CUSTOM_WALLPAPER = "wallpaper";
    private final long MAX_WALLPAPER_SIZE = 1000 * 1000 * 10;
    private Object eqSendFinished = new Object();

    private final String UPDATE_FILE = "wwc2_update.zip";
    private final String ID_DIRTY = "/cache/id_dirty.txt";
    private final String ID_CHECK = "/cache/id_check.txt";
    private final String UPDATE_COPY = "/storage/sdcard0/wwc2_update.zip";


    private final String EMULATED0 = "/storage/emulated/0/";

    private final byte NOT_IMPORT = 0x00;
    private final byte IMPORT_ING = 0x01;
    private final byte IMPORT_OK = 0x02;
    private final byte IMPORT_ERR = 0x03;

    private byte importState = NOT_IMPORT;

    //begin zhongyang.hu add for send MCU state 20180206
    public boolean IsImport() {
        return importingwork;
    }

    public void sendImportState() {
        LogUtils.e(TAG, "sendImportState  ...importState." + importState);
        McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_MCU_IMPORT_STATE, new byte[]{importState}, 1);
    }
    //end


    public boolean needOsUpdate(String disk) {
        //begin zhongyang.hu add for u disk auto update  20171123
        String updatePath = disk + IMPORT_DIR + UPDATE_FILE;
        boolean need_update = true;
        File ufile = new File(updatePath);

        LogUtils.e(TAG, "begin needOsUpdate ...." + updatePath);

        if (ufile.exists()) {
            long size = ufile.length();
            try {
                File cfile = new File(ID_CHECK);
                if (cfile.exists()) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ID_CHECK)));
                    String oldModifytime = br.readLine();
                    String oldSize = br.readLine();
                    br.close();
                    LogUtils.i(TAG, "this ID_CHECK file oldSize ..." + oldSize);
                    if (Integer.parseInt(oldSize) == size) {
                        need_update = false;
                        LogUtils.i(TAG, "this file has been update ...");
                    }
                }
            } catch (Exception e) {
                LogUtils.i(TAG, "read ID_CHECK  Exception...");
            }
        } else {
            LogUtils.e(TAG, " needOsUpdate ...." + updatePath + "is  not exists");
            need_update = false;
        }
        return need_update;
    }

    public boolean isUpdateExists(String disk) {
        String updatePath = disk + IMPORT_DIR + UPDATE_FILE;
        File ufile = new File(updatePath);
        if (ufile.exists()) {
            return true;
        }
        return false;
    }

    public boolean OsUpdateZip(String disk) {
        //begin zhongyang.hu add for u disk auto update  20171123
        String updatePath = disk + IMPORT_DIR + UPDATE_FILE;
        File ufile = new File(updatePath);


        long size = ufile.length();
        long modifytime = ufile.lastModified() / 1000;

        LogUtils.e(TAG, "begin OsUpdateZip ...." + updatePath + "size= " + size);
        //update the ID_DIRTY file.
        try {
            File dfile = new File(ID_DIRTY);
            if (dfile.exists()) {
                dfile.delete();
            }

            dfile.createNewFile();
            Runtime.getRuntime().exec("chmod 666 /cache/id_dirty.txt");
            FileWriter fw = new FileWriter(dfile, false);
            fw.write(String.valueOf(modifytime));
            fw.write("\n");
            fw.write(String.valueOf(size));
            fw.write("\n");
            fw.flush();
            fw.close();
        } catch (Exception e) {
            LogUtils.i(TAG, " write ID_DIRTY  Exception...");
        }

        //goto recovery update.
        long freeSpace = FileUtil.getAvailableSize("/storage/sdcard0/");
        if (freeSpace > 800) {
            try {
                FileUtil.copyFile(updatePath, UPDATE_COPY);
            } catch (IOException e) {
                LogUtils.i(TAG, " copyFile updatePath  Exception...");
                FileUtil.deleteFromName(UPDATE_COPY);
                return false;
            }
            LogUtils.i(TAG, " copyFile updatePath  success...");
            BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
            SystemProperties.set("persist.sys.mcu_update_finish", "false");

            //String updateFinish= SystemProperties.get("persist.sys.mcu_update_finish","false");
            //LogUtils.i(TAG, " updateFinish==" + updateFinish);
            //begin zhongyang.hu add for FOTA updata maybe twice .20190813
            byte[] data = new byte[1];
            data[0] = (byte) 0x02;
            McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.RPT_CPU_Enter_Recover, data, 1);
            //end
            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.OS_UPDATE, null);
            return true;
        } else {
            return false;
        }
    }

    int getMCUUpdateFinishState() {
        int ret = MCU_STAT_FINISH;
        String UPDATE_PATH = "/system/mcu_update_bin/";
        String updateFile = PowerManager.findFile(UPDATE_PATH, "^CM_([0-9]{2}.){2}[0-9]{2}_[0-9]{2}.[0-9]{2}.bin$");
        if (updateFile != null) {
            String updateVersion = updateFile.substring(0, updateFile.length() - 4);
            VersionDriverable driverable = VersionDriver.Driver();
            String CurrentVersion = driverable.getMcuVersion();
            if ("Unknown".equals(CurrentVersion)) {
                ret = MCU_STAT_UNKNOWN;
            } else if (updateVersion.equals(CurrentVersion)) {
                ret = MCU_STAT_FINISH;
            } else {
                ret = MCU_STAT_UNFINISH;
            }
        }
        return ret;
    }
    //end zhongyang.hu add for u disk auto update  20171123

    //begin zhongyang.hu add for updata mcubin form Customconfig file. 20180521
    // return true: is update MCU....
    boolean checkAndUpdateMcu(String disk){
       boolean ret = false;
       String mcu_path =disk + IMPORT_DIR + "mcubin";
        String mcubin = PowerManager.findFile(mcu_path, "^CM_([0-9]{2}.){2}[0-9]{2}_[0-9]{2}.[0-9]{2}.bin$");
        if (mcubin != null) {
            String mcuVersion = mcubin.substring(0, mcubin.length() - 4);
            VersionDriverable driverable = VersionDriver.Driver();
            String CurrentVersion = driverable.getMcuVersion();
            LogUtils.i(TAG, "CurrentVersion = " + CurrentVersion + " new mcuVersion = "+ mcuVersion );
            if (!mcuVersion.equals(CurrentVersion)) {
                Intent it = new Intent(Intent.ACTION_MAIN);
                it.addCategory(Intent.CATEGORY_LAUNCHER);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                it.putExtra("MCU_BIN_PATH", mcu_path+"/"+ mcubin);
                it.setComponent(new ComponentName("com.wwc2.mcuupdate", "com.wwc2.mcuupdate.activity.UpdateActivity"));
                getMainContext().startActivity(it);
                ret = true;
            }
        }
      return  ret;
    }
    //end

    private List<String> loadConfigList() {
        List<String> list = new ArrayList<>();
        FileInputStream reader = null;
        try {
            XmlPullParser xpp = Xml.newPullParser();
            reader = new FileInputStream(OUTPUT_CONFIG_LIST_XML);
            xpp.setInput(reader, "UTF-8");
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tagName = xpp.getName();
                        if (FILE_NAME.equals(tagName)) {
                            String name = xpp.nextText();
                            list.add(name);
                            LogUtils.d(TAG, name);
                        }
                        break;
                    default:
                        break;
                }
                eventType = xpp.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (RuntimeException rethrown) {
                    throw rethrown;
                } catch (Exception ignored) {
                }
            }
        }

        return list;
    }
 //begin zhongyang.hu add the dest path in ResConfigList.xml.
   class PathBean{
       public String mSrc;
       public String mDes;
       PathBean(String src,String des){
           this.mSrc  =src;
           this.mDes =des;
       }
   }
//end
    private boolean loadResFile(String path) {
        boolean ret = true;
        List<PathBean> list = new ArrayList<>();

        FileInputStream reader = null;
        try {
            XmlPullParser xpp = Xml.newPullParser();
            reader = new FileInputStream(path + RES_CONFIG_XML);
            xpp.setInput(reader, "UTF-8");
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tagName = xpp.getName();
                        if (FILE_NAME.equals(tagName)) {
                            String src = xpp.getAttributeValue(null, SRC_PATH);
                            String des = xpp.getAttributeValue(null,DES_PATH);
                            list.add(new PathBean(src,des));
                            LogUtils.d(TAG, "src = " + src  +" des= " +des);
                        }
                        break;
                    default:
                        break;
                }
                eventType = xpp.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (RuntimeException rethrown) {
                    throw rethrown;
                } catch (Exception ignored) {
                }
            }
        }

        if (list != null && !list.isEmpty()) {
            for (PathBean config : list) {
                LogUtils.d(TAG, "src = " + config.mSrc+ " dest = "  + config.mDes);
                try {
                    FileUtil.copyDirectory(new File(path + config.mSrc), new File(config.mDes));
                } catch (IOException e) {
                    System.err.println("SettingsLogic Could not copy resources from " + config + " to " + EMULATED0 +
                            ": " + e.getLocalizedMessage());
                    ret = false;
                }
            }
        }
        return ret;
    }

    public void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
    //zhongyanghu for String importDir = disk + IMPORT_DIR;
    private void startVsync(String dist){
        if (mContext != null && ApkUtils.isAPKExist(mContext, "com.wwc2.vsync")) {
            Intent it = new Intent(Intent.ACTION_MAIN);
            it.addCategory(Intent.CATEGORY_LAUNCHER);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            it.setComponent(new ComponentName("com.wwc2.vsync", "com.wwc2.vsync.VSyncActivity"));
            it.putExtra("dist_path", dist);
            Log.d(TAG, "start VSyncActivity  dist_path =" + dist);
            mContext.startActivity(it);
        }
    }
    //end
    private class ConfigImportRunnable implements Runnable {
        private String path;

        public ConfigImportRunnable(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            isImporting = true;

            if (TextUtils.isEmpty(path)) {
                LogUtils.i(TAG, "no dist mounted to import config");
                isImporting = false;
                return;
            }

            // import js custom id file from USB, only once  --- HuangZeming add 20200429
            importJsCustomId(path);

            // import AispeechAuthkey from USB into /custom/hotword/ dir --- HuangZeming add 20200826
            importAispeechAuthKey(path);

            // import gms voice custom id file from USB, only once  --- HuangZeming add 20200628
            importLiteCustomId(path);

            // output sn list, only once --- HuangZeming add 20200704
            outputSN(path);

            String importDir = path + IMPORT_DIR;
            File importdir = new File(importDir);
            if (!importdir.exists()) {
                LogUtils.i(TAG, " import config is not exit");
                isImporting = false;
                //zhongyanghu for String importDir = path + IMPORT_DIR;
                String vSyncString = path + VSYNC_PATH;
                File vSyncFile = new File(vSyncString);
                if(vSyncFile.exists()){
                    startVsync(path);
                }
                //end
                return;
            }
            //zhongyang.hu add for copy update.zip
            LogUtils.i(TAG, "begin  check needOsUpdate...");
            importState = IMPORT_ING;
            importingwork = true;
            McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_MCU_IMPORT_STATE, new byte[]{importState}, 1);

            if (needOsUpdate(path)) {
                if (toastHandler != null) {
                    toastHandler.sendEmptyMessage(UPDATE_OS);
                }
                if (OsUpdateZip(path)) {
                    LogUtils.i(TAG, "entry OsUpdateZip OK..");
                    importingwork = false;
                    isImporting = false;
                    return;
                } else {
                    LogUtils.i(TAG, "entry OsUpdateZip fail..");
                    importState = IMPORT_ERR;
                    McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_MCU_IMPORT_STATE, new byte[]{importState}, 1);
                    isImporting = false;
                    importingwork = false;
                    return;
                }
            }
            //end
            if (isUpdateExists(path)) {
                int i = 0;
                while (getMCUUpdateFinishState() == MCU_STAT_UNKNOWN && i++ < 10) {
                    sleep(1000);
                    LogUtils.i(TAG, " isMCUUpdate Finish MCU_STAT_UNKNOWN i = " + i);
                }
                int sta = getMCUUpdateFinishState();
                if (sta == MCU_STAT_UNFINISH) {
                    if (toastHandler != null) {
                        toastHandler.sendEmptyMessage(MCU_NOT_MATCH);
                    }
                    isImporting = false;
                    importingwork = false;
                    return;
                } else if (sta == MCU_STAT_UNKNOWN) {
                    LogUtils.i(TAG, " isMCUUpdate get MCU failed !! ");
                }
            }

            //zhongyang.hu add for mcu cannot upgrade auto  20180820
           int i=0;
            String  currentversion = VersionDriver.Driver().getMcuVersion();
            int state= PowerManager.getPowerStep();

            while(("Unknown".equals(currentversion) ||
                    state < PowerManager.PowerStep.POWER_ON_OVER ) || i < 10 ){
                LogUtils.i(TAG, " wait get MCU verison   i= " + i  +"currentversion = " +currentversion);
                try {
                    i++;
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentversion = VersionDriver.Driver().getMcuVersion();
                state= PowerManager.getPowerStep();

                if(i > 25){
                    break;
                }
            }
            //end

            //begin zhongyang.hu add for updata mcubin form Customconfig file. 20180521
             if(checkAndUpdateMcu(path)){
                 LogUtils.i(TAG, " need update mcu bin in CustomConfig");
                 isImporting =false;
                 importingwork = false;
                 return;
             }
            //end
            if (!loadResFile(importDir)) {
                LogUtils.i(TAG, "loadResFile error!");
                importState = IMPORT_ERR;
                McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_MCU_IMPORT_STATE, new byte[]{importState}, 1);
            }

            inputConfigList = loadConfigList();
            if (inputConfigList != null && !inputConfigList.isEmpty()) {
                LogUtils.e(TAG, "导入配置文件....");

                StringBuilder nonexistList = new StringBuilder();

                //拷贝列表中的文件
                boolean importError = false;
                for (String config : inputConfigList) {
                    String configPath = path + IMPORT_DIR + config;
                    if (!FileUtil.checkExist(configPath)) {
                        importError = true;
                        nonexistList.append("\n").append(config);
                        continue;
                    }
                    try {
                        FileUtil.copyFile(configPath, IMPORT_DES + config);
                    } catch (IOException e) {
                        importError = true;
                        e.printStackTrace();
                        LogUtils.e(TAG, e.toString());
                        continue;
                    }
                }

                //写tp升级节点,开始tp固件升级
//                FileUtil.write("1", TP_UPDATE_NODE);//在旋转屏项目会引起机器重启，没有用到，直接去掉。2019-09-20
                //导入壁纸
                inputWallpaper(path);

                //拷贝EQDataConfig.ini配置文件
                String eqConfig = path + IMPORT_DIR + "EQDataConfig.ini";
                if(FileUtil.checkExist(eqConfig)) {
                    try {
                        FileUtil.copyFile(eqConfig, IMPORT_DES + "EQDataConfig.ini");
                        LogUtils.e(TAG, "导入EQ配置文件....");
                        Driver().setImportEQState(true);
                        LogUtils.e(TAG, "EQ配置发送数据完毕");
                    } catch (IOException e) {
                        importError = true;
                        e.printStackTrace();
                        LogUtils.e(TAG, e.toString());
                    }
                }
                //导入出错则返回，不提示成功
                if (importError) {
                    importState = IMPORT_ERR;
                    toastHandler.sendMessage(toastHandler.obtainMessage(IMPORT_ERROR, nonexistList.toString()));
                } else {
                    toastHandler.sendEmptyMessage(IMPORT_SUCCESS);
                }
            }

            if (importState != IMPORT_ERR) {
                importState = IMPORT_OK;
            }
            LogUtils.e(TAG, "ConfigImportRunnable  ...importState." + importState);
            McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_MCU_IMPORT_STATE, new byte[]{importState}, 1);
            isImporting = false;
            importingwork = false;
        }
    }

    private void inputWallpaper(String path) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getMainContext());
        String wallpaperPath = path + IMPORT_DIR + CUSTOM_WALLPAPER;
        File wallpaperFile = new File(wallpaperPath);
        if (!wallpaperFile.exists()) {
            return;
        }
        if (wallpaperFile.length() > MAX_WALLPAPER_SIZE) {
            LogUtils.e("壁纸图片过大,禁止导入");
            return;
        }
        try {
            wallpaperManager.setStream(new FileInputStream(wallpaperPath));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //[begin] HuangZeming add for import JS new custom id --- 20200429
    private void importJsCustomId(String path) {
        String extJsCustomIdPath = path + "custom_id";
        String intJsCustomIdPath = "/custom/custom_id";
        String coverJsCustomIdPath = path + "/sec5896/.cover_custom_id";

        //LogUtils.d("Ext files:" + extJsCustomIdPath + "Int files:" + intJsCustomIdPath);
        File intJsCustomIdFile = new File(intJsCustomIdPath);
        File extJsCustomIdFile = new File(extJsCustomIdPath);
        File coverJsCustomIdFile = new File(coverJsCustomIdPath);
        if(!intJsCustomIdFile.exists() && extJsCustomIdFile.exists()) {
            // copy id from out device to /custom/
            try {
                FileUtil.copyFile(extJsCustomIdPath, intJsCustomIdPath);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.toString());
            }
        } else {
            LogUtils.d("--- The file is imported. will check cover file--- ");
        }

        if(intJsCustomIdFile.exists() && coverJsCustomIdFile.exists()) {
            LogUtils.d("--- cover file exists--- ");
            try {
                FileUtil.copyFile(coverJsCustomIdPath, intJsCustomIdPath);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.toString());
            }
        }
    }
    //[end] for js custom id

    //[begin] HuangZeming add for import Lite new custom id, gms aios voice--- 20200628
    private void importLiteCustomId(String path) {
        String extLiteCustomIdPath = path + "lite_custom_id";
        String intLiteCustomIdPath = "/custom/lite_custom_id";
        String coverLiteCustomIdPath = path + "/sec9896/.cover_lite_custom_id";

        //LogUtils.d("Ext files:" + extLiteCustomIdPath + "Int files:" + intLiteCustomIdPath);
        File intLiteCustomIdFile = new File(intLiteCustomIdPath);
        File extLiteCustomIdFile = new File(extLiteCustomIdPath);
        File coverLiteCustomIdFile = new File(coverLiteCustomIdPath);
        if(!intLiteCustomIdFile.exists() && extLiteCustomIdFile.exists()) {
            // copy id from out device to /custom/
            try {
                FileUtil.copyFile(extLiteCustomIdPath, intLiteCustomIdPath);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.toString());
            }
        } else {
            LogUtils.d("--- The file is imported. will check cover file--- ");
        }

        if(intLiteCustomIdFile.exists() && coverLiteCustomIdFile.exists()) {
            LogUtils.d("--- cover file exists--- ");
            try {
                FileUtil.copyFile(coverLiteCustomIdPath, intLiteCustomIdPath);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.toString());
            }
        }
    }
    //[end] for Lite custom id

    //[begin] HuangZeming add for AiSpeech Key import 20200805
    /*********************************************************
     * step1 - check USB/hotword exist;
     * step2 - exist, check any key files exist in /USB/hotword folder;
     * step3 - if record file exist, there is one key imported /custom/hotword/ before. return
     * step4 - if record does not exist, mv one key file from /USB/hotword/ to /custom/hotword/, will create /custom/hotword/ dir;
     * step5 - delete the key file which in the /USB/hotword/
     * step6 - write record timestamp & key name into /custom/recordhotword
     */
    public static String BASE_FILE = "hotword";
    private void importAispeechAuthKey(String path) {
        String srcDirPath = path + "/" + BASE_FILE;
        String desDirPath = "/custom/" + BASE_FILE;
        String recordFilePath = "/custom/" + "record" + BASE_FILE;
        File srcDir = new File(srcDirPath);
        String infoMsg = null;

        // step1
        if(srcDir.exists()) {
            File[] keyArray = srcDir.listFiles();
            // step2
            if(keyArray.length > 0) {
                // step3 - check /custom/recordhotword file
                File recordFile = new File(recordFilePath);
                if(!recordFile.exists()) {
                    //step4 - cp keyArray[0] to desDirPath
                    String selectFileName = keyArray[0].getName();
                    String srcFilePath = srcDirPath + "/" + selectFileName;
                    String desFilePath = desDirPath + "/gw_key";

                    File desDir = new File(desDirPath);
                    if (!desDir.exists()) {
                        FileUtil.CreateDirectory(desDirPath);
                    }

                    try {
                        FileUtil.copyFile(srcFilePath, desFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, e.toString());
                    }

                    //step5 - del srcFilePath
                    FileUtil.deleteFromName(srcFilePath);

                    //step6 - write record files
                    int stoneLen = selectFileName.length() / 4;
                    String recordText = "" + new Date() + " File:" + selectFileName.substring(0,stoneLen) + "****" 
                                           + selectFileName.substring(selectFileName.length()-stoneLen, selectFileName.length());
                    FileUtil.write(recordText, recordFilePath);

                    infoMsg = "[Tips]\nimport success! \n" + recordText + " \nleft key files:" + (keyArray.length - 1);
                } else {
                    String oldInfo = "";
                    try {
                        oldInfo = FileUtil.readTextFile(recordFilePath);
                    } catch (Exception e) {
                        LogUtils.d("---  Read Data info from " + recordFilePath + " fail!");
                    }

                    infoMsg = "[Tips]\nimport fail! \nThis device has been imported at " + oldInfo;
                } // (!recordFile.exist()) 
            } else {
                LogUtils.d(srcDir + " is exist, but NULL  hzm");

                infoMsg = "[Tips]\nimport fail! \n" + srcDirPath + " is empty folder";
            } // (keyArray.length > 0)

            // send infoMsg to MainUI
            Packet packet = new Packet();
            packet.putString("button", "ok");
            packet.putString("message", infoMsg);
            Notify(false, MainUIDefine.MODULE, MainUIInterface.MAIN_TO_APK.SHOW_DIALOG, packet);
        } else {
            LogUtils.d(srcDir + " do not exist! hzm");
        } // (srcDir.exists())
    }
    //[end] for AiSpeech Key import

    // HuangZeming add for output SN --- 20200704
    // One device will only output once for one mark
    public static String MARK_FILE = "/KLD";
    public static String OutputSnBC = "com.wwc2.outputSN";
    private void outputSN(String path) {
        String markPath = path + MARK_FILE;
        String outputSnPath = markPath + "/" + "snlist.txt";
        String recordPath = "/custom" + MARK_FILE;
        String infoMsg = null;

        File markFile = new File(markPath);
        File recordFile = new File(recordPath);
        if (markFile.exists()) {
            if (!recordFile.exists()) {
                LogUtils.d("---  outputSN recordPath=" + recordPath + " not exist");
                // This is the first time to output
                outputSn(outputSnPath);
                writeRecord(recordPath);

                infoMsg = getMainContext().getString(R.string.sn_input_success) + new Date() +
                        getMainContext().getString(R.string.sn_tips);
            } else {
                String oldTime = "";
                try {
                    oldTime = FileUtil.readTextFile(recordPath);
                } catch (Exception e) {
                    LogUtils.d("---  outputSN Read Data info from " + recordPath + " fail!");
                }

                LogUtils.d("---  outputSN recordPath=" + recordPath + " exist. oldTime = " + oldTime);
                // no mark file or has been output, send broadcast to display
                infoMsg = getMainContext().getString(R.string.sn_input_fail) + oldTime +
                        getMainContext().getString(R.string.sn_tips);
            }
            /// send broadcast to MainUI, display info. pop dialog screen

//            Intent intent = new Intent(OutputSnBC);
//            intent.putExtra("infoMsg", infoMsg + headMsg);
//            getMainContext().sendBroadcast(intent);
            Packet packet = new Packet();
            packet.putString("button", "ok");
            packet.putString("message", infoMsg);
            Notify(false, MainUIDefine.MODULE, MainUIInterface.MAIN_TO_APK.SHOW_DIALOG, packet);
        } else {
            LogUtils.d("---  outputSN markPath=" + markPath + " not exist!");
        }// markFile.exists
    }

    // output sn into outputPath
    private boolean outputSn(String outputPath) {
        String ret = null;
        InfoDriverable infoDriverable = InfoDriver.Driver();
        if (null != infoDriverable) {
                ret = infoDriverable.getSerialNumber(getMainContext());
            }

        FileUtil.writeAppend(ret + "\n", outputPath);
        return true;
    }

    private boolean writeRecord(String recordPath) {
        LogUtils.d(TAG, "writeRecord----recordPath=" + recordPath);
        FileUtil.write("" + new Date(), recordPath);
        return true;
    }
    // HuangZeming add for output SN

//    private class ToastHandler extends Handler {
    private Handler toastHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            switch (msg.what) {
                case IMPORT_SUCCESS:
//                    ToastUtil.show(getMainContext(), R.string.input_success);
                    Packet packet = new Packet();
                    packet.putString("message", getMainContext().getString(R.string.input_success));
                    Notify(false, MainUIDefine.MODULE, MainUIInterface.MAIN_TO_APK.SHOW_DIALOG, packet);
                    TpTouchDriver.Driver().reloadConfig();
                    FactoryDriver.Driver().reloadConfig();
                    setTouchStudyState(FINISH);
                    Driver().setImportStatus(true);
                    getMainContext().sendBroadcast(new Intent(ACTION_IMPORT_CONFIG_SUCCESSED));
                    break;
                case IMPORT_ERROR:
//                    ToastUtil.show(getMainContext(), getMainContext().getString(R.string.input_success_partly) + msg.obj);
                    Packet packet1 = new Packet();
                    packet1.putString("message", getMainContext().getString(R.string.input_success));
                    LogUtils.e("un imported file list:" + msg.obj);
                    Notify(false, MainUIDefine.MODULE, MainUIInterface.MAIN_TO_APK.SHOW_DIALOG, packet1);

                    TpTouchDriver.Driver().reloadConfig();
                    FactoryDriver.Driver().reloadConfig();
                    setTouchStudyState(FINISH);
                    //modify by huwei 180426. error表示有部分文件导入失败,仍需要立即读取已导入的配置文件
                    Driver().setImportStatus(true);
                    getMainContext().sendBroadcast(new Intent(ACTION_IMPORT_CONFIG_SUCCESSED));
                    break;
                case SAME_KEY:
                    ToastUtil.show(getMainContext(), R.string.same_importkey);
                    break;
                case UPDATE_OS:
                    ToastUtil.show(getMainContext(), R.string.update_os);
                    break;
                case MCU_NOT_MATCH:
                    ToastUtil.show(getMainContext(), R.string.mcu_not_match);
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        DriverManager.getDriverByName(BacklightDriver.DRIVER_NAME).getModel().unbindListener(mBacklightListner);
        DriverManager.getDriverByName(SteerDriver.DRIVER_NAME).getModel().unbindListener(mSteerListener);
        DriverManager.getDriverByName(TpTouchDriver.DRIVER_NAME).getModel().unbindListener(mTPTouchListener);
        DriverManager.getDriverByName(LanguageDriver.DRIVER_NAME).getModel().unbindListener(mLanguageListener);
        DriverManager.getDriverByName(FactoryDriver.DRIVER_NAME).getModel().unbindListener(mFactoryListener);
        DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getModel().unbindListener(mCommonListener);
        DriverManager.getDriverByName(EQDriver.DRIVER_NAME).getModel().unbindListener(mEQListener);
        if (importThread != null && importThread.isAlive()) {
            importThread.interrupt();
        }
        try {
            getMainContext().unregisterReceiver(mVsyncBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        switch (nId) {
            case SettingsInterface.APK_TO_MAIN.SET_BACKLIGHT:
                if (null != packet) {
                    int value = packet.getInt(SettingsDefine.Brightness.BRIGHTNESS, -1);
                    if (-1 != value) {
                        BacklightDriver.Driver().setBacklightness(value, 0);
                    }
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SET_BACKLIGHT_DAY:
                if (null != packet) {
                    int value = packet.getInt(SettingsDefine.Brightness.BRIGHTNESS_DAY, -1);
                    if (-1 != value) {
                        BacklightDriver.Driver().setBacklightness(value, 1);
                    }
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SET_BACKLIGHT_NIGHT:
                if (null != packet) {
                    int value = packet.getInt(SettingsDefine.Brightness.BRIGHTNESS_NIGHT, -1);
                    if (-1 != value) {
                        BacklightDriver.Driver().setBacklightness(value, 2);
                    }
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SET_BACKLIGHT_MODE:
                if (null != packet) {
                    int mode = packet.getInt(SettingsDefine.Brightness.BRIGHTNESS_MODE, -1);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.BACKLIGHT_AUTO_ADJUST:
                BacklightDriver.Driver().setAuto(packet.getBoolean(SettingsDefine.Brightness.AOTUADJUST));
                break;

            case SettingsInterface.APK_TO_MAIN.STEER_ENTER_STUDY_MODE:
                SteerDriver.Driver().enterStudyMode();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SteerDriver.Driver().keyStudy();
                    }
                }, 50);
                break;

            case SettingsInterface.APK_TO_MAIN.STEER_EXIT_STUDY_MODE:
                SteerDriver.Driver().exitStudyMode();
                break;

            case SettingsInterface.APK_TO_MAIN.STEER_KEYS_VALUE:
                if (packet != null) {
                    SteerDriver.Driver().keyPressed((byte) (packet.getInt(SettingsDefine.SteerStudy.Extra.keyValue)));
                }
                break;

            case SettingsInterface.APK_TO_MAIN.STEER_KEYS_CLEAR:
                SteerDriver.Driver().keyClear();
                break;

            case SettingsInterface.APK_TO_MAIN.STEER_KEYS_RESET:
                SteerDriver.Driver().keyReset();
                break;

            case SettingsInterface.APK_TO_MAIN.STEER_KEYS_STORE:
                SteerDriver.Driver().keyStore();
                break;

            case SettingsInterface.APK_TO_MAIN.PANEL_ENTER_STUDY_MODE:
                SteerDriver.Driver().enterStudyMode_Panel();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SteerDriver.Driver().keyStudy_Panel();
                    }
                }, 50);
                break;

            case SettingsInterface.APK_TO_MAIN.PANEL_EXIT_STUDY_MODE:
                SteerDriver.Driver().exitStudyMode_Panel();
                break;

            case SettingsInterface.APK_TO_MAIN.PANEL_KEYS_VALUE:
                if (packet != null) {
                    SteerDriver.Driver().keyPressed_Panel((byte) (packet.getInt(SettingsDefine.SteerStudy.Extra.keyValue)));
                }
                break;

            case SettingsInterface.APK_TO_MAIN.PANEL_KEYS_CLEAR:
                SteerDriver.Driver().keyClear_Panel();
                break;

            case SettingsInterface.APK_TO_MAIN.PANEL_KEYS_RESET:
                SteerDriver.Driver().keyReset_Panel();
                break;

            case SettingsInterface.APK_TO_MAIN.PANEL_KEYS_STORE:
                SteerDriver.Driver().keyStore_Panel();
                break;

            case SettingsInterface.APK_TO_MAIN.SCREEN_ENTER_STUDY_MODE: {
                LogUtils.d(TAG, "tptouch bind position listener");
                TpTouchDriver.Driver().bindTPPositionListener(mTPPositionListener);
                TpTouchDriver.Driver().enterLearn();
                Notify(SettingsInterface.MAIN_TO_APK.SCREEN_KEYS_INIT, DriverManager.getDriverByName(TpTouchDriver.DRIVER_NAME).getModel().getInfo());
                break;
            }

            case SettingsInterface.APK_TO_MAIN.SCREEN_KEYS_RESET:
                TpTouchDriver.Driver().reset();
                Notify(SettingsInterface.MAIN_TO_APK.SCREEN_KEYS_INIT, DriverManager.getDriverByName(TpTouchDriver.DRIVER_NAME).getModel().getInfo());
                break;

            case SettingsInterface.APK_TO_MAIN.SCREEN_EXIT_STUDY_MODE:
                LogUtils.d(TAG, "tptouch unbind position listener");
                TpTouchDriver.Driver().leaveLearn();
                TpTouchDriver.Driver().unbindTPPositionListener(mTPPositionListener);
                break;

            case SettingsInterface.APK_TO_MAIN.SCREEN_KEYS_VALUE:
                if (packet != null) {
                    LogUtils.d(TAG, "tptouch screen key pressed");
                }
                break;

            /**<b>保存按键值和按键的扩展类型</b>*/
            case SettingsInterface.APK_TO_MAIN.SCREEN_KEYS_STORE:
                if (null != packet && -1 != mTpDownX && -1 != mTpDownY) {
                    int shortpress = Define.Key.KEY_NONE, longpress = Define.Key.KEY_NONE, continuepress = Define.Key.KEY_NONE;
                    int type = packet.getInt("Short", Define.Key.KEY_NONE);
                    if (Define.Key.KEY_NONE != type) {//short press
                        shortpress = type;
                    }

                    type = packet.getInt("Long", Define.Key.KEY_NONE);
                    if (Define.Key.KEY_NONE != type) {//short press
                        longpress = type;
                    }

                    type = packet.getInt("Continue", Define.Key.KEY_NONE);
                    if (Define.Key.KEY_NONE != type) {//short press
                        continuepress = type;
                    }

                    LogUtils.d(TAG, "keys" + " " + shortpress + " " + longpress + " " + continuepress);
                    TpTouchDriver.Driver().TPCode(mTpDownX, mTpDownY, shortpress, longpress, continuepress);
                    mTpDownX = mTpDownY = -1;
                }
                break;

            case SettingsInterface.APK_TO_MAIN.EQ_STYLE:
                EQDriver.Driver().setStyle(packet.getInt(SettingsDefine.EQ.STYLE));
                break;
            case SettingsInterface.APK_TO_MAIN.EQ_TYPE:
                EQDriver.Driver().setTypeValue(packet.getInt(SettingsDefine.EQ.TYPE), packet.getInt(SettingsDefine.EQ.VALUE));
                break;
            case SettingsInterface.APK_TO_MAIN.EQ_LOUDNESS:
                EQDriver.Driver().setLoudness(packet.getBoolean(SettingsDefine.EQ.LOUDNESS));
                break;
            case SettingsInterface.APK_TO_MAIN.SUBWOOFER:
                EQDriver.Driver().setSubwoofer(packet.getBoolean("SUBWOOFER_SWITCH"),packet.getInt("SUBWOOFER_FREQ",-1));
                break;
            case SettingsInterface.APK_TO_MAIN.SOUND_FIELD:
                if (packet != null) {
                    int[] coords = packet.getIntArray(SettingsDefine.SoundField.COORDS);
                    int[] gains = packet.getIntArray(SettingsDefine.SoundField.GAINS);
                    if (null != coords && coords.length == 2) {
                        EQDriver.Driver().setX(coords[SettingsDefine.SoundField.X]);
                        EQDriver.Driver().setY(coords[SettingsDefine.SoundField.Y]);
                    }
                    if (null != gains && gains.length == 4) {
                        //SoundDriver.Driver().setField(gains);
                        int[] values = new int[]{0, gains[0], gains[1], gains[2], gains[3]};
                        EQDriver.Driver().setField(values);
                    }
                }
                break;
            //dsp
            case SettingsInterface.APK_TO_MAIN.DSP_SOUNDEFFECT:
                if (packet != null) {
                    int index = packet.getInt("index");
                    int type = packet.getInt("type");
                    int value = packet.getInt("value");
                    EQDriver.Driver().setDspSoundEffects(index, type, value);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.DSP_PARAM:
                if (packet != null) {
                    int type = packet.getInt("type");
                    int value = packet.getInt("value");
                    EQDriver.Driver().setDspParam(type, value);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.DSP_SOUNDFILED:
                if (packet != null) {
                    int type = packet.getInt("type");
                    int value = packet.getInt("value");
                    EQDriver.Driver().setDspSoundField(type, value);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.DSP_RESET:
                if (packet != null) {
                    int value = packet.getInt("value");
                    EQDriver.Driver().resetDsp(value);
                }
                break;

            case SettingsInterface.APK_TO_MAIN.DSP_3DSWITCH://0x4a:
                if(packet != null) {
                    boolean value = packet.getBoolean("3DSWITCH");
                    EQDriver.Driver().set3D(value);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.DSP_OUTPUT://0x4b:
                EQDriver.Driver().outputDsp();
                break;
            case SettingsInterface.APK_TO_MAIN.DSP_HPFLPF://高通低通
                if (packet != null) {
                    int type = packet.getInt("type");
                    int value = packet.getInt("value");
                    EQDriver.Driver().setDspHpfLpf(type, value);
                }
                break;
            case 0x4D://77:
                if(packet != null) {
                    int value = packet.getInt("QVALUE");
                    EQDriver.Driver().setQValue(value);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.VOICE_FLOAT_STATE:
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.HIDE_SHOW_RECORD_BUTTON, packet);
                break;

            case SettingsInterface.APK_TO_MAIN.VOICE_MUSIC_SOURCE:
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.CHOOSE_SYSTEM_AUDIO, packet);
                break;

            case SettingsInterface.APK_TO_MAIN.VOICE_WAKEUP_SWITCH:
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.ENABLE_WAKEUP, packet);
                break;
            case SettingsInterface.APK_TO_MAIN.VOICE_WHOLE_CMD_SWITCH:
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.ENABLE_WHOLE_CMD, packet);
                break;
            case SettingsInterface.APK_TO_MAIN.VOICE_TONE_CANCELLATION_SWITCH:
                ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.FILTER_NOISE_TYPE, packet);
                break;

            case SettingsInterface.APK_TO_MAIN.LANGUAGE:
                if (packet != null) {
                    CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
                    if (logic != null) {
                        String lga = packet.getString(SettingsDefine.Language.LANGUAGE);
                        if (null != lga) {
                            Packet pak = new Packet();
                            pak.putString(com.wwc2.systempermission_interface.SystemPermissionDefine.ParseKey.LOCALE, lga);
                            logic.Notify(SystemPermissionInterface.MAIN_TO_APK.LOCALE, pak);
                        }
                    }
                }
                break;

            case SettingsInterface.APK_TO_MAIN.TIME_SET:
                if (packet != null) {
                    String source = packet.getString(Define.Time.RESOURCE);
                    final int year = packet.getInt(Define.Time.YEAR);
                    final int month = packet.getInt(Define.Time.MONTH);
                    final int day = packet.getInt(Define.Time.DAY);
                    final int hour = packet.getInt(Define.Time.HOURE);
                    final int minute = packet.getInt(Define.Time.MUNITE);
                    final int second = packet.getInt(Define.Time.SECOND);
                    DateTimeDriver.Driver().setTimeSource(source, year, month, day, hour, minute, second);
                }
                break;

            case SettingsInterface.APK_TO_MAIN.TIME_FORMAT:
                if (packet != null) {
                    String format = packet.getString(Define.Time.FORMAT);
                    DateTimeDriver.Driver().setTimeFormat(format);
                }
                break;

            case SettingsInterface.APK_TO_MAIN.FACTORY_MODULES: {
                if (packet != null) {
                    CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
                    if (logic != null) {
                        try {
                            String mdl = packet.getString(Define.Factory.MODULE);
                            Define.Factory.Modules modules = Define.Factory.Modules.valueOf(mdl);
                            boolean enable = packet.getBoolean(modules.value());
                            FactoryDriver.Driver().setModuleEnable(mdl, enable);

                            Packet factory = new Packet();
                            factory.putString(Define.Factory.MODULE, modules.value());
                            factory.putBoolean(modules.value(), enable);
                            BaseLogic baseLogic = ModuleManager.getLogicBySource(modules.source());
                            if (baseLogic != null) {
                                factory.putString("PacketName", baseLogic.getAPKPacketName());
                                factory.putString("ClassName", baseLogic.getAPKClassName());
                                logic.Notify(SystemPermissionInterface.MAIN_TO_APK.FACTORY_MODULES, factory);
                            }
                        } catch (IllegalArgumentException e) {

                        }
                    }
                }
                break;
            }

            case SettingsInterface.APK_TO_MAIN.REDIO_REGION:
                if (packet != null) {
                    FactoryDriver.Driver().setRadioRegion(packet.getString(Define.Factory.REGION));
                }
                break;

            case SettingsInterface.APK_TO_MAIN.RADIO_AERIAL:
                if (packet != null) {
                    FactoryDriver.Driver().setRadioAerial(packet.getBoolean(Define.Factory.RADIO_AERIAL));
                }
                break;

            case SettingsInterface.APK_TO_MAIN.RADIO_MODULE:
                if (packet != null) {
                    FactoryDriver.Driver().setRadioModule(packet.getInt(Define.Factory.RADIO_MODULE));
                }
                break;

            case SettingsInterface.APK_TO_MAIN.RDS_ENABLE:
                if (packet != null) {
                    FactoryDriver.Driver().setRDSEnable(packet.getBoolean("rds_enable"));
                }
                break;

            case SettingsInterface.APK_TO_MAIN.COMMON_SWITCH_GET:
                CommonDriver.Driver().getGpsAudioInfo();
                break;

            case SettingsInterface.APK_TO_MAIN.COMMON_SWITCH_SET:
                if (packet != null) {
                    String name = packet.getString(SettingsDefine.Common.SWITCH);
                    LogUtils.d(TAG, "COMMON_SWITCH_SET---name=" + name);
                    if (name != null) {
                        try {
                            if (name.equals("ONE_PANORAMIC")) {
                                FactoryDriver.Driver().setPanoramicType(packet.getInt(name));
                                return ret;
                            } else if(name.equals("COLORFUL_LIGHT_COLOR_3PARTY")) {
                                CommonDriver.Driver().setColorfulLightColor3Party(packet.getInt(name));
                                return ret;
                            } else if (name.equals("SUPPORT_FRONT_CAMERA")) {
                                FactoryDriver.Driver().setSupportFrontCamera(packet.getBoolean(name));
                                return ret;
                            } else if (name.equals("FRONT_CAMERA_TIME")) {
                                FactoryDriver.Driver().setFrontCameraTime(packet.getInt(name));
                                return ret;
                            } else if (name.equals("SHOW_ONLINE_UPGRADE")) {
                                FactoryDriver.Driver().setShowOnlineUpgrade(packet.getBoolean(name));
                                return ret;
                            } else if (name.equals("BLUE_MIC_GAIN")) {
                                FactoryDriver.Driver().setBlueMicGain(packet.getInt(name));
                                return ret;
                            } else if (name.equals("MONITOR_SWITCH")) {
                                FactoryDriver.Driver().setMonitorInfo(1, packet.getBoolean(name) ? 1 : 0);
                                return ret;
                            } else if (name.equals("CAMERA_POWER")) {
                                FactoryDriver.Driver().setCameraPower(packet.getBoolean(name));
                                return ret;
                            } else if (name.equals("CAMERA_SWITCH_TRUCK")) {
                                CommonDriver.Driver().setCameraSwitchTruck(packet.getBoolean(name));
                                return ret;
                            } else if (name.equals("ZHI_NENG_TONG")) {
                                boolean canSwitch = packet.getBoolean(name);
                                FactoryDriver.Driver().setZhiNengTong(canSwitch);
                                return ret;
                            } else if (name.equals("VIDEO_DEMO")) {
                                FactoryDriver.Driver().setVideoDemo(packet.getBoolean(name));
                                return ret;
                            } else if (name.equals("TURNLIGHT_SWITCH_LEFT")) {
                                CommonDriver.Driver().setTurnLightSwitch(1, packet.getBoolean(name));
                                return ret;
                            } else if (name.equals("TURNLIGHT_SWITCH_RIGHT")) {
                                CommonDriver.Driver().setTurnLightSwitch(2, packet.getBoolean(name));
                                return ret;
                            }
                            SettingsDefine.Common.Switch sw = SettingsDefine.Common.Switch.valueOf(name);
                            LogUtils.d(TAG, name + "\t" + sw.value());
                            switch (sw) {
                                case BRAKE_WARNING:
                                    CommonDriver.Driver().setBrakeWarning(packet.getBoolean(sw.value()));
                                    break;
                                case REVERSING_VOLUME:
                                    CommonDriver.Driver().setReversingVolume(packet.getBoolean(sw.value()));
                                    break;
                                case ANY_KEY:
                                    CommonDriver.Driver().setAnyKey(packet.getBoolean(sw.value()));
                                    break;
                                case KEY_TONE:
                                    CommonDriver.Driver().setKeyTone(packet.getBoolean(sw.value()));
                                    break;
                                case REVERSE_IMAGE:
                                    CommonDriver.Driver().setReverseImage(packet.getBoolean(sw.value()));
                                    break;
                                case GPS_MONITOR:
                                    CommonDriver.Driver().setGpsMonitor(packet.getBoolean(sw.value()));
                                    break;
                                case GPS_MIX:
                                    CommonDriver.Driver().setGpsMix(packet.getBoolean(sw.value()));
                                    break;
                                case GPS_MIX_RATIO:
                                    CommonDriver.Driver().setGpsMixRatio(packet.getInt(sw.value()));
                                    break;
                                case SWITCH_MODE_NORETAIN_3PARTY:
                                    CommonDriver.Driver().setNoretain3Party(packet.getBoolean(sw.value()));
                                    break;
                                case MEDIA_JUMP:
                                    CommonDriver.Driver().setMediaJump(packet.getBoolean(sw.value()));
                                    break;
                                case COLORFUL_LIGHT_SWITCH:
                                    CommonDriver.Driver().setColorfulLightSwitch(packet.getBoolean(sw.value()));
                                    break;
                                case SMALL_LIGHT_SWITCH:
                                    CommonDriver.Driver().setSmallLightSwitch(packet.getBoolean(sw.value()));
                                    break;
                                case FLICHER_SWITCH:
                                    CommonDriver.Driver().setFlicherSwitch(packet.getBoolean(sw.value()));
                                    break;
                                case FLICHER_RATE:
                                    CommonDriver.Driver().setFlicherRate(packet.getInt(sw.value()));
                                    break;
                                case COLORFUL_LIGHT_COLOR:
//                                    LogUtils.d(TAG, name + ", value=" + packet.getInt(sw.value()));
                                    CommonDriver.Driver().setColorfulLightColor(packet.getInt(sw.value()));
                                    break;
                                case REVERSE_GUIDE_LINE:
                                    CommonDriver.Driver().setReverseGuideLine(packet.getBoolean(sw.value()));
                                    break;
                                case DEFAULT_SYSTEM_VOLUME:
                                    CommonDriver.Driver().setDefSystemVolume(packet.getInt(sw.value()));
                                    break;
                                case DEFAULT_CALL_VOLUME:
                                    CommonDriver.Driver().setDefCallVolume(packet.getInt(sw.value()));
                                    break;
                                case VCOM:
                                    FactoryDriver.Driver().setVComValue(packet.getInt(sw.value()),packet.getBoolean("save"));
                                    break;
                                case UI_STYLE_NAME:
                                    FactoryDriver.Driver().setUISytle(packet.getString(sw.name()));
                                    break;
                                case CAMERA_SET_SWITCH:
                                    LogUtils.d(TAG, name + ", value=" + packet.getBoolean(sw.value()));
                                    FactoryDriver.Driver().setCameraSwitch(packet.getBoolean(sw.name()));
                                    break;
                                case PANORAMIC_SUPPORT:
                                    FactoryDriver.Driver().setPanoramicSwitch(packet.getBoolean(sw.name()));
                                    break;
                                case PANORAMIC_CONN_TYPE:
                                    FactoryDriver.Driver().setPanoramicConnType(packet.getInt(sw.name()));
                                    break;
                                case PANORAMIC_VIDEO_TYPE:
                                    FactoryDriver.Driver().setPanoramicVideoType(packet.getInt(sw.name()));
                                    break;
                                case AUTO_LAND_PORT:
                                    CommonDriver.Driver().setAutoLandPort(packet.getBoolean(sw.value()));
                                    break;
                                case DVR_SUPPORT:
                                    FactoryDriver.Driver().setDvrEnable(packet.getBoolean(sw.name()));
                                    break;
                                //天宇德增加
                                case KEY_SHAKE:
                                    CommonDriver.Driver().setKeyShake(packet.getBoolean(sw.value()));
                                    break;
                                case LIGHT_SENSITIVE:
                                    CommonDriver.Driver().setLightSensitive(packet.getBoolean(sw.value()));
                                    break;
                                default:
                                    break;
                            }
                        } catch (IllegalArgumentException e) {

                        }
                    }
                }
                break;

            case SettingsInterface.APK_TO_MAIN.NAVIG_SEL:
                NaviDriverable naviDriverable = null;
                BaseDriver driver = ModuleManager.getLogicByName(NaviDefine.MODULE).getDriver();
                if (driver instanceof NaviDriverable) {
                    naviDriverable = (NaviDriverable) driver;
                    naviDriverable.setNavigationPacketName(packet.getString(SettingsDefine.Navig.SELECTION));
                }
                break;

            case SettingsInterface.APK_TO_MAIN.RESTOR_FACTORY_SETTINGS:
                SystemDriver.Driver().restoreFactorySettings();
                break;

            case SettingsInterface.APK_TO_MAIN.WIPE_USER_DATA:
                SystemDriver.Driver().wipeUserData();
                break;

            case SettingsInterface.APK_TO_MAIN.WIPE_CACHE:
                SystemDriver.Driver().wipeCache();
                break;

            case SettingsInterface.APK_TO_MAIN.REBOOT:
                SystemDriver.Driver().reboot();
                break;

            case SettingsInterface.APK_TO_MAIN.TP_COORDINATE_SWITCH:
                if (null != packet) {
                    int tpSwitch = packet.getInt(SettingsDefine.TPCoordinate.SWITCH);
                    TpTouchDriver.Driver().setTpCoordinateSwitch(tpSwitch);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.FACTORY_PASSWD:
                if (null != packet) {
                    String passwd = packet.getString(Define.Factory.PASSWD);
                    FactoryDriver.Driver().setFactoryPasswd(passwd);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.FACTORY_VOICE_CHANNEL:
                if (null != packet) {
                    String channel = packet.getString("VoiceChannel");
                    FactoryDriver.Driver().setVoiceChannel(channel);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_SIZE:
                if (null != packet) {
                    FactoryDriver.Driver().setScreenSize(packet.getInt("ScreenSize"));
                }
                break;
            case SettingsInterface.APK_TO_MAIN.REQUEST_MCU_DEBUG:
                if (packet != null) {
                    FactoryDriver.Driver().requestMcuDebugInfo(packet.getInt(Define.Factory.MCU_DEBUG_INFO));
                }
                break;
            case SettingsInterface.APK_TO_MAIN.LK_LOGO_INDEX:
                if (packet != null) {
                    FactoryDriver.Driver().setLKLogoIndex(packet.getInt("LKLogoIndex"));
                }
                break;
            case SettingsInterface.APK_TO_MAIN.GPRS_APK_NAME:
                if (packet != null) {
                    CommonDriver.Driver().setGprsApkName(packet.getString(SettingsDefine.Common.Switch.GPRS_APK_NAME.name()));
                }
                break;
            case SettingsInterface.APK_TO_MAIN.AVDD:
                if (packet != null) {
                    int avdd = packet.getInt("avdd");
                    FactoryDriver.Driver().setAvdd(avdd);
                }
                break;

            case SettingsInterface.APK_TO_MAIN.SCREEN_BRIGHTNESS://亮度
                if (packet != null) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_BRIGHTNESS);
                    FactoryDriver.Driver().setScreenBrightness(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_COLOUR_TEMP://色温
                if (null != packet) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_COLOUR_TEMP);
                    FactoryDriver.Driver().setScreenColourtemp(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_SATURATION://饱和度
                if (null != packet) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_SATURATION);
                    FactoryDriver.Driver().setScreenSaturation(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_CONTRAST://对比度
                if (null != packet) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_CONTRAST);
                    FactoryDriver.Driver().setScreenContrast(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_SHARPNESS://锐度
                if (null != packet) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_SHARPNESS);
                    FactoryDriver.Driver().setScreenSharpness(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_DYNAMICCONTRAST://动态对比
                if (null != packet) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_DYNAMICCONTRAST);
                    FactoryDriver.Driver().setScreenDynamiccontrast(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_VCOM://VCOM
                if (null != packet) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_VCOM);
                    FactoryDriver.Driver().setScreenVcom(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_AVDD://AVDD
                if (null != packet) {
                    int val = packet.getInt(SettingsDefine.ScreenParam.TYPE_AVDD);
                    FactoryDriver.Driver().setScreenAvdd(val);
                }
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_RESET://恢复默认设置
                MiraVisionJni.resetPQ(getMainContext());
                getPQParam();
                FactoryDriver.Driver().resetScreenParam();
                break;
            case SettingsInterface.APK_TO_MAIN.SCREEN_SAVE://保存
                FactoryDriver.Driver().saveScreenParam();
                initPQParam();
                break;
            case SettingsInterface.APK_TO_MAIN.TMPS_APK_NAME:
                if (packet != null) {
                    CommonDriver.Driver().setTpmsApkName(packet.getString(SettingsDefine.Common.Switch.TMPS_APK_NAME.name()));
                }
                break;
            case SettingsInterface.APK_TO_MAIN.DVR_APK_NAME:
                if (packet != null) {
                    CommonDriver.Driver().setDvrApkName(packet.getString(SettingsDefine.Common.Switch.DVR_APK_NAME.name()));
                }
                break;
            case SettingsInterface.APK_TO_MAIN.ROTATE_VOLATGE:
                if (packet != null) {
                    FactoryDriver.Driver().setRotateVoltage(packet.getInt("rotate_voltage"));
                }
                break;
            case SettingsInterface.APK_TO_MAIN.ROTATE_TIME:
                if (packet != null) {
                    FactoryDriver.Driver().setRotateTime(packet.getInt("rotate_time"));
                }
                break;
            default:
                break;
        }
        return ret;
    }

    private void initPQParam() {
        int newValue = FactoryDriver.Driver().getScreenBrightness();
        MiraVisionJni.setPicBrightnessIndex(newValue);

        newValue = FactoryDriver.Driver().getScreenColourtemp();
        MiraVisionJni.setGammaIndex(newValue);

        newValue = FactoryDriver.Driver().getScreenSaturation();
        MiraVisionJni.setSaturationIndex(newValue);

        newValue = FactoryDriver.Driver().getScreenContrast();
        MiraVisionJni.setContrastIndex(newValue);

        newValue = FactoryDriver.Driver().getScreenSharpness();
        MiraVisionJni.setSharpnessIndex(newValue);

        newValue = FactoryDriver.Driver().getScreenDynamiccontrast();
        MiraVisionJni.setDynamicContrastIndex(newValue);

        newValue = FactoryDriver.Driver().getScreenVcom();
        Intent intent = new Intent();
        intent.setAction(ACTION_AVDD_SIZE);
        intent.putExtra("type", "vcom");
        intent.putExtra("para", newValue);
        getMainContext().sendBroadcast(intent);

        FileUtil.write(newValue + "", VCOM_FILE);

        newValue = FactoryDriver.Driver().getScreenAvdd();
        intent = new Intent();
        intent.setAction(ACTION_AVDD_SIZE);
        intent.putExtra("type", "avdd");
        intent.putExtra("para", newValue);
        getMainContext().sendBroadcast(intent);

        FileUtil.write(newValue + "", AVDD_FILE);
    }

    private void getPQParam() {
        //亮度
        int brightness = MiraVisionJni.getPicBrightnessIndex();
        FactoryDriver.Driver().setScreenBrightness(brightness);

        //对比度
        int contrast = MiraVisionJni.getContrastIndex();
        FactoryDriver.Driver().setScreenContrast(contrast);

        //饱和度
        int saturation = MiraVisionJni.getSaturationIndex();
        FactoryDriver.Driver().setScreenSaturation(saturation);

        //色温
        int gamma = MiraVisionJni.getGammaIndex();
        FactoryDriver.Driver().setScreenColourtemp(gamma);

        int sharpness = MiraVisionJni.getSharpnessIndex();
        FactoryDriver.Driver().setScreenSharpness(sharpness);

        int dyn = MiraVisionJni.getDynamicContrastIndex();
        FactoryDriver.Driver().setScreenDynamiccontrast(dyn);
    }
    /**
     * the driver interface.
     */
    protected SettingsDriver Driver() {
        SettingsDriver ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof SettingsDriver) {
            ret = (SettingsDriver) drive;
        }
        return ret;
    }

    /**
     * 获取ACC阶段
     */
    private int getAccoffStep() {
        int ret = AccoffListener.AccoffStep.STEP_WORK;
        CoreLogic coreLogic = LogicManager.getLogicByName(AccoffDefine.MODULE);
        if (coreLogic != null) {
            Packet packet = coreLogic.getInfo();
            if (null != packet) {
                ret = packet.getInt("AccoffStep");
            }
        }
        return ret;
    }

    private final static int NORMAL = 0;
    private final static int STUDY = 1;
    private final static int FINISH = 2;

    public void setTouchStudyState(int state) {
        try {
            InputManager inputManager = (InputManager) getMainContext().getSystemService(Context.INPUT_SERVICE);
            Class cmClass = inputManager.getClass();
            Method method = cmClass.getMethod("setTouchCalibrationState", int.class);
            method.invoke(inputManager, state);
        } catch (Exception e) {
//            LogUtils.w(TAG, " setTouchStudyState  exception!!! " + e.getMessage());
            e.printStackTrace();
        }
    }
}
