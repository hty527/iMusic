package com.music.player.lib.listener;

import com.music.player.lib.bean.MusicLrcRow;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 */

public interface MusicLrcViewListener {
    /**
     * 持续滚动高亮显示变化
     * @param position 位置
     * @param lrcRow 歌词实体类
     */
    void onLrcSeeked(int position, MusicLrcRow lrcRow);
}