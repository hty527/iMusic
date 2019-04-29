package com.music.player.lib.manager;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.listener.MusicPlayerPresenter;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicPlayModel;
import com.music.player.lib.model.MusicPlayerConfig;
import com.music.player.lib.model.MusicPlayerState;
import com.music.player.lib.model.MusicPlayingChannel;
import com.music.player.lib.model.MusicWindowStyle;
import com.music.player.lib.service.MusicPlayerBinder;
import com.music.player.lib.service.MusicPlayerService;
import com.music.player.lib.util.Logger;
import java.util.List;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/5.
 * MusicPlayer Manager
 * 此 MusicPlayerManager 持有 MusicPlayerService 的中间代理人 MusicPlayerBinder，通过此管理者达到与
 * MusicPlayerService 交互的目的
 */

public class MusicPlayerManager implements MusicPlayerPresenter {

    private static final String TAG = MusicPlayerManager.class.getSimpleName();
    private static MusicPlayerManager mInstance = null;
    private static MusicSubjectObservable cMMusicSubjectObservable;
    private static MusicPlayerServiceConnection mConnection;
    private static MusicPlayerBinder mBinder;
    //这里是我项目中业务逻辑用到，请忽略
    private boolean reBrowse = false;
    //播放器配置
    private static MusicPlayerConfig mMusicPlayerConfig;
    private static String mActivityClassName = null;

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

    public MusicPlayerManager() {
        cMMusicSubjectObservable = new MusicSubjectObservable();
    }

    /**
     * 绑定服务，Activity初始化后调用
     * @param context Activity上下文
     */
    public void bindService(Context context) {
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
     * 解绑服务
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
    //这里是我项目中业务逻辑用到，请忽略
    public boolean isReBrowse() {
        return reBrowse;
    }
    //这里是我项目中业务逻辑用到，请忽略
    public void setReBrowse(boolean reBrowse) {
        this.reBrowse = reBrowse;
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
    public MusicAlarmModel getDefaultAlarmModel() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.getDefaultAlarmModel();
        }
        return MusicAlarmModel.MUSIC_ALARM_MODEL_0;
    }

    /**
     * 设置默认的闹钟模式
     * @param alarmModel
     * @return 已设置的闹钟模式
     */
    public MusicPlayerManager setDefaultAlarmModel(MusicAlarmModel alarmModel) {
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
    public MusicPlayModel getDefaultPlayModel() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.getDefaultPlayModel();
        }
        return MusicPlayModel.MUSIC_MODEL_LOOP;
    }

    /**
     * 设置默认的播放模式
     * @param playModel
     * @return 自身
     */
    public MusicPlayerManager setDefaultPlayModel(MusicPlayModel playModel) {
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
    public MusicWindowStyle getWindownStyle() {
        if(null!=mMusicPlayerConfig){
            return mMusicPlayerConfig.getWindownStyle();
        }
        return MusicWindowStyle.DEFAULT;
    }

    /**
     * 设置悬浮窗播放器样式
     * @param musicWindowStyle 新的样式，参考MusicWindowStyle定义
     */
    public MusicPlayerManager setWindownStyle(MusicWindowStyle musicWindowStyle) {
        if(null==mMusicPlayerConfig){
            mMusicPlayerConfig=new MusicPlayerConfig();
        }
        mMusicPlayerConfig.setWindownStyle(musicWindowStyle);
        return mInstance;
    }

    /**
     * 开始播放新的音频队列，播放器会替换全新音乐列表
     * @param musicList 待播放的数据集，对象需要继承BaseMediaInfo
     * @param index 指定要播放的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(List<?> musicList, int index) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.startPlayMusic(musicList,index);
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
     * @param mediaInfo 音频对象
     */
    @Override
    public void addPlayMusicToTop(BaseMediaInfo mediaInfo) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.addPlayMusicToTop(mediaInfo);
        }
    }

    /**
     * 开始、暂停播放
     * @return
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
     * @param musicLists 数据集
     * @param index 位置
     */
    @Override
    public void updateMusicPlayerData(List<?> musicLists, int index) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.updateMusicPlayerData(musicLists,index);
        }
    }

    /**
     * 设置播放模式
     * @param model 播放模式，参考MusicPlayModel定义
     * @return 成功设置的播放模式
     */
    @Override
    public MusicPlayModel setPlayerModel(MusicPlayModel model) {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.setPlayerModel(model);
        }
        return null;
    }

    /**
     * 获取播放模式
     * @return 播放器播放模式
     */
    @Override
    public MusicPlayModel getPlayerModel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayerModel();
        }
        return null;
    }

    /**
     * 设置定时模式
     * @param model 定时关闭模式，参考MusicAlarmModel定义
     * @return 成功设置的播放模式
     */
    @Override
    public MusicAlarmModel setPlayerAlarmModel(MusicAlarmModel model) {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.setPlayerAlarmModel(model);
        }
        return null;
    }

    /**
     * 获取定时模式
     * @return 定时关闭模式
     */
    @Override
    public MusicAlarmModel getPlayerAlarmModel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayerAlarmModel();
        }
        return null;
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
    public BaseMediaInfo getCurrentPlayerMusic() {
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
    public void setPlayingChannel(MusicPlayingChannel channel) {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.setPlayingChannel(channel);
        }
    }

    /**
     * 返回播放器内部正在处理的对象来源属性,详见 MusicPlayingChannel 描述
     * @return 播放器内部处理数据集的CHANNEL
     */
    @Override
    public MusicPlayingChannel getPlayingChannel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayingChannel();
        }
        return MusicPlayingChannel.CHANNEL_NET;
    }

    /**
     * 返回播放器内部工作状态
     * @return 详见 MusicPlayerState 定义
     */
    @Override
    public MusicPlayerState getPlayerState() {
        if(null!=mBinder&&mBinder.pingBinder()){
            return mBinder.getPlayerState();
        }
        return null;
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
     * 尝试改变播放模式,只在 单曲、列表循环 两种模式之间切换
     */
    @Override
    public void changedPlayerPlayModel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.changedPlayerPlayModel();
        }
    }

    /**
     * 尝试改变播放模式, 单曲、列表循环、随机 三种模式之间切换
     */
    @Override
    public void changedPlayerPlayFullModel() {
        if(null!=mBinder&&mBinder.pingBinder()){
            mBinder.changedPlayerPlayFullModel();
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
     */
    public void setForegroundOpenActivityClassName(String className) {
        mActivityClassName = className;
    }

    /**
     * 返回点击通知栏后打开的Activity对象绝对路径
     * @return
     */
    public String getForegroundActivityClassName() {
        return mActivityClassName;
    }

    /**
     * MusicPlayer Service Connection
     */
    private class MusicPlayerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (null != service) {
                Logger.d(TAG, "onServiceConnected--->"+name.getClassName()+",BINDER:"+service.pingBinder());
                if(service instanceof MusicPlayerBinder){
                    mBinder = (MusicPlayerBinder) service;
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "onServiceDisconnected--->"+name);
        }
    }

    /**
     * APP销毁时同步销毁
     */
    public void onDestroy(){
        removeObservers();
        removeAllPlayerListener();
        mConnection=null;mBinder=null;cMMusicSubjectObservable=null;mInstance=null;reBrowse=false;
        mActivityClassName=null;mMusicPlayerConfig=null;
    }
}