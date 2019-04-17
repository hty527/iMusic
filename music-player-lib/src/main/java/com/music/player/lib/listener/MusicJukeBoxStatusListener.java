package com.music.player.lib.listener;

import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.model.MusicPlayerStatus;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/6
 * Music JukeBox Status Listener
 */

public interface MusicJukeBoxStatusListener {

    /**
     * 手指横向滚动过程对象变化
     * @param mediaInfo 音频对象
     */
    void onJukeBoxOffsetObject(BaseMediaInfo mediaInfo);

    /**
     * 音频对象切换了
     * @param position 索引
     * @param musicPicRes 音频对象
     * @param isEchoDisplay 是否回显
     */
    void onJukeBoxObjectChanged(int position,BaseMediaInfo musicPicRes,boolean isEchoDisplay);

    /**
     * 唱片机状态发生了变化
     * @param playerState 唱片机状态
     */
    void onJukeBoxState(MusicPlayerStatus playerState);
}