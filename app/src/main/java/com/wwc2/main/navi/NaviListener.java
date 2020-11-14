package com.wwc2.main.navi;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the navigation listener.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public class NaviListener extends BaseListener {

    @Override
    public String getClassName() {
        return NaviListener.class.getName();
    }

    /**
     * 导航包名发生变化
     */
    public void NaviPacketNameListener(String oldVal, String newVal) {

    }

    /**
     * 导航包名列表监听器
     */
    public void NaviPacketListListener(String[] oldVal, String[] newVal) {

    }

    /**
     * 确认后的导航包名列表监听器
     */
    public void NaviRealPacketListListener(String[] oldVal, String[] newVal) {

    }
}
