package com.music.player.lib.bean;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * music lrc
 */

public class MusicLrcRow {

    private String content;
    private String timeText;
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