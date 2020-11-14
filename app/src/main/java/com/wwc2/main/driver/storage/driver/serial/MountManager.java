package com.wwc2.main.driver.storage.driver.serial;

import android.content.Context;

import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huwei on 2017/1/13.
 */
public class MountManager implements MountDriverable.MountTimerListener {
    private String TAG = MountManager.class.getSimpleName();
    private Context mContext;
    private MountDriverable mMountDriverable;
    private Map<Integer, MountTimer> MountTimerMap = new ConcurrentHashMap<>();

    public MountManager(Context mContext, MountDriverable mMountDriverable) {
        this.mContext = mContext;
        this.mMountDriverable = mMountDriverable;
    }

    public void addMountTimer(Integer storageId) {
        if (!MountTimerMap.containsKey(storageId)) {
            MountTimer mountTimer = new MountTimer(this);
            mountTimer.start(this.mContext, storageId);
        }
        LogUtils.d(TAG, " MountTimerMap:" + MountTimerMap.size());
    }

    public void stop() {
        for (Map.Entry<Integer, MountTimer> entry : MountTimerMap.entrySet()) {
            entry.getValue().stop();
            removeByKey(entry.getKey());
        }
        LogUtils.d(TAG, " MountTimerMap:" + MountTimerMap.size());
    }

    public void destroy() {
        stop();
    }

    public void removeByKey(int keyInteger) {
        if (MountTimerMap.containsKey(keyInteger)) {
            MountTimerMap.remove(keyInteger);
        }
    }

    @Override
    public void updateMountState(int storageId, boolean mounted) {
        removeByKey(storageId);
        if (mMountDriverable != null) {
            mMountDriverable.updateMountState(storageId, mounted);
        }
    }

    public class MountTimer {
        private final int mountCount = 20;
        private int mSignalTimerId = 0;
        private int currentNumber = 0;
        private MountDriverable.MountTimerListener mMountTimerListener;

        public MountTimer(MountDriverable.MountTimerListener mMountTimerListener) {
            this.mMountTimerListener = mMountTimerListener;
        }

        public void start(final Context context, final int storageId) {
            if (0 == mSignalTimerId) {
                mSignalTimerId = TimerUtils.setTimer(context, 1000, 1000, new Timerable.TimerListener() {
                    @Override
                    public void onTimer(int timerId) {
                        /*-begin-20180507-ydigngen、hzubin-modify-(计时器等于20s时把挂载设备改为卸载)11319本地无视频，只接一个U盘和一个记录仪， 进入视频并播放，ACC深休眠，拔掉U盘，再上ACC，手动唤醒语音，插入U盘，自动退出视频界面--*/
                        if (StorageDevice.isDiskMounted(context, storageId)) {
                            if (mMountTimerListener != null) {
                                mMountDriverable.updateMountState(storageId, true);
                                stop();
                            }
                        } else if (currentNumber == mountCount) {
                            if (mMountTimerListener != null) {
                                mMountTimerListener.updateMountState(storageId, false);
                                stop();
                            }
                        }
                        /*-end-20180507-ydigngen、hzubin-modify-(计时器等于20s时把挂载设备改为卸载)11319本地无视频，只接一个U盘和一个记录仪， 进入视频并播放，ACC深休眠，拔掉U盘，再上ACC，手动唤醒语音，插入U盘，自动退出视频界面--*/
                        currentNumber++;
                    }
                });
            }
        }

        public void stop() {
            if (0 != mSignalTimerId) {
                TimerUtils.killTimer(mSignalTimerId);
                mSignalTimerId = 0;
            }
            currentNumber = 0;
        }

    }
}
