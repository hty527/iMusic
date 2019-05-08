package com.android.imusic.music.ui.presenter;

import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.bean.ResultData;
import com.android.imusic.music.bean.SearchMusicData;
import com.android.imusic.music.bean.SearchResult;
import com.android.imusic.music.bean.SearchResultInfo;
import com.android.imusic.music.model.MusicSearchEngin;
import com.android.imusic.net.OkHttpUtils;
import com.android.imusic.music.ui.contract.MusicSearchContract;

/**
 * TinyHung@Outlook.com
 * 2019/5/6
 * Search Presenter
 */

public class MusicSearchPersenter extends BasePresenter<MusicSearchContract.View,MusicSearchEngin>
        implements MusicSearchContract.Presenter<MusicSearchContract.View>{

    @Override
    protected MusicSearchEngin createEngin() {
        return new MusicSearchEngin();
    }

    /**
     * 根据KEY搜索在线音乐
     * @param key key
     * @param page 页眉
     */
    @Override
    public void queryMusicToKey(String key, int page) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();

            getNetEngin().get().queryMusicToKey(key, page, new OkHttpUtils.OnResultCallBack<ResultData<SearchResult>>() {

                @Override
                public void onResponse(ResultData<SearchResult> data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        if(null!=data.getData()){
                            mViewRef.get().showResult(data.getData());
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showError(code,errorMsg);
                    }
                }
            });
        }
    }

    /**
     * 根据HashKey获取播放地址
     * @param position ITEM Position
     * @param item ITEM
     * @param hashKey Music hashKay
     */
    @Override
    public void getPathBkKey(final int position, final SearchResultInfo item, String hashKey) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();

            getNetEngin().get().getPathBkKey(hashKey,new OkHttpUtils.OnResultCallBack<ResultData<SearchMusicData>>() {

                @Override
                public void onResponse(ResultData<SearchMusicData> data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        if(null!=data.getData()){
                            mViewRef.get().showAudioData(position,item,data.getData());
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showError(code,errorMsg);
                    }
                }
            });
        }
    }
}