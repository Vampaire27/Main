package com.wwc2.main.tpms.driver.protocol;

import android.util.Log;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.manager.McuManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created by huwei on 2017/1/26.
 */
public class WriteThread implements Runnable {
    private String TAG = WriteThread.class.getSimpleName();
    Thread mThread;
    LinkedList<Meta> mSendData = new LinkedList<>();
    /**尾部多余的数据,即解析时候,末尾不是完整帧的数据*/

    static final int WRITEABLE_SIZE_NUMBER = 100;
    int mWriteableSize = WRITEABLE_SIZE_NUMBER;

    public WriteThread() {

    }

    public WriteThread(Thread mThread) {
        this.mThread = mThread;
    }

    public boolean islive() {
        return (null != mThread) ? mThread.isAlive() : false;
    }

    public WriteThread start() {
        if (null == mThread) {
            mThread = new Thread(this);
            mThread.start();
        }
        return this;
    }

    public WriteThread stop() {
        if (null != mThread && !mThread.isInterrupted()) {
            mThread.interrupt();
        }
        return this;
    }

    public void setWritingButterSize(int num) {
        if (num > 0) {
            mWriteableSize = num;
        }
    }

    /**
     * 写缓存,byte[]为单位
     * @param data 数据
     * @param priority 优先级
     * @param listener 发送完成监听
     * @return
     */
    public boolean write(byte[] data, int priority, WritedListener listener) {
        if (null != data ) {
            //超出缓存, 移除优先级最低的第一条数据
            synchronized (mSendData) {
                if (mWriteableSize <= 0) mWriteableSize = WRITEABLE_SIZE_NUMBER;
                if (mSendData.size() > mWriteableSize) mSendData.removeLast();

                //将新的数据到缓存
                if (priority < 0) priority = Integer.MAX_VALUE;
                mSendData.add(new Meta(data, priority, listener));

                //将数据排序
                Collections.sort(mSendData, new Comparator<Meta>() {
                    @Override
                    public int compare(Meta lhs, Meta rhs) {
                        return (null == lhs || null == rhs) ? ((null == lhs) ? 1 : 0) : ((lhs.priority < rhs.priority) ? 1 : 0);
                    }
                });

                if (!islive() && !mSendData.isEmpty()) {
                    start();
                }
                return true;
            }
        }
        return false;
    }

    public boolean write(byte[] data, WritedListener listener) {
        boolean ret = false;
        if(data != null){
//            LogUtils.d(TAG," write:" + MessesDefine.printHexString(data));
//            byte[] newByte = new byte[data.length + 1];
//            newByte[0] = 0;
//            System.arraycopy(data,0,newByte,1,data.length);
            ret = write(data, -1, listener);
        }
        return ret;
    }

    public boolean write(byte[] data) {
        return write(data,null) ;
    }

    protected void writeCmd(byte[] tpmsCmds){
        byte[] values = new byte[tpmsCmds.length + 1];
        values[0] = MessesDefine.MOD_DATA_SERIAL_PORT;
        System.arraycopy(tpmsCmds,0,values,1,tpmsCmds.length);
        LogUtils.d(TAG," writeCmd:" + MessesDefine.printHexString(values));
        McuManager.sendMcu(MessesDefine.EXTERNAL_MOD_SET, values, values.length);
    }
    @Override
    public void run() {
        Log.d(TAG, "\nSerial send thread starting");
        while (!Thread.currentThread().isInterrupted()) {
            //是否busy,用户可以通过setSerialBusy来控制
            Meta meta = null;
            synchronized (mSendData) {
                if (!mSendData.isEmpty()) {
                    meta = mSendData.getFirst();
                } else {
                    break;
                }
            }

            if (null != meta && null != meta.data) {
                //写数据
                writeCmd(meta.data);

                //发送完数据后移除数据
                synchronized (mSendData) {
                    mSendData.removeFirst();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //发送数据后的监听
                if (null != meta.listener) {
                    meta.listener.onWrited(meta.data);
                }
            }
        }
        mThread = null;
        Log.d(TAG, "Serial send thread stoped\n");
    }

    class Meta {
        byte[] data;
        int priority = Integer.MAX_VALUE;//0最高
        WritedListener listener;
        public Meta(byte[] data, WritedListener listener) {
            this(data, Integer.MAX_VALUE, listener);
        }
        public Meta(byte[] data, int priority, WritedListener listener) {
            this.priority = (priority < 0) ? Integer.MAX_VALUE : priority;
            this.data = data;
            this.listener = listener;
        }
    }
    /**发送数据完成监听, 当onWrited返回true, 移除监听*/
    public interface WritedListener { boolean onWrited(byte[] data);}
}
