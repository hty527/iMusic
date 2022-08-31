package com.video.player.lib.manager;

/**
 * TinyHung@Outlook.com
 * 2019/4/9
 * VideoPlayer Persenter
 */

public interface MediaPlayerPresenter {

    /**
     * 设置循环模式
     * @param loop true:循环播放 false:反之
     * @return 代理人
     */
    VideoPlayerManager setLoop(boolean loop);

    /**
     * 移动网络工作开关
     * @param enable true：允许移动网络工作 false：不允许
     */
    void setMobileWorkEnable(boolean enable);

    /**
     * 设置视频画面显示缩放类型
     * @param displayType 详见VideoConstants常量定义
     */
    void setVideoDisplayType(int displayType);

    /**
     * 返回播放器内部播放状态
     * @return true：正在播放，fasle：未播放
     */
    boolean isPlaying();

    /**
     * 返回播放器内部工作状态
     * @return true：正在工作，包含暂停、缓冲等， false：未工作
     */
    boolean isWorking();

    /**
     * 返回当前正在播放的视频宽
     * @return 视频宽（分辨率）
     */
    int getVideoWidth();

    /**
     * 返回当前正在播放的视频高
     * @return 视频高（分辨率）
     */
    int getVideoHeight();

    /**
     * 开始、暂停播放
     */
    void playOrPause();

    /**
     * 恢复播放
     */
    void play();
    /**
     * 暂停播放
     */
    void pause();

    /**
     * 释放、还原播放、监听、渲染等状态
     */
    void onReset();

    /**
     * 停止播放
     * @param isReset 是否内部释放播放器
     */
    void onStop(boolean isReset);

    /**
     * 跳转至指定位置播放
     * @param currentTime 事件位置，单位毫秒
     */
    void seekTo(long currentTime) ;

    /**
     * 返回当前播放对象的总时长
     * @return 视频总时长，单位毫秒
     */
    long getDurtion();

    /**
     * 返回当前已播放的时长
     * @return 已播放的视频长度，单位毫秒
     */
    long getCurrentDurtion();

    /**
     * 是否可以直接返回
     * @return true：可以直接返回 false：存在全屏或小窗口
     */
    boolean isBackPressed();

    /**
     * 是否可以直接返回
     * @param destroy 是否直接销毁，比如说MainActivity返回逻辑还有询问用户是否退出，给定destroy为false，
     *                则只是尝试弹射，并不会去销毁播放器
     * @return true：可以直接返回 false：存在全屏或小窗口
     */
    boolean isBackPressed(boolean destroy);

    /**
     * 返回内部播放器播放状态
     * @return 内部播放状态，详见VideoConstants.定义
     */
    int getVideoPlayerState();

    /**
     * 此标记标识跳转至目标Activity是否衔接播放
     * @param continuePlay true:衔接播放 fasle:不衔接播放
     */
    void setContinuePlay(boolean continuePlay);

    /**
     * 组件对应生命周期调用
     */
    void onResume();

    /**
     * 组件对应生命周期调用
     */
    void onPause();

    /**
     * 组件对应生命周期调用
     */
    void onDestroy();
}