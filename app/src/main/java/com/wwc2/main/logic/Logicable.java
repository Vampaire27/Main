package com.wwc2.main.logic;

import android.content.Context;

import com.wwc2.corelib.db.Packet;
import com.wwc2.main.eventinput.EventInputDefine;

import java.util.List;

/**
 * the logic interface.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public interface Logicable {

    /**
     * 得到类型名称，用于标识模块类型
     *
     * @return 模块类型名称
     */
    String getTypeName();

    /**
     * 得到工厂设置中是否打开了该模块
     *
     * @return 打开返回true，关闭返回false
     */
    boolean funcModule();

    /**
     * 模块是否处于准备状态
     *
     * @return 是否处于准备状态
     */
    boolean isReady();

    /**
     * 是否第一次启动
     *
     * @return 是否需要第一次启动的状态位，见{@link com.wwc2.common_interface.Define.FirstBoot}
     */
    int firstBoot();

    /**
     * 模块是否当作source处理
     *
     * @return 是否为source
     */
    boolean isSource();

    /**
     * 模块对应的source是否可以放入SOURCE栈中，默认为{@link #isSource()}的值
     *
     * @return 是否可以放入SOURCE栈中
     */
    boolean isStackSource();

    /**
     * 是否是关机source
     */
    boolean isPoweroffSource();

    /**
     * 是否是关屏source
     */
    boolean isScreenoffSource();

    /**
     * 是否为全屏source
     */
    boolean isFullScreenSource();

    /**
     * 是否声音条消失source
     */
    boolean isVolumeHideSource();

    /**
     * 是否语音消失source
     */
    boolean isVoiceHideSource();

    /**
     * 是否蓝牙悬浮框消失source
     */
    boolean isHFPFloatHideSource();

    /**
     * 自己处理开背光
     */
    boolean handleBacklightOn();

    /**
     * 返回模块当前对应的SOURCE的值
     *
     * @return 模块当前对应的SOURCE的值，见{@link com.wwc2.common_interface.Define.Source}
     */
    int source();

    /**
     * 得到该模块的使能状态，在工厂设置中打开，对应的APK存在则视为使能
     *
     * @return 使能返回true, 不使能返回false
     */
    boolean enable();

    /**
     * 该模块是否可用，在使能的状态下，还要{@link #isReady()}为真
     *
     * @return 可用返回true, 不可用返回false
     */
    boolean available();

    /**
     * 被动模式，只可能通过触发进入，而没有主动进入的入口
     * 用于是否处理调整source的接口
     *
     * @return true被动，false不是被动模式
     */
    boolean passive();

    /**
     * 启动APK，如果不处理，则使用默认启动方式{@link com.wwc2.corelib.utils.apk.ApkUtils#runApk(Context, String, String, Packet, boolean)}
     *
     * @return true模块处理了，false使用默认启动方式
     */
    boolean runApk();

    /**
     * 得到APK的包名
     *
     * @return APK的包名
     */
    String getAPKPacketName();

    /**
     * 得到APK的类名
     *
     * @return APK的类名
     */
    String getAPKClassName();

    /**
     * 得到APK包名列表
     */
    List<String> getAPKPacketList();

    /**
     * 进入包名，始终为logic的{@link #getAPKPacketName()}
     */
    boolean isEnterAlwaysPackage();

    /**
     * 通用消息中的模块事件入口
     *
     * @param id     接口ID
     * @param packet 接口参数
     * @return 接口返回值
     */
    Packet onModuleEvent(int id, Packet packet);

    /**
     * 通用消息中的按键事件入口
     *
     * @param keyOrigin 按键来源，{@link com.wwc2.common_interface.Define.KeyOrigin}
     * @param key       按键值，{@link com.wwc2.common_interface.Define.Key}
     * @param packet    接口参数
     * @return 接口返回值
     */
    boolean onKeyEvent(int keyOrigin, int key, Packet packet);

    /**
     * 通用消息中的状态事件入口
     *
     * @param type   状态类型，{@link EventInputDefine.Status}
     * @param status 状态
     * @param packet 接口参数
     * @return 接口返回值
     */
    boolean onStatusEvent(int type, boolean status, Packet packet);
}
