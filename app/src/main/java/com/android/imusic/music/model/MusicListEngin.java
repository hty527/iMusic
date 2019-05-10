package com.android.imusic.music.model;

import android.content.Context;
import android.os.AsyncTask;

import com.android.imusic.base.BaseEngin;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.net.OnResultCallBack;
import com.music.player.lib.bean.BaseAudioInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Music Model
 */

public class MusicListEngin extends BaseEngin {

    /**
     * 获取音频列表
     * @param callBack 回调监听器
     */
    public void getAudios(OnResultCallBack callBack){
        sendGetRequst("https://gitee.com/hty_Yuye/OpenFile/raw/master/index/index_list.json",null,callBack);
    }

    /**
     * 根据TAG ID获取音频列表
     * @param tagID tagid
     * @param callBack 回调监听器
     */
    public void getAudiosByTag(String tagID,OnResultCallBack callBack){
        sendGetRequst("https://gitee.com/hty_Yuye/OpenFile/raw/master/index/"+tagID+".json",null,callBack);
    }

    /**
     * 获取音频列表
     * @param callBack 回调监听器
     */
    public void getLocationAudios(final Context context, final OnResultCallBack callBack){
        //如果本地已存在，不再重复查询
        List<BaseAudioInfo> audioInfos = MediaUtils.getInstance().getLocationMusic();
        if(null!=audioInfos&&audioInfos.size()>0){
            if(null!=callBack){
                List<BaseAudioInfo> medias=new ArrayList<>();
                medias.addAll(audioInfos);
                callBack.onResponse(medias);
            }
            return;
        }
        mContext=context;
        new AsyncTask<Void, Void, List<BaseAudioInfo>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected List<BaseAudioInfo> doInBackground(final Void... unused) {
                ArrayList<BaseAudioInfo> audioInfos = MediaUtils.getInstance().queryLocationMusics(context);
                return audioInfos;
            }

            @Override
            protected void onPostExecute(List<BaseAudioInfo> data) {
                if(null!=callBack){
                    if(null!=data){
                        callBack.onResponse(data);
                    }else{
                        callBack.onError(API_RESULT_EMPTY,API_EMPTY);
                    }
                }
            }
        }.execute();
    }
}