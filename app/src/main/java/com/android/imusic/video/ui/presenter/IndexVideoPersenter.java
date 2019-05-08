package com.android.imusic.video.ui.presenter;

import com.android.imusic.base.BasePresenter;
import com.android.imusic.base.BaseEngin;
import com.android.imusic.net.OkHttpUtils;
import com.android.imusic.video.bean.OpenEyesIndexInfo;
import com.android.imusic.video.model.IndexVideoEngin;
import com.android.imusic.video.ui.contract.IndexVideoContract;

/**
 * TinyHung@Outlook.com
 * 2019/5/6
 * Index Video Presenter
 */

public class IndexVideoPersenter extends BasePresenter<IndexVideoContract.View,IndexVideoEngin>
        implements IndexVideoContract.Presenter<IndexVideoContract.View>{

    @Override
    protected IndexVideoEngin createEngin() {
        return new IndexVideoEngin();
    }

    /**
     * 获取视频列表
     * @param page 页眉
     */
    @Override
    public void getIndexVideos(int page) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();
            getNetEngin().get().getIndexVideos(page, new OkHttpUtils.OnResultCallBack<OpenEyesIndexInfo>() {

                @Override
                public void onResponse(OpenEyesIndexInfo data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        if(null!=data.getItemList()&&data.getItemList().size()>0){
                            mViewRef.get().showVideos(data.getItemList());
                        }else{
                            mViewRef.get().showError(BaseEngin.API_RESULT_EMPTY,BaseEngin.API_EMPTY);
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showError(code,errorMsg);
                    }
                }
            });
        }
    }

    /**
     * 根据URL获取视频列表
     * @param url url
     * @param page 页眉
     */
    @Override
    public void getVideosByUrl(String url, int page) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();
            getNetEngin().get().getVideosByUrl(url, page, new OkHttpUtils.OnResultCallBack<OpenEyesIndexInfo>() {

                @Override
                public void onResponse(OpenEyesIndexInfo data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        if(null!=data.getItemList()&&data.getItemList().size()>0){
                            mViewRef.get().showVideos(data.getItemList());
                        }else{
                            mViewRef.get().showError(BaseEngin.API_RESULT_EMPTY,BaseEngin.API_EMPTY);
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showError(code,errorMsg);
                    }
                }
            });
        }
    }

    /**
     * 根据视频ID获取视频列表
     * @param videoID 视频ID
     */
    @Override
    public void getVideosByVideo(String videoID) {
        if(null!=mViewRef&&null!=mViewRef.get()){
            mViewRef.get().showLoading();

            getNetEngin().get().getVideosByVideo(videoID, new OkHttpUtils.OnResultCallBack<OpenEyesIndexInfo>() {
                @Override
                public void onResponse(OpenEyesIndexInfo data) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        if(null!=data.getItemList()&&data.getItemList().size()>0){
                            mViewRef.get().showVideos(data.getItemList());
                        }else{
                            mViewRef.get().showError(BaseEngin.API_RESULT_EMPTY,BaseEngin.API_EMPTY);
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mViewRef&&null!=mViewRef.get()){
                        mViewRef.get().showError(code,errorMsg);
                    }
                }
            });
        }
    }
}