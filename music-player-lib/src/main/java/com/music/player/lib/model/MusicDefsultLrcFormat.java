package com.music.player.lib.model;

import android.os.Debug;
import android.text.TextUtils;
import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.exinterface.MusicLrcRowFormat;
import com.music.player.lib.exinterface.MusicLrcRowParser;
import com.music.player.lib.listener.MusicLrcParserCallBack;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * Default lrc format
 */

public final class MusicDefsultLrcFormat implements MusicLrcRowFormat {

    //歌词解析器
    private MusicLrcRowParser mLrcRowParser;
    private Subscription mSubscribe;

    /**
     * 异步的歌词解析
     * @param lrcContent 源歌词字符串
     * @param lrcRowParser 解析器
     * @param callBack 回调
     */
    @Override
    public void formatLrcFromString(String lrcContent, MusicLrcRowParser lrcRowParser, final MusicLrcParserCallBack callBack){
        if(TextUtils.isEmpty(lrcContent)){
            if(null!=callBack){
                callBack.onLrcRows(null);
            }
            return;
        }
        if(null!=lrcRowParser){
            mLrcRowParser=lrcRowParser;
        }
        if(null==mLrcRowParser){
            mLrcRowParser=new MusicDefaultLrcParser();
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
                        return formatParserLrc(lrcContent, mLrcRowParser);
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
     * 根据歌词内容解析为java list
     * @param lrcContent 源歌词字符串
     * @param lrcRowParser 解析器
     * @return 可能有多行的歌词数组
     */
    private List<MusicLrcRow> formatParserLrc(String lrcContent,MusicLrcRowParser lrcRowParser) {
        StringReader reader=new StringReader(lrcContent);
        BufferedReader bufferedReader=new BufferedReader(reader);
        String lineContent=null;
        List<MusicLrcRow> lrcRowList=new ArrayList<>();
        //逐行解析歌词为java对象
        try {
            do {
                try {
                    lineContent=bufferedReader.readLine();
                    List<MusicLrcRow> parserLrcs = lrcRowParser.parser(lineContent);
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

    public void onDestroy(){
        if(null!=mSubscribe){
            mSubscribe.unsubscribe();
            mSubscribe=null;
        }
    }
}