package com.wwc2.main.media.driver.observer;


import android.content.Context;

import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huwei on 2017/1/3.
 */
public class FileObserverManager implements MultiFileObserver.FileListener {

    private String TAG = FileObserverManager.class.getSimpleName();
    private MultiFileObserver.FileListener mFileListener;
    private Thread mObserver;

    public FileObserverManager(MultiFileObserver.FileListener mFileListener) {
        this.mFileListener = mFileListener;
    }

    Map<Integer, MultiFileObserver> multiFileObserverMap = new ConcurrentHashMap<>();

    /**
     * 添加指定目录文件监听
     *
     * @param storageId
     */
    public synchronized void addMultiFileObserver(Context context, int storageId) {
        LogUtils.d(TAG, "addMultiFileObserver storageId:" + storageId);
        //先移除已经添加的监听(因为暂时默认SD卡有挂载[没有sd时也默认有挂载])
//        removeMultiFileObserver(storageId);
//        Boolean isDiskMounted = StorageDevice.isDiskMounted(ModuleManager.getContext(), storageId);
//        if(isDiskMounted || true){
        if (!multiFileObserverMap.containsKey(storageId)/**/) {
            if (StorageDevice.getPath(context, storageId) != null) {
                MultiFileObserver addMultiFileObserver = new MultiFileObserver(storageId, StorageDevice.getPath(context, storageId), MultiFileObserver.CHANGES_ONLY);
                addMultiFileObserver.setFileListener(this);
                multiFileObserverMap.put(storageId, addMultiFileObserver);
                addMultiFileObserver.startWatching();
            }
        }
    }

    public synchronized void removeMultiFileObserver(int storageId) {
        LogUtils.d(TAG, "removeMultiFileObserver storageId:" + storageId);
        if (multiFileObserverMap.containsKey(storageId)) {
            MultiFileObserver removeMultiFileObserver = multiFileObserverMap.get(storageId);
//            if(!removeMultiFileObserver.hasInitComplete()){//文件监听没有初始完成
            removeMultiFileObserver.stop(true);
            removeMultiFileObserver.stopWatching();
            multiFileObserverMap.remove(storageId);
//            }
        }
    }

    /**
     * remove running file observer
     */
    public synchronized void removeRunMultiFileObserver() {
        LogUtils.d(TAG, "start removeRunMultiFileObserver size:" + multiFileObserverMap.size());
        for (Map.Entry<Integer, MultiFileObserver> entry : multiFileObserverMap.entrySet()) {
            MultiFileObserver stopMultiFileObserver = entry.getValue();
            if (!stopMultiFileObserver.hasInitComplete()) {
                stopMultiFileObserver.stop(true);
                stopMultiFileObserver.stopWatching();
//                multiFileObserverMap.remove(entry.getKey());
            }
        }
        LogUtils.d(TAG, "end removeRunMultiFileObserver size:" + multiFileObserverMap.size());
    }

    public synchronized void removeAllMultiFileObserver() {
        LogUtils.d(TAG, "end removeAllMultiFileObserver ");
        for (Map.Entry<Integer, MultiFileObserver> entry : multiFileObserverMap.entrySet()) {
            entry.getValue().stop(true);
            entry.getValue().stopWatching();
        }
//        multiFileObserverMap.clear();
    }

    @Override
    public void onFileCreated(int storageId, String name) {
        LogUtils.d(TAG, "storage:" + StorageDevice.getPath(storageId) + "onFileCreated name:" + name);
//        if(needUpdate(name)) {
        if (mFileListener != null) {
            mFileListener.onFileCreated(storageId, name);
        }
//        }
    }

    @Override
    public void onFileDeleted(int storageId, String name) {
        LogUtils.d(TAG, "storage:" + StorageDevice.getPath(storageId) + "onFileDeleted name:" + name);
//        if(needUpdate(name)) {
        if (mFileListener != null) {
            mFileListener.onFileDeleted(storageId, name);
        }
//        }
    }

    @Override
    public void onFileModified(int storageId, String name) {
        //LogUtils.d(TAG, "onFileModified");
    }

    @Override
    public void onFileRenamed(int storageId, String oldName, String newName) {
        LogUtils.d(TAG, "storage:" + StorageDevice.getPath(storageId) + "，onFileRenamed newName:" + newName + " oldName:" + oldName);
        //if(needUpdate(newName)){
        if (mFileListener != null) {
            mFileListener.onFileRenamed(storageId, oldName, newName);
        }
        //}
    }

    public void onCreate() {
        LogUtils.d(TAG, "onCreate!");
        mObserver = new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "multiFileObserverMap.size():" + multiFileObserverMap.size());
                for (Map.Entry<Integer, MultiFileObserver> entry : multiFileObserverMap.entrySet()) {
                    MultiFileObserver stopMultiFileObserver = entry.getValue();
                    if (!stopMultiFileObserver.hasInitComplete() && !Thread.currentThread().isInterrupted()) {
                        stopMultiFileObserver.stop(false);
                        stopMultiFileObserver.startWatching();
                        LogUtils.d(TAG, "onCreate dddd");
                    } else {
                        LogUtils.d(TAG, "onCreate faill");
                        break;
                    }
                }
            }
        });
        if (mObserver != null) {
            mObserver.start();
        }
    }

    public void onDestroy() {
        if (mObserver != null) {
            if (!mObserver.isInterrupted()) {
                mObserver.interrupt();
            }
        }
        removeAllMultiFileObserver();
    }

    private boolean needUpdate(String newName) {
        boolean ret = false;
        for (int i = 0; i < FileUtils.FileType.musicExt.length; i++) {
            ret = newName.endsWith(FileUtils.FileType.musicExt[i]);
            if (ret) {
                LogUtils.d(TAG, "find　a file extension:" + newName);
                break;
            }
        }
        return ret;
    }
}
