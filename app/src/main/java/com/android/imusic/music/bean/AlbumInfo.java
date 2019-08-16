package com.android.imusic.music.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/2
 */

public class AlbumInfo {

    private SingerInfo singer;
    private List<AudioInfo> list;

    public SingerInfo getSinger() {
        return singer;
    }

    public void setSinger(SingerInfo singer) {
        this.singer = singer;
    }

    public List<AudioInfo> getList() {
        return list;
    }

    public void setList(List<AudioInfo> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "AlbumInfo{" +
                "singer=" + singer +
                ", list=" + list +
                '}';
    }
}