package com.wwc2.main.driver.memory.mode;

import android.text.TextUtils;

import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.memory.MemoryActionable;
import com.wwc2.main.manager.ConfigManager;

/**
 * the ini file memory.
 *
 * @author wwc2
 * @date 2017/1/11
 */
public abstract class BaseMemory implements Memoryable {

    /**
     * TAG
     */
    private static final String TAG = "BaseMemory";

    /**
     * the memory action.
     */
    protected MemoryActionable mMemoryAction = null;

    @Override
    public boolean clear() {
        boolean ret = false;
        final String path = absoluteFilePath();
        if (!TextUtils.isEmpty(path)) {
            ret = FileUtils.deleteFile(path);
        }
        return ret;
    }

    @Override
    public boolean exist() {
        boolean ret = false;
        final String path = absoluteFilePath();
        if (!TextUtils.isEmpty(path)) {
            ret = FileUtils.isFileExist(path);
        }
        return ret;
    }

    @Override
    public void setMemoryAction(MemoryActionable memoryAction) {
        mMemoryAction = memoryAction;
    }

    /**
     * 记忆文件绝对路径
     */
    public String absoluteFilePath() {
        String ret = null;
        if (null != mMemoryAction) {
            final String filePath = mMemoryAction.filePath();
            if (!TextUtils.isEmpty(filePath)) {
                if (filePath.startsWith("/") || filePath.startsWith("\\")) {
                    // 检查是否重写的绝对路径
                    ret = filePath;
                } else {
                    // 重写的相对路径，则计算绝对路径
                    final String dir = ConfigManager.getSystemConfigDir();
                    if (!TextUtils.isEmpty(dir)) {
                        ret = dir + filePath;
                    } else {
                        LogUtils.w(TAG, "path is error, dir = null.");
                    }
                }
            } else {
                LogUtils.w(TAG, "path is error, rewrite path = null.");
            }
        }
        return ret;
    }
}
