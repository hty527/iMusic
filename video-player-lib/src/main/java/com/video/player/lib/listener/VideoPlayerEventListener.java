package com.video.player.lib.listener;

/**
 * TinyHung@Outlook.com
 * 2018/1/18.
 * MusicPlayer Event Listener
 */

public interface VideoPlayerEventListener {

    /**
     * 播放器所有状态回调
     * @param playerState 播放器内部状态,详见VideoConstants.定义
     */
    void onVideoPlayerState(int playerState, String message);

    /**
     * 播放器准备好了
     * @param totalDurtion 总时长
     */
    void onPrepared(long totalDurtion);

    /**
     * 缓冲百分比
     * @param percent 百分比
     */
    void onBufferingUpdate(int percent);

    /**
     * 播放器反馈信息
     * @param event 事件
     * @param extra
     */
    void onInfo(int event, int extra);

    /**
     * 音频地址无效,组件可处理付费购买等逻辑
     */
    void onVideoPathInvalid();

    /**
     * @param totalDurtion 音频总时间
     * @param currentDurtion 当前播放的位置
     * @param bufferPercent 缓冲进度，从常规默认切换至全屏、小窗时，应该关心此进度
     */
    void onTaskRuntime(long totalDurtion, long currentDurtion,int bufferPercent);

    /**
     * 播放器播放实时进度，每100毫秒回调一次
     * @param totalPosition 视频总时长 单位毫秒
     * @param currentPosition 播放实时位置时长，单位毫秒
     * @param bufferPercent 缓冲进度，单位：百分比
     */
    void currentPosition(long totalPosition,long currentPosition,int bufferPercent);

    /**
     * 销毁
     */
    void onDestroy();
}