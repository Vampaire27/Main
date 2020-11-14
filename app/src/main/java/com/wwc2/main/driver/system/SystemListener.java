package com.wwc2.main.driver.system;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the system listener.
 *
 * @author wwc2
 * @date 2017/1/31
 */
public class SystemListener extends BaseListener {

    @Override
    public String getClassName() {
        return SystemListener.class.getName();
    }

    /**应用安装监听器*/
    public void AppInstallListener(String pkgName) {

    }

    /**应用卸载监听器*/
    public void AppUninstallListener(String pkgName) {

    }

    /**
     * 重启监听器
     */
    public void rebootStateListener(Boolean oldVal,Boolean newVal) {

    }
}
