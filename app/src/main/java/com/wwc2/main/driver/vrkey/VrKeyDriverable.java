package com.wwc2.main.driver.vrkey;

/**
 * the vr key driver interface.
 *
 * @author wwc2
 * @date 2017/1/21
 */
public interface VrKeyDriverable {

    /**
     * simulate the key.
     * @param key the key, see {@link com.wwc2.common_interface.Define.Key}
     * @return true execute success, false execute failed.
     */
    boolean simulate(int key);
}
