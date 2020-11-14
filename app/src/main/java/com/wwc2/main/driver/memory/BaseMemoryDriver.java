package com.wwc2.main.driver.memory;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.main.driver.memory.mode.BaseMemory;
import com.wwc2.main.driver.memory.mode.ini.IniMemory;
import com.wwc2.main.driver.system.SystemDriver;
import com.wwc2.main.driver.system.SystemListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.settings_interface.SettingsDefine;

import java.util.HashSet;
import java.util.List;

/**
 * the base memory driver.
 *
 * @author wwc2
 * @date 2017/1/11
 */
public abstract class BaseMemoryDriver extends BaseDriver implements MemoryActionable {

    /**
     * TAG
     */
    private static final String TAG = "BaseMemoryDriver";

    /**
     * the memory interface.
     */
    protected BaseMemory mMemory = null;

    /**
     * 源监听器
     */
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            BaseLogic oldLogic = ModuleManager.getLogicBySource(oldVal);
            BaseLogic newLogic = ModuleManager.getLogicBySource(newVal);
            if (null != oldLogic && null != newLogic) {
                if (!oldLogic.isPoweroffSource() && newLogic.isPoweroffSource()) {
                    // 进入关机源，保存数据
                    if (null != mMemory) {
                        mMemory.save();
                    }
                }
            }
        }
    };

    /**
     * 监听重启状态
     */
    private SystemListener systemListener = new SystemListener() {
        @Override
        public void rebootStateListener(Boolean oldVal,Boolean newVal) {
            //add by huwei 180426.导入配置文件后该次重启不保存数据,避免覆盖导入的配置文件
            boolean importState = LogicManager.getLogicByName(SettingsDefine.MODULE).getInfo().getBoolean("ImportState");
            if (newVal && !importState) {
                // 重启前先保存数据
                if (null != mMemory) {
                    mMemory.save();
                }
            }
        }
    };

    @Override
    public void onPrepare() {
        super.onPrepare();

        mMemory = new IniMemory();
        mMemory.setMemoryAction(this);
        mMemory.read();
    }

    @Override
    public void onCreate(Packet packet) {
        if (autoSave()) {
            SourceManager.getModel().bindListener(mSourceListener);
            DriverManager.getDriverByName(SystemDriver.DRIVER_NAME).getModel().bindListener(systemListener);
        }
    }

    @Override
    public void onDestroy() {
        if (autoSave()) {
            SourceManager.getModel().unbindListener(mSourceListener);
        }
//        mMemory = null;
    }

    @Override
    public boolean writeData() {
        return false;
    }

    /**
     * absolute file path
     */
    public String absoluteFilePath() {
        String ret = null;
        if (null != mMemory) {
            ret = mMemory.absoluteFilePath();
        }
        return ret;
    }

    /**
     * auto save data to file.
     */
    public boolean autoSave() {
        return true;
    }

    /**
     * remove duplicate item.
     */
    protected static void removeDuplicate(List list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
    }
}
