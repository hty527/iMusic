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
@Deprecated
public class MusicJukeBoxBackgroundLayout1 extends RelativeLayout {

    private Context mContext;
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

    public MusicJukeBoxBackgroundLayout1(Context context) {
        this(context, null);
    }

    public MusicJukeBoxBackgroundLayout1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicJukeBoxBackgroundLayout1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        boolean isEnable= false;
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicJukeBoxBackgroundLayout);
            isEnable= typedArray.getBoolean(R.styleable.MusicJukeBoxBackgroundLayout_backgroundEnable, false);
            typedArray.recycle();
        }
        if(isEnable){
            initObjectAnimator();
        }else{
            MusicJukeBoxBackgroundLayout1.this.setBackgroundResource(R.drawable.music_default_music_bg);
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
            MusicJukeBoxBackgroundLayout1.this.setBackground(mLayerDrawable);
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
                        MusicJukeBoxBackgroundLayout1.this.setBackground(mLayerDrawable);
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
            MusicJukeBoxBackgroundLayout1.this.setBackgroundResource(R.drawable.music_default_music_bg);
        }
    }

    /**
     * 设置背景封面
     * @param imageUrl 图片URL
     * @param delayMillis 加载图片并处理延时时长，单位：毫秒
     */
    @Deprecated
    public void setBackgroundCover(String imageUrl,long delayMillis) {
        setBackgroundCover(imageUrl,delayMillis,true);
    }

    /**
     * 设置背景封面
     * @param imageUrl 图片URL
     * @param delayMillis 加载图片并处理延时时长，单位：毫秒
     * @param isBlur bitmap是否虚化？
     */
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public synchronized void setBackgroundCover(String imageUrl,long delayMillis,boolean isBlur,int blurRadius,boolean shadeEnable) {
        if(null!=mLayerDrawable&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(null!=mBackgroundRunnable&&!TextUtils.isEmpty(mBackgroundRunnable.getImageUrl())
                    &&mBackgroundRunnable.getImageUrl().equals(imageUrl)){
                //重复的，不做任何处理
                return;
            }
            if(null!=mBackgroundRunnable){
                mBackgroundRunnable.onReset();
                MusicJukeBoxBackgroundLayout1.this.removeCallbacks(mBackgroundRunnable);
                mBackgroundRunnable=null;
            }
            mBackgroundRunnable = new SetBackgroundRunnable(imageUrl,isBlur,blurRadius,shadeEnable);
            MusicJukeBoxBackgroundLayout1.this.postDelayed(mBackgroundRunnable,delayMillis);
        }
    }

    /**
     * 设置背景封面
     * @param drawable
     */
    @Deprecated
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
            if(!TextUtils.isEmpty(mImageUrl)){
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
                                    if(null==bitmap){
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
                                    }
                                    if(mIsBlur){
                                        Drawable foregroundDrawable=null;
                                        if(mShadeEnable){
                                            foregroundDrawable = MusicUtils.getInstance().getForegroundDrawable(bitmap,
                                                    mScreenWidth, mScreenHeight, mBlurRadius, Color.parseColor("#FF999999"));
                                        }else{
                                            foregroundDrawable = MusicUtils.getInstance().getForegroundDrawable(bitmap,
                                                    mScreenWidth, mScreenHeight, mBlurRadius, Color.parseColor("#00000000"));
                                        }
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
                    bitmap = MusicImageCache.getInstance().getBitmap(mImageUrl);
                    if(null==bitmap){
                        bitmap=MusicImageCache.getInstance().createBitmap(mImageUrl);
                    }
                    if(null==bitmap){
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
                    }
                    Drawable foregroundDrawable = MusicUtils.getInstance().getForegroundDrawable(bitmap, mScreenWidth,
                            mScreenHeight, 5,Color.parseColor("#FF999999"));
                    if(null==foregroundDrawable){
                        foregroundDrawable = ContextCompat.getDrawable(getContext(),R.drawable.music_default_music_bg);
                    }
                    setForeground(foregroundDrawable);
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

    @Deprecated
    public void onDestroy(){
        if(null!=mBackgroundRunnable){
            mBackgroundRunnable.onReset();
            MusicJukeBoxBackgroundLayout1.this.removeCallbacks(mBackgroundRunnable);
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
        mScreenWidth=0;mScreenHeight=0;DURATION_ANIMATION=0;mContext=null;
    }
}