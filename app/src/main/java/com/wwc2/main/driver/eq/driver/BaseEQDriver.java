package com.wwc2.main.driver.eq.driver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MByteArray;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.MFourIntegerArray;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.eq.EQDriverable;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.settings_interface.SettingsDefine;

import java.util.ArrayList;
import java.util.List;

/**
 * the base EQ driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class BaseEQDriver extends BaseMemoryDriver implements EQDriverable {

    protected static Byte[] DEFAULT_SOUND = new Byte[]{0x00, 0x07, 0x07, 0x07, 0x07};

    protected boolean mSystemBoot = false;

    /**数据Model*/
    protected static class EQModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putInt(SettingsDefine.EQ.STYLE, getStyle().getVal());

            packet.putBoolean(SettingsDefine.EQ.LOUDNESS, getLoudness().getVal());
            packet.putBoolean("SUBWOOFER_SWITCH",getSubwooferSwitch().getVal());
            packet.putInt("SUBWOOFER_FREQ",getSubwoferFreq().getVal());
            List<Integer> values = new ArrayList<>();
            values.add(SettingsDefine.EQ.Type.SUBWOOFER);
            values.add(getSubwoofer().getVal());
            values.add(SettingsDefine.EQ.Type.BASS);
            values.add(getBass().getVal());
            values.add(SettingsDefine.EQ.Type.MIDDLE);
            values.add(getMiddle().getVal());
            values.add(SettingsDefine.EQ.Type.TREBLE);
            values.add(getTreble().getVal());
            Integer[] value = values.toArray(new Integer[]{});
            packet.putIntegerObjectArray(SettingsDefine.EQ.VALUE, value);

            int[] coords = new int[]{getX().getVal(), getY().getVal()};
            packet.putIntArray(SettingsDefine.SoundField.COORDS, coords);
            packet.putBoolean("SoundFieldHidden",getSoundFieldHidden().getVal());
            packet.putParcelableArray("DspSoundEffect", getDspSoundEffect().getVal());
            packet.putIntegerObjectArray("DspParam",getDspParam().getVal());
            packet.putIntegerObjectArray("DspSoundFiled",getDspSoundFiled().getVal());
            packet.putBoolean("3DSWITCH",get3DSwitch().getVal());
            packet.putIntegerObjectArray("DspHpfLpf", getDspHpfLpf().getVal());
            packet.putInt("QVALUE", getQValue().getVal());
            return packet;
        }

        /**EQ模式*/
        private MInteger mStyle = new MInteger(this, "StyleListener", SettingsDefine.EQ.Style.USER);
        public MInteger getStyle() {return mStyle;}
        /**等响度*/
        public MBoolean mLoudness = new MBoolean(this, "LoudnessListener", false);
        public MBoolean getLoudness() {return mLoudness;}
        /**重低音*/
        private MInteger mSubwoofer = new MInteger(this, "SubwooferListener", 7);
        public MInteger getSubwoofer() {return mSubwoofer;}
        /**低音*/
        private MInteger mBass = new MInteger(this, "BassListener", 7);
        public MInteger getBass() {return mBass;}
        /**中音*/
        private MInteger mMiddle = new MInteger(this, "MiddleListener", 7);
        public MInteger getMiddle() {return mMiddle;}
        /**高音*/
        private MInteger mTreble = new MInteger(this, "TrebleListener", 7);
        public MInteger getTreble() {return mTreble;}
        /**威益德 超重音*/
        private MBoolean mSubwooferSwitch = new MBoolean(this,"SubwooferSwitchListener",false);
        public MBoolean getSubwooferSwitch() {return  mSubwooferSwitch;}
        private MInteger mSubwoferFreq = new MInteger(this,"SubwooferFreqListener",0);
        public MInteger getSubwoferFreq() {return mSubwoferFreq;}

        /**声场设置*/
        private MInteger mX = new MInteger(this, "XListener", -1);
        public MInteger getX() {return mX;}

        private MInteger mY = new MInteger(this, "YListener", -1);
        public MInteger getY() {return mY;}

        private MByteArray mSoundFiled = new MByteArray(this, "SoundFiledListener", DEFAULT_SOUND);
        public MByteArray getSoundFiled() {
            return mSoundFiled;
        }

        private MBoolean mSoundFieldHidden = new MBoolean(this,"SoundFieldHiddenListener",false);
        public MBoolean getSoundFieldHidden() {
            return mSoundFieldHidden;
        }

        private MFourIntegerArray mDspSoundEffect = new MFourIntegerArray(this, "DspSoundEffectListener", null);
        public MFourIntegerArray getDspSoundEffect() {
            return mDspSoundEffect;
        }

        private MIntegerArray mDspParam = new MIntegerArray(this, "DspParamListener", null);
        public MIntegerArray getDspParam() {
            return mDspParam;
        }

        private MIntegerArray mDspSoundFiled = new MIntegerArray(this, "DspSoundFiledListener", null);
        public MIntegerArray getDspSoundFiled() {
            return mDspSoundFiled;
        }
        private MBoolean m3DSwitch = new MBoolean(this,"ThreeeDSwitchListener",false);
        public MBoolean get3DSwitch() {
            return m3DSwitch;
        }

        private MIntegerArray mDspHpfLpf = new MIntegerArray(this, "DspHpfLpfListener", null);
        public MIntegerArray getDspHpfLpf() {
            return mDspHpfLpf;
        }

        private MInteger mQValue = new MInteger(this, "QValueListener", 0);
        public MInteger getQValue() {
            return mQValue;
        }
    }

    @Override
    public BaseModel newModel() {
        return new EQModel();
    }

    /**
     * get the model object.
     */
    protected EQModel Model() {
        EQModel ret = null;
        BaseModel model = getModel();
        if (model instanceof EQModel) {
            ret = (EQModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "EQDataConfig.ini";
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            mMemory.set("EQ", "Style", Model().getStyle().getVal()+"");

            mMemory.set("EQ", "Loudness", Model().getLoudness().getVal()+"");

            mMemory.set("EQ", "Subwoofer", Model().getSubwoofer().getVal()+"");

            mMemory.set("EQ", "Bass", Model().getBass().getVal()+"");

            mMemory.set("EQ", "Middle", Model().getMiddle().getVal()+"");

            mMemory.set("EQ", "Treble", Model().getTreble().getVal()+"");

//            mMemory.set("SOUND", "RectX", Model().getX().getVal()+"");
//            mMemory.set("SOUND", "RectY", Model().getY().getVal()+"");

            Byte[] fileds = Model().getSoundFiled().getVal();
            if (fileds != null && fileds.length > 0) {
                String gainString = "";
                for (int i = 0; i < fileds.length; i++) {
                    gainString += (int)fileds[i];
                    if (i != fileds.length - 1) {
                        gainString += ",";
                    }
                }
                LogUtils.d("BaseEQDriver", "GainValue-----" + gainString);
                mMemory.set("SOUND", "GainValue", gainString);
            }

            //音效页DSP调节参数数据导出
            FourInteger[] soundEffects = Model().getDspSoundEffect().getVal();
            if(soundEffects != null && soundEffects.length >0) {
                String keyString = "";
                for(int i= 0; i < soundEffects.length;i++) {
                    keyString += soundEffects[i].getInteger1()+","+soundEffects[i].getInteger2()+","+soundEffects[i].getInteger3()+","+soundEffects[i].getInteger4();
                    if(i != soundEffects.length -1) {
                        keyString += ";";
                    }
                }
                LogUtils.d("BaseEQDriver","SoundEffects-----"+keyString);
                mMemory.set("DSP","SoundEffects",keyString);
            }
            //DSP页参数数据导出
            Integer[] dspParam = Model().getDspParam().getVal();
            if(dspParam != null && dspParam.length>0) {
                String keyString = "";
                for(int i= 0; i < dspParam.length;i++) {
                    keyString += dspParam[i];
                    if(i != dspParam.length -1) {
                        keyString += ",";
                    }
                }
                LogUtils.d("BaseEQDriver","DspParam-----"+keyString);
                mMemory.set("DSP","DspParam",keyString);
            }
            //音场平衡数据导出
            Integer[] dspSoundField = Model().getDspSoundFiled().getVal();
            if(dspSoundField != null && dspSoundField.length>0) {
                String keyString = "";
                for(int i= 0; i < dspSoundField.length;i++) {
                    keyString += dspSoundField[i];
                    if(i != dspSoundField.length -1) {
                        keyString += ",";
                    }
                }
                LogUtils.d("BaseEQDriver","DspSoundField-----"+keyString);
                mMemory.set("DSP","DspSoundField",keyString);
            }
            //3D开关数据导出
            LogUtils.d("BaseEQDriver","3DSWITCH-----"+Model().get3DSwitch().getVal());
            mMemory.set("DSP", "3DSWITCH", Model().get3DSwitch().getVal() + "");

            Integer[] dspHpfLpf = Model().getDspHpfLpf().getVal();
            if (dspHpfLpf != null && dspHpfLpf.length > 0) {
                String keyString = "";
                for (int i = 0; i < dspHpfLpf.length; i++) {
                    keyString += dspHpfLpf[i];
                    if (i != dspHpfLpf.length - 1) {
                        keyString += ",";
                    }
                }
                LogUtils.d("BaseEQDriver", "DSPHPFLPF-----" + keyString);
                mMemory.set("DSP", "DSPHPFLPF", keyString);
            }

            mMemory.set("DSP", "QVALUE", Model().getQValue().getVal() + "");

            ret = true;
        }
        return ret;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            try {
                Object object = mMemory.get("EQ", "Style");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getStyle().setVal(Integer.parseInt(string));
                    }
                }

                object = mMemory.get("EQ", "Loudness");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getLoudness().setVal(Boolean.parseBoolean(string));
                    }
                }

                object = mMemory.get("EQ", "Subwoofer");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getSubwoofer().setVal(Integer.parseInt(string));
                    }
                }

                object = mMemory.get("EQ", "Bass");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getBass().setVal(Integer.parseInt(string));
                    }
                }

                object = mMemory.get("EQ", "Middle");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getMiddle().setVal(Integer.parseInt(string));
                    }
                }

                object = mMemory.get("EQ", "Treble");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getTreble().setVal(Integer.parseInt(string));
                    }
                }

                //bug13657平衡无记忆，界面显示与实际不一致。
//                object = mMemory.get("SOUND", "RectX");
//                if (null != object) {
//                    String string = (String) object;
//                    LogUtils.d("BaseEQDriver", "RectX--read---" + string);
//                    if (!TextUtils.isEmpty(string)) {
//                        Model().getX().setVal(Integer.parseInt(string));
//                    }
//                }
//
//                object = mMemory.get("SOUND", "RectY");
//                if (null != object) {
//                    String string = (String) object;
//                    LogUtils.d("BaseEQDriver", "RectY--read---" + string);
//                    if (!TextUtils.isEmpty(string)) {
//                        Model().getY().setVal(Integer.parseInt(string));
//                    }
//                }

                if (mSystemBoot) {
                    mSystemBoot = false;
                    Model().getSoundFiled().setVal(DEFAULT_SOUND);
                } else {
                    object = mMemory.get("SOUND", "GainValue");
                    if (null != object) {
                        String string = (String) object;
                        LogUtils.d("BaseEQDriver", "GainValue--read---" + string);
                        if (!TextUtils.isEmpty(string)) {
                            String[] fileds = string.split(",");
                            if (fileds != null && fileds.length > 0) {
                                Byte[] filedsByte = new Byte[fileds.length];
                                for (int i = 0; i < fileds.length; i++) {
                                    filedsByte[i] = (byte) Integer.parseInt(fileds[i]);
                                }
                                Model().getSoundFiled().setVal(filedsByte);
                            }
                        }
                    } else {
                        LogUtils.e("BaseEQDriver", "GainValue--read---null");
                    }
                }

                ret = true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) {
            bytes[i++] = b;
        }
        return bytes;

    }

    byte[] toObjects(Byte[] bytesPrim) {
        byte[] bytes = new byte[bytesPrim.length];
        int i = 0;
        for (Byte b : bytesPrim) {
            bytes[i++] = b;
        }
        return bytes;
    }

    @Override
    public void enter() {
        final String string = ModuleManager.getLogicByName(SettingsDefine.MODULE).getAPKPacketName();
        if (!TextUtils.isEmpty(string)) {
            Context context = getMainContext();
            if (ApkUtils.isAPKExist(context, "com.wwc2.settings")) {
                if (null != context) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName(string, "com.wwc2.settings.SoundEffectActivity"));
                    context.startActivity(intent);
                }
            }
        }
    }

    @Override
    public void setX(int x) {
        Model().getX().setVal(x);
    }

    @Override
    public void setY(int y) {
        Model().getY().setVal(y);
    }

    @Override
    public void setField(int[] data) {

    }
}
