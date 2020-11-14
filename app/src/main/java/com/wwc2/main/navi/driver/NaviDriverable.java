package com.wwc2.main.navi.driver;

/**
 * the navigation driver interface.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public interface NaviDriverable {

    /**设置导航包名*/
    boolean setNavigationPacketName(String pkgName);

    String getNavigationPacketName();
}
