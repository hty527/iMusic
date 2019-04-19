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

    /**
     * 标准的必须实现关心的方法
     */
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
    //移动网络状态下工作
    public abstract void mobileWorkTips();
    //播放失败
    public abstract void error(int errorCode,String errorMessage);
    //播放器被重置
    public abstract void reset();

    /**
     * 非必须的，根据自身业务逻辑实现
     */
    //设置视频标题内容
    protected void setTitle(String videoTitle){}
    //播放地址为空
    protected void pathInvalid(){}
    //切换为竖屏方向
    protected void startHorizontal(){}
    //切换为小窗口播放
    protected void startTiny(){}
    //切换为悬浮窗
    protected void startGlobalWindow(){}
    //视频总长度、播放进度、缓冲进度
    protected void onTaskRuntime(long totalDurtion, long currentDurtion,int bufferPercent){}
    //缓冲百分比
    protected void onBufferingUpdate(int percent){}
    //播放器空白位置单击事件，关注此方法实现控制器的现实和隐藏
    protected void changeControllerState(int scrrenOrientation,boolean isInterceptIntent){}

    /**
     * 更新屏幕方向
     * @param scrrenOrientation 1：竖屏，>1：横屏
     */
    public void setScrrenOrientation(int scrrenOrientation){
        this.mScrrenOrientation=scrrenOrientation;
    }

    //子类控制器实现扩展功能
    public abstract static class OnFuctionListener{
        /**
         * 开启全屏
         * @param videoController 继承自BaseVideoController的自定义控制器
         */
        public void onStartFullScreen(BaseVideoController videoController){}
        /**
         * 开启迷你窗口
         * @param miniWindowController 继承自BaseVideoController的自定义控制器
         */
        public void onStartMiniWindow(BaseVideoController miniWindowController){}
        /**
         * 开启全局悬浮窗
         * @param windowController 继承自BaseVideoController的自定义控制器
         * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
         */
        public void onStartGlobalWindown(BaseVideoController windowController,boolean defaultCreatCloseIcon){}
        //关闭迷你窗口
        public void onQuiteMiniWindow(){}
        //打开播放器界面
        public void onStartActivity(){}
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