[MusicPlayerAPI]: https://yuye584312311.github.io/pages.github.io/JavaDoc/music-player-lib/javadoc/index.html "MusicPlayerAPI"
* 有关MusicPlayer全部API文档，请阅读[MusicPlayerAPI]<br/>
![MusicPlayerFrame](https://github.com/Yuye584312311/iMusic/blob/master/Doc/screenshot/music_player.png)

### 一、iMusic完整功能权限
```
    <!--若开启垃圾桶回收播放器、悬浮窗口播放、常驻内存、状态栏控制、锁屏播放控制、耳机监控 等功能，请开启已下权限-->
    <uses-permission android:name="android.permission.VIBRATE" />
    <protected-broadcast android:name="android.intent.action.MEDIA_MOUNTED" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <!--APP后台防杀死-->
    <uses-permission android:name="android.permission.INSTANT_APP_FOREGROUND_SERVICE"/>
```
### 二、音乐播放器更多功能初始化设置及自定义保存播放记录
```
    //音乐播放器配置
    MusicPlayerConfig config=MusicPlayerConfig.Build()
            //设置用户未更改定时关闭模式时 默认的闹钟定时关闭模式，内部默认为MusicConstants.MUSIC_ALARM_MODEL_0
            .setDefaultAlarmModel(MusicConstants.MUSIC_ALARM_MODEL_0)
            //设置用户未更改播放模式时 默认的循环模式，内部默认为MusicConstants.MUSIC_MODEL_LOOP
            .setDefaultPlayModel(MusicConstants.MUSIC_MODEL_LOOP);

    //音乐播放器初始化
    MusicPlayerManager.getInstance()
            //内部存储初始化
            .init(getApplicationContext())
            //应用播放器配置
            .setMusicPlayerConfig(config)
            //通知栏交互，默认开启
            .setNotificationEnable(true)
            //常驻进程开关，默认开启
            .setLockForeground(true)
            //设置点击通知栏跳转的播放器界面,需开启常驻进程开关
            .setPlayerActivityName(MusicPlayerActivity.class.getCanonicalName())
            //设置锁屏界面，如果禁用，不需要设置或者设置为null
            .setLockActivityName(MusicLockActivity.class.getCanonicalName())
            //监听播放状态
            .setPlayInfoListener(new MusicPlayerInfoListener() {
                //此处自行存储播放记录
                @Override
                public void onPlayMusiconInfo(BaseAudioInfo musicInfo, int position) {
                    SqlLiteCacheManager.getInstance().insertHistroyAudio(musicInfo);
                }
            })
            //重载方法，初始化音频媒体服务,成功之后如果系统还在播放音乐，则创建一个悬浮窗承载播放器
            .initialize(MainActivity.this, new MusicInitializeCallBack() {

                @Override
                public void onFinish() {
                    //如果系统正在播放音乐
                    if(null!=MusicPlayerManager.getInstance().getCurrentPlayerMusic()){
                        MusicPlayerManager.getInstance().createWindowJukebox();
                    }
                }
            });
```
### 三、音乐播放器主界面UI和自定义锁屏、自定义通知栏实现
#### 1. 自定义播放器界面UI
    iMusic工程实现了一套近乎完整的播放器工程，内置自定义唱片机交互UI，播放器Activity是MusicPlayerActivity类，请参照该类实现自己的UI效果。
```
    //注册播放器状态监听器，根据丰富的回调方法实现自己的UI，注意：所有回调方法并不保证都在主线程抛出
    MusicPlayerManager.getInstance().addOnPlayerEventListener(this);
```
#### 2. 自定义锁屏界面
    iMusic实现了一套示例的锁屏播放界面交互(MusicLockActivity)，如果需要自定义锁屏界面，在开始播放前，设置锁屏Activity绝对路径即可。
```
    //2设置自己实现的锁屏Activity绝对路径，有关属性设置，请参考iMusic中的MusicLockActivity
    MusicPlayerManager.getInstance().setLockActivityName(MusicLockActivity.class.getCanonicalName());
```
#### 3. 如何使用自定义通知栏?
    播放器内部实现了一套通知栏UI样式以及交互功能。如果需要自行定义UI样式或者交互功能，请禁用内部通知功能并自行设置通知栏。步骤如下：<br/>

##### 3.1 初始化时禁用通知栏
```
    //禁用内部通知栏功能
    MusicPlayerManager.getInstance().setNotificationEnable(false);

```
##### 3.2 在收到播放状态回调时，设置自己的通知栏样式。
```
    //1：先注册播放事件监听
    MusicPlayerManager.getInstance().addOnPlayerEventListener(this);

    //2：在onMusicPlayerState回调中创建并显示你的通知栏，构建好自己的通知栏后调用如下方法显示出来，或者自行显示
    MusicPlayerManager.getInstance().startNotification(Notification notification);

    //构造Notification示例代码:

    /**
     * 构建一个前台进程通知
     * @param audioInfo 播放器正在处理的多媒体对象
     * @param cover 封面
     * @return 通知对象
     */
    private Notification buildNotifyInstance(BaseAudioInfo audioInfo, Bitmap cover) {
        if(null==audioInfo){
            return null;
        }
        //8.0及以上系统需创建通知通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(MusicConstants.CHANNEL_ID,
                    "COM_IMUSIC_MEDIA_PLAYER", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            getNotificationManager().createNotificationChannel(channel);
        }
        //通知栏根部点击意图
        Intent rootIntent = new Intent(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW);
        rootIntent.putExtra(MusicConstants.MUSIC_KEY_MEDIA_ID,audioInfo.getAudioId());
        PendingIntent pendClickIntent = PendingIntent.getBroadcast(this, 1,
                rootIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String appName = getString(R.string.music_text_now_play);
        //默认布局
        RemoteViews defaultRemoteViews= getDefaultCoustomRemoteView(audioInfo,cover);
        //扩展布局
        RemoteViews bigRemoteViews = getBigCoustomRemoteView(audioInfo,cover);
        //构造通知栏
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pendClickIntent)
                .setTicker(appName)
                .setSmallIcon(R.drawable.ic_music_push)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCustomContentView(defaultRemoteViews)
                .setCustomBigContentView(bigRemoteViews)
                .setChannelId(MusicConstants.CHANNEL_ID)
                .setPriority(Notification.PRIORITY_HIGH);
        if(MusicRomUtil.getInstance().isMiui()){
            builder.setFullScreenIntent(pendClickIntent,false);//禁用悬挂
        }else{
            builder.setFullScreenIntent(null,false);//禁用悬挂
        }
        return builder.build();
    }
```
##### 3.3 交互处理
```
    //1：在构建通知栏时，设定好广播意图
    ImageButton button;
    button.setOnClickPendingIntent(R.id.xxx.xxx, getClickPending("你的自定义意图"));

    //2：注册广播
    此处省略若干行代码。。。注册“你的自定义意图”到广播中

    //3：在监听到广播的地方处理你的通知栏点击意图，示例代码：
        String action = intent.getAction();
        Logger.d(TAG,"onReceive:action:"+action);
        //通知栏点击Action
        if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST)){
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
        //收藏
        }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_COLLECT)){
             SqlLiteCacheManager.getInstance().insertCollectAudio(MusicPlayerManager.getInstance().getCurrentPlayerMusic());
        }

```
### 四、播放器内部协调工作说明
```
     MusicPlayerService：内部播放器服务组件，负责音频的播放、暂停、停止、上一首、下一首、闹钟定时关闭等工作。
     MusicPlayerActivity：音乐播放器交互示例容器，负责用户交互。
     MusicPlayerManager：内部播放器代理人，组件与播放器交互需经此代理人访问播放器内部功能。
     MusicJukeBoxView：默认自定义唱片机。
     MusicJukeBoxBackgroundLayout：默认自定义唱片机背景Layout。
     MusicJukeBoxCoverPager：默认唱片机封面。
     MusicAlarmSettingDialog：默认定制闹钟设置。
     MusicPlayerListDialog：默认当前正在播放的列表
```
### 五、本地音乐与网络音频兼容
```
    播放器完美支持本地音乐及网络音乐的兼容播放和音频封面显示兼容，如果本地音乐对象的未指定音频封面，则播放器内部将尝试获取音频自带封面作为唱片机封面显示和背景渐变图层显示。
```
### 六、付费购买逻辑
```
  一般付费音频播放前，播放地址是为空的，播放器内部将抛出onMusicPathInvalid(BaseaudioInfo musicInfo, int position);事件，你可在此方法中处理购买付费逻辑，
  待获取到真实播放地址后，再调用下面方法继续尝试播放。也可以自行处理完购买逻辑后再开始调用播放音频事件。
```
```
    //调用此代码继续尝试播放。
    MusicPlayerManager.getInstance().continuePlay(String sourcePath);
```
### 七、常驻进程播放
```
    在开始播放前，初始化设置时设置MusicPlayerManager.getInstance().setLockForeground(true);为true即可。前提是没有禁用内部通知功能。和用户没有禁用软件通知功能。
```
### 八、播放器歌词
    内部封装了播放器歌词显示及交互控件MusicLrcView，支持常规属性设置，内部提供了默认的本地、网络歌词解析器，当然你也可以完全自定义你自己的歌词解析器。
自定义解析器需继承MusicLrcRowParser类，重写内部两个重要的方法实现自己的逻辑即可。
```
    默认歌词解析器支持歌词结构示例：

    [ti:只要平凡]
    [ar:张杰&张碧晨]
    [00:03.18]作词：未知
    [00:17.73]歌词内容1xxx
    [00:17.73][00:17.73][00:17.73]歌词内容2xxx
    [00:26]歌词内容3xxx
    [00:26]歌词内容4xxx
    [00:26][00:26]歌词内容5xxx

    //歌词控件自定义属性：

    <declare-styleable name="MusicLrcView">
        <!--当歌词为空的提示-->
        <attr name="musicLrcEmptyTips" format="string"></attr>
        <!--歌词文本颜色-->
        <attr name="musicLrcTextColor" format="color"></attr>
        <!--歌词文本大小-->
        <attr name="musicLrcTextSize" format="dimension"></attr>
        <!--歌词高亮文本颜色-->
        <attr name="musicLrcLightTextColor" format="color"></attr>
        <!--歌词高亮文本大小-->
        <attr name="musicLrcLightTextSize" format="dimension"></attr>
        <!--歌词之间的的高度-->
        <attr name="musicLrcLineHeight" format="dimension"></attr>
        <!--时间字体颜色-->
        <attr name="musicLrcTimeTextColor" format="color"></attr>
        <!--时间字体大小-->
        <attr name="musicLrcTimeTextSize" format="dimension"></attr>
        <!--手指拖动歌词底部线条颜色-->
        <attr name="musicLrcBottomLineColor" format="color"></attr>
    </declare-styleable>
```
 ___
### 九、MusicPlayerManager 常用API预览及说明：
```
    /**
     * Activity初始化音乐服务组件，Activity中初始化后调用
     * @param context Activity上下文
     */
    public void initialize(Context context);

    /**
     * Activity初始化音乐服务组件，Activity中初始化后调用
     * @param context Activity上下文
     * @param callBack 初始化成功回调，可用于处理重开APP是否及时回显播放状态
     */
    public void initialize(Context context,MusicInitializeCallBack callBack);

    /**
     * APP销毁时同步注销
     * @param context Activity类型上下文
     */
    public void unInitialize(Activity context);

    /**
     * APP销毁时同步注销
     * @param context Activity类型上下文
     * @param destroy 是否同步注销内部服务组件，true:注销服务结束播放
     */
    public void unInitialize(Activity context,boolean destroy);

    /**
     * 设置默认的闹钟模式
     * @param alarmModel
     * @return 已设置的闹钟模式
     */
    public MusicPlayerManager setDefaultAlarmModel(int alarmModel);

    /**
     * 设置默认的播放模式
     * @param playModel
     * @return 自身
     */
    public MusicPlayerManager setDefaultPlayModel(int playModel);

    /**
     * 设定播放器配置
     * @param musicPlayerConfig
     */
    public MusicPlayerManager setMusicPlayerConfig(MusicPlayerConfig musicPlayerConfig);

    /**
     * 是否开启默认的通知栏交互，默认开启状态
     * @param enable enable true：开启默认的通知栏交互
     * @return MusicPlayerManager
     */
    MusicPlayerManager setNotificationEnable(boolean enable);

    /**
     * 是否开启前台进程，默认开启状态
     * @param enable true：开启前台进程（通知栏）
     * @return MusicPlayerManager
     */
    @Override
    public MusicPlayerManager setLockForeground(boolean enable);

    /**
     * 指定点击通知栏后打开的Activity对象绝对路径
     * @param className 绝对路径，跳转入参Key：MusicConstants.KEY_MUSIC_ID,LongExtra类型
     * @return MusicPlayerManager
     */
    @Override
    public MusicPlayerManager setPlayerActivityName(String className);

    /**
     * 设置锁屏Activity绝对路径
     * @param activityClassName activity绝对路径
     * @return MusicPlayerManager
     */
    @Override
    public MusicPlayerManager setLockActivityName(String activityClassName);

    /**
     * 开始播放新的音频队列，播放器会替换全新音乐列表
     * @param audios 待播放的数据集，对象需要继承BaseaudioInfo
     * @param index 指定要播放的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(List<?> audios, int index);

    /**
     * 开始播放指定位置音频文件，如果播放列表存在
     * @param index 指定的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(int index);

    /**
     * 开始一个新的播放任务，播放器内部自动将其添加至队列顶部,即插队播放
     * @param audioInfo 音频对象
     */
    @Override
    public void addPlayMusicToTop(BaseAudioInfo audioInfo);

    /**
     * 开始、暂停播放
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
     * @param loop true:循环
     */
    @Override
    public void setLoop(boolean loop) ;

    /**
     * 继续上次播放，此方法在特殊场景下调用，如播放的地址为空后组件端购买、鉴权后需要自动重新播放
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     */
    @Override
    public void continuePlay(String sourcePath);

    /**
     * 继续上次播放，此方法在特殊场景下调用，如播放的地址为空后组件端购买、鉴权后需要自动重新播放
     * @param sourcePath 音频文件的绝对地址，支持本地、网络、两种协议
     * @param index 期望重试播放的具体位置
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
     * @param audios 待播放列表
     * @param index 位置
     */
    @Override
    public void updateMusicPlayerData(List<?> audios, int index);

    /**
     * 设置播放模式
     * @param model 播放模式，参考MusicConstants定义
     * @return 成功设置的播放模式
     */
    @Override
    public int setPlayerModel(int model);

    /**
     * 获取播放模式
     * @return 播放器播放模式
     */
    @Override
    public int getPlayerModel();

    /**
     * 设置定时模式
     * @param model 定时关闭模式，参考MusicConstants定义
     * @return 成功设置的播放模式
     */
    @Override
    public int setPlayerAlarmModel(int model);

    /**
     * 获取定时模式
     * @return 定时关闭模式 详见MusicConstants定义
     */
    @Override
    public int getPlayerAlarmModel();

    /**
     * 尝试跳转至某处缓冲播放
     * @param currentTime 时间位置，单位毫秒
     */
    @Override
    public void seekTo(long currentTime);

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
     * @return 单位:毫秒
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
    public BaseAudioInfo getCurrentPlayerMusic();

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
    public void setPlayingChannel(int channel);

    /**
     * 返回播放器内部正在处理的对象来源属性,详见 MusicConstants 描述
     * @return 播放器内部处理数据集的CHANNEL
     */
    @Override
    public int getPlayingChannel();

    /**
     * 返回播放器内部工作状态
     * @return 详见 MusicPlayerState 定义
     */
    @Override
    public int getPlayerState();

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
     * 监听播放器正在处理的对象
     * @param listener 实现监听器的对象
     */
    @Override
    public void setPlayInfoListener(MusicPlayerInfoListener listener);

    /**
     * 移除监听播放对象事件
     */
    @Override
    public void removePlayInfoListener();

    /**
     * 尝试改变播放模式, 单曲、列表循环、随机 三种模式之间切换
     */
    @Override
    public void changedPlayerPlayModel();

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
     * 打开常驻进程，在通知栏创建一个守护通知
     */
    void startNotification();

    /**
     * 打开常驻进程
     * @param notification 通知对象
     */
    void startNotification(Notification notification);

    /**
     * 打开常驻进程
     * @param notification 通知对象
     * @param notificeid 通知ID
     */
    void startNotification(Notification notification,int notificeid);

    /**
     * 更新通知栏，一般在使用内部默认的通知栏时，收藏了音频后调用
     */
    void updateNotification();

    /**
     * 结束常驻进程并清除默认通知
     */
    void cleanNotification();

    /**
     * 添加对播放器状态关心的 内容观察者，轻量级的状态通知，包括但不限于：开始播放、暂停、继续、停止、销毁 等状态
     * @param observer
     */
    public void addObservable(Observer observer);

    /**
     * 移除对播放器状态关心的 内容观察者
     * @param observer
     */
    public void removeObserver(Observer observer);

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
     * @return MusicPlayerManager
     */
    public MusicPlayerManager setMusicPlayerActivityClassName(String className);

    /**
     * 创建一个窗口播放器
     */
    public void createWindowJukebox();
```