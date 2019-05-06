package com.android.imusic.video.ui.contract;

import com.android.imusic.base.BaseContract;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Video Presenter
 */

public interface IndexVideoContract {

    interface View extends BaseContract.BaseView{

        /**
         * 显示视频列表
         * @param data 视频列表
         */
        void showVideos(List<OpenEyesIndexItemBean> data);
    }

    interface Presenter<V> extends BaseContract.BasePresenter<V>{

        /**
         * 获取主页的视频列表
         * @param page 页眉
         */
        void getIndexVideos(int page);

        /**
         * 根据URL获取视频列表
         * @param url url
         * @param page 页眉
         */
        void getVideosByUrl(String url,int page);

        /**
         * 根据视频ID获取推荐列表
         * @param videoID 视频ID
         */
        void getVideosByVideo(String videoID);
    }
}