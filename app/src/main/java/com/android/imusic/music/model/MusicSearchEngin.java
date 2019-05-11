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
        headers.put("Host","songsearch.kugou.com");
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0");
        headers.put("Accept","*/*");
        headers.put("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Accept-Encoding","gzip, deflate");
        headers.put("Referer","http://www.kugou.com/yy/html/search.html");
        headers.put("Cookie","UM_distinctid=15d1131d8985-0e8266a117719a8-12646f4a-1fa400-15d1131d8991cd; kg_mid=8a18832e9fc0845106e1075df481c1c2; Hm_lvt_aedee6983d4cfc62f509129360d6bb3d=1499959312,1499959321,1500005541,1500005583; Hm_lpvt_aedee6983d4cfc62f509129360d6bb3d=1500005583");
        headers.put("Connection","keep-alive");
        sendGetRequst("http://www.kugou.com/yy/index.php",params,headers,callBack);
    }
}