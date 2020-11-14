package com.wwc2.main.driver.language;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the language listener.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class LanguageListener extends BaseListener {

    @Override
    public String getClassName() {
        return LanguageListener.class.getName();
    }

    public void LocaleListener(String oldLgg, String newLgg){}
}
