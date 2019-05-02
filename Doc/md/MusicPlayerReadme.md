# **音乐播放器Wiki**
### 一、APP后台防杀死和更多权限
设置MusicPlayerManager.getInstance().setLockForeground(true)即可实现APP后台防杀死。
```
    <!--更多权限，若开启垃圾桶回收播放器、悬浮窗口播放、常驻内存、状态栏控制、锁屏播放控制、耳机监控 等功能，请开启已下权限-->
    <uses-permission android:name="android.permission.VIBRATE" />
    <protected-broadcast android:name="android.intent.action.MEDIA_MOUNTED" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <!--APP后台防杀死-->
    <uses-permission android:name="android.permission.INSTANT_APP_FOREGROUND_SERVICE"/>
```
### 二、音乐播放器更多功能初始化设置
```
    //若需要实现播放器内部的悬浮窗播放按钮，则需监听悬浮窗单机事件
    MusicWindowManager.getInstance().setOnMusicWindowClickListener(new MusicWindowClickListener() {

        @Override
        public void onWindownClick(View view, long musicID) {
            if(musicID>0){
                Intent intent=new Intent(getApplicationContext(), MusicPlayerActivity.class);
                intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        }

        @Override
        public void onWindownCancel(View view) {}
    });

    MusicPlayerConfig config=MusicPlayerConfig.Build()
        //是否启用前台服务、常驻进程
        .setLockForeground(true)
        //是否启用手指拖动悬浮窗松手时悬浮窗自动靠边悬浮吸附
        .setWindownAutoScrollToEdge(true)
        //是否启用垃圾桶回收播放器
        .setTrashEnable(true)
        //是否启用锁屏控制播放
        .setScreenOffEnable(true)
        //悬浮窗样式：垃圾桶回收样式，默认时点击悬浮窗右上角X按钮回收
        .setWindownStyle(MusicWindowStyle.TRASH);
    //设置给媒体播放管理者
    MusicPlayerManager.getInstance().setMusicPlayerConfig(config);
    //配置点击通知栏跳转至Activity的绝对路径，若支持点击通知栏跳转至播放器界面，则必须设置！！
    MusicPlayerManager.getInstance().setForegroundOpenActivityClassName(MusicPlayerActivity.class.getCanonicalName());
```
### 三、音乐播放器主界面UI实现
```
    iMusic工程实现了一套近乎完整的播放器工程，暂时没打算支持歌词显示。实现播放器交互UI，可以参考MusicPlayerActivity类的实现。
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
  一般付费音频播放前，播放地址是为空的，播放器内部将抛出onMusicPathInvalid(BaseMediaInfo musicInfo, int position);事件，你可在此方法中处理购买付费逻辑，
  待获取到真实播放地址后，再调用下面方法继续尝试播放。也可以自行处理完购买逻辑后再开始调用播放音频事件。
```
```
    //调用此代码继续尝试播放。
    MusicPlayerManager.getInstance().continuePlay(String sourcePath);
```
### 七、后台播放避免被系统杀死
```
    在开始播放前，需设置MusicPlayerManager中的setLockForeground(boolean enable);方法，设置为true即可，播放器内部以兼容处理8.0手机和国产部分机型通知栏显示。
```
 ___
### 八、MusicPlayerManager 常用API预览及说明：
```
    /**
     * Activity初始化音乐服务组件，Activity中初始化后调用
     * @param context Activity上下文
     */
    public void initialize(Context context);

    /**
     * 解绑服务
     * @param context Activity上下文
     */
    public void unBindService(Context context);

    /**
     * 返回播放器配置
     * @return 播放器当前配置
     */
    public MusicPlayerConfig getMusicPlayerConfig();

    /**
     * 设定播放器配置
     * @param musicPlayerConfig
     */
    public void setMusicPlayerConfig(MusicPlayerConfig musicPlayerConfig);

    /**
     * 获取默认的闹钟模式
     * @return 播放器闹钟模式
     */
    public MusicAlarmModel getDefaultAlarmModel();

    /**
     * 设置默认的闹钟模式
     * @param alarmModel
     * @return 已设置的闹钟模式
     */
    public MusicPlayerManager setDefaultAlarmModel(MusicAlarmModel alarmModel);

    /**
     * 获取播放模式
     * @return 播放器默认的闹钟模式
     */
    public MusicPlayModel getDefaultPlayModel();

    /**
     * 设置默认的播放模式
     * @param playModel
     * @return 自身
     */
    public MusicPlayerManager setDefaultPlayModel(MusicPlayModel playModel);

    /**
     * 是否启用前台进程
     * @return 返回场常驻进程开启状态
     */
    public boolean isLockForeground();

    /**
     * 是否开启前台进程
     * @param enable true:开启
     */
    public MusicPlayerManager setLockForeground(boolean enable);

    /**
     * 是否启用悬浮窗自动吸附悬停
     * @return 返回是否启用自动吸附悬停
     */
    public boolean isWindownAutoScrollToEdge();

    /**
     * 设置悬浮窗是否自动吸附至屏幕边缘
     * @param enable true:开启
     */
    public MusicPlayerManager setWindownAutoScrollToEdge(boolean enable);

    /**
     * 是否启用垃圾桶手势取消悬浮窗
     * @return 返回是否启用垃圾桶
     */
    public boolean isTrashEnable();

    /**
     * 设置垃圾桶手势取消悬浮窗
     * @param enable true:开启
     */
    public MusicPlayerManager setTrashEnable(boolean enable);

    /**
     * 是否启用锁屏控制器
     * @return 返回是否启锁屏控制器
     */
    public boolean isScreenOffEnable();

    /**
     * 锁屏控制器开关
     * @param enable true:开启
     */
    public MusicPlayerManager setScreenOffEnable(boolean enable);

    /**
     * 返回悬浮窗播放器样式
     * @return 返回悬浮窗播放器样式
     */
    public MusicWindowStyle getWindownStyle();

    /**
     * 设置悬浮窗播放器样式
     * @param musicWindowStyle 新的样式，参考MusicWindowStyle定义
     */
    public MusicPlayerManager setWindownStyle(MusicWindowStyle musicWindowStyle);

    /**
     * 开始播放新的音频队列，播放器会替换全新音乐列表
     * @param musicList 待播放的数据集，对象需要继承BaseMediaInfo
     * @param index 指定要播放的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(List<?> musicList, int index);

    /**
     * 开始播放指定位置音频文件，如果播放列表存在
     * @param index 指定的位置 0-data.size()
     */
    @Override
    public void startPlayMusic(int index);

    /**
     * 开始一个新的播放任务，播放器内部自动将其添加至队列顶部,即插队播放
     * @param mediaInfo 音频对象
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
     * @param loop true:循环
     */
    @Override
    public void setLoop(boolean loop);

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
     * @param musicLists 数据集
     * @param index 位置
     */
    @Override
    public void updateMusicPlayerData(List<?> musicLists, int index);

    /**
     * 设置播放模式
     * @param model 播放模式，参考MusicPlayModel定义
     * @return 成功设置的播放模式
     */
    @Override
    public MusicPlayModel setPlayerModel(MusicPlayModel model);

    /**
     * 获取播放模式
     * @return 播放器播放模式
     */
    @Override
    public MusicPlayModel getPlayerModel();

    /**
     * 设置定时模式
     * @param model 定时关闭模式，参考MusicAlarmModel定义
     * @return 成功设置的播放模式
     */
    @Override
    public MusicAlarmModel setPlayerAlarmModel(MusicAlarmModel model);

    /**
     * 获取定时模式
     * @return 定时关闭模式
     */
    @Override
    public MusicAlarmModel getPlayerAlarmModel();

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
     * @return 播放器内部处理数据集的CHANNEL
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
     */
    public void setForegroundOpenActivityClassName(String className);

    /**
     * APP销毁时同步销毁
     * @param context Activity类型上下文
     */
    public void unInitialize(Activity context);

```