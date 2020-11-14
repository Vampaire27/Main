package com.wwc2.main.driver.mcu.driver;

/**
 * the mcu define.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class McuDefine {

    /**ARM 发送给 MCU命令定义*/
    public static class ARM_TO_MCU {
        /**CPU启动后向MCU发送握手信号，表明CPU串口准备好*/
        public static final int RPT_SysInitOK = 0x01;
        /**预留：CPU请求MCU版本信息*/
        public static final int REQ_McuVer = 0x02;
        /**CPU报告当前界面状态*/
        public static final int RPT_SysUImode = 0x03;
        /**CPU报告当前媒体源*/
        public static final int RPT_SysMediamode = 0x04;
        /**CPU请求关闭／打开静音*/
        public static final int REQ_SysMute = 0x05;
        /**CPU请求音量大小*/
        public static final int REQ_Requst_Vol = 0x06;
        /**CPU请求改变音量大小*/
        public static final int REQ_Change_Vol = 0x07;
        /**CPU进入关机流程，MCU收到此指令，延时后可完全关电*/
        public static final int RPT_SysPoweroff = 0x08;
        /**CPU告知MCU媒体信息*/
        public static final int RPT_MediaInfo = 0x09;
        /**CPU告知MCU电话信息*/
        public static final int RPT_BtInfo = 0x0A;
        /**CPU设置EQ信息*/
        public static final int RPT_SetEQinfo = 0x0B;
        /**CPU设置SUBWOOFER信息*/
        public static final int RPT_SetSubwoofer = 0x57;
        /**CPU设置喇叭平衡信息*/
        public static final int RPT_SetBlanceInfo = 0x0C;
        /**CPU设置音源默认音量*/
        public static final int RPT_Media_SourceVol = 0x0D;
        /**CPU请求硬件版本号*/
        public static final int REQ_HardwareVer = 0x0E;
        /**报告当前设置的时间*/
        public static final int RPT_SetTime = 0x10;
        /**查询倒车状态*/
        public static final int RPT_RequestReverse = 0x11;
        /**查询ACC状态*/
        public static final int RPT_RequestAcc = 0x12;
        /**查询大灯状态*/
        public static final int RPT_RequestILL = 0x13;
        /**报告GPS发声*/
        public static final int RPT_GPS_SPEAKER = 0X14;
        /**报告媒体播放状态*/
        public static final int RPT_MEDIA_PLAY_ST = 0x15;
        /**报告屏幕的触控按键*/
        public static final int RPT_CPU_SEND_MENIU_KEY = 0x16;
        /**报告至MCU按键出声*/
        public static final int RPT_KEY_SPEAKER = 0x17;
        /**报告至MCU，有声音要播放，请切换ARM通道*/
        public static final int RPT_CPU_SEND_SOUND = 0x18;
        /**对应Cpu命令(0x19~0x1a) Gps音频获取 */
        public static final int MACK_GpsAudioGet = 0x19;
        /**对应Cpu命令(0x19~0x1a) Gps音频设置 */
        public static final int MACK_GpsAudioSet = 0x1a;
        /**对应Cpu命令(0x19~0x1a) 倒车音和按键音设置 */
        public static final int MACK_AsternPressVolSet = 0x1b;
        /**请求其它设置的状态信息*/
        public static final int RPT_REQ_OTHERSET_INFO = 0x1c;
        /**CPU通知MCU，CPU要进入恢复模式*/
        public static final int RPT_CPU_Enter_Recover = 0x1d;
        /**ARM发送心跳包给MCU*/
        public static final int REQ_HeartBeat = 0x1e;
        /**打开或关闭收音机（保留、未使用）*/
        public static final int OP_RadioSwitch = 0x20;
        /**功能操作(选台、波段切换等)*/
        public static final int OP_RadioButton = 0x21;
        /**设置收音机区域*/
        public static final int OP_RadioSetregion = 0x24;
        /**APP请求收音区域信息*/
        public static final int OP_RadioRequstregion = 0x25;
        /**APP设置收音有源天线*/
        public static final int OP_RadioAerial = 0x26;
        /**APP设置收音模块类型*/
        public static final int OP_RadioModule = 0x27;
        /**ARM发送FM发射数据*/
        public static final int OP_FmTransmitter = 0x28;
        /**ARM向MCU请求RDS控制信息*/
        public static final int OP_RDSRequest   = 0x29;
        /**ARM向MCU请求RDS PTY和PS信息*/
        public static final int OP_RDSPtyPs     = 0x2A;
        /**进入方控界面*/
        public static final int OP_SteerStatus = 0x30;
        /**需要学习的按健*/
        public static final int OP_SteerKey = 0x31;
        /**操作保存或清除当前方向盘学习的设置*/
        public static final int OP_SteerButton = 0x32;

        /**进入面板按键学习界面*/
        public static final int OP_PanelKeyStatus = 0x33;
        /**需要学习的按健*/
        public static final int OP_PanelKey = 0x34;
        /**操作保存或清除当前面板学习的设置*/
        public static final int OP_PanelButton = 0x35;

        /**设置音频DSP数据*/
        public static final int OP_DSPInfo = 0x37;

        /**四方位通信串口转发命令(0x42) 【锐派】*/
        public static final int OP_TOUCH_POINT = 0x42;
        /**后视检测开关(0x43)*/
        public static final int OP_CAMERA_SWITCH = 0x43;
        /**摄像头供电开关(0x44) 【锐派】*/
        public static final int OP_CARMERA_POWER = 0x44;

        /**导入面板按键数据*/
        public static final int OP_PanelKeyImport = 0x38;
        /**配置是否支持CVBS*/
        public static final int OP_SupportCVBS = 0x39;//为1表示有视频输出  0表示无视频输出
        /**前视状态命令*/
        public static final int OP_FRONT_CAMERA = 0x3A;//1：进入前视 0：退出前视
        /**右视状态命令 0X3B*/
        public static final int OP_RIGHT_CAMERA = 0x3B;//1：进入右视 0：退出右视

        /**播放曲目信息 0x3d*/
        public static final int OP_MEDIA_TIME_INFO = 0x3D;//data0：时 data1：分钟 data2：秒钟
        /**通话信息 0x3e*/
        public static final int OP_BTCALL_INFO = 0x3E;//data0：通话时间分钟 data1：通话时间秒钟 data2-N：电话号码ASCII码，最后发一个0作为电话号码结束标志

        /**CPU发送DSP数据给MCU（MCU将透传给dsp模块）*/
        public static final int OP_DSP_DATA = 0x41;

        /**通知MCU进入升级命令*/
        public static final int OP_McuUpdataCMD = 0x50;
        /**MCU升级初始化命令*/
        public static final int OP_McuUpdataInit = 0x51;
        /**给MCU发送升级帧*/
        public static final int OP_McuUpdataFrame = 0x52;

        /**通知MCU mute 2s，然后MCU自动解mute，解决POP音的问题。*/
        public static final int OP_MUTE_POP = 0x59;

        /**音乐、视频播放状态等信息*/
        public static final int OP_MEDIA_STATUS_INFO = 0x5B;

        /**外部模块的串口设置*/
        public static final int OP_ExternModSet = 0x60;
        /**ARM发给外部模块的串口数据
         * MCU收到此数据采用IR与盒子通讯*/
        public static final int OP_ExternIRData = 0x61;
        /**360全景通讯方式设置
         * MCU暂没有用到此数据*/
        public static final int OP_ExternIRSet = 0x62;
        /**ARM发给360全景的串口数据
         * MCU收到此数据采用UART与盒子通讯*/
        public static final int OP_ExternUartData = 0x63;

        /**MCU转发TBOX串口数据（0x64）*/
        public static final int OP_SendData_TBOX = 0x64;

        /**ARM通知mcu七彩灯设置*/
        public static final int REQ_colorfulLight = 0x65;

        /**ARM通知威益德mcu七彩灯设置*/
        public static final int REQ_colorfulLight_WYD = 0x56;

        public static final int RPT_MCU_IMPORT_STATE = 0x66;
        /**工厂模式声音通道设置*/
        public static final int OP_soundChannel = 0x67;

        /**停车监控开关、电瓶选择(0x6F) 【威益德货车】*/
        public static final int OP_MONITOR_SWITCH = 0x6F;

        /**声音通道的切换*/
        public static final int OP_VolumeChannel = 0x70;
        /**混音通道切换*/
        public static final int OP_MIXChannel = 0x71;
        /**原车相关声音通道**/
        public static final int OP_CarChannel = 0x72;

        /**Arm断电由MCU记忆当前播放曲目和盘符*/
        public static final int OP_MEDIA_INFO = 0x73;

        /**通知MCU当前Logo显示状态1：显示（静音）；0：不显示（解静音）*/
        public static final int OP_LOGO_STATE = 0x74;

        /**请求MCU调试信息*/
        public static final int OP_REQUEST_MCU_DEBUG = 0x75;

        /**VCOM设置*/
        public static final int OP_SET_VCOM = 0x76;

        /**时间发送*/
        public static final int OP_SEND_TIME = 0x77;
        /**配置MCU重启ARM的时间*/
        public static final int OP_SEND_TIME_TEST = 0x78;

        /**通知MCU抓拍状态*/
        public static final int OP_PHOTO_STATUS = 0x79;//1:开始 2:结束

        public static final int OP_ROTATE_SCREEN = 0x7A;//旋转屏旋转命令

        /**360摄像头开关切换命令(0x7D) 【锐派】*/
        public static final int OP_CAMERA360_SWITCH = 0x7D;//DATA0= 0、切到前摄像头；1、切到后摄像头

        /**散热风扇控制命令*/
        public static final int OP_FAN_CONTROL = 0x7B;
        /**旋转屏参数设定*/
        public static final int OP_ROTATE_PARAM = 0x7C;

        /**ARM-MCU的ACK通道*/
        public static final int OP_ACK = 0x7f;
        /**ARM-MCU的Canbus数据*/
        public static final int OP_CanbusData = 0x40;
    }

    public static class MCU_TO_ARM {
        /**MCU向CPU发送初始化数据*/
        public static final int MACK_SysInitdata = 0x81;
        /**MCU向CPU发送版本号信息*/
        public static final int MRPT_McuVer = 0x82;

        /**MCU发送GPS数据给CPU(0x82)【锐派】*/
        public static final int MRPT_GPS_DATA = 0x82;
        /**数据包=DATA0~DATAN（GPS北斗标准数据格式）
                $GNGGA
                $GPGSA
                $BDGSA
                $GNRMC
         */

        /**MCU向CPU发送硬件版本号信息（比如2.0或2.1）*/
        public static final int MRPT_HardwareVer = 0x83;
        /**MCU向CPU发送静音状态*/
        public static final int MRPT_MuteSt = 0x85;
        /**MCU向CPU发送音量信息*/
        public static final int MACK_SysVol = 0x86;
        /**MCU发送转向灯状态*/
        public static final int MRPT_CarTurnSW = 0x87;
        /**ACC状态（0x89）[用于远程端显示车状态]*/
        public static final int MRPT_RDS_TIME_ACC_STATUS = 0x89;
        /**收到RDS信号时更新系统时间*/
//        public static final int MRPT_RDS_TIME = 0x89;
        /**MCU发送ACC状态*/
        public static final int MRPT_CarAcc = 0x8a;
        /**MCU 发送倒车状态*/
        public static final int MRPT_CarReverse = 0x8b;
        /**MCU 发送手刹状态*/
        public static final int MRPT_CarBrake = 0x8c;
        /**MCU 发送大灯状态*/
        public static final int MRPT_CarILL = 0x8d;
        /**MCU发送左右转向灯状态*/
        public static final int MPRT_LEFT_RIGHT_ST = 0x8e;//以前定义
        /**DSP数据flash应答信号*/
        public static final int MPRT_DSP_FLASH = 0x8f;

        /**MCU转发TBOX串口数据给ARM（0x94）*/
        public static final int MPRT_REV_TBOX_DATA = 0x94;

        /**对应Cpu命令(0x19~0x1a) Gps音频设置的应答 */
        public static final int MRPT_GPS_SET_INFO = 0x9a;
        /**对应Cpu命令(0x19~0x1a) 倒车音和按键音设置 */
        public static final int MRPT_OTHER_SET_INFO = 0x9b;
        /**对应Cpu命令(0xc) Eq设置 */
        public static final int MRPT_EQ_SET_INFO = 0x9c;
        /**SUBWOOFER设置 */
        public static final int MART_SUBWOOFER_INFO = 0xb9;
        /**MCU告知ARM声音平衡的状态*/
        public static final int MRPT_BANLANCE_INFO = 0x9d;
        /**MCU告知ARM默认音量*/
        public static final int MRRT_PowerOn_Default_SourceVol = 0x9e;
        /**初始化或者切波段更新数据*/
        public static final int MRPT_RadioInitdata = 0xa0;
        /**更新主频数据*/
        public static final int MRPT_RadioMaindata = 0xa1;
        /**更新预存频点数据*/
        public static final int MRPT_RadioPredata = 0xa2;
        /**更新标志位*/
        public static final int MRPT_RadioFlag = 0xa3;
        /**发送收音区域信息*/
        public static final int MRPT_RadioRegion = 0xa4;
        /**收音有源天线开关状态*/
        public static final int MRPT_RadioAerial = 0xa5;
        /**收音机模块类型*/
        public static final int MRPT_RadioModule = 0xa6;
        /**RDS的PS信息*/
        public static final int MRPT_RDS_PSINFO = 0xa7;
        /**MCU通知ARM是否支持混音调节*/
        public static final int MRPT_GPS_MIX_SUPPORT = 0xa8;
        /**初始化Fm发射数据*/
        public static final byte MRPT_FmInitdata = (byte) 0xa9;
        /**Fm发射状态*/
        public static final byte MRPT_FmState = (byte) 0xab;
        /**MCU向ARM报告RDS控制信息*/
        public static final byte MRPT_RDSControl = (byte) 0xAC;
        /**MCU向ARM报告RDS PTY和PS信息*/
        public static final byte MRPT_RDSPtyPs = (byte) 0xAD;
        /**方控学习状态*/
        public static final int MRPT_SteerStatus = 0xb0;
        /**学习后的KEY状态*/
        public static final int MPRT_SteerKeyres = 0xb1;
        /**学习后的KEY的信息*/
        public static final int MPRT_SteerKeyinfo = 0xb2;

        /**面板按键学习状态*/
        public static final int MRPT_PanelKeyStatus = 0xb4;
        /**学习后的KEY的信息*/
        public static final int MPRT_PanelKeyinfo = 0xb5;
        /**面板按键按下后，上报健值*/
        public static final int MPRT_PanelKeyPress = 0xb6;

        /**上报音频DSP数据*/
        public static final int MPRT_DSPInfo = 0xB7;

        /**取消平衡设置界面命令*/
        public static final int MPRT_SoundFieldHidden = 0xbb;

        /**导出面板按键数据*/
        public static final int MPRT_PanelKeyOutport = 0xBC;
        /**右视检测状态*/
        public static final int MPRT_Right_Camera = 0xBD;//1：有右视 0：无右视

        /**MCU发送用户操作指令*/
        public static final int MOP_UserKEY = 0xc0;
        /**电源电压上报*/
        public static final int MPRT_VOLTAGE = 0xc1;
        /**MCU上报车门、尾箱、计价器等状态*/
        public static final int MPRT_CAR_STATUS = 0xC4;

        /**自动背光调节数据(0xC3) 【天宇德】*/
        public static final int MPRT_AUTO_BACKLIGHT = 0xc3;//0~255，值越大外部光线越亮

        /**MCU将DSP状态数据封装成标准格式发给CPU*/
        public static final int MRPT_DSP_DATA = 0xD1;

        /**MCU进入IAP 退出IAP指令*/
        public static final int MPRT_McuInIAP = 0xe0;
        /**MCU请求帧数据*/
        public static final int MRPT_McuRequestFrame = 0xe1;
        /**MCU升级成功或者失败*/
        public static final int MRPT_McuUpdataFinish = 0xe2;
        /**MCU反馈七彩灯设置状态*/
        public static final int MRPT_colorfulLight = 0xe4;
        /**MCU反馈七彩灯设置状态*/
        public static final int MRPT_colorfulLight_WYD = 0xb8;
        /**MCU反馈声音通道设置*/
        public static final int MRPT_soundChannel = 0xe5;
        /**Arm断电由MCU记忆当前播放曲目和盘符*/
        public static final int MRPT_MEDIA_INFO = 0xe6;
        /**MCU断USB电通知ARM，针对电源实验状态*/
        public static final int MRPT_CLOSE_USB = 0xe7;
        /**方控AD值*/
        public static final int MRPT_STEER_AD = 0xe8;
        /**MCU调试信息*/
        public static final int MRPT_MCU_DEBUG_INFO = 0xe9;
        /**外部模块发的数据直接转给ARM*/
        public static final int MRPT_ExternalModData = 0xf0;
        /**MCU转发CANBUS数据的命令*/
        public static final int MRPT_CanbusData      = 0xd0;

        /**MCU上电成功通知ARM进行拍照*/
        public static final int MRPT_PHOTO_STATUS = 0xF1;//1:上电完成

        /**MCU告诉ARM横竖屏状态(0xf2)*/
        public static final int MRPT_LAND_PORT_STATUS = 0xF2;

        /**应答ARM*/
        public static final int MRPT_ACK = 0xff;
    }
}