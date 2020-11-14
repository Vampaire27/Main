package com.wwc2.main;

import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.audio_interface.AudioDefine;
import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.camera_interface.CameraDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.logic.LogicService;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr_interface.DVRDefine;
import com.wwc2.irdvr_interface.IRDVRDefine;
import com.wwc2.launcher_interface.LauncherDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.eq.EQDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.mcu.McuDriver;
import com.wwc2.main.driver.steer.SteerDriver;
import com.wwc2.main.driver.storage.StorageDriver;
import com.wwc2.main.driver.system.SystemDriver;
import com.wwc2.main.driver.tptouch.TpTouchDriver;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.driver.vrkey.VrKeyDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.manager.ConfigManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.provider.LogicProviderHelper;
import com.wwc2.main.third_party.ThirdpartyDefine;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.mcu_interface.McuDefine;
import com.wwc2.navi_interface.NaviDefine;
import com.wwc2.phonelink_interface.PhonelinkDefine;
import com.wwc2.poweroff_interface.PoweroffDefine;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.standby_interface.StandbyDefine;
import com.wwc2.system_interface.SystemDefine;
import com.wwc2.systempermission_interface.SystemPermissionDefine;
import com.wwc2.tv_interface.TVDefine;
import com.wwc2.video_interface.VideoDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.weather_interface.WeatherDefine;

/**
 * the main service to handle logic.
 *
 * @author wwc2
 * @date 2017/1/15
 */
public class MainService extends LogicService {

    /**
     * TAG
     */
    private static final String TAG = "MainService";

    //begin  zhongyang.hu add for upload error state info.  20180333
    public static final String INTENT_ERROR = "com.wwc2.system.error";
    public static final String ERROR_TYPE = "TYPE";
    public static final int MAIN_OR_SYS_REBOOT = 0;
    public static final int MCU_UART_ERROR = 1;
    public static final int APP_CRASH=2;
    //end


    /**
     * structure of the main service.
     */
    public MainService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "MainService onBind");

        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        // 配置Config
        ConfigManager.setSystemConfigDir("/custom/");

        super.onCreate();

        LogUtils.d(TAG, "MainService onCreate");
        // -----------------------------------
        // Set the priority of the calling thread, based on Linux priorities:
        // -----------------------------------
        // The Priority.
        final int priority = android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY;
        // Changes the Priority of the calling Thread!
        android.os.Process.setThreadPriority(priority);
        // Changes the Priority of passed Thread (first param)
        android.os.Process.setThreadPriority(android.os.Process.myTid(), priority);

        Packet packet = new Packet();
        packet.putObject("context", this);

        // 创建对象
        LogicProviderHelper.setContext(this);
        ConfigManager.onCreate(packet);
        DriverManager.regDriver(BacklightDriver.DRIVER_NAME, new BacklightDriver());
        DriverManager.regDriver(VolumeDriver.DRIVER_NAME, new VolumeDriver());
        DriverManager.regDriver(EQDriver.DRIVER_NAME, new EQDriver());
        DriverManager.regDriver(SteerDriver.DRIVER_NAME, new SteerDriver());
        DriverManager.regDriver(VersionDriver.DRIVER_NAME, new VersionDriver());
        DriverManager.regDriver(VrKeyDriver.DRIVER_NAME, new VrKeyDriver());
        DriverManager.regDriver(McuDriver.DRIVER_NAME, new McuDriver());
        DriverManager.regDriver(TpTouchDriver.DRIVER_NAME, new TpTouchDriver());
        DriverManager.regDriver(StorageDriver.DRIVER_NAME, new StorageDriver());
        DriverManager.regDriver(FactoryDriver.DRIVER_NAME, new FactoryDriver());
        DriverManager.regDriver(CommonDriver.DRIVER_NAME, new CommonDriver());
        DriverManager.regDriver(AudioDriver.DRIVER_NAME, new AudioDriver());
        DriverManager.regDriver(SystemDriver.DRIVER_NAME, new SystemDriver());

        LogicManager.regLogic(Define.MODULE);
        LogicManager.regLogic(EventInputDefine.MODULE);
        LogicManager.regLogic(NaviDefine.MODULE);
        LogicManager.regLogic(ThirdpartyDefine.MODULE);
        LogicManager.regLogic(LauncherDefine.MODULE);
        LogicManager.regLogic(PoweroffDefine.MODULE);
        LogicManager.regLogic(AccoffDefine.MODULE);
        LogicManager.regLogic(PhonelinkDefine.MODULE);
        LogicManager.regLogic(RadioDefine.MODULE);
        LogicManager.regLogic(BluetoothDefine.MODULE);
        LogicManager.regLogic(AuxDefine.MODULE);
        LogicManager.regLogic(CameraDefine.MODULE);
        LogicManager.regLogic(StandbyDefine.MODULE);
        LogicManager.regLogic(VideoDefine.MODULE);
        LogicManager.regLogic(AudioDefine.MODULE);
        LogicManager.regLogic(McuDefine.MODULE);
        LogicManager.regLogic(SystemDefine.MODULE);
        LogicManager.regLogic(MainUIDefine.MODULE);
        LogicManager.regLogic(SystemPermissionDefine.MODULE);
        LogicManager.regLogic(SettingsDefine.MODULE);
        LogicManager.regLogic(VoiceAssistantDefine.MODULE);
        LogicManager.regLogic(WeatherDefine.MODULE);
//        LogicManager.regLogic(DVRDefine.MODULE);
        //zhongyang.hu remove , for tpms send startCheckingReadIdle msg.what repeat,will make other boradcast lost.
        //LogicManager.regLogic(TPMSDefine.MODULE);
        //end
//        LogicManager.regLogic(TVDefine.MODULE);
//        LogicManager.regLogic(IRDVRDefine.MODULE);

        // 模块管理类被创建
        PowerManager.onCreate(packet);

        //begin  zhongyang.hu add for upload error state info.  20180333
        Intent mIntent = new Intent(INTENT_ERROR);
        mIntent.putExtra(ERROR_TYPE,MAIN_OR_SYS_REBOOT);
        this.sendBroadcast(mIntent);
        //end

        // 提示消息
        //Toast.makeText(this, "main服务启动...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder = new Notification.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置通知标题
                .setContentTitle("MainService is running")
                //设置通知内容
                .setContentText("never kill me！");
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //.setWhen(System.currentTimeMillis());
        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        startForeground(8888, builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // 提示消息
        Toast.makeText(this, "main服务销毁...", Toast.LENGTH_SHORT).show();
        // 打印调用堆栈信息
        RuntimeException e = new RuntimeException("Log: stack info");
        e.fillInStackTrace();
        LogUtils.e(TAG, "onDestroy stack, ", e);
        stopForeground(true);
        // 销毁对象
        ConfigManager.onDestroy();

        // 开机管理类被销毁
        PowerManager.onDestroy();

        super.onDestroy();

        LogUtils.d(TAG, "MainService onDestroy");
    }
}
