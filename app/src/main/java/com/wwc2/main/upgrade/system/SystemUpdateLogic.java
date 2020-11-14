package com.wwc2.main.upgrade.system;


import android.os.SystemProperties;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.settings.util.FileUtil;
import com.wwc2.main.upgrade.OnlineUpgrade;
import com.wwc2.main.upgrade.system.driver.MTK6737SystemDriver;
import com.wwc2.main.upgrade.system.driver.SystemDriverable;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.system_interface.SystemDefine;
import com.wwc2.system_interface.SystemInterface;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

import java.io.IOException;

/**
 * the phone link logic.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public class SystemUpdateLogic extends BaseLogic {

    private static final String TAG = "SystemUpdateLogic";

    private  final String UPDATE_COPY= "/storage/sdcard0/wwc2_update.zip";

    /**
     * online upgrade.
     */
    private OnlineUpgrade mOnlineUpgrade = new OnlineUpgrade(new OnlineUpgrade.OnlineUpgradeListener() {
        @Override
        public void UpgradeStatusChange(int status) {
            Packet packet = new Packet();
            packet.putInt("UpgradeStatus", status);
            Notify(SystemInterface.MainToApk.UPGRADE_STATUS, packet);
        }

        /**
         * http://www.icar001.com:5200/?client=MT1000&name=OS&hardware_version=Unknown
         *
         * {"code":0,"msg":"\u8fd4\u56de\u6210\u529f","data":[{
         * "customername":"MT1000","appname":"OS","version":"1","hardware_version":"",
         * "appfile_url":"http:\/\/www.icar001.com\/public\/os\/update-APP_V0.0.56-CHEJI01_YLK_V0.0.47_20160926.zip",
         * "subpackage_appfile_url":"http:\/\/www.icar001.com\/public\/os\/ota-APP_V0.0.56-CHEJI01_YLK_V0.0.47_20160926.zip"}]}
         *
         */

        @Override
        public void UpgradeRun(String address) {
            Packet packet = new Packet();
            if (!TextUtils.isEmpty(address)) {
                packet.putString("address", address);
            }
            ApkUtils.runApk(getMainContext(), getAPKPacketName(), packet, true);
        }
    });

    @Override
    public String getTypeName() {
        return "SystemUpdate";
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.systemupdate_apk";
    }

    @Override
    public String getMessageType() {
        return SystemDefine.MODULE;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_SYSTEMUPDATE;
    }

    @Override
    public BaseDriver newDriver() {
        return new MTK6737SystemDriver();
    }

    protected SystemDriverable Driver() {
        SystemDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof SystemDriverable) {
            ret = (SystemDriverable) driver;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            driver.onCreate(packet1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Packet getInfo() {
        Packet ret = super.getInfo();
        if (ret == null) {
            ret = new Packet();
        }
        String systemVersion = VersionDriver.Driver().getSystemVersion();
        String AppVersion = VersionDriver.Driver().getAPPVersion();
        ret.putString("SystemVersion", systemVersion);
        ret.putString("AppVersion", AppVersion);
        return ret;
    }

    public void updateUSB(String path) {

        if (!isImporting) {
            updateThread = new Thread(new UpdateRunnable(path));
            updateThread.start();
        }
    }

    private Thread updateThread;
    private static boolean isImporting = false;


    private class UpdateRunnable implements Runnable {
        private String updatePath;


        public UpdateRunnable(String path ) {
            this.updatePath =path;
        }

        @Override
        public void run() {
            isImporting = true;
            long freeSpace = FileUtil.getAvailableSize("/storage/sdcard0/");
            if(freeSpace > 800) {

                try {
                    FileUtil.copyFile(updatePath, UPDATE_COPY);
                } catch (IOException e) {
                    System.out.println(" copyFile updatePath  Exception...");
                    FileUtil.deleteFromName(UPDATE_COPY);
                    isImporting = false;
                    return;
                }
                BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                //begin zhongyang.hu add for mcu auto update 20171211
                SystemProperties.set("persist.sys.mcu_update_finish", "false");
                //end zhongyang.hu add for mcu auto update 20171211
                logic.Notify(SystemPermissionInterface.MAIN_TO_APK.OS_UPDATE, null);
            }

            isImporting = false;

        }
    }
    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        switch (nId) {
            case SystemInterface.ApkToMain.OS_UPDATE:
                BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                if (null != logic) {
                    //begin zhongyang.hu add for mcu auto update 20171211
                    SystemProperties.set("persist.sys.mcu_update_finish", "false");
                    //end zhongyang.hu add for mcu auto update 20171211
                    logic.Notify(SystemPermissionInterface.MAIN_TO_APK.OS_UPDATE, null);
                }
                break;
            case SystemInterface.ApkToMain.UPGRADE:
                if (null != packet) {
                    final String upgrade = packet.getString("upgrade");
                    if (SettingsDefine.Upgrade.ONLINE.equals(upgrade)) {
                        if (null != mOnlineUpgrade) {
                            final String client = VersionDriver.Driver().getClientProject();
                            final String name = SettingsDefine.UpgradeName.OS;
                            final long id = VersionDriver.Driver().getAPPVersionID();
                            final String hardware = VersionDriver.Driver().getHardVersion();
                            mOnlineUpgrade.onlineUpgrade(getMainContext(), client, name, id, hardware);
                        }
                    } else {
                        ApkUtils.runApk(getMainContext(), getAPKPacketName(), packet, true);
                    }
                }
                break;
            case SystemInterface.ApkToMain.FORCE_ONLINE_UPGRADE_ALL_PACKAGE:
                if (null != packet) {
                    final boolean open = packet.getBoolean("open");
                    if (null != mOnlineUpgrade) {
                        mOnlineUpgrade.forceUpgradeAllPackage(open);
                    }
                }
                break;
            default:
                break;
        }
        return ret;
    }
}
