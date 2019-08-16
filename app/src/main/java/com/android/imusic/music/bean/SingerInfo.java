package com.android.imusic.music.bean;

/**
 * TinyHung@Outlook.com
 * 2019/4/2
 */

public class SingerInfo {

    /**
     * id : 1003
     * create_nickname : 黄天宇
     * create_avatar : http://b211.photo.store.qq.com/psu?/2f5cb74f-447c-4cd6-b5a5-6483fe1731d9/4Wwe1NUA.pBWhOqWJ925536YHCoqPAEoYlMJknPJ0.s!/b/YdfQ0X2cUgAAYoG2y310UwAA&bo=ngKeAgAAAAABBCM!&rf=viewer_4
     * song_title : 林俊杰精选
     * song_front : https://p3fx.kgimg.com/stdmusic/20150227/20150227154441807468.jpg
     * singer_nickname : 林俊杰
     * singer_avatar : https://p3fx.kgimg.com/stdmusic/20150227/20150227154441807468.jpg
     * preview_num : 546223526
     */

    private int id;
    private String create_nickname;
    private String create_avatar;
    private String song_title;
    private String song_front;
    private String singer_nickname;
    private String singer_avatar;
    private long preview_num;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreate_nickname() {
        return create_nickname;
    }

    public void setCreate_nickname(String create_nickname) {
        this.create_nickname = create_nickname;
    }

    public String getCreate_avatar() {
        return create_avatar;
    }

    public void setCreate_avatar(String create_avatar) {
        this.create_avatar = create_avatar;
    }

    public String getSong_title() {
        return song_title;
    }

    public void setSong_title(String song_title) {
        this.song_title = song_title;
    }

    public String getSong_front() {
        return song_front;
    }

    public void setSong_front(String song_front) {
        this.song_front = song_front;
    }

    public String getSinger_nickname() {
        return singer_nickname;
    }

    public void setSinger_nickname(String singer_nickname) {
        this.singer_nickname = singer_nickname;
    }

    public String getSinger_avatar() {
        return singer_avatar;
    }

    public void setSinger_avatar(String singer_avatar) {
        this.singer_avatar = singer_avatar;
    }

    public long getPreview_num() {
        return preview_num;
    }

    public void setPreview_num(long preview_num) {
        this.preview_num = preview_num;
    }

    @Override
    public String toString() {
        return "SingerInfo{" +
                "id=" + id +
                ", create_nickname='" + create_nickname + '\'' +
                ", create_avatar='" + create_avatar + '\'' +
                ", song_title='" + song_title + '\'' +
                ", song_front='" + song_front + '\'' +
                ", singer_nickname='" + singer_nickname + '\'' +
                ", singer_avatar='" + singer_avatar + '\'' +
                ", preview_num=" + preview_num +
                '}';
    }
}