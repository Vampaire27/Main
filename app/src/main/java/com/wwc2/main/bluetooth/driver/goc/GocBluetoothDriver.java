package com.wwc2.main.bluetooth.driver.goc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.IGocsdkService;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.custom.IntegerSS;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerSSS;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.bluetooth.driver.BaseBluetoothDriver;
import com.wwc2.main.driver.mcu.driver.McuAdapter;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.phonelink.PhonelinkLogic;
import com.wwc2.main.settings.util.ClickFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 文强蓝牙模块驱动实现.
 *
 * @author wwc2
 * @date 2017/1/5
 */
public class GocBluetoothDriver extends BaseBluetoothDriver {

    private static String TAG = "GocBluetoothDriver";
    private static String SERVICE_NAME = "com.goodocom.gocsdk.service.GocsdkService";
    private static String SERVICE_PACKET_NAME = "com.goodocom.gocsdk";
    private static String SERVICE_CLASS_NAME = "com.goodocom.gocsdk.service.GocsdkService";
    private List<IntegerSSBoolean> contactList = new ArrayList<>();
    protected IGocsdkService mAIDLGocsdk = null;

    private GocCallback mCallbackGocsdk = null;

    /**
     * 已同步联系人数目
     */
    private int downloadedContacts;

    /**
     * 联系人数目下发定时器id
     */
    private int contactsTimerID = -1;
    private static final int KILL_CONTACTTIMER_WHAT = 1;
    private static final int MSG_TRANSFER = 2;
    private static final int MSG_TRANSFER_BACK = 3;

    // handle <java.lang.IllegalArgumentException:Service not registered> exception.
    private boolean mIsBound = false;

    //是否需要获取蓝牙init数据标志位
    private boolean needInit = false;

    private Handler handler;

    /**
     * 强制挂断定时器,点击挂断后DELAY_HANGUP_TIME内仍未收到挂断回调,则直接置为挂断状态
     */
    private int delayHangupTimerID = -1;
    private final int DELAY_HANGUP_TIME = 2000;


    private class ContactsHandler extends Handler {
        public ContactsHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == KILL_CONTACTTIMER_WHAT && contactsTimerID > 0) {
                TimerUtils.killTimer(contactsTimerID);
                contactsTimerID = -1;
                downloadedContacts = 0;
            } else if (msg.what == MSG_TRANSFER) {
                handler.removeMessages(MSG_TRANSFER);
                handler.removeMessages(MSG_TRANSFER_BACK);
                if (null != mAIDLGocsdk) {
                    try {
                        mAIDLGocsdk.phoneTransfer();
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if (msg.what == MSG_TRANSFER_BACK) {
                handler.removeMessages(MSG_TRANSFER);
                handler.removeMessages(MSG_TRANSFER_BACK);
                if (null != mAIDLGocsdk) {
                    try {
                        mAIDLGocsdk.phoneTransferBack();
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * the listener
     */
    private BluetoothListener mListener = new BluetoothListener() {

        @Override
        public void PairingMacAddrListener(String oldVal, String newVal) {
            if (null != oldVal && null != newVal && !newVal.equals(oldVal)) {
                contactList.clear();
            }
        }

        @Override
        public void MusicStatusListener(Integer oldVal, Integer newVal) {
            final boolean oldPlay = BluetoothDefine.MusicStatus.isPlay(oldVal);
            final boolean newPlay = BluetoothDefine.MusicStatus.isPlay(newVal);
            if (oldPlay != newPlay) {
                if (newPlay) {
                    // play
                    InquiryMusicInfo();
                }
            }
        }
    };
    private int mDiscoveryTimerId = 0;

    /**
     * 文强回调文件.
     */
    public class GocCallback extends IGocsdkCallback.Stub {
        BluetoothModel Model() {
            return GocBluetoothDriver.this.Model();
        }

        @Override
        public void onHfpConnected() throws RemoteException {
            //忽略已连接状态下来的connect状态;如通话状态来connect会错误判断为挂断;解决bug10433通话窗口闪一下问题
            if (!BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
                return;
            }
            setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT, false);
            Model().getVoiceInPhone().setVal(false);
            updatePairingItem(BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT);
        }


        @Override
        public void onCallSucceed() throws RemoteException {
            setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_OUT_CALLING, false);
            mLatestCallType = BluetoothDefine.CallType.CALL_TYPE_OUT_GOING;
        }

        @Override
        public void onHfpDisconnected() throws RemoteException {

            //如果是正在通话中断开连接，则保存通话记录
            if (BluetoothDefine.HFPStatus.isCalling(Model().getHFPStatus().getVal())) {
                savePhoneRecord();
                Model().getVoiceInPhone().setVal(true);
            }
            updatePairingItem(BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT);

            setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT, false);
            if (delayHangupTimerID > 0) {
                TimerUtils.killTimer(delayHangupTimerID);
            }
        }

        private void updatePairingItem(int connectState) {
            IntegerSS[] pairList = Model().getPairList().getVal();
            if (pairList == null) {
                return;
            }
            for (int i = 0; i < pairList.length; i++) {
                IntegerSS integerSS = pairList[i];
                if (integerSS != null && integerSS.getString2() != null) {
                    if (integerSS.getString2().equals(Model().getPairingMacAddr().getVal())) {
                        Model().getPairList().setVal(i, new IntegerSS(connectState,
                                integerSS.getString1(), integerSS.getString2()));
                    }
                }
            }
        }

        @Override
        public void onIncoming(String number) throws RemoteException {

            LogUtils.d(TAG, "onIncoming number :" + number);
            setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_IN_CALLING, false);
            Model().getCallNumber().setValAnyway(number);
            mLatestCallType = BluetoothDefine.CallType.CALL_TYPE_MISSED;
        }

        @Override
        public void onHangUp() throws RemoteException {
            setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_HANGUP, false);
            LogUtils.d(TAG, "onHangup");
            if (delayHangupTimerID > 0) {
                TimerUtils.killTimer(delayHangupTimerID);
            }
        }

        @Override
        public void onTalking() throws RemoteException {
            if (BluetoothDefine.CallType.CALL_TYPE_MISSED == mLatestCallType) {
                // 自动接听电话，要认为是来电话了
//                if (Model().getAutoAnswer().getVal()) {
                mLatestCallType = BluetoothDefine.CallType.CALL_TYPE_INCOMMING;
//                }
            }
            setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_TALKING, false);
        }

        @Override
        public void onRingStart() throws RemoteException {


        }

        @Override
        public void onRingStop() throws RemoteException {


        }

        @Override
        public void onHfpLocal() throws RemoteException {

            Model().getVoiceInPhone().setVal(true);
        }

        @Override
        public void onHfpRemote() throws RemoteException {

            Model().getVoiceInPhone().setVal(false);
        }

        @Override
        public void onInPairMode() throws RemoteException {

            Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_ING);
        }

        @Override
        public void onExitPairMode() throws RemoteException {

            Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_NONE);
        }

        @Override
        public void onOutGoingOrTalkingNumber(String number) throws RemoteException {

            Model().getCallNumber().setValAnyway(number);
            if (BluetoothDefine.CallType.CALL_TYPE_MISSED == mLatestCallType) {
                // 自动接听电话，要认为是来电话了
//                if (Model().getAutoAnswer().getVal()) {
                mLatestCallType = BluetoothDefine.CallType.CALL_TYPE_INCOMMING;
//                }
            }
        }

        @Override
        public void onInitSucceed() throws RemoteException {
            LogUtils.d(TAG, "onInitSucceed: 蓝牙初始化成功");

            if (needInit) {
                needInit = false;
//                Model().getOpenOrClose().setValAnyway(true);
                if (isClosedByUser) {
                    LogUtils.d(TAG, "onServiceConnected()+" + "CloseBt()");
                    CloseBt(BluetoothDefine.BluetoothClosedBy.USER);
                } else {//蓝牙开关增加记忆2019-11-20
                    if (Model().getOpenOrClose().getVal()) {
                        OpenBt();
                    } else {
                        CloseBt(BluetoothDefine.BluetoothClosedBy.NORMAL);
                    }
                }
                // 开机绑定服务时禁止蓝牙出声
                musicMute();

                //解决：修改蓝牙名称》搜索设备》连接已搜索到设备（魅族手机）》蓝牙连接后》断开已连接设备》
                //再连接搜索到的另一设备》再断开当前连接设备》删除》断电》再上电》进入
                //蓝牙名称会自动变回默认名称。
                //连接上主动设置一次配对名及PIN，2018-10-13
//                String localName = Model().getLocalName().getVal();
//                if (localName != null) {
//                    setLocalName(localName);
//                }
//                String pinCode = Model().getPinCode().getVal();
//                if (pinCode != null) {
//                    setPinCode(pinCode);
//                }

                // 连接上，需要查询信息

                getLocalName();
                getPinCode();

                InquiryVersion();
                InquiryLocalAddr();
                getPairList();
                InquiryHfpStatus();
                InquiryMusicInfo();
                InquiryCurBtAddr();
                InquiryCurBtName();
                InquiryAutoConnectAccept();

                Model().getModuleStatus().setVal(BluetoothDefine.ModuleStatus.MODULE_STATUS_CONNECT);

            }

        }

        @Override
        public void onConnecting() throws RemoteException {

            setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_CONNECTING, false);
        }

        @Override
        public void onMusicPlaying() throws RemoteException {
            LogUtils.d(TAG, "onMusicPlaying...getEasyConnA2dp="+PhonelinkLogic.getEasyConnA2dp()+
            ", backsource="+SourceManager.getCurBackSource());
            if (SourceManager.getCurBackSource() == Define.Source.SOURCE_BLUETOOTH ||
                    (PhonelinkLogic.getEasyConnA2dp() && SourceManager.getCurBackSource() == Define.Source.SOURCE_PHONELINK) ||
                    SourceManager.getCurBackSource() == Define.Source.SOURCE_PHONELINK) {
                McuManager.firstPlayProcess();
                musicUnmute();
            }
            Model().getMusicStatus().setVal(BluetoothDefine.MusicStatus.MUSIC_STATUS_PLAY);
        }

        @Override
        public void onMusicStopped() throws RemoteException {
            LogUtils.d(TAG, "onMusicStopped...");
//            musicMute();  //170918修改出声不完整问题
            Model().getMusicStatus().setVal(BluetoothDefine.MusicStatus.MUSIC_STATUS_STOP);
        }

        @Override
        public void onVoiceConnected() throws RemoteException {

        }

        @Override
        public void onVoiceDisconnected() throws RemoteException {

        }

        @Override
        public void onAutoConnectAccept(boolean autoConnect, boolean autoAccept)
                throws RemoteException {
            Model().getAutoConnect().setVal(autoConnect);
            Model().getAutoAnswer().setVal(autoAccept);
        }

        @Override
        public void onCurrentAddr(String addr) throws RemoteException {
            Model().getPairingMacAddr().setValAnyway(addr);
        }

        @Override
        public void onCurrentName(String name) throws RemoteException {
            Model().getPairingName().setVal(name);
            LogUtils.d(TAG, "onCurrentName:" + name);
        }

        @Override
        public void onHfpStatus(int status) throws RemoteException {
            if (1 == status) {
                setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT, false);
            } else if (3 == status && BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
                setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT, false);
                LogUtils.d(TAG, "HFP = " + status + ", Status =  connect");
            } else {
                if (4 == status) {
                    setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_OUT_CALLING, false);
                    LogUtils.d(TAG, "HFP = " + status + ", Status =  outCalling");
                } else if (5 == status) {
                    setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_IN_CALLING, false);
                    LogUtils.d(TAG, "HFP = " + status + ", Status =  inCalling");
                } else if (6 == status) {
                    setHFPStatus(BluetoothDefine.HFPStatus.HFP_STATUS_TALKING, false);
                    LogUtils.d(TAG, "HFP = " + status + ", Status =  talking");
                }
            }
        }

        @Override
        public void onAvStatus(int status) throws RemoteException {

            LogUtils.d(TAG, "MU" + status);
        }

        @Override
        public void onVersionDate(String version) throws RemoteException {

            Model().getVersion().setVal(version);
        }

        @Override
        public void onCurrentDeviceName(String name) throws RemoteException {

            Model().getLocalName().setVal(name);
        }

        @Override
        public void onCurrentPinCode(String code) throws RemoteException {

            Model().getPinCode().setVal(code);
        }

        @Override
        public void onA2dpConnected() throws RemoteException {

            Model().getA2DPConnectStatus().setVal(BluetoothDefine.A2DPConnectStatus.CONNECTED);
        }

        @Override
        public void onCurrentAndPairList(int index, String name, String addr)
                throws RemoteException {
            IntegerSS pair;
            LogUtils.d(TAG, "onCurrentAndPairList   index:" + index + "    name:" + name + "    addr:" + addr);
            if (addr.equals(Model().getPairingMacAddr().getVal()) && Model().getHFPStatus().getVal().equals(BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT)) {
                pair = new IntegerSS(BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT, name, addr);
            } else {
                pair = new IntegerSS(BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT, name, addr);
            }
            if (!existedPairedDevices(addr)) {
                Model().getPairList().addVal(-1, pair);
            }

            deleteFormSearchList(addr);

        }

        @Override
        public void onA2dpDisconnected() throws RemoteException {

            Model().getA2DPConnectStatus().setVal(BluetoothDefine.A2DPConnectStatus.DISCONNECT);
        }

        @Override
        public void onPhoneBook(String name, String number) throws RemoteException {
            downloadedContacts++;

            //开启定时器每1s更新一次联系人同步数目
            if (contactsTimerID < 0) {
                contactsTimerID = TimerUtils.setTimer(getMainContext(), 1000, 1000, new Timerable.TimerListener() {
                    @Override
                    public void onTimer(int i) {
                        if (!BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
                            Model().getDownloadedContacts().setVal(downloadedContacts);//
                        }
                    }
                });
            }

            //1s内没有新的联系人过来则认为同步结束或出现异常,kill定时器
            if (handler == null) {
                handler = new ContactsHandler(getMainContext().getMainLooper());
            }
            handler.removeMessages(KILL_CONTACTTIMER_WHAT);
            handler.sendEmptyMessageDelayed(KILL_CONTACTTIMER_WHAT, 1000);

            LogUtils.d(TAG, "onPhoneBook, name = " + name + ", number = " + number);
            if (!BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
                Model().getPhoneDownloadStatus().setVal(BluetoothDefine.DownloadStatus.DOWNLOADING);
                Model().getPhonebookDownloadStatus().setVal(BluetoothDefine.DownloadStatus.DOWNLOADING);

                IntegerSSBoolean book = new IntegerSSBoolean(BluetoothDefine.PhonebookOrigin.PHONE, name, number, false);
                contactList.add(book);
            } else {
                LogUtils.e(TAG, "phonebook after disconnect");
            }
        }

        @Override
        public void onSimBook(String name, String number) throws RemoteException {

            LogUtils.d(TAG, "onSimBook, name = " + name + ", number = " + number);
            Model().getSIMDownloadStatus().setVal(BluetoothDefine.DownloadStatus.DOWNLOADING);
            Model().getSIMDownloadStatus().setVal(BluetoothDefine.DownloadStatus.DOWNLOADING);

            IntegerSSBoolean book = new IntegerSSBoolean(BluetoothDefine.PhonebookOrigin.SIM, name, number, false);
            contactList.add(book);
        }

        @Override
        public void onPhoneBookDone() throws RemoteException {
            if (BluetoothDefine.DownloadStatus.FAILED == Model().getPhonebookDownloadStatus().getVal()) {
                LogUtils.e(TAG, "onPhoneBookDone come after download failed, do nothing");
                return;
            }
            downloadedContacts = 0;
            Model().getDownloadedContacts().setVal(-1);
            if (contactsTimerID > 0) {
                TimerUtils.killTimer(contactsTimerID);
                contactsTimerID = -1;
            }
            LogUtils.e(TAG, "onPhoneBookDone is called");
            final int length = contactList.size();
            IntegerSSBoolean[] temp = new IntegerSSBoolean[length];
            for (int i = 0; i < length; i++) {
                temp[i] = contactList.get(i);
            }
            contactList.clear();
            Model().getPhonebookList().setVal(temp);
            Model().getPhoneDownloadStatus().setVal(BluetoothDefine.DownloadStatus.SUCCESS);
            Model().getPhonebookDownloadStatus().setVal(BluetoothDefine.DownloadStatus.SUCCESS);
            // 联系人下载完成时保存数据
            if (null != mContentMemory) {
                mContentMemory.save();
            }
        }

        @Override
        public void onSimDone() throws RemoteException {

            Model().getSIMDownloadStatus().setVal(BluetoothDefine.DownloadStatus.SUCCESS);
        }

        @Override
        public void onCalllogDone() throws RemoteException {

            Model().getTalkListDownloadStatus().setVal(BluetoothDefine.DownloadStatus.SUCCESS);
        }

        @Override
        public void onCalllog(int type, String name, String number, String date) throws RemoteException {

            Model().getTalkListDownloadStatus().setVal(BluetoothDefine.DownloadStatus.DOWNLOADING);
            int call_type = BluetoothDefine.CallType.CALL_TYPE_UNKNOWN;
            if (4 == type) {
                call_type = BluetoothDefine.CallType.CALL_TYPE_OUT_GOING;
            } else if (5 == type) {
                call_type = BluetoothDefine.CallType.CALL_TYPE_MISSED;
            } else if (6 == type) {
                call_type = BluetoothDefine.CallType.CALL_TYPE_INCOMMING;
            }
            IntegerSSS talk = new IntegerSSS(call_type, null, number, date);
            Model().getTalkList().addVal(-1, talk);
        }

        @Override
        public void onDiscovery(String name, String addr) throws RemoteException {
            Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_ING);
            if (!existedAvailableDevices(addr) && !existedPairedDevices(addr)) {
                IntegerSS search = new IntegerSS(BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT, name, addr);
                Model().getSearchList().addVal(-1, search);
            }
        }

        @Override
        public void onDiscoveryDone() throws RemoteException {

            LogUtils.d(TAG, "onDiscoveryDone is called");
            Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_SUCCESS);
        }

        @Override
        public void onLocalAddress(String addr) throws RemoteException {

            Model().getLocalMacAddr().setVal(addr);
        }

        @Override
        public void onSppData(int index, String data) throws RemoteException {

        }

        @Override
        public void onSppConnect(int index) throws RemoteException {

        }

        @Override
        public void onSppDisconnect(int index) throws RemoteException {

        }

        @Override
        public void onSppStatus(int status) throws RemoteException {


        }

        @Override
        public void onOppReceivedFile(String path) throws RemoteException {

        }

        @Override
        public void onOppPushSuccess() throws RemoteException {

        }

        @Override
        public void onOppPushFailed() throws RemoteException {

        }

        @Override
        public void onOppReceiverSuccess() throws RemoteException {

        }

        @Override
        public void onOppReceiverFailed() throws RemoteException {

        }

        @Override
        public void onHidConnected() throws RemoteException {

        }

        @Override
        public void onHidDisconnected() throws RemoteException {

        }

        @Override
        public void onHidStatus(int status) throws RemoteException {

        }

        @Override
        public void onMusicInfo(String musicName, String artist, int playtime, int number, int total, String albumName)
                throws RemoteException {

            LogUtils.d(TAG, "musicName:" + musicName + "   artist:" + artist + "   playtime:" + playtime + " number:" + number
                    + "  total:" + total + "  albumName:" + albumName);

            Model().getMusicSongName().setVal(musicName);
            Model().getMusicArtistName().setVal(artist);
            Model().getmMusicPlaytime().setVal(playtime);
            Model().getMusicIndex().setVal(number);
            Model().getMusicTotal().setVal(total);
            Model().getMusicAlbumName().setVal(albumName);
        }

        @Override
        public void onPanConnect() throws RemoteException {

        }

        @Override
        public void onPanDisconnect() throws RemoteException {

        }

        @Override
        public void onPanStatus(int status) throws RemoteException {

        }

        @Override
        public void onProfileEnbled(boolean[] booleen) throws RemoteException {

        }

        @Override
        public void onMessageInfo(String s, String s1, String s2, String s3, String s4, String s5) throws RemoteException {

        }

        @Override
        public void onMessageContent(String s) throws RemoteException {

        }

        @Override
        public void onPairedState(int i) throws RemoteException {

        }

        @Override
        public void onA2dpVol(int i) throws RemoteException {

        }

        @Override
        public void onUpdateSuccess() throws RemoteException {

        }

        @Override
        public void onSignalBatteryVal(int i, int i1) throws RemoteException {

        }

        @Override
        public void onSpkMicVol(int i, int i1) throws RemoteException {

        }

        @Override
        public void onMicStatus(int i) throws RemoteException {
            LogUtils.d(TAG, "onMicStatus " + i);
        }

        @Override
        public void onSetPhoneBookStatus() throws RemoteException {

        }

        @Override
        public void onCurrentConnectAddressName(String s, String s1) throws RemoteException {
            LogUtils.e(TAG, s + "\t" + s1);
        }

        @Override
        public void onAvrcpStatus(int i) throws RemoteException {

        }

        @Override
        public void onBtConnecting() throws RemoteException {

        }

        @Override
        public void onHangUpCurrectAcceptWait() throws RemoteException {

        }

        @Override
        public void onIncomingName(String s) throws RemoteException {

        }

        @Override
        public void onHangUpHoldingWaiting() throws RemoteException {

        }

        @Override
        public void onInMeeting() throws RemoteException {

        }

        @Override
        public void onHoldCurrentAcceptWaiting() throws RemoteException {

        }

        @Override
        public void onHoldCall() throws RemoteException {

        }

        @Override
        public void onSecondIncoming() throws RemoteException {

        }

//        @Override
//        public void onSwitchAudioSoft(int i) throws RemoteException {
//
//            Model().getAudioSoft().setVal(i);
//        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.d(TAG, "onServiceDisconnected...");
            Model().getModuleStatus().setVal(BluetoothDefine.ModuleStatus.MODULE_STATUS_DISCONNECT);
            // unregister the call back object.
            if (null != mAIDLGocsdk) {
                try {
                    mAIDLGocsdk.unregisterCallback(mCallbackGocsdk);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            mAIDLGocsdk = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d(TAG, "onServiceConnected...");
            mAIDLGocsdk = IGocsdkService.Stub.asInterface(service);
            if (null != mAIDLGocsdk) {
                try {
                    mCallbackGocsdk = new GocCallback();
                    mAIDLGocsdk.registerCallback(mCallbackGocsdk);


                    ResetBluetooth();


                    // sync audio soft.
//                    final int audioSoft = Model().getAudioSoft().getVal();
//                    if (0 != audioSoft) {
//                        LogUtils.d(TAG, "sync setAudioSoft to " + BluetoothDefine.AudioSoft.toString(audioSoft));
//                        setAudioSoft('1');
//                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {

        super.onCreate(packet);

        Context context = getMainContext();
        if (null != context) {
            Intent intent = new Intent(SERVICE_NAME);
            ComponentName component = new ComponentName(SERVICE_PACKET_NAME, SERVICE_CLASS_NAME);
            intent.setComponent(component);
            context.bindService(intent,
                    mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }

        getModel().bindListener(mListener);

        if (handler == null) {
            handler = new ContactsHandler(getMainContext().getMainLooper());
        }
    }

    @Override
    public void onDestroy() {
        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.closeBlueTooth();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        getModel().unbindListener(mListener);

        if (null != mConnection) {
            mConnection.onServiceDisconnected(null);
        }

        // unbind the service.
        Context context = getMainContext();
        if (mIsBound && null != context && null != mConnection) {
            context.unbindService(mConnection);
            mIsBound = false;
        }
        if (handler != null) {
            handler.removeMessages(KILL_CONTACTTIMER_WHAT);
            handler = null;
        }

        // destroy
        super.onDestroy();
    }

    public void getLocalName() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getLocalName();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setLocalName(String name) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.setLocalName(name);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void getPinCode() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getPinCode();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPinCode(String pincode) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.setPinCode(pincode);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connectLast() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.connectLast();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connectA2dp(String addr) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.connectA2dp(addr);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connectHFP(String addr) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.connectHFP(addr);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.disconnect();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnectA2DP() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.disconnectA2DP();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnectHFP() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.disconnectHFP();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deletePair(String addr) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.deletePair(addr);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startDiscovery() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.startDiscovery();
                LogUtils.d(TAG, "mAIDLGocsdk.startDiscovery()");
                Model().getSearchStatus().setValAnyway(BluetoothDefine.SearchStatus.SEARCH_STATUS_START);
                TimerUtils.killTimer(mDiscoveryTimerId);
                mDiscoveryTimerId = TimerUtils.setTimer(getMainContext(), 65000, 65000, new Timerable.TimerListener() {
                    @Override
                    public void onTimer(int timerId) {
                        stopDiscovery();

                        TimerUtils.killTimer(mDiscoveryTimerId);
                        mDiscoveryTimerId = 0;
                    }
                });
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void getPairList() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getPairList();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据状态设置搜索的状态
     */
    private void setSearchStatus() {
        final int searchStatus = Model().getSearchStatus().getVal();
        if (BluetoothDefine.SearchStatus.searching(searchStatus)) {
            if (Model().getSearchList().getVal() == null) {
                Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_FAILED);
            } else {
                final int length = Model().getSearchList().getVal().length;
                LogUtils.d(TAG, "searchList.Length:" + length);
                if (0 == length) {
                    Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_FAILED);
                } else {
                    Model().getSearchStatus().setVal(BluetoothDefine.SearchStatus.SEARCH_STATUS_SUCCESS);
                }
            }
        }
    }

    @Override
    public void stopDiscovery() {
        LogUtils.d(TAG, "stopDiscovery() is called");

        if (null != mAIDLGocsdk) {
            try {
                setSearchStatus();
                mAIDLGocsdk.stopDiscovery();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void phoneAnswer() {

        if (null != mAIDLGocsdk) {
            try {
                if (BluetoothDefine.HFPStatus.isCallIn(Model().getHFPStatus().getVal())) {
                    mAIDLGocsdk.phoneAnswer();
                    mLatestCallType = BluetoothDefine.CallType.CALL_TYPE_INCOMMING;
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void phoneHangUp() {

        if (null != mAIDLGocsdk) {
            if (BluetoothDefine.HFPStatus.isCalling(Model().getHFPStatus().getVal())) {
                try {
                    mAIDLGocsdk.phoneHangUp();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    //未成功挂断时纠错
                    if (delayHangupTimerID > 0) {
                        TimerUtils.killTimer(delayMusicTimerID);
                    }
                    delayHangupTimerID = TimerUtils.setTimer(getMainContext(), DELAY_HANGUP_TIME, new Timerable.TimerListener() {
                        @Override
                        public void onTimer(int paramInt) {
                            Model().getHFPStatus().setVal(BluetoothDefine.HFPStatus.HFP_STATUS_HANGUP);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void phoneDail(String phonenum) {

        if (null != mAIDLGocsdk) {
            if (!BluetoothDefine.HFPStatus.isCalling(Model().getHFPStatus().getVal())) {
                try {
                    mAIDLGocsdk.phoneDail(phonenum);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void phoneTransmitDTMFCode(char code) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.phoneTransmitDTMFCode(code);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void phoneTransfer() {
        McuManager.notifyMcuMute();//处理杂音问题。2020-04-08

        handler.removeMessages(MSG_TRANSFER);
        handler.removeMessages(MSG_TRANSFER_BACK);
        handler.sendEmptyMessageDelayed(MSG_TRANSFER, 400);
//        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.phoneTransfer();
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void phoneTransferBack() {
        McuManager.notifyMcuMute();//处理杂音问题。2020-04-08

        handler.removeMessages(MSG_TRANSFER);
        handler.removeMessages(MSG_TRANSFER_BACK);
        handler.sendEmptyMessageDelayed(MSG_TRANSFER_BACK, 400);
//        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.phoneTransferBack();
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void phoneBookStartUpdate() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.phoneBookStartUpdate();
                int downloadStatus = Model().getPhonebookDownloadStatus().getVal();

                //已再下载中则直接return
                if (downloadStatus == BluetoothDefine.DownloadStatus.START
                        || downloadStatus == BluetoothDefine.DownloadStatus.DOWNLOADING) {
                    return;
                }
                //蓝牙未连接则直接return
                if (BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
                    LogUtils.e(TAG, "start download phone book fail,hfg is disconnect");
                    return;
                }
                clearPhoneBook();
                Model().getPhoneDownloadStatus().setVal(BluetoothDefine.DownloadStatus.START);
                Model().getPhonebookDownloadStatus().setVal(BluetoothDefine.DownloadStatus.START);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clearPhoneBook() {
        LogUtils.d(TAG, "clearPhoneBook_____________________________");
        Model().getPhonebookList().setVal(null);
        contactList.clear();
        downloadedContacts = 0;
    }

    @Override
    public void clearTalkList() {
        LogUtils.d(TAG, "clearTalkList_____________________________");
        Model().getTalkList().setVal(null);
    }

    @Override
    public void deletePairList(String macAddr) {
        LogUtils.d(TAG, "clearSearchList_____________________________" + macAddr);
        if (TextUtils.isEmpty(macAddr)) {
            Model().getPairList().setVal(null);
        } else {
            deleteSelectedDevice(macAddr);
        }
    }

    private void deleteSelectedDevice(String macAddr) {
        IntegerSS[] pairArray = Model().getPairList().getVal();
        if (pairArray != null) {
            int deleteIndex = -1;
            for (int i = 0; i < pairArray.length; i++) {
                String macAddress = pairArray[i].getString2();
                int state = pairArray[i].getInteger();
                if (null != macAddress && macAddress.equals(macAddr)) {
                    deleteIndex = i;
                    //删除的设备为连接状态则先断开连接
                    if (state == BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT) {
                        disconnect();
                    }
                }
            }
            if (deleteIndex >= 0) {
                Model().getPairList().delVal(deleteIndex);
            }
        }
        IntegerSS[] searchArray = Model().getSearchList().getVal();
        if (searchArray != null) {
            int deleteIndex = -1;
            for (int i = 0; i < searchArray.length; i++) {
                String macAddress = searchArray[i].getString2();
                int state = searchArray[i].getInteger();
                if (null != macAddress && macAddress.equals(macAddr)) {
                    deleteIndex = i;
                    //删除的设备为连接状态则先断开连接
                    if (state == BluetoothDefine.HFPStatus.HFP_STATUS_CONNECT) {
                        disconnect();
                    }
                }
            }
            if (deleteIndex >= 0) {
                Model().getSearchList().delVal(deleteIndex);
            }
        }
    }

    @Override
    public void clearCallNumber() {
        Model().getCallNumber().setVal(null);
    }

    @Override
    public void simBookStartUpdate() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.phoneBookStartUpdate();
                Model().getSIMDownloadStatus().setVal(BluetoothDefine.DownloadStatus.START);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void IncommingStartUpdate() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.callLogstartUpdate(5);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OutgoingStartUpdate() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.callLogstartUpdate(3);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void MissedStartUpdate() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.callLogstartUpdate(4);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private TimerQueue mUserPlayPase = new TimerQueue();

    private boolean isPlay() {
        return BluetoothDefine.MusicStatus.isPlay(Model().getMusicStatus().getVal());
    }

    protected void userPlayPause(boolean play) {
        mUserPlayPase.stop();
        Packet packet = new Packet();
        packet.putBoolean("play", play);
        mUserPlayPase.add(5000, packet, new BaseCallback() {
            @Override
            public void onCallback(int nId, Packet packet) {
                if (null != packet) {
                    final boolean user = packet.getBoolean("play");
                    final boolean fact = isPlay();
                    LogUtils.d(TAG, "userPlayPause##user = " + user + ", fact = " + fact);
                    if (user != fact) {
                        if (user) {
                            musicPlay();
                        } else {
                            musicPause();
                        }
                    }
                }
            }
        });
        mUserPlayPase.start();
    }

    @Override
    public void musicPlay() {

        if (null != mAIDLGocsdk && !BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
            try {
                if (!ClickFilter.filter(500L)) {
                    McuManager.firstPlayProcess();
                    mAIDLGocsdk.musicPlay();
                }
//                Model().getMusicStatus().setVal(BluetoothDefine.MusicStatus.MUSIC_STATUS_PLAY);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicPause() {
        //如在延时播放时间内调用了暂停,则取消延时播放
        if (delayMusicTimerID > 0) {
            TimerUtils.killTimer(delayMusicTimerID);
        }
        if (null != mAIDLGocsdk && !BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
            try {
                mAIDLGocsdk.musicPause();
//                Model().getMusicStatus().setVal(BluetoothDefine.MusicStatus.MUSIC_STATUS_PAUSE);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicStop() {

        if (null != mAIDLGocsdk && !BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
            try {
                mAIDLGocsdk.musicStop();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicPrevious() {

        if (null != mAIDLGocsdk && !BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
            try {
                McuManager.firstPlayProcess();
                mAIDLGocsdk.musicPrevious();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicNext() {

        if (null != mAIDLGocsdk && !BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal())) {
            try {
                McuManager.firstPlayProcess();
                mAIDLGocsdk.musicNext();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void PairMode(int type) {

        if (null != mAIDLGocsdk) {
            try {
                if (1 == type) {
                    mAIDLGocsdk.setPairMode();
                } else if (2 == type) {
                    mAIDLGocsdk.cancelPairMode();
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void ReDail() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.redial();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void VoiceDial() {

        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.VoiceDial();
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void CancelVoiceDial() {

        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.CancelVoiceDial();
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void VolumeSet(String volume) {

        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.VolumeSet();
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void MicSwitch(char type) {

        if (null != mAIDLGocsdk) {
            try {
                if ('1' == type) {
                    mAIDLGocsdk.micOpenAndClose(1);
                    Model().getMicMute().setVal(false);
                } else {
                    mAIDLGocsdk.micOpenAndClose(0);
                    Model().getMicMute().setVal(true);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryHfpStatus() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.inquiryHfpStatus();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void ResetBluetooth() {
        needInit = true;
        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.restBluetooth();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryAutoConnectAccept() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getAutoConnectAnswer();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setAutoConnect() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.setAutoConnect();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancelAutoConnect() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.cancelAutoConnect();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setAutoAccept() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.setAutoAnswer();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancelAutoAccept() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.cancelAutoAnswer();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public void InquiryA2dpStatus() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.inquiryA2dpStatus();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryVersion() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getVersion();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicMute() {

        if (null != mAIDLGocsdk) {
            try {
                LogUtils.d(TAG, "musicMute");
                mAIDLGocsdk.musicMute();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicUnmute() {

        if (null != mAIDLGocsdk) {
            try {
                LogUtils.d(TAG, "musicUnMute");
                mAIDLGocsdk.musicUnmute();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicBackground() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.musicBackground();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void musicNormal() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.musicNormal();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OppSendFile(String path) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.oppSendFile(path);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void SppConnect(String addr) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.connectSpp(addr);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void SppSendData(String data1, String data2) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.sppSendData(data1, data2);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void SppDisConnect() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.disconnectSpp();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquirySppStatus() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.inquirySppStatus();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void HidConnect(String addr) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.connectHid(addr);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void HidMouseMove(String data) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.hidMouseMove(data);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void HidHomeKey() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.hidHomeClick();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void HidBackKey() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.hidBackClick();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void HidMenuKey() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.hidMenuClick();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void HidDisConnect() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.disconnectHid();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryHidStatus() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.inquiryHidStatus();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void SetHidResolution(String data) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.setTounchResolution();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void ForcePauseMusic() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.musicPause();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void PanConnect() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.panConnect();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void PanDisConnect() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.panDisconnect();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryPanStatus() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.inquiryPanStatus();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryLocalAddr() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getLocalAddress();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OpenBt() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.openBlueTooth();
                Model().getOpenOrClose().setVal(true);
                super.isClosedByUser = false;
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void CloseBt(int whoClose) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.closeBlueTooth();
//                Model().getOpenOrClose().setVal(false);
                Model().getHFPStatus().setVal(BluetoothDefine.HFPStatus.HFP_STATUS_DISCONNECT);
                if (whoClose == BluetoothDefine.BluetoothClosedBy.USER) {
                    super.isClosedByUser = true;
                    Model().getOpenOrClose().setVal(false);
                } else super.isClosedByUser = false;
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryCurBtAddr() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getCurrentDeviceAddr();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryCurBtName() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getCurrentDeviceName();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquirySpkMicVal() {
        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.inquirySpkMicVol();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquirySignelBatteryVal() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.inquirySignelBatteryVal();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void SetMusicVal(int val) {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.musicVolSet();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void InquiryMusicInfo() {

        if (null != mAIDLGocsdk) {
            try {
                mAIDLGocsdk.getMusicInfo();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void UpdatePskey() {

        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.UpdatePskey();
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
    }

    public boolean isModuleConnected() {

        return (null != mAIDLGocsdk);
    }

    @Override
    public void TestMode(char ch) {

        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.TestMode();
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void setAudioSoft(char type) {

        if (null != mAIDLGocsdk) {
//            try {
//                mAIDLGocsdk.SetAudioSoft(type);
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void deleteContactItem(String number) {
        IntegerSSBoolean[] integerSSes = Model().getPhonebookList().getVal();
        if (integerSSes == null) {
            return;
        }
        int deleteIndex = -1;
        for (int i = 0; i < integerSSes.length; i++) {
            if (number != null && number.equals(integerSSes[i].getString2())) {
                deleteIndex = i;
            }
        }
        Model().getPhonebookList().delVal(deleteIndex);
    }

    @Override
    public void addToFavorite(String number) {
        IntegerSSBoolean[] integerSSes = Model().getPhonebookList().getVal();
        if (integerSSes == null) {
            return;
        }
        for (int i = 0; i < integerSSes.length; i++) {
            if (number != null && number.equals(integerSSes[i].getString2())) {
                IntegerSSBoolean integerSSBoolean = new IntegerSSBoolean(integerSSes[i].getInteger(),
                        integerSSes[i].getString1(), integerSSes[i].getString2(), true);
                Model().getPhonebookList().setVal(i, integerSSBoolean);
            }
        }
    }

    @Override
    public void removeFromFavorite(String number) {
        IntegerSSBoolean[] integerSSes = Model().getPhonebookList().getVal();
        if (integerSSes == null) {
            return;
        }
        for (int i = 0; i < integerSSes.length; i++) {
            if (number != null && number.equals(integerSSes[i].getString2())) {
                IntegerSSBoolean integerSSBoolean = new IntegerSSBoolean(integerSSes[i].getInteger(),
                        integerSSes[i].getString1(), integerSSes[i].getString2(), false);
                Model().getPhonebookList().setVal(i, integerSSBoolean);
            }
        }
    }

    @Override
    public void clearSearchList() {
        Model().getSearchList().setVal(null);
    }

    private void setHFPStatus(Integer status, boolean bAny) {
        final boolean oldCall = BluetoothDefine.HFPStatus.isCalling(Model().getHFPStatus().getVal());
        final boolean newCall = BluetoothDefine.HFPStatus.isCalling(status);

        final boolean oldDisConnect = BluetoothDefine.HFPStatus.isDisConnect(Model().getHFPStatus().getVal());
        if (oldDisConnect && newCall) {
            LogUtils.e("setHFPStatus----return disconnect!");
            return;
        }

        if (oldCall && !newCall/* || !oldCall && newCall*/) {
            byte[] data = new byte[2];
            data[0] = 0x0d;
            data[1] = (byte) McuAdapter.getHFPStatus(status);
            McuManager.sendMcuImportant((byte) McuDefine.ARM_TO_MCU.RPT_BtInfo, data, data.length);
        }

        if (bAny) {
            Model().getHFPStatus().setValAnyway(status);
        } else {
            Model().getHFPStatus().setVal(status);
        }
    }

    private boolean existedAvailableDevices(String addr) {
        IntegerSS[] array = Model().getSearchList().getVal();
        if (array == null) {
            return false;
        }
        for (IntegerSS i : array) {
            if (addr == null || addr.equals(i.getString2())) {
                return true;
            }
        }
        return false;
    }

    private boolean existedPairedDevices(String addr) {
        IntegerSS[] array = Model().getPairList().getVal();
        if (array == null) {
            return false;
        }
        for (IntegerSS i : array) {
            if (addr == null || addr.equals(i.getString2())) {
                return true;
            }
        }
        return false;
    }

    private void deleteFormSearchList(String addr) {
        IntegerSS[] array = Model().getSearchList().getVal();
        if (array == null) {
            return;
        }
        int deleteIndex = -1;
        for (int i = 0; i < array.length; i++) {
            if (addr != null && addr.equals(array[i].getString2())) {
                deleteIndex = i;
            }
        }
        Model().getSearchList().delVal(deleteIndex);
    }

}
