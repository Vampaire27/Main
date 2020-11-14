package com.wwc2.main.driver.memory;

/**
 * the memory interface.
 *
 * @author wwc2
 * @date 2017/1/11
 */
public interface MemoryActionable {

    /**
     * 记忆可变文件路径，重写绝对路径则使用绝对路径，重写相对路径则使用系统配置目录+相对文件路径为最终路径
     */
    String filePath();

    /**
     * 读取数据
     */
    boolean readData();

    /**
     * 写数据
     */
    boolean writeData();
}
