package com.video.player.lib.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * TinyHung@Outlook.com
 * 2019/4/15
 * Base VideoPlayer GestureTouch
 * 手势识别器基类
 */

public abstract class BaseGestureController extends FrameLayout{

    protected static final String TAG = "BaseGestureController";
    /**
     * 子类必须密切关注此常量定义
     */
    //改变播放进度
    public static final int SCENE_PROGRESS=1;
    //改变屏幕亮度
    public static final int SCENE_BRIGHTNRSS=2;
    //改变声音大小
    public static final int SCENE_SOUND=3;

    /**
     * 手势持续滚动的方向
     */
    //持续向左滑动
    public static final int SCROLL_ORIENTATION_LEFT=0;
    //持续向上滑动
    public static final int SCROLL_ORIENTATION_TOP=1;
    //持续向右滑动
    public static final int SCROLL_ORIENTATION_RIGHT=2;
    //持续向下滑动
    public static final int SCROLL_ORIENTATION_BUTTOM=3;

    public BaseGestureController(@NonNull Context context) {
        this(context,null);
    }

    public BaseGestureController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BaseGestureController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 更新场景
     * @param gestureScene 场景模式，详见此类常量定义说明
     */
    public abstract void updataGestureScene(int gestureScene);

    /**
     * 更新播放进度显示
     * @param totalTime 视频总时长
     * @param speedTime 目标seek时长位置
     * @param progress 转换后的progress,单位百分比
     */
    public abstract void setVideoProgress(long totalTime,long speedTime,int progress);

    /**
     * 设置音量百分比进度
     * @param progress 百分比
     */
    public abstract void setSoundrogress(int progress);

    /**
     * 设置亮度百分比进度
     * @param progress 百分比
     */
    public abstract void setBrightnessProgress(int progress);

    /**
     * 手势事件，当其他四个主要更新UI方法无法满足你的场景时，你应该关心这个方法，如果不允许BaseVideoPlayer
     * 处理手势事件，则返回true：即拦截事件向BaseVideoPlayer传递
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return 为true则表示消费触摸事件并拦截事件继续向BaseVideoPlayer传递
     */
    public abstract boolean onTouchEnevt(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    /**
     * 复原
     */
    public abstract void onReset();

    /**
     * 复原
     * @param delayedMilliss 完全不可见的延时
     */
    public abstract void onReset(long delayedMilliss);

    /**
     * 销毁
     */
    public abstract void onDestroy();
}