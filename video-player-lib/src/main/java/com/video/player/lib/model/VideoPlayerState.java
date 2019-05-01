package com.video.player.lib.model;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/5
 * VideoPlayerState
 */

public enum VideoPlayerState {
    MUSIC_PLAYER_PREPARE,//准备中
    MUSIC_PLAYER_BUFFER,//缓冲中
    MUSIC_PLAYER_START,//播放中
    MUSIC_PLAYER_PAUSE,//暂停
    MUSIC_PLAYER_PLAY,//恢复
    MUSIC_PLAYER_SEEK,//跳转至某处播放中
    MUSIC_PLAYER_STOP,//已结束，或未开始
    MUSIC_PLAYER_MOBILE,//移动网络环境下
    MUSIC_PLAYER_ERROR//错误
}