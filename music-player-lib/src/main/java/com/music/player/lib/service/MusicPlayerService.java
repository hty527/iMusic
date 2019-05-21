package com.music.player.lib.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.music.player.lib.R;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.listener.MusicPlayerInfoListener;
import com.music.player.lib.listener.MusicPlayerPresenter;
import com.music.player.lib.manager.MusicAudioFocusManager;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicPlayModel;
import com.music.player.lib.model.MusicPlayerState;
import com.music.player.lib.model.MusicPlayingChannel;
import com.music.player.lib.model.MusicWindowStyle;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicImageCache;
import com.music.player.lib.util.MusicRomUtil;
import com.music.player.lib.util.MusicUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TinyHung@Outlook.com
 * 2018/3/6
 * Music Service
 * 媒体播放器核心类，通过中间代理人 MusicPlayerBinder 与此 Service 交互
 * 此库设计是 MusicPlayerManager 管理者持有 MusicPlayerBinder，遂通过 MusicPlayerManager 与此 Service 交互
 */

public class MusicPlayerService extends Service implements MusicPlayerPresenter,MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    private static final String TAG = "MusicPlayerService";
    //当前正在工作的播放器对象
    private static MediaPlayer mMediaPlayer;
    //Service委托代理人
    private static MusicPlayerBinder mPlayerBinder;
    //组件回调注册池子
    private static List<MusicPlayerEventListener> mOnPlayerEventListeners = new ArrayList<>();
    //播放对象监听
    private static MusicPlayerInfoListener sMusicPlayerInfoListener;
    //音频焦点Manager
    private static MusicAudioFocusManager mAudioFocusManager;
    //待播放音频队列池子
    private static List<Object> mAudios = new ArrayList<>();
    //当前播放播放器正在处理的对象位置
    private static int mCurrentPlayIndex = 0;
    //循环模式
    private static boolean mLoop;
    //用户设定的内部播放器播放模式，默认MusicPlayModel.MUSIC_MODEL_LOOP
    private static MusicPlayModel mPlayModel=MusicPlayerManager.getInstance().getDefaultPlayModel();
    //播放器工作状态
    private static MusicPlayerState mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_STOP;
    //用户设定的闹钟模式,默认:MusicAlarmModel.MUSIC_ALARM_MODEL_0
    private static MusicAlarmModel mMusicAlarmModel=MusicPlayerManager.getInstance().getDefaultAlarmModel();
    //自动停止播放器的剩余时间
    private static long TIMER_DURTION=Long.MAX_VALUE;
    private PlayTimerTask mPlayTimerTask;
    private Timer mTimer;
    //息屏下WIFI锁
    private static WifiManager.WifiLock mWifiLock;
    //监听系统事件的广播
    private static HeadsetBroadcastReceiver mHeadsetBroadcastReceiver;
    //前台进程对象ID
    private static int NOTIFICATION_ID= 10001;
    //播放器内部正在处理的对象所属的数据渠道,默认是来自网络
    private static MusicPlayingChannel mPlayChannel=MusicPlayingChannel.CHANNEL_NET;
    //MediaPlayer的缓冲进度走到100%就不再回调，此变量只纪录当前播放的对象缓冲的进度，方便播放器UI回显时还原缓冲进度
    private int mBufferProgress;
    //秒针计时器换算
    private long RUN_TAG=0;
    //是否被动暂停，用来处理音频焦点失去标记
    private boolean mIsPassive;

    @Override
    public IBinder onBind(Intent intent) {
        if(null==mPlayerBinder){
            mPlayerBinder = new MusicPlayerBinder(MusicPlayerService.this);
        }
        return mPlayerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioFocusManager = new MusicAudioFocusManager(MusicPlayerService.this.getApplicationContext());
        MusicUtils.getInstance().initSharedPreferencesConfig(this.getApplication());
        initPlayerConfig();
        initAlarmConfig();
        mWifiLock = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "MUSIC_LOCK");
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE);
        intentFilter.addAction(MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE);
        mHeadsetBroadcastReceiver = new HeadsetBroadcastReceiver();
        registerReceiver(mHeadsetBroadcastReceiver,intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    /**
     * 初始化播放器配置
     */
    private void initPlayerConfig(){
        String value = MusicUtils.getInstance().getString(MusicConstants.SP_KEY_PLAYER_MODEL,
                MusicConstants.SP_VALUE_MUSIC_MODEL_LOOP);
        MusicPlayModel model = MusicPlayerManager.getInstance().getDefaultPlayModel();
        if(value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_SINGLE)){
            mLoop=true;
            model=MusicPlayModel.MUSIC_MODEL_SINGLE;
        } else if(value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_LOOP)){
            model=MusicPlayModel.MUSIC_MODEL_LOOP;
            mLoop=false;
        } else if(value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_ORDER)){
            model=MusicPlayModel.MUSIC_MODEL_ORDER;
            mLoop=false;
        } else if(value.equals(MusicConstants.SP_VALUE_MUSIC_MODEL_RANDOM)){
            model=MusicPlayModel.MUSIC_MODEL_RANDOM;
            mLoop=false;
        }
        this.mPlayModel=model;
        Logger.d(TAG,"initPlayerConfig--VALUE:"+value+",PLAY_MODEL:"+mPlayModel);
    }

    /**
     * 初始化闹钟档位配置
     */
    private void initAlarmConfig() {
        if(TIMER_DURTION<=0){
            String value = MusicUtils.getInstance().getString(MusicConstants.SP_KEY_ALARM_MODEL,
                    MusicConstants.SP_VALUE_ALARM_MODE_0);
            Logger.d(TAG,"initAlarmConfig--VALUE:"+value);
            setPlayerAlarmModel(getPlayerAlarmModel(value));
        }
    }

    /**
     * 根据配置文件转换设定的闹钟模式
     * @param value
     * @return 当前播放模式
     */
    private MusicAlarmModel getPlayerAlarmModel(String value) {
        if(value.equals(MusicConstants.SP_VALUE_ALARM_MODE_0)){
            return MusicAlarmModel.MUSIC_ALARM_MODEL_0;
        }
        if(value.equals(MusicConstants.SP_VALUE_ALARM_MODE_10)){
            return MusicAlarmModel.MUSIC_ALARM_MODEL_10;
        }
        if(value.equals(MusicConstants.SP_VALUE_ALARM_MODE_15)){
            return MusicAlarmModel.MUSIC_ALARM_MODEL_15;
        }
        if(value.equals(MusicConstants.SP_VALUE_ALARM_MODE_30)){
            return MusicAlarmModel.MUSIC_ALARM_MODEL_30;
        }
        if(value.equals(MusicConstants.SP_VALUE_ALARM_MODE_60)){
            return MusicAlarmModel.MUSIC_ALARM_MODEL_60;
        }
        if(value.equals(MusicConstants.SP_VALUE_ALARM_MODE_CURRENT)){
            return MusicAlarmModel.MUSIC_ALARM_MODEL_CURRENT;
        }
        return MusicPlayerManager.getInstance().getDefaultAlarmModel();
    }

    /**
     * 播放全新的音频列表任务
     * @param musicList 待播放的数据集，对象需要继承BaseaudioInfo
     * @param index 指定要播放的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(List<?> musicList, int index) {
        if(null!=musicList){
            mAudios.clear();
            mAudios.addAll(musicList);
            startPlayMusic(index);
        }
    }

    /**
     * 播放指定位置的音乐
     * @param index 指定的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(int index) {
        if(index<0){
            throw new IndexOutOfBoundsException("start play index must > 0");
        }
        if(null!=mAudios&&mAudios.size()>index){
            this.mCurrentPlayIndex=index;
            BaseAudioInfo baseMusicInfo = (BaseAudioInfo) mAudios.get(index);
            startPlay(baseMusicInfo);
            //检查用户设定的闹钟
            initAlarmConfig();
        }
    }

    /**
     * 开始一个新的播放，将其加入正在播放的队列顶部
     * @param audioInfo 音频对象
     */
    @Override
    public void addPlayMusicToTop(BaseAudioInfo audioInfo) {
        if(null==audioInfo){
            return;
        }
        if(null==mAudios){
            mAudios=new ArrayList<>();
        }
        BaseAudioInfo playerMusic = getCurrentPlayerMusic();
        if(null!=playerMusic&&playerMusic.getAudioId()==audioInfo.getAudioId()){
            return;
        }
        if(mAudios.size()>0){
            int pisition=-1;
            for (int i = 0; i < mAudios.size(); i++) {
                BaseAudioInfo musicInfo = (BaseAudioInfo) mAudios.get(i);
                if(audioInfo.getAudioId()==musicInfo.getAudioId()){
                    pisition=i;
                    break;
                }
            }
            if(pisition>-1){
                onReset();
                mAudios.remove(pisition);
            }
        }
        mAudios.add(0,audioInfo);
        startPlayMusic(0);
    }

    /**
     * 开始、暂停
     */
    @Override
    public synchronized void playOrPause() {
        Logger.d(TAG,"playOrPause--"+getPlayerState());
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerState()) {
                case MUSIC_PLAYER_STOP:
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                case MUSIC_PLAYER_PREPARE:
                    pause();
                    break;
                case MUSIC_PLAYER_BUFFER:
                    pause();
                    break;
                case MUSIC_PLAYER_PLAYING:
                    pause();
                    break;
                case MUSIC_PLAYER_PAUSE:
                    if(null!=mAudioFocusManager){
                        mAudioFocusManager.requestAudioFocus(null);
                    }
                    play();
                    break;
                case MUSIC_PLAYER_ERROR:
                    startPlayMusic(mCurrentPlayIndex);
                    break;
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
            MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_PAUSE;
            if (null != mOnPlayerEventListeners) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
                }
            }
            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_PAUSE));
            //最后更新通知栏
            startServiceForeground();
        }
    }

    /**
     * 被动暂停播放，仅提供给失去焦点时内部调用
     */
    private void passivePause(){
        try {
            if(null!=mMediaPlayer&&mMediaPlayer.isPlaying()){
                MusicPlayerService.this.mIsPassive=true;
                mMediaPlayer.pause();
            }
        }catch (RuntimeException e){

        }finally {
            MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_PAUSE;
            if (null != mOnPlayerEventListeners) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
                }
            }
            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_PAUSE));
            //最后更新通知栏
            startServiceForeground();
        }
    }

    /**
     * 恢复播放
     */
    @Override
    public void play() {
        if(mMusicPlayerState ==MusicPlayerState.MUSIC_PLAYER_PLAYING){
            return;
        }
        try {
            if(null!=mMediaPlayer){
                MusicPlayerService.this.mIsPassive=false;
                mMediaPlayer.start();
                MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_START));
            }else{
                startPlayMusic(mCurrentPlayIndex);
            }
        }catch (RuntimeException e){

        }finally {
            if(null!=mMediaPlayer){
                MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_PLAYING;
                if (null != mOnPlayerEventListeners) {
                    for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                        onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
                    }
                }
                //最后更新通知栏
                startServiceForeground();
            }
        }
    }

    /**
     * 设置循环模式
     * @param loop 为true循环播放
     */
    @Override
    public void setLoop(boolean loop) {
        this.mLoop=loop;
        try {
            if(null!=mMediaPlayer){
                mMediaPlayer.setLooping(loop);
            }
        }catch (RuntimeException e){

        }
    }

    /**
     * 特殊场景调用，如播放对象URL为空，购买或鉴权成功后，再次恢复上一次的播放任务
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     */
    @Override
    public void continuePlay(String sourcePath) {
        if(TextUtils.isEmpty(sourcePath)){
            return;
        }
        if(null!=mAudios&&mAudios.size()>mCurrentPlayIndex){
            ((BaseAudioInfo) mAudios.get(mCurrentPlayIndex)).setAudioPath(sourcePath);
            startPlayMusic(mCurrentPlayIndex);
        }
    }

    /**
     * 特殊场景调用，如播放对象URL为空，购买或鉴权成功后，再次恢复上一次的播放任务
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     * @param index 期望重试播放的具体位置
     */
    @Override
    public void continuePlay(String sourcePath,int index) {
        if(TextUtils.isEmpty(sourcePath)){
            return;
        }
        if(null!=mAudios&&mAudios.size()>index){
            ((BaseAudioInfo) mAudios.get(index)).setAudioPath(sourcePath);
            startPlayMusic(index);
        }
    }

    /**
     * 设置播放器播放模式
     * @param model 播放模式，参考MusicPlayModel定义
     * @return 成功设置的播放模式
     */
    @Override
    public MusicPlayModel setPlayerModel(MusicPlayModel model) {
        this.mPlayModel=model;
        Logger.d(TAG,"setPlayerModel:MODEL:"+mPlayModel);
        mLoop=false;
        if(model.equals(MusicPlayModel.MUSIC_MODEL_SINGLE)){
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_SINGLE);
            mLoop=true;
        }else if(model.equals(MusicPlayModel.MUSIC_MODEL_LOOP)){
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_LOOP);
        }else if(model.equals(MusicPlayModel.MUSIC_MODEL_ORDER)){
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_ORDER);
        }else if(model.equals(MusicPlayModel.MUSIC_MODEL_RANDOM)){
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_RANDOM);
        }
        if(mLoop&&null!=mMediaPlayer){
            mMediaPlayer.setLooping(mLoop);
        }
        return mPlayModel;
    }

    /**
     * 返回播放器播放模式
     * @return 播放模式
     */
    @Override
    public MusicPlayModel getPlayerModel() {
        return mPlayModel;
    }

    /**
     * 定时关闭音乐的闹钟档次设置
     * @param model 定时关闭模式，参考MusicAlarmModel定义
     * @return 成功设置的定时关闭模式
     */
    @Override
    public MusicAlarmModel setPlayerAlarmModel(MusicAlarmModel model) {
        this.mMusicAlarmModel=model;
        switch (model) {
            case MUSIC_ALARM_MODEL_10:
                TIMER_DURTION = 10 * 60;
                MusicUtils.getInstance().putString(MusicConstants.SP_KEY_ALARM_MODEL,
                        MusicConstants.SP_VALUE_ALARM_MODE_10);
                break;
            case MUSIC_ALARM_MODEL_15:
                TIMER_DURTION = 15 * 60;
                MusicUtils.getInstance().putString(MusicConstants.SP_KEY_ALARM_MODEL,
                        MusicConstants.SP_VALUE_ALARM_MODE_15);
                break;
            case MUSIC_ALARM_MODEL_30:
                TIMER_DURTION = 30 * 60;
                MusicUtils.getInstance().putString(MusicConstants.SP_KEY_ALARM_MODEL,
                        MusicConstants.SP_VALUE_ALARM_MODE_30);
                break;
            case MUSIC_ALARM_MODEL_60:
                TIMER_DURTION = 60 * 60;
                MusicUtils.getInstance().putString(MusicConstants.SP_KEY_ALARM_MODEL,
                        MusicConstants.SP_VALUE_ALARM_MODE_60);
                break;
            case MUSIC_ALARM_MODEL_0:
                TIMER_DURTION = Long.MAX_VALUE;
                MusicUtils.getInstance().putString(MusicConstants.SP_KEY_ALARM_MODEL,
                        MusicConstants.SP_VALUE_ALARM_MODE_0);
                break;
            case MUSIC_ALARM_MODEL_CURRENT:
                MusicUtils.getInstance().putString(MusicConstants.SP_KEY_ALARM_MODEL,
                        MusicConstants.SP_VALUE_ALARM_MODE_CURRENT);
                try {
                    if(null!=mMediaPlayer){
                        TIMER_DURTION = (mMediaPlayer.getDuration()-mMediaPlayer.getCurrentPosition()) / 1000;
                    }
                }catch (RuntimeException e){

                }
                break;
        }
        //设定大于60分钟的视为无限制
        Logger.d(TAG,"setPlayerAlarmModel--TIMER_DURTION:"+TIMER_DURTION+",VALUE"+model);
        return model;
    }

    /**
     * 返回闹钟设定模式
     * @return 闹钟模式
     */
    @Override
    public MusicAlarmModel getPlayerAlarmModel() {
        return mMusicAlarmModel;
    }

    /**
     * 跳转到某个位置播放
     * @param currentTime 时间位置，单位毫秒
     */
    @Override
    public void seekTo(long currentTime) {
        try {
            if(null!=mMediaPlayer){
                mMediaPlayer.seekTo((int) currentTime);
            }
        }catch (RuntimeException e){
        }
    }

    /**
     * 播放上一首，内部维持上一首逻辑
     */
    @Override
    public synchronized void playLastMusic() {
        if (TIMER_DURTION<=0) {
            onStop();
            return;
        }
        Logger.d(TAG,"playLastMusic--oldPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //单曲：上一首不为0，上一首，否则循环当前
//                case MUSIC_MODEL_SINGLE:
//                    if(mCurrentPlayIndex-1>-1){
//                        mCurrentPlayIndex--;
//                    }
//                    startPlayMusic(mCurrentPlayIndex);
//                    break;
                //单曲：等同列表循环
                case MUSIC_MODEL_SINGLE:
                    mCurrentPlayIndex--;
                    if(mCurrentPlayIndex<0){
                        mCurrentPlayIndex=mAudios.size()-1;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //列表循环
                case MUSIC_MODEL_LOOP:
                    mCurrentPlayIndex--;
                    if(mCurrentPlayIndex<0){
                        mCurrentPlayIndex=mAudios.size()-1;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //顺序：上一首部位0，上一首，不存在结束播放
                case MUSIC_MODEL_ORDER:
                    if(mCurrentPlayIndex-1>-1){
                        mCurrentPlayIndex--;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //随机
                case MUSIC_MODEL_RANDOM:
                    int index = MusicUtils.getInstance().getRandomNum(0, mAudios.size() - 1);
                    mCurrentPlayIndex=index;
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
            }
        }
        Logger.d(TAG,"playLastMusic--newPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
    }

    /**
     * 播放下一首，内部维持下一首逻辑
     */
    @Override
    public synchronized void playNextMusic() {
        if (TIMER_DURTION<=0) {
            onStop();
            return;
        }
        Logger.d(TAG,"playNextMusic--oldPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //单曲：上一首不为0，上一首，否则循环当前
//                case MUSIC_MODEL_SINGLE:
//                    if(mCurrentPlayIndex<mAudios.size()-1){
//                        mCurrentPlayIndex++;
//                    }
//                    startPlayMusic(mCurrentPlayIndex);
//                    break;
                //单曲：等同列表循环
                case MUSIC_MODEL_SINGLE:
                    if(mCurrentPlayIndex>=mAudios.size()-1){
                        mCurrentPlayIndex=0;
                    }else{
                        mCurrentPlayIndex++;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //列表循环
                case MUSIC_MODEL_LOOP:
                    if(mCurrentPlayIndex>=mAudios.size()-1){
                        mCurrentPlayIndex=0;
                    }else{
                        mCurrentPlayIndex++;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //顺序：下一首存在播放下一首，不存在则结束播放
                case MUSIC_MODEL_ORDER:
                    if(mAudios.size()-1>mCurrentPlayIndex){
                        mCurrentPlayIndex++;
                    }
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //随机
                case MUSIC_MODEL_RANDOM:
                    int index = MusicUtils.getInstance().getRandomNum(0, mAudios.size() - 1);
                    mCurrentPlayIndex=index;
                    postViewHandlerCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
            }
        }
        Logger.d(TAG,"playNextMusic--newPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
    }

    /**
     * 试探上一首的位置，不会启动播放任务
     * @return 上一首的位置
     */
    @Override
    public int playLastIndex() {
        Logger.d(TAG,"playLastIndex--oldPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //单曲：上一首不为0播放上一首，否则循环当前
//                case MUSIC_MODEL_SINGLE:
//                    if(mCurrentPlayIndex-1>-1){
//                        mCurrentPlayIndex--;
//                    }
//                    break;
                //单曲：等同列表循环
                case MUSIC_MODEL_SINGLE:
                    mCurrentPlayIndex--;
                    if(mCurrentPlayIndex<0){
                        mCurrentPlayIndex=mAudios.size()-1;
                    }
                    break;
                //列表循环
                case MUSIC_MODEL_LOOP:
                    mCurrentPlayIndex--;
                    if(mCurrentPlayIndex<0){
                        mCurrentPlayIndex=mAudios.size()-1;
                    }
                    break;
                //顺序：上一首不为0播放上一首，否则结束播放
                case MUSIC_MODEL_ORDER:
                    if((mCurrentPlayIndex-1)>-1){
                        mCurrentPlayIndex--;
                    }
                    break;
                //随机
                case MUSIC_MODEL_RANDOM:
                    int index = MusicUtils.getInstance().getRandomNum(0, mAudios.size() - 1);
                    mCurrentPlayIndex=index;
                    break;
            }
        }
        Logger.d(TAG,"playLastIndex--newPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
        return mCurrentPlayIndex;
    }

    /**
     * 试探下一首的位置，不会启动播放任务
     * @return 下一首的位置
     */
    @Override
    public int playNextIndex() {
        Logger.d(TAG,"playNextIndex--oldPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //单曲：下一首存在，下一首，不存在重复当前播放
//                case MUSIC_MODEL_SINGLE:
//                    if(mCurrentPlayIndex<mAudios.size()-1){
//                        mCurrentPlayIndex++;
//                    }
//                    break;
                //单曲：等同列表循环
                case MUSIC_MODEL_SINGLE:
                    if(mCurrentPlayIndex>=mAudios.size()-1){
                        mCurrentPlayIndex=0;
                    }else{
                        mCurrentPlayIndex++;
                    }
                    break;
                //列表循环
                case MUSIC_MODEL_LOOP:
                    if(mCurrentPlayIndex>=mAudios.size()-1){
                        mCurrentPlayIndex=0;
                    }else{
                        mCurrentPlayIndex++;
                    }
                    break;
                //顺序：下一首存在，下一首，不存在结束播放
                case MUSIC_MODEL_ORDER:
                    if(mAudios.size()-1>mCurrentPlayIndex){
                        mCurrentPlayIndex++;
                    }
                    break;
                //随机
                case MUSIC_MODEL_RANDOM:
                    int index = MusicUtils.getInstance().getRandomNum(0, mAudios.size() - 1);
                    mCurrentPlayIndex=index;
                    break;
            }
        }
        Logger.d(TAG,"playNextIndex--newPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
        return mCurrentPlayIndex;
    }

    /**
     * 播放器内部播放状态
     * @return 为true正在播放
     */
    @Override
    public boolean isPlaying() {
        try {
            return null!=mMediaPlayer&&(mMusicPlayerState.equals(MusicPlayerState.MUSIC_PLAYER_PREPARE)
                    || mMusicPlayerState.equals(MusicPlayerState.MUSIC_PLAYER_PLAYING)
                    || mMusicPlayerState.equals(MusicPlayerState.MUSIC_PLAYER_BUFFER));
        }catch (RuntimeException e){

        }
        return false;
    }

    /**
     * 返回播放器处理的对象的总时长
     * @return 单位毫秒
     */
    @Override
    public long getDurtion() {
        try {
            if(null!=mMediaPlayer&&mMediaPlayer.isPlaying()){
                return mMediaPlayer.getDuration();
            }
        }catch (RuntimeException e){

        }
        return 0;
    }

    /**
     * 返回播放器正在播放的对象，通知播放状态下，当做未开始播放处理
     * @return 音频ID
     */
    @Override
    public long getCurrentPlayerID() {
        if(mMusicPlayerState.equals(MusicPlayerState.MUSIC_PLAYER_STOP)){
            return 0;
        }
        if(null!=mAudios&&mAudios.size()>mCurrentPlayIndex){
            return ((BaseAudioInfo) mAudios.get(mCurrentPlayIndex)).getAudioId();
        }
        return 0;
    }

    /**
     * 返回正在播放的对象，若播放器停止，为空
     * @return 音频对象
     */
    @Override
    public BaseAudioInfo getCurrentPlayerMusic() {
        if(mMusicPlayerState.equals(MusicPlayerState.MUSIC_PLAYER_STOP)){
            return null;
        }
        if(null!=mAudios&&mAudios.size()>mCurrentPlayIndex){
            return (BaseAudioInfo) mAudios.get(mCurrentPlayIndex);
        }
        return null;
    }

    /**
     * 返回正在播放的第三方网络歌曲HASH KEY
     * @return 音频文件HASH KEY
     */
    @Override
    public String getCurrentPlayerHashKey() {
        if(mMusicPlayerState.equals(MusicPlayerState.MUSIC_PLAYER_STOP)){
            return "";
        }
        if(null!=mAudios&&mAudios.size()>mCurrentPlayIndex){
            return ((BaseAudioInfo) mAudios.get(mCurrentPlayIndex)).getAudioHashKey();
        }
        return "";
    }

    /**
     * 获取播放器正在处理的待播放队列
     * @return 播放器内部持有的播放队列
     */
    @Override
    public List<?> getCurrentPlayList() {
        return mAudios;
    }

    /**
     * 绑定播放器正在处理的数据渠道
     * @param channel 参考 MusicPlayingChannel 定义
     */
    @Override
    public void setPlayingChannel(MusicPlayingChannel channel) {
        mPlayChannel=channel;
    }

    /**
     * 返回放器内部正在处理的播放数据来源CHANNEL
     * @return 数据来源CHANNEL,详见MusicPlayingChannel定义
     */
    @Override
    public MusicPlayingChannel getPlayingChannel() {
        return mPlayChannel;
    }

    /**
     * 返回播放器工作状态
     * @return 播放状态，详见MusicPlayerState定义
     */
    @Override
    public MusicPlayerState getPlayerState() {
        return mMusicPlayerState;
    }

    /**
     * 检查播放器配置
     */
    @Override
    public void onCheckedPlayerConfig() {
        if (null != mOnPlayerEventListeners) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onPlayerConfig(mPlayModel,mMusicAlarmModel,false);
            }
        }
    }

    /**
     * 检查播放器内部正在处理的音频对象
     * 回调：播放器内部播放状态、播放对象、缓冲进度、音频对象总时长、音频对象已播放时长、定时停止播放的剩余时长
     * 用处：回调至关心的UI组件还原播放状态
     */
    @Override
    public void onCheckedCurrentPlayTask() {
        if (null != mMediaPlayer && null != mAudios && mAudios.size() > 0) {
            if (null != mOnPlayerEventListeners) {
                BaseAudioInfo musicInfo = (BaseAudioInfo) mAudios.get(mCurrentPlayIndex);
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
                    onPlayerEventListener.onPlayMusiconInfo(musicInfo,mCurrentPlayIndex);
                    if(null!=mMediaPlayer){
                        try {
                            //+500毫秒是因为1秒一次的播放进度回显，格式化分秒后显示有时候到不了终点时间
                            onPlayerEventListener.onTaskRuntime(mMediaPlayer.getDuration(),
                                    mMediaPlayer.getCurrentPosition()+500,TIMER_DURTION,mBufferProgress);
                        }catch (RuntimeException e){
                            e.printStackTrace();
                            onPlayerEventListener.onTaskRuntime(0,0,TIMER_DURTION,mBufferProgress);
                        }
                    }else{
                        onPlayerEventListener.onTaskRuntime(0,0,TIMER_DURTION,mBufferProgress);
                    }
                }
            }
        }
    }

    /**
     * 向监听池中添加一个监听器
     * @param listener 实现监听器的对象
     */
    @Override
    public void addOnPlayerEventListener(MusicPlayerEventListener listener) {
        if(null!= mOnPlayerEventListeners){
            mOnPlayerEventListeners.add(listener);
        }
    }

    /**
     * 从监听池中移除一个监听器
     * @param listener 实现监听器的对象
     */
    @Override
    public void removePlayerListener(MusicPlayerEventListener listener) {
        if(null!= mOnPlayerEventListeners){
            mOnPlayerEventListeners.remove(listener);
        }
    }

    /**
     * 清空监听池
     */
    @Override
    public void removeAllPlayerListener() {
        if(null!= mOnPlayerEventListeners){
            mOnPlayerEventListeners.clear();
        }
    }

    /**
     * 监听播放器正在处理的对象
     * @param listener 实现监听器的对象
     */
    @Override
    public void setPlayInfoListener(MusicPlayerInfoListener listener) {
        MusicPlayerService.this.sMusicPlayerInfoListener=listener;
    }

    /**
     * 移除监听播放对象事件
     */
    @Override
    public void removePlayInfoListener() {
        MusicPlayerService.this.sMusicPlayerInfoListener=null;
    }

    /**
     * 还原MediaPlayer
     */
    @Override
    public void onReset() {
        mBufferProgress=0;
        MusicPlayerService.this.mIsPassive=false;
        try {
            if(null!=mMediaPlayer){
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    @Override
    public void onStop() {
        mBufferProgress=0;
        RUN_TAG=0;
        //还原播放渠道
        setPlayingChannel(MusicPlayingChannel.CHANNEL_NET);
        seekTo(0);
        stopTimer();
        stopServiceForeground();
        //如果用户设定了播放完当前歌曲后自动关闭，停止播放后自动切换闹钟模式至设置的默认状态
        setPlayerAlarmModel(MusicPlayerManager.getInstance().getDefaultAlarmModel());
        initAlarmConfig();
        if(null!=mAudioFocusManager){
            mAudioFocusManager.releaseAudioFocus();
        }
        try {
            if(null!=mMediaPlayer){
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer.reset();
            }
        }catch (RuntimeException e){

        }finally {
            if(null!=mWifiLock){
                mWifiLock.release();
            }
            mMediaPlayer = null;
            MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_STOP;
            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_STOP));
            if(null!= mOnPlayerEventListeners){
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
                }
            }
        }
    }

    /**
     * 播放器内部数据刷新
     * @param audios 数据集
     * @param index 位置
     */
    @Override
    public void updateMusicPlayerData(List<?> audios, int index) {
        if(null!=mAudios){
            mAudios.clear();
            mAudios.addAll(audios);
        }
        mCurrentPlayIndex=index;
    }

    /**
     * 内部状态销毁
     */
    private void distroy(){
        stopTimer();
        if(null!=mAudioFocusManager){
            mAudioFocusManager.releaseAudioFocus();
            mAudioFocusManager=null;
        }
        try {
            if(null!=mMediaPlayer){
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
            }
        }catch (RuntimeException e){

        }finally {
            try {
                if(null!=mWifiLock){
                    mWifiLock.release();
                }
            }catch (RuntimeException e){

            }finally {
                mMediaPlayer = null;
                MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_STOP;
            }
        }
    }

    /**
     * 改变播放模式,调用此方法
     */
    @Override
    public void changedPlayerPlayModel() {
        //列表循环
        if(mPlayModel.equals(MusicPlayModel.MUSIC_MODEL_LOOP)){
            mPlayModel=MusicPlayModel.MUSIC_MODEL_SINGLE;
            mLoop=true;
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_SINGLE);
        //单曲循环
        }else if(mPlayModel.equals(MusicPlayModel.MUSIC_MODEL_SINGLE)){
            mPlayModel=MusicPlayModel.MUSIC_MODEL_RANDOM;
            mLoop=false;
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_RANDOM);
        //随机播放
        }else if(mPlayModel.equals(MusicPlayModel.MUSIC_MODEL_RANDOM)){
            mPlayModel=MusicPlayModel.MUSIC_MODEL_LOOP;
            MusicUtils.getInstance().putString(MusicConstants.SP_KEY_PLAYER_MODEL,
                    MusicConstants.SP_VALUE_MUSIC_MODEL_LOOP);
            mLoop=false;
        }
        try {
            if(null!=mMediaPlayer){
                mMediaPlayer.setLooping(mLoop);
            }
        }catch (RuntimeException e){

        }finally {
            if (null != mOnPlayerEventListeners) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onPlayerConfig(mPlayModel,mMusicAlarmModel,true);
                }
            }
        }
    }

    /**
     * 创建一个默认的播放器悬浮窗窗口
     */
    @Override
    public void createMiniJukeboxWindow() {
        MusicWindowManager.getInstance().createMiniJukeBoxToWindown(getApplicationContext());
    }

    /**
     * 添加一个播放器内部默认的前台通知组件
     */
    @Override
    public synchronized void startServiceForeground() {
        if(MusicPlayerManager.getInstance().isLockForeground()){
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            boolean isOpen = manager.areNotificationsEnabled();
            if(isOpen){
                BaseAudioInfo audioInfo = getCurrentPlayerMusic();
                if(null!=audioInfo){
                    //先准备好歌曲封面Bitmap
                    if(audioInfo.getAudioPath().startsWith("http:")|| audioInfo.getAudioPath().startsWith("https:")){
                        Glide.with(getApplication().getApplicationContext())
                                .load(TextUtils.isEmpty(audioInfo.getAudioCover())?audioInfo.getAvatar():audioInfo.getAudioCover())
                                .asBitmap()
                                .error(R.drawable.ic_music_default_cover)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(new SimpleTarget<Bitmap>(120,120) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        if(null==resource){
                                            resource = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_default_cover);
                                        }
                                        Notification notification = buildNotifyInstance(getCurrentPlayerMusic(),resource);
                                        startServiceForeground(notification,NOTIFICATION_ID);
                                    }
                                });
                    }else{
                        //File
                        Bitmap bitmap;
                        bitmap = MusicImageCache.getInstance().getBitmap(audioInfo.getAudioPath());
                        //缓存为空，获取音频文件自身封面
                        if(null==bitmap){
                            bitmap=MusicImageCache.getInstance().createBitmap(audioInfo.getAudioPath());
                        }
                        //封面为空，使用默认
                        if(null==bitmap){
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_default_cover);
                        }
                        Notification notification = buildNotifyInstance(getCurrentPlayerMusic(),bitmap);
                        startServiceForeground(notification,NOTIFICATION_ID);
                    }
                }
            }
        }
    }

    /**
     * 添加一个前台通知组件
     * @param notification
     */
    @Override
    public void startServiceForeground(Notification notification) {
        startServiceForeground(notification,NOTIFICATION_ID);
    }

    /**
     * 添加一个前台通知组件
     * @param notification
     * @param notificeid 通知ID
     */
    @Override
    public void startServiceForeground(Notification notification, int notificeid) {
        if(MusicPlayerManager.getInstance().isLockForeground()){
            if(null!=notification){
                NOTIFICATION_ID=notificeid;
                startForeground(NOTIFICATION_ID,notification);
            }
        }
    }

    @Override
    public synchronized void stopServiceForeground() {
        stopServiceForeground(NOTIFICATION_ID);
    }

    /**
     * 移除前台服务
     * @param notificeid
     */
    @Override
    public void stopServiceForeground(int notificeid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(notificeid);
        }
    }

    /**
     * 开始计时任务
     */
    private void startTimer() {
        if(null==mPlayTimerTask){
            Logger.d(TAG,"startTimer");
            mTimer = new Timer();
            mPlayTimerTask = new PlayTimerTask();
            //立即执行，1000毫秒循环一次
            mTimer.schedule(mPlayTimerTask, 0, 1000);
        }
    }

    /**
     * 结束计时任务
     */
    private void stopTimer() {
        Logger.d(TAG,"stopTimer");
        if (null != mPlayTimerTask) {
            mPlayTimerTask.cancel();
            mPlayTimerTask = null;
        }
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 播放进度、闹钟倒计时进度 计时器
     */
    private class PlayTimerTask extends TimerTask {
        @Override
        public void run() {
            //过滤无限期
            //Logger.d(TAG,"TimerTask："+TIMER_DURTION);
            if(TIMER_DURTION<=0){
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    if(null!=mMediaPlayer&&mMediaPlayer.isPlaying()){
                        //+500毫秒是因为1秒一次的播放进度回显，格式化分秒后显示有时候到不了终点时间
                        onPlayerEventListener.onTaskRuntime(mMediaPlayer.getDuration(),
                                mMediaPlayer.getCurrentPosition()+500,TIMER_DURTION,mBufferProgress);
                    }else{
                        onPlayerEventListener.onTaskRuntime(-1,-1,TIMER_DURTION,mBufferProgress);
                    }
                }
                onStop();
                return;
            }
            if(null!= mOnPlayerEventListeners){
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    if(null!=mMediaPlayer&&mMediaPlayer.isPlaying()){
                        onPlayerEventListener.onTaskRuntime(mMediaPlayer.getDuration(),
                                mMediaPlayer.getCurrentPosition()+500,TIMER_DURTION,mBufferProgress);
                    }else{
                        onPlayerEventListener.onTaskRuntime(-1,-1,TIMER_DURTION,mBufferProgress);
                    }
                }
            }
            //注意这里的秒数，TASK是每个500毫秒执行一次，此处的秒数应当换算为与秒针等同计时
            if(0==RUN_TAG%2){
                TIMER_DURTION--;
            }
            RUN_TAG++;
        }
    }

    /**
     * 开始播放媒体文件
     * @param musicInfo
     */
    private synchronized void startPlay(BaseAudioInfo musicInfo) {
        onReset();
        if(null!=musicInfo&&!TextUtils.isEmpty(musicInfo.getAudioPath())){
            startTimer();
            if(null==mAudioFocusManager){
                mAudioFocusManager= new MusicAudioFocusManager(MusicPlayerService.this.getApplicationContext());
            }
            MusicPlayerService.this.mIsPassive=false;
            int requestAudioFocus = mAudioFocusManager.requestAudioFocus(new MusicAudioFocusManager.OnAudioFocusListener() {
                /**
                 * 恢复音频输出焦点，这里恢复播放需要和用户调用恢复播放有区别
                 * 因为当用户主动暂停，获取到音频焦点后不应该恢复播放，而是继续保持暂停状态
                 */
                @Override
                public void onFocusGet() {
                    //如果是被动失去焦点的，则继续播放，否则继续保持暂停状态
                    if(mIsPassive){
                        play();
                    }
                }

                /**
                 * 失去音频焦点后暂停播放，这里暂停播放需要和用户主动暂停有区别，做一个标记，配合onResume。
                 * 当获取到音频焦点后，根据onResume根据标识状态看是否需要恢复播放
                 */
                @Override
                public void onFocusOut() {
                    passivePause();
                }

                /**
                 * 返回播放器是否正在播放
                 * @return 为true正在播放
                 */
                @Override
                public boolean isPlaying() {
                    return MusicPlayerService.this.isPlaying();
                }
            });
            MusicPlayerService.this.mMusicPlayerState = MusicPlayerState.MUSIC_PLAYER_PREPARE;
            startServiceForeground();
            if(requestAudioFocus== AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                if(null!=sMusicPlayerInfoListener){
                    sMusicPlayerInfoListener.onPlayMusiconInfo(musicInfo,mCurrentPlayIndex);
                }
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnPreparedListener(this);
                    mMediaPlayer.setOnCompletionListener(this);
                    mMediaPlayer.setOnBufferingUpdateListener(this);
                    mMediaPlayer.setOnSeekCompleteListener(this);
                    mMediaPlayer.setOnErrorListener(this);
                    mMediaPlayer.setOnInfoListener(this);
                    mMediaPlayer.setLooping(mLoop);
                    mMediaPlayer.setWakeMode(MusicPlayerService.this, PowerManager.PARTIAL_WAKE_LOCK);
                    //更新播放状态
                    MusicStatus musicStatus=new MusicStatus();
                    musicStatus.setId(musicInfo.getAudioId());
                    String frontPath=MusicUtils.getInstance().getMusicFrontPath(musicInfo);
                    musicStatus.setCover(frontPath);
                    musicStatus.setTitle(musicInfo.getAudioName());
                    musicStatus.setPlayerStatus(MusicStatus.PLAYER_STATUS_PREPARED);
                    MusicPlayerManager.getInstance().observerUpdata(musicStatus);
                    Class<MediaPlayer> clazz = MediaPlayer.class;
                    Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
                    String path=getPlayPath(musicInfo.getAudioPath());
                    Logger.e(TAG, "startPlay-->: ID:" + musicInfo.getAudioId() + ",TITLE:"
                            + musicInfo.getAudioName() + ",PATH:" + path);
                    method.invoke(mMediaPlayer, path, null);
                    if (null != mOnPlayerEventListeners) {
                        for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                            onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,"播放准备中");
                        }
                    }
                    if(null!=mWifiLock){
                        mWifiLock.acquire();
                    }
                    mMediaPlayer.prepareAsync();
                }catch (Exception e){
                    e.printStackTrace();
                    Logger.e(TAG,"startPlay-->Exception--e:"+e.getMessage());
                    MusicPlayerService.this.mMusicPlayerState = MusicPlayerState.MUSIC_PLAYER_ERROR;
                    if (null != mOnPlayerEventListeners) {
                        for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                            onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,"播放失败，"+e.getMessage());
                        }
                    }
                    MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_STOP,
                            musicInfo.getAudioId()));
                    startServiceForeground();
                }
            }else{
                MusicPlayerService.this.mMusicPlayerState = MusicPlayerState.MUSIC_PLAYER_ERROR;
                if (null != mOnPlayerEventListeners) {
                    for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                        onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,"未成功获取音频输出焦点");
                    }
                }
                MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_STOP,
                        musicInfo.getAudioId()));
                startServiceForeground();
            }
        }else{
            Logger.d(TAG,"startPlay-->Play Url Is Empty");
            MusicPlayerService.this.mMusicPlayerState = MusicPlayerState.MUSIC_PLAYER_ERROR;
            if (null != mOnPlayerEventListeners && mOnPlayerEventListeners.size() > 0) {
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
                    onPlayerEventListener.onMusicPathInvalid(musicInfo,mCurrentPlayIndex);
                }
            }
            stopServiceForeground();
            MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_ERROR,
                    musicInfo.getAudioId()));
        }
    }

    /**
     * 转换播放地址
     * @param filePath
     * @return 真实的播放地址
     */
    private String getPlayPath(String filePath) {
        if(!TextUtils.isEmpty(filePath)){
            if(filePath.startsWith("http:")||filePath.startsWith("https:")){
                return filePath;
            }
            return Uri.parse(filePath).getPath();
        }
        return null;
    }

    /**
     * 播放器内部根据播放模式自动开始下个任务
     */
    private void onCompletionPlay() {
        if (TIMER_DURTION<=0) {
            Logger.d(TAG,"onCompletionPlay-->Time End");
            onStop();
            return;
        }
        Logger.d(TAG,"onCompletionPlay--mCurrentPlayIndex:"+mCurrentPlayIndex+",MODE:"+getPlayerModel());
        if (null != mAudios && mAudios.size() > 0) {
            switch (getPlayerModel()) {
                //单曲
                case MUSIC_MODEL_SINGLE:
                    Logger.d(TAG,"onCompletionPlay--单曲:"+mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //列表循环
                case MUSIC_MODEL_LOOP:
                    if(mCurrentPlayIndex>=mAudios.size()-1){
                        mCurrentPlayIndex=0;
                    }else{
                        mCurrentPlayIndex++;
                    }
                    Logger.d(TAG,"onCompletionPlay--列表循环:"+mCurrentPlayIndex);
                    postEchoCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
                //顺序
                case MUSIC_MODEL_ORDER:
                    if(mAudios.size()-1>mCurrentPlayIndex){
                        mCurrentPlayIndex++;
                        Logger.d(TAG,"onCompletionPlay--顺序:"+mCurrentPlayIndex);
                        postEchoCurrentPosition(mCurrentPlayIndex);
                        startPlayMusic(mCurrentPlayIndex);
                    }
                    break;
                //随机
                case MUSIC_MODEL_RANDOM:
                    mCurrentPlayIndex = MusicUtils.getInstance().getRandomNum(0, mAudios.size() - 1);
                    Logger.d(TAG,"onCompletionPlay--随机:"+mCurrentPlayIndex);
                    postEchoCurrentPosition(mCurrentPlayIndex);
                    startPlayMusic(mCurrentPlayIndex);
                    break;
            }
        }
    }

    /**
     * 上报给UI组件，当前内部自动正在处理的对象位置，只做回显
     * @param mCurrentPlayIndex
     */
    private void postEchoCurrentPosition(int mCurrentPlayIndex) {
        if (null != mOnPlayerEventListeners &&null!=mAudios&&mAudios.size()>mCurrentPlayIndex) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onEchoPlayCurrentIndex((BaseAudioInfo) mAudios.get(mCurrentPlayIndex),mCurrentPlayIndex);
            }
        }
    }

    /**
     * 上报给UI组件，当前内部自动正在处理的对象位置
     * @param currentPlayIndex
     */
    private void postViewHandlerCurrentPosition(int currentPlayIndex) {
        if (null != mOnPlayerEventListeners &&null!=mAudios&&mAudios.size()>currentPlayIndex) {
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onPlayMusiconInfo((BaseAudioInfo) mAudios.get(currentPlayIndex),currentPlayIndex);
            }
        }
    }

    //===========================================播放回调============================================

    /**
     * 缓冲完成后调用,只有在缓冲成功，才是正在播放状态
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Logger.d(TAG, "onPrepared");
        //如果用户设定的是播放完此歌曲自动关闭，过程中切换了歌曲，则更新本歌曲的时长
        if(getPlayerAlarmModel().equals(MusicAlarmModel.MUSIC_ALARM_MODEL_CURRENT)){
            TIMER_DURTION=(mediaPlayer.getDuration()-mediaPlayer.getCurrentPosition())/1000;
        }
        mediaPlayer.start();
        MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_PLAYING;
        if(null!= mOnPlayerEventListeners){
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onPrepared(mediaPlayer.getDuration());
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,"播放中");
            }
        }
        //通知主页，如果关心正在、最近播放的话
        MusicPlayerManager.getInstance().observerUpdata(new MusicStatus());
        //为防止超过定时还在播放
        if (TIMER_DURTION<=0) {
            onStop();
            return;
        }
    }

    /**
     * 播放完成调用
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Logger.d(TAG, "onCompletion:LOOP:"+mLoop+",PLAYER_MODEL:"+mPlayModel);
        MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_STOP;
        if(null!= mOnPlayerEventListeners){
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,"播放完成");
            }
        }
        startServiceForeground();
        //播放完成，根据用户设置的播放模式来自动播放下一首
        onCompletionPlay();
    }

    /**
     * 缓冲进度，MediaPlayer的onBufferingUpdate到了100%就不再回调
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int progress) {
        this.mBufferProgress=progress;
//        if(null!= mOnPlayerEventListeners){
//            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
//                onPlayerEventListener.onBufferingUpdate(progress);
//            }
//        }
    }

    /**
     * 设置进度完成调用
     */
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Logger.d(TAG, "onSeekComplete");
        //用户如果设定了播放完当前歌曲后立即停止播放的话，应该同步剩余时间
        if(mMusicAlarmModel.equals(MusicAlarmModel.MUSIC_ALARM_MODEL_CURRENT)){
            long durtion = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition();
            TIMER_DURTION=(durtion/1000);
        }
        MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_PLAYING;
        if(null!= mOnPlayerEventListeners){
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
            }
        }
    }

    /**
     * 播放失败
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int event, int extra) {
        Logger.d(TAG, "onError--EVENT:"+event + ",EXTRA:"+extra);
        MusicPlayerService.this.mMusicPlayerState =MusicPlayerState.MUSIC_PLAYER_ERROR;
        onReset();
        String content=getErrorMessage(event);
        if(null!= mOnPlayerEventListeners){
            for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,content);
            }
        }
        //简单的更新播放状态
        MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_STOP));
        startServiceForeground();
        //非付费定制模式，自动下一首
        if(!MusicPlayerManager.getInstance().getWindownStyle().equals(MusicWindowStyle.DEFAULT)){
            if (isCheckNetwork()) {
                onCompletionPlay();
            }
        }
        return false;
    }

    private String getErrorMessage(int event) {
        String content="播放失败，未知错误";
        switch (event) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN :
                content="播放失败，未知错误";
                break;
            //收到次错误APP必须重新实例化新的MediaPlayer
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                content="播放器内部错误";
                break;
            //流开始位置错误
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                content="媒体流错误";
                break;
            //IO,超时错误
            case MediaPlayer.MEDIA_ERROR_IO:
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                content="网络连接超时";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                content="请求播放失败：403";
                break;
            case -2147483648:
                content="系统错误";
                break;
        }
        if(!isCheckNetwork()){
            content="设备未连网，请检查网络连接！";
        }
        return content;
    }

    /**
     * 获取音频信息
     */
    @Override
    public boolean onInfo(MediaPlayer mediaPlayer,int event, int extra) {
        Logger.d(TAG, "onInfo--EVENT:" + event + ",EXTRA:" + extra);
        MusicPlayerState state=null;
        if(event==MediaPlayer.MEDIA_INFO_BUFFERING_START){
            state=MusicPlayerState.MUSIC_PLAYER_BUFFER;
        }else if(event==MediaPlayer.MEDIA_INFO_BUFFERING_END){
            state=MusicPlayerState.MUSIC_PLAYER_PLAYING;
        }else if(event==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
            state=MusicPlayerState.MUSIC_PLAYER_PLAYING;
        }
        if(null!=state){
            MusicPlayerService.this.mMusicPlayerState =state;
            if(null!= mOnPlayerEventListeners){
                for (MusicPlayerEventListener onPlayerEventListener : mOnPlayerEventListeners) {
                    onPlayerEventListener.onMusicPlayerState(mMusicPlayerState,null);
                }
            }
        }
        return false;
    }

    /**
     * 获取当前设备是否有网
     * @return 为true标识已联网
     */
    public boolean isCheckNetwork(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        int type = networkInfo.getType();
        if (type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    //=========================================广播监听==============================================

    private class HeadsetBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.d(TAG,"onReceive:action:"+action);
            //耳机拔出
            if(action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
                pause();
            //屏幕点亮
            }else if(action.equals(Intent.ACTION_SCREEN_ON)){
                //用户需要开启锁屏控制才生效
                if(MusicPlayerManager.getInstance().isScreenOffEnable()&&
                        !TextUtils.isEmpty(MusicPlayerManager.getInstance().getLockActivityName())){
                    MusicPlayerState playerState = getPlayerState();
                    if(playerState.equals(MusicPlayerState.MUSIC_PLAYER_PREPARE)
                            ||playerState.equals(MusicPlayerState.MUSIC_PLAYER_PLAYING)){
                        Intent startIntent=new Intent();
                        startIntent.setClassName(getPackageName(),MusicPlayerManager.getInstance().getLockActivityName());
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getApplicationContext().startActivity(startIntent);
                    }
                }
            //前台进程-通知栏根点击事件
            }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW)){
                if(intent.getLongExtra(MusicConstants.MUSIC_KEY_MEDIA_ID,0)>0){
                    if(!TextUtils.isEmpty(MusicPlayerManager.getInstance().getMusicPlayerActivityClassName())){
                        Intent startIntent=new Intent();
                        startIntent.setClassName(getPackageName(),MusicPlayerManager.getInstance().getMusicPlayerActivityClassName());
                        startIntent.putExtra(MusicConstants.KEY_MUSIC_ID, intent.getLongExtra(MusicConstants.MUSIC_KEY_MEDIA_ID,0));
                        //如果播放器组件未启用，创建新的实例
                        //如果播放器组件已启用且在栈顶，复用播放器不传递任何意图
                        //反之则清除播放器之上的所有栈，让播放器组件显示在最顶层
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(startIntent);
                    }
                }
            //前台进程-上一首
            }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST)){
                MusicPlayerManager.getInstance().playLastMusic();
            //前台进程-下一首
            }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT)){
                MusicPlayerManager.getInstance().playNextMusic();
            //前台进程-暂停、开始
            }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE)){
                MusicPlayerManager.getInstance().playOrPause();
            //前台进程-关闭前台进程
            }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE)){
                MusicPlayerManager.getInstance().stopServiceForeground();
            }
        }
    }

    //=========================================前台服务==============================================

    /**
     * 构建一个前台进程通知
     * @param audioInfo 播放器正在处理的多媒体对象
     * @param resource 封面
     * @return 通知对象
     */
    private Notification buildNotifyInstance(BaseAudioInfo audioInfo, Bitmap resource) {
        if(null==audioInfo){
            return null;
        }
        final NotificationCompat.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setNotificationChannelID(MusicConstants.CHANNEL_ID);
            builder= new NotificationCompat.Builder(MusicPlayerService.this,MusicConstants.CHANNEL_ID);
        }else{
            builder=new NotificationCompat.Builder(MusicPlayerService.this);
        }
        //默认布局
        RemoteViews defaultremoteviews = new RemoteViews(getPackageName(), R.layout.music_notify_default_controller);
        defaultremoteviews.setImageViewBitmap(R.id.music_notice_def_cover, resource);
            defaultremoteviews.setImageViewResource(R.id.music_notice_def_btn_pause,getPauseIcon(getPlayerState()));
            defaultremoteviews.setTextViewText(R.id.music_notice_def_title, audioInfo.getAudioName());
            defaultremoteviews.setTextViewText(R.id.music_notice_def_subtitle, audioInfo.getNickname());
            //通知栏根点击意图
            Intent clickIntent = new Intent(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW);
            clickIntent.putExtra(MusicConstants.MUSIC_KEY_MEDIA_ID,audioInfo.getAudioId());
            PendingIntent pendClickIntent = PendingIntent.getBroadcast(this, 1,
                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //上一首
            defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_last,
                    getClickPending(MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST));
            //下一首
            defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_next,
                    getClickPending(MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT));
            //暂停、开始
            defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_pause,
                    getClickPending(MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE));
            //关闭
            defaultremoteviews.setOnClickPendingIntent(R.id.music_notice_def_btn_close,
                    getClickPending(MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE));
            //大样式布局
//        RemoteViews bigRemoteViews=new RemoteViews(getPackageName(),R.layout.music_notify_big_controller);
            builder.setContent(defaultremoteviews)
                    .setContentIntent(pendClickIntent)
                    .setWhen(System.currentTimeMillis())
                    .setTicker("正在播放")
                    .setOngoing(true)//禁止滑动删除
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_music_push);
            if(MusicRomUtil.getInstance().isMiui()){
                builder.setFullScreenIntent(pendClickIntent,false);//禁用悬挂
            }else{
                builder.setFullScreenIntent(null,false);//禁用悬挂
            }
        Notification notify = builder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        return notify;
    }

    /**
     * 生成待处理广播意图
     * @param action 事件
     * @return 点击意图
     */
    private PendingIntent getClickPending(String action) {
        Intent lastIntent = new Intent(action);
        PendingIntent lastPendIntent = PendingIntent.getBroadcast(MusicPlayerService.this,
                1, lastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return lastPendIntent;
    }

    /**
     * 兼容Android 8.0的群组channel设置
     * @param channelID 通道ID
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setNotificationChannelID(String channelID) {
        NotificationManager  notificationManager = (android.app.NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelID, "COM_IMUSIC_MEDIA_PLAYER",
                android.app.NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setSound(null,null);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 通知栏按钮状态
     * @return
     * @param playerState
     */
    private int getPauseIcon(MusicPlayerState playerState) {
        switch (playerState) {
            case MUSIC_PLAYER_PREPARE:
            case MUSIC_PLAYER_PLAYING:
            case MUSIC_PLAYER_BUFFER:
                return R.drawable.ic_music_mini_pause_noimal;
            case MUSIC_PLAYER_STOP:
            case MUSIC_PLAYER_ERROR:
                return R.drawable.ic_music_mini_play_noimal;
        }
        return R.drawable.ic_music_mini_play_noimal;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG,"onDestroy");
        distroy();
        if(null!=mHeadsetBroadcastReceiver){
            unregisterReceiver(mHeadsetBroadcastReceiver);
            mHeadsetBroadcastReceiver=null;
        }
        MusicPlayerManager.getInstance().observerUpdata(new MusicStatus(MusicStatus.PLAYER_STATUS_DESTROY));
        stopServiceForeground();
        mWifiLock=null;mIsPassive=false;
        mAudioFocusManager=null;
        if(null!= mOnPlayerEventListeners){
            mOnPlayerEventListeners.clear();
        }
        if(null!=mAudios){
            mAudios.clear();
        }
        if(null!=mAudioFocusManager){
            mAudioFocusManager.onDestroy();
            mAudioFocusManager=null;
        }
    }
}