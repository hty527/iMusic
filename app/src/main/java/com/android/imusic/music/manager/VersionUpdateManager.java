package com.android.imusic.music.manager;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.android.imusic.BuildConfig;
import com.android.imusic.MusicApplication;
import com.android.imusic.music.activity.VersionUpdateActivity;
import com.android.imusic.music.bean.VersionInfo;
import com.android.imusic.music.utils.FileUtils;
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
    public static final String VERSION_API="https://raw.githubusercontent.com/Yuye584312311/ConfigFile/master/version/imusic_version.json";
    private static volatile VersionUpdateManager mInstance;
    private String OUT_PATH= Environment.getExternalStorageDirectory().getAbsoluteFile()
            + File.separator + "iMusic" + File.separator + "Download" + File.separator;
    private String FILE_NAME;
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

    /**
     * 设置输出路径
     * @param outPutPath 相对文件夹路径
     */
    public void setOutPutPath(String outPutPath) {
        this.OUT_PATH = outPutPath;
    }

    /**
     * 设置输出文件名称,必须设置且必须在下载开始前设置
     * @param fileName 文件名称，比如：iMusic.apk
     */
    public void setOutPutFileName(String fileName) {
        this.FILE_NAME = fileName;
        if(TextUtils.isEmpty(OUT_PATH)){
            OUT_PATH= Environment.getExternalStorageDirectory().getAbsoluteFile()
                    + File.separator + "iMusic" + File.separator + "Download" + File.separator;
        }
    }

    /**
     * 本地是否存在相同版本的APK文件
     * @param newVersionCode 新版本CODE
     * @param fielPath 绝对路径，
     * @return
     */
    public boolean isExistApk(int newVersionCode,String fielPath){
        File file=new File(OUT_PATH,FILE_NAME);
        Logger.d(TAG,"isExistApk-->"+file.getAbsolutePath());
        boolean isExistApk=false;
        if(file.exists()&&file.isFile()){
            PackageManager packageManager = MusicApplication.getContext().getPackageManager();
            try {
                PackageInfo archiveInfo = packageManager.getPackageArchiveInfo(file.getAbsolutePath(),
                        PackageManager.GET_ACTIVITIES);
                int versionCode = archiveInfo.versionCode;
                if(versionCode>0&&versionCode==newVersionCode){
                    isExistApk=true;
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return isExistApk;
    }

    /**
     * 检查版本更新
     */
    public void checkAppVersion(){
        OkHttpUtils.get(VERSION_API, new OnResultCallBack<ResultData<VersionInfo>>() {

            @Override
            public void onResponse(ResultData<VersionInfo> data) {
                if(null!=data.getData()){
                    VersionInfo versionInfo = data.getData();
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
        if(TextUtils.isEmpty(FILE_NAME)){
            FILE_NAME= FileUtils.getInstance().getFileName(path);
        }
        isDownload=true;
        OkHttpUtils.downloadFile(path, OUT_PATH,FILE_NAME, new OnDownloadListener() {
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
                instanllApk(file);
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
     * @param urlPath http\https apk url绝对路径
     */
    public void instanllApk(String urlPath) {
        FILE_NAME=FileUtils.getInstance().getFileName(urlPath);
        instanllApk(new File(OUT_PATH,FILE_NAME));
    }

    /**
     * 安装APK文件
     * @param file apk文件绝对路径
     */
    public void instanllApk(File file) {
        if(null==file)return;
        Logger.d(TAG,"file:"+file.getAbsolutePath());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            //authority:Provider主机地址 和配置文件中保持一致 ,file: 共享的文件
            Uri uriForFile = FileProvider.getUriForFile(MusicApplication.getContext(),
                    MusicApplication.getContext()
                    .getApplicationContext().getPackageName() + ".apkprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uriForFile, "application/vnd.android.package-archive");
        }else{
            intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
        }
        try {
            MusicApplication.getContext().startActivity(intent);
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    public String getMIMEType(File file) {
        String var1 = "";
        String var2 = file.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }

    /**
     * 对应生命周期调用
     */
    public void onDestroy() {
        OkHttpUtils.cancelDownload();
        OUT_PATH=null;FILE_NAME=null;isDownload=false;mInstance=null;
    }
}