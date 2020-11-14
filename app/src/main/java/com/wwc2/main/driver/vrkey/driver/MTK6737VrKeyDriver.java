package com.wwc2.main.driver.vrkey.driver;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.jni.simulate_key.SimulateKeyNative;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

/**
 * the mtk6737 vr key driver.
 *
 * @author wwc2
 * @date 2017/1/21
 */
public class MTK6737VrKeyDriver extends BaseVrKeyDriver {

    private static final String TAG = "MTK6737VrKeyDriver";

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean simulate(int key) {
        int code = 0;
        Packet packet = null;
        switch (key) {
            case Define.Key.KEY_HOME:
                code = SimulateKeyNative.VR_KEY_CODE_HOME;
                break;
            case Define.Key.KEY_MENU:
                code = SimulateKeyNative.VR_KEY_CODE_MENU;
                break;
            case Define.Key.KEY_BACK:
                code = SimulateKeyNative.VR_KEY_CODE_BACK;
                break;
            case Define.Key.KEY_ANDROID_RECENT:
                packet = new Packet();
                packet.putInt("key", key);
                BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                if (null != logic) {
                    logic.Notify(SystemPermissionInterface.MAIN_TO_APK.SIMULATE_KEY, packet);
                }
                break;
            case Define.Key.KEY_NEXT:
                code = SimulateKeyNative.VR_KEY_CODE_NEXT;
                break;
            case Define.Key.KEY_PREV:
                code = SimulateKeyNative.VR_KEY_CODE_PREV;
                break;
            case Define.Key.KEY_PLAYPAUSE:
                code = SimulateKeyNative.VR_KEY_CODE_PLAY_PAUSE;
                break;
            default:
                break;
        }
        if (0 != code) {
            SimulateKeyNative.SIMULATE_key(code);
            return true;
        }
        return false;
    }
}
