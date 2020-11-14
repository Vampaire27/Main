package com.wwc2.main.silent;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.audio.driver.BaseAudioDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.settings.util.ClickFilter;

/**
 * the silent logic.
 *
 * @author wwc2
 * @date 2017/1/21
*/
public class SilentLogic extends BaseLogic {

    
    @Override
    public String getTypeName() {
        return "Silent";
    }

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_SILENT;
    }

    @Override
    public boolean runApk() {
        return true;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.launcher";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.launcher.ui.MainActivity";
    }


    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;
        LogUtils.d("key:" + Define.Key.toString(key));
        if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
            LogUtils.d("media logic onKeyEvent diabale for acc off mode.");
            return ret;
        }
        switch (key) {
            case Define.Key.KEY_PREV:
            case Define.Key.KEY_CH_DEC:
            case Define.Key.KEY_FB:
            case Define.Key.KEY_SCAN_DEC:
            case Define.Key.KEY_DIRECT_LEFT:
                if (!ClickFilter.filter(300L)) {
                    sendMediaButtonEvent(getMainContext(), KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                }
                break;
            case Define.Key.KEY_NEXT:
            case Define.Key.KEY_CH_INC:
            case Define.Key.KEY_FF:
            case Define.Key.KEY_SCAN_INC:
            case Define.Key.KEY_DIRECT_RIGHT:
                if (!ClickFilter.filter(300L)) {
                    sendMediaButtonEvent(getMainContext(),KeyEvent.KEYCODE_MEDIA_NEXT);
                }
                break;
            case Define.Key.KEY_PAUSE:
                BaseAudioDriver.pauseByVoice(true);
                sendMediaButtonEvent(getMainContext(),KeyEvent.KEYCODE_MEDIA_PAUSE);
                break;
            case Define.Key.KEY_PLAY:
                sendMediaButtonEvent(getMainContext(),KeyEvent.KEYCODE_MEDIA_PLAY);
                break;
            case Define.Key.KEY_PLAYPAUSE:
                if (ClickFilter.filter(300L)) {
                    return false;
                }
                sendMediaButtonEvent(getMainContext(),KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

                break;
            case Define.Key.KEY_STOP:
                sendMediaButtonEvent(getMainContext(),KeyEvent.KEYCODE_MEDIA_STOP);
                break;
            default:
                ret = false;
                break;
        }
        // }
        return ret;
    }

    private void sendMediaButtonEvent(final Context mContext , final int keycodeMedia) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                //KeyEvent(action,keycode)
                KeyEvent _KeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycodeMedia);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, _KeyEvent);
                intent.setFlags(0x01000000);
                mContext.sendBroadcast(intent);

                //模拟按键触发
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                _KeyEvent = new KeyEvent(KeyEvent.ACTION_UP, keycodeMedia);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, _KeyEvent);
                intent.setFlags(0x01000000);
                mContext.sendBroadcast(intent);
            }
        }).start();

    }
}
