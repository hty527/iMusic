package com.video.player.lib.base;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.listener.VideoPlayerEventListener;
import com.video.player.lib.manager.MediaPlayerPresenter;
import com.video.player.lib.manager.VideoAudioFocusManager;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.manager.VideoWindowManager;
import com.video.player.lib.utils.Logger;
import com.video.player.lib.utils.VideoUtils;
import com.video.player.lib.view.VideoTextureView;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * IMediaPlayer 视频播放、渲染、交互
 */

public final class IMediaPlayer implements MediaPlayerPresenter, TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "IMediaPlayer";
    private static volatile IMediaPlayer mInstance;
    private static Context mContext;
    private MediaPlayer mMediaPlayer;
    //画面渲染
    private VideoTextureView mTextureView;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    //播放源
    private String mDataSource;
    //音频焦点管理者
    private static VideoAudioFocusManager mAudioFocusManager;
    //播放器组件监听器
    private static VideoPlayerEventListener mOnPlayerEventListeners;
    //内部播放状态
    private static int mMusicPlayerState = VideoConstants.MUSIC_PLAYER_STOP;
    //息屏下WIFI锁
    private static WifiManager.WifiLock mWifiLock;
    //进度计时器
    private PlayTimerTask mPlayTimerTask;
    private Timer mTimer;
    //实时播放位置
    private int mPercentIndex;
    private BaseVideoPlayer mNoimalPlayer,mFullScrrenPlayer, mMiniWindowPlayer,mWindownPlayer;
    //视频宽高
    private int mVideoWidth,mVideoHeight;
    //缓冲进度
    private int mBufferPercent;
    //TASK执行次数计时
    private long TIMER_RUN_COUNT=0;

    public static IMediaPlayer getInstance(){
        if(null==mInstance){
            synchronized (IMediaPlayer.class){
                if(null==mInstance){
                    mInstance=new IMediaPlayer();
                }
            }
        }
        return mInstance;
    }

    private IMediaPlayer(){
        VideoPlayerManager.getInstance().setIMediaPlayer(this);
    }

    /**
     * 画面渲染图层初始化
     * @param textureView 自定义画面渲染器
     */
    public void initTextureView(VideoTextureView textureView) {
        this.mTextureView=textureView;
        textureView.setSurfaceTextureListener(this);
    }

    /**
     * 返回承载画面渲染器
     * @return textureView
     */
    public VideoTextureView getTextureView() {
        return mTextureView;
    }

    /**
     * 清空画面渲染图层
     */
    private void removeTextureView() {
        if(null!=mTextureView){
            mTextureView.setSurfaceTextureListener(null);
            if(null!=mTextureView.getParent()){
                ((ViewGroup) mTextureView.getParent()).removeView(mTextureView);
            }
            mTextureView=null;
        }
        if(null!=mSurface){
            mSurface.release();
            mSurface=null;
        }
        mSurfaceTexture=null;
    }

    /**
     * 保存常规播放器
     * @param videoPlayer 播放器实例
     */
    public void setNoimalPlayer(BaseVideoPlayer videoPlayer) {
        this.mNoimalPlayer=videoPlayer;
    }

    /**
     * 保存全屏窗口播放器
     * @param videoPlayer 播放器实例
     */
    public void setFullScrrenPlayer(BaseVideoPlayer videoPlayer) {
        this.mFullScrrenPlayer=videoPlayer;
    }

    /**
     * 保存小窗口播放器实例
     * @param videoPlayer 播放器实例
     */
    public void setMiniWindowPlayer(BaseVideoPlayer videoPlayer) {
        this.mMiniWindowPlayer =videoPlayer;
    }

    /**
     * 保存悬浮窗窗口实例
     * @param windownPlayer 播放器实例
     */
    public void setWindownPlayer(BaseVideoPlayer windownPlayer) {
        mWindownPlayer = windownPlayer;
    }

    /**
     * 返回常规播放器实例
     * @return 播放器实例
     */
    public BaseVideoPlayer getNoimalPlayer() {
        return mNoimalPlayer;
    }

    /**
     * 返回全屏播放器实例
     * @return 播放器实例
     */
    public BaseVideoPlayer getFullScrrenPlayer() {
        return mFullScrrenPlayer;
    }

    /**
     * 返回小窗口实例
     * @return 播放器实例
     */
    public BaseVideoPlayer getMiniWindowPlayer() {
        return mMiniWindowPlayer;
    }

    /**
     * 返回悬浮窗窗口实例
     * @return 播放器实例
     */
    public BaseVideoPlayer getWindownPlayer() {
        return mWindownPlayer;
    }

    /**
     * 只是释放播放器
     */
    private void reset() {
        try {
            if(null!=mMediaPlayer){
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer=null;
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }finally {
            mBufferPercent=0;
            if(null!=mAudioFocusManager){
                mAudioFocusManager.releaseAudioFocus();
            }
        }
    }

    /**
     * 视频宽高
     * @param mp 播放器实例
     * @param width 视频宽 单位：分辨率
     * @param height 视频高 单位：分辨率
     */
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        this.mVideoWidth=width;
        this.mVideoHeight=height;
        if(null!=mTextureView){
            mTextureView.setVideoSize(width,height);
        }
    }

    /**
     * 开始计时任务
     */
    private void startTimer() {
        if(null==mPlayTimerTask){
            mTimer = new Timer();
            mPlayTimerTask = new PlayTimerTask();
            mTimer.schedule(mPlayTimerTask, 0, 100);
        }
    }

    /**
     * 结束计时任务
     */
    private void stopTimer() {
        if (null != mPlayTimerTask) {
            mPlayTimerTask.cancel();
            mPlayTimerTask = null;
        }
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
        TIMER_RUN_COUNT=0;
    }

    /**
     * 播放进度、闹钟倒计时进度 计时器
     */
    private class PlayTimerTask extends TimerTask {
        @Override
        public void run() {
            TIMER_RUN_COUNT++;
            //播放进度实时回调
            if(null!=mOnPlayerEventListeners){
                if(null!=mMediaPlayer&&mMediaPlayer.isPlaying()){
                    mOnPlayerEventListeners.currentPosition(mMediaPlayer.getDuration(),
                            mMediaPlayer.getCurrentPosition(),mBufferPercent);
                }else{
                    mOnPlayerEventListeners.currentPosition(-1,-1,mBufferPercent);
                }
            }
            //每隔1秒播放进度回调，主要用于更新Text和SEEK BAR
            if(TIMER_RUN_COUNT%10==0){
                if(null!=mOnPlayerEventListeners){
                    if(null!=mMediaPlayer&&mMediaPlayer.isPlaying()){
                        //+500毫秒是因为1秒一次的播放进度回显，格式化分秒后显示有时候到不了终点时间
                        mOnPlayerEventListeners.onTaskRuntime(mMediaPlayer.getDuration(),
                                mMediaPlayer.getCurrentPosition()+500,mBufferPercent);
                    }else{
                        mOnPlayerEventListeners.onTaskRuntime(-1,-1,mBufferPercent);
                    }
                }
            }
        }
    }

    /**
     * 设置循环模式
     * @param loop true:循环播放 false:反之
     * @return 自身实例
     */
    @Override
    public VideoPlayerManager setLoop(boolean loop) {
        if(null!=mMediaPlayer){
            mMediaPlayer.setLooping(loop);
        }
        return VideoPlayerManager.getInstance();
    }

    @Override
    public void setMobileWorkEnable(boolean enable) {}

    /**
     * 注册播放器工作状态监听器
     * @param listener 实现VideoPlayerEventListener的对象
     */
    public void addOnPlayerEventListener(VideoPlayerEventListener listener) {
        this.mOnPlayerEventListeners=listener;
    }

    /**
     * 移除播放器工作状态监听器
     */
    public void removePlayerListener() {
        mOnPlayerEventListeners=null;
    }

    /**
     * 开始异步准备缓冲播放
     * @param dataSource 播放资源地址，支持file、https、http 等协议
     * @param context 全局上下文
     */
    public void startVideoPlayer(String dataSource, Context context) {
        startVideoPlayer(dataSource,context,0);
    }

    /**
     * 开始异步准备缓冲播放
     * @param dataSource 播放资源地址，支持file、https、http 等协议
     * @param context 全局上下文
     * @param percentIndex 尝试从指定位置开始播放
     */
    public void startVideoPlayer(String dataSource, Context context, int percentIndex) {
        if(TextUtils.isEmpty(dataSource)){
            IMediaPlayer.this.mMusicPlayerState= VideoConstants.MUSIC_PLAYER_STOP;
            if(null!=mOnPlayerEventListeners){
                mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,"播放地址为空");
                mOnPlayerEventListeners.onVideoPathInvalid();
            }
            return;
        }
        if(IMediaPlayer.this.mMusicPlayerState==VideoConstants.MUSIC_PLAYER_PREPARE
                &&mDataSource.equals(dataSource)){
            Logger.d(TAG,"startVideoPlayer-->重复调用");
            return;
        }
        this.mPercentIndex=percentIndex;
        this.mDataSource=dataSource;
        if(null!=context){
            this.mContext=context;
            if(null!=mContext){
                mWifiLock = ((WifiManager) mContext.getApplicationContext().getSystemService(
                        Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "MUSIC_LOCK");
            }
        }
        reset();
        if(VideoUtils.getInstance().isCheckNetwork(mContext)){
            if(!VideoUtils.getInstance().isWifiConnected(mContext)&&!VideoPlayerManager.getInstance().isMobileWorkEnable()){
                IMediaPlayer.this.mMusicPlayerState= VideoConstants.MUSIC_PLAYER_MOBILE;
                if(null!=mOnPlayerEventListeners){
                    mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,"正在使用移动网络");
                }
                return;
            }
            startTimer();
            if(null==mAudioFocusManager){
                mAudioFocusManager= new VideoAudioFocusManager(mContext.getApplicationContext());
            }
            int requestAudioFocus = mAudioFocusManager.requestAudioFocus(
                    new VideoAudioFocusManager.OnAudioFocusListener() {

                @Override
                public void onFocusGet() {
                    play();
                }

                @Override
                public void onFocusOut() {
                    pause();
                }

                @Override
                public boolean isPlaying() {
                    return IMediaPlayer.this.isPlaying();
                }
            });

            if(requestAudioFocus== AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_PREPARE;
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnPreparedListener(this);
                    mMediaPlayer.setOnCompletionListener(this);
                    mMediaPlayer.setOnBufferingUpdateListener(this);
                    mMediaPlayer.setOnSeekCompleteListener(this);
                    mMediaPlayer.setOnErrorListener(this);
                    mMediaPlayer.setOnInfoListener(this);
                    mMediaPlayer.setOnVideoSizeChangedListener(this);
                    mMediaPlayer.setLooping(VideoPlayerManager.getInstance().isLoop());
                    mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
                    Class<MediaPlayer> clazz = MediaPlayer.class;
                    Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
                    Logger.e(TAG, "startVideoPlayer-->" + ",PATH:" + mDataSource);
                    method.invoke(mMediaPlayer, mDataSource, null);
                    if (null != mOnPlayerEventListeners) {
                        mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,"播放准备中");
                    }
                    if(null!=mWifiLock){
                        mWifiLock.acquire();
                    }
                    mMediaPlayer.prepareAsync();
                }catch (Exception e){
                    e.printStackTrace();
                    Logger.e(TAG,"startPlay-->Exception--e:"+e.getMessage());
                    IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_ERROR;
                    if (null != mOnPlayerEventListeners) {
                        mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,"播放失败，"+e.getMessage());
                    }
                }
            }else{
                IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_ERROR;
                if (null != mOnPlayerEventListeners) {
                    mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,"未成功获取音频输出焦点");
                }
            }
        }else{
            IMediaPlayer.this.mMusicPlayerState= VideoConstants.MUSIC_PLAYER_STOP;
            if(null!=mOnPlayerEventListeners){
                mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,"网络未连接");
            }
        }
    }

    /**
     * 设置视频画面显示缩放类型,如果正在播放，会立刻生效
     * @param displayType 详见VideoConstants常量定义
     */
    @Override
    public void setVideoDisplayType(int displayType) {
        if(null!=mTextureView){
            mTextureView.setVideoDisplayType(displayType);
        }
    }

    /**
     * 尝试重新播放
     * @param percentIndex 尝试从指定位置重新开始
     */
    public void reStartVideoPlayer(long percentIndex) {
        if(!TextUtils.isEmpty(mDataSource)){
            startVideoPlayer(mDataSource,null, (int) percentIndex);
        }
    }

    /**
     * 返回播放器内部播放状态
     * @return 播放器内部播放状态
     */
    @Override
    public boolean isPlaying() {
        return null!=mMediaPlayer&&(mMusicPlayerState==VideoConstants.MUSIC_PLAYER_PLAY
                || mMusicPlayerState==VideoConstants.MUSIC_PLAYER_START);
    }

    /**
     * 返回播放器内部工作状态
     * @return true:正在工作，包含暂停、缓冲等 false:未工作
     */
    @Override
    public boolean isWorking() {
        return null!=mMediaPlayer&&(mMusicPlayerState==VideoConstants.MUSIC_PLAYER_PREPARE
                || mMusicPlayerState==VideoConstants.MUSIC_PLAYER_BUFFER
                || mMusicPlayerState==VideoConstants.MUSIC_PLAYER_START
                || mMusicPlayerState==VideoConstants.MUSIC_PLAYER_PAUSE
                || mMusicPlayerState==VideoConstants.MUSIC_PLAYER_PLAY);
    }

    /**
     * 返回当前视频宽
     * @return 播放器实例
     */
    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    /**
     * 返回当前视频高
     * @return 播放器实例
     */
    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    /**
     * 开始、暂停 播放
     */
    @Override
    public void playOrPause() {
        switch (getVideoPlayerState()) {
            case VideoConstants.MUSIC_PLAYER_STOP:
                startVideoPlayer(mDataSource,null);
                break;
            case VideoConstants.MUSIC_PLAYER_PREPARE:
                pause();
                break;
            case VideoConstants.MUSIC_PLAYER_BUFFER:
                pause();
                break;
            case VideoConstants.MUSIC_PLAYER_START:
                pause();
                break;
            case VideoConstants.MUSIC_PLAYER_PLAY:
                pause();
                break;
            case VideoConstants.MUSIC_PLAYER_PAUSE:
                if(null!=mAudioFocusManager){
                    mAudioFocusManager.requestAudioFocus(null);
                }
                play();
                break;
            case VideoConstants.MUSIC_PLAYER_ERROR:
                startVideoPlayer(mDataSource,null);
                break;
            case VideoConstants.MUSIC_PLAYER_MOBILE:

                break;
        }
    }

    /**
     * 恢复播放
     */
    @Override
    public void play() {
        if(mMusicPlayerState == VideoConstants.MUSIC_PLAYER_START ||mMusicPlayerState ==
                VideoConstants.MUSIC_PLAYER_PREPARE
                ||mMusicPlayerState==VideoConstants.MUSIC_PLAYER_PLAY){
            return;
        }
        try {
            if(null!=mMediaPlayer){
                mMediaPlayer.start();
            }
        }catch (RuntimeException e){

        }finally {
            if(null!=mMediaPlayer){
                IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_PLAY;
                if (null != mOnPlayerEventListeners) {
                    mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
                }
                startTimer();
            }
        }
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        try {
            if(null!=mMediaPlayer&&mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
        }catch (RuntimeException e){

        }finally {
            stopTimer();
            IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_PAUSE;
            if (null != mOnPlayerEventListeners) {
                mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
            }
        }
    }

    /**
     * 释放、还原播放、监听、渲染等状态
     */
    @Override
    public void onReset() {
        onStop(true);
        //销毁可能存在的悬浮窗
        VideoWindowManager.getInstance().onDestroy();
        if(null!=mWindownPlayer){
            mWindownPlayer.onDestroy();
            mWindownPlayer=null;
        }
    }

    /**
     * 停止播放
     * @param isReset 是否释放播放器
     */
    @Override
    public void onStop(boolean isReset) {
        //如果明确结束一切播放并且当前窗口无播放器则完全停止
        if(!isReset&& VideoWindowManager.getInstance().isWindowShowing()){
            return;
        }
        stopTimer();
        try {
            if(null!=mMediaPlayer){
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer=null;
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }finally {
            mBufferPercent=0;
            removeTextureView();
            if(null!=mAudioFocusManager){
                mAudioFocusManager.releaseAudioFocus();
            }
            //还原UI状态
            IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_STOP;
            if(null!=mOnPlayerEventListeners){
                mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
            }
            VideoWindowManager.getInstance().onDestroy();
            if(null!=mWindownPlayer){
                mWindownPlayer.onDestroy();
                mWindownPlayer=null;
            }
        }
    }

    /**
     * 跳转至指定位置播放
     * @param currentTime 事件位置，单位毫秒
     */
    @Override
    public void seekTo(long currentTime) {
        try {
            if(null!=mMediaPlayer){
                //非暂停状态下置为加载中状态
                if(IMediaPlayer.getInstance().getVideoPlayerState()!=VideoConstants.MUSIC_PLAYER_PAUSE){
                    IMediaPlayer.this.mMusicPlayerState= VideoConstants.MUSIC_PLAYER_SEEK;
                    if(null!=mOnPlayerEventListeners){
                        mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mMediaPlayer.seekTo(currentTime,MediaPlayer.SEEK_CLOSEST);
                }else{
                    mMediaPlayer.seekTo((int) currentTime);
                }
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    /**
     * 返回正在播放的对象时长
     * @return 视频总时长，单位毫秒
     */
    @Override
    public long getDurtion() {
        try {
            if(null!=mMediaPlayer){
                return mMediaPlayer.getDuration();
            }
        }catch (RuntimeException e){

        }
        return 0;
    }

    /**
     * 返回已播放时长
     * @return 已播放的视频长度，单位毫秒
     */
    @Override
    public long getCurrentDurtion() {
        try {
            if(null!=mMediaPlayer){
                return mMediaPlayer.getCurrentPosition();
            }
        }catch (RuntimeException e){

        }
        return 0;
    }

    /**
     * 尝试弹射退出，若当前播放器处于迷你小窗口、全屏窗口下，则只是退出小窗口\全屏至常规窗口播放
     * 若播放器处于常规状态下，则立即销毁播放器，销毁时内部检测了悬浮窗状态，若正在悬浮窗状态下播放，则啥也不做
     * @return 是否可以销毁界面
     */
    @Override
    public boolean isBackPressed() {
        if(null!=mNoimalPlayer){
            boolean backPressed = mNoimalPlayer.backPressed();
            if(backPressed){
                onDestroy();
            }
            return backPressed;
        }
        onDestroy();
        return true;
    }

    /**
     * 尝试弹射退出，若当前播放器处于迷你小窗口、全屏窗口下，则只是退出小窗口\全屏至常规窗口播放
     * 若播放器处于常规状态下，则立即销毁播放器，销毁时内部检测了悬浮窗状态，若正在悬浮窗状态下播放，则啥也不做
     * @param destroy 是否直接销毁，比如说MainActivity返回逻辑还有询问用户是否退出，给定destroy为false，
     *                则只是尝试弹射，并不会去销毁播放器
     * @return 是否可以销毁界面
     */
    @Override
    public boolean isBackPressed(boolean destroy) {
        if(null!=mNoimalPlayer){
            boolean backPressed = mNoimalPlayer.backPressed();
            if(backPressed&&destroy){
                onReset();
            }
            return backPressed;
        }
        if(destroy){
            onReset();
        }
        return true;
    }

    /**
     * 返回播放器内部播放状态
     * @return 内部播放状态
     */
    @Override
    public int getVideoPlayerState() {
        return mMusicPlayerState;
    }

    /**
     * 检查播放器内部状态
     */
    public void checkedVidepPlayerState() {
        if(null!=mOnPlayerEventListeners){
            mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
        }
    }

    @Override
    public void setContinuePlay(boolean continuePlay) {}

    /**
     * 组件处于可见状态
     */
    @Override
    public void onResume() {
        Logger.d(TAG,"onResume");
        if(!TextUtils.isEmpty(mDataSource)&&isWorking()){
            play();
        }
    }

    /**
     * 组件即将处于不可见状态
     */
    @Override
    public void onPause() {
        //如果不是跳转至其它界面衔接播放，则暂停视频播放
        Logger.d(TAG,"onPause");
        if(!VideoPlayerManager.getInstance().isContinuePlay()){
            //悬浮窗播放模式下不需要暂停
            if(!VideoWindowManager.getInstance().isWindowShowing()){
                if(isWorking()){
                    Logger.d(TAG,"onPause");
                    pause();
                }else{
                    Logger.d(TAG,"onPause-->onStop");
                    onStop(true);
                }
            }
        }else{
            VideoPlayerManager.getInstance().setContinuePlay(false);
        }
    }

    /**
     * 对应生命周期调用
     */
    @Override
    public void onDestroy() {
        Logger.d(TAG,"onDestroy");
        //保留悬浮窗口播放器
        if(VideoWindowManager.getInstance().isWindowShowing()){
            return;
        }
        if(null!=mAudioFocusManager){
            mAudioFocusManager.releaseAudioFocus();
            mAudioFocusManager.onDestroy();
            mAudioFocusManager=null;
        }
        onStop(true);
        mContext=null;mDataSource=null;mInstance=null;VideoPlayerManager.getInstance().setContinuePlay(false);
        if(null!=mFullScrrenPlayer){
            mFullScrrenPlayer.onDestroy();
            mFullScrrenPlayer=null;
        }
        if(null!=mNoimalPlayer){
            mNoimalPlayer.onDestroy();
            mNoimalPlayer=null;
        }
        if(null!= mMiniWindowPlayer){
            mMiniWindowPlayer.onDestroy();
            mMiniWindowPlayer =null;
        }
        if(null!=mWindownPlayer){
            mWindownPlayer.onDestroy();
            mWindownPlayer=null;
        }
    }

    //=========================================画面渲染状态===========================================

    /**
     * TextureView准备好了回调
     * @param surface 内部surface
     * @param width TextureView布局宽
     * @param height TextureView布局高
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Logger.d(TAG,"onSurfaceTextureAvailable-->width:"+width+",height:"+height);
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface;
            //prepare();
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    /**
     * TextureView宽高发生变化时回调
     * @param surface 内部surface
     * @param width 新的TextureView布局宽
     * @param height 新的TextureView布局高
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Logger.d(TAG,"onSurfaceTextureSizeChanged-->width:"+width+",height:"+height);
    }

    /**
     * TextureView销毁时回调
     * @param surface 内部surface
     * @return Most applications should return true.
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Logger.d(TAG,"onSurfaceTextureDestroyed");
        return null==mSurfaceTexture;
    }

    /**
     * TextureView刷新时回调
     * @param surface 内部surface
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    //========================================播放器内部状态==========================================

    /**
     * 播放器准备好了
     * @param mp 解码器
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Logger.d(TAG,"onPrepared");
        if(null!=mSurfaceTexture){
            if(null!=mSurface){
                mSurface.release();
                mSurface=null;
            }
            mSurface =new Surface(mSurfaceTexture);
            mp.setSurface(mSurface);
        }
        mp.start();
        if(mPercentIndex>0){
            mp.seekTo(mPercentIndex);
            mPercentIndex=0;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Logger.d(TAG,"onCompletion");
        IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_STOP;
        stopTimer();
        if(null!=mOnPlayerEventListeners){
            mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Logger.d(TAG,"onBufferingUpdate,percent:"+percent);
        this.mBufferPercent=percent;
        if(null!=mOnPlayerEventListeners){
            mOnPlayerEventListeners.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Logger.d(TAG,"onSeekComplete");
        //非用户主动暂停下，处理为恢复播放事件
        if(IMediaPlayer.this.mMusicPlayerState!= VideoConstants.MUSIC_PLAYER_PAUSE){
            startTimer();
            IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_START;
            if(null!=mOnPlayerEventListeners){
                mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stopTimer();
        Logger.d(TAG,"onError,what:"+what+",extra:"+extra);
        IMediaPlayer.this.mMusicPlayerState = VideoConstants.MUSIC_PLAYER_ERROR;
        if(null!=mOnPlayerEventListeners){
            mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int event, int extra) {
        int state =-1;
        if(event==MediaPlayer.MEDIA_INFO_BUFFERING_START){
            state= VideoConstants.MUSIC_PLAYER_BUFFER;
        }else if(event==MediaPlayer.MEDIA_INFO_BUFFERING_END){
            state= VideoConstants.MUSIC_PLAYER_START;
        }else if(event==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
            state= VideoConstants.MUSIC_PLAYER_START;
        }
        if(state>-1){
            IMediaPlayer.this.mMusicPlayerState = state;
            if(null!= mOnPlayerEventListeners){
                mOnPlayerEventListeners.onVideoPlayerState(mMusicPlayerState,null);
            }
        }
        return false;
    }
}