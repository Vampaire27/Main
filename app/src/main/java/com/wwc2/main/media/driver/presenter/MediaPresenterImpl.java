package com.wwc2.main.media.driver.presenter;


import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.MFourIntegerArray;
import com.wwc2.corelib.model.custom.MFourStringArray;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.storage.StorageDriver;
import com.wwc2.main.driver.storage.StorageDriverable;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.driver.android.AndroidMediaDriver;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.driver.observer.FileObserverManager;
import com.wwc2.main.media.driver.observer.MultiFileObserver;
import com.wwc2.main.media.driver.search.FilesSearchRunnable;
import com.wwc2.main.media.driver.utils.Pinyin4jUtil;
import com.wwc2.media_interface.MediaDefine;
import com.wwc2.settings_interface.SettingsDefine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huwei on 2017/1/13.
 */
public class MediaPresenterImpl implements SuperMediaDriverable.SearchPresenter, SuperMediaDriverable.ListInfoListener, MultiFileObserver.FileListener {
    private final Context mContext;
    private String TAG = MediaPresenterImpl.class.getSimpleName();
    /*-begin-20180423-hzubin-modify-for-修改java.util.ConcurrentModificationException异常-*/
    protected List<FilesSearchRunnable> filesSearchRunnables = new CopyOnWriteArrayList<>();
    /*-end-20180423-hzubin-modify-for-修改java.util.ConcurrentModificationException异常-*/
    //播放顺序热拔真正播放的存储设备那就按顺序播放下一个挂载的设备
    private int[] storageSort = {StorageDevice.USB3, StorageDevice.USB2, StorageDevice.USB1, StorageDevice.USB, StorageDevice.MEDIA_CARD, StorageDevice.NAND_FLASH};
    //Media更新数据
    private SuperMediaDriverable.MediaFileDriverable mMediaFileDriverable;
    //文件监听
    private FileObserverManager mFileObserverManager = new FileObserverManager(this);
    //线程池数量
    private int mPoolSize = 4;
    //线程池
    private ExecutorService executorService;
    private ExitHandler mExitHandler;
    private ScanMemoryHandler scanMemoryHandler = new ScanMemoryHandler();
    private int count = 8;//等待时间
    private final int PLAY_FILE_INFO = 10001;
    private int tmpPlayStorage = StorageDevice.NAND_FLASH;
    UsbManager usbManager = null;
    private final int MEMORY_NO_SCAN = 0; //未搜索记忆设备
    private final int MEMORY_SCANNING = 1; //正在搜索记忆设备
    private final int MEMORY_SCAN_COMPLETION = 2; //记忆设备搜索完成
    private int isScanMemory = MEMORY_NO_SCAN;//记忆设备状态

    public MediaPresenterImpl(Context context, SuperMediaDriverable.MediaFileDriverable mMediaFileDriverable) {
        this.mContext = context;
        this.mMediaFileDriverable = mMediaFileDriverable;
        mExitHandler = new ExitHandler();
        executorService = Executors.newFixedThreadPool(mPoolSize);
    }

    //------------------------mediaPresenter提供其它类调用的API---------------------
    @Override
    public String[] getFilter() {

        return mMediaFileDriverable.getFilter();
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        LogUtils.d("MediaPresenterImpl onResume");

        if (usbManager != null && usbManager.getDeviceList().size() > 0) {
            count = 30;
        } else {
            count = 25;
        }
        LogUtils.d(TAG, "onResume----count=" + count);
        isScanMemory = MEMORY_NO_SCAN;
        tmpPlayStorage = mMediaFileDriverable.getPlayStorage();
        int playStorage = mMediaFileDriverable.getPlayStorage();
        LogUtils.d("init playStorage:" + playStorage + ", isEnterFromFile=" + AndroidMediaDriver.isEnterFromFile);
        LogUtils.d("onResume before AndroidMediaDriver.bFirstboot:" + AndroidMediaDriver.bFirstboot);
        if (AndroidMediaDriver.bootStorage == 0) {//默认值为0不当第一次启动
            AndroidMediaDriver.bFirstboot = false;
        }
        LogUtils.d("onResume after AndroidMediaDriver.bFirstboot:" + AndroidMediaDriver.bFirstboot);
        if (AndroidMediaDriver.bFirstboot && AndroidMediaDriver.bootStorage != 0) {
            tmpPlayStorage = AndroidMediaDriver.bootStorage;
            playStorage = AndroidMediaDriver.bootStorage;
        }
        LogUtils.d("onResume playStorage:" + playStorage + ", old FilePath:" + mMediaFileDriverable.getFilePath() + ", playtime:" + mMediaFileDriverable.getCurrentTime());
        IntegerSSBoolean mIntegerSSBoolean = mMediaFileDriverable.getStorageInfo(playStorage);
        //******************优化后修改代码2018-12-27 begin*******************//
        FourString[] playStorageArray = mMediaFileDriverable.getFourStringArr(playStorage);
//        MFourStringArray mMFourStringArray = mMediaFileDriverable.getMFourStringArray(playStorage);
//        if (mMFourStringArray != null) {
//            playStorageArray = mMFourStringArray.getVal();
//        }
        //******************优化后修改代码2018-12-27 end*******************//
        LogUtils.d(TAG, " onResume playStorage " + StorageDevice.toString(playStorage) + " reflect mounted:" + StorageDevice.isDiskMounted(ModuleManager.getContext(), playStorage) +
                " mounted:" + ((mIntegerSSBoolean == null) ? " false" : mIntegerSSBoolean.getString1() + " " + mIntegerSSBoolean.getBoolean() + " !"));
        LogUtils.d(TAG, "playStorageArray:" + (playStorageArray == null ? "null" : playStorageArray.length));

        for (int i = 0; i < storageSort.length; i++) {
            if (playStorage != storageSort[i]) {
                if (StorageDevice.isDiskMounted(ModuleManager.getContext(), storageSort[i])) {
                    LogUtils.d(TAG, "设备:" + storageSort[i] + "，存在！");

                    /*-begin-20180419-ydinggen-modify-未收到挂载广播，实际设备已挂载，重新更新设备挂载列表。解决usb在音乐读不到但文件管理器中有-*/
                    IntegerSSBoolean mStorageBoolean = mMediaFileDriverable.getStorageInfo(storageSort[i]);
                    if (mStorageBoolean != null && !mStorageBoolean.getBoolean()) {
                        LogUtils.d(TAG, "device:" + storageSort[i] + " not exit in list!");
                        StorageDriverable storageDriverable = StorageDriver.Driver();
                        if (storageDriverable != null) {
                            storageDriverable.updateMountedStatus(storageSort[i], true);
                        }
                    } else {
                        LogUtils.d(TAG, "device:" + storageSort[i] + " exit in list!");
                    }
                    /*-end-20180419-ydinggen-modify-未收到挂载广播，实际设备已挂载，重新更新设备挂载列表。解决usb在音乐读不到但文件管理器中有-*/

                    FourInteger searchInfo = getSearchInfoById(storageSort[i]);
                    if (searchInfo != null) {
                        LogUtils.d("onResume if searchInfo 1:" + searchInfo.getInteger1() + ",2:" + searchInfo.getInteger2() +
                                ",3:" + searchInfo.getInteger3() + ",4:" + searchInfo.getInteger4());
                    }
//                    if (searchInfo == null || searchInfo.getInteger2() != FileUtils.SearchState.SEARCH_STATE_OVER ||
//                            mMediaFileDriverable.getMFourStringArray(storageSort[i]).getVal() == null ||
//                            mMediaFileDriverable.getMFourStringArray(storageSort[i]).getVal().length <= 0) {
                    startSearchFile(storageSort[i], false);
//                    } else {
//                        LogUtils.d(TAG, "设备:" + storageSort[i] + "，已经扫描完成！");
//                    }
                } else {
                    LogUtils.d(TAG, "设备:" + storageSort[i] + "，不存在！");
                }
            } else {
                if (StorageDevice.isDiskMounted(ModuleManager.getContext(), playStorage)) {
                    LogUtils.d(TAG, "记忆设备：" + playStorage + "，存在");
                    /*-begin-20180426-hzubin-add-bug11286播放usb1歌曲，ACC浅休眠，拔掉usb1，再上ACC音乐界面，仍有usb的目录-*/
                    IntegerSSBoolean mStorageBoolean = mMediaFileDriverable.getStorageInfo(storageSort[i]);
                    if (mStorageBoolean != null && !mStorageBoolean.getBoolean()) {
                        LogUtils.d(TAG, "device:" + storageSort[i] + " not exit in list!");
                        StorageDriverable storageDriverable = StorageDriver.Driver();
                        if (storageDriverable != null) {
                            storageDriverable.updateMountedStatus(storageSort[i], true);
                        }
                    } else {
                        LogUtils.d(TAG, "device:" + storageSort[i] + " exit in list!");
                    }
                    /*-end-20180426-hzubin-add-bug11286播放usb1歌曲，ACC浅休眠，拔掉usb1，再上ACC音乐界面，仍有usb的目录-*/

                    if (playStorageArray == null || playStorageArray.length <= 0) {
                        LogUtils.d(TAG, "onResume else playStorageArray is null ");
                        /*-begin-20180505-hzubin-add-for-bug11343本地无视频，断电拔掉USB，再上电进主页后插入USB，全局指令打开视频，在视频界面，无加载中提示且不播放USB视频-*/
                        startSearchFile(playStorage, true);//启动搜索并且播放
                        isScanMemory = MEMORY_SCANNING;
                        /*-end-20180505-hzubin-add-for-bug11343本地无视频，断电拔掉USB，再上电进主页后插入USB，全局指令打开视频，在视频界面，无加载中提示且不播放USB视频-*/
                    } else {
                        String playFile;
                        if (!TextUtils.isEmpty(mMediaFileDriverable.getFilePath()) &&
                                FileUtils.isFileExist(mMediaFileDriverable.getFilePath())) {//记忆路径需要不为空且文件存在才播放记忆
                            LogUtils.d(TAG, "onResume else FilePath exist");
                            playFile = mMediaFileDriverable.getFilePath();
                        } else {
                            LogUtils.d(TAG, "onResume else FilePath no exist play frist");
                            playFile = playStorageArray[0].getString1();
                        }
                        playFileInfo(playStorage, playFile);
                        FourInteger searchInfo = getSearchInfoById(playStorage);
                        if (searchInfo != null) {
                            LogUtils.d("onResume else searchInfo 1:" + searchInfo.getInteger1() + ",2:" + searchInfo.getInteger2() +
                                    ",3:" + searchInfo.getInteger3() + ",4:" + searchInfo.getInteger4());
                            if (searchInfo != null && searchInfo.getInteger2() != FileUtils.SearchState.SEARCH_STATE_OVER) {
                                isScanMemory = MEMORY_SCANNING;
                                startSearchFile(playStorage, true);
                            } else {
                                LogUtils.d(TAG, "记忆设备:" + playStorage + "，已经扫描完成！");
                            }
                        } else {
                            LogUtils.d(TAG, "记忆设备:" + playStorage + "，searchInfo is null");
                        }
                    }
                } else {
                    LogUtils.d(TAG, "记忆设备：" + playStorage + " 不存在，请求扫描, mCloseUsb=" + AndroidMediaDriver.mCloseUsb);
                    scanMemoryHandler.removeMessages(0);
                    if (AndroidMediaDriver.mCloseUsb == 0) {
                        count = 35;
                        scanMemoryHandler.sendEmptyMessage(0);
                    }
                }
            }
        }
    }

    public class ScanMemoryHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            LogUtils.d(TAG, "ScanMemoryHandler count:" + count);
            scanMemoryHandler.sendEmptyMessageDelayed(0, 1000);
            count--;
            LogUtils.d("ScanMemoryHandler playStorage:" + tmpPlayStorage);
            if (StorageDevice.isDiskMounted(ModuleManager.getContext(), tmpPlayStorage)) {
                LogUtils.e(TAG, "ScanMemoryHandler----扫描记忆中的设备");
                startSearchFile(tmpPlayStorage, true);
                scanMemoryHandler.removeMessages(0);
            }
            if (count == 15 || count == 26 || count == 31) {
                LogUtils.d(TAG, "ScanMemryHandloer----提示加载中");
                /*-begin-20180412-hzubin-update-for-H173项目问题跟踪表25号问题-*/
                //if (StorageDevice.isDiskMounted(ModuleManager.getContext(), tmpPlayStorage)) {
                mMediaFileDriverable.storageScanStatus(1);
                //} else {
                //    mMediaFileDriverable.storageScanStatus(2);
                //}
                /*-end-20180412-hzubin-add-for-H173项目问题跟踪表25号问题-*/
            } else if (count == 0) {
                LogUtils.d(TAG, "ScanMemoryHandler----时间已经过了，切换设备");
                AndroidMediaDriver.bFirstboot = false;

                scanMemoryHandler.removeMessages(0);
                FourString[] playStorageArray;

                for (int i = storageSort.length - 1; i >= 0; i--) {
                    /*-begin-20180411-hzubin-add-for-bug10955-*/
                    //if (tmpPlayStorage != storageSort[i]) {//修改记忆设备是USB3时，等待时间已经过了，其他设备没有文件不退出问题
                    /*-end-20180411-hzubin-add-for-bug10955-*/
                    LogUtils.d(TAG, "scanMemoryHandler Storage:" + StorageDevice.toString(storageSort[i]));
                    if (StorageDevice.isDiskMounted(ModuleManager.getContext(), storageSort[i])) {
                        LogUtils.d(TAG, "scanMemoryHandler:" + StorageDevice.toString(storageSort[i]) + " has Mounted!");
                        playStorageArray = mMediaFileDriverable.getFourStringArr(storageSort[i]);//getMFourStringArray(storageSort[i]).getVal();

                        if (playStorageArray != null && playStorageArray.length > 0) {
                            LogUtils.d(TAG, "scanMemoryHandler length:" + playStorageArray.length);
                            //mMediaFileDriverable.playFileInfo(storageSort[i], playStorageArray[0].getString1(), 0);
                            //mMediaFileDriverable.updateCurrentStorage(storageSort[i]);
                            playFileInfo(storageSort[i], playStorageArray[0].getString1());
                            break;
                        } else {
                            LogUtils.d(TAG, "scanMemoryHandler Storage:" + StorageDevice.toString(storageSort[i]) + "not play file");
                            if (StorageDevice.USB3 == storageSort[i]) {
                                /*-begin-20180425-ydigngen-modify-当记忆设备无文件，而其他设备在扫描时不退出--*/
                                if (filesSearchRunnables.size() == 0) {
                                    LogUtils.d(TAG, "scanMemoryHandler Storage:" + StorageDevice.toString(storageSort[i]) + "quit");
                                    mMediaFileDriverable.storageScanStatus(3);
                                    if (mExitHandler != null) {
                                        mExitHandler.sendEmptyMessageDelayed(0, 2000);
                                    }
                                }
                                /*-end-20180425-ydigngen-modify-当记忆设备无文件，而其他设备在扫描时不退出--*/
                            }
                        }
                    } else {
                        LogUtils.d(TAG, "scanMemoryHandler Storage:" + StorageDevice.toString(storageSort[i]) + "not exist");
                        if (StorageDevice.USB3 == storageSort[i]) {
                            /*-begin-20180425-ydigngen-modify-当记忆设备无文件，而其他设备在扫描时不退出--*/
                            if (filesSearchRunnables.size() == 0) {
                                LogUtils.d(TAG, "scanMemoryHandler Storage:" + StorageDevice.toString(storageSort[i]) + "quit");
                                mMediaFileDriverable.storageScanStatus(3);
                                if (mExitHandler != null) {
                                    mExitHandler.sendEmptyMessageDelayed(0, 2000);
                                }
                            }
                            /*-end-20180425-ydigngen-modify-当记忆设备无文件，而其他设备在扫描时不退出--*/
                        }
                    }
                    //}
                }
            }
        }
    }

    @Override
    public void startSearchFile(int storageId, boolean playFirst) {
        LogUtils.d("startSearchFile storageId:" + storageId + ",playFirst:" + playFirst);

        if (playFirst) {
            mMediaFileDriverable.updateStorageErrorStatus(MediaDefine.StorageErrorState.NO_ERROR);
        }

        FilesSearchRunnable findFilesSearch = getSearchRunnable(storageId);
        if (findFilesSearch != null && storageId != StorageDevice.UNKNOWN) {
            LogUtils.d("storageId:" + storageId + "  in scanning");
            return;
        }

        String storagePath = StorageDevice.getPath(mContext, storageId);
        FilesSearchRunnable mFilesSearchRunnable = new FilesSearchRunnable(this, storageId, storagePath, playFirst);
        mFilesSearchRunnable.setListInfoListener(this);
        filesSearchRunnables.add(mFilesSearchRunnable);
        mFilesSearchRunnable.start();

        LogUtils.d(TAG, "startSearchFile filesSearchTasks !size:" + filesSearchRunnables.size() + " path:" + StorageDevice.toString(storageId));
    }


    @Override
    public void startSearchFile(int storageId, boolean playFirst, int source) {
        if (playFirst) {
            mMediaFileDriverable.updateStorageErrorStatus(MediaDefine.StorageErrorState.NO_ERROR);
        }
        FilesSearchRunnable findFilesSearch = getSearchRunnable(storageId);
        if (findFilesSearch != null && storageId != StorageDevice.UNKNOWN) {
            LogUtils.d(TAG, "currentStorage:" + StorageDevice.toString(storageId) + " searching !");
            if (source == Define.Source.SOURCE_AUDIO) {
                findFilesSearch.setListInfoListener(mListInfoListener);
            }
            return;
        }

        String storagePath = StorageDevice.getPath(mContext, storageId);
        FilesSearchRunnable mFilesSearchRunnable = new FilesSearchRunnable(this, storageId, storagePath, playFirst);
        if (source != Define.Source.SOURCE_AUDIO) {
            mFilesSearchRunnable.setListInfoListener(this);
        } else {
            mFilesSearchRunnable.setListInfoListener(mListInfoListener);
        }
        filesSearchRunnables.add(mFilesSearchRunnable);
        //今天遇到奇怪问题不搜索
//        executorService.submit(mFilesSearchRunnable);
        mFilesSearchRunnable.start();
        //会移除之前的监听
        mFileObserverManager.addMultiFileObserver(mContext, storageId);
        LogUtils.d(TAG, "startSearchFile filesSearchTasks !size:" + filesSearchRunnables.size() + " path:" + StorageDevice.toString(storageId));
    }

    @Override
    public void removeStorage(int storageId) {
        FilesSearchRunnable removeFilesSearchRunnable = getSearchRunnable(storageId);
        if (removeFilesSearchRunnable != null) {
            removeFilesSearchRunnable.needCancel();
            filesSearchRunnables.remove(removeFilesSearchRunnable);
        }
        mFileObserverManager.removeMultiFileObserver(storageId);
        LogUtils.d("removeStorage---storage="+storageId+", fourStrings=NULL NULL");
        mMediaFileDriverable.updateListInfo(storageId, null);
    }

    @Override
    public void onStop() {
        mFileObserverManager.removeRunMultiFileObserver();
        exitSearchRunnable();
        mExitHandler.removeAllMessage();
    }

    @Override
    public void onCreate() {
        mFileObserverManager.onCreate();
        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        SourceManager.getModel().bindListener(mSourceListener);
    }

    @Override
    public void onDestroy() {
        LogUtils.d("onDestroy");
        exitSearchRunnable();
        SourceManager.getModel().unbindListener(mSourceListener);
        mFileObserverManager.onDestroy();
    }
    //------------------------mediaPresenter提供其它类调用的API---------------------

    //------------------------文件监听回调---------------------
    @Override
    public void onFileCreated(int storageId, String name) {
        LogUtils.d(TAG, "storage onFileCreated name:" + name);
        //获取文件名
        if (!isFilter(name)) {
            LogUtils.d(TAG, "storage onFileCreated not filter:");
            return;
        }
        synchronized (this) {
            String fileName = name.substring(name.lastIndexOf("/") + 1);
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            FourString createFourString = new FourString(name, fileName, null, null);
            FourString[] oldFourStringArray = mMediaFileDriverable.getFourStringArr(storageId);//getMFourStringArray(storageId).getVal();
            if (oldFourStringArray != null) {
                FourString[] newFourStringArray = new FourString[oldFourStringArray.length + 1];
                for (int i = 0; i < oldFourStringArray.length; i++) {
                    newFourStringArray[i] = oldFourStringArray[i];
                }
                newFourStringArray[newFourStringArray.length - 1] = createFourString;
//                LogUtils.d(TAG, " title:" + createFourString.getString2() + " path:" + createFourString.getString1() + " artist:" + createFourString.getString3() +" album:" + createFourString.getString4());
                Arrays.sort(newFourStringArray, new PinyinComparator());
                Arrays.sort(newFourStringArray, new FilePathComparator());

                for (int i = 0; i < newFourStringArray.length; i++) {
                    FourString logFourString = newFourStringArray[i];
                    LogUtils.d(TAG, " title:" + logFourString.getString2() + " path:" + logFourString.getString1() + " artist:" + logFourString.getString3() + " album:" + logFourString.getString4());
                }

                LogUtils.d(TAG, " sort onFileCreated length:" + newFourStringArray.length);
                mMediaFileDriverable.updateListInfo(storageId, newFourStringArray);
            } else {
                //如果没有创建
                mMediaFileDriverable.updateListInfo(storageId, new FourString[]{createFourString});
            }
            //保证Total正常
            int playStorage = mMediaFileDriverable.getPlayStorage();
            if (playStorage == storageId) {
                mMediaFileDriverable.updatePlayMode();
            }
        }
    }

    @Override
    public void onFileDeleted(int storageId, String name) {
        if (!isFilter(name)) {
            LogUtils.d(TAG, "storage onFileDeleted not filter:");
            return;
        }
        int playStorage = mMediaFileDriverable.getPlayStorage();
        if (playStorage == storageId) {
            //先播放下一曲
            mMediaFileDriverable.onFileDeleted(name);
        }
        //******************优化后修改代码2018-12-27 begin*******************//
//        MFourStringArray dMFourStringArray = mMediaFileDriverable.getMFourStringArray(storageId);
        FourString[] dMFourStringArray = mMediaFileDriverable.getFourStringArr(storageId);
        if (dMFourStringArray != null) {
            FourString[] storageFourString = dMFourStringArray;//.getVal();
            if (storageFourString != null) {
                for (int i = 0; i < storageFourString.length; i++) {
                    String deleteName = storageFourString[i].getString1();
                    if (deleteName != null) {
                        if (deleteName.equals(name)) {
                            LogUtils.d(TAG, "Being deleted name:" + name + " !");
//                            dMFourStringArray.delVal(i);//YDG
                            dMFourStringArray = (FourString[]) deleteOneInArray(i, dMFourStringArray);
                            mMediaFileDriverable.setFourStringArr(storageId, dMFourStringArray);
                            //******************优化后修改代码2018-12-27 end*******************//
                            break;
                        }
                    }
                }
            }
        }
        if (playStorage == storageId) {
            mMediaFileDriverable.updatePlayMode();
        }
    }

    public Object[] deleteOneInArray(int index, Object array[]) {
        //数组的删除其实就是覆盖前一位
        Object[] arrNew = new Object[array.length - 1];
        for (int i = index; i < array.length - 1; i++) {
            array[i] = array[i + 1];
        }
        System.arraycopy(array, 0, arrNew, 0, arrNew.length);
        return arrNew;
    }

    @Override
    public void onFileModified(int storageId, String name) {

    }

    @Override
    public void onFileRenamed(int storageId, String oldName, String newName) {
        if (!isFilter(oldName)) {
            LogUtils.d(TAG, "storage onFileRenamed not filter:");
            return;
        }
        LogUtils.d(TAG, "storage:" + StorageDevice.getPath(storageId) + "onFileRenamed newName:" + newName + " oldName:" + oldName);
        synchronized (this) {
            FourString[] storageFourString = mMediaFileDriverable.getFourStringArr(storageId);//getMFourStringArray(storageId).getVal();
            if (storageFourString != null) {
                //克隆一个新对象!
                storageFourString = storageFourString.clone();
                for (int i = 0; i < storageFourString.length; i++) {
                    String RenameName = storageFourString[i].getString1();
                    if (!TextUtils.isEmpty(RenameName)) {
                        if (RenameName.equals(oldName)) {
                            LogUtils.d(TAG, "storage onFileRenamed RenameName:" + RenameName + ",oldName:" + oldName + ",newName:" + newName);
                            FourString renameFourString = storageFourString[i];
                            String title = newName.substring(newName.lastIndexOf("/") + 1);
                            title = title.substring(0, title.lastIndexOf("."));
                            storageFourString[i] = new FourString(newName, title, renameFourString.getString3(), renameFourString.getString4());
                            //LogUtils.d(TAG, "storage onFileRenamed newName:" + newName + ",title:" + title + ",string3:" + renameFourString.getString3() + ",string4:" + renameFourString.getString4());
                            break;
                        }
                    }
                }
//                Arrays.sort(storageFourString, new PinyinComparator());
//                Arrays.sort(storageFourString, new FilePathComparator());
                mMediaFileDriverable.updateListInfo(storageId, storageFourString.clone());
            }
            int playStorage = mMediaFileDriverable.getPlayStorage();
            if (playStorage == storageId) {
                String getFilePath = mMediaFileDriverable.getFilePath();
                LogUtils.d(TAG, "storage getFilePath:" + getFilePath);
                if (oldName.equals(getFilePath)) {
                    LogUtils.d(TAG, "storage newName:" + newName);
                    //mMediaFileDriverable.playFileInfo(storageId, newName, mMediaFileDriverable.getCurrentTime());
                    playFileInfo(storageId, newName);
                }
            }
        }
//        mMediaFileDriverable.updateListInfo(storageId,null);
    }
    //------------------------文件监听回调---------------------

    //------------------------文件搜索回调---------------------
    @Override
    public void playFileInfo(int searchStorage, String filePath) {
        LogUtils.d(TAG, "playFileInfo searchStorage:" + searchStorage + ",filePath:" + filePath);
//        int playStorage = mMediaFileDriverable.getPlayStorage();
//        String playStoragePath = StorageDevice.getPath(mContext, playStorage);

        String path = mMediaFileDriverable.getFilePath();
        LogUtils.d(TAG, "playFileInfo path:" + path);
        Message msg = Message.obtain();
        msg.what = PLAY_FILE_INFO;
        msg.obj = filePath;
        msg.arg1 = searchStorage;
        if (TextUtils.isEmpty(path) || !filePath.equals(path)) {//如果搜索完成，当前有正在播放，则不重新播放搜索完成的音乐
            //mMediaFileDriverable.playFileInfo(searchStorage, filePath, 0);//子线程创建的播放线程和主线程有冲突，所以用主线程去创建
            msg.arg2 = 0;
        } else {
            msg.arg2 = mMediaFileDriverable.getCurrentTime();
        }
        mhandler.sendMessage(msg);
    }

    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_FILE_INFO:
                    mMediaFileDriverable.playFileInfo(msg.arg1, (String) msg.obj, msg.arg2);
                    break;
            }
        }
    };

    @Override
    public void updateFileInfo(int searchStorage, FourString[] mediaFourStringArr, String[] mediaFile) {
        mExitHandler.removeAllMessage();
        onPostExecute(searchStorage);//搜索完成后删除搜索线程

        if (mediaFourStringArr != null && mediaFourStringArr.length > 0) {
            LogUtils.d(TAG, "search  has start:" + searchStorage + ",mediaFourStringArr size :" + mediaFourStringArr.length + ",mediaFile size:" + mediaFile.length);
            //文件很多时，会比较耗时，会导致插入另外一个USB时，列表还会显示上一个USB的内容。采用线程处理。2019-01-18
//            Arrays.sort(mediaFourStringArr, new PinyinComparator());
//            Arrays.sort(mediaFourStringArr, new FilePathComparator());
            //非文件管理器进入
            mMediaFileDriverable.updateListInfo(searchStorage, mediaFourStringArr);
            /*-begin-20180417-ydinggen-modify-如果不在播放不更新索引，避免索引改变发数据给MCU，导致USB记忆问题-*/
            if (mMediaFileDriverable.getPlayStatus() == MediaDefine.PlayStatus.PLAY_STATUS_PLAY) {
                mMediaFileDriverable.updatePlayMode();//更新Index/Total
            } else {
                LogUtils.d(TAG, "updateFileInfo playstatus:" + mMediaFileDriverable.getPlayStatus());
            }
            /*-end-20180417-ydinggen-modify-如果不在播放不更新索引，避免索引改变发数据给MCU，导致USB记忆问题-*/

            if (mediaFourStringArr != null && mediaFourStringArr.length > 0
                /*&& mMediaFileDriverable.getPlayStorage() == searchStorage/*播放最后挂载的设备*/) {
                if (AndroidMediaDriver.isEnterFromFile) {//从文件管理器进入搜索完成后不重新播放
                    AndroidMediaDriver.isEnterFromFile = false;
                } else {
                    LogUtils.d("search  has end!  -----> Start from the first index searchStorage=" + searchStorage);
                    LogUtils.d("updateFileInfo playFile:" + mMediaFileDriverable.getFilePath() + ", playStorage=" + mMediaFileDriverable.getPlayStorage());
                    LogUtils.d("AndroidMediaDriver.bFirstboot:" + AndroidMediaDriver.bFirstboot);
                    LogUtils.d("AndroidMediaDriver.bootStorage:" + AndroidMediaDriver.bootStorage);
                    LogUtils.d("AndroidMediaDriver.bootIndex:" + AndroidMediaDriver.bootIndex);

                    /*-begin-20180420-ydinggen-modify-修改在记忆设备扫描完开始播放而播放状态还未改变前又扫描完了其他盘，会导致切到其他盘播放-*/
                    /*-begin-20180416-ydinggen-modify-第一次开机取MCU记忆的记录，解决USB针对ARM断电记忆问题-*/
                    if (searchStorage == mMediaFileDriverable.getPlayStorage()) {
                        if (AndroidMediaDriver.bFirstboot) {//第一次开机启动播放muc记忆中的记录
                            if (AndroidMediaDriver.bootIndex > 0 && AndroidMediaDriver.bootIndex <= mediaFourStringArr.length) {
                                LogUtils.d("updateFileInfo boot playFile:" + mediaFourStringArr[AndroidMediaDriver.bootIndex - 1].getString1());
                                playFileInfo(searchStorage, mediaFourStringArr[AndroidMediaDriver.bootIndex - 1].getString1());
                            } else {
                                //播记忆设备的第一首
                                playFileInfo(searchStorage, mediaFourStringArr[0].getString1());
                            }
                        } else {
                            /*-begin-20180507-hzubin-modify-bug11345播放U盘视频时，断ACC深度睡眠，拔掉U盘再上ACC，视频界面接另一个U盘，一直显示“加载中..”不播放-*/
                            if (!TextUtils.isEmpty(mMediaFileDriverable.getFilePath()) && FileUtils.isFileExist(mMediaFileDriverable.getFilePath())) {
                                //记忆路径不为空且文件存在才可播放，防止文件被删除、剪切、换u盘后问题
                                playFileInfo(mMediaFileDriverable.getPlayStorage(), mMediaFileDriverable.getFilePath());
                            } else {
                                playFileInfo(searchStorage, mediaFourStringArr[0].getString1());
                            }
                            /*-end-20180507-hzubin-modify-bug11345播放U盘视频时，断ACC深度睡眠，拔掉U盘再上ACC，视频界面接另一个U盘，一直显示“加载中..”不播放-*/
                        }
                    } else {
                        /*-begin-20180425-ydinggen-add-当本地扫描完且无文件时，需要切换设备-*/
                        if (isScanMemory == MEMORY_SCAN_COMPLETION) {
                            FourString[] playStorageArray;
                            //******************优化后修改代码2018-12-27 begin*******************//
//                            MFourStringArray mMFourStringArray = mMediaFileDriverable.getMFourStringArray(mMediaFileDriverable.getPlayStorage());
                            FourString[] mMFourStringArray = mMediaFileDriverable.getFourStringArr(mMediaFileDriverable.getPlayStorage());
                            if (mMFourStringArray != null) {
                                playStorageArray = mMFourStringArray;//.getVal();
                                //******************优化后修改代码2018-12-27 end*******************//
                                if (playStorageArray != null && playStorageArray.length > 0) {
                                    LogUtils.d("mMediaFileDriverable.getPlayStorage() not play file！！");
                                    return;
                                }
                            }
                            String path = mMediaFileDriverable.getFilePath();
                            LogUtils.d("play searchStorage  file!! path=" + path);
                            if (path != null && (path.toUpperCase().contains("/DVR") || path.contains("/recordVideo"))) {
                                LogUtils.e(TAG, " updateFileInfo 1 when play DVR Video!");
                            } else {
                                playFileInfo(searchStorage, mediaFourStringArr[0].getString1());
                            }
                        }
                        /*-end-20180425-ydinggen-add-当本地扫描完且无文件时，需要切换设备-*/
                    }
                    /*-end-20180420-ydinggen-modify-修改在记忆设备扫描完开始播放而播放状态还未改变前又扫描完了其他盘，会导致切到其他盘播放-*/
                }
            }
        } else {
            LogUtils.e("searchStorage:" + searchStorage + ",没有播放文件！");
            if (AndroidMediaDriver.isEnterFromFile) {//从文件管理器进入搜索完成后不重新播放
                AndroidMediaDriver.isEnterFromFile = false;
            } else {
                mMediaFileDriverable.updateListInfo(searchStorage, mediaFourStringArr);
            /*-begin-20180411-hzubin-add-for-bug10955本地无视频时，播放USB视频，断ACC深度睡眠，拔掉U盘再上ACC，不会退出视频-*/
                if (searchStorage == mMediaFileDriverable.getPlayStorage()) {
                    String path = mMediaFileDriverable.getFilePath();
                    LogUtils.d(TAG, "updateFileInfo playStorageArray path=" + path);
                    if (path != null && (path.toUpperCase().contains("/DVR") || path.contains("/recordVideo"))) {
                        LogUtils.e(TAG, " updateFileInfo 1 when play DVR Video!");
                        return;

                    }
                    for (int i = storageSort.length - 1; i >= 0; i--) {
                        LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(storageSort[i]));
                        FourString[] playStorageArray;
                        if (StorageDevice.isDiskMounted(ModuleManager.getContext(), storageSort[i])) {
                            LogUtils.d(TAG, "updateFileInfo:" + StorageDevice.toString(storageSort[i]) + " has Mounted!");
                            playStorageArray = mMediaFileDriverable.getFourStringArr(storageSort[i]);//getMFourStringArray(storageSort[i]).getVal();

                            if (playStorageArray != null && playStorageArray.length > 0) {
                                LogUtils.d(TAG, "updateFileInfo length:" + playStorageArray.length);
                                playFileInfo(storageSort[i], playStorageArray[0].getString1());
                                break;
                            } else {
                                LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(storageSort[i]) + "not play file, thread size=" + filesSearchRunnables.size());
                                if (StorageDevice.USB3 == storageSort[i]) {
                                    if (filesSearchRunnables.size() == 0) {
                                        LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(storageSort[i]) + "quit");
                                        mMediaFileDriverable.storageScanStatus(3);
                                        if (mExitHandler != null) {
                                            mExitHandler.sendEmptyMessageDelayed(1, 15000);
                                        }
                                    } else {
                                        LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(storageSort[i]) + " another is sacaning 1");
                                    }
                                }
                            }
                        } else {
                            LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(storageSort[i]) + "not exist, thread size=" + filesSearchRunnables.size());
                            if (StorageDevice.USB3 == storageSort[i]) {
                                if (filesSearchRunnables.size() == 0) {
                                    LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(storageSort[i]) + "quit");
                                    mMediaFileDriverable.storageScanStatus(3);
                                    if (mExitHandler != null) {
                                        mExitHandler.sendEmptyMessageDelayed(1, 15000);
                                    }
                                } else {
                                    LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(storageSort[i]) + " another is sacaning 2");
                                }
                            }
                        }
                    }
                } else {
                /*-begin-20180425-ydinggen-add-本地不存在文件，只有一个行车记录仪，会执行此处，15s后退出-*/
                    if (filesSearchRunnables.size() == 0) {
                        LogUtils.d(TAG, "updateFileInfo Storage:" + StorageDevice.toString(searchStorage) + "quit");
                        if (mExitHandler != null) {
                            mExitHandler.sendEmptyMessageDelayed(1, 15000);
                        }
                    }
                /*-end-20180425-ydinggen-add-本地不存在文件，只有一个行车记录仪，会执行此处，15s后退出-*/
                }
            /*-end-20180411-hzubin-add-for-bug10955本地无视频时，播放USB视频，断ACC深度睡眠，拔掉U盘再上ACC，不会退出视频-*/
            }
        }
        /*-begin-20180427-hzubin-add-for-bug11287播放usb歌曲，ACC深休眠，再上ACC自动播放本地歌曲(多设备和记忆设备差不多在同一时间完成扫描不播放记忆)-*/
        if (searchStorage == mMediaFileDriverable.getPlayStorage()) {
            isScanMemory = MEMORY_SCAN_COMPLETION;
        }
        /*-end-20180427-hzubin-add-for-bug11287播放usb歌曲，ACC深休眠，再上ACC自动播放本地歌曲(多设备和记忆设备差不多在同一时间完成扫描不播放记忆)-*/
        if (searchStorage == AndroidMediaDriver.bootStorage) {
            AndroidMediaDriver.bFirstboot = false;
        }
    }

    @Override
    public void updateSearchInfo(int searchStorage, int searchState) {
        mMediaFileDriverable.getSearchInfo().getVal();
        FourInteger searchInfo = getSearchInfoById(searchStorage);
        if (searchInfo == null) {
            FourInteger createSearchStorage = new FourInteger(searchStorage, searchState, FileUtils.SearchState.SEARCH_STATE_DEFAULT, FileUtils.SearchState.SEARCH_STATE_DEFAULT);
            LogUtils.d(TAG, "Create a new store SearchInfo!");
            addAndUpdateSearchInfo(createSearchStorage);
        } else {
            int isOver = searchInfo.getInteger3();
            if (searchState == FileUtils.SearchState.SEARCH_STATE_OVER) {
                isOver = FileUtils.SearchState.SEARCH_STATE_OVER;
            }
            FourInteger newSearchInfo = new FourInteger(searchInfo.getInteger1(), searchState, isOver, searchInfo.getInteger4());
            LogUtils.d(TAG, "Update a store SearchInfo!");
            addAndUpdateSearchInfo(newSearchInfo);
        }
    }

    @Override
    public void updateNoPlayFile(int searchStorage) {
        if (mMediaFileDriverable != null) {
            mMediaFileDriverable.updateNoPlayFile(searchStorage);
        }
        int netStorage = getNextStorage(searchStorage);
        LogUtils.d(TAG, "updateNoPlayFile netStorage:" + netStorage);
        if (searchStorage != netStorage) {
            playPriorityStorage(netStorage);//要搜索播放的设备没有可播放文件搜索播放下一个设备
        }
    }

    //获取
    private int getNextStorage(int storage) {
        int ret = storage;
        for (int i = 0; i < storageSort.length; i++) {
            if (storage != StorageDevice.UNKNOWN) {
                if (storage == storageSort[i]) {
                    int index = i + 1;
                    if (index < storageSort.length) {
                        ret = storageSort[index];
                    }
                    break;
                }
            }
        }
        LogUtils.d(TAG, " getNextStorage:" + ret);
        return ret;
    }

    /**
     * 搜索结束
     *
     * @param searchStorage
     */
    @Override
    public void onPostExecute(int searchStorage) {
        FilesSearchRunnable completeFilesSearch = getSearchRunnable(searchStorage);
        filesSearchRunnables.remove(completeFilesSearch);
        LogUtils.d(TAG, "onPostExecute filesSearchTasks !size:" + filesSearchRunnables.size());
        checkNoFilesPlay();
    }

    private void checkNoFilesPlay() {
        MFourIntegerArray mFourIntegerArray = null;
        LogUtils.d(TAG, " checkNoFilesPlay!");
        if (filesSearchRunnables.size() == 0) {
            boolean ret = true;
            if (mMediaFileDriverable != null) {
                mFourIntegerArray = mMediaFileDriverable.getSearchInfo();
            }
            for (int i = 0; i < storageSort.length; i++) {
                if (StorageDevice.isDiskMounted(ModuleManager.getContext(), storageSort[i])) {
                    if (mFourIntegerArray != null) {
                        FourInteger mFourInteger = mFourIntegerArray.getValByInteger1(storageSort[i]);
                        if (mFourInteger != null) {
                            if (mFourInteger.getInteger2() == FileUtils.SearchState.SEARCH_STATE_NO_FILE) {
                                ret = (ret && true);//找到设备中没有数据
                            } else {
                                ret = false;
                                break;
                            }
                        } else {
                            ret = false;
//                            startSearchFile(storageSort[i],false);
                            break;
                        }
                    }
                }
            }
            if (ret) {
                LogUtils.d(TAG, "exit ! 2s!");
                if (mMediaFileDriverable != null) {
                    mMediaFileDriverable.updateStorageErrorStatus(MediaDefine.StorageErrorState.ERROR_NO_FILE_PLAY);
                }
//                if(mExitHandler != null){
//                    mExitHandler.sendEmptyMessageDelayed(0,2000);
//                }
            } else {
                LogUtils.d(TAG, "checkNoFilesPlay  false!");
            }
        } else {
            LogUtils.d(TAG, "checkNoFilesPlay  filesSearchRunnables size:" + filesSearchRunnables.size());
        }
    }
    //------------------------文件搜索回调---------------------

    /**
     * 退出文件搜索
     */
    private synchronized void exitSearchRunnable() {
        if (filesSearchRunnables != null && filesSearchRunnables.size() > 0) {
            for (int i = 0; i < filesSearchRunnables.size(); i++) {
                try {
                    filesSearchRunnables.get(i).needCancel();
                    filesSearchRunnables.remove(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public FilesSearchRunnable getSearchRunnable(int currentStorage) {
        if (filesSearchRunnables != null) {
            //for (FilesSearchRunnable mFilesSearchRunnable : filesSearchRunnables) {
            //if (mFilesSearchRunnable.getCurrentStorage() == currentStorage) {
            //return mFilesSearchRunnable;
            //}
            //}
            //修改java.util.ConcurrentModificationException异常
            Iterator<FilesSearchRunnable> iterator = filesSearchRunnables.iterator();
            while (iterator.hasNext()) {
                FilesSearchRunnable mFilesSearchRunnable = iterator.next();
                if (mFilesSearchRunnable.getCurrentStorage() == currentStorage) {
                    return mFilesSearchRunnable;
                }
            }
        }
        return null;
    }

    /**
     * 获取正在搜索的属性
     *
     * @param searchStorage storage 编号 see{@link com.wwc2.common_interface.utils.StorageDevice}
     * @return
     */

    private FourInteger getSearchInfoById(int searchStorage) {
        FourInteger[] searchInfoArray = mMediaFileDriverable.getSearchInfo().getVal();
        if (searchInfoArray != null) {
            for (int i = 0; i < searchInfoArray.length; i++) {
                if (searchInfoArray[i] != null && searchInfoArray[i].getInteger1() == searchStorage) {
                    return searchInfoArray[i];
                }
            }
        }
        return null;
    }

    /**
     * 更新正在搜索的属性
     *
     * @param searchInfo FourInteger 正在搜索的属性
     * @return 添加状态
     */
    public Boolean addAndUpdateSearchInfo(FourInteger searchInfo) {
        Boolean isAdd = false;
        FourInteger[] searchInfoArray = mMediaFileDriverable.getSearchInfo().getVal();
        if (searchInfoArray != null && searchInfo != null) {
            searchInfoArray = searchInfoArray.clone();
            for (int i = 0; i < searchInfoArray.length; i++) {
                if (searchInfoArray[i] != null && searchInfoArray[i].getInteger1() == searchInfo.getInteger1()) {
                    searchInfoArray[i] = searchInfo;
//                    mMediaFileDriverable.updateSearchInfo(null);
                    LogUtils.d(TAG, "Update a store SearchInfo !!!! state:" + FileUtils.SearchState.toString(searchInfo.getInteger2()));
                    mMediaFileDriverable.updateSearchInfo(searchInfoArray);
                    isAdd = true;
                    break;
                }
            }
            if (!isAdd) {
                LogUtils.d(TAG, "Add a new store SearchInfo!!!!");
                mMediaFileDriverable.addSearchInfo(searchInfo);
                isAdd = true;
            }
        } else if (searchInfoArray == null && searchInfo != null) {
            LogUtils.d(TAG, "Add a new store SearchInfo!!!! Not one");
            mMediaFileDriverable.addSearchInfo(searchInfo);
        }
        return isAdd;
    }

    /**
     * 优先播放 场景 acc on存储设备没有成功挂载
     */
    @Override
    public void playPriorityStorage(int storageId) {
        FourString[] playStorageArray;
        FourInteger playFourInteger = mMediaFileDriverable.getSearchInfo().getValByInteger1(storageId);
//        if(!StorageDevice.isDiskMounted(ModuleManager.getContext(),storageId) ||( playFourInteger != null && playFourInteger.getInteger2() == FileUtils.SearchState.SEARCH_STATE_NO_FILE)) {
        if (StorageDevice.isDiskMounted(ModuleManager.getContext(), storageId) && (playFourInteger == null)) {
            for (int i = 0; i < storageSort.length; i++) {
                if (storageId == storageSort[i]) {
                    if (StorageDevice.isDiskMounted(ModuleManager.getContext(), storageSort[i])) {
                        LogUtils.d(TAG, " playPriorityStorage Storage:" + StorageDevice.toString(storageSort[i]) + " has Mounted!");
                        playStorageArray = mMediaFileDriverable.getFourStringArr(storageSort[i]);//getMFourStringArray(storageSort[i]).getVal();
                        //判断优先播放的设备是否已经搜索过
                        if (playStorageArray == null) {
                            LogUtils.d(TAG, "playPriorityStorage playPriorityStorage by search:");
                            startSearchFile(storageSort[i], true);
                        } else {
                            LogUtils.d(TAG, "playPriorityStorage  playPriorityStorage by playStorageArray length:" + playStorageArray.length);
                            if (playStorageArray.length > 0) {
                                //mMediaFileDriverable.playFileInfo(storageSort[i], playStorageArray[0].getString1(), 0);
                                playFileInfo(storageSort[i], playStorageArray[0].getString1());
                            }
                        }
                        //mMediaFileDriverable.updateCurrentStorage(storageSort[i]);
                        break;
                    } else {
                        LogUtils.d(TAG, " playPriorityStorage Storage:" + StorageDevice.toString(storageSort[i]) + " not Mounted!");
                    }
                }
            }
        }
    }

    @Override
    public boolean checkJump() {
        boolean ret = false;
        BaseModel mCommonDriverModel = DriverManager.getDriverByName(CommonDriver.DRIVER_NAME).getModel();
        if (mCommonDriverModel != null) {
            ret = mCommonDriverModel.getInfo().getBoolean(SettingsDefine.Common.Switch.MEDIA_JUMP.value(), false);
        }
        switch (SourceManager.getCurSource()) {
            case Define.Source.SOURCE_POWEROFF:
            case Define.Source.SOURCE_CAMERA:
            case Define.Source.SOURCE_ACCOFF:
                ret = ret && false;
                break;
        }

        LogUtils.d(TAG, "checkJump:" + ret);
        return ret;
    }

    public Boolean isFilter(String name) {
        Boolean filter = false;
        for (int i = 0; i < getFilter().length; i++) {
            if (name != null) {
                if (name.endsWith(getFilter()[i]) || name.endsWith(getFilter()[i].toUpperCase())) {
                    filter = true;
                    break;
                }
            }
        }
        return filter;
    }

    /**
     * 拼音排序
     */
    public class PinyinComparator implements Comparator<FourString> {

        private Pinyin4jUtil mPinyin4jUtil;

        public PinyinComparator() {
            mPinyin4jUtil = new Pinyin4jUtil();
        }

        @Override
        public int compare(FourString fourString0, FourString fourString1) {
            //截取空格
            String firstName = fourString0.getString2().trim();
            String SecondName = fourString1.getString2().trim();
            //只获取前两位
            firstName = mPinyin4jUtil.getStringPinYin(firstName.length() > 2 ? firstName.substring(0, 2) : firstName);
            SecondName = mPinyin4jUtil.getStringPinYin(SecondName.length() > 2 ? SecondName.substring(0, 2) : SecondName);
            return firstName.compareTo(SecondName);
        }
    }

    /**
     * @author xiaanming
     *         文件夹排序
     */
    public class FilePathComparator implements Comparator<FourString> {
        @Override
        public int compare(FourString fourString0, FourString fourString1) {
            //截取空格
            String firstName = fourString0.getString1();
            String SecondName = fourString1.getString1();
            firstName = firstName.substring(0, (firstName.lastIndexOf("/") + 1));
            SecondName = SecondName.substring(0, (SecondName.lastIndexOf("/") + 1));
            //只获取前两位
            return firstName.compareTo(SecondName);
        }
    }

    public SuperMediaDriverable.ListInfoListener mListInfoListener = new SuperMediaDriverable.ListInfoListener() {

        @Override
        public void playFileInfo(int searchStorage, String filePath) {

            MediaPresenterImpl.this.playFileInfo(searchStorage, filePath);
        }

        @Override
        public void updateFileInfo(int searchStorage, FourString[] mediaFourStringArr, String[] mediaFile) {
            MediaPresenterImpl.this.updateFileInfo(searchStorage, mediaFourStringArr, mediaFile);
        }

        @Override
        public void updateSearchInfo(int searchStorage, int searchState) {
            MediaPresenterImpl.this.updateSearchInfo(searchStorage, searchState);
        }

        @Override
        public void updateNoPlayFile(int searchStorage) {
//            MediaPresenterImpl.this.updateNoPlayFile(searchStorage);
            if (mMediaFileDriverable != null) {
                mMediaFileDriverable.updateNoPlayFile(searchStorage);
            }
            if (checkJump()) {
//                BaseLogic audioLogic = ModuleManager.getLogicByName(AudioDefine.MODULE);
//                if(audioLogic != null){
//                    SourceManager.onExitPackage(audioLogic.getAPKPacketName());
//                }
                SourceManager.onChangeSource(Define.Source.SOURCE_VIDEO);
            }
        }

        @Override
        public void onPostExecute(int searchStorage) {
            if (!checkJump()) {
                MediaPresenterImpl.this.onPostExecute(searchStorage);
            }
        }
    };


    public class ExitHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                LogUtils.d(TAG, "exit postDelayed!");
                if (mMediaFileDriverable != null) {
                    //mMediaFileDriverable.clearStorage();
                    mMediaFileDriverable.switchCurrentSource();
                }
            } else if (msg.what == 1) {
                /*-begin-20180522-hzubin-modifly-for-bug11372和bug11440本地无视频，只接USB2，播放USB2视频，点击进度条会自动退出视频，回到主页-*/
                if (filesSearchRunnables.size() == 0 && mMediaFileDriverable.getPlayStatus() == MediaDefine.PlayStatus.PLAY_STATUS_STOP) {
                /*-end-20180522-hzubin-modifly-for-bug11372和bug11440本地无视频，只接USB2，播放USB2视频，点击进度条会自动退出视频，回到主页-*/
                    LogUtils.d(TAG, "ExitHandler quit");
                    if (mMediaFileDriverable != null) {
                        //mMediaFileDriverable.clearStorage();
                        mMediaFileDriverable.switchCurrentSource();
                    }
                }
            }
        }

        void removeAllMessage() {
            LogUtils.d("ExitHandler removeAllMessage");
            this.removeMessages(0);
            this.removeMessages(1);
        }
    }


    private SourceManager.SourceListener mSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "CurSourceListener " + " oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            /*-begin-20180510-hzubin-add-bug11372局指令打开蓝牙音乐,音乐播放16秒后跳到蓝牙界面，此时导航无音-*/
            if (oldVal == Define.Source.SOURCE_AUDIO || oldVal == Define.Source.SOURCE_VIDEO) {
                mExitHandler.removeAllMessage();//切源删除退出消息
            }
            /*-end-20180510-hzubin-add-bug11372局指令打开蓝牙音乐,音乐播放16秒后跳到蓝牙界面，此时导航无音-*/
        }
    };
}
