package com.music.player.lib.model;

import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.iinterface.MusicLrcRowParser;
import com.music.player.lib.listener.MusicLrcParserCallBack;
import java.util.List;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * default lrc parser
 * 默认解析器支持的歌词格式:[01:15.33] 或者 [00:00]
 * 使用默认的歌词解析器，啥也不做
 */

public final class MusicDefaultLrcParser extends MusicLrcRowParser {
    /**
     * 解析歌词源文本为java数组，如果不能满足你的需求，请复写实现自己的逻辑
     * 此方法被设计为异步回调
     * @param lrcContent 源歌词字符串
     * @param callBack 回调
     */
    @Override
    public void formatLrc(String lrcContent, MusicLrcParserCallBack callBack) {
        super.formatLrc(lrcContent, callBack);
    }

    /**
     * 逐行解析歌词源文本为java数组，如果不能满足你的需求，请复写实现自己的逻辑
     * @param lrcLineContent 源歌词逐行字符串
     * @return 逐行的歌词数组
     */
    @Override
    public List<MusicLrcRow> parserLineLrc(String lrcLineContent) {
        return super.parserLineLrc(lrcLineContent);
    }
}