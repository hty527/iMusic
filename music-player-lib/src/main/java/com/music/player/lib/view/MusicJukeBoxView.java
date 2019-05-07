package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.music.player.lib.R;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicAnimatorListener;
import com.music.player.lib.listener.MusicJukeBoxStatusListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.model.MusicPlayerStatus;
import com.music.player.lib.model.MusicWindowStyle;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TinyHung@Outlook.com
 * 2019/3/6
 * BIG JukeBox
 * 示例唱片机，负责UI的展示和播放、暂停系列通话，次组件不直接参与下一首、上一首的业务逻辑，将此类事件一律抛出至持有此组件的活动
 */

public class MusicJukeBoxView extends RelativeLayout{

    private static final String TAG = "MusicJukeBoxView";
    private ImageView mHandImage;
    private ViewPager mViewPager;
    private ViewPagerAdapter mPagerAdapter;
    //当前手指松手后的位置
    private int mOffsetPosition=0;
    private MusicJukeBoxStatusListener mPlayerInfoListener;
    private ObjectAnimator mHandAnimator;
    private Map<Integer, MusicJukeBoxCoverPager> mFragments =new HashMap<>();//存放封面片段的集合
    private List<Object> mMusicDatas = new ArrayList<>();//音频数据集
    //标记ViewPager是否处于偏移的状态
    private boolean mViewPagerIsOffset = false;
    //标记唱针复位后，是否需要重新偏移到唱片处
    private boolean mIsNeed2StartPlayAnimator = false;
    private DiscStatus mDiscStatus = DiscStatus.STOP;
    public static final int DURATION_NEEDLE_ANIAMTOR = 500;
    private NeedleAnimatorStatus needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
    private int mScreenWidth;
    private MusicViewPagerScroller mScroller;//缓慢滚动
    private MusicAnimatorListener mAnimatorListener;
    //此标记记录ViewPager调用setCurrentItem()后是否禁用回调onPageSelected(int pisotion);方法
    private boolean mEchoPageSelectedEnable;

    /**
     * 唱针当前所处的状态
     */
    private enum NeedleAnimatorStatus {
        //移动时：从唱盘往远处移动
        TO_FAR_END,
        //移动时：从远处往唱盘移动
        TO_NEAR_END,
        //静止时：离开唱盘
        IN_FAR_END,
        //静止时：贴近唱盘
        IN_NEAR_END
    }

    /**
     * 胶盘状态
     */
    public enum DiscStatus {
        PLAY, PAUSE, STOP
    }

    public MusicJukeBoxView(Context context) {
        this(context, null);
    }

    public MusicJukeBoxView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicJukeBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initDiscBlackground();
        initViewPager();
        initHandView();
        initObjectAnimator();
    }

    /**
     * 初始化唱片背景
     */
    private void initDiscBlackground() {
        ImageView mDiscBlackground = (ImageView) findViewById(R.id.view_disc_backgound);
        mDiscBlackground.setImageDrawable(getDiscBlackgroundDrawable());
        int marginTop = (int) (MusicConstants.SCALE_DISC_MARGIN_TOP * mScreenWidth);
        //Demo垃圾桶样式，背景透明圆盘可见
        if(MusicPlayerManager.getInstance().getWindownStyle().equals(MusicWindowStyle.TRASH)){
            marginTop = (int) (MusicConstants.SCALE_DISC_BG_MARGIN_TOP * mScreenWidth);
        }
        LayoutParams layoutParams = (LayoutParams) mDiscBlackground.getLayoutParams();
        layoutParams.setMargins(0, marginTop, 0, 0);
        mDiscBlackground.setLayoutParams(layoutParams);
    }

    /**
     * 初始化横向滑动Pager
     */
    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.view_disc_viewpager);
        //禁止滑动阴影
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mPagerAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        LayoutParams layoutParams = (LayoutParams) mViewPager.getLayoutParams();
        int marginTop = (int) (MusicConstants.SCALE_DISC_MARGIN_TOP * mScreenWidth);
        layoutParams.setMargins(0, marginTop, 0, 0);
        mViewPager.setLayoutParams(layoutParams);
        mViewPager.setOffscreenPageLimit(1);
        mScroller = new MusicViewPagerScroller(getContext());
        mScroller.initViewPagerScroll(mViewPager);
    }

    /**
     * 初始化指针
     */
    private void initHandView() {
        mHandImage = (ImageView) findViewById(R.id.view_disc_hand);
        int needleWidth = (int) (MusicConstants.SCALE_NEEDLE_WIDTH * mScreenWidth);
        int needleHeight = (int) (MusicConstants.SCALE_NEEDLE_HEIGHT * mScreenWidth);
        //设置手柄的外边距为负数，让其隐藏一部分
        int marginTop = (int) (MusicConstants.SCALE_NEEDLE_MARGIN_TOP * mScreenWidth) * -1;
        int marginLeft = (int) (MusicConstants.SCALE_NEEDLE_MARGIN_LEFT * mScreenWidth);
        Bitmap originBitmap = BitmapFactory.decodeResource(getResources(), R.drawable
                .ic_music_cover_hand);
        Bitmap bitmap = Bitmap.createScaledBitmap(originBitmap, needleWidth, needleHeight, true);
        LayoutParams layoutParams = (LayoutParams) mHandImage.getLayoutParams();
        layoutParams.setMargins(marginLeft, marginTop, 0, 0);
        int pivotX = (int) (MusicConstants.SCALE_NEEDLE_PIVOT_X * mScreenWidth);
        int pivotY = (int) (MusicConstants.SCALE_NEEDLE_PIVOT_Y * mScreenWidth);
        mHandImage.setPivotX(pivotX);
        mHandImage.setPivotY(pivotY);
        mHandImage.setRotation(MusicConstants.ROTATION_INIT_NEEDLE);
        mHandImage.setImageBitmap(bitmap);
        mHandImage.setLayoutParams(layoutParams);
    }

    /**
     * 指针动画
     */
    private void initObjectAnimator() {
        mHandAnimator = ObjectAnimator.ofFloat(mHandImage, View.ROTATION, MusicConstants.ROTATION_INIT_NEEDLE, 0);
        mHandAnimator.setDuration(DURATION_NEEDLE_ANIAMTOR);
        mHandAnimator.setInterpolator(new AccelerateInterpolator());
        mHandAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //根据动画开始前NeedleAnimatorStatus的状态，即可得出动画进行时NeedleAnimatorStatus的状态
                if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_NEAR_END;
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_NEAR_END;
                    playDiscAnimator(mViewPager.getCurrentItem());
                    mDiscStatus = DiscStatus.PLAY;
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
                    if (mDiscStatus == DiscStatus.STOP) {
                        mIsNeed2StartPlayAnimator = true;
                    }
                }
                if (mIsNeed2StartPlayAnimator) {
                    mIsNeed2StartPlayAnimator = false;
                    //只有在ViewPager不处于偏移状态时，才开始唱盘旋转动画
                    if (!mViewPagerIsOffset&&MusicPlayerManager.getInstance().isPlaying()) {
                        Logger.d(TAG,"onAnimationEnd--绞盘开始动画");
                        MusicJukeBoxView.this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                playAnimator();
                            }
                        }, 50);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }

    /**
     * 绑定数据
     * @param musicDataList
     */
    public void setNewData(List<?> musicDataList, int position) {
        if (musicDataList.isEmpty()) return;
        if(null!=mPagerAdapter){
            if(null==mMusicDatas){
                mMusicDatas =new ArrayList<>();
            }
            mMusicDatas.clear();
            mMusicDatas.addAll(musicDataList);
            mPagerAdapter.notifyDataSetChanged();
            if(mPagerAdapter.getCount()>position){
                setCurrentMusicItem(position,false,false);
                this.mOffsetPosition=mViewPager.getCurrentItem();
                if(null!=mPlayerInfoListener) mPlayerInfoListener.onJukeBoxObjectChanged(position,(BaseAudioInfo) mMusicDatas.get(position),true);
            }
            mViewPager.addOnPageChangeListener(onPageChangedListener);
        }
    }

    private ViewPager.OnPageChangeListener onPageChangedListener=new ViewPager.OnPageChangeListener() {

        int mScrollOffsetX = 0;
        /**
         * @param position Position index of the first page currently being displayed. Page 起始位置索引
         *            position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset Value from [0, 1) indicating the offset from the page at position. 正在移动的偏移量 [0, 1]
         * @param positionOffsetPixels Value in pixels indicating the offset from position. 起始位置像素偏移量，这里是Y轴  ++ 上滑  --下滑
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //左滑
            if (mScrollOffsetX > positionOffsetPixels) {
                if (positionOffset < 0.5) {
                    postOffsetMusicInfo(position);
                } else {
                    postOffsetMusicInfo(mViewPager.getCurrentItem());
                }
            }
            //右滑
            else if (mScrollOffsetX < positionOffsetPixels) {
                if (positionOffset > 0.5) {
                    postOffsetMusicInfo(position + 1);
                } else {
                    postOffsetMusicInfo(position);
                }
            }
            mScrollOffsetX = positionOffsetPixels;
        }
        @Override
        public void onPageSelected(int position) {
            Logger.d(TAG,"onPageSelected:"+position+",mOffsetPosition:"+mOffsetPosition);
            try {
                if(null!=mFragments){
                    mFragments.get(mOffsetPosition).onReset();
                }
            }catch (RuntimeException e){

            }finally {
                mOffsetPosition=position;
                //静止了，处理界面和播放，有效避免顿挫感现象
                if(null!=mPlayerInfoListener&&null!=mMusicDatas&&mMusicDatas.size()>position){
                    if(mEchoPageSelectedEnable){
                        mEchoPageSelectedEnable=false;
                        Logger.d(TAG,"onPageSelected-->onPageSelected(index)遭到禁用，只处理回显");
                        mPlayerInfoListener.onJukeBoxObjectChanged(position,(BaseAudioInfo)
                                mMusicDatas.get(position),true);
                    }else{
                        mPlayerInfoListener.onJukeBoxObjectChanged(position,(BaseAudioInfo)
                                mMusicDatas.get(position),false);
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Logger.d(TAG,"onPageScrollStateChanged--state:"+state+",mOffsetPosition:"+mOffsetPosition);
            switch (state) {
                //用户松手了
                case ViewPager.SCROLL_STATE_SETTLING:

                    break;
                //动画静止
                case ViewPager.SCROLL_STATE_IDLE:
                    mViewPagerIsOffset = false;
                    //松手后还是停留在此Pager
                    if(null!=mViewPager){
                        if(mOffsetPosition==mViewPager.getCurrentItem()){
                            //松手时若播放器内部正在播放，恢复动画
                            if(MusicPlayerManager.getInstance().isPlaying()){
                                playAnimator();
                            }
                        }else{
                            //切换了Pager，一律开始播放动作
                            playAnimator();
                        }
                    }
                    break;
                //用户开始滚动Pager
                case ViewPager.SCROLL_STATE_DRAGGING:
                    mViewPagerIsOffset = true;
                    if(null!=mScroller) {
                        mScroller.setScroller(true);
                    }
                    pauseAnimator();
                    break;
            }
        }
    };

    /**
     * 播放唱针动画
     */
    private synchronized void playAnimator() {
        if(null!=mHandAnimator){
            //唱针处于远端时，直接播放动画
            if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
                notifyMusicStatusChanged(MusicPlayerStatus.PLAY);
                mHandAnimator.start();
            }else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
                ////唱针处于往远端移动时，设置标记，等动画结束后再播放动画
                mIsNeed2StartPlayAnimator = true;
            }else if(needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END){
                if(null!=mViewPager){
                    playDiscAnimator(mViewPager.getCurrentItem());
                    mDiscStatus = DiscStatus.PLAY;
                }
            }
        }
    }

    /**
     * 暂停唱针动画
     */
    private void pauseAnimator() {
        Logger.d(TAG,"pauseAnimator:"+needleAnimatorStatus);
        if(null!=mHandAnimator){
            //播放时暂停动画
            if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
                if(null!=mViewPager){
                    int index = mViewPager.getCurrentItem();
                    pauseDiscAnimatior(index);
                }
            } else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
                //唱针往唱盘移动时暂停动画
                mHandAnimator.reverse();
                //若动画在没结束时执行reverse方法，则不会执行监听器的onStart方法，此时需要手动设置
                needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
            }
            //动画可能执行多次，只有音乐处于停止 / 暂停状态时，才执行暂停命令
            if (mDiscStatus == DiscStatus.STOP) {
                notifyMusicStatusChanged(MusicPlayerStatus.STOP);
            }else if (mDiscStatus == DiscStatus.PAUSE) {
                notifyMusicStatusChanged(MusicPlayerStatus.PAUSE);
            }
        }
    }

    /**
     * 播放唱盘动画
     * @param position
     */
    private void playDiscAnimator(int position) {
        Logger.d(TAG,"playDiscAnimator--POSITION:"+position);
        if(null!= mFragments && mFragments.size()>0){
            MusicJukeBoxCoverPager musicJukeBoxCoverPager = mFragments.get(position);
            if(null!=musicJukeBoxCoverPager){
                ObjectAnimator animator=musicJukeBoxCoverPager.getObjectAnimator();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (animator.isPaused()) {
                        animator.resume();
                    } else {
                        if(animator.isRunning()){
                            return;
                        }
                        animator.start();
                    }
                }else{
                    animator.start();
                }
                if(null!=mAnimatorListener){
                    mAnimatorListener.onAnimationEnd();
                }
            }
        }
    }

    /**
     * 暂停唱盘动画
     * @param position
     */
    private void pauseDiscAnimatior(int position) {
        Logger.d(TAG,"playDiscAnimator:"+position);
        if(null!= mFragments && mFragments.size()>0){
            MusicJukeBoxCoverPager musicJukeBoxCoverPager = mFragments.get(position);
            if(null!=musicJukeBoxCoverPager){
                ObjectAnimator animator=musicJukeBoxCoverPager.getObjectAnimator();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    animator.pause();
                }
            }
        }
        if(null!=mHandAnimator) mHandAnimator.reverse();
    }

    /**
     * 通知数据源发生了改变s
     * @param position
     */
    public void postOffsetMusicInfo(int position) {
        if(null!=mPlayerInfoListener&&null!=mMusicDatas&&mMusicDatas.size()>position){
            mPlayerInfoListener.onJukeBoxOffsetObject((BaseAudioInfo) mMusicDatas.get(position));
        }
    }

    /**
     * 播放器状态
     * @param status
     */
    public void notifyMusicStatusChanged(MusicPlayerStatus status) {
        if(null!=mPlayerInfoListener&&null!=mMusicDatas&&mMusicDatas.size()>0){
            mPlayerInfoListener.onJukeBoxState(status);
        }
    }

    /**
     * 定位当当前正在播放的位置
     */
    public void setCurrentMusicItem(int position) {
        setCurrentMusicItem(position,false,false);
    }

    /**
     * 定位至指定位置
     */
    public void setCurrentMusicItem(int position,boolean smoothScroll,boolean isPlayAnimator) {
        setCurrentMusicItem(position,smoothScroll,isPlayAnimator,false);
    }

    /**
     * 定位至指定位置
     * @param position 索引位置
     * @param smoothScroll 是否动画过渡到指定索引位置
     * @param isPlayAnimator 是否直接开始指针、唱片机动画
     * @param echoPageSelectedEnable 是否禁用回调onPageSelected(int position);方法
     */
    public void setCurrentMusicItem(int position,boolean smoothScroll,boolean isPlayAnimator,boolean
            echoPageSelectedEnable) {
        Logger.d(TAG,"setCurrentMusicItem:CurrentPosition"+mViewPager.getCurrentItem()
                +",position:"+position+",smoothScroll:"+smoothScroll+",isPlayAnimator:"
                +isPlayAnimator+",isEchoCurrentIndex:"+echoPageSelectedEnable);
        if(null!=mScroller) {
            mScroller.setScroller(smoothScroll);//不需要缓慢滚动落地
        }
        if(null!=mViewPager){
            this.mEchoPageSelectedEnable =echoPageSelectedEnable;
            mViewPager.setCurrentItem(position,smoothScroll);
        }
        if(isPlayAnimator){
            needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
            onStart();
        }
    }

    /**
     * 开始唱片机指针动画，指针动画结束后紧接着唱片机旋转动画
     */
    public void onStart() {
        mDiscStatus = DiscStatus.PLAY;
        playAnimator();
    }

    /**
     * 开始唱片机指针动画，指针动画结束后紧接着唱片机旋转动画
     * @param animatorListener 对指针的动画状态监听
     */
    public void onStart(MusicAnimatorListener animatorListener) {
        mDiscStatus = DiscStatus.PLAY;
        this.mAnimatorListener=animatorListener;
        playAnimator();
    }

    /**
     * 暂停
     */
    public void onPause() {
        mDiscStatus = DiscStatus.PAUSE;
        pauseAnimator();
    }

    /**
     * 停止
     */
    public void onStop() {
        mDiscStatus = DiscStatus.STOP;
        pauseAnimator();
    }

    public void resetAnimationListener() {
        mAnimatorListener=null;
    }

    /**
     * 唱片是否正在播放状态
     * @return 为true正在播放动画
     */
    public boolean isPlaying() {
        return mDiscStatus == DiscStatus.PLAY;
    }

    /**
     * 放回当前正在显示的INDEX
     * @return 正在处理的位置
     */
    public int getCurrentItem() {
        return null==mViewPager?0:mViewPager.getCurrentItem();
    }

    /**
     * 放回当前正在显示Media对象
     * @return 唱片机正在处理的对象
     */
    public BaseAudioInfo getCurrentMedia() {
        if(null!=mViewPager){
            if(null!=mMusicDatas&&mMusicDatas.size()>mViewPager.getCurrentItem()){
                return (BaseAudioInfo) mMusicDatas.get(mViewPager.getCurrentItem());
            }
        }
        return null;
    }

    /**
     * 监听器设定
     * @param playerInfoListener
     */
    public void setPlayerInfoListener(MusicJukeBoxStatusListener playerInfoListener) {
        mPlayerInfoListener = playerInfoListener;
    }

    /**
     * 得到唱盘背后半透明的圆形背景
     * @return 图形
     */
    private Drawable getDiscBlackgroundDrawable() {
        int discSize = (int) (mScreenWidth * MusicConstants.SCALE_DISC_SIZE);
        //Demo垃圾桶样式，背景透明圆盘可见
        if(MusicPlayerManager.getInstance().getWindownStyle().equals(MusicWindowStyle.TRASH)){
            discSize = (int) (mScreenWidth * MusicConstants.SCALE_DISC_BG_SIZE);
        }
        Bitmap bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R
                .drawable.ic_music_disc_blackground), discSize, discSize, true);
        RoundedBitmapDrawable roundDiscDrawable = RoundedBitmapDrawableFactory.create
                (getResources(), bitmapDisc);
        return roundDiscDrawable;
    }

    /**
     * Pager Adapter
     */
    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return null==mMusicDatas?0:mMusicDatas.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BaseAudioInfo musicInfo = (BaseAudioInfo) mMusicDatas.get(position);
            MusicJukeBoxCoverPager coverPager=new MusicJukeBoxCoverPager(container.getContext());
            coverPager.setMusicCover(MusicUtils.getInstance().getMusicFrontPath(musicInfo));
            coverPager.setId(position);
            mFragments.put(position,coverPager);
            container.addView(coverPager);
            return coverPager;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if(null!=object && object instanceof MusicJukeBoxCoverPager){
                MusicJukeBoxCoverPager coverPager= (MusicJukeBoxCoverPager) object;
                coverPager.onDestroy();
                mFragments.remove(position);
            }
            container.removeView(((View) object));
        }
    }

    /**
     * 销毁
     */
    public void onDestroy() {
        onStop();
        if(null!=mHandAnimator){
            mHandAnimator.cancel();
            mHandAnimator=null;
        }
        mViewPagerIsOffset=false;mIsNeed2StartPlayAnimator=false;mEchoPageSelectedEnable=false;
        mDiscStatus = DiscStatus.STOP;
        needleAnimatorStatus=NeedleAnimatorStatus.IN_FAR_END;mScreenWidth=0;
        mPlayerInfoListener=null;mAnimatorListener=null;
        if(null!=mViewPager){
            mViewPager.removeOnPageChangeListener(onPageChangedListener);
            mViewPager.removeAllViews();
            mViewPager=null;
            onPageChangedListener=null;
        }
        if(null!=mMusicDatas){
            mMusicDatas.clear();
            mMusicDatas=null;
        }
        if(null!=mFragments){
            mFragments.clear();
            mFragments=null;
        }
    }
}