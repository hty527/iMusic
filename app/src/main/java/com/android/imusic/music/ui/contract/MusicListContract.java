package com.android.imusic.music.ui.contract;

import com.android.imusic.base.BaseContract;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Presenter
 */

public interface MusicListContract {

    interface View extends BaseContract.BaseView{
        /**
         * 显示音频列表
         * @param data 音频列表
         */
        void showAudios(List<AudioInfo> data);

        /**
         * 显示专辑信息
         * @param data 专辑信息
         */
        void showAudiosFromTag(AlbumInfo data);
    }

    interface Presenter<V> extends BaseContract.BasePresenter<V>{

        //获取主页的音频列表
        void getIndexAudios();

        //根据TAG ID获取音频列表
        void getAudiosByTag(String tagID);
    }
}