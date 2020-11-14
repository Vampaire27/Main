package com.wwc2.main.media.driver;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.wwc2.audio.IjkAudioPlayer;
import com.wwc2.audio.MediaPlayControlable;
import com.wwc2.audio.SystemMediaPlayer;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.status.PlayerStateListener;

import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * Created by huwei on 2016/9/7.
 */
public class MusicThreadHandler extends Handler {
    private static final int MSG_SETPATH = 0;
    private static final int MSG_START = 1;
    private static final int MSG_PAUSE = 2;
    private static final int MSG_SEEK = 3;
    private static final int MSG_STOP = 4;
    private static final int MSG_SETLISTENER = 5;
    private static final int MSG_VOLUME = 6;
    WeakReference<MusicThread> mWeakMusicThread;

    public MusicThreadHandler(MusicThread musicThread) {
        mWeakMusicThread = new WeakReference<MusicThread>(musicThread);

    }

    public static MusicThreadHandler createHandler(Context context) {
        final MusicThread mMusicThread = new MusicThread(context);
        mMusicThread.start();
        return mMusicThread.getHandler();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        final MusicThread thread = mWeakMusicThread.get();
        if (thread == null) {
            return;
        }
        switch (msg.what) {
            case MSG_SETPATH:
                Bundle bundle = msg.getData();
                String path = bundle.getString("path");
                long startTime = bundle.getLong("time");
                thread.setPath(path, startTime);
                break;
            case MSG_START:
                thread.startPlay();
                break;
            case MSG_PAUSE:
                thread.pause();
                break;
            case MSG_SEEK:
                thread.seekTo((Long) msg.obj);
                break;
            case MSG_STOP:
                thread.stopPlay();
                break;
            case MSG_SETLISTENER:
                thread.setBackPlayerStateListener((PlayerStateListener) msg.obj);
                break;
            case MSG_VOLUME:
                Bundle bundle1 = msg.getData();
                float left = (float) bundle1.get("left");
                float right = (float) bundle1.get("right");
                thread.setVolume(left,right);
        }
    }

    public void setPath(String path, long startTime) {
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        bundle.putLong("time", startTime);
        Message message = obtainMessage(MSG_SETPATH);
        message.setData(bundle);
        sendMessage(message);
    }

    public void start() {
        sendEmptyMessage(MSG_START);
    }

    public void pause() {
        sendEmptyMessage(MSG_PAUSE);
    }

    public void seekTo(long seekTime) {
        sendMessage(obtainMessage(MSG_SEEK, seekTime));
    }

    public void stop() {
        sendEmptyMessage(MSG_STOP);
    }

    public void setPlayListener(PlayerStateListener listener) {
        sendMessage(obtainMessage(MSG_SETLISTENER, listener));
    }

    public void setVolume(float left, float right) {
        Bundle bundle = new Bundle();
        bundle.putFloat("left", left);
        bundle.putFloat("right", right);
        Message message = obtainMessage(MSG_VOLUME);
        message.setData(bundle);
        sendMessage(message);
    }


    private static class MusicThread extends Thread {

        private static final String TAG = MusicThread.class.getSimpleName();
        private MediaPlayControlable systemMediaPlayer;/*android player*/
        private MediaPlayControlable ijkAudioPlayer;
        private MediaPlayControlable currentPlayer;
        private String filePath = null;/*player path*/
        private long startTime = 0L;  /*player time*/
        private MusicThreadHandler mHandler;
        private final Object mSync = new Object();


        public MusicThread(Context mContext) {
            systemMediaPlayer = new SystemMediaPlayer(mContext);
            ijkAudioPlayer = new IjkAudioPlayer(mContext);
            bindListener();
        }

        @Override
        public void run() {
            super.run();
            int priority = android.os.Process.THREAD_PRIORITY_AUDIO;
            android.os.Process.setThreadPriority(priority);
            Looper.prepare();
//            LogUtils.e(TAG, "musicThread pid:" + getId()+"\tpriority "+getPriority());
            synchronized (mSync) {
                    mHandler = new MusicThreadHandler(this);
                    mSync.notifyAll();

            }
            Looper.loop();
            synchronized (mSync) {
                mHandler = null;
                stopPlay();
                mSync.notifyAll();
            }
            LogUtils.e(TAG, "MusicThread finished");
        }


        @Override
        protected void finalize() throws Throwable {
            LogUtils.i(TAG, "MusicThread#finalize");
            super.finalize();
        }


        /**
         * set play path and time
         *
         * @param path
         * @param currentTime
         * @return
         */
        public boolean setPath(String path, long currentTime) {
            this.filePath = path;
            this.startTime = currentTime;
            if (currentPlayer != null) {
                stopPlay();
            }
            currentPlayer = systemMediaPlayer;
            /*-begin-20180424-hzubin-add-for-修改部分ogg格式的音频自动无限循环-*/
            if (!TextUtils.isEmpty(path) && path.lastIndexOf(".") > 0) {
                String str = path.substring(path.lastIndexOf("."), path.length());
                if (!TextUtils.isEmpty(str) && str.equals(".ogg")) {
                    currentPlayer = ijkAudioPlayer;
                }
            }
            /*-end-20180424-hzubin-add-for-修改部分ogg格式的音频自动无限循环-*/

            if (currentPlayer != null) {
                currentPlayer.seekTo(currentTime);
                currentPlayer.setMediaPath(path);
            }
            return true;
        }

        /**
         * start player
         *
         * @return
         */
        public void startPlay() {
            if (currentPlayer != null) {
                currentPlayer.start();
            }
        }

        /**
         * pause player
         */
        public void pause() {
            if (currentPlayer != null) {
                currentPlayer.pause();
            }
        }

        /**
         * play seekTo
         *
         * @param progress
         * @return
         */
        public boolean seekTo(long progress) {
            if (currentPlayer != null) {
                currentPlayer.seekTo(progress);
            }
            return true;
        }

        /**
         * stop player
         *
         * @return
         */
        public boolean stopPlay() {
            synchronized (mSync) {

                if (currentPlayer != null) {
                    currentPlayer.stopPlayback();
                }
            }

            return true;
        }

        public void setVolume(float left, float right) {
            if (currentPlayer != null) {
                currentPlayer.setVolume(left, right);
            }
        }

        private void bindListener() {
            systemMediaPlayer.setPlayerStateListener(mPlayerStateListener);
            ijkAudioPlayer.setPlayerStateListener(mPlayerStateListener);
        }

        private PlayerStateListener mBackPlayerStateListener;/*player backer*/

        public void setBackPlayerStateListener(PlayerStateListener mBackPlayerStateListener) {
            this.mBackPlayerStateListener = mBackPlayerStateListener;
        }

        /**
         * video player back Listener
         */
        private PlayerStateListener mPlayerStateListener = new PlayerStateListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                if (mBackPlayerStateListener != null && SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                    mBackPlayerStateListener.onPrepared(mp);
                    mBackPlayerStateListener.updateDuration(mp.getDuration());
                }
//            mMessageHelper.Notify(false, VideoInterface.APK_TO_MAIN.PLAY, null);
            }

            @Override
            public void onCompletion(IMediaPlayer mp) {
                if (mBackPlayerStateListener != null && SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                    mBackPlayerStateListener.onCompletion(mp);
                }
            }

            @Override
            public void onSeekComplete(IMediaPlayer mp) {
                if (mBackPlayerStateListener != null && SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                    mBackPlayerStateListener.onSeekComplete(mp);
                }
            }

            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                boolean ret = false;
                if (SourceManager.getCurBackSource() != Define.Source.SOURCE_AUDIO) {
                    return ret;
                }
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        if (mp instanceof AndroidMediaPlayer) {
                            LogUtils.e(TAG, "MediaPlayer播放出错，尝试调用IjkMediaPlayer播放");
                            startIjkMediaPlayer();
                        } else {
                            if (mBackPlayerStateListener != null) {
                                mBackPlayerStateListener.onError(mp, what, extra);
                            }
                        }
                        ret = true;
                        break;
                    default:
                        if (mBackPlayerStateListener != null) {
                            mBackPlayerStateListener.onError(mp, what, extra);
                            ret = true;
                        }
                        break;
                }
                return ret;
            }

            @Override
            public void updateDuration(long duration) {
                if (mBackPlayerStateListener != null) {
//                mBackPlayerStateListener.updateDuration(duration);
                }
            }

            @Override
            public void updateProgress(long progress) {
                if (mBackPlayerStateListener != null && SourceManager.getCurBackSource() == Define.Source.SOURCE_AUDIO) {
                    mBackPlayerStateListener.updateProgress(progress);
                }
            }
        };

        public void startIjkMediaPlayer() {
            systemMediaPlayer.stopPlayback();
            currentPlayer = ijkAudioPlayer;
            if (currentPlayer != null) {
                currentPlayer.seekTo(startTime);
                currentPlayer.setMediaPath(filePath);
            }
        }


        public MusicThreadHandler getHandler() {
            synchronized (mSync) {
                if (mHandler == null) {
                    try {
                        mSync.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return mHandler;
        }
    }

}
