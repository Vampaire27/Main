package com.wwc2.main.eventinput;

import com.wwc2.corelib.db.Packet;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.logic.Logicable;

/**
 * the event input define.
 *
 * @author wwc2
 * @date 2017/1/25
 */
public class EventInputDefine {

    /**
     * config: module select.
     */
    public static final String MODULE = "com.wwc2.main.eventinput.EventInputLogic";

    /**MCU错误状态(<b>int error</b>), see {@link McuDriverable}*/
    public static final int MODULE_ID_MCU_ERROR = 1;
    /**SOURCE切换(<b>int source</b>), see {@link com.wwc2.common_interface.Define.Source}*/
    public static final int MODULE_ID_SOURCE_CHANGE = 2;

    /**模块事件输入*/
    public static final int EVENT_INPUT_MODULE = 1;

    /**按键事件输入*/
    public static final int EVENT_INPUT_KEY = 2;

    /**状态事件输入*/
    public static final int EVENT_INPUT_STATUS = 3;

    /**通用模块事件输入，入口为{@link Logicable#onModuleEvent(int, Packet)}*/
    public static final class Module {
        /**通用模块事件输入ID最大值*/
        protected static final int MAX = 1000000;

        /**恢复出厂设置<b>void</b>*/
        public static final int RESUME_FACTORY_SETTING = MAX + 1;
    }

    /**状态事件输入*/
    public static final class Status {
        /**ACC状态变化<b>boolean on</b>*/
        public static final int TYPE_ACC = 1;

        /**大灯状态变化<b>boolean on</b>*/
        public static final int TYPE_ILL = 2;

        /**倒车状态变化<b>boolean on</b>*/
        public static final int TYPE_CAMERA = 3;

        /**刹车状态变化<b>boolean on</b>*/
        public static final int TYPE_BRAKE = 4;

        /**本地flash状态变化<b>boolean mount</b>*/
        public static final int TYPE_NAND_FLASH = 5;

        /**媒体卡状态变化<b>boolean mount</b>*/
        public static final int TYPE_MEDIA_CARD = 6;

        /**导航卡状态变化<b>boolean mount</b>*/
        public static final int TYPE_GPS_CARD = 7;

        /**USB状态变化<b>boolean mount</b>*/
        public static final int TYPE_USB = 8;

        /**USB1状态变化<b>boolean mount</b>*/
        public static final int TYPE_USB1 = 9;

        /**USB2状态变化<b>boolean mount</b>*/
        public static final int TYPE_USB2 = 10;

        /**USB3状态变化<b>boolean mount</b>*/
        public static final int TYPE_USB3 = 11;

        /**CD ROM状态变化<b>boolean mount</b>*/
        public static final int TYPE_CD_ROM = 12;

        /**IPOD状态变化<b>boolean mount</b>*/
        public static final int TYPE_IPOD = 13;

        /**DVD状态变化<b>int status</b>*/
        public static final int TYPE_DVD = 14;

        /**左转向灯<b>boolean leftLight</b>*/
        public static final int TYPE_LEFT_LIGHT = 15;

        /**右转向灯<b>boolean rightLight</b>*/
        public static final int TYPE_RIGHT_LIGHT = 16;

        /**BEEP输入(<b>void</b>)*/
        public static final int TYPE_BEEP = 17;
    }
}
