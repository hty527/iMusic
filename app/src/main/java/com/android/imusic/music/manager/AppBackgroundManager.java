package com.android.imusic.music.manager;

import com.music.player.lib.util.MusicUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TinyHung@Outlook.com
 * 2019/2/11
 */

public class AppBackgroundManager {

    private boolean isAppForeground = false;
    private int mActivityStated;
    private final int STATE_OPEN = 0;
    private final int STATE_RESUMED = 1;
    private final int STATE_STOPPED = 2;
    private IAppStateChangeListener mListener;
    //上一次是否调用resume方法
    private AtomicBoolean mLastResume = new AtomicBoolean(false);
    private AtomicInteger mMultiStart = new AtomicInteger(0);
    //上一次触发resume的页面
    private String mLastStartActivityName;
    static final AppBackgroundManager mInstance = new AppBackgroundManager();

    private AppBackgroundManager() {}

    public static AppBackgroundManager getInstance() {
        return mInstance;
    }

    /**
     * 在Application的onActivityStarted中调用
     */
    public void onActivityStarted(String activityName) {
        //如果跟上一次是同一个activity，则不认为是多次resume
        boolean isTheSame = MusicUtils.getInstance().getNotNullStr(activityName).equals(mLastStartActivityName);
        if (!isTheSame && mLastResume.get()) {
            mMultiStart.incrementAndGet();
        }
        mLastStartActivityName = activityName;
        mLastResume.set(true);
        //如果是切换进前台
        if (!isAppForeground) {
            //第一次打开状态
            mActivityStated = STATE_OPEN;
            onAppForegroundStateChange(true);
        } else {
            mActivityStated = STATE_RESUMED;
        }
        isAppForeground = true;
    }

    /**
     * 在Application的onActivityStopped中调用
     * 连续两次stop会触发进入后台，如果是程序本身快速关闭两个页面导致的连续stop，需要过滤掉
     */
    public void onActivityStopped() {
        //上一次是stop，且上一次之前有连续多次不同activity的resume
        if (mMultiStart.get() > 1) {
            mMultiStart.decrementAndGet();
            return;
        }
        mLastResume.set(false);
        //可以理解为最新的Activity在应用内
        if (mActivityStated == STATE_RESUMED) {
            mActivityStated = STATE_STOPPED;
            return;
        }
        if (isAppForeground) {
            mMultiStart.set(0);
            isAppForeground = false;
            onAppForegroundStateChange(false);
        }
    }

    public boolean isAppOnForeground() {
        return isAppForeground;
    }

    //App前后台切换
    private void onAppForegroundStateChange(boolean isAppForeground) {
        if (mListener == null) {
            return;
        }
        mListener.onAppStateChanged(isAppForeground);
    }

    public void setAppStateListener(IAppStateChangeListener listener) {
        mListener = listener;
    }

    public interface IAppStateChangeListener {

        void onAppStateChanged(boolean isAppForceground);
    }
}