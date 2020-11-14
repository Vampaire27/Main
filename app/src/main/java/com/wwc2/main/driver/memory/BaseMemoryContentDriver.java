package com.wwc2.main.driver.memory;

import com.wwc2.corelib.db.Packet;
import com.wwc2.main.driver.memory.mode.BaseMemory;
import com.wwc2.main.driver.memory.mode.ini.IniMemory;

/**
 * the base memory content driver.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public abstract class BaseMemoryContentDriver extends BaseMemoryDriver {

    /**
     * TAG
     */
    private static final String TAG = "BaseMemoryDriver";

    /**
     * the memory interface.
     */
    protected BaseMemory mContentMemory = null;

    /**
     * 记忆可变文件路径，重写绝对路径则使用绝对路径，重写相对路径则使用系统配置目录+相对文件路径为最终路径
     */
    public abstract String contentFilePath();

    /**
     * 读取数据
     */
    public abstract boolean contentReadData();

    /**
     * 写数据
     */
    public abstract boolean contentWriteData();

    @Override
    public void onPrepare() {
        super.onPrepare();

        mContentMemory = new IniMemory();
        mContentMemory.setMemoryAction(new MemoryActionable() {
            @Override
            public String filePath() {
                return contentFilePath();
            }

            @Override
            public boolean readData() {
                return contentReadData();
            }

            @Override
            public boolean writeData() {
                return contentWriteData();
            }
        });
        mContentMemory.read();
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        mContentMemory = null;
        super.onDestroy();
    }

    @Override
    public boolean writeData() {
        return false;
    }

    /**
     * absolute file path
     */
    public String absoluteContentFilePath() {
        String ret = null;
        if (null != mContentMemory) {
            ret = mContentMemory.absoluteFilePath();
        }
        return ret;
    }
}