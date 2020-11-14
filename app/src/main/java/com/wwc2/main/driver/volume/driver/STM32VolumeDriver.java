package com.wwc2.main.driver.volume.driver;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.main.bluetooth.EcarHelper;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.audio.AudioListener;
import com.wwc2.main.driver.mcu.driver.McuAdapter;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.mainui_interface.MainUIInterface;
import com.wwc2.navi_interface.NaviDefine;

import java.util.HashMap;
import java.util.Map;

/**
 * the stm32 volume driver.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class STM32VolumeDriver extends BaseVolumeDriver {

    /**TAG*/
    private static final String TAG = "STM32VolumeDriver";

    /**the mcu volume map.*/
    private Map<Integer, Integer> mMCUVolumeMap = new HashMap<>();

    /**the mcu delay set volume timer queue.*/
    private TimerQueue mMCUVolumeTimerQueue = new TimerQueue();

    /**the MCU data listener.*/
    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            // 请求声音数据
            McuManager.sendMcu((byte)McuDefine.ARM_TO_MCU.REQ_Requst_Vol, new byte[]{0x00}, 1);
            McuManager.sendMcu((byte)McuDefine.ARM_TO_MCU.REQ_Requst_Vol, new byte[]{0x01}, 1);
           // McuManager.sendMcu((byte)McuDefine.ARM_TO_MCU.REQ_Requst_Vol, new byte[]{0x02}, 1);
        }

        @Override
        public void DataListener(byte[] val) {
            if (null != val) {
                final int length = val.length;
                if (length > 1) {
                    byte cmd = val[0];
                    if ((byte) McuDefine.MCU_TO_ARM.MRPT_MuteSt == cmd) {
                        final boolean mute = (1 == val[1]);
                        LogUtils.d(TAG, "Mcu mute, mute = " + mute);
                        Model().getVolumeMute().setVal(mute);
                    } else if ((byte) McuDefine.MCU_TO_ARM.MACK_SysVol == cmd) {
                        final int type = McuAdapter.getVolumeType(val[1]);
                        if (length > 2) {
                            final int value = val[2]&0xFF;
                            LogUtils.d(TAG, "Mcu volume, type = " + Define.VolumeType.toString(type) + ", value = " + value);
                            //MCU只在开机时从此地址发送音量，正常开机状态下音量由AP自己处理。
//                            Model().getVolumeValue().setVal(type, value);
                            updateVolume(type, value);

//                            if (null != mMCUVolumeMap) {
//                                mMCUVolumeMap.put(type, value);
//                            }
//                            if (null != mMCUVolumeTimerQueue) {
//                                mMCUVolumeTimerQueue.stop();
//                                mMCUVolumeTimerQueue.add(5/*2000*/, null, new BaseCallback() {//MCU只在开机时从此地址发送音量
//                                    @Override
//                                    public void onCallback(int nId, Packet packet) {
//                                        if (null != mMCUVolumeMap) {
//                                            for (Map.Entry<Integer, Integer> entry : mMCUVolumeMap.entrySet()) {
//                                                final int type = entry.getKey();
//                                                final int value = entry.getValue();
//                                                Model().getVolumeValue().setVal(type, value);
//                                            }
//                                        }
//                                    }
//                                });
//                                mMCUVolumeTimerQueue.start();
//                            }
                        }
                    }
                }
            }
        }
    };

    /**the bluetooth listener.*/
    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            if (!BluetoothDefine.HFPStatus.isCalling(oldVal) && BluetoothDefine.HFPStatus.isCalling(newVal)) {
                // calling
                setVolumeType(Define.VolumeType.BT_HFP);
            } else if (BluetoothDefine.HFPStatus.isCalling(oldVal) && !BluetoothDefine.HFPStatus.isCalling(newVal)) {
                // hangup
                setVolumeType(Define.VolumeType.DEFAULT);
            }
        }
    };

    /**the gps audio out listener.*/
    private AudioListener mAudioListener = new AudioListener() {
        @Override
        public void NaviAudioActiveListener(Boolean oldVal, Boolean newVal) {
            //不区分导航的音量。
//            if (newVal) {
//                setVolumeType(Define.VolumeType.GPS);
//            } else {
//                setVolumeType(Define.VolumeType.DEFAULT);
//            }
        }
    };

    /**the source listener.*/
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            if (Define.Source.SOURCE_NAVI == newVal) {
                setVolumeType(Define.VolumeType.GPS);
            } else if (Define.Source.SOURCE_NAVI == oldVal){
                setVolumeType(Define.VolumeType.DEFAULT);
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        McuManager.getModel().bindListener(mMcuListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);
//        DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel().bindListener(mAudioListener);
        SourceManager.getModel().bindListener(mSourceListener);
    }

    @Override
    public void onDestroy() {
        SourceManager.getModel().unbindListener(mSourceListener);
//        DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel().unbindListener(mAudioListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);
        McuManager.getModel().unbindListener(mMcuListener);

        super.onDestroy();
    }

    @Override
    public boolean mute(boolean mute) {
        boolean ret = super.mute(mute);
        final int type = Model().getVolumeType().getVal();
        if (ret) {
            byte[] data = new byte[1];
            if (mute) {
                data[0] = 1;
            } else {
                data[0] = 0;
            }
            McuManager.sendMcu((byte)McuDefine.ARM_TO_MCU.REQ_SysMute, data, 1);
            Model().getVolumeMute().setVal(mute);

            LogUtils.d(TAG, "ARM(send MCU) mute, type = " + Define.VolumeType.toString(type) + ", mute = " + mute);
        } else {
            LogUtils.e(TAG, "ARM(send MCU) mute failed, type = " + Define.VolumeType.toString(type));
        }
        return ret;
    }

    @Override
    public boolean set(int value) {
        boolean ret = super.set(value);
        final int type = Model().getVolumeType().getVal();

        if (ret) {
            byte[] data = new byte[2];
            data[0] = (byte)McuAdapter.getMCUVolumeType(type);
            data[1] = (byte) value;
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.REQ_Change_Vol, data, 2);

            // 使用MCU反馈音量
//            Model().getVolumeValue().setVal(type, value);
            updateVolume(type, value);

            LogUtils.d(TAG, "ARM(send MCU) set volume, type = " + Define.VolumeType.toString(type) + ", value = " + value);
        } else {
            final int max = Model().getVolumeMax().getVal();
            LogUtils.e(TAG, "ARM(send MCU) set volume failed, type = " + Define.VolumeType.toString(type) + ", value = " + value + ", max = " + max);
        }
        return ret;
    }

    private synchronized void updateVolume(int type, int volume) {
        //解决蓝牙通话音量问题bug14063，优化调节音量处理。2018-12-07
        LogUtils.e(TAG, "updateVolume type" + type + ", volume=" + volume);
        Model().getVolumeValue().setVal(type, volume);
    }

    @Override
    public boolean increase(int value) {
        boolean ret = super.increase(value);
        return ret;
    }

    @Override
    public boolean decrease(int value) {
        boolean ret = super.decrease(value);
        return ret;
    }

    @Override
    public boolean operate(int operate) {
        boolean ret = super.operate(operate);
        if (ret) {
            Packet packet = new Packet();
            packet.putInt("operate", operate);
            BaseLogic logic = ModuleManager.getLogicByName(MainUIDefine.MODULE);
            if (null != logic) {
                logic.dispatch(MainUIInterface.APK_TO_MAIN.OPERATE_VOLUME, packet);
            }
        }
        return ret;
    }

    /**设置声音类型*/
    @Override
    public void setVolumeType(int type) {
        final boolean calling = BluetoothDefine.HFPStatus.isCalling(ModuleManager.getLogicByName(BluetoothDefine.MODULE).getInfo().getInt("HFPStatus"));
        final boolean navi_page = (Define.Source.SOURCE_NAVI == SourceManager.getCurSource());
        final boolean navi_active = ModuleManager.getLogicByName(NaviDefine.MODULE).getInfo().getBoolean("NaviAudioActive");
        // 通话优先
        if (calling || EcarHelper.getEcarState()) {
            Model().getVolumeType().setVal(Define.VolumeType.BT_HFP);
        }
        /*
        // masked by Denny on 2016-06-14 去掉导航音量调节
        else if (navi_page || navi_active) {
            Model().getVolumeType().setVal(Define.VolumeType.GPS);
        } else {
            Model().getVolumeType().setVal(type);
        }
        */
        else {
            Model().getVolumeType().setVal(Define.VolumeType.DEFAULT);
        }
    }

    /**get mute state*/
    @Override
    public boolean getMuteState() {
        return Model().getVolumeMute().getVal();
    }



}
