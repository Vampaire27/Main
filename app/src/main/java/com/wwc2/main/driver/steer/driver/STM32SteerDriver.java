package com.wwc2.main.driver.steer.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.McuManager;

/**
 * the stm32 steer driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class STM32SteerDriver extends BaseSteerDriver {
    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {
        @Override
        public void DataListener(byte[] val) {
            if (val != null) {
                switch ((int)val[0]&0xff) {
                    case McuDefine.MCU_TO_ARM.MPRT_SteerKeyinfo:
                        Byte[] bytes = new Byte[val.length];
                        for (int i = 0; i < val.length; i++) {
                            bytes[i] = val[i];
                        }
                        Model().getADKeyInfo().setValAnyway(bytes);
                        break;

                    case McuDefine.MCU_TO_ARM.MRPT_SteerStatus:
                        Model().getADKeyStatus().setValAnyway(val[1]);
                        break;

                    case McuDefine.MCU_TO_ARM.MPRT_PanelKeyinfo:
                        Byte[] bytesPanel = new Byte[val.length];
                        for (int i = 0; i < val.length; i++) {
                            bytesPanel[i] = val[i];
                        }
                        Model().getPanelKeyInfo().setValAnyway(bytesPanel);
                        break;
                    case McuDefine.MCU_TO_ARM.MRPT_PanelKeyStatus:
                        Model().getPanelKeyStatus().setValAnyway(val[1]);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        McuManager.getModel().bindListener(mMcuListener);
    }

    @Override
    public void onDestroy() {
        McuManager.getModel().unbindListener(mMcuListener);
        super.onDestroy();
    }

    @Override
    public void enterStudyMode() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerStatus, new byte[]{(byte) 0x01}, 1);
    }

    @Override
    public void exitStudyMode() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerStatus, new byte[]{(byte) 0x00}, 1);
    }

    @Override
    public void keyPressed(byte keyID) {
        Byte byt = SteerKeysDefine.getValue(keyID);
        if (byt != null) {
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerKey, new byte[]{byt}, 1);
        }
    }

    @Override
    public void keyStore() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerButton, new byte[]{0}, 1);
    }

    @Override
    public void keyReset() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerButton, new byte[]{1}, 1);
    }

    @Override
    public void keyClear() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerButton, new byte[]{2}, 1);
    }

    @Override
    public void keyStudy() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerButton, new byte[]{4}, 1);
    }

    @Override
    public void keyInfo() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_SteerButton, new byte[]{3}, 1);
    }

    void send2Mcu(byte cmd, byte[] data, int len) {
        LogUtils.d("SteerStudy Send", byteTohexString(cmd)+" "+ bytesTohexString(data));
        McuManager.sendMcu(cmd, data, len);
    }

    public static String bytesTohexString(byte[] data) {
        String str = "";
        for (byte d: data) {
            str += byteTohexString(d);
        }
        return str;
    }

    public static String byteTohexString(byte data) {
        String hexStr = "";
        if ((data & 0xf0) == 0) hexStr += "0";
        hexStr += Integer.toHexString(data&0xff);
        hexStr += " ";
        return hexStr;
    }

    @Override
    public void enterStudyMode_Panel() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_PanelKeyStatus, new byte[]{(byte) 0x01}, 1);
    }

    @Override
    public void exitStudyMode_Panel() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_PanelKeyStatus, new byte[]{(byte) 0x00}, 1);
    }

    @Override
    public void keyPressed_Panel(byte keyID) {
        Byte byt = SteerKeysDefine.getValue(keyID);
        if (byt != null) {
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_PanelKey, new byte[]{byt}, 1);
        }
    }

    @Override
    public void keyStore_Panel() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_PanelButton, new byte[]{0}, 1);
    }

    @Override
    public void keyReset_Panel() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_PanelButton, new byte[]{1}, 1);
    }

    @Override
    public void keyClear_Panel() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_PanelButton, new byte[]{2}, 1);
    }

    @Override
    public void keyStudy_Panel() {
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_PanelButton, new byte[]{4}, 1);
    }
}
