package com.wwc2.main.accoff.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.mainui.MainUILogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.settings.SettingsLogic;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.settings_interface.SettingsDefine;

/**
 * Created by swd1 on 17-6-16.
 */

public class AcconBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "AcconBroadcastReceiver";
    /**ACC OFF message.*/
    private static final String ACC_OFF = "com.android.wwc2.sleep";
    /**ACC ON message.*/
    private static final String ACC_ON = "com.android.wwc2.wakeup";


    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    private static final String Logic_Name = "com.wwc2.main.accoff.AccoffLogic";

    private static final String SDcardPath ="/storage/sdcard1/";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogUtils.d(TAG, "ACC " + action + " receive...");
        if (ACC_OFF.equals(action)) {
//            new Thread(new Runnable() {//不需处理此消息。
//                @Override
//                public void run() {
//                    BaseLogic logic = ModuleManager.getLogicByName(Logic_Name);
//                    if (logic != null) {/*-begin-20180425-ydinggen-add-解决空指针异常问题-*/
//                        MTK6737AccoffDriver Driver = (MTK6737AccoffDriver) logic.getDriver();
//                        if (Driver != null) {
//                            Driver.gotoDeepSleepComming();
//                        }
//                    }
//                }
//            }).start();
        } else if (ACC_ON.equals(action)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BaseLogic logicAccoff = ModuleManager.getLogicByName(AccoffDefine.MODULE);
                    if (logicAccoff == null) {
                        return;
                    }
                    Packet packet = logicAccoff.getInfo();
                    if (packet != null) {
                        int accStep = packet.getInt("AccoffStep");

                        //深度睡眠唤醒才执行以下操作
                        if (AccoffListener.AccoffStep.isDeepSleep(accStep)) {

                            //通知ｍａｉｎＵＩ显示ｌｏｇｏ
                            BaseLogic mLogic = ModuleManager.getLogicByName(MainUIDefine.MODULE);
                            if (mLogic instanceof MainUILogic) {
                                MainUILogic mainUILogic = (MainUILogic) mLogic;
                                mainUILogic.showLogoWhileLeaveDeepSleep();
                            }
                        }
                        BaseLogic logic = ModuleManager.getLogicByName(Logic_Name);
                        MTK6737AccoffDriver Driver = (MTK6737AccoffDriver) logic.getDriver();
                        if (Driver != null) {
                            Driver.wakeupFromDeepSleepComming();
                        }

//                        Avm360Manager.openCameraBack(null);//灵动飞扬360
                    }
                }
            }).start();
            //zhongyang.hu add for mcu cannot upgrade auto  20180820
        }else if(BOOT_COMPLETED.equals(action)){
            CoreLogic coreLogic = LogicManager.getLogicByName(SettingsDefine.MODULE);
            if (coreLogic instanceof SettingsLogic) {
                ((SettingsLogic) coreLogic).checkUsbConfig();
            }

            LogUtils.d(TAG, "Power test add start dvr server!");
            if (FactoryDriver.Driver() != null) {
                if (SystemProperties.get("ro.wtDVR", "false").equals("false") ||
                        !FactoryDriver.Driver().getDvrEnable()) {

                } else {
                    //test add dvr
                    if (ApkUtils.isAPKExist(context, "com.wwc2.dvr")) {
                        ApkUtils.startServiceSafety(context,
                                "com.wwc2.dvr.RecordService",
                                "com.wwc2.dvr",
                                "com.wwc2.dvr.RecordService");

                        SystemProperties.set("wwc2.avm360.enable", "0");
                    }
                }
            }
        }
           //end
    }
}