package com.android.imusic.video.ui.contract;

import com.android.imusic.base.BaseContract;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/6
 * Index Video Contract
 */

public interface IndexVideoContract {

    interface View extends BaseContract.BaseView{

        /**
         * 显示视频列表
         * @param data 视频列表
         * @param isRestart 是否从第一页开始加载的
         */
        void showVideos(List<OpenEyesIndexItemBean> data,boolean isRestart);
    }

    interface Presenter<V> extends BaseContract.BasePresenter<V>{

        /**
         * 获取主页的视频列表
         * @param isRestart 是否重新开始？
         */
        void getIndexVideos(boolean isRestart);

        /**
         * 根据URL获取视频列表
         * @param url url
         * @param isRestart 是否从第一页开始加载的
         */
        void getVideosByUrl(String url,boolean isRestart);

        /**
         * 根据视频ID获取推荐列表
         * @param videoID 视频ID
         */
        void getVideosByVideo(String videoID);
    }
}