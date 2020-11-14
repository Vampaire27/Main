package com.wwc2.main.media.driver.search;

import android.os.Environment;
import android.text.TextUtils;

import com.wwc2.common_interface.utils.FileUtils;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.driver.utils.Pinyin4jUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by huwei on 2017/1/13.
 */
public class FilesSearchRunnable extends Thread {
    //这个提供拦截文件名
    private SuperMediaDriverable.SearchPresenter mSearchPresenter;

    private final String TAG = FilesSearchRunnable.class.getSimpleName();
    private int currentStorage = StorageDevice.MEDIA_CARD;
    private String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private Boolean playFirst = true;
    protected List<FourString> mFourStringList = new ArrayList<>();
    public SuperMediaDriverable.ListInfoListener mListInfoListener;
    private final int FILE_RECURSION_NUM = 5;

    private boolean isEnable = false;

    public int getCurrentStorage() {
        return currentStorage;
    }

    public void setListInfoListener(SuperMediaDriverable.ListInfoListener mListInfoListener) {
        this.mListInfoListener = mListInfoListener;
    }


    public FilesSearchRunnable(int currentStorage, String storagePath, Boolean playFirst) {
        this.currentStorage = currentStorage;
        this.storagePath = storagePath;
        this.playFirst = playFirst;
    }

    public FilesSearchRunnable(SuperMediaDriverable.SearchPresenter mSearchPresenter, int currentStorage, String storagePath, Boolean playFirst) {
        this(currentStorage, storagePath, playFirst);
        this.mSearchPresenter = mSearchPresenter;
    }

    @Override
    public void run() {
        mFourStringList.clear();
        if (mListInfoListener != null) {
            mListInfoListener.updateSearchInfo(currentStorage, FileUtils.SearchState.SEARCH_STATE_START);
        }
        LogUtils.d(TAG, "search file start  storagePath:" + storagePath + ", isEnable=" + isEnable);
        if (!TextUtils.isEmpty(storagePath)) {
            //根据设备编号查询指定文件
            getMediaFiles(storagePath, 0);
            LogUtils.d(TAG, "currentStorage:" + currentStorage + " ,storagePath file size:" + mFourStringList.size());
            if (mFourStringList.size() > 0) {
                //拼音排序&文件夹排序
                FourString[] mediaFourStringArr = new FourString[mFourStringList.size()];
                String[] mediaFile = new String[mFourStringList.size()];
                for (int i = 0; i < mediaFourStringArr.length; i++) {
                    mediaFourStringArr[i] = mFourStringList.get(i);
                    mediaFile[i] = mediaFourStringArr[i].getString1();
                }

                if (mListInfoListener != null) {
                    if (!isEnable) {
                        mListInfoListener.updateSearchInfo(currentStorage, FileUtils.SearchState.SEARCH_STATE_OVER);

                        //文件很多时，会比较耗时，会导致插入另外一个USB时，列表还会显示上一个USB的内容。采用线程处理。2019-01-18
                        Arrays.sort(mediaFourStringArr, new PinyinComparator());
                        Arrays.sort(mediaFourStringArr, new FilePathComparator());
                        if (!isEnable) {
                            mListInfoListener.updateFileInfo(currentStorage, mediaFourStringArr, mediaFile);
                        }
                    } else {
                        LogUtils.e(TAG, "search file start 11 storagePath:" + storagePath + ", isEnable=" + isEnable);
                    }
                }
                mFourStringList.clear();
            } else {
                if (mListInfoListener != null) {
                    LogUtils.d("currentStorage:" + currentStorage);
                    if (!isEnable) {
                        mListInfoListener.updateSearchInfo(currentStorage, FileUtils.SearchState.SEARCH_STATE_NO_FILE);
                    }
                    mListInfoListener.updateFileInfo(currentStorage, null, null);//解决剪切后列表不更新
                    if (playFirst) {
                        mListInfoListener.updateNoPlayFile(currentStorage);
                    }
                }
                LogUtils.d(TAG, " path:" + StorageDevice.getPath(currentStorage) + "not have file!");
            }
        }
        /*-begin-20180510-hzubin-modify-删除线程在MediaPresenterImpl.updateFileInfo(bug11367本地无视频-打开视频，画面一直显示在视频界面)-*/
        //if (mListInfoListener != null) {
        //    mListInfoListener.onPostExecute(currentStorage);
        //}
        /*-end-20180510-hzubin-modify-删除线程在MediaPresenterImpl.updateFileInfo(bug11367本地无视频-打开视频，画面一直显示在视频界面)-*/
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

    /**
     * 添加文件信息到文件列表
     *
     * @param mediaFile
     */
    private void addFourStringArray(File mediaFile) {
        String path = mediaFile.getAbsolutePath();
        String title = mediaFile.getName();
        title = title.substring(0, title.lastIndexOf("."));
        FourString musicFourString = new FourString(path, title, null, null);//根据null值同步歌曲信息不可以更改!2017/1/28
        //搜到第一首播放第一首
        if (mFourStringList.size() == 1 && playFirst) {
            if (mListInfoListener != null) {
//                mListInfoListener.playFileInfo(path);
            }
        }
        mFourStringList.add(musicFourString);
    }

    /**
     * 获取自定格式的所有文件
     *
     * @param path
     */
    public void getMediaFiles(final String path, final int dep) {
        File file = new File(path);
        final String[] filterString = mSearchPresenter.getFilter();
        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                boolean ret = false;

                if (file.isFile() && file.length() > 1024) {

                    if (filterString != null) {
                        for (int i = 0; i < filterString.length; i++) {
                            ret = name.endsWith(filterString[i]) || name.endsWith(filterString[i].toUpperCase());
                            if (ret) {
                                //LogUtils.d(TAG, "find　a file extension:" + name);
                                break;
                            }
                            if (isEnable) {
                                if (mListInfoListener != null) {
                                    mListInfoListener.updateSearchInfo(currentStorage, FileUtils.SearchState.SEARCH_STATE_ABORT);
                                }
                                LogUtils.d(TAG, "isCancelled:" + isEnable);
                                break;
                            }
                        }
                        if (ret) {
                            addFourStringArray(file);
                            return true;
                        }
                    }

                } else if (file.isDirectory()) {
                    if (!isEnable) {
                        if (dep < FILE_RECURSION_NUM) {
                            //LogUtils.e("dep:=="+dep);
//                            LogUtils.d(TAG, "find a Directory:" + file.getAbsolutePath() + ", file=" + file.getPath());
                            if (file.getAbsolutePath().toUpperCase().contains("/DVR") ||//ch010
                                    file.getAbsolutePath().toUpperCase().contains("IGO") ||
                                    file.getAbsolutePath().toUpperCase().contains("NAVIONE") ||//凯立德
                                    file.getAbsolutePath().contains("recordVideo")) {//ch009
                                LogUtils.e(TAG, "find a Directory:" + file.getAbsolutePath());
                            } else {
                                getMediaFiles(file.getAbsolutePath(), (dep + 1));
                            }
                        }
                    } else {
                        if (mListInfoListener != null) {
                            mListInfoListener.updateSearchInfo(currentStorage, FileUtils.SearchState.SEARCH_STATE_ABORT);
                        }
                    }
                }
                return false;
            }
        });
    }

    public void needCancel() {
        LogUtils.e(TAG, "needCancel");
        isEnable = true;
        interrupt();
    }
}
