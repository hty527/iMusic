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
     * 根据key获取播放地址,此酷狗API坏掉了。。。2019-05-11
     * @param hashKey key
     * @param callBack 回调
     */
    public void getPathBkKey(String hashKey,OnResultCallBack callBack){
        Map<String ,String> params=new HashMap<>();
        params.put("r","play/getdata");
        params.put("hash",hashKey);
        Map<String ,String> headers=new HashMap<>();
        headers.put("Access-Control-Allow-Origin","*");
        headers.put("Accept","*/*");
        headers.put("Cookie", "kg_mid=8a18832e9fc0845106e1075df481c1c2;Hm_lvt_aedee6983d4cfc62f509129360d6bb3d=1557584633");
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0");
        sendGetRequst("http://www.kugou.com/yy/index.php",params,headers,callBack);
    }
}