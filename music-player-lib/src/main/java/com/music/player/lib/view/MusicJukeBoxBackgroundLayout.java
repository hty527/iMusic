package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.music.player.lib.R;
import com.music.player.lib.util.MusicImageCache;
import com.music.player.lib.util.MusicUtils;

/**
 * TinyHung@Outlook.com
 * 2019/3/6
 * PlayerJukeBox BG
 * 要开启渐变，需指定backgroundEnable属性为true， 渐变特效只支持 >=23 API
 */

public class MusicJukeBoxBackgroundLayout extends RelativeLayout {

    private static final String TAG = "MusicJukeBoxBackgroundLayout";
    private int DURATION_ANIMATION = 500;
    //背景图层
    private int BACKGROUND_DRAWABLE=0;
    //前景图层
    private int FOREGROUND_DRAWABLE=1;
    //背景、前景 容器
    private LayerDrawable mLayerDrawable;
    private ObjectAnimator objectAnimator;
    private int mScreenWidth;
    private int mScreenHeight;
    private SetBackgroundRunnable mBackgroundRunnable;

    public MusicJukeBoxBackgroundLayout(Context context) {
        this(context, null);
    }

    public MusicJukeBoxBackgroundLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicJukeBoxBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        boolean isEnable= false;
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicJukeBoxBackgroundLayout);
            isEnable= typedArray.getBoolean(R.styleable.MusicJukeBoxBackgroundLayout_backgroundEnable, false);
            typedArray.recycle();
        }
        if(isEnable){
            initObjectAnimator();
        }else{
            MusicJukeBoxBackgroundLayout.this.setBackgroundResource(R.drawable.music_default_music_bg);
        }
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(getContext());
        mScreenHeight = MusicUtils.getInstance().getScreenHeight(getContext());
    }

    /**
     * 此特效只支持6.0及以上API
     */
    @SuppressLint("ObjectAnimatorBinding")
    private void initObjectAnimator() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //初始化默认图层
            Drawable defaultBackgroundDrawable = ContextCompat.getDrawable(getContext(),R.drawable.music_default_music_bg);
            Drawable[] drawables=new Drawable[2];
            drawables[BACKGROUND_DRAWABLE]=defaultBackgroundDrawable;
            drawables[FOREGROUND_DRAWABLE]=defaultBackgroundDrawable;
            mLayerDrawable = new LayerDrawable(drawables);
            MusicJukeBoxBackgroundLayout.this.setBackground(mLayerDrawable);
            //渐变动画
            objectAnimator = ObjectAnimator.ofFloat(this, "number", 0f, 1.0f);
            objectAnimator.setDuration(DURATION_ANIMATION);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(null!=mLayerDrawable){
                        //前景图层慢慢显示
                        int foregroundAlpha = (int) ((float) animation.getAnimatedValue() * 255);
                        mLayerDrawable.getDrawable(FOREGROUND_DRAWABLE).setAlpha(foregroundAlpha);
                        MusicJukeBoxBackgroundLayout.this.setBackground(mLayerDrawable);
                    }
                }
            });
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //将当前前景图层赋值到背景图层上
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&null!=mLayerDrawable) {
                        mLayerDrawable.setDrawable(BACKGROUND_DRAWABLE,mLayerDrawable.getDrawable(FOREGROUND_DRAWABLE));
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }else{
            MusicJukeBoxBackgroundLayout.this.setBackgroundResource(R.drawable.music_default_music_bg);
        }
    }

    /**
     * 设置背景封面
     * @param frontCover
     * @param delayMillis
     */
    public void setBackgroundCover(String frontCover,long delayMillis) {
        setBackgroundCover(frontCover,delayMillis,true);
    }

    /**
     * 设置背景封面
     * @param frontCover
     * @param delayMillis
     * @param isBlur 是否毛玻璃处理
     */
    public synchronized void setBackgroundCover(String frontCover,long delayMillis,boolean isBlur) {
        if(null!=mLayerDrawable&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(null!=mBackgroundRunnable&&!TextUtils.isEmpty(mBackgroundRunnable.getFrontCover())&&mBackgroundRunnable.getFrontCover().equals(frontCover)){
                //重复的，不做任何处理
                return;
            }
            if(null!=mBackgroundRunnable){
                mBackgroundRunnable.onReset();
                MusicJukeBoxBackgroundLayout.this.removeCallbacks(mBackgroundRunnable);
                mBackgroundRunnable=null;
            }
            mBackgroundRunnable = new SetBackgroundRunnable(frontCover,isBlur);
            MusicJukeBoxBackgroundLayout.this.postDelayed(mBackgroundRunnable,delayMillis);
        }
    }

    /**
     * 设置背景封面
     * @param drawable
     */
    public void setForeground(Drawable drawable) {
        if(null!=mLayerDrawable&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            mLayerDrawable.setDrawable(FOREGROUND_DRAWABLE,drawable);
            startGradualAnimator();
        }
    }

    /**
     * 开始渐变动画
     */
    private void startGradualAnimator() {
        if(null!=objectAnimator&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            objectAnimator.start();
        }
    }

    private class SetBackgroundRunnable implements Runnable{
        //是否毛玻璃处理
        private final boolean mIsBlur;
        private String mFrontCover;

        public SetBackgroundRunnable(String frontCover, boolean isBlur) {
            this.mFrontCover=frontCover;
            this.mIsBlur=isBlur;
        }

        @Override
        public void run() {
            if(!TextUtils.isEmpty(mFrontCover)){
                //HTTP || HTTPS
                if(mFrontCover.startsWith("http:")|| mFrontCover.startsWith("https:")){
                    Glide.with(getContext())
                            .load(mFrontCover)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    if(null==bitmap){
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
                                    }
                                    if(mIsBlur){
                                        Drawable foregroundDrawable = MusicUtils.getInstance().getForegroundDrawable(bitmap, mScreenWidth, mScreenHeight, 5, Color.parseColor("#FF999999"));
                                        if(null==foregroundDrawable){
                                            foregroundDrawable = ContextCompat.getDrawable(getContext(),R.drawable.music_default_music_bg);
                                        }
                                        setForeground(foregroundDrawable);
                                    }else{
                                        BitmapDrawable drawable = new BitmapDrawable(bitmap);
                                        setForeground(drawable);
                                    }
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.music_default_music_bg);
                                    if(null!=drawable){
                                        setForeground(drawable);
                                    }
                                }
                            });
                }else{
                    //File
                    Bitmap bitmap;
                    bitmap = MusicImageCache.getInstance().getBitmap(mFrontCover);
                    if(null==bitmap){
                        bitmap=MusicImageCache.getInstance().createBitmap(mFrontCover);
                    }
                    if(null==bitmap){
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
                    }
                    Drawable foregroundDrawable = MusicUtils.getInstance().getForegroundDrawable(bitmap, mScreenWidth, mScreenHeight, 5,Color.parseColor("#FF999999"));
                    if(null==foregroundDrawable){
                        foregroundDrawable = ContextCompat.getDrawable(getContext(),R.drawable.music_default_music_bg);
                    }
                    setForeground(foregroundDrawable);
                }
            }
        }

        public void onReset() {
            mFrontCover=null;
        }

        public String getFrontCover() {
            return mFrontCover;
        }
    }

    public void onDestroy(){
        if(null!=mBackgroundRunnable){
            mBackgroundRunnable.onReset();
            MusicJukeBoxBackgroundLayout.this.removeCallbacks(mBackgroundRunnable);
            mBackgroundRunnable=null;
        }
        if(null!=objectAnimator){
            objectAnimator.cancel();
            objectAnimator=null;
        }
        if(null!=mLayerDrawable){
            mLayerDrawable=null;
        }
        setBackgroundResource(0);
        mScreenWidth=0;mScreenHeight=0;DURATION_ANIMATION=0;
    }
}