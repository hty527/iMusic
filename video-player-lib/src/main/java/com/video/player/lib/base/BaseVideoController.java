package com.video.player.lib.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.video.player.lib.constants.VideoConstants;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * All Controller Base
 * 自定义视频播放器请继承此BaseVideoController组件实现自己的逻辑和UI
 */

public abstract class BaseVideoController extends FrameLayout{

    protected static final String TAG = "BaseVideoController";
    //屏幕方向,默认常规竖屏方向
    protected int mScrrenOrientation = VideoConstants.SCREEN_ORIENTATION_PORTRAIT;

    public BaseVideoController(@NonNull Context context) {
        this(context,null);
    }

    public BaseVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BaseVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //准备播放中
    public abstract void readyPlaying();
    //开始缓冲中
    public abstract void startBuffer();
    //缓冲结束
    public abstract void endBuffer();
    //开始播放中
    public abstract void play();
    //已暂停播放
    public abstract void pause();
    //已回复播放
    public abstract void repeatPlay();
    //播放失败
    public abstract void error(int errorCode,String errorMessage);
    //播放器被重置
    public abstract void reset();
    //切换为竖屏方向
    public abstract void startHorizontal();
    //切换为小窗口播放
    public abstract void startTiny();
    //切换为悬浮窗
    public abstract void startGlobalWindow();
    //移动网络状态下工作
    public abstract void mobileWorkTips();
    //视频总长度、播放进度、缓冲进度
    public void onTaskRuntime(long totalDurtion, long currentDurtion,int bufferPercent){}
    //缓冲百分比
    public void onBufferingUpdate(int percent){}
    //播放器空白位置单击事件，关注此方法实现控制器的现实和隐藏
    public abstract void changeControllerState(int scrrenOrientation,boolean isInterceptIntent);

    /**
     * 视频标题，子类若关心可实现
     * @param videoTitle 视频标题内容
     */
    protected void setTitle(String videoTitle){}

    /**
     * 播放地址为空
     */
    protected void pathInvalid(){}

    /**
     * 更新屏幕方向
     * @param scrrenOrientation 1：竖屏，>1：横屏
     */
    public void setScrrenOrientation(int scrrenOrientation){
        this.mScrrenOrientation=scrrenOrientation;
    }

    //子类控制器实现扩展功能
    public abstract static class OnFuctionListener{
        //开启全屏
        public void onStartFullScreen(){}
        //开启迷你窗口
        public void onStartMiniWindow(){}
        //开启全局悬浮窗
        public void onStartGlobalWindown(){}
        //关闭迷你窗口
        public void onQuiteMiniWindow(){}
        //弹射返回
        public void onBackPressed(){}
    }

    protected OnFuctionListener mOnFuctionListener;

    public void setOnFuctionListener(OnFuctionListener onFuctionListener) {
        mOnFuctionListener = onFuctionListener;
    }

    protected void onDestroy(){
        mOnFuctionListener=null;
    }
}