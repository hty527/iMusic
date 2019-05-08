package com.android.imusic.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.android.imusic.net.OkHttpUtils;
import java.util.Map;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * ModelBase 基类本应该不适合直接和网路交互的，但本项目只是个小的示例项目，所以不要见外，不封装出去了
 */

public class BaseEngin{

    protected static final String TAG = "BaseEngin";
    protected Context mContext;
    protected Handler mHandler;
    //数据为空
    public static final int API_RESULT_EMPTY = OkHttpUtils.ERROR_EMPTY;
    public static final String API_EMPTY = "没有数据";

    public boolean isRequsting() {
        return OkHttpUtils.isRequst;
    }

    protected Handler getHandler() {
        if(null==mHandler){
            mHandler=new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /**
     * 发送异步GET请求
     * @param url URL
     * @param callBack CALL BACK
     */
    protected void sendGetRequst(String url,OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.get(url,null,null,callBack);
    }

    /**
     * 发送异步GET请求
     * @param url URL
     * @param params PARAMS
     * @param callBack CALL BACK
     */
    protected void sendGetRequst(String url, Map<String,String> params,
                                 OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.get(url,params,null,callBack);
    }

    /**
     * 发送异步GET请求
     * @param url URL
     * @param params PARAMS
     * @param params HEADERS
     * @param callBack CALL BACK
     */
    protected void sendGetRequst(String url, Map<String,String> params,Map<String,String> headers,
                                 OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.get(url,params,headers,callBack);
    }

    /**
     * 发送同步GET请求
     * @param url URL
     * @param params PARAMS
     * @param headers HEADERS
     * @param callBack CALL BACK
     */
    protected void sendGetSynchroRequst(String url, Map<String,String> params,Map<String,String> headers,
                                        OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.getSynchro(url,params,headers,callBack);
    }

    /**
     * 发送异步Post请求
     * @param url URL
     * @param callBack CALL BACK
     */
    protected void sendPostRequst(String url, OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.post(url,null,null,callBack);
    }

    /**
     * 发送异步Post请求
     * @param url URL
     * @param params PARAMS
     * @param callBack CALL BACK
     */
    protected void sendPostRequst(String url, Map<String,String> params,
                                  OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.post(url,params,null,callBack);
    }

    /**
     * 发送异步Post请求
     * @param url URL
     * @param params PARAMS
     * @param headers HEADERS
     * @param callBack CALL BACK
     */
    protected void sendPostRequst(String url, Map<String,String> params,Map<String,String> headers,
                                  OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.post(url,params,headers,callBack);
    }

    /**
     * 发送同步Post请求
     * @param url URL
     * @param params PARAMS
     * @param headers HEADERS
     * @param callBack CALL BACK
     */
    protected void sendPostSynchroRequst(String url, Map<String,String> params,Map<String,String> headers,
                                         OkHttpUtils.OnResultCallBack callBack){
        OkHttpUtils.postSynchro(url,params,headers,callBack);
    }

    /**
     * 对应生命周期调用
     */
    public void onDestroy(){
        mContext=null;
        if(null!=mHandler){
            mHandler.removeCallbacksAndMessages(null);
            mHandler.removeMessages(0);
            mHandler=null;
        }
        OkHttpUtils.onDestroy();
    }
}