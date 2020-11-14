package com.wwc2.main.manager;

import android.content.Context;

import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.android_serialport_api.SerialManagerEx;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.uart_util.OnReceiveListener;

/**
 * the DSP manager.
 * DSP apk是第三方提供，通过打开"/dev/ttys5"，main通过读虚拟串口转发给mcu
 * mcu再转发给DSP模块
 *
 * @author wwc2
 */
public class DspManager {

    /**
     * tag
     */
    private static final String TAG = "DspManager";

    private boolean isUartOpen   = false;

    private SerialManagerEx serialManager    = null;

    private static DspManager mDspManager  = null;

    private Context mContext                 = null;

    public static DspManager getInstance() {
        if (mDspManager == null) {
            mDspManager = new DspManager();
        }
        return mDspManager;
    }

    /**
     * DspManager manager create method.
     */
    public void onCreate(Packet packet) {
        if (null != packet) {
            mContext = (Context) packet.getObject("context");
        }

        isUartOpen = true;
        if (serialManager == null) {
            serialManager = SerialManagerEx.getInstance();
        }
        //虚拟串口
        serialManager.start("/dev/ptyp5", 115200, 0);
        serialManager.registerReceiveListener(receiveListener);
    }

    /**
     * manager destroy method.
     */
    public void onDestroy() {
        isUartOpen = false;
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

        if (isUartOpen && null != serialManager) {
            serialManager.sendBytes(buf);
        } else {
            LogUtils.w(TAG, "warning, sendMcu failed, because the port is close");
        }
        return ret;
    }

    private OnReceiveListener receiveListener = new OnReceiveListener() {
        public void onBytesReceive(byte[] buffer) {
            LogUtils.d(TAG, "enqueueLocked recive data:" + FormatData.formatHexBufToString(buffer, buffer.length));
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_DSP_DATA, buffer, buffer.length);
        }

        @Override
        public void onError() {

        }
    };
}
