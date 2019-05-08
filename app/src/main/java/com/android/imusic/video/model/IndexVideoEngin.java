package com.android.imusic.video.model;

import com.android.imusic.base.BaseEngin;
import com.android.imusic.net.OkHttpUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Video Model
 */

public class IndexVideoEngin extends BaseEngin {

    /**
     * 获取视频列为表
     * @param page 页眉
     * @param callBack 回调
     */
    public void getIndexVideos(int page,OkHttpUtils.OnResultCallBack callBack){
        Map<String ,String> params=new HashMap<>();
        params.put("page",page+"");
        params.put("udid","a53873ffaa4430bbb41ea178c1187e97c4b3c4a");
        sendGetRequst("http://baobab.kaiyanapp.com/api/v5/index/tab/allRec",params,callBack);
    }

    /**
     * 根据URL获取视频列表
     * @param url url
     * @param page 页眉
     * @param callBack 回调
     */
    public void getVideosByUrl(String url,int page,OkHttpUtils.OnResultCallBack callBack){
        Map<String ,String> params=new HashMap<>();
        params.put("page",page+"");
        params.put("udid","a53873ffaa4430bbb41ea178c1187e97c4b3c4a");
        sendGetRequst("http://baobab.kaiyanapp.com/api/v2/"+url,params,callBack);
    }

    /**
     * 根据视频ID获取视频列表
     * @param videoID 视频ID
     * @param callBack 回调
     */
    public void getVideosByVideo(String videoID,OkHttpUtils.OnResultCallBack callBack){
        Map<String ,String> params=new HashMap<>();
        params.put("id",videoID);
        params.put("udid","a53873ffaa4430bbb41ea178c1187e97c4b3c4a");
        sendGetRequst("http://baobab.kaiyanapp.com/api/v4/video/related",params,callBack);
    }
}