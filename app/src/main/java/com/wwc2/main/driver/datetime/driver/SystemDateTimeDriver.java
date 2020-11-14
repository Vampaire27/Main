package com.wwc2.main.driver.datetime.driver;

import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.systempermission_interface.SystemPermissionInterface;

/**
 * the date time driver.
 *
 * @author wwc2
 * @date 2017/1/19
 */
public class SystemDateTimeDriver extends BaseDateTimeDriver {

    private CoreLogic mSystemPermissionLogic = null;

    /**the power on listener.*/
    PowerManager.PowerListener mPoweronListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            if ( PowerManager.PowerStep.isPoweronCreateOvered(newVal)) {
                CoreLogic logic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
                if (logic != null) {
                    BaseModel model = getModel();
                    if (model instanceof DateTimeModel) {
                        Packet smart = new Packet();
                        smart.putString(Define.Time.RESOURCE, Define.Time.SRC_SMART);
                        logic.Notify(SystemPermissionInterface.MAIN_TO_APK.TIME_SET, smart);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        PowerManager.getModel().bindListener(mPoweronListener);

        mSystemPermissionLogic = LogicManager.getLogicByName(com.wwc2.systempermission_interface.SystemPermissionDefine.MODULE);
    }

    @Override
    public void onDestroy() {
        PowerManager.getModel().unbindListener(mPoweronListener);

        super.onDestroy();
    }

    @Override
    public boolean setTimeSource(String src, int year, int month, int day, int hour, int minute, int second) {
        boolean ret = false;
        if (!TextUtils.isEmpty(src)) {
            CoreLogic logic = mSystemPermissionLogic;
            if (logic != null) {
                Packet packet = new Packet();
                packet.putString(Define.Time.RESOURCE, src);
                packet.putInt(Define.Time.YEAR, year);
                packet.putInt(Define.Time.MONTH, month);
                packet.putInt(Define.Time.DAY, day);
                packet.putInt(Define.Time.HOURE, hour);
                packet.putInt(Define.Time.MUNITE, minute);
                packet.putInt(Define.Time.SECOND, second);
                logic.Notify(SystemPermissionInterface.MAIN_TO_APK.TIME_SET, packet);

                super.setTimeSource(src, year, month, day, hour, minute, second);
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean setTimeFormat(String format) {
        boolean ret = false;
        if ((format != null) && (format.equals(Define.Time.FORMAT_24)|| format.equals(Define.Time.FORMAT_12))) {
            CoreLogic logic = mSystemPermissionLogic;
            if (logic != null) {
                Packet packet = new Packet();
                packet.putString(Define.Time.FORMAT, format);
                logic.Notify(SystemPermissionInterface.MAIN_TO_APK.TIME_FORMAT, packet);

                super.setTimeFormat(format);
                ret = true;
            }
        }
        return ret;
    }
}
