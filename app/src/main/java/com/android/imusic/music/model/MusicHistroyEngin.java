package com.android.imusic.music.model;

import com.android.imusic.base.BaseEngin;
import com.android.imusic.net.OnResultCallBack;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.util.MusicUtils;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Histroy Music Model
 */

public class MusicHistroyEngin extends BaseEngin {

    /**
     * 获取历史播放记录
     * @param callBack 回调监听器
     */
    public void getMusicsByHistroy(final OnResultCallBack callBack){
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<BaseAudioInfo> audioInfos = MusicUtils.getInstance().getMusicsByHistroy();
                if(null!=callBack){
                    if(null!=audioInfos&&audioInfos.size()>0){
                        callBack.onResponse(audioInfos);
                    }else{
                        callBack.onError(API_RESULT_EMPTY,"播放记录空空如也");
                    }
                }
            }
        },100);
    }

    /**
     * 获取收藏记录
     * @param callBack 回调监听器
     */
    public void getMusicsByCollect(final OnResultCallBack callBack){
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<BaseAudioInfo> audioInfos = MusicUtils.getInstance().getMusicsByCollect();
                if(null!=callBack){
                    if(null!=audioInfos&&audioInfos.size()>0){
                        callBack.onResponse(audioInfos);
                    }else{
                        callBack.onError(API_RESULT_EMPTY,"收藏记录空空如也");
                    }
                }
            }
        },100);
    }
}