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
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
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
 * 兼容所有API版本的渐变BackgroundLayout
 */

public class MusicJukeBoxBackgroundLayout extends RelativeLayout {

    private static final String TAG = "MusicJukeBoxBackgroundLayout";
    private Context mContext;
    private int DURATION_ANIMATION = 500;
    private ObjectAnimator objectAnimator;
    //背景,前景图层
    private ImageView mImageViewBg,mImageViewFg;
    //屏幕宽高
    private int mScreenWidth,mScreenHeight;
    //任务Runnable
    private SetBackgroundRunnable mBackgroundRunnable;

    public MusicJukeBoxBackgroundLayout(Context context) {
        this(context, null);
    }

    public MusicJukeBoxBackgroundLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    //R.drawable.music_default_music_bg
    public MusicJukeBoxBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        mImageViewBg = new ImageView(context);
        mImageViewBg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(mImageViewBg,new RelativeLayout.LayoutParams(-1,-1));

        mImageViewFg = new ImageView(context);
        mImageViewFg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(mImageViewFg,new RelativeLayout.LayoutParams(-1,-1));
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(getContext());
        mScreenHeight = MusicUtils.getInstance().getScreenHeight(getContext());
        mImageViewBg.setImageResource(R.drawable.music_default_music_bg);
        boolean isEnable=false;
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicJukeBoxBackgroundLayout);
            isEnable= typedArray.getBoolean(R.styleable.MusicJukeBoxBackgroundLayout_backgroundEnable, false);
            typedArray.recycle();
        }
        if(isEnable){
            initObjectAnimator();
        }
    }
    public void setAnimatorEnable(boolean enable){
        if(enable){
            initObjectAnimator();
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private void initObjectAnimator() {
        if(null==objectAnimator){
            //渐变动画
            objectAnimator = ObjectAnimator.ofFloat(this, "number", 0f, 1.0f);
            objectAnimator.setDuration(DURATION_ANIMATION);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if(null!=mImageViewFg){
                        int foregroundAlpha = (int) ((float) animation.getAnimatedValue() * 255);
                        mImageViewFg.getDrawable().mutate().setAlpha(foregroundAlpha);
                    }
                }
            });
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    //将当前前景图层赋值到背景图层上
                    if(null!=mImageViewFg&&null!=mImageViewFg.getDrawable()){
                        Drawable drawable = mImageViewFg.getDrawable();
                        if(null!=mImageViewBg){
                            mImageViewBg.setImageDrawable(drawable);
                        }
                    }
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        }
    }

    /**
     * 设置背景封面
     * @param imageUrl 图片URL
     * @param delayMillis 加载图片并处理延时时长，单位：毫秒
     */
    public void setBackgroundCover(String imageUrl,long delayMillis) {
        setBackgroundCover(imageUrl,delayMillis,true);
    }

    /**
     * 设置背景封面
     * @param imageUrl 图片URL
     * @param delayMillis 加载图片并处理延时时长，单位：毫秒
     * @param isBlur bitmap是否虚化？
     */
    public synchronized void setBackgroundCover(String imageUrl,long delayMillis,boolean isBlur) {
        setBackgroundCover(imageUrl, delayMillis,isBlur,5);
    }

    /**
     * 设置背景封面
     * @param imageUrl 图片URL
     * @param delayMillis 加载图片并处理延时时长，单位：毫秒
     * @param isBlur bitmap是否虚化？
     * @param blurRadius bitmap 虚化角度阈值
     */
    public synchronized void setBackgroundCover(String imageUrl,long delayMillis,boolean isBlur,int blurRadius) {
        setBackgroundCover(imageUrl, delayMillis, isBlur, blurRadius,true);
    }

    /**
     * 设置背景封面
     * @param imageUrl 图片URL
     * @param delayMillis 加载图片并处理延时时长，单位：毫秒
     * @param isBlur bitmap是否虚化？
     * @param blurRadius bitmap 虚化角度阈值
     * @param shadeEnable 是否启用遮罩层图层
     */
    public synchronized void setBackgroundCover(String imageUrl,long delayMillis,boolean isBlur,int blurRadius,boolean shadeEnable) {
        if(null!=mBackgroundRunnable&&!TextUtils.isEmpty(mBackgroundRunnable.getImageUrl())
                &&mBackgroundRunnable.getImageUrl().equals(imageUrl)){
            //重复的，不做任何处理
            return;
        }
        if(null!=mBackgroundRunnable){
            mBackgroundRunnable.onReset();
            MusicJukeBoxBackgroundLayout.this.removeCallbacks(mBackgroundRunnable);
            mBackgroundRunnable=null;
        }
        mBackgroundRunnable = new SetBackgroundRunnable(imageUrl,isBlur,blurRadius,shadeEnable);
        MusicJukeBoxBackgroundLayout.this.postDelayed(mBackgroundRunnable,delayMillis);
    }

    /**
     * 设置背景封面
     * @param bitmap 位图
     */
    public void setForeground(Bitmap bitmap) {
        if(null!=mImageViewFg){
            mImageViewFg.setImageDrawable(new BitmapDrawable(bitmap));
            startGradualAnimator();
        }
    }

    /**
     * 设置背景封面
     * @param drawable 位图
     */
    public void setForeground(Drawable drawable) {
        if(null!=mImageViewFg){
            mImageViewFg.setImageDrawable(drawable);
            startGradualAnimator();
        }
    }

    /**
     * 开始渐变动画
     */
    private void startGradualAnimator() {
        if(null!=objectAnimator) {
            objectAnimator.start();
        }
    }

    private class SetBackgroundRunnable implements Runnable{
        //Bitmap是否虚化处理
        private final boolean mIsBlur;
        //遮罩图层默认是开启的
        private boolean mShadeEnable=true;
        //图片URL
        private String mImageUrl;
        //虚化半径
        private int mBlurRadius=5;

        public SetBackgroundRunnable(String imageUrl, boolean isBlur, int blurRadius, boolean shadeEnable) {
            this.mImageUrl=imageUrl;
            this.mIsBlur=isBlur;
            this.mBlurRadius=blurRadius;
            this.mShadeEnable=shadeEnable;
        }

        @Override
        public void run() {
            if(!TextUtils.isEmpty(mImageUrl)&&null!=mImageViewFg){
                //HTTP || HTTPS
                if(mImageUrl.startsWith("http:")|| mImageUrl.startsWith("https:")){
                    Glide.with(getContext())
                            .load(mImageUrl)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    if(null!=mImageViewFg){
                                        //是否高斯模糊处理
                                        if(mIsBlur){
                                            Drawable drawable;
                                            //是否添加遮罩层
                                            if(mShadeEnable){
                                                drawable = MusicUtils.getInstance().getForegroundDrawable(bitmap,
                                                        mScreenWidth, mScreenHeight, mBlurRadius, Color.parseColor("#FF999999"));
                                            }else{
                                                drawable = MusicUtils.getInstance().getForegroundDrawable(bitmap,
                                                        mScreenWidth, mScreenHeight, mBlurRadius, Color.parseColor("#00000000"));
                                            }
                                            if(null==drawable){
                                                drawable = ContextCompat.getDrawable(getContext(),R.drawable.music_default_music_bg);
                                            }
                                            mImageViewFg.setImageDrawable(drawable);
                                        }else{
                                            mImageViewFg.setImageDrawable(new BitmapDrawable(bitmap));
                                        }
                                        startGradualAnimator();
                                    }
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    if(null!=mImageViewFg){
                                        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.music_default_music_bg);
                                        mImageViewFg.setImageDrawable(drawable);
                                        startGradualAnimator();
                                    }
                                }
                            });
                }else{
                    //File
                    Bitmap bitmap;
                    bitmap = MusicImageCache.getInstance().getBitmap(mImageUrl);
                    if(null==bitmap){
                        bitmap=MusicImageCache.getInstance().createBitmap(mImageUrl);
                    }
                    if(null==bitmap){
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
                    }
                    Drawable foregroundDrawable = MusicUtils.getInstance().getForegroundDrawable(bitmap, mScreenWidth,
                            mScreenHeight, 5, Color.parseColor("#FF999999"));
                    if(null==foregroundDrawable){
                        //BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
                        foregroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.music_default_music_bg);
                    }
                    mImageViewFg.setImageDrawable(foregroundDrawable);
                    startGradualAnimator();
                }
            }
        }

        public void onReset() {
            mImageUrl=null;
        }

        public String getImageUrl() {
            return mImageUrl;
        }
    }

    private void recyclerBitmap(ImageView imageView) {
        if(null==imageView){
            return;
        }
        Drawable drawable = imageView.getDrawable();
        if(null!=drawable&& drawable instanceof BitmapDrawable){
            BitmapDrawable drawableBitmap = (BitmapDrawable) drawable;
            Bitmap bitmap = drawableBitmap.getBitmap();
            imageView.setImageBitmap(null);
            if(null!=bitmap&&bitmap.isRecycled()){
                bitmap.recycle();
                bitmap=null;
            }
        }else{
            imageView.setImageBitmap(null);
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
        recyclerBitmap(mImageViewBg);
        recyclerBitmap(mImageViewFg);
        this.removeAllViews();
        mScreenWidth=0;mScreenHeight=0;DURATION_ANIMATION=0;mContext=null;mImageViewBg=null;mImageViewFg=null;
    }
}