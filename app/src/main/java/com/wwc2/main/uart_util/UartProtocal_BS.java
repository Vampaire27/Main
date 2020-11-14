package com.wwc2.main.uart_util;

import android.util.Log;

/**
 * Created by swd1 on 17-5-6.
 */

public class UartProtocal_BS implements UartProtocalDriverable {

    public final static int CMD_HEAD        = 0xAA55;

    private static final String TAG         = "UartProtocal_BS";

    private final boolean DEBUG      = false;

    private final int DataState_HEAD_H   = 0;
    private final int DataState_HEAD_L   = 1;
    private final int DataState_LENGHT_H = 2;
    private final int DataState_LENGHT_L = 3;
    private final int DataState_CMD      = 4;
    private final int DataState_DATA     = 5;
    private final int DataState_CHECKSUM = 6;

    private final int CHECKSUM_LEN       = 1;
    private final int CMD_LEN       = 1;
    private int rxDataState              = DataState_HEAD_H;

    private final int MAX_RX_LEN         = 200;
    private int rxIndex                  = 0;
    private int rxDataLen;
    private int rxDataLenBak;
    private byte[] rxRawByteData         = new byte[MAX_RX_LEN];
    boolean rxReceivedFinishFlag         = false;

    byte[] leftRxRawByteData           = null;

    private final byte mcuAck            = (byte)0xFF;

    private final byte ArmAck            = (byte)0x7F;

    private final int ACK_SIZE           = 7;//9;
    private final int ACK_DATA_LEN       = 3;//4; //n+2

    /**将发送数据打包*/
    public byte[] packageData(byte head, byte[] buf, int len) {
        byte[] txbuf = new byte[len+6];
        int i= 0;

        txbuf[0] = (byte) (CMD_HEAD >> 8);
        txbuf[1] = (byte) (CMD_HEAD & 0x00ff);
        txbuf[2] = (byte) ((len+2) >> 8);
        txbuf[3] = (byte) ((len+2)& 0x00ff);

        txbuf[4] = head;

        for (i =0;i < len ; i++){
            txbuf[i+5]= buf[i];
        }

        txbuf[i+5]= Check_Sum(txbuf,len+3);
        return txbuf;
    }

    public byte[] unpackageData(byte[] buffer) {
        int i;
        byte rxdata = 0;
        byte rev_checksum = 0x00;
        byte calc_checksum = 0x00;

        leftRxRawByteData = null;

        //rxDataState = DataState_HEAD_H;  // for every get data is from HEAD. may need more modify.
        if (DEBUG) {
            Log.d(TAG, "reviver buffer.length  = ==" + buffer.length);
        }

        for (i = 0; i < buffer.length; i++) {
            rxdata = buffer[i];

            if (DEBUG) {
                String hex = Integer.toHexString(buffer[i] & 0xFF);
                Log.d(TAG, "  hex buffer[" + i + "] = " + hex.toUpperCase() + " ");
            }

            switch (rxDataState) {
                case DataState_HEAD_H: // head H
                    rxIndex = 0;
                    if (rxdata == (byte) (CMD_HEAD >> 8))
                        rxDataState = DataState_HEAD_L;
                    break;
                case DataState_HEAD_L: // head Low
                    if (rxdata == (byte) (CMD_HEAD & 0xff))
                        rxDataState = DataState_LENGHT_H;
                    else
                        rxDataState = DataState_HEAD_H; // error
                    break;
                case DataState_LENGHT_H: // lenght
                    rxDataState = DataState_LENGHT_L;
                    break;
                case DataState_LENGHT_L: // lenght
                    rxDataState = DataState_CMD;
                    break;
                case DataState_CMD: // cmd
                    rxDataState = DataState_DATA;
                    rxDataLen = ((rxRawByteData[DataState_LENGHT_H] << 8) + (rxRawByteData[DataState_LENGHT_L] & 0xff))-CHECKSUM_LEN-CMD_LEN;
                    rxDataLenBak=rxDataLen;  //datalen +checknum
                    break;
                case DataState_DATA: // data
                    if (--rxDataLen == 0)
                        rxDataState = DataState_CHECKSUM;//
                    break;
                case DataState_CHECKSUM: // crc
                    rev_checksum=rxdata;
                    rxReceivedFinishFlag = true; // 结束了
                    break;
            }
            rxRawByteData[rxIndex] = rxdata;

            rxIndex++;

            // 处理数据
            if (rxReceivedFinishFlag) {
                rxReceivedFinishFlag = false;
                calc_checksum = Check_Sum(rxRawByteData,rxDataLenBak+3);
                if(rev_checksum == calc_checksum) {
                    if (DEBUG) Log.d(TAG,"reviver a cmd complete!");
                    rxDataState = DataState_HEAD_H;
                    if(i < buffer.length) {
                        getLeftBytes(buffer, (i + 1), (buffer.length -1-i)); // cmd + data
                    }
                    return subBytes(rxRawByteData, 4, rxDataLenBak + 1); // cmd + data
                }else{
                    Log.d(TAG,"reviver cmd checksun error calc calc_checksum = " + calc_checksum);
                }
                rxDataState = DataState_HEAD_H;
            } else if (rxIndex >= MAX_RX_LEN) {
                rxDataState = DataState_HEAD_H; // error
                Log.d(TAG,"reviver a cmd too long!");
            }
        }
        return null;
    }

    public  void getLeftBytes(byte[] src, int begin, int count) {
        leftRxRawByteData = new byte[count];
        for (int i = begin; i < begin + count; i++) {
            leftRxRawByteData[i - begin] = src[i];
        }
    }

    public byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++){
            bs[i-begin] = src[i];}
        return bs;
    }

    public Boolean isAckData(byte[] buffer) {
        if(buffer[0] == mcuAck){
            if (DEBUG) {
                String hex = Integer.toHexString(buffer[0] & 0xFF);
                Log.d(TAG, "From MCU isAckData = " + hex.toUpperCase() + " ");
            }
            return  true;
        }
        return  false;
    }

    /**将发送ACK数据打包*/
    public byte [] packAckData(byte[] buffer) {
        byte[] ackData = new byte[ACK_SIZE];

        ackData[0] = (byte) (CMD_HEAD >> 8);
        ackData[1] = (byte) (CMD_HEAD & 0x00ff);
        ackData[2] = (byte) (ACK_DATA_LEN >> 8);
        ackData[3] = (byte) (ACK_DATA_LEN & 0x00ff);
        ackData[4] = ArmAck;
        ackData[5] = buffer[0];
        ackData[6] = Check_Sum(ackData,5);

        return ackData;
    }

    public byte[] getleftRxRawByteData(){
        return leftRxRawByteData;
    };



//    byte Check_Sum(byte[] Data, int Len) {
//        byte CheckSum = 0;
//        for (int i = 0; i < Len; i++) {
//            CheckSum += (Data[i+2]&0xFF); // DataState_HEAD_H & DataState_HEAD_L needn't  checksum ,so +2
//        }
//       // CheckSum = (byte) (CheckSum^0xFF+1);
//        CheckSum=  (byte)(0xFF - (byte)CheckSum + (byte)1);
//        return CheckSum;
//    }


     byte Check_Sum(byte[] Data, int Len) {byte CheckSum = 0;
		for (int i = 0; i < Len; i++) {
			CheckSum += (byte) Data[i+2]; // DataState_HEAD_H & DataState_HEAD_L needn't  checksum ,so +2
		}
         CheckSum= (byte) ((byte)(CheckSum ^ 0xff) +1);
		return CheckSum ;
	}


}

