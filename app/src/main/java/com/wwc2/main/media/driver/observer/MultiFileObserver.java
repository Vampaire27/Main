package com.wwc2.main.media.driver.observer;

import android.os.FileObserver;
import android.text.TextUtils;

import com.wwc2.corelib.utils.log.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

/**
 * multi file observer.
 *
 * @author wwc2
 */
public class MultiFileObserver extends FileObserver {
    private String TAG = MultiFileObserver.class.getSimpleName();

    /**
     * Only modification events
     */
    public static final int CHANGES_ONLY = CREATE | MODIFY | DELETE | CLOSE_WRITE
            | DELETE_SELF | MOVE_SELF | MOVED_FROM | MOVED_TO;

    private List<SingleFileObserver> mObservers;
    private String mPath;
    private int mMask;
    private int mStorageId;
    private FileListener mFileListener;
    private boolean accOff = false;
    private boolean hasInit = false;
    private String oldName = null;


    public void setFileListener(FileListener mFileListener) {
        this.mFileListener = mFileListener;
    }

    public MultiFileObserver(String path) {
        this(path, ALL_EVENTS);
    }

    public MultiFileObserver(String path, int mask) {
        super(path, mask);
        mPath = path;
        mMask = mask;
    }

    /**
     * @param storageId 存储对应ID
     * @param path
     * @param mask
     */
    public MultiFileObserver(int storageId, String path, int mask) {
        this(path, mask);
        mStorageId = storageId;
        accOff = false;
        hasInit = false;
    }

    @Override
    public synchronized void startWatching() {
        if (mObservers != null)
            return;

        mObservers = new ArrayList<>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.isEmpty() && (!accOff)) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (null == files)
                continue;
            for (File f : files) {
                if (f.isDirectory() && !f.getName().equals(".")
                        && !f.getName().equals("..")) {
                    if (accOff) {
                        break;
                    }
//                	LogUtils.d(TAG, "startWatching Paths:" + f.getPath());
                    stack.push(f.getPath());
                }
            }
        }

        for (int i = 0; i < mObservers.size(); i++) {
            SingleFileObserver sfo = mObservers.get(i);
            if (accOff) {
                break;
            }
            sfo.startWatching();
        }
        if (!accOff) {
            hasInit = true;
            LogUtils.d(TAG, " startWatching hasInit:" + hasInit);
        }
    }

    @Override
    public synchronized void stopWatching() {
        hasInit = false;
        if (mObservers == null)
            return;

        for (int i = 0; i < mObservers.size(); i++) {
            SingleFileObserver sfo = mObservers.get(i);
            sfo.stopWatching();
        }

        mObservers.clear();
        mObservers = null;
    }


    @Override
    public void onEvent(int event, String path) {
        switch (event) {
            case FileObserver.ACCESS:
                LogUtils.i("RecursiveFileObserver", "ACCESS: " + path);
                break;
            case FileObserver.ATTRIB:
                LogUtils.i("RecursiveFileObserver", "ATTRIB: " + path);
                break;
            case FileObserver.CLOSE_NOWRITE:
                LogUtils.i("RecursiveFileObserver", "CLOSE_NOWRITE: " + path);
                break;
            case FileObserver.CLOSE_WRITE:
//            LogUtils.i("RecursiveFileObserver", "CLOSE_WRITE: " + path);
                break;
            case FileObserver.CREATE:
                LogUtils.i("RecursiveFileObserver", "CREATE: " + path);
                create(path);
                if (mFileListener != null) {
                    mFileListener.onFileCreated(mStorageId, path);
                }
                break;
            case FileObserver.DELETE:
                LogUtils.i("RecursiveFileObserver", "DELETE: " + path);
                delete(path);
                if (mFileListener != null) {
                    mFileListener.onFileDeleted(mStorageId, path);
                }
                break;
            case FileObserver.DELETE_SELF:
                LogUtils.i("RecursiveFileObserver", "DELETE_SELF: " + path);
                delete(path);
                break;
            case FileObserver.MODIFY:
//            LogUtils.i("RecursiveFileObserver", "MODIFY: " + path);
                if (mFileListener != null) {
                    mFileListener.onFileModified(mStorageId, path);
                }
                break;
            case FileObserver.MOVE_SELF:
                LogUtils.i("RecursiveFileObserver", "MOVE_SELF: " + path);
                break;
            case FileObserver.MOVED_FROM:
                LogUtils.i("RecursiveFileObserver", "MOVED_FROM: " + path);
                break;
            case FileObserver.MOVED_TO:
                LogUtils.i("RecursiveFileObserver", "MOVED_TO: " + path);
                break;
            case FileObserver.OPEN:
                LogUtils.i("RecursiveFileObserver", "OPEN: " + path);
                break;
            default:
//            LogUtils.i("RecursiveFileObserver", "DEFAULT(0x" + Integer.toString(event, 16) + " : " + path);
                break;
        }
    }

    /**
     * create the path
     *
     * @param path the path
     */
    private void create(String path) {
        boolean isDir = false;
        if (FileType.FILE_TYPE_DIR == FileType.checkFile(path)) {
            isDir = true;
        }

        if (isDir) {
            SingleFileObserver fileObserver = new SingleFileObserver(path, mMask);
            mObservers.add(fileObserver);
            fileObserver.startWatching();
            LogUtils.d("RecursiveFileObserver", "startWatching: " + path);
        }
    }

    /**
     * delete the path
     *
     * @param path the path
     */
    private void delete(String path) {
        boolean isDir = false;
        if (FileType.FILE_TYPE_DIR == FileType.checkFile(path)) {
            isDir = true;
        }

        if (isDir) {
            if (null != mObservers && !TextUtils.isEmpty(path)) {
                for (int i = 0; i < mObservers.size(); i++) {
                    final SingleFileObserver fileObserver = mObservers.get(i);
                    if (null != fileObserver) {
                        if (path.equals(fileObserver.getPath())) {
                            fileObserver.stopWatching();
                            try {
                                mObservers.remove(i);
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                            LogUtils.d("RecursiveFileObserver", "stopWatching: " + path);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Monitor single directory and dispatch all events to its parent, with full
     * path.
     */
    class SingleFileObserver extends FileObserver {
        String mPath;
        Hashtable<Integer, String> mRenameCookies = new Hashtable<Integer, String>();

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }


        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath;
            if (null != path) {
                if (null != newPath) {
                    if (isEndAdd(newPath)) {
                        newPath += "/" + path;
                    } else {
                        newPath += path;
                    }
                }
            }

            switch (event & ALL_EVENTS) {
                case FileObserver.MOVED_FROM:
                    oldName = newPath;
                    LogUtils.e(TAG, "storage RenameOld" + newPath);
                    mRenameCookies.put(FileObserver.MOVED_FROM, newPath);
                    break;
                case FileObserver.MOVED_TO:
                    //LogUtils.e(TAG, "storage RenameNew" + newPath);
                    //String oldName = mRenameCookies.remove(FileObserver.MOVED_FROM); oldName value is null。
                    //LogUtils.e(TAG, "storage oldName" + oldName);
                    if (mFileListener != null) {
                        mFileListener.onFileRenamed(mStorageId, oldName, newPath);
                    }
                    break;
            }
            MultiFileObserver.this.onEvent(event & ALL_EVENTS, newPath);
        }

        /**
         * get the path
         *
         * @return the path
         */
        public String getPath() {
            return mPath;
        }


        /**
         * is the string end add
         *
         * @param string the string
         * @return
         */
        private boolean isEndAdd(String string) {
            boolean ret = true;
            if (!TextUtils.isEmpty(string)) {
                final int length = string.length();
                if (length > 2) {
                    final String sub = string.substring(length - 1, length);
                    if ("/".equals(sub)) {
                        ret = false;
                    }
                }
            }
            return ret;
        }
    }

    public void stop(boolean isAccOff) {
        accOff = isAccOff;
    }

    public void setHasInit(boolean hasInit) {
        this.hasInit = hasInit;
    }

    public boolean hasInitComplete() {
        LogUtils.d(TAG, " hasInitComplete hasInit:" + hasInit);
        return hasInit;
    }

    public enum FileType {
        FILE_TYPE_DIR, FILE_TYPE_MEDIA, FILE_TYPE_FILE;

        public static FileType checkFile(String path) {
            File mFile = new File(path);
            if (mFile.isDirectory()) {
                return FILE_TYPE_DIR;
            }
            return FILE_TYPE_FILE;
        }
    }

    public interface FileListener {
        /**
         * 文件创建
         */
        void onFileCreated(int storageId, String name);

        /**
         * 文件删除
         */
        void onFileDeleted(int storageId, String name);

        /**
         * 文件修改
         */
        void onFileModified(int storageId, String name);

        /**
         * 文件重命名
         */
        void onFileRenamed(int storageId, String oldName, String newName);
    }

}
