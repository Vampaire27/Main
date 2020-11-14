package com.wwc2.main.driver.common.driver;

import android.os.SystemProperties;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.common.CommonDriverable;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.settings_interface.SettingsDefine;

/**
 * the base version driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class BaseCommonDriver extends BaseMemoryDriver implements CommonDriverable {

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            boolean value = false;
            value = Model().getBrakeWarning().getVal();
            mMemory.set("GLOBAL", "brake_warning", value+"");

            //begin zhongyang.hu remove for ReversingVolume  use MCU value
            value = Model().getReversingVolume().getVal();
            mMemory.set("GLOBAL", "reversing_volume", value + "");
            //end

            value = Model().getAnyKey().getVal();
            mMemory.set("GLOBAL", "any_key", value+"");
            //有客户要求通过配置文件，所以不再接收MCU的开关状态，由Main自己处理。2018-11-14
           //begin zhongyang.hu remove for key_tone  use MCU value
            value = Model().getKeyTone().getVal();
            mMemory.set("GLOBAL", "key_tone", value+"");
           //end
            value = Model().getReverseImage().getVal();
            mMemory.set("GLOBAL", "reverse_image", value+"");

            value = Model().getGpsMonitor().getVal();
            mMemory.set("GLOBAL", "gps_monitor", value+"");

            value = Model().getGpsMix().getVal();
            mMemory.set("GLOBAL", "gps_mix", value+"");

            int val = Model().getGpsMixRatio().getVal();
            mMemory.set("GLOBAL", "gps_mix_ratio", val+"");

            value = Model().getNoretain3Party().getVal();
            mMemory.set("GLOBAL", "noretain_3party", value+"");

            value = Model().getMediaJump().getVal();
            mMemory.set("GLOBAL", "media_jump", value+"");

            value = Model().getReversingGuideLine().getVal();
            mMemory.set("GLOBAL", "guide_line", value+"");

            value = Model().getGpsMixSupport().getVal();
            mMemory.set("GLOBAL", "gps_mix_support", value + "");

            String name = Model().getGprsApkName().getVal();
//            mMemory.set("GLOBAL", "gprs_apk_name", name);

            name = Model().getTpmsApkName().getVal();
            mMemory.set("GLOBAL", "tpms_apk_name", name);

            name = Model().getDvrApkName().getVal();
            mMemory.set("GLOBAL", "dvr_apk_name", name);

            val = Model().getDefSystemVolume().getVal();
            mMemory.set("GLOBAL", "default_system_vol", val + "");

            val = Model().getDefCallVolume().getVal();
            mMemory.set("GLOBAL", "default_blue_vol", val + "");

            value = Model().getKeyShake().getVal();
            mMemory.set("GLOBAL", "key_shake", value + "");

            value = Model().getLightSensitive().getVal();
            mMemory.set("GLOBAL", "light_sensitive", value + "");

            value = Model().getCameraSwitchTruck().getVal();
            mMemory.set("GLOBAL", "camera_switch_truck", value + "");

            value = Model().getTurnLightSwitchRight().getVal();
            mMemory.set("GLOBAL", "turnlight_switch_right", value + "");

            value = Model().getTurnLightSwitchLeft().getVal();
            mMemory.set("GLOBAL", "turnlight_switch_left", value + "");

            ret = true;
        }
        return ret;
    }

    @Override
    public BaseModel newModel() {
        return new CommonModel();
    }

    /**
     * get the model object.
     */
    protected CommonModel Model() {
        CommonModel ret = null;
        BaseModel model = getModel();
        if (model instanceof CommonModel) {
            ret = (CommonModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "GlobalDataConfig.ini";
    }

    /**数据Model*/
    protected static class CommonModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet ret = new Packet();

            ret.putBoolean(SettingsDefine.Common.Switch.AUTO_LAND_PORT.name(), getAutoLandPort().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.BRAKE_WARNING.value() ,getBrakeWarning().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.REVERSING_VOLUME.value() ,getReversingVolume().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.ANY_KEY.value() ,getAnyKey().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.KEY_TONE.value() ,getKeyTone().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.REVERSE_IMAGE.value() ,getReverseImage().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.GPS_MONITOR.value() ,getGpsMonitor().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.GPS_MIX.value() ,getGpsMix().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.MEDIA_JUMP.value() ,getMediaJump().getVal());
            ret.putInt(SettingsDefine.Common.Switch.GPS_MIX_RATIO.value() ,getGpsMixRatio().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.SWITCH_MODE_NORETAIN_3PARTY.value(), getNoretain3Party().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.COLORFUL_LIGHT_SWITCH.value(), getColorfulLightSwitch().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.SMALL_LIGHT_SWITCH.value(), getSmallLightSwitch().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.FLICHER_SWITCH.value(), getFlicherSwitch().getVal());
            ret.putInt(SettingsDefine.Common.Switch.FLICHER_RATE.value(), getFlicherRate().getVal());
            ret.putInt(SettingsDefine.Common.Switch.COLORFUL_LIGHT_COLOR.value(), getColorfulLightColor().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.REVERSE_GUIDE_LINE.value(), getReversingGuideLine().getVal());
            ret.putInt(SettingsDefine.Common.Switch.DEFAULT_SYSTEM_VOLUME.value(), getDefSystemVolume().getVal());
            ret.putInt(SettingsDefine.Common.Switch.DEFAULT_CALL_VOLUME.value(), getDefCallVolume().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.GPS_MIX_SUPPORT.value(), getGpsMixSupport().getVal());
            ret.putString(SettingsDefine.Common.Switch.GPRS_APK_NAME.name(), getGprsApkName().getVal());
            ret.putString(SettingsDefine.Common.Switch.TMPS_APK_NAME.name(), getTpmsApkName().getVal());
            ret.putString(SettingsDefine.Common.Switch.DVR_APK_NAME.name(), getDvrApkName().getVal());
            ret.putInt("COLORFUL_LIGHT_COLOR_3PARTY", getColorfulLightColor3Party().getVal());
            LogUtils.d("base getInfo COLORFUL_LIGHT_COLOR =" + getColorfulLightColor3Party().getVal());

            ret.putBoolean(SettingsDefine.Common.Switch.KEY_SHAKE.value(), getKeyShake().getVal());
            ret.putBoolean(SettingsDefine.Common.Switch.LIGHT_SENSITIVE.value(), getLightSensitive().getVal());

            ret.putBoolean("CAMERA_SWITCH_TRUCK", getCameraSwitchTruck().getVal());

            ret.putBoolean("TURNLIGHT_SWITCH_LEFT", getTurnLightSwitchLeft().getVal());
            ret.putBoolean("TURNLIGHT_SWITCH_RIGHT", getTurnLightSwitchRight().getVal());

            return ret;
        }

        public MBoolean mBrakeWarning = new MBoolean(this, "BrakeWarningListener", false);
        public MBoolean getBrakeWarning() {return mBrakeWarning;}
        public MBoolean mReversingVolume = new MBoolean(this, "ReversingVolumeListener", false);
        public MBoolean getReversingVolume() {return mReversingVolume;}
        public MBoolean mAnyKey = new MBoolean(this, "AnyKeyListener", false);
        public MBoolean getAnyKey() {return mAnyKey;}
        public MBoolean mKeyTone = new MBoolean(this, "KeyToneListener", true);
        public MBoolean getKeyTone() {return mKeyTone;}
        public MBoolean mReverseImage = new MBoolean(this, "ReverseImageListener", false);
        public MBoolean getReverseImage() {return mReverseImage;}
        public MBoolean mGpsMonitor = new MBoolean(this, "GpsMonitorListener", true);
        public MBoolean getGpsMonitor() {return mGpsMonitor;}
        public MBoolean mGpsMix = new MBoolean(this, "GpsMixListener", true);
        public MBoolean getGpsMix() {return mGpsMix;}
        public MInteger mGpsMixRatio = new MInteger(this, "GpsMixRatioListener", 7);
        public MInteger getGpsMixRatio() {return mGpsMixRatio;}
        public MBoolean mNoretain3Party = new MBoolean(this, "Noretain3PartyListener", false);
        public MBoolean getNoretain3Party() {return mNoretain3Party;}
        public MBoolean mMediaJump = new MBoolean(this, "MediaJumpListener", false);
        public MBoolean getMediaJump() {return mMediaJump;}
        public MBoolean mColorfulLightSwitch = new MBoolean(this, "ColorfulLightSwitchListener", false);
        public MBoolean getColorfulLightSwitch() {return mColorfulLightSwitch;}
        public MBoolean mSmallLightSwitch = new MBoolean(this, "SmallLightSwitchListener", false);
        public MBoolean getSmallLightSwitch() {return mSmallLightSwitch;}
        public MBoolean mFlicherSwitch = new MBoolean(this, "FlicherSwitchListener", false);
        public MBoolean getFlicherSwitch() {return mFlicherSwitch;}
        public MInteger mFlicherRate = new MInteger(this, "FlicherRateListener", 0);
        public MInteger getFlicherRate() {return mFlicherRate;}
        public MInteger mColorfulLightColor = new MInteger(this, "ColorfulLightColorListener", 0);
        public MInteger getColorfulLightColor() {return mColorfulLightColor;}
        public MInteger mColorfulLightColor3Party = new MInteger(this, "ColorfulLightColorListener3Party", 0);
        public MInteger getColorfulLightColor3Party() {return mColorfulLightColor3Party;}
        private MBoolean reversingGuideLine = new MBoolean(this, "reversingGuideLineListener", false);
        public MBoolean getReversingGuideLine() {return reversingGuideLine;}

        private MInteger defSystemVolume = new MInteger(this, "defSystemVolumeListener", Define.VOLUME_DEFAULT_VALUE);
        public MInteger getDefSystemVolume() {return defSystemVolume;}
        private MInteger defCallVolume = new MInteger(this, "defCallVolumeListener", Define.VOLUME_DEFAULT_VALUE);
        public MInteger getDefCallVolume() {return defCallVolume;}

        public MBoolean getGpsMixSupport() {return mGpsMixSupport;}
        public MBoolean mGpsMixSupport = new MBoolean(this, "GpsMixSupportListener", true);

        private MString mGprsApkName = new MString(this, "GprsApkNameListener", "com.jiashen");
        public MString getGprsApkName() {
            return mGprsApkName;
        }

        private MString mTpmsApkName = new MString(this, "TpmsApkNameListener", "");
        public MString getTpmsApkName() {
            return mTpmsApkName;
        }

        private MString mDvrApkName = new MString(this, "DvrApkNameListener", "");
        public MString getDvrApkName() {
            return mDvrApkName;
        }

        public MBoolean mAutoLandPort = new MBoolean(this, "AutoLandPortListener", false);
        public MBoolean getAutoLandPort() {return mAutoLandPort;}

        private MString mStartApkName = new MString(this, "StartApkNameListener", "");
        public MString getStartApkName() {
            return mStartApkName;
        }

        public MBoolean mKeyShake = new MBoolean(this, "KeyShakeListener", false);
        public MBoolean getKeyShake() {return mKeyShake;}

        public MBoolean mLightSensitive = new MBoolean(this, "LightSensitiveListener", false);
        public MBoolean getLightSensitive() {return mLightSensitive;}

        private MBoolean mCameraSwitchTruck = new MBoolean(this, "cameraSwitchTruckListener", false);
        public MBoolean getCameraSwitchTruck() {return mCameraSwitchTruck;}

        private MBoolean mTurnLightSwitchLeft = new MBoolean(this, "turnLightSwitchLeftListener", false);
        public MBoolean getTurnLightSwitchLeft() {return mTurnLightSwitchLeft;}

        private MBoolean mTurnLightSwitchRight = new MBoolean(this, "turnLightSwitchRightListener", false);
        public MBoolean getTurnLightSwitchRight() {return mTurnLightSwitchRight;}

    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            try {
                Object object = null;
                boolean value = false;
                object = mMemory.get("GLOBAL", "brake_warning");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getBrakeWarning().setVal(value);
                    ret = true;
                }
                //begin zhongyang.hu remove for key_tone  use MCU value
                  object = mMemory.get("GLOBAL", "reversing_volume");
                  if (null != object) {
                      value = Boolean.parseBoolean((String)object);
                      Model().getReversingVolume().setVal(value);
                      ret = true;
                  }
                //end zhongyang.hu remove for key_tone  use MCU value
                object = mMemory.get("GLOBAL", "any_key");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getAnyKey().setVal(value);
                    ret = true;
                }

                //有客户要求通过配置文件，所以不再接收MCU的开关状态，由Main自己处理。2018-11-14
                //begin zhongyang.hu remove for key_tone  use MCU value
                object = mMemory.get("GLOBAL", "key_tone");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getKeyTone().setVal(value);
                    ret = true;
                }
                //end

                object = mMemory.get("GLOBAL", "reverse_image");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getReverseImage().setVal(value);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "gps_monitor");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getGpsMonitor().setVal(value);
                    ret = true;
                }

                boolean bGW = false;//isGWVersion();//IGO地图要去掉混音，2020-03-04
                LogUtils.d("BaseCommonDriver", "isGWVersion=====" + bGW);
                object = mMemory.get("GLOBAL", "gps_mix");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    if (bGW) {
                        value = true;
                    }
                    Model().getGpsMix().setVal(value);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "gps_mix_ratio");
                if (null != object) {
                    int val = Integer.parseInt((String) object);
                    if (bGW) {
                        val = 7;
                    }
                    Model().getGpsMixRatio().setVal(val);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "noretain_3party");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getNoretain3Party().setVal(value);
                    ret = true;
                }
                object = mMemory.get("GLOBAL", "media_jump");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getMediaJump().setVal(value);
                    ret = true;
                }
                object = mMemory.get("GLOBAL", "guide_line");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getReversingGuideLine().setVal(value);
                    ret = true;
                }
                object = mMemory.get("GLOBAL", "gps_mix_support");
                if (null != object) {
                    value = Boolean.parseBoolean((String) object);
                    Model().getGpsMixSupport().setVal(value);
                    ret = true;
                }

//                object = mMemory.get("GLOBAL", "gprs_apk_name");
//                if (null != object) {
//                    String name = (String) object;
//                    Model().getGprsApkName().setVal(name);
//                    ret = true;
//                }

                object = mMemory.get("GLOBAL", "tpms_apk_name");
                if (null != object) {
                    String name = (String) object;
                    Model().getTpmsApkName().setVal(name);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "dvr_apk_name");
                if (null != object) {
                    String name = (String) object;
                    Model().getDvrApkName().setVal(name);
                    ret = true;
                }

                int totalTime = -1;
                int hour = -1;
                int accoffTime = -1;
                object = mMemory.get("GLOBAL", "reboot_time");
                if (null != object) {
                    String time = (String) object;
                    totalTime = Integer.parseInt(time);
                    ret = true;
                }
                object = mMemory.get("GLOBAL", "reboot_hour");
                if (null != object) {
                    String hourStr = (String) object;
                    hour = Integer.parseInt(hourStr);
                    ret = true;
                }
                object = mMemory.get("GLOBAL", "reboot_accoff");
                if (null != object) {
                    String accoffStr = (String) object;
                    accoffTime = Integer.parseInt(accoffStr);
                    ret = true;
                }
                if (totalTime != -1 && hour != -1 && accoffTime != -1) {
                    STM32MCUDriver.sendTestTimeToMcu(totalTime, hour, accoffTime);
                }

                object = mMemory.get("GLOBAL", "start_apk_name");
                if (null != object) {
                    String name = (String) object;
                    Model().getStartApkName().setVal(name);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "default_system_vol");
                if (null != object) {
                    int vol = Integer.parseInt((String) object);
                    Model().getDefSystemVolume().setVal(vol);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "default_blue_vol");
                if (null != object) {
                    int vol = Integer.parseInt((String) object);
                    Model().getDefCallVolume().setVal(vol);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "key_shake");
                if (null != object) {
                    boolean shake = Boolean.parseBoolean((String) object);
                    Model().getKeyShake().setVal(shake);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "light_sensitive");
                if (null != object) {
                    boolean light = Boolean.parseBoolean((String) object);
                    Model().getLightSensitive().setVal(light);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "camera_switch_truck");
                if (null != object) {
                    boolean open = Boolean.parseBoolean((String) object);
                    Model().getCameraSwitchTruck().setVal(open);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "turnlight_switch_right");
                if (null != object) {
                    boolean open = Boolean.parseBoolean((String) object);
                    Model().getTurnLightSwitchRight().setVal(open);
                    ret = true;
                }

                object = mMemory.get("GLOBAL", "turnlight_switch_left");
                if (null != object) {
                    boolean open = Boolean.parseBoolean((String) object);
                    Model().getTurnLightSwitchLeft().setVal(open);
                    ret = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static boolean isGWVersion() {
        boolean ret = false;
        String area = SystemProperties.get("ro.system_area", "");
        if (!TextUtils.isEmpty(area)) {
            if (area.equals("gwai")) {
                ret = true;
            }
        }
        return ret;
    }
}
