package com.wwc2.main.media.video;

import android.os.SystemClock;
import android.text.TextUtils;

import com.wwc2.audio_interface.AudioInterface;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.StringII;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.driver.audio.AudioDefine;
import com.wwc2.main.driver.audio.AudioDriver;
import com.wwc2.main.driver.audio.AudioListener;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.common.CommonListener;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.main.eventinput.EventInputListener;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.MediaListener;
import com.wwc2.main.media.MediaLogic;
import com.wwc2.main.media.driver.MediaDriverable;
import com.wwc2.main.media.driver.android.AndroidMediaDriver;
import com.wwc2.main.media.driver.android.AndroidVideoDriver;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.driver.search.FilesSendRunnable;
import com.wwc2.media_interface.MediaDefine;
import com.wwc2.settings_interface.SettingsDefine;
import com.wwc2.video_interface.VideoDefine;
import com.wwc2.video_interface.VideoInterface;

import java.util.Arrays;


/**
 * the video logic.
 *
 * @author wwc2
 * @date 2017/1/12
 */
public class VideoLogic extends MediaLogic {

    /**
     * 通用驱动Model
     */
    private BaseModel mCommonDriverModel = null;

    private boolean mIsCalling = false;//通话状态

    private boolean mVoiceAssistantActive = false;//语音状态

    private boolean isVoicePausePlayStatus = false; //判断是否是语音改变的播放状态

    private int playStatus;

    private final String TAG = VideoLogic.class.getSimpleName();

    @Override
    public String getTypeName() {
        return "video";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_VIDEO;
    }

    @Override
    public String getMessageType() {
        return VideoDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.video";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.video.view.MainActivity";
    }

    @Override
    public boolean runApk() {
        return super.runApk();
    }

    protected MediaListener mMediaListener = new MediaListener() {
        //******************已优化代码，暂保留2018-12-27 begin*******************//
        /**文件列表监听器*/
        public void FileListListener(String[] oldVal, String[] newVal) {
            LogUtils.d(TAG, "FileList:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putStringArray("FileList", newVal);
            Notify(VideoInterface.MAIN_TO_APK.FILE_LIST, mPacket);
        }
        //******************已优化代码，暂保留2018-12-27 end*******************//

        /**播放模式监听器, see {@link com.wwc2.media_interface.MediaDefine.PlayMode}*/
        public void PlayModeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "PlayMode oldVal:" + oldVal);
            LogUtils.d(TAG, "PlayMode newVal:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("PlayMode", newVal);
            Notify(VideoInterface.MAIN_TO_APK.PLAY_MODE, mPacket);
        }

        /**当前播放时间监听器*/
        public void CurrentTimeListener(Integer oldVal, Integer newVal) {
            //LogUtils.d("CurrentTime:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("CurTime", newVal);
            Notify(VideoInterface.MAIN_TO_APK.CURRENT_TIME, mPacket);

            sendTimeInfoToMcu(newVal);
        }

        /**媒体总时间监听器*/
        public void TotalTimeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "TotalTime:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("TotalTime", newVal);
            Notify(VideoInterface.MAIN_TO_APK.TOTAL_TIME, mPacket);
        }

        /**播放状态监听器, see {@link com.wwc2.media_interface.MediaDefine.PlayStatus}*/
        public void PlayStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d("PlayStatus-->oldVal:" + oldVal + ",newVal:" + newVal + ",mIsCalling:" + mIsCalling);
            playStatus = newVal;
            Packet mPacket = new Packet();
            LogUtils.d("mVoiceAssistantActive:" + mVoiceAssistantActive);
            LogUtils.d("PlayStatus-->camera:" + EventInputManager.getCamera() + ", curSource:" + SourceManager.getCurSource());
            if (MediaDefine.PlayStatus.isPlay(newVal) &&
                    (mIsCalling ||
                            (SourceManager.getCurSource() != source() && !SourceManager.isVideoTop()) ||
                            EventInputManager.getCamera())) {
                mPacket.putInt("PlayStatus", MediaDefine.PlayStatus.PLAY_STATUS_PAUSE);
            } else {
                if (mVoiceAssistantActive) {
                    isVoicePausePlayStatus = true;
                    mPacket.putInt("PlayStatus", MediaDefine.PlayStatus.PLAY_STATUS_PAUSE);
                } else {
                    mPacket.putInt("PlayStatus", newVal);
                }
            }
            Notify(VideoInterface.MAIN_TO_APK.PLAY_STATUS, mPacket);
        }

        /**播放下标监听器, 从0开始*/
        public void IndexListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Index:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("Index", newVal);
            Notify(VideoInterface.MAIN_TO_APK.INDEX, mPacket);
        }

        /**媒体列表总数监听器*/
        public void TotalListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Total:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("Total", newVal);
            Notify(VideoInterface.MAIN_TO_APK.TOTAL, mPacket);
        }

        /**播放文件全路径监听器*/
        public void FilePathListener(String oldVal, String newVal) {
            LogUtils.d("FilePath:" + newVal);
            if (!TextUtils.isEmpty(newVal)) {
                Packet mPacket = new Packet();
                //传下去会影响其它视频播放进度
                int curTime = getInfo().getInt("CurTime");
                LogUtils.d("FilePath:" + newVal + " curTime:" + curTime);
                mPacket.putInt("CurTime", curTime);
                mPacket.putString("FilePath", newVal);
                Notify(VideoInterface.MAIN_TO_APK.FILE_PATH, mPacket);
            }
        }

        /**媒体播放器错误状态监听器, see {@link com.wwc2.media_interface.MediaDefine.ErrorState}*/
        public void ErrorStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "ErrorStatus:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("ErrorStatus", newVal);
            Notify(VideoInterface.MAIN_TO_APK.ERROR_STATUS, mPacket);
        }

        /**媒体播放器错误状态监听器, see {@link com.wwc2.media_interface.MediaDefine.StorageErrorState}*/
        public void StorageErrorStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "StorageErrorStatus:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("StorageErrorStatus", newVal);
            Notify(VideoInterface.MAIN_TO_APK.STORAGE_ERROR_STATUS, mPacket);
        }

        /**存储设备列表监听器, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
        public void StorageListListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "StorageList:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putIntegerObjectArray("StorageList", newVal);
            Notify(VideoInterface.MAIN_TO_APK.STORAGE_LIST, mPacket);
        }

        /**正在播放的存储设备监听器, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
        public void PlayStorageListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "PlayStorage:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("PlayStorage", newVal);
            Notify(VideoInterface.MAIN_TO_APK.PLAY_STORAGE, mPacket);
        }

        /**当前显示的存储设备监听器*/
        public void CurrentStorageListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "CurrentStorage:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("CurrentStorage", newVal);
            Notify(VideoInterface.MAIN_TO_APK.CURRENT_STORAGE, mPacket);
        }

        /**搜索的存储信息设备监听器, int storage, int state, boolean over, boolean auto*/
        public void SearchInfoListener(FourInteger[] oldVal, FourInteger[] newVal) {
            LogUtils.d(TAG, "Search_info:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("SearchInfo", newVal);
            Notify(AudioInterface.MAIN_TO_APK.SEARCH_INFO, mPacket);
        }

        /**音频信息列表监听器, string path, string title, string artist, string album.*/
        public void ListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "ListInfo:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putObject("info", newVal);
//            Notify(VideoInterface.MAIN_TO_APK.,mPacket);
        }

        //******************已优化代码，暂保留2018-12-27 begin*******************//
        /**本地音频信息列表监听器, string path, string title, string artist, string album.*/
        public void NandFlashListInfo(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "NandFlashListInfo:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("FlashInfo", newVal);
            Notify(VideoInterface.MAIN_TO_APK.FLASH_LIST_INFO, mPacket);
        }

        /**SdCard音频信息列表监听器, string path, string title, string artist, string album.*/
        public void SdListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "SdListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            /*//调试日志
            if(newVal!= null){
                for(int i = 0; i< newVal.length; i++){
                    FourString itemFourString = newVal[i];
                    LogUtils.d(TAG,"title:" + itemFourString.getString2() + " artist:" + itemFourString.getString3() + " album:" + itemFourString.getString4() + " path:" + itemFourString.getString1());
                }
            }
            */
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("SdInfo", newVal);
            Notify(VideoInterface.MAIN_TO_APK.SD_LIST_INFO, mPacket);
        }

        /**Usb1音频信息列表监听器, string path, string title, string artist, string album.*/
        public void UsbListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "UsbListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("UsbInfo", null);//由于视频文件过多，video收不到，这里只做通知，由Video主动获取;usb1、2、3同理
            Notify(VideoInterface.MAIN_TO_APK.USB_LIST_INFO, mPacket);
        }

        /**Usb2音频信息列表监听器, string path, string title, string artist, string album.*/
        public void Usb1ListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "Usb1ListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("Usb1Info", null);
            Notify(VideoInterface.MAIN_TO_APK.USB1_LIST_INFO, mPacket);
        }

        /**Usb3音频信息列表监听器, string path, string title, string artist, string album.*/
        public void Usb2ListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "Usb2ListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("Usb2Info", null);
            Notify(VideoInterface.MAIN_TO_APK.USB2_LIST_INFO, mPacket);
        }

        /**Usb4音频信息列表监听器, string path, string title, string artist, string album.*/
        public void Usb3ListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "Usb3ListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("Usb3Info", null);
            Notify(VideoInterface.MAIN_TO_APK.USB3_LIST_INFO, mPacket);
        }
        //******************已优化代码，暂保留2018-12-27 end*******************//

        /**视频尺寸监听器*/
        public void ScreenSizeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "ScreenSize:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("screenSize", newVal);
            Notify(VideoInterface.MAIN_TO_APK.SCREEN_SIZE, mPacket);
        }

        /**AccOn后是否在扫描记忆设备, 0：没有,1：正在扫描*/
        public void StorageScanStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "StorageScanStatus:" + newVal);
            if (newVal != 0) {
                Packet mPacket = new Packet();
                mPacket.putInt("StorageScanStatus", newVal);
                Notify(VideoInterface.MAIN_TO_APK.STORAGE_SCAN_STATUS, mPacket);
            }
        }

        //******************优化后新增代码2018-12-27 begin*******************//
        @Override
        public void UpdateListInfoListener(Integer oldVal, Integer newVal) {
            if (newVal == 0) {
//                for ()
//                updateUsb3ListInfo(Driver().getDeviceListInfo(StorageDevice.USB3));
            } else {
                updateDeviceListInfo(newVal, Driver().getDeviceListInfo(newVal));
            }
        }
        //******************优化后新增代码2018-12-27 end*******************//

        @Override
        public void UpdateCurrentTime(Integer value) {
            Packet mPacket = new Packet();
            mPacket.putInt("CurTime", value);
            Notify(VideoInterface.MAIN_TO_APK.CURRENT_TIME, mPacket);

            sendTimeInfoToMcu(value);
        }
    };

    //******************优化后新增代码2018-12-27 begin*******************//
    private static final int SEND_LEN   = 800;
    FilesSendRunnable mNandThread = null;
    FilesSendRunnable mSdThread  = null;
    FilesSendRunnable mUsbThread = null;
    FilesSendRunnable mUsb1Thread = null;
    FilesSendRunnable mUsb2Thread = null;
    FilesSendRunnable mUsb3Thread = null;
    public void updateDeviceListInfo(int storageId, FourString[] newVal) {
        LogUtils.d(TAG, "updateDeviceListInfo:" + ((null == newVal) ? "null" : newVal.length) + ", storageId=" + storageId);

        //文件很多时，会比较耗时，会导致插入另外一个USB时，列表还会显示上一个USB的内容。采用线程处理。2019-01-18
        switch (storageId) {
            case StorageDevice.NAND_FLASH:
                if (mNandThread != null) {
                    mNandThread.needCancel();
                    mNandThread = null;
                }
                mNandThread = new FilesSendRunnable(storageId, newVal, this);
                mNandThread.start();
                break;
            case StorageDevice.MEDIA_CARD:
                if (mSdThread != null) {
                    mSdThread.needCancel();
                    mSdThread = null;
                }
                mSdThread = new FilesSendRunnable(storageId, newVal, this);
                mSdThread.start();
                break;
            case StorageDevice.USB:
                if (mUsbThread != null) {
                    mUsbThread.needCancel();
                    mUsbThread = null;
                }
                mUsbThread = new FilesSendRunnable(storageId, newVal, this);
                mUsbThread.start();
                break;
            case StorageDevice.USB1:
                if (mUsb1Thread != null) {
                    mUsb1Thread.needCancel();
                    mUsb1Thread = null;
                }
                mUsb1Thread = new FilesSendRunnable(storageId, newVal, this);
                mUsb1Thread.start();
                break;
            case StorageDevice.USB2:
                if (mUsb2Thread != null) {
                    mUsb2Thread.needCancel();
                    mUsb2Thread = null;
                }
                mUsb2Thread = new FilesSendRunnable(storageId, newVal, this);
                mUsb2Thread.start();
                break;
            case StorageDevice.USB3:
                if (mUsb3Thread != null) {
                    mUsb3Thread.needCancel();
                    mUsb3Thread = null;
                }
                mUsb3Thread = new FilesSendRunnable(storageId, newVal, this);
                mUsb3Thread.start();
                break;
            default:
                return;
        }
    }

    int fileListIndex = 0;
    public void updateFileList(String[] newVal) {
        if (true) {
            //现在APK端没有用到此数据。
            return;
        }
        if (null == newVal) {
            Packet mPacket = new Packet();
            mPacket.putStringArray("FileList", newVal);
            Notify(AudioInterface.MAIN_TO_APK.FILE_LIST, mPacket);
            return;
        }
        fileListIndex = 0;
        int total = (newVal.length / SEND_LEN) + 1;
        for (int i=0; i<total; i++) {
            fileListIndex++;
            Packet mPacket1 = new Packet();
            mPacket1.putInt("FileListIndex", fileListIndex);
            mPacket1.putInt("FileListTotal", total);
            if (i == (total - 1)) {
                mPacket1.putStringArray("FileList", Arrays.copyOfRange(newVal, i * SEND_LEN, newVal.length));
            } else {
                mPacket1.putStringArray("FileList", Arrays.copyOfRange(newVal, i * SEND_LEN, (i * SEND_LEN) + SEND_LEN));
            }
            Notify(VideoInterface.MAIN_TO_APK.FILE_LIST, mPacket1);
        }
    }
    //******************优化后新增代码2018-12-27 end*******************//

    /**
     * 设置监听器
     */
    private CommonListener mCommonListener = new CommonListener() {
        @Override
        public void BrakeWarningListener(Boolean oldValue, Boolean newValue) {
            Packet packet = new Packet();
            Boolean isBrake = EventInputManager.getBrake();
            LogUtils.d(TAG, "CommonListener isBrake:" + isBrake + " brakeSwitch:" + newValue);
            packet.putBoolean("ShowBrakeWarning", (!isBrake && newValue));
            Notify(VideoInterface.MAIN_TO_APK.SHOW_BRAKE_WARNING, packet);
        }
    };

    /**
     * 事件输入监听器
     */
    private EventInputListener mEventInputListener = new EventInputListener() {
        @Override
        public void BrakeListener(Boolean oldVal, Boolean newVal) {
            Packet packet = new Packet();
            Boolean brakeSwitch = mCommonDriverModel.getInfo().getBoolean(SettingsDefine.Common.Switch.BRAKE_WARNING.value());
            Boolean showBrakeWarning = ((!newVal) && brakeSwitch);
            LogUtils.d(TAG, "EventInputListener isBrake:" + newVal + " brakeSwitch:" + brakeSwitch + " ShowBrakeWarning:" + showBrakeWarning);
            packet.putBoolean("ShowBrakeWarning", showBrakeWarning);
            Notify(VideoInterface.MAIN_TO_APK.SHOW_BRAKE_WARNING, packet);
        }
    };

    @Override
    public Packet dispatch(int nId, Packet packet) {
        if (nId != VideoInterface.APK_TO_MAIN.CURRENT_TIME) {
            LogUtils.d("dispatch nId:" + nId);
        }
        if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
            LogUtils.d("videlogic dispatch diabale for acc off mode.");
            return super.dispatch(nId, packet);
        }
        switch (nId) {
            case VideoInterface.APK_TO_MAIN.PAUSE:
                Driver().pause();
                break;
            case VideoInterface.APK_TO_MAIN.PLAY:
                Driver().play();
                break;
            case VideoInterface.APK_TO_MAIN.STOP:
                Driver().stop();
                break;
            case VideoInterface.APK_TO_MAIN.PREV:
                if (!mIsCalling)
                    Driver().prev();
                break;
            case VideoInterface.APK_TO_MAIN.NEXT:
                if (!mIsCalling)
                    Driver().next();
                break;
            case VideoInterface.APK_TO_MAIN.PLAY_MODE:
                int mode = packet.getInt("mode");
                LogUtils.d(TAG, "video mode:" + mode);
                if (mode > 0) {
                    Driver().playMode(mode);
                }
                break;
            case VideoInterface.APK_TO_MAIN.SEEK:
                int seek = packet.getInt("seek");
                if (seek > 0) {
                    Driver().seek(seek);
                }
                LogUtils.d(TAG, "video seek:" + seek);
                break;
            case VideoInterface.APK_TO_MAIN.SWITCH_STORAGE:
                int storage = packet.getInt("storage");
                LogUtils.d(TAG, "Request play video play storage:" + storage);
                if (storage > 0) {
                    VideoDriver().switchStorage(storage);
                }
                break;
            case VideoInterface.APK_TO_MAIN.PLAY_FILEPATH:
                String path = packet.getString("path");
                LogUtils.d(TAG, "video play path:" + path);
                if (!TextUtils.isEmpty(path)) {
                    Driver().playFilePath(path, 0);
                }
                break;
            case VideoInterface.APK_TO_MAIN.ENTER:
                //******************优化后新增代码2018-12-27 begin*******************//
                //第一次启动APK接收此消息刷新列表。
                boolean bInit = packet.getBoolean("initData", false);
                if (bInit) {
                    int[] storageSort = {StorageDevice.NAND_FLASH, StorageDevice.MEDIA_CARD, StorageDevice.USB,
                            StorageDevice.USB1, StorageDevice.USB2, StorageDevice.USB3};
                    for (int i=0; i<storageSort.length; i++) {
                        updateDeviceListInfo(storageSort[i], Driver().getDeviceListInfo(storageSort[i]));
                    }
                } else {
                    //******************优化后新增代码2018-12-27 end*******************//
                    String enterPath = packet.getString("path");
                    boolean isFileEnter = packet.getBoolean("isFileEnter", false);
                    if (isFileEnter) {
                        AndroidMediaDriver.isEnterFromFile = true;
                    } else {
                        AndroidMediaDriver.isEnterFromFile = false;
                    }
                    LogUtils.d(TAG, "video enter enterPath:!! " + enterPath +
                            ", curSource=" + SourceManager.getCurSource() +
                            ", topPacket=" + ApkUtils.getTopPackage(getMainContext()));
                    if (!TextUtils.isEmpty(enterPath) &&
                            (SourceManager.getCurSource() == Define.Source.SOURCE_VIDEO || SourceManager.isVideoTop())) {//当前源不是视频不请求播放
                        VideoDriver().enter(enterPath);
                    }
                }
                break;
            case VideoInterface.APK_TO_MAIN.SCREEN_SIZE:
                int screenSize = packet.getInt("screenSize");
                LogUtils.d(TAG, "video play screenSize:" + screenSize);
                if (screenSize > 0) {
                    VideoDriver().setScreenSize(screenSize);
                }
                break;
            case VideoInterface.APK_TO_MAIN.TOTAL_TIME:
                int totalTime = packet.getInt("TotalTime");
                //LogUtils.d(TAG, "video play totalTime:" + totalTime);
                if (totalTime > 0) {
                    VideoDriver().setTotalTime(totalTime);
                }
                break;
            case VideoInterface.APK_TO_MAIN.CURRENT_TIME:
                int curTime = packet.getInt("CurTime");
                //LogUtils.d("video play curTime:" + curTime);
                if (curTime >= 0) {
                    VideoDriver().setCurrentTime(curTime);
                }
                break;
            case VideoInterface.APK_TO_MAIN.ERROR_STATUS:
                int errorStatus = packet.getInt("ErrorStatus");
                LogUtils.d(TAG, "video play errorStatus:" + errorStatus);
                if (errorStatus >= 0) {
                    VideoDriver().setErrorStatus(errorStatus);
                }
                break;
            case VideoInterface.APK_TO_MAIN.SEARCH_CURRENT_STORAGE:
                int sStorage = packet.getInt("storage");
                VideoDriver().searchStorage(sStorage);
                break;
        }
        return super.dispatch(nId, packet);
    }

    @Override
    public Packet onModuleEvent(int id, Packet packet) {
        switch (id) {
            case VideoInterface.APK_TO_MAIN.PAUSE:
                Driver().pause();
                break;
            case VideoInterface.APK_TO_MAIN.PLAY:
                Driver().play();
                break;
            case VideoInterface.APK_TO_MAIN.STOP:
                Driver().stop();
                break;
            case VideoInterface.APK_TO_MAIN.PREV:
                Driver().prev();
                break;
            case VideoInterface.APK_TO_MAIN.NEXT:
                Driver().next();
                break;
            case VideoInterface.APK_TO_MAIN.PLAY_MODE:
                int mode = packet.getInt("mode");
                LogUtils.d("video mode:" + mode);
                if (mode > 0) {
                    Driver().playMode(mode);
                }
                break;
        }
        return super.dispatch(id, packet);
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        getDriver().getModel().bindListener(mMediaListener);
        DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel().bindListener(mAudioListener);
        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().bindListener(mEventInputListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);
        mCommonDriverModel = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getModel();
        mCommonDriverModel.bindListener(mCommonListener);
    }


    /**
     * 快速上下一曲来电话在MediaLogic里面蓝牙监听比上下一曲先执行，所以这里加了个监听，在播放状态暂停播放.
     * the bluetooth listener
     */
    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            final boolean oldCall = BluetoothDefine.HFPStatus.isCalling(oldVal);
            final boolean newCall = BluetoothDefine.HFPStatus.isCalling(newVal);
            LogUtils.d(TAG, "HFPStatus-->oldVal:" + oldVal + ",newVal:" + newVal + ",mIsCalling:" + mIsCalling);
            LogUtils.d(TAG, "CurSource:" + SourceManager.getCurSource() + ",source:" + source());
            if (newCall) {
                mIsCalling = true;
            } else {
                mIsCalling = false;
                //20180315 添加当前源判断，防止在频繁模式切换时在蓝牙应用挂断电话改变播放状态
                if (newVal == BluetoothDefine.HFPStatus.HFP_STATUS_HANGUP &&
                        playStatus != MediaDefine.PlayStatus.PLAY_STATUS_PAUSE && SourceManager.getCurSource() == source()) {
                    Driver().play();
                }

            }
        }
    };

    private AudioListener mAudioListener = new AudioListener() {
        @Override
        public void AudioStartListener(StringII val) {
            if (null != val) {
                LogUtils.d(TAG, "VideoLogic SystemAudioHandleDriver AudioStartListener, pkgName = " + val.getString() +
                        ", stream = " + AudioDefine.AudioStream.toString(val.getInteger1()) + "[" + val.getInteger1() + "]" +
                        ", focus = " + AudioDefine.AudioFocus.toString(val.getInteger2()) + "[" + val.getInteger2() + "]" +
                        ", active = " + true);

                final String pkgName = val.getString();
                if (!TextUtils.isEmpty(pkgName) && pkgName.equals("com.aispeech.aios")) {
                    /*-begin-20180427-hzubin-modify-for-bug11263视频一直显示加载中（原因未打开视频前，语音被调起，语言退出后播放状态有问题）-*/
                    LogUtils.d("AudioStartListener PlayStatus:" + getInfo().getInt("PlayStatus"));
                    if (getInfo().getInt("PlayStatus") == MediaDefine.PlayStatus.PLAY_STATUS_PLAY) {
                        // 判断指定为语音的包名
                        mVoiceAssistantActive = true;
                    }
                    /*-end-20180427-hzubin-modify-for-bug11263视频一直显示加载中（原因未打开视频前，语音被调起，语言退出后播放状态有问题）-*/
                } else {
                    LogUtils.d(TAG, "VideoLogic AudioStartListener ignore handle real navigation, package = " + pkgName);
                }
            }
        }

        @Override
        public void AudioStopListener(StringII val) {
            if (null != val) {
                LogUtils.d(TAG, "VideoLogic SystemAudioHandleDriver AudioStopListener, pkgName = " + val.getString() +
                        ", stream = " + AudioDefine.AudioStream.toString(val.getInteger1()) + "[" + val.getInteger1() + "]" +
                        ", focus = " + AudioDefine.AudioFocus.toString(val.getInteger2()) + "[" + val.getInteger2() + "]" +
                        ", active = " + false);

                final String pkgName = val.getString();
                LogUtils.d("source:" + source());
                LogUtils.d("SourceManager.getCurSource():" + SourceManager.getCurSource());
                LogUtils.d("SourceManager.getOldSource():" + SourceManager.getOldSource());
                LogUtils.d("SourceManager.getCurBackSource():" + SourceManager.getCurBackSource());
                LogUtils.d("SourceManager.getOldBackSource():" + SourceManager.getOldBackSource());
                if (pkgName.equals("com.aispeech.aios")) {
                    mVoiceAssistantActive = false;
                    if (source() == SourceManager.getCurSource() || (source() == SourceManager.getCurBackSource()
                            && source() == SourceManager.getOldSource())) {//优化恢复出厂设置几率出现无法播放问题,或者条件是修改倒车来电挂断后无法播放问题
                        if (isVoicePausePlayStatus) {//如果不是语音暂停的播放，不继续播放
                            isVoicePausePlayStatus = false;
                            Driver().play();
                        }
                    }
                } else {
                    LogUtils.d(TAG, "VideoLogic AudioStopListener ignore handle real navigation, package = " + pkgName);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        mCommonDriverModel.unbindListener(mCommonListener);
        DriverManager.getDriverByName(AudioDriver.DRIVER_NAME).getModel().unbindListener(mAudioListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);
        ModuleManager.getLogicByName(EventInputDefine.MODULE).getModel().unbindListener(mEventInputListener);
        getDriver().getModel().unbindListener(mMediaListener);
        super.onDestroy();
    }

    @Override
    public Packet getInfo() {
        Packet ret = super.getInfo();
        if (null == ret) {
            ret = new Packet();
        }
        Boolean isBrake = EventInputManager.getBrake();
        Boolean brakeSwitch = mCommonDriverModel.getInfo().getBoolean(SettingsDefine.Common.Switch.BRAKE_WARNING.value());
        Boolean showBrakeWarning = ((!isBrake) && brakeSwitch);
        //LogUtils.d(TAG, " getInfo isBrake:" + isBrake + " brakeSwitch:" + brakeSwitch + " ShowBrakeWarning:" + showBrakeWarning);
        ret.putBoolean("ShowBrakeWarning", showBrakeWarning);
        return ret;
    }

    @Override
    protected int getStorageMethod() {
        return VideoInterface.MAIN_TO_APK.STORAGES_MOUNT_INFO;
    }

    /**
     * the driver interface.
     */
    protected MediaDriverable Driver() {
        MediaDriverable ret = null;     //播放器接口
        BaseDriver drive = getDriver();
        if (drive instanceof MediaDriverable) {
            ret = (MediaDriverable) drive;
        }
        return ret;
    }

    /**
     * the video driver interface.
     */
    protected SuperMediaDriverable.Video.VideoDriverable VideoDriver() {
        SuperMediaDriverable.Video.VideoDriverable ret = null;  //通用接口
        BaseDriver drive = getDriver();
        if (drive instanceof SuperMediaDriverable.Video.VideoDriverable) {
            ret = (SuperMediaDriverable.Video.VideoDriverable) drive;
        }
        return ret;
    }

    @Override
    public BaseDriver newDriver() {
        return new AndroidVideoDriver();
    }

    @Override
    public void onStart() {
        super.onStart();
//        getDriver().onStart();
        if (source() != SourceManager.getCurSource()) {
            String enterPath = getInfo().getString("FilePath");
            if (enterPath != null && VideoDriver() != null) {
                //VideoDriver().enter(enterPath);
            }
        }
        LogUtils.d(TAG, "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        Driver().pause();
        super.onStop();
//        getDriver().onStop();
        LogUtils.d(TAG, "onStop");
    }
}
