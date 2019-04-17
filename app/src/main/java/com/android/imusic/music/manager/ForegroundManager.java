package com.android.imusic.music.manager;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.music.player.lib.util.Logger;

/**
 * 2019/2/20
 * Activity活动状态监测
 */

public class ForegroundManager implements Application.ActivityLifecycleCallbacks{

    private static final String TAG = "ForegroundManager";
    private static ForegroundManager mInstance;
    private Activity mRunActivity;
    private int activityAount = 0;
    private boolean isForeground;//是否进入后台

    public static synchronized ForegroundManager getInstance(){
        synchronized (ForegroundManager.class){
            if(null==mInstance){
                mInstance=new ForegroundManager();
            }
            return mInstance;
        }
    }

    public void init(Application application){
        application.registerActivityLifecycleCallbacks(this);
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean foreground) {
        isForeground = foreground;
    }

    public Activity getRunActivity() {
        return mRunActivity;
    }

    public void setRunActivity(Activity runActivity) {
        mRunActivity = runActivity;
    }

    public int getActivityAount() {
        return activityAount;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        activityAount++;
        Logger.d(TAG,"onActivityStarted--"+activityAount);
//        if(activity instanceof SplashActivity) return;
        AppBackgroundManager.getInstance().onActivityStarted(activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        setForeground(true);
        setRunActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        setForeground(false);
        setRunActivity(null);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityAount--;
        if (activityAount == 0) {
            isForeground = false;
        }
        Logger.d(TAG,"onActivityStopped--"+activityAount);
//        if(activity instanceof SplashActivity) return;
        AppBackgroundManager.getInstance().onActivityStopped();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    public void onDestroy(Application application) {
        application.unregisterActivityLifecycleCallbacks(mInstance);
        mInstance = null;mRunActivity=null;
    }
}