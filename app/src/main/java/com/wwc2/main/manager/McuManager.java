package com.wwc2.main.manager;

import android.content.Context;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.data.ByteUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.McuStatusDefine;
import com.wwc2.main.driver.mcu.McuDriver;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.eventinput.driver.STM32EventInputDriver;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

import java.util.List;

/**
 * the MCU manager.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class McuManager {

    /**
     * tag
     */
    private static final String TAG = "McuManager";

    //begin zhongyang.hu add for uart open 20170508
    private static boolean isUartOpen= false;
    //end
    private static boolean mInLogo = false;

    private static STM32EventInputDriver mEventInputDriver = null;

    private static boolean mFirstPlay = true;

    /**
     * MCU监听器
     */
    public static class MCUListener extends BaseListener {

        @Override
        public String getClassName() {
            return MCUListener.class.getName();
        }

        /**
         * 串口已打开, see {@link McuStatusDefine.OpenStatus}
         */
        public void OpenListener(int status) {

        }

        /**
         * 串口已关闭, see {@link McuStatusDefine.CloseStatus}
         */
        public void CloseListener(int status) {

        }

        /**
         * 数据
         */
        public void DataListener(byte[] val) {

        }
    }

    /**
     * MCU Model
     */
    protected static class MCUModel extends BaseModel {

        @Override
        public Packet getInfo() {
            return null;
        }
    }

    /**
     * Model对象
     */
    private static MCUModel mModel = new MCUModel();

    /**
     * 获取Model对象
     */
    public static BaseModel getModel() {
        return mModel;
    }

    /**
     * MCU manager create method.
     */
    public static void onCreate(Packet packet) {
        final int status = McuDriver.Driver().open();
        //begin zhongyang.hu add for uart open 20170508
        isUartOpen= true;
        //end
        // 打开
        BaseModel model = getModel();
        List<BaseListener> listeners = model.getListeners();
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i) instanceof McuManager.MCUListener) {
                McuManager.MCUListener listener = (McuManager.MCUListener) listeners.get(i);
                if (null != listener) {
                    listener.OpenListener(status);
                }
            }
        }
    }

    /**
     * MCU manager destroy method.
     */
    public static void onDestroy() {
        McuDriverable mcuDriver = McuDriver.Driver();
        int status = -1;
        //begin zhongyang.hu add for uart open 20170508
        isUartOpen= false;
        //end
        if (mcuDriver != null) {
            status = McuDriver.Driver().close();
        }


        // 关闭
        BaseModel model = getModel();
        List<BaseListener> listeners = model.getListeners();
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i) instanceof McuManager.MCUListener) {
                McuManager.MCUListener listener = (McuManager.MCUListener) listeners.get(i);
                if (null != listener) {
                    listener.CloseListener(status);
                }
            }
        }
    }

    /**
     * MCU manager get first boot status.
     */
    public static int firstBoot() {
        int ret = Define.FirstBoot.DEFAULT;
        McuDriverable driverable = McuDriver.Driver();
        if (null != driverable) {
            ret = driverable.firstBoot();
        }
        return ret;
    }

    /**
     * MCU manager get memory source.
     */
    public static int getMemorySource() {
        return McuDriver.Driver().getMemorySource();
    }

    /**
     * 发送MCU数据
     *
     * @param priority 串口发送优先级
     * @param check    true表示重复数据检查并替换，false不进行检查
     * @param head     头码
     * @param buf      数据
     * @param len      数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    public static int sendMcu(int priority, boolean check, byte head, byte[] buf, int len) {
        int ret = -1;
        McuDriverable driverable = McuDriver.Driver();
        if (null != driverable) {
            ret = driverable.sendMcu(true,priority, check, head, buf, len);
        }
        return ret;
    }

    /**
     * 发送MCU数据，针对模块重要的数据
     *
     * @param head 头码
     * @param buf  数据
     * @param len  数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    public static int sendMcuImportant(byte head, byte[] buf, int len) {
        int ret = -1;
        McuDriverable driverable = McuDriver.Driver();
        if (null != driverable) {
            ret = driverable.sendMcuImportant(head, buf, len);
        }
        return ret;
    }

    /**
     * 发送MCU数据，普通发送方式
     *
     * @param head 头码
     * @param buf  数据
     * @param len  数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    public static int sendMcu(byte head, byte[] buf, int len) {
        int ret = -1;
        McuDriverable driverable = McuDriver.Driver();
        if (null != driverable) {
            ret = driverable.sendMcu(head, buf, len);
        }
        return ret;
    }


    /**
     * 发送MCU数据，普通发送方式
     *
     * @param head 头码
     * @param buf  数据
     * @param len  数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    public static int sendMcuNack(byte head, byte[] buf, int len) {
        int ret = -1;
        McuDriverable driverable = McuDriver.Driver();
        if (null != driverable) {
            ret = driverable.sendMcuNack(head, buf, len);
        }
        return ret;
    }

    /**将发送ACK数据打包*/
    private static final byte ArmAck            = (byte)0x7F;
    public static void sendAckData(byte[] buffer) {
        sendMcuNack(ArmAck, new byte[]{buffer[0]}, 1);
    }


    /**
     * MCU上抛的数据入口
     *
     * @param buf 数据
     * @param len 数据长度
     */
    public static void dispatch(byte[] buf, int len) {
        if (null != buf && len > 0) {
            byte[] data = ByteUtils.cutBytes(0, len, buf);

            if (data[0] == (byte) McuDefine.MCU_TO_ARM.MRPT_CarReverse) {
                boolean camera = 1 == data[1];
                LogUtils.d(TAG, "McuCamera = " + camera);
                if (mEventInputDriver != null) {
                    mEventInputDriver.setCamera(camera);
                    return;
                } else {
                    LogUtils.e(TAG, "McuCamera = " + camera);
                }
            } else if (data[0] == (byte) McuDefine.MCU_TO_ARM.MRPT_DSP_DATA) {
                if (buf.length > 2) {
                    byte[] dspData = new byte[buf.length - 1];
                    System.arraycopy(buf, 1, dspData, 0, buf.length - 1);
                    if (PowerManager.isPortProject() && !PowerManager.isRuiPai_SP()) {
                        DspManager.getInstance().sendData(dspData, buf.length - 1);
                    }
                    if (PowerManager.isTaxiClient()) {
                        TaxiManager.getInstance().sendData(dspData, buf.length - 1);
                    }
                    return;
                }
            }
            // 发送MCU数据
            BaseModel model = getModel();
            List<BaseListener> listeners = model.getListeners();
            for (int i = 0; i < listeners.size(); i++) {
                if (listeners.get(i) instanceof McuManager.MCUListener) {
                    McuManager.MCUListener listener = (McuManager.MCUListener) listeners.get(i);
                    if (null != listener) {
                        listener.DataListener(data);
                    }
                }
            }
        }
    }

    /**
     * 通信产生错误
     *
     * @param no 错误码
     */
    public static void error(int no) {
        LogUtils.d("Jni to java error, no = " + no);
        Packet packet = new Packet();
        packet.putInt("error", no);
        EventInputManager.NotifyModuleEvent(true, EventInputDefine.MODULE, EventInputDefine.MODULE_ID_MCU_ERROR, packet);
    }

    //begin zhongyang.hu add for uart open 20170508
    public static boolean isUartOpen() {
        return isUartOpen;
    }
    //end

    public static void sendInitOkToMcu() {
        LogUtils.d("sendInitOkToMcu RPT_SysInitOK");
        byte[] data = new byte[1];
        data[0] = 0;
        sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_SysInitOK, data, 1);

        if (PowerManager.isPortProject()) {
            sendMcu((byte) McuDefine.ARM_TO_MCU.OP_FAN_CONTROL, FactoryDriver.Driver().getFanControlData(), 2);

            byte[] data1 = new byte[4];
            data1[0] = (byte) (FactoryDriver.Driver().getRotateVoltage() & 0xFF);
            data1[1] = (byte) (FactoryDriver.Driver().getRotateTime() & 0xFF);
            sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ROTATE_PARAM, data1, 4);
        }
    }

    public static void setLogoStatus(Context context, boolean inLogo) {
        mInLogo = inLogo;
        McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_DIRECT, false, (byte) McuDefine.ARM_TO_MCU.OP_LOGO_STATE, new byte[]{(byte) (inLogo ? 0x01 : 0x00)}, 1);

        if (!inLogo) {
            if (ApkUtils.isAPKExist(context, "com.baony.avm360")) {
                Avm360Manager.openCameraBack(null, true);//灵动飞扬360
            }

            if (SourceManager.getOldBackSource() == Define.Source.SOURCE_POWEROFF) {
                CoreLogic logic = LogicManager.getLogicByName(VoiceAssistantDefine.MODULE);
                if (logic != null) {
                    Packet packet1 = new Packet();
                    packet1.putBoolean("power", true);
                    logic.Notify(VoiceAssistantInterface.MainToApk.KEY_POWER_STATE, packet1);
                }
            }
        }
    }
    public static boolean getLogoStatus() {
        return mInLogo;
    }

    public static void registerEventInputDriver(STM32EventInputDriver eventInputDriver) {
        mEventInputDriver = eventInputDriver;
    }

    public static void firstPlayProcess() {
        if (mFirstPlay) {//上电第一次播放通知MCU静音，解决POP音问题。
            LogUtils.e("firstPlayProcess!");
            mFirstPlay = false;
            sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_MUTE_POP, new byte[]{0x01}, 1);
        }
    }

    public static void notifyMcuMute() {
        LogUtils.d("notifyMcuMute!");
        sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_MUTE_POP, new byte[]{0x01}, 1);
    }
}
