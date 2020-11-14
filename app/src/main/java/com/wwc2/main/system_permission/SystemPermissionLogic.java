package com.wwc2.main.system_permission;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.message.MessageDefine;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.language.LanguageDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

/**
 * the system permission logic.
 *
 * @author wwc2
 * @date 2017/1/18
 */
public class SystemPermissionLogic extends BaseLogic {

    /**TAG*/
    public static final String TAG = "SystemPermissionLogic";

    /**create*/
    private boolean mCreateOver = false;

    /**destroy*/
    private boolean mDestroyOver = false;

    @Override
    public String getTypeName() {
        return "SystemPermission";
    }

    @Override
    public String getMessageType() {
        return SystemPermissionDefine.MODULE;
    }

    @Override
    public boolean isCreateOver() {
        return mCreateOver;
    }

    @Override
    public boolean isDestroyOver() {
        return mDestroyOver;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        mCreateOver = true;//android go已将SystemPermission合并到了MainUI中。
//        ApkUtils.stopServiceSafety(getMainContext(), com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_NAME,
//                com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
//                com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
//        ApkUtils.startServiceSafety(getMainContext(), com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_NAME,
//                com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
//                com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
    }

    @Override
    public void onDestroy() {
//        ApkUtils.stopServiceSafety(getMainContext(), com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_NAME,
//                com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
//                com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);

        super.onDestroy();
    }

    @Override
    public Packet Notify(int nId, Packet packet) {
        switch (nId) {
            case SystemPermissionInterface.MAIN_TO_APK.TIME_SET:
            case SystemPermissionInterface.MAIN_TO_APK.TIME_FORMAT:
            case SystemPermissionInterface.MAIN_TO_APK.SIMULATE_KEY:
                break;
            default:
//                ApkUtils.startServiceSafety(getMainContext(), com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_NAME,
//                        com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME,
//                        com.wwc2.systempermission_interface.SystemPermissionDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
                break;
        }
        return super.Notify(nId, packet);
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        switch (nId) {
            case MessageDefine.APK_TO_MAIN_ID_CREATE:
                mCreateOver = true;
                LogUtils.d(TAG, "System permission service connect create...");
                break;
            case MessageDefine.APK_TO_MAIN_ID_DESTROY:
                mDestroyOver = true;
                LogUtils.d(TAG, "System permission service connect destroy...");
                break;
            case SystemPermissionInterface.APK_TO_MAIN.LOCALE:
                if (packet != null) {
                    String language = packet.getString(com.wwc2.systempermission_interface.SystemPermissionDefine.ParseKey.LOCALE);
                    if (null != language) {
                        LanguageDriver.Driver().set(language);
                    }
                }
                break;

            case SystemPermissionInterface.APK_TO_MAIN.FACTORY_MODULES_INIT:
                if (packet != null) {
                    for (Define.Factory.Modules modules : Define.Factory.Modules.values()) {
                        if (modules != null) {
                            FactoryDriver.Driver().setModuleEnable(modules.value(), packet.getBoolean(modules.value(), false));
                        }
                    }
                }
                break;

            case SystemPermissionInterface.APK_TO_MAIN.FACTORY_MODULES:
//                if (packet != null) {
//                    CoreLogic logic = LogicManager.getLogicByName(SettingsDefine.MODULE);
//                    if (logic != null) {
//                        logic.Notify(false, SettingsInterface.MAIN_TO_APK.FACTORY_MODULES, packet);
//                    }
//                }
                break;

            default:
                break;
        }
        return super.dispatch(nId, packet);
    }
}
