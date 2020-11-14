package com.wwc2.main.uart_util;

/**
 * Created by swd1 on 17-5-23.
 */

public interface UartProtocalDriverable {

     byte[] packageData(byte head, byte[] buf, int len);

     byte[] unpackageData(byte[] buffer) ;

     byte [] packAckData(byte[] buffer);

     Boolean isAckData(byte[] buffer);

    byte[] getleftRxRawByteData();
}
