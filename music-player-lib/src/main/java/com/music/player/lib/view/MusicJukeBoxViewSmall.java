package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.music.player.lib.R;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.model.MusicWindowStyle;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/6
 * Mini JukeBox
 */

public class MusicJukeBoxViewSmall extends FrameLayout implements Observer {

    private static final String TAG = "MusicJukeBoxViewSmall";
    private int mRotationDurtion=MusicConstants.BOX_MINI_REVOLVE_MINUTE;
    private final int mScreenWidth;
    private ImageView mViewCover;
    private boolean readyPlay=false;//是否准备要开始动画
    private MusicOnItemClickListener mListener;
    private ObjectAnimator mDiscObjectAnimator;
    private boolean isVisible=true;//是否允许悬浮窗显示

    public MusicJukeBoxViewSmall(@NonNull Context context) {
        this(context,null);
    }

    public MusicJukeBoxViewSmall(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.music_view_small_disc,this);
        mViewCover = (ImageView) findViewById(R.id.view_cover);
        ImageView cancelBtn = (ImageView) findViewById(R.id.view_close);
        MusicWindowStyle musicWindowStyle =MusicPlayerManager.getInstance().getWindownStyle();
        if(musicWindowStyle.equals(MusicWindowStyle.DEFAULT)){
            FrameLayout.LayoutParams layoutParams = (LayoutParams) mViewCover.getLayoutParams();
            int margin = MusicUtils.getInstance().dpToPxInt(context, 6f);
            layoutParams.setMargins(margin,margin,margin,margin);
            mViewCover.setLayoutParams(layoutParams);
            cancelBtn.setVisibility(VISIBLE);
        }
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicJukeBoxViewSmall);
            boolean enable = typedArray.getBoolean(R.styleable.MusicJukeBoxViewSmall_musicMiniJukeEnable, true);
            mRotationDurtion = typedArray.getInteger(R.styleable.MusicJukeBoxViewSmall_musicMiniJukeRotationDurtion, MusicConstants.BOX_MINI_REVOLVE_MINUTE);
            if(enable){
                mViewCover.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(null!=MusicJukeBoxViewSmall.this.getTag()){
                            long musicID= (long) MusicJukeBoxViewSmall.this.getTag();
                            if(null!=mListener) mListener.onItemClick(v, 0, musicID);
                        }
                    }
                });
            }
            typedArray.recycle();
        }
        mScreenWidth= MusicUtils.getInstance().getScreenWidth(getContext());
        MusicPlayerManager.getInstance().addObservable(this);
    }

    /**
     * 开始旋转动画
     */
    public synchronized void startAnimator(){
        if(null!=mDiscObjectAnimator){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(mDiscObjectAnimator.isPaused()){
                    mDiscObjectAnimator.resume();
                }else{
                    if(mDiscObjectAnimator.isRunning()){
                        return;
                    }
                    mDiscObjectAnimator.start();
                }
            }
        }else{
            ObjectAnimator discObjectAnimator = getDiscObjectAnimator();
            if(null!=discObjectAnimator){
                discObjectAnimator.start();
            }
        }
    }

    /**
     * 暂停旋转动画
     */
    private void pausAnimator() {
        if(null!=mDiscObjectAnimator){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mDiscObjectAnimator.pause();
            }else{
                mDiscObjectAnimator.cancel();
                mDiscObjectAnimator=null;
                if(null!=mViewCover){
                    mViewCover.clearAnimation();
                    mViewCover.setRotation(0);
                }
            }
        }
    }

    /**
     * 停止旋转动画
     */
    public void stopAnimator(boolean resetRotation){
        if(null!=mDiscObjectAnimator){
            mDiscObjectAnimator.cancel();
            mDiscObjectAnimator=null;
        }
        if(null!=mViewCover){
            mViewCover.clearAnimation();
            if(resetRotation){
                mViewCover.setRotation(0);
            }
        }
    }

    /**
     * 创建一个旋转动画实体
     * @return
     */
    private ObjectAnimator getDiscObjectAnimator() {
        if(null!=mViewCover){
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mViewCover, View.ROTATION, 0, 360);
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            objectAnimator.setDuration(mRotationDurtion * 1000);
            objectAnimator.setInterpolator(new LinearInterpolator());
            this.mDiscObjectAnimator=objectAnimator;
            return objectAnimator;
        }
        return null;
    }

    /**
     * 单击事件
     * @param view
     */
    public void onClick(View view) {
        if(null!=MusicJukeBoxViewSmall.this.getTag()){
            long musicID= (long) MusicJukeBoxViewSmall.this.getTag();
            if(null!=mListener) mListener.onItemClick(view, 0, musicID);
        }
    }

    /**
     * 刷新数据
     * @param musicStatus
     */
    public void updateData(final MusicStatus musicStatus){
        if(!TextUtils.isEmpty(musicStatus.getCover())&&null!=mViewCover){
            setMusicFront(musicStatus.getCover());
        }
        MusicJukeBoxViewSmall.this.post(new Runnable() {
            @Override
            public void run() {
                //ID更新
                if(musicStatus.getId()>0){
                    MusicJukeBoxViewSmall.this.setTag(musicStatus.getId());
                }
                //停止
                if(MusicStatus.PLAYER_STATUS_STOP==musicStatus.getPlayerStatus()){
                    Logger.d(TAG,"update，播放器停止");
                    readyPlay=false;
                    stopAnimator(true);
                    //暂停
                }else if(MusicStatus.PLAYER_STATUS_PAUSE==musicStatus.getPlayerStatus()){
                    Logger.d(TAG,"update，播放器暂停");
                    readyPlay=false;
                    pausAnimator();
                    //播放
                }else if(MusicStatus.PLAYER_STATUS_START==musicStatus.getPlayerStatus()||MusicStatus.PLAYER_STATUS_PREPARED==musicStatus.getPlayerStatus()){
                    Logger.d(TAG,"update，播放器开始");
                    readyPlay=true;
                    startAnimator();
                    //销毁
                }else if(MusicStatus.PLAYER_STATUS_DESTROY==musicStatus.getPlayerStatus()){
                    Logger.d(TAG,"update，播放器销毁");
                    readyPlay=false;
                    if(null!=mViewCover){
                        mViewCover.setImageResource(0);
                    }
                    stopAnimator(true);
                    //失败
                }else if(MusicStatus.PLAYER_STATUS_ERROR==musicStatus.getPlayerStatus()){
                    Logger.d(TAG,"update，播放器收到无效播放地址");
                    readyPlay=false;
                }
            }
        });
    }

    /**
     * 更新音频文件封面
     * @param filePath
     */
    private void setMusicFront(String filePath) {
        MusicUtils.getInstance().setMusicComposeFront(getContext(),mViewCover,filePath,MusicConstants.SCALE_DISC_MINI_SIZE
                ,MusicConstants.SCALE_MUSIC_PIC_MINE_SIZE,R.drawable.ic_music_disc_bg_mini,R.drawable.ic_music_juke_default_cover);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof MusicSubjectObservable && null!=arg && arg instanceof MusicStatus){
            MusicStatus musicStatus= (MusicStatus) arg;
            //过滤其它组件的特殊事件
            if(musicStatus.getPlayerStatus()>-2){
                updateData(musicStatus);
            }
        }
    }

    /**
     * 设置唱片机旋转一圈耗时
     * @param rotationDurtion
     */
    public void setRotationDurtion(int rotationDurtion) {
        mRotationDurtion = rotationDurtion;
    }

    /**
     * 将封面与唱片圆盘合成一张图片,封面位于底图之上
     * @param bitmap
     * @return
     */
    private Drawable getDiscDrawable(Bitmap bitmap) {
        if(mScreenWidth==0){
            return null;
        }
        int discSize = (int) (mScreenWidth * MusicConstants.SCALE_DISC_MINI_SIZE);
        int musicPicSize = (int) (mScreenWidth * MusicConstants.SCALE_MUSIC_PIC_MINE_SIZE);
        Bitmap bgBitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R
                .drawable.ic_music_disc_bg_mini), discSize, discSize, true);//去除锯齿
        Bitmap coverBitmap = getMusicPicBitmap(musicPicSize,bitmap);
        BitmapDrawable bgDiscDrawable = new BitmapDrawable(bgBitmapDisc);
        RoundedBitmapDrawable roundMusicDrawable = RoundedBitmapDrawableFactory.create(getResources(), coverBitmap);
        //抗锯齿
        bgDiscDrawable.setAntiAlias(true);
        roundMusicDrawable.setAntiAlias(true);
        Drawable[] drawables = new Drawable[2];
        drawables[0] = bgDiscDrawable;
        drawables[1] = roundMusicDrawable;
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        int musicPicMargin = (int) ((MusicConstants.SCALE_DISC_MINI_SIZE - MusicConstants
                .SCALE_MUSIC_PIC_MINE_SIZE) * mScreenWidth / 2);
        //调整专辑图片的四周边距，让其显示在正中
        layerDrawable.setLayerInset(1, musicPicMargin, musicPicMargin, musicPicMargin, musicPicMargin);
        return layerDrawable;
    }

    private Bitmap getMusicPicBitmap(int musicPicSize, Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imageWidth = bitmap.getWidth();
        int sample = imageWidth / musicPicSize;
        int dstSample = 1;
        if (sample > dstSample) {
            dstSample = sample;
        }
        options.inJustDecodeBounds = false;
        //设置图片采样率
        options.inSampleSize = dstSample;
        //设置图片解码格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return Bitmap.createScaledBitmap(bitmap, musicPicSize, musicPicSize, true);
    }

    public void setOnItemClickListener(MusicOnItemClickListener listener){
        this.mListener=listener;
    }

    public void onResume(){
        if(readyPlay){
            Logger.d(TAG,"onResume");
            startAnimator();
        }
    }

    public void onPause(){
        Logger.d(TAG,"onPause");
        stopAnimator(true);
    }

    public void onVisible() {
        Logger.d(TAG,"onVisible");
        isVisible=true;
        onResume();
    }

    public void onInvisible() {
        Logger.d(TAG,"onInvisible");
        isVisible=false;
        stopAnimator(false);
    }

    /**
     * 显示悬浮窗
     * @param view
     */
    public void showWindowAnimation(View view){
        if(null==view) return;
        if(view.getVisibility()==VISIBLE) return;
        view.clearAnimation();
        view.setVisibility(VISIBLE);
        AnimatorSet animatorSet=new AnimatorSet();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleX", 0.0f, 1.0f).setDuration(350);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 0.0f, 1.0f).setDuration(350);
        animatorSet.playTogether(animator1,animator2);
        animatorSet.start();
    }

    /**
     * 隐藏悬浮窗
     * @param view
     */
    public void hideWindowAnimation(final View view){
        if(null==view) return;
        if(view.getVisibility()==GONE) return;
        view.clearAnimation();
        AnimatorSet animatorSet=new AnimatorSet();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.0f).setDuration(260);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.0f).setDuration(260);
        animatorSet.playTogether(animator1,animator2);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(GONE);
            }
        });
        animatorSet.start();
    }

    public void onDestroy(){
        Logger.d(TAG,"onDestroy");
        readyPlay=false;
        isVisible=false;
        stopAnimator(true);
        if(null!=mViewCover){
            mViewCover.setImageResource(0);
            mViewCover=null;
        }
        mListener=null;mViewCover=null;
        MusicPlayerManager.getInstance().removeObserver(this);
    }
}