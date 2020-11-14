package com.wwc2.main.manager;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.main.driver.volume.VolumeDriver;

/**
 * the volume manager.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class VolumeManager {

    /**
     * the volume driver.
     */
    private static Driver mVolumeDriver = null;

    /**
     * volume manager create method.
     */
    public static void onCreate(Packet packet) {
        mVolumeDriver = DriverManager.getDriverByName(VolumeDriver.DRIVER_NAME);
    }

    /**
     * volume manager destroy method.
     */
    public static void onDestroy() {

    }

    /**
     * get the volume type.
     */
    public static int getType() {
        int ret = Define.VolumeType.DEFAULT;
        if (null != mVolumeDriver) {
            Packet packet = mVolumeDriver.getInfo();
            if (null != packet) {
                ret = packet.getInt("VolumeType");
            }
        }
        return ret;
    }

    /**
     * get the volume mute.
     */
    public static boolean getMute() {
        boolean ret = false;
        if (null != mVolumeDriver) {
            Packet packet = mVolumeDriver.getInfo();
            if (null != packet) {
                ret = packet.getBoolean("VolumeMute");
            }
        }
        return ret;
    }

    /**
     * get the volume value.
     */
    public static int getValue() {
        final int type = getType();
        int ret = getValue(type);
        return ret;
    }

    /**
     * get the volume type.
     */
    public static int getValue(int type) {
        int ret = Define.VOLUME_DEFAULT_VALUE;
        if (null != mVolumeDriver) {
            Packet packet = mVolumeDriver.getInfo();
            if (null != packet) {
                Integer[] values = packet.getIntObjectArray("VolumeValue");
                if (null != values) {
                    final int length = values.length;
                    if (type >= 0 && type < length) {
                        ret = values[type];
                    }
                }
            }
        }
        return ret;
    }

    /**
     * get the volume max.
     */
    public static int getMax() {
        int ret = Define.VOLUME_MAX_VALUE;
        if (null != mVolumeDriver) {
            Packet packet = mVolumeDriver.getInfo();
            if (null != packet) {
                ret = packet.getInt("VolumeMax");
            }
        }
        return ret;
    }

    /**
     * get the volume show.
     */
    public static boolean getShow() {
        boolean ret = false;
        if (null != mVolumeDriver) {
            Packet packet = mVolumeDriver.getInfo();
            if (null != packet) {
                ret = packet.getBoolean("VolumeShow");
            }
        }
        return ret;
    }
}
