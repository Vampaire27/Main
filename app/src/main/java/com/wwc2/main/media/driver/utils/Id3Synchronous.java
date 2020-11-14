package com.wwc2.main.media.driver.utils;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable.Audio.AudioPresenter;

import java.io.IOException;


/**同步歌词
 * Created by huwei on 2017/1/18.
 */
public class Id3Synchronous extends Thread{
    private String TAG = Id3Synchronous.class.getSimpleName();
    //线程退出标志
    private boolean needExit = false;
    //默认当前为Sd卡编号
    private int currentStorage = StorageDevice.MEDIA_CARD;
    //回调接口
    private AudioPresenter mAudioPresenter;
    //同步状态
    private int syncState = FileUtils.SearchState.SEARCH_STATE_DEFAULT;

    public Id3Synchronous(int currentStorage, AudioPresenter mAudioPresenter){
        this.currentStorage = currentStorage;
        this.mAudioPresenter = mAudioPresenter;
    }

    public int getCurrentStorage() {
        return currentStorage;
    }

    public int getSyncState() {
        return syncState;
    }

    @Override
    public void run() {
        super.run();
        syncState = FileUtils.SearchState.SEARCH_STATE_START;
        LogUtils.w(TAG, "Synchronous Id3 start!");
        FourString[] oldFourStringArray = this.mAudioPresenter.getMFourStringArrayById(currentStorage);
        if(oldFourStringArray != null){
            for(int i = 0; i< oldFourStringArray.length ;i++){
                if(needExit){
                    syncState = FileUtils.SearchState.SEARCH_STATE_ABORT;
                    LogUtils.e(TAG, "exit Synchronous Id3!");
                    break;
                }
                if(oldFourStringArray != null){
                    FourString oldFourString = oldFourStringArray[i];
                    Mp3File mMp3File = null;
                    try {
                        mMp3File = new Mp3File(oldFourString.getString1());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (UnsupportedTagException e) {
                        e.printStackTrace();
                    } catch (InvalidDataException e) {
                        e.printStackTrace();
                    }
                    oldFourStringArray[i] = mAudioPresenter.createFourString(oldFourString.getString1(),oldFourString.getString2(),mMp3File);
//                    LogUtils.d(TAG, "Synchronous Id3 ing! path:" + oldFourString.getString1());
                }
            }
            if(!needExit){
                syncState = FileUtils.SearchState.SEARCH_STATE_OVER;
                mAudioPresenter.updateFourStringArray(currentStorage,oldFourStringArray.clone());
                LogUtils.d(TAG, "Synchronous Id3 over!");
            }
        }else{
            syncState = FileUtils.SearchState.SEARCH_STATE_NO_FILE;
            LogUtils.d(TAG, "Synchronous Id3 no file!");
        }
    }

    public void needCancel(){
        needExit = true;
        interrupt();
    }

    public boolean needExe(){
        boolean result = false;
        switch(syncState){
//            case FileUtils.SearchState.SEARCH_STATE_START:
            case FileUtils.SearchState.SEARCH_STATE_DEFAULT:
            case FileUtils.SearchState.SEARCH_STATE_ABORT:
//            case FileUtils.SearchState.SEARCH_STATE_NO_FILE:
                result = true;
            break;
        }
        return result;
    }
}
