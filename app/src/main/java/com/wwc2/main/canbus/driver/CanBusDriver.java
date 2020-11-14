package com.wwc2.main.canbus.driver;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.canbus_interface.CarSettingsDefine;
import com.wwc2.canbussdk.ICanbusCallback;
import com.wwc2.canbussdk.ICanbusDriver;
import com.wwc2.canmcu_interface.CanMcuDefine;
import com.wwc2.canmcusdk.ICanMcuCallback;
import com.wwc2.canmcusdk.ICanMcuDriver;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.SwitchStatus;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.model.MStringArray;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.canbus.CanBusListener;
import com.wwc2.main.canbus.CanBusManager;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.audio.AudioListener;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.language.LanguageDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.memory.mode.ini.IniMemory;
import com.wwc2.main.driver.sdk.BaseSDKMemoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.settings.SettingsListener;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.video_interface.VideoDefine;
import com.wwc2.video_interface.VideoInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * the can bus driver.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public class CanBusDriver extends BaseSDKMemoryDriver implements CanBusDriverable {
    /**TAG*/
    private static final String TAG = "CanBusDriver";

    /**main can bus logic*/
    private BaseLogic mCanBusLogic = null;

    /**aidl driver*/
    public ICanbusDriver mCanbusAIDLDriver = null;

    /**
     * callback interface
     */
    private ICanbusCallback mCanbusCallback = null;

    //CanMCUSDK
    private static ICanMcuDriver iCanMcuDriver = null;
    private static ICanMcuCallback iCanMcuCallback = null;

    private static List<HostInfoObject> mHostObject = null;

    private static boolean panoramicView = false;

    private boolean mAjxPhone = false;

    private String canboxVersion = "0.0";
    private String strCanSerises = "Unknown";

    private static final String WWC2_CANMCUSDK_NAME = "com.wwc2.canmcusdk";
    private static final String WWC2_CANBUSSDK_NAME = "com.wwc2.canbussdk";

    public void reloadConfig() {
        LogUtils.d(TAG, "reloadConfig()---");
        if (mMemory != null && mMemory instanceof IniMemory) {
            ((IniMemory) mMemory).reloadIniFile();
            readData();
        } else {
            LogUtils.e(TAG, "reloadConfig()---error");
        }
    }

    /**
     * the model data.
     */
    protected static class CanBusModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putBoolean("CanEnable", getCanEnable().getVal());
            packet.putBoolean("CanSwitch", getCanSwitch().getVal());
            packet.putInt("CanSwitchStatus", getCanSwitchStatus().getVal());
            packet.putInt("CanSeries", getCanSeries().getVal());
            packet.putString("Implementation", getImplementation().getVal());
            packet.putStringArray("SupportFunction", getSupportFunction().getVal());
            packet.putString("CanBusVersion", getCanBusVersion().getVal());
            packet.putBoolean("CanBusConnected", getCanBusConnected().getVal());
//            packet.putString("SerialPort", getCanBusSerialPort().getVal());
            packet.putString("SerialPort", SystemProperties.get("ro.uart_canbus", "/dev/ttyMT2"));//Can的串口号不需要配置，只与平台有关
            return packet;
        }

        /**
         * CanBus使能
         */
        private MBoolean mCanEnable = new MBoolean(this, "CanEnableListener", true);
        public MBoolean getCanEnable() {
            return mCanEnable;
        }

        /**
         * CanBus开关
         */
        private MBoolean mCanSwitch = new MBoolean(this, "CanSwitchListener", false);
        public MBoolean getCanSwitch() {
            return mCanSwitch;
        }

        /**
         * CanBus开关, see {@link SwitchStatus}
         */
        private MInteger mCanSwitchStatus = new MInteger(this, "CanSwitchStatusListener", SwitchStatus.DEFAULT);

        public MInteger getCanSwitchStatus() {
            return mCanSwitchStatus;
        }

        /**
         * Implementation 实现方式
         */
        private MString mImplementation = new MString(this, "ImplementationListener", CanBusDefine.Implementation.DEFAULT);

        public MString getImplementation() {
            return mImplementation;
        }

        /**
         * can协议类型
         */
        private MInteger mCanSeries = new MInteger(this, "CanSeriesListener", CanBusDefine.CanSeries.RuiZhiCheng.VOLKSWAGEN_GENERAL);

        public MInteger getCanSeries() {
            return mCanSeries;
        }

        /**
         * 具体车型所支持的功能列表
         */
        private MStringArray mSupportFunction = new MStringArray(this, "SupportFunctionListener", CanBusDefine.Function.DEFAULT_SUPPORT);

        public MStringArray getSupportFunction() {
            return mSupportFunction;
        }

        /**
         * can(canBus)版本信息
         */
        private MString mCanBusVersion = new MString(this, "CanBusVersionListener", "Unknown:0.0");

        public MString getCanBusVersion() {
            return mCanBusVersion;
        }

        /**
         * can(canBus)是否连接信息
         */
        private MBoolean mCanBusConnected = new MBoolean(this, "CanBusConnectedListener", false);

        public MBoolean getCanBusConnected() {
            return mCanBusConnected;
        }

        /**
         * CAN串口号
         */
        private MString mCanBusSerialPort = new MString(this, "CanBusSerialPortListener", "/dev/ttyMT2");

        public MString getCanBusSerialPort() {
            return mCanBusSerialPort;
        }
    }

    @Override
    public BaseModel newModel() {
        return new CanBusModel();
    }

    /**
     * get the model object.
     */
    protected CanBusModel Model() {
        CanBusModel ret = null;
        BaseModel model = getModel();
        if (model instanceof CanBusModel) {
            ret = (CanBusModel) model;
        }
        return ret;
    }

    /**
     * the callback interface.
     */
    public class CanbusCallBack extends ICanbusCallback.Stub {

        @Override
        public boolean sendDataToCan(byte[] bytes) throws RemoteException {
            return CanBusDriver.this._sendData(bytes);
        }

        @Override
        public boolean carSteerProcess(int i) throws RemoteException {
//            LogUtils.i(TAG, "carSteerProcess--i="+i);
            Packet packet = new Packet();
            packet.putInt("key", i);

            if (null != packet) {
                int key = i;//packet.getInt("key", -1);
                if (-1 != key) {
                    LogUtils.d(TAG, "NotifyKeyEvent key = " + Define.Key.toString(key));
                    if (key == Define.Key.KEY_RADARON) {
                        panoramicView = true;
                    } else if (key == Define.Key.KEY_RADAROFF) {
                        panoramicView = false;
                    }
                    EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.CAN, key, packet);
                }
            }
            return false;
        }

        @Override
        public void canBusVersion(String s) throws RemoteException {
            LogUtils.d(TAG, "CanBusVersion:" + s);
            canboxVersion = s;
            Model().getCanBusVersion().setVal(strCanSerises + ":" + canboxVersion);
        }

        @Override
        public void canBusConnect(boolean b) throws RemoteException {
            LogUtils.d(TAG, "CanBusConnected:" + b);
            Model().getCanBusConnected().setVal(b);
        }

        @Override
        public boolean canbusDataCallBack(String s, int i, Bundle bundle) throws RemoteException {
            return false;
        }

        @Override
        public void supportFunction(String[] strings) throws RemoteException {

        }

        @Override
        public void setSoundChannelToMcu(int i) throws RemoteException {
            LogUtils.d(TAG, "setSoundChannelToMcu 00 :" + i);
            mAjxPhone = (i == 1);
            if (mAjxPhone) {
                // 申请音频
                AudioDriver.Driver().request(null,
                        AudioDefine.AudioStream.STREAM_MUSIC,
                        AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
            }

            byte[] data = new byte[1];
            data[0] = (byte) i;
            McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.OP_CarChannel, data, 1);
        }

        @Override
        public void setCanSerises(int i, String s) throws RemoteException {
            LogUtils.d(TAG, "setCanSerises :" + i);
            initCanbus(Model().getImplementation().getVal(),
                    Model().getCanSwitch().getVal(), i, false);

            strCanSerises = s;
            Model().getCanBusVersion().setVal(strCanSerises + ":" + canboxVersion);
        }
    }

    @Override
    public void switchStatusChanged(int oldVal, int newVal) {
        LogUtils.d(TAG, "switchStatusChanged()---newVal="+newVal);
        Model().getCanSwitchStatus().setVal(newVal);
        if (!SwitchStatus.isOpened(oldVal) && SwitchStatus.isOpened(newVal)) {
            LogUtils.d(TAG, "CanBusReady.");
//            if (null != mAIDLDriver) {
//                try {
//                    mAIDLDriver.ready();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    /**main can bus listener.*/
    private CanBusListener mCanBusListener = new CanBusListener() {
        @Override
        public void ImplementationListener(String oldVal, String newVal) {
            final boolean oldMcu = CanBusDefine.Implementation.isMcu(oldVal);
            final boolean newMcu = CanBusDefine.Implementation.isMcu(newVal);
            if (oldMcu != newMcu) {
                CanBusDriver.this.needMCUChanged(newMcu);
                if (newMcu) {
                    McuManager.getModel().bindListener(mMcuListener);
                } else {
                    McuManager.getModel().unbindListener(mMcuListener);
                }
            }
        }
    };

    @Override
    public boolean enableCanbus(boolean enable) {
        boolean ret = false;
        Model().getCanEnable().setVal(enable);
        LogUtils.d(TAG, "enable can bus, enable = " + enable + " canSwitch:" + Model().getCanSwitch().getVal());
        if (enable) {
            boolean canSwitch = Model().getCanSwitch().getVal();
            if (!canSwitch && ApkUtils.isAPKExist(getMainContext(), "com.baony.avm360")) {//带360的默认启动Canbus，在Canbus中判断开关状态。
                ret = CanBusManager.connectSDK();
            } else {
                ret = initCanbus(Model().getImplementation().getVal(),
                        Model().getCanSwitch().getVal(),
                        Model().getCanSeries().getVal(), true);
            }
        } else {
            ret = initCanbus(Model().getImplementation().getVal(),
                    false,
                    Model().getCanSeries().getVal(), true);
        }
        return ret;
    }

    @Override
    public boolean initCanbus(String impl, boolean open, int canSeries, boolean kill) {
        LogUtils.d(TAG, "initCanbus()---impl="+impl+", open="+open+", canSeries="+canSeries);

        boolean ret = false;
        if (!Model().getCanEnable().getVal()) {
            if (open) {
                LogUtils.w(TAG, "initCanbus failed, because can bus is disable.");
                return false;
            }
        }

        String oldImpl = Model().getImplementation().getVal();
        int oldCanSeries = Model().getCanSeries().getVal();

        Model().getCanSeries().setVal(canSeries);
        Model().getImplementation().setVal(impl);
        Model().getCanSwitch().setVal(open);
        if (mMemory != null) {
            mMemory.save();
        } else {
            LogUtils.e(TAG, "mMemory == null");
        }

        if (ApkUtils.isAPKExist(getMainContext(), WWC2_CANMCUSDK_NAME)) {
            disconnectCanMcuSDK();
            connectCanMcuSDK(getMainContext());
            return true;
        }

        if (!impl.equals(oldImpl) || canSeries != oldCanSeries) {
            LogUtils.d(TAG, "re open Canbus old canseries=" + oldCanSeries);
            if (open) {
                LogUtils.d(TAG, "re open Canbus cur canseries=" + Model().getCanSeries().getVal());
                if (ApkUtils.isAPKExist(getMainContext(), "com.wwc2.canbusapk") && kill) {
                    killProcess(getMainContext(), "com.wwc2.canbusapk");
                }
                ret = CanBusManager.disconnectSDK();
                if (ret) {
                    Model().getCanBusConnected().setVal(false);
                    canboxVersion = "0.0";
                    Model().getCanBusVersion().setVal(strCanSerises + ":" + canboxVersion);
                }
            }
        }

        if (open) {
            ret = CanBusManager.connectSDK();
            /*ApkUtils.startServiceSafety(getMainContext(), "com.wwc2.canbusapk.CanbusDriver",
                    "com.wwc2.canbusapk", "com.wwc2.canbusapk.CanbusDriver");*/
        } else {
            //bug13496CANBUS设置界面关闭再打开CANBUS，进入CAN参数设置界面，点击开关无效，退出再进入状态已变2018-10-26
            if (ApkUtils.isAPKExist(getMainContext(), "com.wwc2.canbusapk") && kill) {
                killProcess(getMainContext(), "com.wwc2.canbusapk");
            }

            if (!ApkUtils.isAPKExist(getMainContext(), "com.baony.avm360")) {
                ret = CanBusManager.disconnectSDK();
                if (ret) {
                    Model().getCanBusConnected().setVal(open);
                }
            }
        }

        return ret;
    }

    /**
     * 杀死指定包名的进程
     */
    public static boolean killProcess(Context context, String packageName) {
        boolean ret = false;
        if (null == context) {
            LogUtils.w(TAG, "camera#killProcess failed, because the context is null.");
            return ret;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (null != am) {
            try {
                LogUtils.d(TAG, "camera#killProcess packageName = " + packageName);
                Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
                forceStopPackage.setAccessible(true);
                forceStopPackage.invoke(am, packageName);
                ret = true;
            } catch (NoSuchMethodException e) {
                LogUtils.w(TAG, "camera#killProcess failed, because NoSuchMethodException.");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                LogUtils.w(TAG, "camera#killProcess failed, because IllegalAccessException.");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                LogUtils.w(TAG, "camera#killProcess failed, because InvocationTargetException.");
                e.printStackTrace();
            } catch (Exception e) {
                LogUtils.w(TAG, "camera#killProcess failed, because Exception.");
                e.printStackTrace();
            }
        } else {
            LogUtils.w(TAG, "camera#killProcess failed, because the activity manager is null.");
        }
        return ret;
    }


    @Override
    public void setCanbusType(int nId, Bundle bundle) {
//        try {
//            mAIDLDriver.setCanbusType(nId, bundle);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void sendHostInfoToCan(String key, int nId, Bundle bundle) {
        try {
            if (mCanbusAIDLDriver != null) {
                if (key.equals(CarSettingsDefine.Spinner.LANGUAGE.value())) {
                    mCanbusAIDLDriver.sendCanbusCmdToSDK(CarSettingsDefine.MODULE, nId, bundle);
                } else {
                    mCanbusAIDLDriver.sendCanbusCmdToSDK(CanBusDefine.MODULE, nId, bundle);
                }
            } else {
                if (key.equals(CanBusDefine.HostInfo.BACKCAMMER_STATE.value())) {
                    if (bundle.getInt(key) == 1) {
                        return;
                    }
                } else if (key.equals(CarSettingsDefine.Spinner.LANGUAGE.value())) {
                    //LANGUAGE是CarSettingsDefine中的定义，会引起异常。2018-10-24
                    return;
                }

                if (mHostObject == null) {
                    mHostObject = new ArrayList<>();
                }
                mHostObject.add(new HostInfoObject(nId, bundle));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        LogUtils.d(TAG, "onCreate()---");
        mCanBusLogic = ModuleManager.getLogicByName(CanBusDefine.MODULE);
        if (null != mCanBusLogic) {
            mCanBusLogic.getModel().bindListener(mCanBusListener);
        }
        SourceManager.getModel().bindListener(mSourceListener);
        DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel().bindListener(mAudioListener);
        LogicManager.getLogicByName(SettingsDefine.MODULE).getModel().bindListener(mSettingsListener);

        if (ApkUtils.isAPKExist(getMainContext(), WWC2_CANMCUSDK_NAME)) {
            if (PowerManager.isRuiPai_SP()) {
                FactoryDriver.Driver().setZhiNengTong(FactoryDriver.Driver().getZhiNengTong());
            } else {
                connectCanMcuSDK(getMainContext());
            }
        } else {
            if (ApkUtils.isAPKExist(getMainContext(), WWC2_CANBUSSDK_NAME)) {
                enableCanbus(true);
            } else {
                //当canbussdk不存在时，开关打开时仍要处理MCU转发的CAN数据2020-11-11暂适用进业智车007项目
                if (Model().getCanSwitch().getVal()) {
                    McuManager.getModel().bindListener(mMcuListener);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mMemory != null) {
            mMemory.save();
        }
        LogicManager.getLogicByName(SettingsDefine.MODULE).getModel().unbindListener(mSettingsListener);
        DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel().unbindListener(mAudioListener);
        SourceManager.getModel().unbindListener(mSourceListener);
        if (null != mCanBusLogic) {
            mCanBusLogic.getModel().unbindListener(mCanBusListener);
        }
        mCanBusLogic = null;

        if (ApkUtils.isAPKExist(getMainContext(), WWC2_CANMCUSDK_NAME)) {
            disconnectCanMcuSDK();
        } else {
            if (ApkUtils.isAPKExist(getMainContext(), WWC2_CANBUSSDK_NAME)) {
                CanBusManager.disconnectSDK();
            } else {
                //当canbussdk不存在时，开关打开时仍要处理MCU转发的CAN数据2020-11-11暂适用进业智车007项目
                if (Model().getCanSwitch().getVal()) {
                    McuManager.getModel().unbindListener(mMcuListener);
                }
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean needMCU() {
        boolean ret = false;
        if (null != mCanBusLogic) {
            Packet packet = mCanBusLogic.getInfo();
            if (null != packet) {
                final String implementation = packet.getString("Implementation");
                if (CanBusDefine.Implementation.MCU.equals(implementation)) {
                    ret = true;
                }
            }
        }
        LogUtils.d(TAG, "needMCU()---ret="+ret);
        return ret;
    }

    @Override
    public void onSDKConnected(IInterface driver) {
        LogUtils.d(TAG, "onSDKConnected()---");
        if (driver instanceof ICanbusDriver) {
            mCanbusAIDLDriver = (ICanbusDriver) driver;
            mCanbusCallback = new CanbusCallBack();
            try {
                mCanbusAIDLDriver.registerCallback(mCanbusCallback);
                if (needMCU()) {
                    McuManager.getModel().bindListener(mMcuListener);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            initDataToSDK(true);
        }
    }

    private static Thread mThread = null;
    private void initDataToSDK(boolean init) {
        if (mHostObject != null) {
            if (init) {
                if (mThread == null) {
                    mThread = new Thread() {
                        public void run() {
                            try {
                                if (mCanbusAIDLDriver != null) {
                                    LogUtils.d(TAG, "initDataToSDK   mHostObject.size=" + mHostObject.size());
                                    for (int i = 0; i < mHostObject.size(); i++) {
                                        mCanbusAIDLDriver.sendCanbusCmdToSDK(CanBusDefine.MODULE, mHostObject.get(i).getHostId(), mHostObject.get(i).getHostBundle());
                                    }

                                    LogUtils.d(TAG, "initDataToSDK   camera=" + EventInputManager.getCamera() + ", language=" + LanguageDriver.Driver().get());
                                    packHostInfo(CanBusDefine.HostInfo.BACKCAMMER_STATE.value(), EventInputManager.getCamera() ? 1 : 0);
                                    packHostInfo(CanBusDefine.HostInfo.CURRENT_SOURCE.value(), SourceManager.getCurBackSource());
                                    packCarSettingInfo(CarSettingsDefine.Spinner.LANGUAGE.value(), getLanuage(LanguageDriver.Driver().get()));
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } finally {
                                mThread = null;
                            }
                        }
                    };
                    mThread.start();
                }
            } else {
                mHostObject.clear();
                mHostObject = null;
            }
        }

        LogUtils.d(TAG, "initDataToSDK end  camera=" + EventInputManager.getCamera() + ", init=" + init);
    }

    @Override
    public void onSDKDisconnected(IInterface driver) {
        initDataToSDK(false);
        if (null != mCanbusAIDLDriver) {
            try {
                mCanbusAIDLDriver.unregisterCallback(mCanbusCallback);
                McuManager.getModel().unbindListener(mMcuListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCanbusCallback = null;
        mCanbusAIDLDriver = null;
    }

    @Override
    public String filePath() {
        LogUtils.d(TAG, "filePath()---");
        return "CanBusDataConfig.ini";
    }

    @Override
    public boolean readData() {
        LogUtils.d(TAG, "readData()---");
        boolean ret = false;
        if (mMemory != null) {
            Object object = mMemory.get("CANBUS", "CanSwitch");
            if (null != object) {
                String string = (String) object;
                if (!TextUtils.isEmpty(string)) {
                    Model().getCanSwitch().setVal(Boolean.valueOf(string));
                    LogUtils.d(TAG, " CanSwitch:" + string + "|" + Boolean.valueOf(string) + "|" + Model().getCanSwitch().getVal());
                    ret = true;
                }
            }
            Object objectImplementation = mMemory.get("CANBUS", "Implementation");
            if (null != objectImplementation) {
                String implementation = (String) objectImplementation;
                if (!TextUtils.isEmpty(implementation)) {
                    Model().getImplementation().setVal(implementation);
                    LogUtils.d(TAG, " implementation:" + implementation);
                    ret = true;
                }
            }
            Object SerialPort = mMemory.get("CANBUS", "SerialPort");
            if (null != SerialPort) {
                String serialPort = (String) SerialPort;
                if (!TextUtils.isEmpty(serialPort)) {
                    Model().getCanBusSerialPort().setVal(serialPort);
                    LogUtils.d(TAG, " serialPort:" + serialPort);
                    ret = true;
                }
            }
            Object canSeriesObject = mMemory.get("CANBUS", "CanSeries");
            if (null != canSeriesObject) {
                int canSeries = Integer.valueOf((String) canSeriesObject);
                Model().getCanSeries().setVal(canSeries);
                LogUtils.d(TAG, " canSeries:" + CanBusDefine.CanSeries.toString(canSeries));
                ret = true;
            }
            Object canSeriesInfo = mMemory.get("CANBUS", "CanSeriesInfo");
            if (null != canSeriesInfo) {
                strCanSerises = (String) canSeriesInfo;
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean writeData() {

        LogUtils.d(TAG, "writeData()---CanSeries="+Model().getCanSeries().getVal());
        boolean ret = false;
        if (null != mMemory) {
            final boolean canSwitch = Model().getCanSwitch().getVal();
            mMemory.set("CANBUS", "CanSwitch", canSwitch+"");
            final String implementation = Model().getImplementation().getVal();//
            if (!TextUtils.isEmpty(implementation)) {
                mMemory.set("CANBUS", "Implementation", implementation);
                ret = true;
            }
            final String serialPort = Model().getCanBusSerialPort().getVal();//
            if (!TextUtils.isEmpty(serialPort)) {
                mMemory.set("CANBUS", "SerialPort", serialPort);
                ret = true;
            }
            mMemory.set("CANBUS", "CanSeries", Model().getCanSeries().getVal()+"");

            mMemory.set("CANBUS", "CanSeriesInfo", strCanSerises);
        } else {
            LogUtils.e(TAG, "writeData null == mMemory");
        }
        LogUtils.d(TAG, "writeData ret:" + ret);
        return ret;
    }

    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {
        @Override
        public void DataListener(byte[] val) {
            if (val != null && val.length > 0) {
                byte cmd = (byte) (val[0]&0xFF);
                if (cmd == (byte) McuDefine.MCU_TO_ARM.MRPT_CanbusData) {
                    LogUtils.d("BaseSDKMemoryDriver", "mMcuListener--="+cmd);
                    recvData(val);
                }
            }
        }
    };

    public void recvData(byte[] val) {
        // 解析MCU透传的CAN数据
        int len = val.length-1;
        byte[] can = new byte[len];
        System.arraycopy(val, 1, can, 0, len);

        //CANBUSSDK
        if (null != mCanbusAIDLDriver) {
            try {
                if (Model().getCanBusConnected().getVal() == false) {
                    Model().getCanBusConnected().setVal(true);
                }
                mCanbusAIDLDriver.canbusDataFromMCU(can);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            LogUtils.e("CanBusdriver", "mAIDLDriver==null");
        }
        //CANMCUSDK 锐派　
        if (null != iCanMcuDriver) {
            try {
                iCanMcuDriver.sendDataToMcu(can);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (mCanbusAIDLDriver == null && iCanMcuDriver == null) {//都不存在时，由Main解析，暂适用进业智车007项目
            parseCanbusData(can);
        }
    }

    static int speedLimit = -1;
    public static int getSpeedLimit() {
        return speedLimit;
    }

    private void parseCanbusData(byte[] data) {
        if (data.length == 12) {
            if (data[0] == 0x0C &&
                    data[1] == (byte) 0xFE &&
                    data[2] == 0x6C &&
                    data[3] == 0x17) {//帧ID：0C FE 6C 17
                int speed = data[11] & 0xFF + (data[10] & 0xFF) * 256 +
                        (data[9] & 0xFF) * 256 * 256 + (data[8] & 0xFF) * 256 * 256 * 256;
                int limit = (speed > 5) ? 1 : 0;
                if (speedLimit != limit) {
                    speedLimit = limit;
                    Packet packet = new Packet();
                    BaseModel mCommonDriverModel = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getModel();
                    Boolean brakeSwitch = mCommonDriverModel.getInfo().getBoolean(SettingsDefine.Common.Switch.BRAKE_WARNING.value());
                    Boolean isBrake = EventInputManager.getBrake();
                    LogUtils.d(TAG, "EventInputListener brakeSwitch:" + brakeSwitch + ", isBrake=" + isBrake + ", speedLimit:" + speedLimit);
                    packet.putBoolean("ShowBrakeWarning", ((!isBrake) && brakeSwitch));
                    BaseLogic logic = ModuleManager.getLogicByName(VideoDefine.MODULE);
                    if (null != logic) {
                        logic.Notify(VideoInterface.MAIN_TO_APK.SHOW_BRAKE_WARNING, packet);
                    }
                }
            }
        }
    }

    public class HostInfoObject {

        private int mId;
        private Bundle mBundle;

        public HostInfoObject(int id, Bundle bundle) {
            mId = id;
            mBundle = bundle;
        }

        public int getHostId() {
            return mId;
        }

        public Bundle getHostBundle() {
            return mBundle;
        }
    }

    public static boolean getPanoramicView() {
        return panoramicView;
    }

    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG,"CurBackSourceListener " +" oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            if (mAjxPhone) {
                mAjxPhone = false;
                byte[] data = new byte[1];
                data[0] = (byte) 0;
                LogUtils.d(TAG, "setSoundChannelToMcu 11 :0" );
                McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.OP_CarChannel, data, 1);
            }
        }
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "CurSourceListener " + " oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            BaseLogic curLogic = ModuleManager.getLogicBySource(newVal);
            if (curLogic != null) {
                LogUtils.d(TAG, "CurSourceListener   mAjxPhone=" + mAjxPhone+", issource="+curLogic.isSource());
                if (curLogic.isSource()) {
                    if (mAjxPhone) {
                        mAjxPhone = false;
                        byte[] data = new byte[1];
                        data[0] = (byte) 0;
                        LogUtils.d(TAG, "setSoundChannelToMcu 22 :0" );
                        McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.OP_CarChannel, data, 1);
                    }
                }
            }
        }
    };

    AudioListener mAudioListener = new AudioListener() {
        @Override
        public void ArmAudioActivieListener(Boolean oldVal, Boolean newVal) {
            if (newVal) {
                // ARM声音开始
                if (mAjxPhone) {
                    mAjxPhone = false;
                    byte[] data = new byte[1];
                    data[0] = (byte) 0;
                    LogUtils.d(TAG, "setSoundChannelToMcu 33 :0" );
                    McuManager.sendMcuImportant((byte)McuDefine.ARM_TO_MCU.OP_CarChannel, data, 1);
                }
            } else {
                // ARM声音结束
            }
        }
    };

    SettingsListener mSettingsListener = new SettingsListener() {
        @Override
        public void ImportStatusListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "ImportStatusListener val="+newVal);
            reloadConfig();
        }
    };

    private void packHostInfo(String key, int value) {
        Bundle bundle = new Bundle();
        bundle.putString(CanBusDefine.Parameter.PARAMETER_INT.value(), key);
        bundle.putInt(key, value);
        sendHostInfoToCan(key, CanBusDefine.PARAMETER_SPINNER, bundle);
    }

    private void packCarSettingInfo(String key, int value) {
        Bundle bundle = new Bundle();
        bundle.putString(CanBusDefine.Parameter.PARAMETER_INT.value(), key);
        bundle.putInt(key, value);
        sendHostInfoToCan(key, CanBusDefine.PARAMETER_SPINNER, bundle);
    }

    public int getLanuage(String lan) {
        int ret = 1;
        if (lan.equals(Define.Language.zh_CN.name())) {
            ret = 0;
        } else if (lan.equals(Define.Language.en_US.name())) {
            ret = 1;
        }
        LogUtils.d(TAG, "getLanuage----lan="+lan+", ret="+ret+", zh_CN="+Define.Language.zh_CN);
        return ret;
    }

    //CanMcu凌飞
    public boolean connectCanMcuSDK(Context context) {
        boolean ret = false;
        Bundle bundle = new Bundle();
        bundle.putString("SerialPort", SystemProperties.get("ro.uart_canbus", "/dev/ttyMT2"));
        bundle.putBoolean("Mcu_data", needMCU());

        Intent intent = new Intent(CanMcuDefine.CANMCU_SERVICE_NAME);
        ComponentName component = new ComponentName(CanMcuDefine.CANMCU_SERVICE_PACKET_NAME, CanMcuDefine.CANMCU_SERVICE_CLASS_NAME);
        intent.setComponent(component);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.bindService(intent, mCanMcuConnection, Context.BIND_AUTO_CREATE);
        return ret;
    }

    private void disconnectCanMcuSDK() {
        if (mCanMcuConnection != null) {
            try {
                mCanMcuConnection.onServiceDisconnected(null);
                if (iCanMcuDriver != null) {
                    getMainContext().unbindService(mCanMcuConnection);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class canMcuCallback extends ICanMcuCallback.Stub {
        @Override
        public void carSteerProcess(int i) throws RemoteException {
        }

        @Override
        public void mediaControl(int i) throws RemoteException {
        }

        @Override
        public void notifyBacklightMode(int i) throws RemoteException {
        }

        @Override
        public void nofityMcuData(byte[] bytes) throws RemoteException {

        }

        @Override
        public void commonCallback(int i, Bundle bundle) throws RemoteException {

        }

        @Override
        public boolean canMcuDataCallBack(String s, int i, Bundle bundle) throws RemoteException {
            return false;
        }
    }
    public ServiceConnection mCanMcuConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.i(TAG, "onServiceConnected ");
            // 获取远程Service的onBind方法返回的对象的代理
            iCanMcuDriver = ICanMcuDriver.Stub.asInterface(service);
            if (iCanMcuDriver == null) {
                LogUtils.e(TAG, "iCanMcuDriver == null");
                return;
            }

            iCanMcuCallback = new canMcuCallback();
            if (iCanMcuCallback != null) {
                try {
                    iCanMcuDriver.registerCallback(iCanMcuCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                LogUtils.e(TAG, "iCanMcuCallback == null");
            }

            if (needMCU()) {
                McuManager.getModel().bindListener(mMcuListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (needMCU()) {
                    McuManager.getModel().unbindListener(mMcuListener);
                }

                if (iCanMcuDriver != null && iCanMcuCallback != null) {
                    iCanMcuDriver.unregisterCallback(iCanMcuCallback);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            iCanMcuDriver = null;
        }
    };

}
