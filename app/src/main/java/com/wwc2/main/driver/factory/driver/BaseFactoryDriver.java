package com.wwc2.main.driver.factory.driver;

import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.internal.util.XmlUtils;
import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MByteArray;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.model.MStringArray;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.client.driver.BaseClientDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.factory.FactoryDriverable;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.driver.memory.mode.ini.IniMemory;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.settings.util.FileUtil;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * the base version driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class BaseFactoryDriver extends BaseMemoryDriver implements FactoryDriverable {
    private final String UI_STYLE_FILE_IMPORT = "/custom/uiStyleInfo.xml";
    private final String UI_STYLE_FILE_SYSTEM = "/system/etc/uiStyleInfo.xml";

    private List<Map<String, Object>> uiStyleList = new ArrayList<>();
    private String[] uiNameArray;

    private static final String STREAM_NAME = "/sys/class/gpiodrv/gpio_ctrl/360_camtype";
    private static final String MICGAIN_NAME = "/sys/class/mt_soc_codec_63xx/mt_codec_mic_gain";

    public static int DEFAULT_VCOM  = 48;
    public static int DEFAULT_AVDD  = 32;

    private int simswitch           = 0;
    private boolean importFlag      = false;

    private boolean mCloseScreen    = false;

    private boolean mSupportCamera  = false;
    private boolean mNoTouchKey     = false;//是否有触摸按键，true：没有，false：有
    private boolean mSupportPort    = false;//是否支持横竖屏自动旋转

    private boolean mElink          = true;//是否支持亿连

    protected boolean mCameraPower  = false;//摄像头是否长供电

    private int     mRotation       = 1;//旋转方向，默认是1

    private boolean mFanControlEnable   = false;//风扇控制开关
    private int     mFanVolume          = 15;//风扇控制音量值

    private boolean mGsensor            = false;//是否带g-sensor

    protected boolean mSupportCVBS  = false;//是否支持CVBS

    protected boolean mSupportPoweroff  = false;//是否支持长按太阳图标关机

    protected boolean mSupportRightCamera = false;//是否支持MCU右视

    public static boolean mSupportFrontCamera = false;//是否支持前视
    public static int mFrontCameraTime = 0;//0：关 5：5秒 10：10秒 20：20秒
    public static boolean mShowOnlineUpgrade = true;//是否显示在线升级
    public boolean mWakeUpScreenPower = false;//是否点击触摸唤醒屏幕开机
    protected String mTimeFormat = null;

    public static boolean mZhiNengTong  = true;//锐派竖屏智能通开关
    public static boolean mVideoDemo    = true;//锐派竖屏国六视频开关

    /**
     enum AUX_TYPE{
         AHD_25FPS = 1,
         AHD_30FPS,
         CVBS_NTSC,
         CVBS_PAL,
         FHD_25FPS,
         FHD_30FPS,
     };
     */
    private int mAuxVideoType       = -1;
    private int mRightVideoType     = -1;
    private boolean mSupportAngle   = false;
    private boolean mVoiceEnable = false;
    /**
     * 开机监听器
     */
    private PowerManager.PowerListener mPowerListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            if (PowerManager.PowerStep.isPoweronCreateOvered(newVal)) {
                // 开机完成，向SystemPermission请求同步APK使能状态
                CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
                if (logic != null) {
                    Packet init = new Packet();
                    for (Define.Factory.Modules modules : Define.Factory.Modules.values()) {
                        BaseLogic baseLogic = ModuleManager.getLogicBySource(modules.source());
                        if (baseLogic != null) {
                            String apkPacket = baseLogic.getAPKPacketName();
                            if (apkPacket != null) {
                                init.putString(modules.value(), apkPacket);
                            }
                        }
                    }
                    logic.Notify(SystemPermissionInterface.MAIN_TO_APK.FACTORY_MODULES_INIT, init);
                }
            }
        }
    };

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            // 模块使能
            boolean enable = false;
            enable = Model().getNavigation().getVal();
            mMemory.set("ENABLE", "navigation", enable+"");

            enable = Model().getRadio().getVal();
            mMemory.set("ENABLE", "radio", enable+"");

            enable = Model().getBluetooth().getVal();
            mMemory.set("ENABLE", "bluetooth", enable+"");

            enable = Model().getAudioFrequency().getVal();
            mMemory.set("ENABLE", "audio", enable+"");

            enable = Model().getVideo().getVal();
            mMemory.set("ENABLE", "video", enable+"");

            enable = Model().getPicture().getVal();
            mMemory.set("ENABLE", "picture", enable+"");

            enable = Model().getAux().getVal();
            mMemory.set("ENABLE", "aux", enable+"");

            enable = Model().getVoiceAssistant().getVal();
            mMemory.set("ENABLE", "voice_assistant", enable+"");

            // 收音区域
            final String region = Model().getRadioRegion().getVal();
            if (!TextUtils.isEmpty(region)) {
                mMemory.set("RADIO", "region", region);
            }
            String passwd = Model().getmFactoryPasswd().getVal();
            mMemory.set("Factory", "PASSWD", passwd);

            int screenSize = Model().getScreenSize().getVal();
            mMemory.set("Factory", "screen_size", screenSize+"");

            enable = Model().getRadioAerial().getVal();
            mMemory.set("ENABLE", "radio_aerial", enable+"");

            int radioModule = Model().getRadioModule().getVal();
            mMemory.set("Factory", "radio_module", radioModule+"");

            enable = Model().getRDSEnable().getVal();
            mMemory.set("ENABLE", "rds_enable", enable+"");

            int lkLogoIndex = Model().getmLKLogoIndex().getVal();
            mMemory.set("Factory", "lk_logo_index", lkLogoIndex+"");

            String resetPasswd = Model().getmResetPassword().getVal();
            mMemory.set("Factory", "reset_passwd", resetPasswd);

            int val = Model().getVComValue().getVal();
            mMemory.set("Factory", "vcom_value", val+"");

            val = Model().getAvdd().getVal();
            mMemory.set("Factory", "avdd", val + "");

//            val = Model().getUiStyle().getVal();
            String uiStyle = Model().getUiStyleDate().getVal();
            mMemory.set("Factory", "ui_style", uiStyle);

            enable = Model().getUiStyleShow().getVal();
            mMemory.set("Factory", "ui_style_show", enable+"");

//            Integer[] intArray = Model().getUiStyleNumber().getVal();
//            String number = "";
//            if (intArray != null && intArray.length > 0) {
//                for (int i = 0; i < intArray.length; i++) {
//                    if (i == intArray.length - 1) {
//                        number += intArray[i];
//                    } else {
//                        number += (intArray[i] + ",");
//                    }
//                }
//            }
            String[] dateArray = Model().getUiStyleDateArray().getVal();
            String number = "";
            if (dateArray != null && dateArray.length > 0) {
                for (int i = 0; i < dateArray.length; i++) {
                    if (i == dateArray.length - 1) {
                        number += dateArray[i];
                    } else {
                        number += (dateArray[i] + ",");
                    }
                }
            }
            mMemory.set("Factory", "ui_style_number", number);

            mMemory.set("Factory", "camera_switch", Model().getCameraSwitch().getVal() + "");
            mMemory.set("Factory", "panoramic_switch", Model().getPanoramicSwitch().getVal() + "");
            mMemory.set("Factory", "panoramic_conn_type", Model().getPanoramicConnType().getVal() + "");
            mMemory.set("Factory", "panoramic_video_type", Model().getPanoramicVideoType().getVal() + "");
            mMemory.set("Factory", "panoramic_type", Model().getPanoramicType().getVal() + "");

            mMemory.set("Factory", "dvr_enable", Model().getDvrEnable().getVal() + "");

            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_BRIGHTNESS, Model().getScreenBrightness().getVal() + "");
            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_COLOUR_TEMP, Model().getScreenColourtemp().getVal() + "");
            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_CONTRAST, Model().getScreenContrast().getVal() + "");
            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_SATURATION, Model().getScreenSaturation().getVal() + "");
            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_DYNAMICCONTRAST, Model().getScreenDynamiccontrast().getVal() + "");
            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_SHARPNESS, Model().getScreenSharpness().getVal() + "");
            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_AVDD, Model().getScreenAvdd().getVal() + "");
            mMemory.set("Factory", SettingsDefine.ScreenParam.TYPE_VCOM, Model().getScreenVcom().getVal() + "");

            mMemory.set("Factory", "close_screen", mCloseScreen + "");
            mMemory.set("Factory", "support_camra", mSupportCamera + "");

//            mMemory.set("Factory", "no_touch_key", mNoTouchKey + "");
//            mMemory.set("Factory", "support_port", mSupportPort + "");

            mMemory.set("Factory", "gprs_apk_name", CommonDriver.Driver().getGprsApkName());

            mMemory.set("ENABLE", "elink", mElink + "");

            mMemory.set("Factory", "time_zone", getTimezone());

            mMemory.set("Factory", "camera_power", mCameraPower + "");

            mMemory.set("Factory", "rotation", mRotation + "");

            mMemory.set("Factory", "fan_enable", mFanControlEnable + "");
            mMemory.set("Factory", "fan_volume", mFanVolume + "");

            mMemory.set("Factory", "rotate_voltage", Model().getRotateVoltage().getVal() + "");
            mMemory.set("Factory", "rotate_time", Model().getRotateTime().getVal() + "");

            mMemory.set("Factory", "gsensor", mGsensor + "");

            //面板按键导入导出
            Byte[] panelKeys = Model().getPanelKeyArray().getVal();
            if (panelKeys != null && panelKeys.length > 0) {
                String keyString = "";
                for (int i = 0; i < panelKeys.length; i++) {
                    keyString += (int)panelKeys[i];
                    if (i != panelKeys.length - 1) {
                        keyString += ",";
                    }
                }
                LogUtils.d("PanelKey-----" + keyString);
                mMemory.set("MCUDATA", "panelKey", keyString);
            }

            mMemory.set("Factory", "support_cvbs", mSupportCVBS + "");

            mMemory.set("Factory", "support_poweroff", mSupportPoweroff + "");

            mMemory.set("Factory", "support_rightcamera", mSupportRightCamera + "");

            mMemory.set("Factory", "aux_video_type", mAuxVideoType + "");
            mMemory.set("Factory", "right_video_type", mRightVideoType + "");

            mMemory.set("Factory", "support_frontcamera", mSupportFrontCamera + "");
            mMemory.set("Factory", "frontCameraTime", mFrontCameraTime + "");
            mMemory.set("Factory", "show_onlineupgrade", mShowOnlineUpgrade + "");
            mMemory.set("Factory", "blue_micgain", Model().getBlueMicGain().getVal() + "");
            mMemory.set("Factory", "wakeup_power", mWakeUpScreenPower + "");
            mMemory.set("Factory", "support_angle", mSupportAngle + "");
            mMemory.set("Factory", "voice_enable", mVoiceEnable + "");

            if (mTimeFormat != null) {
                mMemory.set("Factory", "time_format", mTimeFormat);
            }

            val = Model().getMonitorSwitch().getVal();
            mMemory.set("Factory", "monitor_switch", val + "");

            val = Model().getDeviceVoltale().getVal();
            mMemory.set("Factory", "device_voltage", val + "");

            mMemory.set("Factory", "zhi_neng_tong", mZhiNengTong + "");//锐派竖屏智能通开关
            mMemory.set("Factory", "video_demo_icon", mVideoDemo + "");//锐派竖屏国六视频开关

            ret = true;
        }
        return ret;
    }

    @Override
    public BaseModel newModel() {
        return new FactoryModel();
    }

    /**
     * get the model object.
     */
    protected FactoryModel Model() {
        FactoryModel ret = null;
        BaseModel model = getModel();
        if (model instanceof FactoryModel) {
            ret = (FactoryModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        PowerManager.getModel().bindListener(mPowerListener);
        initUistyle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PowerManager.getModel().unbindListener(mPowerListener);
    }

    @Override
    public String filePath() {
        return "FactoryDataConfig.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            Object object = null;
            // 模块使能
            boolean enable = false;
            try {
                object = mMemory.get("ENABLE", "navigation");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getNavigation().setVal(enable);
                    ret = true;
                }

                object = mMemory.get("ENABLE", "radio");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getRadio().setVal(enable);
                    ret = true;
                }

                object = mMemory.get("ENABLE", "bluetooth");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getBluetooth().setVal(enable);
                    ret = true;
                }

                SystemProperties.set("persist.sys.bluetooth.enable", Model().getBluetooth().getVal() + "");

                object = mMemory.get("ENABLE", "audio");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getAudioFrequency().setVal(enable);
                    ret = true;
                }

                object = mMemory.get("ENABLE", "video");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getVideo().setVal(enable);
                    ret = true;
                }

                object = mMemory.get("ENABLE", "picture");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getPicture().setVal(enable);
                    ret = true;
                }

                object = mMemory.get("ENABLE", "aux");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getAux().setVal(enable);
                    ret = true;
                }

                object = mMemory.get("ENABLE", "voice_assistant");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getVoiceAssistant().setVal(enable);
                    ret = true;
                }

                // 收音区域
                object = mMemory.get("RADIO", "region");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getRadioRegion().setVal(string);
                        ret = true;
                    }
                }

                object = mMemory.get("Factory", Define.Factory.PASSWD);
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getmFactoryPasswd().setVal(string);
                        ret = true;
                    }
                }
                //收音有源天线开关
                object = mMemory.get("ENABLE", "radio_aerial");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getRadioAerial().setVal(enable);
                    ret = true;
                }
                //收音模块
                object = mMemory.get("Factory", "radio_module");
                if (null != object) {
                    int module = Integer.parseInt((String) object);
                    Model().getRadioModule().setVal(module);
                    ret = true;
                }
                //RDS开关
                object = mMemory.get("ENABLE", "rds_enable");
                if (null != object) {
                    enable = Boolean.parseBoolean((String) object);
                    Model().getRDSEnable().setVal(enable);
                    ret = true;
                }

                //屏幕尺寸设置
                object = mMemory.get("Factory", "screen_size");
                if (null != object) {
                    int screenSize = Integer.parseInt((String) object);
                    Model().getScreenSize().setVal(screenSize);
                    ret = true;
                }
                object = mMemory.get("Factory", "lk_logo_index");
                if (null != object) {
                    int index = Integer.parseInt((String) object);
                    Model().getmLKLogoIndex().setVal(index);
                    ret = true;
                }
                object = mMemory.get("Factory", "reset_passwd");
                if (null != object) {
                    String resetPasswd = (String) object;
                    Model().getmResetPassword().setVal(resetPasswd);
                    ret = true;
                }
                object = mMemory.get("Factory", "vcom_value");
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getVComValue().setVal(val);
                    ret = true;
                }

                object = mMemory.get("Factory", "ui_style");
                if (null != object) {
                    String uiStyleDate = (String) object;
                    if (uiStyleDate.length() < 5) {//100以上无法通过数字配置
                        int val = Integer.parseInt((String) object);
                        LogUtils.d("BaseFactoryDriver", "ui_style:" + val);
                        Model().getUiStyle().setVal(val);
                    } else {
                        Model().getUiStyle().setVal(-1);
                        Model().getUiStyleDate().setVal(uiStyleDate);
                    }
                    ret = true;
                }
                object = mMemory.get("Factory", "ui_style_show");
                if (null != object) {
                    boolean val = Boolean.parseBoolean((String) object);
                    LogUtils.d("BaseFactoryDriver", "ui_style_show:" + val);
                    Model().getUiStyleShow().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", "ui_style_number");
                if (null != object) {
                    LogUtils.d("BaseFactoryDriver", "ui_style_number:"+((String) object));
                    String[] arrayStr = ((String) object).split(",");
                    if (arrayStr.length > 0) {
                        LogUtils.d("BaseFactoryDriver", "arrayStr[0].length():" + arrayStr[0].length());
                        if (arrayStr[0].length() < 5) {//100以上无法通过数字配置
                            Integer[] intType = new Integer[arrayStr.length];
                            int realLen = 0;
                            for (int i = 0; i < arrayStr.length; i++) {
                                if (arrayStr[i].length() < 5) {//100以上无法通过数字配置
                                    intType[realLen++] = Integer.parseInt(arrayStr[i]);
                                }
                            }
                            if (realLen > 0) {
                                intType = Arrays.copyOfRange(intType, 0, realLen);
                            }
                            Model().getUiStyleNumber().setVal(intType);
                        } else {
                            Model().getUiStyleNumber().setVal(null);
                            Model().getUiStyleDateArray().setVal(arrayStr);
                        }
                        ret = true;
                    }
                }

                //后视和360全景相关设置不通过配置文件的导入导出2018-11-01
                if (!importFlag) {
                    object = mMemory.get("Factory", "camera_switch");
                    if (null != object) {
                        boolean val = Boolean.parseBoolean((String) object);
                        Model().getCameraSwitch().setVal(val);
                        ret = true;
                    }
                    object = mMemory.get("Factory", "panoramic_switch");
                    if (null != object) {
                        boolean val = Boolean.parseBoolean((String) object);
                        Model().getPanoramicSwitch().setVal(val);
                        ret = true;
                    }
                    object = mMemory.get("Factory", "panoramic_conn_type");
                    if (null != object) {
                        int val = Integer.parseInt((String) object);
                        Model().getPanoramicConnType().setVal(val);
                        ret = true;
                    }
                    object = mMemory.get("Factory", "panoramic_video_type");
                    if (null != object) {
                        int val = Integer.parseInt((String) object);
                        /*
                        1:AHD25fps,
                        2:AHD30fps,
                        3:CVBS_NTSC,
                        4:CVBS_PAL,
                        5:1080P25fps,
                        6:1080P30fps,
                        7:TVI,
                        8:CVI,
                        9:PVI
                        */
                        if (val < 1 && val > 9) {//当用户配置的值在1～9范围外，默认为1，避免出现黑白图像问题。2018-11-23
                            val = 1;
                        }
                        Model().getPanoramicVideoType().setVal(val);
                        ret = true;
                    }
                }

                object = mMemory.get("Factory", "panoramic_type");
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getPanoramicType().setVal(val);
                    ret = true;
                }

                object = mMemory.get("Factory", "dvr_enable");
                if (null != object) {
                    boolean dvrEnable = Boolean.parseBoolean((String) object);
                    Model().getDvrEnable().setVal(dvrEnable);
                    LogUtils.e("DVR_SUPPORT----" + dvrEnable);

                    SystemProperties.set("persist.sys.dvr_enable", dvrEnable + "");
                    ret = true;
                }

                object = mMemory.get("Factory", "avdd");
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getAvdd().setVal(val);
                    ret = true;
                }

                if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                        !Model().getDvrEnable().getVal()) {
                    //由DVR处理，不再写节点。2019-10-23
//                if (Model().getCameraSwitch().getVal()) {
                    FileUtil.write(Model().getPanoramicVideoType().getVal() + "", STREAM_NAME);
//                } else {
//                    FileUtil.write(1 + "", STREAM_NAME);//经讨论，去掉驱动的自动检测，默认AHD25fps。2019-01-09
//                }
                }

                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_BRIGHTNESS);
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getScreenBrightness().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_COLOUR_TEMP);
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getScreenColourtemp().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_CONTRAST);
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getScreenContrast().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_SATURATION);
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getScreenSaturation().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_DYNAMICCONTRAST);
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getScreenDynamiccontrast().setVal(val);
                    ret = true;
                }
                //2019-07-24：将锐度默认值改为2，并且手动设置无效。
//                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_SHARPNESS);
//                if (null != object) {
//                    int val = Integer.parseInt((String) object);
//                    Model().getScreenSharpness().setVal(val);
//                    ret = true;
//                }
                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_AVDD);
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getScreenAvdd().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", SettingsDefine.ScreenParam.TYPE_VCOM);
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getScreenVcom().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", "simswitch");
                if (null != object) {
                    simswitch = Integer.parseInt((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory", "close_screen");
                if (null != object) {
                    mCloseScreen = Boolean.parseBoolean((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory", "support_camra");
                if (null != object) {
                    mSupportCamera = Boolean.parseBoolean((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory", "no_touch_key");
                if (null != object) {
                    mNoTouchKey = Boolean.parseBoolean((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory", "support_port");
                if (null != object) {
                    mSupportPort = Boolean.parseBoolean((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "gprs_apk_name");
                if (null != object) {
                    String name = (String) object;
                    CommonDriver.Driver().setGprsApkName(name);
                    ret = true;
                }

                object = mMemory.get("ENABLE", "elink");
                if (null != object) {
                    mElink = Boolean.parseBoolean((String) object);
                    ret = true;
                }
                SystemProperties.set("user.elink.enable", mElink + "");

                object = mMemory.get("Factory", "time_zone");
                if (null != object) {
                    setTimezone((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "camera_power");
                if (null != object) {
                    mCameraPower = Boolean.parseBoolean((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "rotation");
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    if (val >= 0 && val < 4) {
                        mRotation = val;
                        SystemProperties.set("persist.sys.customrotation", mRotation + "");
                    }
                    ret = true;
                }

                object = mMemory.get("Factory", "fan_enable");
                if (null != object) {
                    mFanControlEnable = Boolean.parseBoolean((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory", "fan_volume");
                if (null != object) {
                    mFanVolume = Integer.parseInt((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "rotate_voltage");
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getRotateVoltage().setVal(val);
                    ret = true;
                }
                object = mMemory.get("Factory", "rotate_time");
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    Model().getRotateTime().setVal(val);
                    ret = true;
                }

                object = mMemory.get("Factory", "gsensor");
                if (null != object) {
                    mGsensor = Boolean.parseBoolean((String) object);
                    Settings.System.putInt(getMainContext().getContentResolver(),
                            Settings.System.ACCELEROMETER_ROTATION, mGsensor ? 1 : 0);
                    ret = true;
                }

                if (importFlag) {//面板按键只在导入时发给MCU
                    object = mMemory.get("MCUDATA", "panelKey");
                    if (null != object) {
                        String string = (String) object;
                        LogUtils.d("panelKey--read---" + string);
                        if (!TextUtils.isEmpty(string)) {
                            String[] fileds = string.split(",");
                            if (fileds != null && fileds.length > 0) {
                                Byte[] filedsByte = new Byte[fileds.length];
                                for (int i = 0; i < fileds.length; i++) {
                                    filedsByte[i] = (byte) Integer.parseInt(fileds[i]);
                                }
                                Model().getPanelKeyArray().setVal(filedsByte);
                                //导入时发送给MCU
                                McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_PanelKeyImport,
                                        toObjects(filedsByte), filedsByte.length);
                            }
                        }
                    } else {
                        LogUtils.e("BaseEQDriver", "GainValue--read---null");
                    }

                    //收音区域配置
                    String region = Model().getRadioRegion().getVal();
                    Define.Factory.Regions rg = Define.Factory.Regions.valueOf(region);
                    int regions = rg.index();
                    if (regions < 0) {
                        regions = 5;
                    }
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_RadioSetregion, new byte[]{(byte) regions}, 1);
                }

                object = mMemory.get("Factory", "support_cvbs");
                if (null != object) {
                    mSupportCVBS = Boolean.parseBoolean((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "support_poweroff");
                if (null != object) {
                    mSupportPoweroff = Boolean.parseBoolean((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "support_rightcamera");
                if (null != object) {
                    mSupportRightCamera = Boolean.parseBoolean((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "aux_video_type");
                if (null != object) {
                    mAuxVideoType = Integer.parseInt((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "right_video_type");
                if (null != object) {
                    mRightVideoType = Integer.parseInt((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "support_frontcamera");
                if (null != object) {
                    mSupportFrontCamera = Boolean.parseBoolean((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory", "frontCameraTime");
                if (null != object) {
                    mFrontCameraTime = Integer.parseInt((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory", "show_onlineupgrade");
                if (null != object) {
                    mShowOnlineUpgrade = Boolean.parseBoolean((String) object);
                }
                object = mMemory.get("Factory", "blue_micgain");
                if (null != object) {
                    int gain = Integer.parseInt((String) object);
                    Model().getBlueMicGain().setVal(gain);

                    FileUtil.write(gain + "", MICGAIN_NAME);
                    ret = true;
                }
                object = mMemory.get("Factory", "wakeup_power");
                if (null != object) {
                    mWakeUpScreenPower = Boolean.parseBoolean((String) object);
                    ret = true;
                }

                object = mMemory.get("Factory", "time_format");
                if (null != object) {
                    mTimeFormat = (String) object;
                    if (mTimeFormat.equals("12") || mTimeFormat.equals("24")) {
                        Settings.System.putString(getMainContext().getContentResolver(), Settings.System.TIME_12_24, mTimeFormat);
                    }
                    ret = true;
                }

                object = mMemory.get("Factory","support_angle");
                if (null != object) {
                    mSupportAngle = Boolean.parseBoolean((String) object);
                    ret = true;
                }
                object = mMemory.get("Factory","voice_enable");
                if (null != object) {
                    mVoiceEnable = Boolean.parseBoolean((String) object);
                    if (!mVoiceEnable) {
                        //根据系统属性默认打开海外语音。2020-08-27
                        String sysAios = SystemProperties.get("ro.sys_gw_aios", "false");
                        if ("true".equals(sysAios)) {
                            mVoiceEnable = true;
                        }
                    }
                    ret = true;
                }

                object = mMemory.get("Factory", "monitor_switch");
                if (null != object) {
                    int sw = Integer.parseInt((String) object);
                    Model().getMonitorSwitch().setVal(sw);
                    ret = true;
                }
                object = mMemory.get("Factory", "device_voltage");
                if (null != object) {
                    int voltage = Integer.parseInt((String) object);
                    Model().getDeviceVoltale().setVal(voltage);
                    ret = true;
                }

                object = mMemory.get("Factory", "zhi_neng_tong");
                if (null != object) {
                    mZhiNengTong = Boolean.parseBoolean((String) object);//锐派竖屏智能通开关
                    ret = true;
                }

                object = mMemory.get("Factory", "video_demo_icon");
                if (null != object) {
                    mVideoDemo = Boolean.parseBoolean((String) object);
                    ret = true;
                }//锐派竖屏国六视频开关
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public void setAvdd(int avdd) {
        Model().getAvdd().setValAnyway(avdd);
        if (mMemory != null) {
            mMemory.save();
        }
    }

    @Override
    public void setModuleEnable(String module, boolean enable) {
        if (Define.Factory.Modules.NAVIGATION.value().equals(module)) {
            Model().getNavigation().setVal(enable);
        } else if (Define.Factory.Modules.RADIO.value().equals(module)) {
            Model().getRadio().setVal(enable);
        } else if (Define.Factory.Modules.BLUETOOTH.value().equals(module)) {
            Model().getBluetooth().setVal(enable);

            SystemProperties.set("persist.sys.bluetooth.enable", enable + "");
        } else if (Define.Factory.Modules.AUDIO_FREQUENCY.value().equals(module)) {
            Model().getAudioFrequency().setVal(enable);
        } else if (Define.Factory.Modules.VIDEO.value().equals(module)) {
            Model().getVideo().setVal(enable);
        } else if (Define.Factory.Modules.PICTURE.value().equals(module)) {
            Model().getPicture().setVal(enable);
        } else if (Define.Factory.Modules.AUX.value().equals(module)) {
            Model().getAux().setVal(enable);
        } else if (Define.Factory.Modules.VOICE_ASSISTANT.value().equals(module)) {
            Model().getVoiceAssistant().setVal(enable);
        }
    }

    @Override
    public void setFactoryPasswd(String passwd) {
        Model().getmFactoryPasswd().setVal(passwd);
        if (mMemory != null) {
            mMemory.save();
        }
    }

    @Override
    public void setScreenSize(int screenSize) {
        Model().getScreenSize().setVal(screenSize);
        if (mMemory != null) {
            mMemory.save();
        }
    }

    @Override
    public void reloadConfig() {
        if (mMemory != null && mMemory instanceof IniMemory) {
            ((IniMemory) mMemory).reloadIniFile();
            importFlag = true;//后视和360全景相关设置不通过配置文件的导入导出2018-11-01
            readData();
            initUistyle();

            //增加SIM切换配置，只在导入的时候写入。
            if (simswitch == 1 || simswitch == 2) {
                SystemProperties.set("persist.sys.simswich", simswitch + "");
            }

            //后视和360全景相关设置不通过配置文件的导入导出2018-11-01
            importFlag = false;
            mMemory.save();
        }
    }

    @Override
    public void setLKLogoIndex(int lkLogoIndex) {
        Model().getmLKLogoIndex().setVal(lkLogoIndex);
        if (mMemory != null) {
            mMemory.save();
        }
    }

    @Override
    public void setUISytle(String style) {
        Model().getUiStyleName().setVal(style);
        for (Map<String, Object> map : uiStyleList) {
            if (style != null && style.equals(map.get("name"))) {
                int styleID = (int) map.get("id");
                Model().getUiStyle().setVal(styleID);
                Model().getUiStyleDate().setVal((String) map.get("date"));
                if (mMemory != null) {
                    mMemory.save();
                }
            }
        }

        writeUIStyle(Model().getUiStyle().getVal() + "");
    }

    @Override
    public void setScreenBrightness(int brightness) {
        Model().getScreenBrightness().setVal(brightness);
    }

    @Override
    public void setScreenColourtemp(int colourtemp) {
        Model().getScreenColourtemp().setVal(colourtemp);
    }

    @Override
    public void setScreenSaturation(int saturation) {
        Model().getScreenSaturation().setVal(saturation);
    }

    @Override
    public void setScreenContrast(int contrast) {
        Model().getScreenContrast().setVal(contrast);
    }

    @Override
    public void setScreenSharpness(int sharpness) {
        //2019-07-24：将锐度默认值改为2，并且手动设置无效。
//        Model().getScreenSharpness().setVal(sharpness);
    }

    @Override
    public void setScreenDynamiccontrast(int dynamiccontrast) {
        Model().getScreenDynamiccontrast().setVal(dynamiccontrast);
    }

    @Override
    public void setScreenVcom(int vcom) {
        Model().getScreenVcom().setVal(vcom);
    }

    @Override
    public void setScreenAvdd(int avdd) {
        Model().getScreenAvdd().setVal(avdd);
    }

    @Override
    public void resetScreenParam() {
        setScreenAvdd(DEFAULT_AVDD);
        setScreenVcom(DEFAULT_VCOM);
    }

    @Override
    public void saveScreenParam() {
        if (null != mMemory) {
            mMemory.save();
        }
    }

    @Override
    public int getScreenBrightness() {
        return Model().getScreenBrightness().getVal();
    }

    @Override
    public int getScreenColourtemp() {
        return Model().getScreenColourtemp().getVal();
    }

    @Override
    public int getScreenSaturation() {
        return Model().getScreenSaturation().getVal();
    }

    @Override
    public int getScreenContrast() {
        return Model().getScreenContrast().getVal();
    }

    @Override
    public int getScreenSharpness() {
        return Model().getScreenSharpness().getVal();
    }

    @Override
    public int getScreenDynamiccontrast() {
        return Model().getScreenDynamiccontrast().getVal();
    }

    @Override
    public int getScreenVcom() {
        return Model().getScreenVcom().getVal();
    }

    @Override
    public int getScreenAvdd() {
        return Model().getScreenAvdd().getVal();
    }

    @Override
    public void setDvrEnable(boolean enable) {
        LogUtils.d("setDvrEnable---enable=" + enable + ", old=" + Model().getDvrEnable().getVal());
        Model().getDvrEnable().setVal(enable);
    }

    @Override
    public boolean getDvrEnable() {
        return Model().getDvrEnable().getVal();
    }

    /**
     * 数据Model
     */
    protected static class FactoryModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet ret = new Packet();
            /**初始化工厂模块的初始化*/
            ret.putBoolean(Define.Factory.Modules.NAVIGATION.value(), getNavigation().getVal());
            ret.putBoolean(Define.Factory.Modules.RADIO.value(), getRadio().getVal());
            ret.putBoolean(Define.Factory.Modules.BLUETOOTH.value(), getBluetooth().getVal());
            ret.putBoolean(Define.Factory.Modules.AUDIO_FREQUENCY.value(), getAudioFrequency().getVal());
            ret.putBoolean(Define.Factory.Modules.VIDEO.value(), getVideo().getVal());
            ret.putBoolean(Define.Factory.Modules.PICTURE.value(), getPicture().getVal());
            ret.putBoolean(Define.Factory.Modules.AUX.value(), getAux().getVal());
            ret.putBoolean(Define.Factory.Modules.VOICE_ASSISTANT.value(), getVoiceAssistant().getVal());

            /** 收音机区域初始化 */
            ret.putString(Define.Factory.REGION, getRadioRegion().getVal());
            ret.putBoolean("rds_enable", getRDSEnable().getVal());
            ret.putString(Define.Factory.PASSWD, getmFactoryPasswd().getVal());
            ret.putString("VoiceChannel", getVoiceChannel().getVal());
            ret.putBoolean(Define.Factory.RADIO_AERIAL, getRadioAerial().getVal());
            ret.putInt(Define.Factory.RADIO_MODULE, getRadioModule().getVal());
            ret.putInt("ScreenSize",getScreenSize().getVal());
            ret.putInt("LKLogoIndex",getmLKLogoIndex().getVal());
            ret.putString(Define.Factory.RESET_PASSWD,getmResetPassword().getVal());

            ret.putInt(SettingsDefine.Common.Switch.VCOM.value(), getVComValue().getVal());

            ret.putInt(SettingsDefine.Common.Switch.UI_STYLE.name(), getUiStyle().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.UI_STYLE_SHOW.name(), getUiStyleShow().getVal());
            ret.putStringArray(SettingsDefine.Common.Switch.UI_NAME_ARRAY.name(), getUiNameArray().getVal());
            ret.putString(SettingsDefine.Common.Switch.UI_STYLE_NAME.name(), getUiStyleName().getVal());

            ret.putBoolean(SettingsDefine.Common.Switch.CAMERA_SET_SWITCH.name(), getCameraSwitch().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.PANORAMIC_SUPPORT.name(), getPanoramicSwitch().getVal());
            ret.putInt(SettingsDefine.Common.Switch.PANORAMIC_CONN_TYPE.name(), getPanoramicConnType().getVal());
            ret.putInt(SettingsDefine.Common.Switch.PANORAMIC_VIDEO_TYPE.name(), getPanoramicVideoType().getVal());
            ret.putInt("ONE_PANORAMIC", getPanoramicType().getVal());
            ret.putInt("avdd", getAvdd().getVal());

            ret.putBoolean(SettingsDefine.Common.Switch.DVR_SUPPORT.name(), getDvrEnable().getVal());

            ret.putInt(SettingsDefine.ScreenParam.TYPE_BRIGHTNESS, getScreenBrightness().getVal());
            ret.putInt(SettingsDefine.ScreenParam.TYPE_COLOUR_TEMP, getScreenColourtemp().getVal());
            ret.putInt(SettingsDefine.ScreenParam.TYPE_CONTRAST, getScreenContrast().getVal());
            ret.putInt(SettingsDefine.ScreenParam.TYPE_SATURATION, getScreenSaturation().getVal());
            ret.putInt(SettingsDefine.ScreenParam.TYPE_DYNAMICCONTRAST, getScreenDynamiccontrast().getVal());
            ret.putInt(SettingsDefine.ScreenParam.TYPE_SHARPNESS, getScreenSharpness().getVal());
            ret.putInt(SettingsDefine.ScreenParam.TYPE_AVDD, getScreenAvdd().getVal());
            ret.putInt(SettingsDefine.ScreenParam.TYPE_VCOM, getScreenVcom().getVal());

            ret.putInt("rotate_voltage", getRotateVoltage().getVal());
            ret.putInt("rotate_time", getRotateTime().getVal());

            ret.putBoolean("support_frong_camera", mSupportFrontCamera);
            ret.putInt("front_camera_time", mFrontCameraTime);
            ret.putBoolean("show_online_upgrade", mShowOnlineUpgrade);
            ret.putInt("blue_mic_gain", getBlueMicGain().getVal());

            ret.putInt("MONITOR_SWITCH", getMonitorSwitch().getVal());

            ret.putBoolean("zhi_neng_tong", mZhiNengTong);//锐派竖屏智能通开关
            ret.putBoolean("video_demo_icon", mVideoDemo);//锐派竖屏国六视频开关

            return ret;
        }

        public MBoolean mNavigation = new MBoolean(this, "NavigationListener", true);

        public MBoolean getNavigation() {
            return mNavigation;
        }

        public MBoolean mRadio = new MBoolean(this, "RadioListener", true);

        public MBoolean getRadio() {
            return mRadio;
        }

        public MBoolean mBluetooth = new MBoolean(this, "BluetoothListener", true);

        public MBoolean getBluetooth() {
            return mBluetooth;
        }

        public MBoolean mAudioFrequency = new MBoolean(this, "AudioFrequencyListener", true);

        public MBoolean getAudioFrequency() {
            return mAudioFrequency;
        }

        public MBoolean mVideo = new MBoolean(this, "VideoListener", true);

        public MBoolean getVideo() {
            return mVideo;
        }

        public MBoolean mPicture = new MBoolean(this, "PictureListener", true);

        public MBoolean getPicture() {
            return mPicture;
        }

        public MBoolean mAux = new MBoolean(this, "AuxListener", true);

        public MBoolean getAux() {
            return mAux;
        }

        public MBoolean mVoiceAssistant = new MBoolean(this, "VoiceAssistantListener", true);

        public MBoolean getVoiceAssistant() {
            return mVoiceAssistant;
        }

        public MString mRadioRegion = new MString(this, "RadioRegionListener", Define.Factory.Regions.CN.value());

        public MString getRadioRegion() {
            return mRadioRegion;
        }

        private MString mFactoryPasswd = new MString(this, "FactoryPasswdListener", "8888");

        public MString getmFactoryPasswd() {
            return mFactoryPasswd;
        }

        private MString voiceChannel = new MString(this, "voiceChannelListener", "aux");

        public MString getVoiceChannel() {
            return voiceChannel;
        }

        public MBoolean mRadioAerial = new MBoolean(this, "RadioAerialListener", false);

        public MBoolean getRadioAerial() {
            return mRadioAerial;
        }

        private MInteger mRadioModuel = new MInteger(this, "RadioModuelListener", 0);

        public MInteger getRadioModule() {
            return mRadioModuel;
        }

        private MBoolean mRDSEnable = new MBoolean(this, "RDSEnableListener", false);

        public MBoolean getRDSEnable() {
            return mRDSEnable;
        }

        public MBoolean mVoiceEnable = new MBoolean(this,"VoiceEnableListener",false);

        public MBoolean getVoiceEnable() {
            return mVoiceEnable;
        }

        private MInteger mScreenSize = new MInteger(this, "ScreenSizeListener", 10);

        public MInteger getScreenSize() {
            return mScreenSize;
        }

        private MIntegerArray mMcuDebugInfo = new MIntegerArray(this, "McuDebugInfoListener", null);

        public MIntegerArray getMcuDebugInfo() {
            return mMcuDebugInfo;
        }

        private MInteger mSteerADValue = new MInteger(this, "SteerADValueListener", 255);

        public MInteger getSteerADValue() {
            return mSteerADValue;
        }

        private MInteger mLKLogoIndex = new MInteger(this, "lkLogoIndexListener", 1);

        public MInteger getmLKLogoIndex() {
            return mLKLogoIndex;
        }

        private MString mResetPassword = new MString(this, "resetPasswordListener", "123456");

        public MString getmResetPassword() {
            return mResetPassword;
        }

        private MInteger mSetVCom = new MInteger(this, "setVComListener", 15);
        public MInteger getVComValue() {
            return mSetVCom;
        }

        //其他APK会调用
        private MInteger uiStyle = new MInteger(this, "uiStyleListener", -1);
        public MInteger getUiStyle() {
            return uiStyle;
        }

        //保存配置文件
        private MString uiStyleDate = new MString(this, "uiStyleDateListener", "GB0120180130");
        public MString getUiStyleDate() {
            return uiStyleDate;
        }

        //设置调用、保存配置文件
        private MBoolean uiStyleShow = new MBoolean(this, "uiStyleShowListener", false);
        public MBoolean getUiStyleShow() {
            return uiStyleShow;
        }

        //配置文件保存，兼容之前做法
        private MIntegerArray uiStyleNumber = new MIntegerArray(this, "uiStyleNumberListener", new Integer[]{0,1,2});
        public MIntegerArray getUiStyleNumber() {
            return uiStyleNumber;
        }

        //配置文件保存，现有做法
        private MStringArray uiStyleDateArray = new MStringArray(this, "uiStyleDateArrayListener", new String[]{"GB0120180130", "GB0220180709", "GB0320180711"});
        public MStringArray getUiStyleDateArray() {
            return uiStyleDateArray;
        }

        //设置调用
        private MStringArray uiNameArray = new MStringArray(this, "uiStyleNumberListener", new String[]{"GB001","GB002","GB003"});
        public MStringArray getUiNameArray() {
            return uiNameArray;
        }

        //设置调用
        private MString uiStyleName = new MString(this, "uiStyleNameListener", "");
        public MString getUiStyleName() {
            return uiStyleName;
        }

        private MBoolean cameraSwitch = new MBoolean(this, "cameraSwitchListener", false);
        public MBoolean getCameraSwitch() {
            return cameraSwitch;
        }
        private MBoolean panoramicSwitch = new MBoolean(this, "panoramicSwitchListener", false);
        public MBoolean getPanoramicSwitch() {
            return panoramicSwitch;
        }
        private MInteger panoramicConnType = new MInteger(this, "panoramicConnTypeListener", 0);
        public MInteger getPanoramicConnType() {
            return panoramicConnType;
        }
        private MInteger panoramicVideoType = new MInteger(this, "panoramicVideoTypeListener", 1);
        public MInteger getPanoramicVideoType() {
            return panoramicVideoType;
        }

        private MInteger panoramicType = new MInteger(this, "panoramicTypeListener", 0);
        public MInteger getPanoramicType() {
            return panoramicType;
        }

        private MBoolean dvrEnable = new MBoolean(this, "dvrEnableListener", false);
        public MBoolean getDvrEnable() {
            return dvrEnable;
        }

        private MInteger avdd = new MInteger(this, "avddListener", 2);
        public MInteger getAvdd() {
            return avdd;
        }

        private MInteger mScreenBrightness = new MInteger(this, "screenBrightnessListener", 2);
        public MInteger getScreenBrightness() {
            return mScreenBrightness;
        }
        private MInteger mScreenColourtemp = new MInteger(this, "screenColourtempListener", 7);
        public MInteger getScreenColourtemp() {
            return mScreenColourtemp;
        }
        private MInteger mScreenSaturation = new MInteger(this, "screenSaturationListener", 9);
        public MInteger getScreenSaturation() {
            return mScreenSaturation;
        }
        private MInteger mScreenContrast = new MInteger(this, "screenContrastListener", 1);
        public MInteger getScreenContrast() {
            return mScreenContrast;
        }
        private MInteger mScreenSharpness = new MInteger(this, "screenSharpnessListener", 2);//2019-07-24：将锐度默认值改为2，并且手动设置无效。
        public MInteger getScreenSharpness() {
            return mScreenSharpness;
        }
        private MInteger mScreenDynamiccontrast = new MInteger(this, "screenDynamiccontrastListener", 1);
        public MInteger getScreenDynamiccontrast() {
            return mScreenDynamiccontrast;
        }
        private MInteger mScreenVcom = new MInteger(this, "screenVcomListener", DEFAULT_VCOM);
        public MInteger getScreenVcom() {
            return mScreenVcom;
        }
        private MInteger mScreenAvdd = new MInteger(this, "screenAvddListener", DEFAULT_AVDD);
        public MInteger getScreenAvdd() {
            return mScreenAvdd;
        }

        private MInteger mRotateVoltage = new MInteger(this, "rotateVoltageListener", 40);
        public MInteger getRotateVoltage() {
            return mRotateVoltage;
        }
        private MInteger mRotateTime = new MInteger(this, "rotateTimeListener", 60);
        public MInteger getRotateTime() {
            return mRotateTime;
        }

        private MByteArray panelKeyArray = new MByteArray(this, "panelKeyListener", null);
        public MByteArray getPanelKeyArray() {
            return panelKeyArray;
        }

        private MInteger mBlueMicGain = new MInteger(this, "blueMicGainListener", 3);//0-5 { "-6Db", "0Db", "6Db", "12Db", "18Db", "24Db" }
        public MInteger getBlueMicGain() {
            return mBlueMicGain;
        }

        //停车监控 0：关闭且开关不显示，1：开启且开关不显示，16：关闭且开关显示，17：开启且开关显示
        private MInteger monitorSwitch = new MInteger(this, "monitorSwitchListener", 0);
        public MInteger getMonitorSwitch() {return monitorSwitch;}

        private MInteger deviceVoltale = new MInteger(this, "deviceVoltaleListener", 255);//255：没有配置，0：指示12V电源， 1：指示24V电源
        public MInteger getDeviceVoltale() {return deviceVoltale;}
    }

    private void initUistyle() {
        //不采用异步的方式，避免出现用数字方式配置UI，导入配置文件无效的问题。2018-11-08
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                try {
                    if (FileUtil.checkExist(UI_STYLE_FILE_SYSTEM)) {
                        FileUtil.copyFile(UI_STYLE_FILE_SYSTEM, UI_STYLE_FILE_IMPORT);
                    }
                    if (!FileUtil.checkExist(UI_STYLE_FILE_IMPORT)) {
                        return;
                    }
                    List<Map<String,Object>> list = XmlUtils.readListXml(new FileInputStream(UI_STYLE_FILE_IMPORT));
                    LogUtils.e("initUistyle---list.size():" + list.size());
                    String client = getClientProject();

                    uiStyleList.clear();

                    for (Map map : list) {
                        if (map != null && client.equals(map.get("client"))) {
                            uiStyleList.add(map);
                        }
                    }
                    if (uiStyleList.size() <= 0) {
                        return;
                    }
//                    LogUtils.e("initUistyle---list.size():" + list.size() + ", size=" + uiStyleList.size());

                    int defaultUIStyle = Model().getUiStyle().getVal();
                    if (defaultUIStyle == -1) {
                        String uiDate = Model().getUiStyleDate().getVal();
                        for (Map<String, Object> map : uiStyleList) {
                            if (uiDate != null && uiDate.equals(map.get("date"))) {
                                defaultUIStyle = (int) map.get("id");
                                break;
                            }
                        }
                        //避免出现uiStyle为-1时导致主页报错。2018-10-31
                        if (defaultUIStyle == -1) {
                            defaultUIStyle = (int) uiStyleList.get(0).get("id");
                        }
                        Model().getUiStyle().setVal(defaultUIStyle);
                    }
                    Integer[] uiStyleArray = Model().getUiStyleNumber().getVal();

                    boolean mUiNull = (uiStyleArray == null ? true : false);
                    int num = uiStyleList.size();
                    String[] uiStyleDateArray = Model().getUiStyleDateArray().getVal();
                    if (mUiNull) {
                        if (uiStyleDateArray == null) {
                            return;
                        }
                        num = uiStyleDateArray.length;// > uiStyleList.size() ? uiStyleList.size() : uiStyleDateArray.length;
                    } else {
                        num = uiStyleArray.length;// > uiStyleList.size() ? uiStyleList.size() : uiStyleArray.length;
                    }
                    Integer[] uiNumber = new Integer[num];
                    int realLen = 0;
                    if (mUiNull) {
                        LogUtils.e("initUistyle---uiStyleDateArray=" + uiStyleDateArray.length + ", uiStyleList:"+uiStyleList.size());
                        for (int i = 0; i < uiStyleDateArray.length; i++) {
                            for (Map<String, Object> map : uiStyleList) {
//                                LogUtils.e("initUistyle---i=" + i + ", uiStyleDate[i]:" + uiStyleDateArray[i] + ", date=" + map.get("date"));
                                if (uiStyleDateArray[i].equals((String) map.get("date")) ||
                                        uiStyleDateArray[i].contains((String) map.get("date"))) {
                                    uiNumber[realLen] = (Integer) map.get("id");
                                    realLen++;
                                    break;
                                }
                            }
                        }
                    } else {//兼容之前的做法
                        LogUtils.e("initUistyle---len=" + uiStyleArray.length + ", uiStyle.size:" + uiStyleList.size());
                        uiStyleDateArray = new String[num];
                        for (int i = 0; i < uiStyleArray.length; i++) {
                            for (int j = 0; j < uiStyleList.size(); j++) {
//                                LogUtils.e("initUistyle---i="+i+", uiStyle[i]:" + uiStyleArray[i]+", j="+j+", id="+(int)uiStyleList.get(j).get("id"));
                                if (uiStyleArray[i] == (int) uiStyleList.get(j).get("id")) {
                                    uiNumber[realLen] = uiStyleArray[i];
                                    uiStyleDateArray[realLen] = (String) uiStyleList.get(j).get("date");
                                    realLen++;
                                    break;
                                }
                            }
                        }
                    }

                    if (realLen > 0) {
                        uiNumber = Arrays.copyOfRange(uiNumber, 0, realLen);
                        if (!mUiNull) {
                            uiStyleDateArray = Arrays.copyOfRange(uiStyleDateArray, 0, realLen);
                            Model().getUiStyleDateArray().setVal(uiStyleDateArray);
                        }
                    } else {
                        realLen = 1;//uiStyleList.size();//配置的不存在时，默认显示xml中的第一套，避免在设置暴露xml中全部的UI。2019-11-23
                        //当配置文件中配置的在XML中不存在时，会引起报错。
                        uiNumber = new Integer[realLen];
                        uiStyleDateArray = new String[realLen];
                        for (int n = 0; n < realLen; n++) {
                            uiNumber[n] = (int) uiStyleList.get(n).get("id");
                            //当配置文件中不配置UI，会导致空指针异常2018-11-25
                            uiStyleDateArray[n] = (String) uiStyleList.get(n).get("date");
                        }
                    }

                    uiNameArray = new String[realLen];
                    boolean find = false;
                    for (int i = 0; i < realLen; i++) {
//                        LogUtils.e("initUistyle---i="+i+", defaultUIStyle:" + defaultUIStyle+", uiNumber[i]="+uiNumber[i]);
                        if (defaultUIStyle == uiNumber[i]) {
                            find = true;

                            for (Map<String, Object> map : uiStyleList) {
                                if (defaultUIStyle == (int) map.get("id")) {
                                    String uiStyleDate = (String) map.get("date");
                                    Model().getUiStyleDate().setVal(uiStyleDate);
                                    break;
                                }
                            }
                        } else if ((i == (realLen - 1)) && !find) {
                            defaultUIStyle = uiNumber[0];
                            Model().getUiStyle().setVal(defaultUIStyle);
                            Model().getUiStyleDate().setVal(uiStyleDateArray[0]);
                        }


                        for (Map<String, Object> map : uiStyleList) {
                            if (uiNumber[i] == (int) map.get("id")) {
                                uiNameArray[i] = (String) map.get("name");
                            }

                            if (defaultUIStyle == (int) map.get("id")) {
                                Model().getUiStyleName().setVal((String) map.get("name"));
                            }
                        }
                    }

                    LogUtils.e("initUistyle---defaultUIStyle:" + defaultUIStyle + ", nameLen=" + uiNameArray.length);
                    Model().getUiNameArray().setVal(uiNameArray);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

//            }
//        }).start();

        //由于Settings在此类后启动，导致监听器未创建而没有写系统属性。
        SystemProperties.set("persist.sys.ui_style", Model().getUiStyle().getVal() + "");
        writeUIStyle(Model().getUiStyle().getVal() + "");
    }

    private String getClientProject() {
        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        String clientProject = BaseClientDriver.CLIENT_JS;
        if (client != null) {
            clientProject = client.getString("ClientProject");

        }
        return clientProject;
    }

    @Override
    public boolean getCloseScreen() {
        return mCloseScreen;
    }

    @Override
    public boolean getSupportOpenCamera() {
        return mSupportCamera;
    }

    @Override
    public boolean getIsNoTouchKey() {
        return mNoTouchKey;
    }

    @Override
    public boolean getSupportPort() {
        return mSupportPort;
    }

    @Override
    public boolean getSupportPoweroff() {
        return mSupportPoweroff;
    }

    @Override
    public boolean getSupportRightCamera() {
        return mSupportRightCamera;
    }

    @Override
    public int getAuxVideoType() {
        return mAuxVideoType;
    }

    @Override
    public int getRightVideoType() {
        return mRightVideoType;
    }

    @Override
    public void setSupportFrontCamera(boolean supportFrontCamera) {
        mSupportFrontCamera = supportFrontCamera;
    }
    @Override
    public boolean getSupportFrontCamera() {
        return mSupportFrontCamera;
    }
    @Override
    public void setFrontCameraTime(int time) {
        mFrontCameraTime = time;
    }
    @Override
    public int getFrontCameraTime() {
        return mFrontCameraTime;
    }
    @Override
    public void setShowOnlineUpgrade(boolean showOnlineUpgrade) {
        mShowOnlineUpgrade = showOnlineUpgrade;
    }
    @Override
    public boolean getShowOnlineUpgrade() {
        return mShowOnlineUpgrade;
    }
    @Override
    public void setBlueMicGain(int gain) {
        Model().getBlueMicGain().setVal(gain);
        FileUtil.write(gain + "", MICGAIN_NAME);
    }
    @Override
    public int getBlueMicGain() {
        return Model().getBlueMicGain().getVal();
    }
    @Override
    public boolean getWakeupPower() {
        return mWakeUpScreenPower;
    }

    private void setTimezone(String timezone) {
        if (timezone == null) {
            return;
        }
        String[] _id = TimeZone.getAvailableIDs();
        for (int i = 0; i < _id.length; i++) {
            if (timezone.equals(_id[i])) {
                LogUtils.e("setTimezone----i=" + i + ", zone=" + _id[i]);
                AlarmManager alarm = (AlarmManager) getMainContext().getSystemService(Context.ALARM_SERVICE);
                alarm.setTimeZone(_id[i]);
                break;
            }
        }
    }

    private String getTimezone() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getID();//获取时区id
    }

    @Override
    public byte[] getFanControlData() {
        byte[] data = new byte[2];
        data[0] = (byte) (mFanControlEnable ? 0x01 : 0x00);
        data[1] = (byte) mFanVolume;
        return data;
    }

    @Override
    public boolean getSupportAngle() {
        return mSupportAngle;
    }

    @Override
    public boolean getVoiceEnable() {
        return mVoiceEnable;
    }

    @Override
    public void setZhiNengTong(boolean zhiNengTong) {
        mZhiNengTong = zhiNengTong;

        //去掉Canbus开关，锐派竖屏直接通过智能通开关处理
        LogUtils.d("setZhiNengTong----mZhiNengTong=" + mZhiNengTong);
        Packet mPacket = new Packet();
        mPacket.putBoolean("CanSwitch", mZhiNengTong);
        mPacket.putString("Implementation", CanBusDefine.Implementation.MCU);
        mPacket.putInt("CanSeries", 1);
        ModuleManager.getLogicByName(CanBusDefine.MODULE).dispatch(CanBusDefine.MainDefine.CANBUS_SETTINGS, mPacket);
    }
    @Override
    public boolean getZhiNengTong() {
        return mZhiNengTong;
    }
    @Override
    public void setVideoDemo(boolean videoDemo) {
        mVideoDemo = videoDemo;
    }
    @Override
    public boolean getVideoDemo() {
        return mVideoDemo;
    }

    protected Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) {
            bytes[i++] = b;
        }
        return bytes;
    }

    byte[] toObjects(Byte[] bytesPrim) {
        byte[] bytes = new byte[bytesPrim.length];
        int i = 0;
        for (Byte b : bytesPrim) {
            bytes[i++] = b;
        }
        return bytes;
    }

    private String UI_STYLE = "/custom/uistyle.ini";
    private void writeUIStyle(String uiStyle) {
        LogUtils.d("BaseFactoryDriver", "writeUIStyle----" + uiStyle);

        if (uiStyle.equals(readUIStyle().trim())) {
            LogUtils.e("BaseFactoryDriver", "writeUIStyle----return uiStyle is save!");
            return;
        }

        try {
            byte[] bMsg = uiStyle.getBytes();
            FileOutputStream fOut = new FileOutputStream(UI_STYLE);
            fOut.write(bMsg);
            fOut.flush();
            fOut.getFD().sync();
            fOut.close();
        } catch (IOException e) {
            //throw the exception
        }
    }

    private String readUIStyle() {
        StringBuilder sb = new StringBuilder("");
        //打开文件输入流
        try {
            FileInputStream inputStream = new FileInputStream(UI_STYLE);

            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //读取文件内容
            while (len > 0) {
                sb.append(new String(buffer, 0, len));

                //继续将数据放到buffer中
                len = inputStream.read(buffer);
            }
            //关闭输入流
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
