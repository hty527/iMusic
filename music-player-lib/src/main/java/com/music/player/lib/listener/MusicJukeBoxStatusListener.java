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
     * 手指横向持续滚动过程对象不断的发生变化，持续调用，请不要在此方法做耗时操作
     * @param audioInfo 音频对象
     */
    void onScrollOffsetObject(BaseAudioInfo audioInfo);

    /**
     * Pager处于可见状态
     * @param audioInfo 音频对象
     * @param newPosition 刚刚处于可见状态的Position
     */
    void onVisible(BaseAudioInfo audioInfo,int newPosition);

    /**
     * Pager处于不可见状态
     * @param oldPosition 不可见状态的Position
     */
    void onInvisible(int oldPosition);

    /**
     * 新的Pager落地了，唱片机在Pager滚动后，最终静止不动，回调此方法
     * @param offsetPosition 索引
     * @param audioInfo 音频对象
     * @param startPlayer true:播放事件 false:只是回显同步状态
     */
    void onOffsetPosition(int offsetPosition, BaseAudioInfo audioInfo,boolean startPlayer);

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