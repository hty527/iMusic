package com.android.imusic.music.model;

import com.android.imusic.base.BaseEngin;
import com.android.imusic.net.OnResultCallBack;
import java.util.HashMap;
import java.util.Map;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Search Music Model
 */

public class MusicSearchEngin extends BaseEngin {

    /**
     * 根据key检索音乐列表
     * @param key key
     * @param page 页眉
     * @param callBack 回调
     */
    public void queryMusicToKey(String key,int page,OnResultCallBack callBack){
        Map<String ,String> params=new HashMap<>();
        params.put("format","json");
        params.put("keyword",key);
        params.put("page",page+"");
        params.put("pagesize","20");
        params.put("showtype","1");
        sendGetRequst("http://mobilecdn.kugou.com/api/v3/search/song",params,callBack);
    }

    /**
     * 根据key获取播放地址
     * @param hashKey key
     * @param callBack 回调
     */
    public void getPathBkKey(String hashKey,OnResultCallBack callBack){
        Map<String ,String> params=new HashMap<>();
        params.put("r","play/getdata");
        params.put("hash",hashKey);
        sendGetRequst("http://www.kugou.com/yy/index.php",params,callBack);
    }
}