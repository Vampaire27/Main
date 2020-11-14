package com.wwc2.main.driver.backlight.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.driver.backlight.BacklightDriverable;
import com.wwc2.main.driver.backlight.BacklightListener;
import com.wwc2.main.driver.client.driver.BaseClientDriver;
import com.wwc2.main.driver.common.driver.BaseCommonDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.driver.storage.StorageDriver;
import com.wwc2.main.driver.storage.StorageListener;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.settings_interface.SettingsDefine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * the back light base.
 *
 * @author wwc2
 * @date 2017/1/14
 */
public abstract class BaseBacklightDriver extends BaseMemoryDriver implements BacklightDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseBacklightDriver";

    /**
     * 临时调节禁用滑动调节
     */
    private static final boolean DISABLE = false;

    public static final String BRIGHTNESS_STATUS = "brightness_status";
    /**
     * 白天模式
     */
    public static final int DAY = 1;
    /**
     * 黑夜模式
     */
    public static final int NIGHT = 2;

    private static final String ACTION_OPEN_CLOSE_LCD = "com.wwc2.lcd";
    private static final String LCD_STATE = "lcd.state";

    private static final String ACTION_CHANGE_BRIGHT_MODE = "com.wwc2.bright.mode";
    private static final String MODE_STATE = "mode.state";//1:白天; 2:黑夜

    private static final String ACTION_SCREEN_OFF = "com.wwc2.screen.off";//关屏

    static int mDayBeforeILL = 0;//bug11683屏幕亮度夜间模式-关ACC，开ACC模式默认白天模式

    /**
     * 存储设备监听器
     */
    private StorageListener mStorageListener = new StorageListener() {
        @Override
        public void StorageInfoListener(IntegerSSBoolean oldVal, IntegerSSBoolean newVal) {
            if (SourceManager.getCurSource() != Define.Source.SOURCE_POWEROFF &&
                    SourceManager.getCurSource() != Define.Source.SOURCE_ACCOFF) {//在待机或ACC OFF状态下，拔插设备不应该开屏。
                open();
            }
        }
    };

    /**
     * 背光数据Model
     */
    protected static class BacklightModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putBoolean("BacklightOpenOrClose", mBacklightOpenOrClose.getVal());
            packet.putBoolean(SettingsDefine.Brightness.AOTUADJUST, mBacklightAutoSwitch.getVal());
            packet.putBoolean(SettingsDefine.Brightness.ENABLE, mBacklightAdjustEnable.getVal());
            packet.putInt(SettingsDefine.Brightness.BRIGHTNESS_MODE, mBacklightMode.getVal());
            if (mBacklightMode.getVal() == NIGHT) {
                packet.putInt(SettingsDefine.Brightness.BRIGHTNESS, mBacklightValueNight.getVal());
            } else {
                packet.putInt(SettingsDefine.Brightness.BRIGHTNESS, mBacklightValueDay.getVal());
            }
            packet.putInt(SettingsDefine.Brightness.BRIGHTNESS_DAY, mBacklightValueDay.getVal());
            packet.putInt(SettingsDefine.Brightness.BRIGHTNESS_NIGHT, mBacklightValueNight.getVal());
            return packet;
        }

        /**
         * 获取背光开关状态
         */
        private MBoolean mBacklightOpenOrClose = new MBoolean(this, "BacklightOpenOrCloseListener", false/*true*/);

        public MBoolean getBacklightOpenOrClose() {
            return mBacklightOpenOrClose;
        }

        /**
         * 背光自动开关
         */
        private MBoolean mBacklightAutoSwitch = new MBoolean(this, "BacklightAutoSwitchListener", false);

        public MBoolean getBacklightAutoSwitch() {
            return mBacklightAutoSwitch;
        }

        /**
         * 背光值
         */
        private MInteger mBacklightValue = new MInteger(this, "BacklightValueListener", 90);

        public MInteger getBacklightValue() {
            return mBacklightValue;
        }

        private MInteger mBacklightValueDay = new MInteger(this, "BacklightValueDayListener", 90);

        public MInteger getBacklightValueDay() {
            return mBacklightValueDay;
        }

        private MInteger mBacklightValueNight = new MInteger(this, "BacklightValueNightListener", 20);

        public MInteger getBacklightValueNight() {
            return mBacklightValueNight;
        }

        private MInteger mBacklightMode = new MInteger(this, "BacklightModeListener", 1);

        public MInteger getBacklightMode() {
            return mBacklightMode;
        }


        /**
         * 背光是否允许调节
         */
        private MBoolean mBacklightAdjustEnable = new MBoolean(this, "BacklightAdjustEnableListener", true);

        public MBoolean getBacklightAdjustEnable() {
            return mBacklightAdjustEnable;
        }
    }

    @Override
    public BaseModel newModel() {
        return new BacklightModel();
    }

    /**
     * open or close the back light.
     */
    protected abstract boolean open(final boolean open);

    /**
     * set auto status.
     */
    protected abstract boolean auto(boolean auto);

    /**
     * get the model object.
     */
    protected BacklightModel Model() {
        BacklightModel ret = null;
        BaseModel model = getModel();
        if (model instanceof BacklightModel) {
            ret = (BacklightModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        Model().bindListener(mBacklightListner);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OPEN_CLOSE_LCD);
        filter.addAction(ACTION_CHANGE_BRIGHT_MODE);
        filter.addAction(ACTION_SCREEN_OFF);
        getMainContext().registerReceiver(mIntentReceiver, filter);
        // init data
        boolean ill = false;
        Packet packet1 = ModuleManager.getLogicByName(EventInputDefine.MODULE).getInfo();
        if (null != packet1) {
            ill = packet1.getBoolean("Ill");
            Model().getBacklightAdjustEnable().setVal(!ill);
        }

        if (ill) {
            setIllState(ill);
        } else {
            if (Model().getBacklightMode().getVal() == NIGHT) {
                setBacklightNight();
            } else {
                setBacklightDay();
            }
        }

        open();

        // bind
        DriverManager.getDriverByName(StorageDriver.DRIVER_NAME).getModel().bindListener(mStorageListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);

        getMainContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);
    }

    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            //
            try {
                int backlight = Settings.System.getInt(getMainContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                BigDecimal bigDecimal = new BigDecimal((backlight*100)/255.0);
                if (bigDecimal != null) {
                    backlight = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                } else {
                    backlight = (int) ((backlight*100)/255.0);
                }
                LogUtils.d(TAG, "backlight="+backlight+
                    ", day="+Model().getBacklightValueDay().getVal()+
                    ", night="+Model().getBacklightValueNight().getVal()+
                    ", mode="+Model().getBacklightMode().getVal());
                if (Model().getBacklightMode().getVal() == DAY) {
                    if (backlight != Model().getBacklightValueDay().getVal()) {
                        Model().getBacklightValueDay().setVal(backlight);
                    }
                } else if (Model().getBacklightMode().getVal() == NIGHT) {
                    if (backlight != Model().getBacklightValueNight().getVal()) {
                        Model().getBacklightValueNight().setVal(backlight);
                    }
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public void onDestroy() {
        // unbind
        try {
            getMainContext().unregisterReceiver(mIntentReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Model().unbindListener(mBacklightListner);
        DriverManager.getDriverByName(StorageDriver.DRIVER_NAME).getModel().unbindListener(mStorageListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "BacklightDataConfig.ini";
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            int value = Model().getBacklightValue().getVal();
            mMemory.set("BACKLIGHT", "BacklightValue", value+"");

            mMemory.set("BACKLIGHT", "BacklightValueDay", Model().getBacklightValueDay().getVal()+"");
            mMemory.set("BACKLIGHT", "BacklightValueNight", Model().getBacklightValueNight().getVal()+"");
            mMemory.set("BACKLIGHT", "BacklightMode", Model().getBacklightMode().getVal()+"");

            boolean auto = Model().getBacklightAutoSwitch().getVal();
            mMemory.set("BACKLIGHT", "BacklightAutoSwitch", auto+"");

            ret = true;
        }
        return ret;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            try {
                Object object = null;
                object = mMemory.get("BACKLIGHT", "BacklightValue");
                if (null != object) {
                    int value = Integer.parseInt((String) object);
                    Model().getBacklightValue().setVal(value);
                    ret = true;
                }

                object = mMemory.get("BACKLIGHT", "BacklightValueDay");
                if (null != object) {
                    int value = Integer.parseInt((String) object);
                    Model().getBacklightValueDay().setVal(value);
                    ret = true;
                }
                object = mMemory.get("BACKLIGHT", "BacklightValueNight");
                if (null != object) {
                    int value = Integer.parseInt((String) object);
                    Model().getBacklightValueNight().setVal(value);
                    ret = true;
                }
                object = mMemory.get("BACKLIGHT", "BacklightMode");
                if (null != object) {
                    //威益德：状态栏背光调节成黑夜模式，不要做掉B+记忆，要回到默认状态。2018-10-18
                    int value = DAY;//Integer.parseInt((String) object);
                    Model().getBacklightMode().setVal(value);
                    ret = true;
                }

                object = mMemory.get("BACKLIGHT", "BacklightAutoSwitch");
                if (null != object) {
                    boolean auto = Boolean.parseBoolean((String) object);
                    Model().getBacklightAutoSwitch().setVal(auto);
                    ret = true;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public boolean open() {
        boolean ret = false;
        Model().getBacklightOpenOrClose().setVal(true);
        //开背光时先处理大灯状态
        setIllState(EventInputManager.getIll());
        return ret;
    }

    @Override
    public boolean close() {
        boolean ret = false;
        Model().getBacklightOpenOrClose().setVal(false);
        return ret;
    }

    @Override
    public boolean setAuto(boolean auto) {
        boolean ret = false;
        if (auto(auto)) {
            ret = true;
            Model().getBacklightAutoSwitch().setVal(auto);
        }
        return ret;
    }

    /**
     * 设置背光亮度 YDG 2017－11－27
     */
    @Override
    public void setBacklightness(int value, int type) {
        LogUtils.d(TAG, "setBacklightness----value="+value+", type="+type);
        switch (type) {
            case 0:
                setBacklightToSystem(value);
                Model().getBacklightValue().setVal(value);
                if (Model().getBacklightMode().getVal() == DAY) {
                    //
                    Model().getBacklightValueDay().setVal(value);
                } else if (Model().getBacklightMode().getVal() == NIGHT) {
                    //
                    Model().getBacklightValueNight().setVal(value);
                }
                break;
            case 1:
                if (Model().getBacklightMode().getVal() == DAY) {
                    setBacklightToSystem(value);
                    Model().getBacklightValue().setVal(value);
                }
                //
                Model().getBacklightValueDay().setVal(value);
                break;
            case 2:
                if (Model().getBacklightMode().getVal() == NIGHT) {
                    setBacklightToSystem(value);
                    Model().getBacklightValue().setVal(value);
                }
                //
                LogUtils.d(TAG, "setBacklightness 2 --old="+Model().getBacklightValueNight().getVal()+", new="+value);
                Model().getBacklightValueNight().setVal(value);
                break;
        }
    }

    @Override
    public int getBacklightness() {
        return Model().getBacklightValue().getVal();
    }

    @Override
    public void changeBacklightMode() {
        int mode = DAY;
        int backlight = Model().getBacklightValueDay().getVal();
        if (Model().getBacklightMode().getVal() == DAY) {
            mode = NIGHT;
            backlight = Model().getBacklightValueNight().getVal();
        }
        Model().getBacklightMode().setVal(mode);
        Model().getBacklightValue().setVal(backlight);
    }

    @Override
    public void setIllState(boolean on) {
        LogUtils.e(TAG, "setIllState---" + Model().getBacklightOpenOrClose().getVal() +
                ", mDayBeforeILL=" + mDayBeforeILL + ", on=" + on);
        if (Model().getBacklightOpenOrClose().getVal() == false) {
            LogUtils.e(TAG, "setIllState---return when close screen!");
            if (on) {
                if (mDayBeforeILL == 0) {
                    mDayBeforeILL = Model().getBacklightMode().getVal();
                }
            }
            return;
        }
        if (on) {
            if (mDayBeforeILL == 0) {
                mDayBeforeILL = Model().getBacklightMode().getVal();
            }
            setBacklightNight();
        } else {
            if (mDayBeforeILL == DAY) {
                setBacklightDay();
            } else if (Model().getBacklightMode().getVal() == NIGHT) {
                setBacklightNight();
            }
            mDayBeforeILL = 0;
        }
    }

    @Override
    public boolean setBacklightNight() {
        boolean ret = false;
        if (Model().getBacklightMode().getVal() != NIGHT) {
            Model().getBacklightMode().setVal(NIGHT);
        }
        Settings.System.putInt(getMainContext().getContentResolver(),
                BRIGHTNESS_STATUS, NIGHT);

        setBacklightToSystem(Model().getBacklightValueNight().getVal());
        LogUtils.d(TAG, "setBacklightNight----value="+Model().getBacklightValueNight().getVal());
        return ret;
    }

    @Override
    public boolean setBacklightDay() {
        boolean ret = false;
        if (Model().getBacklightMode().getVal() != DAY) {
            Model().getBacklightMode().setVal(DAY);
        }
        Settings.System.putInt(getMainContext().getContentResolver(),
                BRIGHTNESS_STATUS, DAY);

        setBacklightToSystem(Model().getBacklightValueDay().getVal());
        return ret;
    }

    private void setBacklightToSystem(int value) {
        int value1 = value*255/100;
        BigDecimal bigDecimal = new BigDecimal((value*255)/100.0);
        if (bigDecimal != null) {
            value1 = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        } else {
            value1 = (int) ((value*255)/100.0);
        }

        LogUtils.i(TAG, "setBacklightToSystem----value=" + value);
        Settings.System.putInt(getMainContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, value1 > 255 ? 255 : value1);
    }

    /**
     * 判断背光是否打开
     */
    protected boolean getBacklightOpenOrClose() {
        return Model().getBacklightOpenOrClose().getVal();
    }

    private BacklightListener mBacklightListner = new BacklightListener() {
        @Override
        public void BacklightModeListener(Integer oldVal, Integer newVal) {
            if (newVal == DAY) {
                setBacklightDay();
            } else if (newVal == NIGHT) {
                setBacklightNight();
            }
        }
        /**背光开关状态监听器*/
        @Override
        public void BacklightOpenOrCloseListener(Boolean oldVal, Boolean newVal) {
            String client = VersionDriver.Driver().getClientProject();
//            if (client.equals(BaseClientDriver.CLIENT_AM)) {
                LogUtils.i(TAG, "BacklightOpenOrCloseListener---newVal=" + newVal);
                open(newVal);
                SystemProperties.set("lcd.state", String.valueOf(newVal ? 1 : 0));
//            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.i(TAG, "mIntentReceiver---action="+action);
            if (action.equals(ACTION_OPEN_CLOSE_LCD)) {
                boolean state = intent.getBooleanExtra(LCD_STATE, false);
                if (state) {
                    //增加长按关屏功能，通过配置文件配置。
                    if (SourceManager.getCurSource() == Define.Source.SOURCE_POWEROFF && FactoryDriver.Driver().getCloseScreen()) {
                    } else {
                        open();
                    }
                } else {
                    //去掉客户，增加语音指令的支持。2018-10-24
//                    String client = VersionDriver.Driver().getClientProject();
//                    if (client.equals(BaseClientDriver.CLIENT_AM)) {
                        close();
//                    }
                }
            } else if (action.equals(ACTION_CHANGE_BRIGHT_MODE)) {
                int mode = Model().getBacklightMode().getVal() == DAY ? NIGHT : DAY;//intent.getIntExtra(MODE_STATE, DAY);
                LogUtils.i(TAG, "ACTION_CHANGE_BRIGHT_MODE---mode="+mode+", oldmode="+Model().getBacklightMode().getVal());
                Model().getBacklightMode().setVal(mode);
                if (mode == DAY) {
                    Model().getBacklightValue().setVal(Model().getBacklightValueDay().getVal());
                    if (BaseCommonDriver.isGWVersion()) {
                        close();//国外版本增加关屏的功能2019-11-18
                    }
                } else if (mode == NIGHT) {
                    Model().getBacklightValue().setVal(Model().getBacklightValueNight().getVal());
                }
            } else if (action.equals(ACTION_SCREEN_OFF)) {//SystemUI关屏功能，原车屏项目在CanMcuSDK中接收，由MCU关。
                close();
            }
        }
    };

    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            final boolean oldCall = BluetoothDefine.HFPStatus.isCalling(oldVal);
            final boolean newCall = BluetoothDefine.HFPStatus.isCalling(newVal);

            if (oldCall && !newCall) {
//                mIsCalling = false;
            } else if (!oldCall && newCall) {
                if (SourceManager.getCurSource() != Define.Source.SOURCE_POWEROFF) {
                    open();
                }
            }
        }
    };

    public int getBacklightMode() {
        return Model().getBacklightMode().getVal();
    }
}
