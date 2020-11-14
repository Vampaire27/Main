package com.wwc2.main.driver.eq.driver;

import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.memory.mode.ini.IniMemory;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.settings.SettingsListener;
import com.wwc2.settings_interface.SettingsDefine;

/**
 * the stm32 EQ driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class STM32EQDriver extends BaseEQDriver {

    private static final String TAG = "STM32EQDriver";

    private boolean importFlag      = false;  //只有导入true时才发给MCU

    /**the mcu listener.*/
    McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            send2Mcu(1, 7, 0, 0, 0); //请求 all EQ infor
            //send2Mcu(1, 1, 0, 0, 0);
           // send2Mcu(1, 2, 0, 0, 0);
            //send2Mcu(1, 3, 0, 0, 0);
           // send2Mcu(1, 5, 0, 0, 0);
           // send2Mcu(1, 6, 0, 0, 0);

            byte[] fields = toObjects(Model().getSoundFiled().getVal());
            if (mSystemBoot) {
                fields = toObjects(DEFAULT_SOUND);
                Model().getSoundFiled().setVal(DEFAULT_SOUND);
                mSystemBoot = false;
            }
            send2Mcu((byte) McuDefine.ARM_TO_MCU.RPT_SetBlanceInfo, fields, fields.length);
        }

        @Override
        public void DataListener(byte[] val) {
            if (null != val && val.length > 2 && (val[0] == (byte)(0xff& McuDefine.MCU_TO_ARM.MRPT_EQ_SET_INFO))) {
                LogUtils.d(TAG, "MCUListener data = " + bytesTohexString(val));
                switch (val[1]) {
                    case 0://低音
                        Model().getBass().setVal((int)val[2]);
                        break;
                    case 1://中音
                        Model().getMiddle().setVal((int)val[2]);
                        break;
                    case 2://高音
                        Model().getTreble().setVal((int)val[2]);
                        break;
                    case 3://重低音
                        Model().getSubwoofer().setVal((int)val[2]);
                        break;
                    case 4://中心频率
                        break;
                    case 5://等响度
                        Model().getLoudness().setVal((val[2] == 1) ? true: false);
                        break;
                    case 6://模式
                        int style;
                        switch (val[2]) {
                            case 0:     style = SettingsDefine.EQ.Style.CLOSE; break;
                            case 1:     style = SettingsDefine.EQ.Style.CLASSICAL; break;
                            case 3:     style = SettingsDefine.EQ.Style.CLUB; break;
                            case 9:     style = SettingsDefine.EQ.Style.DISCO; break;
                            case 7:     style = SettingsDefine.EQ.Style.GENTLE; break;
                            case 6:     style = SettingsDefine.EQ.Style.JAZZ; break;
                            case 2:     style = SettingsDefine.EQ.Style.POPULAR; break;
                            case 8:     style = SettingsDefine.EQ.Style.ROCK; break;
                            case 4:     style = SettingsDefine.EQ.Style.SITE; break;
                            case 5:     style = SettingsDefine.EQ.Style.USER; break;
                            default: return;
                        }
                        Model().getStyle().setVal(style);
                        break;
                }
            } else if (null != val && val.length > 2 && (val[0] == (byte) (0xff & McuDefine.MCU_TO_ARM.MPRT_DSPInfo))) {
                LogUtils.d(TAG, "MCUListener data = " + bytesTohexString(val));
                switch (val[1]) {
                    case 0://音效类型：0：用户；1：柔和；2：古典；3：舞曲；4：现场；5：流行
                        int style;
                        switch (val[2]) {
                            case 0:     style = SettingsDefine.EQ.Style.USER; break;
                            case 1:     style = SettingsDefine.EQ.Style.GENTLE; break;
                            case 2:     style = SettingsDefine.EQ.Style.CLASSICAL; break;
                            case 3:     style = SettingsDefine.EQ.Style.DISCO; break;
                            case 4:     style = SettingsDefine.EQ.Style.SITE; break;
                            case 5:     style = SettingsDefine.EQ.Style.POPULAR; break;
                            case 6:     style = 11;break;
                            default: return;
                        }
                        Model().getStyle().setVal(style);
                        break;
                    case 1:
                        if (val.length > 7) {
                            FourInteger[] soundEffects = Model().getDspSoundEffect().getVal();
                            if (soundEffects == null || soundEffects.length < 14) {
                                soundEffects = new FourInteger[14];
                            }
                            int index = val[2] & 0xFF;
                            int eqFreq = (int)((val[3]) & 0xFF) * 256 + (int)(val[4] & 0xFF);  //强转为int类型，否则计算时只会取到频率低字节
                            int eqType = val[5] & 0xFF;
                            int qValue = val[6] & 0xFF;
                            int eqGain = (val[7] & 0x80) == 1 << 7 ? -(~val[7]+1):val[7]&0xFF;
                            if (index <= 14) {
                                soundEffects[index - 1] = new FourInteger(eqGain,eqFreq, eqType, qValue);
                                Model().getDspSoundEffect().setValAnyway(soundEffects);
                            }
                        }
                        break;
                    case 2:
                        if (val.length > 6) {
                            Integer[] dspParam = Model().getDspParam().getVal();
                            if (dspParam == null || dspParam.length < 5) {
                                dspParam = new Integer[5];
                            }
                            for (int i=0; i<5; i++) {
                                dspParam[i] = val[2 + i] & 0xFF;
                            }
                            Model().getDspParam().setValAnyway(dspParam);
                        }
                        break;
                    case 3://音场平衡
                        if (val.length > 13) {
                            Integer[] dspSoundField = Model().getDspSoundFiled().getVal();
                            if (dspSoundField == null || dspSoundField.length < 12) {
                                dspSoundField = new Integer[12];
                            }
                            for (int i=0; i<12; i++) {
                                dspSoundField[i] = val[2 + i] & 0xFF;
                            }
                            Model().getDspSoundFiled().setValAnyway(dspSoundField);
                        }
                        break;
                    case 5: //3D开关
                        if(val.length > 2) {
                            Model().get3DSwitch().setValAnyway(val[2] == 1? true : false);
                        }
                        break;
                    case 6://高通低通配置
                        if (val.length > 7) {
                            Integer[] dsps = Model().getDspHpfLpf().getVal();
                            if (dsps == null || dsps.length < 4) {
                                dsps = new Integer[]{0, 0, 0, 0};
                            }
                            dsps[0] = val[2] & 0xFF;
                            dsps[1] = ((val[3] & 0xFF) * 256) + (val[4] & 0xFF);
                            dsps[2] = val[5] & 0xFF;
                            dsps[3] = ((val[6] & 0xFF) * 256) + (val[7] & 0xFF);

//                            LogUtils.d(TAG, "DataListen----dsp[0]=" + dsps[0] + ", dsp[1]=" + dsps[1] + ", dsp[2]=" + dsps[2] + ", dsp[3]=" + dsps[3]);
                            Model().getDspHpfLpf().setValAnyway(dsps);
                        }
                        break;
                    case 7://Q值
                        if(val.length > 2) {
                            Model().getQValue().setValAnyway(val[2] & 0xFF);
                        }
                        break;

                    case (byte) 0xf1:  //数据导入
                        if(val[2] == 1 && val.length >= 11) {  // 音效页DSP调节参数数据
                            if(val[3] == 0x06) Model().getStyle().setVal(11);
                            FourInteger[] soundEffects = Model().getDspSoundEffect().getVal();
                            if (soundEffects == null || soundEffects.length < 14) {
                                soundEffects = new FourInteger[14];
                            }
                            int index = val[5] & 0xFF;
                            int eqFreq = (int)((val[6]) & 0xFF) * 256 + (int)(val[7] & 0xFF);  //强转为int类型，否则计算时只会取到频率低字节
                            int eqType = val[8] & 0xFF;
                            int qValue = val[9] & 0xFF;
                            int eqGain = (val[10] & 0x80) == 1 << 7 ? -(~val[10]+1):val[10]&0xFF;
                            if (index <= 14) {
                                soundEffects[index - 1] = new FourInteger(eqGain,eqFreq, eqType, qValue);
                                Model().getDspSoundEffect().setValAnyway(soundEffects);
                            }
                        }
                        if(val[2] == 2 && val.length >= 8) {  // DSP页参数数据
                            Integer[] dspParam = Model().getDspParam().getVal();
                            if (dspParam == null || dspParam.length < 5) {
                                dspParam = new Integer[5];
                            }
                            for (int i=0; i<5; i++) {
                                dspParam[i] = val[3 + i] & 0xFF;
                            }
                            Model().getDspParam().setValAnyway(dspParam);
                        }
                        if(val[2] == 3 &&  val.length >= 15) {   //音场平衡数据
                            Integer[] dspSoundField = Model().getDspSoundFiled().getVal();
                            if (dspSoundField == null || dspSoundField.length < 12) {
                                dspSoundField = new Integer[12];
                            }
                            for (int i=0; i<12; i++) {
                                dspSoundField[i] = val[3 + i] & 0xFF;
                            }
                            Model().getDspSoundFiled().setValAnyway(dspSoundField);
                        }
                        if(val[2] == 4 && val.length >=4) {   //3D开关数据
                            Model().get3DSwitch().setValAnyway(val[3] == 1? true : false);
                        }
                        //高通低通不需要导入导出
                        //Q值
                        if (val[2] == 5 && val.length >= 4) {
                            Model().getQValue().setValAnyway(val[3] & 0xFF);
                        }
                        break;
                    default:
                        break;
                }
            }  else if (null != val && val.length > 2 && (val[0] == (byte)(0xff& McuDefine.MCU_TO_ARM.MART_SUBWOOFER_INFO))) {
                LogUtils.d(TAG, "SUBWOOFER data = " + bytesTohexString(val));
                Model().getSubwooferSwitch().setVal(val[1] == 1? true : false);
                Model().getSubwoferFreq().setVal((int) val[2]);
            } else if(null != val && val.length > 1 && (val[0] == (byte)(0xff& McuDefine.MCU_TO_ARM.MPRT_SoundFieldHidden))) {
                LogUtils.d(TAG, "SoundFieldHidden data = " + bytesTohexString(val));
                Model().getSoundFieldHidden().setVal(val[1] == 1? true : false);
            } else if(null != val && val.length > 0 &&(val[0]) == (byte)(0xff& McuDefine.MCU_TO_ARM.MPRT_DSP_FLASH)) {
//                LogUtils.d(TAG,"DSP Flash finished");
//                synchronized (flashOver) {
//                     flashOver.notifyAll();
//                }
            }
        }
    };

    SettingsListener mSettingsListener = new SettingsListener() {
        @Override
        public void ImportEQStatusListener(Boolean oldVal, Boolean newVal) {
            LogUtils.d(TAG, "ImportEQStatusListener val="+newVal);
            if(newVal){
                reloadConfig();
            }
        }
    };

    private PowerManager.PowerListener mPowerListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            LogUtils.e("mPowerListener PowerStepListener newVal:" + newVal);
            mSystemBoot = true;
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        McuManager.getModel().bindListener(mMCUListener);
        PowerManager.getModel().bindListener(mPowerListener);
        LogicManager.getLogicByName(SettingsDefine.MODULE).getModel().bindListener(mSettingsListener);
    }

    @Override
    public void onDestroy() {
        PowerManager.getModel().unbindListener(mPowerListener);
        McuManager.getModel().unbindListener(mMCUListener);
        mSystemBoot = false;
        super.onDestroy();
    }

    @Override
    public void setLoudness(boolean enable) {
        send2Mcu(0, 5, enable?1:0, 0, 0);
    }

    @Override
    public boolean getLoudness() {
        return Model().getLoudness().getVal();
    }

    @Override
    public void setSubwoofer(boolean enable, int subwooferFreq) {
            Model().getSubwooferSwitch().setVal(enable);
            byte[] data = new byte[]{enable? (byte) 1:0 , (byte) (subwooferFreq == -1 ? Model().getSubwoferFreq().getVal()&0xff : subwooferFreq&0xff)};
        if(subwooferFreq != -1) {
            Model().getSubwoferFreq().setVal(subwooferFreq);
        }
        send2Mcu((byte) McuDefine.ARM_TO_MCU.RPT_SetSubwoofer,data,data.length);
    }

    @Override
    public void setStyle(int style) {
        int tempStyle = style;
        switch (style) {
            case SettingsDefine.EQ.Style.CLOSE:     style = 0; break;
            case SettingsDefine.EQ.Style.CLASSICAL: style = 1; break;
            case SettingsDefine.EQ.Style.CLUB:      style = 3; break;
            case SettingsDefine.EQ.Style.DISCO:     style = 9; break;
            case SettingsDefine.EQ.Style.GENTLE:    style = 7; break;
            case SettingsDefine.EQ.Style.JAZZ:      style = 6; break;
            case SettingsDefine.EQ.Style.POPULAR:   style = 2; break;
            case SettingsDefine.EQ.Style.ROCK:      style = 8; break;
            case SettingsDefine.EQ.Style.SITE:      style = 4; break;
            case SettingsDefine.EQ.Style.USER:      style = 5; break;
            case 11:      style = 11; break;
            default: return;
        }
        send2Mcu(0, 6, style, 0, 0);

        byte[] data = new byte[2];
        data[0] = 0x00;
        switch (tempStyle) {
            case SettingsDefine.EQ.Style.USER:      data[1] = 0x00; break;
            case SettingsDefine.EQ.Style.GENTLE:    data[1] = 0x01; break;
            case SettingsDefine.EQ.Style.CLASSICAL: data[1] = 0x02; break;
            case SettingsDefine.EQ.Style.DISCO:     data[1] = 0x03; break;
            case SettingsDefine.EQ.Style.SITE:      data[1] = 0x04; break;
            case SettingsDefine.EQ.Style.POPULAR:   data[1] = 0x05; break;
            case 11: data[1] = 0x06; break;
            default: return;
        }
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 2);
    }

    @Override
    public void setTypeValue(int type, int value) {
        if (type == SettingsDefine.EQ.Type.BASS) {
            type = 0;
            Model().getBass().setVal(value);
        }
        if (type == SettingsDefine.EQ.Type.MIDDLE) {
            type = 1;
            Model().getMiddle().setVal(value);
        }
        if (type == SettingsDefine.EQ.Type.TREBLE) {
            type = 2;
            Model().getTreble().setVal(value);
        }
        if (type == SettingsDefine.EQ.Type.SUBWOOFER) {
            type = 3;
            Model().getSubwoofer().setVal(value);
        }
        send2Mcu(0, type, value, 0, 0);
    }

    void send2Mcu(int catogery, int type, int param1, int param2, int param3) {
        byte[] data = new byte[]{(byte)(catogery&0xff), (byte)(type&0xff), (byte)(param1&0xff), (byte)(param2&0xff), (byte)(param3&0xff)};
        LogUtils.d("EQ Send", bytesTohexString(data));
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.RPT_SetEQinfo, data, data.length);
    }

    public static String bytesTohexString(byte[] data) {
        String str = "";
        for (byte d: data) {
            str += byteTohexString(d);
        }
        return str;
    }

    public static String byteTohexString(byte data) {
        String hexStr = "";
        if ((data & 0xf0) == 0) hexStr += "0";
        hexStr += Integer.toHexString(data&0xff);
        hexStr += " ";
        return hexStr;
    }

    /**设置声场*/
    public void setField(int[] data) {
        if ((data != null) && (data.length >= 2)) {
            //以7为中心点
            if (null != data && data.length >= 4) {
                byte[] fields = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    fields[i] = (byte)(data[i]&0xff);
                }
                send2Mcu((byte) McuDefine.ARM_TO_MCU.RPT_SetBlanceInfo, fields, fields.length);

                Model().getSoundFiled().setVal(toObjects(fields));
            }
        }
    }

    @Override
    public void setDspSoundEffects(int index, int type, int value) {
        if (index > 0 && index <= 14) {
            FourInteger[] soundEffects = Model().getDspSoundEffect().getVal();
            if (soundEffects != null && soundEffects.length >= index) {
                FourInteger soundEffect = soundEffects[index -1];
//                LogUtils.d("setDspSoundEffects ==>"+index+";"+type+";"+value);
                byte[] data = new byte[7];
                data[0] = 0x01;
                data[1] = (byte) index;
                data[2] = (byte) ((soundEffect.getInteger2() >> 8) & 0xFF);
                data[3] = (byte) (soundEffect.getInteger2() & 0xFF);
                data[4] = (byte) (soundEffect.getInteger3() & 0xFF);
                data[5] = (byte) (soundEffect.getInteger4() & 0xFF);
                data[6] = (byte) (soundEffect.getInteger1() & 0xFF);
                switch (type) {
                    case 1:
                        data[2] = (byte) ((value >> 8) & 0xFF);
                        data[3] = (byte) (value & 0xFF);
                        break;
                    case 2:
                        data[4] = (byte) value;
                        break;
                    case 3:
                        data[5] = (byte) value;
                        break;
                    case 4:
                        data[6] = (byte) value;
                        break;
                    case 5:
                        data[2] = (byte) ((value >> 16) & 0xFF);
                        data[3] = (byte) (value >> 8 & 0xFF);
                        data[6] = (byte)(value & 0xFF);
                        break;
                }
                int eqFreq = ((int)(data[2] & 0xFF) * 256) + (int)(data[3] & 0xFF);
                int eqType = data[4] & 0xFF;
                int qValue = data[5] & 0xFF;
                int eqGain = (data[6] & 0x80) == 1 << 7 ? -(~data[6]+1):data[6]&0xFF;
//                LogUtils.d(TAG,"eqFrag="+eqFreq+";eqGain="+eqGain);
                soundEffects[index-1] = new FourInteger(eqGain,eqFreq,eqType,qValue);
                send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 7);
                Model().getDspSoundEffect().setVal(soundEffects);
            }
        }
    }

    @Override
    public void setDspParam(int type, int value) {
        Integer[] dspParam = Model().getDspParam().getVal();
        if (dspParam != null && dspParam.length >= type) {
            byte[] data = new byte[6];
            data[0] = 0x02;
            for (int i = 0; i < 5 && i < dspParam.length; i++) {
                data[i + 1] = (byte) (dspParam[i] & 0xFF);
            }

            data[type] = (byte) value;
            dspParam[type-1] = data[type]& 0xFF;
            Model().getDspParam().setVal(dspParam);
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 6);
        }
    }

    @Override
    public void setDspSoundField(int type, int value) {
        Integer[] dspSoundField = Model().getDspSoundFiled().getVal();
        if (dspSoundField != null && dspSoundField.length >= type) {
            byte[] data = new byte[13];
            data[0] = 0x03;
            for (int i = 0; i < 12 && i < dspSoundField.length; i++) {
                data[i + 1] = (byte) (dspSoundField[i] & 0xFF);
            }
            data[type] = (byte) value;
            dspSoundField[type-1] = data[type]&0xFF;
            Model().getDspSoundFiled().setVal(dspSoundField);
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 13);
        }
    }

    @Override
    public void resetDsp(int value) {
        byte[] data = new byte[2];
        data[0] = 0x04;
        data[1] = (byte) value;
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 2);
    }

    @Override
    public void set3D(boolean enable) {
        byte[] data = new byte[2];
        data[0] = 0x05;
        data[1] = (byte) (enable?1:0);
        Model().get3DSwitch().setVal(enable);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 2);
    }

    @Override
    public void setDspHpfLpf(int type, int value) {
        Integer[] dsps = Model().getDspHpfLpf().getVal();
        if (dsps == null || dsps.length < 4) {
            dsps = new Integer[]{0, 0, 0, 0};
        }
        if (dsps != null && dsps.length > type) {
            dsps[type] = value;
            Model().getDspHpfLpf().setValAnyway(dsps);

            byte[] data = new byte[7];
            data[0] = 0x06;
            data[1] = (byte) (dsps[0] & 0xFF);
            data[2] = (byte) ((dsps[1] >> 8) & 0xFF);
            data[3] = (byte) (dsps[1] & 0xFF);
            data[4] = (byte) (dsps[2] & 0xFF);
            data[5] = (byte) ((dsps[3] >> 8) & 0xFF);
            data[6] = (byte) (dsps[3] & 0xFF);
//            LogUtils.d(TAG, "setDspHpfLpf----type=" + type + ", data=" + FormatData.formatHexBufToString(data, data.length));
            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 7);
        }
    }

    @Override
    public void setQValue(int value) {
        byte[] data = new byte[2];
        data[0] = 0x07;
        data[1] = (byte) value;
        Model().getQValue().setVal(value);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 2);
    }

    public void outputDsp() {
        mMemory.save();
    }

    void send2Mcu(byte cmd, byte[] data, int len) {
        LogUtils.d(TAG, byteTohexString(cmd) + " " + bytesTohexString(data));
        McuManager.sendMcu(cmd, data, len);
    }

    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) {
            bytes[i++] = b;
        }
        return bytes;

    }

    public void reloadConfig() {
        LogUtils.d(TAG, "reloadConfig()---");
        if (mMemory != null && mMemory instanceof IniMemory) {
            ((IniMemory) mMemory).reloadIniFile();
            importFlag = true;
            readData();

        } else {
            LogUtils.e(TAG, "reloadConfig()---error");
        }
    }


    @Override
    public boolean readData() {
        boolean ret = super.readData();
        if (null != mMemory) {
            try {
                    Object object = mMemory.get("DSP","SoundEffects");
                    if(null != object) {
                        String string = (String) object;
                        LogUtils.d("BaseEQDriver", "SoundEffects--read---" + string);
                        if(!TextUtils.isEmpty(string)) {
                            String[] fields = string.split(";");
                            if(fields != null && fields.length > 0) {
                                FourInteger[] fieldFourString = new FourInteger[fields.length];
                                for (int i = 0; i < fields.length; i++) {
                                    String[] subFields = fields[i].split(",");
                                    if(subFields != null && subFields.length >= 4) {
                                        fieldFourString[i] = new FourInteger(Integer.parseInt(subFields[0]),Integer.parseInt(subFields[1]),Integer.parseInt(subFields[2]),Integer.parseInt(subFields[3]));
                                        byte[] data = new byte[10];
                                        data[0] = (byte) 0xF1;
                                        data[1] = 0x01;
                                        data[2] = 0x06;
                                        data[3] = 0x00;
                                        data[4] = (byte) ((i+1)& 0xFF);
                                        data[5] = (byte) ((fieldFourString[i].getInteger2() >> 8) & 0xFF);
                                        data[6] = (byte) (fieldFourString[i].getInteger2() & 0xFF);
                                        data[7] = (byte) (fieldFourString[i].getInteger3() & 0xFF);
                                        data[8] = (byte) (fieldFourString[i].getInteger4() & 0xFF);
                                        data[9] = (byte) (fieldFourString[i].getInteger1() & 0xFF);
                                        if(importFlag) {
                                            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 10);
//                                                    flashOver.wait();
                                        }
                                    }
                                }
                                Model().getDspSoundEffect().setVal(fieldFourString);
                            }
                        }
                    }

                    object = mMemory.get("DSP","DspParam");
                    if(null != object) {
                        String string = (String) object;
                        LogUtils.d("BaseEQDriver", "DspParam--read---" + string);
                        if(!TextUtils.isEmpty(string)) {
                            String[] fields = string.split(",");
                            if(fields != null && fields.length > 0) {
                                Integer[] filedsInteger = new Integer[fields.length];
                                for (int i = 0; i < fields.length; i++) {
                                    filedsInteger[i] = Integer.parseInt(fields[i]);
                                }
                                byte[] data = new byte[7];
                                data[0] = (byte) 0xF1;
                                data[1] = 0x02;
                                for (int i = 0; i < 5 && i < filedsInteger.length; i++) {
                                    data[i + 2] = (byte) (filedsInteger[i] & 0xFF);
                                }
                                 if(importFlag) {
                                    send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 7);
//                                             flashOver.wait();
                                 }
                                 Model().getDspParam().setVal(filedsInteger);
                            }
                        }
                    }
                    object = mMemory.get("DSP","DspSoundField");
                    if(null != object) {
                        String string = (String) object;
                        LogUtils.d("BaseEQDriver", "DspSoundField--read---" + string);
                        if(!TextUtils.isEmpty(string)) {
                            String[] fields = string.split(",");
                            if(fields != null && fields.length > 0) {
                                Integer[] filedsInteger = new Integer[fields.length];
                                for (int i = 0; i < fields.length; i++) {
                                    filedsInteger[i] = Integer.parseInt(fields[i]);
                                }
                                byte[] data = new byte[14];
                                data[0] = (byte) 0xF1;
                                data[1] = 0x03;
                                for (int i = 0; i < 12 && i < filedsInteger.length; i++) {
                                    data[i + 2] = (byte) (filedsInteger[i] & 0xFF);
                                }
                                if(importFlag) {
                                     send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 14);
                                    LogUtils.d("BaseEQDriver", "send DspSoundField--read---");
//                                            flashOver.wait();
                                }
                              Model().getDspSoundFiled().setVal(filedsInteger);
                            }
                        }
                    }

                    object = mMemory.get("DSP","3DSWITCH");
                    if(null != object) {
                        String string = (String) object;
                        LogUtils.d("BaseEQDriver", "3DSWITCH--read---" + string);
                        boolean enable = Boolean.parseBoolean(string);
                        byte[] data = new byte[3];
                        data[0] = (byte) 0xF1;
                        data[1] = 0x04;
                        data[2] = (byte) (enable?1:0);
                        if(importFlag) {
                            send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 3);
//                                    flashOver.wait();
                        }
                        Model().get3DSwitch().setVal(enable);
                    }

                    object = mMemory.get("DSP", "DSPHPFLPF");
                    if (null != object) {
                        String string = (String) object;
                        LogUtils.d("BaseEQDriver", "DspHfpLfp--read---" + string);
                        if(!TextUtils.isEmpty(string)) {
                            String[] dsps = string.split(",");
                            if(dsps != null && dsps.length > 0) {
                                Integer[] dspInteger = new Integer[dsps.length];
                                for (int i = 0; i < dsps.length; i++) {
                                    dspInteger[i] = Integer.parseInt(dsps[i]);
                                }
//                                byte[] data = new byte[14];
//                                data[0] = (byte) 0xF1;
//                                data[1] = 0x03;
//                                for (int i = 0; i < 12 && i < dspInteger.length; i++) {
//                                    data[i + 2] = (byte) (dspInteger[i] & 0xFF);
//                                }
//                                if(importFlag) {//高通低通不需要配置
//                                    send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 14);
//                                    LogUtils.d("BaseEQDriver", "send DspSoundField--read---");
////                                            flashOver.wait();
//                                }
                                Model().getDspHpfLpf().setVal(dspInteger);
                            }
                        }
                    }

                object = mMemory.get("DSP","QVALUE");
                if(null != object) {
                    String string = (String) object;
                    LogUtils.d("BaseEQDriver", "QVALUE--read---" + string);
                    int value = Integer.parseInt(string);
                    byte[] data = new byte[3];
                    data[0] = (byte) 0xF1;
                    data[1] = 0x05;
                    data[2] = (byte) value;
                    if(importFlag) {
                        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_DSPInfo, data, 3);
//                                    flashOver.wait();
                    }
                    Model().getQValue().setVal(value);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            ret = true;
        }
        importFlag = false;
        return ret;
    }


}
