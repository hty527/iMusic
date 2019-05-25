package com.video.player.lib.listener;

/**
 * TinyHung@Outlook.com
 * 2019/4/23
 * 视频播放状态，若关心视频播放器内部状态，请注册此抽象类并按需实现抽象方法，所有方法都在主线程回调
 * 此抽象类方法回调时机时播放器内部UI处理完成后开始回调,onPlayingPresent()方法 500毫秒回调一次，
 * 请注意不要在此方法内耗时操作
 */

public abstract class OnVideoEventListener {

    /**
     * 播放中各种状态，开始、暂停、恢复、停止、完成、失败 等
     * @param videoPlayerState 详见 VideoConstants. 定义说明
     */
    public void onPlayerStatus(int videoPlayerState){}

    /**
     * 播放进度，此方法在播放状态中持续被回调，已暂停情况下总时长和已播放时长返回-1
     * @param currentDurtion 实时播放位置 毫秒
     * @param totalDurtion 总长度 毫秒
     * @param bufferProgress 缓冲进度 百分比
     */
    public void onPlayingPresent(long currentDurtion,long totalDurtion,int bufferProgress){}
}