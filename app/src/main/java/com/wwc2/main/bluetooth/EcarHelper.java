package com.wwc2.main.bluetooth;

import android.content.Context;
import android.content.Intent;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.manager.McuManager;
import com.wwc2.voiceassistant_interface.VoiceAssistantDefine;
import com.wwc2.voiceassistant_interface.VoiceAssistantInterface;

/**
 *
 * @author huwei
 * @date 17-10-18
 */

public class EcarHelper {

    /**
     * 方案商接收翼卡的广播所使用的Action名称
     **/
    public static final String ACTION_ECAR_SEND = "com.android.ecar.send";
    /**
     * 方案商发送至翼卡的所使用的Action名称
     **/
    public static final String ACTION_ECAR_RECV = "com.android.ecar.recv";
    /**
     * VOIP 电话通话状态的广播消息
     */
    //1)来电或响铃状态:
    public static final String ACTION_CALL_INCOMING = "com.ecar.call.incoming";
    //2)通话中状态:
    public static final String ACTION_CALL_OFFHOOK = "com.ecar.call.offhook";
    // 3)空闲状态:
    public static final String ACTION_CALL_IDLE = "com.ecar.call.idle";
    /**
     * CMD参数所用的标示
     **/
    public static final String _CMD_ = "ecarSendKey";
    /**
     * TYPE参数的标示
     **/
    public static final String _TYPE_ = "cmdType";
    /**
     * _TYPE_ = "standCMD"时为普通广播
     **/
    public static final String _TYPE_STANDCMD_ = "standCMD";
    /**
     * call state
     */

    public static final String ACCOFF = "ACC_OFF";

    public static final String ACCON = "ACC_ON";


    public static final int BT_CALL_IDLE = 3;//挂断
    public static final int BT_CALL_RINGING = 4;//来电响铃和去电已拨出
    public static final int BT_CALL_OFFHOOK = 5;//去电接通

    /**
     * bt state
     */
    public static final int BT_OFF = 0; //关闭
    public static final int BT_DISCONNECT = 1; //未连接蓝牙设备
    public static final int BT_CONNECTED = 2; //已连接蓝牙设备
    /**
     * 所传递的参数列表,每个参数名会用逗号分开,需要解释出参数名,再以此作为Key取出数据
     */
    public static String _KEYS_ = "keySet";

    /**
     * 翼卡电话包名
     */
    public static final String E_CAR_VOIP = "com.coagent.voip";
    public static final String E_CAR_CALL = "com.coagent.ecar";

    private static boolean mIsEcarActive = false;//翼云电话状态
    private static boolean mIsEcarPhone = false;//翼卡电话状态

    /**
     * 车主没有激活，ACC ON 20次之后，直接全屏跳出二维码遮挡功能界面，激活后二维码消失（随板补的按此执行，强制用户注册激活）
     */
    public static final String E_CAR_OCCUPY_SCREEN = "ecar_action_force_occupy_screen";
    static boolean mEcarRegistState = true;

    public static final String ACTION_OCCUPY_SCREEN = "wwc2_sim_auth_fail";

    enum CMDFormEcar {
        //查询蓝牙状态
        BluetoothQueryState,
        //打开蓝牙界面
        BluetoothConnect,
        //拨打蓝牙电话 tmpIntent.putExtra(“name”, name);tmpIntent.putExtra(“number”, number);
        BluetoothMakeCall,
        //当”oper”参数为”hide”时隐藏拨号界面,”oper”参数为”show”时取消隐藏
        HideCallUI,
        BeginVoipCall,
        EndVoipCall,
        BluetoothEndCall,
    }

    enum CMDToEcar {
        BluetoothState, //发送蓝牙状态
        CallState,   //发送通话状态
        VoipMakeCall, //语音拨打翼云电话
        StartVoip, //显示翼云电话界面
    }

    /**
     * @param cmd   命令类型
     * @param state 命令内容
     */
    public static void sendMsgToEcar(Context context, String cmd, String state) {
        Intent intent = new Intent(ACTION_ECAR_RECV);
        intent.putExtra(_CMD_, cmd);
        intent.putExtra(_TYPE_, _TYPE_STANDCMD_);
        intent.putExtra(_KEYS_, state);
        intent.putExtra("state", state);
        context.sendBroadcast(intent);
        LogUtils.d("BluetoothLogic", "cmd:" + cmd + "\tstate:" + state);
    }


    public static void sendMsgToEcar_2(Context context, String cmd) {
        Intent intent = new Intent(ACTION_ECAR_RECV);
        intent.putExtra(_CMD_, cmd);
        intent.putExtra(_TYPE_, _TYPE_STANDCMD_);
        intent.putExtra(_KEYS_, "");
        context.sendBroadcast(intent);
        LogUtils.d("SEND TO ECAR", "cmd:" + cmd );
    }

    public static void setEcarState(boolean active) {
        LogUtils.d("EcarHelper", "setEcarState  "+active);
        mIsEcarActive = active;

        CoreLogic logic = LogicManager.getLogicByName(VoiceAssistantDefine.MODULE);
        if (logic != null) {
            Packet packet1 = new Packet();
            if (active) {
                //退出语音
                packet1.putBoolean("open", false);
                logic.Notify(false, VoiceAssistantInterface.MainToApk.KEY_TRIGGER_VOICE, packet1);
            }

            //禁用语音
            packet1.putBoolean("active", !active);
            logic.Notify(VoiceAssistantInterface.MainToApk.KEY_ENABLE_VOICE, packet1);
        }

        byte[] data = new byte[2];
        data[0] = 0x0d;
        data[1] = (byte) (active ? 0x03 : 0x01);
        McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_BtInfo, data, data.length);

        //修改翼卡电话时调节音量无作用。
        VolumeDriver.Driver().setVolumeType(active ? Define.VolumeType.BT_HFP : Define.VolumeType.DEFAULT);
    }

    public static boolean getEcarState() {
        return mIsEcarActive;
    }

    public static void setEcarPhoneState(boolean active) {
        LogUtils.d("EcarHelper", "setEcarPhoneState  "+active);
        mIsEcarPhone = active;
    }

    public static boolean getEcarPhoneState() {
        return mIsEcarPhone;
    }

    public static void setEcarRegistState(boolean regist) {
        mEcarRegistState = regist;
    }

    public static boolean getEcarRegistState() {
        return mEcarRegistState;
    }
}
