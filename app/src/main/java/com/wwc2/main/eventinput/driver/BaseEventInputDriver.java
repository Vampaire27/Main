package com.wwc2.main.eventinput.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.system.SystemDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.PanoramicManager;

/**
 * the event input driver.
 *
 * @author wwc2
 * @date 2017/1/13
 */
public abstract class BaseEventInputDriver extends BaseDriver implements EventInputDriverable {

    /**
     * TAG
     */
    private static final String TAG = "BaseEventInputDriver";

    static boolean beforeCamera = false;
    protected static boolean realAccState = true;

    /**
     * the model data.
     */
    protected static class EventInputModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putBoolean("Brake", mBrake.getVal());
            packet.putBoolean("Camera", mCamera.getVal());
            packet.putBoolean("Acc", mAcc.getVal());
            packet.putBoolean("Ill", mIll.getVal());
            packet.putBoolean("LeftLight", mLeftLight.getVal());
            packet.putBoolean("RightLight", mRightLight.getVal());
            packet.putBoolean("PanoramicBeforCamera", beforeCamera);
            packet.putInt("TurnLight", mTurnLight.getVal());
            packet.putBoolean("RealAcc", realAccState);
            return packet;
        }

        /**
         * 刹车
         */
        private MBoolean mBrake = new MBoolean(this, "BrakeListener", false);

        public MBoolean getBrake() {
            return mBrake;
        }

        /**
         * 倒车
         */
        private MBoolean mCamera = new MBoolean(this, "CameraListener", false);

        public MBoolean getCamera() {
            return mCamera;
        }

        /**
         * ACC
         */
        private MBoolean mAcc = new MBoolean(this, "AccListener", false);

        public MBoolean getAcc() {
            return mAcc;
        }

        /**
         * 大灯
         */
        private MBoolean mIll = new MBoolean(this, "IllListener", false);

        public MBoolean getIll() {
            return mIll;
        }

        /**
         * 左转向灯
         */
        private MBoolean mLeftLight = new MBoolean(this, "LeftLightListener", false);

        public MBoolean getLeftLight() {
            return mLeftLight;
        }

        /**
         * 右转向灯
         */
        private MBoolean mRightLight = new MBoolean(this, "RightLightListener", false);

        public MBoolean getRightLight() {
            return mRightLight;
        }

        private MInteger mTurnLight = new MInteger(this, "TurnLightListener", 0);

        public MInteger getTurnLight() {
            return mTurnLight;
        }
    }

    @Override
    public BaseModel newModel() {
        return new EventInputModel();
    }

    /**
     * get the model object.
     */
    protected EventInputModel Model() {
        EventInputModel ret = null;
        BaseModel model = getModel();
        if (model instanceof EventInputModel) {
            ret = (EventInputModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void setBrake(boolean on) {
        Packet client = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
        if (client != null) {
            String clientProject = client.getString("ClientProject");
            if (clientProject != null && clientProject.equals("ch010_23")) {//马自达刹车警告默认关，并隐藏设置项。
                return;
            }
        }
        Model().getBrake().setVal(on);
    }

    @Override
    public void setCamera(boolean on) {
        if (PanoramicManager.getInstance().getPanoramicState()) {
            if (on) {
                beforeCamera = true;
            }
            if (!on && beforeCamera) {
                beforeCamera = false;
                PanoramicManager.getInstance().openPanoramic(false, true);
            } else {
                return;
            }
        } else {
            beforeCamera = false;
        }
        Model().getCamera().setVal(on);
    }

    @Override
    public void setAcc(boolean on, boolean any) {
        if (SystemDriver.Driver().getRebootState()) {
            LogUtils.e("setAcc return rebooting!");
            return;
        }
        if (any) {
            Model().getAcc().setValAnyway(on);
        } else {
            Model().getAcc().setVal(on);
        }
    }

    @Override
    public void setIll(boolean on, boolean init) {
        if (on == Model().getIll().getVal() && init) {//接大灯线打倒车 深度睡眠退倒车再上ACC 显示为白天模式
            EventInputManager.NotifyStatusEvent(false, EventInputDefine.Status.TYPE_ILL, on, null);
        }
        Model().getIll().setVal(on);
    }

    @Override
    public void setLeftLight(boolean open) {
        Model().getLeftLight().setVal(open);
    }

    @Override
    public void setRightLight(boolean open) {
        Model().getRightLight().setVal(open);
    }

    @Override
    public void setTurnLight(int value) {
        Model().getTurnLight().setVal(value);
    }
}
