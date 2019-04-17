package com.music.player.lib.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * StatusUtils
 */

public class MusicStatusUtils {

    public static int screenWidth;
    public static int screenHeight;
    public static int navigationHeight = 0;
    private static DisplayMetrics mMetrics;

    private static MusicStatusUtils mInstance;

    public static synchronized MusicStatusUtils getInstance() {
        synchronized (MusicStatusUtils.class) {
            if (null == mInstance) {
                mInstance = new MusicStatusUtils();
            }
        }
        return mInstance;
    }

    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取底部导航栏高度
     *
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        navigationHeight = resources.getDimensionPixelSize(resourceId);
        return navigationHeight;
    }

    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;

    }

    /**
     * @param activity
     * @param useThemestatusBarColor   是否要状态栏的颜色，不设置则为透明色
     * @param withoutUseStatusBarColor 是否不需要使用状态栏为暗色调
     */
    public static void setStatusBar(Activity activity, boolean useThemestatusBarColor, boolean withoutUseStatusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            if (useThemestatusBarColor) {
                activity.getWindow().setStatusBarColor(Color.WHITE);
            } else {
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !withoutUseStatusBarColor) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void reMeasure(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        mMetrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealMetrics(mMetrics);
        } else {
            display.getMetrics(mMetrics);
        }

        screenWidth = mMetrics.widthPixels;
        screenHeight = mMetrics.heightPixels;
    }

    /**
     * 改变魅族的状态栏字体为黑色，要求FlyMe4以上
     */
    private static void processFlyMe(boolean isLightStatusBar, Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        try {
            Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
            int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
            Field field = instance.getDeclaredField("meizuFlags");
            field.setAccessible(true);
            int origin = field.getInt(lp);
            if (isLightStatusBar) {
                field.set(lp, origin | value);
            } else {
                field.set(lp, (~value) & origin);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 改变小米的状态栏字体颜色为黑色, 要求MIUI6以上  lightStatusBar为真时表示黑色字体
     */
    private static void processMIUI(boolean lightStatusBar, Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags",int.class,int.class);
            extraFlagField.invoke(activity.getWindow(), lightStatusBar? darkModeFlag : 0, darkModeFlag);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }


    /**
     * 判断手机是否是小米
     * @return
     */
    public boolean isMIUI() {
        return MusicRomUtil.getInstance().isMiui();
    }

    /**
     * 判断手机是否是魅族
     * @return
     */
    public static boolean isFlyme() {
        try {
            // Invoke Build.hasSmartBar()
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * 设置状态栏文字色值为深色调
     * @param useDart 是否使用深色调
     * @param activity
     */
    public void setStatusTextColor(boolean useDart, Activity activity) {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP) return;
        if (isFlyme()) {
            processFlyMe(useDart, activity);
        } else if (isMIUI()) {
            processMIUI(useDart, activity);
        } else {
            if (useDart) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            activity.getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, navigationHeight);
        }
    }









    /**
     * @param activity
     * @param useThemestatusBarColor   是否要状态栏的颜色，不设置则为透明色
     * @param withoutUseStatusBarColor 是否不需要使用状态栏为暗色调
     */
    public static void setStatusBar1(Activity activity, boolean useThemestatusBarColor, boolean withoutUseStatusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            if (useThemestatusBarColor) {
                activity.getWindow().setStatusBarColor(Color.WHITE);
            } else {
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !withoutUseStatusBarColor) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void reMeasure1(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        mMetrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealMetrics(mMetrics);
        } else {
            display.getMetrics(mMetrics);
        }

        screenWidth = mMetrics.widthPixels;
        screenHeight = mMetrics.heightPixels;
    }




    /**
     * 设置状态栏文字色值为深色调
     *
     * @param useDart  是否使用深色调
     * @param activity
     */
    public void setStatusTextColor1(boolean useDart, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (isFlyme1()) {
                //魅族
                processFlyMe1(useDart, activity);
            } else if (isMIUI()) {
                //小米
                setMIUIStatusTextColor(activity, useDart);
            } else if (Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
                //OPPO
                setOPPOStatusTextColor(useDart, activity);
            } else {
                //其他
                setOtherStatusTextColor(activity, useDart);
            }
        }
    }

    /**
     * 判断手机是否是魅族
     *
     * @return
     */
    private static boolean isFlyme1() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * 改变魅族的状态栏字体为黑色，要求FlyMe4以上
     */
    private static void processFlyMe1(boolean isLightStatusBar, Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        try {
            Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
            int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
            Field field = instance.getDeclaredField("meizuFlags");
            field.setAccessible(true);
            int origin = field.getInt(lp);
            if (isLightStatusBar) {
                field.set(lp, origin | value);
            } else {
                field.set(lp, (~value) & origin);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 小米手机更改状态栏颜色
     *
     * @param activity
     * @param useDart
     */
    private static void setMIUIStatusTextColor(Activity activity, boolean useDart) {
        //6.0后小米状态栏用的原生的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (useDart) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            activity.getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, navigationHeight);
        } else {
            processMIUI1(useDart, activity);
        }
    }

    /**
     * 改变小米的状态栏字体颜色为黑色, 要求MIUI6以上  lightStatusBar为真时表示黑色字体
     */
    private static void processMIUI1(boolean lightStatusBar, Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), lightStatusBar ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 设置OPPO手机状态栏字体为黑色(colorOS3.0,6.0以下部分手机)
     *
     * @param lightStatusBar
     * @param activity
     */
    private static final int SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010;

    private static void setOPPOStatusTextColor(boolean lightStatusBar, Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int vis = window.getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (lightStatusBar) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (lightStatusBar) {
                vis |= SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            } else {
                vis &= ~SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT;
            }
        }
        window.getDecorView().setSystemUiVisibility(vis);
    }

    /**
     * 其他手机更改状态栏字体颜色
     */
    private static void setOtherStatusTextColor(Activity activity, boolean useDart) {
        if (useDart) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.getWindow().getDecorView().setSystemUiVisibility
                        (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        activity.getWindow().getDecorView().findViewById(android.R.id.content).setPadding
                (0, 0, 0, navigationHeight);
    }
}
