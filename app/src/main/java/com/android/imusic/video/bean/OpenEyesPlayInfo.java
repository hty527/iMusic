package com.android.imusic.video.bean;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * 清晰度
 */

public class OpenEyesPlayInfo {

    /**
     * height : 480
     * name : 标清
     * type : normal
     * url : http://baobab.kaiyanapp.com/api/v1/playUrl?vid=155005&resourceType=video&editionType=normal&source=aliyun&playUrlType=url_oss
     * urlList : [{"name":"aliyun","size":7145563,"url":"http://baobab.kaiyanapp.com/api/v1/playUrl?vid=155005&resourceType=video&editionType=normal&source=aliyun&playUrlType=url_oss"},{"name":"qcloud","size":7145563,"url":"http://baobab.kaiyanapp.com/api/v1/playUrl?vid=155005&resourceType=video&editionType=normal&source=qcloud&playUrlType=url_oss"},{"name":"ucloud","size":7145563,"url":"http://baobab.kaiyanapp.com/api/v1/playUrl?vid=155005&resourceType=video&editionType=normal&source=ucloud&playUrlType=url_oss"}]
     * width : 854
     */

    private int height;
    private String name;
    private String type;
    private String url;
    private int width;
    /**
     * name : aliyun
     * size : 7145563
     * url : http://baobab.kaiyanapp.com/api/v1/playUrl?vid=155005&resourceType=video&editionType=normal&source=aliyun&playUrlType=url_oss
     */

    private List<UrlListBean> urlList;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public List<UrlListBean> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<UrlListBean> urlList) {
        this.urlList = urlList;
    }

    public static class UrlListBean {
        private String name;
        private int size;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
