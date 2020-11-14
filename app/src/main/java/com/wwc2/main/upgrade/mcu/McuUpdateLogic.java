package com.wwc2.main.upgrade.mcu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.message.MessageDefine;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.upgrade.OnlineUpgrade;
import com.wwc2.mcu_interface.McuDefine;
import com.wwc2.mcu_interface.McuInterface;
import com.wwc2.settings_interface.SettingsDefine;

/**
 * the phone link logic.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public class McuUpdateLogic extends BaseLogic implements Runnable {

    private static final String TAG = "McuUpdateLogic";


    private static Boolean serialEnable =true;


    private static byte oldVale =0x55;

    private static int cnt =0;

    /**
     * true: mcu  is update ing...
     * false:mcu not in update
     */

    public static boolean getMCUupdateState(){
        return !serialEnable;
    }
    /**
     * online upgrade.
     */
    private OnlineUpgrade mOnlineUpgrade = new OnlineUpgrade(new OnlineUpgrade.OnlineUpgradeListener() {
        @Override
        public void UpgradeStatusChange(int status) {
            Packet packet = new Packet();
            packet.putInt("UpgradeStatus", status);
            Notify(McuInterface.MainToApk.UPGRADE_STATUS, packet);
        }

        /**
         *
         * http://www.icar001.com:5200/?client=MT1000&name=MCU&hardware_version=Unknown
         *
         * {"code":0,"msg":"\u8fd4\u56de\u6210\u529f","data":[{
         * "customername":"MT1000","appname":"MCU","version":"","hardware_version":"2.0",
         * "appfile_url":"http:\/\/www.icar001.com\/public\/mcu\/CM_16.09.18_18.37.bin","subpackage_appfile_url":""},{
         * "customername":"MT1000","appname":"MCU","version":"","hardware_version":"2.1",
         * "appfile_url":"http:\/\/www.icar001.com\/public\/mcu\/CM_16.09.22_15.39.bin","subpackage_appfile_url":""}]}
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
        return "McuUpdate";
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.mcuupdate";
    }

    @Override
    public String getMessageType() {
        return McuDefine.MODULE;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_MCUUPDATE;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        serialEnable =true;
        McuManager.getModel().bindListener(mMCUListener);
        bindMessage(McuDefine.MCU_UPDATE, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        McuManager.getModel().unbindListener(mMCUListener);
    }


    /**MCU数据监听器*/
    private McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {


        @Override
        public void DataListener(byte[] val) {
            if (null != val) {
                final int length = val.length;
                if (length >= 1) {
                    byte cmd = val[0];
                    switch (cmd) {
                        case (byte) com.wwc2.main.driver.mcu.driver.McuDefine.MCU_TO_ARM.MRPT_McuUpdataFinish:
                            LogUtils.d(TAG, "MRPT_McuUpdata Finish cnt= " + cnt );
                            if(++cnt >= 2){
                                cnt = 0;
                            }else{
                                return;
                            }
                            if(val[1]==0 && oldVale!=val[1] ){ //&& oldVale!=val[1]
                                String UPDATE_AGAIN_ACTION = "com.wwc2.mcuupdate.mupdateagain";
                                Context mCtx= getMainContext();
                                if(mCtx != null) {
                                    mCtx.sendBroadcast(new Intent(UPDATE_AGAIN_ACTION));
                                    oldVale = val[1];
                                    LogUtils.d(TAG, "MRPT_Mcu mCtx= is ok "  );
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };

    @Override
    public boolean dispatch(int nId, Object object) {
        if (object instanceof Intent) {
            Intent updateIntent = (Intent) object;
            final String action = updateIntent.getAction();
            if (McuDefine.MCU_UPDATE.equals(action)) {
                serialEnable = updateIntent.getBooleanExtra(McuDefine.REQUEST_ALLOW, true);
                if (!serialEnable) {
                    new Thread(McuUpdateLogic.this).start();
                    Intent mcuIntent = new Intent(McuDefine.ACTION_RECEIVE);
                    mcuIntent.putExtra(McuDefine.SERIAL_STATE, false);
                    getMainContext().sendBroadcast(mcuIntent);
                    LogUtils.d(TAG, "McuUpdateLogic complete!");
                } else {
                    new Thread(McuUpdateLogic.this).start();
                    Intent mcuIntent = new Intent(McuDefine.ACTION_RECEIVE);
                    mcuIntent.putExtra(McuDefine.SERIAL_STATE, true);
                    getMainContext().sendBroadcast(mcuIntent);
                }
            } else if (McuDefine.MODULE.equals(action)) {
//                switch (nId) {
//                    case MessageDefine.APK_TO_MAIN_ID_CREATE:
//                        break;
//                    case MessageDefine.APK_TO_MAIN_ID_DESTROY:
//                        McuManager.onCreate(null);
//                        break;
//                    default:
//                        break;
//               }
            }
        }
        return super.dispatch(nId, object);
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        switch (nId) {
            case McuInterface.ApkToMain.UPGRADE:
                if (null != packet) {
                    final String upgrade = packet.getString("upgrade");
                    //begin zhongyang.hu add for MCU online upgrade
                    if (SettingsDefine.Upgrade.ONLINE.equals(upgrade))
                    {
                        ComponentName online = new ComponentName("com.wwc2.mcuupdate","com.wwc2.mcuupdate.activity.onLineActivity");
                        Intent mIntent  = new Intent();
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mIntent.setComponent(online);
                        getMainContext().startActivity(mIntent);
                    } else {
                        ApkUtils.runApk(getMainContext(), getAPKPacketName(), packet, true);
                    }
                }
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public void run() {
        if (!serialEnable) {
            LogUtils.d(TAG, " McuManager onDestroy!");
            McuManager.onDestroy();
        }else{
            LogUtils.d(TAG, " McuManager onCreate!");
            McuManager.onCreate(null);
        }
    }
}
