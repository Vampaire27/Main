package com.wwc2.main.driver.mcu.driver;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;

/**
 * the mcu adapter.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class McuAdapter {
    /**
     * 根据模式获取MCU协议值
     */
    public static final int getUIMode(int source) {
        int ret = 0xFF;
        switch (source) {
            case Define.Source.SOURCE_RADIO:
                ret = 0x00;
                break;
            case Define.Source.SOURCE_DVD:
                ret = 0x01;
                break;
            case Define.Source.SOURCE_AUDIO:
                ret = 0x02;
                break;
            case Define.Source.SOURCE_VIDEO:
                ret = 0x03;
                break;
            case Define.Source.SOURCE_IPOD:
                ret = 0x04;
                break;
            case Define.Source.SOURCE_BLUETOOTH:
                ret = 0x05;
                break;
            case Define.Source.SOURCE_NAVI:
                ret = 0x06;
                break;
            case Define.Source.SOURCE_TV:
                ret = 0x08;
                break;
            case Define.Source.SOURCE_AUX:
                ret = 0x09;
                break;
            case Define.Source.SOURCE_DVR:
                ret = 0x0B;
                break;
            case Define.Source.SOURCE_CAMERA:
                ret = 0x0C;
                break;
            case Define.Source.SOURCE_SETTINGS:
                ret = 0x0D;
                break;
            case Define.Source.SOURCE_LAUNCHER:
                ret = 0x0E;
                break;
            case Define.Source.SOURCE_POWEROFF:
                ret = 0x10;
                break;
            case Define.Source.SOURCE_PHONELINK:
                ret = 0x0A;
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * 根据模式获取MCU协议值
     */
    public static final int getMediaMode(int source) {
        int ret = 0xFF;
        switch (source) {
            case Define.Source.SOURCE_RADIO:
                ret = 0x00;
                break;
            case Define.Source.SOURCE_DVD:
                ret = 0x01;
                break;
            case Define.Source.SOURCE_AUDIO:
                ret = 0x02;
                break;
            case Define.Source.SOURCE_VIDEO:
                ret = 0x03;
                break;
            case Define.Source.SOURCE_IPOD:
                ret = 0x04;
                break;
            case Define.Source.SOURCE_BLUETOOTH:
                ret = 0x05;
                break;
            case Define.Source.SOURCE_TV:
                ret = 0x08;
                break;
            case Define.Source.SOURCE_AUX:
                ret = 0x09;
                break;
            case Define.Source.SOURCE_DVR:
                ret = 0x0B;
                break;
            case Define.Source.SOURCE_POWEROFF:
                ret = 0x0D;
                break;
            case Define.Source.SOURCE_SILENT:
                ret = 0xFD;
                break;
            case Define.Source.SOURCE_PHONELINK:
                ret = 0x0A;
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * 根据MCU模式获取ARM的模式值
     */
    public static final int getMcuMediaSource(int source) {
        int ret = Define.Source.SOURCE_NONE;
        switch (source) {
            case 0x00:
                ret = Define.Source.SOURCE_RADIO;
                break;
            case 0x01:
                ret = Define.Source.SOURCE_DVD;
                break;
            case 0x02:
                ret = Define.Source.SOURCE_AUDIO;
                break;
            case 0x03:
                ret = Define.Source.SOURCE_VIDEO;
                break;
            case 0x04:
                ret = Define.Source.SOURCE_IPOD;
                break;
            case 0x05:
                ret = Define.Source.SOURCE_BLUETOOTH;
                break;
            case 0x08:
                ret = Define.Source.SOURCE_TV;
                break;
            case 0x09:
                ret = Define.Source.SOURCE_AUX;
                break;
            case 0x0A:
                ret = Define.Source.SOURCE_BLUETOOTH;
                break;
            case 0x0B:
                ret = Define.Source.SOURCE_DVR;
                break;
            case 0x0D:
                ret = Define.Source.SOURCE_POWEROFF;
                break;
            case 0xFD:
                ret = Define.Source.SOURCE_SILENT;
                break;
            default:
                break;
        }
        return ret;
    }

    /**
     * 发送MCU蓝牙通话状态
     */
    public static final int getHFPStatus(int status) {
        int ret = 0;
        if (BluetoothDefine.HFPStatus.isCalling(status)) {
            ret = 3;
        } else if (!BluetoothDefine.HFPStatus.isDisConnect(status)) {
            ret = 1;
        }
        return ret;
    }

    /**
     * 根据MCU声音类型，获取当前声音类型
     */
    public static final int getVolumeType(int mcu) {
        int ret = Define.VolumeType.DEFAULT;
        switch (mcu) {
            case 0:
                ret = Define.VolumeType.DEFAULT;
                break;
            case 1:
                ret = Define.VolumeType.BT_HFP;
                break;
            case 2:
                ret = Define.VolumeType.GPS;
            default:
                break;
        }
        return ret;
    }

    /**
     * 根据声音类型，获取MCU声音类型
     */
    public static final int getMCUVolumeType(int type) {
        int ret = 0;
        switch (type) {
            case Define.VolumeType.DEFAULT:
                ret = 0;
                break;
            case Define.VolumeType.BT_HFP:
                ret = 1;
                break;
            case Define.VolumeType.GPS:
                ret = 2;
            default:
                break;
        }
        return ret;
    }

    /**
     * 根据MCU按键转换为定义的按键
     */
    public static final int getKey(int key) {
        int ret = Define.Key.KEY_NONE;
        switch (key) {
            case 0x00:
                ret = Define.Key.KEY_NUM_0;
                break;
            case 0x01:
                ret = Define.Key.KEY_NUM_1;
                break;
            case 0x02:
                ret = Define.Key.KEY_NUM_2;
                break;
            case 0x03:
                ret = Define.Key.KEY_NUM_3;
                break;
            case 0x04:
                ret = Define.Key.KEY_NUM_4;
                break;
            case 0x05:
                ret = Define.Key.KEY_NUM_5;
                break;
            case 0x06:
                ret = Define.Key.KEY_NUM_6;
                break;
            case 0x07:
                ret = Define.Key.KEY_NUM_7;
                break;
            case 0x08:
                ret = Define.Key.KEY_NUM_8;
                break;
            case 0x09:
                ret = Define.Key.KEY_NUM_9;
                break;
            case 0x0a:
                ret = Define.Key.KEY_NUM_ADD;
                break;
            case 0x0b:
                ret = Define.Key.KEY_CONTROL_UP;
                break;
            case 0x0c:
                ret = Define.Key.KEY_CONTROL_DOWN;
                break;
            case 0x0d:
                ret = Define.Key.KEY_CONTROL_LEFT;
                break;
            case 0x0e:
                ret = Define.Key.KEY_CONTROL_RIGHT;
                break;
            case 0x0f:
                ret = Define.Key.KEY_CONTROL_ENTER;
                break;
            case 0x10:
                ret = Define.Key.KEY_PLAYPAUSE;
                break;
            case 0x11:
                ret = Define.Key.KEY_STOP;
                break;
            case 0x12:
                ret = Define.Key.KEY_NEXT;
                break;
            case 0x13:
                ret = Define.Key.KEY_PREV;
                break;
            case 0x14:
                ret = Define.Key.KEY_REPEAT;
                break;
            case 0x15:
                ret = Define.Key.KEY_RAND;
                break;
            case 0x16:
                ret = Define.Key.KEY_FF;
                break;
            case 0x17:
                ret = Define.Key.KEY_FB;
                break;
            case 0x18:
                ret = Define.Key.KEY_SLOW_FF;
                break;
            case 0x19:
                ret = Define.Key.KEY_SLOW_FB;
                break;
            case 0x1a:
                ret = Define.Key.KEY_MENU;
                break;
            case 0x1b:
                ret = Define.Key.KEY_ANDROID;
                break;
            case 0x1c:
                ret = Define.Key.KEY_AUDIO;
                break;
            case 0x1d:
                ret = Define.Key.KEY_SUB;
                break;
            case 0x1e:
                ret = Define.Key.KEY_ANGLE;
                break;
            case 0x1f:
                ret = Define.Key.KEY_OSD;
                break;
            case 0x20:
                ret = Define.Key.KEY_PBC;
                break;
            case 0x21:
                ret = Define.Key.KEY_PROGRAM;
                break;
            case 0x22:
                ret = Define.Key.KEY_GOTO;
                break;
            case 0x23:
                ret = Define.Key.KEY_PP;
                break;
            case 0x24:
                ret = Define.Key.KEY_ZOOM;
                break;
            case 0x25:
                ret = Define.Key.KEY_SETTINGS;
                break;
            case 0x26:
                ret = Define.Key.KEY_PN;
                break;
            case 0x27:
                ret = Define.Key.KEY_ENTER;
                break;
            case 0x28:
                ret = Define.Key.KEY_AMS;
                break;
            case 0x29:
                ret = Define.Key.KEY_CH_INC;
                break;
            case 0x2a:
                ret = Define.Key.KEY_CH_DEC;
                break;
            case 0x2b:
                ret = Define.Key.KEY_LIST;
                break;
            case 0x2c:
                ret = Define.Key.KEY_BACK;
                break;
            case 0x30:
                ret = Define.Key.KEY_HOME;
                break;
            case 0x31:
                ret = Define.Key.KEY_VOL_MUTE;
                break;
            case 0x32:
                ret = Define.Key.KEY_VOL_INC;
                break;
            case 0x33:
                ret = Define.Key.KEY_VOL_DEC;
                break;
            case 0x34:
                ret = Define.Key.KEY_EJECT;
                break;
            case 0x35:
                ret = Define.Key.KEY_EQ;
                break;
            case 0x36:
                ret = Define.Key.KEY_VIDEO_SETTINGS;
                break;
            case 0x37:
                ret = Define.Key.KEY_NAVI;
                break;
            case 0x38:
                ret = Define.Key.KEY_DVD;
                break;
            case 0x39:
                ret = Define.Key.KEY_RADIO;
                break;
            case 0x3a:
                ret = Define.Key.KEY_PICKUP;
                break;
            case 0x3b:
                ret = Define.Key.KEY_HANGUP;
                break;
            case 0x3c:
                ret = Define.Key.KEY_MODE;
                break;
            case 0x3d:
                ret = Define.Key.KEY_USB;
                break;
            case 0x3e:
                ret = Define.Key.KEY_CARD;
                break;
            case 0x3f:
                ret = Define.Key.KEY_AUX;
                break;
            case 0x40:
                ret = Define.Key.KEY_BLUETOOTH_MULTI_FUNCTION;
                break;
            case 0x41:
                ret = Define.Key.KEY_DIM;
                break;
            case 0x42:
                ret = Define.Key.KEY_MEDIA;
                break;
            case 0x43:
                ret = Define.Key.KEY_TV;
                break;
            case 0x44:
                ret = Define.Key.KEY_CAN_SETTINGS;
                break;
            case 0x45:
                ret = Define.Key.KEY_LOUD;
                break;
            case 0x46:
                ret = Define.Key.KEY_CALIBRATE;
                break;
            case 0x47:
                ret = Define.Key.KEY_SETTINGS;
                break;
            case 0x48:
                ret = Define.Key.KEY_STANDBY;
                break;
            case 0x49:
                ret = Define.Key.KEY_TIME_SETTINGS;
                break;
            case 0x50:
                ret = Define.Key.KEY_SOUND;
                break;
            case 0x51:
                ret = Define.Key.KEY_SHORT_POWEROFF;
                break;
            case 0x52:
                ret = Define.Key.KEY_LONG_POWEROFF;
                break;
            case 0x55:
                ret = Define.Key.KEY_POWER;
                break;
            case 0x56:
                ret = Define.Key.KEY_LONG_POWEROFF;
                break;
            case 0x57:
                ret = Define.Key.KEY_BLUETOOTH_MUSIC;
                break;
            case 0x58:
                ret = Define.Key.KEY_TA;
                break;
            case 0x59:
                ret = Define.Key.KEY_AF;
                break;
            case 0x5A:
                ret = Define.Key.KEY_PTY;
                break;
            case 0x5B:
                ret = Define.Key.KEY_LOC;
                break;
            case 0x5C:
                ret = Define.Key.KEY_ST;
                break;
            case 0x5D:
                ret = Define.Key.KEY_LOUD;
                break;
            case 0x60:
                ret = Define.Key.KEY_AUDIO_RPT;
                break;
            case 0x61:
                ret = Define.Key.KEY_AUDIO_RDM;
                break;
            case 0x62://旋钮_左旋
                ret = Define.Key.KEY_CONTROL_LEFT;
                break;
            case 0x63://0x61://旋钮_右旋 //电动摩拖车项目，与威益德有冲突改为0x63，后续需MCU同步改2020-08-19
                ret = Define.Key.KEY_CONTROL_RIGHT;
                break;
            case 0x68:
                ret = Define.Key.KEY_BT_DISCONNECT;
                break;
            case 0x71://按键按下
                break;
            case 0x72://按键弹起
                ret = Define.Key.KEY_CONTROL_ENTER;
                break;
            case 0x73://按键长按
                ret = Define.Key.KEY_BACK;
                break;
            default:
                break;
        }
        return ret;
    }
}
