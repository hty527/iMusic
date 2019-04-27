# **视频播放器Wiki**
### BaseVideoPlayer 常用API预览及说明：
```
    /**
      * 设置播放资源
      * @param path 暂支持file、http、https等协议
      * @param title 视频描述
      */
     public void setDataSource(String path, String title);

    /**
     * 设置播放资源
     * @param path 暂支持file、http、https等协议
     * @param title 视频描述
     * @param videoID 视频ID
     */
    public void setDataSource(String path, String title,long videoID);

    /**
     * 设置参数TAG，可选的，若支持悬浮窗中打开播放器功能，则必须调用此方法绑定PlayerActivity所需参数
     * @param params VideoPlayerActivity 组件所需参数
     */
    public void setParamsTag(VideoParams params);

    /**
     * 设置循环模式，也可调用VideoWindowManager的setLoop(boolean loop);方法设置
     * @param loop
     */
    public void setLoop(boolean loop);

    /**
     * 设置缩放类型
     * @param displayType 详见VideoConstants常量定义
     */
    public void setVideoDisplayType(int displayType);

    /**
     * 悬浮窗功能入口开关，需要在C不为空下调用
     * @param enable true:允许 false:禁用
     */
    public void setGlobaEnable(boolean enable);

    /**
     * 设置视频控制器
     * @param videoController 自定义VideoPlayer控制器
     * @param autoCreateDefault 当 controller 为空，是否自动创建默认的控制器
     */
    public void setVideoController(V videoController, boolean autoCreateDefault);

    /**
     * 设置封面控制器
     * @param coverController 自定义VideoPlayerCover控制器
     * @param autoCreateDefault 当 controller 为空，是否自动创建默认的控制器
     */
    public void setVideoCoverController(C coverController, boolean autoCreateDefault);

    /**
     * 设置自定义的手势识别器
     * @param gestureController
     */
    public void setVideoGestureController(G gestureController);

    /**
     * 移动网络工作开关
     * @param mobileWorkEnable
     */
    public void setMobileWorkEnable(boolean mobileWorkEnable);

    /**
     * 返回封面控制器
     * @return
     */
    public C getCoverController();

    /**
     * 返回视频控制器
     * @return
     */
    public V getVideoController();

    /**
     * 返回全屏的手势识别控制器
     * @return
     */
    public G getGestureController();

    /**
     * 开始播放的入口开始播放、准备入口
     */
    public void starPlaytVideo();

    /**
     * 从悬浮窗播放器窗口转向VideoPlayerActivity播放
     */
    public void startWindowToActivity();

    /**
     * @param fullScreenVideoController 全屏控制器，为空则使用默认控制器
     */
    public void startFullScreen(V fullScreenVideoController);

    /**
     * 退出全屏播放
     * 退出全屏播放的原理：和开启全屏反过来
     */
    public void backFullScreenWindow();

    /**
     * 开启小窗口播放
     * 默认X：30像素 Y：30像素 位于屏幕左上角,使用默认控制器
     * @param miniWindowController 适用于迷你窗口播放器的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startMiniWindow(BaseVideoController miniWindowController);

    /**
     * 开启小窗口播放
     *
     * @param startX     起点位于屏幕的X轴像素
     * @param startY     起点位于屏幕的Y轴像素
     * @param tinyWidth  小窗口的宽 未指定使用默认 屏幕宽的 1/2(二分之一)
     * @param tinyHeight 小窗口的高 未指定使用默认 屏幕宽的 1/2 *9/16
     * @param miniWindowController 适用于迷你窗口播放器的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startMiniWindow(int startX, int startY, int tinyWidth, int tinyHeight,V miniWindowController);

    /**
     * 退出迷你小窗口播放
     */
    public void backMiniWindow();


    /**
     * 转向全局的悬浮窗播放,默认起点X,Y轴为=播放器Vide的起始X,Y轴，播放器默认居中显示，宽：屏幕宽度3/4(四分之三)，高：16：9高度
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindown(BaseVideoController windowController,boolean defaultCreatCloseIcon);

    /**
     * 转向全局的悬浮窗播放
     * @param startX 屏幕X起始轴
     * @param startY 屏幕Y起始轴
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindown(int startX, int startY,V windowController,boolean defaultCreatCloseIcon);

    /**
     * 转向全局的悬浮窗播放
     * @param width 播放器宽
     * @param height 播放器高
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindownPlayerSetWH(int width,int height,V windowController,boolean defaultCreatCloseIcon);

    /**
     * 转向全局的悬浮窗播放
     * @param startX 播放器位于屏幕起始X轴
     * @param startY 播放器位于屏幕起始Y轴
     * @param width 播放器宽
     * @param height 播放器高
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     * @param defaultCreatCloseIcon 是否创建一个默认的关闭按钮，位于悬浮窗右上角，若允许创建，则播放器内部消化关闭时间
     */
    public void startGlobalWindown(int startX, int startY, int width, int height,V windowController,boolean defaultCreatCloseIcon);


    /**
     * 弹射返回
     */
    public boolean backPressed();

    /**
     * 此处返回此组件绑定的工作状态
     * @return
     */
    public boolean isWorking();

    public void setWorking(boolean working);

    /**
     * 销毁
     */
    public void onReset();

```
### VideoPlayerManager 常用API预览及说明：

```
    /**
     * 指定点击通知栏后打开的Activity对象绝对路径
     * @param className VideoPlayerActivity的绝对路径
     */
    public VideoPlayerManager setVideoPlayerActivityClassName(String className);

    /**
     * 设置循环模式
     * @param loop
     */
    @Override
    public VideoPlayerManager setLoop(boolean loop);

    /**
     * 设置是否允许移动网络环境下工作
     * @param enable true：允许移动网络工作 false：不允许
     */
    @Override
    public void setMobileWorkEnable(boolean enable);

    /**
     * 是否允许移动网络环境下工作
     * @return
     */
    public boolean isMobileWorkEnable();

    /**
     * 注册播放器工作状态监听器
     * @param listener 实现VideoPlayerEventListener的对象
     */
    @Override
    public void addOnPlayerEventListener(VideoPlayerEventListener listener);

    /**
     * 移除播放器工作状态监听器
     */
    @Override
    public void removePlayerListener();

    /**
     * 开始异步准备缓冲播放
     * @param dataSource 播放资源地址，支持file、https、http 等协议
     * @param context
     */
    @Override
    public void startVideoPlayer(String dataSource, Context context);

    /**
     * 开始异步准备缓冲播放
     * @param dataSource 播放资源地址，支持file、https、http 等协议
     * @param context
     * @param percentIndex 尝试从指定位置开始播放
     */
    @Override
    public void startVideoPlayer(String dataSource, Context context, int percentIndex);

    /**
     * 尝试重新播放
     * @param percentIndex 尝试从指定位置重新开始
     */
    @Override
    public void reStartVideoPlayer(long percentIndex);

    /**
     * 返回播放器内部播放状态
     * @return
     */
    @Override
    public boolean isPlaying();
    /**
     * 返回播放器内部工作状态
     * @return true:正在工作，包含暂停、缓冲等 false:未工作
     */
    @Override
    public boolean isWorking();

    /**
     * 开始、暂停 播放
     */
    @Override
    public void playOrPause();
    /**
     * 恢复播放
     */
    @Override
    public void play();

    /**
     * 暂停播放
     */
    @Override
    public void pause();

    /**
     * 释放、还原播放、监听、渲染等状态
     */
    @Override
    public void onReset();

    /**
     * 停止播放
     * @param isReset 是否释放播放器
     */
    @Override
    public void onStop(boolean isReset);

    /**
     * 跳转至指定位置播放
     * @param currentTime 事件位置，单位毫秒
     */
    @Override
    public void seekTo(long currentTime);

    /**
     * 返回正在播放的对象时长
     * @return
     */
    @Override
    public long getDurtion();

    /**
     * 返回已播放时长
     * @return
     */
    @Override
    public long getCurrentDurtion();

    /**
     * 尝试弹射退出，若当前播放器处于迷你小窗口、全屏窗口下，则只是退出小窗口\全屏至常规窗口播放
     * 若播放器处于常规状态下，则立即销毁播放器，销毁时内部检测了悬浮窗状态，若正在悬浮窗状态下播放，则啥也不做
     * @return 是否可以销毁界面
     */
    @Override
    public boolean isBackPressed();
    /**
     * 尝试弹射退出，若当前播放器处于迷你小窗口、全屏窗口下，则只是退出小窗口\全屏至常规窗口播放
     * 若播放器处于常规状态下，则立即销毁播放器，销毁时内部检测了悬浮窗状态，若正在悬浮窗状态下播放，则啥也不做
     * @param destroy 是否直接销毁，比如说MainActivity返回逻辑还有询问用户是否退出，给定destroy为false，则只是尝试弹射，并不会去销毁播放器
     * @return
     */
    @Override
    public boolean isBackPressed(boolean destroy);

    /**
     * 返回播放器内部播放状态
     * @return
     */
    @Override
    public VideoPlayerState getVideoPlayerState();

    /**
     * 检查播放器内部状态
     */
    @Override
    public void checkedVidepPlayerState();

    /**
     * 若跳转至目标Activity后需要衔接播放，则必须设置此标记，以便在生命周期切换时处理用户动作意图
     * @param continuePlay true:衔接播放 fasle:不衔接播放
     */
    @Override
    public void setContinuePlay(boolean continuePlay);

    /**
     * 组件处于可见状态
     */
    @Override
    public void onResume();

    /**
     * 组件即将处于不可见状态
     */
    @Override
    public void onPause();

    /**
     * 对应生命周期调用
     */
    @Override
    public void onDestroy();
```