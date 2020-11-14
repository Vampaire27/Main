package com.wwc2.main.driver.language.driver;

import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MString;
import com.wwc2.main.driver.language.LanguageDriverable;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.settings_interface.SettingsDefine;

/**
 * the base language driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class BaseLanguageDriver extends BaseMemoryDriver implements LanguageDriverable {

    /**数据Model*/
    protected static class LanguageModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString(SettingsDefine.Language.LANGUAGE, getLanguage().getVal());
            return packet;
        }

        /**
         * 语言监听
         */
        private MString mLanguage = new MString(this, "LocaleListener", Define.Language.zh_CN.name());
        public MString getLanguage() {
            return mLanguage;
        }
    }

    @Override
    public BaseModel newModel() {
        return new LanguageModel();
    }

    /**
     * get the model object.
     */
    protected LanguageModel Model() {
        LanguageModel ret = null;
        BaseModel model = getModel();
        if (model instanceof LanguageModel) {
            ret = (LanguageModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "LanguageDataConfig.ini";
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final String language = Model().getLanguage().getVal();
            if (!TextUtils.isEmpty(language)) {
                mMemory.set("LANGUAGE", "language", language);
            }
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            Object object = mMemory.get("LANGUAGE", "language");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getLanguage().setVal(string);
                    ret = true;
                }
            }
        }
        return ret;
    }

    @Override
    public void set(String language) {
        Model().getLanguage().setVal(language);
    }

    @Override
    public String get() {
        return Model().getLanguage().getVal();
    }
}
