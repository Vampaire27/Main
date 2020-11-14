package com.wwc2.main.tpms.driver.protocol;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.tpms.driver.TPMSDriverable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by devil on 2017/1/30.
 */
public class MassesProtocol extends Handler{

    private final static String TAG = MassesProtocol.class.getSimpleName();

    private final int pressureUnit = 10;//气压单位

    Buffer mByteBuffer = new Buffer();

    private TPMSDriverable.DataListener mDataListener;

    public void setDataListener(TPMSDriverable.DataListener mDataListener) {
        this.mDataListener = mDataListener;
    }

    @Override
    public void handleMessage(Message msg) {
        Iterator<ReadIdleListener> iterator = mReadIdleListener.iterator();
        while(iterator.hasNext()){
            ReadIdleListener l = iterator.next();
            if (null != l) {
                if(msg.what == l.getReadIdleTime()){
                    int ret = l.onReadIdle(null);
                    synchronized (mReadIdleListener) {
                        //1:移除此监听
                        if (ret == ReadIdleListener.REMOVE) {
//                            mReadIdleListener.remove(l);
                            iterator.remove();   //注意这个地方
                        }
                        //-1:重新发送此消息, 使此监听无限循环
                        else if (ret == ReadIdleListener.LOOP) {
                            LogUtils.w(TAG,"startCheckingReadIdle msg.what:" + msg.what);
                            sendEmptyMessageDelayed(msg.what, msg.what);
                        }
                    }
                }
            }

        }
    }

    public void data(byte[] data){
        List<byte[]> result = new ArrayList<>();
        //将数据写入buffer末尾
        mByteBuffer.append(data);
        //将解析后多余的数据插入buffer开头
        mByteBuffer.insert(0, parse(mByteBuffer.read(), result));
        startCheckingReadIdle(result);
        for(byte[] frame : result){
//            LogUtils.d(TAG,"data:" /*+ TpmsActivity.printHexString(frame)*/);
            shareMessage(frame);
        }
    }

    public byte[] parse(byte[] data, List<byte[]> result) {
        if (null == data || null == result) {
            LogUtils.d(TAG,"dataArray is null:");
            return new byte[0];
        }
        int index = 0;
        //由于数据协议问题,暂时遇到不完整包直接丢掉,即如果数据发生错位,那么所有数据将无效
        //因为应答为单字节,当错位的时候无法区别应答和头码及数据
        for (int i = 0; i < data.length; i++) {
            switch (data[i]) {
                case (byte)0xaa:
                    if ((data.length - i) >= 4) {
                        int len = data[i + 3], j = 0;
                        //长度在范围内
                        byte checksum = 0;
                        for (j = 0; j < (len-1)  && (i + j) < data.length; j++) {
                            checksum += (0xff & data[i + j]);
                        }
                        len = (j > 0) ? (len - 1) : 0;
                        if ((i + len) < data.length && data[i + len] == checksum) {
                            //过滤掉头尾和校验
                            byte[] transmit = new byte[len - 4];
                            System.arraycopy(data, i + 4, transmit, 0, transmit.length);
                            result.add(transmit);

                            //偏移调整,此处默认+4, 但for循环中i会+1
                            i += (len + 1);
                            index = i + 1;
                        } else {
                            LogUtils.d(TAG,"checkSum is error!");
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        byte[] remain = new byte[(index < data.length) ? (data.length - index) : 0];
        if (remain.length > 0) {
            System.arraycopy(data, index, remain, 0, remain.length);
        }
        return remain;
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


    //------------------------------------------解析分发消息 start---------------------------------------
    public void shareMessage(byte[]  val){
        if (val != null) {
            byte cmd = val[0];
            switch (cmd) {
                case MessesDefine.FunctionCmd.QUERY_ERROR_CONFIG:
                    configErrorInfo(val);
                    break;
                case MessesDefine.FunctionCmd.QUERY_WHEEL_ID:
                    wheelModuleIDInfo(val);
                    break;
                case MessesDefine.FunctionCmd.QUERY_INFO:
                    wheelModuleInfo(val);
                    break;
                case MessesDefine.FunctionCmd.QUERY_TPMS_VERSION:
                    tpmsVersionInfo(val);
                    break;
                case MessesDefine.FunctionCmd.HAND_SHAKE:
                    handShakeInfo(val);
                    break;
            }
        }
    }

    /**
     * 协议版本
     */
    private void tpmsVersionInfo(byte[] dataArray) {
        if(dataArray != null){
            if(dataArray.length > 5 ){
                StringBuffer version = new StringBuffer();
                int day = dataArray[2];
                int month = dataArray[3];
                int year = dataArray[4];
                int mainVersion = (dataArray[5]>>4) & 0x0F ;
                int minVersion = dataArray[5] & 0x0F;
                version.append(year + "年");
                version.append(month + "月");
                version.append(day + "日");
                version.append("主版本号:" + mainVersion);
                version.append("次版本号:" + minVersion);
                LogUtils.d(TAG,"tpms Version:" + version.toString());
                if(mDataListener != null){
                    mDataListener.updateVersion(version.toString());
                }
            }
        }
    }

    /**
     * 查询报警参数
     */
    private void configErrorInfo(byte[] dataArray) {
        if(dataArray != null){
            if(dataArray.length > 4 ){//实例:AA  A1  B1  0A  21  00  20  12  41  9A //气压单位为 10kPa，温度单位为 1℃
                //依次为气压上限、 气压下限、温度上限
                int maxPressure = dataArray[2];
                int minPressure = dataArray[2+1];
                int maxTemperature = dataArray[2+2];
                if(mDataListener != null){
                    mDataListener.updateMaxPressure(maxPressure * pressureUnit);
                    mDataListener.updateMinPressure(minPressure * pressureUnit);
                    mDataListener.updateMaxTemperature(maxTemperature);
                }
                LogUtils.d(TAG,"configError maxPressure:" + maxPressure * pressureUnit);
                LogUtils.d(TAG,"configError minPressure:" + minPressure * pressureUnit);
                LogUtils.d(TAG,"configError maxTemperature:" + maxTemperature);
            }
        }
    }

    /**
     * 查询接收模块ID
     */
    private void wheelModuleIDInfo(byte[] dataArray) {
        if (dataArray != null) {
            if (dataArray.length > 3) {                                        //主机发送：AA  A1  B1  09  41  00  00  FF  45
                Integer modelId = MessesDefine.toIntL(new byte[]{dataArray[2],dataArray[3]});//数据字节：上位机发送 2 字节，依次是 IDL, IDH。
                int index = dataArray[1];
                if(mDataListener != null){
                    mDataListener.updateTireSensorID(index,modelId);
                }
//                Log.d(TAG,"wheelModuleIDInfo index:" + index + " modelId:" + modelId);
                LogUtils.d(TAG,"wheelModuleIDInfo index:" + index + " ID:"  /* + TpmsActivity.printHexString(new byte[]{dataArray[3],dataArray[2]})*/);
            }
        }
    }

    /**
     * 查询传感器信息
     * @param dataArray
     */
    private void wheelModuleInfo(byte[] dataArray) {
        if (dataArray != null) {
            if (dataArray.length > 5) {//报警优先级：无信号>漏气>高温>高气压、低气压>低电压
                Integer pressure = MessesDefine.toIntL(new byte[]{dataArray[2],dataArray[3]});//Byte0、Byte1：压力，单位为 kPa，依次是 PL, PH
                Integer temperature = (int)dataArray[4];//温度
                int lowPressure = dataArray[5]&0x01;//低气压
                int highPressure = (dataArray[5]>>1)&0x01;//高气压
                int highTemperature = (dataArray[5]>>2)&0x01;//高温
                int airLeakage = (dataArray[5]>>3)&0x01;//漏气
                int lowVoltage = (dataArray[5]>>4)&0x01;//低电压
                int signal = (dataArray[5]>>5)&0x01;//无信号
                int dataUpdate = (dataArray[5]>>6)&0x01;//数据更新 为 0 时表示当前数据已上传跟新，为 1 时表示当前数据为最新数据，未 上传跟新
                int validData = (dataArray[5]>>7)&0x01;//无信号 ：为 0 时表示当前数据有效，为 1 时表示当前数据无效。在从机刚启动时， 数据为上一次接收数据，此数据为无效数据
                int wheelLocation = dataArray[1];
                if(mDataListener != null){
                    mDataListener.updateTirePressure(wheelLocation,pressure);
                    mDataListener.updateTireTemperature(wheelLocation,temperature);
                    mDataListener.updateLowVoltage(wheelLocation,lowVoltage);
                    mDataListener.updateLowPressure(wheelLocation,lowPressure);
                    mDataListener.updateHighPressure(wheelLocation,highPressure);
                    mDataListener.updateHighTemperature(wheelLocation,highTemperature);
                    mDataListener.updateTireLeakageStatus(wheelLocation,airLeakage);
                    mDataListener.updateTireStatus(wheelLocation,signal);
                }
                LogUtils.e(TAG,"wheelModuleInfo------------------------------------");
                LogUtils.d(TAG,"wheelModuleInfo wheelLocation:" + wheelLocation);
                LogUtils.d(TAG,"wheelModuleInfo pressure:" + pressure);
                LogUtils.d(TAG,"wheelModuleInfo temperature:" + temperature);
                LogUtils.d(TAG,"wheelModuleInfo lowPressure:" + lowPressure);
                LogUtils.d(TAG,"wheelModuleInfo highPressure:" + highPressure);
                LogUtils.d(TAG,"wheelModuleInfo highTemperature:" + highTemperature);
                LogUtils.d(TAG,"wheelModuleInfo airLeakage:" + airLeakage);
                LogUtils.d(TAG,"wheelModuleInfo lowVoltage:" + lowVoltage);
                LogUtils.d(TAG,"wheelModuleInfo signal:" + signal);
                LogUtils.d(TAG,"wheelModuleInfo dataUpdate:" + dataUpdate);
                LogUtils.d(TAG,"wheelModuleInfo validData:" + validData);
                LogUtils.e(TAG,"wheelModuleInfo------------------------------------");
            }
        }
    }

    private void handShakeInfo(byte[] dataArray) {
        if (dataArray != null) {
            if (dataArray.length > 2) {
            }
        }
        Log.d(TAG," handShake!");
    }

    //------------------------------------------解析分发消息 end---------------------------------------


    List<ReadIdleListener> mReadIdleListener = new ArrayList<>();

    public void addReadIdleListener(ReadIdleListener readIdleListener) {
        if(this.mReadIdleListener != null){
            synchronized (mReadIdleListener) {
                this.mReadIdleListener.add(readIdleListener);
            }
        }
    }

    public void onDestroy(){
        if(this.mReadIdleListener != null){
            synchronized (mReadIdleListener) {
                mReadIdleListener.clear();
            }
        }
    }

    /**开始监听读空闲,在打开串口和接收到数据的时候启动*/
    public void startCheckingReadIdle(List<byte[]> result) {
        //发送接收空闲监听
        for (int i = 0; i < mReadIdleListener.size(); i++) {
            ReadIdleListener readIdleListener = mReadIdleListener.get(i);
            int delay = readIdleListener.getReadIdleTime();
            //重新刷新空闲时间
            if (hasMessages(delay)) {
                removeMessages(delay);
            }
            if (delay > 0) {
                sendEmptyMessageDelayed(delay, delay);
                LogUtils.d(TAG,"startCheckingReadIdle:" + delay);
            }else{
                if(result != null){
                    readIdleListener.onReadIdle(result);
                }
            }
        }
    }

    /**
     * 读取空闲监听
     */
    public interface ReadIdleListener {
        int NORMAL = 0;
        int REMOVE = 1;
        int LOOP = -1;

        int getReadIdleTime();

        /**
         * 读取数据后,空闲监听, 当onReadIdle返回true, 移除监听
         * @return
         *  -1 = 表示无限循环
         *  1  = 表示移除此监听
         *  0  = 表示无限循环
         */
        int onReadIdle(List<byte[]> result);
    }
}