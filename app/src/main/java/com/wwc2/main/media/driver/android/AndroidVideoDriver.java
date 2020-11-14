package com.wwc2.main.media.driver.android;

import android.os.Message;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.MediaListener;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.media_interface.MediaDefine;

import java.util.List;

/**
 * the android video driver.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public class AndroidVideoDriver extends AndroidMediaDriver implements SuperMediaDriverable.Video.VideoDriverable {
    private final String TAG = AndroidVideoDriver.class.getSimpleName();

    private boolean restore = false;
    private String cameraBeforePath;
    private int videoBeforePlayStatus = MediaDefine.PlayStatus.PLAY_STATUS_DEFAULT;

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        Model().getPlayMode().setVal(MediaDefine.PlayMode.PLAY_MODE_REPEAT_LIST_ALL);       //音频默认全部循环
        SourceManager.getModel().bindListener(mVideoSourceListener);
    }

    @Override
    public void onDestroy() {
        SourceManager.getModel().unbindListener(mVideoSourceListener);
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "VideoInfo.ini";
    }

    @Override
    public String[] filter() {
        return FileUtils.FileType.videoExt;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_VIDEO;
    }

    @Override
    public void playPath(String pathFile, int startTime) {
        //设置为暂停状态!二次点击UI获取不到状态！
        //super.pause();//修改播放视频关ACC深度睡眠再上ACC，视频播放界面会显示二次暂停播放作用
        LogUtils.d(TAG, "playPath source：" + source() + ",SourceManager.getCurSource():" + SourceManager.getCurSource() + ",pathFile:" + pathFile + " startTime:" + startTime);
        //if (ApkUtils.isApkForeground(getMainContext(), "com.wwc2.video")) {//判断Video是否在前台，否则不响应（解决方控操控上下一曲问题）
        if (source() == SourceManager.getCurSource() || SourceManager.isVideoTop()) {
            McuManager.firstPlayProcess();

//            Model().getCurrentTime().setVal(0);
            Model().getFilePath().setVal(null);
//            Model().setCurrentPlayTime(startTime);
            setCurrentTime(startTime);
            Model().getFilePath().setVal(pathFile);
            //Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PLAY);
        }
    }

    @Override
    public void setLooping(Boolean isLoop) {

    }

    @Override
    public boolean play() {
        restore = false;
        int playStatus = Model().getPlayStatus().getVal();
        //语音结束会影响播放状态,先设置为暂停
        if (playStatus == MediaDefine.PlayStatus.PLAY_STATUS_PLAY) {
            Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PAUSE);
        }
        /*-begin-20180929-hzubin-modify-for-bug13244 播放音乐/视频按暂停作用时，再试倒车作用，取消倒车，音乐/视频会自动播放，体验不好-*/
        if (videoBeforePlayStatus == MediaDefine.PlayStatus.PLAY_STATUS_PAUSE) {
            videoBeforePlayStatus = MediaDefine.PlayStatus.PLAY_STATUS_DEFAULT;
            Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_STOP);
            pause();
        } else {
            if (FileUtils.isFileExist(Model().getFilePath().getVal()) &&//修改bug13398，2018-10-29
                    StorageDevice.isDiskMounted(getMainContext(), Model().getPlayStorage().getVal())) {
                super.play();
            } else {
                LogUtils.e(TAG, "Players play return fileExist=" + FileUtils.isFileExist(Model().getFilePath().getVal()) +
                        ", usbExist=" + StorageDevice.isDiskMounted(getMainContext(), Model().getPlayStorage().getVal()));
            }
        }
        /*-end-20180929-hzubin-modify-for-bug13244 播放音乐/视频按暂停作用时，再试倒车作用，取消倒车，音乐/视频会自动播放，体验不好-*/
        Model().getErrorStatus().setVal(MediaDefine.ErrorState.NO_ERROR);
//        LogUtils.d(TAG, " play!");
        return true;
    }

    @Override
    public boolean pause() {
        LogUtils.d(TAG, " pause!");
        super.pause();
        return true;
    }

    //zhongyang.hu add for when entery into camera, stop vido
    @Override
    public void onPause() {
        LogUtils.d(TAG, " onPause! PlayStatus:" + Model().getPlayStatus().getVal());
        if (Model().getPlayStatus().getVal() == MediaDefine.PlayStatus.PLAY_STATUS_PLAY) {
            //restore = true;
            super.pause();
        }
    }

    @Override
    public void onResume() {
        LogUtils.d(TAG, " onResume! restore=" + restore);
        if (restore == true) {
            //添加文件判断，不存在不下发错误状态
            if (!TextUtils.isEmpty(Model().getFilePath().getVal()) && FileUtils.isFileExist(Model().getFilePath().getVal())) {
                super.play();
            }
            restore = false;
        }
        super.onResume();
    }
    //end....

    @Override
    public boolean stop() {
//        LogUtils.d(TAG , " stop!");
        restore = false;
        super.stop();
        return true;
    }

    @Override
    public boolean seek(int seek) {
//        Model().setCurrentPlayTime(seek);
        setCurrentTime(seek);
        LogUtils.d(TAG, " seek!" + seek);
        restore = false;
        return true;
    }

    @Override
    public void setVolume(float left, float right) {

    }

    @Override
    public boolean changeRepeatMode() {
        return false;
    }

    @Override
    public boolean changeRandomMode() {
        return false;
    }

    //******************优化后新增代码2018-12-27 begin*******************//
    @Override
    public FourString[] getDeviceListInfo(int storageId) {
        return Model().getDeviceListInfoArray(storageId);
    }
    //******************优化后新增代码2018-12-27 end*******************//

    @Override
    public void setScreenSize(int screenSize) {
        Model().getScreenSize().setVal(screenSize);
        LogUtils.d(TAG, " screenSize!" + screenSize);
    }

    @Override
    public void setCurrentTime(int currentTime) {
        int playStatus = Model().getPlayStatus().getVal();
        //LogUtils.d(" setCurrentTime!" + currentTime + ",playStatus" + playStatus);
        if (MediaDefine.PlayStatus.PLAY_STATUS_STOP != playStatus) {
            Model().setCurrentPlayTime(currentTime);
            List<BaseListener> listeners = getModel().getListeners();
            if (null != listeners) {
                for (int i = 0; i < listeners.size(); i++) {
                    BaseListener temp = listeners.get(i);
                    if (null != temp) {
                        if (temp instanceof MediaListener) {
                            MediaListener listener = (MediaListener) temp;
                            listener.UpdateCurrentTime((int) currentTime);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setTotalTime(int totalTime) {
        LogUtils.d(TAG, " setTotalTime!" + totalTime);
        int mTotalTime = Model().getTotalTime().getVal();
        //文件浏览器播放同一视频总时间显示问题
        if (mTotalTime == totalTime) {
            Model().getTotalTime().setVal(0);
        }
        Model().getTotalTime().setVal(totalTime);
    }

    @Override
    public boolean prev() {
        LogUtils.d("AndroidMediaDriver prev()");
        restore = false;
        return super.prev();
    }

    @Override
    public boolean next() {
        restore = false;
        if (Model().getErrorStatus().getVal() != MediaDefine.ErrorState.ERROR_NORMAL) {
            if (super.next()) {
                return true;
            } else {
                //只有一首歌继续循环播放
                String[] fileList = Model().getFileListArray();
                if (fileList != null) {
                    if (fileList.length == 1) {
                        //2017/1/20 设置为空然后才可以继续播放
                        //Model().getFilePath().setVal(null);
                        //Model().getFilePath().setVal(null);//防止当前源不对记忆被抹掉
                        super.enter();
                        super.next();//2017/1/1 next() -> super.next()
                        return true;
                    }
                }
                return false;
            }
        } else {
            LogUtils.d(TAG, "Play file error pause to play the next!");
            Model().getErrorStatus().setVal(MediaDefine.ErrorState.ERROR_DEFAULT);
        }
        return false;
    }

    @Override
    public void setErrorStatus(int errorStatus) {
        LogUtils.d(TAG, " setErrorStatus!" + errorStatus);
        if (errorStatus == MediaDefine.ErrorState.ERROR_NOT_SEEKABLE) {
            super.updateErrorStatus(errorStatus);
        } else {
            super.setErrorStatus(errorStatus, Model().getFilePath().getVal());
        }
    }

    @Override
    public void setStorageErrorStatus(int errorStatus) {
        super.updateStorageErrorStatus(errorStatus);
    }

    @Override
    public void enter(String path) {
        int playStorage = Model().getPlayStorage().getVal();
        //判断设备挂载
        if (StorageDevice.isDiskMounted(getMainContext(), playStorage)) {
            if (!TextUtils.isEmpty(path)) {
                super.enter();
                //super.pause();//修改播放视频关ACC深度睡眠再上ACC，视频播放界面会显示二次暂停播放作用
                String filePath = Model().getFilePath().getVal();
                //Model().getFilePath().setVal(null);     //清空然后才能播放（path和播放的全路径一样的特殊情况）
                int currentTime = Model().getCurrentPlayTime();
                if (!path.equals(filePath)) {
                    currentTime = 0;
                }
                LogUtils.d(" do enter playPath:" + path + ",old:" + filePath + ",currentTime:" + currentTime);
                super.enter(path, currentTime);
            } else {
                //正播放中拔卡,退出上一个模式,插卡,然后进入
                startSearchFile(playStorage);
                LogUtils.d(TAG + " do enter startSearchFile:" + StorageDevice.toString(playStorage));
            }

        } else {
            //播放下个盘符
            LogUtils.d(TAG, " do enter wait not play can set net storage!");
//            Model().getErrorStatus().setVal(MediaDefine.ErrorState.ERROR_NORMAL);//acc 起来不用提示文件无法播放
            //浏览器进入
            if (FileUtils.isFileExist(path)) {
                super.enter(path, 0);
            } else {
                //防止ErrorHandler的FilePath路径错误
                Message msg = Message.obtain();
                msg.what = ErrorHandler.MOUNTED_TAG;
                msg.obj = path;
                mErrorHandler.sendMessage(msg);
            }

        }
    }

    @Override
    public void searchStorage(int storage) {
        if (StorageDevice.isDiskMounted(getMainContext(), storage)) {
            startSearchFile(storage);
        }
    }


    @Override
    public void switchStorage(int storage) {
        super.switchStorage(storage);
        LogUtils.d(TAG, " do switchStorage storage:" + storage + " storagePath:" + StorageDevice.toString(storage));
    }

    /**
     * 休眠听器
     */
    private SourceManager.SourceListener mVideoSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "CurSourceListener " + " oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            if (!TextUtils.isEmpty(Model().getFilePath().getVal())) {//防止Model().getFilePath().getVal()为空，先保存；
                cameraBeforePath = Model().getFilePath().getVal();
            }
            //开机acc处理
            if (Define.Source.SOURCE_POWEROFF == newVal || Define.Source.SOURCE_ACCOFF == newVal) {
                storageScanStatus(0);//设置扫描记忆设备提示为默认值
                mSearchPresenter.onStop();//在onStop不停止中断扫描线程，目前改为可中断，避免acc或者进时钟卡问题
                mErrorHandler.removeAllMessage();
            } else if (Define.Source.SOURCE_POWEROFF == oldVal || Define.Source.SOURCE_ACCOFF == oldVal) {

            } else if (Define.Source.SOURCE_VIDEO == oldVal && Define.Source.SOURCE_CAMERA == newVal
                    && source() == Define.Source.SOURCE_VIDEO) {
                restore = true;
                if (videoBeforePlayStatus == MediaDefine.PlayStatus.PLAY_STATUS_DEFAULT) {//bug13459视频播放界面，多次打倒车，退倒车后视频暂停播放2018-11-25
                    videoBeforePlayStatus = Model().getPlayStatus().getVal();
                }
                LogUtils.d(TAG,"videoBeforePlayStatus：" +videoBeforePlayStatus);
            } else if (Define.Source.SOURCE_CAMERA == oldVal && Define.Source.SOURCE_VIDEO == newVal
                    && source() == Define.Source.SOURCE_VIDEO) {//修改倒车后视频不播放
                /*-begin-20180425-hzeming-modify-for-bug11261 播usb2视频，倒车，ACC深休眠，再上ACC显示倒车画面，退出倒车，有2个视频声音输出-*/
                LogUtils.d("cameraBeforePath:" + cameraBeforePath + ",currentTime:" + Model().getCurrentPlayTime());
                /*if (!TextUtils.isEmpty(cameraBeforePath)) {//未完全解决bug11261所以注释
                    enter();
                    playFilePath(cameraBeforePath, Model().getCurrentTime().getVal());
                    //play();
                }*/
                /*-end-20180425-hzeming-modify-for-bug11261 播usb2视频，倒车，ACC深休眠，再上ACC显示倒车画面，退出倒车，有2个视频声音输出-*/
            }
        }

        /*-begin-20180419-hzubin-modify-for-bug11235播放usb2音乐，主页，视频，ACC浅休眠，拔掉usb2，再上ACC在视频界面，主页，再进入音乐不播放其它目录歌曲，且自动退回主页-*/
        @Override
        public void CurBackSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "CurBackSourceListener " + " oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));
            mErrorHandler.removeAllMessage();
        }
        /*-end-20180419-hzubin-modify-for-bug11235播放usb2音乐，主页，视频，ACC浅休眠，拔掉usb2，再上ACC在视频界面，主页，再进入音乐不播放其它目录歌曲，且自动退回主页-*/
    };
}
