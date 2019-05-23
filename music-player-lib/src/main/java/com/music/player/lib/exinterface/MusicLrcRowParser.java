package com.music.player.lib.exinterface;

import com.music.player.lib.bean.MusicLrcRow;
import java.util.List;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * Music Lrc parser
 * 实现自己的逐行歌词解析，请实现此接口
 */

public interface MusicLrcRowParser {
    /**
     * 解析歌词
     * @param lrcLineContent 源歌词逐行字符串
     * @return 解析后的歌词格式
     */
     List<MusicLrcRow> parser(String lrcLineContent);
}