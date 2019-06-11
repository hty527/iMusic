package com.android.imusic;

import android.app.Application;
import android.content.Context;
import com.android.imusic.music.activity.MusicPlayerActivity;
import com.android.imusic.music.manager.AppBackgroundManager;
import com.android.imusic.music.manager.ForegroundManager;
import com.android.imusic.net.OkHttpUtils;
import com.music.player.lib.manager.MusicFullWindowManager;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * TinyHung@Outlook.com
 * 2019/3/17
 */

public class MusicApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext=getApplicationContext();
        ForegroundManager.getInstance().init(this);
        //APP前后台监测,悬浮窗的处理
        AppBackgroundManager.getInstance().setAppStateListener( new AppBackgroundManager.IAppStateChangeListener() {
            @Override
            public void onAppStateChanged(String activityName,boolean isAppForceground) {
                //APP不可见并且直接是从播放器界面不可见的，让悬浮窗显示出来
                if(!isAppForceground&&activityName.equals(MusicPlayerActivity.class.getCanonicalName())){
                    MusicFullWindowManager.getInstance().onVisible();
                }
//                if(isAppForceground){
//                    MusicWindowManager.getInstance().onVisible();
//                }else{
//                    MusicWindowManager.getInstance().onInvisible();
//                }
            }
        });
        CrashReport.initCrashReport(getApplicationContext(), "da36e5e1da", false);
        if(BuildConfig.FLAVOR.equals("imusicPublish")){
            com.music.player.lib.util.Logger .IS_DEBUG=false;
            com.video.player.lib.utils.Logger.IS_DEBUG=false;
            OkHttpUtils.DEBUG=false;
        }
    }

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ForegroundManager.getInstance().onDestroy(this);
        sContext=null;
    }
}