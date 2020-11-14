package com.wwc2.main.driver.tptouch;

import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.FourInteger;

/**
 * the tp touch listener.
 *
 * @author wwc2
 * @date 2017/1/23
 */
public class TPTouchListener extends BaseListener {

    @Override
    public String getClassName() {
        return TPTouchListener.class.getName();
    }

    /**
     * 位置监听
     */
    public interface TPPositionListener {
        /**
         * TP 按下，返回true，则截获消息
         */
        boolean TPDown(int x, int y);

        /**
         * TP 弹起，返回true，则截获消息
         */
        boolean TPUp(int x, int y);
    }

    /**绑定时候,初始化*/
    public void InitListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**可触摸区域监听*/
    public void TouchRectsListener(FourInteger[] oldVal, FourInteger[] newVal) {

    }

    /**
     * 区域监听
     */
    public void RectsListener(FourInteger[] oldVal, FourInteger[] newVal) {

    }

    /**
     * 短按按键码监听
     */
    public void CodesListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * 长按按键码监听
     */
    public void LongCodesListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * 持续按键码监听
     */
    public void LastCodesListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**
     * 是否正在学习监听
     */
    public void LearnListener(Boolean oldVal, Boolean newVal) {

    }

    /**学习按键的坐标点，上下左右偏移的像素*/
    public void PixelOffsetListener(Integer oldVal, Integer newVal) {

    }

    /**
     * TP x,y 坐标交换
     */
    public void TPCoordinateSwitchListener(Integer oldVal, Integer newVal) {

    }
}
