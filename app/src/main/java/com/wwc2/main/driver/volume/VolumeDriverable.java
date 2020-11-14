package com.wwc2.main.driver.volume;

/**
 * the volume driver interface.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public interface VolumeDriverable {

    /**mute the volume.*/
    boolean mute(boolean mute);

    /**set the volume value.*/
    boolean set(int value);

    /**increase the volume value.*/
    boolean increase(int value);

    /**decrease the volume value.*/
    boolean decrease(int value);

    /**operate the volume panel.*/
    boolean operate(int operate);

    /**adjust show status.*/
    boolean adjustShow(boolean show);

    /**for other model to get Mute state.*/
    boolean getMuteState();

    /**for other model to get Volume Value.*/
    int getVolumeValue();

    void setVolumeType(int type);

}
