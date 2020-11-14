package com.wwc2.main.aux1;

import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.avin_interface.AvinInterface;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.avin.AvinLogic;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.SourceManager;

/**
 * the aux logic.
 *
 * @author wwc2
 * @date 2017/1/11
 */
public class AuxLogic extends AvinLogic {

    /**
     * TAG
     */
    private static final String TAG = "AuxLogic";

    @Override
    public String getTypeName() {
        return "AUX";
    }

    @Override
    public String getMessageType() {
        return AuxDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.aux1";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.aux1.MainActivity";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_AUX;
    }

    @Override
    public boolean isSource() {
        if (mRightCamera || mFrontCamera) {
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRightCamera || mFrontCamera) {
            return;
        }
        // 申请音频
        AudioDriver.Driver().request(null,
                AudioDefine.AudioStream.STREAM_MUSIC,
                AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause!");
        auxBefore = true;
        if (mRightCamera) {
            sendMcuRightCamera(false);
            mRightCamera = false;
            mRightCameraBefore = false;
        }
        if (mFrontCamera) {
            sendMcuFrontCamera(false);
            mFrontCamera = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        LogUtils.d(TAG, "onStop!");
        auxBefore = true;
        if (mRightCamera) {
            mRightCamera = false;
            mRightCameraBefore = false;
            sendMcuRightCamera(false);
            return;
        }
        if (mFrontCamera) {
            mFrontCamera = false;
            sendMcuFrontCamera(false);
        }
        // 释放音频
        AudioDriver.Driver().abandon();
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        LogUtils.d(TAG, "onKeyEvent----key=" + key);
        boolean ret = true;
        switch (key) {
            case Define.Key.KEY_RADAR_ONOFF:
                if (mRightCamera) {
                    mRightCamera = false;
                    auxBefore = true;
                } else {
                    mRightCamera = true;
                    auxBefore = false;
                    SourceManager.runApk(getAPKPacketName(), getAPKClassName(), null, false);
                }
                Packet packet1 = new Packet();
                packet1.putBoolean("rightCamera", mRightCamera);
                Notify(AvinInterface.MAIN_TO_APK.RIGHT_CAMERA, packet1);
                break;
//            case Define.Key.KEY_FRONT_CAMERA://只有在主页点击图标才会发此按键
//                if (mFrontCamera) {
//                    mFrontCamera = false;
//                    auxBefore = true;
//                } else {
//                    mFrontCamera = true;
//                    auxBefore = false;
//                    SourceManager.runApk(getAPKPacketName(), getAPKClassName(), null, false);
//                }
//                Packet packet2 = new Packet();
//                packet2.putBoolean("frontCamera", mFrontCamera);
//                Notify(AvinInterface.MAIN_TO_APK.FRONT_CAMERA, packet2);
//                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        LogUtils.d(TAG, "AuxLogic nId:" + nId);
        if (null == ret) {
            switch (nId) {
                case AvinInterface.APK_TO_MAIN.RIGHT_CAMERA:
                    if (packet != null) {
                        //MCU的右视
                        boolean rightCamera = packet.getBoolean("rightCamera");
                        boolean mcuFrom = packet.getBoolean("mcudata");
                        LogUtils.d("AuxLogic nId:FRONT_CAMERA  rightCamera=" + rightCamera + ", mRightCamera=" + mRightCamera +
                                ", backSource=" + SourceManager.getCurBackSource() + ", mRightCameraBefore=" + mRightCameraBefore +
                                ", camera=" + EventInputManager.getCamera() + ", mcuFrom=" + mcuFrom + ", mCameraBefore=" + mCameraBefore);
                        if (rightCamera) {
                            if (EventInputManager.getCamera() && mcuFrom) {//先倒车再打右转向，不作处理。
                                mCameraBefore = true;
                                return ret;
                            }

                            if (mRightCamera && mcuFrom) {//手动进入右视，再打右转向
                                mRightCameraBefore = true;
                            } else {
                                mRightCameraBefore = false;
                            }
                            mRightCamera = true;
                            auxBefore = false;
                            if (SourceManager.getCurBackSource() != Define.Source.SOURCE_AUX ||
                                    SourceManager.getCurSource() != Define.Source.SOURCE_AUX) {
                                SourceManager.runApk(getAPKPacketName(), getAPKClassName(), null, false);
                            }

                            Packet packet2 = new Packet();
                            packet2.putBoolean("rightCamera", mRightCamera);
                            Notify(AvinInterface.MAIN_TO_APK.RIGHT_CAMERA, packet2);
                        } else {
                            mCameraBefore = false;
                            mHandler.removeMessages(1);

                            if (!mRightCameraBefore || !mcuFrom) {
                                Packet packet2 = new Packet();
                                packet2.putBoolean("rightCamera", false);
                                Notify(AvinInterface.MAIN_TO_APK.RIGHT_CAMERA, packet2);

                                if (SourceManager.getCurBackSource() != Define.Source.SOURCE_AUX) {
                                    SourceManager.onExitPackage(getAPKPacketName());
                                }

                                mRightCamera = false;
                            }
                        }
                        sendMcuRightCamera(mRightCamera);
                    } else {
                        //Can的右视
                        mRightCamera = true;
                        auxBefore = false;
                        SourceManager.runApk(getAPKPacketName(), getAPKClassName(), null, false);
                    }
                    break;
                case AvinInterface.APK_TO_MAIN.FRONT_CAMERA:
                    if (packet != null) {
                        boolean frontCamera = packet.getBoolean("frontCamera");
                        sendMcuFrontCamera(frontCamera);
                        LogUtils.d("AuxLogic nId:FRONT_CAMERA  frontCamera=" + frontCamera + ", mFrontCamera=" + mFrontCamera +
                                ", backSource=" + SourceManager.getCurBackSource());
                        if (frontCamera) {
                            mFrontCamera = true;
                            auxBefore = false;
                            if (SourceManager.getCurBackSource() != Define.Source.SOURCE_AUX ||
                                    SourceManager.getCurSource() != Define.Source.SOURCE_AUX) {
                                SourceManager.runApk(getAPKPacketName(), getAPKClassName(), null, false);
                            } else {
                                auxBefore = true;
                            }

                            Packet packet2 = new Packet();
                            packet2.putBoolean("frontCamera", mFrontCamera);
                            Notify(AvinInterface.MAIN_TO_APK.FRONT_CAMERA, packet2);
                        } else {
                            auxBefore = true;
                            Packet packet2 = new Packet();
                            packet2.putBoolean("frontCamera", false);
                            Notify(AvinInterface.MAIN_TO_APK.FRONT_CAMERA, packet2);

                            if (SourceManager.getCurBackSource() != Define.Source.SOURCE_AUX) {
                                SourceManager.onExitPackage(getAPKPacketName());
                            }

                            mFrontCamera = false;
                        }
                    }
                    break;
            }
        }
        return ret;
    }

    private void sendMcuFrontCamera(boolean open) {
        LogUtils.d("sendMcuFrontCamera---open=" + open);
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_FRONT_CAMERA, new byte[]{(byte) (open ? 0x01 : 0x00)}, 1);
    }

    private void sendMcuRightCamera(boolean open) {
        LogUtils.d("sendMcuRightCamera---open=" + open);
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_RIGHT_CAMERA, new byte[]{(byte) (open ? 0x01 : 0x00)}, 1);
    }
}
