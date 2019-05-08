package com.android.imusic.music.ui.presenter;

import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.model.MusicHistroyEngin;
import com.android.imusic.music.ui.contract.MusicHistroyContract;
import com.android.imusic.net.OkHttpUtils;
import com.music.player.lib.bean.BaseAudioInfo;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/5/8
 * Histroy\Collect Music Presenter
 */

public class MusicHistroyPersenter extends BasePresenter<MusicHistroyContract.View,MusicHistroyEngin>
        implements MusicHistroyContract.Presenter<MusicHistroyContract.View>{

    @Override
    protected MusicHistroyEngin createEngin() {
        return new MusicHistroyEngin();
    }

    /**
     * 获取历史播放记录
     */
    @Override
    public void getHistroyAudios() {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();
            getNetEngin().get().getMusicsByHistroy(new OkHttpUtils.OnResultCallBack<List<BaseAudioInfo>>() {

                @Override
                public void onResponse(List<BaseAudioInfo> data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showAudios(data);
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
     * 获取收藏记录
     */
    @Override
    public void getCollectAudios() {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();
            getNetEngin().get().getMusicsByCollect(new OkHttpUtils.OnResultCallBack<List<BaseAudioInfo>>() {

                @Override
                public void onResponse(List<BaseAudioInfo> data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showAudios(data);
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