package com.android.imusic.music.ui.contract;

import com.android.imusic.base.BaseContract;
import com.android.imusic.music.bean.SearchMusicData;
import com.android.imusic.music.bean.SearchResult;
import com.android.imusic.music.bean.SearchResultInfo;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Contract
 */

public interface MusicSearchContract {

    interface View extends BaseContract.BaseView{
        /**搜索结果音频列表
         * @param data 搜索结果
         */
        void showResult(SearchResult data);

        /**
         * 音频详细信息
         * @param position ITEM Position
         * @param item 音频相关的ITEM
         * @param data 音频信息
         */
        void showAudioData(int position,SearchResultInfo item,SearchMusicData data);

        /**
         * 获取音频信息失败、为空
         * @param code 错误码
         * @param errorMsg 描述信息
         */
        void showAudioDataError(int code,String errorMsg);
    }

    interface Presenter<V> extends BaseContract.BasePresenter<V>{
        /**
         * 根据Key搜索音乐
         * @param key key
         * @param page 页眉
         */
        void queryMusicToKey(String key,int page);

        /**
         * 根据hashKey获取播放地址
         * @param position ITEM Position
         * @param item ITEM
         * @param hashKey Music hashKay
         */
        void getPathBkKey(int position,SearchResultInfo item,String hashKey);
    }
}