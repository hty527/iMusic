package com.android.imusic.music.bean;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 * 作者信息
 */

public class SearchMusicAnchor {

    /**
     * author_id : 722869
     * author_name : 毛不易
     * avatar : http://singerimg.kugou.com/uploadpic/softhead/400/20180611/20180611160019456.jpg
     * is_publish : 1
     * sizable_avatar : http://singerimg.kugou.com/uploadpic/softhead/{size}/20180611/20180611160019456.jpg
     */

    private String author_id;
    private String author_name;
    private String avatar;
    private String is_publish;
    private String sizable_avatar;

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIs_publish() {
        return is_publish;
    }

    public void setIs_publish(String is_publish) {
        this.is_publish = is_publish;
    }

    public String getSizable_avatar() {
        return sizable_avatar;
    }

    public void setSizable_avatar(String sizable_avatar) {
        this.sizable_avatar = sizable_avatar;
    }

    @Override
    public String toString() {
        return "SearchMusicAnchor{" +
                "author_id='" + author_id + '\'' +
                ", author_name='" + author_name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", is_publish='" + is_publish + '\'' +
                ", sizable_avatar='" + sizable_avatar + '\'' +
                '}';
    }
}
