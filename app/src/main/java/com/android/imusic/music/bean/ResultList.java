package com.android.imusic.music.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/13
 */

public class ResultList<T> {

    private List<T> list;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "ResultList{" +
                "list=" + list +
                '}';
    }
}
