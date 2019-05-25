package com.music.player.lib.manager;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.iinterface.MusicPlayerPresenter;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.listener.MusicPlayerInfoListener;
import com.music.player.lib.model.MusicPlayerConfig;
import com.music.player.lib.service.MusicPlayerBinder;
import com.music.player.lib.service.MusicPlayerService;
import com.music.player.lib.util.MusicUtils;
import java.util.List;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/5.
 * MusicPlayer Manager
 * 此 MusicPlayerManager 持有 MusicPlayerService 的中间代理人 MusicPlayerBinder，通过此管理者达到与
 * MusicPlayerService 交互的目的
 */

public final class MusicPlayerManager implements MusicPlayerPresenter {

    private static volatile MusicPlayerManager mInstance = null;
    private static MusicSubjectObservable cMMusicSubjectObservable;
    private static MusicPlayerServiceConnection mConnection;
    private static MusicPlayerBinder mBinder;
    //播放器配置
    private static MusicPlayerConfig mMusicPlayerConfig;
    private static String mActivityPlayerClassName, mActivityLockClassName;
    //临时存储的变量，防止在初始化时设置监听内部Service还未启动
    private MusicPlayerInfoListener mTempInfoListener;

    public static MusicPlayerManager getInstance() {
        if(null==mInstance){
            synchronized (MusicPlayerManager.class) {
                if (null == mInstance) {
                    mInstance = new MusicPlayerManager();
                }
            }
        }
        return mInstance;
    }

    private MusicPlayerManager(){
        cMMusicSubjectObservable = new MusicSubjectObservable();
    }

    /**
     * 全局初始化
     * @param context ApplicaionContext
     */
    public MusicPlayerManager init(Context context){
        MusicUtils.getInstance().initSharedPreferencesConfig(context);
        return mInstance;
    }

    /**
     * Activity初始化音乐服务组件，Activity中初始化后调用
     * @param context Activity上下文
     */
    public void initialize(Context context) {
        if(null!=context&&context instanceof Activity){
            mConnection = new MusicPlayerServiceConnection();
            Intent intent = new Intent(context, MusicPlayerService.class);
            context.startService(intent);
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            new IllegalStateException("Must pass in Activity type Context!");
        }
    }

    /**
     * 解绑音乐服务组件
     * @param context Activity上下文
     */
    public void unBindService(Context context) {
        if(null!=context&&context instanceof Activity){
            if(null!=mConnection&&null!=mBinder&&mBinder.pingBinder()){
                context.unbindService(mConnection);
            }
            context.stopService(new Intent(context, MusicPlayerService.class));
        }else{
            new IllegalStateException("Must pass in Activity type Context!");
        }
    }

    /**
     * 返回播放器配置
     * @return 播放器当前配置
     */
    public MusicPlayerConfig getMusicPlayerConfig() {
        return mMusicPlayerConfig;
    }

    /**
     * 设定播放器配置
     * @param musicPlayerConfig
     */
    public void setMusicPlayerConfig(MusicPlayerConfig musicPlayerConfig) {
        mMusicPlayerConfig = musicPlayerConfig;
    }

    /**
     * 获取默认的闹钟模式
     * @return 播放器闹钟模式
     */
    public int getDefaultAlarmModel() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.getDefaultAlarmModel();
        }
        return MusicConstants.MUSIC_ALARM_MODEL_0;
    }

    /**
     * 设置默认的闹钟模式
     * @param alarmModel
     * @return 已设置的闹钟模式
     */
    public MusicPlayerManager setDefaultAlarmModel(int alarmModel) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setDefaultAlarmModel(alarmModel);
        return mInstance;
    }

    /**
     * 获取播放模式
     * @return 播放器默认的闹钟模式
     */
    public int getDefaultPlayModel() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.getDefaultPlayModel();
        }
        return MusicConstants.MUSIC_MODEL_LOOP;
    }

    /**
     * 设置默认的播放模式
     * @param playModel
     * @return 自身
     */
    public MusicPlayerManager setDefaultPlayModel(int playModel) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setDefaultPlayModel(playModel);
        return mInstance;
    }

    /**
     * 是否启用前台进程
     * @return 返回场常驻进程开启状态
     */
    public boolean isLockForeground() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.isLockForeground();
        }
        return false;
    }

    /**
     * 是否开启前台进程
     * @param enable true:开启
     */
    public MusicPlayerManager setLockForeground(boolean enable) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setLockForeground(enable);
        return mInstance;
    }

    /**
     * 是否启用悬浮窗自动吸附悬停
     * @return 返回是否启用自动吸附悬停
     */
    public boolean isWindownAutoScrollToEdge() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.isWindownAutoScrollToEdge();
        }
        return false;
    }

    /**
     * 设置悬浮窗是否自动吸附至屏幕边缘
     * @param enable true:开启
     */
    public MusicPlayerManager setWindownAutoScrollToEdge(boolean enable) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setWindownAutoScrollToEdge(enable);
        return mInstance;
    }

    /**
     * 是否启用垃圾桶手势取消悬浮窗
     * @return 返回是否启用垃圾桶
     */
    public boolean isTrashEnable() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.isTrashEnable();
        }
        return false;
    }

    /**
     * 设置垃圾桶手势取消悬浮窗
     * @param enable true:开启
     */
    public MusicPlayerManager setTrashEnable(boolean enable) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setTrashEnable(enable);
        return mInstance;
    }

    /**
     * 是否启用锁屏控制器
     * @return 返回是否启锁屏控制器
     */
    public boolean isScreenOffEnable() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.isScreenOffEnable();
        }
        return false;
    }

    /**
     * 锁屏控制器开关
     * @param enable true:开启
     */
    public MusicPlayerManager setScreenOffEnable(boolean enable) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setScreenOffEnable(enable);
        return mInstance;
    }

    /**
     * 返回悬浮窗播放器样式
     * @return 返回悬浮窗播放器样式
     */
    public int getWindownStyle() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.getWindownStyle();
        }
        return MusicConstants.DEFAULT;
    }

    /**
     * 设置悬浮窗播放器样式
     * @param musicWindowStyle 新的样式，参考MusicConstants定义
     */
    public MusicPlayerManager setWindownStyle(int musicWindowStyle) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setWindownStyle(musicWindowStyle);
        return mInstance;
    }

    /**
     * 开始播放新的音频队列，播放器会替换全新音乐列表
     * @param audios 待播放的数据集，对象需要继承BaseaudioInfo
     * @param index 指定要播放的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(List<?> audios, int index) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.startPlayMusic(audios,index);
        }
    }

    /**
     * 开始播放指定位置音频文件，如果播放列表存在
     * @param index 指定的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(int index) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.startPlayMusic(index);
        }
    }

    /**
     * 开始一个新的播放任务，播放器内部自动将其添加至队列顶部,即插队播放
     * @param audioInfo 音频对象
     */
    @Override
    public void addPlayMusicToTop(BaseAudioInfo audioInfo) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.addPlayMusicToTop(audioInfo);
        }
    }

    /**
     * 开始、暂停播放
     */
    @Override
    public void playOrPause() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.playOrPause();
        }
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.pause();
        }
    }

    /**
     * 开始播放
     */
    @Override
    public void play() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.play();
        }
    }

    /**
     * 是否循环
     * @param loop true:循环
     */
    @Override
    public void setLoop(boolean loop) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.setLoop(loop);
        }
    }

    /**
     * 继续上次播放，此方法在特殊场景下调用，如播放的地址为空后组件端购买、鉴权后需要自动重新播放
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     */
    @Override
    public void continuePlay(String sourcePath) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.continuePlay(sourcePath);
        }
    }

    /**
     * 继续上次播放，此方法在特殊场景下调用，如播放的地址为空后组件端购买、鉴权后需要自动重新播放
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     * @param index 期望重试播放的具体位置
     */
    @Override
    public void continuePlay(String sourcePath,int index) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.continuePlay(sourcePath,index);
        }
    }

    /**
     * 释放
     */
    @Override
    public void onReset() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.onReset();
        }
    }

    /**
     * 停止播放
     */
    @Override
    public void onStop() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.onStop();
        }
    }

    /**
     * 替换播放器内部待播放列表
     * @param audios 待播放列表
     * @param index 位置
     */
    @Override
    public void updateMusicPlayerData(List<?> audios, int index) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.updateMusicPlayerData(audios,index);
        }
    }

    /**
     * 设置播放模式
     * @param model 播放模式，参考MusicConstants定义
     * @return 成功设置的播放模式
     */
    @Override
    public int setPlayerModel(int model) {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.setPlayerModel(model);
        }
        return MusicConstants.MUSIC_MODEL_LOOP;
    }

    /**
     * 获取播放模式
     * @return 播放器播放模式
     */
    @Override
    public int getPlayerModel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayerModel();
        }
        return MusicConstants.MUSIC_MODEL_LOOP;
    }

    /**
     * 设置定时模式
     * @param model 定时关闭模式，参考MusicConstants定义
     * @return 成功设置的播放模式
     */
    @Override
    public int setPlayerAlarmModel(int model) {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.setPlayerAlarmModel(model);
        }
        return MusicConstants.MUSIC_ALARM_MODEL_0;
    }

    /**
     * 获取定时模式
     * @return 定时关闭模式 详见MusicConstants定义
     */
    @Override
    public int getPlayerAlarmModel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayerAlarmModel();
        }
        return MusicConstants.MUSIC_ALARM_MODEL_0;
    }

    /**
     * 尝试跳转至某处缓冲播放
     * @param currentTime 时间位置，单位毫秒
     */
    @Override
    public void seekTo(long currentTime) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.onSeekTo(currentTime);
        }
    }

    /**
     * 播放上一首，播放器内部根据用户设置的播放模式自动处理
     */
    @Override
    public void playLastMusic() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.playLastMusic();
        }
    }

    /**
     * 播放下一首，播放器内部根据用户设置的播放模式自动处理
     */
    @Override
    public void playNextMusic() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.playNextMusic();
        }
    }

    /**
     * 探测上一首的播放位置，播放器内部根据用户设置的播放模式返回合法的播放位置，内部播放器并不会自动开始播放
     * @return 合法的可播放位置
     */
    @Override
    public int playLastIndex() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.playLastIndex();
        }
        return -1;
    }

    /**
     * 探测下一首的播放位置，播放器内部根据用户设置的播放模式返回合法的播放位置，内部播放器并不会自动开始播放
     * @return 合法的可播放位置
     */
    @Override
    public int playNextIndex() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.playNextIndex();
        }
        return -1;
    }

    /**
     * 返回播放器内部工作状态
     * @return 开始准备、缓冲、正在播放等状态为 true，其他为 false
     */
    @Override
    public boolean isPlaying() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.isPlaying();
        }
        return false;
    }

    /**
     * 返回媒体音频对象的总时长
     * @return 单位:毫秒
     */
    @Override
    public long getDurtion() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getDurtion();
        }
        return 0;
    }

    /**
     * 返回当前正在播放的音频对象ID标识
     * @return 音频ID
     */
    @Override
    public long getCurrentPlayerID() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getCurrentPlayerID();
        }
        return 0;
    }

    /**
     * 返回当前正在播放的音频对象
     * @return 音频对象
     */
    @Override
    public BaseAudioInfo getCurrentPlayerMusic() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getCurrentPlayerMusic();
        }
        return null;
    }

    /**
     * 获取播放器正在处理第三方网络歌曲的唯一标识，此hashKey只有搜索的歌曲有此属性
     * @return 唯一标识
     */
    @Override
    public String getCurrentPlayerHashKey() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getCurrentPlayerHashKey();
        }
        return "";
    }

    /**
     * 返回当前正在播放的音频队列
     * @return 音频队列
     */
    @Override
    public List<?> getCurrentPlayList() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getCurrentPlayList();
        }
        return null;
    }

    /**
     * 更新播放器内部正在处理的对象来源属性
     * @param channel 详见 MusicPlayingChannel 定义
     */
    @Override
    public void setPlayingChannel(int channel) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.setPlayingChannel(channel);
        }
    }

    /**
     * 返回播放器内部正在处理的对象来源属性,详见 MusicConstants 描述
     * @return 播放器内部处理数据集的CHANNEL
     */
    @Override
    public int getPlayingChannel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayingChannel();
        }
        return MusicConstants.CHANNEL_NET;
    }

    /**
     * 返回播放器内部工作状态
     * @return 详见 MusicPlayerState 定义
     */
    @Override
    public int getPlayerState() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayerState();
        }
        return 0;
    }

    /**
     * 检查播放器配置
     */
    @Override
    public void onCheckedPlayerConfig() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.onCheckedPlayerConfig();
        }
    }

    /**
     * 检查播放器内部正在处理的音频对象
     * 回调：播放器内部播放状态、播放对象、缓冲进度、音频对象总时长、音频对象已播放时长、定时停止播放的剩余时长
     * 用处：回调至关心的UI组件还原播放状态
     */
    @Override
    public void onCheckedCurrentPlayTask() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.onCheckedCurrentPlayTask();
        }
    }

    /**
     * 添加播放器状态监听器
     * @param listener 实现对象
     */
    @Override
    public void addOnPlayerEventListener(MusicPlayerEventListener listener) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.setOnPlayerEventListener(listener);
        }
    }

    /**
     * 移除播放器状态监听器
     * @param listener 实现对象
     */
    @Override
    public void removePlayerListener(MusicPlayerEventListener listener) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.removePlayerListener(listener);
        }
    }

    /**
     * 移除所有播放器状态监听器
     */
    @Override
    public void removeAllPlayerListener() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.removeAllPlayerListener();
        }
    }

    /**
     * 监听播放器正在处理的对象
     * @param listener 实现监听器的对象
     */
    @Override
    public void setPlayInfoListener(MusicPlayerInfoListener listener) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.setPlayInfoListener(listener);
        }else{
            mTempInfoListener=listener;
        }
    }

    /**
     * 移除监听播放对象事件
     */
    @Override
    public void removePlayInfoListener() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.removePlayInfoListener();
        }
    }

    /**
     * 尝试改变播放模式, 单曲、列表循环、随机 三种模式之间切换
     */
    @Override
    public void changedPlayerPlayModel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.changedPlayerPlayModel();
        }
    }

    /**
     * 创建迷你悬浮播放器窗口，内部已过滤重复创建
     */
    @Override
    public void createMiniJukeboxWindow() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.createMiniJukeboxWindow();
        }
    }

    /**
     * 开启一个默认样式的前台进程
     */
    @Override
    public void startServiceForeground() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.startServiceForeground();
        }
    }

    /**
     * 开启一个前台进程
     * @param notification 自定义前台进程
     */
    @Override
    public void startServiceForeground(Notification notification) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.startServiceForeground(notification);
        }
    }
    /**
     * 开启一个前台进程
     * @param notification 自定义前台进程
     * @param notificeid 通知栏通道ID
     */
    @Override
    public void startServiceForeground(Notification notification, int notificeid) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.startServiceForeground(notification,notificeid);
        }
    }

    /**
     * 关闭前台进程
     */
    @Override
    public void stopServiceForeground() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.stopServiceForeground();
        }
    }

    /**
     * 关闭指定前台进程
     * @param notificeid 前台通知ID
     */
    @Override
    public void stopServiceForeground(int notificeid) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.stopServiceForeground(notificeid);
        }
    }

    /**
     * 添加对播放器状态关心的 内容观察者，轻量级的状态通知，包括但不限于：开始播放、暂停、继续、停止、销毁 等状态
     * @param observer
     */
    public void addObservable(Observer observer) {
        if (null != cMMusicSubjectObservable) {
            cMMusicSubjectObservable.addObserver(observer);
        }
    }

    /**
     * 移除对播放器状态关心的 内容观察者
     * @param observer
     */
    public void removeObserver(Observer observer) {
        if (null != cMMusicSubjectObservable) {
            cMMusicSubjectObservable.deleteObserver(observer);
        }
    }

    /**
     * 移除所有对播放器状态关心的 内容观察者
     */
    public void removeObservers() {
        if (null != cMMusicSubjectObservable) {
            cMMusicSubjectObservable.deleteObservers();
        }
    }

    /**
     * 播放器内部状态刷新
     * @param object
     */
    public void observerUpdata(Object object){
        if (null != cMMusicSubjectObservable) {
            cMMusicSubjectObservable.updataSubjectObserivce(object);
        }
    }

    /**
     * 指定点击通知栏后打开的Activity对象绝对路径
     * @param className
     * @return MusicPlayerManager
     */
    public MusicPlayerManager setMusicPlayerActivityClassName(String className) {
        mActivityPlayerClassName = className;
        return mInstance;
    }

    /**
     * 返回点击通知栏后打开的Activity对象绝对路径
     * @return Anctivity绝对路径
     */
    public String getMusicPlayerActivityClassName() {
        return mActivityPlayerClassName;
    }

    /**
     * 设置锁屏Activity绝对路径
     * @param activityClassName activity绝对路径
     * @return MusicPlayerManager
     */
    public MusicPlayerManager setLockActivityName(String activityClassName){
        mActivityLockClassName=activityClassName;
        return mInstance;
    }

    /**
     * 返回锁屏Activity绝对路径
     * @return activity绝对路径
     */
    public String getLockActivityName(){
        return mActivityLockClassName;
    }

    /**
     * MusicPlayer Service Connection
     */
    private class MusicPlayerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (null != service) {
                if(service instanceof MusicPlayerBinder){
                    mBinder = (MusicPlayerBinder) service;
                    if(null!=mTempInfoListener){
                        mBinder.setPlayInfoListener(mTempInfoListener);
                    }
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    }

    /**
     * APP销毁时同步销毁
     * @param context Activity类型上下文
     */
    public void unInitialize(Activity context){
        unBindService(context);
        removeObservers();
        removeAllPlayerListener();
        //音频悬浮窗口释放
        MusicWindowManager.getInstance().onDestroy();
        mConnection=null;mBinder=null;cMMusicSubjectObservable=null;mInstance=null;
        mActivityPlayerClassName =null;mActivityLockClassName=null;mMusicPlayerConfig=null;
        mTempInfoListener=null;
    }
}