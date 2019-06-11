package com.music.player.lib.manager;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicWindowPlayer;
import java.lang.reflect.Method;

/**
 * TinyHung@Outlook.com
 * 2019/6/11
 * WindowManager
 * 将悬浮窗改为全屏悬浮窗，垃圾桶交互在一个View之内，不再跨View通信和交互
 */

public class MusicFullWindowManager {

	private static final String TAG = "MusicFullWindowManager";
	private static volatile MusicFullWindowManager mInstance;
	//迷你唱片机
	private MusicWindowPlayer mMusicWindowPlayer;
	private static WindowManager mWindowManager;

    public static MusicFullWindowManager getInstance() {
        if(null==mInstance){
            synchronized (MusicFullWindowManager.class) {
                if (null == mInstance) {
                    mInstance = new MusicFullWindowManager();
                }
            }
        }
		return mInstance;
	}

	private MusicFullWindowManager(){}

	/**
	 * 添加一个播放器View到窗口
	 * @param context Application全局上下文
	 */
	public synchronized MusicFullWindowManager createMiniJukeBoxToWindown(Context context) {
        createMiniJukeBoxToWindown(context,-1,-1);
		return mInstance;
	}

    /**
     * 添加一个播放器View到窗口,默认圆形播放器位于屏幕右下角
     * @param context Application全局上下文
     * @param offsetPixelX 圆形播放器X轴偏移量 单位像素 -1:使用默认
     * @param offsetPixelY 圆形播放器Y轴偏移量 单位像素 -1:使用默认
     */
	public synchronized void createMiniJukeBoxToWindown(Context context, int offsetPixelX, int offsetPixelY) {
        if(!isWindowShowing()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse( "package:"+ MusicUtils.getInstance().getPackageName(context)));
                    context.startActivity(intent);
                } else {
                    addMiniJukeBoxToWindown(context,offsetPixelX,offsetPixelY);
                }
            }else {
                addMiniJukeBoxToWindown(context,offsetPixelX,offsetPixelY);
            }
        }
	}

    /**
     * 添加一个View到窗口,默认位置是位于屏幕左上角，自行指定X、Y轴偏移量
     * @param context 全局上下文
     * @param offsetPixelX X轴偏移量 单位像素 -1:使用默认
     * @param offsetPixelY Y轴偏移量 单位像素 -1:使用默认
     */
    private synchronized void addMiniJukeBoxToWindown(Context context, int offsetPixelX, int offsetPixelY) {
        if (null== mMusicWindowPlayer) {
            WindowManager windowManager = getWindowManager(context);
            mMusicWindowPlayer = new MusicWindowPlayer(context);
            mMusicWindowPlayer.setLayoutParamsOffset(offsetPixelX,offsetPixelY);
            WindowManager.LayoutParams miniJukeBoxLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }else if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }else{
                miniJukeBoxLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            //不拦截焦点、使焦点穿透到底层
            miniJukeBoxLayoutParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            //背景透明
            miniJukeBoxLayoutParams.format = PixelFormat.RGBA_8888;
            //默认位于屏幕的左上角，具体位置定位定传X、Y偏移量
            miniJukeBoxLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            miniJukeBoxLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            miniJukeBoxLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            windowManager.addView(mMusicWindowPlayer, miniJukeBoxLayoutParams);
        }
    }

    /**
	 * 将小悬浮窗从屏幕上移除。
	 * @param context 全局上下文
	 */
	public synchronized void removeMiniJukeBoxFromWindow(Context context) {
		if(null!= mMusicWindowPlayer){
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(mMusicWindowPlayer);
            mMusicWindowPlayer = null;
		}
	}

    /**
     * 此应用是否拥有悬浮窗权限
     * @param context 全局上下文
     * @return 为true表示拥有悬浮窗权限
     */
	public boolean haveWindownPermission(Context context){
	    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
	        return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * @param context 全局上下文
     * @return true 允许  false禁止
     */
    public boolean checkAlertWindowsPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

	/**
	 * 更新小悬浮窗的TextView上的数据，显示正在加载中
	 */
	public void updateWindowStatus(MusicStatus musicStatus) {
		if(mMusicWindowPlayer !=null){
            mMusicWindowPlayer.updateData(musicStatus);
		}
	}

	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	/**
	 * 窗口是否有唱片机悬浮窗口显示
	 * @return 为true标识当前窗口已经存在悬浮窗
	 */
	public boolean isWindowShowing() {
		return null!= mMusicWindowPlayer;
	}

    /**
     * 悬浮窗中迷你唱片机动画开启
     */
    public void onResume() {
	    if(null!= mMusicWindowPlayer) {
            mMusicWindowPlayer.onResume();
        }
    }

    /**
     * 悬浮窗中迷你唱片机动画关闭
     */
    public void onPause() {
        if(null!= mMusicWindowPlayer) {
            mMusicWindowPlayer.onPause();
        }
    }

    /**
     * MINIJukeBox悬浮窗可见
     */
    public void onVisible() {
        if(null!= mMusicWindowPlayer) {
            mMusicWindowPlayer.onVisible();
        }
    }

    /**
     * MINIJukeBox悬浮窗可见
     * @param audioID 音频ID
     */
    public void onVisible(long audioID) {
        if(null!= mMusicWindowPlayer) {
            mMusicWindowPlayer.onVisible(audioID);
        }
    }

    /**
     * MINIJukeBox悬浮窗不可见
     */
    public void onInvisible() {
        if(null!= mMusicWindowPlayer) {
            mMusicWindowPlayer.onInvisible();
        }
    }

    /**
     * 组件中对应函数调用
     */
    public void onDestroy() {
        if(null!= mMusicWindowPlayer &&null!=mWindowManager){
            mMusicWindowPlayer.onDestroy();
            mWindowManager.removeViewImmediate(mMusicWindowPlayer);
            mMusicWindowPlayer =null;
        }
        mWindowManager=null;mInstance=null;
    }
}