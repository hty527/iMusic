package com.music.player.lib.listener;

import com.music.player.lib.bean.MusicLrcRow;
import java.util.List;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * Music lrc parser call back
 */

public interface MusicLrcParserCallBack {
    /**
     * 回调在主线程的歌词数据
     * @param lrcRows 歌词文件
     */
    void onLrcRows(List<MusicLrcRow> lrcRows);
}