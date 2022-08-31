package com.music.player.lib.bean;

/**
 * TinyHung@Outlook.com
 * 2019/3/8
 * AlarmSetting
 */

public class MusicAlarmSetting {

    private String title;
    private int alarmModel;

    public MusicAlarmSetting(){

    }

    public MusicAlarmSetting(String title, int alarmModel) {
        this.title = title;
        this.alarmModel = alarmModel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAlarmModel() {
        return alarmModel;
    }

    public void setAlarmModel(int alarmModel) {
        this.alarmModel = alarmModel;
    }
}
