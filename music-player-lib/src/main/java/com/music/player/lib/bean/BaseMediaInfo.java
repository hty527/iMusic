package com.music.player.lib.bean;

import java.io.Serializable;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/5
 * Music Data
 * 除标记必须赋值的字段外，其他字段为非必须赋值的字段，非赋值字段取决于你的业务功能逻辑
 */

public class BaseMediaInfo implements Serializable {

    private long id;//文件ID
    //第三方网络歌曲唯一标识，这个标识只适用于本Demo，由于没有合适的ID字段，故使用这个字段作为一标识
    private String hashKey="";
    //文件大小
    private long file_size;
    //多媒体时长 *必须
    private long video_durtion;
    //多媒体类型  0：图片 1：视频  3：音频
    private int file_type;
    //专辑名称
    private String mediaAlbum;
    //音频类型
    private String mediaType;
    //多媒体封面 *必须
    private String img_path;
    //作者昵称 *必须
    private String nickname;
    //作者ID
    private String userid;
    //作者头像
    private String avatar;
    //真实文件地址 *必须
    private String file_path;
    //多媒体描述 *必须
    private String video_desp;//这个字段由于我司项目的原因，使用的是这个字段名称。
    //单价
    private int price;
    //交互
    protected boolean isSelected;
    //最近播放时间
    private long lastPlayTime;

    public BaseMediaInfo(){

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public long getVideo_durtion() {
        return video_durtion;
    }

    public void setVideo_durtion(long video_durtion) {
        this.video_durtion = video_durtion;
    }

    public int getFile_type() {
        return file_type;
    }

    public void setFile_type(int file_type) {
        this.file_type = file_type;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getVideo_desp() {
        return video_desp;
    }

    public void setVideo_desp(String video_desp) {
        this.video_desp = video_desp;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getMediaAlbum() {
        return mediaAlbum;
    }

    public void setMediaAlbum(String mediaAlbum) {
        this.mediaAlbum = mediaAlbum;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    @Override
    public String toString() {
        return "BaseMediaInfo{" +
                "id=" + id +
                ", hashKey='" + hashKey + '\'' +
                ", file_size=" + file_size +
                ", video_durtion=" + video_durtion +
                ", file_type=" + file_type +
                ", img_path='" + img_path + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userid='" + userid + '\'' +
                ", avatar='" + avatar + '\'' +
                ", file_path='" + file_path + '\'' +
                ", video_desp='" + video_desp + '\'' +
                ", mediaAlbum='" + mediaAlbum + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", price=" + price +
                ", isSelected=" + isSelected +
                ", lastPlayTime=" + lastPlayTime +
                '}';
    }
}