package com.wwc2.main.tpms.driver.protocol;

/**
 * Created by huwei on 2017/1/21.
 *
 *
 * 帧头                                   数据段                     校验
 * 起始字节 被叫地址 主叫地址 长度字节    功能号 子功能号 数据字节   校验字节
 1Byte 1Byte 1Byte 1Byte 1Byte 1Byte nByte 1Byte
 *
 *
 */
public class MessesDefine {
    public static final byte EXTERNAL_MOD_SET = (byte)0x61;
    public static final byte EXTERNAL_MOD_DATA = (byte)0xF0;
    public static final byte MOD_DATA_SERIAL_PORT = (byte)0x00;
    public static final byte  HEADER = (byte)0xAA;//头
    public static final byte HOST_DEVICE = (byte)0xA1;//主机
    public static final byte CHILD_DEVICE = (byte)0xB1;//从机

    public static class CarWheel{//车轮
        public static final byte LEFT_BACK = 0x00;//左后
        public static final byte LEFT_FRONT = 0x01;//左前
        public static final byte RIGHT_FRONT = 0x02;//右前
        public static final byte RIGHT_BACK = 0x03;//右后
    }
    public static class FunctionCmd {
        /**
         * 描述：主机发送 握手命令 握手命令 ，从机应答握手命令表明从机存在且正常工作。
         * 子功能号：无。
         * 数据字节：无
         */
        public static final byte HAND_SHAKE = 0x11;//握手
        /**
         * 查询报警参数
         * 描述：主机发送0x21，从机 应答 报警参数。
         子功能号：无
         数据字节： 主机 发送时无数据字节，从机应答时有 3字节，依次为气压上限、气压下限、温度上限
         备注 ：气压单位为 10kPa，温度单位为 1℃
         */
        public static final byte QUERY_ERROR_CONFIG = 0x21;//查询报警参数
        /**
         * 设置报警参数
         */
        public static final byte SET_ERROR_CONFIG = 0x31;//设置报警参数
        /**
         * 查询ID
         */
        public static final byte QUERY_WHEEL_ID = 0x41;//查询ID
        /**
         * 设置车轮ID
         */
        public static final byte SET_WHELL_ID = 0x51;//设置ID
        /**
         * 配对命令
         */
        public static final byte PAIRING = 0x61;
        /**
         * 查询数据
         */
        public static final byte QUERY_INFO = 0x71;//查询数据
        /**
         * 查询协议版本号
         */
        public static final byte QUERY_TPMS_VERSION = (byte)0x81;
        /**
         * 查询协议版本号
         */
        public static final byte TPMS_ERROR= (byte)0xFF;
    }


    public static class Query{
        /**
         * 查询车轮Id
         * @return
         */
        public static byte[] cmdQueryWheelId(byte tireLocation){
            return assemblyData(FunctionCmd.QUERY_WHEEL_ID,tireLocation);
        }
        /**
         * 查询报警参数
         * @return
         */
        public static byte[] cmdQueryErrorConfig(){
            return assemblyData(FunctionCmd.QUERY_ERROR_CONFIG,(byte)0x00);
        }

        /**
         * 握手
         * @return
         */
        public static byte[] cmdHandShake(){
            return assemblyData(FunctionCmd.HAND_SHAKE,(byte)0x00);
        }

        /**
         * 配对
         * @param tireLocation
         * @return
         */
        public static byte[] cmdPairing(byte tireLocation){
            return assemblyData(FunctionCmd.PAIRING,tireLocation);
        }

        /**
         * 查询
         * @param tireLocation
         * @return
         */
        public static byte[] cmdQuery(byte tireLocation){
            return assemblyData(FunctionCmd.QUERY_INFO,tireLocation);
        }

        /**
         * 查询版本号
         * @return
         */
        public static byte[] cmdQueryVersion(){
            return assemblyData(FunctionCmd.QUERY_TPMS_VERSION,(byte)0x00);
        }
    }

    public static class Error {
        public static final byte ERROR_DATA = 0x01;//通信错误 (起始地址，被叫地址，主叫地址，长度字节，校验字节等错误)
        public static final byte ERROR_FUNCTION = 0x02;//不支持该功能号
        public static final byte ERROR_CHILD_FUNCTION = 0x03;//不支持该子功能号
        public static final byte ERROR_DATA_BYTE = 0x04;    //不支持该数据字节
        public static final byte ERROR_WRITE_ROM = 0x05;    //写ROM失败(写ROM失败后从机数据保存为缓存最新数据)
        public static final byte ERROR_PAIRING_TIMEOUT = 0x06; //配对超时
        public static final byte ERROR_FR_HARDWARE = 0x07;//接收机RF硬件错误，上电初始化过程中测试，每秒发一次
        //0x08：1字节，表示 轮位
        //0x09：1字节，表示轮位
        public static final byte ERROR_PRESSURE_SENSOR = 0x08;//压力传感器硬件错误，每秒发一次
        public static final byte ERROR_TEMPERATURE_SENSOR = 0x09;//温度传感器硬件错误，每秒发一次

        /**
         * 通信错误 (起始地址，被叫地址，主叫地址，长度字节，校验字节等错误)
         * @return
         */
        public static byte[] cmdErrorData(){
            return assemblyData(FunctionCmd.TPMS_ERROR,ERROR_DATA);
        }

        /**
         * 不支持该功能号
         * @return
         */
        public static byte[] cmdErrorFunction(){
            return assemblyData(FunctionCmd.TPMS_ERROR,ERROR_FUNCTION);
        }

        /**
         * 不支持该子功能号
         * @return
         */
        public static byte[] cmdErrorChildFunction(){
            return assemblyData(FunctionCmd.TPMS_ERROR,ERROR_CHILD_FUNCTION);
        }

        /**
         * 不支持该数据字节
         * @return
         */
        public static byte[] cmdErrorDataByte(){
            return assemblyData(FunctionCmd.TPMS_ERROR,ERROR_DATA_BYTE);
        }

        /**
         * 写ROM失败(写ROM失败后从机数据保存为缓存最新数据)
         * @return
         */
        public static byte[] cmdErrorWriteRom(){
            return assemblyData(FunctionCmd.TPMS_ERROR,ERROR_WRITE_ROM);
        }

        /**
         * 配对超时
         * @return
         */
        public static byte[] cmdErrorPairingTimeout(){
            return assemblyData(FunctionCmd.TPMS_ERROR,ERROR_PAIRING_TIMEOUT);
        }

        /**
         * 接收机RF硬件错误，上电初始化过程中测试，每秒发一次
         * @return
         */
        public static byte[] cmdErrorFrHardware(){
            return assemblyData(FunctionCmd.TPMS_ERROR,ERROR_FR_HARDWARE);
        }

        /**
         * 压力传感器硬件错误，每秒发一次
         * @return
         */
        public static byte[] cmdErrorPressureSensor(byte tireLocation){
            return assemblyData(FunctionCmd.TPMS_ERROR,new byte[]{tireLocation , ERROR_PRESSURE_SENSOR});
        }
        /**
         * 压力传感器硬件错误，每秒发一次
         * @return
         */
        public static byte[] cmdErrorTemperatureSensor(byte tireLocation){
            return assemblyData(FunctionCmd.TPMS_ERROR,new byte[]{tireLocation , ERROR_TEMPERATURE_SENSOR});
        }

    }

    /**
     * 报警参数
     */
    public static class Config{
        public static final byte PARAMETER_MAX_PRESSURE = 0x00;     //气压上限
        public static final byte PARAMETER_MIN_PRESSURE = 0x01;     //气压下限
        public static final byte PARAMETER_MAX_TEMPERATURE = 0x02;  //温度下限
        public static byte[] setMaxPressure(Integer pressure){
            return assemblyData(FunctionCmd.SET_ERROR_CONFIG,new byte[]{PARAMETER_MAX_PRESSURE,(byte)(pressure/10)});//270-400
        }
        public static byte[] setMinPressure(Integer pressure){
            return assemblyData(FunctionCmd.SET_ERROR_CONFIG,new byte[]{PARAMETER_MIN_PRESSURE,(byte)(pressure/10)});//170-260
        }
        public static byte[] setMaxTemperature(int Temperature){
            return assemblyData(FunctionCmd.SET_ERROR_CONFIG,new byte[]{PARAMETER_MAX_TEMPERATURE,(byte)Temperature});//50-90
        }
        public static byte[] setTireSensorID(int tireLocation, int tireSensorID){
            byte[] bytes = toByteArrayl(tireSensorID,2);
            return assemblyData(FunctionCmd.SET_WHELL_ID,new byte[]{(byte)tireLocation,bytes[0],bytes[1]});
        }
    }


    public static byte[] assemblyData(byte cmd,byte data){
        return assemblyData(cmd, new byte[]{data});
    }

    /**
     * 封装数据包
     * @param cmd
     * @param dataN
     * @return
     */
    public static byte[] assemblyData(byte cmd,byte[] dataN){
        byte[] cmdFrame = null;
        if(dataN != null){
            cmdFrame = new byte[4 + (1 + dataN.length) + 1];//帧头 + 数字字段 + 校验;
            cmdFrame[0] = (byte)0xAA;
            cmdFrame[1] = (byte)0xB1;
            cmdFrame[2] = (byte)0xA1;
            cmdFrame[3] = (byte)cmdFrame.length;
            cmdFrame[4] = cmd;
            byte checkSum = (byte)(cmdFrame[0] + cmdFrame[1]+cmdFrame[2]+cmdFrame[3]+cmdFrame[4]);
            int j = 4+1;
            for(int i =0 ;i < dataN.length;i++){
                cmdFrame[j+ i] = dataN[i];
                checkSum += dataN[i];
            }
            cmdFrame[cmdFrame.length -1] = checkSum;
        }
        return cmdFrame;
    }

    /**
     * 封装数据包
     * @param dataN
     * @return
     */
    public static byte[] assemblyData(byte[] dataN){
        byte[] cmdFrame = null;
        if(dataN != null){
            cmdFrame = new byte[4 + (dataN.length) + 1];//帧头 + 数字字段 + 校验;
            cmdFrame[0] = (byte)0xAA;
            cmdFrame[1] = (byte)0xB1;
            cmdFrame[2] = (byte)0xA1;
            cmdFrame[3] = (byte)cmdFrame.length;
            byte checkSum = (byte)(cmdFrame[0] + cmdFrame[1]+cmdFrame[2]+cmdFrame[3]);
            int j = 3+1;
            for(int i =0 ;i < dataN.length;i++){
                cmdFrame[j+ i] = dataN[i];
                checkSum += dataN[i];
            }
            cmdFrame[cmdFrame.length -1] = checkSum;
        }
        return cmdFrame;
    }

    /**
     * 计算校验和
     * @param dataArray
     * @return
     */
    public static byte checkSum(byte[] dataArray){
        byte checkSum = 0x00;
        for(int i = 0; i< dataArray.length -1;i++){
            checkSum += dataArray[i];
        }
        return (byte)(checkSum &0xFF);
    }

    public static int toIntL(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
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
        return mStringBuffer.toString().substring(0,mStringBuffer.length()-1);
    }
}
