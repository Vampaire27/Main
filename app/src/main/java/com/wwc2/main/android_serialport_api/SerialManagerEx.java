

package com.wwc2.main.android_serialport_api;

import android.util.Log;

import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.uart_util.OnReceiveListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;


public class SerialManagerEx {
    private static final String TAG = "SerialManagerEx";

    private static SerialManagerEx instance;

    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private boolean isRunning = false;

    private OnReceiveListener receiveListener = null;


    public SerialManagerEx() {

    }

    public static SerialManagerEx getInstance() {
        if (instance == null) {
            synchronized (SerialManagerEx.class) {
                if (instance == null)
                    instance = new SerialManagerEx();
            }
        }
        return instance;
    }

    public SerialPort getSerialPort(String DevName, int BaudRate, int Flags) throws
            SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            this.mSerialPort = new SerialPort(new File(DevName), BaudRate, Flags);
            Log.d(TAG, " setuart DevName=" + DevName);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        }
        return mSerialPort;
    }

    public void start(String DevName, int BaudRate, int Flags) {
        LogUtils.d(TAG, " start DevName=" + DevName);
        if (!isRunning) {
            isRunning = true;
            try {
                getSerialPort(DevName, BaudRate, Flags);
            } catch (InvalidParameterException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //只发送坐标，不需接收。
            SerialReadStart();
        }
    }

    public void stop() {
        isRunning = false;
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        instance = null;
    }

    private void SerialReadStart() {
        this.mReadThread = new ReadThread();
        mReadThread.start();
    }

    public void sendBytes(byte[] buffer) {
        if (mOutputStream == null || isRunning == false) {
            Log.d(TAG, "isRunning is  " + isRunning);
            return;
        }
        try {
            synchronized (this) {
                LogUtils.d(TAG, "sendBytes----" + FormatData.formatHexBufToString(buffer, buffer.length));
                mOutputStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (!isInterrupted()) {
                    int size;
                    try {
                        byte[] buffer = new byte[1000];
                        if (mInputStream == null)
                            return;
                        size = mInputStream.read(buffer);
                        if (size > 0) {
                            byte[] buf = new byte[size];
                            for (int i = 0; i < size; i++)
                                buf[i] = buffer[i];
                            notifyBytesReceive(buf);
                            Thread.sleep(5);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        notifyError();
                        return;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerReceiveListener(OnReceiveListener listener) {
        receiveListener = listener;
    }



    private void notifyBytesReceive(byte[] buffer) {
        if (receiveListener != null) {
            receiveListener.onBytesReceive(buffer);
        }
    }

    private void notifyError() {
        if (receiveListener != null) {
            receiveListener.onError();
        }
    }

}
