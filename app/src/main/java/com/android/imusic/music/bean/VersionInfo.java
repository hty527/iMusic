package com.android.imusic.music.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/15
 * 版本信息
 */

public class VersionInfo implements Parcelable{

    /**
     * compel_update : 0
     * down_url : https://raw.githubusercontent.com/Yuye584312311/IMusic/master/Doc/apk/iMusic-1.0.2.apk
     * size : 2.64
     * update_log : 更新内容： 1.全新电影界面，呈现更多精彩 2.全新视频播放交互，更懂你的播放器 3.全新盒子中心，精彩内容不再错过
     * version : 1.0.2
     * version_code : 10020
     */

    private int compel_update;
    private String down_url;
    private String size;
    private String update_log;
    private String version;
    private int version_code;

    protected VersionInfo(Parcel in) {
        compel_update = in.readInt();
        down_url = in.readString();
        size = in.readString();
        update_log = in.readString();
        version = in.readString();
        version_code = in.readInt();
    }

    public static final Creator<VersionInfo> CREATOR = new Creator<VersionInfo>() {
        @Override
        public VersionInfo createFromParcel(Parcel in) {
            return new VersionInfo(in);
        }

        @Override
        public VersionInfo[] newArray(int size) {
            return new VersionInfo[size];
        }
    };

    public int getCompel_update() {
        return compel_update;
    }

    public void setCompel_update(int compel_update) {
        this.compel_update = compel_update;
    }

    public String getDown_url() {
        return down_url;
    }

    public void setDown_url(String down_url) {
        this.down_url = down_url;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUpdate_log() {
        return update_log;
    }

    public void setUpdate_log(String update_log) {
        this.update_log = update_log;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "compel_update=" + compel_update +
                ", down_url='" + down_url + '\'' +
                ", size='" + size + '\'' +
                ", update_log='" + update_log + '\'' +
                ", version='" + version + '\'' +
                ", version_code=" + version_code +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(compel_update);
        dest.writeString(down_url);
        dest.writeString(size);
        dest.writeString(update_log);
        dest.writeString(version);
        dest.writeInt(version_code);
    }
}