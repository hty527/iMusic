package com.android.imusic;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import com.android.imusic.music.activity.MusicPlayerActivity;
import com.android.imusic.music.manager.AppBackgroundManager;
import com.android.imusic.music.manager.ForegroundManager;
import com.android.imusic.music.manager.SqlLiteCacheManager;
import com.android.imusic.net.OkHttpUtils;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicPlayerInfoListener;
import com.music.player.lib.listener.MusicWindowClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
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
        //全局初始化
        MusicPlayerManager.getInstance()
                .init(getApplicationContext())
                .setPlayInfoListener(new MusicPlayerInfoListener() {
                    /**
                     * 播放器对象发生了变化
                     * @param musicInfo 播放器内部正在处理的音频对象
                     * @param position 位置
                     */
                    @Override
                    public void onPlayMusiconInfo(BaseAudioInfo musicInfo, int position) {
                        //使用SQL存储本地播放记录
                        SqlLiteCacheManager.getInstance().insertHistroyAudio(musicInfo);
                    }
                });
        //APP前后台监测,悬浮窗的处理
        AppBackgroundManager.getInstance().setAppStateListener( new AppBackgroundManager.IAppStateChangeListener() {
            @Override
            public void onAppStateChanged(String activityName,boolean isAppForceground) {
                //APP不可见，但直接是从播放器界面不可见的，让悬浮窗可见
                if(!isAppForceground&&activityName.equals(MusicPlayerActivity.class.getCanonicalName())){
                    MusicWindowManager.getInstance().onVisible();
                }
//                if(isAppForceground){
//                    MusicWindowManager.getInstance().onVisible();
//                }else{
//                    MusicWindowManager.getInstance().onInvisible();
//                }
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