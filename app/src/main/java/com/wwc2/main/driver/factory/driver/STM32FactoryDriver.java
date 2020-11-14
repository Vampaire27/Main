package com.wwc2.main.driver.factory.driver;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.McuManager;
import com.wwc2.settings_interface.SettingsDefine;

import java.util.Arrays;

/**
 * the stm32 EQ driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class STM32FactoryDriver extends BaseFactoryDriver {

    /**
     * the mcu listener.
     */
    private McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            setVComValue(Model().getVComValue().getVal(),true);

            //请求收音机区域
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_RadioRequstregion, new byte[]{1}, 1);

            int module = Model().getRadioModule().getVal();
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioModule, new byte[]{(byte) module}, 1);

            //增加有源天线的配置
            setRadioAerial(Model().getRadioAerial().getVal());

            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioButton,
                    new byte[]{0x27, (byte) (Model().getRDSEnable().getVal() ? 0x01 : 0x00)}, 2);

            //通知MCU是否支持CVBS输出
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SupportCVBS, new byte[]{(byte) (mSupportCVBS ? 0x01 : 0x00)}, 1);

            int mMonitorSW = Model().getMonitorSwitch().getVal();
            int mDeivceVol = Model().getDeviceVoltale().getVal();
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_MONITOR_SWITCH,
                    new byte[]{(byte) mDeivceVol, (byte) (mMonitorSW & 0x01)}, 2);
        }

        @Override
        public void DataListener(byte[] val) {
            if (null != val && val.length > 1) {
                if (val[0] == (byte) (0xff & McuDefine.MCU_TO_ARM.MRPT_RadioRegion)) {
                    LogUtils.d("Radio Region", Integer.toHexString(val[1]));
                    switch (val[1]) {
                        case 0:
                            Model().getRadioRegion().setVal(Define.Factory.Regions.AMERICAS1.value());
                            break;//北美洲
                        case 1:
                            Model().getRadioRegion().setVal(Define.Factory.Regions.AMERICAS2.value());
                            break;//南美洲
                        case 2:
                            Model().getRadioRegion().setVal(Define.Factory.Regions.EUROPE.value());
                            break;//欧洲
                        case 3:
                            Model().getRadioRegion().setVal(Define.Factory.Regions.RUSSIA.value());
                            break;//东欧
                        case 4:
                            Model().getRadioRegion().setVal(Define.Factory.Regions.JAPAN.value());
                            break;//日本
                        case 5:
                            Model().getRadioRegion().setVal(Define.Factory.Regions.CN.value());
                            break;//中国
                    }
                } else if (val[0] == (byte) (McuDefine.MCU_TO_ARM.MRPT_soundChannel)) {
                    switch (val[1]) {
                        case 1:
                            Model().getVoiceChannel().setVal(SettingsDefine.VoiceChannel.IPOD);
                            break;
                        case 2:
                            Model().getVoiceChannel().setVal(SettingsDefine.VoiceChannel.AUX);
                            break;
                    }
                } else if (val[0] == (byte) (McuDefine.MCU_TO_ARM.MRPT_RadioAerial)) {
                    //增加有源天线的配置，不接收MCU的数据。2018-12-08
//                    Model().getRadioAerial().setVal(val[1] == 0x01);
                } else if (val[0] == (byte) (McuDefine.MCU_TO_ARM.MRPT_RadioModule)) {
                    int module = val[1] & 0xFF;
                    if (module == 0xFF) {
                        module = Model().getRadioModule().getVal();
                        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioModule, new byte[]{(byte) module}, 1);
                    } else {
                        Model().getRadioModule().setVal(val[1] & 0xFF);
                    }
                } else if (val[0] == (byte) McuDefine.MCU_TO_ARM.MRPT_STEER_AD) {
                    if (val.length > 2) {
                        int steerAD = ((val[1] << 8) & 0xFF00) | (val[2] & 0xFF);
                        Model().getSteerADValue().setVal(steerAD);
                    }
                } else if (val[0] == (byte) McuDefine.MCU_TO_ARM.MRPT_MCU_DEBUG_INFO) {
                    if (val.length > 17) {
                        Integer[] debug = new Integer[17];
                        for (int i = 0; i < 17; i++) {
                            debug[i] = val[i + 1] & 0xff;
                        }
//                        System.arraycopy(val, 1, debug, 0, debug.length);
                        Model().getMcuDebugInfo().setValAnyway(debug);
                    } else {
                        LogUtils.e("MCU_DEBUG", "mcu debug info len is len 15!");
                    }
                } else if (val[0] == McuDefine.MCU_TO_ARM.MRPT_RDSControl) {
                    if (val.length < 3) return;

                    boolean enable = (val[1] & 0x01) == 0x01;
                    Model().getRDSEnable().setVal(enable);
                } else if (val[0] == (byte) McuDefine.MCU_TO_ARM.MPRT_PanelKeyOutport) {
                    byte[] panelKey = Arrays.copyOfRange(val, 1, val.length);
                    Model().getPanelKeyArray().setVal(toObjects(panelKey));

                    if (mMemory != null) {
                        mMemory.save();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        McuManager.getModel().bindListener(mMCUListener);
    }

    @Override
    public void onDestroy() {
        McuManager.getModel().unbindListener(mMCUListener);
        super.onDestroy();
    }

    @Override
    public void setRadioRegion(String region) {
        if (region != null) {
            try {
                Define.Factory.Regions rg = Define.Factory.Regions.valueOf(region);
                if (rg == Define.Factory.Regions.ENTER) {
                    send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioRequstregion, new byte[]{(byte) 1}, 1);
                } else {
                    send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioSetregion, new byte[]{(byte) rg.index()}, 1);
                    Model().getRadioRegion().setVal(region);
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Override
    public void requestMcuDebugInfo(int value) {
        byte[] data = new byte[1];
        data[0] = (byte) value;
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_REQUEST_MCU_DEBUG, data, data.length);
    }

    void send2Mcu(byte cmd, byte[] data, int len) {
        LogUtils.d("Factory Send", byteTohexString(cmd) + " " + bytesTohexString(data));
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

    @Override
    public void setVoiceChannel(String channel) {
        if (SettingsDefine.VoiceChannel.AUX.equals(channel)) {
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_soundChannel, new byte[]{(byte) 2}, 1);
        } else {
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_soundChannel, new byte[]{(byte) 1}, 1);
        }
        Model().getVoiceChannel().setVal(channel);
    }

    @Override
    public void setRadioAerial(boolean open) {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioAerial, new byte[]{(byte) (open ? 0x01 : 0x00)}, 1);
        Model().getRadioAerial().setVal(open);
    }

    @Override
    public void setRadioModule(int module) {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioModule, new byte[]{(byte) module}, 1);
        Model().getRadioModule().setVal(module);
    }

    @Override
    public void setRDSEnable(boolean enable) {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_RadioButton, new byte[]{0x27, (byte) (enable ? 0x01 : 0x00)}, 2);
        Model().getRDSEnable().setVal(enable);
    }

    @Override
    public boolean getRDSEnable() {
        return Model().getRDSEnable().getVal();
    }

    @Override
    public void setVComValue(int vComValue,boolean save) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) vComValue;
        bytes[1] = (byte) (save ? 1 : 0);
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_SET_VCOM, bytes, bytes.length);

        Model().getVComValue().setVal(vComValue);
        if (mMemory != null && save) {
            mMemory.save();
        }
    }


    @Override
    public int getUiStyle() {
        return Model().getUiStyle().getVal();
    }

    @Override
    public boolean getUiStyleShow() {
        return Model().getUiStyleShow().getVal();
    }

    @Override
    public void setCameraSwitch(boolean sw) {
        Model().getCameraSwitch().setVal(sw);
    }

    @Override
    public boolean getCameraSwitch() {
        return Model().getCameraSwitch().getVal();
    }

    @Override
    public void setPanoramicSwitch(boolean sw) {
        Model().getPanoramicSwitch().setVal(sw);
    }

    @Override
    public boolean getPanoramicSwitch() {
        return Model().getPanoramicSwitch().getVal();
    }

    @Override
    public void setPanoramicConnType(int type) {
        Model().getPanoramicConnType().setVal(type);
    }

    @Override
    public int getPanoramicConnType() {
        return Model().getPanoramicConnType().getVal();
    }

    @Override
    public void setPanoramicVideoType(int type) {
        Model().getPanoramicVideoType().setVal(type);

        if (mMemory != null) {
            mMemory.save();
        }
    }

    @Override
    public int getPanoramicVideoType() {
        return Model().getPanoramicVideoType().getVal();
    }

    @Override
    public void setPanoramicType(int type) {
        Model().getPanoramicType().setVal(type);
    }

    @Override
    public int getPanoramicType() {
        return Model().getPanoramicType().getVal();
    }

    @Override
    public String getFactoryPassword() {
        return Model().getmFactoryPasswd().getVal();
    }

//    @Override
//    public boolean getUiStyleShow() {
//        return Model().getUiStyleShow().getVal();
//    }
//
//    @Override
//    public Integer[] getUiStyleNumber() {
//        return Model().getUiStyleNumber().getVal();
//    }

    @Override
    public void setCameraPower(boolean power) {
        mCameraPower = power;
    }

    @Override
    public boolean getCameraPower() {
        return mCameraPower;
    }

    @Override
    public void setRotateVoltage(int voltage) {
        LogUtils.d("setRotateVoltage----voltage=" + voltage);
        Model().getRotateVoltage().setVal(voltage);

        byte[] data1 = new byte[4];
        data1[0] = (byte) voltage;
        data1[1] = (byte) (Model().getRotateTime().getVal() & 0xFF);
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ROTATE_PARAM, data1, 4);
    }
    @Override
    public int getRotateVoltage() {
        return Model().getRotateVoltage().getVal();
    }
    @Override
    public void setRotateTime(int time) {
        LogUtils.d("setRotateTime----time=" + time);
        Model().getRotateTime().setVal(time);

        byte[] data1 = new byte[4];
        data1[0] = (byte) (Model().getRotateVoltage().getVal() & 0xFF);
        data1[1] = (byte) time;
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ROTATE_PARAM, data1, 4);
    }
    @Override
    public int getRotateTime() {
        return Model().getRotateTime().getVal();
    }

    @Override
    public void setMonitorInfo(int type, int value) {
        if (type == 1) {
            int ret = Model().getMonitorSwitch().getVal();
            ret = (ret & 0x10) | value;
            Model().getMonitorSwitch().setVal(ret);
        } else if (type == 2) {
            Model().getDeviceVoltale().setVal(value);
        }

        int mMonitorSW = Model().getMonitorSwitch().getVal();
        int mDeivceVol = Model().getDeviceVoltale().getVal();
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_MONITOR_SWITCH,
                new byte[]{(byte) mDeivceVol, (byte) (mMonitorSW & 0x01)}, 2);
    }

    @Override
    public int getMonitorInfo(int type) {
        int ret = 0;
        switch (type) {
            case 1:
                ret = Model().getMonitorSwitch().getVal();
                break;
            case 2:
                ret = Model().getDeviceVoltale().getVal();
                break;
        }
        return ret;
    }
}
