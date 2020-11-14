package com.wwc2.main.tv;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.main.avin.AvinLogic;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.tv.driver.STM32TVDriver;
import com.wwc2.main.tv.driver.TVDriverable;
import com.wwc2.tv_interface.TVDefine;
import com.wwc2.tv_interface.TVInterface;

/**
 * the tv logic.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public class TVLogic extends AvinLogic {

    @Override
    public String getTypeName() {
        return "TV";
    }

    @Override
    public String getMessageType() {
        return TVDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.tv";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_TV;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public BaseDriver newDriver() {
        return new STM32TVDriver();
    }

    /**
     * the driver interface.
     */
    protected TVDriverable Driver() {
        TVDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof TVDriverable) {
            ret = (TVDriverable) driver;
        }
        return ret;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 申请音频
        AudioDriver.Driver().request(null,
                AudioDefine.AudioStream.STREAM_MUSIC,
                AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        // 释放音频
        AudioDriver.Driver().abandon();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        switch (nId) {
            case TVInterface.APK_TO_MAIN.POST_XY:
                if (null != packet) {
                    final float x = packet.getFloat("x");
                    final float y = packet.getFloat("y");
                    Driver().postXY(x, y);
                }
                break;
            case TVInterface.APK_TO_MAIN.POWER:
                Driver().power();
                break;
            case TVInterface.APK_TO_MAIN.MENU:
                Driver().menu();
                break;
            case TVInterface.APK_TO_MAIN.EXIT:
                Driver().exit();
                break;
            case TVInterface.APK_TO_MAIN.UP:
                Driver().up();
                break;
            case TVInterface.APK_TO_MAIN.DOWN:
                Driver().down();
                break;
            case TVInterface.APK_TO_MAIN.LEFT:
                Driver().left();
                break;
            case TVInterface.APK_TO_MAIN.RIGHT:
                Driver().right();
                break;
            case TVInterface.APK_TO_MAIN.OK:
                Driver().ok();
                break;
            case TVInterface.APK_TO_MAIN.SCAN:
                Driver().scan();
                break;
            case TVInterface.APK_TO_MAIN.PVR:
                Driver().pvr();
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;
        switch (key) {
            case Define.Key.KEY_CONTROL_UP:
                dispatch(TVInterface.APK_TO_MAIN.UP, null);
                break;
            case Define.Key.KEY_CONTROL_DOWN:
                dispatch(TVInterface.APK_TO_MAIN.DOWN, null);
                break;
            case Define.Key.KEY_CONTROL_LEFT:
                dispatch(TVInterface.APK_TO_MAIN.LEFT, null);
                break;
            case Define.Key.KEY_CONTROL_RIGHT:
                dispatch(TVInterface.APK_TO_MAIN.RIGHT, null);
                break;
            case Define.Key.KEY_ENTER:
                dispatch(TVInterface.APK_TO_MAIN.OK, null);
                break;
            case Define.Key.KEY_SCAN:
                dispatch(TVInterface.APK_TO_MAIN.SCAN, null);
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }
}
