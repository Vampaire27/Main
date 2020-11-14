package com.wwc2.main.media.driver.android;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.bluetooth_interface.BluetoothDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerSSS;
import com.wwc2.corelib.model.custom.MFourIntegerArray;
import com.wwc2.corelib.model.custom.MFourStringArray;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.bluetooth.BluetoothListener;
import com.wwc2.main.camera.CameraLogic;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.storage.StorageDriver;
import com.wwc2.main.driver.storage.StorageListener;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.PowerManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.MediaListener;
import com.wwc2.main.media.audio.AudioLogic;
import com.wwc2.main.media.driver.BaseMediaDriver;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.driver.presenter.MediaPresenterImpl;
import com.wwc2.media_interface.MediaDefine;

import java.util.ArrayList;
import java.util.List;

/**
 * the android media driver.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public abstract class AndroidMediaDriver extends BaseMediaDriver implements SuperMediaDriverable.MediaFileDriverable {
    protected Boolean isEnter = false;
    private final String TAG = AndroidMediaDriver.class.getSimpleName();
    protected SuperMediaDriverable.SearchPresenter mSearchPresenter;
    protected List<String> errorFiles = new ArrayList<>();
    protected ErrorHandler mErrorHandler = new ErrorHandler();
    //---------------------------------------------------业务处理--------------------------------------------------------------------------------------------------------

    /**
     * 媒体文件格式过滤器
     */
    public abstract String[] filter();

    /**
     * 要播放的文件
     */
    public abstract void playPath(String pathFile, int startTime);

    /**
     * 单曲循环
     */
    public abstract void setLooping(Boolean isLoop);

    /**
     * 通话状态
     **/
    private boolean mIsCalling = false;

    public static boolean isEnterFromFile = false;//从文件管理器进入

    public static boolean bFirstboot = false;// 是否第一次启动
    public static int bootStorage = 0;/*-begin-20180418-ydinggen-modify-修改默认值为0，如果MCU发默认的0过来时会导致第一次上电进去不会自动播放-*/
    public static int bootIndex = 1;
    public static int mCloseUsb = 0;
    public static int pauseFlag = 0;
    public static int playStorage = 0;

    private Integer mCurrentAccStep = AccoffListener.AccoffStep.STEP_DEFAULT;

    protected void removeStorage(int storageId) {
        mSearchPresenter.removeStorage(storageId);
        Model().setDeviceListInfoArray(storageId, null);//YDG
        removeStorageById(storageId);
        updateSearchInfo(storageId);
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        if (mSearchPresenter == null) {
            mSearchPresenter = new MediaPresenterImpl(getMainContext(), this);
            mSearchPresenter.onCreate();
        }
        LogUtils.d(TAG, "onCreate!");
//        Model().getPlayMode().setVal(MediaDefine.PlayMode.PLAY_MODE_REPEAT_LIST_ALL);//
        DriverManager.getDriverByName(StorageDriver.DRIVER_NAME).getModel().bindListener(mStorageListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().bindListener(mBluetoothListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);
        //SourceManager.getModel().bindListener(mSourceListener);
        PowerManager.getModel().bindListener(mPowerListener);
        McuManager.getModel().bindListener(mMcuListener);
        Model().bindListener(mMediaListener);
        initStorageList();
        playStorage = Model().getPlayStorage().getVal();
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy!");
        bFirstboot = false;
        DriverManager.getDriverByName(StorageDriver.DRIVER_NAME).getModel().unbindListener(mStorageListener);
        ModuleManager.getLogicByName(BluetoothDefine.MODULE).getModel().unbindListener(mBluetoothListener);
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().unbindListener(mAccoffListener);
        //SourceManager.getModel().unbindListener(mSourceListener);
        McuManager.getModel().unbindListener(mMcuListener);
        PowerManager.getModel().unbindListener(mPowerListener);
        Model().unbindListener(mMediaListener);
        if (mSearchPresenter != null) {
            mSearchPresenter.onDestroy();
        }
        super.onDestroy();
        if (AccoffListener.AccoffStep.isDeepSleep(mCurrentAccStep)) {
            LogUtils.d(TAG, "----onDestroy! to removeDevice");
            Model().getTitle().setVal("");
            Model().getArtist().setVal("");
            Model().getAlbum().setVal("");

            removeStorage(StorageDevice.USB);
            removeStorage(StorageDevice.USB1);
            removeStorage(StorageDevice.USB2);
            removeStorage(StorageDevice.USB3);
        }
    }

    @Override
    public void onStart() {
//        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_DEFAULT);
        mSearchPresenter.onStart();
        super.onStart();
    }

    @Override
    public void onStop() {
        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_STOP);
        mSearchPresenter.onStop();
        super.onStop();
    }

    @Override
    public void onResume() {
        mSearchPresenter.onResume();
        super.onResume();
    }

    @Override
    public boolean playFilePath(String filePath, int startTime) {
        LogUtils.d("playFilePath filePath:" + filePath);
        //获取上一首播放的FilePath
        String oldPath = Model().getFilePath().getVal();
        if (TextUtils.isEmpty(filePath)) {
            return false;
        } else {
            if (!FileUtils.isFileExist(filePath)) {
                LogUtils.e("多媒体文件出错");
//                updateErrorStatus(MediaDefine.ErrorState.ERROR_NORMAL);
//                updateByPlayMode(Model().getPlayMode().getVal(), oldPath);
//                startSearchFile(Model().getPlayStorage().getVal());
                /*-begin-20180411-hzubin-modify-for-bug11308播放视频，进入文件管理界面把当前视频剪切到其他文件夹，再进入视频，提示“抱歉，无法播放此视频”后，不会跳到其他视频-*/
                setErrorStatus(MediaDefine.ErrorState.ERROR_NORMAL, filePath);
                /*-end-20180411-hzubin-modify-for-bug11308播放视频，进入文件管理界面把当前视频剪切到其他文件夹，再进入视频，提示“抱歉，无法播放此视频”后，不会跳到其他视频-*/
                return false;
            }
        }
        //LogUtils.d(TAG, "startTime:" + startTime);
        LogUtils.d(TAG, "playFilePath:" + filePath + " oldPath:" + oldPath + " isEnter:" + isEnter);

        if (!filePath.equals(oldPath) || isEnter) {
            //先更新当前播放的存储设备
            updateStorage(filePath, oldPath);
            //更新外设当前下标和播放列表
            updatePlayMode(filePath);
            //1.可以播放添加播放路径
            playPath(filePath, startTime);
            //媒体加载文件后会请求播放！(位置不可以错乱,应为filePath数据包中包含startTime)
//            Model().getFilePath().setVal(filePath);

            updateStorageErrorStatus(MediaDefine.ErrorState.NO_ERROR);
            isEnter = false;
            return true;
        } else {
            LogUtils.d(TAG, "playFilePath" + "Playing the same song!");
        }
        return false;
    }

    @Override
    public boolean play() {
        LogUtils.d(TAG, Define.Source.toString(source()) + " play!");
        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PLAY);
        return true;
    }

    @Override
    public boolean pause() {
        LogUtils.d(TAG, Define.Source.toString(source()) + " pause!");
        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PAUSE);
        return true;
    }

    @Override
    public boolean stop() {
        LogUtils.d(TAG, " stop!");
//        exitSearchTasks();
        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_STOP);
        return true;
    }

    @Override
    public boolean seek(int seek) {
        updateStorageErrorStatus(MediaDefine.ErrorState.NO_ERROR);
        LogUtils.d(TAG + " seek!" + seek);
        return true;
    }

    /**
     * 播放模式
     */
    @Override
    public boolean playMode(int mode) {
        Model().getPlayMode().setVal(mode);//设置播放模式
        String path = Model().getFilePath().getVal();//获取正在播放的全路径
        LogUtils.d(TAG, "test playMode:" + MediaDefine.PlayMode.toString(mode) + " path:" + path);
        updateByPlayMode(mode, path);
        return true;
    }

    @Override
    public boolean prev() {
        LogUtils.d("AndroidMediaDriver prev()");
        if (mIsCalling) {//修改：在音乐播放时快速点击下一曲，来电，通话状态时没暂停音乐。YDG 2017-04-11
            return false;
        }
        String[] fileList = Model().getFileListArray();
        if (fileList != null) {
            int index = getPlayIndex(true);
            LogUtils.d(TAG, "api prev:" + index);
            if (index >= 0 && index < fileList.length) {
                enter();//(单曲)下一曲重播
                int startTime = 0;
                if (index == 0 && 1 == fileList.length) {//只有一首时，上下一曲不把时间先设为0
                    startTime = Model().getCurrentPlayTime();
                }
                if (playFilePath(fileList[index], startTime)) {
                    mErrorHandler.removeMessages(mErrorHandler.NEXT_TAG);//连续上一曲遇到错误文件后导致前一个也无法播放；
                    Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PREV);
                    LogUtils.e(TAG, "mIsCalling:" + mIsCalling);
                    if (mIsCalling) {
                        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PAUSE);
                    }
                }
            } else {
                LogUtils.d(TAG, "next:" + "Song does not exist");
            }
        }
        return false;
    }

    @Override
    public boolean next() {
        LogUtils.d(TAG, "media next:");
        mErrorHandler.removeMessages(mErrorHandler.NEXT_TAG);
//        if (mIsCalling) {//修改：在音乐播放时快速点击下一曲，来电，通话状态时没暂停音乐。YDG 2017-04-11
//            return false;//修改播放完一曲之后来电话和倒车导致停止在此处，导致后续状态有问题倒车回到音乐后无法播放，判断加在下面254行； bug10606 ，bin 2018-1-17
//        }
        String[] fileList = Model().getFileListArray();
        if (fileList != null) {
            int index = getPlayIndex(false);
            LogUtils.d(TAG, "api next:" + index + ",fileList.length:" + fileList.length);
            if (index >= 0 && index < fileList.length) {
                enter();//(单曲)下一曲重播
                int startTime = 0;
                if (index == 0 && 1 == fileList.length) {//只有一首时，上下一曲不把时间先设为0
                    LogUtils.d(TAG, "next----curTime:" + Model().getCurrentPlayTime() + ", totalTime:" + Model().getTotalTime().getVal());
//                    startTime = Model().getCurrentTime().getVal();//播放完成或按下一曲时重新播放。2019-03-13
                }
                if (playFilePath(fileList[index], startTime)) {
                    Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_NEXT);
                    if (index == 0 && 1 == fileList.length) {
                        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PLAY);
                    }
                    if (mIsCalling) {
                        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PAUSE);
                    }
                }
            } else {
                LogUtils.d(TAG, "next:" + "Song does not exist");
            }
        } else {
            LogUtils.d(TAG, "fileList is null!");
        }
        return false;
    }

    /**
     * 设备切换
     *
     * @param storage 设备编号
     */
    public void switchStorage(int storage) {
        LogUtils.d(TAG, "switchStorage setCurrentStorage storage:" + storage);
        updateCurrentStorage(storage);
        String storagePath = StorageDevice.getPath(getMainContext(), storage);
        //******************优化后修改代码2018-12-27 begin*******************//
//        MFourStringArray mFourStringArray = getMFourStringArray(storage);
        FourString[] mFourStringArray = getFourStringArr(storage);
        if (StorageDevice.isDiskMounted(ModuleManager.getContext(), storage)) {
            if (!TextUtils.isEmpty(storagePath)/* && mFourStringArray != null*/) {
                FourString[] storageFourArray = mFourStringArray;//.getVal();
                //******************优化后修改代码2018-12-27 end*******************//
                if (storageFourArray == null) {
                    LogUtils.d(TAG, "storage Switch start search!");
                    mSearchPresenter.startSearchFile(storage, false);//选中搜索不用播放2016/6/16
                } else {
                    LogUtils.d(TAG, "storage Data retransmission!");
//                updateListInfo(storage, null);//设备切换无法收到数据！
                    storageFourArray = storageFourArray.clone();
                    if (storageFourArray.length > 0) {
                        FourString itemFourString = storageFourArray[0];
                        storageFourArray[0] = new FourString(itemFourString.getString1(), itemFourString.getString2(), itemFourString.getString3(), itemFourString.getString4());
                    }
                    updateListInfo(storage, storageFourArray);
                    LogUtils.d(TAG, "switchStorage send again by new !");
                }
            } else {
                LogUtils.d(TAG, "switchStorage cant find storagePath!");
            }
        } else {
            LogUtils.d(TAG, "switchStorage storage not exist!");
        }
    }

    /**
     * 允许播放,特殊情况播放同一首歌
     */
    protected void enter() {
        isEnter = true;
    }

    //----------------------------------------------------------播放处理----------------------------------------------------------------------------
    protected void updateStorage(String filePath, String oldPath) {
        if (TextUtils.isEmpty(filePath)) {
            LogUtils.d(TAG, " updateStorage fail filePah is null");
            return;
        }

        switch (Model().getPlayStorage().getVal()) {
            case StorageDevice.NAND_FLASH:
                if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.NAND_FLASH))) {
                    return;
                }
                break;
            case StorageDevice.MEDIA_CARD:
                if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.MEDIA_CARD))) {
                    return;
                }
                break;
            case  StorageDevice.USB:
                if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB))) {
                    return;
                }
                break;
            case StorageDevice.USB1:
                if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB1))) {
                    return;
                }
                break;
            case StorageDevice.USB2:
                if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB2))) {
                    return;
                }
                break;
            case StorageDevice.USB3:
                if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB3))) {
                    return;
                }
                break;
            default:
                break;
        }

        if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.NAND_FLASH))) {
            if (StorageDevice.NAND_FLASH != Model().getPlayStorage().getVal()) {
                LogUtils.d(TAG, "Play" + StorageDevice.toString(StorageDevice.NAND_FLASH) + "File in! Clear error file log ");
                errorFiles.clear();
                Model().getPlayStorage().setVal(StorageDevice.NAND_FLASH);
            }
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.MEDIA_CARD))) {
            if (StorageDevice.MEDIA_CARD != Model().getPlayStorage().getVal()) {
                LogUtils.d(TAG, "Play" + StorageDevice.toString(StorageDevice.MEDIA_CARD) + "File in! Clear error file log");
                errorFiles.clear();
                Model().getPlayStorage().setVal(StorageDevice.MEDIA_CARD);
            }
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB))) {
            if (StorageDevice.USB != Model().getPlayStorage().getVal()) {
                LogUtils.d(TAG, "Play" + StorageDevice.toString(StorageDevice.USB) + "File in! Clear error file log");
                errorFiles.clear();
                Model().getPlayStorage().setVal(StorageDevice.USB);
            }
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB1))) {
            if (StorageDevice.USB1 != Model().getPlayStorage().getVal()) {
                LogUtils.d(TAG, "Play" + StorageDevice.toString(StorageDevice.USB1) + "File in! Clear error file log");
                errorFiles.clear();
                Model().getPlayStorage().setVal(StorageDevice.USB1);
            }
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB2))) {
            if (StorageDevice.USB2 != Model().getPlayStorage().getVal()) {
                LogUtils.d(TAG, "Play" + StorageDevice.toString(StorageDevice.USB2) + "File in! Clear error file log");
                errorFiles.clear();
                Model().getPlayStorage().setVal(StorageDevice.USB2);
            }
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB3))) {
            if (StorageDevice.USB3 != Model().getPlayStorage().getVal()) {
                LogUtils.d(TAG, "Play" + StorageDevice.toString(StorageDevice.USB3) + "File in! Clear error file log");
                errorFiles.clear();
                Model().getPlayStorage().setVal(StorageDevice.USB3);
            }
        } else if (!TextUtils.isEmpty(oldPath)) {
            if (!filePath.substring(0, filePath.lastIndexOf("/") + 1).equals(oldPath.substring(0, oldPath.lastIndexOf("/") + 1))) {
                LogUtils.d(TAG, "Play files in different folders!");
            } else {
                LogUtils.d(TAG, "Play files in the same folder!");
            }
        }
    }

    //----------------------------------------------------------播放处理----------------------------------------------------------------------------
    protected int getStorage(String filePath) {
        if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.NAND_FLASH))) {
            LogUtils.d(TAG, "enter Play" + StorageDevice.toString(StorageDevice.NAND_FLASH) + "File in!");
            return StorageDevice.NAND_FLASH;
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.MEDIA_CARD))) {
            LogUtils.d(TAG, "enter Play" + StorageDevice.toString(StorageDevice.MEDIA_CARD) + "File in!");
            return StorageDevice.MEDIA_CARD;
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB))) {
            LogUtils.d(TAG, "enter Play" + StorageDevice.toString(StorageDevice.USB) + "File in!");
            return StorageDevice.USB;
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB1))) {
            LogUtils.d(TAG, "enter Play" + StorageDevice.toString(StorageDevice.USB1) + "File in!");
            return StorageDevice.USB1;
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB2))) {
            LogUtils.d(TAG, "enter Play" + StorageDevice.toString(StorageDevice.USB2) + "File in!");
            return StorageDevice.USB2;
        } else if (filePath.startsWith(StorageDevice.getPath(getMainContext(), StorageDevice.USB3))) {
            LogUtils.d(TAG, "enter Play" + StorageDevice.toString(StorageDevice.USB3) + "File in!");
            return StorageDevice.USB3;
        }
        return StorageDevice.UNKNOWN;
    }

    protected void updatePlayMode(String playPath) {
        //LogUtils.d(TAG, "super.updatePlayMode!");
        updateByPlayMode(Model().getPlayMode().getVal(), playPath);
    }

    /**
     * 根据播放类型和播放路径更新播放列表
     *
     * @param playMode 播放模式
     * @param path     播放路径
     */
    protected void updateByPlayMode(int playMode, String path) {
        //LogUtils.d(TAG, "super.updateByPlayMode!");
        if (TextUtils.isEmpty(path)) {
            LogUtils.d(TAG, "updateByPlayMode: update index,total,FileList Failed!");
            return;
        }
        Boolean isRepeatAll = MediaDefine.PlayMode.isRepeatAll(playMode);//列表重复播放
        Boolean isRepeatClose = MediaDefine.PlayMode.isRepeatClose(playMode);//列表重复播放
        Boolean isCurrent = MediaDefine.PlayMode.isCurrent(playMode);//单曲重复播放
        Boolean isFolderAll = MediaDefine.PlayMode.isFolderAll(playMode);
        LogUtils.d("isRepeatAll:" + isRepeatAll + ",isRepeatClose:" + isRepeatClose + ",isCurrent:"
                + isCurrent + ",isFolderAll:" + isFolderAll);
        if (isFolderAll) {
            setLooping(false);
            //******************优化后新增代码2018-12-27 begin*******************//
            Model().setFileListArray(getFolderFileList(path));
            BaseLogic audioLogic = ModuleManager.getLogicByName(com.wwc2.audio_interface.AudioDefine.MODULE);
            if (audioLogic != null && audioLogic instanceof AudioLogic) {
                ((AudioLogic) audioLogic).updateFileList(Model().getFileListArray());
            }
            //******************优化后新增代码2018-12-27 end*******************//
        } else if (isRepeatAll || isRepeatClose || isCurrent) {
            setLooping(isCurrent ? true : false);
            //******************优化后新增代码2018-12-27 begin*******************//
            Model().setFileListArray(getAllFileList());
            BaseLogic audioLogic = ModuleManager.getLogicByName(com.wwc2.audio_interface.AudioDefine.MODULE);
            if (audioLogic != null && audioLogic instanceof AudioLogic) {
                ((AudioLogic) audioLogic).updateFileList(Model().getFileListArray());
            }
            //******************优化后新增代码2018-12-27 begin*******************//
        }
        String[] fileArray = Model().getFileListArray();

        if (fileArray != null) {
            /*-begin-20180605-hzubin-add-bug11515播放视频，ACC深休眠 ，再上ACC视频继续播放，当前曲目和总曲目显示00/00-*/
            //Model().getTotal().setVal(0);
            /*-end-20180605-hzubin-add-bug11515播放视频，ACC深休眠 ，再上ACC视频继续播放，当前曲目和总曲目显示00/00-*/
            Model().getTotal().setValAnyway(fileArray.length);//解决bug12024
            /*-begin-20180213-ydinggen-add-解决在音乐播放时曲目不能同步到Can-*/
            if (Model().getIndex().getVal() == getFileIndex(path) + 1) {
                Model().getIndex().setValAnyway(getFileIndex(path) + 1);//bug14261
            } else {
            /*-end-20180213-ydinggen-add-解决在音乐播放时曲目不能同步到Can-*/
                Model().getIndex().setVal(getFileIndex(path) + 1);
            }
        } else {
            Model().getTotal().setVal(1);
            Model().getIndex().setVal(1);
        }
    }

    /**
     * 播放逻辑处理
     *
     * @param prev
     * @return
     */
    private int getPlayIndex(boolean prev) {
        LogUtils.d(TAG, " prev:" + prev);
        int playMode = Model().getPlayMode().getVal();
        int playIndex = Model().getIndex().getVal() > 0 ? (Model().getIndex().getVal() - 1) : 0;
        LogUtils.d(TAG, " getPlayIndex:" + playIndex);
        String[] folderList = Model().getFileListArray();
        int folderIndexSize = folderList.length - 1;
        Boolean isRepeatAll = MediaDefine.PlayMode.isRepeatAll(playMode);//列表重复播放
        Boolean isRepeatClose = MediaDefine.PlayMode.isRepeatClose(playMode);//列表重复播放
        Boolean isRand = MediaDefine.PlayMode.isRand(playMode);//随机
        Boolean isCurrent = MediaDefine.PlayMode.isCurrent(playMode);//单曲重复播放
        Boolean isFolderAll = MediaDefine.PlayMode.isFolderAll(playMode);
        if (isFolderAll || isRepeatAll || (isCurrent)) {
            //文件夹循环
            if (folderList != null) {
                if (isRand) {
                    int randNum = 0;
                    if (folderList.length > 1) {/*防止单曲点击下一首死循环*/
//                        do{
                        randNum = (int) (0 + Math.random() * (folderIndexSize + 1));
                        LogUtils.d(TAG, "getPlayIndex index:" + randNum + "  isFolderAll&isRepeatAll rand ");
//                        }while(randNum == playIndex);
                    }
//                    randNum += (index - folderIndex);
                    return randNum;
                } else if (prev) {
                    LogUtils.d("getPlayIndex index:" + (playIndex - 1 < 0 ? playIndex : playIndex - 1) + " isFolderAll&isRepeatAll prev " + "  index:" + playIndex);
                    return playIndex - 1 < 0 ? folderIndexSize : playIndex - 1;//已经是第一首了播放最后一首
                } else {
                    LogUtils.d("getPlayIndex index:" + ((playIndex + 1) > (folderList.length - 1) ? 0 : (playIndex + 1)) + " isFolderAll&isRepeatAll next " + "  index:" + playIndex);
                    return (playIndex + 1) > folderIndexSize ? 0 : (playIndex + 1);//已经是最有一首了播放第一首
                }
            }
        } else if (isRepeatClose) {
            //列表不循环
            if (isRand) {
                int randNum = 0;
                if (folderList.length > 1) {/*防止单曲点击下一首死循环*/
//                    do {
                    randNum = (int) (0 + Math.random() * (folderIndexSize + 1));
                    LogUtils.d("getPlayIndex index:" + randNum + " RepeatClose rand ");
//                    } while (randNum == playIndex);
                }
                return randNum;
            } else if (prev) {
                LogUtils.d("getPlayIndex index:" + (playIndex - 1 < 0 ? playIndex : playIndex - 1) + " RepeatClose prev " + "  index:" + playIndex);
                return playIndex - 1 < 0 ? playIndex : playIndex - 1;//已经是第一首了
            } else {
                LogUtils.d("getPlayIndex index:" + ((playIndex + 1) > folderIndexSize ? folderIndexSize : (playIndex + 1)) + " RepeatClose prev " + "  index:" + playIndex);
                return (playIndex + 1) > folderIndexSize ? -1 : (playIndex + 1);//已经是最有一首了//列表不循环，播放到最后一首是错误的
            }
        }
        return 0;
    }

    /**
     * 获取播放文件在播放列表中的下标
     *
     * @param path
     * @return
     */
    public int getFileIndex(String path) {
        int index = 0;
        if (path != null) {
            String[] folderList = Model().getFileListArray();
            if (folderList != null) {
                for (int i = 0; i < folderList.length; i++) {
                    if (path.equals(folderList[i])) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }

    /**
     * 支持文件浏览器播放
     *
     * @param path        文件路径
     * @param currentTime
     */
    protected void enter(String path, int currentTime) {
        int playStorage = Model().getPlayStorage().getVal();
        int pathPlayStorage = getStorage(path);
        //******************优化后修改代码2018-12-27 begin*******************//
//        MFourStringArray mMFourStringArray = getMFourStringArray(pathPlayStorage);
        FourString[] mMFourStringArray = getFourStringArr(pathPlayStorage);
        LogUtils.d(TAG, " do enter playStorage:" + StorageDevice.toString(playStorage) + ",path:" + path + " ,pathPlayStorage:" + StorageDevice.toString(pathPlayStorage) + " mMFourStringArray:" + (mMFourStringArray == null ? "null" : mMFourStringArray.length));
        if (mMFourStringArray != null) {
            playFilePath(path, currentTime);
        } else{
            playFilePath(path, currentTime);
            Model().getPlayStorage().setVal(pathPlayStorage);
            if (path != null && (path.toUpperCase().contains("/DVR") || path.contains("/recordVideo"))) {
                LogUtils.d(TAG, " do enter play DVR Video!");
            } else {
                mSearchPresenter.startSearchFile(pathPlayStorage, false);
            }
        }
    }


    /**
     * 获取当前播放的存储设备文件详细数据
     *
     * @return 全部文件信息
     */
    public FourString[] getMFourStringArrayByPlay() {
        FourString[] mFourString = null;
        int currentStorage = Model().getPlayStorage().getVal();
        //LogUtils.d(TAG, " currentStorage:" + currentStorage + " :" + StorageDevice.toString(currentStorage));
        //******************优化后修改代码2018-12-27 begin*******************//
//        MFourStringArray mFourStringArray = getMFourStringArray(currentStorage);
//        if (mFourStringArray != null) {
//            mFourString = mFourStringArray.getVal();
//        }
        mFourString = getFourStringArr(currentStorage);
        //******************优化后修改代码2018-12-27 end*******************//
        return mFourString;
    }

    /**
     * 获取播放类表
     *
     * @return 当前storage的全部音频视频列表
     */
    protected String[] getAllFileList() {
        FourString[] currentFfArray = getMFourStringArrayByPlay();
        if (currentFfArray == null) {
            LogUtils.d(TAG, "Failed to obtain the full number of media playback peripherals!");
            return null;
        }
        String[] fileList = new String[currentFfArray.length];
        for (int i = 0; i < currentFfArray.length; i++) {
            fileList[i] = currentFfArray[i].getString1();
//            LogUtils.d(TAG," folder:" + fileList[i]);
        }
        return fileList;
    }

    /**
     * 获取文件夹播放列表
     *
     * @return 获取文件夹播放列表
     * @playPath 当前播放的文件
     */
    protected String[] getFolderFileList(String playPath) {
        List<String> folderPathList = new ArrayList<>();
        if (TextUtils.isEmpty(playPath)) {
            LogUtils.d(TAG, " getFolderFileList playPath is null!!:" + playPath);
            return null;
        }
        FourString[] currentFfArray = getMFourStringArrayByPlay();
        if (currentFfArray == null) {
            LogUtils.d(TAG, "Failed to obtain the number of media folders to play!");
            return null;
        }
        if (currentFfArray != null) {
            for (int i = 0; i < currentFfArray.length; i++) {
                //获取文件夹路径
                String path = playPath.substring(0, playPath.lastIndexOf("/") + 1);
                String filePathItem = currentFfArray[i].getString1();
                String filePath = filePathItem.substring(0, filePathItem.lastIndexOf("/") + 1);
                if (path.equals(filePath)) {
//                if(currentFfArray[i].getString1().startsWith(path)){
                    folderPathList.add(currentFfArray[i].getString1());
                }
            }
        }
        LogUtils.d(TAG, "Number of files in a folder:" + folderPathList.size());
        String[] fileList = new String[folderPathList.size()];
        for (int i = 0; i < folderPathList.size(); i++) {
            fileList[i] = folderPathList.get(i);
//            LogUtils.d(TAG," folder:" + fileList[i]);
        }
        return fileList;
    }
    //----------------------------------------------------------播放处理----------------------------------------------------------------------------
    /**
     * 监听多媒体挂载设备
     */
    public StorageListener mStorageListener = new StorageListener() {
        @Override
        public void StorageInfoListener(IntegerSSBoolean oldVal, IntegerSSBoolean newVal) {
            if (null != oldVal) {
                LogUtils.d(TAG, "oldVal type = " + oldVal.getInteger() +
                        ", describe = " + oldVal.getString1() +
                        ", path = " + oldVal.getString2() +
                        ", mounted = " + oldVal.getBoolean() + " source:" + Define.Source.toString(source()));
            }

            if (null != newVal) {
                LogUtils.d(TAG, "newVal type = " + newVal.getInteger() +
                        ", describe = " + newVal.getString1() +
                        ", path = " + newVal.getString2() +
                        ", mounted = " + newVal.getBoolean() + " source:" + Define.Source.toString(source()));

                if (newVal.getBoolean()) {
                    Model().getStorageList().addVal(-1, newVal.getInteger());
                    updateSearchInfo(newVal.getInteger());
                    //updateCurrentStorage(Model().getPlayStorage().getVal());//插入新的设备不高亮
                    if (SourceManager.getCurSource() == source()) {
                        LogUtils.d(TAG, " get Boolean newVal.getInteger():" + newVal.getInteger() + " PlayStorage:" + getPlayStorage());
                        mErrorHandler.removeAllMessage();//设备插入后移除错误检查，防止多次Acc操作后还使用之前的判断，导致切换设备，无记忆
                        if (getPlayStorage() != newVal.getInteger() /*开始获取所有文件(插卡播放)*/) {
//                            Model().getPlayStorage().setVal(newVal.getInteger());//插入U盘不用更新playStorage（避免快速插拔U盘最后拔U盘退出应用）
//                            mSearchPresenter.startSearchFile(newVal.getInteger(), false);//开始获取所有文件(插卡不播放)
                            //startSearchFile(newVal.getInteger());/*监听非播放的设备挂载*/
                            //mErrorHandler.removeAllMessage();//acc sleep change the storage
                            mSearchPresenter.startSearchFile(newVal.getInteger(), false);//不等于记忆中的设备不自动播放
                        } else {
                            mSearchPresenter.startSearchFile(newVal.getInteger(), true);//acc 两个usb切换
                        }
                    } else if (source() == Define.Source.SOURCE_AUDIO && SourceManager.getCurSource() != Define.Source.SOURCE_VIDEO && mSearchPresenter.checkJump()) {
//                    }else if(source() == Define.Source.SOURCE_AUDIO ){
//                        LogUtils.d("playStorage:"+newVal.getInteger());
                        Model().getPlayStorage().setVal(newVal.getInteger());
                        //不是Video和Audio插卡启动Audio AccOn会影响到这里
                        SourceManager.onChangeSource(Define.Source.SOURCE_AUDIO);
                        mSearchPresenter.startSearchFile(newVal.getInteger(), true, source());
                    } else if (SourceManager.getCurBackSource() == source()) {//修改bug14541，解决非记忆USB不扫描问题。2019-01-04
                        mErrorHandler.removeAllMessage();//设备插入后移除错误检查，防止多次Acc操作后还使用之前的判断，导致切换设备，无记忆
                        if (source() == Define.Source.SOURCE_VIDEO) {
                            mSearchPresenter.startSearchFile(newVal.getInteger(), false);
                        } else if (source() == Define.Source.SOURCE_AUDIO) {
                            if (getPlayStorage() != newVal.getInteger() /*开始获取所有文件(插卡播放)*/) {
                                mSearchPresenter.startSearchFile(newVal.getInteger(), false);//不等于记忆中的设备不自动播放
                            } else {
                                mSearchPresenter.startSearchFile(newVal.getInteger(), true);//acc 两个usb切换
                            }
                        }
                    }
                } else {
                    //拔卡退出
                    int playStorage = Model().getPlayStorage().getVal();
                    LogUtils.d(TAG, "remove storage exit !! " + Define.Source.toString(source()) + " :" + StorageDevice.toString(newVal.getInteger()) + " playStorage:" + playStorage + " newVal.getInteger():" + newVal.getInteger());
                    removeStorage(newVal.getInteger());
                    if (playStorage == newVal.getInteger()) {
                        stop();
                        localClearStorage();
                    } else {
                        Model().getCurrentStorage().setVal(playStorage);
                    }
                }
            }
        }

        /**存储设备发生变化, see {@link StorageDevice}*/
        public void StorageSerialNoListener(IntegerSSS oldVal, IntegerSSS newVal) {
            // 打印调用堆栈信息
            RuntimeException e = new RuntimeException("Log: stack info");
            e.fillInStackTrace();
            LogUtils.i(TAG, "onLifeAction stack, ", e);

            if (null != oldVal) {
                LogUtils.d(TAG, "SerialNo newVal type = " + newVal.getInteger() +
                        ", describe = " + newVal.getString1() +
                        ", path = " + newVal.getString2() +
                        ", source:" + Define.Source.toString(source()));
            }
            if (null != newVal) {
                LogUtils.d(TAG, " SerialNo newVal type = " + newVal.getInteger() +
                        ", describe = " + newVal.getString1() +
                        ", path = " + newVal.getString2() +
                        ", source:" + Define.Source.toString(source()));

                if (getPlayStorage() == newVal.getInteger() /*开始获取所有文件(插卡播放)*/) {
                    startSearchFile(newVal.getInteger());/*监听非播放的设备挂载*/
                    LogUtils.d(TAG, StorageDevice.toString(newVal.getInteger()) + " search again and play!");
                } else {
                    //******************优化后修改代码2018-12-27 begin*******************//
//                    MFourStringArray mMFourStringArray = getMFourStringArray(newVal.getInteger());
                    FourString[] mMFourStringArray = getFourStringArr(newVal.getInteger());
                    if (mMFourStringArray != null) {
                        FourString[] mFourString = mMFourStringArray;//.getVal();
                        //******************优化后修改代码2018-12-27 end*******************//
                        if (mFourString != null) {
                            LogUtils.d(TAG, StorageDevice.toString(newVal.getInteger()) + " search again not play!");
                            mSearchPresenter.startSearchFile(newVal.getInteger(), false);//开始获取所有文件(插卡不播放)
                        }
                    }
                }
            }
        }
    };

    public void startSearchFile(int searchStorage) {
        mErrorHandler.removeAllMessage();//acc sleep change the storage
        mSearchPresenter.startSearchFile(searchStorage, true);
    }
    //---------------------------------------------------设备管理--------------------------------------------------------------------------------------------------------

    /**
     * 搜索文件后缀数组
     *
     * @return 文件后缀
     */
    @Override
    public String[] getFilter() {
        return filter();
    }

    /**
     * 获取正在播放的设备号
     *
     * @return 设备号
     */
    @Override
    public int getPlayStorage() {
        return Model().getPlayStorage().getVal();
    }

    /**
     * 获取当前设备的文件类表
     *
     * @return 文件列表
     */
    @Override
    public MFourStringArray getMFourStringArray(int currentStorage) {
        MFourStringArray currentMFourStringArray = null;
        switch (currentStorage) {
            case StorageDevice.NAND_FLASH:
                currentMFourStringArray = Model().getNandFlashListInfo();
                break;
            case StorageDevice.MEDIA_CARD:
                currentMFourStringArray = Model().getSdListInfo();
                break;
            case StorageDevice.USB:
                currentMFourStringArray = Model().getUsbListInfo();
                break;
            case StorageDevice.USB1:
                currentMFourStringArray = Model().getUsb1ListInfo();
                break;
            case StorageDevice.USB2:
                currentMFourStringArray = Model().getUsb2ListInfo();
                break;
            case StorageDevice.USB3:
                currentMFourStringArray = Model().getUsb3ListInfo();
                break;
        }
        return currentMFourStringArray;
    }

    //******************优化后新增代码2018-12-27 begin*******************//
    @Override
    public FourString[] getFourStringArr(int currentStorage) {
        FourString[] currentMFourStringArray = Model().getDeviceListInfoArray(currentStorage);
        return currentMFourStringArray;
    }

    @Override
    public void setFourStringArr(int storageId, FourString[] fileArr) {
        Model().setDeviceListInfoArray(storageId, fileArr);
    }
    //******************优化后新增代码2018-12-27 end*******************//

    /**
     * 更新搜索列表
     *
     * @param searchStorage
     * @param mediaFourStringArr
     */
    @Override
    public void updateListInfo(int searchStorage, FourString[] mediaFourStringArr) {
        if (getPlayStorage() == searchStorage) {
            updateErrorStatus(MediaDefine.ErrorState.NO_ERROR);//初始化
        }
        LogUtils.d(TAG, "storage searchStorage:" + searchStorage + " mediaFourStringArr:" + (mediaFourStringArr == null ? "null" : mediaFourStringArr.length));
        //******************优化后修改代码2018-12-27 begin*******************//
        Model().setDeviceListInfoArray(searchStorage, mediaFourStringArr);
//        switch (searchStorage) {
//            case StorageDevice.NAND_FLASH:
//                Model().getNandFlashListInfo().setVal(mediaFourStringArr);
//                break;
//            case StorageDevice.MEDIA_CARD:
//                Model().getSdListInfo().setVal(mediaFourStringArr);
//                break;
//            case StorageDevice.USB:
//                Model().getUsbListInfo().setVal(mediaFourStringArr);
//                break;
//            case StorageDevice.USB1:
//                Model().getUsb1ListInfo().setVal(mediaFourStringArr);
//                break;
//            case StorageDevice.USB2:
//                Model().getUsb2ListInfo().setVal(mediaFourStringArr);
//                break;
//            case StorageDevice.USB3:
//                Model().getUsb3ListInfo().setVal(mediaFourStringArr);
//                break;
//        }
        //******************优化后修改代码2018-12-27 end*******************//
    }

    /**
     * 获取搜索状态
     *
     * @return
     */
    @Override
    public MFourIntegerArray getSearchInfo() {
        return Model().getSearchInfo();
    }

    @Override
    public void updateSearchInfo(FourInteger[] searchInfo) {
        Model().getSearchInfo().setVal(searchInfo);
    }

    @Override
    public void addSearchInfo(FourInteger searchInfo) {
        Model().getSearchInfo().addVal(-1, searchInfo);
    }

    @Override
    public void playFileInfo(int searchStorage, String filePath, int currentTime) {/*搜索完成播放接口*/
        LogUtils.d("playStorage:" + searchStorage + ",filePath:" + filePath + ",PlayStatus:" + Model().getPlayStatus().getVal());
        //判断加上当前源判断，避免进入音乐搜索后退出音乐进入视频，音乐会播放的问题
        if (/*TextUtils.isEmpty(Model().getFilePath().getVal()) &&  //修改9923 当有两个USB时，播放USB1歌曲关ACC深度休眠再上ACC，USB1歌曲播放不出来（文件名不为空导致）
                (*/Model().getPlayStatus().getVal() == MediaDefine.PlayStatus.PLAY_STATUS_PAUSE ||
                Model().getPlayStatus().getVal() == MediaDefine.PlayStatus.PLAY_STATUS_STOP/*) &&
                        source() == SourceManager.getCurBackSource()*/) {//先判断历史播放记录是否存在，不存在则播放搜索出来的多媒体数据
//            Model().getPlayStorage().setVal(searchStorage);
//            Model().getFilePath().setVal(filePath);//可能出现问题
            enter();
            playFilePath(filePath, currentTime);
        }
    }

    @Override
    public void updateFileInfo(int searchStorage, FourString[] mediaFourStringArr) {
        //调用子类的
        String playFile = Model().getFilePath().getVal();
        //更新播放设备
        updateStorage(playFile, null);
        //维护播放列表
        updatePlayMode(playFile);
    }

    @Override
    public void updatePlayMode() {
        updateByPlayMode(Model().getPlayMode().getVal(), Model().getFilePath().getVal());
    }

    @Override
    public void onFileDeleted(String name) {
        String getFilePath = Model().getFilePath().getVal();
        if (name.equals(getFilePath)) {
            String[] fileList = Model().getFileListArray();
            if (fileList != null && fileList.length == 1) {//delete only one media
                stop();
                localClearStorage();//clear
            } else {
                next();
            }
        }
    }

    @Override
    public String getFilePath() {
        return Model().getFilePath().getVal();
    }

    @Override
    public int getCurrentTime() {
        return Model().getCurrentPlayTime();
    }

    /**
     * 获取挂载设备信息
     */
    @Override
    public IntegerSSBoolean getStorageInfo(Integer type) {
        return StorageDriver.Driver().getStorageInfo(type);
    }

    @Override
    public void updateCurrentStorage(int currentStorage) {
        Model().getCurrentStorage().setVal(currentStorage);
    }

    @Override
    public void updateNoPlayFile(int searchStorage) {
        updateCurrentStorage(searchStorage);
//        Model().getPlayStorage().setVal(searchStorage);
    }

    @Override
    public void updateStorageErrorStatus(int error) {
        Model().getStorageErrorStatus().setVal(error);
    }

    @Override
    public void storageScanStatus(int status) {
        /*-begin-20180502-hzubin-add-for-bug11309在视频界面，显示黑屏，无加载中提示，等25s左右视频才播放(相同的值不会重新设置,先设置为默认值)-*/
        Model().getStorageScanStatus().setVal(0);
        /*-end-20180502-hzubin-add-for-bug11309在视频界面，显示黑屏，无加载中提示，等25s左右视频才播放(相同的值不会重新设置，先设置为默认值)-*/
        Model().getStorageScanStatus().setVal(status);
    }

    @Override
    public int getPlayStatus() {
        return Model().getPlayStatus().getVal();
    }

    @Override
    public void switchCurrentSource() {
        //在当前源才切换到上一个源
        if (SourceManager.getCurSource() == source()) {
            SourceManager.onPopSourceNoPoweroff(Define.Source.SOURCE_AUDIO, Define.Source.SOURCE_VIDEO);
        }
    }

    @Override
    public int currentSource() {
        return source();
    }

    public void updateErrorStatus(int error) {
        Model().getErrorStatus().setValAnyway(error);
    }

    /**
     * 文件播放错误
     *
     * @param errorStatus see{@link com.wwc2.media_interface.MediaDefine.ErrorState}
     */
    protected void setErrorStatus(int errorStatus, String filePath) {
        updateErrorStatus(errorStatus);
        String[] fileList = Model().getFileListArray();
        if (StorageDevice.isDiskMounted(getMainContext(), getPlayStorage())) {
            updateErrorList(filePath);/*添加错误*/
            if (fileList != null) {
                LogUtils.d("errorFile.length:" + errorFiles.size() + " ,fileList.length:" + fileList.length);
                if (errorFiles.size() >= fileList.length) {
                    updateStorageErrorStatus(MediaDefine.StorageErrorState.ERROR_ALL_FILE);
                    errorFiles.clear();//扫描前把错误文件列表清空
                    startSearchFile(Model().getPlayStorage().getVal());
//                    mErrorHandler.sendEmptyMessage(ErrorHandler.MOUNTED_TAG);//启动40s后退出没有处理会死循环
//                    mErrorHandler.sendEmptyMessage(ErrorHandler.SEARCH_TAG);//按优先级播放下一个存储设备
                } else {
                    LogUtils.e("播放错误下一曲");
                    mErrorHandler.playNext();/*播放下一首*/
                }
            } else {
                //bug13497主页接U盘，提示“外部存储器已准备好”后再进音乐，拔掉U盘，15秒后不会自动退出音乐2018-10-26
                Message msg = Message.obtain();
                msg.what = ErrorHandler.MOUNTED_TAG;
                msg.obj = Model().getFilePath().getVal();
                mErrorHandler.sendMessage(msg);//启动40s后退出
            }
        } else {
            //防止ErrorHandler的FilePath路径错误
            Message msg = Message.obtain();
            msg.what = ErrorHandler.MOUNTED_TAG;
            msg.obj = Model().getFilePath().getVal();
            mErrorHandler.sendMessage(msg);//启动40s后退出
            //updateErrorStatus((MediaDefine.ErrorState.ERROR_FILE_PLAY);
        }
    }

    /**
     * 更新错误列表
     */
    void updateErrorList(String errorPath) {
        LogUtils.e("updateErrorList errorPath:" + errorPath);
        boolean needAdd = true;
        if (!TextUtils.isEmpty(errorPath)) {
            for (int i = 0; i < errorFiles.size(); i++) {
                if (errorFiles.get(i).equals(errorPath)) {
                    needAdd = false;
                    break;
                }
            }
            if (needAdd) {
                LogUtils.d("add updateErrorList path:" + errorPath);
                errorFiles.add(errorPath);
            }
            /*-begin-20180509-hzubin-add-播放到错误文件设置当前索引-*/
            LogUtils.d("updateErrorList index:" + Model().getIndex().getVal());
            //******************优化后修改代码2018-12-27 begin*******************//
            if (Model().getFileListArray() != null) {
                for (int j = 0; j < Model().getFileListArray().length; j++) {
                    if (Model().getFileListArray()[j].equals(errorPath)) {
                        if (j + 1 > Model().getFileListArray().length) {
                            //******************优化后修改代码2018-12-27 end*******************//
                            Model().getIndex().setVal(1);
                        } else {
                            Model().getIndex().setVal(j + 1);
                        }
                        break;
                    }
                }
            }
            /*-end-20180509-hzubin-add-播放到错误文件设置当前索引-*/
        }
    }

    /**
     * 错误处理
     * 1、文件无法播放,延迟播放下一首
     * 2、播放下一个挂载设备
     * 3、检查挂载40s
     */
    public class ErrorHandler extends Handler {
        public static final int NEXT_TIME = 2000;
        public static final int MOUNTED_TIME = 1000;
        public static final int NEXT_TAG = 1;
        public static final int SEARCH_TAG = 2;
        public static final int MOUNTED_TAG = 3;
        final int checkCount = 35;
        int cycleCount = checkCount;
        private String TAG = ErrorHandler.class.getSimpleName();

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NEXT_TAG:
                    this.removeMessages(NEXT_TAG);
                    LogUtils.d(TAG, "postDelayed ErrorStatus:" + MediaDefine.ErrorState.toString(Model().getErrorStatus().getVal()) + " SourceManager.getCurBackSource():" + Define.Source.toString(SourceManager.getCurBackSource()));
                    if (SourceManager.getCurBackSource() == source()) {
                        if (Model().getStorageErrorStatus().getVal() != MediaDefine.StorageErrorState.ERROR_ALL_FILE) {
                            updateErrorStatus(MediaDefine.ErrorState.NO_ERROR);
                            next();
                        }
                    }
                    break;
                case SEARCH_TAG:
                    mSearchPresenter.updateNoPlayFile(Model().getPlayStorage().getVal());
                    break;
                case MOUNTED_TAG:
                    this.removeMessages(MOUNTED_TAG);
                    String filePath = (String) msg.obj;
                    cycleCount--;
                    LogUtils.d(TAG, " MOUNTED_TAG cycleCount:" + cycleCount);
                    int playStorage = Model().getPlayStorage().getVal();
                    if (StorageDevice.isDiskMounted(getMainContext(), playStorage)) {
                        LogUtils.d(TAG, " MOUNTED_TAG playStorage:" + StorageDevice.toString(playStorage) + " mounted go play");
                        if (!TextUtils.isEmpty(filePath) && FileUtils.isFileExist(filePath)) {
                            enter();
                            int currentTime = Model().getCurrentPlayTime();
                            //Model().getFilePath().setVal(null);//防止当前源不对记忆被抹掉
                            playFilePath(filePath, currentTime);
                        } else {
                            //修改bug13497，当本地没有文件时，需要退出。2018-11-09
                            //******************优化后修改代码2018-12-27 begin*******************//
//                            MFourStringArray mMFourStringArray = getMFourStringArray(playStorage);
                            FourString[] mMFourStringArray = getFourStringArr(playStorage);
                            //******************优化后修改代码2018-12-27 end*******************//
                            if (null == mMFourStringArray) {
                                LogUtils.e(TAG, "mMFourStringArray == null");
                                if (cycleCount > 0) {
                                    this.sendEmptyMessageDelayed(MOUNTED_TAG, MOUNTED_TIME / 2);
                                    return;
                                } else {
                                    localClearStorage();
                                }
                            } else {
                                LogUtils.d(TAG, "mMFourStringArray != null");
                                if (cycleCount > 0) {//修改acc on后，音乐会自动下一曲。2018-12-18
                                    this.sendEmptyMessageDelayed(MOUNTED_TAG, MOUNTED_TIME);
                                } else {
                                    mErrorHandler.playNext();
                                }
                            }
                        }
                        cycleCount = checkCount;
                    } else {
                        if (cycleCount > 0) {
                            this.sendEmptyMessageDelayed(MOUNTED_TAG, MOUNTED_TIME);
                        } else {
                            updateStorageErrorStatus(MediaDefine.StorageErrorState.ERROR_NO_FILE_PLAY);
                            LogUtils.d(TAG, " MOUNTED_TAG playStorage:" + StorageDevice.toString(playStorage) + " exit!");
                            cycleCount = checkCount;
                            removeStorage(playStorage);
                            clearStorage();
//                            localClearStorage();
//                            mounting = false;
                            /*-begin-20180419-hzubin-modify-for-播放文件连续不存在，则扫描设备-*/
                            //如果当前设备文件错误，先查看本地、SDCard、usb、usb1、2、3，都没有切上一个源
                            //******************优化后修改代码2018-12-27 begin*******************//
                            if (Model().getDeviceListInfoArray(StorageDevice.NAND_FLASH) != null) {
                                LogUtils.d(TAG, "MOUNTED_TAG local card not null");
                                mSearchPresenter.startSearchFile(StorageDevice.NAND_FLASH, false);
                            } else {
                                if (Model().getDeviceListInfoArray(StorageDevice.MEDIA_CARD) != null) {
                                    LogUtils.d(TAG, "MOUNTED_TAG sd card not null");
                                    mSearchPresenter.startSearchFile(StorageDevice.MEDIA_CARD, false);
                                } else {
                                    if (Model().getDeviceListInfoArray(StorageDevice.USB) != null && playStorage != StorageDevice.USB) {
                                        LogUtils.d(TAG, "MOUNTED_TAG usb not null");
                                        mSearchPresenter.startSearchFile(StorageDevice.USB, false);
                                    } else {
                                        if (Model().getDeviceListInfoArray(StorageDevice.USB1) != null && playStorage != StorageDevice.USB1) {
                                            LogUtils.d(TAG, "MOUNTED_TAG usb1 not null");
                                            mSearchPresenter.startSearchFile(StorageDevice.USB1, false);
                                        } else {
                                            if (Model().getDeviceListInfoArray(StorageDevice.USB2) != null && playStorage != StorageDevice.USB2) {
                                                LogUtils.d(TAG, "MOUNTED_TAG usb2 not null");
                                                mSearchPresenter.startSearchFile(StorageDevice.USB2, false);
                                            } else {
                                                if (Model().getDeviceListInfoArray(StorageDevice.USB3) != null && playStorage != StorageDevice.USB3) {
                                                    //******************优化后修改代码2018-12-27 end*******************//
                                                    LogUtils.d(TAG, "MOUNTED_TAG usb3 not null");
                                                    mSearchPresenter.startSearchFile(StorageDevice.USB3, false);
                                                } else {
                                                    LogUtils.d(TAG, "MOUNTED_TAG usb3 is null");
                                                    localClearStorage();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            /*-end-20180419-hzubin-modify-for-播放文件连续不存在，则扫描设备-*/
                        }
                    }
                    break;
            }
        }

        public void removeAllMessage() {
            cycleCount = checkCount;
            this.removeMessages(MOUNTED_TAG);
            this.removeMessages(NEXT_TAG);
        }

        //播放下一首
        public void playNext() {
            this.removeMessages(NEXT_TAG);
            this.sendEmptyMessageDelayed(NEXT_TAG, NEXT_TIME);
        }
    }

    /**
     * 清空历史
     */
    @Override
    public void clearStorage() {
        /*当前播放的设备不清除*/
        Model().getFileList().setVal(null);
        Model().setFileListArray(null);
        Model().getFilePath().setVal(null);
        Model().getTotal().setVal(0);
        Model().getIndex().setVal(0);
        Model().getTitle().setVal(null);
        Model().getAlbum().setVal(null);
        Model().getArtist().setVal(null);
        Model().setCurrentPlayTime(0);
        Model().getTotalTime().setVal(0);
        LogUtils.d(TAG, " clearStorage:" + "SourceManager.getCurSource():" + SourceManager.getCurSource() + "source():" + source());

    }

    void localClearStorage() {
        clearStorage();
        /*-begin-20180504-hzubin-add-设备拔出后默认切换到本地-*/
        Model().getPlayStorage().setVal(StorageDevice.NAND_FLASH);
        /*-end-20180504-hzubin-add-设备拔出后默认切换到本地-*/
        //在当前源才切换到上一个源
        if (SourceManager.getCurSource() == source()) {
            SourceManager.onPopSourceNoPoweroff(Define.Source.SOURCE_AUDIO, Define.Source.SOURCE_VIDEO);
        } else if (SourceManager.getCurSource() == Define.Source.SOURCE_CAMERA) {
            int source = SourceManager.popSourceNoPoweroffExceptArray(Define.Source.SOURCE_AUDIO, Define.Source.SOURCE_VIDEO);
            if (Define.Source.SOURCE_NONE == source) {
                source = SourceManager.getFirstPoweronDefaultSource();
            }
            CameraLogic.setEnterCameraSource(source, false);
            SourceManager.onOpenBackgroundSource(source);
            /*-begin-20180418-ydinggen-modify-后台U盘播放音乐拔U盘，再进音乐不会播放-*/
        } else if (SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
            SourceManager.onOpenBackgroundSource(Define.Source.SOURCE_SILENT);
            /*-end-20180418-ydinggen-modify-后台U盘播放音乐拔U盘，再进音乐不会播放-*/
        }
    }

    private void updateSearchInfo(int storageId) {
        MFourIntegerArray mFourIntegerArray = getSearchInfo();/*移除搜索状态*/
        if (mFourIntegerArray != null) {
            FourInteger[] mFourIntegers = mFourIntegerArray.getVal();
            if (mFourIntegers != null) {
                for (int i = 0; i < mFourIntegers.length; i++) {
                    if (storageId == mFourIntegers[i].getInteger1()) {
                        mFourIntegerArray.delVal(i);
                    }
                    break;
                }
            }
        }
    }

    /**
     * init mount storageList
     */
    private void initStorageList() {
        List<Integer> mountStorageArray = StorageDevice.getAllMountStorageDevices(getMainContext());
        if (mountStorageArray != null) {
            Object[] objectArray = mountStorageArray.toArray();
            if (objectArray instanceof Integer[]) {
                LogUtils.d(TAG, "mountStorageArray:" + mountStorageArray.size());
                Integer[] mountArray = (Integer[]) objectArray;
                Model().getStorageList().setVal(mountArray);
            }
        }
    }

    private void removeStorageById(int storageId) {
        Integer[] mountStorageArray = Model().getStorageList().getVal();
        if (mountStorageArray != null) {
            for (int i = 0; i < mountStorageArray.length; i++) {
                if (storageId == mountStorageArray[i]) {
                    Model().getStorageList().delVal(i);
                    LogUtils.d(TAG, "removeStorageById path:" + StorageDevice.toString(storageId));
                }
            }
        }
    }

    /**
     * the bluetooth listener
     */
    private BluetoothListener mBluetoothListener = new BluetoothListener() {
        @Override
        public void HFPStatusListener(Integer oldVal, Integer newVal) {
            final boolean oldCall = BluetoothDefine.HFPStatus.isCalling(oldVal);
            final boolean newCall = BluetoothDefine.HFPStatus.isCalling(newVal);
            //LogUtils.d("media bt oldVal:" + BluetoothDefine.HFPStatus.toString(oldVal) + ", newVal:" + BluetoothDefine.HFPStatus.toString(newVal));
            //LogUtils.d("media bt oldCall:" + oldCall + ", newCall:" + newCall);

            if (oldCall && !newCall) {
                mIsCalling = false;
            } else if (!oldCall && newCall) {
                mIsCalling = true;
            }
        }
    };

    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            mCurrentAccStep = newVal;
        }
    };

    private McuManager.MCUListener mMcuListener = new McuManager.MCUListener() {
        @Override
        public void DataListener(byte[] val) {
            //LogUtils.e("MCU_TO_ARM.MRPT_MEDIA_INFO---val[0]:" + FormatData.formatHexBufToString(val, 1));
            if (null != val) {
                byte cmd = (byte) (val[0] & 0xFF);
                if (cmd == (byte) McuDefine.MCU_TO_ARM.MRPT_MEDIA_INFO) {
                    //LogUtils.e("MCU_TO_ARM.MRPT_MEDIA_INFO---val[0]:" + FormatData.formatHexBufToString(val, 1));
                    if (val != null && val.length > 5) {
                        int PlayStorage = val[1] & 0xFF;
                        int curIndex = (int) ((val[5] & 0xFF)
                                | ((val[4] & 0xFF) << 8)
                                | ((val[3] & 0xFF) << 16)
                                | ((val[2] & 0xFF) << 24));
                        LogUtils.d(TAG, "MCU_TO_ARM.MRPT_MEDIA_INFO---PlayStorage=" + PlayStorage + ", index=" + curIndex);
                        if (bFirstboot) {
                            bootStorage = PlayStorage;
                            bootIndex = curIndex;
                            LogUtils.e("第一次启动 bootStorage：" + bootStorage + ",bootIndex:" + bootIndex);
                            /*-begin-20180418-ydinggen-add-增加MCU记忆的盘符判断，如果MCU发默认的0过来时会导致第一次上电进去不会自动播放-*/
                            if (PlayStorage > 0) {
                                /*-end-20180418-ydinggen-add-增加MCU记忆的盘符判断，如果MCU发默认的0过来时会导致第一次上电进去不会自动播放-*/
                                Model().getPlayStorage().setVal(PlayStorage);
                                Model().getIndex().setVal(curIndex);
                            }
                        } else {
                            LogUtils.e("不是第一次启动");
                        }
                    }
//                } else if (cmd == (byte) McuDefine.MCU_TO_ARM.MRPT_CLOSE_USB) {
//                    LogUtils.e("MCU_TO_ARM.MRPT_CLOSE_USB---val[1]:" + (val[1] & 0xFF) + ", curSource=" + SourceManager.getCurSource());
//                    if (SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
//                        mCloseUsb = 0;
//                        LogUtils.e("MCU_TO_ARM.MRPT_CLOSE_USB-return-when acc off");
//                        SystemStorageDriver.setUsbOffFlag();
//                        return;
//                    }
//                    if (val.length > 1) {
//                        if ((val[1] & 0xFF) == 1) {
//                            mCloseUsb = (val[1] & 0xFF) + 1;
//                            SystemStorageDriver.setUsbOffFlag();
//                            if (SourceManager.getCurSource() == Define.Source.SOURCE_VIDEO) {
//                                if (MediaLogic.playStorage != 1) {
//                                    pauseFlag = 1;
//                                }
//                            } else if (SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
//                                if (MediaLogic.playStorage != 1) {
//                                    pauseFlag = 2;
////                                    pause();
//                                }
//                            }
//                            if (pauseFlag == 1 || pauseFlag == 2) {
//                                if (EventInputManager.getCamera()) {
//                                    CameraLogic.setEnterCameraSource(Define.Source.SOURCE_SILENT, false);
//                                    SourceManager.onOpenBackgroundSource(Define.Source.SOURCE_SILENT);
//                                } else {
//                                    SourceManager.onChangeSource(Define.Source.SOURCE_SILENT);
//                                }
//                            }
//                        } else {
//                            if (mCloseUsb != 0 ) {
//                                mCloseUsb = (val[1] & 0xFF) + 1;
//                                SystemStorageDriver.setUsbOffFlag();
//                            } else {
//                                mCloseUsb = 0;
//                            }
//                        }
//                        LogUtils.e("MCU_TO_ARM.MRPT_CLOSE_USB---mCloseUsb:" + mCloseUsb + ", val=" + (val[1] & 0xFF + 1));
//                    }
                }
            }
        }
    };

    private PowerManager.PowerListener mPowerListener = new PowerManager.PowerListener() {
        @Override
        public void PowerStepListener(Integer oldVal, Integer newVal) {
            //if (PowerManager.PowerStep.isPoweronOvered(newVal)) {
            LogUtils.e("mPowerListener PowerStepListener newVal:" + newVal);
            bFirstboot = true;
            //}
        }
    };

    public static boolean setMountedAfterOpen(int type, boolean mount) {
        LogUtils.d("setMountedAfterOpen--- type=" + type + ", playStorage=" + playStorage + ", mount=" + mount + ", mCloseUsb=" + AndroidMediaDriver.mCloseUsb);
        boolean ret = false;
        if (mCloseUsb == 1) {
            int source = Define.Source.SOURCE_NONE;
            if (type == playStorage) {
                mCloseUsb = 0;
                if (mount) {
                    if (pauseFlag == 2) {
//                    EventInputManager.NotifyKeyEvent(true, Define.KeyOrigin.DEFAULT, Define.Key.KEY_PLAY, null);
                        source = Define.Source.SOURCE_AUDIO;
                    } else if (pauseFlag == 1) {
                        source = Define.Source.SOURCE_VIDEO;
                    }
                    if (source != Define.Source.SOURCE_NONE) {
                        if (EventInputManager.getCamera()) {
                            CameraLogic.setEnterCameraSource(source, false);
                            SourceManager.onOpenBackgroundSource(source);
                        } else {
                            SourceManager.onChangeSource(source);
                        }
                        pauseFlag = 0;
                    }
                    ret = true;
                }
            }
        }
        return ret;
    }

    /*-begin-20180419-hzubin-modify-for-bug11235播放usb2音乐，主页，视频，ACC浅休眠，拔掉usb2，再上ACC在视频界面，主页，再进入音乐不播放其它目录歌曲，且自动退回主页-*/
    MediaListener mMediaListener = new MediaListener() {
        public void IndexListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "Index:" + newVal);
            playStorage = Model().getPlayStorage().getVal();
            sendMediaInfoToMcu(playStorage, newVal);
        }

        @Override
        public void PlayStorageListener(Integer oldVal, Integer newVal) {
            LogUtils.d("PlayStorageListener PlayStorageListener oldVal:" + oldVal + " , newVal:" + newVal);
            sendPlayStatusToMcu();
            mErrorHandler.removeAllMessage();
        }

        @Override
        public void PlayModeListener(Integer oldVal, Integer mode) {
            LogUtils.d(TAG, "PlayModeListener mode:" + MediaDefine.PlayMode.toString(mode));
            sendPlayStatusToMcu();
        }

        @Override
        public void PlayStatusListener(Integer oldVal, Integer playStatus) {
            LogUtils.d(TAG, "PlayStatusListener playStatus:" + MediaDefine.PlayStatus.toString(playStatus));
            sendPlayStatusToMcu();
        }
    };
    /*-end-20180419-hzubin-modify-for-bug11235播放usb2音乐，主页，视频，ACC浅休眠，拔掉usb2，再上ACC在视频界面，主页，再进入音乐不播放其它目录歌曲，且自动退回主页-*/

    public void sendMediaInfoToMcu(int storage, int index) {
        if (EventInputManager.getAcc()) {
            if (storage > 0 && index > 0) {
                byte[] src = new byte[5];
                src[0] = (byte) storage;
                src[1] = (byte) ((index >> 24) & 0xFF);
                src[2] = (byte) ((index >> 16) & 0xFF);
                src[3] = (byte) ((index >> 8) & 0xFF);
                src[4] = (byte) (index & 0xFF);
                LogUtils.d(TAG, "ARM_TO_MCU.MRPT_MEDIA_INFO---PlayStorage=" + storage + ", index=" + index);
                McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_MEDIA_INFO, src, 5);
            }
        }
    }


    public void sendPlayStatusToMcu() {
        byte[] data = new byte[4];
        int playStatus = Model().getPlayStatus().getVal();
        if(playStatus == MediaDefine.PlayStatus.PLAY_STATUS_PAUSE) {
            data[0] = 0x00;
        } else if(playStatus == MediaDefine.PlayStatus.PLAY_STATUS_PLAY) {
            data[0] = 0x01;
        }
        int mode = Model().getPlayMode().getVal();
        if(MediaDefine.PlayMode.isFolderAll(mode)) {
            data[1] = 0x00; //文件夹循环
        } else if(MediaDefine.PlayMode.isCurrent(mode)){
            data[1] = 0x01;//单曲循环
        } else if(MediaDefine.PlayMode.isRepeatClose(mode)) {
            data[1] = 0x02;//顺序播放
        } else if(MediaDefine.PlayMode.isRepeatAll(mode)){
            data[1] = 0x03;//全部循环
        }
        if(MediaDefine.PlayMode.isRand(mode)) {
            data[2] = 0x01; //随机播放开
        } else {
            data[2] = 0x00; //随机播放关
        }
        int playStorage =  Model().getPlayStorage().getVal();
        if(playStorage == StorageDevice.USB || playStorage == StorageDevice.USB1 ||
                playStorage == StorageDevice.USB2 || playStorage == StorageDevice.USB3) {
            data[3] = 0x01;  //usb设备
        } else if(playStorage == StorageDevice.MEDIA_CARD) {
            data[3] = 0x02;  //sd卡
        } else {
            data[3] = 0x00;  //非USB、SD
        }
        LogUtils.d(TAG, "sendPlayStatusToMcu : playStatus=" + MediaDefine.PlayStatus.toString(playStatus)+";playMode="+mode+";playStorage="+playStorage);
        send2Mcu((byte) McuDefine.ARM_TO_MCU.OP_MEDIA_STATUS_INFO, data, 4);
    }


    public void send2Mcu(byte cmd, byte[] data, int len) {
        LogUtils.d(TAG, byteTohexString(cmd) + " " + bytesTohexString(data));
        McuManager.sendMcu(cmd, data, len);
    }

    public static String byteTohexString(byte data) {
        String hexStr = "";
        if ((data & 0xf0) == 0) hexStr += "0";
        hexStr += Integer.toHexString(data&0xff);
        hexStr += " ";
        return hexStr;
    }

    public static String bytesTohexString(byte[] data) {
        String str = "";
        for (byte d: data) {
            str += byteTohexString(d);
        }
        return str;
    }
}