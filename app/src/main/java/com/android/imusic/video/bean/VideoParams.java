package com.android.imusic.video.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * hty_Yuye@Outlook.com
 * 2019/4/10
 */

public class VideoParams implements Parcelable{

    private long videoiId;
    private String videoTitle;
    private String videoCover;
    private String videoDesp;
    private String videoUrl;
    private String nickName;
    private String userFront;
    private String userSinger;
    private long previewCount;
    private long durtion;
    private long lastTime;
    private String headTitle;

    public VideoParams(){}

    protected VideoParams(Parcel in) {
        videoiId = in.readLong();
        videoTitle = in.readString();
        videoCover = in.readString();
        videoDesp = in.readString();
        videoUrl = in.readString();
        nickName = in.readString();
        userFront = in.readString();
        userSinger = in.readString();
        previewCount = in.readLong();
        durtion = in.readLong();
        lastTime = in.readLong();
        headTitle = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(videoiId);
        dest.writeString(videoTitle);
        dest.writeString(videoCover);
        dest.writeString(videoDesp);
        dest.writeString(videoUrl);
        dest.writeString(nickName);
        dest.writeString(userFront);
        dest.writeString(userSinger);
        dest.writeLong(previewCount);
        dest.writeLong(durtion);
        dest.writeLong(lastTime);
        dest.writeString(headTitle);
    }

    public static final Creator<VideoParams> CREATOR = new Creator<VideoParams>() {
        @Override
        public VideoParams createFromParcel(Parcel in) {
            return new VideoParams(in);
        }

        @Override
        public VideoParams[] newArray(int size) {
            return new VideoParams[size];
        }
    };

    public long getVideoiId() {
        return videoiId;
    }

    public void setVideoiId(long videoiId) {
        this.videoiId = videoiId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getVideoDesp() {
        return videoDesp;
    }

    public void setVideoDesp(String videoDesp) {
        this.videoDesp = videoDesp;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserFront() {
        return userFront;
    }

    public void setUserFront(String userFront) {
        this.userFront = userFront;
    }

    public String getUserSinger() {
        return userSinger;
    }

    public void setUserSinger(String userSinger) {
        this.userSinger = userSinger;
    }

    public long getPreviewCount() {
        return previewCount;
    }

    public void setPreviewCount(long previewCount) {
        this.previewCount = previewCount;
    }

    public long getDurtion() {
        return durtion;
    }

    public void setDurtion(long durtion) {
        this.durtion = durtion;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getHeadTitle() {
        return headTitle;
    }

    public void setHeadTitle(String headTitle) {
        this.headTitle = headTitle;
    }
}