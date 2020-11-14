package com.wwc2.main;

/**
 * the sdk version.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public class SDKVersion {

    /**
     * 获取SDK版本
     */
    public static String getSDKVersion() {
        return "1.0.8";
    }

    /**
     * 1.0.1
     * 1、分离Main的抽象层和业务层；
     *
     * 1.0.2
     * 1、增加TV逻辑的处理；
     * 2、调试TPMS功能；
     *
     * 1.0.3
     * 1、修改系统无法休眠的问题，使用同行者语音自带休眠的功能；
     * 2、修改个别机器蓝牙通话无声音的问题（通话走模块）数据配置文件丢失导致；
     *
     * 1.0.4
     * 1、分离SDK版本定义；
     *
     * 1.0.5
     * 1、修改ACC ON USB视频不播放；
     * 2、修改ACC ON SD卡拔卡还有SD卡显示；
     *
     * 1.0.6
     * 1、修改断开ACC开关倒车休眠唤醒页面记忆错误的问题；
     *
     * 1.0.7
     * 1、修改开机以及休眠唤醒对MCU串口的操作流程；
     * 2、修改唤醒起来弹出STK记忆会在主页的问题；
     *
     * 1.0.8
     * 1、增加CANBUS数据PROVIDER导出；
     *
     * 1.0.9
     * 1、雷达延迟1500关闭；
     */
}
