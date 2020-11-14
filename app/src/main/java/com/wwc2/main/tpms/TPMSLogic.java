package com.wwc2.main.tpms;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.main.tpms.driver.STM32TPMSDriver;

/**
 * the tpms logic.
 *
 * @author wwc2
 * @date 2017/1/8
 */
public class TPMSLogic extends BaseTPMSLogic {

    /**
     * TAG
     */
    private static final String TAG = "TPMSLogic";

    @Override
    public BaseDriver newDriver() {
        return new STM32TPMSDriver();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
//        switch (nId){
//            case TPMSInterface.APK_TO_MAIN.ACITVITY_QUERY:
//                String stringTag = packet.getString(TPMSDefine.STRING_TAG);
//                if(!TextUtils.isEmpty(stringTag)){
//                    TPMSDefine.StringTag st = TPMSDefine.StringTag.valueOf(stringTag);
//                    switch (st) {
//                        case ACTIVITY_MAIN:
////                            Driver().activityMain();
//                            LogUtils.d(TAG,"ACTIVITY_MAIN:");
//                            break;
//                        case ACTIVITY_SETTING:
////                            Driver().activitySetting();
//                            LogUtils.d(TAG,"ACTIVITY_SETTING:");
//                            break;
//                    }
//                }
//                break;
//        }
        return super.dispatch(nId, packet);
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        // create driver.
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onCreate(null);
        }

    }

    @Override
    public void onDestroy() {
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }
        super.onDestroy();
    }
}
