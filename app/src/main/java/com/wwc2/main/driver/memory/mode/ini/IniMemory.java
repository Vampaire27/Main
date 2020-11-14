package com.wwc2.main.driver.memory.mode.ini;

import android.text.TextUtils;

import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.common.driver.STM32CommonDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.factory.driver.STM32FactoryDriver;
import com.wwc2.main.driver.memory.MemoryActionable;
import com.wwc2.main.driver.memory.mode.BaseMemory;
import com.wwc2.main.driver.tptouch.driver.MTK6737TPTouchDriver;
import com.wwc2.main.manager.ConfigManager;
import com.wwc2.main.settings.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * the ini file memory.
 *
 * @author wwc2
 * @date 2017/1/11
 */
public class IniMemory extends BaseMemory {

    /**ini file object.*/
    private IniFile mIniFile = null;

    private final String FACTORY_FILE_BAK = ConfigManager.getSystemConfigDir() + "FactoryDataConfig_bak.ini";
    private final String CONFIG_BAK_PATH = "/system/etc/";

    @Override
    public void setMemoryAction(MemoryActionable memoryAction) {
        super.setMemoryAction(memoryAction);

        final String path = absoluteFilePath();
        if (TextUtils.isEmpty(path)) {
            mIniFile = new IniFile();
        } else {
            try {
                if (null != mMemoryAction) {
                    if (mMemoryAction instanceof STM32FactoryDriver) {
                        String configFile = ConfigManager.getSystemConfigDir() + mMemoryAction.filePath();
                        if (FileUtil.checkExist(configFile)) {
                            FileUtil.copyFile(configFile, FACTORY_FILE_BAK);
                        } else {
                            LogUtils.e("IniMemory", "FactoryDataConfig.ini not exist!");
                            if (FileUtil.checkExist(FACTORY_FILE_BAK)) {
                                FileUtil.copyFile(FACTORY_FILE_BAK, configFile);
                            } else {
                                LogUtils.e("IniMemory", "FactoryDataConfig_bak.ini not exist!");
                            }
                        }
                    } else if (mMemoryAction instanceof STM32CommonDriver) {
                        //有客户要求通过配置文件，所以不再接收MCU的开关状态，由Main自己处理。2018-11-14
                        //当客户按键音默认关时，此处是防止恢复出厂设置后又会变成开。
                        String commonFile = ConfigManager.getSystemConfigDir() + mMemoryAction.filePath();
                        String bakFile = CONFIG_BAK_PATH + mMemoryAction.filePath();
                        if (FileUtil.checkExist(bakFile) && !FileUtil.checkExist(commonFile)) {
                            FileUtil.copyFile(bakFile, commonFile);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mIniFile = new IniFile(new File(path));
        }
    }

    @Override
    public boolean read() {
        boolean ret = false;
        if (null != mIniFile) {
            if (null != mMemoryAction) {
                ret = mMemoryAction.readData();
            }
        }
        return ret;
    }

    @Override
    public boolean save() {
        boolean ret = false;
        final String path = absoluteFilePath();
        if (TextUtils.isEmpty(path)) {
            mIniFile = new IniFile();
        } else {
            //add by hwei to fix ini file data lost when power outage after acc.
//            if (mMemoryAction instanceof MTK6737TPTouchDriver) {
                mIniFile = new IniFile(new File(path));
//            } else {
//                FileUtils.deleteFile(path);
//                mIniFile = new IniFile(new File(path));
//            }
            //end
        }
        if (null != mMemoryAction) {
            ret = mMemoryAction.writeData();
            if (ret) {
                if (null != mIniFile) {
                    mIniFile.save();
                }
            }
        }
        return ret;
    }

    @Override
    public Object get(String section, String key) {
        Object ret = null;
        if (null != mIniFile) {
            ret = mIniFile.get(section, key);
        }
        return ret;
    }

    @Override
    public boolean set(String section, String key, Object value) {
        boolean ret = false;
        if (null != mIniFile) {
            mIniFile.set(section, key, value);
        }
        return ret;
    }

    public void reloadIniFile() {
        if (mIniFile != null) {
            mIniFile.reloadFromFile();
        }
    }
}
