package com.music.player.lib.listener;

import android.app.Notification;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicPlayModel;
import com.music.player.lib.model.MusicPlayerState;
import com.music.player.lib.model.MusicPlayingChannel;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/5
 * MusicPlayer Listener
 */

public interface MusicPlayerPresenter {
    /**
     * 开始播放任务
     * @param audios 待播放的数据集，对象需要继承BaseaudioInfo
     * @param index 指定要播放的位置 0-data.size()
     */
    void startPlayMusic(List<?> audios,int index);

    /**
     * 开始播放指定位置音频文件，如果内部数据集存在的话，此方法需在调用updateMusicPlayerData之后调用生效
     * @param index 指定的位置 0-data.size()
     */
    void startPlayMusic(int index);

    /**
     * 开始播放，并将此播放对象添加至正在播放的队列顶部
     * @param audioInfo 音频对象
     */
    void addPlayMusicToTop(BaseAudioInfo audioInfo);

    /**
     * 开始、暂停
     */
    void playOrPause();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 恢复播放
     */
    void play();

    /**
     * 设置循环模式
     * @param loop 为true循环播放
     */
    void setLoop(boolean loop);

    /**
     * 继续刚才播放的位置，此方法设计是在特殊场合调用，如音频文件购买付费了，尝试重新播放
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     */
    void continuePlay(String sourcePath);

    /**
     * 继续刚才播放的位置，此方法设计是在特殊场合调用，如音频文件购买付费了，尝试重新播放
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     * @param index 期望重试播放的具体位置
     */
    void continuePlay(String sourcePath,int index);

    /**
     * 播放器内部释放
     */
    void onReset();

    /**
     * 播放器停止工作
     */
    void onStop();

    /**
     * 更新内部播放器的数据集合位置，此方法适合在外部列表已经改变了，需同步至内部播放器
     * @param audios 待播放列表
     * @param index 位置
     */
    void updateMusicPlayerData(List<?> audios, int index);

    /**
     * 设置播放模式
     * @param model 播放模式，参考MusicPlayModel定义
     * @return 成功设置的播放模式
     */
    MusicPlayModel setPlayerModel(MusicPlayModel model);

    /**
     * 返回播放器内部播放模式
     * @return 播放模式
     */
    MusicPlayModel getPlayerModel();

    /**
     * 设置播放器定时关闭的模式
     * @param model 定时关闭模式，参考MusicAlarmModel定义
     * @return 成功设置的定时关闭模式
     */
    MusicAlarmModel setPlayerAlarmModel(MusicAlarmModel model);

    /**
     * 返回播放器定时关闭模式
     * @return 定时关闭模式
     */
    MusicAlarmModel getPlayerAlarmModel();

    /**
     * 跳转至某处播放
     * @param currentTime 时间位置，单位毫秒
     */
    void seekTo(long currentTime);

    /**
     * 播放上一首，内部维持上一首逻辑
     */
    void playLastMusic();

    /**
     * 播放下一首，内部维持下一首逻辑
     */
    void playNextMusic();

    /**
     * 试探上一首的位置，不会启动播放任务
     * @return 上一首的位置
     */
    int playLastIndex();

    /**
     * 试探下一首的位置，不会启动播放任务
     * @return 下一首的位置
     */
    int playNextIndex();

    /**
     * 播放器内部播放状态
     * @return 为true正在播放
     */
    boolean isPlaying();

    /**
     * 返回正在播放的音频总时长
     * @return 单位毫秒
     */
    long getDurtion();

    /**
     * 返回正在播放的音频ID
     * @return 音频ID
     */
    long getCurrentPlayerID();

    /**
     * 返回正在播放的音频对象
     * @return 音频对象
     */
    BaseAudioInfo getCurrentPlayerMusic();

    /**
     * 返回正在播放的音频HASH KEY，此处只是酷狗音乐对象用到
     * @return 音频的HASH KEY
     */
    String getCurrentPlayerHashKey();

    /**
     * 返回内部播放器正在播放的队列
     * @return 播放器持有的播放数据集
     */
    List<?> getCurrentPlayList();

    /**
     * 更改播放器内部正在处理的播放数据来源CHANNEL
     * @param channel 数据来源CHANNEL,详见MusicPlayingChannel定义
     */
    void setPlayingChannel(MusicPlayingChannel channel);

    /**
     * 返回放器内部正在处理的播放数据来源CHANNEL
     * @return 数据来源CHANNEL,详见MusicPlayingChannel定义
     */
    MusicPlayingChannel getPlayingChannel();

    /**
     * 返回播放器内部工作状态
     * @return 播放状态，详见MusicPlayerState定义
     */
    MusicPlayerState getPlayerState();

    /**
     * 检查播放配置，一般在播放器界面回显调用
     */
    void onCheckedPlayerConfig();

    /**
     * 检查播放器内部正在播放的对象，如果正在播放，会回调MusicPlayerEventListener的onPlayMusiconInfo方法
     */
    void onCheckedCurrentPlayTask();

    /**
     * 添加一个播放状态监听器到监听器池子
     * @param listener 实现监听器的对象
     */
    void addOnPlayerEventListener(MusicPlayerEventListener listener);

    /**
     * 从监听器池子中移除一个监听器
     * @param listener 实现监听器的对象
     */
    void removePlayerListener(MusicPlayerEventListener listener);

    /**
     * 清空监听器池子所有的监听器对象
     */
    void removeAllPlayerListener();

    /**
     * 顺序改变播放器播放模式
     */
    void changedPlayerPlayModel();

    /**
     * 创建一个播放器悬浮窗口至Window
     */
    void createMiniJukeboxWindow();

    /**
     * 打开常驻进程，在通知栏创建一个守护通知
     */
    void startServiceForeground();

    /**
     * 打开常驻进程
     * @param notification 通知对象
     */
    void startServiceForeground(Notification notification);

    /**
     * 打开常驻进程
     * @param notification 通知对象
     * @param notificeid 通知ID
     */
    void startServiceForeground(Notification notification,int notificeid);

    /**
     * 结束常驻进程并清除默认通知
     */
    void stopServiceForeground();

    /**
     * 结束常驻进程并指定通知
     * @param notificeid 指定通知ID
     */
    void stopServiceForeground(int notificeid);
}