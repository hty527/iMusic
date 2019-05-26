[VideoPlayerAPI]: https://yuye584312311.github.io/pages.github.io/JavaDoc/video-player-lib/javadoc/index.html "VideoPlayerAPI"
* 有关VideoPlayer全部API文档，请阅读[VideoPlayerAPI]<br/>
![VideoPlayerFrame](https://github.com/Yuye584312311/iMusic/blob/master/Doc/screenshot/videp_player.png)

### 一、播放器框架定义的名词概念释义

#### 1. 播放器通道

为满足播放器在不同场景下的不同功能实现，封装了播放器通道的概念，所有播放器通道都需继承BaseVideoPlayer抽象基类，指定三种控制器的泛型类型。具体用法请参考内部播放器封装的默认播放器通道VideoPlayerTrackView类。<br/>

#### 2. 控制器

播放器在不同场景下，UI和功能难免有所差异，出于避免功能交互耦合问题，播放器封装支持用户自定义控制器。播放器内部三种核心的控制器分别是：播放控制器、封面控制器、手势识别控制器。这三个控制器由BaseVideoPlayer
类持有，分发内部播放器的各种播放事件。如：播放进度、暂停、开始、缓冲进度等等。播放器通道并不参与界面的更新，全部交由控制器自己内部实现。<br/>

#### 3. BaseVideoPlayer

BaseVideoPlayer被设计成抽象的基类，所有自定义的播放器通道必须继承BaseVideoPlayer类，返回LayoutID交给BaseVideoPlayer。还需指定泛型控制器的类型，Layout布局中必须申明id:surface_view,如果你的播放器不需要与用户交互和封面图层，则其他ID无需申明。另外，此播放器默认封装了一套 播放器通道+三种控制器。
播放器通道布局中需要声明的资源ID如下：
#### 4. 代理人
视频播放器内部架构被设计为代理模式，常规的生命周期、暂停开始等，通过VideoPlayerManager类来代理交互。有关VideoPlayerManager中的方法，请阅读下文。
```
    <resources>
        <!--播放器渲染ID-->
        <item name="surface_view" type="id"></item>
        <!--视频播放器控制器ID-->
        <item name="video_player_controller" type="id"></item>
        <!--封面控制器ID-->
        <item name="video_cover_controller" type="id"></item>
        <!--全屏窗口播放器ID-->
        <item name="video_full_screen_window" type="id"></item>
        <!--迷你小窗口播放器ID-->
        <item name="video_mini_window" type="id"></item>
    </resources>
```
在你的播放器通道布局中,声明示例：
```
   <?xml version="1.0" encoding="utf-8"?>
   <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
       <!--画面-->
       <FrameLayout
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:id="@id/surface_view"/>
       <!--上层封面或其他-->
       <FrameLayout
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:id="@id/video_cover_controller"/>
       <!--控制器-->
       <FrameLayout
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:id="@id/video_player_controller"/>
   </FrameLayout>
```
### 二、播放器通道创建
#### 1. 全局初始化
```
    //视频播放器初始化
    VideoPlayerManager.getInstance()
            //循环模式
            .setLoop(true)
            //悬浮窗中打开播放器的绝对路径
            .setPlayerActivityClassName(VideoPlayerActviity.class.getCanonicalName());
```
#### 2. 播放器通道java代码创建
```
    FrameLayout frameLayout = (FrameLayout) findViewById(R.id.xxx);
    VideoPlayerTrackView playerTrackView=new VideoPlayerTrackView(context);
    playerTrackView.setVideoController(videoController);
    playerTrackView.setVideoCoverController(coverController);
    playerTrackView.setVideoGestureController(gestureController);
    frameLayout.addView(playerTrackView,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200dp,Gravity.CENTER));
```
#### 3. xml初始化支持的自定义属性
```
    <declare-styleable name="BaseVideoPlayer">
            <!--是否自动设置默认控制器-->
            <attr name="video_autoSetVideoController" format="boolean"/>
            <!--是否自动设置封面控制器-->
            <attr name="video_autoSetCoverController" format="boolean"/>
    </declare-styleable>
```
#### 4. 播放器通道拓展功能初始设置
```
    //如需在悬浮窗中支持点击全屏切换至播放器界面，此TAG必须绑定,假如你的播放器界面入参只需一个ID则可忽略此设置并调用setDataSource的三参方法
    playerTrackView.setParamsTag(mVideoParams);
    //设置画面渲染缩放模式,默认VideoConstants.VIDEO_DISPLAY_TYPE_CUT，详见VideoConstants常量定义
    playerTrackView.setVideoDisplayType(mode);
    //是否支持悬浮窗播放功能，这个开关只针对入口有效，不会限制对startGlobalWindown();的调用
    playerTrackView.setGlobaEnable(true); 或 mVideoPlayer.getVideoController().setGlobaEnable(true);
```

### 三、自定义交互UI的具体实现
#### 1. 自定义交互控制器

交互控制器在本项目中被定义为：用户与播放器的UI交互控制器。如需自定义请继承BaseVideoController类并实现其抽象方法，调用播放器通道的setVideoController(V controller);绑定控制器。</br>
如在播放过程中开启小窗口、悬浮窗播放器时，可指定控制器小窗口、悬浮窗专用的交互控制器。悬浮窗口的关闭按钮不支持自定义。另外，控制器还提供了子线程中的播放进度方法，100毫秒执行一次，主线程的播放进度方法是1000毫秒执行一次，请注意你的UI刷新。<br/>

#### 2. 自定义封面控制器

封面控制器是指视频在开始播放前的封面显示图层，如需自定义请继承BaseCoverController类，调用播放器通道的setVideoCoverController(C controller);绑定控制器。BaseCoverController中默认实现了点击开始播放能力。若需自定义点击自己的View开始播放，请实现点击事件后
调用BaseCoverController的mOnStartListener.onStartPlay();方法开始播放。<br/>

#### 3. 自定义手势识别器

手势识别器是播放器在全屏状态下播放时，播放器内部检测用户手势滑动行为对播放器功能做出改变时的UI交互提示，如快进、快退、音量、亮度等调节后的UI显示交互，如需自定义
请继承BaseGestureController类，实现其抽象方法，调用调用播放器通道的setVideoGestureController(G controller);绑定控制器。自定义手势识别器还支持消费手势触摸事件，详见BaseGestureController抽象方法onTouchEnevt介绍。<br/>

**特别注意**<br/>
* 播放器是支持播放器窗口切换无缝衔接播放、悬浮窗中点击全屏打开播放器界面功能的，在使用转场播放前，必须调用VideoPlayerManager.getInstance().setContinuePlay(true); <br/>

### 四、其他功能拓展
#### 1. 界面跳转无缝衔接播放
界面跳转无缝衔接播放大多用在列表播放时点击条目跳转至视频详情界面继续播放，为了更好的用户体验，不应该重新去加载视频数据，而是衔接列表的播放进度和画面、声音等继续播放。<br/>
在你的Actvity中实现如下代码：<br/>
跳转之前
```
    //播放器控件
    VideoPlayerTrackView trackView = (VideoPlayerTrackView) view.findViewById(R.id.video_track);
    //此处格式化界面传递所需参数
    VideoParams videoParams= MediaUtils.getInstance().formatVideoParams(indexItemBean);
    Intent intent=new Intent(getActivity(), VideoPlayerActviity.class);
    intent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,videoParams);
    //如果播放器正在工作，转场衔接播放
    if(null!=trackView&&trackView.isWorking()){
        //界面衔接播放前，一定要设置此标记，用来区分Activity的onResume();事件
        VideoPlayerManager.getInstance().setContinuePlay(true);
        //销毁当前播放器窗口画面渲染
        trackView.reset();
        //传参表示衔接播放
        intent.putExtra(VideoConstants.KEY_VIDEO_PLAYING,true);
    }else{
        //否则直接释放可能存在的播放任务
        VideoPlayerManager.getInstance().onReset();
    }
    //到播放器界面
    startActivity(intent);
```
跳转之后Actviity的衔接工作。
```
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xxx);
        boolean isPlaying = intent.getBooleanExtra(VideoConstants.KEY_VIDEO_PLAYING,false);
        mVideoPlayer = (VideoDetailsPlayerTrackView) findViewById(R.id.video_player);
        //设置播放资源
        mVideoPlayer.setDataSource(mVideoParams.getVideoUrl(),mVideoParams.getVideoTitle(),mVideoParams.getVideoiId());
        ...此处省去其他初始化
        //衔接播放任务
        if(isPlaying&&null!=IMediaPlayer.getInstance().getTextureView()){
            addTextrueViewToView(mVideoPlayer);
            //为新的播放器窗口添加监听器
            IMediaPlayer.getInstance().addOnPlayerEventListener(mVideoPlayer);
            //手动检查播放器内部状态，同步常规播放器状态至全屏播放器
            IMediaPlayer.getInstance().checkedVidepPlayerState();
        }else{
            //开始全新播放任务
            mVideoPlayer.startPlayVideo();
        }
    }

    /**
     * 添加一个视频渲染组件至View
     * @param videoPlayer
     */
    private void addTextrueViewToView(BaseVideoPlayer videoPlayer) {
        //先移除存在的TextrueView
        if(null!=IMediaPlayer.getInstance().getTextureView()){
            VideoTextureView textureView = IMediaPlayer.getInstance().getTextureView();
            if(null!=textureView.getParent()){
                ((ViewGroup) textureView.getParent()).removeView(textureView);
            }
        }
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            videoPlayer.mSurfaceView.addView(IMediaPlayer.getInstance().getTextureView(),new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }
```
#### 2. 迷你小窗口播放器与常规播放器切换
本库内部已封装支持从常规播放器切换至迷你小窗口播放且只能是常规播放器切换至小窗口，请不要尝试从全屏或者悬浮窗切换至小窗口播放！也支持小窗口全屏幕拖拽功能。此处代码演示迷你小窗口默认出现在播放器右下方，播放器宽为屏幕1/2，高为16:9。具体的实现代码如下：
```
    int startY=mVideoPlayer.getMeasuredHeight()+VideoUtils.getInstance().dpToPxInt(VideoPlayerActviity.this,10f);
    //切换至小窗口播放
    mVideoPlayer.startMiniWindowToLocaion(Gravity.RIGHT,startY,1280,720,null);

```
#### 3. 切换至悬浮窗播放
播放器封装支持从常规播放器切换至全局悬浮窗口播放，也支持从悬浮窗口播放器跳回至视频播放器界面无缝衔接播放，示例代码如下：
##### 3.1 切换至全局悬浮窗
```
    //直接调用播放器通道的开始全局悬浮窗，startGlobalWindown方法为多参重载，请阅读方法参数注释
    mVideoPlayer.startGlobalWindown(null);
```
##### 3.2 全局悬浮窗跳回至Activity
若支持此功能，需在切换至悬浮窗口播放之前，设置回跳目标Activity的绝对路径和设置目标Activity的Params参数信息。
```
    //设置回跳路径建议在初始化时调用，设置悬浮窗跳转至目标Activity的绝对路径
    VideoPlayerManager.getInstance().setPlayerActivityClassName(VideoPlayerActviity.class.getCanonicalName());

    //切换至悬浮窗口播放之前需设置目标Activity所需的参数TAG，见VideoParams成员属性注释
    mVideoPlayer.setParamsTag(mVideoParams);
    //若你的目标Activity已经处理了无缝衔接播放，则不做再做些什么，如果不支持无缝衔接播放，则需在你的目标Activity加上下面代码
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xxx);
        boolean isPlaying = intent.getBooleanExtra(VideoConstants.KEY_VIDEO_PLAYING,false);
        mVideoPlayer = (VideoDetailsPlayerTrackView) findViewById(R.id.video_player);
        //设置播放资源
        mVideoPlayer.setDataSource(mVideoParams.getVideoUrl(),mVideoParams.getVideoTitle(),mVideoParams.getVideoiId());
        ...此处省去其他初始化
        //衔接播放任务
        if(isPlaying&&null!=IMediaPlayer.getInstance().getTextureView()){
            addTextrueViewToView(mVideoPlayer);
            //为新的播放器窗口添加监听器
            IMediaPlayer.getInstance().addOnPlayerEventListener(mVideoPlayer);
            //手动检查播放器内部状态，同步常规播放器状态至全屏播放器
            IMediaPlayer.getInstance().checkedVidepPlayerState();
        }else{
            //开始全新播放任务
            mVideoPlayer.startPlayVideo();
        }
    }

    /**
     * 添加一个视频渲染组件至View
     * @param videoPlayer
     */
    private void addTextrueViewToView(BaseVideoPlayer videoPlayer) {
        //先移除存在的TextrueView
        if(null!=IMediaPlayer.getInstance().getTextureView()){
            VideoTextureView textureView = IMediaPlayer.getInstance().getTextureView();
            if(null!=textureView.getParent()){
                ((ViewGroup) textureView.getParent()).removeView(textureView);
            }
        }
        if(null!=IMediaPlayer.getInstance().getTextureView()){
            videoPlayer.mSurfaceView.addView(IMediaPlayer.getInstance().getTextureView(),new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }
```
### 五、更多公开API介绍
#### 1. BaseVideoPlayer 常用API预览及说明：
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
    public void setDataSource(String path, String title,String videoID);

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
     * 设置画面缩放类型
     * @param displayType 详见VideoConstants常量定义释义说明
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
     * 主播监听播放器内部事件
     * @param eventListener
     */
    public void setOnVideoEventListener(OnVideoEventListener eventListener);

    /**
     * 开始播放的入口开始播放、准备入口
     */
    public void startPlayVideo();

    /**
     * 开始播放的入口开始播放、准备入口,调用此方法，可省去setDataSource()方法的调用
     * @param dataSource 播放资源地址
     * @param title 视频标题
     */
    public void startPlayVideo(String dataSource,String title);

    /**
     * 开始播放的入口开始播放、准备入口,调用此方法，可省去setDataSource()方法的调用
     * @param dataSource 播放资源地址
     * @param title 视频标题
     * @param videoID 视频ID
     */
    public void startPlayVideo(String dataSource,String title,long videoID);

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
     * 开启迷你小窗口播放，将窗口添加至屏幕的具体方位，内部换算显示比例。这个方法有别于startMiniWindow方法请阅读参数注解
     * 视频显示换算比例：宽屏视频：16:9，竖屏视频：9:16，正方形：1:1。
     * @param gravity 位于屏幕中的这里只能是左侧、右侧，(Gravity.LEFT、Gravity.RIGHT)内部切换至迷你小窗口会保证不会超出屏幕边界
     * @param videoWidth 视频真实宽度，用来换算窗口缩放的真实px
     * @param videoHeight 视频真实高度，用来换算窗口缩放的真实px
     * @param startY 其实Y轴位置
     * @param miniWindowController 适用于迷你窗口播放器的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startMiniWindowToLocaion(int gravity,int startY,int videoWidth, int videoHeight,V miniWindowController);

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
#### 2. 视频播放器代理人VideoPlayerManager 常用API预览及说明：

```

/**
     * 设置循环模式
     * @param loop true:循环播放 false:反之
     * @return 自身实例
     */
    @Override
    public VideoPlayerManager setLoop(boolean loop);

    /**
     * 返回循环播放模式
     * @return true:循环播放，false:不循环
     */
    public boolean isLoop();

    /**
     * 设置是否允许移动网络环境下工作
     * @param enable true：允许移动网络工作 false：不允许
     */
    @Override
    public void setMobileWorkEnable(boolean enable);

    /**
     * 是否允许移动网络环境下工作
     * @return 是否允许在移动网络下工作
     */
    public boolean isMobileWorkEnable();

    /**
     * 设置视频画面显示缩放类型,如果正在播放，会立刻生效
     * @param displayType 详见VideoConstants常量定义
     */
    @Override
    public void setVideoDisplayType(int displayType);

    /**
     * 返回视频画面缩放模式
     * @return 用户设定的缩放模式
     */
    public int getVideoDisplayType();

    /**
     * 指定点击通知栏后打开的Activity对象绝对路径
     * @param className 播放器Activity绝对路径
     */
    public void setPlayerActivityClassName(String className);

    /**
     * 返回点击通知栏后打开的Activity对象绝对路径
     * @return 播放器Activity绝对路径
     */
    public String getPlayerActivityClassName();

    /**
     * 返回播放器内部播放状态
     * @return 播放器内部播放状态
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
     * 返回当前正在播放的视频宽
     * @return 视频宽（分辨率）
     */
    @Override
    public int getVideoWidth();


    /**
     * 返回当前正在播放的视频高
     * @return 视频高（分辨率）
     */
    @Override
    public int getVideoHeight();

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
     * @return 视频总时长，单位毫秒
     */
    @Override
    public long getDurtion();
    /**
     * 返回已播放时长
     * @return 已播放的视频长度，单位毫秒
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
     * @param destroy 是否直接销毁，比如说MainActivity返回逻辑还有询问用户是否退出，给定destroy为false，
     *                则只是尝试弹射，并不会去销毁播放器
     * @return 是否可以销毁界面
     */
    @Override
    public boolean isBackPressed(boolean destroy);

    /**
     * 返回播放器内部播放状态
     * @return 内部播放状态
     */
    @Override
    public int getVideoPlayerState();


    /**
     * 若跳转至目标Activity后需要衔接播放，则必须设置此标记，以便在生命周期切换时处理用户动作意图
     * @param continuePlay true:衔接播放 fasle:不衔接播放
     */
    @Override
    public void setContinuePlay(boolean continuePlay);

    /**
     * 返回衔接播放状态
     * @return true:衔接播放 fasle:不衔接播放
     */
    public boolean isContinuePlay();

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