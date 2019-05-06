package com.android.imusic.music.model;

import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.bean.ResultData;
import com.android.imusic.music.bean.ResultList;
import com.android.imusic.music.net.MusicNetUtils;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 */

public class MusicListEngin extends MusicNetUtils {
    /**
     * 获取音频列表
     * @param callBack 回调监听器
     */
    public void getAudios(OnRequstCallBack callBack){
        getIndexMusicList(new TypeToken<ResultData<ResultList<AudioInfo>>>(){}.getType(),callBack);
    }

    /**
     * 根据TAG ID获取音频列表
     * @param tagID tagid
     * @param callBack 回调监听器
     */
    public void getAudiosByTag(String tagID,OnRequstCallBack callBack){
        getMusicListsByTag(tagID,new TypeToken<ResultData<AlbumInfo>>(){}.getType(),callBack);
    }

    /**
     * 获取首页音乐数据
     * @param type
     * @param callBack
     */
    public void getIndexMusicList(Type type, OnRequstCallBack callBack){
        requstApi("https://gitee.com/hty_Yuye/OpenFile/raw/master/index/index_list.json",type,callBack);
    }

    /**
     * 根据TAG获取音乐列表
     * http://www.9ku.com/geshou/798.htm
     * http://www.9ku.com/
     * @param callBack
     */
    public void getMusicListsByTag(String tag,Type type, OnRequstCallBack callBack){
        requstApi("https://gitee.com/hty_Yuye/OpenFile/raw/master/index/"+tag+".json",type,callBack);
    }
}