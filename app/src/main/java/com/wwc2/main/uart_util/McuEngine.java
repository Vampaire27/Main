package com.wwc2.main.uart_util;

import android.util.Log;

import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.jni.mcu_serial.JniToJava;
import com.wwc2.main.android_serialport_api.SerialManager;
import com.wwc2.main.driver.client.ClientDriver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static com.wwc2.main.driver.client.driver.BaseClientDriver.CLIENT_BS;
import static com.wwc2.main.driver.client.driver.BaseClientDriver.CLIENT_RP;
import static com.wwc2.main.driver.client.driver.BaseClientDriver.CLIENT_ST;

public class McuEngine {
	private static final String TAG = "McuEngine";

	private SerialManager serialManager = null;

	private Object mSendListSemaphore = new Object(); //list modify sync

	private Semaphore mUartSemaphore = new Semaphore(1); // uart0  only one. so need Semaphore.

	private UartProtocalDriverable mProtocal = null;

	private Object mSendListThead = new Object(); //list modify sync

	private ArrayList<Byte> receiveByteBuffer = new ArrayList<Byte>();

	public final static int SERIAL_PRIORITY_NORMAL = 1;//普通，存在，则替换，buffer已满，则发送失败，普通命令
	public final static int SERIAL_PRIORITY_HIGH = 2;//高，buffer已满，则丢弃第一条数据，重要数据，比如模式命令(不检查)，模式数据
	public final static int SERIAL_PRIORITY_HIGHEST = 3;//最高，清除所有缓存数据，发送该条数据，比如开关机命令
	public final static int SERIAL_PRIORITY_DIRECT = 4;//直接发送到串口，ACK发送方式

	public final static int TIME_INTERVAL = 500; //400ms

	public static Thread mSendThread; //200ms

	private final LinkedList<byte[]> mCmdQueue = new LinkedList();

	private final LinkedList<byte[]> mCmdQueueSave = new LinkedList();

	private final LinkedList<byte[]> mActionCmd = new LinkedList();

	private Thread mRcThread;

	private enum LinkState {
		NULL, LINKING, LINKED, LINK_FAILED
	}

	private LinkState linkState = LinkState.NULL;
	private boolean mSentListThreadStart = false;

	final ArrayList<WaitAckData> WaitAckDataList
			= new ArrayList<WaitAckData>();


	class WaitAckData {
		int times = 2;
		byte[] txbuf;
		long nextSendTime;

		WaitAckData(byte[] mtxbuf) {
			txbuf = mtxbuf;
			nextSendTime = System.currentTimeMillis() + TIME_INTERVAL;
			Log.d(TAG, "System.currentTimeMillis()" + nextSendTime);
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

	private static McuEngine instance = null;

	public static McuEngine getInstance() {
		if (instance == null) {
			instance = new McuEngine();
		}
		return instance;
	}

	McuEngine() {
		Log.d(TAG, "make a McuEngine...");
	}


	public void start() {
		if (serialManager == null) {
			serialManager = SerialManager.getInstance();
			serialManager.registerReceiveListener(receiveListener);
		}
		Probe();
	}


	public void stop() {
		Log.d(TAG, "McuEngine stop...");
		if (serialManager != null) {
			serialManager.stop();
			serialManager = null;
		}
	}

	private boolean isConnecting() {
		if (linkState == LinkState.LINKING)
			return true;
		else
			return false;
	}


	private void Probe() {
		Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
		String clientProject;
		if (client != null) {
			clientProject = client.getString("ClientProject");
		} else clientProject = CLIENT_BS;
		if (mProtocal == null) {
			switch (clientProject) {
				case CLIENT_RP:
					mProtocal = new UartProtocal_RP();
					break;
				case CLIENT_BS:
					mProtocal = new UartProtocal_BS();
					break;
				case CLIENT_ST:
					mProtocal = new UartProtocal_ST();
					break;
				default:
					mProtocal = new UartProtocal_BS();
					break;
			}
		}
		if (isConnecting())
			return;
		linkState = LinkState.LINKING;

		new Thread(new Runnable() {
			public void run() {
				int i;
				ArrayList<Byte> buffer = null;
				receiveByteBuffer.clear();
/*
				for (int j = 0; j < 20; j++) {
					for (i = 0; i < 5; i++) {
						byte[] txbuf = ObdVkelProtocal.SendCmdLink();
						sendCmd(txbuf);
						buffer = getRespData();
						if (buffer != null) {
							mObdVersion = ObdVkelProtocal.getObdVersion(buffer);
							if (mObdVersion != 0) {
								mLinked = true;
								if (mObdVersion < 0xa0) {
									protocolType = PROTOCOL_VKEL;
									linkState = LinkState.VKEL_LINKED;
								} else {
									protocolType = PROTOCOL_MCU;
									linkState = LinkState.ILINCAR_LINKED;
								}
								return;
							}
						}
					}
				}*/

				if (linkState == linkState.LINKING) {
					linkState = LinkState.LINKED;
				}
			}
		}).start();
	}


	private void enqueueLocked(byte[] cmd) {
		mCmdQueue.add(cmd);
		if (mRcThread == null) {
			mRcThread = new RCThread();
			mRcThread.start();
		}
	}

	private OnReceiveListener receiveListener = new OnReceiveListener() {
		public void onBytesReceive(byte[] buffer) {
			synchronized (mCmdQueue) {
				//Log.d(TAG, "enqueueLocked thread1 recive data save to mCmdQueue");
				enqueueLocked(buffer);
			}
		}

		@Override
		public void onError() {

		}
	};

	public void sendCmd(final byte[] txbuf) {
		if (serialManager != null) {
			synchronized (mUartSemaphore) {
				try {
					mUartSemaphore.acquire(); // make sure only one thread to use uart0
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				serialManager.sendBytes(txbuf);
				mUartSemaphore.release();
			}
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


	public int DEVICE_write(boolean needAck, int priority, boolean check, byte head, byte[] buf, int len) {

		byte txbuf[];

		txbuf = mProtocal.packageData(head, buf, len);

		sendCmd(txbuf);
		if (!needAck) {
			return 0;
		}

		if (mSentListThreadStart == false) {
			satrtSendCmdList();
			mSentListThreadStart = true;
		}

		synchronized (mSendListSemaphore) {
			addToAckList(txbuf);
		}
		synchronized (mSendListThead) {
			mSendListThead.notifyAll();
		}

		return 0;
	}


	private void satrtSendCmdList() {
		mSendThread = new Thread(new Runnable() {
			public void run() {

				while (true) {
					try {
						//Log.d(TAG, "send cmd Thread.sleep 200/4ms===");
						Thread.sleep(TIME_INTERVAL / 4);
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

									sendCmd(WaitAckDataList.get(index).txbuf);

									if (WaitAckDataList.get(index).isEnd()) {
										Log.d(TAG, "======start not recive ack,the data is lose");
                                        Log.d(TAG, " send it  for  ack_error buffer=" + FormatData.formatHexBufToString(WaitAckDataList.get(index).txbuf, WaitAckDataList.get(index).txbuf.length));
//                                        for (int j = 0; j < WaitAckDataList.get(index).txbuf.length; j++) {
//											String hex = Integer.toHexString(WaitAckDataList.get(index).txbuf[j] & 0xFF);
//											Log.d(TAG, " send it  for  ack_error buffer[" + j + "] = " + hex.toUpperCase() + " ");
//										}

										WaitAckDataList.remove(index);

									}
								}
							}
						}
						try {
							//Log.d(TAG, "send cmd Thread.sleep 200/4ms===");
							Thread.sleep(TIME_INTERVAL / 4);
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

	private void sendAck(final byte[] txbuf) {
		new Thread(new Runnable() {
			public void run() {
				if (serialManager != null) {
					serialManager.sendBytes(txbuf);
				}
			}
		}).start();
	}


	private final class RCThread extends Thread {

		RCThread() {
			super("RCThread- start");
		}

		public void run() {
			while (true) {
               //1.backup the recevier data
				synchronized (mCmdQueue) {
					while(mCmdQueue.size()>0) {
						//Log.d(TAG, "mCmdQueueSave backdata +++");
						mCmdQueueSave.add(mCmdQueue.removeFirst());
					}
				}

                // make the receiver data to cmdline
				while(mCmdQueueSave.size() > 0){
					byte[] packet = mProtocal.unpackageData(mCmdQueueSave.removeFirst());
					if(packet !=null){
					    mActionCmd.add(packet);
					}
					if (mProtocal.getleftRxRawByteData() != null) {
						mCmdQueueSave.addFirst(mProtocal.getleftRxRawByteData());
					}
				}

//				for (int i = 0; i < mActionCmd.size(); i++) {
//					String hex = Integer.toHexString(mActionCmd.get(i)[0] & 0xFF);
//					Log.d(TAG, "   mActionCmd[0]" + "= " + hex.toUpperCase() + " ");
//				}

				//pre deal with the ack cmd
				for(int i = 0; i < mActionCmd.size() ; i++ ){
					if(mProtocal.isAckData(mActionCmd.get(i))){
						synchronized (mSendListSemaphore) {
							if (mActionCmd.get(i).length <= 2) {
								//Log.d(TAG, "isAckData is less 2");
								removeFromAckList(mActionCmd.get(i)[1]);
							} else {
								//Log.d(TAG, "isAckData is  2");
								removeFromAckList(mActionCmd.get(i)[1], mActionCmd.get(i)[2]);
							}
						}
					}
				}


				while (mActionCmd.size() > 0){
					//begin zhongyang.hu remove the MCU ACK. 20180201
					//byte[] ackData = mProtocal.packAckData(packet);
					//if (ackData != null) {
					//	sendCmd(ackData);
					//}
					//end
					byte [] cmd = mActionCmd.removeFirst();
					JniToJava.dispatch(cmd, cmd.length);
				}


				synchronized (mCmdQueue) {
					if (mCmdQueue.size() == 0) {
						// nothing left to do, quit
						// doing this check after we're done prevents the case where they
						// added it during the operation from spawning two threads and
						// trying to do them in parallel.
						//Log.d(TAG, "todo recive Thread  finish");
						mRcThread = null;
						return;
					}
				}
			}
		}
	}
}
