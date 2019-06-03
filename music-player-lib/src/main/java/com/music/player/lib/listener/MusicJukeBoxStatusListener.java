package com.music.player.lib.listener;

import android.view.View;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicLrcRow;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/6
 * Music JukeBox Status Listener
 */

public interface MusicJukeBoxStatusListener {

    /**
     * 手指横向滚动过程对象不断变化
     * @param audioInfo 音频对象
     */
    void onJukeBoxOffsetObject(BaseAudioInfo audioInfo);

    /**
     * 音频对象切换了，这个是切换了Pager之后，完全静止下回调，配合onJukeBoxFlashObjectChanged使用，不要做重复的事
     * @param position 索引
     * @param audioInfo 音频对象
     * @param isEchoDisplay 是否回显
     */
    void onJukeBoxObjectChanged(int position, BaseAudioInfo audioInfo,boolean isEchoDisplay);

    /**
     * 松手瞬间发生了变化，发生在切换Pager瞬间，配合onJukeBoxObjectChanged使用，不要做重复的时间
     * @param position 索引
     * @param audioInfo 音频对象
     * @param isEchoDisplay 是否回显
     */
    void onJukeBoxFlashObjectChanged(int position, BaseAudioInfo audioInfo,boolean isEchoDisplay);

    /**
     * 唱片机状态发生了变化
     * @param playerState 唱片机状态,参见MusicConstants常量定义
     */
    void onJukeBoxState(int playerState);

    /**
     * 唱片机点击事件,主要抛出给组件来控制歌词控件
     * @param view click view
     */
    void onClickJukeBox(View view);

    /**
     * 歌词被拖动了，跳转至指定位置开始播放
     * @param lrcRow 歌词对象
     */
    void onLrcSeek(MusicLrcRow lrcRow);
}