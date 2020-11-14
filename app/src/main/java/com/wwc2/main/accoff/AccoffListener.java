package com.wwc2.main.accoff;

import com.wwc2.corelib.listener.BaseListener;

/**
 * the acc off listener.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class AccoffListener extends BaseListener {

    /**
     * ACC OFF阶段定义
     */
    public static class AccoffStep {
        /**
         * 工作阶段
         */
        public static final int STEP_WORK = 0;

        /**
         * 暂停阶段，没有真正休眠，处于APP数据保存阶段
         */
        public static final int STEP_ACCOFF_PAUSE = 1;

        /**
         * 停止阶段，真正休眠，除了与启动相关的逻辑，其余逻辑均被销毁
         */
        public static final int STEP_ACCOFF_STOP = 2;

        /**
         * 恢复阶段，在没有真正休眠下上ACC触发
         */
        public static final int STEP_ACCOFF_RESUME = 3;

        /**
         * 启动阶段，在真正休眠下上ACC触发
         */
        public static final int STEP_ACCOFF_START = 4;

        /**
         * 默认阶段
         */
        public static final int STEP_DEFAULT = STEP_WORK;

        /**
         * 是否处于ACC OFF阶段
         */
        public static final boolean isAccoff(int step) {
            boolean ret = false;
            switch (step) {
                case STEP_ACCOFF_PAUSE:
                case STEP_ACCOFF_STOP:
                    ret = true;
                    break;
                default:
                    break;
            }
            return ret;
        }

        /**
         * 是否处于轻度睡眠
         */
        public static final boolean isLightSleep(int step) {
            boolean ret = false;
            if (STEP_ACCOFF_PAUSE == step) {
                ret = true;
            }
            return ret;
        }

        /**
         * 是否处于深度睡眠阶段
         */
        public static final boolean isDeepSleep(int step) {
            boolean ret = false;
            if (STEP_ACCOFF_STOP == step) {
                ret = true;
            }
            return ret;
        }

        /**
         * 转换为字符串
         */
        public static final String toString(int step) {
            String ret = null;
            switch (step) {
                case STEP_WORK:
                    ret = "STEP_WORK";
                    break;
                case STEP_ACCOFF_PAUSE:
                    ret = "STEP_ACCOFF_PAUSE";
                    break;
                case STEP_ACCOFF_STOP:
                    ret = "STEP_ACCOFF_STOP";
                    break;
                case STEP_ACCOFF_RESUME:
                    ret = "STEP_ACCOFF_RESUME";
                    break;
                case STEP_ACCOFF_START:
                    ret = "STEP_ACCOFF_START";
                    break;
                default:
                    break;
            }
            return ret;
        }
    }

    @Override
    public String getClassName() {
        return AccoffListener.class.getName();
    }

    /**
     * ACC OFF阶段监听器，see {@link AccoffListener.AccoffStep}
     * 如果ACC OFF在5S内上ACC：{@link AccoffListener.AccoffStep#STEP_ACCOFF_PAUSE} --> {@link AccoffListener.AccoffStep#STEP_ACCOFF_RESUME}
     * 如果ACC OFF在5S后上ACC：{@link AccoffListener.AccoffStep#STEP_ACCOFF_PAUSE} --> {@link AccoffListener.AccoffStep#STEP_ACCOFF_STOP} --> {@link AccoffListener.AccoffStep#STEP_ACCOFF_RESUME} --> {@link AccoffListener.AccoffStep#STEP_ACCOFF_START}
     */
    public void AccoffStepListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 快速倒车监听器
     */
    public void FastCameraListener(Boolean oldVal, Boolean newVal) {

    }
}
