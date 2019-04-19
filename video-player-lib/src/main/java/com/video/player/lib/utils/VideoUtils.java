package com.video.player.lib.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.ViewConfiguration;

import com.video.player.lib.constants.VideoConstants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * TinyHung@Outlook.com
 * 2019/4/9
 */

public class VideoUtils {

    private static final String TAG = "VideoUtils";

    private static VideoUtils mInstance;

    public static synchronized VideoUtils getInstance() {
        synchronized (VideoUtils.class) {
            if (null == mInstance) {
                mInstance = new VideoUtils();
            }
        }
        return mInstance;
    }

    /**
     * 时长格式化
     * @param timeMs
     * @return
     */
    public String stringForAudioTime(long timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = timeMs / 1000;
        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 返回设备是否连接至WIFI网络
     * @param context context
     * @return if wifi is connected,return true
     */
    public boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 检查设备是否已连接至可用网络
     * @return
     */
    public boolean isCheckNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        int type = networkInfo.getType();
        if (type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * Get AppCompatActivity from context
     *
     * @param context context
     * @return AppCompatActivity if it's not null
     */
    public AppCompatActivity getAppCompActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * Get activity from context object
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public Activity getContextForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getContextForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    //设备屏幕宽度
    public int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    //设备屏幕高度
    public int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 将dp转换成px
     * @param dp
     * @return
     */
    public float dpToPx(Context context,float dp) {
        return dp * context.getApplicationContext().getResources().getDisplayMetrics().density;
    }

    public int dpToPxInt(Context context,float dp) {
        return (int) (dpToPx(context,dp) + 0.5f);
    }

    /**
     * 格式化URL eyepetizer://ranklist/
     * @param actionUrl
     * @return
     */
    public String formatActionUrl(String actionUrl) {
        if(TextUtils.isEmpty(actionUrl)){
            return actionUrl;
        }
        Uri uri = Uri.parse(actionUrl);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String path = uri.getPath();
        Logger.d(TAG,"scheme:"+scheme+",host:"+host+",path:"+path);
        return host;
    }

    /**
     * 根据URL格式化标题
     * @param url
     * @return
     */
    public String formatTitleByUrl(String url) {
        if(VideoConstants.HOST_TOP_ALL.equals(url)){
            return "热门排行";
        }
        return "所有视频";
    }

    /**
     * 根据标题格式化标题
     * @param title
     * @return
     */
    public String formatTitleByTitle(String title) {
        if(TextUtils.isEmpty(title)){
            return "全部";
        }
        return title.replace("查看","");
    }

    /**
     * 获取应用的包名
     * @param context
     * @return
     */
    public String getPackageName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取底部虚拟按键的高度
     * @param context
     * @return
     */
    public int getNavigationHeight(Context context){
        int result = 0;
        if (hasNavBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 检查是否存在虚拟按键
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else {
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 检查虚拟按键是否被重写
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }

    /**
     * 反射获取Context
     * @return
     */
    public Context getContext() {
        try {
            Class<?> ActivityThread = Class.forName("android.app.ActivityThread");

            Method method = ActivityThread.getMethod("currentActivityThread");
            Object currentActivityThread = method.invoke(ActivityThread);//获取currentActivityThread 对象

            Method method2 = currentActivityThread.getClass().getMethod("getApplication");
            return (Context)method2.invoke(currentActivityThread);//获取 Context对象

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检测资源地址是否是直播流
     * @param dataSource
     * @return
     */
    public boolean isLiveStream(String dataSource) {
        if(dataSource.isEmpty()) return false;
        if(dataSource.startsWith("htpp")||dataSource.startsWith("htpps")){
            if(dataSource.endsWith(".m3u8")||dataSource.endsWith(".hks")||dataSource.endsWith(".rtmp")){
                return true;
            }
        }
        return false;
    }
}