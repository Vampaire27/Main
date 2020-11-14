package com.wwc2.main.poweroff;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.client.driver.BaseClientDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.mainui.MainUILogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.navi.driver.NaviDriverable;
import com.wwc2.main.poweroff.driver.PoweroffDriverable;
import com.wwc2.main.poweroff.driver.SystemPoweroffDriver;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.poweroff_interface.PoweroffDefine;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * the power off logic.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public class PoweroffLogic extends BaseLogic {

    /**TAG*/
    private static String TAG = "PoweroffLogic";

    /**进入关机前的source*/
    private int mEnterPoweroffSource = Define.Source.SOURCE_NONE;

    /**进入关机前的包名*/
    protected String mEnterPoweroffPkgName = null;

    /**进入关机前的类名*/
    protected String mEnterPoweroffClsName = null;

    /**power off lock object.*/
    protected static final Lock mPoweroffLock = new ReentrantLock();

    @Override
    public String getTypeName() {
        return "Poweroff";
    }

    @Override
    public String getMessageType() {
        return PoweroffDefine.MODULE;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public boolean isPoweroffSource() {
        return true;
    }

    @Override
    public boolean isScreenoffSource() {//进关机界面，增加关屏功能。
        String client = VersionDriver.Driver().getClientProject();
        LogUtils.d(TAG, "PoweroffLogic---client="+client);
        if (client.equals(BaseClientDriver.CLIENT_AM) || FactoryDriver.Driver().getCloseScreen()) {//只有爱民的需要关闭时钟
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean passive() {
        return false;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_POWEROFF;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.poweroff";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.poweroff.MainActivity";
    }

    @Override
    public BaseDriver newDriver() {
        return new SystemPoweroffDriver();
    }

    /**
     * the driver interface.
     */
    protected PoweroffDriverable Driver() {
        PoweroffDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof PoweroffDriverable) {
            ret = (PoweroffDriverable) driver;
        }
        return ret;
    }

    @Override
    public boolean runApk() {
        // enter the poweroff, start poweroff service.
        LogUtils.d(TAG, "start poweroff service start.");
//        if (ApkUtils.isServiceRunning(getMainContext(), PoweroffDefine.FLOAT_WINDOW_SERVICE_NAME)) {
//            LogUtils.e(TAG, "poweroff service is running!");
//        }
//        ApkUtils.startServiceSafety(getMainContext(), PoweroffDefine.FLOAT_WINDOW_SERVICE_NAME, PoweroffDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME, PoweroffDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
//        LogUtils.d(TAG, "start poweroff service over.");
        if (FactoryDriver.Driver().getCloseScreen()) {
            return false;//旋转屏的需启动apk，点击apk需退出关机时钟界面。
        }
        return false;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        final int curSource = SourceManager.getCurSource();
        int oldSource = SourceManager.getOldSource();
        String curPkgName = SourceManager.getCurPkgName();
        String curClsName = SourceManager.getCurClsName();
        LogUtils.d(TAG, "Enter Poweroff, curSource = " + Define.Source.toString(curSource) +
                ", oldSource = " + Define.Source.toString(oldSource) +
                ", curPkgName = " + curPkgName +
                ", curClsName = " + curClsName);

        //待机后开机，需进入待机前的真源界面。
        BaseLogic oldLogic = ModuleManager.getLogicBySource(oldSource);
        boolean bGetLast = false;
        if (null != oldLogic) {
            if (oldLogic.isPoweroffSource() || (!oldLogic.isSource() && oldSource != Define.Source.SOURCE_THIRDPARTY)) {
                bGetLast = true;
            }
        } else {
            bGetLast = true;
        }
        if (bGetLast) {
            oldSource = SourceManager.getLastNoPoweroffRealSource();
        }

        mEnterPoweroffSource = oldSource;
        mEnterPoweroffPkgName = curPkgName;
        mEnterPoweroffClsName = curClsName;

        Driver().powerOff();
    }

    @Override
    public void onPause() {
        super.onPause();

//        Driver().powerOn();
    }

    @Override
    public void onStop() {
        // leave the poweroff, stop poweroff service.
        LogUtils.d(TAG, "stop poweroff service start.");
        ApkUtils.stopServiceSafety(getMainContext(), PoweroffDefine.FLOAT_WINDOW_SERVICE_NAME, PoweroffDefine.FLOAT_WINDOW_SERVICE_PACKET_NAME, PoweroffDefine.FLOAT_WINDOW_SERVICE_CLASS_NAME);
        LogUtils.d(TAG, "stop poweroff service over.");

        Packet packet = new Packet();
        packet.putString("pkgName", getAPKPacketName());
        BaseLogic l = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
        if (null != l) {
            l.Notify(SystemPermissionInterface.MAIN_TO_APK.KILL_PROCCESS, packet);
        }

        Driver().powerOn();

        super.onStop();
    }



    @Override
    public void onDestroy() {
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public boolean onStatusEvent(int type, boolean status, Packet packet) {
        boolean ret = false;
        switch (type) {
            case EventInputDefine.Status.TYPE_ACC:
                break;
            case EventInputDefine.Status.TYPE_BRAKE:
                break;
            case EventInputDefine.Status.TYPE_ILL:
                break;
            case EventInputDefine.Status.TYPE_CAMERA:
                break;
            case EventInputDefine.Status.TYPE_BEEP:
                ret = true;
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;

        mPoweroffLock.lock();
        try {
            final boolean anyKey = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getInfo().getBoolean(SettingsDefine.Common.Switch.ANY_KEY.value());
            boolean pop = false;
            if (anyKey) {
                pop = true;
            } else if (Define.Key.KEY_POWER == key) {
                pop = true;
            } else if (Define.Key.KEY_SHORT_POWEROFF == key) {
                pop = true;
            } if (Define.Key.KEY_LONG_POWEROFF == key) {
                pop = true;
            }
            if (pop) {
                if (PowerManager.isRuiPai()) {//锐派增加开机时显示开机LOGO 2020-03-31
                    //通知ｍａｉｎＵＩ显示ｌｏｇｏ
                    BaseLogic mLogic = ModuleManager.getLogicByName(MainUIDefine.MODULE);
                    if (mLogic instanceof MainUILogic) {
                        MainUILogic mainUILogic = (MainUILogic) mLogic;
                        mainUILogic.showLogoWhileLeaveDeepSleep();
                    }
                }

                boolean result;
                if ((mEnterPoweroffPkgName != null && mEnterPoweroffPkgName.equals(query_pkg_name(getMainContext()))) ||
                        mEnterPoweroffSource == Define.Source.SOURCE_THIRDPARTY) {
                    result = SourceManager.onRemoveChangePackage(mEnterPoweroffPkgName, null, source());
                    if (result) SourceManager.onOpenBackgroundSource();
                }
                else {
                    result = SourceManager.onRemoveChangeSource(mEnterPoweroffSource, source());
                }

                if (!result) {
                    SourceManager.onPopSourceNoPoweroff();
                }
            }
        } finally {
            mPoweroffLock.unlock();
        }
        return ret;
    }

    private String query_pkg_name(Context context) {
        String ret = null;
//        ContentResolver resolver = context.getContentResolver();
//        if (null != resolver) {
//            Cursor cursor = resolver.query(com.wwc2.common_interface.Provider.ProviderColumns.CONTENT_URI, null, null, null, null);
//            if(cursor != null){
//                if(cursor.moveToFirst()){
//                    ret = cursor.getString(cursor.getColumnIndex(com.wwc2.navi_interface.Provider.NAVI_PKG_NAME()));
//                }
//                cursor.close();
//            }
//        }
        BaseDriver driver = ModuleManager.getLogicByName(NaviDefine.MODULE).getDriver();
        if (driver instanceof NaviDriverable) {
            NaviDriverable naviDriverable = (NaviDriverable) driver;
            ret = naviDriverable.getNavigationPacketName();
        }
        return ret;
    }
}
