package com.android.imusic.music.net;

import android.os.Handler;

import com.android.imusic.music.bean.ResultData;
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
 */

public class MusicNetUtils <T> {

    private static final String TAG = "MusicNetUtils";
    private Handler mHandler;
    public static boolean DEBUG=true;
    private int TIME_OUT_TIME=15000;
    //请求状态
    private boolean isRequst=false;

    public MusicNetUtils() {
        mHandler = new Handler();
    }

    public interface OnRequstCallBack<T>{
        void onResponse(ResultData<T> data);
        void onError(int code,String errorMsg);
    }

    public interface OnOtherRequstCallBack<T>{
        void onResponse(T data);
        void onError(int code,String errorMsg);
    }

    public void setTimeOutTime(int timeOutTime) {
        this.TIME_OUT_TIME = timeOutTime;
    }

    public boolean isRequsting() {
        return isRequst;
    }

    /**
     * 发送请求
     * @param apiUrl
     * @param callBack
     */
    protected void requstApi(final String apiUrl, final Type type, final OnRequstCallBack<T> callBack) {
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
                        if(urlConnection.getResponseCode()==200){
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
                            final ResultData<T> resultInfo = getResultInfo(content, type);
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
                                            callBack.onError(urlConnection.getResponseCode(),urlConnection.getResponseMessage());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            callBack.onError(-1,"请求失败"+e.getMessage());
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
                                    callBack.onError(-1,e.getMessage());
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
                                    callBack.onError(-1,e.getMessage());
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
    protected void requstOtherApi(final String apiUrl, final Type type, final OnOtherRequstCallBack<T> callBack) {
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
                        if(urlConnection.getResponseCode()==200){
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
                            final T resultInfo = getOtherResultInfo(content, type);
                            isRequst=false;
                            if(null!=mHandler&&null!=callBack){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(null!=resultInfo){
                                            callBack.onResponse(resultInfo);
                                        }else{
                                            callBack.onError(-1,"解析Json失败");
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
                                            callBack.onError(urlConnection.getResponseCode(),urlConnection.getResponseMessage());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            callBack.onError(-1,"请求失败"+e.getMessage());
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
                                    callBack.onError(-1,e.getMessage());
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
                                    callBack.onError(-1,e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }

    /**
     * JSON解析
     * @param jsonContent
     * @param type
     * @return
     */
    public ResultData<T> getResultInfo(String jsonContent,Type type){
        ResultData<T> resultData;
        try {
            if(null!=type){
                resultData=new Gson().fromJson(jsonContent, type);
            }else{
                resultData=new Gson().fromJson(jsonContent, new TypeToken<ResultData<T>>(){}.getType());
            }
            return resultData;
        }catch (RuntimeException e){
            e.printStackTrace();
            ResultData errorResultData=new ResultData();
            errorResultData.setCode(-1);
            errorResultData.setMsg("解析Json失败,Error:"+e);
            errorResultData.setError("解析Json失败,Error:"+e);
            return errorResultData;
        }
    }


    /**
     * JSON解析
     * @param jsonContent
     * @param type
     * @return
     */
    public T getOtherResultInfo(String jsonContent,Type type){
        T resultData;
        try {
            if(null!=type){
                resultData=new Gson().fromJson(jsonContent, type);
            }else{
                resultData=new Gson().fromJson(jsonContent, new TypeToken<ResultData<T>>(){}.getType());
            }
            return resultData;
        }catch (RuntimeException e){
            e.printStackTrace();
            return null;
        }
    }

    public void onDestroy(){
        if(null!=mHandler){
            mHandler.removeMessages(0);
            mHandler=null;
        }
    }
}