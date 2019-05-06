package com.android.imusic.video.model;

import com.android.imusic.music.net.MusicNetUtils;
import com.android.imusic.video.bean.OpenEyesIndexInfo;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Video Model
 */

public class IndexVideoEngin extends MusicNetUtils {

    /**
     * 获取视频列为表
     * @param page 页眉
     * @param callBack 回调
     */
    public void getIndexVideos(int page,OnOtherRequstCallBack callBack){
        getIndexVideoList(page,new TypeToken<OpenEyesIndexInfo>(){}.getType(),callBack);
    }

    /**
     * 根据URL获取视频列表
     * @param url url
     * @param page 页眉
     * @param callBack 回调
     */
    public void getVideosByUrl(String url,int page,OnOtherRequstCallBack callBack){
        getVideoByUrl(url,page,new TypeToken<OpenEyesIndexInfo>(){}.getType(),callBack);
    }

    /**
     * 根据视频ID获取视频列表
     * @param videoID 视频ID
     * @param callBack 回调
     */
    public void getVideosByVideo(String videoID,OnOtherRequstCallBack callBack){
        getRecommendVideoList(videoID,new TypeToken<OpenEyesIndexInfo>(){}.getType(),callBack);
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
    public void getRecommendVideoList(String videoID,Type type, OnOtherRequstCallBack callBack){
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