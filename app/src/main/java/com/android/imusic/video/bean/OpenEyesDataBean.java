package com.android.imusic.video.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * Card Item Bean
 */

public class OpenEyesDataBean {

    private int count;
    private long id;
    private String dataType;
    private OpenEyesHeader header;
    private OpenEyesContent content;
    private OpenEyesIndexItemBean data;
    private List<OpenEyesIndexItemBean> itemList;
    private String actionUrl;
    private String text;
    private String type;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public OpenEyesHeader getHeader() {
        return header;
    }

    public void setHeader(OpenEyesHeader header) {
        this.header = header;
    }

    public OpenEyesContent getContent() {
        return content;
    }

    public void setContent(OpenEyesContent content) {
        this.content = content;
    }

    public OpenEyesIndexItemBean getData() {
        return data;
    }

    public void setData(OpenEyesIndexItemBean data) {
        this.data = data;
    }

    public List<OpenEyesIndexItemBean> getItemList() {
        return itemList;
    }

    public void setItemList(List<OpenEyesIndexItemBean> itemList) {
        this.itemList = itemList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}