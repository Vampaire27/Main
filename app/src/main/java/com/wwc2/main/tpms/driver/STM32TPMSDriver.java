package com.wwc2.main.tpms.driver;

import android.util.Log;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.tpms.driver.protocol.MassesProtocol;
import com.wwc2.main.tpms.driver.protocol.MessesDefine;
import com.wwc2.main.tpms.driver.protocol.WriteThread;
import com.wwc2.main.manager.McuManager;

import java.util.List;


/**
 * the TPMS driver.
 *
 * @author wwc2
 * @date 2017/1/8
 */
public class STM32TPMSDriver extends BaseTPMSDriver implements TPMSDriverable,TPMSDriverable.DataListener,WriteThread.WritedListener{

    private String TAG = STM32TPMSDriver.class.getSimpleName();

    BaseModel mcuModel;

    private MassesProtocol mMassesProtocol = new MassesProtocol();//协议解析
    private WriteThread mWriteThread = new WriteThread();//协议发送
    private boolean mIsConnected = false;
    /**
     * MCU数据的监听
     */
    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            init();
        }

        @Override
        public void CloseListener(int status) {
            mMassesProtocol.onDestroy();
        }

        @Override
        public void DataListener(byte[] val) {
//            LogUtils.d(TAG,"tpms mcu DataListener:" + MessesDefine.printHexString(val));

            if(val != null){
                byte cmdByte = val[0];
                int startIndex = 2;
                byte[] data =  new byte[val.length - startIndex];
                if(cmdByte == MessesDefine.EXTERNAL_MOD_DATA){
                    if(data.length >1){
                        byte cmdSerialPort  = val[1];
                        if(MessesDefine.MOD_DATA_SERIAL_PORT == cmdSerialPort){
                            System.arraycopy(val,startIndex,data,0,data.length);
                            mMassesProtocol.data(data);
                            LogUtils.d(TAG,MessesDefine.printHexString(data));
                        }
                    }
                }
            }

        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        mcuModel = McuManager.getModel();
        if (null != mcuModel) {
            mcuModel.bindListener(mMcuListener);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mcuModel) {
            mcuModel.unbindListener(mMcuListener);
        }
    }

    /**
     * 初始轮胎位置
     */
    public void init(){
        mMassesProtocol.addReadIdleListener(new MassesProtocol.ReadIdleListener() {
            @Override
            public int getReadIdleTime() {
                return 3000;
            }

            @Override
            public int onReadIdle(List<byte[]> result) {
                Log.d(TAG,"tpms  HandShake");
                mWriteThread.write(MessesDefine.Query.cmdHandShake(),null);

                return MassesProtocol.ReadIdleListener.LOOP;
            }
        });

        mMassesProtocol.addReadIdleListener(new MassesProtocol.ReadIdleListener(){

            @Override
            public int getReadIdleTime() {
                return 1000 * 10;
            }

            @Override
            public int onReadIdle(List<byte[]> result) {
                Log.d(TAG,"tpms  disconnected!");
                mIsConnected = false;
                Model().getTpmsConnected().setVal(false);
                return MassesProtocol.ReadIdleListener.NORMAL;
            }
        });
        mMassesProtocol.addReadIdleListener(new MassesProtocol.ReadIdleListener() {
            @Override
            public int getReadIdleTime() {
                return 0;
            }

            @Override
            public int onReadIdle(List<byte[]> result) {
                if(result != null){
                    if(result.size() != 0){
                        if(mIsConnected == false){
                            Model().getTpmsConnected().setVal(true);
                            mIsConnected = true;
                            Log.d(TAG,"tpms connected!");
                        }
                    }
                }
                return MassesProtocol.ReadIdleListener.NORMAL;
            }
        });
        mMassesProtocol.setDataListener(this);
        Integer[] tireLocation = new Integer[]{(int) MessesDefine.CarWheel.LEFT_BACK, (int) MessesDefine.CarWheel.LEFT_FRONT, (int) MessesDefine.CarWheel.RIGHT_FRONT, (int) MessesDefine.CarWheel.RIGHT_BACK};
        Model().getTireLocation().setVal(tireLocation);
//        mMassesProtocol.data(new byte[]{(byte)0xAA,(byte)0xA1,(byte)0xB1,0x07,0x11,0x00,0x14 });
        mMassesProtocol.startCheckingReadIdle(null);
//        mWriteThread.write(MessesDefine.Query.cmdHandShake(),null);

    }


    @Override
    public void setMaxPressure(Integer pressure) {
        mWriteThread.write(MessesDefine.Config.setMaxPressure(pressure),this);
    }

    @Override
    public void setMinPressure(Integer pressure) {
        mWriteThread.write(MessesDefine.Config.setMinPressure(pressure),this);
    }

    @Override
    public void setMaxTemperature(Integer temperature) {
        mWriteThread.write(MessesDefine.Config.setMaxTemperature(temperature),this);
    }

    @Override
    public void setTireSensorID(int tireLocation, int tireSensorID) {
        mWriteThread.write(MessesDefine.Config.setTireSensorID(tireLocation,tireSensorID),this);
    }

    @Override
    public void openTpms(boolean open) {
        Model().getTpmsSwtich().setVal(open);
    }

    @Override
    public void activityMain() {
        mWriteThread.write(MessesDefine.Query.cmdQuery(MessesDefine.CarWheel.LEFT_BACK),null);
        mWriteThread.write(MessesDefine.Query.cmdQuery(MessesDefine.CarWheel.LEFT_FRONT),null);
        mWriteThread.write(MessesDefine.Query.cmdQuery(MessesDefine.CarWheel.RIGHT_BACK),null);
        mWriteThread.write(MessesDefine.Query.cmdQuery(MessesDefine.CarWheel.RIGHT_FRONT),null);
        mWriteThread.write(MessesDefine.Query.cmdQueryVersion(),null);
    }

    @Override
    public void activitySetting() {
        mWriteThread.write(MessesDefine.Query.cmdQueryWheelId(MessesDefine.CarWheel.LEFT_BACK),null);
        mWriteThread.write(MessesDefine.Query.cmdQueryWheelId(MessesDefine.CarWheel.LEFT_FRONT),null);
        mWriteThread.write(MessesDefine.Query.cmdQueryWheelId(MessesDefine.CarWheel.RIGHT_BACK),null);
        mWriteThread.write(MessesDefine.Query.cmdQueryWheelId(MessesDefine.CarWheel.RIGHT_FRONT),null);
        mWriteThread.write(MessesDefine.Query.cmdQueryErrorConfig(),null);
    }

    /*-------------------------------------------------------------数据更新-------------------------------------------------------------*/

    @Override
    public void updateMaxPressure(Integer pressure) {
        Model().getMaxPressure().setVal(pressure);
    }

    @Override
    public void updateMinPressure(Integer pressure) {
        Model().getMinPressure().setVal(pressure);
    }

    @Override
    public void updateMaxTemperature(Integer temperature) {
        Model().getMaxTemperature().setVal(temperature);
    }

    @Override
    public void updateVersion(String version) {
        Model().getVersion().setVal(version);
    }

    @Override
    public void updateTireSensorID(int index, Integer sensorID) {
        Model().getTireSensorID().setVal(index,sensorID);
    }

    @Override
    public void updateTirePressure(int index, Integer pressure) {
        Model().getTirePressure().setVal(index,(float)pressure);
    }

    @Override
    public void updateTireTemperature(int index, Integer temperature) {
        Model().getTireTemperature().setVal(index,temperature);
    }

    @Override
    public void updateLowPressure(int index, Integer value) {
        Model().getLowPressure().setVal(index,value);
    }

    @Override
    public void updateHighPressure(int index, Integer value) {
        Model().getHighPressure().setVal(index,value);
    }

    @Override
    public void updateHighTemperature(int index, Integer value) {
        Model().getHighTemperature().setVal(index,value);
    }

    @Override
    public void updateTireLeakageStatus(int index, Integer value) {
        Model().getTireLeakageStatus().setVal(index,value);
    }

    @Override
    public void updateLowVoltage(int index, Integer value) {
        Model().getLowVoltage().setVal(index,value);
    }

    @Override
    public void updateTireStatus(int index, Integer value) {
        Model().getTireStatus().setVal(index ,value);
    }

    @Override
    public boolean onWrited(byte[] data) {
        if(data != null){
            if(data.length > 4){
                byte cmd = data[4];
                switch(cmd){
                    case MessesDefine.FunctionCmd.SET_ERROR_CONFIG:
                        mWriteThread.write(MessesDefine.Query.cmdQueryErrorConfig(),null);
                        break;
                    case MessesDefine.FunctionCmd.SET_WHELL_ID:
                        if(data.length > 5){
                            byte wheelId = data[5];
                            mWriteThread.write(MessesDefine.Query.cmdQueryWheelId(wheelId),null);
                        }
                        break;
                }
            }
        }
        return false;
    }
}
