## MusicPlayerManager 常用API预览：
```
    /**
     * 绑定服务
     * @param context
     */
    public void bindService(Context context);

    /**
     * 解绑服务
     * @param context
     */
    public void unBindService(Context context);

    public boolean isReBrowse();

    public void setReBrowse(boolean reBrowse);

    /**
     * 返回播放器配置
     * @return
     */
    public MusicPlayerConfig getMusicPlayerConfig();
    /**
     * 设定播放器配置
     * @param musicPlayerConfig
     */
    public void setMusicPlayerConfig(MusicPlayerConfig musicPlayerConfig);

    /**
     * 获取默认的闹钟模式
     * @return
     */
    public MusicAlarmModel getDefaultAlarmModel();

    /**
     * 设置默认的闹钟模式
     * @param alarmModel
     * @return
     */
    public MusicPlayerManager setDefaultAlarmModel(MusicAlarmModel alarmModel);

    /**
     * 获取播放模式
     * @return
     */
    public MusicPlayModel getDefaultPlayModel();

    /**
     * 设置默认的播放模式
     * @param playModel
     * @return
     */
    public MusicPlayerManager setDefaultPlayModel(MusicPlayModel playModel);

    /**
     * 是否启用前台进程
     * @return
     */
    public boolean isLockForeground();

    /**
     * 是否开启前台进程
     * @param enable
     */
    public MusicPlayerManager setLockForeground(boolean enable);

    /**
     * 是否启用悬浮窗自动吸附悬停
     * @return
     */
    public boolean isWindownAutoScrollToEdge();

    /**
     * 设置悬浮窗是否自动吸附至屏幕边缘
     * @param enable
     */
    public MusicPlayerManager setWindownAutoScrollToEdge(boolean enable);

    /**
     * 是否启用垃圾桶手势取消悬浮窗
     * @return
     */
    public boolean isTrashEnable();

    /**
     * 设置垃圾桶手势取消悬浮窗
     * @param enable
     */
    public MusicPlayerManager setTrashEnable(boolean enable);

    /**
     * 是否启用锁屏控制器
     * @return
     */
    public boolean isScreenOffEnable();

    /**
     * 锁屏控制器开关
     * @param enable
     */
    public MusicPlayerManager setScreenOffEnable(boolean enable);

    /**
     * 返回悬浮窗播放器样式
     * @return
     */
    public MusicWindowStyle getWindownStyle();

    /**
     * 设置悬浮窗播放器样式
     * @param musicWindowStyle
     */
    public MusicPlayerManager setWindownStyle(MusicWindowStyle musicWindowStyle);

    /**
     * 开始播放新的音频队列，播放器会替换全新音乐列表
     * @param musicList 新的音乐列表
     * @param index 期待播放的位置
     */
    @Override
    public void startPlayMusic(List<?> musicList, int index);

    /**
     * 开始播放指定位置音频文件，如果播放列表存在
     * @param index 期望播放的具体位置
     */
    @Override
    public void startPlayMusic(int index);

    /**
     * 开始一个新的播放任务，播放器内部自动将其添加至队列顶部,即插队播放
     * @param mediaInfo 新的音频对象
     */
    @Override
    public void addPlayMusicToTop(BaseMediaInfo mediaInfo);

    /**
     * 开始、暂停播放
     * @return
     */
    @Override
    public void playOrPause();

    /**
     * 暂停播放
     */
    @Override
    public void pause();

    /**
     * 开始播放
     */
    @Override
    public void play();

    /**
     * 是否循环
     * @param loop
     */
    @Override
    public void setLoop(boolean loop);

    /**
     * 继续上次播放，此方法在特殊场景下调用，如播放的地址为空后组件端购买、鉴权后需要自动重新播放
     * @param sourcePath 安全的播放地址
     */
    @Override
    public void continuePlay(String sourcePath);

    /**
     * 继续上次播放，此方法在特殊场景下调用，如播放的地址为空后组件端购买、鉴权后需要自动重新播放
     * @param sourcePath 安全的播放地址
     * @param index 指定需要继续播放的位置
     */
    @Override
    public void continuePlay(String sourcePath,int index);

    /**
     * 释放
     */
    @Override
    public void onReset();

    /**
     * 停止播放
     */
    @Override
    public void onStop();

    /**
     * 替换播放器内部待播放列表
     * @param musicLists 全新的音频列表
     * @param index 需要强制矫正的播放器正在处理的对象位置
     */
    @Override
    public void updateMusicPlayerData(List<?> musicLists, int index);

    /**
     * 设置播放模式
     * @param model 新的播放模式
     */
    @Override
    public MusicPlayModel setPlayerModel(MusicPlayModel model);

    /**
     * 获取播放模式
     * @return
     */
    @Override
    public MusicPlayModel getPlayerModel();

    /**
     * 设置定时模式
     * @param model 新的定时模式
     */
    @Override
    public MusicAlarmModel setPlayerAlarmModel(MusicAlarmModel model);

    /**
     * 获取定时模式
     */
    @Override
    public MusicAlarmModel getPlayerAlarmModel();
    /**
     * 尝试跳转至某处缓冲播放
     * @param currentTime 此值不能大于MediaPlayer的durtion
     */
    @Override
    public void onSeekTo(long currentTime);

    /**
     * 播放上一首，播放器内部根据用户设置的播放模式自动处理
     */
    @Override
    public void playLastMusic();

    /**
     * 播放下一首，播放器内部根据用户设置的播放模式自动处理
     */
    @Override
    public void playNextMusic();

    /**
     * 探测上一首的播放位置，播放器内部根据用户设置的播放模式返回合法的播放位置，内部播放器并不会自动开始播放
     * @return 合法的可播放位置
     */
    @Override
    public int playLastIndex();

    /**
     * 探测下一首的播放位置，播放器内部根据用户设置的播放模式返回合法的播放位置，内部播放器并不会自动开始播放
     * @return 合法的可播放位置
     */
    @Override
    public int playNextIndex();

    /**
     * 返回播放器内部工作状态
     * @return 开始准备、缓冲、正在播放等状态为 true，其他为 false
     */
    @Override
    public boolean isPlaying();

    /**
     * 返回媒体音频对象的总时长
     * @return
     */
    @Override
    public long getDurtion();

    /**
     * 返回当前正在播放的音频对象ID标识
     * @return 音频ID
     */
    @Override
    public long getCurrentPlayerID();

    /**
     * 返回当前正在播放的音频对象
     * @return 音频对象
     */
    @Override
    public BaseMediaInfo getCurrentPlayerMusic();

    /**
     * 获取播放器正在处理第三方网络歌曲的唯一标识，此hashKey只有搜索的歌曲有此属性
     * @return 唯一标识
     */
    @Override
    public String getCurrentPlayerHashKey();

    /**
     * 返回当前正在播放的音频队列
     * @return 音频队列
     */
    @Override
    public List<?> getCurrentPlayList();
    /**
     * 更新播放器内部正在处理的对象来源属性
     * @param channel 详见 MusicPlayingChannel 定义
     */
    @Override
    public void setPlayingChannel(MusicPlayingChannel channel);

    /**
     * 返回播放器内部正在处理的对象来源属性,详见 MusicPlayingChannel 描述
     * @return
     */
    @Override
    public MusicPlayingChannel getPlayingChannel();

    /**
     * 返回播放器内部工作状态
     * @return 详见 MusicPlayerState 定义
     */
    @Override
    public MusicPlayerState getPlayerState();

    /**
     * 检查播放器配置
     */
    @Override
    public void onCheckedPlayerConfig();

    /**
     * 检查播放器内部正在处理的音频对象
     * 回调：播放器内部播放状态、播放对象、缓冲进度、音频对象总时长、音频对象已播放时长、定时停止播放的剩余时长
     * 用处：回调至关心的UI组件还原播放状态
     */
    @Override
    public void onCheckedCurrentPlayTask();

    /**
     * 添加播放器状态监听器
     * @param listener 实现对象
     */
    @Override
    public void addOnPlayerEventListener(MusicPlayerEventListener listener);

    /**
     * 移除播放器状态监听器
     * @param listener 实现对象
     */
    @Override
    public void removePlayerListener(MusicPlayerEventListener listener);

    /**
     * 移除所有播放器状态监听器
     */
    @Override
    public void removeAllPlayerListener();

    /**
     * 尝试改变播放模式,只在 单曲、列表循环 两种模式之间切换
     */
    @Override
    public void changedPlayerPlayModel();

    /**
     * 尝试改变播放模式, 单曲、列表循环、随机 三种模式之间切换
     */
    @Override
    public void changedPlayerPlayFullModel();

    /**
     * 创建迷你悬浮播放器窗口，内部已过滤重复创建
     */
    @Override
    public void createMiniJukeboxWindow();

    /**
     * 开启一个默认样式的前台进程
     */
    @Override
    public void startServiceForeground();

    /**
     * 开启一个前台进程
     * @param notification 自定义前台进程
     */
    @Override
    public void startServiceForeground(Notification notification);

    /**
     * 开启一个前台进程
     * @param notification 自定义前台进程
     * @param notificeid 通知栏通道ID
     */
    @Override
    public void startServiceForeground(Notification notification, int notificeid);

    /**
     * 关闭前台进程
     */
    @Override
    public void stopServiceForeground();

    /**
     * 关闭指定前台进程
     * @param notificeid 前台通知ID
     */
    @Override
    public void stopServiceForeground(int notificeid);

    /**
     * 添加对播放器状态关心的 内容观察者，轻量级的状态通知，包括但不限于：开始播放、暂停、继续、停止、销毁 等状态
     * @param observer
     */
    public void addObservable(Observer observer);

    /**
     * 移除对播放器状态关心的 内容观察者
     * @param observer
     */
    public void removeObserver(Observer observer);;

    /**
     * 移除所有对播放器状态关心的 内容观察者
     */
    public void removeObservers();

    /**
     * 播放器内部状态刷新
     * @param object
     */
    public void observerUpdata(Object object);

    /**
     * 指定点击通知栏后打开的Activity对象绝对路径
     * @param className
     */
    public void setForegroundOpenActivityClassName(String className);

    /**
     * APP销毁时同步销毁
     */
    public void onDestroy();
```