package com.wwc2.main.uart_util;

import android.util.Log;



public class UartProtocal_RP implements UartProtocalDriverable{

	public final  int CMD_HEAD = 0xAA55;

	private final String TAG = "UartProtocal_RP";

	private  final boolean DEBUG = false;

	private final int DataState_HEAD_H= 0;
	private final int DataState_HEAD_L= 1;
	private final int DataState_LENGHT_H= 2;
	private final int DataState_LENGHT_L= 3;
	private final int DataState_CMD= 4;
	private final int DataState_DATA= 5;
	private final int DataState_CHECKSUM_H= 6;
	private final int DataState_CHECKSUM_L= 7;

	private final int CHECKSUM_LEN= 2;



	private int rxDataState = DataState_HEAD_H;

	private final int MAX_RX_LEN = 200;

	private int rxIndex = 0;
	private int rxDataLen;
	private int rxDataLenBak;
	private byte[] rxRawByteData = new byte[MAX_RX_LEN];
	boolean rxReceivedFinishFlag = false;

	byte[] leftRxRawByteData           = null;

	private final byte[] mcuAckArray={(byte)0x8f,(byte)0x9f,(byte)0xaf,(byte)0xbf,(byte)0xdf,(byte)0xef,(byte)0xf8};

	private final byte[] ArmMask={(byte)0x08,(byte)0x09,(byte)0x0a,(byte)0x0b,(byte)0x0c,(byte)0x0d,(byte)0x0e,(byte)0x0f};
	private final byte[] ArmAck={(byte)0x0F,(byte)0x1F,(byte)0x2F,(byte)0x3F,(byte)0x7f,(byte)0x4f,(byte)0x5f,(byte)0x68};

	private final int ACK_SIZE = 9;
	private final int ACK_DATA_LEN = 4; //n+2


	public byte[] packageData(byte head, byte[] buf, int len) {
		byte[] txbuf = new byte[len+7];
		int i= 0;

		txbuf[0] = (byte) (CMD_HEAD >> 8);
		txbuf[1] = (byte) (CMD_HEAD & 0x00ff);
		txbuf[2] = (byte) ((len+2) >> 8);
		txbuf[3] = (byte) ((len+2)& 0x00ff);

		txbuf[4] = head;

		 for (i =0;i < len ; i++){
			txbuf[i+5]= buf[i];
		}

		int mCheckSum = Check_Sum(txbuf,len+3);//data + DataState_LENGHT_H +DataState_LENGHT_L  + DataState_CMD

		txbuf[i+5]= (byte) ((mCheckSum >> 8) & 0x0FF);
		txbuf[i+6]= (byte) ((mCheckSum) & 0x0FF);
		return txbuf;
	}



	public  byte[] unpackageData(byte[] buffer) {
		int i;
		byte rxdata = 0;
		byte[] rev_checksum = new byte[2];
		byte[] calc_checksum = new byte[2];

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
					rxDataLen = ((rxRawByteData[DataState_LENGHT_H] << 8) + (rxRawByteData[DataState_LENGHT_L] & 0xff))-CHECKSUM_LEN;
					rxDataLenBak=rxDataLen;  //datalen +checknum
					break;

				case DataState_DATA: // data
					if (--rxDataLen == 0)
						 rxDataState = DataState_CHECKSUM_H;//
					break;

				case DataState_CHECKSUM_H: // crc1
					rxDataState = DataState_CHECKSUM_L;
					rev_checksum[0]=rxdata;
					break;

				case DataState_CHECKSUM_L: // crc2
					rev_checksum[1]=rxdata;
					rxReceivedFinishFlag = true; // 结束了
					break;
			}
			rxRawByteData[rxIndex] = rxdata;

			rxIndex++;

			// 处理数据
			if (rxReceivedFinishFlag) {
				rxReceivedFinishFlag = false;
				int checksum_int = Check_Sum(rxRawByteData,rxDataLenBak+3);
				calc_checksum[0] =(byte) ((checksum_int >> 8) & 0x0FF);
				calc_checksum[1] =(byte) ((checksum_int) & 0x0FF);
				if(rev_checksum[0] == calc_checksum[0] && rev_checksum[1] == calc_checksum[1] ){
					if (DEBUG) Log.d(TAG,"reviver a cmd complete!");
					rxDataState = DataState_HEAD_H;
					if(i < buffer.length) {
						getLeftBytes(buffer, (i + 1), (buffer.length -1-i)); // cmd + data
					}
					return subBytes(rxRawByteData, 4, rxDataLenBak + 1); // cmd + data
				}else{
					Log.d(TAG,"reviver cmd checksun error calc checksum_int = " + checksum_int);
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


	public  byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		for (int i=begin; i<begin+count; i++){
			bs[i-begin] = src[i];}
		return bs;
	}

	public Boolean isAckData(byte[] buffer) {
		for(int i=0; i < mcuAckArray.length;i++){
			if(buffer[0] == mcuAckArray[i]){
				if (DEBUG) {
					String hex = Integer.toHexString(buffer[0] & 0xFF);
					Log.d(TAG, "From MCU isAckData = " + hex.toUpperCase() + " ");
				}
				return  true;
			}
		}
		return  false;
	}

	public  byte [] packAckData(byte[] buffer) {
		int iner;
		byte temp_cmd = buffer[0];
		byte[] ackData = new byte[ACK_SIZE];

		ackData[0] = (byte) (CMD_HEAD >> 8);
		ackData[1] = (byte) (CMD_HEAD & 0x00ff);
		ackData[2] = (byte) (ACK_DATA_LEN >> 8);
		ackData[3] = (byte) (ACK_DATA_LEN & 0x00ff);

		for(iner = 0;iner < ArmMask.length; iner ++){
			if(ArmMask[iner]== ((temp_cmd >>4)& 0x0f)){
				ackData[4] = ArmAck[iner];
				break;
			}
		}

		if(iner > ArmMask.length){
			if (DEBUG) {
				String hex = Integer.toHexString(buffer[1] & 0xFF);
				Log.d(TAG, "MCU send a Unknow cmd,cant no find ack "  + hex.toUpperCase());
			}
			return null;
		}

		ackData[5] = buffer[0];
		ackData[6] = buffer[1];

		int mCheckSum = Check_Sum(ackData,5);
		ackData[7] =(byte) ((mCheckSum >> 8) & 0x0FF);
		ackData[8] =(byte) ((mCheckSum) & 0x0FF);

	   return ackData;
	}


	public byte[] getleftRxRawByteData(){
		return leftRxRawByteData;
	};


	// checksum  is 16bit

	int Check_Sum(byte[] Data, int Len) {
		int CheckSum = 0;
		for (int i = 0; i < Len; i++) {
			CheckSum += (Data[i+2]&0xFF); // DataState_HEAD_H & DataState_HEAD_L needn't  checksum ,so +2
		}
		return CheckSum;
	}


//	static int Check_Sum(byte[] Data, int Len) {
//		int CheckSum = 0;
//		for (int i = 0; i < Len; i++) {
//			CheckSum += Data[i+2]; // DataState_HEAD_H & DataState_HEAD_L needn't  checksum ,so +2
//		}
//		CheckSum= CheckSum^0xff + 1;
//		return CheckSum;
//	}




}
