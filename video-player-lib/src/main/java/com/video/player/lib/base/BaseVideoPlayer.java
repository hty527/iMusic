package com.video.player.lib.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.video.player.lib.R;
import com.video.player.lib.bean.VideoParams;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.controller.DefaultCoverController;
import com.video.player.lib.controller.DefaultVideoController;
import com.video.player.lib.controller.VideoMiniWindowController;
import com.video.player.lib.controller.VideoWindowController;
import com.video.player.lib.listener.VideoEventListener;
import com.video.player.lib.listener.VideoOrientationListener;
import com.video.player.lib.listener.VideoPlayerEventListener;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.manager.VideoWindowManager;
import com.video.player.lib.model.VideoPlayerState;
import com.video.player.lib.utils.Logger;
import com.video.player.lib.utils.VideoUtils;
import com.video.player.lib.view.PlayerGestureView;
import com.video.player.lib.view.VideoTextureView;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * Base Video Player
 * 视频播放器核心类
 * 拓展泛型说明：V：视频控制器 C：封面控制器 G：手势识别控制器
 * 继承此类，在.xml文件中必须引用 values下的ids.xml中定义的surface_view ID
 * 播放器控制器video_player_controller_view 和 封面控制器video_cover_controller 非强制定义，可选择定义
 *
 * 支持的功能包括但不限于：列表单例播放、列表横竖屏切换、常规横竖屏切换、可拖拽迷你窗口切换、可拖拽悬浮窗口切换、
 * 从 A activity跳转至B activity 无缝衔接播放、手势调节控制器（音量、亮度、进度）、
 * 自定义VideoController、CoverController、GestureController
 * 播放器默认不会创建默认的封面、播放器控制器，需在xml文件中指定属性atts.BaseVideoPlayer,详见属性注释说明。
 * 也可以调用BaseVideoPlayer的setVideoController和setVideoCoverController设置控制器
 * 注意：此播放器在创建全局、迷你小窗口、悬浮窗时，需要区别实例对象，内部已做处理。
 */

public abstract class BaseVideoPlayer<V extends BaseVideoController,C extends BaseCoverController
        ,G extends BaseGestureController> extends FrameLayout implements VideoPlayerEventListener
        ,View.OnTouchListener {

    private static final String TAG = "BaseVideoPlayer";
    //VideoController
    protected V mVideoController;
    //CoverController
    protected C mCoverController;
    //GestureController
    protected G mGestureController;
    //资源地址、视频标题
    private String mDataSource,mTitle;
    //视频ID，悬浮窗打开Activity用到
    private long mVideoID;
    //视频帧渲染父容器
    public FrameLayout mSurfaceView;
    //缩放类型,默认是等比缩放
    public static int VIDEO_DISPLAY_TYPE = VideoConstants.VIDEO_DISPLAY_TYPE_CUT;
    //此播放器是否正在工作,配合列表滚动时，检测工作状态
    private boolean isWorking=false;
    //屏幕方向、手势调节，默认未知
    private int SCRREN_ORIENTATION = 0,GESTURE_SCENE=0;
    //屏幕的方向,竖屏、横屏、窗口
    private SensorManager mSensorManager;
    //屏幕方向监听器
    private VideoOrientationListener mOrientationListener;
    //播放器内部事件监听器
    private VideoEventListener mEventListener;
    //常规播放器手势代理，配合悬浮窗使用、全屏手势代理，配合手势调节功能使用
    private GestureDetector mGestureDetector,mFullScreenGestureDetector;
    //全屏的手势触摸、迷你小窗口的手势触摸
    private FrameLayout mTouchViewGroup,mMiniTouchViewGroup;
    //手势跳转播放进度
    private long mSpeedTime=0;
    //手势结束后是否需要跳转至对应位置播放
    private boolean isSpeedSeek=false;

    protected abstract int getLayoutID();

    public BaseVideoPlayer(@NonNull Context context) {
        this(context,null);
    }

    public BaseVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BaseVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        boolean autoSetVideoController=false;
        boolean autoSetCoverController=false;
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseVideoPlayer);
            autoSetVideoController = typedArray.getBoolean(R.styleable.BaseVideoPlayer_video_autoSetVideoController, false);
            autoSetCoverController = typedArray.getBoolean(R.styleable.BaseVideoPlayer_video_autoSetCoverController, false);
            boolean loop = typedArray.getBoolean(R.styleable.BaseVideoPlayer_video_loop, false);
            VideoPlayerManager.getInstance().setLoop(loop);
            typedArray.recycle();
        }
        View.inflate(context,getLayoutID(),this);
        //默认的初始化
        setVideoController(null,autoSetVideoController);
        setVideoCoverController(null,autoSetCoverController);
        //画面渲染
        mSurfaceView = (FrameLayout) findViewById(R.id.surface_view);
        // TODO: 2019/4/15 这里遇到了坑：当播放器组件设置了OnTouchListener或OnClickListener事件后，悬浮窗开启时，Windown无法取得焦点
        // 故这里采用GestureDetector手势分发器代为处理手势,至于全屏后的播放器，这里做法是新创建一个ViewGroup至控制器的底层处理手势事件
        if(null!=mSurfaceView){
            mGestureDetector = new GestureDetector(context,new TouchOnGestureListener());
            mSurfaceView.setOnTouchListener(this);
        }
    }

    //========================================播放器手势处理=========================================

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(null!=mGestureDetector) mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 拦截触摸屏幕的所有事件
     */
    private  class TouchOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            if(VideoPlayerManager.getInstance().isPlaying()){
                if(null!=mVideoController){
                    mVideoController.changeControllerState(SCRREN_ORIENTATION,true);
                }
            }
            return true;
        }
    }

    //======================================播放器对外开放功能========================================

    /**
     * 设置播放资源
     * @param path 暂支持file、http、https等协议
     * @param title 视频描述
     */
    public void setDataSource(String path, String title) {
        if(null!= mVideoController){
            mVideoController.setTitle(title);
        }
        this.mDataSource=path;
        this.mTitle=title;
    }

    /**
     * 设置播放资源
     * @param path 暂支持file、http、https等协议
     * @param title 视频描述
     * @param videoID 视频ID
     */
    public void setDataSource(String path, String title,long videoID) {
        if(null!= mVideoController){
            mVideoController.setTitle(title);
        }
        this.mDataSource=path;
        this.mTitle=title;
        this.mVideoID=videoID;
    }

    /**
     * 设置参数TAG，可选的，若支持悬浮窗中打开播放器功能，则必须调用此方法绑定PlayerActivity所需参数
     * @param params VideoPlayerActivity 组件所需参数
     */
    public void setParamsTag(VideoParams params) {
        this.setTag(params);
    }

    /**
     * 设置循环模式，也可调用VideoWindowManager的setLoop(boolean loop);方法设置
     * @param loop
     */
    public void setLoop(boolean loop) {
        VideoPlayerManager.getInstance().setLoop(loop);
    }

    /**
     * 设置缩放类型
     * @param displayType 详见VideoConstants常量定义
     */
    public void setVideoDisplayType(int displayType) {
        this.VIDEO_DISPLAY_TYPE= displayType;
    }

    /**
     * 悬浮窗功能入口开关，需要在C不为空下调用
     * @param enable true:允许 false:禁用
     */
    public void setGlobaEnable(boolean enable) {
        if(null!=mVideoController){
            mVideoController.setGlobaEnable(enable);
        }
    }

    /**
     * 设置视频控制器
     * @param videoController 自定义VideoPlayer控制器
     * @param autoCreateDefault 当 controller 为空，是否自动创建默认的控制器
     */
    public void setVideoController(V videoController, boolean autoCreateDefault) {
        FrameLayout conntrollerView = (FrameLayout) findViewById(R.id.video_player_controller);
        if(null!=conntrollerView){
            removeGroupView(mVideoController);
            if(conntrollerView.getChildCount()>0){
                conntrollerView.removeAllViews();
            }
            if(null!= mVideoController){
                mVideoController.onDestroy();
                mVideoController =null;
            }
            //使用自定义的
            if(null!=videoController){
                mVideoController = videoController;
            }else{
                //是否使用默认的
                if(autoCreateDefault){
                    mVideoController = (V) new DefaultVideoController(getContext());
                }
            }
            //添加控制器到播放器
            if(null!=mVideoController){
                mVideoController.setOnFuctionListener(new BaseVideoController.OnFuctionListener() {
                    /**
                     * 转向全屏播放
                     */
                    @Override
                    public void onStartFullScreen(BaseVideoController videoController) {
                        if(SCRREN_ORIENTATION==VideoConstants.SCREEN_ORIENTATION_PORTRAIT){
                            startFullScreen((V) videoController);
                        }else{
                            backFullScreenWindow();
                        }
                    }

                    /**
                     * 转向迷你窗口播放
                     */
                    @Override
                    public void onStartMiniWindow(BaseVideoController miniWindowController) {
                        startMiniWindow(miniWindowController);
                    }

                    /**
                     * 转向悬浮窗播放
                     */
                    @Override
                    public void onStartGlobalWindown(BaseVideoController windowController,boolean defaultCreatCloseIcon) {
                        startGlobalWindown(windowController,defaultCreatCloseIcon);
                    }

                    /**
                     * 退出迷你窗口
                     */
                    @Override
                    public void onQuiteMiniWindow() {
                        if(SCRREN_ORIENTATION==VideoConstants.SCREEN_ORIENTATION_TINY){
                            backMiniWindow();
                        }
                    }

                    /**
                     * 从悬浮窗中打开视频播放器
                     */
                    @Override
                    public void onStartActivity() {
                        //悬浮窗打开Activity
                        startWindowToActivity();
                    }

                    /**
                     * 尝试弹射返回，在Activity onBackPressed方法中调用
                     */
                    @Override
                    public void onBackPressed() {
                        backPressed();
                    }
                });
                conntrollerView.addView(mVideoController,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            }
        }
    }

    /**
     * 将自己从父Parent中移除
     * @param viewGroup
     */
    private void removeGroupView(ViewGroup viewGroup) {
        if(null!=viewGroup&&null!=viewGroup.getParent()){
            Logger.d(TAG,"removeGroupView-->");
            ViewGroup parent = (ViewGroup) viewGroup.getParent();
            parent.removeView(viewGroup);
        }
    }

    /**
     * 设置封面控制器
     * @param coverController 自定义VideoPlayerCover控制器
     * @param autoCreateDefault 当 controller 为空，是否自动创建默认的控制器
     */
    public void setVideoCoverController(C coverController, boolean autoCreateDefault) {
        FrameLayout conntrollerView = (FrameLayout) findViewById(R.id.video_cover_controller);
        if(null!=conntrollerView){
            removeGroupView(mCoverController);
            if(conntrollerView.getChildCount()>0){
                conntrollerView.removeAllViews();
            }
            if(null!= mCoverController){
                mCoverController.onDestroy();
                mCoverController =null;
            }
            //使用自定义的
            if(null!=coverController){
                mCoverController = coverController;
            }else{
                //是否使用默认的
                if(autoCreateDefault){
                    mCoverController = (C) new DefaultCoverController(getContext());
                }
            }
            //添加控制器到播放器
            if(null!=mCoverController){
                mCoverController.setOnStartListener(new BaseCoverController.OnStartListener() {
                    @Override
                    public void onStartPlay() {
                        startPlayVideo();
                    }
                });
                conntrollerView.addView(mCoverController,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            }
        }
    }

    /**
     * 设置自定义的手势识别器
     * @param gestureController
     */
    public void setVideoGestureController(G gestureController){
        this.mGestureController=gestureController;
    }

    /**
     * 移动网络工作开关
     * @param mobileWorkEnable
     */
    public void setMobileWorkEnable(boolean mobileWorkEnable){
        VideoPlayerManager.getInstance().setMobileWorkEnable(mobileWorkEnable);
    }

    /**
     * 返回封面控制器
     * @return
     */
    public C getCoverController() {
        return mCoverController;
    }

    /**
     * 返回视频控制器
     * @return
     */
    public V getVideoController() {
        return mVideoController;
    }

    /**
     * 返回全屏的手势识别控制器
     * @return
     */
    public G getGestureController() {
        return mGestureController;
    }

    /**
     * 更新播放器方向
     * @param scrrenOrientation
     */
    public void setScrrenOrientation(int scrrenOrientation) {
        this.SCRREN_ORIENTATION=scrrenOrientation;
        if(null!=mVideoController){
            mVideoController.setScrrenOrientation(scrrenOrientation);
        }
    }

    /**
     * 方向重力感应开关
     * @param enable
     */
    public void setOrientantionEnable(boolean enable){
        if(enable){
            AppCompatActivity appCompActivity = VideoUtils.getInstance().getAppCompActivity(getContext());
            if(null!=appCompActivity){
                mSensorManager = (SensorManager)appCompActivity.getSystemService(Context.SENSOR_SERVICE);
                mOrientationListener = new VideoOrientationListener(new VideoOrientationListener.OnOrientationChangeListener() {
                    @Override
                    public void orientationChanged(int orientation) {
                        if(SCRREN_ORIENTATION==VideoConstants.SCREEN_ORIENTATION_FULL){
                            Logger.d(TAG,"orientationChanged-->newOrientation:"+orientation);
                        }
                    }
                });
                mSensorManager.registerListener(mOrientationListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            }
        }else{
            if(null!=mSensorManager&&null!=mOrientationListener){
                mSensorManager.unregisterListener(mOrientationListener);
                mSensorManager=null;mOrientationListener=null;
            }
        }
    }

    /**
     * 主播监听播放器内部事件
     * @param eventListener
     */
    public void setVideoEventListener(VideoEventListener eventListener){
        this.mEventListener=eventListener;
    }

    /**
     * 开始播放的入口开始播放、准备入口
     */
    public void startPlayVideo(){
        if(TextUtils.isEmpty(mDataSource)){
            Toast.makeText(getContext(),"播放地址为空",Toast.LENGTH_SHORT).show();
             return;
        }
        Logger.d(TAG,"startVideo-->");
        //还原可能正在进行的播放任务
        VideoPlayerManager.getInstance().onReset();
        VideoPlayerManager.getInstance().addOnPlayerEventListener(this);
        setWorking(true);
        //准备画面渲染图层
        if(null!=mSurfaceView){
            addTextrueViewToView(BaseVideoPlayer.this);
            //开始准备播放
            VideoPlayerManager.getInstance().startVideoPlayer(mDataSource,getContext());
        }
    }

    /**
     * 开始播放的入口开始播放、准备入口,调用此方法，可省去setDataSource()方法的调用
     * @param dataSource 播放资源地址
     * @param title 视频标题
     */
    public void startPlayVideo(String dataSource,String title){
        this.mDataSource=dataSource;
        this.mTitle=title;
        startPlayVideo();
    }

    /**
     * 开始播放的入口开始播放、准备入口,调用此方法，可省去setDataSource()方法的调用
     * @param dataSource 播放资源地址
     * @param title 视频标题
     * @param videoID 视频ID
     */
    public void startPlayVideo(String dataSource,String title,long videoID){
        this.mDataSource=dataSource;
        this.mTitle=title;
        this.mVideoID=videoID;
        startPlayVideo();
    }

    /**
     * 添加一个视频渲染组件至VideoPlayer
     * @param videoPlayer
     */
    private void addTextrueViewToView(BaseVideoPlayer videoPlayer) {
        //先移除存在的TextrueView
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            VideoTextureView textureView = VideoPlayerManager.getInstance().getTextureView();
            if(null!=textureView.getParent()){
                ((ViewGroup) textureView.getParent()).removeView(textureView);
            }
        }
        if(null==videoPlayer.mSurfaceView) return;
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            videoPlayer.mSurfaceView.addView(VideoPlayerManager.getInstance().getTextureView(),new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }else{
            VideoTextureView textureView=new VideoTextureView(getContext());
            VideoPlayerManager.getInstance().initTextureView(textureView);
            videoPlayer.mSurfaceView.addView(textureView,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }

    /**
     * 从悬浮窗播放器窗口转向VideoPlayerActivity播放
     */
    public void startWindowToActivity() {
        if(!TextUtils.isEmpty(VideoPlayerManager.getInstance().getVideoPlayerActivityClassName())){
            //先结束悬浮窗播放任务
            BaseVideoPlayer baseVideoPlayer = backGlobalWindownToActivity();
            Intent startIntent=new Intent();
            startIntent.setClassName(VideoUtils.getInstance().getPackageName(getContext().getApplicationContext()),VideoPlayerManager.getInstance().getVideoPlayerActivityClassName());
            startIntent.putExtra(VideoConstants.KEY_VIDEO_PLAYING,true);
            //如果播放器组件未启用，创建新的实例
            //如果播放器组件已启用且在栈顶，复用播放器不传递任何意图
            //反之则清除播放器之上的所有栈，让播放器组件显示在最顶层
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(null!=this.getTag()&&getTag() instanceof VideoParams){
                VideoParams videoParams = (VideoParams) this.getTag();
                startIntent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,videoParams);
            }else{
                VideoParams videoParams=new VideoParams();
                Logger.d(TAG,"mTitle:"+mTitle+",mDataSource:"+mDataSource);
                videoParams.setVideoTitle(mTitle);
                videoParams.setVideoUrl(mDataSource);
                videoParams.setVideoiId(mVideoID);
                startIntent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,videoParams);
            }
            getContext().getApplicationContext().startActivity(startIntent);
            //销毁一下，万不能再界面跳转之前销毁
            if(null!=baseVideoPlayer){
                baseVideoPlayer.destroy();
                VideoPlayerManager.getInstance().setWindownPlayer(null);
            }
        }
    }

    /**
     * 开启全屏播放的原理：
     * 1：改变屏幕方向，Activity 属性必须设置为android:configChanges="orientation|screenSize"，避免Activity销毁重建
     * 2：移除常规播放器已有的TextureView组件
     * 3：向Windown ViewGroup 添加一个新的VideoPlayer组件,赋值已有的TextrueView到VideoPlayer，设置新的播放器监听，结合TextrueView onSurfaceTextureAvailable 回调事件处理
     * 4：根据自身业务，向新的播放器添加控制器
     * 5：记录全屏窗口播放器，退出全屏恢复常规播放用到
     * @param fullScreenVideoController 全屏控制器，为空则使用默认控制器
     */
    public void startFullScreen(V fullScreenVideoController) {
        AppCompatActivity appCompActivity = VideoUtils.getInstance().getAppCompActivity(getContext());
        if (null != appCompActivity) {
            SCRREN_ORIENTATION = VideoConstants.SCREEN_ORIENTATION_FULL;
            setScrrenOrientation(SCRREN_ORIENTATION);
            //改变屏幕方向
            appCompActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            appCompActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ViewGroup viewGroup = (ViewGroup) appCompActivity.getWindow().getDecorView();
            if (null != viewGroup && null != VideoPlayerManager.getInstance().getTextureView()) {
                View oldFullVideo = viewGroup.findViewById(R.id.video_full_screen_window);
                //移除Window可能存在的播放器组件
                if (null != oldFullVideo) {
                    viewGroup.removeView(oldFullVideo);
                }
                //保存当前实例
                VideoPlayerManager.getInstance().setNoimalPlayer(BaseVideoPlayer.this);
                try {
                    Constructor<? extends BaseVideoPlayer> constructor = BaseVideoPlayer.this.getClass().getConstructor(Context.class);
                    //新实例化自己
                    BaseVideoPlayer videoPlayer = constructor.newInstance(getContext());
                    videoPlayer.setBackgroundColor(Color.parseColor("#000000"));
                    //绑定组件ID
                    videoPlayer.setId(R.id.video_full_screen_window);
                    //保存全屏窗口实例
                    VideoPlayerManager.getInstance().setFullScrrenPlayer(videoPlayer);
                    //将新的实例化添加至Window
                    viewGroup.addView(videoPlayer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    //设置用户自定义全屏播放器控制器
                    if(null!=fullScreenVideoController){
                        videoPlayer.setVideoController(fullScreenVideoController, false);
                    }else{
                        //设置内部默认控制器
                        videoPlayer.setVideoController(null, true);
                    }
                    //更新屏幕方向
                    videoPlayer.setScrrenOrientation(SCRREN_ORIENTATION);
                    //转换为横屏方向
                    videoPlayer.mVideoController.startHorizontal();
                    videoPlayer.setWorking(true);
                    //设置基础的配置
                    videoPlayer.setDataSource(mDataSource, mTitle);
                    //清除全屏控件的手势事件
                    if (null != videoPlayer.mSurfaceView) {
                        videoPlayer.mSurfaceView.setOnTouchListener(null);
                    }
                    //添加一个不可见的ViewGroup至最底层，用来处理手势事件
                    mTouchViewGroup = new FrameLayout(videoPlayer.getContext());
                    OnFullScreenGestureListener gestureListener = new OnFullScreenGestureListener(videoPlayer.getVideoController());
                    mFullScreenGestureDetector = new GestureDetector(videoPlayer.getContext(), gestureListener);
                    OnFullScreenTouchListener fullScreenTouchListener = new OnFullScreenTouchListener();
                    mTouchViewGroup.setOnTouchListener(fullScreenTouchListener);
                    removeGroupView(mTouchViewGroup);
                    videoPlayer.addView(mTouchViewGroup, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    //生成一个默认的亮度、进度、声音手势进度调节View
                    if(null==mGestureController){
                        Logger.d(TAG,"startFullScreen-->使用默认的手势控制器");
                        mGestureController = (G) new PlayerGestureView(videoPlayer.getContext());
                    }
                    removeGroupView(mGestureController);
                    videoPlayer.addView(mGestureController,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    //添加TextrueView至播放控件
                    addTextrueViewToView(videoPlayer);
                    //添加监听器
                    VideoPlayerManager.getInstance().addOnPlayerEventListener(videoPlayer);
                    //手动检查播放器内部状态，同步常规播放器状态至新的播放器
                    VideoPlayerManager.getInstance().checkedVidepPlayerState();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 播放器全屏状态下的手势处理，处理上、下、左、右、左侧区域、右侧区域等手势
     */

    private  class OnFullScreenGestureListener extends GestureDetector.SimpleOnGestureListener {

        public static final String TAG = "OnFullScreenGestureListener";
        private BaseVideoController videoController;
        // 每次触摸屏幕后，第一次scroll的标志
        private boolean mFirstScroll = false;
        private int mVideoPlayerWidth,mVideoPlayerHeight,mMaxVolume,mCurrentVolume;
        //总时长、已播放时长、跳转累加时长
        private long mTotalTime;
        //亮度
        private float mBrightness = -1f;
        //进度阻尼敏感度，为1像素的3倍，意为滑动3像素等于1像素,2f=2*3
        private static final float STEP_PROGRESS = 2f;
        //声音的阻尼敏感度，为像素的3倍，意为滑动3像素等于1像素，2f=2*3
        private static final float STEP_SOUND = 2f;
        //音量管理者
        private AudioManager mAudioManager;
        //窗口
        private Window mWindow=null;

        /**
         * 构造器
         * @param controller
         */
        public OnFullScreenGestureListener(BaseVideoController controller){
            this.videoController = controller;
            if(null!=videoController){
                OnFullScreenGestureListener.this.videoController.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        OnFullScreenGestureListener.this.videoController.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mVideoPlayerWidth = OnFullScreenGestureListener.this.videoController.getWidth();
                        mVideoPlayerHeight = OnFullScreenGestureListener.this.videoController.getHeight();
                        Logger.d(TAG,"setVideoController-->VIDEO_PLAYER_WIDTH:"+mVideoPlayerWidth+",VIDEO_PLAYER_HEIGHT:"+mVideoPlayerHeight);
                    }
                });
                AppCompatActivity appCompActivity = VideoUtils.getInstance().getAppCompActivity(videoController.getContext());
                if(null!=appCompActivity){
                    mWindow = appCompActivity.getWindow();
                }
                mAudioManager = (AudioManager) videoController.getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            }
        }

        /**
         * 处理用户手势滚动
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @return 1000  879
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Logger.d(TAG,"onScroll-->distanceX:"+distanceX+",distanceY:"+distanceY+",e1X:"+e1.getX()+",e1Y:"+e1.getY()+",e2X:"+e2.getX()+",e2Y:"+e2.getY());
            if(null==videoController||null==mGestureController) return false;
            float oldX = e1.getX(), oldY = e1.getY();
            int y = (int) e2.getRawY();
            if (mFirstScroll) {
                //初始化音量
                if(null!=mAudioManager){
                    mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                }else{
                    mAudioManager = (AudioManager) videoController.getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                }
                //总时长和播放进度
                mTotalTime=VideoPlayerManager.getInstance().getDurtion();
                mSpeedTime=VideoPlayerManager.getInstance().getCurrentDurtion();
                //初始化亮度
                if(null!=mWindow){
                    mBrightness = mWindow.getAttributes().screenBrightness;
                }else{
                    AppCompatActivity appCompActivity = VideoUtils.getInstance().getAppCompActivity(videoController.getContext());
                    if(null!=appCompActivity){
                        mWindow = appCompActivity.getWindow();
                        mBrightness = mWindow.getAttributes().screenBrightness;
                    }
                }
                // 横向的距离变化大则调整进度，纵向的变化大则调整音量
                if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                    //进度
                    GESTURE_SCENE = BaseGestureController.SCENE_PROGRESS;
                } else {
                    if (oldX > mVideoPlayerWidth * 3.0 / 5) {
                        // 音量
                        GESTURE_SCENE = BaseGestureController.SCENE_SOUND;
                    } else if (oldX < mVideoPlayerWidth * 2.0 / 5) {
                        // 亮度
                        GESTURE_SCENE = BaseGestureController.SCENE_BRIGHTNRSS;
                    }
                }
                Logger.d(TAG,"FIRST-->mMaxVolume:"+mMaxVolume+",mCurrentVolume:"+mCurrentVolume+",mBrightness:"+mBrightness+",GESTURE_SCENE:"+GESTURE_SCENE);
            }
            //如果是直播类型的，且是快进快退动作，则啥也不做
            if(GESTURE_SCENE==BaseGestureController.SCENE_PROGRESS&&VideoUtils.getInstance().isLiveStream(mDataSource)){
                return false;
            }
            //更新手势控制器UI显示
            mGestureController.updataGestureScene(GESTURE_SCENE);
            //定性为调节进度事件
            if (GESTURE_SCENE == BaseGestureController.SCENE_PROGRESS) {
                // 横向移动大于纵向移动
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX >= VideoUtils.getInstance().dpToPxInt(videoController.getContext(), STEP_PROGRESS)) {
                        // 快退，用步长控制改变速度，可微调
                        //5000毫秒一格
                        mSpeedTime=(mSpeedTime-1000);
                        if(mSpeedTime<1){
                            mSpeedTime=1;
                        }
                    } else if (distanceX <= -VideoUtils.getInstance().dpToPxInt(videoController.getContext(), STEP_PROGRESS)) {
                        mSpeedTime=(mSpeedTime+1000);
                        if(mSpeedTime>mTotalTime){
                            mSpeedTime=mTotalTime;
                        }
                    }
                    int progress = (int) (((float) mSpeedTime / mTotalTime) * 100);
                    isSpeedSeek=true;
                    Logger.d(TAG,"--快进、快退--:"+progress);
                    mGestureController.setVideoProgress(mTotalTime,mSpeedTime,progress);
                }
            }
            //定性为调节音量事件
            else if (GESTURE_SCENE == BaseGestureController.SCENE_SOUND) {
                if (Math.abs(distanceY) > Math.abs(distanceX)&&null!=mAudioManager) {
                    // 纵向移动大于横向移动
                    if (distanceY >= VideoUtils.getInstance().dpToPxInt(videoController.getContext(), STEP_SOUND)) {
                        // 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                        if (mCurrentVolume < mMaxVolume) {
                            mCurrentVolume++;
                        }
                    } else if (distanceY <= -VideoUtils.getInstance().dpToPxInt(videoController.getContext(), STEP_SOUND)) {
                        // 音量调小
                        if (mCurrentVolume > 0) {
                            mCurrentVolume--;
                        }
                    }
                    int percentage = (mCurrentVolume * 100) / mMaxVolume;
                    mGestureController.setSoundrogress(percentage);
                    Logger.d(TAG,"--音量--:"+percentage);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mCurrentVolume, 0);
                }
            }
            //定性为调节亮度事件
            else if (GESTURE_SCENE == BaseGestureController.SCENE_BRIGHTNRSS) {
                if(null!=mWindow){
                    if (mBrightness < 0) {
                        mBrightness = mWindow.getAttributes().screenBrightness;
                        if (mBrightness <= 0.00f){
                            mBrightness = 0.50f;
                        }
                        if (mBrightness < 0.01f){
                            mBrightness = 0.01f;
                        }
                    }
                    WindowManager.LayoutParams lpa = mWindow.getAttributes();
                    lpa.screenBrightness = mBrightness + (oldY - y) / mVideoPlayerHeight;
                    if (lpa.screenBrightness > 1.0f){
                        lpa.screenBrightness = 1.0f;
                    }else if (lpa.screenBrightness < 0.00f){
                        lpa.screenBrightness = 0.00f;
                    }
                    int brigthness = (int) (lpa.screenBrightness * 100);
                    Logger.d(TAG,"--亮度--:"+brigthness);
                    mGestureController.setBrightnessProgress(brigthness);
                    mWindow.setAttributes(lpa);
                }
            }
            // 第一次scroll执行完成，修改标志
            mFirstScroll = false;
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            //设定是触摸屏幕后第一次scroll的标志
            mFirstScroll = true;
            return true;
        }

        /**
         * 双击
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            VideoPlayerManager.getInstance().playOrPause();
            return false;
        }

        /**
         * 单击
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(VideoPlayerManager.getInstance().isPlaying()){
                //回调至播放器内部C控制器单击事件
                if(null!=videoController){
                    videoController.changeControllerState(SCRREN_ORIENTATION,true);
                }
            }
            return false;
        }

        /**
         * 绑定播放器组件
         * @param videoController
         */
        public void setVideoController(BaseVideoController videoController) {
            this.videoController = videoController;
            OnFullScreenGestureListener.this.videoController.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    OnFullScreenGestureListener.this.videoController.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mVideoPlayerWidth = OnFullScreenGestureListener.this.videoController.getWidth();
                    mVideoPlayerHeight = OnFullScreenGestureListener.this.videoController.getHeight();
                }
            });
        }
    }

    /**
     * 全屏播放器手势,交给手势控制器接管 OnFullScreenGestureListener
     */
    private class OnFullScreenTouchListener implements OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //手指离开屏幕，还原手势控制器
            if(event.getAction()==MotionEvent.ACTION_CANCEL||event.getAction()==MotionEvent.ACTION_UP){
                GESTURE_SCENE= BaseGestureController.SCENE_PROGRESS;
                //还原
                if(null!=mGestureController){
                    mGestureController.onReset(800);
                }
                //手势控制调节生效
                if(isSpeedSeek&&mSpeedTime>0){
                    VideoPlayerManager.getInstance().seekTo(mSpeedTime);
                    isSpeedSeek=false;
                }
            }
            if(null!=mFullScreenGestureDetector){
                return mFullScreenGestureDetector.onTouchEvent(event);
            }
            return false;
        }
    }

    /**
     * 退出全屏播放
     * 退出全屏播放的原理：和开启全屏反过来
     */
    public void backFullScreenWindow(){
        AppCompatActivity appCompActivity = VideoUtils.getInstance().getAppCompActivity(getContext());
        if(null!=appCompActivity){
            SCRREN_ORIENTATION=VideoConstants.SCREEN_ORIENTATION_PORTRAIT;
            //改变屏幕方向
            appCompActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            appCompActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            BaseVideoPlayer fullScrrenPlayer = VideoPlayerManager.getInstance().getFullScrrenPlayer();
            //移除全屏播放器的SurfaceView及屏幕窗口的VideoPlayer
            if(null!=fullScrrenPlayer){
                //清除底层手势控制的ViewGroup
                if(null!=mTouchViewGroup){
                    mTouchViewGroup.setOnTouchListener(null);
                    if(null!=mTouchViewGroup.getParent()){
                        ViewGroup parent = (ViewGroup) mTouchViewGroup.getParent();
                        parent.removeView(mTouchViewGroup);
                    }
                    mTouchViewGroup=null;mFullScreenGestureDetector=null;
                }
                //清除全屏手势控制器
                if(null!=mGestureController&&null!=mGestureController.getParent()){
                    ViewGroup parent = (ViewGroup) mGestureController.getParent();
                    mGestureController.onDestroy();
                    parent.removeView(mGestureController);
                }
                if(null!=VideoPlayerManager.getInstance().getTextureView()&&null!=fullScrrenPlayer.mSurfaceView){
                    fullScrrenPlayer.mSurfaceView.removeView(VideoPlayerManager.getInstance().getTextureView());
                }
                fullScrrenPlayer.destroy();
                //从窗口移除ViewPlayer
                ViewGroup viewGroup = (ViewGroup) appCompActivity.getWindow().getDecorView();
                View oldFullVideo = viewGroup.findViewById(R.id.video_full_screen_window);
                if(null!=oldFullVideo){
                    viewGroup.removeView(oldFullVideo);
                }else{
                    viewGroup.removeView(fullScrrenPlayer);
                }
                VideoPlayerManager.getInstance().setFullScrrenPlayer(null);
            }
            BaseVideoPlayer noimalPlayer = VideoPlayerManager.getInstance().getNoimalPlayer();
            if(null!=noimalPlayer){
                noimalPlayer.setScrrenOrientation(SCRREN_ORIENTATION);
                addTextrueViewToView(noimalPlayer);
                VideoPlayerManager.getInstance().addOnPlayerEventListener(noimalPlayer);
                //手动检查播放器内部状态，同步全屏播放器至常规播放器状态
                VideoPlayerManager.getInstance().checkedVidepPlayerState();
            }
        }
    }

    /**
     * 开启小窗口播放
     * 默认X：30像素 Y：30像素 位于屏幕左上角,使用默认控制器
     * @param miniWindowController 适用于迷你窗口播放器的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startMiniWindow(BaseVideoController miniWindowController){
        startMiniWindow(30,30,0,0, (V) miniWindowController);
    }

    /**
     * 开启小窗口播放
     *
     * @param startX     起点位于屏幕的X轴像素
     * @param startY     起点位于屏幕的Y轴像素
     * @param tinyWidth  小窗口的宽 未指定使用默认 屏幕宽的 1/2(二分之一)
     * @param tinyHeight 小窗口的高 未指定使用默认 屏幕宽的 1/2 *9/16
     * @param miniWindowController 适用于迷你窗口播放器的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startMiniWindow(int startX, int startY, int tinyWidth, int tinyHeight,V miniWindowController) {
        Logger.d(TAG,"startMiniWindow-->isPlaying():"+isPlaying());
        if (VideoWindowManager.getInstance().isWindowShowing()) {
            Toast.makeText(getContext(), "已在悬浮窗播放", Toast.LENGTH_SHORT).show();
            return;
        }
        if (SCRREN_ORIENTATION == VideoConstants.SCREEN_ORIENTATION_TINY) {
            Toast.makeText(getContext(), "已切换至小窗口", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isPlaying()){
            Toast.makeText(getContext(), "只能在正在播放状态下切换小窗口播放", Toast.LENGTH_SHORT).show();
            return;
        }
        AppCompatActivity appCompActivity = VideoUtils.getInstance().getAppCompActivity(getContext());
        if (null != appCompActivity) {
            SCRREN_ORIENTATION = VideoConstants.SCREEN_ORIENTATION_TINY;
            ViewGroup viewGroup = (ViewGroup) appCompActivity.getWindow().getDecorView();
            if (null != viewGroup && null != VideoPlayerManager.getInstance().getTextureView()) {
                View oldTinyVideo = viewGroup.findViewById(R.id.video_mini_window);
                //移除Window可能存在的播放器组件
                if (null != oldTinyVideo) {
                    viewGroup.removeView(oldTinyVideo);
                }
                //保存当前常规实例
                VideoPlayerManager.getInstance().setNoimalPlayer(BaseVideoPlayer.this);
                VideoPlayerManager.getInstance().getNoimalPlayer().reset();
                try {
                    Constructor<? extends BaseVideoPlayer> constructor = BaseVideoPlayer.this.getClass().getConstructor(Context.class);
                    //新实例化窗口
                    BaseVideoPlayer videoPlayer = constructor.newInstance(getContext());
                    //绑定组件ID
                    videoPlayer.setId(R.id.video_mini_window);
                    //保存小窗口实例
                    VideoPlayerManager.getInstance().setMiniWindowPlayer(videoPlayer);
                    //将新的实例化添加至Window
                    int screenWidth = VideoUtils.getInstance().getScreenWidth(appCompActivity);
                    int width = screenWidth / 2;
                    int height = width * 9 / 16;
                    if (tinyWidth > 0) {
                        width = tinyWidth;
                    }
                    if (tinyHeight > 0) {
                        height = tinyHeight;
                    }
                    Logger.d(TAG, "startMiniWindow-->startX:" + startX + ",startY:" + startY + ",tinyWidth:" + tinyWidth + ",tinyHeight:" + tinyHeight);
                    LayoutParams layoutParams = new LayoutParams(width, height);
                    layoutParams.setMargins(startX, startY, 0, 0);
                    viewGroup.addView(videoPlayer, layoutParams);
                    //设置自定义迷你窗口控制器
                    if(null!=miniWindowController){
                        videoPlayer.setVideoController(miniWindowController, false);
                    }else{
                        //设置一个默认的控制器
                        videoPlayer.setVideoController(new VideoMiniWindowController(videoPlayer.getContext()), false);
                    }
                    //更新屏幕方向,这里只更新为窗口模式即可
                    videoPlayer.setScrrenOrientation(SCRREN_ORIENTATION);
                    //转换为小窗口模式
                    videoPlayer.mVideoController.startTiny();
                    videoPlayer.setWorking(true);
                    //清除小窗口播放器的手势事件
                    if (null != videoPlayer.mSurfaceView) {
                        videoPlayer.mSurfaceView.setOnTouchListener(null);
                    }
                    //添加一个不可见的ViewGroup至最底层，用来处理手势事件
                    mMiniTouchViewGroup = new FrameLayout(videoPlayer.getContext());
                    OnMiniWindownTouchListener miniWindownTouchListener = new OnMiniWindownTouchListener();
                    miniWindownTouchListener.setAnchorView(videoPlayer);
                    mMiniTouchViewGroup.setOnTouchListener(miniWindownTouchListener);
                    videoPlayer.addView(mMiniTouchViewGroup, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    //设置基础的配置
                    videoPlayer.setDataSource(mDataSource, mTitle);
                    //添加TextrueView至播放控件
                    addTextrueViewToView(videoPlayer);
                    VideoPlayerManager.getInstance().addOnPlayerEventListener(videoPlayer);
                    //手动检查播放器内部状态，同步常规播放器状态至新的播放器
                    VideoPlayerManager.getInstance().checkedVidepPlayerState();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 迷你窗口播放器手势处理，允许在屏幕分辨率范围内随手势滚动。上至状态栏，下至虚拟导航栏区域内显示
     */
    private class OnMiniWindownTouchListener implements OnTouchListener{

        private int mStatusBarHeight,mScreenWidth,mScreenHeight;
        //手指在屏幕上的实时X、Y坐标
        private float xInScreen,yInScreen;
        //手指按下此View在屏幕中X、Y坐标
        private float xInView,yInView;
        //播放器View
        private BaseVideoPlayer mView;

        public OnMiniWindownTouchListener(){
            mScreenWidth = VideoUtils.getInstance().getScreenWidth(getContext());
            mScreenHeight = VideoUtils.getInstance().getScreenHeight(getContext());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xInView = event.getX();
                    yInView = event.getY();
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY();
                    float toX = xInScreen - xInView;
                    float toY = yInScreen - yInView;
                    if(null!=mView){
                        if(toX<0){
                            toX=0;
                        }else if(toX>(mScreenWidth-mView.getWidth())){
                            toX=mScreenWidth-mView.getWidth();
                        }
                        if(toY<0){
                            toY=0;
                        }else if(toY>(mScreenHeight-mView.getHeight())){
                            toY=mScreenHeight-mView.getHeight();
                        }
                        Logger.d(TAG,"X:"+xInScreen+",Y:"+yInScreen+",toX:"+toX+",toY:"+toY);
                        mView.setX(toX);
                        mView.setY(toY);
                    }
                    break;
            }
            return true;
        }

        /**
         * 获取状态栏高度
         * @return
         */
        private int getStatusBarHeight() {
            if (mStatusBarHeight == 0) {
                mStatusBarHeight =VideoUtils.getInstance().getStatusBarHeight(getContext());
            }
            return mStatusBarHeight;
        }

        /**
         * 宿主View
         * @param videoPlayer
         */
        public void setAnchorView(BaseVideoPlayer videoPlayer) {
            this.mView=videoPlayer;
        }
    }

    /**
     * 退出迷你小窗口播放
     */
    public void backMiniWindow(){
        AppCompatActivity appCompActivity = VideoUtils.getInstance().getAppCompActivity(getContext());
        if(null!=appCompActivity){
            SCRREN_ORIENTATION=VideoConstants.SCREEN_ORIENTATION_PORTRAIT;
            BaseVideoPlayer miniWindowPlayer = VideoPlayerManager.getInstance().getMiniWindowPlayer();
            //移除全屏播放器的SurfaceView及屏幕窗口的VideoPlayer
            if(null!=miniWindowPlayer){
                //清除底层手势控制的ViewGroup
                if(null!=mMiniTouchViewGroup){
                    mMiniTouchViewGroup.setOnTouchListener(null);
                    miniWindowPlayer.removeView(mMiniTouchViewGroup);
                    mMiniTouchViewGroup=null;
                }
                if(null!=VideoPlayerManager.getInstance().getTextureView()&&null!=miniWindowPlayer.mSurfaceView){
                    miniWindowPlayer.mSurfaceView.removeView(VideoPlayerManager.getInstance().getTextureView());
                }
                miniWindowPlayer.destroy();
                //从窗口移除ViewPlayer
                ViewGroup viewGroup = (ViewGroup) appCompActivity.getWindow().getDecorView();
                View oldTinyVideo = viewGroup.findViewById(R.id.video_mini_window);
                if(null!=oldTinyVideo){
                    viewGroup.removeView(oldTinyVideo);
                }else{
                    viewGroup.removeView(oldTinyVideo);
                }
                VideoPlayerManager.getInstance().setMiniWindowPlayer(null);
            }
            BaseVideoPlayer noimalPlayer = VideoPlayerManager.getInstance().getNoimalPlayer();
            if(null!=noimalPlayer){
                noimalPlayer.setScrrenOrientation(SCRREN_ORIENTATION);
                addTextrueViewToView(noimalPlayer);
                VideoPlayerManager.getInstance().addOnPlayerEventListener(noimalPlayer);
                //手动检查播放器内部状态，同步全屏播放器至常规播放器状态
                VideoPlayerManager.getInstance().checkedVidepPlayerState();
            }
        }
    }

    /**
     * 转向全局的悬浮窗播放,默认起点X,Y轴为=播放器Vide的起始X,Y轴，播放器默认居中显示，宽：屏幕宽度3/4(四分之三)，高：16：9高度
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindown(BaseVideoController windowController,boolean defaultCreatCloseIcon) {
        int screenWidth = VideoUtils.getInstance().getScreenWidth(getContext());
        int screenHeight = VideoUtils.getInstance().getScreenHeight(getContext());
        //playerWidth宽度为屏幕3/4
        int playerWidth = screenWidth/4*3;
        int playerHeight=playerWidth*9/16;
        //startX位于4/1/2的位置
        int startX=screenWidth/4/2;
        int startY=screenHeight/2-playerHeight/2;
        startGlobalWindown(startX,startY,playerWidth,playerHeight, (V) windowController,defaultCreatCloseIcon);
    }

    /**
     * 转向全局的悬浮窗播放
     * @param startX 屏幕X起始轴
     * @param startY 屏幕Y起始轴
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindown(int startX, int startY,V windowController,boolean defaultCreatCloseIcon){
        int screenWidth = VideoUtils.getInstance().getScreenWidth(getContext());
        int playerWidth = screenWidth / 2;
        int playerHeight=playerWidth*9/16;
        startGlobalWindown(startX,startY,playerWidth,playerHeight,windowController,defaultCreatCloseIcon);
    }

    /**
     * 转向全局的悬浮窗播放
     * @param width 播放器宽
     * @param height 播放器高
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindownPlayerSetWH(int width,int height,V windowController,boolean defaultCreatCloseIcon){
        startGlobalWindown(10,10,width,height,windowController,defaultCreatCloseIcon);
    }

    /**
     * 转向全局的悬浮窗播放
     * @param startX 播放器位于屏幕起始X轴
     * @param startY 播放器位于屏幕起始Y轴
     * @param width 播放器宽
     * @param height 播放器高
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindown(int startX, int startY, int width, int height,V windowController,boolean defaultCreatCloseIcon){
        if(!VideoWindowManager.getInstance().isWindowShowing()){
            if(VideoWindowManager.getInstance().checkAlertWindowsPermission(getContext())){
                FrameLayout viewGroup = VideoWindowManager.getInstance().addVideoPlayerToWindow(getContext().getApplicationContext(), startX, startY, width, height);
                if(null!=viewGroup){
                    //释放自身
                    BaseVideoPlayer.this.reset();
                    try {
                        Constructor<? extends BaseVideoPlayer> constructor = BaseVideoPlayer.this.getClass().getConstructor(Context.class);
                        //新实例化窗口
                        BaseVideoPlayer videoPlayer = constructor.newInstance(getContext());
                        //保存悬浮窗口实例
                        VideoPlayerManager.getInstance().setWindownPlayer(videoPlayer);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,Gravity.CENTER);
                        viewGroup.addView(videoPlayer,layoutParams);
                        //如果允许创建一个默认的按钮位于悬浮窗右上角，则创建
                        if(defaultCreatCloseIcon){
                            //添加一个关闭按钮位于播放器右上角
                            ImageView imageView=new ImageView(getContext());
                            imageView.setImageResource(R.drawable.ic_video_tiny_close);
                            FrameLayout.LayoutParams closeParams=new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                            closeParams.gravity=Gravity.RIGHT;
                            int toPxInt = VideoUtils.getInstance().dpToPxInt(getContext(), 8f);
                            closeParams.setMargins(toPxInt,toPxInt,toPxInt,toPxInt);
                            imageView.setLayoutParams(closeParams);
                            imageView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    VideoPlayerManager.getInstance().onReset();
                                }
                            });
                            viewGroup.addView(imageView);
                        }
                        //设置自定义悬浮窗窗口控制器
                        if(null!=windowController){
                            videoPlayer.setVideoController(windowController,false);
                        }else{
                            //设置一个默认的控制器
                            videoPlayer.setVideoController(new VideoWindowController(videoPlayer.getContext()),false);
                        }
                        //更新屏幕方向,这里只更新为窗口模式即可
                        videoPlayer.setScrrenOrientation(VideoConstants.SCREEN_ORIENTATION_WINDOW);
                        //转换为小窗口模式
                        videoPlayer.setWorking(true);
                        //设置基础的配置
                        videoPlayer.setDataSource(mDataSource,mTitle,mVideoID);
                        if(null!=BaseVideoPlayer.this.getTag()){
                            //绑定打开Activity所需参数
                            videoPlayer.setParamsTag((VideoParams) BaseVideoPlayer.this.getTag());
                        }
                        //添加TextrueView至播放控件
                        addTextrueViewToView(videoPlayer);
                        if(null!=videoPlayer.mVideoController){
                            videoPlayer.mVideoController.startGlobalWindow();
                        }
                        VideoPlayerManager.getInstance().addOnPlayerEventListener(videoPlayer);
                        //手动检查播放器内部状态，同步常规播放器状态至新的播放器
                        VideoPlayerManager.getInstance().checkedVidepPlayerState();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse( "package:"+VideoUtils.getInstance().getPackageName(getContext().getApplicationContext())));
                } else {
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package",VideoUtils.getInstance().getPackageName(getContext().getApplicationContext()), null));
                }
                getContext().getApplicationContext().startActivity(intent);
            }
        }
    }

    /**
     * 退出全局悬浮窗口播放
     */
    private BaseVideoPlayer backGlobalWindownToActivity(){
        VideoPlayerManager.getInstance().setContinuePlay(true);
        if(null!=VideoPlayerManager.getInstance().getWindownPlayer()) {
            BaseVideoPlayer windownPlayer = VideoPlayerManager.getInstance().getWindownPlayer();
            if (windownPlayer.isWorking()) {
                windownPlayer.reset();
            }
            VideoWindowManager.getInstance().onDestroy();
            return windownPlayer;
        }
        VideoWindowManager.getInstance().onDestroy();
        return null;
    }

    /**
     * 弹射返回
     */
    public boolean backPressed() {
        //常规播放器下
        if(SCRREN_ORIENTATION==VideoConstants.SCREEN_ORIENTATION_PORTRAIT){
            return true;
        }
        //全屏下
        if(SCRREN_ORIENTATION==VideoConstants.SCREEN_ORIENTATION_FULL){
            backFullScreenWindow();
            return false;
        }
        //小窗口播放下
        if(SCRREN_ORIENTATION==VideoConstants.SCREEN_ORIENTATION_TINY){
            backMiniWindow();
            return false;
        }
        //悬浮窗允许全局播放
        return true;
    }

    /**
     * 此处返回此组件绑定的工作状态
     * @return
     */
    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    /**
     * 播放器状态
     * @return
     */
    private boolean isPlaying() {
        return VideoPlayerManager.getInstance().isPlaying();
    }

    //======================================播放器内部状态回调========================================

    /**
     * 播放器内部状态变化
     * @param playerState 播放器内部状态
     * @param message
     */
    @Override
    public void onVideoPlayerState(final VideoPlayerState playerState, final String message) {
        Logger.d(TAG,"onVideoPlayerState-->"+playerState);
        if(playerState.equals(VideoPlayerState.MUSIC_PLAYER_ERROR)&&!TextUtils.isEmpty(message)){
            Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
        }
        BaseVideoPlayer.this.post(new Runnable() {
            @Override
            public void run() {
                switch (playerState) {
                    //播放器准备中
                    case MUSIC_PLAYER_PREPARE:
                        if(null!=mCoverController&&mCoverController.getVisibility()!=VISIBLE){
                            mCoverController.setVisibility(VISIBLE);
                        }
                        if(null!=mVideoController){
                            mVideoController.readyPlaying();
                        }
                        break;
                    //播放过程缓冲中
                    case MUSIC_PLAYER_BUFFER:
                        if(null!=mCoverController&&mCoverController.getVisibility()!=GONE){
                            mCoverController.setVisibility(GONE);
                        }
                        if(null!=mVideoController){
                            mVideoController.startBuffer();
                        }
                        break;
                    //缓冲结束、准备结束 后的开始播放
                    case MUSIC_PLAYER_START:
                        if(null!=mCoverController&&mCoverController.getVisibility()!=GONE){
                            mCoverController.setVisibility(GONE);
                        }
                        if(null!=mVideoController){
                            mVideoController.play();
                        }
                        break;
                    //恢复播放
                    case MUSIC_PLAYER_PLAY:
                        if(null!=mVideoController){
                            mVideoController.repeatPlay();
                        }
                        break;
                    //移动网络环境下播放
                    case MUSIC_PLAYER_MOBILE:
                        if(null!=mCoverController&&mCoverController.getVisibility()!=VISIBLE){
                            mCoverController.setVisibility(VISIBLE);
                        }
                        if(null!=mVideoController){
                            mVideoController.mobileWorkTips();
                        }
                        break;
                    //暂停
                    case MUSIC_PLAYER_PAUSE:
                        if(null!=mCoverController&&mCoverController.getVisibility()!=GONE){
                            mCoverController.setVisibility(GONE);
                        }
                        if(null!=mVideoController){
                            mVideoController.pause();
                        }
                        //如果是小窗口模式下播放时被被暂停了，直接停止播放,并退出小窗口
                        if(SCRREN_ORIENTATION==VideoConstants.SCREEN_ORIENTATION_TINY){
                            VideoPlayerManager.getInstance().onStop(true);
                        }
                        break;
                    //停止
                    case MUSIC_PLAYER_STOP:
                        isWorking=false;
                        if(null!=mCoverController&&mCoverController.getVisibility()!=VISIBLE){
                            mCoverController.setVisibility(VISIBLE);
                        }
                        if(null!=mVideoController){
                            mVideoController.reset();
                        }
                        VideoWindowManager.getInstance().onDestroy();
                        //停止、结束 播放时，检测当前播放器如果处于非常规状态下，退出全屏、或小窗
                        if(SCRREN_ORIENTATION!=VideoConstants.SCREEN_ORIENTATION_PORTRAIT){
                            backPressed();
                        }
                        break;
                    //失败
                    case MUSIC_PLAYER_ERROR:
                        isWorking=false;
                        if(null!=mVideoController){
                            mVideoController.error(0,message);
                        }
                        if(null!=mCoverController&&mCoverController.getVisibility()!=VISIBLE){
                            mCoverController.setVisibility(VISIBLE);
                        }
                        VideoWindowManager.getInstance().onDestroy();
                        //播放失败，检测当前播放器如果处于非常规状态下，退出全屏、或小窗
                        if(SCRREN_ORIENTATION!=VideoConstants.SCREEN_ORIENTATION_PORTRAIT){
                            backPressed();
                        }
                        break;
                }
                //回调至调用方
                if(null!=mEventListener){
                    mEventListener.onPlayerStatus(playerState);
                }
            }
        });
    }

    /**
     * 播放器准备完成
     * @param totalDurtion 总时长
     */
    @Override
    public void onPrepared(long totalDurtion) {}

    /**
     * 播放器缓冲进度
     * @param percent 百分比
     */
    @Override
    public void onBufferingUpdate(final int percent) {
        if(null!=mVideoController){
            mVideoController.post(new Runnable() {
                @Override
                public void run() {
                    if(null!=mVideoController){
                        mVideoController.onBufferingUpdate(percent);
                    }
                }
            });
        }
    }

    @Override
    public void onInfo(int event, int extra) {}

    /**
     * 播放器地址无效
     */
    @Override
    public void onVideoPathInvalid() {
        if(null!=mVideoController){
            mVideoController.pathInvalid();
        }
    }

    /**
     * 播放器实时进度
     * @param totalDurtion 音频总时间
     * @param currentDurtion 当前播放的位置
     * @param bufferPercent 缓冲进度，从常规默认切换至全屏、小窗时，应该关心此进度
     */
    @Override
    public void onTaskRuntime(final long totalDurtion, final long currentDurtion, final int bufferPercent) {
        if(null!=mVideoController){
            mVideoController.post(new Runnable() {
                @Override
                public void run() {
                    if(null!=mVideoController){
                        mVideoController.onTaskRuntime(totalDurtion,currentDurtion,bufferPercent);
                    }
                    //回调至调用方
                    if(null!=mEventListener){
                        mEventListener.onPlayingPresent(currentDurtion,totalDurtion,bufferPercent);
                    }
                }
            });
        }
    }

    /**
     * 仅释放播放器窗口UI
     */
    public void reset() {
        //先移除存在的TextrueView
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            VideoTextureView textureView = VideoPlayerManager.getInstance().getTextureView();
            if(null!=textureView.getParent()){
                ((ViewGroup) textureView.getParent()).removeView(textureView);
            }
        }
        if(null!=mVideoController){
            mVideoController.reset();
        }
        if(null!=mCoverController){
            mCoverController.setVisibility(VISIBLE);
        }
        setWorking(false);
    }

    /**
     * 销毁
     */
    public void onReset() {
        if(isWorking()){
            VideoPlayerManager.getInstance().onReset();
        }
    }

    /**
     * 仅仅内部销毁，外部组件调用VideoPlayerManager 的 onDestroy()方法销毁播放器
     */
    @Override
    public void destroy() {
        if(null!=mSensorManager&&null!=mOrientationListener){
            mSensorManager.unregisterListener(mOrientationListener);
            mSensorManager=null;mOrientationListener=null;
        }
        mEventListener=null;
    }
}