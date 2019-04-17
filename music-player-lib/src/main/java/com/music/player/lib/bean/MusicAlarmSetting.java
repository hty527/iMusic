package com.music.player.lib.bean;

import com.music.player.lib.model.MusicAlarmModel;

/**
 * TinyHung@Outlook.com
 * 2019/3/8
 * AlarmSetting
 */

public class MusicAlarmSetting {

    private String title;
    private MusicAlarmModel alarmModel;

    public MusicAlarmSetting(){

    }

    public MusicAlarmSetting(String title, MusicAlarmModel alarmModel) {
        this.title = title;
        this.alarmModel = alarmModel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MusicAlarmModel getAlarmModel() {
        return alarmModel;
    }

    public void setAlarmModel(MusicAlarmModel alarmModel) {
        this.alarmModel = alarmModel;
    }
}
