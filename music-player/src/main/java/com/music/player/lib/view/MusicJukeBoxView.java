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
 * 此类持有一个单例的歌词控件，默认是不会初始化的，需用户点击屏幕后初始化。
 * 代理歌词功能，你也可以直接通过此对象取出歌词控件对象
 */

public class MusicJukeBoxView extends RelativeLayout{

    private static final String TAG = "MusicJukeBoxView";
    private Context mContext;
    private ImageView mHandImage;
    private MusicViewPager mViewPager;
    private ViewPagerAdapter mPagerAdapter;
    //记录旧的显示的项，回显生命周期
    private int mCurrentPosition=0,mOffsetPosition=0;
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
        this.mContext=context;
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
        //设置唱针的外边距为负数，让其隐藏一部分
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
                //指针动画播放完毕并且唱针处于附着胶盘状态（Rotation 为0：附着，小于0(-30)为与胶盘分离状态）
                if(mHandImage.getRotation()>=HANDLE_STATIC&&null!=mPagerAdapter&&mPagerAdapter.getCount()>0){
                    //开始播放胶盘旋转动画
                    playDiscAnimator(mViewPager.getCurrentItem());
                }
                //唱针动画执行完成后，最终回调偏移位置到组件
                if(null!=mHandImage.getTag()&&null!=mPlayerInfoListener&&null!=mViewPager
                        &&null!=mMusicDatas&&mMusicDatas.size()>mViewPager.getCurrentItem()){
                    int currentItem = mViewPager.getCurrentItem();
                    mPlayerInfoListener.onOffsetPosition(currentItem,(BaseAudioInfo)
                            mMusicDatas.get(currentItem),true);
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
     * 移除View的Parent
     * @param view 目标View
     */
    private void removeViewByGroupVoew(View view) {
        if(null!=view&&null!=view.getParent()){
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }
    }

    /**
     * 播放唱针动画,此方法会在不同交互场景下短时间同时被调用多次，这里已经过滤了重复调用
     * @param compel 是否强制执行，如果为true,将不顾一切尝试动画播放
     * @param tag tag标记如果不为空，则唱针动画执行完成后，回调开始播放事件。否则，唱针动画结束后不做任何处理
     */
    private synchronized void playHandlerAnimator(boolean compel,String tag) {
        if(!mViewPagerIsOffset&&null!=mHandImage) {
            mHandImage.setTag(tag);
            Logger.d(TAG, "playHandlerAnimator-->ROTATION：" + mHandImage.getRotation());
            if (compel) {
                mHandAnimator.start();
                return;
            }
            //仅当完全静止不动并且动画没有开始执行时，开始播放指针动画
            if (mHandImage.getRotation() < HANDLE_STATIC) {
                if (!mHandAnimator.isRunning()) {
                    mHandAnimator.start();
                }
            } else {
                if (null != mPagerAdapter && mPagerAdapter.getCount() > 0) {
                    playDiscAnimator(mViewPager.getCurrentItem());
                }
                //如果指针已经附体，则直接回调到组件新的偏移位置
                if(null!=tag&&null!=mPlayerInfoListener&&null!=mViewPager&&null!=mMusicDatas
                        &&mMusicDatas.size()>mViewPager.getCurrentItem()){
                    mPlayerInfoListener.onOffsetPosition(mViewPager.getCurrentItem(),(BaseAudioInfo)
                            mMusicDatas.get(mViewPager.getCurrentItem()),true);
                }
            }
        }
    }

    /**
     * 暂停唱针动画和封面旋转动画
     */
    private void pauseHandlerAnimator() {
        if(null!=mHandAnimator&&null!=mHandImage){
            //仅当唱针附着胶盘时，让唱针恢复至停止状态
            if(mHandImage.getRotation()!=HANDLE_EXPAND){
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
                    if(!animator.isRunning()){
                        animator.start();
                    }
                }
            }else{
                animator.start();
            }
            notifyMusicStatusChanged(MusicConstants.JUKE_BOX_PLAY);
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
     * 根据数据初始化Pager片段
     * @param musicDataList 源歌曲数据集
     * @param position 定位的位置
     * @param startPlayer true:开始播放 false:只是回显同步
     */
    public void setNewData(List<?> musicDataList, int position,boolean startPlayer) {
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
                this.mCurrentPosition=mViewPager.getCurrentItem();
                this.mOffsetPosition=mCurrentPosition;
                //立即同步状态到组件
                if(null!=mPlayerInfoListener){
                    mPlayerInfoListener.onVisible((BaseAudioInfo) mMusicDatas.get(position),position);
                    mPlayerInfoListener.onOffsetPosition(position,(BaseAudioInfo) mMusicDatas.get(position),startPlayer);
                }
            }
            mViewPager.addOnPageChangeListener(onPageChangedListener);
        }
    }

    /**
     * ViewPager滚动监听
     */
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
            if (mScrollOffsetX > positionOffsetPixels) {
                //左滑
                if (positionOffset < 0.5) {
                    postOffsetMusicInfo(position);
                } else {
                    postOffsetMusicInfo(mViewPager.getCurrentItem());
                }
            }else if (mScrollOffsetX < positionOffsetPixels) {
                //右滑
                if (positionOffset > 0.5) {
                    postOffsetMusicInfo(position + 1);
                } else {
                    postOffsetMusicInfo(position);
                }
            }
            mScrollOffsetX = positionOffsetPixels;
        }

        /**
         * 新的切换的项
         * @param position Position index of the new selected page.
         */
        @Override
        public void onPageSelected(int position) {
            Logger.d(TAG,"onPageSelected-->position:"+position);
            try {
                //先结束内部唱片机动画状态
                if(null!=mFragments){
                    mFragments.get(mCurrentPosition).onReset();
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }finally {
                //歌词还原
                if(null!=mMusicLrcView){
                    mMusicLrcView.onReset();
                    //只有当歌词已经在显示了，尝试隐藏歌词控件
                    if(null!=mMusicLrcView.getParent()){
                        showLrcView(false);
                    }
                    removeViewByGroupVoew(mMusicLrcView);
                }
                //将刚才不可见的项抛出
                if(null!=mPlayerInfoListener){
                    mPlayerInfoListener.onInvisible(mCurrentPosition);
                }
                mCurrentPosition=position;
                //新的position可见
                if(null!=mPlayerInfoListener&&null!=mMusicDatas&&mMusicDatas.size()>position){
                    mPlayerInfoListener.onVisible((BaseAudioInfo) mMusicDatas.get(position),position);
                }
            }
        }

        /**
         * Viewpager切换状态
         * @param state The new scroll state.
         */
        @Override
        public void onPageScrollStateChanged(int state) {
            Logger.d(TAG,"onPageScrollStateChanged-->state:"+state);
            switch (state) {
                //用户已经松手了
                case MusicViewPager.SCROLL_STATE_SETTLING:
                    mViewPagerIsOffset = false;
                    break;
                //界面完全静止，根据播放状态处理动画
                case MusicViewPager.SCROLL_STATE_IDLE:
                    mViewPagerIsOffset = false;
                    if(null!=mViewPager){
                        //若松手后还是停留在旧的PagerPosition,根据播放状态恢复动画
                        if(mOffsetPosition==mViewPager.getCurrentItem()){
                            if(MusicPlayerManager.getInstance().isPlaying()){
                                playHandlerAnimator(true,null);
                            }
                        }else{
                            //开始播放唱针动画
                            playHandlerAnimator(true,TAG);
                        }
                    }
                    mOffsetPosition=mCurrentPosition;
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
     * 定位最新的显示项，用在当界面不可见，内部切换播放源时后界面可见时动画开始执行导致的BUG
     */
    public void updatePosition() {
        if(null!=mViewPager){
            mOffsetPosition=mViewPager.getCurrentItem();
        }
    }

    /**
     * 通知数据源发生了改变s
     * @param position
     */
    public void postOffsetMusicInfo(int position) {
        if(null!=mPlayerInfoListener&&null!=mMusicDatas&&mMusicDatas.size()>position){
            mPlayerInfoListener.onScrollOffsetObject((BaseAudioInfo) mMusicDatas.get(position));
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
        setCurrentMusicItem(position,false,true);
    }

    /**
     * 定位至指定位置
     * @param position 索引位置
     * @param smoothScroll 是否启用滚动动画过渡到指定索引位置
     */
    public void setCurrentMusicItem(int position,boolean smoothScroll) {
        setCurrentMusicItem(position,smoothScroll,false);
    }

    /**
     * 定位至指定位置
     * @param position 索引位置
     * @param smoothScroll 是否启用滚动动画过渡到指定索引位置
     */
    public void setCurrentMusicItem(int position,boolean smoothScroll,boolean startPlay) {
        if(null!=mScroller) {
            //是否缓慢滚动
            mScroller.setScroller(smoothScroll);
        }
        if(null!=mViewPager){
            mViewPager.setCurrentItem(position,smoothScroll);
            if(!smoothScroll){
                mOffsetPosition=mViewPager.getCurrentItem();
            }
        }
        //如果是静止状态下切换Pager，则不会回调OnPageChangeListener中的，onPageScrollStateChanged
        //需手动触发新的选中的项
        if(!smoothScroll&&startPlay){
            playHandlerAnimator(true,TAG);
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
        if(null!=mHandAnimator&&!mHandAnimator.isRunning()){
            playHandlerAnimator(false,null);
        }
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
     * 返回歌词控件实例
     * @return 歌词控件
     */
    public MusicLrcView getMusicLrcView() {
        return mMusicLrcView;
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

        mViewPagerIsOffset=false;
        mDiscStatus = DiscStatus.STOP;
        mScreenWidth=0;mPlayerInfoListener=null;
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
        mContext=null;
    }
}