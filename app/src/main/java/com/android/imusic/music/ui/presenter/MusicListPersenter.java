package com.android.imusic.music.ui.presenter;

import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.bean.ResultData;
import com.android.imusic.music.bean.ResultList;
import com.android.imusic.music.model.MusicListEngin;
import com.android.imusic.music.net.MusicNetUtils;
import com.android.imusic.music.ui.contract.MusicListContract;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 */

public class MusicListPersenter extends BasePresenter<MusicListContract.View,MusicListEngin>
        implements MusicListContract.Presenter<MusicListContract.View>{

    @Override
    protected MusicListEngin createEngin() {
        return new MusicListEngin();
    }

    /**
     * 获取主页音频列表
     */
    @Override
    public void getIndexAudios() {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();
            getNetEngin().get().getAudios(new MusicNetUtils.OnRequstCallBack<ResultList<AudioInfo>>() {
                @Override
                public void onResponse(ResultData<ResultList<AudioInfo>> data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        if(null!=data.getData()&&null!=data.getData().getList()
                                &&data.getData().getList().size()>0){
                            mViewRef.get().showAudios(data.getData().getList());
                        }else{
                            mViewRef.get().showError(data.getCode(),data.getMsg());
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
     * 根据TAG ID获取音频列表
     * @param tagID
     */
    @Override
    public void getAudiosByTag(String tagID) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();
            getNetEngin().get().getAudiosByTag(tagID,new MusicNetUtils.OnRequstCallBack<AlbumInfo>() {
                @Override
                public void onResponse(ResultData<AlbumInfo> data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        if(null!=data.getData()){
                            mViewRef.get().showAudiosFromTag(data.getData());
                        }else{
                            mViewRef.get().showError(data.getCode(),data.getMsg());
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