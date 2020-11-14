package com.wwc2.main.manager;

import android.content.Context;

import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.android_serialport_api.SerialManagerEx;
import com.wwc2.main.uart_util.OnReceiveListener;

/**
 * the DSP manager.
 *
 * @author wwc2
 */
public class GpsDataManager {

    /**
     * tag
     */
    private static final String TAG = "GpsDataManager";

    private boolean isUartOpen   = false;

    private SerialManagerEx serialManager    = null;

    private static GpsDataManager mDspManager  = null;

    private Context mContext                 = null;

    public static GpsDataManager getInstance() {
        if (mDspManager == null) {
            mDspManager = new GpsDataManager();
        }
        return mDspManager;
    }

    /**
     * GpsDataManager manager create method.
     */
    public void onCreate(Packet packet) {
        if (null != packet) {
            mContext = (Context) packet.getObject("context");
        }

        if (!isUartOpen) {
            isUartOpen = true;
            if (serialManager == null) {
                serialManager = SerialManagerEx.getInstance();
            }
            serialManager.start("/dev/ttyp6", 115200, 0);
            serialManager.registerReceiveListener(receiveListener);
        }
    }

    /**
     * manager destroy method.
     */
    public void onDestroy() {
//        isUartOpen = false;
//        if (null != serialManager) {
//            serialManager.stop();
//        }
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
        }

        @Override
        public void onError() {

        }
    };
}
