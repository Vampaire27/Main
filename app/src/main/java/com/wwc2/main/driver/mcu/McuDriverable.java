package com.wwc2.main.driver.mcu;

/**
 * the mcu driver interface.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public interface McuDriverable {

    /**
     * 通信数据接收超时
     */
    public static final int ERROR_RECV_DATA_TIMEOUT = 1;

    /**
     * 串口发送优先级
     */
    public final static int SERIAL_PRIORITY_NORMAL = 1;//普通，存在，则替换，buffer已满，则发送失败，普通命令
    public final static int SERIAL_PRIORITY_HIGH = 2;//高，buffer已满，则丢弃第一条数据，重要数据，比如模式命令(不检查)，模式数据
    public final static int SERIAL_PRIORITY_HIGHEST = 3;//最高，清除所有缓存数据，发送该条数据，比如开关机命令
    public final static int SERIAL_PRIORITY_DIRECT = 4;//直接发送到串口，ACK发送方式

    /**
     * MCU串口打开状态
     */
    public final static int OPEN_RET_REPEAT = 0;
    public final static int OPEN_RET_SUCCESS = 1;

    /**
     * MCU串口关闭状态
     */
    public final static int CLOSE_RET_REPEAT = 0;
    public final static int CLOSE_RET_SUCCESS = 1;

    /**
     * open the mcu serial.
     *
     * @return the {@link #OPEN_RET_REPEAT} or {@link #OPEN_RET_SUCCESS}, other is failed.
     */
    int open();

    /**
     * close the mcu serial.
     *
     * @return the {@link #CLOSE_RET_REPEAT} or {@link #CLOSE_RET_SUCCESS}, other is failed.
     */
    int close();

    /**get first boot status
     *
     * @return see {@link com.wwc2.common_interface.Define.FirstBoot}
     */
    int firstBoot();

    /**get mcu memory source.
     *
     * @return see {@link com.wwc2.common_interface.Define.Source}
     */
    int getMemorySource();

    /**
     * 发送MCU数据
     *
     * @param priority 串口发送优先级
     * @param check    true表示重复数据检查并替换，false不进行检查
     * @param head     头码
     * @param buf      数据
     * @param len      数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    int sendMcu(boolean needAck,int priority, boolean check, byte head, byte[] buf, int len);

    /**
     * 发送MCU数据，针对模块重要的数据
     *
     * @param head 头码
     * @param buf  数据
     * @param len  数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    int sendMcuImportant(byte head, byte[] buf, int len);

    /**
     * 发送MCU数据，普通发送方式
     *
     * @param head 头码
     * @param buf  数据
     * @param len  数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    int sendMcu(byte head, byte[] buf, int len);



    /**
     * 发送MCU数据，普通发送方式 不需要应答
     *
     * @param head 头码
     * @param buf  数据
     * @param len  数据长度
     * @return 1表示发送成功，-1表示发送失败
     */
    int sendMcuNack(byte head, byte[] buf, int len);


}
