package com.music.player.lib.iinterface;

import android.text.TextUtils;
import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.listener.MusicLrcParserCallBack;
import com.music.player.lib.util.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * Music Lrc parser
 * 此歌词解析器默认使用rxJava实现IO解析，如果不能满足你的解析需求
 * 请继承此类复写formatLrc或者parserLineLrc方法
 *
 */

public abstract class MusicLrcRowParser {

    protected static final String TAG = "MusicLrcRowParser";
    //匹配两种歌词形式[00:00.00] [00:00]
    String RGEX1="\\[([0-9]+:[0-9]+.[0-9]+)\\]";
    String RGEX2="\\[([\\d]+:[0-9]+)\\]";
    protected Subscription mSubscribe;

    /**
     * 异步方法，解析歌词字符串为java数组对象，工作内容：逐行读取歌词文件，调用parserLineLrc逐行包装为歌词对象
     * @param lrcContent 源歌词字符串
     * @param callBack 回调
     */
    public void formatLrc(String lrcContent, final MusicLrcParserCallBack callBack){
        if(TextUtils.isEmpty(lrcContent)){
            if(null!=callBack){
                callBack.onLrcRows(null);
            }
            return;
        }
        if(null==callBack){
            return;
        }
        if(null!=mSubscribe){
            mSubscribe.unsubscribe();
            mSubscribe=null;
        }
        mSubscribe =rx.Observable
                .just(lrcContent)
                .map(new Func1<String, List<MusicLrcRow>>() {
                    @Override
                    public List<MusicLrcRow> call(String lrcContent) {
                        return formatParserLrc(lrcContent);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MusicLrcRow>>() {
                    @Override
                    public void call(List<MusicLrcRow> lrcRows) {
                        if(null!=callBack){
                            callBack.onLrcRows(lrcRows);
                        }
                    }
                });
    }

    /**
     * 逐行解析歌词并封装为数组，当出现多个时间标签会存在数组，复写请注意！
     * 工作内容：逐行将String类型歌词包装为歌词数组对象
     * @param lrcLineContent 源歌词逐行字符串
     * @return 解析后的歌词格式
     */
     public List<MusicLrcRow> parserLineLrc(String lrcLineContent){
         if(TextUtils.isEmpty(lrcLineContent)){
             return null;
         }
         //先正则过滤一遍
         boolean b = Pattern.compile(RGEX1).matcher(lrcLineContent).find();
         if(!b){
             boolean b1 = Pattern.compile(RGEX2).matcher(lrcLineContent).find();
             //不匹配再正则第二种
             if(!b1){
                 //两种都未匹配成功，放弃了
                 return null;
             }
         }
         //再文本长度过滤一遍
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
     * 根据歌词内容解析为java list
     * @param lrcContent 源歌词字符串
     * @return 可能有多行的歌词数组
     */
    private List<MusicLrcRow> formatParserLrc(String lrcContent) {
        StringReader reader=new StringReader(lrcContent);
        BufferedReader bufferedReader=new BufferedReader(reader);
        String lineContent=null;
        List<MusicLrcRow> lrcRowList=new ArrayList<>();
        //逐行解析歌词为java对象
        try {
            do {
                try {
                    lineContent=bufferedReader.readLine();
                    List<MusicLrcRow> parserLrcs = parserLineLrc(lineContent);
                    if(null!=parserLrcs&&parserLrcs.size()>0){
                        for (int i = 0; i < parserLrcs.size(); i++) {
                            lrcRowList.add(parserLrcs.get(i));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    reader.close();
                    try {
                        bufferedReader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }while (lineContent!=null);
        }catch (RuntimeException e){
            e.printStackTrace();
            reader.close();
            try {
                bufferedReader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
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

    public void onDestroy(){
        if(null!=mSubscribe){
            mSubscribe.unsubscribe();
            mSubscribe=null;
        }
    }
}