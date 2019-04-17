package com.music.player.lib.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.bean.MusicAlarmSetting;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicGlideCircleTransform;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * TinyHung@Outlook.com
 * 2018/3/922
 */

public class MusicUtils {

    private static final String TAG = "MusicUtils";
    //最大保存播放记录个数
    private static int MAX_PLAY_HISTROY_COUNT = 50;
    //允许收藏个数
    private static int MAX_COLLECT_COUNT = 100;

    private static MusicUtils mInstance;
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;
    private static MusicACache mACache;

    public static synchronized MusicUtils getInstance() {
        synchronized (MusicUtils.class) {
            if (null == mInstance) {
                mInstance = new MusicUtils();
            }
        }
        return mInstance;
    }

    /**
     * 初始化历史记录存储器
     * @param context
     */
    public void initACache(Context context) {
        MusicACache cache = MusicACache.get(context);
        setACache(cache);
    }

    public void setACache(MusicACache ACache) {
        mACache = ACache;
    }

    public MusicACache getACache() {
        return mACache;
    }

    /**
     * 设置最大的保存历史播放记录
     * @param maxHistroyCount
     */
    public void setMaxPlayHistroyCount(int maxHistroyCount){
        MAX_PLAY_HISTROY_COUNT =maxHistroyCount;
    }

    /**
     * 设置最大的收藏记录个数
     * @param maxCollectCount
     */
    public void setMaxCollectCount(int maxCollectCount){
        MAX_COLLECT_COUNT =maxCollectCount;
    }

    /**
     * 不透明度
     100% — FF
     95% — F2
     90% — E6
     85% — D9
     80% — CC
     75% — BF
     70% — B3
     65% — A6
     60% — 99
     55% — 8C
     50% — 80
     45% — 73
     40% — 66
     35% — 59
     30% — 4D
     25% — 40
     20% — 33
     15% — 26
     10% — 1A
     5% — 0D
     0% — 00
     * @param context
     */
    @SuppressLint("CommitPrefEdits")
    public synchronized void initSharedPreferencesConfig(Context context) {
        if(null==mSharedPreferences){
            mSharedPreferences = context.getSharedPreferences(context.getPackageName() + MusicConstants.SP_KEY_NAME, Context.MODE_MULTI_PROCESS);
            mEditor = mSharedPreferences.edit();
        }
    }

    public boolean putString(String key,String value){
        if(null!=mEditor){
            mEditor.putString(key,value);
            mEditor.commit();
            return true;
        }
        return false;
    }

    public boolean putInt(String key,int value){
        if(null!=mEditor){
            mEditor.putInt(key,value);
            mEditor.commit();
            return true;
        }
        return false;
    }

    public boolean putLong(String key,long value){
        if(null!=mEditor){
            mEditor.putLong(key,value);
            mEditor.commit();
            return true;
        }
        return false;
    }

    public boolean putBoolean(String key,boolean value){
        if(null!=mEditor){
            mEditor.putBoolean(key,value);
            mEditor.commit();
            return true;
        }
        return false;
    }

    public boolean putFloat(String key,float value){
        if(null!=mEditor){
            mEditor.putFloat(key,value);
            mEditor.commit();
            return true;
        }
        return false;
    }

    public boolean putStringSet(String key,Set<String> value){
        if(null!=mEditor){
            mEditor.putStringSet(key,value);
            mEditor.commit();
            return true;
        }
        return false;
    }

    public String getString(String key){
        return getString(key,"");
    }

    public String getString(String key,String defaultValue){
        if(null!=mSharedPreferences){
            return mSharedPreferences.getString(key,defaultValue);
        }
        return "";
    }


    public int getInt(String key){
        return getInt(key,0);
    }

    public int getInt(String key,int defaultValue){
        if(null!=mSharedPreferences){
            return mSharedPreferences.getInt(key,defaultValue);
        }
        return 0;
    }

    public long getLong(String key){
        return getLong(key,0);
    }

    public long getLong(String key,long defaultValue){
        if(null!=mSharedPreferences){
            return mSharedPreferences.getLong(key,defaultValue);
        }
        return 0;
    }

    public float getFloat(String key){
        return getFloat(key,0);
    }

    public float getFloat(String key,float defaultValue){
        if(null!=mSharedPreferences){
            return mSharedPreferences.getFloat(key,defaultValue);
        }
        return 0;
    }

    public boolean getBoolean(String key){
        return getBoolean(key,false);
    }

    public boolean getBoolean(String key,boolean defaultValue){
        if(null!=mSharedPreferences){
            return mSharedPreferences.getBoolean(key,defaultValue);
        }
        return false;
    }

    public Set<String> getStringSet(String key){
        return getStringSet(key,null);
    }

    public Set<String> getStringSet(String key,Set<String> defaultValue){
        if(null!=mSharedPreferences){
            return mSharedPreferences.getStringSet(key,defaultValue);
        }
        return null;
    }

    /**
     * 生成 min 到 max之间的随机数,包含 min max
     * @param min
     * @param max
     * @return
     */
    public int getRandomNum(int min,int max) {
        return min + (int)(Math.random() * max);
    }

    /**
     * 格式化时间
     * @param seconds 单位秒
     * @return
     * 当时间小于半个小时
     */
    public String stringForTime(long seconds) {
        if(seconds<=0) return "无限制";
        if(seconds >= 24 * 60 * 60 ) return "24小时";
        if(seconds<3600){//如果是再一个小时以内，直接返回分钟数
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes+":"+remainingSeconds;
        }else{
            //否则返回小时和分钟
            long hours = seconds/60/60;
            long minutes =(seconds-60*60)/60;//分钟=减去一个小时后，剩余的分钟
            long remainingSeconds = seconds % 60;
            return hours+":"+minutes+":"+remainingSeconds;
        }
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
     * 将秒格式化为小时分钟
     * @param timeMs
     * @return
     */
    public String stringHoursForTime(long timeMs) {
        if(timeMs<=0) return "无限制";
        if(timeMs >= 24 * 60 * 60 ) return "24小时";
        if(timeMs<3600){//如果是在一个小时以内
            return timeMs/60+"分钟";
        }else{
            long hours = timeMs/60/60;
            long minutes =(timeMs-60*60)/60;
            return hours+"小时"+minutes+"分钟";
        }
    }

    public boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * 将dp转换成px
     *
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
     * Bitmap转换高斯模糊
     * @param bitmap
     * @param screenWidth
     * @param screenHeight
     * @param radius 半径>=1 越大越模糊
     * @param filterColor 遮罩层颜色
     * @return
     */
    public Drawable getForegroundDrawable(Bitmap bitmap,int screenWidth,int screenHeight,int radius,int filterColor) {
        if(radius<=0) radius=8;
        if(null!=bitmap&&bitmap.getWidth()>0){
            //得到屏幕的宽高比，以便按比例切割图片一部分
            final float widthHeightSize = (float) (screenWidth * 1.0 / screenHeight * 1.0);
            int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
            int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);
            try {
                //切割部分图片
                Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth, bitmap.getHeight());
                //缩小图片
                Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50, bitmap.getHeight() / 50, false);
                //模糊化
                final Bitmap blurBitmap = doBlur(scaleBitmap, radius, true);
                final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
                //加入灰色遮罩层，避免图片过亮影响其他控件
                foregroundDrawable.setColorFilter(filterColor, PorterDuff.Mode.MULTIPLY);
                return foregroundDrawable;
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 资源文件转换高斯模糊图片
     * @param context
     * @param musicPicRes
     * @return
     */
    private Bitmap getForegroundBitmap(Context context,int musicPicRes) {
        int screenWidth = MusicUtils.getInstance().getScreenWidth(context);
        int screenHeight =  MusicUtils.getInstance().getScreenHeight(context);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if (imageWidth < screenWidth && imageHeight < screenHeight) {
            return BitmapFactory.decodeResource(context.getResources(), musicPicRes);
        }
        int sample = 2;
        int sampleX = imageWidth / MusicUtils.getInstance().getScreenWidth(context);;
        int sampleY = imageHeight / MusicUtils.getInstance().getScreenHeight(context);

        if (sampleX > sampleY && sampleY > 1) {
            sample = sampleX;
        } else if (sampleY > sampleX && sampleX > 1) {
            sample = sampleY;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeResource(context.getResources(), musicPicRes, options);
    }

    public Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }
        if (radius < 1) {
            return (null);
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }
        yw = yi = 0;
        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
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
     * 创建闹钟列表
     */
    public List<MusicAlarmSetting> createAlarmSettings() {
        List<MusicAlarmSetting> alarmSettings=new ArrayList<>();
        MusicAlarmSetting alarmSetting1=new MusicAlarmSetting("10", MusicAlarmModel.MUSIC_ALARM_MODEL_10);
        alarmSettings.add(alarmSetting1);
        MusicAlarmSetting alarmSetting2=new MusicAlarmSetting("15", MusicAlarmModel.MUSIC_ALARM_MODEL_15);
        alarmSettings.add(alarmSetting2);
        MusicAlarmSetting alarmSetting3=new MusicAlarmSetting("30", MusicAlarmModel.MUSIC_ALARM_MODEL_30);
        alarmSettings.add(alarmSetting3);
        MusicAlarmSetting alarmSetting4=new MusicAlarmSetting("60", MusicAlarmModel.MUSIC_ALARM_MODEL_60);
        alarmSettings.add(alarmSetting4);
        return alarmSettings;
    }

    /**
     * 截图字段
     * @param content
     * @return
     */

    public String subString(String content, int maxLength) {
        if(TextUtils.isEmpty(content)){
            return content;
        }
        if(content.length()<=maxLength){
            return content+" ";
        }
        return content.substring(0,11)+"...";
    }

    /**
     * 返回正在播放的位置
     * @param mediaInfos
     * @param musicID
     * @return
     */
    public int getCurrentPlayIndex(List<?> mediaInfos, long musicID) {
        if(null==mediaInfos){
            mediaInfos= MusicPlayerManager.getInstance().getCurrentPlayList();
        }
        if(null!=mediaInfos&&mediaInfos.size()>0){
            List<BaseMediaInfo> mediaInfoList= (List<BaseMediaInfo>) mediaInfos;
            for (int i = 0; i < mediaInfoList.size(); i++) {
                if(musicID==mediaInfoList.get(i).getId()){
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 返回相对于此数组正在播放的位置
     * @param mediaInfos
     * @param musicID
     * @return
     */
    public int getCurrentPlayIndexInThis(List<?> mediaInfos, long musicID) {
        if(musicID<=0){
            return 0;
        }
        if(null!=mediaInfos&&mediaInfos.size()>0){
            List<BaseMediaInfo> mediaInfoList= (List<BaseMediaInfo>) mediaInfos;
            for (int i = 0; i < mediaInfoList.size(); i++) {
                if(mediaInfoList.get(i).getId()==musicID){
                    return i;
                }
            }
        }
        return 0;
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

    public String getNotNullStr(String activityName) {
        if(TextUtils.isEmpty(activityName)) return "";
        return activityName;
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
     * MD5加密
     * @param byteStr 需要加密的内容
     * @return 返回 byteStr的md5值
     */
    public String encryptionMD5(byte[] byteStr) {
        MessageDigest messageDigest = null;
        StringBuffer md5StrBuff = new StringBuffer("");
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(byteStr);
            byte[] byteArray = messageDigest.digest();
//            return Base64.encodeToString(byteArray,Base64.NO_WRAP);
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5StrBuff.toString();
    }

    /**
     * 获取app签名md5值,与“keytool -list -keystore D:\Desktop\app_key”‘keytool -printcert
     * file D:\Desktop\CERT.RSA’获取的md5值一样
     */
    public String getAppSignToMd5(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    getPackageName(context), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            String signStr = encryptionMD5(sign.toByteArray());
            return signStr;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 从控件的底部移动到控件所在位置
     * 从下往上进场
     * @param animationMillis
     * @return
     */
    public TranslateAnimation animationFromBottomToLocation(long animationMillis) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                2.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(animationMillis);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    /**
     * 从控件所在位置移动到控件的底部
     * 从上往下出场
     * @param animationMillis
     * @return
     */
    public TranslateAnimation animationFromLocationToBottom(long animationMillis) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 2.0f);
        animation.setDuration(animationMillis);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    /**
     * 获取SD卡所有音频文件
     * @return
     */
    public ArrayList<BaseMediaInfo> queryLocationMusics(Context context) {
        ArrayList<BaseMediaInfo> mediaInfos=null;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);
        if (null!=cursor&&cursor.moveToFirst()) {
            mediaInfos = new ArrayList<>();
            do {
                if(!TextUtils.isEmpty(cursor.getString(9))){
                    BaseMediaInfo mediaInfo = new BaseMediaInfo();
                    // 文件名
                    //mediaInfo.setVideo_desp(cursor.getString(1));
                    // 歌曲名
                    mediaInfo.setVideo_desp(cursor.getString(2));
//                song.setPinyin(Pinyin.toPinyin(title.charAt(0)).substring(0, 1).toUpperCase());
                    // 时长
                    mediaInfo.setVideo_durtion(cursor.getInt(3));
                    // 歌手名
                    mediaInfo.setNickname(cursor.getString(4));
                    // 专辑名
                    mediaInfo.setMediaAlbum(cursor.getString(5));
                    // 年代 cursor.getString(6)
                    // 歌曲格式
                    if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                        mediaInfo.setMediaType("mp3");
                    } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
                        mediaInfo.setMediaType("wma");
                    }
                    // 文件大小 cursor.getString(8)
                    // 文件路径
                    mediaInfo.setFile_path(cursor.getString(9));
                    mediaInfos.add(mediaInfo);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return mediaInfos;
    }

    /**
     * 获取音频文件的封面地址
     * @param mediaInfo
     * @return
     */
    public String getMusicFrontPath(BaseMediaInfo mediaInfo) {
        if(null==mediaInfo){
            return null;
        }
        //未购买，直接返回封面
        if(TextUtils.isEmpty(mediaInfo.getFile_path())){
            return TextUtils.isEmpty(mediaInfo.getImg_path())?mediaInfo.getAvatar():mediaInfo.getImg_path();
        }
        if(mediaInfo.getFile_path().startsWith("http:")||mediaInfo.getFile_path().startsWith("https:")){
            return TextUtils.isEmpty(mediaInfo.getImg_path())?mediaInfo.getAvatar():mediaInfo.getImg_path();
        }else{
            //本地音频文件
            return mediaInfo.getFile_path();
        }
    }

    /**
     * 合成音频文件封面并显示到空间上
     * @param context
     * @param musicCover 显示对象
     * @param filePath 封面地址
     * @param frontBgSize 唱片机背景大小(宽高)
     * @param frontCoverSize 唱片机封面大小(宽高)
     * @param jukeBoxBgCover 唱片机背景封面
     * @param defaultCover 默认音频
     */
    public void setMusicComposeFront(final Context context, final ImageView musicCover, final String filePath,
                              final float frontBgSize, final float frontCoverSize, final int jukeBoxBgCover, final int defaultCover) {
        if(null==context||null==musicCover||null==filePath){
            return;
        }
        //HTTP || HTTPS
        if(filePath.startsWith("http:")|| filePath.startsWith("https:")){
            Logger.d(TAG,"setMusicComposeFront-->HTTP || HTTPS");
            Glide.with(context)
                    .load(filePath)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .transform(new MusicGlideCircleTransform(context))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            if(null!=musicCover){
                                if(null==bitmap){
                                    bitmap = BitmapFactory.decodeResource(context.getResources(),defaultCover);
                                    bitmap=drawRoundBitmap(bitmap);
                                }
                                if(null!=bitmap){
                                    LayerDrawable discDrawable = composeJukeBoxDrawable(context,bitmap,frontBgSize,frontCoverSize,jukeBoxBgCover);
                                    if(null!=discDrawable){
                                        musicCover.setImageDrawable(discDrawable);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),defaultCover);
                            if(null!=bitmap){
                                bitmap=drawRoundBitmap(bitmap);
                                if(null!=bitmap){
                                    LayerDrawable discDrawable = composeJukeBoxDrawable(context,bitmap,frontBgSize,frontCoverSize,jukeBoxBgCover);
                                    if(null!=discDrawable){
                                        musicCover.setImageDrawable(discDrawable);
                                    }
                                }
                            }
                        }
                    });
        }else{
            Logger.d(TAG,"setMusicCover-->File");
            long startMillis = System.currentTimeMillis();
            //File
            Bitmap bitmap;
            bitmap = MusicImageCache.getInstance().getBitmap(filePath);
            //缓存为空，获取音频文件自身封面
            if(null==bitmap){
                bitmap=MusicImageCache.getInstance().createBitmap(filePath);
            }
            //封面为空，使用默认
            if(null==bitmap){
                bitmap = BitmapFactory.decodeResource(context.getResources(), defaultCover);
            }
            bitmap=drawRoundBitmap(bitmap);
            LayerDrawable discDrawable = composeJukeBoxDrawable(context,bitmap,frontBgSize,frontCoverSize,jukeBoxBgCover);
            long endMillis = System.currentTimeMillis();
            Logger.d(TAG,"本地音乐生成封面耗时："+(endMillis-startMillis));
            if(null!=discDrawable){
                musicCover.setImageDrawable(discDrawable);
            }
        }
    }

    /**
     * 合成封面并显示
     * @param context
     * @param musicCover 显示对象
     * @param bitmap 封面位图
     * @param frontBgSize 唱片机背景大小(宽高)
     * @param frontCoverSize 唱片机封面大小(宽高)
     * @param jukeBoxBgCover 唱片机背景封面
     * @param defaultCover 默认音频封面
     */
    public void setMusicComposeFront(final Context context, final ImageView musicCover,Bitmap bitmap
                              ,final float frontBgSize, final float frontCoverSize, final int jukeBoxBgCover, final int defaultCover){
        if(null!=context&&null!=musicCover){
            if(null==bitmap){
                bitmap = BitmapFactory.decodeResource(context.getResources(), defaultCover);
            }
            LayerDrawable discDrawable = composeJukeBoxDrawable(context,bitmap,frontBgSize,frontCoverSize,jukeBoxBgCover);
            if(null!=discDrawable){
                musicCover.setImageDrawable(discDrawable);
            }
        }
    }

    /**
     * 合成唱片机封面，将音乐封面合成在地图上层
     * @param context
     * @param bitmap 封面位图对象
     * @param frontJukeBoxScale 封面底盘大小比例
     * @param frontCoverScale 封面大小比例
     * @param jukeBoxBgCover 唱片背景封面
     * @return
     */
    public LayerDrawable composeJukeBoxDrawable(Context context, Bitmap bitmap, float frontJukeBoxScale, float frontCoverScale, int jukeBoxBgCover) {
        if(frontJukeBoxScale<=0||frontCoverScale<=0){
            return null;
        }
        int screenWidth = getScreenWidth(context);
        //背景图片大小
        int jukeBoxCoverBgSize = (int) (screenWidth * frontJukeBoxScale);
        //封面大小
        int jukeBoxCoverFgSize = (int) (screenWidth * frontCoverScale);
        //生成一张去除锯齿的背景位图
        Bitmap bgBitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), jukeBoxBgCover), jukeBoxCoverBgSize, jukeBoxCoverBgSize, true);
        BitmapDrawable bgDiscDrawable = new BitmapDrawable(bgBitmapDisc);
        //适配大小
        Bitmap finalBitmap = scalePicSize(jukeBoxCoverFgSize,bitmap);
        RoundedBitmapDrawable roundMusicDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), finalBitmap);
        //抗锯齿
        bgDiscDrawable.setAntiAlias(true);
        roundMusicDrawable.setAntiAlias(true);
        Drawable[] drawables = new Drawable[2];
        drawables[0] = bgDiscDrawable;
        drawables[1] = roundMusicDrawable;
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        int musicPicMargin = (int) ((frontJukeBoxScale - frontCoverScale) * screenWidth / 2);
        //调整专辑图片的四周边距，让其显示在正中
        layerDrawable.setLayerInset(1, musicPicMargin, musicPicMargin, musicPicMargin, musicPicMargin);
        return layerDrawable;
    }

    /**
     * 缩放封面大小，适配唱盘
     * @param musicPicSize
     * @param bitmap
     * @return
     */
    private Bitmap scalePicSize(int musicPicSize, Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imageWidth = bitmap.getWidth();
        int sample = imageWidth / musicPicSize;
        int dstSample = 1;
        if (sample > dstSample) {
            dstSample = sample;
        }
        options.inJustDecodeBounds = false;
        //设置图片采样率
        options.inSampleSize = dstSample;
        //设置图片解码格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return Bitmap.createScaledBitmap(bitmap, musicPicSize, musicPicSize, true);
    }

    /**
     * 矩形转换为圆形
     * @param bitmap
     * @return
     */
    public Bitmap drawRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
        final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    /**
     * 格式化搜索名词
     * @param filename
     * @param currentKey
     * @return
     */
    public String formatSearchContent(String filename, String currentKey) {
        if(TextUtils.isEmpty(currentKey)||TextUtils.isEmpty(filename)){
            return filename;
        }
        return filename.replace(currentKey, "<font color='#8000ff'>" + currentKey + "</font>");
    }
    /**
     * 打卡软键盘
     *
     * @param context
     * @param mEditText
     */
    public void openKeybord(Context context,EditText mEditText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     *
     * @param context
     * @param mEditText
     */
    public void closeKeybord(Context context,EditText mEditText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    /**
     * 创建根缓存目录
     * @return
     */
    public String createRootPath(Context context) {
        String cacheRootPath = "";
        //SD卡已挂载，使用SD卡缓存目录，这个缓存补录数据不会随着应用的卸载而清除
        if (isSdCardAvailable()) {
            // /sdcard/Android/data/<application package>/cache
            if(null!=context.getExternalCacheDir()){
                cacheRootPath = context.getExternalCacheDir().getPath();//SD卡内部临时缓存目录
            }
            //内部缓存目录，会随着应用的卸载而清除
        } else {
            // /data/data/<application package>/cache
            if(null!=context.getCacheDir()){
                cacheRootPath = context.getCacheDir().getPath();//应用内部临时缓存目录
            }else{
                File cacheDirectory = getCacheDirectory(context, null);
                if(null!=cacheDirectory){
                    cacheRootPath=cacheDirectory.getAbsolutePath();
                }
            }
        }
        return cacheRootPath;
    }

    /**
     * 获取临时数据缓存目录
     * @param context
     * @return
     */
    public String getCacheDir(Context context) {
        String cacheRootPath = null;
        if(null!=context.getCacheDir()){
            cacheRootPath= context.getCacheDir().getPath();
        } else if(null!=context.getFilesDir()){
            cacheRootPath=context.getFilesDir().getPath();
        }else if(isSdCardAvailable()){
            if(null!=context.getExternalCacheDir()){
                cacheRootPath = context.getExternalCacheDir().getPath();//SD卡内部临时缓存目录
            }
        }else{
            File cacheDirectory = getCacheDirectory(context, null);
            if(null!=cacheDirectory){
                cacheRootPath=cacheDirectory.getAbsolutePath();
            }
        }
        return cacheRootPath;
    }

    public static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    /**
     * 递归创建文件夹
     *
     * @param file
     * @return 创建失败返回""
     */
    public String createFile(File file) {
        try {
            if (file.getParentFile().exists()) {

                file.createNewFile();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                file.createNewFile();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 递归创建文件夹
     *
     * @param dirPath
     * @return 创建失败返回""
     */
    public String createDir(String dirPath) {
        try {
            File file = new File(dirPath);
            if (file.getParentFile().exists()) {

                file.mkdir();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());

                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dirPath;
    }

    /**
     * 获取应用专属缓存目录
     * android 4.4及以上系统不需要申请SD卡读写权限
     * 因此也不用考虑6.0系统动态申请SD卡读写权限问题，切随应用被卸载后自动清空 不会污染用户存储空间
     * @param context 上下文
     * @param type 文件夹类型 可以为空，为空则返回API得到的一级目录
     * @return 缓存文件夹 如果没有SD卡或SD卡有问题则返回内存缓存目录，否则优先返回SD卡缓存目录
     */
    public File getCacheDirectory(Context context,String type) {
        File appCacheDir = getExternalCacheDirectory(context,type);
        if (appCacheDir == null){
            appCacheDir = getInternalCacheDirectory(context,type);
        }

        if (appCacheDir == null){
            Log.e("getCacheDirectory","getCacheDirectory fail ,the reason is mobile phone unknown exception !");
        }else {
            if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
                Log.e("getCacheDirectory","getCacheDirectory fail ,the reason is make directory fail !");
            }
        }
        return appCacheDir;
    }

    /**
     * 获取SD卡缓存目录
     * @param context 上下文
     * @param type 文件夹类型 如果为空则返回 /storage/emulated/0/Android/data/app_package_name/cache
     *             否则返回对应类型的文件夹如Environment.DIRECTORY_PICTURES 对应的文件夹为 .../data/app_package_name/files/Pictures
     * {@link Environment#DIRECTORY_MUSIC},
     * {@link Environment#DIRECTORY_PODCASTS},
     * {@link Environment#DIRECTORY_RINGTONES},
     * {@link Environment#DIRECTORY_ALARMS},
     * {@link Environment#DIRECTORY_NOTIFICATIONS},
     * {@link Environment#DIRECTORY_PICTURES}, or
     * {@link Environment#DIRECTORY_MOVIES}.or 自定义文件夹名称
     * @return 缓存目录文件夹 或 null（无SD卡或SD卡挂载失败）
     */
    public File getExternalCacheDirectory(Context context,String type) {
        File appCacheDir = null;
        if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (TextUtils.isEmpty(type)){
                appCacheDir = context.getExternalCacheDir();
            }else {
                appCacheDir = context.getExternalFilesDir(type);
            }

            if (appCacheDir == null){// 有些手机需要通过自定义目录
                appCacheDir = new File(Environment.getExternalStorageDirectory(),"Android/data/"+context.getPackageName()+"/cache/"+type);
            }

            if (appCacheDir == null){
                Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard unknown exception !");
            }else {
                if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
                    Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is make directory fail !");
                }
            }
        }else {
            Log.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
        }
        return appCacheDir;
    }

    /**
     * 获取内存缓存目录
     * @param type 子目录，可以为空，为空直接返回一级目录
     * @return 缓存目录文件夹 或 null（创建目录文件失败）
     * 注：该方法获取的目录是能供当前应用自己使用，外部应用没有读写权限，如 系统相机应用
     */
    public File getInternalCacheDirectory(Context context,String type) {
        File appCacheDir = null;
        if (TextUtils.isEmpty(type)){
            appCacheDir = context.getCacheDir();// /data/data/app_package_name/cache
        }else {
            appCacheDir = new File(context.getFilesDir(),type);// /data/data/app_package_name/files/type
        }

        if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
            Log.e("getInternalDirectory","getInternalDirectory fail ,the reason is make directory fail !");
        }
        return appCacheDir;
    }

    /**
     * 保存播放记录到历史记录中，使用默认最大个数
     * @param mediaInfo
     */
    public void putMusicToHistory(BaseMediaInfo mediaInfo){
        putMusicToHistory(mediaInfo, MAX_PLAY_HISTROY_COUNT);
    }

    /**
     * 保存播放记录到历史记录中
     * @param mediaInfo
     * @param maxHistoryCount 最大历史记录个数
     */
    public void putMusicToHistory(final BaseMediaInfo mediaInfo, final int maxHistoryCount) {
        if(null!=getACache()&&null!=mediaInfo&&!TextUtils.isEmpty(mediaInfo.getFile_path())){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    List<BaseMediaInfo> cacheMusics = (List<BaseMediaInfo>) getACache().getAsObject(MusicConstants.A_KEY_PLAY_HISTORY);
                    if(null!=cacheMusics){
                        int currentIndex=-1;
                        for (int i = 0; i < cacheMusics.size(); i++) {
                            BaseMediaInfo baseMediaInfo = cacheMusics.get(i);
                            if(baseMediaInfo.getId()==mediaInfo.getId()){
                                currentIndex=i;
                                break;
                            }
                        }
                        //本地存在此播放记录,移除掉重新添加
                        if(currentIndex>-1){
                            cacheMusics.remove(currentIndex);
                        }
                        mediaInfo.setLastPlayTime(System.currentTimeMillis());
                        cacheMusics.add(mediaInfo);
                        //冒泡排序，由近到远
                        for (int i = 0; i < cacheMusics.size()-1; i++) {
                            for (int i1 = 0; i1 < cacheMusics.size()-1-i; i1++) {
                                if(cacheMusics.get(i1).getLastPlayTime()<cacheMusics.get(i1+1).getLastPlayTime()){
                                    BaseMediaInfo tempMedia=cacheMusics.get(i1);
                                    cacheMusics.set(i1,cacheMusics.get(i1+1));
                                    cacheMusics.set(i1+1,tempMedia);
                                }
                            }
                        }
                        //缓存个数到达上限，移除最后一个
                        if(cacheMusics.size()>maxHistoryCount){
                            cacheMusics.remove(cacheMusics.size()-1);
                        }
                        getACache().remove(MusicConstants.A_KEY_PLAY_HISTORY);
                        getACache().put(MusicConstants.A_KEY_PLAY_HISTORY, (Serializable) cacheMusics);
                    }else{
                        //新的第一条播放记录
                        List<BaseMediaInfo> newCacheMediaInfos=new ArrayList<>();
                        mediaInfo.setLastPlayTime(System.currentTimeMillis());
                        newCacheMediaInfos.add(mediaInfo);
                        getACache().put(MusicConstants.A_KEY_PLAY_HISTORY, (Serializable) newCacheMediaInfos);
                    }
                }
            }.start();
        }
    }

    /**
     * 获取历史播放记录
     * @return
     */
    public List<BaseMediaInfo> getMusicsByHistroy(){
        if(null==getACache()){
            return null;
        }
        List<BaseMediaInfo> mediaInfos = (List<BaseMediaInfo>) getACache().getAsObject(MusicConstants.A_KEY_PLAY_HISTORY);
        return mediaInfos;
    }

    /**
     * 删除指定ID的历史记录
     * @param musicID
     */
    public boolean removeMusicHistroyById(long musicID) {
        if(null==getACache()){
            return false;
        }
        List<BaseMediaInfo> mediaInfos = (List<BaseMediaInfo>) getACache().getAsObject(MusicConstants.A_KEY_PLAY_HISTORY);
        if(null!=mediaInfos&&mediaInfos.size()>0){
            int index=-1;
            for (int i = 0; i < mediaInfos.size(); i++) {
                if(mediaInfos.get(i).getId()==musicID){
                    index=i;
                    break;
                }
            }
            if(index>-1){
                mediaInfos.remove(index);
                getACache().remove(MusicConstants.A_KEY_PLAY_HISTORY);
                getACache().put(MusicConstants.A_KEY_PLAY_HISTORY, (Serializable) mediaInfos);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 清空所有最近播放记录
     * @return
     */
    public boolean removeMusicHistroys(){
        if(null==getACache()){
            return false;
        }
        getACache().remove(MusicConstants.A_KEY_PLAY_HISTORY);
        return true;
    }


    /**
     * 保存音乐到收藏记录，使用默认最大个数
     * @param mediaInfo
     */
    public boolean putMusicToCollect(BaseMediaInfo mediaInfo){
        return putMusicToCollect(mediaInfo, MAX_COLLECT_COUNT);
    }

    public boolean putMusicToCollect(final BaseMediaInfo mediaInfo, final int maxCollectCount){
        if(null!=getACache()&&null!=mediaInfo&&!TextUtils.isEmpty(mediaInfo.getFile_path())) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    List<BaseMediaInfo> cacheMusics = (List<BaseMediaInfo>) getACache().getAsObject(MusicConstants.A_KEY_PLAY_COLLECT);
                    if (null != cacheMusics) {
                        int currentIndex = -1;
                        for (int i = 0; i < cacheMusics.size(); i++) {
                            BaseMediaInfo baseMediaInfo = cacheMusics.get(i);
                            if (baseMediaInfo.getId() == mediaInfo.getId()) {
                                currentIndex = i;
                                break;
                            }
                        }
                        //本地存在此播放记录,移除掉重新添加
                        if (currentIndex > -1) {
                            cacheMusics.remove(currentIndex);
                        }
                        mediaInfo.setLastPlayTime(System.currentTimeMillis());
                        cacheMusics.add(mediaInfo);
                        //冒泡排序算法交换位置
                        //冒泡排序，由近到远
                        for (int i = 0; i < cacheMusics.size() - 1; i++) {
                            for (int i1 = 0; i1 < cacheMusics.size() - 1 - i; i1++) {
                                if (cacheMusics.get(i1).getLastPlayTime() < cacheMusics.get(i1 + 1).getLastPlayTime()) {
                                    BaseMediaInfo tempMedia = cacheMusics.get(i1);
                                    cacheMusics.set(i1, cacheMusics.get(i1 + 1));
                                    cacheMusics.set(i1 + 1, tempMedia);
                                }
                            }
                        }
                        //缓存个数到达上限，移除最后一个
                        if (cacheMusics.size() > maxCollectCount) {
                            cacheMusics.remove(cacheMusics.size() - 1);
                        }
                        getACache().remove(MusicConstants.A_KEY_PLAY_COLLECT);
                        getACache().put(MusicConstants.A_KEY_PLAY_COLLECT, (Serializable) cacheMusics);
                    } else {
                        //第一条播放记录
                        List<BaseMediaInfo> newCacheMediaInfos = new ArrayList<>();
                        mediaInfo.setLastPlayTime(System.currentTimeMillis());
                        newCacheMediaInfos.add(mediaInfo);
                        getACache().put(MusicConstants.A_KEY_PLAY_COLLECT, (Serializable) newCacheMediaInfos);
                    }
                }
            }.start();
            return true;
        }
        return false;
    }

    /**
     * 删除指定ID的收藏记录
     * @param musicID
     */
    public boolean removeMusicCollectById(long musicID) {
        if(null==getACache()){
            return false;
        }
        List<BaseMediaInfo> mediaInfos = (List<BaseMediaInfo>) getACache().getAsObject(MusicConstants.A_KEY_PLAY_COLLECT);
        if(null!=mediaInfos&&mediaInfos.size()>0){
            int index=-1;
            for (int i = 0; i < mediaInfos.size(); i++) {
                if(mediaInfos.get(i).getId()==musicID){
                    index=i;
                    break;
                }
            }
            if(index>-1){
                mediaInfos.remove(index);
                getACache().remove(MusicConstants.A_KEY_PLAY_COLLECT);
                getACache().put(MusicConstants.A_KEY_PLAY_COLLECT, (Serializable) mediaInfos);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 获取收藏记录
     * @return
     */
    public List<BaseMediaInfo> getMusicsByCollect(){
        if(null==getACache()){
            return null;
        }
        List<BaseMediaInfo> mediaInfos = (List<BaseMediaInfo>) getACache().getAsObject(MusicConstants.A_KEY_PLAY_COLLECT);
        return mediaInfos;
    }

    /**
     * 收藏列表是否存在此收藏记录
     * @param musicID
     * @return
     */
    public boolean isExistCollectHistroy(long musicID) {
        if(null==getACache()){
            return false;
        }
        List<BaseMediaInfo> mediaInfos = (List<BaseMediaInfo>) getACache().getAsObject(MusicConstants.A_KEY_PLAY_COLLECT);
        if(null!=mediaInfos&&mediaInfos.size()>0) {
            for (int i = 0; i < mediaInfos.size(); i++) {
                if (mediaInfos.get(i).getId() == musicID) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 检查是否拥有通知栏权限
     * @param context
     * @return
     */

    public boolean hasNiticePremission(Context context) {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }
        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null;
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 前往设置中心
     * @param context
     */
    public void startAppSetting(Context context) {
        Intent localIntent = new Intent();
        //直接跳转到应用通知设置的代码：
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            localIntent.putExtra("app_package", context.getPackageName());
            localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            localIntent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }
        }
        context.startActivity(localIntent);
    }

    /**
     * 返回真实的文件地址
     * @param context
     * @param contentUri
     * @return
     */
    public String getPathFromURI(Context context,Uri contentUri){
        //来自第三方
        if("file".equals(contentUri.getScheme())){
            return contentUri.getPath();
        }
        if(ContentResolver.SCHEME_CONTENT.equals(contentUri.getScheme())){
            File file;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                String path = getPath(context, contentUri);
                file = new File("file://" + path);
            } else {//4.4一下系统调用方法
                String realPathFromURI = getRealPathFromURI(context,contentUri);
                file = new File("file://" + realPathFromURI);//上传文件
            }
            if(null!=file){
                return file.getAbsolutePath();
            }
            return null;
        }
        return null;//getPath(context, contentUri)
    }

    /**
     * 格式化真实文件地址
     * @param context
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Context context,Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    public static String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
    public static int Build_VERSION_KITKAT = 19;

    public  String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;
        // DocumentProvider
        if (isKitKat && isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static final String PATH_DOCUMENT = "document";

    private boolean isDocumentUri(Context context, Uri uri) {
        final List<String> paths = uri.getPathSegments();
        if (paths.size() < 2) {
            return false;
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            return false;
        }

        return true;
    }

    private String getDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() < 2) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        return paths.get(1);
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     *            [url=home.php?mod=space&uid=7300]@return[/url] The value of
     *            the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 格式化数字为‘万’单位
     * @param count
     * @param continueSmall 是否保留最小单位为原样
     * @return
     */
    public String formatNumToWan(long count, boolean continueSmall) {
        if (continueSmall && count <= 10000) return String.valueOf(count);
        double n = (double) count / 10000;
        return changeDouble(n) + "万";
    }

    public double changeDouble(Double dou) {
        try {
            NumberFormat nf = new DecimalFormat("0.0 ");
            dou = Double.parseDouble(nf.format(dou));
            return dou;
        }catch (RuntimeException e){

        }
        return dou;
    }

    /**
     * 将毫秒格式化成时分秒
     * @param millis
     * @return
     */
    public static String formatDateFromMillis(long millis){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return formatter.format(millis);
    }

    public String getTimeNow(Long time) {
        Calendar cal = Calendar.getInstance();
        long timel = cal.getTimeInMillis() - time;
        if (timel / 1000 < 60) {
            return "1分钟以内";
        } else if (timel / 1000 / 60 < 60) {
            return timel / 1000 / 60 + "分钟前";
        } else if (timel / 1000 / 60 / 60 < 24) {
            return timel / 1000 / 60 / 60 + "小时前";
        } else {
            return getTimeForString(time);
        }
    }
    public String getTimeForString(long time){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(time);
    }

    /**
     * 去除图片地址的重定向代理
     * @param url
     * @return
     */
    public String formatImageUrl(String url) {
        if(TextUtils.isEmpty(url)){
            return url;
        }
        return url.substring(0,url.indexOf("?"));
    }
}