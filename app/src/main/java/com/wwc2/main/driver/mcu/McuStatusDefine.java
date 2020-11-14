package com.wwc2.main.driver.mcu;

/**
 * the mcu status define.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public class McuStatusDefine {

    /**MCU通信错误(<b>int error</b>)*/
    public static final int MODULE_ID_MCU_ERROR = 1;

    /**MCU打开状态*/
    public static class OpenStatus {
        /**未知*/
        public static final int UNKNOWN = 0;

        /**打开成功*/
        public static final int SUCCESS = 1;

        /**打开失败*/
        public static final int FAILED = 2;

        /**已打开，无需再打开*/
        public static final int NONEED = 3;

        /**to string.*/
        public static String toString(int status) {
            String ret = null;
            switch (status) {
                case UNKNOWN: ret = "UNKNOWN"; break;
                case SUCCESS: ret = "SUCCESS"; break;
                case FAILED: ret = "FAILED"; break;
                case NONEED: ret = "NONEED"; break;
                default: break;
            }
            return ret;
        }
    }

    /**MCU关闭状态*/
    public static class CloseStatus {
        /**未知*/
        public static final int UNKNOWN = 0;

        /**关闭成功*/
        public static final int SUCCESS = 1;

        /**关闭失败*/
        public static final int FAILED = 2;

        /**已关闭，无需再关闭*/
        public static final int NONEED = 3;

        /**to string.*/
        public static String toString(int status) {
            String ret = null;
            switch (status) {
                case UNKNOWN: ret = "UNKNOWN"; break;
                case SUCCESS: ret = "SUCCESS"; break;
                case FAILED: ret = "FAILED"; break;
                case NONEED: ret = "NONEED"; break;
                default: break;
            }
            return ret;
        }
    }
}
