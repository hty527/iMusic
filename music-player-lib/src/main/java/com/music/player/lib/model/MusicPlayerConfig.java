package com.music.player.lib.model;

import com.music.player.lib.constants.MusicConstants;

/**
 * TinyHung@Outlook.com
 * 2019/3/20
 * MusicPlayer Config
 */

public class MusicPlayerConfig {

    //默认闹钟模式
    private int defaultAlarmModel = MusicConstants.MUSIC_ALARM_MODEL_0;
    //默认播放模式
    private int defaultPlayModel = MusicConstants.MUSIC_MODEL_LOOP;
    //是否锁定前台Service
    private boolean lockForeground = false;

    public int getDefaultAlarmModel() {
        return defaultAlarmModel;
    }

    public MusicPlayerConfig setDefaultAlarmModel(int defaultAlarmModel) {
        this.defaultAlarmModel = defaultAlarmModel;
        return this;
    }

    public int getDefaultPlayModel() {
        return defaultPlayModel;
    }

    public MusicPlayerConfig setDefaultPlayModel(int defaultPlayModel) {
        this.defaultPlayModel = defaultPlayModel;
        return this;
    }

    public boolean isLockForeground() {
        return lockForeground;
    }

    public MusicPlayerConfig setLockForeground(boolean lockForeground) {
        this.lockForeground = lockForeground;
        return this;
    }

    public static MusicPlayerConfig Build() {
        return new MusicPlayerConfig();
    }
}