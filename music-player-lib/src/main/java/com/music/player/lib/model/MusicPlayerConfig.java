package com.music.player.lib.model;

/**
 * TinyHung@Outlook.com
 * 2019/3/20
 * MusicPlayer Config
 */

public class MusicPlayerConfig {

    //默认闹钟模式
    private MusicAlarmModel defaultAlarmModel = MusicAlarmModel.MUSIC_ALARM_MODEL_0;
    //默认播放模式
    private MusicPlayModel defaultPlayModel = MusicPlayModel.MUSIC_MODEL_LOOP;
    //是否锁定前台Service
    private boolean lockForeground = false;
    //悬浮窗样式,默认是带有删除按钮的
    private MusicWindowStyle windownStyle = MusicWindowStyle.DEFAULT;
    //悬浮窗是否自动吸附至屏幕边缘
    private boolean windownAutoScrollToEdge =false;
    //迷你悬浮窗垃圾桶功能是否开启
    private boolean trashEnable =false;
    //锁屏控制器是否开启
    private boolean screenOffEnable =false;

    public MusicAlarmModel getDefaultAlarmModel() {
        return defaultAlarmModel;
    }

    public MusicPlayerConfig setDefaultAlarmModel(MusicAlarmModel defaultAlarmModel) {
        this.defaultAlarmModel = defaultAlarmModel;
        return this;
    }

    public MusicPlayModel getDefaultPlayModel() {
        return defaultPlayModel;
    }

    public MusicPlayerConfig setDefaultPlayModel(MusicPlayModel defaultPlayModel) {
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

    public MusicWindowStyle getWindownStyle() {
        return windownStyle;
    }

    public MusicPlayerConfig setWindownStyle(MusicWindowStyle windownStyle) {
        this.windownStyle = windownStyle;
        return this;
    }

    public boolean isWindownAutoScrollToEdge() {
        return windownAutoScrollToEdge;
    }

    public MusicPlayerConfig setWindownAutoScrollToEdge(boolean windownAutoScrollToEdge) {
        this.windownAutoScrollToEdge = windownAutoScrollToEdge;
        return this;
    }

    public boolean isTrashEnable() {
        return trashEnable;
    }

    public MusicPlayerConfig setTrashEnable(boolean trashEnable) {
        this.trashEnable = trashEnable;
        return this;
    }

    public boolean isScreenOffEnable() {
        return screenOffEnable;
    }

    public MusicPlayerConfig setScreenOffEnable(boolean screenOffEnable) {
        this.screenOffEnable = screenOffEnable;
        return this;
    }

    public static MusicPlayerConfig Build() {
        return new MusicPlayerConfig();
    }
}