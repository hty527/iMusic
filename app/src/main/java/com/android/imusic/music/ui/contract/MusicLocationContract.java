package com.android.imusic.music.ui.contract;

import android.content.Context;
import com.android.imusic.base.BaseContract;
import com.android.imusic.music.bean.AudioInfo;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Location Music Contract
 */

public interface MusicLocationContract {

    interface View extends BaseContract.BaseView{
        /**
         * 显示音频列表
         * @param data 收藏、历史播放 记录
         */
        void showAudios(List<AudioInfo> data);
    }

    interface Presenter<V> extends BaseContract.BasePresenter<V>{

        /**
         * 查询本机音乐
         * @param context Activity上下文
         */
        void getLocationAudios(Context context);
    }
}