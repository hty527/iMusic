package com.android.imusic.music.bean;

import com.music.player.lib.bean.BaseMediaInfo;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/2
 */

public class AlbumInfo {

    private SingerInfo singer;
    private List<BaseMediaInfo> list;

    public SingerInfo getSinger() {
        return singer;
    }

    public void setSinger(SingerInfo singer) {
        this.singer = singer;
    }

    public List<BaseMediaInfo> getList() {
        return list;
    }

    public void setList(List<BaseMediaInfo> list) {
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