package com.wwc2.main;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.message.MessageDefine;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.backlight.BacklightDriverable;
import com.wwc2.main.driver.info.InfoDriver;
import com.wwc2.main.driver.info.InfoDriverable;
import com.wwc2.main.driver.mcu.McuDriverable;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.driver.version.VersionDriverable;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.driver.volume.VolumeDriverable;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.manager.VolumeManager;
import com.wwc2.main.upgrade.system.NetworkUtils;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.systempermission_interface.SystemPermissionInterface;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    /**tag*/
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 110;

    public boolean isServiceRunning() {
        final boolean ret = ApkUtils.isServiceRunning(MainActivity.this, MessageDefine.MAIN_SERVICE_CLASS_NAME);
        if (!ret) {
            Toast.makeText(MainActivity.this, "服务还未启动！", Toast.LENGTH_SHORT).show();
        }
        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&& Settings.System.canWrite(this)) {
                //检查返回结果
                Toast.makeText(MainActivity.this, "WRITE_SETTINGS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "WRITE_SETTINGS permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }


        findViewById(R.id.start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApkUtils.startServiceSafety(MainActivity.this, MessageDefine.MAIN_SERVICE_NAME,
                        MessageDefine.MAIN_SERVICE_PACKET_NAME, MessageDefine.MAIN_SERVICE_CLASS_NAME);
            }
        });

        //mButtonStopService.setVisibility(View.INVISIBLE);
        findViewById(R.id.stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApkUtils.stopServiceSafety(MainActivity.this, MessageDefine.MAIN_SERVICE_NAME,
                        MessageDefine.MAIN_SERVICE_PACKET_NAME, MessageDefine.MAIN_SERVICE_CLASS_NAME);
            }
        });

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.run_radio_apk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_RADIO);
                }
            }
        });

        findViewById(R.id.return_third_party).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    SourceManager.onOpenBackgroundSource(Define.Source.SOURCE_RADIO);
                }
            }
        });

        findViewById(R.id.return_last_source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    SourceManager.onPopSource();
                }
            }
        });

        findViewById(R.id.jni_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    byte[] data = new byte[1];
                    data[0] = 0;
                    McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_NORMAL, false, (byte) 0x01, data, 1);
//                      byte[] data = new byte[3];
//                      data[0] = 0x1C;
//                      data[1] = 0x00;
//                      data[2] = 0x00;
//                      McuManager.sendMcu(McuDriverable.SERIAL_PRIORITY_NORMAL, false, (byte) 0x21, data, 3);
                    LogUtils.d("jni test.");
                }
            }
        });

        findViewById(R.id.run_bluetooth_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.enter_screenoff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BacklightDriverable driverable = BacklightDriver.Driver();
                if (null != driverable) {
                    driverable.close();
                }
            }
        });

        findViewById(R.id.simulate_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    EventInputManager.NotifyKeyEvent(true, Define.KeyOrigin.SOFTWARE, Define.Key.KEY_HOME, null);
                }
            }
        });

        findViewById(R.id.volume_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolumeDriverable driverable = VolumeDriver.Driver();
                if (null != driverable) {
                    driverable.increase(1);
                    driverable.operate(Define.VolumeOperate.SHOW);
                }
            }
        });

        findViewById(R.id.volume_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolumeDriverable driverable = VolumeDriver.Driver();
                if (null != driverable) {
                    driverable.decrease(1);
                    driverable.operate(Define.VolumeOperate.SHOW);
                }
            }
        });

        findViewById(R.id.volume_mute_unmute).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolumeDriverable driverable = VolumeDriver.Driver();
                if (null != driverable) {
                    boolean mute = VolumeManager.getMute();
                    driverable.mute(mute);
                    driverable.operate(Define.VolumeOperate.SHOW);
                }
            }
        });

        findViewById(R.id.volume_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolumeDriverable driverable = VolumeDriver.Driver();
                if (null != driverable) {
                    driverable.operate(Define.VolumeOperate.SHOW);
                }
            }
        });

        findViewById(R.id.get_all_apps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ResolveInfo> list = ApkUtils.getAppResolveInfo(MainActivity.this);
                if (null != list) {
                    for (int i = 0; i < list.size(); i++) {
                        ResolveInfo info = list.get(i);
                        if (null != info) {
                            String packet = info.activityInfo.packageName;
                            String name = ApkUtils.getApkName(MainActivity.this, packet);
                            LogUtils.d("AllApps", "name = " + name + ", packet = " + packet);
                        }
                    }
                }
            }
        });

        findViewById(R.id.enter_standby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    SourceManager.onChangeSource(Define.Source.SOURCE_STANDBY);
                }
            }
        });

        findViewById(R.id.btn_open_eq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName("com.wwc2.settings", "com.wwc2.settings.SoundEffectActivity"));
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.get_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VersionDriverable driverable = VersionDriver.Driver();
                if (null != driverable) {
                    String version = null;
                    version = driverable.getModelNumber();
                    version = driverable.getFirewareVersion();
                    version = driverable.getBasebandVersion();
                    version = driverable.getKernelVersion();
                    version = driverable.getSystemVersion();
                    version = driverable.getMcuVersion();
                    version = driverable.getBluetoothVersion();
                    version = driverable.getAPPVersion();
                    version = driverable.getApkVersion(VoiceAssistantDefine.MODULE);
                    version = driverable.getApkVersion(BluetoothDefine.MODULE);
                    version = driverable.getApkVersion(RadioDefine.MODULE);
                }
            }
        });

        findViewById(R.id.reboot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.REBOOT, null);
                    }
                }
            }
        });

        findViewById(R.id.os_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.OS_UPDATE, null);
                    }
                }
            }
        });

        findViewById(R.id.kill_process).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    SourceManager.onExitPackage("com.android.browser");
                }
            }
        });

        findViewById(R.id.enter_backlight_ui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    SourceManager.onRemoveChangePackage("com.wwc2.settings",
                            "com.wwc2.settings.BrightessActivity",
                            Define.Source.SOURCE_NONE);
                }
            }
        });

        findViewById(R.id.get_state_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    InfoDriverable driverable = InfoDriver.Driver();
                    if (null != driverable) {
                        Context context = MainActivity.this;
                        String sinfo = null;
                        boolean binfo = false;
                        int iinfo = 0;
                        binfo = driverable.isNetworkRoaming(context);
                        binfo = driverable.hasIccCard(context);
                        sinfo = driverable.getVoiceMailNumber(context);
                        sinfo = driverable.getVoiceMailAlphaTag(context);
                        sinfo = driverable.getSubscriberId(context);
                        iinfo = driverable.getSimState(context);
                        sinfo = driverable.getSimSerialNumber(context);
                        sinfo = driverable.getSimOperatorName(context);
                        sinfo = driverable.getSimOperator(context);
                        sinfo = driverable.getSimCountryIso(context);
                        iinfo = driverable.getPhoneType(context);
                        iinfo = driverable.getNetworkType(context);
                        sinfo = driverable.getLine1Number(context);
                        sinfo = driverable.getIMEI(context);
                        sinfo = driverable.getIMEISV(context);
                        sinfo = driverable.getIpAddress(context);
                        sinfo = driverable.getWifiIpAddresses(context);
                        sinfo = driverable.getSerialNumber(context);
                        LogUtils.d(TAG, "get info over.");
                    }
                }
            }
        });

        findViewById(R.id.kill_process_sleep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> mKeepProcessPackageFilter = new ArrayList<>();
                mKeepProcessPackageFilter.add("com.wwc2.main");
                mKeepProcessPackageFilter.add("com.wwc2.accoff");
                mKeepProcessPackageFilter.add("com.wwc2.systempermission");
                mKeepProcessPackageFilter.add("com.wwc2.camera");
                mKeepProcessPackageFilter.add("com.wwc2.mainui");
                mKeepProcessPackageFilter.add("com.android.smspush");
                mKeepProcessPackageFilter.add("android.process.acore");
                mKeepProcessPackageFilter.add("com.android.externalstorage");
                mKeepProcessPackageFilter.add("com.android.cellbroadcastreceiver");
                mKeepProcessPackageFilter.add("com.android.dm");
                mKeepProcessPackageFilter.add("com.android.systemui");
                mKeepProcessPackageFilter.add("com.wwc2.launcher");
                mKeepProcessPackageFilter.add("com.android.launcher");
                mKeepProcessPackageFilter.add("com.android.launcher3");
                mKeepProcessPackageFilter.add("android.process.media");
                mKeepProcessPackageFilter.add("com.android.onetimeinitializer");
//                    mKeepProcessPackageFilter.add("com.android.settings");
//                    mKeepProcessPackageFilter.add("system_server");
//                    mKeepProcessPackageFilter.add("android.process.media");
//                    mKeepProcessPackageFilter.add("app_process");
//                    mKeepProcessPackageFilter.add("/system/bin/servicemanager");
//                    mKeepProcessPackageFilter.add("com.android.inputmethod.latin");
//                    mKeepProcessPackageFilter.add("/init");
//                    mKeepProcessPackageFilter.add("kthreadd");
//                    mKeepProcessPackageFilter.add("binder");
//                    mKeepProcessPackageFilter.add("zygote");
//                    mKeepProcessPackageFilter.add("/system/bin/drmserver");
//                    mKeepProcessPackageFilter.add("/system/bin/vtservice");
//                    mKeepProcessPackageFilter.add("/system/bin/installd");
//                    mKeepProcessPackageFilter.add(" /system/bin/keystore");
//                    mKeepProcessPackageFilter.add("permmgrd");
//                    mKeepProcessPackageFilter.add("emdaemon");
//                    mKeepProcessPackageFilter.add("/system/bin/dm_agent_binder");
//                    mKeepProcessPackageFilter.add("/system/bin/ppl_agent");
//                    mKeepProcessPackageFilter.add("/system/bin/em_svr");
//                    mKeepProcessPackageFilter.add("migration/1");
//                    mKeepProcessPackageFilter.add("kworker/1:0");
//                    mKeepProcessPackageFilter.add("ksoftirqd/1");
//                    mKeepProcessPackageFilter.add("kworker/1:1");
//                    mKeepProcessPackageFilter.add("com.android.defcontainer");
//                    mKeepProcessPackageFilter.add("com.android.gallery3d");
//                    mKeepProcessPackageFilter.add("com.android.keychain");
//                    mKeepProcessPackageFilter.add("/system/xbin/mnld");
//                    mKeepProcessPackageFilter.add("com.android.phone");
//                    mKeepProcessPackageFilter.add("tx_thread");
//                    mKeepProcessPackageFilter.add("/system/bin/wpa_supplicant");
//                    mKeepProcessPackageFilter.add("/system/bin/ccci_fsd");
//                    mKeepProcessPackageFilter.add("/system/bin/dhcpcd");
//                    mKeepProcessPackageFilter.add("/system/bin/rild");
//                    mKeepProcessPackageFilter.add("libmnlp");
//                    mKeepProcessPackageFilter.add("com.android.externalstorage");
//                    mKeepProcessPackageFilter.add("/system/bin/sh");
//                    mKeepProcessPackageFilter.add("permmgrd");
//                    mKeepProcessPackageFilter.add("com.svox.pico");

                mKeepProcessPackageFilter.add("com.baidu.input");
//                    mKeepProcessPackageFilter.add("com.baidu.BaiduMap:bdservice_v1");
//                    mKeepProcessPackageFilter.add("com.android.browser");
                Packet packet = new Packet();
                if (null != mKeepProcessPackageFilter) {
                    final int size = mKeepProcessPackageFilter.size();
                    if (size > 0) {
                        String[] packages = new String[size];
                        for (int i = 0; i < size; i++) {
                            packages[i] = mKeepProcessPackageFilter.get(i);
                        }
                        packet.putStringArray("KeepPackages", packages);
                    }
                }
                BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                if (null != logic) {
                    logic.Notify(SystemPermissionInterface.MAIN_TO_APK.KILL_SAVE_PROCESS, packet);
                }
            }
        });

        findViewById(R.id.status_bar_visible).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.ACTION_OPT_VISIBLE");
                intent.putExtra("visible", View.VISIBLE);
                sendBroadcast(intent);
            }
        });

        findViewById(R.id.status_bar_invisible).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.ACTION_OPT_VISIBLE");
                intent.putExtra("visible", View.INVISIBLE);
                sendBroadcast(intent);
            }
        });

        findViewById(R.id.notify_dvr_recording).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.wwc2.dvrapk.recordstatus");
                intent.putExtra("status", 1);
                sendBroadcast(intent);
            }
        });

        findViewById(R.id.notify_dvr_disrecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.wwc2.dvrapk.recordstatus");
                intent.putExtra("status", 0);
                sendBroadcast(intent);
            }
        });

        findViewById(R.id.notify_dvr_disable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.wwc2.dvrapk.recordstatus");
                intent.putExtra("status", -1);
                sendBroadcast(intent);
            }
        });

        findViewById(R.id.test_notify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pkgName = "com.wwc2.main";
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBulider = new NotificationCompat.Builder(MainActivity.this);
                mBulider.setShowWhen(false);
                Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
                if (null != intent) {
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                    mBulider.setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("测试通知Title")
                            .setContentText("测试通知Text")
                            .setPriority(Notification.PRIORITY_MAX)
                            .setContentIntent(pendingIntent);
                    Notification mNotification = mBulider.build();
                    mNotification.flags = Notification.FLAG_ONGOING_EVENT;
                    mNotification.defaults = Notification.DEFAULT_VIBRATE;
                    notificationManager.notify(99, mNotification);
                }
            }
        });

        findViewById(R.id.online_upgrade_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    if (NetworkUtils.checkNetworkAvailable(MainActivity.this)) {
                        // 创建线程获取数据
                        new Thread() {
                            @Override
                            public void run() {
                                String app = "0.0.55";
                                String system = "CHEJI01_YLK_V0.0.47_20160923";
                                final String version = "update-APP_V" + app + "-" + system;
                                String info = NetworkUtils.getWebContent("http://www.icar001.com:5200/?version=" + version);
                                if (!TextUtils.isEmpty(info)) {
                                    // {"code":0,"msg":"\u6709\u6700\u65b0\u7248\u672c","data":{"url":"http:\/\/www.icar001.com\/public\/appfile\/32.zip","version":"update-APP_V0.0.60-CHEJI01_YLK_V0.0.47_20160923","size":"1829KB"}}
                                    LogUtils.d(TAG, "ARM version info：" + info);
                                }
                            };
                        }.start();
                    } else {
                        Toast.makeText(MainActivity.this, "网络未连接，ARM版本获取失败！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        findViewById(R.id.call_system_bin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        Packet packet = new Packet();
                        packet.putString("command", "./system/bin/boot_logo_updater");
                        //packet.putString("command", "./sdcard/boot_logo_updater");
                        packet.putBoolean("isRoot", true);
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.EXEC_SHELL_COMMAND, packet);
                    }
                }
            }
        });

        final String ACTION_STORAGE_NOTIFICATION_ENABLE = "com.android.storage.notification.enable";
        findViewById(R.id.storage_notification_enable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = MainActivity.this;
                if (null != context) {
                    Intent intent = new Intent(ACTION_STORAGE_NOTIFICATION_ENABLE);
                    intent.putExtra("enable", true);
                    context.sendBroadcast(intent);
                }
            }
        });

        findViewById(R.id.storage_notification_disable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = MainActivity.this;
                if (null != context) {
                    Intent intent = new Intent(ACTION_STORAGE_NOTIFICATION_ENABLE);
                    intent.putExtra("enable", false);
                    context.sendBroadcast(intent);
                }
            }
        });

        findViewById(R.id.LOCATION_MODE_OFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        Packet packet = new Packet();
                        packet.putInt("mode", Settings.Secure.LOCATION_MODE_OFF);
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.GPS_MODE, packet);
                    }
                }
            }
        });

        findViewById(R.id.LOCATION_MODE_SENSORS_ONLY).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        Packet packet = new Packet();
                        packet.putInt("mode", Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.GPS_MODE, packet);
                    }
                }
            }
        });

        findViewById(R.id.LOCATION_MODE_BATTERY_SAVING).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        Packet packet = new Packet();
                        packet.putInt("mode", Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.GPS_MODE, packet);
                    }
                }
            }
        });

        findViewById(R.id.LOCATION_MODE_HIGH_ACCURACY).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning()) {
                    BaseLogic logic = ModuleManager.getLogicByName(SystemPermissionDefine.MODULE);
                    if (null != logic) {
                        Packet packet = new Packet();
                        packet.putInt("mode", Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.GPS_MODE, packet);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
