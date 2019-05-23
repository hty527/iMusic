package com.music.player.lib.exinterface;

import com.music.player.lib.listener.MusicLrcParserCallBack;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * MusicLrc Format
 * 实现自己的歌词解析，请实现此接口
 */

public interface MusicLrcRowFormat {
    /**
     * 异步方法，构造歌词为数组对象
     * @param lrcContent 源歌词字符串
     * @param parser 解析器，为空则使用默认
     * @param callBack 回调
     */
    void formatLrcFromString(String lrcContent,MusicLrcRowParser parser,MusicLrcParserCallBack callBack);
}