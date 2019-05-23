package com.music.player.lib.model;

import android.text.TextUtils;
import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.exinterface.MusicLrcRowParser;
import com.music.player.lib.util.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * default lrc parser
 * 本默认解析器支持的歌词格式:[01:15.33] 或者 [00:00]
 * 如果此默认的解析器满足不了你的需求，请扩展MusicLrcRowParser自行解析
 */

public final class MusicDefaultLrcParser implements MusicLrcRowParser {

    private static final String TAG = "MusicDefaultLrcParser";

    /**
     * 歌词解析为对象
     * @param lrcLineContent 源歌词逐行内容,，如：[00:04.69]也许很远或是昨天
     * @return 解析后的歌词数组
     */
    @Override
    public List<MusicLrcRow> parser(String lrcLineContent) {

        if(TextUtils.isEmpty(lrcLineContent)){
            return null;
        }
        boolean form1 = lrcLineContent.indexOf("[") == 0 && lrcLineContent.indexOf("]") == 9;
        boolean form2 = lrcLineContent.indexOf("[") == 0 && lrcLineContent.indexOf("]") == 6;
        if(!form1&&!form2){
            Logger.d(TAG,"过滤不合法的正文歌词");
            return null;
        }
        //此行歌词的最后一个时间节点
        int lastTimeForm = lrcLineContent.lastIndexOf("]");
        //格式化歌词内容
        String content = lrcLineContent.substring(lastTimeForm + 1, lrcLineContent.length());
        //格式化歌词时间为-01:15.33-格式，方便后面切割
        String times = lrcLineContent.substring(0, lastTimeForm + 1).replace("[", "-")
                .replace("]", "-");
        String[] split = times.split("-");
//            Logger.d(TAG,"parser-->\ncontent:"+content+"\ntimes:"+times+"\nsplit:"+split.toString());
        List<MusicLrcRow> lrcRowList=new ArrayList<>();
        for (String s : split) {
            if(s.trim().length()==0){
                continue;
            }
            lrcRowList.add(new MusicLrcRow(content,s,formatTime(s)));
        }
        return lrcRowList;
    }

    /**
     * 将 01:15.33 或者 00:00 格式歌词格式化为毫秒时间
     * @param time
     * @return 毫秒时间
     */
    private long formatTime(String time) {
        if(TextUtils.isEmpty(time)){
            return 0;
        }
        //替换为统一分隔符
        String replace = time.replace(".", ":");
        String[] split = replace.split(":");
        if(split.length>0){
            if(split.length==3){
                int minute = Integer.parseInt(split[0]) * 60 * 1000;
                int second = Integer.parseInt(split[1]) * 1000;
                int millis = Integer.parseInt(split[2]);
                return minute+second+millis;
            }
            if(split.length==2){
                int minute = Integer.parseInt(split[0]) * 60 * 1000;
                int second = Integer.parseInt(split[1]) * 1000;
                return minute+second;
            }
            int minute = Integer.parseInt(split[0]) * 60 * 1000;
            return minute;
        }
        return 0;
    }
}