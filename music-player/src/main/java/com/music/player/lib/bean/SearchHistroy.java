package com.music.player.lib.bean;

import java.io.Serializable;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 */

public class SearchHistroy implements Serializable{

    private long addtime;
    private String key;

    public long getAddtime() {
        return addtime;
    }

    public void setAddtime(long addtime) {
        this.addtime = addtime;
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
                "addtime=" + addtime +
                ", key='" + key + '\'' +
                '}';
    }
}
