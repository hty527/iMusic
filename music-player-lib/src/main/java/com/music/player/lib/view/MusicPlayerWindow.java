package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import com.music.player.lib.R;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.util.MusicUtils;

/**
 * Created by TinyHung@outlook.com
 * 2019/6/11
 * Music Window Player
 * 一个持有垃圾桶交互的音乐播放器mini窗口，适用于Window
 */

public class MusicPlayerWindow extends FrameLayout{

    private static final String TAG = "MusicPlayerWindow";
    //状态栏高度屏幕的宽高,悬浮球停靠在屏幕边界的最小距离
    private int mStatusBarHeight, mScreenWidth, mScreenHeight,mHorMiniMagin;
    //震动
    private Vibrator mVibrator=null;
    //窗口
    private WindowManager mWindowManager=null;
    //窗口参数
    private WindowManager.LayoutParams mWindowLayoutParams=null;
    //迷你唱片机
    private MusicJukeBoxViewSmall mBoxViewSmall=null;
    //垃圾桶
    private MusicWindowTrash mWindowTrash=null;
    //手势分发
    private GestureDetector mGestureDetector=null;
    //手指在屏幕上的实时X、Y坐标
    private float xInScreen,yInScreen;
    //手指按下X、Y坐标
    private static float xDownInScreen,yDownInScreen;
    //手指按下此View在屏幕中X、Y坐标
    private float xInView,yInView;
    //手指是否持续触摸屏幕中,是否持续碰撞中
    private boolean isTouchIng =false,isCollideIng=false;
    //单击\滚动 事件的有效像素
    public static int SCROLL_PIXEL=5;

    public MusicPlayerWindow(@NonNull Context context) {
        this(context,null);
    }

    public MusicPlayerWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicPlayerWindow(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.music_player_window,this);
        mBoxViewSmall = (MusicJukeBoxViewSmall) findViewById(R.id.music_window_juke);
        mVibrator = (Vibrator)getContext().getSystemService(getContext().VIBRATOR_SERVICE);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicPlayerWindow);
            mHorMiniMagin = typedArray.getDimensionPixelSize(R.styleable.MusicPlayerWindow_musicPlayerWinHorMagin, 15);
            typedArray.recycle();
        }else{
            //停靠边界,四周
            mHorMiniMagin = MusicUtils.getInstance().dpToPxInt(getContext(), 15f);
        }
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(context);
        mScreenHeight = MusicUtils.getInstance().getScreenHeight(context);
        //手势分发
        mGestureDetector = new GestureDetector(getContext(),new JukeBoxGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录手指按下时手指在唱片机View中的位置
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen=event.getRawX();
                yDownInScreen = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //实时获取相对于屏幕X,Y位置刷新
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                updateJukeLocation();
                updateTrachWindow(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                actionTouchUp(event);
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 手势识别
     */
    private class JukeBoxGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            //前往播放器界面
            String activityName = MusicPlayerManager.getInstance().getPlayerActivityName();
            if(!TextUtils.isEmpty(activityName)&&null!=mBoxViewSmall.getTag()){
                Context context = getContext().getApplicationContext();
                Intent startIntent=new Intent();
                startIntent.setClassName(context.getPackageName(),activityName);
                startIntent.putExtra(MusicConstants.KEY_MUSIC_ID, (Long) mBoxViewSmall.getTag());
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startIntent);
            }
            return super.onSingleTapUp(event);
        }
    }

    public void setWindowLayoutParams(WindowManager.LayoutParams windowLayoutParams) {
        this.mWindowLayoutParams=windowLayoutParams;
    }

    public void setTrashWindow(MusicWindowTrash windowTrash) {
        this.mWindowTrash=windowTrash;
    }

    public void setWindowManager(WindowManager windowManager) {
        mWindowManager = windowManager;
    }

    /**
     * 松手
     * @param event 手势事件
     */
    private void actionTouchUp(MotionEvent event) {
        xInView=0;yInView=0;
        isTouchIng =false;isCollideIng=false;
        if(null!=mWindowTrash){
            boolean containsXY = isContainsXY(event);
            //如果松手时重合了
            if(containsXY){
                MusicPlayerManager.getInstance().onStop();
                MusicWindowManager.getInstance().removeAllWindowView(getContext().getApplicationContext());
                return;
            }else{
                //未重合，隐藏垃圾桶
                hideTrashAnimation(mWindowTrash);
                //自动吸附到控件边侧
                int[] locations=new int[2];
                getLocationOnScreen(locations);
                scrollToPixel(locations[0],locations[1], (int) event.getRawX(),350);
                //判断是否触发单击事件 Math.abs(xInScreen - xDownInScreen) < SCROLL_PIXEL && Math.abs(yInScreen - yDownInScreen) < SCROLL_PIXEL
            }
        }
    }

    /**
     * 移除View的Parent
     * @param view 目标View
     */
    private void removeViewByGroupVoew(View view) {
        if(null!=view&&null!=view.getParent()){
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }
    }

    /**
     * 返回唱片机的锚点是否在屏幕左侧
     * @return true：位于屏幕左侧
     * @param event
     */
    public boolean isLeftPoint(MotionEvent event){
        if(event.getRawX()>(mScreenWidth /2)){
            return false;
        }
        return true;
    }

    /**
     * 设置横向的最小边距
     * @param maginBorder 边距距离
     */
    public void setHorMiniMagin(int maginBorder) {
        this.mHorMiniMagin =maginBorder;
    }

    /**
     * 获取状态栏高度
     * @return 状态栏高度
     */
    private int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            mStatusBarHeight =MusicUtils.getInstance().getStatusBarHeight(getContext());
        }
        return mStatusBarHeight;
    }

    /**
     * 持续移动中
     * @param event 手势事件
     */
    private void updateTrachWindow(MotionEvent event) {
        //手势垃圾桶,在用户手指上下滑动10个像素触发垃圾桶,
        if(null!=mWindowTrash&&Math.abs(xInScreen-xDownInScreen)>=SCROLL_PIXEL||Math.abs(yInScreen-yDownInScreen)>=SCROLL_PIXEL){
            //持续碰撞检测
            boolean containsXY=isContainsXY(event);
            mWindowTrash.setText(containsXY?"松手取消悬浮":"取消悬浮播放");
            //首次触摸移动检测，显示垃圾桶
            if(!isTouchIng){
                showTrashAnimation(mWindowTrash);
            }
            //首次碰撞检测,并且重合，震动
            if(!isCollideIng&&containsXY&&null!=mVibrator){
                //震动
                mVibrator.vibrate(50);
                //抖动
                mWindowTrash.startShakeAnimation();
            }
            //是否重合
            if(containsXY){
                isCollideIng=true;
            }else{
                isCollideIng=false;
            }
            isTouchIng =true;
        }
    }

    /**
     * 交集碰撞检测
     * @param event 手势事件
     * @return  true:重合了
     */
    private boolean isContainsXY(MotionEvent event) {
        if(null!=mWindowTrash){
            //注意，这里需要使用屏幕坐标和和控件左边宽高的的 余集 的坐标位置，最终换算为垃圾桶View的view坐标
            int rawX = (int) event.getRawX()-(mScreenWidth -mWindowTrash.getWidth());
            //Y轴点应该加上状态栏高度，避免响应不准确
            int rawY = (int) event.getRawY()-(mScreenHeight -mWindowTrash.getHeight());
            return mWindowTrash.isContainsXY(rawX, rawY);
        }
        return false;
    }

    /**
     * 更新唱片机显示位置
     */
    private void updateJukeLocation() {
        if(null!=mWindowManager&&null!=mWindowLayoutParams){
            float toX = xInScreen - xInView;
            float toY = yInScreen - yInView;
            if(toX< mHorMiniMagin){
                toX= mHorMiniMagin;
            }else if(toX>(mScreenWidth -getWidth()- mHorMiniMagin)){
                toX= mScreenWidth -getWidth()- mHorMiniMagin;
            }
            if(toY<0){
                toY=0;
            }else if(toY>(mScreenHeight -getHeight())){
                toY= mScreenHeight -getHeight();
            }
            mWindowLayoutParams.x = (int) toX;
            mWindowLayoutParams.y = (int) toY;
            mWindowManager.updateViewLayout(this, mWindowLayoutParams);
        }
    }

    /**
     * 自动吸附至屏幕位置
     * @param viewCurrentPixelX 悬浮窗当前所属位置X
     * @param viewCurrentPixelY 悬浮窗当前所属位置Y
     * @param currentRowX 手指松手所在的位置
     * @param scrollDurtion 滚动耗时
     */
    @SuppressLint("ObjectAnimatorBinding")
    private void scrollToPixel(int viewCurrentPixelX, final int viewCurrentPixelY, int currentRowX, int scrollDurtion) {
        int toPixelX= mHorMiniMagin;
        if(currentRowX>(mScreenWidth /2)){
            //左边停靠最大X：屏幕宽-自身宽-边距大小
            toPixelX=(mScreenWidth -getWidth()- mHorMiniMagin);
        }
        //Logger.d(TAG,"scrollToPixel:pixelX:"+viewCurrentPixelX+",pixelY:"+viewCurrentPixelY+",
        // currentRowX:"+currentRowX+",scrollDurtion:"+scrollDurtion+",toPixelX:"+toPixelX);
        if(null!=mWindowManager&&null!=mWindowLayoutParams){
            if(scrollDurtion<=0){
                mWindowLayoutParams.x = toPixelX;
                mWindowLayoutParams.y =  viewCurrentPixelY;
                mWindowManager.updateViewLayout(this, mWindowLayoutParams);
                return;
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "number", viewCurrentPixelX, toPixelX);
            objectAnimator.setDuration(scrollDurtion);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    mWindowLayoutParams.x = animatedValue;
                    mWindowLayoutParams.y =  viewCurrentPixelY;
                    mWindowManager.updateViewLayout(MusicPlayerWindow.this, mWindowLayoutParams);
                }
            });
            objectAnimator.start();
        }
    }

    /**
     * 垃圾桶出现动画
     * @param view 锚点View
     */
    @SuppressLint("ObjectAnimatorBinding")
    public synchronized void showTrashAnimation(View view){
        if(null==view) return;
        //alpha\scaleX\rotation\rotationX\translationX
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "translationX", view.getWidth(),0);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(),0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator1,objectAnimator2);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new LinearInterpolator());
        view.setVisibility(VISIBLE);
        animatorSet.start();
    }

    /**
     * 垃圾桶隐藏动画
     * @param view 锚点View
     */
    @SuppressLint("ObjectAnimatorBinding")
    public synchronized void hideTrashAnimation(final View view){
        if(null==view)return;
        view.clearAnimation();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "translationX", 0,view.getWidth());
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationY", 0,view.getHeight());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator1,objectAnimator2);
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(null!=view){
                    view.setVisibility(GONE);
                }
            }
        });
        animatorSet.start();
    }

    /**
     * 播放器悬浮窗可见
     * @param view 锚点View
     */
    public void showWindowAnimation(final View view){
        if(null==view) return;
        if(view.getVisibility()==VISIBLE) return;
        view.clearAnimation();
        AnimatorSet animatorSet=new AnimatorSet();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleX", 0.0f, 1.0f).setDuration(600);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 0.0f, 1.0f).setDuration(600);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 1.0f).setDuration(1);
        animatorSet.playTogether(animator1,animator2,animator3);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(VISIBLE);
            }
        });
        animatorSet.start();
    }

    /**
     * 播放器悬浮窗不可见
     * @param view 锚点View
     */
    public void hideWindowAnimation(final View view){
        if(null==view) return;
        if(view.getVisibility()==GONE) return;
        view.clearAnimation();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
        objectAnimator.setDuration(160);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(null!=view){
                    view.setVisibility(GONE);
                }
            }
        });
        objectAnimator.start();
    }

    /**
     * 刷新数据
     * @param musicStatus 播放器内部状态
     */
    public void updateData(MusicStatus musicStatus) {
        if(null!=mBoxViewSmall){
            mBoxViewSmall.updateData(musicStatus);
        }
    }

    public void onResume() {
        if(null!=mBoxViewSmall){
            mBoxViewSmall.onResume();
        }
    }

    public void onPause() {
        if(null!=mBoxViewSmall){
            mBoxViewSmall.onPause();
        }
    }

    /**
     * 唱片机可见
     */
    public void onVisible() {
        if(null!=mBoxViewSmall){
            mBoxViewSmall.clearAnimation();
            mBoxViewSmall.onVisible();
        }
        showWindowAnimation(mBoxViewSmall);
    }

    /**
     * 唱片机可见
     * @param audioID 音频ID
     */
    public void onVisible(long audioID) {
        if(null!=mBoxViewSmall){
            mBoxViewSmall.clearAnimation();
            if(audioID>0){
                mBoxViewSmall.setTag(audioID);
            }
            mBoxViewSmall.onVisible();
        }
        showWindowAnimation(mBoxViewSmall);
    }

    /**
     * 唱片机不可见
     */
    public void onInvisible() {
        if(null!=mBoxViewSmall){
            mBoxViewSmall.clearAnimation();
            mBoxViewSmall.onInvisible();
            if(mBoxViewSmall.getVisibility()!=GONE){
                hideWindowAnimation(mBoxViewSmall);
            }
        }
    }

    public void onDestroy() {
        if(null!=mBoxViewSmall){
            mBoxViewSmall.onDestroy();
            mBoxViewSmall=null;
        }
        if(null!=mVibrator){
            mVibrator.cancel();
            mVibrator=null;
        }
        mScreenWidth =0;
        mScreenHeight =0;
        xInScreen=0;yInScreen=0;xInView=0;yInView=0;isTouchIng=false;isCollideIng=false;
    }
}