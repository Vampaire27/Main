package com.wwc2.main.media;

import com.wwc2.corelib.listener.BaseListener;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.FourString;
import com.wwc2.corelib.model.custom.IntegerString;

/**
 * the media listener.
 *
 * @author wwc2
 * @date 2017/1/12
 */
public class MediaListener extends BaseListener {

    @Override
    public String getClassName() {
        return MediaListener.class.getName();
    }

    //******************已优化代码，暂保留2018-12-27 begin*******************//
    /**文件列表监听器*/
    public void FileListListener(String[] oldVal, String[] newVal) {

    }
    //******************已优化代码，暂保留2018-12-27 end*******************//

    /**播放模式监听器, see {@link com.wwc2.media_interface.MediaDefine.PlayMode}*/
    public void PlayModeListener(Integer oldVal, Integer newVal) {

    }

    /**当前播放时间监听器*/
    public void CurrentTimeListener(Integer oldVal, Integer newVal) {

    }

    /**媒体总时间监听器*/
    public void TotalTimeListener(Integer oldVal, Integer newVal) {

    }

    /**播放状态监听器, see {@link com.wwc2.media_interface.MediaDefine.PlayStatus}*/
    public void PlayStatusListener(Integer oldVal, Integer newVal) {

    }

    /**播放下标监听器, 从0开始*/
    public void IndexListener(Integer oldVal, Integer newVal) {

    }

    /**媒体列表总数监听器*/
    public void TotalListener(Integer oldVal, Integer newVal) {

    }

    /**播放文件全路径监听器*/
    public void FilePathListener(String oldVal, String newVal) {

    }

    /**视频尺寸监听器*/
    public void ScreenSizeListener(Integer oldVal, Integer newVal) {

    }

    /**媒体播放器错误状态监听器, see {@link com.wwc2.media_interface.MediaDefine.ErrorState}*/
    public void ErrorStatusListener(Integer oldVal, Integer newVal) {

    }
    /**媒体播放器错误状态监听器, see {@link com.wwc2.media_interface.MediaDefine.ErrorState}*/
    public void StorageErrorStatusListener(Integer oldVal, Integer newVal) {

    }

    /**存储设备列表监听器, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
    public void StorageListListener(Integer[] oldVal, Integer[] newVal) {

    }

    /**正在播放的存储设备监听器, see {@link com.wwc2.common_interface.utils.StorageDevice}*/
    public void PlayStorageListener(Integer oldVal, Integer newVal) {

    }

    /**当前显示的存储设备监听器*/
    public void CurrentStorageListener(Integer oldVal, Integer newVal) {

    }

    /**搜索的存储信息设备监听器, int storage, int state, boolean over, boolean auto*/
    public void SearchInfoListener(FourInteger[] oldVal, FourInteger[] newVal) {

    }

    /**音频标题名称监听器*/
    public void TitleListener(String oldVal, String newVal) {

    }

    /**音频艺术家名称监听器*/
    public void ArtistListener(String oldVal, String newVal) {

    }

    /**音频专辑名称监听器*/
    public void AlbumListener(String oldVal, String newVal) {

    }

    /**音频专辑封面数据监听器*/
    public void AlbumDataListener(Byte[] oldVal, Byte[] newVal) {

    }

    /**音频歌词数据监听器*/
    public void LrcDataListener(IntegerString[] oldVal, IntegerString[] newVal) {

    }

    /**音频FFT频谱数据监听器*/
    public void FftDataListener(Byte[] oldVal, Byte[] newVal) {

    }

    /**音频WAVE频谱数据监听器*/
    public void WaveDataListener(Byte[] oldVal, Byte[] newVal) {

    }

    /**音频信息列表监听器, string path, string title, string artist, string album.*/
    public void ListInfoListener(FourString[] oldVal, FourString[] newVal) {

    }

    //******************已优化代码，暂保留2018-12-27 begin*******************//
    /**本地音频信息列表监听器, string path, string title, string artist, string album.*/
    public void NandFlashListInfo(FourString[] oldVal, FourString[] newVal) {

    }
    /**SdCard音频信息列表监听器, string path, string title, string artist, string album.*/
    public void SdListInfoListener(FourString[] oldVal, FourString[] newVal) {

    }
    /**Usb1音频信息列表监听器, string path, string title, string artist, string album.*/
    public void UsbListInfoListener(FourString[] oldVal, FourString[] newVal) {

    }

    /**Usb2音频信息列表监听器, string path, string title, string artist, string album.*/
    public void Usb1ListInfoListener(FourString[] oldVal, FourString[] newVal) {

    }
    /**Usb3音频信息列表监听器, string path, string title, string artist, string album.*/
    public void Usb2ListInfoListener(FourString[] oldVal, FourString[] newVal) {

    }
    /**Usb4音频信息列表监听器, string path, string title, string artist, string album.*/
    public void Usb3ListInfoListener(FourString[] oldVal, FourString[] newVal) {

    }
    //******************已优化代码，暂保留2018-12-27 end*******************//

    /**AccOn后是否在扫描记忆设备, 0：没有,1：正在扫描，2:未挂载*/
    public void StorageScanStatusListener(Integer oldVal, Integer newVal){}

    //******************优化后新增代码2018-12-27 begin*******************//
    public void UpdateListInfoListener(Integer oldVal, Integer newVal) {}
    //******************优化后新增代码2018-12-27 end*******************//

    public void UpdateCurrentTime(Integer value) {

    }
}
