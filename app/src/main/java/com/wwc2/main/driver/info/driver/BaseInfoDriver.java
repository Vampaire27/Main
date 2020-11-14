package com.wwc2.main.driver.info.driver;

import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MString;
import com.wwc2.main.driver.info.InfoDriverable;

/**
 * the base info driver.
 *
 * @author wwc2
 * @date 2017/1/26
 */
public abstract class BaseInfoDriver extends BaseDriver implements InfoDriverable {

    /**数据Model*/
    protected class InfoModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet ret = new Packet();
            return ret;
        }

        private MInteger mSignalStrengthDbm = new MInteger(this, "", 0);//信号强度dBm
        private MInteger mSignalStrengthLevelAsu = new MInteger(this, "", 0);//信号强度asu
        private MString mNetworkTypeName = new MString(this, "", null);//网络类型 Whether EDGE, UMTS, etc...
        private MInteger mSerivceState = new MInteger(this, "", ServiceState.STATE_OUT_OF_SERVICE);//服务状态，不在服务区等
        private MInteger mDataState = new MInteger(this, "", TelephonyManager.DATA_DISCONNECTED);//移动网络状态，已断开连接
        private MString mIMEI = new MString(this, "", null);//IMEI号
        private MString mIMEISV = new MString(this, "", null);//IMEI SV号
        private MString mIPAddr = new MString(this, "", null);//IP地址，10.0.0.177 fe80::2a5b::20ff::fe80::dca2
        private MString mWifiMacAddr = new MString(this, "", null);//WLAN MAC地址，28:5b:20:80:dc:a2
        private MString mSerialNumber = new MString(this, "", null);//序列号，0123456789ABCDEF
    }

    @Override
    public BaseModel newModel() {
        return new InfoModel();
    }

    /**
     * get the model object.
     */
    protected InfoModel Model() {
        InfoModel ret = null;
        BaseModel model = getModel();
        if (model instanceof InfoModel) {
            ret = (InfoModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }
}
