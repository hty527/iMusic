package com.android.imusic.music.model;

import android.text.TextUtils;
import com.android.imusic.music.bean.SearchMusicData;
import com.android.imusic.net.bean.ResultData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.iinterface.MusicLrcRowParser;
import com.music.player.lib.listener.MusicLrcParserCallBack;
import com.music.player.lib.util.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/24
 * 自定义同步的网络歌词解析器
 */

public class MusicLrcRowParserEngin extends MusicLrcRowParser {

    private OkHttpClient mHttpClient;
    private Gson mGson;

    /**
     * 异步获取歌词并解析成歌词数组，这里根据hashKey获取歌词文件后解析
     * @param hashKey 酷狗音乐唯一标识
     * @param callBack 回调
     */
    @Override
    public void formatLrc(String hashKey, final MusicLrcParserCallBack callBack) {
        if(null!=mSubscribe){
            mSubscribe.unsubscribe();
            mSubscribe=null;
        }
        mSubscribe =rx.Observable
                .just(hashKey)
                .map(new Func1<String, List<MusicLrcRow>>() {
                    @Override
                    public List<MusicLrcRow> call(String hashKey) {
                        return getNetLrcToRows(hashKey);
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
     * 联网获取歌词文件，同步获取后返回解析的歌词java数组
     * @param hashKey 酷狗音乐唯一标识
     * @return 可能有多行的歌词数组
     */
    private List<MusicLrcRow> getNetLrcToRows(String hashKey) {

        Logger.d(TAG,"getLrcToRows-->hashKey："+hashKey);
        Request.Builder builder = new Request.Builder();
        Map<String ,String> params=new HashMap<>();
        params.put("r","play/getdata");
        params.put("hash",hashKey);
        Map<String ,String> headers=new HashMap<>();
        headers.put("Access-Control-Allow-Origin","*");
        headers.put("Accept","*/*");
        headers.put("Cookie", "kg_mid=8a18832e9fc0845106e1075df481c1c2;Hm_lvt_aedee6983d4cfc62f509129360d6bb3d=1557584633");
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0");
        buildHeaderToRequest(builder,headers);
        String url = buildParamsToUrl("http://www.kugou.com/yy/index.php", params);
        Request request = builder.url(url).build();
        try {
            Response response = createHttpUtils().newCall(request).execute();
            if(null!=response&&200==response.code()){
                try {
                    String string = response.body().string();
                    if(!TextUtils.isEmpty(string)){
                        final Object resultInfo = getResultInfo(string, new TypeToken<ResultData<SearchMusicData>>(){}.getType());
                        if(null!=resultInfo){
                            ResultData<SearchMusicData> resultData= (ResultData<SearchMusicData>) resultInfo;
                            if(null!=resultData.getData()){
                                if(!TextUtils.isEmpty(resultData.getData().getLyrics())){
                                    return formatLrcByString(resultData.getData().getLyrics());
                                }
                            }
                        }
                    }
                    return null;
                } catch (final IOException e) {
                    e.printStackTrace();
                }catch (final RuntimeException e){
                    e.printStackTrace();
                }
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 解析歌词文件
     * @param lrcContent 源歌词文本
     * @return
     */
    private List<MusicLrcRow> formatLrcByString(String lrcContent) {
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
     * JSON解析,这里不再返回T了，直接Object
     * @param json json字符串
     * @param type 指定class
     * @return 泛型实体对象
     */
    public Object getResultInfo(String json,Type type){
        Object resultData = null;
        if(null==mGson){
            mGson=new Gson();
        }
        try {
            if(null!=type){
                resultData=mGson.fromJson(json, type);
            }else{
                //如果用户没有指定Type,则直接使用String.class
                resultData=mGson.fromJson(json, new TypeToken<String>(){}.getType());
            }
            return resultData;
        }catch (RuntimeException e){
            e.printStackTrace();
            return resultData;
        }
    }

    /**
     * 添加Header参数
     * @param builder requst
     * @param headers 头部参数
     * @return 设置参数后的builder
     */
    private Request.Builder buildHeaderToRequest(Request.Builder builder, Map<String, String> headers) {
        if(null!=headers&&headers.size()>0){
            Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                builder.header(next.getKey(),next.getValue());
            }
        }
        return builder;
    }

    /**
     * 根据MAP构建URL
     * @param url 原URL
     * @param params 参数
     * @return 组合后的URL
     */
    private String buildParamsToUrl(String url, Map<String, String> params) {
        StringBuilder stringBuilder=new StringBuilder(url);
        if(null!=params&&params.size()>0){
            boolean hasParams=false;
            if(url.contains("?")){
                //如果URL中已经存在参数，标记一下
                hasParams=true;
            }
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                if(i==0 && !hasParams){
                    //如果不存在参数，拼接第一个参数
                    stringBuilder.append( "?" +next.getKey()+ "=" +next.getValue());
                }else{
                    stringBuilder.append( "&" +next.getKey()+ "=" +next.getValue());
                }
                i++;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 创建HttpClient
     * @return HttpClient example
     */
    private OkHttpClient createHttpUtils() {
        if(null==mHttpClient){
            synchronized (OkHttpClient.class){
                mHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .cookieJar(new CookieJar() {
                            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                            @Override
                            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                                cookieStore.put(url.host(), cookies);
                            }
                            @Override
                            public List<Cookie> loadForRequest(HttpUrl url) {
                                List<Cookie> cookies = cookieStore.get(url.host());
                                return cookies != null ? cookies : new ArrayList<Cookie>();
                            }
                        })
                        .build();
            }
        }
        return mHttpClient;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHttpClient=null;
        mGson=null;
    }
}