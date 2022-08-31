package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.music.player.lib.R;
import com.music.player.lib.listener.MusicAnimatorListener;

/**
 * TinyHung@Outlook.com
 * 2019/3/18
 * Window Music Trash
 */

public class MusicWindowTrash extends RelativeLayout {

    private Context mContext;
    private Animation mShakeAnimation;
	private ImageView mIcTrash;
	private final TextView mTvTrash;
    private int viewWidth,viewHeight;
    private MusicFanLayout mTrashLayout;

    public MusicWindowTrash(Context context) {
        this(context,null);
    }

    public MusicWindowTrash(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicWindowTrash(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        LayoutInflater.from(context).inflate(R.layout.music_trash_window, this);
        mIcTrash = (ImageView) findViewById(R.id.music_ic_trash);
        mTvTrash = (TextView) findViewById(R.id.music_tv_trash);
        mShakeAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.music_shake);
        mTrashLayout = (MusicFanLayout) findViewById(R.id.music_trash_view);
    }

    /**
     * 返回扇形控件包含轨迹的Region
     * @return Region
     */
    public Region getRegion(){
        if(null!=mTrashLayout){
            return mTrashLayout.getRegion();
        }
        return null;
    }

    /**
     * 检测某个X,Y点是否在扇形内
     * @param rawX 相对于屏幕的X点
     * @param rawY 相对于屏幕的Y点
     * @return true:在扇形区域内
     */
    public boolean isContainsXY(int rawX,int rawY){
        if(null!=mTrashLayout){
            return mTrashLayout.isContainsXY(rawX,rawY);
        }
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.viewWidth=w;
        this.viewHeight=h;
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
        //alpha\scaleX\rotation\rotationX\translationX
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(MusicWindowTrash.this, "translationX", viewWidth,0);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(MusicWindowTrash.this, "translationY", viewHeight,0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator1,objectAnimator2);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new LinearInterpolator());
        MusicWindowTrash.this.setVisibility(VISIBLE);
        animatorSet.start();
	}

	/**
	 * 开始隐藏动画
	 */
    @SuppressLint("ObjectAnimatorBinding")
	public synchronized void startHideAnimation(final MusicAnimatorListener animatorListener){
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(MusicWindowTrash.this, "translationX", 0,viewWidth);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(MusicWindowTrash.this, "translationY", 0,viewHeight);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator1,objectAnimator2);
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                MusicWindowTrash.this.setVisibility(GONE);
                if(null!=animatorListener){
                    animatorListener.onAnimationEnd();
                }
            }
        });
        animatorSet.start();
	}

	/**
	 * 垃圾桶是否捕获到了焦点
	 * @param focusCap
	 */
	public void jukeBoxTrashFocusCap(boolean focusCap) {
		if(null!=mTvTrash){
			mTvTrash.setText(focusCap?"松手取消悬浮":"取消悬浮播放");
		}
	}

    /**
     * 垃圾桶提示
     * @param text
     */
    public void setText(String text) {
        if(null!=mTvTrash){
            mTvTrash.setText(text);
        }
    }

	public void onDestroy(){
		if(null!=mShakeAnimation){
			mShakeAnimation.cancel();
			mShakeAnimation=null;
		}
        MusicWindowTrash.this.clearAnimation();
        if(null!=mTrashLayout){
            mTrashLayout.onDestroy();
            mTrashLayout=null;
        }
		mIcTrash=null;mContext=null;mTrashLayout=null;
	}
}