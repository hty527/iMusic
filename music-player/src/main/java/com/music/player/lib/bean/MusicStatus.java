package com.music.player.lib.bean;

import java.io.Serializable;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/8
 */

public class MusicStatus implements Serializable{

    public static final int PLAYER_STATUS_DESTROY = -1;
    public static final int PLAYER_STATUS_STOP = 0;
    public static final int PLAYER_STATUS_PAUSE = 1;
    public static final int PLAYER_STATUS_START = 2;
    public static final int PLAYER_STATUS_PREPARED = 3;
    public static final int PLAYER_STATUS_ERROR = 4;
    private long id;
    //播放器状态，-1:销毁 0：停止 1：暂停 2：播放 3：准备 4:失败
    private int playerStatus=-2;//默认是Demo处理的事件，自己关心则必须设定此值大于-1
    private String title;
    private String cover;

    public MusicStatus(){

    }

    public MusicStatus (int status){
        this.playerStatus=status;
    }

    public MusicStatus (int status,long musicID){
        this.playerStatus=status;
        this.id=musicID;
    }

    public MusicStatus (int status,String cover){
        this.playerStatus=status;
        this.cover=cover;
    }

    public MusicStatus(long id, int playerStatus, String title, String cover) {
        this.id = id;
        this.playerStatus = playerStatus;
        this.title = title;
        this.cover = cover;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(int playerStatus) {
        this.playerStatus = playerStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    @Override
    public String toString() {
        return "MusicStatus{" +
                "id=" + id +
                ", playerStatus=" + playerStatus +
                ", title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                '}';
    }
}
