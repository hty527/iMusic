package com.android.imusic.music.engin;

import com.android.imusic.music.net.MusicNetUtils;
import com.music.player.lib.util.Logger;

import java.lang.reflect.Type;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 */

public class SearchPersenter extends MusicNetUtils {

    private static final String TAG = "SearchPersenter";

    /**
     * 根据关键字检索音频信息
     * @param key 关键字
     * @param callBack
     */
    public void queryMusicToKey(String key, int page,Type type,OnRequstCallBack callBack){
        Logger.d(TAG,"queryMusicToKey-->page:"+page+",key:"+key);
        requstApi("http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword="+key+"&page="+page+"&pagesize=20&showtype=1",type,callBack);
    }

    /**
     * 根据HashKey获取播放地址
     * @param hashKey
     * @param type
     * @param callBack
     */
    public void getPlayUrl(String hashKey, Type type,OnRequstCallBack callBack){
        requstApi("http://www.kugou.com/yy/index.php?r=play/getdata&hash="+hashKey,type,callBack);
    }
}