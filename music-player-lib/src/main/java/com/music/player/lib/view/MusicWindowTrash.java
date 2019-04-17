package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.music.player.lib.R;
import com.music.player.lib.listener.MusicAnimatorListener;
import com.music.player.lib.util.Logger;

/**
 * TinyHung@Outlook.com
 * 2019/3/18
 * Window Trash
 */

public class MusicWindowTrash extends LinearLayout {

    public static final String TAG="MusicWindowTrash";
    private Animation mShakeAnimation;
	private ImageView mIcTrash;
	private final TextView mTvTrash;
	//是否正在清除中
	private boolean isCleaning=false;
	private boolean isStarting=false;

    public MusicWindowTrash(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.music_trash_window, this);
        mIcTrash = (ImageView) findViewById(R.id.music_ic_trash);
        mTvTrash = (TextView) findViewById(R.id.music_tv_trash);
        mShakeAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.music_shake);
    }

	/**
	 * 执行垃圾桶抖动动画
	 */
	public synchronized void startShakeAnimation() {
		if(null!=mIcTrash&&null!=mShakeAnimation){
			mIcTrash.startAnimation(mShakeAnimation);
		}
	}

	/**
	 * 开始显示动画
	 */
    @SuppressLint("ObjectAnimatorBinding")
	public synchronized void startTrashWindowAnimation(){
        if(isStarting){
            return;
        }
        Logger.d(TAG,"startTrashWindowAnimation-->");
        isStarting=true;
        this.clearAnimation();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(MusicWindowTrash.this, "alpha", 0.0f, 0.9f);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isStarting=false;
                if(null!=mTvTrash){
                    mTvTrash.setAlpha(1.0f);
                }
            }
        });
        objectAnimator.start();
	}

	/**
	 * 开始隐藏动画
	 */
    @SuppressLint("ObjectAnimatorBinding")
	public synchronized void startHideAnimation(final MusicAnimatorListener animatorListener){
        if(isCleaning){
            return;
        }
        Logger.d(TAG,"startHideAnimation-->");
        isCleaning=true;
        this.clearAnimation();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(MusicWindowTrash.this, "alpha", 0.9f, 0.0f);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isCleaning=false;
                if(null!=animatorListener){
                    animatorListener.onAnimationEnd();
                }
            }
        });
        objectAnimator.start();
	}

	/**
	 * 垃圾桶是否捕获到了焦点
	 * @param focusCap
	 */
	public void jukeBoxTrashFocusCap(boolean focusCap) {
		if(null!=mTvTrash){
			mTvTrash.setText(focusCap?"松手关闭悬浮播放":"拖动到此处关闭悬浮播放");
		}
	}

	public void onDestroy(){
		if(null!=mShakeAnimation){
			mShakeAnimation.cancel();
			mShakeAnimation=null;
		}
		MusicWindowTrash.this.clearAnimation();
		mIcTrash=null;
	}
}