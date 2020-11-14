package com.wwc2.main.media.driver;

import com.wwc2.corelib.model.custom.FourString;

/**
 * the media driver interface.
 *
 * @author wwc2
 * @date 2017/1/30
 */
public interface MediaDriverable {

    /**
     * 播放指定文件路径的媒体
     * @param filePath 媒体文件的全路径
     * @param startTime 媒体文件播放的开始时间
     * @return true 成功， false失败
     */
    boolean playFilePath(String filePath, int startTime);

    /**播放*/
    boolean play();

    /**暂停*/
    boolean pause();

    /**停止*/
    boolean stop();

    /**上一曲*/
    boolean prev();

    /**下一曲*/
    boolean next();

    /**快进*/
    boolean ff();

    /**快退*/
    boolean fb();

    /**切换到指定的播放模式，see {@link com.wwc2.media_interface.MediaDefine.PlayMode}*/
    boolean playMode(int mode);

    /**定位到媒体播放的时间*/
    boolean seek(int seek);

    /**设置播放器音量*/
    void setVolume(float left,float right);

    /**切换重复模式*/
    boolean changeRepeatMode();

    /**切换随机模式*/
    boolean changeRandomMode();

    //******************优化后新增代码2018-12-27 begin*******************//
    FourString[] getDeviceListInfo(int storageId);
    //******************优化后新增代码2018-12-27 end*******************//
}
