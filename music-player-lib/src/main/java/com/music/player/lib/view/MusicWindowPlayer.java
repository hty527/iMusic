package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import com.music.player.lib.R;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.listener.MusicAnimatorListener;
import com.music.player.lib.manager.MusicFullWindowManager;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;

/**
 * Created by TinyHung@outlook.com
 * 2019/6/11
 * Music Window Player
 * 一个包含唱片机、垃圾桶的窗口View
 */

public class MusicWindowPlayer extends FrameLayout {

    private static final String TAG = "MusicWindowPlayer";
    //手势分发
    private GestureDetector mGestureDetector;
    //震动
    private Vibrator mVibrator;
    //迷你唱片机
    private MusicJukeBoxViewSmall mBoxViewSmall;
    //垃圾桶
    private MusicWindowTrash mWindowTrash;
    //悬浮球停靠在屏幕边上的边界大小
    private final int mMaginBorder;
    //手指在屏幕上的实时X、Y坐标
    private float xInScreen,yInScreen;
    //手指按下此View在屏幕中X、Y坐标
    private float xInView,yInView;
    //是否允许悬浮窗显示
    private boolean isVisible=true;
    //手指是否持续触摸屏幕中,是否持续碰撞中
    private boolean isTouchIng =false,isCollideIng=false;
    //容器的宽高
    private static int mGroupWidth, mGroupHeight;

    public MusicWindowPlayer(@NonNull Context context) {
        this(context,null);
    }

    public MusicWindowPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicWindowPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.music_window_player,this);
        mWindowTrash = (MusicWindowTrash) findViewById(R.id.music_window_trash);
        mBoxViewSmall = (MusicJukeBoxViewSmall) findViewById(R.id.music_window_juke);
        mVibrator = (Vibrator)getContext().getSystemService(getContext().VIBRATOR_SERVICE);
        //停靠边界
        mMaginBorder = MusicUtils.getInstance().dpToPxInt(getContext(), 15f);
        mGestureDetector = new GestureDetector(context,new TouchOnGestureListener());
        mBoxViewSmall.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);//如果是继承的viewgroup比如linearlayout时，可以先计算
        int widthResult = 0;
        //view根据xml中layout_width和layout_height测量出对应的宽度和高度值，
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthSpecMode){
            //match_parent
            case MeasureSpec.UNSPECIFIED:
                widthResult = widthSpecSize;
                break;
            //wrap_content
            case MeasureSpec.AT_MOST:
                widthResult = getContentWidth();
                break;
            //写死的固定高度
            case MeasureSpec.EXACTLY:
                //当xml布局中是准确的值，比如200dp是，判断一下当前view的宽度和准确值,取两个中大的，这样的好处是当view的宽度本事超过准确值不会出界
                widthResult = Math.max(getContentWidth(), widthSpecSize);
                break;
        }
        int heightResult = 0;
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightSpecMode){
            case MeasureSpec.UNSPECIFIED:
                heightResult = heightSpecSize;
                break;
            case MeasureSpec.AT_MOST:
                heightResult = getContentHeight();
                break;
            case MeasureSpec.EXACTLY:
                heightResult = Math.max(getContentHeight(), heightSpecSize);
                break;
        }
        this.mGroupWidth=widthResult;
        this.mGroupHeight=heightResult;
        setMeasuredDimension(widthResult, heightResult);
    }

    private int getContentWidth(){
        float contentWidth = getWidth()+getPaddingLeft()+getPaddingRight();
        return (int)contentWidth;
    }

    int getContentHeight(){
        float contentHeight = getHeight()+getPaddingTop()+getPaddingBottom();
        return (int)contentHeight;
    }

    /**
     * 拦截触摸屏幕的所有事件
     */
    private  class TouchOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * 拦截手势
         * @param e
         * @return
         */
        @Override
        public boolean onDown(MotionEvent e) {
            //记录手指按下时手指在唱片机View中的位置
            xInView = e.getX();
            yInView = e.getY();
            return true;
        }

        /**
         * 单击事件
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        /**
         * 手势分发持续滚动事件
         * @param event2
         * @param event1
         * @param distanceX
         * @param distanceY
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            switch (event2.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    //实时获取相对于屏幕X,Y位置刷新
                    xInScreen = event2.getRawX();
                    yInScreen = event2.getRawY();
                    updateJukeLocation();
                    updateTrachWindow(event2);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Logger.d(TAG,",ACTION_UP,ACTION_CANCEL");
                    actionTouchUp(event2);
                    break;
            }
            return true;
        }

        /**
         * 手势分发结束了
         * @param e1
         * @param e2
         * @param velocityX
         * @param velocityY
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            actionTouchUp(e2);
            return true;
        }
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
            if(containsXY){
                MusicPlayerManager.getInstance().onStop();
                MusicFullWindowManager.getInstance().removeMiniJukeBoxFromWindow(getContext().getApplicationContext());
            }else{
                hideTrashAnimation(mWindowTrash);
            }
        }
    }

    /**
     * 迷你唱片机的X,Y轴偏移量
     * @param offsetPixelX X轴位置
     * @param offsetPixelY Y轴位置
     */
    public void setLayoutParamsOffset(int offsetPixelX, int offsetPixelY) {
        if(offsetPixelX>-1&&offsetPixelY>-1){
            if(null!=mBoxViewSmall){
                mBoxViewSmall.setX(offsetPixelX);
                mBoxViewSmall.setY(offsetPixelY);
            }
        }
    }

    /**
     * 持续移动中
     * @param event 手势事件
     */
    private void updateTrachWindow(MotionEvent event) {
        if(null!=mWindowTrash){
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
        }
        isTouchIng =true;
    }

    /**
     * 交集碰撞检测
     * @param event 手势事件
     * @return  true:重合了
     */
    private boolean isContainsXY(MotionEvent event) {
        if(null!=mWindowTrash){
            //注意，这里需要使用屏幕坐标和和控件左边宽高的的 余集 的坐标位置，最终换算为垃圾桶View的view坐标
            int rawX = (int) event.getRawX()-(mGroupWidth-mWindowTrash.getWidth());
            //Y轴点应该加上状态栏高度，避免响应不准确
            int rawY = (int) event.getRawY()-(mGroupHeight-mWindowTrash.getHeight());
            return mWindowTrash.isContainsXY(rawX, rawY);
        }
        return false;
    }

    /**
     * 更新唱片机显示位置
     */
    private void updateJukeLocation() {
        if(null!=mBoxViewSmall){
            float toX = xInScreen - xInView;
            float toY = yInScreen - yInView;
            if(toX<0){
                toX=0;
            }else if(toX>(mGroupWidth -mBoxViewSmall.getWidth())){
                toX= mGroupWidth -mBoxViewSmall.getWidth();
            }
            if(toY<0){
                toY=0;
            }else if(toY>(mGroupHeight -mBoxViewSmall.getHeight())){
                toY= mGroupHeight -mBoxViewSmall.getHeight();
            }
            mBoxViewSmall.setX(toX);
            mBoxViewSmall.setY(toY);
        }
    }

    /**
     * 垃圾桶出现动画
     */
    @SuppressLint("ObjectAnimatorBinding")
    public synchronized void showTrashAnimation(View view){
        if(null==view) return;
        //alpha\scaleX\rotation\rotationX\translationX
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "translationX", view.getWidth(),0);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(),0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator1,objectAnimator2);
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new LinearInterpolator());
        view.setVisibility(VISIBLE);
        animatorSet.start();
    }

    /**
     * 垃圾桶隐藏动画
     */
    @SuppressLint("ObjectAnimatorBinding")
    public synchronized void hideTrashAnimation(final View view){
        if(null==view)return;
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
     * @param view
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
     * @param view
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
     * @param musicStatus
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
        isVisible=true;
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
        isVisible=true;
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
        isVisible=false;
    }

    public void onDestroy() {
        isVisible=false;
        if(null!=mBoxViewSmall){
            mBoxViewSmall.onDestroy();
            mBoxViewSmall=null;
        }
        if(null!=mWindowTrash){
            mWindowTrash.onDestroy();
            mWindowTrash=null;
        }
        if(null!=mVibrator){
            mVibrator.cancel();
            mVibrator=null;
        }
        mGroupWidth=0;mGroupHeight=0;mGestureDetector=null;
        xInScreen=0;yInScreen=0;xInView=0;yInView=0;isTouchIng=false;isCollideIng=false;
    }
}