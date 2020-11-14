package com.wwc2.jni.mcu_serial;

import com.wwc2.main.uart_util.McuEngine;

/**
 * JNI提供的方法
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class McuSerialNative {


    //private static final byte[] test={(byte)0x8f,(byte)0x9f,(byte)0xaf,(byte)0xbf,(byte)0xef,(byte)0xf8};

    public  static McuEngine mMcuEngine;

    // 串口FLAG
    public final static int O_CLSTTY = 0x10000000;

    public final static  int DEVICE_open(String DevName, int BaudRate, int Flags){

        int open =1;
        if(mMcuEngine == null ){
            mMcuEngine= McuEngine.getInstance();
        }
        mMcuEngine.start();

       // mMcuEngine.sendCmd(test);

        return open;
     };

    public final static  int DEVICE_write(boolean needAck, int priority, boolean check, byte head, byte[] buf, int len){
        mMcuEngine.DEVICE_write(needAck,priority,check,head,buf,len);

        return 1;
    };

    public final static  int DEVICE_close(){
        mMcuEngine.stop();
        return 1;
    };
}
