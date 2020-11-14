package com.wwc2.main.driver.volume.driver;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.volume.VolumeDriverable;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;

/**
 * the base volume driver.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public abstract class BaseVolumeDriver extends BaseDriver implements VolumeDriverable {

    private static final String TAG = "BaseVolumeDriver";

    /**
     * the model data.
     */
    protected static class VolumeModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            if (mVolumeType != null) packet.putInt("VolumeType", mVolumeType.getVal());
            if (mVolumeValue != null) packet.putIntegerObjectArray("VolumeValue", mVolumeValue.getVal());
            if (mVolumeMute != null) packet.putBoolean("VolumeMute", mVolumeMute.getVal());
            if (mVolumeMax != null) packet.putInt("VolumeMax", mVolumeMax.getVal());
            if (mVolumeShow != null) packet.putBoolean("VolumeShow", mVolumeShow.getVal());
            return packet;
        }

        /**the volume type.*/
        private MInteger mVolumeType = new MInteger(this, "VolumeTypeListener", Define.VolumeType.DEFAULT);
        public MInteger getVolumeType() {
            return mVolumeType;
        }

        /**the volume value.*/
        private MIntegerArray mVolumeValue = new MIntegerArray(this, "VolumeValueListener", Define.VOLUME_DEFAULT_VALUES);
        public MIntegerArray getVolumeValue() {
            return mVolumeValue;
        }

        /**the volume mute.*/
        private MBoolean mVolumeMute = new MBoolean(this, "VolumeMuteListener", true);
        public MBoolean getVolumeMute() {
            return mVolumeMute;
        }

        /**the volume max.*/
        private MInteger mVolumeMax = new MInteger(this, "VolumeMaxListener", Define.VOLUME_MAX_VALUE);
        public MInteger getVolumeMax() {
            return mVolumeMax;
        }

        /**the volume show.*/
        private MBoolean mVolumeShow = new MBoolean(this, "VolumeShowListener", false);
        public MBoolean getVolumeShow() {
            return mVolumeShow;
        }
    }

    @Override
    public BaseModel newModel() {
        return new VolumeModel();
    }

    /**
     * get the model object.
     */
    protected VolumeModel Model() {
        VolumeModel ret = null;
        BaseModel model = getModel();
        if (model instanceof VolumeModel) {
            ret = (VolumeModel) model;
        }
        return ret;
    }

    /**source listener.*/
    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            BaseLogic newLogic = ModuleManager.getLogicBySource(newVal);
            if (null != newLogic) {
                if (newLogic.isVolumeHideSource()) {
                    operate(Define.VolumeOperate.HIDE);
                }
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        SourceManager.getModel().bindListener(mSourceListener);
    }

    @Override
    public void onDestroy() {
        SourceManager.getModel().unbindListener(mSourceListener);
    }

    @Override
    public boolean mute(boolean mute) {
        return true;
    }

    @Override
    public boolean set(int value) {
        boolean ret = false;
        final int max = Model().getVolumeMax().getVal();
        if (value >= 0 && value <= max) {
            ret = true;
        } else {
            LogUtils.w(TAG, "set volume value failed, value = " + value + ", min = 0, max = " + max);
            if (value > max) {
                if (Model().getVolumeMute().getVal()) {
                    mute(false);
                }
            }
        }
        return ret;
    }

    @Override
    public boolean increase(int value) {
        final int volume = getVolumeValue();
        boolean ret = set(volume + value);
        return ret;
    }

    @Override
    public boolean decrease(int value) {
        final int volume = getVolumeValue();
        boolean ret = set(volume - value);
        return ret;
    }

    @Override
    public boolean operate(int operate) {
        return true;
    }

    @Override
    public boolean adjustShow(boolean show) {
        boolean ret = false;
        if (Model().getVolumeShow().getVal() != show) {
            if (show) {
                Model().getVolumeShow().setVal(true);
            } else {
                Model().getVolumeShow().setVal(false);
            }
            ret = true;
        } else {
            LogUtils.w(TAG, "same show status, show = " + show);
        }
        return ret;
    }

    /**the the volume value.*/
    public int getVolumeValue() {
        final int type = Model().getVolumeType().getVal();
        int ret = getVolumeValue(type);
        return ret;
    }

    /**the the specify volume type value.*/
    protected int getVolumeValue(int type) {
        int ret = Define.VOLUME_DEFAULT_VALUE;
        Integer[] values = Model().getVolumeValue().getVal();
        if (null != values) {
            final int length = values.length;
            if (type >= 0 && type < length) {
                ret = values[type];
            }
        }
        return ret;
    }
}
