package com.android.imusic.music.ui.contract;

import com.android.imusic.base.BaseContract;
import com.music.player.lib.bean.BaseAudioInfo;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Histroy Music Contract
 */

public interface MusicHistroyContract {

    interface View extends BaseContract.BaseView{
        /**
         * 显示历史、收藏音频列表
         * @param data 本地历史、收藏音频列表
         */
        void showAudios(List<BaseAudioInfo> data);
    }

    interface Presenter<V> extends BaseContract.BasePresenter<V>{

        /**
         * 获取历史播放记录
         */
        void getHistroyAudios();

        /**
         * 获取收藏记录
         */
        void getCollectAudios();
    }
}