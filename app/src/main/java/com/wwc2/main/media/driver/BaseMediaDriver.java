package com.wwc2.main.media.driver;

import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MByteArray;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.model.MStringArray;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.MFourIntegerArray;
import com.wwc2.corelib.model.custom.MFourStringArray;
import com.wwc2.corelib.model.custom.MIntegerStringArray;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.media_interface.MediaDefine;

/**
 * the base media driver.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public abstract class BaseMediaDriver extends BaseMemoryDriver implements MediaDriverable {
    private String TAG = BaseMediaDriver.class.getSimpleName();
    private final String KEY_FILEPATH = "FILE_PATH";
    private final String KEY_CURRENTTIME = "KEY_CURRENTTIME";
    private final String SECTION = "MEDIA";

    /**
     * 数据Model
     */
    protected static class MediaModel extends BaseModel {

        //******************已优化代码，暂保留2018-12-27 begin*******************//
        /**
         * 文件列表
         */
        private MStringArray mFileList = new MStringArray(this, "FileListListener", null);

        public MStringArray getFileList() {
            return mFileList;
        }
        //******************已优化代码，暂保留2018-12-27 end*******************//


        /**
         * 播放模式
         */
        private MInteger mPlayMode = new MInteger(this, "PlayModeListener", MediaDefine.PlayMode.PLAY_MODE_REPEAT_LIST_ALL);

        public MInteger getPlayMode() {
            return mPlayMode;
        }

        /**
         * 当前播放时间
         */
        private MInteger mCurrentTime = new MInteger(this, "CurrentTimeListener", 0);

        public MInteger getCurrentTime() {
            return mCurrentTime;
        }

        int mCurrentPlayTime = 0;
        public int getCurrentPlayTime() {
            return mCurrentPlayTime;
        }
        public void setCurrentPlayTime(int time) {
            mCurrentPlayTime = time;
        }

        /**
         * 媒体总时间
         */
        private MInteger mTotalTime = new MInteger(this, "TotalTimeListener", 0);

        public MInteger getTotalTime() {
            return mTotalTime;
        }

        /**
         * 播放状态, see {@link com.wwc2.media_interface.MediaDefine.PlayMode}
         */
        private MInteger mPlayStatus = new MInteger(this, "PlayStatusListener", MediaDefine.PlayStatus.PLAY_STATUS_DEFAULT);

        public MInteger getPlayStatus() {
            return mPlayStatus;
        }

        /**
         * 播放下标, 从0开始
         */
        private MInteger mIndex = new MInteger(this, "IndexListener", -1);

        public MInteger getIndex() {
            return mIndex;
        }

        /**
         * 媒体列表总数
         */
        private MInteger mTotal = new MInteger(this, "TotalListener", 0);

        public MInteger getTotal() {
            return mTotal;
        }

        /**
         * 播放文件全路径
         */
        private MString mFilePath = new MString(this, "FilePathListener", null);

        public MString getFilePath() {
            return mFilePath;
        }

        /**
         * 视频尺寸
         */
        private MInteger mScreenSize = new MInteger(this, "ScreenSizeListener", MediaDefine.ScreenSize.SCREEN_SIZE_DEFAULT);

        public MInteger getScreenSize() {
            return mScreenSize;
        }

        /**
         * 媒体播放器错误状态, see {@link com.wwc2.media_interface.MediaDefine.ErrorState}
         */
        private MInteger mErrorStatus = new MInteger(this, "ErrorStatusListener", MediaDefine.ErrorState.ERROR_DEFAULT);

        public MInteger getErrorStatus() {
            return mErrorStatus;
        }

        /**
         * 状态媒体播放器错误, see {@link com.wwc2.media_interface.MediaDefine.ErrorState}
         */
        private MInteger mStorageErrorStatus = new MInteger(this, "StorageErrorStatusListener", MediaDefine.ErrorState.ERROR_DEFAULT);

        public MInteger getStorageErrorStatus() {
            return mStorageErrorStatus;
        }

        /**
         * 存储设备列表, see {@link com.wwc2.common_interface.utils.StorageDevice}
         */
        private MIntegerArray mStorageList = new MIntegerArray(this, "StorageListListener", null);

        public MIntegerArray getStorageList() {
            return mStorageList;
        }

        /**
         * 正在播放的存储设备, see {@link com.wwc2.common_interface.utils.StorageDevice}
         */
        private MInteger mPlayStorage = new MInteger(this, "PlayStorageListener", StorageDevice.NAND_FLASH);//按要求默认本地存储卡，避免等待时间过长

        public MInteger getPlayStorage() {
            return mPlayStorage;
        }

        /**
         * 当前显示的存储设备
         */
        private MInteger mCurrentStorage = new MInteger(this, "CurrentStorageListener", StorageDevice.MEDIA_CARD);//按要求默认sd卡

        public MInteger getCurrentStorage() {
            return mCurrentStorage;
        }

        /**
         * 搜索的存储信息设备, int storage, int state, boolean over, boolean auto
         */
        private MFourIntegerArray mSearchInfo = new MFourIntegerArray(this, "SearchInfoListener", null);

        public MFourIntegerArray getSearchInfo() {
            return mSearchInfo;
        }

        /**
         * 音频标题名称
         */
        private MString mTitle = new MString(this, "TitleListener", null);

        public MString getTitle() {
            return mTitle;
        }

        /**
         * 音频艺术家名称
         */
        private MString mArtist = new MString(this, "ArtistListener", null);

        public MString getArtist() {
            return mArtist;
        }

        /**
         * 音频专辑名称
         */
        private MString mAlbum = new MString(this, "AlbumListener", null);

        public MString getAlbum() {
            return mAlbum;
        }

        /**
         * 音频专辑封面数据
         */
        private MByteArray mAlbumData = new MByteArray(this, "AlbumDataListener", null);

        public MByteArray getAlbumData() {
            return mAlbumData;
        }

        /**
         * 音频歌词数据
         */
        private MIntegerStringArray mLrcData = new MIntegerStringArray(this, "LrcDataListener", null);

        public MIntegerStringArray getLrcData() {
            return mLrcData;
        }

        /**
         * 音频FFT频谱数据
         */
        private MByteArray mFftData = new MByteArray(this, "FftDataListener", null);

        public MByteArray getFftData() {
            return mFftData;
        }

        /**
         * 音频WAVE频谱数据
         */
        private MByteArray mWaveData = new MByteArray(this, "WaveDataListener", null);

        public MByteArray getWaveData() {
            return mWaveData;
        }

        //******************已优化代码，暂保留2018-12-27 begin*******************//
        /**
         * 本地音频信息列表, string path, string title, string artist, string album.
         */
        private MFourStringArray mNandFlashListInfo = new MFourStringArray(this, "NandFlashListInfo", null);

        public MFourStringArray getNandFlashListInfo() {
            return mNandFlashListInfo;
        }

        /**
         * SdCard音频信息列表, string path, string title, string artist, string album.
         */
        private MFourStringArray mSdListInfo = new MFourStringArray(this, "SdListInfoListener", null);

        public MFourStringArray getSdListInfo() {
            return mSdListInfo;
        }

        /**
         * Usb1音频信息列表, string path, string title, string artist, string album.
         */
        private MFourStringArray mUsbListInfo = new MFourStringArray(this, "UsbListInfoListener", null);

        public MFourStringArray getUsbListInfo() {
            return mUsbListInfo;
        }

        /**
         * Usb2音频信息列表, string path, string title, string artist, string album.
         */
        private MFourStringArray mUsb1ListInfo = new MFourStringArray(this, "Usb1ListInfoListener", null);

        public MFourStringArray getUsb1ListInfo() {
            return mUsb1ListInfo;
        }

        /**
         * Usb3音频信息列表, string path, string title, string artist, string album.
         */
        private MFourStringArray mUsb2ListInfo = new MFourStringArray(this, "Usb2ListInfoListener", null);

        public MFourStringArray getUsb2ListInfo() {
            return mUsb2ListInfo;
        }

        /**
         * Usb4音频信息列表, string path, string title, string artist, string album.
         */
        private MFourStringArray mUsb3ListInfo = new MFourStringArray(this, "Usb3ListInfoListener", null);

        public MFourStringArray getUsb3ListInfo() {
            return mUsb3ListInfo;
        }
        //******************已优化代码，暂保留2018-12-27 end*******************//

        /**
         * AccOn后是否在扫描记忆设备, 0：没有,1：正在扫描,2:未挂载,3:10s或者20s后未检测出视频文件
         */
        private MInteger mStorageScanStatus = new MInteger(this, "StorageScanStatusListener", 0);

        public MInteger getStorageScanStatus() {
            return mStorageScanStatus;
        }

        //******************优化后新增代码2018-12-27 begin*******************//
        private FourString[] mNandFlashListArray = null;
        private FourString[] mCardListArray = null;
        private FourString[] mUsbListArray = null;
        private FourString[] mUsb1ListArray = null;
        private FourString[] mUsb2ListArray = null;
        private FourString[] mUsb3ListArray = null;
        public void setDeviceListInfoArray(int storageId, FourString[] mListArray) {
            switch (storageId) {
                case StorageDevice.NAND_FLASH:
                    mNandFlashListArray = mListArray;
                    break;
                case StorageDevice.MEDIA_CARD:
                    mCardListArray = mListArray;
                    break;
                case StorageDevice.USB:
                    mUsbListArray = mListArray;
                    break;
                case StorageDevice.USB1:
                    mUsb1ListArray = mListArray;
                    break;
                case StorageDevice.USB2:
                    mUsb2ListArray = mListArray;
                    break;
                case StorageDevice.USB3:
                    mUsb3ListArray = mListArray;
                    break;
                default:
                    break;
            }

            if (mListArray != null) {
                LogUtils.d("setDeviceListInfoArray-storageId=" + storageId + ", size=" + mListArray.length);
            }
            getUpdateListInfo().setValAnyway(storageId);
        }
        public FourString[] getDeviceListInfoArray(int storageId) {
            FourString[] ret = null;
            switch (storageId) {
                case StorageDevice.NAND_FLASH:
                    ret = mNandFlashListArray;
                    break;
                case StorageDevice.MEDIA_CARD:
                    ret = mCardListArray;
                    break;
                case StorageDevice.USB:
                    ret = mUsbListArray;
                    break;
                case StorageDevice.USB1:
                    ret = mUsb1ListArray;
                    break;
                case StorageDevice.USB2:
                    ret = mUsb2ListArray;
                    break;
                case StorageDevice.USB3:
                    ret = mUsb3ListArray;
                    break;
                default:
                    break;
            }
            return ret;
        }

        /**
         * 当前文件列表
         */
        private String[] mFileListArray = null;
        public void setFileListArray(String[] array) {
            mFileListArray = array;
        }
        public String[] getFileListArray() {
            return mFileListArray;
        }

        private MInteger mUpdateListInfo = new MInteger(this, "UpdateListInfoListener", -1);

        public MInteger getUpdateListInfo() {
            return mUpdateListInfo;
        }
        //******************优化后新增代码2018-12-27 end*******************//

        @Override
        public Packet getInfo() {
            Packet mPacket = new Packet();
            mPacket.putInt("PlayMode", mPlayMode.getVal());
            mPacket.putInt("CurTime", mCurrentPlayTime);//mCurrentTime.getVal());
            mPacket.putInt("TotalTime", mTotalTime.getVal());
            mPacket.putInt("PlayStatus", mPlayStatus.getVal());
            mPacket.putInt("Index", mIndex.getVal());
            mPacket.putInt("Total", mTotal.getVal());
            mPacket.putString("FilePath", mFilePath.getVal());
            mPacket.putInt("ErrorStatus", mErrorStatus.getVal());
            mPacket.putInt("StorageErrorStatus", mStorageErrorStatus.getVal());
            mPacket.putIntegerObjectArray("StorageList", mStorageList.getVal());
            mPacket.putInt("PlayStorage", mPlayStorage.getVal());
            mPacket.putInt("CurrentStorage", mCurrentStorage.getVal());
            mPacket.putObject("SearchInfo", mSearchInfo.getVal());
            mPacket.putString("artist", mArtist.getVal());
            mPacket.putString("album", mAlbum.getVal());
            mPacket.putString("title", mTitle.getVal());
            mPacket.putByteArray("AlbumData", toBytes(mAlbumData.getVal()));

            //******************已优化代码，暂保留2018-12-27 begin*******************//
            //通过APK起来后从Main中获取，避免2000首以上出现数据传送失败，导致列表无法显示。
//            mPacket.putStringArray("FileList", mFileList.getVal());
//            mPacket.putParcelableArray("LrcData", mLrcData.getVal());
//            mPacket.putParcelableArray("FlashInfo", mNandFlashListInfo.getVal());
//            mPacket.putParcelableArray("SdInfo", mSdListInfo.getVal());
//            mPacket.putParcelableArray("UsbInfo", mUsbListInfo.getVal());
//            mPacket.putParcelableArray("Usb1Info", mUsb1ListInfo.getVal());
//            mPacket.putParcelableArray("Usb2Info", mUsb2ListInfo.getVal());
//            mPacket.putParcelableArray("Usb3Info", mUsb3ListInfo.getVal());
            //******************已优化代码，暂保留2018-12-27 end*******************//

            mPacket.putInt("screenSize", mScreenSize.getVal());
            mPacket.putInt("StorageScanStatus", mStorageScanStatus.getVal());
            return mPacket;
        }


        byte[] toBytes(Byte[] bytesPrim) {
            byte[] bytes = null;
            if (bytesPrim != null) {
                bytes = new byte[bytesPrim.length];
//                int i = 0;
//                for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
                for (int i = 0; i < bytes.length; i++) {
                    if (bytesPrim != null) {
                        bytes[i] = bytesPrim[i];
                    } else {
                        bytes = null;
                        break;
                    }
                }
            }
            return bytes;

        }
    }

    @Override
    public BaseModel newModel() {
        return new MediaModel();
    }

    /**
     * get the model object.
     */
    protected MediaModel Model() {
        MediaModel ret = null;
        BaseModel model = getModel();
        if (model instanceof MediaModel) {
            ret = (MediaModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        if (mMemory != null) {
            mMemory.save();
        }
    }

    @Override
    public boolean prev() {
        return false;
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public boolean ff() {
        return false;
    }

    @Override
    public boolean fb() {
        return false;
    }

    @Override
    public boolean playMode(int mode) {
        return false;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        LogUtils.d(TAG, "readData");
        if (mMemory != null) {
            Object filePath = mMemory.get(SECTION, KEY_FILEPATH);
            if (filePath != null) {
                String path = (String) filePath;
                if (!TextUtils.isEmpty(path)) {
//                    Model().getFilePath().setVal(path);
                    LogUtils.d(TAG, "readData filePath:" + path);
                    ret = true;
                }
            }
            Object currentTime = mMemory.get(SECTION, KEY_CURRENTTIME);
            if (filePath != null) {
                try {
                    Integer cTime = Integer.parseInt(currentTime.toString());
//                    Model().getCurrentTime().setVal(cTime);
                    LogUtils.d(TAG, "readData cTime:" + cTime);
                    ret = true;
                } catch (NumberFormatException e) {
                    LogUtils.w(TAG, "read CurrentTime fail!!");
                }
            }
            LogUtils.d(TAG, "readData  filePath:" + filePath + " currentTime:" + currentTime + Define.Source.toString(source()));
        }
        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        LogUtils.d(TAG, "writeData!");
        if (mMemory != null) {
            String filePath = Model().getFilePath().getVal();
            if (!TextUtils.isEmpty(filePath)) {
                mMemory.set(SECTION, KEY_FILEPATH, filePath);
            }
            int currentTime = Model().getCurrentPlayTime();
            mMemory.set(SECTION, KEY_CURRENTTIME, currentTime+"");
            LogUtils.d(TAG, "writeData  filePath:" + filePath + " currentTime:" + currentTime + Define.Source.toString(source()));
            ret = true;
        }
        return ret;
    }

    public abstract int source();
}
