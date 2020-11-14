package com.wwc2.main.radio.driver;


/**
 * 
 * @author huwei
 *	6737协议封装
 */

public abstract class CommandUtil {
	public static final byte REQ_FIRST_HEAD = (byte)0xAA;		//CPU启动后向MCU发送握手信号，表明CPU串口准备好。
	public static final byte REQ_SECOND_HEAD = 0x55;		//CPU启动后向MCU发送握手信号，表明CPU串口准备好。
	private static final int numLength = 2;	//cmd和checkSum位
    private static final int headLength = 2;//包头位
    private static final int byteLength = 2;//长度位

    public static byte[] getData(byte[] params,int startIndex){
        byte[] datas = new byte[params.length - startIndex];//数据+cmd;
        System.arraycopy(params, startIndex, datas, 0, datas.length);
        return datas;
    }

	/**
     * 通用数据包封装
     *
     * @param
     * @param cmd
     * @return
     */
    public static byte[] cmdBaseReq(byte cmd,byte[] cmds) {
        byte[] writeByte = new byte[headLength + byteLength + numLength + cmds.length];//
        writeByte[0] = REQ_FIRST_HEAD;
        writeByte[1] = REQ_SECOND_HEAD;
        writeByte[2] = (byte) ((cmds.length + numLength)>>8) ;
        writeByte[3] = (byte) ((cmds.length + numLength)&0xff);
        writeByte[4] = cmd;
        int j = 5;
        for (int i = 0; i < cmds.length; i++) {
            writeByte[j + i] = cmds[i];
        }
        writeByte[writeByte.length - 1] = checkSum(writeByte);
        printHexString(writeByte);
        return writeByte;
    }
    
    /**
     * 计算校验和
     *
     * @param cmds 命令包
     * @return
     */
    public static byte checkSum(byte[] cmds) {
        byte checkSum = 0x00;
        for (int j = 2; j < cmds.length - 1; j++) {
            checkSum += cmds[j];
        }
        return (byte)(0 - checkSum);
    }
    
    /**
     * 打印数据包
     * @param b 源数组
     */
    public static String printHexString(byte[] b) {
    	StringBuffer mStringBuffer = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            mStringBuffer.append(hex.toUpperCase()+",");
            
        }
//        System.out.println(mStringBuffer.toString());
        return mStringBuffer.toString();
    }

    /**
     * 组装低位在前的字节数组
     * @param iSource   整形数据
     * @param iArrayLen 字节数组长度 0-4
     * @return  返回低位在前的字节数组
     */
    public static byte[] toByteArrayl(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }
        return bLocalArr;
    }

    /**
     * 组装高位在前的字节数组
     * @param iSource   整形数据
     * @param iArrayLen 字节数组长度 0-4
     * @return  返回高位在前的字节数组
     */
    public static byte[] toByteArrayh(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * (iArrayLen - i - 1) & 0xFF);
        }
        return bLocalArr;
    }

    /**
     *解析低位在前的字节数组  返回整数
     * @param bRefArr  低位在前的字节数组
     * @return  整形数据
     */
    public static int toIntl(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }
    /**
     *解析高位在前的字节数组  返回整数
     * @param bRefArr  高位在前的字节数组
     * @return  整形数据
     */
    public static int toInth(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += ((bLoop & 0xFF) << (8 * (bRefArr.length-i-1)));
        }
        return iOutcome;
    }

    /**
     *解析高位在前的字节数组  返回整数
     * @param bRefArr  高位在前的字节数组
     * @return  整形数据
     */
    public static float toFloath(byte[] bRefArr) {
        float iOutcome = 0.0f;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * (bRefArr.length-i-1));
        }
        return iOutcome;
    }
}
