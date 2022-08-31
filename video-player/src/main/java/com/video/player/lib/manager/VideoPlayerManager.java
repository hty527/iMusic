package com.video.player.lib.manager;

import com.video.player.lib.constants.VideoConstants;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * VideoPlayerManager 与播放器直接交互代理者
 */

public final class VideoPlayerManager implements MediaPlayerPresenter {

    private static volatile VideoPlayerManager mInstance;
    //交互实现类
    private static MediaPlayerPresenter mIMediaPlayer;
    //是否循环播放
    private boolean mLoop;
    //移动网络下是否允许播放
    private boolean mobileWorkEnable;
    //缩放类型,默认是等比缩放
    private int VIDEO_DISPLAY_TYPE = VideoConstants.VIDEO_DISPLAY_TYPE_CUT;
    //悬浮窗点击展开的目标Activity
    private static String mActivityClassName=null;
    //是否衔接播放的
    private boolean mContinuePlay;

    public static VideoPlayerManager getInstance(){
        if(null==mInstance){
            synchronized (VideoPlayerManager.class){
                if(null==mInstance){
                    mInstance=new VideoPlayerManager();
                }
            }
        }
        return mInstance;
    }
    private VideoPlayerManager(){}

    /**
     * 绑定IMediaPlayer
     * @param iMediaPlayer 具体的实现类
     */
    public void setIMediaPlayer(MediaPlayerPresenter iMediaPlayer){
        mIMediaPlayer=iMediaPlayer;
    }

    /**
     * 设置循环模式
     * @param loop true:循环播放 false:反之
     * @return 自身实例
     */
    @Override
    public VideoPlayerManager setLoop(boolean loop) {
        this.mLoop=loop;
        if(null!=mIMediaPlayer){
            mIMediaPlayer.setLoop(loop);
        }
        return mInstance;
    }

    /**
     * 返回循环播放模式
     * @return true:循环播放，false:不循环
     */
    public boolean isLoop() {
        return mLoop;
    }

    /**
     * 设置是否允许移动网络环境下工作
     * @param enable true：允许移动网络工作 false：不允许
     */
    @Override
    public void setMobileWorkEnable(boolean enable) {
        this.mobileWorkEnable=enable;
    }

    /**
     * 是否允许移动网络环境下工作
     * @return 是否允许在移动网络下工作
     */
    public boolean isMobileWorkEnable() {
        return mobileWorkEnable;
    }

    /**
     * 设置视频画面显示缩放类型,如果正在播放，会立刻生效
     * @param displayType 详见VideoConstants常量定义
     */
    @Override
    public void setVideoDisplayType(int displayType) {
        this.VIDEO_DISPLAY_TYPE=displayType;
        if(null!=mIMediaPlayer){
            mIMediaPlayer.setVideoDisplayType(displayType);
        }
    }

    /**
     * 返回视频画面缩放模式
     * @return 用户设定的缩放模式
     */
    public int getVideoDisplayType() {
        return VIDEO_DISPLAY_TYPE;
    }

    /**
     * 指定点击通知栏后打开的Activity对象绝对路径
     * @param className 播放器Activity绝对路径
     */
    public void setPlayerActivityClassName(String className) {
        this.mActivityClassName=className;
    }

    /**
     * 返回点击通知栏后打开的Activity对象绝对路径
     * @return 播放器Activity绝对路径
     */
    public String getPlayerActivityClassName() {
        return mActivityClassName;
    }

    /**
     * 返回播放器内部播放状态
     * @return 播放器内部播放状态
     */
    @Override
    public boolean isPlaying() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.isPlaying();
        }
        return false;
    }

    /**
     * 返回播放器内部工作状态
     * @return true:正在工作，包含暂停、缓冲等 false:未工作
     */
    @Override
    public boolean isWorking() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.isWorking();
        }
        return false;
    }

    /**
     * 返回当前正在播放的视频宽
     * @return 视频宽（分辨率）
     */
    @Override
    public int getVideoWidth() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.getVideoWidth();
        }
        return 0;
    }


    /**
     * 返回当前正在播放的视频高
     * @return 视频高（分辨率）
     */
    @Override
    public int getVideoHeight() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.getVideoHeight();
        }
        return 0;
    }

    /**
     * 开始、暂停 播放
     */
    @Override
    public void playOrPause() {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.playOrPause();
        }
    }

    /**
     * 恢复播放
     */
    @Override
    public void play() {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.play();
        }
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.pause();
        }
    }

    /**
     * 释放、还原播放、监听、渲染等状态
     */
    @Override
    public void onReset() {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.onReset();
        }
    }

    /**
     * 停止播放
     * @param isReset 是否释放播放器
     */
    @Override
    public void onStop(boolean isReset) {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.onStop(isReset);
        }
    }

    /**
     * 跳转至指定位置播放
     * @param currentTime 事件位置，单位毫秒
     */
    @Override
    public void seekTo(long currentTime) {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.seekTo(currentTime);
        }
    }

    /**
     * 返回正在播放的对象时长
     * @return 视频总时长，单位毫秒
     */
    @Override
    public long getDurtion() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.getDurtion();
        }
        return 0;
    }

    /**
     * 返回已播放时长
     * @return 已播放的视频长度，单位毫秒
     */
    @Override
    public long getCurrentDurtion() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.getCurrentDurtion();
        }
        return 0;
    }

    /**
     * 尝试弹射退出，若当前播放器处于迷你小窗口、全屏窗口下，则只是退出小窗口\全屏至常规窗口播放
     * 若播放器处于常规状态下，则立即销毁播放器，销毁时内部检测了悬浮窗状态，若正在悬浮窗状态下播放，则啥也不做
     * @return 是否可以销毁界面
     */
    @Override
    public boolean isBackPressed() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.isBackPressed();
        }
        return true;
    }

    /**
     * 尝试弹射退出，若当前播放器处于迷你小窗口、全屏窗口下，则只是退出小窗口\全屏至常规窗口播放
     * 若播放器处于常规状态下，则立即销毁播放器，销毁时内部检测了悬浮窗状态，若正在悬浮窗状态下播放，则啥也不做
     * @param destroy 是否直接销毁，比如说MainActivity返回逻辑还有询问用户是否退出，给定destroy为false，
     *                则只是尝试弹射，并不会去销毁播放器
     * @return 是否可以销毁界面
     */
    @Override
    public boolean isBackPressed(boolean destroy) {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.isBackPressed(destroy);
        }
        return true;
    }

    /**
     * 返回播放器内部播放状态
     * @return 内部播放状态
     */
    @Override
    public int getVideoPlayerState() {
        if(null!=mIMediaPlayer){
            return mIMediaPlayer.getVideoPlayerState();
        }
        return VideoConstants.MUSIC_PLAYER_STOP;
    }


    /**
     * 若跳转至目标Activity后需要衔接播放，则必须设置此标记，以便在生命周期切换时处理用户动作意图
     * @param continuePlay true:衔接播放 fasle:不衔接播放
     */
    @Override
    public void setContinuePlay(boolean continuePlay) {
        this.mContinuePlay = continuePlay;
    }

    /**
     * 返回衔接播放状态
     * @return true:衔接播放 fasle:不衔接播放
     */
    public boolean isContinuePlay() {
        return mContinuePlay;
    }

    /**
     * 组件处于可见状态
     */
    @Override
    public void onResume() {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.onResume();
        }
    }

    /**
     * 组件即将处于不可见状态
     */
    @Override
    public void onPause() {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.onPause();
        }
    }

    /**
     * 对应生命周期调用
     */
    @Override
    public void onDestroy() {
        if(null!=mIMediaPlayer){
            mIMediaPlayer.onDestroy();
        }
    }
}