package com.wwc2.main.radio.driver;


public class RadioCommand extends CommandUtil{
	public static final byte OP_RADIOSWITCH = 0x20;	//开关命令字
	public static final byte OP_RADIOBUTTON = 0x21;	//搜台命令字
	public static final byte OP_RADIOSETREGION = 0x24;	//搜台命令字
	public static final byte OP_RADIOREQUSTREGION = 0x25; //APP请求收音区域信息
	public static final byte OP_RADIOACK = 0x2F; //应答MCU发过来的消息(0xa0~0xaf)

	public static final byte MRPT_RADIOINITDATA = (byte) 0xa0; //初始化或者切波段更新数据
	public static final byte MRPT_RADIOMAINDATA = (byte) 0xa1; //更新主频数据     保留
	public static final byte MRPT_RADIOPREDATA = (byte) 0xa2; //更新预存频点数据    保留
	public static final byte MRPT_RADIOFLAG = (byte) 0xa3; //更新标志位
	public static final byte MRPT_RADIOREGION = (byte) 0xa4; //更新标志位

	public static final byte RADIO_STOP = 0x02;		//收音停止退出界面
	public static final byte RADIO_FM_AM_SWITCH = 0x03;		//FM/AM切换
	private static final byte RADIO_FM1 = 0x04;
	private static final byte RADIO_FM2 = 0x05;
	private static final byte RADIO_FM3 = 0x06;
	private static final byte RADIO_AM1 = 0x07;
	private static final byte RADIO_AM2 = 0x08;
	private static final byte RADIO_FM 	= 0x09;
	private static final byte RADIO_AM	= 0x0A;
	private static final byte RADIO_BAND = 0x0B;
	private static final byte RADIO_LP1 = 0x0C;		//选台(点击)
	private static final byte RADIO_LP2 = 0x0D;
	private static final byte RADIO_LP3 = 0x0E;
	private static final byte RADIO_LP4 = 0x0F;
	private static final byte RADIO_LP5 = 0x10;
	private static final byte RADIO_LP6 = 0x11;
	private static final byte RADIO_SP1 = 0x12;		//保存电台(长按)
	private static final byte RADIO_SP2 = 0x13;
	private static final byte RADIO_SP3 = 0x14;
	private static final byte RADIO_SP4 = 0x15;
	private static final byte RADIO_SP5 = 0x16;
	private static final byte RADIO_SP6 = 0x17;


	public static final byte RADIO_NPRE = 0x18;	//切换下个预存台
	public static final byte RADIO_PPRE = 0x19;	//切换上个预存台
	public static final byte RADIO_STPUP = 0x1A;	//向上微调
	public static final byte RADIO_STPDN = 0x1B;	//向下微调
	public static final byte RADIO_SRHUP = 0x1C;	//向上搜索
	public static final byte RADIO_SRHDN = 0x1D;	//向下搜索
	public static final byte RADIO_SCAN = 0x1E;	//浏览
	public static final byte RADIO_ASRH = 0x1F;	//自动搜索
	public static final byte RADIO_STEREO = 0x20 ;//设置立体声
	public static final byte RADIO_LOC = 0x21;	//远程进程
	public static final byte RADIO_SET_FRQ = 0x22 ;//设置当前频率
	public static final byte RADIO_PW = 0x2C;//开关收音机声音
	public static final byte RADIO_CHANGE_ANGLE = 0x5A;//调节伸缩屏角度
	public static final byte RADIO_ANGLE = (byte) 0xC6;
	public static final byte[] openRadio = {0x01,0x01};	//开启收音机
	public static final byte[] closeRadio = {0x02,0x00};	//关闭收音机

	public static final byte[] RADIO_FM_AM_PAGE = {RADIO_FM1,RADIO_FM2,RADIO_FM3,RADIO_AM1,RADIO_AM2};	//fm+am索引页
//	public static final byte[] RADIO_AM_PAGE = {RADIO_AM1,RADIO_AM2};	//am索引页
	public static final byte[] RADIO_LPS = {RADIO_LP1,RADIO_LP2,RADIO_LP3,RADIO_LP4,RADIO_LP5,RADIO_LP6};//选台(点击)
	public static final byte[] RADIO_SPS = {RADIO_SP1,RADIO_SP2,RADIO_SP3,RADIO_SP4,RADIO_SP5,RADIO_SP6};//保存电台(长按)
	public static final byte[] RADIO_SETREGION = {0x00,0x01,0x02,0x03,0x04,0x05};	//0-美洲1.拉丁美洲2-欧洲 3-OIRT  4-日本 5-南美洲 6-东欧
	public static final byte[] RADIO_ENTER_REQUEST = {0x01,0x01};
	public static final byte[] RADIO_EXIT_REQUEST = {0x02,0x00};
	/**
	 * 收音机开关
	 * @param rSwitch
	 * @param request
	 * @return
	 */
	public static byte[] radioSwitch(byte rSwitch,byte request){
		return new byte[]{OP_RADIOSWITCH,rSwitch,request};
	}

	/**
	 * radio data
	 * @param param
	 * @return
	 */
	public static byte[] radioGeneral(byte param){
		return new byte[]{param,0x00,0x00};
	}

	/**
	 *
	 * @param data1
	 * @param param
	 * @return
	 */
	public static byte[] radioSetFrq(byte data1,int param){
		byte[] cmdBytes = new byte[3];
		cmdBytes[0] = data1;
		byte[] stations = toByteArrayh(param,2);
		cmdBytes[1] = stations[0];
		cmdBytes[2] = stations[1];
		return cmdBytes;
	}


}
