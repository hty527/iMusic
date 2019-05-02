package com.video.player.lib.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import com.video.player.lib.R;
import com.video.player.lib.base.BaseVideoController;
import com.video.player.lib.utils.Logger;

/**
 * TinyHung@Outlook.com
 * 2019/4/18
 * MiniWindow Controller
 */

public class VideoMiniWindowController extends BaseVideoController {

    private ProgressBar mBottomProgressBar,mProgressBar;

    public VideoMiniWindowController(@NonNull Context context) {
        this(context,null);
    }

    public VideoMiniWindowController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoMiniWindowController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.video_mini_window_controller_layout,this);
        mBottomProgressBar = (ProgressBar) findViewById(R.id.bottom_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.video_loading);
        //退出小窗口
        findViewById(R.id.video_btn_back_tiny).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnFuctionListener){
                    mOnFuctionListener.onQuiteMiniWindow();
                }
            }
        });
    }

    /**
     * 开始播放器准备中
     */
    @Override
    public void readyPlaying() {
        Logger.d(TAG,"readyPlaying："+mScrrenOrientation);
        updateVideoControllerUI(View.VISIBLE,View.VISIBLE);
    }

    /**
     * 开始缓冲中
     */
    @Override
    public void startBuffer() {
        Logger.d(TAG,"startBuffer："+mScrrenOrientation);
        updateVideoControllerUI(View.VISIBLE,View.VISIBLE);
    }

    /**
     * 缓冲结束
     */
    @Override
    public void endBuffer() {
        Logger.d(TAG,"endBuffer："+mScrrenOrientation);
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
    }

    /**
     * 已开始
     */
    @Override
    public synchronized void play() {
        Logger.d(TAG,"play："+mScrrenOrientation);
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
    }

    /**
     * 已暂停
     * tips:播放器组件处理了暂停时若处在小窗口播放，则停止播放,故此处无需处理小窗口事件
     */
    @Override
    public void pause() {
        Logger.d(TAG,"pause："+mScrrenOrientation);
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
    }

    /**
     * 恢复播放
     * tips:播放器组件处理了暂停时若处在小窗口播放，则停止播放,故此处无需处理小窗口事件
     */
    @Override
    public void repeatPlay() {
        Logger.d(TAG,"repeatPlay："+mScrrenOrientation);
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
    }

    @Override
    public void mobileWorkTips() {}

    /**
     * 播放失败
     * tips:播放器组件处理了播放失败时若处在小窗口播放，则停止播放,故此处无需处理小窗口事件
     * @param errorCode
     * @param errorMessage
     */
    @Override
    public void error(int errorCode, String errorMessage) {
        Logger.d(TAG,"error,errorMessage:"+errorMessage+",SCRREN:"+mScrrenOrientation);
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
    }

    /**
     * 还原所有状态
     */
    @Override
    public void reset() {
        Logger.d(TAG,"reset："+mScrrenOrientation);
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
        if(null!=mBottomProgressBar){
            mBottomProgressBar.setSecondaryProgress(0);
            mBottomProgressBar.setProgress(0);
        }
    }

    /**
     * 实时播放和缓冲进度，子线程更新
     * @param totalPosition 总视频时长，单位：毫秒
     * @param currentPosition 实施播放进度，单位：毫秒
     * @param bufferPercent 缓冲进度，单位：百分比
     */
    @Override
    protected void currentPosition(long totalPosition, long currentPosition, int bufferPercent) {
        if(null!=mBottomProgressBar&&currentPosition>-1){
            //播放进度，这里比例是1/1000
            int progress = (int) (((float) currentPosition / totalPosition) * 1000);
            mBottomProgressBar.setProgress(progress);
            //缓冲进度
            if(null!=mBottomProgressBar&&mBottomProgressBar.getSecondaryProgress()<(bufferPercent*10)){
                mBottomProgressBar.setSecondaryProgress(bufferPercent*10);
            }
        }
    }

    /**
     * 更新控制器
     * @param loading 加载中
     * @param bottomProgress 底部进度条
     */
    private void updateVideoControllerUI(int loading,int bottomProgress) {
        if(null!=mProgressBar){
            mProgressBar.setVisibility(loading);
        }
        if(null!=mBottomProgressBar){
            mBottomProgressBar.setVisibility(bottomProgress);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBottomProgressBar=null; mProgressBar=null;
    }
}