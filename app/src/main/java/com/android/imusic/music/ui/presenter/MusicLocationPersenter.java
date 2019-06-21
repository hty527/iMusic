package com.android.imusic.music.ui.presenter;

import android.content.Context;
import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.model.MusicLocationEngin;
import com.android.imusic.music.ui.contract.MusicLocationContract;
import com.android.imusic.net.OnResultCallBack;
import com.music.player.lib.bean.BaseAudioInfo;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/5/8
 * Location Music Presenter
 */

public class MusicLocationPersenter extends BasePresenter<MusicLocationContract.View,MusicLocationEngin>
        implements MusicLocationContract.Presenter<MusicLocationContract.View>{

    @Override
    protected MusicLocationEngin createEngin() {
        return new MusicLocationEngin();
    }

    /**
     * 查询本机音乐
     * @param context Activity上下文
     */
    @Override
    public void getLocationAudios(Context context) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();
            getNetEngin().get().getLocationAudios(context,new OnResultCallBack<List<AudioInfo>>() {

                @Override
                public void onResponse(List<AudioInfo> data) {
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