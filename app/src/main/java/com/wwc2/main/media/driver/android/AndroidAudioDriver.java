package com.wwc2.main.media.driver.android;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.IntegerString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.media.MediaListener;
import com.wwc2.main.media.audio.AudioLogic;
import com.wwc2.main.media.driver.MusicThreadHandler;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.driver.presenter.AudioPresenterImpl;
import com.wwc2.media_interface.MediaDefine;
import com.wwc2.status.PlayerStateListener;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * the android audio driver.
 *
 * @author wwc2
 * @date 2017/1/3
 */
public class AndroidAudioDriver extends AndroidMediaDriver implements PlayerStateListener, SuperMediaDriverable.Audio.AudioDriverable {
    private final String TAG = AndroidAudioDriver.class.getSimpleName();
    private MusicThreadHandler mMusicHandler;
    private PlayType mPlayType = PlayType.PATH;
    private SuperMediaDriverable.Audio.AudioPresenter mAudioPresenter;

    private AudioDriverHandler mHandler = new AudioDriverHandler();
    private final int MSG_READ_ID3INFO = 8000;
    private int audioBeforePlayStatus = MediaDefine.PlayStatus.PLAY_STATUS_DEFAULT;
    //public static boolean isFirstFromFile= false;//上电第一次从文件管理器进入

    private boolean mediaPlayerPrepared = false;

    @Override
    public void onCreate(Packet packet) {
        LogUtils.d(TAG, "AndroidAudioDriver onCreate start.");
        super.onCreate(packet);
        LogUtils.d(TAG, "AndroidAudioDriver new AudioPresenterImpl.");
        mAudioPresenter = new AudioPresenterImpl(getMainContext(), this);//YDG 2017-04-18
//        Model().getPlayMode().setVal(MediaDefine.PlayMode.PLAY_MODE_REPEAT_LIST_ALL);       //音频默认全部循环
        SourceManager.getModel().bindListener(mAudioSourceListener);
        LogUtils.d(TAG, "AndroidAudioDriver onCreate over.");
    }

    @Override
    public void onDestroy() {
        SourceManager.getModel().unbindListener(mAudioSourceListener);
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "AudioInfo.ini";
    }

    @Override
    public String[] filter() {
        return FileUtils.FileType.musicExt;
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_AUDIO;
    }

    /**
     * 根据文件更新标题、艺术家、专辑、播放、播放索引、获取歌词
     *
     * @param pathFile
     */
    @Override
    public void playPath(String pathFile, int startTime) {
        LogUtils.d("audio playPath:" + pathFile);
        //hzy add for when search finish, auto play,may user has change the source. 20180326
        if (source() == SourceManager.getCurBackSource()) {
            //end
            checkMusicThreadHandler();

            if (mMusicHandler != null && FileUtils.isFileExist(pathFile)) {
                LogUtils.d("更新标题、艺术家、专辑、播放、播放索引");

                McuManager.firstPlayProcess();

                //更新标题、艺术家、专辑、播放、播放索引
                //设置时间在更新时间
//                Model().setCurrentPlayTime(startTime);
                updateProgress(startTime);

                Model().getFilePath().setVal(pathFile);//添加错误提示时有问题 2017/1/22

                mMusicHandler.setPath(pathFile, startTime);
                //修改获取歌曲ID3信息的方式，优化CPU占有率。YDG 2017-04-19
                //mAudioPresenter.findMusicInfo(pathFile);//更新歌曲信息
                mHandler.removeMessages(MSG_READ_ID3INFO);
                if (!mAudioPresenter.getMusicId3Info(pathFile)) {
                    Message msg = Message.obtain();
                    msg.what = MSG_READ_ID3INFO;
                    msg.obj = pathFile;
                    //在第一次没获取到时，延时5s重新获取一次。
                    mHandler.sendMessageDelayed(msg, 5000);
                }
            } else {
                LogUtils.e("音乐文件出错");
                setErrorStatus(MediaDefine.ErrorState.ERROR_NORMAL, pathFile);
            }
            LogUtils.d(TAG, "audio playPath:-------------------- end!");
        } else {
            LogUtils.d(TAG, "current BackSource " + SourceManager.getCurBackSource());
        }
    }

    private void checkMusicThreadHandler() {
        if (mMusicHandler == null) {
            mMusicHandler = MusicThreadHandler.createHandler(getMainContext());
            mMusicHandler.setPlayListener(this);
            LogUtils.d(TAG, " audio checkMusicThreadHandler onCreate()!");
        }
    }

    @Override
    public void setLooping(Boolean isLoop) {
//        if(mSystemAudioPlayer!= null){
//            mSystemAudioPlayer.playLoop(isLoop);
//        }
    }

    @Override
    public boolean play() {
        LogUtils.d(TAG, " play!");
        int fileIndex = Model().getIndex().getVal();
        int fileTotal = Model().getTotal().getVal();
        int playMode = Model().getPlayMode().getVal();
        int totalTime = Model().getTotalTime().getVal();
        int currentTime = Model().getCurrentPlayTime();
        //不循环播放模式
        if (fileIndex == fileTotal && MediaDefine.PlayMode.isRepeatClose(playMode)
                && ((totalTime - currentTime) / 1000 == 0) && !MediaDefine.PlayMode.isCurrent(playMode)) {
            String[] fileString = Model().getFileListArray();
            if (fileString != null && fileString.length > 0) {
                playFilePath(fileString[0], 0);
            }
            LogUtils.d(TAG, "Do not cycle playback mode the last one to broadcast the play from the first play!");
        } else if (mMusicHandler != null) {
            if (!TextUtils.isEmpty(Model().getFilePath().getVal()) &&
                    FileUtils.isFileExist(Model().getFilePath().getVal()) &&//修改bug13334，2018-10-29
                    StorageDevice.isDiskMounted(getMainContext(), Model().getPlayStorage().getVal())) {
                LogUtils.d(TAG, "Players continue to play!");
                /*-begin-20180929-hzubin-modify-for-bug13244 播放音乐/视频按暂停作用时，再试倒车作用，取消倒车，音乐/视频会自动播放，体验不好-*/
                if (audioBeforePlayStatus == MediaDefine.PlayStatus.PLAY_STATUS_PAUSE &&
                        SourceManager.getCurSource() != Define.Source.SOURCE_CAMERA) {//bug13453播放音乐用手机拨打电话试倒车，在倒车界面用手机挂掉电话，后台音乐不会自动播放

                } else {
                    LogUtils.d(TAG, "Players continue to play!  mediaPlayerPrepared=" + mediaPlayerPrepared +
                            ", file=" + Model().getFilePath().getVal());
                    if (mediaPlayerPrepared) {
                        if (null != mMusicHandler) mMusicHandler.start();
                        super.play();
                    } else {
                        //bug13334在ACC ON还未搜索到文件时，马上点击播放按钮，状态与实际不一致2018-11-09
                        enter();
                        playFilePath(Model().getFilePath().getVal(), currentTime);
                    }
                    mediaPlayerPrepared = false;
                }
                audioBeforePlayStatus = MediaDefine.PlayStatus.PLAY_STATUS_DEFAULT;
                /*-end-20180929-hzubin-modify-for-bug13244 播放音乐/视频按暂停作用时，再试倒车作用，取消倒车，音乐/视频会自动播放，体验不好-*/
            } else {
                LogUtils.e(TAG, "Players play return fileExist=" + FileUtils.isFileExist(Model().getFilePath().getVal()) +
                        ", usbExist=" + StorageDevice.isDiskMounted(getMainContext(), Model().getPlayStorage().getVal()));
            }
        } else {
            LogUtils.d(TAG, "Players are not allowed to stop play!");
        }
        return true;
    }

    @Override
    public boolean pause() {
        if (mMusicHandler != null) {
            mMusicHandler.pause();
            super.pause();
        }
        LogUtils.d(TAG, " pause!");
        return true;
    }

    @Override
    public boolean stop() {
        super.stop();
        LogUtils.d(TAG, " stop! after Destroy!");
        if (mMusicHandler != null) {
            mMusicHandler.stop();
//            mSystemAudioPlayer.stop();
        }
        return true;
    }

    @Override
    public boolean seek(int seek) {
        super.seek(seek);
        if (mMusicHandler != null) {
            int fileIndex = Model().getIndex().getVal();
            int fileTotal = Model().getTotal().getVal();
            int playMode = Model().getPlayMode().getVal();
            int totalTime = Model().getTotalTime().getVal();
            int currentTime = Model().getCurrentPlayTime();
            if (fileIndex == fileTotal
                    && MediaDefine.PlayMode.isRepeatClose(playMode)
                    && ((totalTime - currentTime) / 1000 < 2)
                    && !MediaDefine.PlayMode.isCurrent(playMode)) {
                //当前播放模式是列表循环，播放到最后一首，播放完后，拖拉进度条无效；防止更新当前播放时间，导致this.play()出现异常
                LogUtils.d(TAG, " seek列表循环最后一首后拖拉进度条无效");
                /*-begin-20180508-hzubin-add-bug11139播放完最后一首,快退该歌曲点播放按钮音乐无法播放，切换上一首正常-*/
                playPath(Model().getFilePath().getVal(), seek);
                /*-end-20180508-hzubin-add-bug11139播放完最后一首,快退该歌曲点播放按钮音乐无法播放，切换上一首正常-*/
            } else {
                mMusicHandler.seekTo(seek);
//                Model().setCurrentPlayTime(seek);
                updateProgress(seek);
            }
        }
        LogUtils.d(TAG, " seek!" + seek);
        return true;
    }

    @Override
    public void setVolume(float left, float right) {
        if (mMusicHandler != null) {
            mMusicHandler.setVolume(left, right);
        }
        LogUtils.d(TAG, " setVolume" + left + "/" + right);

    }

    @Override
    public boolean changeRepeatMode() {
        int playmode = Model().getPlayMode().getVal();
        playMode(MediaDefine.PlayMode.changePlayModeWithoutRand(playmode));
        return true;
    }

    @Override
    public boolean changeRandomMode() {
        int playmode = Model().getPlayMode().getVal();
        playMode(MediaDefine.PlayMode.changePlayModeByRand(playmode));
        return true;
    }

    //******************优化后新增代码2018-12-27 begin*******************//
    @Override
    public FourString[] getDeviceListInfo(int storageId) {
        return Model().getDeviceListInfoArray(storageId);
    }
    //******************优化后新增代码2018-12-27 end*******************//

    @Override
    public boolean next() {
        if (super.next()) {
            return true;
        }
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        LogUtils.d(TAG, "Audio files can be played!");
        mediaPlayerPrepared = true;
        play();
        Model().getErrorStatus().setVal(MediaDefine.ErrorState.NO_ERROR);
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        int playMode = Model().getPlayMode().getVal();
        LogUtils.d(TAG, "Play finish!" + MediaDefine.PlayMode.toString(playMode) + " ErrorStatus:" + Model().getErrorStatus().getVal());
        Model().getPlayStatus().setVal(MediaDefine.PlayStatus.PLAY_STATUS_PAUSE);

        if (Model().getErrorStatus().getVal() != MediaDefine.ErrorState.ERROR_NORMAL) {
            if (MediaDefine.PlayMode.isCurrent(playMode)) {
                //单曲循环()
                setLooping(true);
                //循环播放
                String filePath = Model().getFilePath().getVal();
                if (!TextUtils.isEmpty(filePath)) {
                    enter();
                    playFilePath(filePath, 0);
                }
            } else {
                String[] fileList = Model().getFileListArray();
                //只有一首歌曲循环播放
                if (fileList != null) {
                    if (fileList.length == 1) {
                        enter();
                        playFilePath(fileList[0], 0);
                    } else {
                        next();
                    }
                }
            }
        } else {
//            Model().getErrorStatus().setVal(MediaDefine.ErrorState.ERROR_DEFAULT);
        }
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        LogUtils.d(TAG, "audio onSeekComplete");
        updateErrorStatus(MediaDefine.StorageErrorState.NO_ERROR);
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        LogUtils.d(TAG, "File damage cannot be played!" + what);
        if (IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE == what) {
            updateErrorStatus(MediaDefine.ErrorState.ERROR_NOT_SEEKABLE);
        } else {
            setErrorStatus(MediaDefine.ErrorState.ERROR_NORMAL, Model().getFilePath().getVal());
        }
        return false;
    }

    @Override
    public void updateDuration(long totalTime) {
        LogUtils.d(TAG, "Total file time!" + totalTime);
        Model().getTotalTime().setVal((int) totalTime);
    }

    @Override
    public void updateProgress(long currentTime) {
//        LogUtils.d(TAG,"文件当前进度！" + currentTime);
        /***/
//        Model().getCurrentTime().setVal((int) currentTime);
        Model().setCurrentPlayTime((int) currentTime);
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

    @Override
    public void enter(String path) {
        LogUtils.d("enter path:" + path);
        int playStorage = Model().getPlayStorage().getVal();
        LogUtils.d(TAG, "enter playStorage:" + playStorage);
        LogUtils.d(TAG, "enter playStorage isDiskMounted:" + StorageDevice.isDiskMounted(getMainContext(), playStorage));
        LogUtils.d(TAG, "enter PlayStatus:" + Model().getPlayStatus().getVal());
        if (StorageDevice.isDiskMounted(getMainContext(), playStorage)) {
//            checkUpdatePlayMode(path);
            if (!TextUtils.isEmpty(path) && FileUtils.isFileExist(path)) {
                //如果不是播放就允许重播
                if (Model().getPlayStatus().getVal() != MediaDefine.PlayStatus.PLAY_STATUS_PLAY) {
                    super.enter();
                }
                String filePath = Model().getFilePath().getVal();
                int currentTime = Model().getCurrentPlayTime();
                if (!path.equals(filePath)) {
                    currentTime = 0;
                }
                LogUtils.d(TAG, " do enter playPath:" + path + " currentTime:" + currentTime);
                super.enter(path, currentTime);
            } else {
                //真正播放中拔卡,退出上一个模式,插卡,然后进入
                startSearchFile(playStorage);
                LogUtils.d(TAG, " do enter startSearchFile:" + StorageDevice.toString(playStorage));
            }
        } else {
            LogUtils.d(TAG, " do enter wait not play can set net storage!");
//            Model().getErrorStatus().setVal(MediaDefine.ErrorState.ERROR_NORMAL);//acc 起来不用提示文件无法播放
            //浏览器进入
//            if (FileUtils.isFileExist(path)) {//U盘没挂载上，避免出现播放错误问题，无记忆盘符问题。2018-12-17
//                LogUtils.d("FileUtils.isFileExist:" + path);
//                //super.enter();
//                super.enter(path, 0);
//            } else {
                //防止ErrorHandler的FilePath路径错误
                Message msg = Message.obtain();
                msg.what = ErrorHandler.MOUNTED_TAG;
                msg.obj = path;
                mErrorHandler.sendMessage(msg);
//            }
//            updateFourStringArray(playStorage, null);
//            mSearchPresenter.playPriorityStorage(playStorage);
            //播放下个盘符
        }
    }

    @Override
    public void searchStorage(int storage) {
        if (StorageDevice.isDiskMounted(getMainContext(), storage)) {
            startSearchFile(storage);
        }
    }

    //听歌手专辑后 选择播放非专辑列表中的歌
    public void checkUpdatePlayMode(String path) {
        LogUtils.d(TAG, "checkUpdatePlayMode:" + path);
        boolean ret = false;
        if (!TextUtils.isEmpty(path)) {
            LogUtils.d("mPlayType:" + mPlayType);
            if (mPlayType != PlayType.PATH) {
                String[] folderList = Model().getFileListArray();
                if (folderList != null) {
                    LogUtils.d("checkUpdatePlayMode: length:" + folderList.length);
                    for (int i = 0; i < folderList.length; i++) {
                        LogUtils.d("checkUpdatePlayMode:" + folderList[i]);
                        if (path.equals(folderList[i])) {
                            LogUtils.d(TAG, "checkUpdatePlayMode path.equals(folderList[i])");
                            ret = true;
                            break;
                        }
                    }
                    if (!ret) {
                        mPlayType = PlayType.PATH;
                        LogUtils.d(TAG, "checkUpdatePlayMode mPlayType Path");
                    }
                }
            }
            LogUtils.d(TAG, "checkUpdatePlayMode end!");
        }
    }

    @Override
    public void switchStorage(int storage) {
        super.switchStorage(storage);
        LogUtils.d(TAG, " do  switchStorage storage:" + storage + " storagePath:" + StorageDevice.toString(storage));
    }

    @Override
    public boolean enterByTitle(String paramString) {
        boolean result = false;
        mPlayType = PlayType.PATH;
//        LogUtils.d(TAG, " do enterByTitle title:" + paramString);
        FourString[] currentFfArray = getMFourStringArrayByPlay();
        LogUtils.d(TAG, " do enterByTitle title:" + paramString + " size:" + (currentFfArray == null ? "null" : currentFfArray.length));
        if (currentFfArray != null) {
            for (int i = 0; i < currentFfArray.length; i++) {
                String path = currentFfArray[i].getString1();
                if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(paramString)) {
                    if (path.equals(paramString)) {
                        result = true;
                        playFilePath(currentFfArray[i].getString1(), 0);
                        break;
                    }
                }
            }
        }
        return result;
    }


    @Override
    public boolean enterByArtist(String[] artistPathArray) {
        boolean result = false;
        mPlayType = PlayType.ARTIST;
        LogUtils.d(TAG, " do enterByArtist artistPathArray:" + (artistPathArray == null ? "null" : artistPathArray.length));
        String[] strArr = artistPathArray;
        if (strArr != null && strArr.length > 0) {
            //******************优化后新增代码2018-12-27 begin*******************//
            Model().setFileListArray(strArr);
            BaseLogic audioLogic = ModuleManager.getLogicByName(com.wwc2.audio_interface.AudioDefine.MODULE);
            if (audioLogic != null && audioLogic instanceof AudioLogic) {
                ((AudioLogic) audioLogic).updateFileList(strArr);
            }
            //******************优化后新增代码2018-12-27 end*******************//
            LogUtils.d(TAG, " do enterByTitle artist: length:" + strArr.length);
            playFilePath(strArr[0], 0);
            result = true;
        }
        return result;
    }

    @Override
    public boolean enterByAlbum(String[] albumPathArray) {
        boolean result = false;
        mPlayType = PlayType.ALBUM;
        LogUtils.d(TAG, " do enterByAlbum albumPathArray:" + (albumPathArray == null ? "null" : albumPathArray.length));
        String[] strArr = albumPathArray;
        if (strArr != null && strArr.length > 0) {
            //******************优化后新增代码2018-12-27 begin*******************//
            Model().setFileListArray(strArr);
            BaseLogic audioLogic = ModuleManager.getLogicByName(com.wwc2.audio_interface.AudioDefine.MODULE);
            if (audioLogic != null && audioLogic instanceof AudioLogic) {
                ((AudioLogic) audioLogic).updateFileList(strArr);
            }
            //******************优化后新增代码2018-12-27 end*******************//
            playFilePath(strArr[0], 0);
            result = true;
        }
        return result;
    }

    @Override
    public void updateLrcData(IntegerString[] lrcIntegerString) {
        Model().getLrcData().setVal(lrcIntegerString);
    }


    @Override
    public void onStart() {
        checkMusicThreadHandler();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        mAudioPresenter.stop();
        if (mMusicHandler != null) {
            mMusicHandler.stop();
            mMusicHandler = null;
        }
        mHandler.removeMessages(MSG_READ_ID3INFO);
        super.onStop();
    }

    /**
     * 播放模式
     */
    @Override
    public boolean playMode(int mode) {
        mPlayType = PlayType.PATH;
        super.playMode(mode);
        return true;
    }

    /**
     * 根据播放类型和播放路径更新播放列表(这里添加歌手和专辑)
     *
     * @param playMode 播放模式
     * @param path     播放路径
     */
    @Override
    protected void updateByPlayMode(int playMode, String path) {
        LogUtils.d(TAG, "child updateByPlayMode!");
        checkUpdatePlayMode(path);
        switch (mPlayType) {
            case PATH:
                LogUtils.d(TAG, "updateByPlayMode: path");
                super.updateByPlayMode(playMode, path);
                break;
            case TITLE:
                LogUtils.d(TAG, "updateByPlayMode: title");
                updatePlayType(path);
                break;
            case ARTIST:
                LogUtils.d(TAG, "updateByPlayMode: artist");
                updatePlayType(path);
                break;
            case ALBUM:
                LogUtils.d(TAG, "updateByPlayMode: album");
                updatePlayType(path);
                break;
        }
        LogUtils.d(TAG, "child updateByPlayMode end!");
    }

    public void updatePlayType(String path) {
        String[] fileArray = Model().getFileListArray();
        if (fileArray != null) {
            Model().getTotal().setVal(fileArray.length);
            /*-begin-20180213-ydinggen-add-解决在音乐播放时曲目不能同步到Can-*/
            if (Model().getIndex().getVal() == getFileIndex(path) + 1) {
                Model().getIndex().setVal(0);
            }
            /*-end-20180213-ydinggen-add-解决在音乐播放时曲目不能同步到Can-*/
            Model().getIndex().setVal(getFileIndex(path) + 1);
        } else {
            Model().getTotal().setVal(1);
            Model().getIndex().setVal(1);
        }
    }

    /**
     * 获取当前存储设备的播放列表
     *
     * @param paramString 关键字
     * @param mPlayType   TITLE:音乐标题;ARTIST:艺术家;ALBUM:专辑;
     * @return 播放列表
     */
    private String[] getEnterType(String paramString, PlayType mPlayType) {
        FourString[] mFourString = getMFourStringArrayByPlay();
        List<String> titleList = new ArrayList<>();
        if (mFourString != null) {
            for (int i = 0; i < mFourString.length; i++) {
                if (mPlayType == PlayType.TITLE) {

                    String title = mFourString[i].getString2();
                    if (!TextUtils.isEmpty(title)) {
                        if (mFourString[i].getString2().indexOf(paramString) >= 0) {
                            titleList.add(mFourString[i].getString1());
                        }
                    }
                } else if (mPlayType == PlayType.ARTIST) {
                    String artist = mFourString[i].getString3();
                    if (!TextUtils.isEmpty(artist) && !"unknown".equals(artist)) {
                        if (artist.indexOf(paramString) >= 0) {
                            titleList.add(mFourString[i].getString1());
                        }
                    } else {
                        String path = mFourString[i].getString1();
                        path = path.substring(path.lastIndexOf("/"));
                        if (path.indexOf(paramString) >= 0) {
                            titleList.add(mFourString[i].getString1());
                        }
                    }

                } else if (mPlayType == PlayType.ALBUM) {
                    String album = mFourString[i].getString4();
                    if (!TextUtils.isEmpty(album)) {
                        if (album.indexOf(paramString) >= 0) {
                            titleList.add(mFourString[i].getString1());
                        }
                    }

                }
            }
        }
        String[] strArr = new String[titleList.size()];
        titleList.toArray(strArr);
        return strArr;
    }


    @Override
    public FourString getMusicFourString(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        FourString[] arrFourString = getMFourStringArrayByPlay();
        if (null != arrFourString) {
            for (int i = 0; i < arrFourString.length; i++) {
                FourString mFourString = arrFourString[i];
                if (filePath.equals(mFourString.getString1())) {
                    return mFourString;
                }
            }
        }
        return null;
    }

    @Override
    public void updateFileDetail(FourString musicFourString) {
        if (musicFourString != null) {
            LogUtils.d(TAG, "updateFileDetail musicFourString.getString1():" + musicFourString.getString1());
            LogUtils.d(TAG, "updateFileDetail Model().getFilePath().getVal():" + Model().getFilePath().getVal());
            if (musicFourString.getString1().equals(Model().getFilePath().getVal())) {
                Model().getTitle().setVal(musicFourString.getString2());
                Model().getArtist().setVal(musicFourString.getString3());
                Model().getAlbum().setVal(musicFourString.getString4());
            } else {
                LogUtils.d(TAG, "updateFileDetail musicFourString is null!");
            }
        } else {
            LogUtils.d(TAG, "updateFileDetail musicFourString is null!");
        }
    }


    @Override
    public FourString[] getMFourStringArrayById(int currentStorage) {
        //******************优化后新增代码2018-12-27 begin*******************//
//        return getMFourStringArray(currentStorage).getVal();
        return getFourStringArr(currentStorage);
        //******************优化后新增代码2018-12-27 end*******************//
    }

    @Override
    public void updateFourStringArray(int currentStorage, FourString[] fourStrings) {
        updateListInfo(currentStorage, fourStrings);
    }

    @Override
    public int getPlayStorageId() {
        return Model().getPlayStorage().getVal();
    }


    /**
     * 获取正在搜索的属性
     *
     * @param searchStorage storage 编号 see{@link com.wwc2.common_interface.utils.StorageDevice}
     * @return
     */
    private FourInteger getSearchInfoById(int searchStorage) {
        FourInteger[] searchInfoArray = Model().getSearchInfo().getVal();
        if (searchInfoArray != null) {
            for (int i = 0; i < searchInfoArray.length; i++) {
                if (searchInfoArray[i] != null && searchInfoArray[i].getInteger1() == searchStorage) {
                    return searchInfoArray[i];
                }
            }
        }
        return null;
    }

    public enum PlayType {
        PATH, TITLE, ARTIST, ALBUM
    }

    @Override
    protected void removeStorage(int storageId) {
        super.removeStorage(storageId);
        mAudioPresenter.removeStorage(storageId);
    }

    private class AudioDriverHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            LogUtils.d(TAG, "AudioDriverHandler" + msg.what);
            switch (msg.what) {
                case MSG_READ_ID3INFO:
                    mHandler.removeMessages(MSG_READ_ID3INFO);
                    String path = (String) msg.obj;
                    mAudioPresenter.getMusicId3Info(path);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 休眠听器
     */
    private SourceManager.SourceListener mAudioSourceListener = new SourceManager.SourceListener() {
        @Override
        public void CurSourceListener(Integer oldVal, Integer newVal) {
            LogUtils.d(TAG, "CurSourceListener " + " oldVal:" + Define.Source.toString(oldVal) + " newVal:" + Define.Source.toString(newVal));

            //开机acc处理
            if (Define.Source.SOURCE_POWEROFF == newVal || Define.Source.SOURCE_ACCOFF == newVal) {
                storageScanStatus(0);//设置扫描记忆设备提示为默认值
                mSearchPresenter.onStop();//在onStop不停止中断扫描线程,目前改为可中断，避免acc或者进时钟卡问题
                mErrorHandler.removeAllMessage();
            } else if (Define.Source.SOURCE_POWEROFF == oldVal || Define.Source.SOURCE_ACCOFF == oldVal) {

            } else if (Define.Source.SOURCE_AUDIO == oldVal && Define.Source.SOURCE_CAMERA == newVal) {
                audioBeforePlayStatus = Model().getPlayStatus().getVal();
                LogUtils.d(TAG, "audioBeforePlayStatus:" + audioBeforePlayStatus);
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
