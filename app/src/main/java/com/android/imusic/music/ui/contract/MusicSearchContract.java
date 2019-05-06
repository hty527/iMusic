package com.android.imusic.music.ui.contract;

import com.android.imusic.base.BaseContract;
import com.android.imusic.music.bean.SearchResult;
import com.android.imusic.music.net.MusicNetUtils;
import java.lang.reflect.Type;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Presenter
 */

public interface MusicSearchContract {

    interface View extends BaseContract.BaseView{
        /**搜索结果音频列表
         * @param data 搜索结果
         */
        void showResult(SearchResult data);
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
         * @param hashKey Music hashKay
         * @param type Data Type
         * @param callBack 回调
         */
        void getPathBkKey(String hashKey, Type type, MusicNetUtils.OnRequstCallBack callBack);
    }
}