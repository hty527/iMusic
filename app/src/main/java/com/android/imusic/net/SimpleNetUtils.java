package com.android.imusic.net;

import android.os.Handler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.player.lib.util.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * ModelBase 基类本应该不适合直接和网路交互的，但本项目只是个小的示例项目，所以不要见外，不封装出去了
 */
@Deprecated
public class SimpleNetUtils {

    private static final String TAG = "MusicNetUtils";
    private Handler mHandler;
    public static boolean DEBUG=true;
    private int TIME_OUT_TIME=15000;
    //请求状态
    private boolean isRequst=false;

    public static final int API_RESULT_EMPTY = 0;//数据为空
    public static final int API_RESULT_ERROR = -1;//加载失败
    public static final String API_EMPTY = "没有数据";
    public static final String API_ERROR = "获取数据失败";

    public SimpleNetUtils() {
        mHandler = new Handler();
    }

    @Deprecated
    public void setTimeOutTime(int timeOutTime) {
        this.TIME_OUT_TIME = timeOutTime;
    }

    @Deprecated
    public boolean isRequsting() {
        return isRequst;
    }

    /**
     * 发送请求
     * @param apiUrl
     * @param callBack
     */
    @Deprecated
    public void requstApi(final String apiUrl, final OnResultCallBack callBack) {
        if(isRequst){
            return;
        }
        if(DEBUG){
            Logger.d(TAG,"客户端请求：apiUrl:"+apiUrl);
        }
        isRequst=true;
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(apiUrl);
                    try {
                        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setReadTimeout(TIME_OUT_TIME);
                        urlConnection.setConnectTimeout(TIME_OUT_TIME);
                        urlConnection.setDoInput(true);

                        if(urlConnection.getResponseCode()==200&&null!=callBack){
                            InputStream inputStream = urlConnection.getInputStream();
                            final StringBuilder out = new StringBuilder();
                            char[] buffer = new char[1024];
                            Reader in = new InputStreamReader(inputStream, "UTF-8");
                            for (; ; ) {
                                int rsz = in.read(buffer, 0, buffer.length);
                                if (rsz < 0)
                                    break;
                                out.append(buffer, 0, rsz);
                            }
                            inputStream.close();
                            in.close();
                            String content = out.toString();
                            if(DEBUG){
                                Logger.d(TAG,"URL返回数据-->"+content);
                            }
                            final Object resultInfo = getResultInfo(content, callBack.getType());
                            isRequst=false;
                            if(null!=mHandler&&null!=callBack){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callBack.onResponse(resultInfo);
                                    }
                                });
                            }
                        }else{
                            isRequst=false;
                            if(null!=mHandler&&null!=callBack){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            callBack.onError(urlConnection.getResponseCode(),
                                                    urlConnection.getResponseMessage());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            callBack.onError(API_RESULT_ERROR,"请求失败"+e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }catch (SocketTimeoutException e){
                        e.printStackTrace();
                        isRequst=false;
                        if(null!=mHandler&&null!=callBack){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onError(405,"请求超时，请检查网络连接");
                                }
                            });
                        }
                    }catch (final IOException e) {
                        e.printStackTrace();
                        isRequst=false;
                        if(null!=mHandler&&null!=callBack){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onError(API_RESULT_ERROR,e.getMessage());
                                }
                            });
                        }
                    }
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                    isRequst=false;
                    if(null!=mHandler&&null!=callBack){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(null!=callBack){
                                    callBack.onError(API_RESULT_ERROR,e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }

    /**
     * 发送请求
     * @param apiUrl
     * @param callBack
     */
    @Deprecated
    protected void requstOtherApi(final String apiUrl,final OnResultCallBack callBack) {
        if(isRequst){
            return;
        }
        isRequst=true;
        if(DEBUG){
            Logger.d(TAG,"客户端请求：apiUrl:"+apiUrl);
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(apiUrl);
                    try {
                        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setReadTimeout(TIME_OUT_TIME);
                        urlConnection.setConnectTimeout(TIME_OUT_TIME);
                        urlConnection.setDoInput(true);
                        if(urlConnection.getResponseCode()==200&&null!=callBack){
                            InputStream inputStream = urlConnection.getInputStream();
                            final StringBuilder out = new StringBuilder();
                            char[] buffer = new char[1024];
                            Reader in = new InputStreamReader(inputStream, "UTF-8");
                            for (; ; ) {
                                int rsz = in.read(buffer, 0, buffer.length);
                                if (rsz < 0)
                                    break;
                                out.append(buffer, 0, rsz);
                            }
                            inputStream.close();
                            in.close();
                            String content = out.toString();
                            if(DEBUG){
                                Logger.d(TAG,"URL返回数据-->"+content);
                            }
                            final Object resultInfo = getResultInfo(content, callBack.getType());
                            isRequst=false;
                            if(null!=mHandler&&null!=callBack){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(null!=resultInfo){
                                            callBack.onResponse(resultInfo);
                                        }else{
                                            callBack.onError(API_RESULT_ERROR,"解析Json失败");
                                        }
                                    }
                                });
                            }
                        }else{
                            isRequst=false;
                            if(null!=mHandler&&null!=callBack){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            callBack.onError(urlConnection.getResponseCode(),
                                                    urlConnection.getResponseMessage());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            callBack.onError(API_RESULT_ERROR,"请求失败"+e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }catch (SocketTimeoutException e){
                        e.printStackTrace();
                        isRequst=false;
                        if(null!=mHandler&&null!=callBack){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onError(405,"请求超时，请检查网络连接");
                                }
                            });
                        }
                    }catch (final IOException e) {
                        e.printStackTrace();
                        isRequst=false;
                        if(null!=mHandler&&null!=callBack){
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onError(API_RESULT_ERROR,e.getMessage());
                                }
                            });
                        }
                    }
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                    isRequst=false;
                    if(null!=mHandler&&null!=callBack){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(null!=callBack){
                                    callBack.onError(API_RESULT_ERROR,e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }

    /**
     * JSON解析,这里不再返回T了，直接Object
     * @param json json字符串
     * @param type 指定class
     * @return 泛型实体对象
     */
    @Deprecated
    public Object getResultInfo(String json,Type type){
        Object resultData = null;

        try {
            if(null!=type){
                resultData=new Gson().fromJson(json, type);
            }else{
                //如果用户没有指定Type,则直接使用String.class
                resultData=new Gson().fromJson(json, new TypeToken<String>(){}.getType());
            }
            return resultData;
        }catch (RuntimeException e){
            e.printStackTrace();
            return resultData;
        }
    }

    @Deprecated
    public void onDestroy(){
        if(null!=mHandler){
            mHandler.removeMessages(0);
            mHandler.removeCallbacksAndMessages(null);
            mHandler=null;
        }
    }
}