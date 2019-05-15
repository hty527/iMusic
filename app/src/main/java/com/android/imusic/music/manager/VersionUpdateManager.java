package com.android.imusic.music.manager;

import android.content.Intent;
import com.android.imusic.BuildConfig;
import com.android.imusic.MusicApplication;
import com.android.imusic.music.activity.VersionUpdateActivity;
import com.android.imusic.music.bean.VersionInfo;
import com.android.imusic.net.OkHttpUtils;
import com.android.imusic.net.OnDownloadListener;
import com.android.imusic.net.OnResultCallBack;
import com.android.imusic.net.bean.ResultData;
import com.music.player.lib.util.Logger;
import java.io.File;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/15
 * 版本检测、更新
 */

public class VersionUpdateManager {

    private static final String TAG = "VersionUpdateManager";

    private static volatile VersionUpdateManager mInstance;
    //是否正在下载中
    private boolean isDownload=false;

    public static VersionUpdateManager getInstance(){
        if(null==mInstance){
            synchronized (VersionUpdateManager.class){
                if(null==mInstance){
                    mInstance=new VersionUpdateManager();
                }
            }
        }
        return mInstance;
    }

    public void checkAppVersion(){
        String url="https://raw.githubusercontent.com/Yuye584312311/ConfigFile/master/version/imusic_version.json";
        OkHttpUtils.get(url, new OnResultCallBack<ResultData<VersionInfo>>() {

            @Override
            public void onResponse(ResultData<VersionInfo> data) {
                if(null!=data.getData()){
                    VersionInfo versionInfo = data.getData();
                    Logger.d(TAG,"versionInfo:"+versionInfo.toString());
                    if(versionInfo.getVersion_code()> BuildConfig.VERSION_CODE){
                        Intent intent=new Intent(MusicApplication.getContext(),VersionUpdateActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("versionInfo",versionInfo);
                        MusicApplication.getContext().startActivity(intent);
                    }
                }
            }

            @Override
            public void onError(int code, String errorMsg) {

            }
        });
    }

    /**
     * 下载文件
     * @param path 绝对地址
     * @param listener 监听器
     */
    public void downloadAPK(String path, final OnDownloadListener listener) {
        if(isDownload){
            if(null!=listener){
                listener.onError(0,"正在下载中，请等待下载完成");
            }
            return;
        }
        isDownload=true;
        OkHttpUtils.downloadFile(path, new OnDownloadListener() {
            @Override
            public void progress(int progress, long totloLength, long readLength) {
                if(null!=listener){
                    listener.progress(progress,totloLength,readLength);
                }
            }

            @Override
            public void onSuccess(File file) {
                isDownload=false;
                if(null!=listener){
                    listener.onSuccess(file);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                isDownload=false;
                if(null!=listener){
                    listener.onError(errorCode,message);
                }
            }
        });
    }

    /**
     * 安装APK文件
     * @param file apk文件绝对路径
     */
    public void instanllApk(File file) {

    }
}