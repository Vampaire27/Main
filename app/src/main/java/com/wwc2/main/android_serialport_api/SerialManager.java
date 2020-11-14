

package com.wwc2.main.android_serialport_api;

import android.os.SystemProperties;
import android.util.Log;

import com.wwc2.main.uart_util.OnReceiveListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;



public class SerialManager {
    private static final String TAG = "SerialManager";

    private static SerialManager instance;

    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private boolean isRunning = false;

    private OnReceiveListener receiveListener = null;


    public SerialManager() {
        start();
    }

    public static SerialManager getInstance() {
        if (instance == null) {
            synchronized (SerialManager.class) {
                if (instance == null)
                    instance = new SerialManager();
            }
        }
        return instance;
    }

    public SerialPort getSerialPort() throws SecurityException, IOException,
            InvalidParameterException {
        if (mSerialPort == null) {
            String port = SystemProperties.get("ro.uart_mcu", "/dev/ttyMT3");
            this.mSerialPort = new SerialPort(new File(port), 115200, 0);
            Log.d(TAG, " setuart " + port);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        }
        return mSerialPort;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            try {
                getSerialPort();
            } catch (InvalidParameterException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        //bedin zhongyang.hu add isRunning when uart is going to close, then not to send any data to mcu
        if (mOutputStream == null || isRunning == false) {
            Log.d(TAG, "isRunning is  " + isRunning);
            return;
        }
        //end
        try {
            synchronized (this) {
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

}
