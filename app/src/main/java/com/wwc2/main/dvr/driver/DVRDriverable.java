package com.wwc2.main.dvr.driver;

/**
 * the dvr driver interface.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public interface DVRDriverable {
    /**
     * 设置自动保存录像时间
     */
    void setAutoSaveTime(String selection);

    /**
     * 设置视频质量
     */
    void setVideoQualiqy(String selection);

    /**
     * 设置静音录像
     */
    void setMuteRecord(boolean selection);

    /**
     * 设置开机自动录像
     */
    void setAutoRecord(boolean selection);

    /**
     * 设置水印显示
     */
    void setWatermark(boolean selection);

    /**
     * 设置存储路径
     */
    void setLocation(String selection);
}
