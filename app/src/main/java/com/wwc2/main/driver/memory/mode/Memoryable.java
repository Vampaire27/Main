package com.wwc2.main.driver.memory.mode;

import com.wwc2.main.driver.memory.MemoryActionable;

/**
 * the memory interface.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public interface Memoryable {

    /**
     * 读取记忆数据
     */
    boolean read();

    /**
     * 保存记忆数据
     */
    boolean save();

    /**
     * 清空记忆数据
     */
    boolean clear();

    /**
     * 记忆文件是否存在
     */
    boolean exist();

    /**
     * 设置记忆动作
     */
    void setMemoryAction(MemoryActionable memoryAction);

    /**
     * 获取值
     *
     * @param section 节点名称
     * @param key     属性名称
     * @return 获取的对象
     */
    Object get(String section, String key);

    /**
     * 设置值
     *
     * @param section 节点
     * @param key     属性名
     * @param value   属性值
     */
    boolean set(String section, String key, Object value);
}
