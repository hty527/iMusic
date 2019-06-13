package com.video.player.lib.manager;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.video.player.lib.utils.VideoUtils;
import com.video.player.lib.view.VideoWindowPlayerGroup;
import java.lang.reflect.Method;

/**
 * TinyHung@Outlook.com
 * 2019/4/13
 * Video WindowManager
 */

public class VideoWindowManager {

	private static final String TAG = "VideoWindowManager";
	private static volatile VideoWindowManager mInstance;
	//悬浮窗口容器
	private VideoWindowPlayerGroup mVideoWindowPlayerGroup;
	private static WindowManager mWindowManager;

    public static VideoWindowManager getInstance() {
        if(null==mInstance){
            synchronized (VideoWindowManager.class) {
                if (null == mInstance) {
                    mInstance = new VideoWindowManager();
                }
            }
        }
		return mInstance;
	}

    private VideoWindowManager(){}

    /**
     * 添加一个播放器到窗口
     * @param context
     * @param offsetPixelX 屏幕中起始X轴
     * @param offsetPixelY 屏幕中起始Y轴
     * @param viewWidth 播放器宽
     * @param viewHeight 播放器高
     */
	public synchronized FrameLayout addVideoPlayerToWindow(Context context, int offsetPixelX,
                                    int offsetPixelY, int viewWidth, int viewHeight) {
        if(!isWindowShowing()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse( "package:"+ VideoUtils.getInstance().getPackageName(context)));
                    context.startActivity(intent);
                    return null;
                } else {
                    return addMiniVideoPlayerToWindown(context,offsetPixelX,offsetPixelY,viewWidth,viewHeight);
                }
            }else {
                return addMiniVideoPlayerToWindown(context,offsetPixelX,offsetPixelY,viewWidth,viewHeight);
            }
        }
        return null;
	}

    /**
     * 添加一个View到窗口
     * @param context
     * @param offsetPixelX
     * @param offsetPixelY
     * @param viewWidth
     * @param viewHeight
     * @param viewHeight
     */
    private synchronized FrameLayout addMiniVideoPlayerToWindown(Context context, int offsetPixelX,
                                     int offsetPixelY, int viewWidth, int viewHeight) {
        if (null!= mVideoWindowPlayerGroup) {
            removeMiniVideoPlayerFromWindow(context);
        }
        WindowManager windowManager = getWindowManager(context);
        mVideoWindowPlayerGroup = new VideoWindowPlayerGroup(context,windowManager);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        //WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }else{
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        //不拦截焦点、使焦点穿透到底层
        layoutParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        //背景透明
        layoutParams.format = PixelFormat.RGBA_8888;
        //需要默认位于屏幕的左上角，具体定位用x,y轴
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.width = viewWidth;
        layoutParams.height = viewHeight;
        layoutParams.x=offsetPixelX;
        layoutParams.y=offsetPixelY;
        mVideoWindowPlayerGroup.setWindowManagerParams(layoutParams);
        mVideoWindowPlayerGroup.setBackgroundColor(Color.parseColor("#000000"));
        windowManager.addView(mVideoWindowPlayerGroup, layoutParams);
        return mVideoWindowPlayerGroup;
    }

    /**
	 * 将播放器从窗口移除
	 * @param context
	 */
	public synchronized void removeMiniVideoPlayerFromWindow(Context context) {
		if(null!= mVideoWindowPlayerGroup){
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(mVideoWindowPlayerGroup);
			mVideoWindowPlayerGroup = null;
		}
	}

    /**
     * 此应用是否拥有悬浮窗权限
     * @param context 上下文
     * @return 是否拥有悬浮窗权限？
     */
	public boolean haveWindownPermission(Context context){
	    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
	        return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * @param context
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

	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	/**
	 * 窗口是否已被添加悬浮播放器
	 * @return 当前窗口是否已经存在悬浮窗
	 */
	public boolean isWindowShowing() {
		return null!= mVideoWindowPlayerGroup;
	}

    /**
     * 组件中对应函数调用
     */
    public void onDestroy() {
        if(null!= mVideoWindowPlayerGroup){
            mVideoWindowPlayerGroup.onDestroy();
        }
        if(null==mWindowManager){
            mWindowManager = getWindowManager(VideoUtils.getInstance().getContext());
        }
	    if(null!= mVideoWindowPlayerGroup &&null!=mWindowManager){
            mWindowManager.removeViewImmediate(mVideoWindowPlayerGroup);
            mVideoWindowPlayerGroup =null;
        }
        mWindowManager=null;mInstance=null;
    }
}