package com.android.imusic.net;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/8
 * 简单的 OkHttpUtils 实现
 */

public final class OkHttpUtils {

    private static final String TAG = "OkHttpUtils";
    private static volatile OkHttpUtils mInstance;
    private static OkHttpClient mHttpClient;
    //调试模式开关
    public static boolean DEBUG=true;
    //是否正在请求
    public static boolean isRequst=false;
    //共有参数
    private static HashMap<String, String> mDefaultParams;
    private static Handler mHandler;
    private static Gson mGson;
    //IO异常，一般发生在同步请求下
    public static final int ERROR_IO = 3000;
    //OkHttpUtils内部异常
    public static final int ERROR_INVALID = 3001;
    //返回正常但数据为空
    public static final int ERROR_EMPTY = 3002;
    //JSON解析失败
    public static final int ERROR_JSON_FORMAT = 3003;

    public static OkHttpUtils getInstance(){
        if(null==mInstance){
            synchronized (OkHttpUtils.class){
                if(null==mInstance){
                    mInstance=new OkHttpUtils();
                }
            }
        }
        return mInstance;
    }

    private OkHttpUtils(){
        mHttpClient=createHttpUtils();
        mGson = new Gson();
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

    /**
     * 设置默认参数
     * @param defaultParams 键值对参数
     */
    public static void setDefaultParams(HashMap<String,String> defaultParams){
        mDefaultParams=defaultParams;
    }


    /**
     * 对外的异步方法  GET Requst
     * @param url host api
     * @param callBack 回调
     */
    public static void get(String url,OnResultCallBack callBack){
        get(url,null,null,callBack);
    }

    /**
     * 对外的异步方法  GET Requst
     * @param url host api
     * @param params 键值对参数
     * @param callBack 回调
     */
    public static void get(String url,Map<String,String> params,OnResultCallBack callBack){
        OkHttpUtils.getInstance().sendGetRequst(url,params,null,callBack,false);
    }


    /**
     * 对外的异步方法  GET Requst
     * @param url host api
     * @param params 键值对参数
     * @param headers 键值对Header
     * @param callBack 回调
     */
    public static void get(String url,Map<String,String> params,Map<String,String> headers,
                           OnResultCallBack callBack){
        OkHttpUtils.getInstance().sendGetRequst(url,params,headers,callBack,false);
    }

    /**
     *
     * 对外的异同步方法  GET Requst
     * @param url host api
     * @param params 键值对参数
     * @param headers 键值对Header
     * @param callBack 回调
     */
    public static void getSynchro(String url,Map<String,String> params,Map<String,String> headers,
                                  OnResultCallBack callBack){
        OkHttpUtils.getInstance().sendGetRequst(url,params,headers,callBack,true);
    }

    /**
     * 对外的异步方法  POST Requst
     * @param url host api
     * @param callBack 回调
     */
    public static void post(String url, OnResultCallBack callBack){
        post(url,null,null,callBack);
    }

    /**
     * 对外的异步方法 POST Requst
     * @param url host api
     * @param params 键值对参数
     * @param callBack 回调
     */
    public static void post(String url, Map<String,String> params, OnResultCallBack callBack){
        OkHttpUtils.getInstance().sendPostRequst(url,params,null,callBack,false);
    }


    /**
     * 对外的异步方法 POST Requst
     * @param url host api
     * @param params 键值对参数
     * @param headers 键值对Header
     * @param callBack 回调
     */
    public static void post(String url, Map<String,String> params,Map<String,String> headers,
                            OnResultCallBack callBack){
        OkHttpUtils.getInstance().sendPostRequst(url,params,headers,callBack,false);
    }

    /**
     * 对外的同步方法 POST Requst
     * @param url host api
     * @param params 键值对参数
     * @param headers 键值对Header
     * @param callBack 回调
     */
    public static void postSynchro(String url,Map<String,String> params,Map<String,String> headers,
                                   OnResultCallBack callBack){
        OkHttpUtils.getInstance().sendPostRequst(url,params,headers,callBack,true);
    }

    /**
     * 下载文件
     * @param path http\https 文件绝对路径地址
     * @param listener 监听器
     */
    public static void downloadFile(String path,OnDownloadListener listener) {
        OkHttpUtils.getInstance().download(path,null,null,listener);
    }

    /**
     * 下载文件
     * @param path http\https 文件绝对路径地址
     * @param outPutPath 文件输出路径
     * @param listener 监听器
     */
    public static void downloadFile(String path,String outPutPath,OnDownloadListener listener) {
        OkHttpUtils.getInstance().download(path,outPutPath,null,listener);
    }

    /**
     * 下载文件
     * @param path http\https 文件绝对路径地址
     * @param outPutPath 文件输出路径
     * @param outPutFileName 文件名称
     * @param listener 监听器
     */
    public static void downloadFile(String path,String outPutPath,String outPutFileName,OnDownloadListener listener) {
        OkHttpUtils.getInstance().download(path,outPutPath,outPutFileName,listener);
    }

    /**
     * 下载文件
     * @param path http\https 文件绝对路径地址
     * @param outPutPath 文件输出路径
     * @param outPutFileName 文件名称
     * @param listener 监听器
     */
    private void download(final String path, String outPutPath, String outPutFileName, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(path).build();
        //初始化路径
        if(TextUtils.isEmpty(outPutPath)){
            outPutPath = Environment.getExternalStorageDirectory().getAbsoluteFile()
                    + File.separator + "iMusic" + File.separator + "Download" + File.separator;
        }
        final File file=new File(outPutPath);
        if(!file.exists()){
            file.mkdirs();
        }
        //初始化文件名
        if(TextUtils.isEmpty(outPutFileName)){
            outPutFileName=MusicUtils.getInstance().getFileName(path);
        }
        final String finalOutPutFileName = outPutFileName;
        createHttpUtils().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if(DEBUG){
                    Logger.d(TAG,"onFailure-->e:"+e.getMessage()+call.toString());
                }
                isRequst=false;
                if(null!=listener&&null!=mHandler){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(null!=listener){
                                listener.onError(ERROR_IO,e.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, final Response response) {
                if(null!=mHandler&&null!=listener){
                    if(null!=response){
                        if(200==response.code()){
                            try {
                                final File apkDownloadPath = new File(file, finalOutPutFileName);
                                Logger.d(TAG,"目标存储路径："+apkDownloadPath.getAbsolutePath());
                                ResponseBody responseBody = response.body();
                                //已读长度
                                long laterate=0;
                                if(null!=responseBody){
                                    //总长度
                                    long length = responseBody.contentLength();
                                    //已读长度
                                    long readCount = 0;
                                    //输入流
                                    InputStream inputStream = responseBody.byteStream();
                                    //输出流
                                    FileOutputStream outputStream=new FileOutputStream(apkDownloadPath);
                                    byte[] buffer = new byte[1024];
                                    do {
                                        int read = inputStream.read(buffer);
                                        readCount+=read;
                                        // 得到当前进度
                                        final int progress = (int) (((float) readCount / length) * 100);
                                        // 只有当前进度比上一次进度大于等于1，才可以更新进度,避免无效重复刷新
                                        if (progress >= laterate + 1) {
                                            laterate = progress;
                                            if(null!=listener){
                                                listener.progress(progress,length,readCount);
                                            }
                                        }
                                        //下载完毕
                                        if (read <= 0) {
                                            break;
                                        }
                                        outputStream.write(buffer,0,read);
                                    }while (true);
                                    inputStream.close();
                                    outputStream.close();
                                    if(null!=listener&&null!=mHandler){
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(null!=listener){
                                                    listener.onSuccess(apkDownloadPath);
                                                }
                                            }
                                        });
                                    }
                                }else{
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(null!=listener){
                                                listener.onError(response.code(),"body is empty");
                                            }
                                        }
                                    });
                                }
                            } catch (final IOException e) {
                                e.printStackTrace();
                                if(null!=mHandler){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(null!=listener){
                                                listener.onError(ERROR_JSON_FORMAT,e.getMessage());
                                            }
                                        }
                                    });
                                }
                            }catch (final RuntimeException e){
                                e.printStackTrace();
                                if(null!=mHandler){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(null!=listener){
                                                listener.onError(ERROR_JSON_FORMAT,e.getMessage());
                                            }
                                        }
                                    });
                                }
                            }
                        }else{
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(null!=listener){
                                        listener.onError(response.code(),response.message());
                                    }
                                }
                            });
                        }
                    }else{
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(null!=listener){
                                    listener.onError(ERROR_EMPTY,"response is empty");
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    /**
     * 发送GET请求
     * @param url host api
     * @param params 键值对参数
     * @param headers 键值对Header
     * @param callBack 回调
     * @param isSynchro 是否同步调用
     */
    private void sendGetRequst(String url,Map<String, String> params,Map<String,String> headers,OnResultCallBack
            callBack,boolean isSynchro) {

        Request.Builder builder = new Request.Builder();
        //初始化共有参数
        if(null==params){
            params=new HashMap<>();
        }
        if(null!=mDefaultParams){
            params.putAll(mDefaultParams);
        }
        //构建请求参数
        if(null!=params&&params.size()>0){
            url = buildParamsToUrl(url, params);
        }
        builder.url(url);
        //构建Header
        builder=buildHeaderToRequest(builder,headers);
        final Request request = builder.build();
        setdRequst(request,callBack,isSynchro);
    }

    /**
     * 发送POST请求
     * @param url host api
     * @param params 键值对参数
     * @param headers 键值对Header
     * @param callBack 回调
     * @param isSynchro 是否同步调用
     */
    private void sendPostRequst(String url, Map<String, String> params,Map<String,String> headers,
                                OnResultCallBack callBack,boolean isSynchro) {
        //构建请求Body参数
        RequestBody requestBody = formatPostParams(params);
        Request.Builder builder = new Request.Builder();
        builder.url(url).post(requestBody);
        //构建Header参数
        builder=buildHeaderToRequest(builder,headers);
        setdRequst(builder.build(),callBack,isSynchro);
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
     * 添加Body参数
     * @param params 键值对参数
     * @return RequestBody
     */
    private RequestBody formatPostParams(Map<String, String> params) {
        //初始化共有参数
        if(null==params){
            params=new HashMap<>();
        }
        if(null!=mDefaultParams){
            params.putAll(mDefaultParams);
        }
        if(null!=params&&params.size()>0){
            FormBody.Builder builder= new FormBody.Builder();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> next = iterator.next();
                builder.add(next.getKey(), next.getValue());
            }
            return builder.build();
        }
        return null;
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
     * 发送请求
     * @param request requst
     * @param callBack callBack
     * @param isSynchro 是否同步调用
     */
    private void setdRequst(final Request request, final OnResultCallBack callBack,
                            boolean isSynchro) {
        if(DEBUG){
            Logger.d(TAG,"setdRequst-->URL:"+request.url());
        }
        if(null==mHandler){
            mHandler = new Handler(Looper.getMainLooper());
        }
        //同步请求
        if(isSynchro){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        isRequst=true;
                        Response response = createHttpUtils().newCall(request).execute();
                        formatResponse(response,callBack);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        isRequst=false;
                        if(null!=mHandler&&null!=callBack){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onError(ERROR_IO,e.getMessage());
                                }
                            });
                        }
                    }catch (final RuntimeException e){
                        e.printStackTrace();
                        isRequst=false;
                        if(null!=mHandler&&null!=callBack){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onError(ERROR_IO,e.getMessage());
                                }
                            });
                        }
                    }
                }
            }).start();
            return;
        }
        //异步请求
        isRequst=true;
        createHttpUtils().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if(DEBUG){
                    Logger.d(TAG,"onFailure-->e:"+e.getMessage()+call.toString());
                }
                isRequst=false;
                if(null!=callBack&&null!=mHandler){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(null!=callBack){
                                callBack.onError(ERROR_IO,e.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                formatResponse(response,callBack);
            }
        });
    }

    /**
     * 解析Response
     * @param response response响应体
     * @param callBack callBack回调
     */
    private void formatResponse(final Response response,final OnResultCallBack callBack) {
        isRequst=false;
        if(null!=mHandler&&null!=callBack){
            if(null!=response){
                if(200==response.code()){
                    try {
                        String string = response.body().string();
                        if(!TextUtils.isEmpty(string)){
                            if(DEBUG){
                                Logger.d(TAG,"服务端返回数据-->"+string);
                            }
                            final Object resultInfo = getResultInfo(string, callBack.getType());
                            if(null!=mHandler&&null!=callBack){
                                if(null!=resultInfo){
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callBack.onResponse(resultInfo);
                                        }
                                    });
                                }else{
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callBack.onError(ERROR_JSON_FORMAT,
                                                    "Json format error");
                                        }
                                    });
                                }
                            }
                        }else{
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(null!=callBack){
                                        callBack.onError(response.code(),"body is empty");
                                    }
                                }
                            });
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                        if(null!=mHandler){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(null!=callBack){
                                        callBack.onError(ERROR_JSON_FORMAT,e.getMessage());
                                    }
                                }
                            });
                        }
                    }catch (final RuntimeException e){
                        e.printStackTrace();
                        if(null!=mHandler){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(null!=callBack){
                                        callBack.onError(ERROR_JSON_FORMAT,e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(null!=callBack){
                                callBack.onError(response.code(),response.message());
                            }
                        }
                    });
                }
            }else{
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(null!=callBack){
                            callBack.onError(ERROR_EMPTY,"response is empty");
                        }
                    }
                });
            }
        }
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
     * 释放、销毁
     */
    public static void onDestroy(){
        if(null!=mHttpClient){
            mHttpClient=null;
        }
        isRequst=false;
        if(null!=mHandler){
            mHandler.removeCallbacksAndMessages(null);
            mHandler.removeMessages(0);
            mHandler=null;
        }
        mGson=null;
    }
}