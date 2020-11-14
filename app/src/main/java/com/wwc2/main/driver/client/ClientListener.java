package com.wwc2.main.driver.client;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the client listener.
 *
 * @author wwc2
 * @date 2017/1/19
 */
public class ClientListener extends BaseListener {

    @Override
    public String getClassName() {
        return ClientListener.class.getName();
    }

    /**
     * 客户项目号
     */
    public void ClientProjectListener(String oldVal, String newVal) {

    }

    /**
     * 客户版本号
     */
    public void ClientVersionListener(String oldVal, String newVal) {

    }

    /**
     * 客户ID版本号
     */
    public void ClientIDVersionListener(Long id) {

    }
}
