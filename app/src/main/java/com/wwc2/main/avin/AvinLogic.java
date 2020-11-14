package com.wwc2.main.avin;

import android.os.Handler;
import android.os.Message;

import com.wwc2.aux_interface.AuxDefine;
import com.wwc2.avin_interface.AvinInterface;
import com.wwc2.avin_interface.Provider;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.common.CommonListener;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.eventinput.EventInputListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.provider.LogicProviderHelper;
import com.wwc2.settings_interface.SettingsDefine;

/**
 * the AVIN logic, it's abstract class.
 *
 * @author wwc2
 * @date 2017/1/5
 */
public abstract class AvinLogic extends BaseLogic {

    /**
     * TAG
     */
    private static final String TAG = "AvinLogic";

    /**
     * 通用驱动Model
     */
    private BaseModel mCommonDriverModel = null;

    protected boolean mRightCamera = false;
    protected boolean mRightCameraBefore = false;
    protected boolean auxBefore = true;
    protected boolean mFrontCamera = false;

    protected boolean mCameraBefore = false;

    /**
     * ContentProvider.
     */
    static {
//        LogicProviderHelper.Provider(Provider.CAMERA(), "" + false);
        LogicProviderHelper.Provider(Provider.CAMERA_MIRROR(), "" + false);
        LogicProviderHelper.Provider(Provider.CAMERA_GUIDE_LINE(), "" + false);
    }

    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    removeMessages(1);
                    Packet packet = new Packet();
                    packet.putBoolean("rightCamera", true);
                    packet.putBoolean("mcudata", true);
                    ModuleManager.getLogicByName(AuxDefine.MODULE).dispatch(AvinInterface.APK_TO_MAIN.RIGHT_CAMERA, packet);
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 事件输入监听器
     */
    private EventInputListener mEventInputListener = new EventInputListener() {
        @Override
        public void BrakeListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("ShowBrakeWarning", !newVal && getRealtimeBrakeSwitch());
            Notify(AvinInterface.MAIN_TO_APK.SHOW_BARAKE_WARNING, packet);
        }

        //此处没有用到，而且会影响倒车的处理速度。外部APK获取倒车状态可以调用LogicProvider接口。2018-03-30
        @Override
        public void CameraListener(Boolean oldVal, Boolean newVal) {
            // 保存状态
//            LogicProviderHelper.getInstance().update(com.wwc2.avin_interface.Provider.CAMERA(), "" + newVal);
            LogUtils.d("AuxLogic---CameraListener---" + newVal + ", mCameraBefore=" + mCameraBefore);
            if (!newVal) {//先倒车再打右转向，退出倒车时进入右视
                if (mCameraBefore) {
                    mHandler.sendEmptyMessageDelayed(1, 1000);//延时1s再切右视
                }
            } else {
                mHandler.removeMessages(1);
            }
            mCameraBefore = false;
        }
    };

    /**
     * 设置监听器
     */
    private CommonListener mCommonListener = new CommonListener() {
        @Override
        public void BrakeWarningListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            packet.putBoolean("ShowBrakeWarning", !getRealtimeBrake() && newValue);
            Notify(AvinInterface.MAIN_TO_APK.SHOW_BARAKE_WARNING, packet);
        }

        @Override
        public void ReverseImageListener(Boolean oldValue, Boolean newValue) {
            // 保存状态
            LogUtils.d(TAG, "reversingGuideLineListener:" + newValue);
            LogicProviderHelper.getInstance().update(com.wwc2.avin_interface.Provider.CAMERA_MIRROR(), "" + newValue);
        }

        @Override
        public void reversingGuideLineListener(Boolean oldValue, Boolean newValue) {
            LogUtils.d(TAG, "reversingGuideLineListener:" + newValue);
            LogicProviderHelper.getInstance().update(com.wwc2.avin_interface.Provider.CAMERA_GUIDE_LINE(), "" + newValue);

        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }

        mCommonDriverModel = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getModel();
        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().bindListener(mEventInputListener);
        if (null != mCommonDriverModel) {
            mCommonDriverModel.bindListener(mCommonListener);
        }

//        LogicProviderHelper.getInstance().update(com.wwc2.avin_interface.Provider.CAMERA(),
//                "" + ModuleManager.getLogicByName(EventInputDefine.MODULE).getInfo().getBoolean("Camera"));
        LogicProviderHelper.getInstance().update(com.wwc2.avin_interface.Provider.CAMERA_MIRROR(),
                "" + DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getInfo().getBoolean(SettingsDefine.Common.Switch.REVERSE_IMAGE.value()));
        LogicProviderHelper.getInstance().update(com.wwc2.avin_interface.Provider.CAMERA_GUIDE_LINE(),
                "" + DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getInfo().getBoolean(SettingsDefine.Common.Switch.REVERSE_GUIDE_LINE.value()));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (null != mCommonDriverModel) {
            mCommonDriverModel.unbindListener(mCommonListener);
        }
        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().unbindListener(mEventInputListener);

        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public Packet getInfo() {
        Packet ret = super.getInfo();
        if (null == ret) {
            ret = new Packet();
        }
        ret.putBoolean("ShowBrakeWarning", !getRealtimeBrake() && getRealtimeBrakeSwitch());
        ret.putBoolean("Camera", EventInputManager.getCamera());
        ret.putBoolean("Acc", EventInputManager.getAcc());
        ret.putBoolean("auxBefore", auxBefore);
        ret.putBoolean("rightCamera", mRightCamera);
        ret.putBoolean("frontCamera", mFrontCamera);

        LogUtils.d(TAG, "getInfo----auxBefore=" + auxBefore);

        try {
            ret.putInt("aux_video_type", FactoryDriver.Driver().getAuxVideoType());
            ret.putInt("right_video_type", FactoryDriver.Driver().getRightVideoType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**enter*/
    protected void enter() {
        LogUtils.d(TAG, getTypeName() + " enter start.");
        Notify(AvinInterface.MAIN_TO_APK.ENTER, null);
        LogUtils.d(TAG, getTypeName() + " enter over.");
    }

    /**leave*/
    protected void leave() {
        LogUtils.d(TAG, getTypeName() + " leave start.");
        Notify(AvinInterface.MAIN_TO_APK.LEAVE, null);
        LogUtils.d(TAG, getTypeName() + " leave over.");
    }

    /**
     * 获取实时刹车线状态
     */
    private boolean getRealtimeBrake() {
        return EventInputManager.getBrake();
    }

    /**
     * 获取实时刹车开关状态
     */
    private boolean getRealtimeBrakeSwitch() {
        boolean ret = false;
        if (null != mCommonDriverModel) {
            ret = mCommonDriverModel.getInfo().getBoolean(SettingsDefine.Common.Switch.BRAKE_WARNING.value());
        }
        return ret;
    }
}
