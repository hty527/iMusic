package com.android.imusic.music.ui.presenter;

import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.bean.ResultData;
import com.android.imusic.music.bean.SearchResult;
import com.android.imusic.music.model.MusicSearchEngin;
import com.android.imusic.music.net.MusicNetUtils;
import com.android.imusic.music.ui.contract.MusicSearchContract;
import java.lang.reflect.Type;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * Search Presenter
 */

public class MusicSearchPersenter extends BasePresenter<MusicSearchContract.View,MusicSearchEngin> implements MusicSearchContract.Presenter<MusicSearchContract.View>{

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
        if(null!=mNetEngin){
            if(null!=mView){
                mView.showLoading();
            }
            mNetEngin.queryMusicToKey(key, page, new MusicNetUtils.OnRequstCallBack<SearchResult>() {
                @Override
                public void onResponse(ResultData<SearchResult> data) {
                    if(null!=mView){
                        if(null!=data.getData()){
                            mView.showResult(data.getData());
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mView){
                        mView.showError(code,errorMsg);
                    }
                }
            });
        }
    }

    /**
     * 根据HashKey获取播放地址
     * @param hashKey Music hashKay
     * @param type Data Type
     * @param callBack 回调
     */
    @Override
    public void getPathBkKey(String hashKey, Type type, MusicNetUtils.OnRequstCallBack callBack) {
        if(null!=mNetEngin){
            if(null!=mView){
                mView.showLoading();
            }
            mNetEngin.getPathBkKey(hashKey, type,callBack);
        }
    }
}