package com.wwc2.main.radio.driver;

/**
 * the radio module interface.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public interface RadioDriverable {

    /**
     * 上一频点
     */
    void prevFrequency();

    /**
     * 下一频点
     */
    void nextFrequency();

    /**
     * 向上搜索
     */
    void prevSearch();

    /**
     * 向下搜索
     */
    void nextSearch();

    /**
     * 搜台
     */
    void AMS();

    /**
     * 浏览电台
     */
    void PS();

    /**
     * 切换波段, see {@link com.wwc2.radio_interface.RadioDefine.Band}
     */
    void changeBand(int band);
    /**
     * 智能切换波段, see {@link com.wwc2.radio_interface.RadioDefine.Band}
     */
    void changeBand();

    /**
     * 处理按键FM／AM
     * @param bFm
     */
    void changFmAm(boolean bFm);

    /**
     * 替换站台频率，station是替换的站台
     */
    void replaceStation(int station);

    /**
     * 选中电台频率(currentIndex 1- 6)
     */
    void LP(int currentIndex);

    /**
     * 保存电台频率(currentIndex 1- 6)
     */
    void SP(int currentIndex);

    /**
     * 下一个预存电台
     */
    void NPRE();

    /**
     * 上一个预存电台
     */
    void PPRE();

    /**
     * AM/FM 切换
     */
    void changeFmAm();
    /**
     * 开收音机
     */
    void radioOpen();

    /**
     * 关收音机
     */
    void radioClose();
    /**
     * 退出音机
     */
    void radioExit();
    /**
     * 打开立体声
     */
    void openST();

    /**
     * 关闭立体声
     */
    void closeST();

    /**
     * 切换本地近程
     */
    void switchLOC();

    /**
     * 切换远程
     */
    void switchDX();

    /**
     * 打开RDS功能
     */
    void openRds();

    /**
     * 关闭RDS功能
     */
    void closeRds();

    /**
     * RDS的PTY功能，type为PTY的类型
     */
    void PTY(int type);

    /**
     * RDS的TA功能
     */
    void TA();
    void TA(int value);

    /**
     * RDS的TP功能
     */
    void TP();

    /**
     * RDS的AF功能
     */
    void rds_AF();

    /**
     * 设置收音区域
     */
    void setArea(int area);

    /**
     * Fm发射
     */
    void setFmSendCmd(int type, int value);

    /**
     * 调节角度
     */
    void changeAngle(int value);
}
