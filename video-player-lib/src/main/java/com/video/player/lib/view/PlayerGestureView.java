package com.video.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.video.player.lib.R;
import com.video.player.lib.base.BaseGestureController;
import com.video.player.lib.utils.Logger;
import com.video.player.lib.utils.VideoUtils;

/**
 * TinyHung@Outlook.com
 * 2019/4/15
 * VideoPlayer GestureTouch
 * 默认的手势识别器
 */

public class PlayerGestureView extends BaseGestureController {

    private View mSoundPresent,mVideoPresent,mCurrentView;
    private ImageView mSoundIcon,mProgressIcon;
    private TextView mProgressText;
    private ProgressBar mSoundProgressBar,mProgressBar;

    public PlayerGestureView(@NonNull Context context) {
        this(context,null);
    }

    public PlayerGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlayerGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.video_full_plsyer_gesture_layout,this);
        mSoundPresent = findViewById(R.id.view_progress_sound_present);
        mVideoPresent = findViewById(R.id.view_progress_video_present);
        mSoundIcon = (ImageView) findViewById(R.id.view_sound_icon);
        mProgressIcon = (ImageView) findViewById(R.id.view_progress_icon);
        mProgressText = (TextView) findViewById(R.id.view_progress_text);
        mSoundProgressBar = (ProgressBar) findViewById(R.id.view_progress_sound_bar);
        mProgressBar = (ProgressBar) findViewById(R.id.view_progress_bar);
    }

    /**
     * 更新手势发生的场景
     * @param gestureScene 场景模式，详见此类常量定义说明
     */
    @Override
    public void updataGestureScene(int gestureScene) {
        if(null!=controllerRunnable){
            PlayerGestureView.this.removeCallbacks(controllerRunnable);
        }
        if(SCENE_PROGRESS==gestureScene){
            changedControllerUi(View.INVISIBLE, View.VISIBLE);
            mCurrentView=mVideoPresent;
        }else if(SCENE_BRIGHTNRSS==gestureScene){
            changedControllerUi(View.VISIBLE, View.INVISIBLE);
            if(null!=mSoundIcon){
                mSoundIcon.setImageResource(R.drawable.ic_video_brightness);
            }
            mCurrentView=mSoundPresent;
        }else if(SCENE_SOUND==gestureScene){
            changedControllerUi(View.VISIBLE, View.INVISIBLE);
            if(null!=mSoundIcon){
                mSoundIcon.setImageResource(R.drawable.ic_video_sound);
            }
            mCurrentView=mSoundPresent;
        }
    }

    /**
     * 更新快进、快退 UI回显
     * @param totalTime 视频总时长
     * @param speedTime 目标seek时长位置
     * @param progress 转换后的progress,单位百分比
     */
    @Override
    public void setVideoProgress(long totalTime, long speedTime, int progress) {
        Logger.d(TAG,"setVideoProgress-->"+progress);
        if(null!=mProgressText){
            String progressText = VideoUtils.getInstance().stringForAudioTime(speedTime) + "/" + VideoUtils.getInstance().stringForAudioTime(totalTime);
            mProgressText.setText(progressText);
        }
        if(null!=mProgressBar){
            //向左、向右标识
            if(null!=mProgressIcon){
                int oldProgress = mProgressBar.getProgress();
                //如果刚才设置的进度大于新的进度，即视为向左，相反反之
                if(oldProgress>progress){
                    mProgressIcon.setImageResource(R.drawable.ic_video_gesture_last);
                }else if(oldProgress<progress){
                    mProgressIcon.setImageResource(R.drawable.ic_video_gesture_next);
                }
            }
            mProgressBar.setProgress(progress);
        }
    }

    /**
     * 更新音量调节进度
     * @param progress 百分比
     */
    @Override
    public void setSoundrogress(int progress) {
        Logger.d(TAG,"setSoundrogress-->"+progress);
        if(null!=mSoundProgressBar){
            mSoundProgressBar.setProgress(progress);
        }
        if(null!=mSoundIcon){
            if(progress<=0){
                mSoundIcon.setImageResource(R.drawable.ic_video_sound_off);
            }else{
                mSoundIcon.setImageResource(R.drawable.ic_video_sound);
            }
        }
    }

    /**
     * 更新屏幕亮度进度UI
     * @param progress 百分比
     */
    @Override
    public void setBrightnessProgress(int progress) {
        Logger.d(TAG,"setBrightnessProgress-->"+progress);
        if(null!=mSoundProgressBar){
            mSoundProgressBar.setProgress(progress);
        }
    }

    /**
     * 这个默认手势识别器不需要内部处理onTouchEnevt事件，这里不关心
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return 为true即拦截触摸事件向BaseVideoPlayer传递
     */
    @Override
    public boolean onTouchEnevt(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    /**
     * 隐藏和显示控制器功能,还原透明度
     * @param sound 声音、亮度
     * @param progress 进度
     */
    private void changedControllerUi(int sound,int progress) {
        if(null!=mSoundPresent){
            mSoundPresent.setAlpha(1f);
            mSoundPresent.setVisibility(sound);
        }
        if(null!=mVideoPresent){
            mVideoPresent.setAlpha(1f);
            mVideoPresent.setVisibility(progress);
        }
    }

    /**
     * 负责控制器显示、隐藏
     */
    private  Runnable controllerRunnable=new Runnable() {
        @Override
        public void run() {
            if(null!=mCurrentView){
                ObjectAnimator animator = ObjectAnimator.ofFloat(mCurrentView, "alpha", 1.0f, 0.0f).setDuration(300);
                animator.setInterpolator(new LinearInterpolator());
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        reset();
                    }
                });
                animator.start();
            }
        }
    };

    /**
     * 内部释放
     */
    private void reset() {
        PlayerGestureView.this.removeCallbacks(controllerRunnable);
        if(null!=mSoundPresent){
            mSoundPresent.setVisibility(View.INVISIBLE);
        }
        if(null!=mVideoPresent){
            mVideoPresent.setVisibility(View.INVISIBLE);
        }

        if(null!=mCurrentView){
            mCurrentView.setVisibility(INVISIBLE);
        }

        if(null!=mSoundIcon){
            mSoundIcon.setImageResource(0);
        }
        if(null!=mProgressText){
            mProgressText.setText("");
        }
        if(null!=mSoundProgressBar){
            mSoundProgressBar.setProgress(0);
        }
        if(null!=mProgressBar){
            mProgressBar.setProgress(0);
        }
    }

    /**
     * 还原
     */
    @Override
    public void onReset() {
        onReset(800);
    }

    /**
     * 还原
     */
    @Override
    public void onReset(long delayedMilliss) {
        if(null!=mCurrentView&&null!=controllerRunnable){
            Logger.d(TAG,"onReset-->");
            PlayerGestureView.this.removeCallbacks(controllerRunnable);
            PlayerGestureView.this.postDelayed(controllerRunnable,delayedMilliss);
        }else{
            reset();
        }
    }

    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        Logger.d(TAG,"onDestroy-->");
        reset();
    }
}