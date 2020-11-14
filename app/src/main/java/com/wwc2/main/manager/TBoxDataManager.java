package com.wwc2.main.manager;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.wwc2.common_interface.Provider;
import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.info.InfoDriver;
import com.wwc2.main.driver.info.InfoDriverable;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.provider.LogicProvider;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by swd1 on 20-3-20.
 */

public class TBoxDataManager {
    /**
     * tag
     */
    private static final String TAG = "TBoxDataManager";

    public static final byte HEADER1 = (byte) 0x5A;//头
    public static final byte HEADER2 = (byte) 0xA5;

    private static TBoxDataManager tBoxDataManager = null;

    private String mTboxIccid = "";
    private String mTboxImei = "";
    private Context mContext = null;
    int count = 0;
    boolean mUartOpen = false;

    public static TBoxDataManager getTBoxManager() {
        if (tBoxDataManager == null) {
            tBoxDataManager = new TBoxDataManager();
        }
        return tBoxDataManager;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mHandler.removeMessages(1);
                    InfoDriverable infoDriverable = InfoDriver.Driver();
                    if (null != infoDriverable && mContext != null) {
                        String iccid = infoDriverable.getSimSerialNumber(mContext);
                        if (iccid != null && iccid.length() > 0) {
                            byte[] arrayiccid = iccid.getBytes();
                            LogUtils.d("sendSimToTBox---count" + count + ", iccid=" + iccid);
//                            String str = count + ":iccid=" + iccid;
//                            Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
                            if (arrayiccid.length <= 20) {
                                sendDataToTBox(true, (byte) 0x10, arrayiccid);
                                return;
                            } else {
                                LogUtils.e("ICCID len > 20!");
                            }
                        }
                        count ++;
                        if (count < 120) {
                            mHandler.sendEmptyMessageDelayed(1, 500);
                        }
                    }
                    break;
                case 2:
                    mHandler.removeMessages(2);
                    InfoDriverable infoDriverable1 = InfoDriver.Driver();
                    if (null != infoDriverable1 && mContext != null) {
                        String imei = infoDriverable1.getIMEI(mContext);
                        if (imei != null && imei.length() > 0) {
                            byte[] arrayimei = imei.getBytes();
                            sendDataToTBox(true, (byte) 0x11, arrayimei);//IMEI不需要发给TBOX 2020-04-10
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    public void sendSimToTBox(Context context) {
        if (context == null) {
            mUartOpen = false;
            return;
        } else {
            mUartOpen = true;
        }

        mContext = context;

        InfoDriverable infoDriverable = InfoDriver.Driver();
        if (null != infoDriverable) {
            String iccid = infoDriverable.getSimSerialNumber(context);
            if (iccid != null && iccid.length() > 0) {
                byte[] arrayiccid = iccid.getBytes();
                if (arrayiccid.length <= 20) {
                    sendDataToTBox(true, (byte) 0x10, arrayiccid);
                } else {
                    LogUtils.e("ICCID len > 20!");
                }
            } else {
                count ++;
                mHandler.sendEmptyMessageDelayed(1, 500);//刚开机可能不有获取到ICCID
            }

            String imei = infoDriverable.getIMEI(context);
            if (imei != null && imei.length() > 0) {
                byte[] arrayimei = imei.getBytes();
                sendDataToTBox(true, (byte) 0x11, arrayimei);//IMEI不需要发给TBOX 2020-04-10
            } else {
                mHandler.sendEmptyMessageDelayed(2, 2000);
            }
            LogUtils.d(TAG, "getSimInfo----imei=" + imei + ", iccid=" + iccid);
        }

        //请求TBOX信息
        sendDataToTBox(true, (byte) 0x20, null);//ICCID
        sendDataToTBox(true, (byte) 0x21, null);//IMEI
    }

    public void revDataFromTBox(byte[] data) {
        if (data != null && data.length > 1) {
//            LogUtils.d("revDataFromTBox---data=" + FormatData.formatHexBufToString(data, data.length));
            mCmdQueue.add(data);
            if (mRcThread == null) {
                mRcThread = new RCThread();
                mRcThread.start();
            }
        }
    }

    public void sendDataToTBox(boolean needAck, byte cmd, byte[] data) {
        //数据块长度
        int dataLength = (data == null ? 2 : (2 + data.length));
        //存放数据数组
        byte[] frame = new byte[(null == data) ? 6 : 4 + dataLength];
        frame[0] = HEADER1;//起始字头1
        frame[1] = HEADER2;//起始字头2
        frame[2] = (byte) (dataLength >> 8);//数据块长度(高字节)
        frame[3] = (byte) (dataLength & 0x00FF);//数据块长度(低字节)
        frame[4] = (byte) (0xff & cmd);//命令字
        if (data != null) {
            //数据块
            for (int i = 0; i < data.length; i++) {
                frame[5 + i] = data[i];
            }
        }
        //校验字节
        byte checksum = 0;
        int mLength = frame.length - 1;
        for (int i = 2; i < mLength; i++){
            checksum += (frame[i]);
//            LogUtils.d("packTBoxData---data=" + frame[i] + ", checksum=" + checksum);
        }
        checksum = (byte) ((byte) (checksum ^ 0xff) + 1);
//        LogUtils.d("packTBoxData-1--checksum=" + checksum);
        frame[frame.length - 1] = checksum;

//        LogUtils.d("sendDataToTBox---cmd=" + cmd + ", frame=" + FormatData.formatHexBufToString(frame, frame.length));
        sendDataToTBoxByMcu(needAck, frame);
    }

    private void sendDataToTBoxByMcu(boolean needAck, byte[] data) {
        if (!mUartOpen) {
            return;
        }

        LogUtils.d("sendDataToTBox---data=" + FormatData.formatHexBufToString(data, data.length));

        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_SendData_TBOX, data, data.length);

        //ACK重发机制
        if (needAck) {
            if (mSentListThreadStart == false) {
                satrtSendCmdList();
                mSentListThreadStart = true;
            }
            synchronized (mSendListSemaphore) {
                addToAckList(data);
            }
            synchronized (mSendListThead) {
                mSendListThead.notifyAll();
            }
        }
    }

    public byte[] parseCanbusData(byte[] data, List<byte[]> result) {
        if (null == data || null == result) {
            return new byte[0];
        }

        if (data.length < 6) {
            return data;
        }

        int index = 0;
        LogUtils.d("parseCanbusData---data=" + FormatData.formatHexBufToString(data, data.length));

        for (int i = 0; i < data.length; i++) {
            if (6 > (data.length - i)) {
                break;
            }
            if ((data[i] == HEADER1) && (data[i + 1] == HEADER2)) {
                int len = (data[i + 2] & 0xFF) * 256 + (data[i + 3] & 0xFF);
                if ((data.length - i) < (len + 4)) {
                    break;
                }

                int j = 2;
                //长度在范围内
                byte checksum = 0;
                for (; (j < (len + 4 - 1)) && ((i + j) < data.length - 1); j++) {
                    checksum += (data[j + i]);
//                    LogUtils.d("parseCanbusData---j=" + j + ", data=" + (data[j + i]) + ", checksum=" + checksum);
                }
                checksum = (byte) ((byte) (checksum ^ 0xff) + 1);
                len = (j > 0) ? (j) : 0;

//                LogUtils.d("parseCanbusData-2--len=" + len + ", j=" + j + ", checksum=" + checksum);
                if ((checksum & 0xFF) == (data[i + j] & 0xFF)) {
                    //过滤掉头尾和校验
                    byte[] transmit = new byte[len];
                    System.arraycopy(data, i, transmit, 0, len);
                    result.add(transmit);
                    //偏移调整,此处默认+4, 但for循环中i会+1
                    i += (len /*+ 1*/);
                    index = i + 1;
                } else {
                    LogUtils.d(TAG, "checkSum is error!");
                }
            }
        }

        byte[] remain = new byte[(index < data.length) ? (data.length - index) : 0];
        if (remain.length > 0) {
            System.arraycopy(data, index, remain, 0, remain.length);
        }
        return remain;
    }

    private void remoteMessage(byte[] data) {
        if (data != null) {
            LogUtils.d("remoteMessage---" + FormatData.formatHexBufToString(data, data.length));
            if (data.length >= 6) {
                int cmd = data[4] & 0xFF;
                if (cmd != 0xFF && cmd != 0x7F) {
                    sendDataToTBox(false, (byte) 0x7F, new byte[]{(byte) cmd});//ACK
                } else {
                    //ACK重发机制
                    //pre deal with the ack cmd
                    synchronized (mSendListSemaphore) {
                        if (data.length >= 7) {
                            LogUtils.d(TAG, "isAckData is less 2");
                            removeFromAckList(data[5], data[6]);
                        } else {
                            LogUtils.d(TAG, "isAckData is  2");
                            removeFromAckList(data[5]);
                        }
                    }
                }

                switch (cmd) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        if (data.length >= 5) {
                            int status = data[5] & 0xFF;
                            Packet packet = new Packet();
                            packet.putInt("sound_info", status == 1 ? cmd : (cmd + 6));
                            ModuleManager.getLogicByName(VoiceAssistantDefine.MODULE).
                                    dispatch(VoiceAssistantInterface.ApkToMain.CHOOSE_SYSTEM_AUDIO, packet);
                        } else {
                            LogUtils.e("data len error len=" + data.length);
                        }
                        break;
                    case 0x20://ICCID
                        int len = (data[2] & 0xFF) * 256 + (data[3] & 0xFF);
                        if (len > 2) {
                            byte[] iccid = new byte[len - 2];
                            System.arraycopy(data, 5, iccid, 0, iccid.length);
                            try {
                                mTboxIccid = new String(iccid, "UTF-8");
                                LogUtils.d(TAG, "getTBOXSimInfo----mTboxIccid=" + mTboxIccid);
                                if (mContext != null) {
                                    Uri uri = Uri.parse("content://" + Provider.AUTHORITY + "/" + LogicProvider.TBOX_ICCID);
                                    mContext.getContentResolver().notifyChange(uri, null);
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 0x21://IMEI
                        int lenImei = (data[2] & 0xFF) * 256 + (data[3] & 0xFF);
                        if (lenImei > 2) {
                            byte[] imei = new byte[lenImei - 2];
                            System.arraycopy(data, 5, imei, 0, imei.length);
                            try {
                                mTboxImei = new String(imei, "UTF-8");
                                LogUtils.d(TAG, "getTBOXSimInfo----mTboxImei=" + mTboxImei);
                                if (mContext != null) {
                                    Uri uri = Uri.parse("content://" + Provider.AUTHORITY + "/" + LogicProvider.TBOX_ICCID);
                                    mContext.getContentResolver().notifyChange(uri, null);
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private final LinkedList<byte[]> mCmdQueue = new LinkedList();
    private Thread mRcThread;
    Buffer mByteBuffer = new Buffer();

    private final class RCThread extends Thread {

        RCThread() {
            super("TBoxThread- tbox data start");
        }

        public void run() {
            while (true) {
                try {
                    //1.backup the recevier data
                    synchronized (mCmdQueue) {
                        while (mCmdQueue.size() > 0) {
                            mByteBuffer.append(mCmdQueue.removeFirst());
                        }
                    }

                    List<byte[]> result = new ArrayList<>();
                    //将解析后多余的数据插入buffer开头
                    mByteBuffer.insert(0, parseCanbusData(mByteBuffer.read(), result));
                    for (byte[] frame : result) {
                        //是写数据,进入等待
                        if (null != frame && frame.length > 0) {
                            //将信息发送给远程应用
                            remoteMessage(frame);
                        }
                    }

                    synchronized (mCmdQueue) {
                        if (mCmdQueue.size() == 0) {
                            mRcThread = null;
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 字节缓存
     */
    public static class Buffer {
        byte[] mBuffer = null;
        public synchronized int read(byte[] buffer, int offset, int len) {
            if (null != mBuffer && null != buffer) {
                //校准读取的长度
                if (offset + len > buffer.length) {
                    len = buffer.length - offset;
                }

                if (mBuffer.length > len) {
                    //读取部分数据,并将保留多余的数据
                    byte[] remain = new byte[mBuffer.length - len];
                    System.arraycopy(mBuffer, 0, buffer, offset, len);
                    System.arraycopy(mBuffer, len, remain, 0, remain.length);
                    mBuffer = remain;
                } else {
                    //读取所有数据
                    len = mBuffer.length;
                    System.arraycopy(mBuffer, 0, buffer, offset, mBuffer.length);
                    mBuffer = null;
                }
                return len;
            }
            return 0;
        }

        public synchronized byte[] read() {
            byte[] back = mBuffer;
            mBuffer = null;
            return back;
        }

        public synchronized int read(byte[] buffer) {
            return (null != buffer) ? read(buffer, 0, buffer.length) : 0;
        }

        /**
         * 指定位置插入字节
         * @param position
         * @param data
         * @param offset
         * @param len
         * @return
         */
        public synchronized boolean insert(int position, byte[] data, int offset, int len) {
            if (null != data && offset < data.length) {
                byte[] buffer;
                //调整长度
                if ((offset + len) > data.length) {
                    len = data.length - offset;
                }

                if (null != mBuffer) {
                    buffer = new byte[mBuffer.length + len];
                    //插入末尾
                    if (position == -1) {
                        System.arraycopy(mBuffer, 0, buffer, 0, mBuffer.length);
                        System.arraycopy(data, offset, buffer, mBuffer.length, len);
                    }
                    //指定位置插入
                    else if (position <= mBuffer.length) {
                        System.arraycopy(mBuffer, 0, buffer, 0, position);
                        System.arraycopy(data, offset, buffer, position, len);
                        System.arraycopy(mBuffer, position, buffer, position + len, mBuffer.length - position);
                    }
                } else {
                    buffer = new byte[len];
                    System.arraycopy(data, offset, buffer, 0, buffer.length);
                }
                mBuffer = buffer;
                return true;
            }
            return false;
        }

        public synchronized boolean insert(int position, byte[] data) {
            return insert(position, data, 0, (null != data)?data.length:0);
        }

        public synchronized boolean append(byte[] data, int offset, int len) {
            return insert(-1, data, offset, len);
        }

        public synchronized boolean append(byte[] data) {
            return insert(-1, data, 0, (null != data)?data.length:0);
        }
    }

    //ACK 重发机制
    public final static int TIME_INTERVAL = 500; //400ms
    final ArrayList<WaitAckData> WaitAckDataList = new ArrayList<WaitAckData>();
    private Object mSendListSemaphore = new Object(); //list modify sync
    private Object mSendListThead = new Object(); //list modify sync
    private boolean mSentListThreadStart = false;
    public static Thread mSendThread; //200ms

    class WaitAckData {
        int times = 2;
        byte[] txbuf;
        long nextSendTime;

        WaitAckData(byte[] mtxbuf) {
            txbuf = mtxbuf;
            nextSendTime = System.currentTimeMillis() + TIME_INTERVAL;
//            LogUtils.d(TAG, "System.currentTimeMillis()" + nextSendTime);
        }

        Boolean isTimeCome() {
            if (nextSendTime < System.currentTimeMillis()) {
                return true;
            } else {
                return false;
            }
        }

        void updataTime() {
            nextSendTime = System.currentTimeMillis() + TIME_INTERVAL;
        }

        Boolean isEnd() {
            times = times - 1;
            if (times == 0) {
                return true;
            }
            return false;
        }
    }

    private void addToAckList(final byte[] txbuf) {
        WaitAckDataList.add(new WaitAckData(txbuf));
    }

    private void removeFromAckList(byte data) {
        for (int i = 0; i < WaitAckDataList.size(); i++) {
            if (WaitAckDataList.get(i).txbuf[4] == data) {
                WaitAckDataList.remove(i);
                break;
            }
        }
    }

    private void removeFromAckList(byte data0, byte data1) {
        for (int i = 0; i < WaitAckDataList.size(); i++) {
            if (WaitAckDataList.get(i).txbuf[4] == data0 && WaitAckDataList.get(i).txbuf[5] == data1) {
                WaitAckDataList.remove(i);
                break;
            }
        }
    }

    private void satrtSendCmdList() {
        mSendThread = new Thread(new Runnable() {
            public void run() {

                while (true) {
                    try {
                        //Log.d(TAG, "send cmd Thread.sleep 200/4ms===");
                        Thread.sleep(TIME_INTERVAL / 2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    while (WaitAckDataList != null && WaitAckDataList.size() != 0) {
                        synchronized (mSendListSemaphore) {
                            for (int index = 0; index < WaitAckDataList.size(); index++) {
                                if (WaitAckDataList.get(index).isTimeCome()) {
                                    WaitAckDataList.get(index).updataTime();

                                    //Log.d(TAG, "======start not recive ack,the data is need send again1=====");
                                    //for (int i = 0; i < WaitAckDataList.get(index).txbuf.length; i++) {
                                    //	String hex = Integer.toHexString(WaitAckDataList.get(index).txbuf[i] & 0xFF);
                                    //	Log.d(TAG, " send it  for  ack_error buffer[" + i + "] = " + hex.toUpperCase() + " ");
                                    //}

                                    byte[] data = WaitAckDataList.get(index).txbuf;
//                                    sendDataToTBoxByMcu(true, data);

                                    if (WaitAckDataList.get(index).isEnd()) {
                                        LogUtils.d(TAG, "======start not recive ack,the data is lose");
//                                        for (int j = 0; j < WaitAckDataList.get(index).txbuf.length; j++) {
//                                            String hex = Integer.toHexString(WaitAckDataList.get(index).txbuf[j] & 0xFF);
//                                            LogUtils.d(TAG, " send it  for  ack_error buffer[" + j + "] = " + hex.toUpperCase() + " ");
//                                        }
                                        LogUtils.d(TAG, " send it  for  ack_error buffer = " + FormatData.formatHexBufToString(data, data.length));

                                        sendDataToTBoxByMcu(true, data);
                                        WaitAckDataList.remove(index);

                                    }
                                }
                            }
                        }
                        try {
                            //LogUtils.d(TAG, "send cmd Thread.sleep 200/4ms===");
                            Thread.sleep(TIME_INTERVAL / 2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    try {
                        //Log.d(TAG, "send cmd  Thread. wait");
                        synchronized (mSendListThead) {
                            mSendListThead.wait();
                        }
                        //Log.d(TAG, "send cmd  Thread.is wake up");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        });
        mSendThread.start();
    }

    //外部接口
    public String getIccidImei(int type) {
        String ret = "";
        if (type == 1) {
            ret = mTboxIccid;
            if (TextUtils.isEmpty(mTboxIccid)) {
                sendDataToTBox(true, (byte) 0x20, null);//ICCID
            }
        } else if (type == 2) {
            ret = mTboxImei;
            if (TextUtils.isEmpty(mTboxImei)) {
                sendDataToTBox(true, (byte) 0x21, null);//IMEI
            }
        }
        return ret;
    }
}
