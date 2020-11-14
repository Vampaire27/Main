package com.wwc2.main.media.driver.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.mpatric.mp3agic.Mp3File;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.media.driver.mediable.SuperMediaDriverable;
import com.wwc2.main.media.driver.utils.Id3Synchronous;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huwei on 2017/1/13.
 */
public class AudioPresenterImpl implements SuperMediaDriverable.Audio.AudioPresenter {
    private String TAG = AudioPresenterImpl.class.getSimpleName();
    private SuperMediaDriverable.Audio.AudioDriverable mAudioDriverable;
//    private int mPoolSize = 3;
//    private ExecutorService executorService;
//    private MusicInfoTask mMusicInfoTask;
//    private Context mContext = null;
//    private MediaScanner mScanner = null;
//    private MediaMetadataRetriever retriever = null;
//    private Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//    final String[] mCursorCols = new String[]{
//            MediaStore.Audio.Media._ID, // index must match IDCOLIDX below
//            MediaStore.Audio.Media.ARTIST,
//            MediaStore.Audio.Media.ALBUM,
//            MediaStore.Audio.Media.TITLE
//            MediaStore.Audio.Media.DISPLAY_NAME//文件名和ID3title同步，所以取文件名
//            MediaStore.Audio.Media.DATA,
//            MediaStore.Audio.Media.MIME_TYPE,
//            MediaStore.Audio.Media.ALBUM_ID,
//            MediaStore.Audio.Media.ARTIST_ID,
//            MediaStore.Audio.Media.IS_PODCAST, // index must match PODCASTCOLIDX
//            MediaStore.Audio.Media.BOOKMARK // index must match BOOKMARKCOLIDX
//    };

    private Map<Integer, Id3Synchronous> mId3Synchronouses = new ConcurrentHashMap<>();

    public AudioPresenterImpl(Context context, SuperMediaDriverable.Audio.AudioDriverable mAudioDriverable) {
        //mContext = context;
        //executorService = Executors.newFixedThreadPool(mPoolSize);
        this.mAudioDriverable = mAudioDriverable;

    }


    @Override
    public void findMusicInfo(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;
//        if(mMusicInfoTask != null && mMusicInfoTask.isAlive()){
//            mMusicInfoTask.stop();
//        }
//        mMusicInfoTask.interrupt();
        //mMusicInfoTask = new MusicInfoTask();//使用异步更新歌词(避免时长较长的歌曲切歌卡顿)
//        LogUtils.d(TAG, "updateMusicInfo filePath:" + filePath);
//        File musicFile = new File(filePath);
//        if (musicFile.exists()) {
//            if (!musicFile.isDirectory()) {
//                FourString musicFourString = mAudioDriverable.getMusicFourString(filePath);
//                LogUtils.d(TAG, "updateMusicInfo musicFourString getFourString artist:" + (musicFourString == null ? " null" : " " + musicFourString.getString2()));
        //优先更新专辑封面为空(有几率更新crash)
//                mAudioDriverable.updateAlbumData(filePath, null);
        //判断文件信息是否同步成功
//                if (musicFourString != null && musicFourString.getString3() != null) {
//                    LogUtils.d(TAG, "updateMusicInfo musicFourString getFourString from listInfo!");
//                    mAudioDriverable.updateFileDetail(musicFourString);
//                } else {
//                    //第一次更新
//                    if (musicFourString != null) {
//                        LogUtils.d(TAG, "updateMusicInfo musicFourString getFourString from init listInfo!");
//                        mAudioDriverable.updateFileDetail(new FourString(musicFourString.getString1(), musicFourString.getString2(), "unknown", "unknown"));
//                    }
//                    //启动文件搜索
//                    startId3Synchronous();
//                }
//            }
//        } else {
//            LogUtils.d(TAG, "play path is null!");
//        }
    }

    @Override
    public boolean getMusicId3Info(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;
        LogUtils.d(TAG, "getMusicId3Info filePath:" + filePath);
        File musicFile = new File(filePath);
        if (musicFile.exists()) {
            if (!musicFile.isDirectory()) {
//                try {
//                    //获取ID3信息
//                    mScanner = new MediaScanner(mContext);
//                    mUri = mScanner.scanSingleFile(filePath, "external", null);
//                    mScanner.release();
//                    if (mUri != null) {
//                        String sWhere = String.format("%s='%s'", MediaStore.Audio.Media.DATA, filePath);
//                        Cursor c = mContext.getContentResolver().query(mUri, mCursorCols, sWhere, null, null);
//                        if (c != null) {
//                            if (c.moveToFirst()) {
//                                //String name = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                                //String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());//避免乱码问题，取文件名
//                                String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
//                                if (TextUtils.isEmpty(name)) {
//                                    name = "unknown";
//                                } else {
//                                    if (filePath.lastIndexOf(".") > 1) {
//                                        name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
//                                    } else {
//                                        name = "unknown";
//                                    }
//                                    //name = changeMessyCode(name);
//                                    LogUtils.d("name:" + name);
//                                }
//                                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                                if (!TextUtils.isEmpty(artist)) {
//                                    LogUtils.d("artist:" + artist);
//                                    artist = changeMessyCode(artist);
//                                } else {
//                                    artist = "unknown";
//                                }
//                                String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//                                if (!TextUtils.isEmpty(album)) {
//                                    LogUtils.d("album:" + album);
//                                    album = changeMessyCode(album);
//                                } else {
//                                    album = "unknown";
//                                }
//                                mAudioDriverable.updateFileDetail(new FourString(filePath, name, artist, album));
//                                c.close();
//                                return true;
//                            } else {
//                                LogUtils.d(TAG, "getMusicId3Info c moveToFirst false");
//                            }
//                            c.close();
//                        } else {
//                            LogUtils.d(TAG, "getMusicId3Info c is null");
//                        }
//                    } else {
//                        LogUtils.d(TAG, "getMusicId3Info mUri is null");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                /*-begin-20180725-hzubin-modify-去掉歌曲名格式后缀(bug12188主页切换音乐的时候会显示一下“歌曲名.mp3”，然后显示“歌曲名”)-*/
                String track = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
                /*-end-20180725-hzubin-modify-去掉歌曲名格式后缀(bug12188主页切换音乐的时候会显示一下“歌曲名.mp3”，然后显示“歌曲名”)-*/
                LogUtils.d(TAG, "updateMusicInfo track:" + track);
                mAudioDriverable.updateFileDetail(new FourString(filePath, track, "unknown", "unknown"));
                LogUtils.d(TAG, "updateMusicInfo error filePath:" + filePath + ", track=" + track);
                return true;
            } else {
                LogUtils.d(TAG, "getMusicId3Info musicFile is directory");
            }
        } else {
            LogUtils.d(TAG, "getMusicId3Info musicFile exists");
        }

        return false;
    }

    @Override
    public FourString[] getMFourStringArrayById(int currentStorage) {
        return mAudioDriverable.getMFourStringArrayById(currentStorage);
    }

    @Override
    public void updateFourStringArray(int currentStorage, FourString[] fourStrings) {
        mAudioDriverable.updateFourStringArray(currentStorage, fourStrings);
    }

    @Override
    public void removeStorage(int storageId) {
//        Id3Synchronous mId3Synchronous = mId3Synchronouses.get(storageId);
//        if (mId3Synchronous != null) {
//            mId3Synchronous.needCancel();
//            mId3Synchronouses.remove(storageId);
//        }
        LogUtils.d(TAG, " exit " + StorageDevice.toString(storageId) + " Synchronous Id3 !");
    }

    //退出搜索
    @Override
    public void stop() {
//        for (Map.Entry<Integer, Id3Synchronous> entryId3 : mId3Synchronouses.entrySet()) {
//            entryId3.getValue().needCancel();
//            mId3Synchronouses.remove(entryId3.getKey());
//        }
        LogUtils.d(TAG, " exit all Synchronous Id3 !");
    }

    Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
        return bytes;

    }

    /**
     * 获取音频文件信息
     *
     * @param audioPath
     */
    @Override
    public FourString createFourString(String audioPath, String fileName, Mp3File mp3file) {
        String path = audioPath;
        String title = fileName;
        String artist = "unknown";
        String album = "unknown";
//        if (mp3file != null) {
        //文件支持id3v1和id3v2
//            if (mp3file.hasId3v1Tag() && mp3file.hasId3v2Tag()) {
//                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
//                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
//                LogUtils.d(TAG, "id3v2Tag Artist: " + id3v2Tag.getArtist());
//                LogUtils.d(TAG, "id3v2Tag Title: " + id3v2Tag.getTitle());
//                LogUtils.d(TAG, "id3v2Tag Album: " + id3v2Tag.getAlbum());
//
//                LogUtils.d(TAG, "id3v1Tag Artist: " + id3v1Tag.getArtist());
//                LogUtils.d(TAG, "id3v1Tag Title: " + id3v1Tag.getTitle());
//                LogUtils.d(TAG, "id3v1Tag Album: " + id3v1Tag.getAlbum());
//                String tagArtist = id3v2Tag.getArtist();
//                String tagTitle = id3v2Tag.getTitle();
//                String tagAlbum = id3v2Tag.getAlbum();
//                if (!textEmpty(tagArtist)) {
//                    artist = tagArtist;
//                }
//                if (!textEmpty(tagTitle)) {
//                    title = tagTitle;
//                }
//                if (!textEmpty(tagAlbum)) {
//                    album = tagAlbum;
//                }
//                //文件支持id3v1
//            } else if (mp3file.hasId3v1Tag() && !mp3file.hasCustomTag()) {
//                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
//                String tagArtist = id3v1Tag.getArtist();
//                String tagTitle = id3v1Tag.getTitle();
//                String tagAlbum = id3v1Tag.getAlbum();
//                if (!textEmpty(tagArtist)) {
//                    artist = tagArtist;
//                }
//                if (!textEmpty(tagTitle)) {
//                    title = tagTitle;
//                }
//                if (!textEmpty(tagAlbum)) {
//                    album = tagAlbum;
//                }
//                LogUtils.d(TAG, "id3v1Tag Artist: " + id3v1Tag.getArtist());
//                LogUtils.d(TAG, "id3v1Tag Title: " + id3v1Tag.getTitle());
//                LogUtils.d(TAG, "id3v1Tag Album: " + id3v1Tag.getAlbum());
        //文件支持id3v2()hasCustomTag()会乱码
//            } else if (mp3file.hasId3v2Tag() && !mp3file.hasCustomTag()) {
//                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
//                LogUtils.d(TAG, "id3v2Tag Artist: " + id3v2Tag.getArtist());
//                LogUtils.d(TAG, "id3v2Tag Title: " + id3v2Tag.getTitle());
//                LogUtils.d(TAG, "id3v2Tag Album: " + id3v2Tag.getAlbum());
//                String tagArtist = id3v2Tag.getArtist();
//                String tagTitle = id3v2Tag.getTitle();
//                String tagAlbum = id3v2Tag.getAlbum();
//                if (!textEmpty(tagArtist)) {
//                    artist = tagArtist;
//                }
//                if (!textEmpty(tagTitle)) {
//                    title = tagTitle;
//                }
//                if (!textEmpty(tagAlbum)) {
//                    album = tagAlbum;
//                }
//                if(TextUtils.isEmpty(title)){
//                    title = fileName;
//                    title = title.substring(0,title.lastIndexOf("."));
//                }
//            }
//        }
//            FourString musicFourString = new FourString(path,title,artist,album);
        return new FourString(path, title, artist, album);
    }


    public boolean textEmpty(String textString) {
        if (textString == null || TextUtils.isEmpty(textString) || "null".equals(textString)) {
            return true;
        }
        return false;
    }

    /**
     * 启动歌曲信息同步
     */
    private void startId3Synchronous() {
//        int playStorage = mAudioDriverable.getPlayStorageId();
//        LogUtils.d(TAG, " start Id3Synchronous path:" + StorageDevice.toString(playStorage));
//        if (!mId3Synchronouses.containsKey(playStorage)) {
//            Id3Synchronous mId3Synchronous = new Id3Synchronous(playStorage, this);
//            mId3Synchronouses.put(playStorage, mId3Synchronous);
//            //executorService.submit(mId3Synchronous);
//        } else {
//            Id3Synchronous mId3Synchronous = mId3Synchronouses.get(playStorage);
//            if (mId3Synchronous.needExe()) {
//                LogUtils.d(TAG, " second start Id3Synchronous path:" + StorageDevice.toString(playStorage) + " !");
//               // executorService.submit(mId3Synchronous);
//            }
//        }
    }

//    public class MusicInfoTask implements Runnable {
//        private FourString mFourString;
//        public void setFourString(FourString mFourString) {
//            this.mFourString = mFourString;
//        }
//        @Override
//        public void run() {
//            String filePath = null;
//            if (mFourString != null) {
//                filePath = mFourString.getString1();
//            }
//            if (TextUtils.isEmpty(filePath)) return;
//            Mp3File mMp3File = getMp3File(filePath);
//            //优先更新专辑封面为空
////            mAudioDriverable.updateAlbumData(filePath,null);
//            //判断文件信息是否同步成功
//            if (mFourString != null && mFourString.getString3() == null) {
//                mFourString = createFourString(mFourString.getString1(), mFourString.getString2(), mMp3File);
//                if (mFourString != null) {
//                    LogUtils.d(TAG, "updateMusicInfo musicFourString getFourString from createFourString!");
//                    mAudioDriverable.updateFileDetail(mFourString);
//                }
//            }
//        }
//    }

    public Mp3File getMp3File(String path) {
        Mp3File mMp3File = null;
//        try {
//            mMp3File = new Mp3File(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (UnsupportedTagException e) {
//            e.printStackTrace();
//        } catch (InvalidDataException e) {
//            e.printStackTrace();
//        } catch (OutOfMemoryError e) {
//            e.printStackTrace();
//        }
        return mMp3File;
    }

    public String getCoding(String str) {
        if (TextUtils.isEmpty(str)) {
            return "unknown";
        }
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是GB2312
                return encode;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是ISO-8859-1
                return encode;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是UTF-8
                return encode;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是GBK
                return encode;
            }
        } catch (Exception exception3) {
        }
        LogUtils.d("未知格式");
        return "unknown";        //如果都不是，说明输入的内容不属于常见的编码格式。
    }

    public String changeMessyCode(String str) {
        if (TextUtils.isEmpty(str)) {
            return "unknown";
        }
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是GB2312
                return str;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是ISO-8859-1
                return new String(str.getBytes("ISO8859_1"), "GB2312");
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是UTF-8
                return new String(str.getBytes("UTF-8"), "GB2312");
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是GBK
                return str;
            }
        } catch (Exception exception3) {
        }
        return "unknown";        //如果都不是，说明输入的内容不属于常见的编码格式。
    }
}
