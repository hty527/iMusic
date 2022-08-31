package com.music.player.lib.bean;

import java.io.Serializable;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/5
 * Music java Bean
 * 带 * 标识的成员为必须赋值类型成员
 */

public class BaseAudioInfo implements Serializable {
    /**
     * 必选字段
     */
    // * 音频唯一标识ID
    private long audioId;
    // * 音频时长
    private long audioDurtion;
    // * 音频名称
    private String audioName;
    // * 音频封面
    private String audioCover;
    // * 音频资源地址
    private String audioPath;
    // * 作者昵称
    private String nickname;
    /**
     * 非必选
     */
    //作者ID
    private String userid;
    //作者头像
    private String avatar;
    //文件大小
    private long audioSize;
    //专辑名称
    private String audioAlbumName;
    //音频类型
    private String audioType;
    //多媒体描述
    private String audioDescribe;
    //这个字段请忽视。。。这个标识只适用于本Demo的酷狗API，由于数据中没有合适的ID字段，故使用这个字段作为一标识
    private String audioHashKey="";
    //本地缓存时用到
    private long addtime;

    /**
     * 本地UI和历史记录交互字段
     */
    //UI正在播放选中交互
    protected boolean isSelected;
    //最近播放时间
    private long lastPlayTime;

    public BaseAudioInfo(){

    }

    public long getAudioId() {
        return audioId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    public long getAudioDurtion() {
        return audioDurtion;
    }

    public void setAudioDurtion(long audioDurtion) {
        this.audioDurtion = audioDurtion;
    }

    public String getAudioName() {
        return audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public String getAudioCover() {
        return audioCover;
    }

    public void setAudioCover(String audioCover) {
        this.audioCover = audioCover;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getAudioSize() {
        return audioSize;
    }

    public void setAudioSize(long audioSize) {
        this.audioSize = audioSize;
    }

    public String getAudioAlbumName() {
        return audioAlbumName;
    }

    public void setAudioAlbumName(String audioAlbumName) {
        this.audioAlbumName = audioAlbumName;
    }

    public String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public String getAudioDescribe() {
        return audioDescribe;
    }

    public void setAudioDescribe(String audioDescribe) {
        this.audioDescribe = audioDescribe;
    }

    public String getAudioHashKey() {
        return audioHashKey;
    }

    public void setAudioHashKey(String audioHashKey) {
        this.audioHashKey = audioHashKey;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public long getAddtime() {
        return addtime;
    }

    public void setAddtime(long addtime) {
        this.addtime = addtime;
    }

    @Override
    public String toString() {
        return "BaseaudioInfo{" +
                "audioId=" + audioId +
                ", audioDurtion=" + audioDurtion +
                ", audioName='" + audioName + '\'' +
                ", audioCover='" + audioCover + '\'' +
                ", audioPath='" + audioPath + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userid='" + userid + '\'' +
                ", avatar='" + avatar + '\'' +
                ", audioSize=" + audioSize +
                ", audioAlbumName='" + audioAlbumName + '\'' +
                ", audioType='" + audioType + '\'' +
                ", audioDescribe='" + audioDescribe + '\'' +
                ", audioHashKey='" + audioHashKey + '\'' +
                ", isSelected=" + isSelected +
                ", lastPlayTime=" + lastPlayTime +
                ", addtime=" + addtime +
                '}';
    }
}