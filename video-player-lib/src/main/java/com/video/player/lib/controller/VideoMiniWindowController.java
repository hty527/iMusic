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
     * 播放进度
     * @param totalDurtion 总时长
     * @param currentDurtion 已播放时长
     * @param bufferPercent 缓冲进度，未必满重复刷新控件，在>=100时更新,比如说切换至全屏后需要更新进度
     */
    @Override
    public void onTaskRuntime(long totalDurtion, long currentDurtion,int bufferPercent) {
        Logger.d("播放实时进度","onTaskRuntime-->totalDurtion:"+totalDurtion+",currentDurtion:"+currentDurtion);
        if(totalDurtion>-1){
            //得到当前进度
            int progress = (int) (((float) currentDurtion / totalDurtion) * 100);
            if(null!=mBottomProgressBar){
                if(bufferPercent>=100&&mBottomProgressBar.getSecondaryProgress()<bufferPercent){
                    mBottomProgressBar.setSecondaryProgress(bufferPercent);
                }
                mBottomProgressBar.setProgress(progress);
            }
        }
    }

    /**
     * 缓冲进度
     * @param percent
     */
    @Override
    public void onBufferingUpdate(int percent) {
        Logger.d("onBufferingUpdate","percent-->"+percent);
        if(null!=mBottomProgressBar&&mBottomProgressBar.getSecondaryProgress()<100){
            mBottomProgressBar.setSecondaryProgress(percent);
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