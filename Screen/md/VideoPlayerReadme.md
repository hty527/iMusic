# **视频播放器Wiki**

### 自定义交互UI的实现
**1.自定义控制器实现**<br/>

控制器是用户与播放器交互的控制器，如需自定义请继承BaseVideoController类并实现其抽象方法，调用BaseVideoPlayer的setVideoController(V controller);绑定控制器。</br>
如在播放过程中开启小窗口、悬浮窗播放器时，可指定控制器小窗口、悬浮窗专用的交互控制器。悬浮窗口的关闭按钮不支持自定义。<br/>

**2.自定义封面控制器实现**<br/>

封面控制器是指视频在开始播放前的封面显示图层，如需自定义请继承BaseCoverController类，调用BaseVideoPlayer的setVideoCoverController(C controller);绑定控制器。BaseCoverController中默认实现了点击开始播放能力。若需自定义点击自己的View开始播放，请实现点击事件后
调用BaseVideoPlayer的mOnStartListener.onStartPlay();方法开始播放。<br/>

**3.自定义手势识别器实现**<br/>

手势识别器是播放器在全屏状态下播放时，播放器内部检测用户手势滑动行为对播放器功能做出改变时的UI交互提示，如快进、快退、音量、亮度等调节后的UI显示交互，如需自定义
请继承BaseGestureController类，实现其抽象方法，调用调用BaseVideoPlayer的setVideoGestureController(G controller);绑定控制器。<br/>

#### 特别注意
** 播放器是支持播放器窗口切换无缝衔接播放、悬浮窗中点击全屏打开播放器界面功能的，在使用转场播放前，必须调用VideoPlayerManager.getInstance().setContinuePlay(true); **<br/>

#### 部分功能交互处理
**1.转场衔接播放处理**<br/>
示例代码如下：(这里示意从A 界面列表跳转至B Activity衔接播放)<br/>
跳转之前：<br/>
```
    //找出播放器控件
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
跳转之后VideoPlayerActviity的衔接工作。
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
        if(isPlaying&&null!=VideoPlayerManager.getInstance().getTextureView()){
            addTextrueViewToView(mVideoPlayer);
            //为新的播放器窗口添加监听器
            VideoPlayerManager.getInstance().addOnPlayerEventListener(mVideoPlayer);
            //手动检查播放器内部状态，同步常规播放器状态至全屏播放器
            VideoPlayerManager.getInstance().checkedVidepPlayerState();
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
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            VideoTextureView textureView = VideoPlayerManager.getInstance().getTextureView();
            if(null!=textureView.getParent()){
                ((ViewGroup) textureView.getParent()).removeView(textureView);
            }
        }
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            videoPlayer.mSurfaceView.addView(VideoPlayerManager.getInstance().getTextureView(),new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }
```
到此即可实现画面无闪烁、无卡顿的衔接播放任务了。</br>
**2.悬浮窗口中打开APP播放器界面处理**<br/>
##### 2.1：首先在全局初始化中设置要跳转的Activity绝对路径:
```
    //设置跳转的Activity的绝对路径
    VideoPlayerManager.getInstance().setVideoPlayerActivityClassName(VideoPlayerActviity.class.getCanonicalName());
```
##### 2.2：开始播放前的TAG设置：
```
    //VideoParams中的字段根据自己需求填写，基本的ID、播放地址等不能为空。这个参数最红会在跳转至播放器Activity时传递过去。
    VideoPlayerTrackView.setParamsTag(VideoParams params);
```
##### 2.3：设置TAG后，在悬浮窗中点击全屏按钮即可正确打开播放器Activity并传递参数了。

#### 视频播放器功能API介绍
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