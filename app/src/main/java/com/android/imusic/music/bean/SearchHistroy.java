package com.android.imusic.music.bean;

import java.io.Serializable;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 */

public class SearchHistroy implements Serializable{

    private long time;
    private String key;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "SearchHistroy{" +
                "time=" + time +
                ", key='" + key + '\'' +
                '}';
    }
}
