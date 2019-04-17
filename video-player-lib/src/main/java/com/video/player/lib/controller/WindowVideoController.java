package com.video.player.lib.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.video.player.lib.R;
import com.video.player.lib.base.BaseVideoController;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.model.VideoPlayerState;
import com.video.player.lib.utils.Logger;
import com.video.player.lib.utils.VideoUtils;

/**
 * TinyHung@Outlook.com
 * 2019/4/17
 * Window Controller
 */

public class WindowVideoController extends BaseVideoController implements SeekBar.OnSeekBarChangeListener {

    private View mBottomBarLayout,mBtnFull;
    private TextView mVideoCurrent,mVideoTotal;
    private ProgressBar mBottomProgressBar,mProgressBar;
    private SeekBar mSeekBar;
    private ImageView mBtnStart;
    //用户是否手指正在持续滚动
    private boolean isTouchSeekBar=false;

    public WindowVideoController(@NonNull Context context) {
        this(context,null);
    }

    public WindowVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WindowVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.video_window_controller_layout,this);
        mBottomBarLayout = findViewById(R.id.video_bottom_tab);
        mBtnStart = (ImageView) findViewById(R.id.video_btn_start);
        mBtnFull = findViewById(R.id.video_full_screen);
        mVideoCurrent = (TextView) findViewById(R.id.video_current);
        mVideoTotal = (TextView) findViewById(R.id.video_total);
        mBottomProgressBar = findViewById(R.id.bottom_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.video_loading);
        mSeekBar = (SeekBar) findViewById(R.id.video_seek_progress);
        OnClickListener onClickListener=new OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if(id == R.id.video_btn_start){
                    VideoPlayerManager.getInstance().playOrPause();
                }else if(id == R.id.video_full_screen){
                    if(null!=mOnFuctionListener){
                        mOnFuctionListener.onStartActivity();
                    }
                }
            }
        };
        mBtnStart.setOnClickListener(onClickListener);
        mBtnFull.setOnClickListener(onClickListener);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    /**
     * 负责控制器显示、隐藏
     */
    private  Runnable controllerRunnable=new Runnable() {
        @Override
        public void run() {
            if(null!=mBottomBarLayout){
                mBottomBarLayout.setVisibility(INVISIBLE);
            }
            if(null!=mBottomProgressBar){
                mBottomProgressBar.setVisibility(VISIBLE);
            }
        }
    };

    //=========================================SEEK BAR=============================================

    /**
     * 用户手指持续拨动中
     * @param seekBar
     * @param progress 实时进度
     * @param fromUser 是否来自用户手动拖动
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            long durtion = VideoPlayerManager.getInstance().getDurtion();
            if(durtion>0){
                mVideoCurrent.setText(VideoUtils.getInstance().stringForAudioTime(progress * durtion / 100));
            }
        }
    }

    /**
     * 手指按下
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTouchSeekBar=true;
        removeCallbacks(View.VISIBLE);
    }

    /**
     * 手指松开
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTouchSeekBar=false;
        //恢复控制器显示隐藏规则
        changeControllerState(mScrrenOrientation,false);
        //非暂停状态下置为加载中状态
        if(!VideoPlayerManager.getInstance().getVideoPlayerState().equals(VideoPlayerState.MUSIC_PLAYER_PAUSE)){
            startSeekLoading();
        }
        //跳转至某处
        long durtion = VideoPlayerManager.getInstance().getDurtion();
        if(durtion>0){
            long currentTime = seekBar.getProgress() * durtion / 100;
            VideoPlayerManager.getInstance().seekTo(currentTime);
        }
    }

    //=========================================播放状态=============================================

    /**
     * 开始播放器准备中
     */
    @Override
    public void readyPlaying() {
        Logger.d(TAG,"readyPlaying："+mScrrenOrientation);
        removeCallbacks(View.INVISIBLE);
        //小窗口
        updateVideoControllerUI(View.VISIBLE,View.INVISIBLE);
    }

    /**
     * 跳转至某处播放中
     */
    public void startSeekLoading(){
        Logger.d(TAG,"startSeekLoading："+mScrrenOrientation);
        //小窗口
        updateVideoControllerUI(View.VISIBLE,View.INVISIBLE);
    }

    /**
     * 开始缓冲中
     */
    @Override
    public void startBuffer() {
        Logger.d(TAG,"startBuffer："+mScrrenOrientation);
        updateVideoControllerUI(View.VISIBLE,View.INVISIBLE);
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
     * 已暂停
     * tips:播放器组件处理了暂停时若处在小窗口播放，则停止播放,故此处无需处理小窗口事件
     */
    @Override
    public void pause() {
        Logger.d(TAG,"pause："+mScrrenOrientation);
        if(null!=mBtnStart){
            mBtnStart.setImageResource(R.drawable.ic_video_controller_play);
        }
        removeCallbacks(View.VISIBLE);
    }

    /**
     * 恢复播放
     * tips:播放器组件处理了暂停时若处在小窗口播放，则停止播放,故此处无需处理小窗口事件
     */
    @Override
    public void repeatPlay() {
        Logger.d(TAG,"repeatPlay："+mScrrenOrientation);
        if(null!=mBtnStart){
            mBtnStart.setImageResource(R.drawable.ic_video_controller_pause);
        }
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
        changeControllerState(mScrrenOrientation,false);
    }

    /**
     * 播放失败
     * tips:播放器组件处理了播放失败时若处在小窗口播放，则停止播放,故此处无需处理小窗口事件
     * @param errorCode
     * @param errorMessage
     */
    @Override
    public void error(int errorCode, String errorMessage) {
        Logger.d(TAG,"error,errorMessage:"+errorMessage+",SCRREN:"+mScrrenOrientation);
        if(null!=mBtnStart){
            mBtnStart.setImageResource(R.drawable.ic_video_controller_play);
        }
        removeCallbacks(View.INVISIBLE);
        updateVideoControllerUI(View.INVISIBLE,View.INVISIBLE);
    }

    /**
     * 已开始
     */
    @Override
    public synchronized void play() {
        Logger.d(TAG,"play："+mScrrenOrientation);
        if(null!=mBtnStart){
            mBtnStart.setImageResource(R.drawable.ic_video_controller_pause);
        }
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
    }

    /**
     * 开启全局悬浮窗播放
     */
    @Override
    public void startGlobalWindow() {
        removeCallbacks(View.INVISIBLE);
        Logger.d(TAG,"startWindow");
        updateVideoControllerUI(View.INVISIBLE,View.VISIBLE);
    }

    @Override
    public void mobileWorkTips() {}

    /**
     * 还原所有状态
     */
    @Override
    public void reset() {
        Logger.d(TAG,"reset："+mScrrenOrientation);
        removeCallbacks(View.INVISIBLE);
        updateVideoControllerUI(View.INVISIBLE,View.INVISIBLE);
        if(null!=mVideoTotal){
            mVideoTotal.setText("00:00");
            mVideoCurrent.setText("00:00");
        }
        if(null!=mSeekBar){
            mSeekBar.setSecondaryProgress(0);
            mSeekBar.setProgress(0);
        }
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
            if(null!=mVideoTotal){
                mVideoTotal.setText(VideoUtils.getInstance().stringForAudioTime(totalDurtion));
                mVideoCurrent.setText(VideoUtils.getInstance().stringForAudioTime(currentDurtion));
            }
            //得到当前进度
            int progress = (int) (((float) currentDurtion / totalDurtion) * 100);
            if(null!=mSeekBar){
                if(bufferPercent>=100&&mSeekBar.getSecondaryProgress()<bufferPercent){
                    mSeekBar.setSecondaryProgress(bufferPercent);
                }
                if(!isTouchSeekBar){
                    mSeekBar.setProgress(progress);
                }
            }
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
        if(null!=mSeekBar&&mSeekBar.getSecondaryProgress()<100){
            mSeekBar.setSecondaryProgress(percent);
        }
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
        //如果是显示底部进度条，当底部控制器显示时不应该显示底部进度条
        if(null!=mBottomProgressBar){
            if(bottomProgress==View.VISIBLE){
                //仅当底部控制器处于非显示状态下，才显示底部进度条
                if(null!=mBottomBarLayout&&mBottomBarLayout.getVisibility()!=VISIBLE){
                    mBottomProgressBar.setVisibility(bottomProgress);
                }
            }else{
                mBottomProgressBar.setVisibility(bottomProgress);
            }
        }
    }

    /**
     * 移除定时器，保留在最后的状态
     * @param visible 最后的状态
     */
    private void removeCallbacks(int visible) {
        WindowVideoController.this.removeCallbacks(controllerRunnable);
        if(null!=mBottomBarLayout){
            mBottomBarLayout.setVisibility(visible);
        }
        if(View.INVISIBLE==visible){
            mBottomProgressBar.setVisibility(VISIBLE);
        }
    }

    /**
     * 显示、隐藏 控制器 上下交互功能区
     * 手动点击，根据播放状态自动处理，手势交互处理等状态
     * @param scrrenOrientation 横竖屏状态
     * @param isInterceptIntent 是否拦截意图
     */
    @Override
    public void changeControllerState(int scrrenOrientation,boolean isInterceptIntent) {
        if(null==mBottomBarLayout) return;
        Logger.d(TAG,"changeControllerState-->"+scrrenOrientation+",isInterceptIntent:"+isInterceptIntent);
        //重复显示
        if(isInterceptIntent&&mBottomBarLayout.getVisibility()==View.VISIBLE){
            removeCallbacks(View.INVISIBLE);
            return;
        }
        //移除已post的事件
        WindowVideoController.this.removeCallbacks(controllerRunnable);
        if(mBottomBarLayout.getVisibility()!=View.VISIBLE){
            mBottomBarLayout.setVisibility(View.VISIBLE);
        }
        if(null!=mBottomProgressBar){
            mBottomProgressBar.setVisibility(View.INVISIBLE);
        }
        WindowVideoController.this.postDelayed(controllerRunnable,5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoCurrent=null; mVideoTotal=null; mBottomProgressBar=null; mProgressBar=null;
        mSeekBar=null; mBtnStart=null; isTouchSeekBar=false;
    }
}