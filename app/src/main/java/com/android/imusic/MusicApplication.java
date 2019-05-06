package com.android.imusic;

import android.app.Application;
import android.content.Intent;
import android.view.View;
import com.android.imusic.music.activity.MusicPlayerActivity;
import com.android.imusic.music.manager.AppBackgroundManager;
import com.android.imusic.music.manager.ForegroundManager;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicWindowClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * TinyHung@Outlook.com
 * 2019/3/17
 */

public class MusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ForegroundManager.getInstance().init(this);
        //全局初始化
        MusicPlayerManager.getInstance().init(getApplicationContext());
        //APP前后台监测,悬浮窗的处理
        AppBackgroundManager.getInstance().setAppStateListener(new AppBackgroundManager.IAppStateChangeListener() {
            @Override
            public void onAppStateChanged(boolean isAppForceground) {
                if(isAppForceground){
                    MusicWindowManager.getInstance().onVisible();
                }else{
                    MusicWindowManager.getInstance().onInvisible();
                }
            }
        });
        //全局迷你悬浮窗单击事件
        MusicWindowManager.getInstance().setOnMusicWindowClickListener(new MusicWindowClickListener() {

            @Override
            public void onWindownClick(View view, long musicID) {
                if(musicID>0){
                    Intent intent=new Intent(getApplicationContext(), MusicPlayerActivity.class);
                    intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            }

            @Override
            public void onWindownCancel(View view) {}
        });
        CrashReport.initCrashReport(getApplicationContext(), "da36e5e1da", false);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ForegroundManager.getInstance().onDestroy(this);
    }
}