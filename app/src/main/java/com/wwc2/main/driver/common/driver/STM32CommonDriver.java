package com.wwc2.main.driver.common.driver;


import android.provider.Settings;

import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.avin_interface.AvinInterface;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.GpsDataManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.settings_interface.SettingsDefine;

import java.util.Arrays;

/**
 * the stm32 EQ driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class STM32CommonDriver extends BaseCommonDriver {
    private final String TAG = "STM32CommonDriver";

    private final String GPS_NODE = "/sys/class/stpgps/mcugps";

    McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {
        @Override
        public void OpenListener(int status) {
            boolean checked = Model().getKeyTone().getVal();
            send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_AsternPressVolSet, new byte[]{(byte) 1, (byte) (checked ? 1 : 0)}, 2);

            //默认打开混音。GPS混音相关数据由ARM保存。2019-04-19
            setGpsMonitor(Model().getGpsMonitor().getVal());
            setGpsMix(Model().getGpsMix().getVal());
            setGpsMixRatio(Model().getGpsMixRatio().getVal());

            setDefSystemVolume(Model().getDefSystemVolume().getVal());
            setDefCallVolume(Model().getDefCallVolume().getVal());

            setReversingVolume(Model().getReversingVolume().getVal());

            setCameraSwitchTruck(Model().getCameraSwitchTruck().getVal());
        }

        @Override
        public void DataListener(byte[] val) {
            if ((val != null) && val.length == 3) {
                if ((int) (val[0] & 0xff) == McuDefine.MCU_TO_ARM.MRPT_GPS_SET_INFO) {
                    switch (val[1]) {
                        //默认打开混音。GPS混音相关数据由ARM保存。2019-04-19
                        case 0://gps监听
//                            Model().getGpsMonitor().setVal((val[2] == 0) ? false : true);
                            break;
                        case 1://GPS混音
//                            Model().getGpsMix().setVal((val[2] == 0) ? false : true);
                            break;
                        case 2: //GPS混音比例
//                            int ratio = val[2];
//                            if (ratio >= SettingsDefine.Common.MIXRATIO_MIN && ratio <= SettingsDefine.Common.MIXRATIO_MAX) {
//                                Model().getGpsMixRatio().setVal(ratio);
//                            }
                            break;
                        default:
                            break;
                    }
                } else if ((int) (val[0] & 0xff) == McuDefine.MCU_TO_ARM.MRPT_OTHER_SET_INFO) {
                    switch (val[1]) {
                        case 0://倒车音量
//                            Model().getReversingVolume().setVal((val[2] == 0) ? false : true);
                            LogUtils.e("Reversing:"+(val[2] == 0));
                            break;
                        case 1://按键音量
                            //有客户要求通过配置文件，所以不再接收MCU的开关状态，由Main自己处理。2018-11-14
//                            Model().getKeyTone().setVal((val[2] == 0) ? false : true);
                            LogUtils.e("KeyTone:"+(val[2] == 0));
                            break;
                        default:
                            break;
                    }
                } else if ((val[0] & 0xff) == McuDefine.MCU_TO_ARM.MRRT_PowerOn_Default_SourceVol) {
                    LogUtils.d(TAG, "Default_SourceVol val1=" + val[1] + ", val2=" + val[2]);
//                    switch (val[1]) {
//                        case 0: //系统音量
//                            Model().getDefSystemVolume().setVal((int) val[2]);
//                            break;
//                        case 1: //通话音量
//                            Model().getDefCallVolume().setVal((int) val[2]);
//                            break;
//                    }
                }
            } else if (val != null && val.length == 6) {
                if ((val[0] & 0xff) == McuDefine.MCU_TO_ARM.MRPT_colorfulLight) {
                    LogUtils.d(TAG, "colorfulLight val1=" + val[1] + ", val2=" + val[2] + ", val3=" + val[3] + ", val4=" + val[4] + ", val5=" + val[5]);
                    Model().getColorfulLightSwitch().setVal(val[1] == 1);
                    Model().getSmallLightSwitch().setVal(val[2] == 1);
                    Model().getFlicherSwitch().setVal(val[3] == 1);
                    Model().getFlicherRate().setVal(val[4] & 0xff);
                    Model().getColorfulLightColor().setVal(val[5] & 0xff);
                }
            } else if (val != null && val.length == 2) {
                //打开ARM混音，中音是否显示根据此状态判断。2019-04-19
                if ((val[0] & 0xFF) == McuDefine.MCU_TO_ARM.MRPT_GPS_MIX_SUPPORT) {
                    Model().getGpsMixSupport().setVal(val[1] == 0x01);
                } else if((val[0] & 0xFF) == McuDefine.MCU_TO_ARM.MRPT_colorfulLight_WYD) {
                    Model().getColorfulLightColor3Party().setVal(val[1] & 0xff);
                } else if ((val[0] & 0xFF) == McuDefine.MCU_TO_ARM.MPRT_Right_Camera) {
                    if (FactoryDriver.Driver().getSupportRightCamera()) {
                        boolean rightCamera = ((val[1] & 0xFF) == 1);
                        Packet packet = new Packet();
                        packet.putBoolean("rightCamera", rightCamera);
                        packet.putBoolean("mcudata", true);
                        ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.RIGHT_CAMERA, packet);
                    } else {
                        LogUtils.d("MPRT_Right_Camera---Config not support right camera!");
                    }
                } else if ((val[0] & 0xFF) == McuDefine.MCU_TO_ARM.MPRT_AUTO_BACKLIGHT) {
                    if (Model().getLightSensitive().getVal()) {
                        int backlight = val[1] & 0xFF;
                        Settings.System.putInt(getMainContext().getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS, backlight > 255 ? 255 : backlight);
                    }
                }
            } else if (val != null && ((val[0] & 0xFF) == McuDefine.MCU_TO_ARM.MRPT_GPS_DATA)) {
                try {
//                    String gpsString = new String(val, 1, val.length - 1, "utf-8");
//                    LogUtils.d("MRPT_GPS_DATA-----len=" + val.length + ", gpsString=" + gpsString);
                    if (PowerManager.isRuiPai()) {
                        byte[] data = Arrays.copyOfRange(val, 1, val.length);
//                    FileUtil.write(data, GPS_NODE);
                        //通过写虚拟串口的方式通知底层。
                        GpsDataManager.getInstance().sendData(data, data.length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        McuManager.getModel().bindListener(mMCUListener);
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        McuManager.getModel().unbindListener(mMCUListener);
        super.onDestroy();
    }

    @Override
    public void setBrakeWarning(boolean checked) {
        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        if (client != null) {
            String clientProject = client.getString("ClientProject");
            if (clientProject != null && clientProject.equals("ch010_23")) {//马自达刹车警告默认关，并隐藏设置项。
                return;
            }
        }
        Model().getBrakeWarning().setVal(checked);
    }

    @Override
    public void setReversingVolume(boolean checked) {
        Model().getReversingVolume().setVal(checked);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_AsternPressVolSet, new byte[]{(byte) 0, (byte) (checked ? 1 : 0)}, 2);
    }

    @Override
    public void setAnyKey(boolean checked) {
        Model().getAnyKey().setVal(checked);
    }

    @Override
    public void setKeyTone(boolean checked) {
        Model().getKeyTone().setVal(checked);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_AsternPressVolSet, new byte[]{(byte) 1, (byte) (checked ? 1 : 0)}, 2);
    }

    @Override
    public void setReverseImage(boolean checked) {
        Model().getReverseImage().setVal(checked);
    }

    @Override
    public void setGpsMonitor(boolean checked) {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_GpsAudioSet, new byte[]{(byte) 0, (byte) (checked ? 1 : 0)}, 2);
        Model().getGpsMonitor().setVal(checked);
    }

    @Override
    public void setGpsMix(boolean checked) {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_GpsAudioSet, new byte[]{(byte) 1, (byte) (checked ? 1 : 0)}, 2);
        Model().getGpsMix().setVal(checked);
    }

    @Override
    public void setGpsMixRatio(int ratio) {
        if (ratio >= SettingsDefine.Common.MIXRATIO_MIN && ratio <= SettingsDefine.Common.MIXRATIO_MAX) {
            Model().getGpsMixRatio().setVal(ratio);
            send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_GpsAudioSet, new byte[]{(byte) 2, (byte) ratio}, 2);
        }
    }

    @Override
    public void setNoretain3Party(boolean checked) {
        Model().getNoretain3Party().setVal(checked);
    }

    @Override
    public void setMediaJump(boolean checked) {
        Model().getMediaJump().setVal(checked);
    }

    @Override
    public void beep() {
        if (Model().getKeyTone().getVal()) {//有客户要求通过配置文件，所以不再接收MCU的开关状态，由Main自己处理。2018-11-14
            // 发送按键音
            byte[] data = new byte[1];
            data[0] = (byte) 0x01;
            //bein gzhongyang.hu modify for 7881,because new MCU  ACK is delay. //old sendMcu
            McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.RPT_KEY_SPEAKER, data, 1);
            //end
        } else {
            LogUtils.d(TAG, "beep switch is off!");
        }
    }

    @Override
    public void getGpsAudioInfo() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_GpsAudioGet, new byte[]{(byte) 0}, 1);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_GpsAudioGet, new byte[]{(byte) 1}, 1);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.MACK_GpsAudioGet, new byte[]{(byte) 2}, 1);
    }

    @Override
    public void setColorfulLightSwitch(boolean checked) {
        sendColorfulLightToMcu(checked, Model().getSmallLightSwitch().getVal(), Model().getFlicherSwitch().getVal(),
                Model().getFlicherRate().getVal(), Model().getColorfulLightColor().getVal());
        Model().getColorfulLightSwitch().setVal(checked);
    }

    @Override
    public void setSmallLightSwitch(boolean checked) {
        sendColorfulLightToMcu(Model().getColorfulLightSwitch().getVal(), checked, Model().getFlicherSwitch().getVal(),
                Model().getFlicherRate().getVal(), Model().getColorfulLightColor().getVal());
        Model().getSmallLightSwitch().setVal(checked);
    }

    @Override
    public void setFlicherSwitch(boolean checked) {
        sendColorfulLightToMcu(Model().getColorfulLightSwitch().getVal(), Model().getSmallLightSwitch().getVal(),
                checked, Model().getFlicherRate().getVal(), Model().getColorfulLightColor().getVal());
        Model().getFlicherSwitch().setVal(checked);
    }

    @Override
    public void setFlicherRate(int rate) {
        sendColorfulLightToMcu(Model().getColorfulLightSwitch().getVal(), Model().getSmallLightSwitch().getVal(),
                Model().getFlicherSwitch().getVal(), rate, Model().getColorfulLightColor().getVal());
        Model().getFlicherRate().setVal(rate);
    }

    @Override
    public void setColorfulLightColor(int color) {
        sendColorfulLightToMcu(Model().getColorfulLightSwitch().getVal(), Model().getSmallLightSwitch().getVal(),
                Model().getFlicherSwitch().getVal(), Model().getFlicherRate().getVal(), color);
        Model().getColorfulLightColor().setVal(color);
    }

    @Override
    public void setColorfulLightColor3Party(int color) {
        sendColorfulLightToMcu(color);
        Model().getColorfulLightColor3Party().setVal(color);
    }

    @Override
    public void setKeyShake(boolean keyShake) {
        LogUtils.d("setKeyShake---" + keyShake);
        Model().getKeyShake().setVal(keyShake);
    }

    @Override
    public void setLightSensitive(boolean lightSensitive) {
        LogUtils.d("setLightSensitive---" + lightSensitive);
        Model().getLightSensitive().setVal(lightSensitive);
    }

    @Override
    public void setReverseGuideLine(boolean checked) {
        Model().getReversingGuideLine().setVal(checked);
    }

    @Override
    public void setDefSystemVolume(int volume) {
        byte[] bytes = new byte[2];
        bytes[0] = 0x00;
        bytes[1] = (byte) volume;
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.RPT_Media_SourceVol, bytes, bytes.length);

        Model().getDefSystemVolume().setVal(volume);
    }

    @Override
    public void setDefCallVolume(int volume) {
        byte[] bytes = new byte[2];
        bytes[0] = 0x01;
        bytes[1] = (byte) volume;
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.RPT_Media_SourceVol, bytes, bytes.length);

        Model().getDefCallVolume().setVal(volume);
    }

    private void sendColorfulLightToMcu(boolean bColor, boolean bSmall, boolean bFlicher, int rate, int color) {
//        LogUtils.d(TAG, "sendColorfulLightToMcu--"+bColor+", "+bSmall+", "+bFlicher+", "+rate+", "+color);
        byte[] data = new byte[5];
        data[0] = (byte) (bColor ? 0x01 : 0x00);
        data[1] = (byte) (bSmall ? 0x01 : 0x00);
        data[2] = (byte) (bFlicher ? 0x01 : 0x00);
        data[3] = (byte) rate;
        data[4] = (byte) color;
        send2Mcu((byte) McuDefine.ARM_TO_MCU.REQ_colorfulLight, data, data.length);
    }

    private void sendColorfulLightToMcu(int color) {
        byte[] data = new byte[1];
        data[0] = (byte) color;
        send2Mcu((byte) McuDefine.ARM_TO_MCU.REQ_colorfulLight_WYD, data, data.length);
    }

    @Override
    public String getGprsApkName() {
        return Model().getGprsApkName().getVal();
    }

    @Override
    public void setGprsApkName(String apkName) {
        Model().getGprsApkName().setVal(apkName);
    }

    @Override
    public String getTpmsApkName() {
        return Model().getTpmsApkName().getVal();
    }

    @Override
    public void setTpmsApkName(String apkName) {
        Model().getTpmsApkName().setVal(apkName);
    }

    @Override
    public String getDvrApkName() {
        return Model().getDvrApkName().getVal();
    }

    @Override
    public void setDvrApkName(String apkName) {
        Model().getDvrApkName().setVal(apkName);
    }

    @Override
    public boolean getAutoLandPort() {
        return Model().getAutoLandPort().getVal();
    }

    @Override
    public void setAutoLandPort(boolean checked) {
        Model().getAutoLandPort().setVal(checked);
    }

    @Override
    public String getStartApkName() {
        return Model().getStartApkName().getVal();
    }

    @Override
    public void setStartApkName(String apkName) {
        Model().getStartApkName().setVal(apkName);
    }

    @Override
    public void setCameraSwitchTruck(boolean open) {
        LogUtils.d("setCameraSwitchTruck open=" + open);
        Model().getCameraSwitchTruck().setVal(open);

        byte[] data = new byte[1];
        data[0] = (byte) (open ? 0x01 : 0x00);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_CAMERA_SWITCH, data, data.length);
    }

    @Override
    public void setTurnLightSwitch(int type, boolean open) {
        switch (type) {
            case 1:
                Model().getTurnLightSwitchLeft().setVal(open);
                break;
            case 2:
                Model().getTurnLightSwitchRight().setVal(open);
                break;
        }
    }

    @Override
    public boolean getTurnLightSwitch(int type) {
        boolean ret = false;
        switch (type) {
            case 1:
                ret = Model().getTurnLightSwitchLeft().getVal();
                break;
            case 2:
                ret = Model().getTurnLightSwitchRight().getVal();
                break;
        }
        return ret;
    }

    void send2Mcu(byte cmd, byte[] data, int len) {
        LogUtils.d("CommonSwitch Send", byteTohexString(cmd) + " " + bytesTohexString(data));
        McuManager.sendMcu(cmd, data, len);
    }

    public static String bytesTohexString(byte[] data) {
        String str = "";
        for (byte d : data) {
            str += byteTohexString(d);
        }
        return str;
    }

    public static String byteTohexString(byte data) {
        String hexStr = "";
        if ((data & 0xf0) == 0) hexStr += "0";
        hexStr += Integer.toHexString(data & 0xff);
        hexStr += " ";
        return hexStr;
    }
}
