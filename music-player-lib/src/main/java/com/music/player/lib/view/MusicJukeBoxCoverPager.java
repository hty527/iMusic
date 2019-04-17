package com.music.player.lib.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.music.player.lib.R;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/6
 * MusicJukeBoxCoverPager
 */

public class MusicJukeBoxCoverPager extends LinearLayout{

    private static final String TAG = "MusicJukeBoxCoverPager";
    //唱片机旋转一圈耗时
    private int mRotationDurtion = MusicConstants.BOX_REVOLVE_MINUTE;
    private ObjectAnimator mDiscObjectAnimator;
    private ImageView mDiseCover;

    public MusicJukeBoxCoverPager(Context context) {
        this(context,null);
    }

    public MusicJukeBoxCoverPager(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicJukeBoxCoverPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.music_view_cover_pager,this);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicJukeBoxCoverPager);
            mRotationDurtion = typedArray.getInteger(R.styleable.MusicJukeBoxCoverPager_musicJukeRotationDurtion, MusicConstants.BOX_REVOLVE_MINUTE);
            typedArray.recycle();
        }
        mDiseCover = (ImageView) findViewById(R.id.view_dise_cover);
    }

    /**
     * 设置封面
     * @param drawable
     */
    public void setMusicCover(Drawable drawable){
        if(null!=drawable){
            BitmapDrawable bitmapDrawable= (BitmapDrawable) drawable;
            MusicUtils.getInstance().setMusicComposeFront(getContext(),mDiseCover,bitmapDrawable.getBitmap(),MusicConstants.SCALE_DISC_SIZE
                    ,MusicConstants.SCALE_MUSIC_PIC_SIZE,R.drawable.ic_music_disc,R.drawable.ic_music_juke_default_cover);
        }
    }

    /**
     * 设置封面
     * @param bitmap
     */
    public void setMusicCover(Bitmap bitmap){
        MusicUtils.getInstance().setMusicComposeFront(getContext(),mDiseCover,bitmap,MusicConstants.SCALE_DISC_SIZE
                ,MusicConstants.SCALE_MUSIC_PIC_SIZE,R.drawable.ic_music_disc,R.drawable.ic_music_juke_default_cover);
    }

    /**
     * 设置封面
     * @param filePath http 或者 file://
     */
    public void setMusicCover(String filePath){
        Logger.d(TAG,"setMusicCover-->filePath:"+filePath);
        MusicUtils.getInstance().setMusicComposeFront(getContext(),mDiseCover,filePath,MusicConstants.SCALE_DISC_SIZE
                ,MusicConstants.SCALE_MUSIC_PIC_SIZE,R.drawable.ic_music_disc,R.drawable.ic_music_juke_default_cover);
    }

    /**
     * 设置封面
     * @param drawable
     */
    private void setImageDrawable(Drawable drawable) {
        if(null==drawable||null==mDiseCover) return;
        mDiseCover.setImageDrawable(drawable);
    }

    /**
     * 设置唱片机旋转一圈耗时
     * @param rotationDurtion
     */
    public void setRotationDurtion(int rotationDurtion) {
        this.mRotationDurtion = rotationDurtion;
    }

    /**
     * 创建一个旋转动画实体
     * @return
     */
    private ObjectAnimator getDiscObjectAnimator() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mDiseCover, View.ROTATION, 0, 360);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(mRotationDurtion * 1000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        this.mDiscObjectAnimator=objectAnimator;
        return objectAnimator;
    }

    /**
     * 控制器透明度
     * @param alpha
     */
    public void setConntrollerAlpha(float alpha) {

    }

    public ObjectAnimator getObjectAnimator() {
        if(null==mDiscObjectAnimator){
            return getDiscObjectAnimator();
        }
        return mDiscObjectAnimator;
    }

    public void onStart() {
        if(null==mDiscObjectAnimator){
            mDiscObjectAnimator = getDiscObjectAnimator();
        }
        mDiscObjectAnimator.start();
    }

    public void onStop() {
        if(null!=mDiscObjectAnimator){
            mDiscObjectAnimator.cancel();
            mDiscObjectAnimator=null;
        }
        if(null!=mDiseCover) mDiseCover.setRotation(0);
    }

    public void onReset() {
        Logger.d(TAG,"onReset");
        onStop();
    }

    public void onDestroy(){
        if(null!=mDiseCover) mDiseCover.setImageResource(0);
    }
}