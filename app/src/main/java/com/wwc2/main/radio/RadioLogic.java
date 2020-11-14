package com.wwc2.main.radio;


import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.message.MessageDefine;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.eq.EQDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.provider.LogicProviderHelper;
import com.wwc2.main.radio.driver.MCURadioDriver;
import com.wwc2.main.radio.driver.RadioDriverable;
import com.wwc2.radio_interface.Provider;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.radio_interface.RadioInterface;

import java.util.Arrays;

/**
 * the radio logic base.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class RadioLogic extends BaseLogic {
    private final String TAG = RadioLogic.class.getSimpleName();
    private Boolean mRadioSwitch = true;
    /**
     * ContentProvider.
     */
    static {
        LogicProviderHelper.Provider(Provider.RDS(), "" + false);
    }

    /**
     * the Apk Listener.
     */
    protected RadioListener mListener = new RadioListener() {
        @Override
        public void BandListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "band change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_BAND, newVal);
            Notify(RadioInterface.MainToApk.BAND, packet);
        }

        @Override
        public void PSInfoListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "PS info change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putString(RadioDefine.KeyMain.KEY_PS_INFO, newVal);
//            Notify(RadioInterface.MainToApk.BAND, packet);
        }

        /**
         * 立体声状态
         */
        public void STStateListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "ST State change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putBoolean(RadioDefine.KeyMain.KEY_ST_ENABLE, newVal);
            Notify(RadioInterface.MainToApk.RADIO_STEREO, packet);
        }
        /**
         * 立体声使能开关
         */
        public void STSwitchListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "ST Switch change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putBoolean(RadioDefine.KeyMain.KEY_STEREO_SWITCH, newVal);
            Notify(RadioInterface.MainToApk.RADIO_STEREO_SWITCH, packet);
        }

        @Override
        public void LOCEnableListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "LOC enable change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putBoolean(RadioDefine.KeyMain.KEY_LOC_ENABLE, newVal);
            Notify(RadioInterface.MainToApk.RADIO_LOC, packet);
        }

        @Override
        public void RDSEnableListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "RDS enable change, oldVal = " + oldVal + ", newVal = " + newVal);

            LogicProviderHelper.getInstance().update(Provider.RDS(), "" + newVal);
        }

        @Override
        public void FMFreqArrayListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "fm freq array change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_FM_FREQ_ARRAY,newVal);
//            Arrays.asList(newVal);
//            packet.putIntegerArrayList(RadioDefine.KeyMain.KEY_FM_FREQ_ARRAY, (ArrayList<Integer>) Arrays.asList(newVal));
            Notify(RadioInterface.MainToApk.FM_ARRAYFREQUENCE, packet);
        }

        @Override
        public void AMFreqArrayListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "am freq array change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_AM_FREQ_ARRAY,newVal);
//            Arrays.asList(newVal);
//            packet.putIntegerArrayList(RadioDefine.KeyMain.KEY_AM_FREQ_ARRAY, (ArrayList<Integer>) Arrays.asList(newVal));
            Notify(RadioInterface.MainToApk.AM_ARRAYFREQUENCE, packet);
        }

        @Override
        public void FreqListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "freq change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_FREQ, newVal);
            Notify(RadioInterface.MainToApk.FREQ, packet);
        }

        @Override
        public void WorkListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "state change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_WORK, newVal);
            Notify(RadioInterface.MainToApk.STATE, packet);
        }

        @Override
        public void SwitchEnableListener(Boolean oldVal, Boolean newVal) {
            mRadioSwitch = newVal;
            LogUtils.d(TAG, "Switch enable change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putBoolean(RadioDefine.KeyMain.KEY_RADIO_SWITCH, newVal);
            Notify(RadioInterface.MainToApk.RADIO_SWITCH, packet);
        }

        @Override
        public void FrequenceSaveOnListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "index change, oldVal = " + oldVal + ", newVal = " + newVal);
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_FREQ_SAVE_ON, newVal);
            Notify(RadioInterface.MainToApk.FREQ_SAVE_ON, packet);
        }

        @Override
        public void FMFreqRegionListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, " fm Region, oldVal = " + (oldVal == null ? "null" : Arrays.toString(newVal)));
            Packet packet = new Packet();
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_FM_REGION_ARRAY, newVal);
            Notify(RadioInterface.MainToApk.RADIO_FM_REGION, packet);
        }

        @Override
        public void AMFreqRegionListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, " am Region, oldVal = " + (oldVal == null ? "null" : Arrays.toString(newVal)));
            Packet packet = new Packet();
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_AM_REGION_ARRAY, newVal);
            Notify(RadioInterface.MainToApk.RADIO_AM_REGION, packet);
        }

        @Override
        public void FmSendSwitchListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG,"FmSendSwitchListener  newVal=" + newVal);
            Packet packet = new Packet();
            packet.putBoolean(RadioDefine.KeyMain.KEY_FMSEND_SWITCH, newVal);
            Notify(RadioInterface.MainToApk.FM_SEND_SWITCH, packet);
        }

        @Override
        public void FmSendIndexListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_FMSEND_INDEX, newVal);
            Notify(RadioInterface.MainToApk.FM_SEND_INDEX, packet);
        }

        @Override
        public void FmSendFreqArrayListener(Integer[] oldVal, Integer[] newVal) {
            Packet packet = new Packet();
            packet.putIntegerObjectArray(RadioDefine.KeyMain.KEY_FMSEND_FREQ, newVal);
            Notify(RadioInterface.MainToApk.FM_SEND_FREQ, packet);
        }

        @Override
        public void RDSTaSwitchListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean(RadioDefine.KeyMain.KEY_RDS_TA, newVal);
            Notify(RadioInterface.MainToApk.RDS_TA, packet);
        }

        @Override
        public void RDSAfSwitchListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean(RadioDefine.KeyMain.KEY_RDS_AF, newVal);
            Notify(RadioInterface.MainToApk.RDS_AF, packet);
        }

        @Override
        public void RDSStationListener(String oldVal, String newVal) {
            Packet packet = new Packet();
            packet.putString(RadioDefine.KeyMain.KEY_RDS_STATION, newVal);
            Notify(RadioInterface.MainToApk.RDS_STATION, packet);
        }

        @Override
        public void RDSStationTypeListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_RDS_STATION_PTY, newVal);
            Notify(RadioInterface.MainToApk.RDS_STATION_PTY, packet);
        }

        @Override
        public void RDSTPSwitchListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("rds_tp_enable", newVal);
            Notify(24, packet);
        }

        @Override
        public void RDSTrafficListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("rds_traffic", newVal);
            Notify(25, packet);
        }

        @Override
        public void RDSSignalListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("rds_signal", newVal);
            Notify(26, packet);
        }

        @Override
        public void RDSNoPtyListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            packet.putBoolean("rds_no_pty", newVal);
            Notify(27, packet);
        }

        @Override
        public void RDSSeekStatusListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt("rds_seek_status", newVal);
            Notify(28, packet);
        }

        @Override
        public void AngleListener(Integer oldVal, Integer newVal) {
            Packet packet = new Packet();
            packet.putInt(RadioDefine.KeyMain.KEY_ANGLE,newVal);
            Notify(RadioInterface.MainToApk.ANGLE,packet);
        }
    };

    @Override
    public BaseDriver newDriver() {
        return new MCURadioDriver();
    }

    /**
     * the driver interface.
     */
    protected RadioDriverable Driver() {
        RadioDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof RadioDriverable) {
            ret = (RadioDriverable) drive;
        }
        return ret;
    }

    @Override
    public String getTypeName() {
        return "Radio";
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.radio_apk";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.radio_apk.RadioActivity";
    }

    @Override
    public String getMessageType() {
        return RadioDefine.MODULE;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_RADIO;
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
    }

    @Override
    public Packet getInfo() {
        Packet initPacket = super.getInfo();
        if(initPacket != null){
            //收音默认为开
//            initPacket.putBoolean(RadioDefine.KeyMain.KEY_RADIO_SWITCH, mRadioSwitch);
            initPacket.getBoolean(RadioDefine.KeyMain.KEY_RADIO_SWITCH);
            LogUtils.d(TAG, "radio getInfo -mRadioSwitch:"+mRadioSwitch);
        }
        return initPacket;
    }

    @Override
    public void onCreate(Packet packet) {
        // logic create.
        super.onCreate(packet);

        // bind the model listener.
        getModel().bindListener(mListener);

        // create driver.
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onCreate(null);
        }

        LogicProviderHelper.getInstance().update(Provider.RDS(), "" + getInfo().getBoolean(RadioDefine.KeyMain.KEY_RDS_ENABLE));
    }

    @Override
    public void onDestroy() {
        // destroy driver.
        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }

        // unbind the model listener.
        getModel().unbindListener(mListener);

        // logic destroy.
        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        LogUtils.d(TAG, "radio nId:" + nId);
        if (null == ret) {
            switch (nId) {
                case MessageDefine.APK_TO_MAIN_ID_CREATE:
//                Driver().AMS();
                    break;
                case MessageDefine.APK_TO_MAIN_ID_DESTROY:
//                Driver().AMS();
                    break;
                case RadioInterface.ApkToMain.AMS:
                    Driver().AMS();
                    break;
                case RadioInterface.ApkToMain.PREV_FREQUENCY:
                    Driver().prevFrequency();
                    break;
                case RadioInterface.ApkToMain.NEXT_FREQUENCY:
                    Driver().nextFrequency();
                    break;
                case RadioInterface.ApkToMain.PREV_SEARCH:
                    Driver().prevSearch();
                    break;
                case RadioInterface.ApkToMain.NEXT_SEARCH:
                    Driver().nextSearch();
                    break;
                case RadioInterface.ApkToMain.RADIO_STEREO:
                    //打开关闭立体声
                    Driver().openST();
//                Driver().closeST();
                    break;
                case RadioInterface.ApkToMain.RADIO_LOC: //
                    //打开关闭近程远程
                    Driver().switchLOC();
//                Driver().switchDX();
                    break;
                case RadioInterface.ApkToMain.PS:
                    Driver().PS();
                    break;
                case RadioInterface.ApkToMain.RADIO_OPEN:
                    Driver().radioOpen();
                    break;
                case RadioInterface.ApkToMain.RADIO_CLOSE:
                    Driver().radioClose();
                    break;
                case RadioInterface.ApkToMain.CHANGE_BAND:
                    int band = packet.getInt(RadioDefine.KeyRadio.KEY_BAND);
                    LogUtils.d(TAG, "radio band:" + band);
                    if (source() == SourceManager.getCurBackSource()) {
                        Driver().changeBand(band);
                    } else {
                        SourceManager.onChangeSource(source());
                    }
                    break;
                case RadioInterface.ApkToMain.REPLACE_STATION:
                    int station = packet.getInt(RadioDefine.KeyRadio.KEY_FREQ);
                    LogUtils.d(TAG, "radio station:" + station);
                    if (source() == SourceManager.getCurBackSource()) {
                        Driver().replaceStation(station);
                    } else {
                        SourceManager.onChangeSource(source());
                    }
                    break;
                case RadioInterface.ApkToMain.LP:
                    int lp = packet.getInt(RadioDefine.KeyRadio.KEY_LP);
                    LogUtils.d(TAG, "radio lp:" + lp);
                    Driver().LP(lp);
                    break;
                case RadioInterface.ApkToMain.SP:
                    int sp = packet.getInt(RadioDefine.KeyRadio.KEY_SP);
                    LogUtils.d(TAG, "radio sp:" + sp);
                    Driver().SP(sp);
                    break;
                case RadioInterface.ApkToMain.NPRE:
                    Driver().NPRE();
                    break;
                case RadioInterface.ApkToMain.PPRE:
                    Driver().PPRE();
                    break;
                case RadioInterface.ApkToMain.CHANGE_AM_FM:
                    Driver().changeFmAm();
                    break;
                case RadioInterface.ApkToMain.AREA:
                    int area = packet.getInt(RadioDefine.KeyRadio.KEY_AREA);
                    LogUtils.d(TAG, "radio sp:" + area);
                    Driver().setArea(area);
                    break;
                case RadioInterface.ApkToMain.SETTING_EQUALIZER:
                    LogUtils.d(TAG, "radio equalizer:"+SourceManager.getCurSource()+", source="+source());
                    //bug12700收音连续按二次音效作用会出现主机闪一下黑屏后又显示在收音界面。
                    //还原此改动，在收音APK作延时处理解决。2019-02-20
//                    if (SourceManager.getCurSource() == source()) {
                        EQDriver.Driver().enter();
//                    }
                    break;
                case RadioInterface.ApkToMain.WIDGET_PREV:
                    if (source() == SourceManager.getCurBackSource()) {
                        Driver().PPRE();
                    } else {
                        SourceManager.onChangeSource(source());
                    }
                    break;
                case RadioInterface.ApkToMain.WIDGET_NEXT:
                    if (source() == SourceManager.getCurBackSource()) {
                        Driver().NPRE();
                    } else {
                        SourceManager.onChangeSource(source());
                    }
                    break;
                case RadioInterface.ApkToMain.FM_SEND_SWITCH:
                    int fmSwitch = packet.getBoolean(RadioDefine.KeyMain.KEY_FMSEND_SWITCH) ? 1 : 0;
                    Driver().setFmSendCmd(1, fmSwitch);
                    break;
                case RadioInterface.ApkToMain.FM_SEND_INDEX:
                    int index = packet.getInt(RadioDefine.KeyMain.KEY_FMSEND_INDEX);
                    Driver().setFmSendCmd(2, index);
                    break;
                //RDS
                case RadioInterface.ApkToMain.RDS_AF:
                    Driver().rds_AF();
                    break;
                case RadioInterface.ApkToMain.RDS_TA:
                    if (packet != null) {
                        int value = packet.getInt("rds_button", -1);
                        if (value != -1) {//按弹出框的关闭和暂停
                            Driver().TA(value);
                            return ret;
                        }
                    }
                    Driver().TA();
                    break;
                case RadioInterface.ApkToMain.RDS_PTY:
                    int index1 = packet.getInt(RadioDefine.KeyMain.KEY_RDS_STATION_PTY);
                    Driver().PTY(index1);
                    break;
                case RadioInterface.ApkToMain.CHANGE_ANGLE:
                    int direction = packet.getInt(RadioDefine.KeyRadio.KEY_DIRECTION);
                    LogUtils.d(TAG, "change angle:" + direction);
                    Driver().changeAngle(direction);
                    break;
            }
        }
        return ret;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        LogUtils.d(TAG, "onKeyEvent----key="+key);
        boolean ret = true;
        switch (key) {
            case Define.Key.KEY_CH_INC:
                Driver().nextFrequency();
                break;
            case Define.Key.KEY_CH_DEC:
                Driver().prevFrequency();
                break;
            case Define.Key.KEY_PREV:
                Driver().PPRE();
                break;
            case Define.Key.KEY_FB:
            case Define.Key.KEY_SCAN_DEC:
            case Define.Key.KEY_DIRECT_LEFT:
                Driver().prevSearch();
                break;
            case Define.Key.KEY_NEXT:
                Driver().NPRE();
                break;
            case Define.Key.KEY_FF:
            case Define.Key.KEY_SCAN_INC:
            case Define.Key.KEY_DIRECT_RIGHT:
                Driver().nextSearch();
                break;
            case Define.Key.KEY_AMS:
                Driver().AMS();//搜台
                break;
            /*-begin-20180522-ydinggen-modifly-增加功能，康大提出-*/
            case Define.Key.KEY_BAND:
            case Define.Key.KEY_RADIO:
                if (SourceManager.getCurSource() != source() ||
                        (PowerManager.isPortProject() && key == Define.Key.KEY_RADIO)) {
                    SourceManager.onChangeSource(source());
                } else {
                    if (PowerManager.isKSProject()) {
                        Driver().changeBand();
                    } else {
                        //bug13246收音界面操作波段作用时，切换不到AM，操作方控收音作用时能切换到AM
                        //原因：有些收音模块不支持AM，由MCU处理。
                        Driver().changeFmAm();
                    }
                }
                break;
            case Define.Key.KEY_PS:
                Driver().PS();
                break;
            case Define.Key.KEY_SCAN:
                Driver().PS();//浏览
                break;
            case Define.Key.KEY_DIRECT_UP:
                Driver().prevFrequency();
                break;
            case Define.Key.KEY_DIRECT_DOWN:
                Driver().nextFrequency();
                break;
            case Define.Key.KEY_ST:
                Driver().openST();
                break;
            case Define.Key.KEY_NUM_1:
                Driver().LP(1);
                break;
            case Define.Key.KEY_NUM_2:
                Driver().LP(2);
                break;
            case Define.Key.KEY_NUM_3:
                Driver().LP(3);
                break;
            case Define.Key.KEY_NUM_4:
                Driver().LP(4);
                break;
            case Define.Key.KEY_NUM_5:
                Driver().LP(5);
                break;
            case Define.Key.KEY_NUM_6:
                Driver().LP(6);
                break;
            case Define.Key.KEY_AM:
                Driver().changFmAm(false);
                break;
            case Define.Key.KEY_FM:
                Driver().changFmAm(true);
                break;
//            case Define.Key.KEY_TA:
//                Driver().TA();
//                break;
//            case Define.Key.KEY_AF:
//                Driver().rds_AF();
//                break;
            case Define.Key.KEY_PTY://按遥控PTY按键，弹出PTY选择界面，配合Radio修改
                Packet packet1 = new Packet();
                packet1.putInt(RadioDefine.KeyMain.KEY_RDS_STATION_PTY, -1);
                Notify(RadioInterface.MainToApk.RDS_STATION_PTY, packet1);
                break;
            case Define.Key.KEY_LOC:
                Driver().switchLOC();
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }

    @Override
    public void onStop() {
        // 释放音频
        AudioDriver.Driver().abandon();
        LogUtils.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onStart() {
        LogUtils.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 申请音频
        AudioDriver.Driver().request(null,
                AudioDefine.AudioStream.STREAM_MUSIC,
                AudioDefine.AudioFocus.AUDIOFOCUS_GAIN);
    }

    @Override
    public boolean onStatusEvent(int type, boolean status, Packet packet) {
        return super.onStatusEvent(type, status, packet);
    }
}
