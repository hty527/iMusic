package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.music.player.lib.R;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.iinterface.MusicLrcRowParser;
import com.music.player.lib.listener.MusicAnimatorListener;
import com.music.player.lib.listener.MusicJukeBoxStatusListener;
import com.music.player.lib.listener.MusicLrcViewListener;
import com.music.player.lib.manager.MusicPlayerManager;
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
 * 示例唱片机，负责UI的展示和播放、暂停系列通话，次组件不直接参与下一首、上一首的业务逻辑
 * 内部并持有歌词显示控件
 * 将此类事件一律抛出至持有此组件的活动
 * @2019-05-30: 对唱片机优化，使用普通的图片加载模式，避免内存占用高等问题，其功能MusicJukeBoxView保持一致
 */

public class MusicJukeBoxView2 extends RelativeLayout{

    private static final String TAG = "MusicJukeBoxView2";
    private ImageView mHandImage;
    private MusicViewPager mViewPager;
    private ViewPagerAdapter mPagerAdapter;
    //当前手指松手后的位置
    private int mOffsetPosition=0;
    private MusicJukeBoxStatusListener mPlayerInfoListener;
    private ObjectAnimator mHandAnimator;
    private Map<Integer, MusicJukeBoxCoverPager2> mFragments =new HashMap<>();//存放封面片段的集合
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
    //歌词控件
    private MusicLrcView mMusicLrcView;
    //歌词控件容器
    private FrameLayout mLrcLayout;
    //唱片容器
    private View mDiscRoot;

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

    public MusicJukeBoxView2(Context context) {
        this(context, null);
    }

    public MusicJukeBoxView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicJukeBoxView2(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mLrcLayout = (FrameLayout) findViewById(R.id.msuic_lrc_view);
    }

    private void removeViewByGroupVoew(View view) {
        if(null!=view&&null!=view.getParent()){
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }
    }

    /**
     * 初始化唱片背景
     */
    private void initDiscBlackground() {
        ImageView mDiscBlackground = (ImageView) findViewById(R.id.view_disc_backgound);
        int discSize = (int) (mScreenWidth * MusicConstants.SCALE_DISC_BG_SIZE);
        int marginTop = (int) (MusicConstants.SCALE_DISC_BG_MARGIN_TOP * mScreenWidth);
        LayoutParams layoutParams = (LayoutParams) mDiscBlackground.getLayoutParams();
        layoutParams.width=discSize;
        layoutParams.height=discSize;
        layoutParams.setMargins(0, marginTop, 0, 0);
        mDiscBlackground.setLayoutParams(layoutParams);
        mDiscBlackground.setImageResource(R.drawable.ic_music_disc_blackground);
    }

    /**
     * 初始化横向滑动Pager,在源代码中在这里是将ViewPager的整体高度下移，此处新代码做法是不下移ViewPager,
     * 下移子View中的专辑封面,避免单击事件无效问题
     */
    private void initViewPager() {
        mDiscRoot = findViewById(R.id.music_disc_root);
        mViewPager = (MusicViewPager) findViewById(R.id.view_disc_viewpager);
        mViewPager.setOnClickViewListener(new MusicViewPager.OnClickViewListener() {
            @Override
            public void onClick(View view) {
                if(null!=mPlayerInfoListener){
                    mPlayerInfoListener.onClickJukeBox(view);
                }
            }
        });
        //禁止滑动阴影
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mPagerAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
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
                        MusicJukeBoxView2.this.postDelayed(new Runnable() {
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

    private MusicViewPager.OnPageChangeListener onPageChangedListener=new MusicViewPager.OnPageChangeListener() {

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
            try {
                if(null!=mFragments){
                    mFragments.get(mOffsetPosition).onReset();
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }finally {
                if(null!=mMusicLrcView){
                    mMusicLrcView.onReset();
                    //只有当歌词已经在显示了，尝试隐藏歌词控件
                    if(null!=mMusicLrcView.getParent()){
                        showLrcView(false);
                    }
                    //还原唱片机状态
                    removeViewByGroupVoew(mMusicLrcView);
                }
                mOffsetPosition=position;
                //静止了，处理界面和播放，有效避免顿挫感现象
                if(null!=mPlayerInfoListener&&null!=mMusicDatas&&mMusicDatas.size()>position){
                    if(mEchoPageSelectedEnable){
                        mEchoPageSelectedEnable=false;
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
            switch (state) {
                //用户松手了
                case MusicViewPager.SCROLL_STATE_SETTLING:

                    break;
                //动画静止
                case MusicViewPager.SCROLL_STATE_IDLE:
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
                case MusicViewPager.SCROLL_STATE_DRAGGING:
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
                notifyMusicStatusChanged(MusicConstants.JUKE_BOX_PLAY);
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
                notifyMusicStatusChanged(MusicConstants.JUKE_BOX_STOP);
            }else if (mDiscStatus == DiscStatus.PAUSE) {
                notifyMusicStatusChanged(MusicConstants.JUKE_BOX_PAUSE);
            }
        }
    }

    /**
     * 播放唱盘动画
     * @param position
     */
    private void playDiscAnimator(int position) {
        if(null!= mFragments && mFragments.size()>0){
            MusicJukeBoxCoverPager2 musicJukeBoxCoverPager = mFragments.get(position);
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
        if(null!= mFragments && mFragments.size()>0){
            MusicJukeBoxCoverPager2 musicJukeBoxCoverPager = mFragments.get(position);
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
    public void notifyMusicStatusChanged(int status) {
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

    //=============================================歌词相关==========================================

    /**
     * 歌词控件显示与否
     * @param showlrc true:显示
     */
    private void showLrcView(boolean showlrc) {
        if(null!=mMusicLrcView){
            mMusicLrcView.setEnable(showlrc);
        }
        if(null!=mDiscRoot){
            ObjectAnimator animator;
            if(showlrc){
                //显示歌词，隐藏唱片机
                animator = ObjectAnimator.ofFloat(mDiscRoot, "alpha", 1.0f, 0.0f);
            }else{
                //隐藏歌词，显示唱片机
                animator = ObjectAnimator.ofFloat(mDiscRoot, "alpha", 0.0f, 1.0f);
            }
            animator.setDuration(200);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
        }
    }

    /**
     * 设置歌词,已经准备好的歌词源文本请调用此方法
     * @param audioID 音频ID，如果传入，启用歌词内部缓存
     * @param lrcContent 歌词源文本内容
     */
    public void setLrcRows(String audioID,String lrcContent) {
        if(null==mMusicLrcView){
            mMusicLrcView =new MusicLrcView(getContext());
            mMusicLrcView.setLrcViewListener(new MusicLrcViewListener() {
                @Override
                public void onLrcSeeked(int position, MusicLrcRow lrcRow) {
                    Logger.d(TAG,"onLrcSeeked-->position:"+position+",lrcRow:"+lrcRow.toString());
                    if(null!=mPlayerInfoListener){
                        mPlayerInfoListener.onLrcSeek(lrcRow);
                    }
                }

                @Override
                public void onClick(View view) {
                    Logger.d(TAG,"onClick-->");
                    removeViewByGroupVoew(mMusicLrcView);
                    showLrcView(false);
                }
            });
        }
        mMusicLrcView.setLrcRow(audioID,lrcContent);
    }

    /**
     * 酷狗专用-设置异步的网络歌词
     * @param audioID 音频ID，如果传入，启用歌词内部缓存
     * @param hashKey 酷狗音乐的文件唯一标识
     * @param lrcRowParser 自己实现的歌词解析器
     */
    public void setLrcRows(String audioID, String hashKey,MusicLrcRowParser lrcRowParser) {
        if(null==lrcRowParser){
            return;
        }
        if(null==mMusicLrcView){
            mMusicLrcView =new MusicLrcView(getContext());
            mMusicLrcView.setLrcViewListener(new MusicLrcViewListener() {
                @Override
                public void onLrcSeeked(int position, MusicLrcRow lrcRow) {
                    Logger.d(TAG,"onLrcSeeked-->position:"+position+",lrcRow:"+lrcRow.toString());
                    if(null!=mPlayerInfoListener){
                        mPlayerInfoListener.onLrcSeek(lrcRow);
                    }
                }

                @Override
                public void onClick(View view) {
                    Logger.d(TAG,"onClick-->");
                    removeViewByGroupVoew(mMusicLrcView);
                    showLrcView(false);
                }
            });
        }
        removeViewByGroupVoew(mMusicLrcView);
        showLrcView(true);
        mLrcLayout.addView(mMusicLrcView,new FrameLayout.LayoutParams(-1,-1));
        mMusicLrcView.setNetLrcRow(lrcRowParser,audioID,hashKey,"歌词获取中...");
    }

    /**
     * 更新歌词显示位置
     * @param currentDurtion 正在播放的位置，毫秒
     */
    public void updateLrcPosition(long currentDurtion) {
        if(null!=mMusicLrcView){
            mMusicLrcView.seekTo(currentDurtion);
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
     * Pager Adapter
     */
    private class ViewPagerAdapter extends MusicPagerAdapter {

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
            return MusicPagerAdapter.POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BaseAudioInfo musicInfo = (BaseAudioInfo) mMusicDatas.get(position);
            MusicJukeBoxCoverPager2 coverPager=new MusicJukeBoxCoverPager2(container.getContext());
            coverPager.setMusicCover(MusicUtils.getInstance().getMusicFrontPath(musicInfo));
            coverPager.setId(position);
            mFragments.put(position,coverPager);
            container.addView(coverPager);
            return coverPager;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            if(null!=object && object instanceof MusicJukeBoxCoverPager){
                MusicJukeBoxCoverPager coverPager= (MusicJukeBoxCoverPager) object;
                coverPager.onDestroy();
            }else if(null!=object && object instanceof MusicJukeBoxCoverPager2){
                MusicJukeBoxCoverPager2 coverPager= (MusicJukeBoxCoverPager2) object;
                coverPager.onDestroy();
            }
            container.removeView(((View) object));
        }
    }

    /**
     * 唱片机内部是否有返回事件需要自行处理
     * @return true：唱片机内部有自己的返回事件需要处理 ，false：没有事件处理
     */
    public boolean isBackPressed() {
        //卸装歌词控件
        if(null!=mMusicLrcView&&null!=mMusicLrcView.getParent()){
            removeViewByGroupVoew(mMusicLrcView);
            showLrcView(false);
            return false;
        }
        return true;
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
        needleAnimatorStatus= NeedleAnimatorStatus.IN_FAR_END;mScreenWidth=0;
        mPlayerInfoListener=null;mAnimatorListener=null;
        if(null!=mMusicLrcView){
            mMusicLrcView.onDestroy();
            mMusicLrcView=null;
        }
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