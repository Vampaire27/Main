package com.wwc2.main.radio.driver;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.camera.CameraLogic;
import com.wwc2.main.driver.client.ClientDriver;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.settings.util.ToastUtil;
import com.wwc2.radio_interface.RadioDefine;
import com.wwc2.radio_interface.RadioInterface;

import java.io.UnsupportedEncodingException;

import static com.wwc2.main.driver.client.driver.BaseClientDriver.CLIENT_BS;
import static com.wwc2.main.driver.client.driver.BaseClientDriver.CLIENT_RP;

/**
 * the MCU radio module.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public class MCURadioDriver extends BaseRadioDriver {

    /**TAG*/
    private static final String TAG = "MCURadioDriver";

    private int bakSource = Define.Source.SOURCE_NONE;

    private final int MES_RDS = 1001;

    private boolean firstRevData = true;

    private Handler MegHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MES_RDS:
                    String value = (String) msg.obj;
                    ToastUtil.show(getMainContext(), value);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * MCU数据的监听
     */
    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {
        int pageSize = 5;
        @Override
        public void DataListener(byte[] val) {
            if (null != val) {
                byte cmdByte = val[0];
                if(cmdByte == RadioCommand.MRPT_RADIOINITDATA){
                    if(val.length < 65)return;
                    byte[] radioBytes = CommandUtil.getData(val, 1);
                    int working		= radioBytes[0];    //state:正常-0 搜索-1 浏览-2
                    Model().getWork().setVal(working);
                    int band		= radioBytes[1];    //band(FM1=0 FM2=1 FM3=2 AM1=3 AM2=4)
                    Model().getBand().setVal(band);
                    int mainFrequency = CommandUtil.toInth(new byte[]{radioBytes[2], radioBytes[3]});
                    int saveFrequencyOn	= radioBytes[4];//saveFrequencyOn 被点亮的预存频点(1~6)
//                    Model().getFreqSaveOn().setVal(saveFrequencyOn);
                    Model().getFreq().setValAnyway(mainFrequency);
                    byte[] saveFrequency = new byte[pageSize * 12];
                    System.arraycopy(val, 6, saveFrequency, 0, saveFrequency.length);
                    LogUtils.d(TAG,"band:" + band + " working:" + working + " mainFrequency:" + mainFrequency + " saveFrequencyOn:" + saveFrequencyOn);
                    Integer[] fmFrequencyFreq = getAllAFM(saveFrequency, 0, 3);
                    Integer[] amFrequencyFreq = getAllAFM(saveFrequency, 3, 5);
                    Model().getFMFreqArray().setVal(fmFrequencyFreq);
                    Model().getAMFreqArray().setVal(amFrequencyFreq);
                    if (saveFrequencyOn > 0) {//修改：由于MCU发送的高亮频点与当前频率对应不上，暂由Main判断。2017-07-11
                        if (band >= 0 && band < 3) {
                            if (fmFrequencyFreq[band * 6 + saveFrequencyOn - 1] != mainFrequency) {
                                LogUtils.d(TAG, "saveFrequency:" + fmFrequencyFreq[band * 6 + saveFrequencyOn - 1] + ", index=" + (band * 6 + saveFrequencyOn - 1));
                                saveFrequencyOn = 0;
                            }
                        } else if (band >= 3) {
                            if (amFrequencyFreq[(band - 3) * 6 + saveFrequencyOn - 1] != mainFrequency) {
                                saveFrequencyOn = 0;
                            }
                        }
                    }
                    if (saveFrequencyOn == Model().getFreqSaveOn().getVal()) {
                        BaseLogic logic = ModuleManager.getLogicByName(RadioDefine.MODULE);
                        if (logic != null) {
                            Packet packet = new Packet();
                            packet.putInt(RadioDefine.KeyMain.KEY_FREQ_SAVE_ON, saveFrequencyOn);
                            logic.Notify(false, RadioInterface.MainToApk.FREQ_SAVE_ON, packet);
                        }
                    }
                    Model().getFreqSaveOn().setVal(saveFrequencyOn);

                    if (working == 0) {//增加返回ACK给MCU，避免扫描状态收不到的情况。2020-09-01
                        McuManager.sendAckData(val);
                    }
                }else if(cmdByte == RadioCommand.MRPT_RADIOFLAG){
                    if(val.length >= 5){
                        int stState = val[1];//[0关/1开]
                        int locState = val[2];//[1近程/0远程]
                        int powState = val[3];//[0开/1关]
                        int stSwitch = val[4];//[0开/1关]
                        //LogUtils.d(TAG,"DataListener stState:" + stState + " locState:" + locState + "powState:" + powState);
                        Model().getSTState().setVal(stState == 1 ? true : false);   //立体声状态
                        Model().getLOCEnable().setVal(locState == 1? true : false);     //近程远程
                        Model().getSwitchEnable().setVal(powState == 0 ? true : false);  //收音开关
                        Model().getmSTSwitch().setVal(stSwitch == 0 ? true : false);//立体声开关()
                        //LogUtils.d(TAG,"st state:" + Model().getSTState().getVal() + " st switch:" + Model().getmSTSwitch().getVal() + " loc:" + Model().getLOCEnable().getVal());
                    }
                }else if(cmdByte == RadioCommand.MRPT_RADIOREGION){
                    if(val.length < 3){
                        int area = val[1];
                        LogUtils.d(TAG,"MCUListener area:" + area );
                    }else if(val.length < 13){
                        Integer fmMaxFreq = CommandUtil.toInth(new byte[]{val[2],val[3]});
                        Integer fmMinFreq = CommandUtil.toInth(new byte[]{val[4],val[5]});
//                        Integer fmSetup = (int)val[6]/10;//暂时由AP处理。
                        Integer fmSetup;

                        String clientProject;
                        Packet packet = DriverManager.getDriverByName(ClientDriver.DRIVER_NAME).getInfo();
                        if (packet != null) {
                            clientProject = packet.getString("ClientProject");
                        } else {
                            clientProject = CLIENT_BS;
                        }
                        //只有锐派的步进需要除以10，其他所有客户全部按照百盛协议做法。否则会出现步进为0，避免导致收音报错。2017-08-01
                        if (CLIENT_RP.equals(clientProject)) {
                            if (fmMaxFreq == 7600 & fmMinFreq == 6400) { // 20170524 说明是俄罗斯频段
                                fmSetup = val[6]&0xFF;
                            } else {
                                fmSetup = (val[6]&0xFF) / 10;
                            }
                        } else {
                            fmSetup = val[6]&0xFF;
                        }
                        Model().getFMFreqRegionArray().setVal(new Integer[]{fmMaxFreq,fmMinFreq,fmSetup});
                        LogUtils.d(TAG,"MCUListener fmMaxFreq:" + fmMaxFreq + " fmMinFreq:" + fmMinFreq + " fmSetup:" + fmSetup);
                        Integer amMaxFreq = CommandUtil.toInth(new byte[]{val[7],val[8]});
                        Integer amMinFreq = CommandUtil.toInth(new byte[]{val[9],val[10]});
                        Integer amSetup = (int)val[11];
                        Model().getAMFreqRegionArray().setVal(new Integer[]{amMaxFreq,amMinFreq,amSetup});
                        LogUtils.d(TAG,"MCUListener amMaxFreq:" + amMaxFreq + " amMinFreq:" + amMinFreq + " amSetup:" + amSetup);
                    }
                } else if (cmdByte == McuDefine.MCU_TO_ARM.MRPT_FmInitdata) {
                    if (val.length < 18) return;
                    int fmSendSwitch = val[1]&0xFF;    //state:关-0 开-1
                    Model().getFmSendSwitch().setVal(fmSendSwitch == 1);
                    int fmSendIndex = val[2]&0xFF;
                    Model().getFmSendIndex().setVal(fmSendIndex);
                    byte[] saveFrequency = new byte[16];
                    System.arraycopy(val, 3, saveFrequency, 0, saveFrequency.length);
                    LogUtils.d(TAG,"fmSendSwitch:" + fmSendSwitch + " fmSendIndex:" + fmSendIndex);
                    Integer[] fmSendFreq = new Integer[8];
                    for (int i=0; i<fmSendFreq.length; i++) {
                        fmSendFreq[i] = ((saveFrequency[i*2] << 8) & 0xFF00) + (saveFrequency[i*2 + 1] & 0xFF);
                    }
                    if (fmSendFreq != null) {
                        Model().getFmSendFreqArray().setVal(fmSendFreq);
                    }
                } else if (cmdByte == McuDefine.MCU_TO_ARM.MRPT_FmState) {
                    if (val.length < 3) return;
                    int state = val[1] & 0xFF;
                    Model().getFmSendSwitch().setVal(state == 1);//state:关-0 开-1
                    int fmSendIndex = val[2]&0xFF;
                    LogUtils.d(TAG,"11 fmSendSwitch:" + state + " fmSendIndex:" + fmSendIndex);
                    Model().getFmSendIndex().setVal(fmSendIndex);
                } else if (cmdByte == McuDefine.MCU_TO_ARM.MRPT_RDSControl) {
                    if (val.length < 3) return;

                    LogUtils.d(TAG, "RDSDATA--0--val[1]:" + (val[1] & 0xFF) + " val[2]:" + (val[2] & 0xFF));

                    boolean enable = (val[1] & 0x01) == 0x01;
                    Model().getRDSEnable().setVal(enable);//YDG
                    enable = (((val[1] >> 1) & 0x01) == 0x01);
                    Model().getRDSTPSwitch().setVal(enable);

                    enable = ((val[1] >> 2) & 0x01) == 0x01;
                    if (Model().getRDSTaSwitch().getVal() != enable) {
                        Model().getRDSTaSwitch().setVal(enable);
                        if (!firstRevData) {
                            Message msg = MegHandler.obtainMessage(MES_RDS);
                            msg.obj = (enable ? "TA ON" : "TA OFF");
                            MegHandler.sendMessage(msg);
                        }
                    }

                    //RDS信号指示
                    enable = (((val[1] >> 3) & 0x01) == 0x01);
                    Model().getRDSSignal().setVal(enable);

                    //PTY seek status
                    int status = (val[1] >> 4) & 0x07;
//                    Model().getRDSSeekStatus().setVal(status);
                    //NO PTY
                    enable = (((val[1] >> 7) & 0x01) == 0x01);
                    Model().getRDSNoPty().setVal(enable);

                    enable = ((val[2] & 0x01) == 0x01);
                    if (Model().getRDSAfSwitch().getVal() != enable) {
                        Model().getRDSAfSwitch().setVal(enable);
                        if (!firstRevData) {
                            Message msg = MegHandler.obtainMessage(MES_RDS);
                            msg.obj = (enable ? "AF ON" : "AF OFF");
                            MegHandler.sendMessage(msg);
                        }
                    }

                    //解决第一次上电开机不显示Toast
                    firstRevData = false;

                    //PI SEEK
                    if (((val[2] >> 4) & 0x01) == 0x01) {
                        status = (status | 0x08);
                    }
                    Model().getRDSSeekStatus().setVal(status);

                    int traffic = (val[2] >> 5) & 0x01;
                    //收到交通信息，需要切到收音。
                    setRdsTraffic(traffic == 1);
                } else if (cmdByte == McuDefine.MCU_TO_ARM.MRPT_RDSPtyPs) {
                    if (val.length < 10) return;
                    try {
                        String station = new String(val, 1, 8, "utf-8");
                        LogUtils.d("RDSDATA--1--data=" + FormatData.formatHexBufToString(val, val.length) + ", =" + station);
                        Model().getRDSStationInfo().setVal(station);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    Model().getRDSStationType().setVal(val[9] & 0xFF);
                }  else if(cmdByte == RadioCommand.RADIO_ANGLE) {
                    if(val.length >= 2) {
                        LogUtils.d("radio_angle---" + val[1]);
                        Model().getAngle().setValAnyway((int) val[1]);
                    }
                }
            }
        }

        @Override
        public void OpenListener(int status) {
            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_RDSPtyPs, null, 0);

            McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_RDSRequest, null, 0);
        }

        /**
         * 获取FM/AM的返回数据
         * @param freq  源数据(FM/AM)
         * @param startIndex    FM:0/AM:3/FM+AM:0
         * @param endIndex      FM:3/AM:5/FM+AM:5
         * @return  FM/AM/FM+AM
         */
        public Integer[] getAllAFM(byte[] freq,int startIndex,int endIndex){
            int pageCount = endIndex - startIndex;
            int pageSize =6;
            Integer[] iFreq = new Integer[pageCount * pageSize];
            for(int i = 0;i<iFreq.length;i++ ){
                iFreq[i] =  CommandUtil.toInth(new byte[]{freq[i*2+(startIndex * pageSize*2)], freq[i*2+1+(startIndex * pageSize*2)]});
            }
            return  iFreq;
        }

        public Integer[] getAFMByteDetial(byte[] freq){
//            Log.d(TAG,"getAFMByteDetial:"+CommandUtil.printHexString(freq));
            int pageSize = 6;
            Integer[] iFreq = new Integer[pageSize];
            for(int i = 0;i < pageSize ;i++){
//                Log.d(TAG,"getAFMByteDetial_iFreq[i]:"+ CommandUtil.printHexString(new byte[]{freq[i*2], freq[i*2+1]}));
                iFreq[i] = CommandUtil.toInth(new byte[]{freq[i*2], freq[i*2+1]});
            }
            return iFreq;
        }
        /**
         * 获取FM/AM的返回数据
         * @param freq  源数据(FM/AM)
         * @param startIndex    FM:0/AM:3/FM+AM:0
         * @param endIndex      FM:3/AM:5/FM+AM:5
         * @return  FM/AM/FM+AM
         */
        public Integer[][] getAllAFM2(byte[] freq,int startIndex,int endIndex){
            int pageSize = endIndex - startIndex;
            Integer[][] iFreq = new Integer[pageSize][];
            for(int i = 0;i<pageSize;i++ ){
                byte[] mFreq = new byte[12];
//            System.arraycopy(freq,(startIndex * 12)+i * 12,mFreq,0,mFreq.length);
                System.arraycopy(freq,(startIndex + i) * 12,mFreq,0,mFreq.length);
                iFreq[i] =  getAFMByteDetial(mFreq);
            }
            return  iFreq;
        }

    };

    @Override
    public void onCreate(Packet packet) {
        BaseModel model = McuManager.getModel();
        if (null != model) {
            model.bindListener(mMcuListener);
        }
    }

    @Override
    public void onDestroy() {
        BaseModel model = McuManager.getModel();
        if (null != model) {
            model.unbindListener(mMcuListener);
        }
    }

    @Override
    public void prevFrequency() {                       //向左-向下频点
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_STPDN));
    }

    @Override
    public void nextFrequency() {                       //向右-向上频点
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_STPUP));
    }

    @Override
    public void prevSearch() {
//        writeCmd(RadioCommand.radioSrhdn());        //向左搜台-向下搜台
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_SRHDN));
    }

    @Override
    public void nextSearch() {
//        writeCmd(RadioCommand.radioSrhup());         //向右搜台-向上搜台
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_SRHUP));
    }

    @Override
    public void AMS() {                             //自动搜索
        //for can not stop search ... zhongyang.hu 20170520  old:writeCmd
        writeCmdNack(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_ASRH));
    }

    @Override
    public void PS() {
//        writeCmd(RadioCommand.radioScan());         //浏览
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_SCAN));
    }

    @Override
    public void changeBand(int band) {              //band 修改
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_FM_AM_PAGE[band]));
    }

    @Override
    public void changeBand() {
        int band = Model().getBand().getVal();
        if(band >= RadioDefine.Band.BAND_AM2){
            band = RadioDefine.Band.BAND_FM1;
        }else{
            band +=1;
        }
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_FM_AM_PAGE[band]));
    }

    @Override
    public void changFmAm(boolean bFm) {
        int band = Model().getBand().getVal();
        if (bFm) {
            if (band >= RadioDefine.Band.BAND_FM3) {
                band = RadioDefine.Band.BAND_FM1;
            } else {
                band += 1;
            }
        } else {
            if (band < RadioDefine.Band.BAND_AM1 || band >= RadioDefine.Band.BAND_AM2) {
                band = RadioDefine.Band.BAND_AM1;
            } else {
                band += 1;
            }
        }
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_FM_AM_PAGE[band]));
    }


    @Override
    public void replaceStation(int station) {       //设置频点
        Log.d("chenyuhan","  replaceStation  = "+station);
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioSetFrq(RadioCommand.RADIO_SET_FRQ,station));
//        writeCmd(RadioCommand.radioSetFrq(CommandUtil.toByteArrayh(station, 2)));
    }

    @Override
    public void LP(int currentIndex) {                              //选台
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_LPS[currentIndex-1]));
//        writeCmd(RadioCommand.radioButton(RadioCommand.RADIO_LPS[currentIndex]));
    }

    @Override
    public void SP(int currentIndex) {                             //存台
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_SPS[currentIndex-1]));
//        writeCmd(RadioCommand.radioButton(RadioCommand.RADIO_SPS[currentIndex]));
    }

    @Override
    public void NPRE() {                                            //上一个存台
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_NPRE));
//        writeCmd(RadioCommand.radioButton(RadioCommand.RADIO_NPRE));
    }

    @Override
    public void PPRE() {                                            //下一个存台
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_PPRE));
    }

    @Override
    public void changeFmAm() {                      //Fm-AM切换
        Log.d("chenyuhan","  changeFmAm  = Fm-AM切换 ");
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_FM_AM_SWITCH));
    }

    @Override
    public void radioOpen() {
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_PW));
    }

    @Override
    public void radioClose() {
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_PW));
    }

    @Override
    public void radioExit() {
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_STOP));
    }

    @Override
    public void openST() {
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_STEREO));
    }

    @Override
    public void closeST() {
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_STEREO));
    }

    @Override
    public void switchLOC() {
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_LOC));
    }

    @Override
    public void switchDX() {
        writeCmd(RadioCommand.OP_RADIOBUTTON,RadioCommand.radioGeneral(RadioCommand.RADIO_LOC));
    }

    @Override
    public void openRds() {

    }

    @Override
    public void closeRds() {

    }

    @Override
    public void PTY(int type) {
        writeCmd(RadioCommand.OP_RADIOBUTTON, new byte[]{0x26, (byte) type});
    }

    @Override
    public void TA() {
//        if (Model().getRDSTaSwitch().getVal()) {
            writeCmd(RadioCommand.OP_RADIOBUTTON, new byte[]{0x25, 0x00});
//        } else {
//            writeCmd(RadioCommand.OP_RADIOBUTTON, new byte[]{0x25, (byte) 0xAA});
//        }
    }

    @Override
    public void TA(int value) {
        if (value == 1) {
            writeCmd(RadioCommand.OP_RADIOBUTTON, new byte[]{0x25, (byte) 0xAA});
        } else {
            writeCmd(RadioCommand.OP_RADIOBUTTON, new byte[]{0x25, 0x00});
        }
    }

    @Override
    public void TP() {

    }

    @Override
    public void rds_AF() {
        writeCmd(RadioCommand.OP_RADIOBUTTON, new byte[]{0x24});
    }

    @Override
    public void changeAngle(int value) {
        writeCmd(RadioCommand.RADIO_CHANGE_ANGLE,new byte[]{(byte) value});
    }

    @Override
    public void setArea(int area) {             //请求收音区域    0-美洲1.拉丁美洲2-欧洲 3-OIRT  4-日本 5-南美洲 6-东欧
        writeCmd(RadioCommand.OP_RADIOSETREGION,new byte[]{RadioCommand.RADIO_SETREGION[area]});
    }

    public void writeCmd(byte head,byte[] radioCmds){
        McuManager.sendMcuNack(head, radioCmds, radioCmds.length);
    }

    public void writeCmdNack(byte head,byte[] radioCmds){
        McuManager.sendMcuNack(head, radioCmds, radioCmds.length);
    }

    @Override
    public void setFmSendCmd(int type, int value) {
        writeCmd((byte) McuDefine.ARM_TO_MCU.OP_FmTransmitter, new byte[]{(byte) type, (byte) value});
    }

    private void setRdsTraffic(boolean traffic) {
        Model().getRDSTraffic().setVal(traffic);

        if (traffic) {
            if (SourceManager.getCurSource() != Define.Source.SOURCE_RADIO) {
                bakSource = SourceManager.getLastNoPoweroffRealSource();
                if (SourceManager.getCurSource() == Define.Source.SOURCE_CAMERA) {
                    CameraLogic.setEnterCameraSource(Define.Source.SOURCE_RADIO, false);
                    SourceManager.onOpenBackgroundSource(Define.Source.SOURCE_RADIO);
                } else {
                    SourceManager.onChangeSource(Define.Source.SOURCE_RADIO);
                }
            }
        } else {
            if (bakSource != Define.Source.SOURCE_NONE) {
                if (SourceManager.getCurSource() == Define.Source.SOURCE_CAMERA) {
                    CameraLogic.setEnterCameraSource(bakSource, false);
                    SourceManager.onOpenBackgroundSource(bakSource);
                } else {
                    SourceManager.onChangeSource(bakSource);
                }
                bakSource = Define.Source.SOURCE_NONE;
            }
        }
    }
}
