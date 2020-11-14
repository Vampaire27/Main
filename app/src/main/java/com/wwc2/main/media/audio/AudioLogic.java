package com.wwc2.main.media.audio;

import android.os.SystemClock;
import android.text.TextUtils;

import com.wwc2.audio_interface.AudioDefine;
import com.wwc2.audio_interface.AudioInterface;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.IntegerString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.MediaListener;
import com.wwc2.main.media.MediaLogic;
import com.wwc2.main.media.driver.MediaDriverable;
import com.wwc2.main.media.driver.android.AndroidAudioDriver;
import com.wwc2.main.media.driver.android.AndroidMediaDriver;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.driver.search.FilesSendRunnable;
import com.wwc2.media_interface.MediaDefine;

import java.util.Arrays;

/**
 * the audio logic.
 *
 * @author wwc2
 * @date 2017/1/12
 */
public class AudioLogic extends MediaLogic {
    private final String TAG = AudioLogic.class.getSimpleName();

    @Override
    public String getTypeName() {
        return "audio";
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.audio";
    }

    @Override
    public String getAPKClassName() {
        return "com.wwc2.audio.view.MainActivity";
    }

    protected MediaListener mMediaListener = new MediaListener() {
        //******************已优化代码，暂保留2018-12-27 begin*******************//
        /**文件列表监听器*/
        public void FileListListener(String[] oldVal, String[] newVal) {
            LogUtils.d(TAG, "FileList:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putStringArray("FileList", newVal);
            Notify(AudioInterface.MAIN_TO_APK.FILE_LIST, mPacket);
        }
        //******************已优化代码，暂保留2018-12-27 end*******************//

        /**播放模式监听器, see {@link com.wwc2.media_interface.MediaDefine.PlayMode}*/
        public void PlayModeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "PlayMode:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("PlayMode", newVal);
            Notify(AudioInterface.MAIN_TO_APK.PLAY_MODE, mPacket);
        }

        /**当前播放时间监听器*/
        public void CurrentTimeListener(Integer oldVal, Integer newVal) {
//            LogUtils.d(TAG,"CurTime:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("CurTime", newVal);
            Notify(AudioInterface.MAIN_TO_APK.CURRENT_TIME, mPacket);

            sendTimeInfoToMcu(newVal);
        }

        /**媒体总时间监听器*/
        public void TotalTimeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "TotalTime:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("TotalTime", newVal);
            Notify(AudioInterface.MAIN_TO_APK.TOTAL_TIME, mPacket);
        }

        /**播放状态监听器, see {@link com.wwc2.media_interface.MediaDefine.PlayStatus}*/
        public void PlayStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d("PlayStatus:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("PlayStatus", newVal);
            Notify(AudioInterface.MAIN_TO_APK.PLAY_STATUS, mPacket);

            if (MediaDefine.PlayStatus.isPlay(newVal) && SourceManager.getCurBackSource() != source()) {
                Driver().pause();
            }
        }

        /**播放下标监听器, 从0开始*/
        public void IndexListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Index:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("Index", newVal);
            Notify(AudioInterface.MAIN_TO_APK.INDEX, mPacket);
        }

        /**媒体列表总数监听器*/
        public void TotalListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Total:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("Total", newVal);
            Notify(AudioInterface.MAIN_TO_APK.TOTAL, mPacket);
        }

        /**播放文件全路径监听器*/
        public void FilePathListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "FilePath:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putString("FilePath", newVal);
            Notify(AudioInterface.MAIN_TO_APK.FILE_PATH, mPacket);
        }

        /**视频尺寸监听器*/
        public void ScreenSizeListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "ScreenSize:" + newVal);
        }

        /**媒体播放器错误状态监听器, see {@link com.wwc2.media_interface.MediaDefine.ErrorState}*/
        public void ErrorStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "ErrorStatus:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("ErrorStatus", newVal);
            Notify(AudioInterface.MAIN_TO_APK.ERROR_STATUS, mPacket);
        }

        /**媒体播放器错误状态监听器, see {@link com.wwc2.media_interface.MediaDefine.StorageErrorState}*/
        public void StorageErrorStatusListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "StorageErrorStatus:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("StorageErrorStatus", newVal);
            Notify(AudioInterface.MAIN_TO_APK.STORAGE_ERROR_STATUS, mPacket);
        }

        /**存储设备列表监听器, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
        public void StorageListListener(Integer[] oldVal, Integer[] newVal) {
            LogUtils.d(TAG, "StorageList:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putIntegerObjectArray("StorageList", newVal);
            Notify(AudioInterface.MAIN_TO_APK.STORAGE_LIST, mPacket);
        }

        /**正在播放的存储设备监听器, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
        public void PlayStorageListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "PlayStorage:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("PlayStorage", newVal);
            Notify(AudioInterface.MAIN_TO_APK.PLAY_STORAGE, mPacket);
        }

        /**当前显示的存储设备监听器*/
        public void CurrentStorageListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "CurrentStorage:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putInt("CurrentStorage", newVal);
            Notify(AudioInterface.MAIN_TO_APK.CURRENT_STORAGE, mPacket);
        }

        /**搜索的存储信息设备监听器, int storage, int state, boolean over, boolean auto*/
        public void SearchInfoListener(FourInteger[] oldVal, FourInteger[] newVal) {
            LogUtils.d(TAG, "Search_info:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("SearchInfo", newVal);
            Notify(AudioInterface.MAIN_TO_APK.SEARCH_INFO, mPacket);
        }

        /**音频标题名称监听器*/
        public void TitleListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "Title:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putString("title", newVal);
            Notify(AudioInterface.MAIN_TO_APK.TITLE, mPacket);
        }

        /**音频艺术家名称监听器*/
        public void ArtistListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "Artist:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putString("artist", newVal);
            Notify(AudioInterface.MAIN_TO_APK.ARTIST, mPacket);
        }

        /**音频专辑名称监听器*/
        public void AlbumListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "Album:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putString("album", newVal);
            Notify(AudioInterface.MAIN_TO_APK.ALBUM, mPacket);
        }

        /**音频专辑封面数据监听器*/
        public void AlbumDataListener(Byte[] oldVal, Byte[] newVal) {
            LogUtils.d(TAG, "AlbumData:");
            Packet mPacket = new Packet();
            if (newVal != null) {
                mPacket.putByteArray("AlbumData", toBytes(newVal));
            } else {
                mPacket.putByteArray("AlbumData", null);
            }
            Notify(AudioInterface.MAIN_TO_APK.ALBUM_DATA, mPacket);
        }

        /**音频歌词数据监听器*/
        public void LrcDataListener(IntegerString[] oldVal, IntegerString[] newVal) {
            LogUtils.d(TAG, "LrcData:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("LrcData", newVal);
            Notify(AudioInterface.MAIN_TO_APK.LRC_DATA, mPacket);
        }

        /**音频FFT频谱数据监听器*/
        public void FftDataListener(Byte[] oldVal, Byte[] newVal) {
            LogUtils.d(TAG, "FftData:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putByteObjectArray("FftData", newVal);
            Notify(AudioInterface.MAIN_TO_APK.FFT_DATA, mPacket);
        }

        /**音频WAVE频谱数据监听器*/
        public void WaveDataListener(Byte[] oldVal, Byte[] newVal) {
            LogUtils.d(TAG, "WaveData:" + newVal);
            Packet mPacket = new Packet();
            mPacket.putByteObjectArray("WaveData", newVal);
            Notify(AudioInterface.MAIN_TO_APK.WAVE_DATA, mPacket);
        }

        /**音频信息列表监听器, string path, string title, string artist, string album.*/
        public void ListInfoListener(FourString[] oldVal, FourString[] newVal) {
//            LogUtils.d(TAG, "ListInfo:" +  ((null == newVal) ? "null":newVal.length));
//            Packet mPacket = new Packet();
//            mPacket.putParcelableArray("info", newVal);
//            Notify(AudioInterface.MAIN_TO_APK.LIST_INFO, mPacket);
        }

        //******************已优化代码，暂保留2018-12-27 begin*******************//
        /**本地音频信息列表监听器, string path, string title, string artist, string album.*/
        public void NandFlashListInfo(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "NandFlashListInfo:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("FlashInfo", newVal);
            Notify(AudioInterface.MAIN_TO_APK.FLASH_LIST_INFO, mPacket);
        }

        /**SdCard音频信息列表监听器, string path, string title, string artist, string album.*/
        public void SdListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "SdListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("SdInfo", newVal);
            Notify(AudioInterface.MAIN_TO_APK.SD_LIST_INFO, mPacket);
        }

        /**Usb1音频信息列表监听器, string path, string title, string artist, string album.*/
        public void UsbListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "UsbListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("UsbInfo", null);
            Notify(AudioInterface.MAIN_TO_APK.USB_LIST_INFO, mPacket);
        }

        /**Usb2音频信息列表监听器, string path, string title, string artist, string album.*/
        public void Usb1ListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "Usb1ListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("Usb1Info", null);
            Notify(AudioInterface.MAIN_TO_APK.USB1_LIST_INFO, mPacket);
        }

        /**Usb3音频信息列表监听器, string path, string title, string artist, string album.*/
        public void Usb2ListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "Usb2ListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("Usb2Info", null);
            Notify(AudioInterface.MAIN_TO_APK.USB2_LIST_INFO, mPacket);
        }

        /**Usb4音频信息列表监听器, string path, string title, string artist, string album.*/
        public void Usb3ListInfoListener(FourString[] oldVal, FourString[] newVal) {
            LogUtils.d(TAG, "Usb3ListInfoListener:" + ((null == newVal) ? "null" : newVal.length));
            Packet mPacket = new Packet();
            mPacket.putParcelableArray("Usb3Info", null);
            Notify(AudioInterface.MAIN_TO_APK.USB3_LIST_INFO, mPacket);
        }
        //******************已优化代码，暂保留2018-12-27 begin*******************//

        byte[] toBytes(Byte[] bytesPrim) {

            byte[] bytes = new byte[bytesPrim.length];
            int i = 0;
            for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
            return bytes;

        }

        //******************优化后新增代码2018-12-27 begin*******************//
        @Override
        public void UpdateListInfoListener(Integer oldVal, Integer newVal) {
            if (newVal == 0) {
//                for ()
//                updateUsb3ListInfo(Driver().getDeviceListInfo(StorageDevice.USB3));
            } else {
                //刷新newVal设备列表
                updateDeviceListInfo(newVal, Driver().getDeviceListInfo(newVal));
            }
        }
        //******************优化后新增代码2018-12-27 end*******************//

        @Override
        public void UpdateCurrentTime(Integer value) {
            Packet mPacket = new Packet();
            mPacket.putInt("CurTime", value);
            Notify(AudioInterface.MAIN_TO_APK.CURRENT_TIME, mPacket);

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
            Notify(AudioInterface.MAIN_TO_APK.FILE_LIST, mPacket1);
        }
    }
    //******************优化后新增代码2018-12-27 end*******************//

    @Override
    public Packet dispatch(int nId, Packet packet) {
        LogUtils.d("dispatch:" + nId);
        if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
            LogUtils.d("audio logicdispatch diabale for acc off mode.");
            return super.dispatch(nId, packet);
        }
        switch (nId) {
            case AudioInterface.APK_TO_MAIN.PLAY:
                Driver().play();
                break;
            case AudioInterface.APK_TO_MAIN.PAUSE:
                Driver().pause();
                break;
            case AudioInterface.APK_TO_MAIN.STOP:
                Driver().stop();
                break;
            case AudioInterface.APK_TO_MAIN.PREV:
                Driver().prev();
                break;
            case AudioInterface.APK_TO_MAIN.NEXT:
                Driver().next();
                break;
            case AudioInterface.APK_TO_MAIN.FF:
                break;
            case AudioInterface.APK_TO_MAIN.FB:
                break;
            case AudioInterface.APK_TO_MAIN.PLAY_MODE:
                int mode = packet.getInt("mode");
                //LogUtils.d("mode:" + mode);
                if (mode > 0) {
                    Driver().playMode(mode);
                }
                break;
            case AudioInterface.APK_TO_MAIN.SEEK:
                int seek = packet.getInt("seek");
                if (seek >= 0) {
                    Driver().seek(seek);
                }
                //LogUtils.d(TAG, "seek:" + seek);
                break;
            case AudioInterface.APK_TO_MAIN.SWITCH_STORAGE:
                int storage = packet.getInt("storage");
                LogUtils.d(TAG, "storage:" + storage);
                if (storage > 0 && AudioDriver() != null) {
                    AudioDriver().switchStorage(storage);
                }
                break;
            case AudioInterface.APK_TO_MAIN.PLAY_FILEPATH:
                String path = packet.getString("path");
                if (!TextUtils.isEmpty(path)) {
                    Driver().playFilePath(path, 0);
                }
                LogUtils.d(TAG, "path:" + path);
                break;
            case AudioInterface.APK_TO_MAIN.ENTER:
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
                    LogUtils.d(TAG, "do enter enterPath:!! " + enterPath);
                    if (!TextUtils.isEmpty(enterPath) && AudioDriver() != null) {
                        AudioDriver().enter(enterPath);
                    }
                }
                break;
            case AudioInterface.APK_TO_MAIN.ENTER_TITLE:
                String title = packet.getString("titlePath");
                if (AudioDriver().enterByTitle(title)) {
                    packet.putBoolean("result", true);
                }
                break;
            case AudioInterface.APK_TO_MAIN.ENTER_ARTIST:
                String[] artistPathArray = packet.getStringArray("artistPathArray");
                AudioDriver().enterByArtist(artistPathArray);
                break;
            case AudioInterface.APK_TO_MAIN.ENTER_ALBUM:
                String[] albumPathArray = packet.getStringArray("albumPathArray");
                AudioDriver().enterByAlbum(albumPathArray);
                break;
            case AudioInterface.APK_TO_MAIN.SEARCH_CURRENT_STORAGE:
                int sStorage = packet.getInt("storage");
                AudioDriver().searchStorage(sStorage);
                break;
            case AudioInterface.APK_TO_MAIN.WIDGET_PLAY:
                if (source() == SourceManager.getCurBackSource()) {
                    Driver().play();
                } else {
                    SourceManager.onChangeSource(source());
                }
                break;
            case AudioInterface.APK_TO_MAIN.WIDGET_NEXT:
                if (source() == SourceManager.getCurBackSource()) {
                    Driver().next();
                } else {
                    SourceManager.onChangeSource(source());
                }
                break;
            case AudioInterface.APK_TO_MAIN.WIDGET_PREV:
                if (source() == SourceManager.getCurBackSource()) {
                    Driver().prev();
                } else {
                    SourceManager.onChangeSource(source());
                }
                break;
            case AudioInterface.APK_TO_MAIN.MUSIC_ID3INFO:
                LogUtils.d(" AudioInterface.APK_TO_MAIN.MUSIC_ID3INFO name:" + packet.getString("filePath"));
                AudioDriver().updateFileDetail(new FourString(packet.getString("filePath"),
                        packet.getString(AudioDefine.TITLE), packet.getString(AudioDefine.ARTIST),
                        packet.getString(AudioDefine.ALBUM)));
                break;
            default:
                break;
        }
        return super.dispatch(nId, packet);
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = true;
        // if (!mIsCalling) {
        LogUtils.d(TAG, "key:" + Define.Key.toString(key));
        if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
            LogUtils.d("media logic onKeyEvent diabale for acc off mode.");
            return ret;
        }

        /*-begin-20180530-ydinggen-modifly-方控随机改为随机模式切换，重复改为重复模式切换-*/
        switch (key) {
            case Define.Key.KEY_RAND:
//                Driver().playMode(MediaDefine.PlayMode.PLAY_MODE_RAND);
                Driver().changeRandomMode();
                break;
            case Define.Key.KEY_RPT:
//                Driver().playMode(MediaDefine.PlayMode.PLAY_MODE_REPEAT_CURRENT);
                Driver().changeRepeatMode();
                break;
            default:
                ret = false;
                break;
        }
        /*-begin-20180530-ydinggen-modifly-防止按键处理两次-*/
        return ret ? ret : super.onKeyEvent(keyOrigin, key, packet);
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        getDriver().getModel().bindListener(mMediaListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getDriver().getModel().unbindListener(mMediaListener);
    }

    @Override
    public Packet getInfo() {
//        LogUtils.d( "Audio getInfo");
        return super.getInfo();
    }

    @Override
    protected int getStorageMethod() {
        return AudioInterface.MAIN_TO_APK.STORAGES_MOUNT_INFO;
    }

    /**
     * the driver interface.
     */
    protected MediaDriverable Driver() {
        MediaDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof MediaDriverable) {
            ret = (MediaDriverable) drive;
        }
        return ret;
    }

    /**
     * the driver interface.
     */
    protected SuperMediaDriverable.Audio.AudioDriverable AudioDriver() {
        SuperMediaDriverable.Audio.AudioDriverable ret = null;
        BaseDriver drive = getDriver();
        if (drive instanceof SuperMediaDriverable.Audio.AudioDriverable) {
            ret = (SuperMediaDriverable.Audio.AudioDriverable) drive;
        }
        return ret;
    }


    @Override
    public String getMessageType() {
        return AudioDefine.MODULE;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_AUDIO;
    }

    @Override
    public BaseDriver newDriver() {
        return new AndroidAudioDriver();
    }

    @Override
    public void onStart() {
        LogUtils.d(TAG, "onStart");
        super.onStart();
//        getDriver().onStart();
//        if(source() == SourceManager.getCurSource()){
        //2017/1/16     accOn 无法打开后台(播放)
//        LogUtils.d(TAG, "onStart source() == SourceManager.getCurSource()");
        String enterPath = getInfo().getString("FilePath");
        LogUtils.d("onStart enterPath:" + enterPath);
        if (!TextUtils.isEmpty(enterPath) && AudioDriver() != null) {
            AudioDriver().enter(enterPath);
        }
//        }
    }

//    @Override
//    public void onResume() {
//        LogUtils.d(TAG, "onResume");
//        super.onResume();
////        getDriver().onResume();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
////        getDriver().onStop();
//        LogUtils.d(TAG, "onStop");
//    }
//
//    @Override
//    public boolean runApk() {
//        return super.runApk();
//    }

}
