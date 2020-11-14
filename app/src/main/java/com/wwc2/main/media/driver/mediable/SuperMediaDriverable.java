package com.wwc2.main.media.driver.mediable;

import com.mpatric.mp3agic.Mp3File;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.IntegerString;
import com.wwc2.corelib.model.custom.MFourIntegerArray;
import com.wwc2.corelib.model.custom.MFourStringArray;

/**
 * Created by huwei on 2017/1/13.
 */
public class SuperMediaDriverable {

    public static class Audio {
        /**
         * 音频多媒体特有接口
         * Created by huwei on 2017/1/11.
         */
        public interface AudioDriverable {

            /**
             * 进入播放器
             */
            void enter(String path);

            /**
             * 进入设备 see {@link com.wwc2.common_interface.utils.StorageDevice}
             */
            void switchStorage(int storage);

            /**
             * 根据标题播放
             */
            boolean enterByTitle(String title);

            /**
             * 根据艺术家播放
             */
            boolean enterByArtist(String[] artistPathArray);

            /**
             * 根据专辑播放
             */
            boolean enterByAlbum(String[] albumPathArray);

            /**
             * 更新歌词
             */
            void updateLrcData(IntegerString[] lrcIntegerString);

            /**
             * 查询播放歌曲信息
             */
            FourString getMusicFourString(String filePath);

            /**
             * 更新播放歌曲信息
             */
            void updateFileDetail(FourString musicFourString);


            /**
             * 查询指定设备文件信息
             */
            FourString[] getMFourStringArrayById(int currentStorage);

            void updateFourStringArray(int currentStorage, FourString[] fourStrings);

            int getPlayStorageId();

            /**
             * 搜索设备音乐文件
             *
             * @param storage
             */
            void searchStorage(int storage);
        }

        public interface AudioPresenter {


            /**
             * 更新歌曲信息 title,artist,album,albumData
             */
            void findMusicInfo(String filePath);

            /**
             * 获取文件信息 title,artist,album,albumData
             */
            FourString createFourString(String audioPath, String fileName, Mp3File mp3File);

            /**
             * 调用Android接口获取ID信息
             */
            boolean getMusicId3Info(String filePath);

            /**
             * 查询指定设备文件信息
             */
            FourString[] getMFourStringArrayById(int currentStorage);

            /**
             * 更新全部文件信息
             */
            void updateFourStringArray(int currentStorage, FourString[] fourStrings);

            void removeStorage(int storageId);

            /**
             * 停止同步所有歌曲信息
             */
            void stop();
        }
    }

    public static class Video {
        /**
         * 视频多媒体特有接口
         * Created by huwei on 2017/1/11.
         */
        public interface VideoDriverable {
            /**
             * 进入播放器
             */
            void enter(String path);

            /**
             * 进入设备 see {@link com.wwc2.common_interface.utils.StorageDevice}
             */
            void switchStorage(int storage);

            /**
             * 设置
             */
            void setScreenSize(int screenSize);

            /**
             * 设置视频当前时间
             */
            void setCurrentTime(int currentTime);

            /**
             * 设置视频总时间
             */
            void setTotalTime(int totalTime);

            /**
             * 设置损坏类型 see {@link com.wwc2.media_interface.MediaDefine.ErrorState}
             */
            void setErrorStatus(int errorStatus);

            /**
             * 设置损坏类型 see {@link com.wwc2.media_interface.MediaDefine.ErrorState}
             */
            void setStorageErrorStatus(int errorStatus);

            /**
             * 搜索设备视频文件
             *
             * @param storage
             */
            void searchStorage(int storage);
        }
    }

    /**
     * 搜索线程监听类
     */
    public interface ListInfoListener {// extends MultiFileObserver.FileListener

        /**
         * 播放文件
         */
        void playFileInfo(int searchStorage, String filePath);

        /**
         * 搜索完成
         */
        void updateFileInfo(int searchStorage, FourString[] mediaFourStringArr, String[] mediaFile);

        /**
         * 更新搜索状态
         */
        void updateSearchInfo(int searchStorage, int searchState);

        /**
         * 无文件播放
         */
        void updateNoPlayFile(int searchStorage);

        /**
         * 移除搜索
         */
        void onPostExecute(int searchStorage);
    }

    /**
     * 提供接口给Presenter调用
     */
    public interface MediaFileDriverable {

        /**
         * 搜索文件后缀
         */
        String[] getFilter();

        /**
         * 获取正在播放的设备编号
         */
        int getPlayStorage();

        /**
         * 获取设备文件信息
         */
        MFourStringArray getMFourStringArray(int currentStorage);

        //******************优化后新增代码2018-12-27 begin*******************//
        FourString[] getFourStringArr(int currentStorage);

        void setFourStringArr(int storageId, FourString[] fileArr);
        //******************优化后新增代码2018-12-27 end*******************//

        /**
         * 更新存储设备中的文件信息
         */
        void updateListInfo(int searchStorage, FourString[] mediaFourStringArr);

        /**
         * 搜索状态
         */
        MFourIntegerArray getSearchInfo();

        /**
         * 更新搜索状态
         */
        void updateSearchInfo(FourInteger[] searchInfo);

        /**
         * 添加搜索状态
         */
        void addSearchInfo(FourInteger searchInfo);

        /**
         * 播放指定路径的文件
         */
        void playFileInfo(int searchStorage, String filePath, int currentTime);

        /**
         * 更新播放索引
         */
        void updateFileInfo(int searchStorage, FourString[] mediaFourStringArr);

        /**
         * 根据播放模式更新total,index等
         */
        void updatePlayMode();

        /**
         * 删除文件
         */
        void onFileDeleted(String name);

        /**
         * 播放路径
         */
        String getFilePath();

        /**
         * 当前播放时间
         */
        int getCurrentTime();

        /**
         * 获取挂载设备信息
         */
        IntegerSSBoolean getStorageInfo(Integer type);

        void updateCurrentStorage(int currentStorage);

        /**
         * 没有可播放文件
         */
        void updateNoPlayFile(int searchStorage);

        /**
         * 更新错误信息
         */
        void updateStorageErrorStatus(int error);

        void clearStorage();

        /**
         * 添加扫描记忆设备提示
         */
        void storageScanStatus(int status);
        /**
         * 10s或者20s后未检测出视频文件退出视频
         */
        void switchCurrentSource();

        /**
         * 获取播放状态
         */
        int getPlayStatus();

        int currentSource();
    }

    public interface SearchPresenter {

        /**
         * 搜索文件后缀
         */
        String[] getFilter();

        void onStart();

        /**
         * 启动搜索
         */
        void onResume();

        /**
         * 根据设备编号搜索 see {@link com.wwc2.common_interface.utils.StorageDevice}
         */
        @Deprecated
        void startSearchFile(int storageId, boolean playFirst);

        /**
         * 根据设备编号搜索
         */
        void startSearchFile(int storageId, boolean playFirst, int source);

        /**
         * 移除指定编号设备搜索 see {@link com.wwc2.common_interface.utils.StorageDevice}
         */
        void removeStorage(int storageId);

        /**
         * 没有可播放文件
         */
        void updateNoPlayFile(int searchStorage);

        Boolean addAndUpdateSearchInfo(FourInteger searchInfo);

        void playPriorityStorage(int storageId);

        boolean checkJump();

        void onStop();

        void onCreate();

        void onDestroy();


    }
}
