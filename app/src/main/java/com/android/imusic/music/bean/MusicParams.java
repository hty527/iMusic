package com.android.imusic.music.bean;

import java.util.List;

/**
 * Created by TinyHung@outlook.com
 * 2019/6/21
 * 音乐播放器界面跳转所需参数
 */

public class MusicParams {

    private List<AudioInfo> audioInfos;

    public List<AudioInfo> getAudioInfos() {
        return audioInfos;
    }

    public void setAudioInfos(List<AudioInfo> audioInfos) {
        this.audioInfos = audioInfos;
    }

    @Override
    public String toString() {
        return "MusicParams{" +
                "audioInfos=" + audioInfos +
                '}';
    }
}