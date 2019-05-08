package com.android.imusic.music.ui.contract;

import android.content.Context;
import com.android.imusic.base.BaseContract;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import com.music.player.lib.bean.BaseAudioInfo;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Contract
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

        /**
         * 显示本地音频列表
         * @param data 本地音频列表
         */
        void showLocationAudios(List<BaseAudioInfo> data);
    }

    interface Presenter<V> extends BaseContract.BasePresenter<V>{

        /**
         * 获取主页的音频列表
         */
        void getIndexAudios();

        /**
         * 根据TAG ID获取音频列表
         * @param tagID tagID
         */
        void getAudiosByTag(String tagID);

        /**
         * 查询本机音乐
         * @param context Activity上下文
         */
        void getLocationAudios(Context context);
    }
}