package com.android.imusic.video.ui.presenter;

import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.net.MusicNetUtils;
import com.android.imusic.video.bean.OpenEyesIndexInfo;
import com.android.imusic.video.model.IndexVideoEngin;
import com.android.imusic.video.ui.contract.IndexVideoContract;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * Video Presenter
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
        if(null!=mNetEngin){
            if(null!=mView){
                mView.showLoading();
            }
            mNetEngin.getIndexVideos(page, new MusicNetUtils.OnOtherRequstCallBack<OpenEyesIndexInfo>() {

                @Override
                public void onResponse(OpenEyesIndexInfo data) {
                    if(null!=mView){
                        if(null!=data.getItemList()&&data.getItemList().size()>0){
                            mView.showVideos(data.getItemList());
                        }else{
                            mView.showError(MusicNetUtils.API_RESULT_EMPTY,MusicNetUtils.API_EMPTY);
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mView){
                        mView.showError(code,errorMsg);
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
        if(null!=mNetEngin){
            if(null!=mView){
                mView.showLoading();
            }
            mNetEngin.getVideosByUrl(url,page, new MusicNetUtils.OnOtherRequstCallBack<OpenEyesIndexInfo>() {

                @Override
                public void onResponse(OpenEyesIndexInfo data) {
                    if(null!=mView){
                        if(null!=data.getItemList()&&data.getItemList().size()>0){
                            mView.showVideos(data.getItemList());
                        }else{
                            mView.showError(MusicNetUtils.API_RESULT_EMPTY,MusicNetUtils.API_EMPTY);
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mView){
                        mView.showError(code,errorMsg);
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
        if(null!=mNetEngin){
            if(null!=mView){
                mView.showLoading();
            }
            mNetEngin.getVideosByVideo(videoID,new MusicNetUtils.OnOtherRequstCallBack<OpenEyesIndexInfo>() {

                @Override
                public void onResponse(OpenEyesIndexInfo data) {
                    if(null!=mView){
                        if(null!=data.getItemList()&&data.getItemList().size()>0){
                            mView.showVideos(data.getItemList());
                        }else{
                            mView.showError(MusicNetUtils.API_RESULT_EMPTY,MusicNetUtils.API_EMPTY);
                        }
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    if(null!=mView){
                        mView.showError(code,errorMsg);
                    }
                }
            });
        }
    }
}