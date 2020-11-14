package com.wwc2.main.driver.system;

/**
 * the system driver interface.
 *
 * @author wwc2
 * @date 2017/1/23
 */
public interface SystemDriverable {

    /**
     * 收起下拉通知栏
     */
    boolean collapseStatusBar();

    /**
     * 重启
     */
    boolean reboot();

    /**
     * 恢复出厂设置，系统会重启
     */
    boolean restoreFactorySettings();

    /**
     * 清除用户数据，系统会重启
     */
    boolean wipeUserData();

    /**
     * 清除系统缓存，系统会重启
     */
    boolean wipeCache();

    boolean getRebootState();
}
