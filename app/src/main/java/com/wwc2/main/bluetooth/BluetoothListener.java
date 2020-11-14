package com.wwc2.main.bluetooth;

import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.IntegerSS;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerSSS;

/**
 * the bluetooth listener.
 *
 * @author wwc2
 * @date 2017/1/5
 */
public class BluetoothListener extends BaseListener {

    @Override
    public String getClassName() {
        return BluetoothListener.class.getName();
    }

    /**
     * 模块状态监听器
     */
    public void ModuleStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 当前HFP状态改变监听器.
     */
    public void HFPStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 蓝牙本地名字监听器
     */
    public void LocalNameListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙本机MAC地址监听器
     */
    public void LocalMacAddrListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙PIN码监听器
     */
    public void PinCodeListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙自动连接监听器
     */
    public void AutoConnectListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * 蓝牙自动应答监听器
     */
    public void AutoAnswerListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * 蓝牙配对名字监听器
     */
    public void PairingNameListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙配对MAC地址监听器
     */
    public void PairingMacAddrListener(String oldVal, String newVal) {

    }

    /**
     * 通话时间监听器
     */
    public void CallTimeListener(Long oldVal, Long newVal) {

    }

    /**
     * 通话号码监听器
     */
    public void CallNumberListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙音乐状态监听器
     */
    public void MusicStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 蓝牙音乐艺术家名称监听器
     */
    public void MusicArtistNameListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙音乐歌曲名称监听器
     */
    public void MusicSongNameListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙音乐playtime监听器
     */
    public void MusicPlaytimeListener(Integer oldVal, Integer newVal) {

    } /**
     * 蓝牙音乐index监听器
     */
    public void MusicIndexListener(Integer oldVal, Integer newVal) {

    } /**
     * 蓝牙音乐total监听器
     */
    public void MusicTotalListener(Integer oldVal, Integer newVal) {

    } /**
     * 蓝牙音乐专辑名称监听器
     */
    public void MusicAlbumNameListener(String oldVal, String newVal) {

    }

    /**
     * 蓝牙通话声音是否在手机监听器
     */
    public void VoiceInPhoneListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * 蓝牙MIC是否静音监听器
     */
    public void MicMuteListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * 蓝牙版本监听器
     */
    public void VersionListener(String oldVal, String newVal) {

    }

    /**
     * A2DP连接状态监听器
     */
    public void A2DPConnectStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 蓝牙模块开关状态监听器
     */
    public void OpenOrCloseListener(Boolean oldVal, Boolean newVal) {

    }

    /**
     * 电话本列表监听器
     */
    public void PhonebookListListener(IntegerSSBoolean[] oldVal, IntegerSSBoolean[] newVal) {

    }

    /**
     * 电话本列表下载状态监听器
     */
    public void PhonebookDownloadStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 手机电话本列表下载状态监听器
     */
    public void PhoneDownloadStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * SIM电话本列表下载状态监听器
     */
    public void SIMDownloadStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 通话列表监听器
     */
    public void TalkListListener(IntegerSSS[] oldVal, IntegerSSS[] newVal) {

    }

    /**
     * 已拨通话列表下载状态监听器
     */
    public void DialTalkListDownloadStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 已接通话列表下载状态监听器
     */
    public void IncomingTalkListDownloadStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 未接通话列表下载状态监听器
     */
    public void MissedTalkListDownloadStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 通话列表下载状态监听器
     */
    public void TalkListDownloadStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 搜索列表监听器
     */
    public void SearchListListener(IntegerSS[] oldVal, IntegerSS[] newVal) {

    }

    /**
     * 搜索列表状态监听器
     */
    public void SearchListStatusListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 蓝牙通话声音通道监听器
     */
    public void AudioSoftListener(Integer oldVal, Integer newVal) {

    }

    /**
     * 蓝牙已下载联系人数目监听器
     */
    public void ContactsNumberListener(Integer oldVal, Integer newVal) {

    }
    /**
     * 蓝牙已配对列表监听器
     */
    public void PairListListener(IntegerSS[] oldVal, IntegerSS[] newVal) {

    }
}
