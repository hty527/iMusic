package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
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
 * 示例唱片机容器，负责同步播放器的UI状态，歌词交互等，封面显示：MusicJukeBoxCoverPager
 * 内部并持有歌词显示控件
 * 将此类事件一律抛出至持有此组件的活动
 */

public class MusicJukeBoxView extends RelativeLayout{

    private static final String TAG = "MusicJukeBoxView";
    private ImageView mHandImage;
    private MusicViewPager mViewPager;
    private ViewPagerAdapter mPagerAdapter;
    //当前手指松手后的位置
    private int mOffsetPosition=0;
    private MusicJukeBoxStatusListener mPlayerInfoListener;
    //唱针动画
    private ObjectAnimator mHandAnimator;
    private Map<Integer, MusicJukeBoxCoverPager> mFragments =new HashMap<>();//存放封面片段的集合
    private List<Object> mMusicDatas = new ArrayList<>();//音频数据集
    //标记ViewPager是否处于偏移的状态
    private boolean mViewPagerIsOffset = false;
    //唱片机内部状态
    private DiscStatus mDiscStatus = DiscStatus.STOP;
    //屏幕宽
    private int mScreenWidth;
    private MusicViewPagerScroller mScroller;//缓慢滚动
    //唱针动画展开、静止
    private float HANDLE_EXPAND =-30.0f,HANDLE_STATIC=0.0f;
    //唱片机指针动画状态监听
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
        //歌词容器
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
        //禁止滑动阻尼阴影
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mPagerAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mScroller = new MusicViewPagerScroller(getContext());
        //自定义滚动
        mScroller.initViewPagerScroll(mViewPager);
    }

    /**
     * 初始化指针
     * getRotation():0.0：禁止不动 -30：动画已经被展开
     */
    private void initHandView() {
        mHandImage = (ImageView) findViewById(R.id.view_disc_hand);
        int needleWidth = (int) (MusicConstants.SCALE_NEEDLE_WIDTH * mScreenWidth);
        int needleHeight = (int) (MusicConstants.SCALE_NEEDLE_HEIGHT * mScreenWidth);
        //设置手柄的外边距为负数，让其隐藏一部分
        int marginTop = (int) (MusicConstants.SCALE_NEEDLE_MARGIN_TOP * mScreenWidth) * -1;
        int marginLeft = (int) (MusicConstants.SCALE_NEEDLE_MARGIN_LEFT * mScreenWidth);
        //Bitmap originBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_cover_hand);
        //Bitmap bitmap = Bitmap.createScaledBitmap(originBitmap, needleWidth, needleHeight, true);
        LayoutParams layoutParams = (LayoutParams) mHandImage.getLayoutParams();
        layoutParams.setMargins(marginLeft, marginTop, 0, 0);
        layoutParams.width=needleWidth;
        layoutParams.height=needleHeight;
        int pivotX = (int) (MusicConstants.SCALE_NEEDLE_PIVOT_X * mScreenWidth);
        int pivotY = (int) (MusicConstants.SCALE_NEEDLE_PIVOT_Y * mScreenWidth);
        //设置旋转中心锚点坐标，View默认的锚点是View的中心X,Y
        mHandImage.setPivotX(pivotX);
        mHandImage.setPivotY(pivotY);
        mHandImage.setRotation(MusicConstants.ROTATION_INIT_NEEDLE);
        mHandImage.setLayoutParams(layoutParams);
        mHandImage.setImageResource( R.drawable.ic_music_cover_hand);
    }

    /**
     * 唱片机指针动画
     */
    private void initObjectAnimator() {
        mHandAnimator = ObjectAnimator.ofFloat(mHandImage, View.ROTATION, MusicConstants.ROTATION_INIT_NEEDLE, 0);
        mHandAnimator.setDuration(MusicConstants.DURATION_NEEDLE_ANIAMTOR);
        mHandAnimator.setInterpolator(new AccelerateInterpolator());
        mHandAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animator) {
                //指针动画播放完毕并且唱针处于附着胶盘状态（Rotation 为0：附着，大于0(-30)为与胶盘分离状态）
                // ，立即开始播放胶盘旋转动画
                if(mHandImage.getRotation()>=HANDLE_STATIC&&null!=mPagerAdapter&&mPagerAdapter.getCount()>0){
                    playDiscAnimator(mViewPager.getCurrentItem());
                }
            }

            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }

    /**
     * 播放唱针动画,此方法会在不同交互场景下短时间同时被调用多次，这里已经过滤了重复调用
     */
    private synchronized void playHandlerAnimator() {
        if(!mViewPagerIsOffset&&null!=mHandAnimator&&null!=mHandImage){
            //仅当完全静止不动并且动画没有开始执行时，开始播放指针动画
            if(mHandImage.getRotation()<HANDLE_STATIC){
                if(!mHandAnimator.isRunning()){
                    mHandAnimator.start();
                }
            }else{
                //直接开始胶盘旋转动画,避免太快显的不自然，歇一会儿再开始动画吧
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(null!=mPagerAdapter&&mPagerAdapter.getCount()>0){
                            playDiscAnimator(mViewPager.getCurrentItem());
                        }
                    }
                },100);
            }
        }
    }

    /**
     * 暂停唱针动画
     */
    private void pauseHandlerAnimator() {
        if(null!=mHandAnimator&&null!=mHandImage){
            //仅当唱针附着胶盘时，让唱针恢复至停止状态
            if(mHandImage.getRotation()!=HANDLE_EXPAND&&!mHandAnimator.isRunning()){
                mHandAnimator.reverse();
            }
            if(null!=mViewPager){
                int index = mViewPager.getCurrentItem();
                pauseDiscAnimatior(index);
            }
        }
    }

    /**
     * 播放唱盘动画
     * @param position Page的ID
     */
    private void playDiscAnimator(int position) {
        if(!mViewPagerIsOffset&&null!= mFragments && mFragments.size()>0){
            mDiscStatus = DiscStatus.PLAY;
            MusicJukeBoxCoverPager musicJukeBoxCoverPager = mFragments.get(position);
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
            notifyMusicStatusChanged(MusicConstants.JUKE_BOX_PLAY);
            if(null!=mAnimatorListener){
                mAnimatorListener.onAnimationEnd();
            }
        }
    }

    /**
     * 暂停唱盘动画
     * @param position Page的ID
     */
    private void pauseDiscAnimatior(int position) {
        if(null!= mFragments && mFragments.size()>0){
            MusicJukeBoxCoverPager musicJukeBoxCoverPager = mFragments.get(position);
            ObjectAnimator animator=musicJukeBoxCoverPager.getObjectAnimator();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.pause();
            }else{
                animator.cancel();
            }
            mDiscStatus = DiscStatus.PAUSE;
            notifyMusicStatusChanged(MusicConstants.JUKE_BOX_PAUSE);
        }
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
            Logger.d(TAG,"onPageScrollStateChanged-->state:"+state);
            switch (state) {
                //用户已经松手了
                case MusicViewPager.SCROLL_STATE_SETTLING:

                    break;
                //界面完全静止
                case MusicViewPager.SCROLL_STATE_IDLE:
                    mViewPagerIsOffset = false;
                    //松手后还是停留在此Pager
                    if(null!=mViewPager){
                        if(mOffsetPosition==mViewPager.getCurrentItem()){
                            //松手后还是停留在当前Pager,根据播放状态尝试恢复动画播放
                            if(MusicPlayerManager.getInstance().isPlaying()){
                                playHandlerAnimator();
                            }
                        }else{
                            //切换了Pager，一律开始播放动作
                            playHandlerAnimator();
                        }
                    }
                    break;
                //手势开始滑动PagerView
                case MusicViewPager.SCROLL_STATE_DRAGGING:
                    mViewPagerIsOffset = true;
                    //立即开启自动以Scroll动画
                    if(null!=mScroller) {
                        mScroller.setScroller(true);
                    }
                    //尝试暂停正在进行的所有动画
                    pauseHandlerAnimator();
                    break;
            }
        }
    };

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
                    if(null!=mPlayerInfoListener){
                        mPlayerInfoListener.onLrcSeek(lrcRow);
                    }
                }

                @Override
                public void onClick(View view) {
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
                    if(null!=mPlayerInfoListener){
                        mPlayerInfoListener.onLrcSeek(lrcRow);
                    }
                }

                @Override
                public void onClick(View view) {
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
        playHandlerAnimator();
    }

    /**
     * 开始唱片机指针动画，指针动画结束后紧接着唱片机旋转动画
     * @param animatorListener 对指针的动画状态监听
     */
    public void onStart(MusicAnimatorListener animatorListener) {
        this.mAnimatorListener=animatorListener;
        playHandlerAnimator();
    }

    /**
     * 暂停
     */
    public void onPause() {
        pauseHandlerAnimator();
    }

    /**
     * 停止
     */
    public void onStop() {
        pauseHandlerAnimator();
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
            MusicJukeBoxCoverPager coverPager=new MusicJukeBoxCoverPager(container.getContext());
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

        mViewPagerIsOffset=false;mEchoPageSelectedEnable=false;
        mDiscStatus = DiscStatus.STOP;
        mScreenWidth=0;
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