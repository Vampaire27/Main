package com.wwc2.main.bluetooth.driver;

/**
 * 蓝牙模块驱动接口.
 *
 * @author wwc2
 * @date 2017/1/5
 */
public interface BluetoothDriverable {

    /**
     * 设置本地蓝牙名字
     */
    void setLocalName(String name);

    /**
     * 设置本地蓝牙密码
     */
    void setPinCode(String pincode);

    /**
     * 连接最后个已配对设备
     */
    void connectLast();

    /**
     * 连接指定蓝牙地址a2dp设备
     */
    void connectA2dp(String addr);

    /**
     * 连接指定蓝牙地址hfp设备
     */
    void connectHFP(String addr);

    /**
     * 断开a2dp & hfp连接
     */
    void disconnect();

    /**
     * 断开a2dp连接
     */
    void disconnectA2DP();

    /**
     * 断开hfp连接
     */
    void disconnectHFP();

    /**
     * 删除指定已配对蓝牙设备, isEmpty删除所有蓝牙设备
     */
    void deletePair(String addr);

    /**
     * 开始搜索设备
     */
    void startDiscovery();

    /**
     * 停止搜索设备
     */
    void stopDiscovery();

    /**
     * 接通电话
     */
    void phoneAnswer();

    /**
     * 挂断电话/拒接电话
     */
    void phoneHangUp();

    /**
     * 拨打电话
     */
    void phoneDail(String phonenum);

    /**
     * 拨打分机号
     */
    void phoneTransmitDTMFCode(char code);

    /**
     * 语音切换到手机
     */
    void phoneTransfer();

    /**
     * 语音切换到车机
     */
    void phoneTransferBack();

    /**
     * 读取手机电话本
     */
    void phoneBookStartUpdate();

    /**
     * 清空电话本
     */
    void clearPhoneBook();

    /**
     * 清空通话列表
     */
    void clearTalkList();

    /**
     * 删除配对列表
     */
    void deletePairList(String macAddr);

    /**
     * 清空通话号码
     */
    void clearCallNumber();

    /**
     * 读取SIM电话本
     */
    void simBookStartUpdate();

    /**
     * 读取来电通话记录
     */
    void IncommingStartUpdate();

    /**
     * 读取去电通话记录
     */
    void OutgoingStartUpdate();

    /**
     * 读取未接通话记录
     */
    void MissedStartUpdate();

    /**
     * 播放音乐
     */
    void musicPlay();

    /**
     * 暂停音乐
     */
    void musicPause();

    /**
     * 强制暂停音乐
     */
    void ForcePauseMusic();

    /**
     * 停止音乐
     */
    void musicStop();

    /**
     * 上一曲
     */
    void musicPrevious();

    /**
     * 下一曲
     */
    void musicNext();

    /**
     * 1 = 进入配对
     * 2 = 取消配对
     */
    void PairMode(int type);

    /**
     * 重拨号码
     */
    void ReDail();

    /**
     * 语言拨号
     */
    void VoiceDial();

    /**
     * 取消语音拨号
     */
    void CancelVoiceDial();

    /**
     * SPK+MIC音量调试
     * volume[0] = spk
     * volume[1] = mic
     */
    void VolumeSet(String volume);

    /**
     * 麦克打开(1)/关闭(0)
     */
    void MicSwitch(char type);

    /**
     * 复位蓝牙模块
     */
    void ResetBluetooth();

    /**
     * 设置上电自动连接
     */
    void setAutoConnect();

    /**
     * 取消自动连接
     */
    void cancelAutoConnect();

    /**
     * 设置自动接听
     */
    void setAutoAccept();

    /**
     * 取消自动接听
     */
    void cancelAutoAccept();

    /**
     * 禁止蓝牙音乐
     */
    void musicMute();

    /**
     * 启动蓝牙音乐
     */
    void musicUnmute();

    /**
     * 减半蓝牙音乐
     */
    void musicBackground();

    /**
     * 恢复蓝牙音乐
     */
     void musicNormal();

    /**
     * 通过OPP发送文件给手机
     */
    void OppSendFile(String path);

    /**
     * 连接spp,isEmpty为连接当前spp
     */
    void SppConnect(String addr);

    /**
     * 发送spp数据
     * data[0] = spp index
     */
    void SppSendData(String data1,String date2);

    /**
     * 断开spp
     */
    void SppDisConnect();

    /**
     * 连接Hid, isEmpty为连接当前的hid
     */
    void HidConnect(String addr);

    /**
     * Hid 鼠标移动
     * data[0] = key(1 = down 0 = up)
     * data[1]-data[4] = x
     * data[5]-data[8] = y
     */
    void HidMouseMove(String data);

    /**
     * HID home按键
     */
    void HidHomeKey();

    /**
     * HID back按键
     */
    void HidBackKey();

    /**
     * HID menu按键
     */
    void HidMenuKey();

    /**
     * 断开Hid
     */
    void HidDisConnect();

    /**
     * 设置车机可投射区域分辨率
     * data[0]-data[3] = x
     * data[4]-data[7] = y
     */
    void SetHidResolution(String data);

    /**
     * pan 连接, isEmpty为连接当前pan
     */
    void PanConnect();

    /**
     * 断开 pan
     */
    void PanDisConnect();

    /**
     * 打开蓝牙设备
     */
    void OpenBt();

    /**
     * 关闭蓝牙设备
     */
    void CloseBt(int whoClose);

    /**
     * 设置蓝牙音乐音量 0 - 30
     */
    void SetMusicVal(int val);

    /**
     * BC6 蓝牙pskey升级goc.psr模块寄存器文件
     */
    void UpdatePskey();

    /**
     * BC6蓝牙产线测试模式接口
     */
    void TestMode(char ch);

    /**
     * 设置通话走模块('1')/通话走ARM('0')
     */
    void setAudioSoft(char type);

    /**
     * 删除联系人条目
     */
    void deleteContactItem(String number);

    /**
     * 收藏联系人
     */
    void addToFavorite(String number);

    /**
     * 移除收藏的联系人
     */
    void removeFromFavorite(String number);

    /**
     * 清空搜索列表
     */
    void clearSearchList();
}
