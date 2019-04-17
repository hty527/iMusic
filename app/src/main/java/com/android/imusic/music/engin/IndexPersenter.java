package com.android.imusic.music.engin;

import com.android.imusic.music.net.MusicNetUtils;
import java.lang.reflect.Type;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 */

public class IndexPersenter extends MusicNetUtils {

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

    /**
     * 获取首页视频数据
     * @param page 页眉 从0 开始
     * @param type
     * @param callBack
     */
    public void getIndexVideoList(int page,Type type, OnOtherRequstCallBack callBack){
        requstOtherApi("http://baobab.kaiyanapp.com/api/v5/index/tab/allRec?page="+page+"&udid=a53873ffaa4430bbb41ea178c1187e97c4b3c4a",type,callBack);
    }

    /**
     * 根据视频ID获取相关推荐视频
     * @param videoID 视频ID
     * @param type
     * @param callBack
     */
    public void getRecommendVideoList(long videoID,Type type, OnOtherRequstCallBack callBack){
        requstOtherApi("http://baobab.kaiyanapp.com/api/v4/video/related?id="+videoID+"&udid=a53873ffaa4430bbb41ea178c1187e97c4b3c4a",type,callBack);
    }

    /**
     * 根据URL获取视频
     * @param url 视频ID
     * @param page 页眉
     * @param type
     * @param callBack
     */
    public void getVideoByUrl(String url,int page,Type type, OnOtherRequstCallBack callBack){
        requstOtherApi("http://baobab.kaiyanapp.com/api/v2/"+url+"/?page="+page+"&udid=a53873ffaa4430bbb41ea178c1187e97c4b3c4a",type,callBack);
    }
}
