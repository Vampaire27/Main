package com.wwc2.main.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;

import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.android_serialport_api.SerialManagerEx;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.settings.util.FileUtil;
import com.wwc2.main.uart_util.OnReceiveListener;

import java.util.Arrays;

/**
 * apk是第三方提供，通过打开"/dev/ttys1~4"，main通过读虚拟串口转发给mcu
 * mcu再转发给外部模块
 *
 * @author wwc2
 */
public class TaxiManager {

    /**
     * tag
     */
    private static final String TAG = "TaxiManager";

    private boolean isUart1Open   = false;
    private boolean isUart2Open   = false;
    private boolean isUart3Open   = false;
    private boolean isUart4Open   = false;
    private boolean isUart8Open   = false;

    private SerialManagerEx serialManager1    = null;
    private SerialManagerEx serialManager2    = null;
    private SerialManagerEx serialManager3    = null;
    private SerialManagerEx serialManager4    = null;
    private SerialManagerEx serialManager8    = null;

    private static TaxiManager mTaxiManager  = null;

    public static final String SYSTEM_KEY = "com.wwc2.otherKeyCode";
    private TaxiBroadCast mTaxiBroadCast;

    String BAUDDRATE_NODE = "/sys/class/tty/pty_app_baud_rate";

    private Context mContext                 = null;

    private class TaxiBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyValue", 0);
            LogUtils.d("---keyCode=" + keyCode);
            if(keyCode == KeyEvent.KEYCODE_BOOKMARK){
                try {
                    String baudRate = FileUtil.readTextFile(BAUDDRATE_NODE);
                    LogUtils.d(TAG, "enqueueLocked baudRate=" + baudRate);
                    String[] baudArray = baudRate.split(",");
                    if (baudArray != null && baudArray.length == 5) {
                        for (int i = 0; i < baudArray.length; i++) {
                            int port = getValue(baudArray[i], 0);
                            int value = getValue(baudArray[i], 1);
                            switch (port) {
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                    if (value > 0) {
                                        sendBaundrateToMcu(port, value);
                                    }
                                    break;
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                    if (value == 0) {
                                        reOpenPort(port - 10);
                                    }
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getValue(String info, int index) {
        int ret = -1;
        String[] portInfo = info.split("-");
        if (portInfo != null && portInfo.length == 2) {
            try {
                ret = Integer.parseInt(portInfo[index]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static TaxiManager getInstance() {
        if (mTaxiManager == null) {
            mTaxiManager = new TaxiManager();
        }
        return mTaxiManager;
    }

    public void onCreate(Packet packet) {
        if (null != packet) {
            mContext = (Context) packet.getObject("context");
        }

        try {
            if (serialManager1 == null) {
                serialManager1 = new SerialManagerEx();
            }
            //虚拟串口
            serialManager1.start("/dev/ptyp1", 115200, 0);
            serialManager1.registerReceiveListener(receiveListener1);
            isUart1Open = true;

            if (serialManager2 == null) {
                serialManager2 = new SerialManagerEx();
            }
            //虚拟串口
            serialManager2.start("/dev/ptyp2", 115200, 0);
            serialManager2.registerReceiveListener(receiveListener2);
            isUart2Open = true;

            if (serialManager3 == null) {
                serialManager3 = new SerialManagerEx();
            }
            //虚拟串口
            serialManager3.start("/dev/ptyp3", 115200, 0);
            serialManager3.registerReceiveListener(receiveListener3);
            isUart3Open = true;

            if (serialManager4 == null) {
                serialManager4 = new SerialManagerEx();
            }
            //虚拟串口
            serialManager4.start("/dev/ptyp4", 115200, 0);
            serialManager4.registerReceiveListener(receiveListener4);
            isUart4Open = true;

            if (serialManager8 == null) {
                serialManager8 = new SerialManagerEx();
            }
            //虚拟串口
            serialManager8.start("/dev/ptyp5", 115200, 0);
            serialManager8.registerReceiveListener(receiveListener8);
            isUart8Open = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mContext != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SYSTEM_KEY);
            mTaxiBroadCast = new TaxiBroadCast();
            mContext.registerReceiver(mTaxiBroadCast, intentFilter);
        }
    }

    /**
     * manager destroy method.
     */
    public void onDestroy() {
        isUart1Open = false;
        if (null != serialManager1) {
            serialManager1.stop();
        }

        isUart2Open = false;
        if (null != serialManager2) {
            serialManager2.stop();
        }

        isUart3Open = false;
        if (null != serialManager3) {
            serialManager3.stop();
        }

        isUart4Open = false;
        if (null != serialManager4) {
            serialManager4.stop();
        }

        isUart8Open = false;
        if (null != serialManager8) {
            serialManager8.stop();
        }

        try {
            if (mTaxiBroadCast != null) {
                mContext.unregisterReceiver(mTaxiBroadCast);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        int uart = buf[0] & 0xFF;
        switch (uart) {
            case 0x01:
                if (isUart1Open && null != serialManager1) {
                    serialManager1.sendBytes(Arrays.copyOfRange(buf, 1, buf.length));
                } else {
                    LogUtils.w(TAG, "warning, sendMcu failed, because the port is close");
                }
                break;
            case 0x02:
                if (isUart2Open && null != serialManager2) {
                    serialManager2.sendBytes(Arrays.copyOfRange(buf, 1, buf.length));
                } else {
                    LogUtils.w(TAG, "warning, sendMcu failed, because the port is close");
                }
                break;
            case 0x03:
                if (isUart3Open && null != serialManager3) {
                    serialManager3.sendBytes(Arrays.copyOfRange(buf, 1, buf.length));
                } else {
                    LogUtils.w(TAG, "warning, sendMcu failed, because the port is close");
                }
                break;
            case 0x04:
                if (isUart4Open && null != serialManager4) {
                    serialManager4.sendBytes(Arrays.copyOfRange(buf, 1, buf.length));
                } else {
                    LogUtils.w(TAG, "warning, sendMcu failed, because the port is close");
                }
                break;
            case 0x08:
                if (isUart8Open && null != serialManager8) {
                    serialManager8.sendBytes(Arrays.copyOfRange(buf, 1, buf.length));
                } else {
                    LogUtils.w(TAG, "warning, sendMcu failed, because the port is close");
                }
                break;
        }
        return ret;
    }

    private OnReceiveListener receiveListener1 = new OnReceiveListener() {
        public void onBytesReceive(byte[] buffer) {
//            LogUtils.d(TAG, "enqueueLocked recive data:" + FormatData.formatHexBufToString(buffer, buffer.length));
            byte[] data = new byte[buffer.length + 1];
            System.arraycopy(buffer, 0, data, 1, buffer.length);
            data[0] = 0x01;
            McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.OP_DSP_DATA, data, data.length);
        }

        @Override
        public void onError() {
            LogUtils.e(TAG, "enqueueLocked recive 1 error!");
            reOpenPort(1);
        }
    };

    private OnReceiveListener receiveListener2 = new OnReceiveListener() {
        public void onBytesReceive(byte[] buffer) {
//            LogUtils.d(TAG, "enqueueLocked recive data:" + FormatData.formatHexBufToString(buffer, buffer.length));
            byte[] data = new byte[buffer.length + 1];
            System.arraycopy(buffer, 0, data, 1, buffer.length);
            data[0] = 0x02;
            McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.OP_DSP_DATA, data, data.length);
        }

        @Override
        public void onError() {
            LogUtils.e(TAG, "enqueueLocked recive 2 error!");
            reOpenPort(2);
        }
    };

    private OnReceiveListener receiveListener3 = new OnReceiveListener() {
        public void onBytesReceive(byte[] buffer) {
//            LogUtils.d(TAG, "enqueueLocked recive data:" + FormatData.formatHexBufToString(buffer, buffer.length));
            byte[] data = new byte[buffer.length + 1];
            System.arraycopy(buffer, 0, data, 1, buffer.length);
            data[0] = 0x03;
            McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.OP_DSP_DATA, data, data.length);
        }

        @Override
        public void onError() {
            LogUtils.e(TAG, "enqueueLocked recive 3 error!");
            reOpenPort(3);
        }
    };

    private OnReceiveListener receiveListener4 = new OnReceiveListener() {
        public void onBytesReceive(byte[] buffer) {
//            LogUtils.d(TAG, "enqueueLocked recive data:" + FormatData.formatHexBufToString(buffer, buffer.length));
            byte[] data = new byte[buffer.length + 1];
            System.arraycopy(buffer, 0, data, 1, buffer.length);
            data[0] = 0x04;
            McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.OP_DSP_DATA, data, data.length);
        }

        @Override
        public void onError() {
            LogUtils.e(TAG, "enqueueLocked recive 4 error!");
            reOpenPort(4);
        }
    };

    private OnReceiveListener receiveListener8 = new OnReceiveListener() {
        public void onBytesReceive(byte[] buffer) {
//            LogUtils.d(TAG, "enqueueLocked recive data:" + FormatData.formatHexBufToString(buffer, buffer.length));
            byte[] data = new byte[buffer.length + 1];
            System.arraycopy(buffer, 0, data, 1, buffer.length);
            data[0] = 0x08;
            McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.OP_DSP_DATA, data, data.length);
        }

        @Override
        public void onError() {
            LogUtils.e(TAG, "enqueueLocked recive 5 error!");
            reOpenPort(5);
        }
    };

    private void sendBaundrateToMcu(int port, int baundrate) {
        byte[] data = new byte[6];
        data[0] = 0x00;
        data[1] = (byte) (port + 0xA0);
        data[2] = (byte) ((baundrate >> 24) & 0xFF);
        data[3] = (byte) ((baundrate >> 16) & 0xFF);
        data[4] = (byte) ((baundrate >> 8) & 0xFF);
        data[5] = (byte) (baundrate & 0xFF);
        McuManager.sendMcuNack((byte) McuDefine.ARM_TO_MCU.OP_DSP_DATA, data, data.length);
    }

    private void reOpenPort(int port) {
        switch (port) {
            case 1:
                isUart1Open = false;
                if (null != serialManager1) {
                    serialManager1.stop();
                }

                if (serialManager1 == null) {
                    serialManager1 = new SerialManagerEx();
                }
                //虚拟串口
                serialManager1.start("/dev/ptyp1", 115200, 0);
                serialManager1.registerReceiveListener(receiveListener1);
                isUart1Open = true;
                break;
            case 2:
                isUart2Open = false;
                if (null != serialManager2) {
                    serialManager2.stop();
                }

                if (serialManager2 == null) {
                    serialManager2 = new SerialManagerEx();
                }
                //虚拟串口
                serialManager2.start("/dev/ptyp2", 115200, 0);
                serialManager2.registerReceiveListener(receiveListener2);
                isUart2Open = true;
                break;
            case 3:
                isUart3Open = false;
                if (null != serialManager3) {
                    serialManager3.stop();
                }

                if (serialManager3 == null) {
                    serialManager3 = new SerialManagerEx();
                }
                //虚拟串口
                serialManager3.start("/dev/ptyp3", 115200, 0);
                serialManager3.registerReceiveListener(receiveListener3);
                isUart3Open = true;
                break;
            case 4:
                isUart4Open = false;
                if (null != serialManager4) {
                    serialManager4.stop();
                }

                if (serialManager4 == null) {
                    serialManager4 = new SerialManagerEx();
                }
                //虚拟串口
                serialManager4.start("/dev/ptyp4", 115200, 0);
                serialManager4.registerReceiveListener(receiveListener4);
                isUart4Open = true;
                break;
            case 5:
                isUart8Open = false;
                if (null != serialManager8) {
                    serialManager8.stop();
                }

                if (serialManager8 == null) {
                    serialManager8 = new SerialManagerEx();
                }
                //虚拟串口
                serialManager8.start("/dev/ptyp5", 115200, 0);
                serialManager8.registerReceiveListener(receiveListener8);
                isUart8Open = true;
                break;
        }
    }
}
