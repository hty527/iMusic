package com.music.player.lib.bean;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * 音乐歌词 lrc
 */

public class MusicLrcRow {
    //歌词内容
    private String content;
    //时间：00:00.12
    private String timeText;
    //时间位置，毫秒
    private long time;

    public MusicLrcRow(){}

    public MusicLrcRow(String content, String timeText, long time) {
        this.content = content;
        this.timeText = timeText;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MusicLrcRow{" +
                "content='" + content + '\'' +
                ", timeText='" + timeText + '\'' +
                ", time=" + time +
                '}';
    }
}