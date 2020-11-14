package com.wwc2.main.launcher;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.launcher_interface.LauncherDefine;
import com.wwc2.launcher_interface.LauncherInterface;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.eventinput.EventInputListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * the launcher logic.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public class LauncherLogic extends BaseLogic {

    /**
     * TAG
     */
    private static final String TAG = "LauncherLogic";

    @Override
    public String getTypeName() {
        return "Launcher";
    }

    @Override
    public String getMessageType() {
        return LauncherDefine.MODULE;
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
    public int source() {
        return Define.Source.SOURCE_LAUNCHER;
    }

    @Override
    public List<String> getAPKPacketList() {
        // 主服务需要挂起模块的白名单
        List<String> list = new ArrayList<String>();
        list.add("com.android.launcher");
        list.add("com.android.launcher3");
        return list;
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        switch (nId) {
            case LauncherInterface.APK_TO_MAIN.OPEN_THIRD_APPS:
                Notify(false, LauncherInterface.MAIN_TO_APK.OPEN_THIRD_APPS, null);
                break;
            default:
                break;
        }
        return super.dispatch(nId, packet);
    }

    /**
     * 事件输入监听器
     */
    private EventInputListener mEventInputListener = new EventInputListener() {
        @Override
        public void BrakeListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("Brake", newVal);
            Notify(LauncherInterface.MAIN_TO_APK.BRAKE_STATE, packet);
            LogUtils.d(TAG, "EventInputListener isBrake:" + newVal);
        }

        @Override
        public void IllListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("IllState",newVal);
            Notify(LauncherInterface.MAIN_TO_APK.ILL_STATE,packet);
            LogUtils.d(TAG, "EventInputListener illState:" + newVal);

        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().bindListener(mEventInputListener);
    }


    @Override
    public Packet getInfo() {
        Packet ret = super.getInfo();
        if (null == ret) {
            ret = new Packet();
        }
        ret.putBoolean("Brake", EventInputManager.getBrake());
        ret.putBoolean("IllState",EventInputManager.getIll());
        return ret;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;
        switch (key) {
            case Define.Key.KEY_PAGE_NEXT:
                Notify(false, LauncherInterface.MAIN_TO_APK.PAGE_NEXT, null);
                break;
            case Define.Key.KEY_PAGE_PREV:
                Notify(false, LauncherInterface.MAIN_TO_APK.PAGE_PREV, null);
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }
}
