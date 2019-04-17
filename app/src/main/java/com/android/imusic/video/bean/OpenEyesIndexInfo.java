package com.android.imusic.video.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * 开眼视频
 */

public class OpenEyesIndexInfo {

    private int count;
    private int total;
    //下一页API URL
    private String nextPageUrl;
    private boolean adExist;
    //一维数组
    private List<OpenEyesIndexItemBean> itemList;

    private List<OpenEyesIndexItemBean> videoList;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }

    public boolean isAdExist() {
        return adExist;
    }

    public void setAdExist(boolean adExist) {
        this.adExist = adExist;
    }

    public List<OpenEyesIndexItemBean> getItemList() {
        return itemList;
    }

    public void setItemList(List<OpenEyesIndexItemBean> itemList) {
        this.itemList = itemList;
    }

    public List<OpenEyesIndexItemBean> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<OpenEyesIndexItemBean> videoList) {
        this.videoList = videoList;
    }

    @Override
    public String toString() {
        return "OpenEyesIndexInfo{" +
                "count=" + count +
                ", total=" + total +
                ", nextPageUrl='" + nextPageUrl + '\'' +
                ", adExist=" + adExist +
                ", itemList=" + itemList +
                ", videoList=" + videoList +
                '}';
    }
}
