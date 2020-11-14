package com.wwc2.main.manager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.data.ByteUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.android_serialport_api.SerialManagerEx;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.eventinput.EventInputLogic;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.uart_util.OnReceiveListener;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

import java.util.Arrays;

/**
 * the Panoramic manager.
 *
 * @author wwc2
 */
public class PanoramicManager {

    /**
     * tag
     */
    private static final String TAG = "PanoramicManager";

    public final String FLOAT_WINDOW_SERVICE_NAME = "com.wwc2.panoramic.MainService";
    public final String FLOAT_WINDOW_SERVICE_PACKET_NAME = "com.wwc2.panoramic";
    public final String FLOAT_WINDOW_SERVICE_CLASS_NAME = "com.wwc2.panoramic.MainService";

    private boolean isUartOpen   = false;
    private boolean mPanoramic   = false;
    private boolean bCh007Project= false;

    /**
     * MCU串口打开状态
     */
    public final int OPEN_RET_REPEAT         = 0;
    public final int OPEN_RET_SUCCESS        = 1;

    private final int CONN_UART             = 0;//串口通讯：1、ARM与盒子。2、CH007通过MCU转发。
    private final int CONN_IR               = 1;//IR通讯

    private final int CMD_HEAD               = 0x2E;
    private SerialManagerEx serialManager    = null;

    private static PanoramicManager mPanoramicManager  = null;

    private Context mContext                 = null;

    private final byte CMD_THIRD_VIEW   = 0x01;//三画面
    private final byte CMD_RETURN       = 0x02;//返回
    private final byte CMD_OK           = 0x03;//OK
    private final byte CMD_ALL_FRONT    = 0x04;//全景+前视
    private final byte CMD_ALL_REAR     = 0x05;//全景+后视
    private final byte CMD_ALL_LEFT     = 0x06;//全景+左视
    private final byte CMD_ALL_RIGHT    = 0x07;//全景+右视
    private final byte CMD_FRONT        = 0x08;//单画面前视
    private final byte CMD_REAR         = 0x09;//单画面后视
    private final byte CMD_LEFT         = 0x0A;//单画面左视
    private final byte CMD_RIGHT        = 0x0B;//单画面右视
    private final byte CMD_FOUR_VIEW    = 0x0C;//4分割
    public static final byte CMD_OPEN   = 0x0D;//打开360
    public static final byte CMD_CLOSE  = 0x0E;//关闭360
    private final byte CMD_NARROW_ROAD  = 0x11;//窄道模式
    private final byte CMD_STREAM_FRONT = 0x12;//流媒体前视
    private final byte CMD_STREAM_REAR  = 0x13;//流媒体后视

    private final byte CMD_OPEN_CAMERA  = 0x31;//打开后视
    private final byte CMD_CLOSE_CAMERA = 0x32;//关闭后视

    public static PanoramicManager getInstance() {
        if (mPanoramicManager == null) {
            mPanoramicManager = new PanoramicManager();
        }
        return mPanoramicManager;
    }

    /**
     * Panoramic manager create method.
     */
    public void onCreate(Packet packet) {
        if (null != packet) {
            mContext = (Context) packet.getObject("context");
        }

        if (FactoryDriver.Driver().getPanoramicSwitch()) {//360开关
            if (FactoryDriver.Driver().getPanoramicConnType() == CONN_IR) {//IR通讯
                bCh007Project = false;
                McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRSet, new byte[]{0x01}, 1);//此帧数据MCU没有用到，可不发
            } else {//串口通讯
                isUartOpen = true;
                if (isCh007Project()) {//ch007平台ARM没有多余串口，只能通过MCU转发。
                    bCh007Project = true;
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRSet, new byte[]{0x00}, 1);//此帧数据MCU没有用到，可不发
                } else {//ch004平台ARM直接与盒子串口通讯
                    if (serialManager == null) {
                        serialManager = SerialManagerEx.getInstance();
                    }
                    String port = SystemProperties.get("ro.uart_sanliuling", "/dev/ttyMT1");
                    serialManager.start(port, 19200, 0);
                    serialManager.registerReceiveListener(receiveListener);
                }
            }
        }
    }

    /**
     * manager destroy method.
     */
    public void onDestroy() {
        isUartOpen = false;
        bCh007Project = false;
        if (null != serialManager) {
            serialManager.stop();
        }
    }

    /**
     * 发送数据，针对模块重要的数据
     *
     * @param buf  数据
     * @param len  数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    public int sendData(byte[] buf, int len) {
        int ret = -1;

        if (isUartOpen) {

            // 发送MCU数据
            byte txbuf[] = packagePanoramicData(buf, len);

            // 打印数据
            final String string = "ARM --> PANORAMIC: buf = " + FormatData.formatHexBufToString(txbuf, txbuf.length);
            LogUtils.d(TAG,  string);

            if (null != txbuf) {
                if (bCh007Project) {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternUartData, txbuf, txbuf.length);
                } else {
                    if (null != serialManager) {
                        serialManager.sendBytes(txbuf);
                    }
                }
            }
        } else {
            LogUtils.w(TAG, "warning, sendMcu failed, because the port is close");
        }
        return ret;
    }

    /**
     * 上抛的数据入口
     *
     * @param buf 数据
     * @param len 数据长度
     */
    public void dispatch(byte[] buf, int len) {
        if (null != buf && len > 0) {
            byte[] data = ByteUtils.cutBytes(0, len, buf);

        }
    }

    private byte[] packagePanoramicData(byte[] buf, int len) {
        byte[] txbuf = new byte[len + 2];
        int i= 0;
        byte checkSum = 0;

        txbuf[0] = (byte) CMD_HEAD;
        for (i = 0; i < len; i++) {
            txbuf[i + 1] = buf[i];
            checkSum += buf[i]&0xFF;
        }
        txbuf[txbuf.length - 1] = (byte) (checkSum ^ 0xff);

        return txbuf;
    }

    private OnReceiveListener receiveListener = new OnReceiveListener() {
        public void onBytesReceive(byte[] buffer) {
//            synchronized (mCmdQueue) {
//                //Log.d(TAG, "enqueueLocked thread1 recive data save to mCmdQueue");
//                enqueueLocked(buffer);
//            }
        }

        @Override
        public void onError() {

        }
    };

    public void sendCMDToPanoramic(int cmd, boolean pop) {
        LogUtils.d(TAG, "sendCMDToPanoramic---cmd=" + cmd);
        if (cmd == CMD_OPEN_CAMERA || cmd == CMD_CLOSE_CAMERA) {
            if (FactoryDriver.Driver().getCameraSwitch() && FactoryDriver.Driver().getSupportOpenCamera()) {
                if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                        !FactoryDriver.Driver().getDvrEnable()) {
                    openPanoramic(cmd == CMD_OPEN_CAMERA, true);
                } else {
                    Intent intent = new Intent("com.wwc2.Panoramic");
                    intent.putExtra("panoramic", cmd == CMD_OPEN_CAMERA);
                    mContext.sendBroadcast(intent);

                    Packet packet = new Packet();
                    packet.putBoolean("isShowButton", cmd != CMD_OPEN_CAMERA);
                    ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.HIDE_SHOW_RECORD_BUTTON, packet);
                }
            }
            return;
        } else {
            if (!FactoryDriver.Driver().getPanoramicSwitch() || !FactoryDriver.Driver().getCameraSwitch()) {
                return;
            }
        }

        boolean bOpen = true;

        switch (cmd) {
            case CMD_FRONT:
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
//                buf = new byte[]{0x2E, 0x02, 0x02, 0x15, 0x01, (byte) 0xE5};
                    sendData(new byte[]{0x09, 0x01, 0x01}, 3);
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x05, (byte) 0xFB}, 4);
                }
                break;
            case CMD_REAR:
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
//                buf = new byte[]{0x2E, 0x02, 0x02, 0x16, 0x01, (byte) 0xE4};
                    sendData(new byte[]{0x09, 0x01, 0x02}, 3);
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x06, (byte) 0xFB}, 4);
                }
                break;
            case CMD_LEFT:
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
//                buf = new byte[]{0x2E, 0x02, 0x02, 0x17, 0x01, (byte) 0xE3};
                    sendData(new byte[]{0x09, 0x01, 0x03}, 3);
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x07, (byte) 0xFB}, 4);
                }
                break;
            case CMD_RIGHT:
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
//                buf = new byte[]{0x2E, 0x02, 0x02, 0x18, 0x01, (byte) 0xE2};
                    sendData(new byte[]{0x09, 0x01, 0x04}, 3);
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x08, (byte) 0xFB}, 4);
                }
                break;
            case CMD_NARROW_ROAD://窄道模式
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
//                    sendData(new byte[]{0x09, 0x29, 0x04}, 3);
                    //英莫特360不支持
                    bOpen = false;
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x13, (byte) 0xFB}, 4);
                }
                break;
            case CMD_STREAM_FRONT://流媒体前视
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
                    bOpen = false;
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x02, (byte) 0xFD, 0x06, (byte) 0xF9}, 4);
                }
                break;
            case CMD_STREAM_REAR://流媒体后视
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
//                    sendData(new byte[]{0x02, 0x02, (byte) 0xE7, 0x00}, 4);
                    //英莫特360不支持
                    bOpen = false;
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x02, (byte) 0xFD, 0x07, (byte) 0xF8}, 4);
                }
                break;
            case CMD_OPEN:
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
                    sendData(new byte[]{0x02, 0x02, (byte) 0xf1, 0x00}, 4);
//                    sendData(new byte[]{0x09, 0x01, 0x02}, 3);
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x03, (byte) 0xFB}, 4);
                }
                break;
            case CMD_CLOSE:
                //1、修改360界面进倒车，退出倒车后花屏。（不发关闭命令给盒子）2018-09-19
                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
                    sendData(new byte[]{0x02, 0x02, (byte) 0xf0, 0x00}, 4);
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x02, (byte) 0xFB}, 4);
                }
                if (pop) {
                    openPanoramic(false, false);
                }
                bOpen = false;
                break;
            default:
                bOpen = false;
                break;
        }

        //解决语音直接“切换前视”等指令时，进360后，语音指令没作用。2018-12-28
        if (bOpen && pop) {
            openPanoramic(true, false);
        }
    }

    public boolean openPanoramic(boolean open, boolean closeUI) {
        LogUtils.d(TAG, "openPanoramic---open=" + open + ", closeUI=" + closeUI + ", getBeforCamera()=" + getBeforCamera());
        if (mPanoramic == open || mContext == null) {
            return false;
        }

        if (open) {
            ApkUtils.startServiceSafety(mContext, FLOAT_WINDOW_SERVICE_NAME,
                    FLOAT_WINDOW_SERVICE_PACKET_NAME,
                    FLOAT_WINDOW_SERVICE_CLASS_NAME);
            mPanoramic = open;
        } else {
            if (ApkUtils.isServiceRunning(mContext, FLOAT_WINDOW_SERVICE_NAME)) {
                if (closeUI || !getBeforCamera()) {//解决在没接360盒子时，语音进360后，无法退出。2018-12-28
                    ApkUtils.stopServiceSafety(mContext, FLOAT_WINDOW_SERVICE_NAME,
                            FLOAT_WINDOW_SERVICE_PACKET_NAME,
                            FLOAT_WINDOW_SERVICE_CLASS_NAME);
                    mPanoramic = open;
                }

                if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
                    sendData(new byte[]{0x02, 0x02, (byte) 0xf0, 0x00}, 4);
                } else {
                    McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, new byte[]{0x00, (byte) 0xFF, 0x02, (byte) 0xFB, }, 4);
                }
            }
        }
        Intent intent = new Intent("com.wwc2.Panoramic");
        intent.putExtra("panoramic", open);
        mContext.sendBroadcast(intent);

        Packet packet = new Packet();
        packet.putBoolean("isShowButton", !open);
        ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).dispatch(VoiceAssistantInterface.ApkToMain.HIDE_SHOW_RECORD_BUTTON, packet);
        return true;
    }

    public void sendTouchXY(Context context, int x, int y) {
        byte[] rect = new byte[4];
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int mCurrentOrientation = context.getResources().getConfiguration().orientation;
        LogUtils.d(TAG, "sendTouchXY---x=" + x + ", y=" + y + ", mCurrentOrientation=" + mCurrentOrientation +
                ", width=" + dm.widthPixels + ", height=" + dm.heightPixels);
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {//修改竖屏360触摸y坐标不正常。2019-03-05
            dm.heightPixels = 432;//倒车设置的图像高度。
            y -= 200;
        }

        if (FactoryDriver.Driver().getPanoramicConnType() == CONN_UART) {
            x = (x * 4096) / dm.widthPixels;
            y = (y * 4096) / dm.heightPixels;
            byte[] buf = new byte[7];
            buf[0] = 0x05;
            buf[1] = 0x05;
            buf[2] = 0x01;
            buf[3] = (byte) ((x >> 8) & 0xFF);
            buf[4] = (byte) (x & 0xFF);
            buf[5] = (byte) ((y >> 8) & 0xFF);
            buf[6] = (byte) (y & 0xFF);
            sendData(buf, 7);
        } else {
            rect[0] = (byte) 0xBB;
            rect[1] = 0x66;
            rect[2] = (byte) ((x * 255) / dm.widthPixels);
            rect[3] = (byte) ((y * 255) / dm.heightPixels);
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_ExternIRData, rect, 4);
        }
    }

    public boolean isCh007Project() {
        boolean ret = true;//false;
//        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
//        String clientProject;
//        if (client != null) {
//            clientProject = client.getString("ClientProject");
//            if (clientProject.contains("ch007") ||
//                    clientProject.contains("ch006")) {
//                ret =true;
//            }
//            LogUtils.d(TAG, "isCh007Project---ret=" + ret + ", clientProject=" + clientProject);
//        }
//
//        if (!ret) {
//            String clientMcu = SystemProperties.get("ro.mcuType", "WTWD");
//            //WYD_4G WYD_WIFI WTWD
//            LogUtils.d(TAG, "isCh007Project---clientMcu=" + clientMcu);
//            if (clientMcu.equals("WYD_4G") || clientMcu.equals("WYD_WIFI")) {
//                ret = true;
//            }
//        }
        String mPlatfromID =SystemProperties.get("ro.board.platform", "");
        if (mPlatfromID.contains("6737")) {//根据平台判断
            ret = false;
        }

        return ret;
    }

    public boolean getPanoramicState() {
        if (mPanoramic) {
            if (!ApkUtils.isServiceRunning(mContext, FLOAT_WINDOW_SERVICE_NAME)) {
                mPanoramic = false;
            }
        }

        return mPanoramic;
    }

    private boolean getBeforCamera() {
        boolean ret = false;
        BaseLogic mEventInputLogic =  ModuleManager.getLogicByName(EventInputDefine.MODULE);
        if (mEventInputLogic != null && mEventInputLogic instanceof EventInputLogic) {
            if (null != mEventInputLogic) {
                Packet packet = mEventInputLogic.getInfo();
                if (null != packet) {
                    ret = packet.getBoolean("PanoramicBeforCamera", false);
                }
            }
        }
        return ret;
    }

    int index = 0;
    public void sendTouchXY(int action, int x, int y) {
        if (PowerManager.isRuiPai()) {
            if (mContext != null) {
                DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
                int mCurrentOrientation = mContext.getResources().getConfiguration().orientation;
                LogUtils.d(TAG, "sendTouchXY---x=" + x + ", y=" + y + ", mCurrentOrientation=" + mCurrentOrientation +
                        ", width=" + dm.widthPixels + ", height=" + dm.heightPixels);
                if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {//修改竖屏360触摸y坐标不正常。2019-03-05
                    dm.heightPixels = 432;//倒车设置的图像高度。
//                    y -= 200;

                    x = (x * 1024) / dm.widthPixels;
                    y = (y * 600) / dm.heightPixels;
                }
            }

            if (action == MotionEvent.ACTION_MOVE) {
                sendTouchXYToMcu(1, x, y);
            } else {
                sendTouchXYToMcu(1, x, y);
                SystemClock.sleep(20);
                sendTouchXYToMcu(0, x, y);
            }
        } else {
            if (mContext != null) {
                sendTouchXY(mContext, x, y);
            }
        }
    }

    private void sendTouchXYToMcu(int action, int x, int y) {
        index += 1;
        byte[] data = new byte[12];
        data[0] = 0x03;
        data[1] = 0x01;
        data[2] = 0x00;
        data[3] = 0x05;
        data[4] = (byte) ((index >> 8) & 0xFF);
        data[5] = (byte) (index & 0xFF);
        data[6] = (byte) (action | 0x80);//bit7: 1表示按分辨率坐标，0表示按相对坐标
        data[7] = (byte) ((x >> 8) & 0xFF);
        data[8] = (byte) (x & 0xFF);
        data[9] = (byte) ((y >> 8) & 0xFF);
        data[10] = (byte) (y & 0xFF);
        int checksum = 0;
        for (int i=0; i<11; i++) {
            checksum ^= data[i];
        }
        data[11] = (byte) (checksum & 0xFF);

        byte[] point = convertData(data);
        byte[] mcuData = new byte[point.length + 2];
        mcuData[0] = 0x7e;
        System.arraycopy(point, 0, mcuData, 1, point.length);
        mcuData[mcuData.length - 1] = 0x7e;

//        LogUtils.d(TAG, "convertData--1--data=" + FormatData.formatHexBufToString(point, point.length));
//        LogUtils.d(TAG, "convertData--2--data=" + FormatData.formatHexBufToString(mcuData, mcuData.length));
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_TOUCH_POINT, mcuData, mcuData.length);
    }

    private byte[] convertData(byte[] data) {
        byte[] retData = new byte[data.length * 2];
        int num = 0;
        for (int i=0; i<data.length; i++) {
            if (data[i] == 0x7e) {
                retData[num] = 0x7d;
                retData[num + 1] = 0x02;
                num += 2;
            } else if (data[i] == 0x7d) {
                retData[num] = 0x7d;
                retData[num + 1] = 0x01;
                num += 2;
            } else {
                retData[num] = data[i];
                num += 1;
            }
        }

//        LogUtils.d(TAG, "convertData---datalen=" + data.length + ", num=" + num);
//        LogUtils.d(TAG, "convertData--0--data=" + FormatData.formatHexBufToString(data, data.length));
        return Arrays.copyOfRange(retData, 0, num);
    }
}
