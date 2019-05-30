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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.music.player.lib.R;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicImageCache;
import com.music.player.lib.util.MusicUtils;

/**
 * TinyHung@Outlook.com
 * 2019/3/6
 * PlayerJukeBox BG
 * 支持所有版本的渐变Layout
 */

public class MusicJukeBoxBackgroundLayout1 extends RelativeLayout {

    private static final String TAG = "recyclerImageViewBitmap";
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
    private ImageView mImageViewBg;
    private ImageView mImageViewFg;

    public MusicJukeBoxBackgroundLayout1(Context context) {
        this(context, null);
    }

    public MusicJukeBoxBackgroundLayout1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    //R.drawable.music_default_music_bg
    public MusicJukeBoxBackgroundLayout1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(-1,-1);
        mImageViewBg = new ImageView(context);
        mImageViewBg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mImageViewBg.setLayoutParams(layoutParams);
        mImageViewFg = new ImageView(context);
        mImageViewFg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mImageViewFg.setLayoutParams(layoutParams);
        this.addView(mImageViewBg);
        this.addView(mImageViewFg);
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(getContext());
        mScreenHeight = MusicUtils.getInstance().getScreenHeight(getContext());
        Bitmap resource = BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
        mImageViewBg.setImageBitmap(resource);
        initObjectAnimator();
    }


    @SuppressLint("ObjectAnimatorBinding")
    private void initObjectAnimator() {
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
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //将当前前景图层赋值到背景图层上
                if(null!=mImageViewBg&&null!=mImageViewFg){
                    recyclerImageViewBitmap(mImageViewBg);
                    if(null!=mImageViewFg.getDrawable()){
                        BitmapDrawable drawable = (BitmapDrawable) mImageViewFg.getDrawable();
                        mImageViewBg.setImageBitmap(drawable.getBitmap());
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
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
                                        BitmapDrawable bitmapDrawable= (BitmapDrawable) foregroundDrawable;
                                        if(null!=mImageViewFg){
                                            mImageViewFg.setImageBitmap(bitmapDrawable.getBitmap());
                                        }
                                        bitmapDrawable.getBitmap().recycle();
                                    }else{
                                        if(null!=mImageViewFg){
                                            mImageViewFg.setImageBitmap(bitmap);
                                        }
                                        bitmap.recycle();
                                    }
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    Bitmap resource = BitmapFactory.decodeResource(getResources(), R.drawable.music_default_music_bg);
                                    if(null!=mImageViewFg){
                                        mImageViewFg.setImageBitmap(resource);
                                    }
                                    resource.recycle();
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
                    BitmapDrawable bitmapDrawable= (BitmapDrawable) foregroundDrawable;
                    if(null!=mImageViewFg){
                        mImageViewFg.setImageBitmap(bitmapDrawable.getBitmap());
                    }
                    bitmapDrawable.getBitmap().recycle();
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

    /**
     * 回收ImageView的Bitmap
     * @param imageView imageView
     */
    private void recyclerImageViewBitmap(ImageView imageView) {
        if(null!=imageView&&null!=imageView.getDrawable()){
            try {
                Drawable drawable = imageView.getDrawable();
                if(drawable instanceof BitmapDrawable){
                    BitmapDrawable bitmapDrawable= (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    if(!bitmap.isRecycled()){
                        Logger.d(TAG,"recyclerImageViewBitmap-->");
                        bitmap.recycle();
                    }
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

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