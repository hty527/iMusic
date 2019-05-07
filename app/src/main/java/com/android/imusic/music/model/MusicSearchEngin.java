package com.android.imusic.music.model;

import com.android.imusic.music.bean.ResultData;
import com.android.imusic.music.bean.SearchMusicData;
import com.android.imusic.music.bean.SearchResult;
import com.android.imusic.music.net.MusicNetUtils;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 */

public class MusicSearchEngin extends MusicNetUtils {

    /**
     * 根据key检索音乐列表
     * @param key key
     * @param page 页眉
     * @param callBack 回调
     */
    public void queryMusicToKey(String key,int page,OnRequstCallBack callBack){
        queryMusicToKey(key,page,new TypeToken<ResultData<SearchResult>>() {}.getType(),callBack);
    }

    /**
     * 根据key获取播放地址
     * @param hashKey key
     * @param callBack 回调
     */
    public void getPathBkKey(String hashKey,OnRequstCallBack callBack){
        getPathBkKey(hashKey,new TypeToken<ResultData<SearchMusicData>>() {}.getType(),callBack);
    }

    /**
     * 根据关键字检索音频信息
     * @param key 关键字
     * @param callBack
     */
    public void queryMusicToKey(String key, int page, Type type, OnRequstCallBack callBack){
        requstApi("http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword="+key
                +"&page="+page+"&pagesize=20&showtype=1",type,callBack);
    }

    /**
     * 根据HashKey获取播放地址
     * @param hashKey
     * @param type
     * @param callBack
     */
    public void getPathBkKey(String hashKey, Type type,OnRequstCallBack callBack){
        requstApi("http://www.kugou.com/yy/index.php?r=play/getdata&hash="+hashKey,type,callBack);
    }
}