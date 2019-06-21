package com.android.imusic.music.model;

import android.content.Context;
import com.android.imusic.base.BaseEngin;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.net.OnResultCallBack;
import java.util.ArrayList;
import java.util.List;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Location Music Model
 */

public class MusicLocationEngin extends BaseEngin {

    private Subscription mSubscribe;

    /**
     * 获取音频列表
     * @param callBack 回调监听器
     */
    public void getLocationAudios(final Context context, final OnResultCallBack callBack){
        //内存中已经存在本地歌曲列表，不再重复查询
        List<AudioInfo> audioInfos = MediaUtils.getInstance().getLocationMusic();
        if(null!=audioInfos){
            if(null!=callBack){
                List<AudioInfo> medias=new ArrayList<>();
                medias.addAll(audioInfos);
                if(medias.size()>0){
                    callBack.onResponse(medias);
                }else{
                    callBack.onError(API_RESULT_EMPTY,API_EMPTY);
                }
            }
            return;
        }
        mContext=context;
        mSubscribe =rx.Observable
                .just(context)
                .map(new Func1<Context, List<AudioInfo>>() {
                    @Override
                    public List<AudioInfo> call(Context cts) {
                        return MediaUtils.getInstance().queryLocationMusics(cts);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<AudioInfo>>() {
                    @Override
                    public void call(List<AudioInfo> data) {
                        if(null!=data&&data.size()>0){
                            callBack.onResponse(data);
                        }else{
                            callBack.onError(API_RESULT_EMPTY,API_EMPTY);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mSubscribe){
            mSubscribe.unsubscribe();
            mSubscribe=null;
        }
    }
}