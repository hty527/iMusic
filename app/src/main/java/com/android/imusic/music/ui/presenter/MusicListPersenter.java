package com.android.imusic.music.ui.presenter;

import android.content.Context;
import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.utils.DataFactory;
import com.android.imusic.net.bean.ResultData;
import com.android.imusic.music.model.MusicListEngin;
import com.android.imusic.music.ui.contract.MusicListContract;
import com.android.imusic.net.OnResultCallBack;
import com.music.player.lib.bean.BaseAudioInfo;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/5/6
 * Index Music Presenter
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
            mViewRef.get().showAudios(DataFactory.getInstance().getIndexMusic());
//            mViewRef.get().showLoading();
//            getNetEngin().get().getAudios(new OnResultCallBack<ResultData<ResultList<AudioInfo>>>() {
//
//                @Override
//                public void onResponse(ResultData<ResultList<AudioInfo>> data) {
//                    if(null!=mViewRef&&null!=mViewRef.get()){
//                        if(null!=data.getData()&&null!=data.getData().getList()
//                                &&data.getData().getList().size()>0){
//                            mViewRef.get().showAudios(data.getData().getList());
//                        }else{
//                            mViewRef.get().showError(data.getCode(),data.getErr());
//                        }
//                    }
//                }
//
//                @Override
//                public void onError(int code, String errorMsg) {
//                    if(null!=mViewRef&&null!=mViewRef.get()){
//                        mViewRef.get().showError(code,errorMsg);
//                    }
//                }
//            });
        }
    }

    /**
     * 根据TAG ID获取音频列表
     * @param tagID
     */
    @Override
    public void getAudiosByTag(String tagID) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showAudiosFromTag(DataFactory.getInstance().getMusicByAlbum(tagID));
//            mViewRef.get().showLoading();
//            getNetEngin().get().getAudiosByTag(tagID,new OnResultCallBack<ResultData<AlbumInfo>>() {
//
//                @Override
//                public void onResponse(ResultData<AlbumInfo> data) {
//                    if(null!=mViewRef&&null!=mViewRef.get()){
//                        if(null!=data.getData()){
//                            mViewRef.get().showAudiosFromTag(data.getData());
//                        }else{
//                            mViewRef.get().showError(data.getCode(),data.getErr());
//                        }
//                    }
//                }
//
//                @Override
//                public void onError(int code, String errorMsg) {
//                    if(null!=mViewRef&&null!=mViewRef.get()){
//                        mViewRef.get().showError(code,errorMsg);
//                    }
//                }
//            });
        }
    }

    /**
     * 查询本地音频列表
     * @param context Activity上下文
     */
    @Override
    public void getLocationAudios(Context context) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            getNetEngin().get().getLocationAudios(context,new OnResultCallBack<List<BaseAudioInfo>>() {

                @Override
                public void onResponse(List<BaseAudioInfo> data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showLocationAudios(data);
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