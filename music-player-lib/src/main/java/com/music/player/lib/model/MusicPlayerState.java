package com.music.player.lib.model;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/5
 * MusicPlayerState
 */

public enum MusicPlayerState {
    MUSIC_PLAYER_PREPARE,//准备中
    MUSIC_PLAYER_BUFFER,//缓冲中
    MUSIC_PLAYER_PLAYING,//播放中
    MUSIC_PLAYER_PAUSE,//暂停
    MUSIC_PLAYER_STOP,//已结束，或未开始
    MUSIC_PLAYER_ERROR//错误
}
