# **iMusic**
### 完整的 音乐播放器 和 视频播放器 封装库
[作者简书主页](https://www.jianshu.com/u/6a64162caadd)

[所属组织](https://github.com/feiyouAndroidTeam)
# 项目介绍
#### 视频演示:
![image](https://github.com/Yuye584312311/IMusic/blob/master/Screen/video/video.gif)
## 音乐播放器:
* 完整的音乐播放器功能，包括但不限于：</br>
    • 网络音乐播放</br>
    • 本地音乐检索播放</br>
    • 搜索(歌手、专辑、歌曲名)播放</br>
    • 基本常规操作示例播放器</br>
    • 锁屏播放控制</br>
    • 自定义唱片机</br>
    • 悬浮窗播放</br>
    • 状态栏通知控制</br>
    • 定时关闭播放</br>
    • 播放模式设置</br>
    • 对音乐收藏至本地</br>
    • 最近播放记录浏览</br>
    • 已对音频输入焦点管理作处理</br>
已封装成库，集成请参照com.android.imusic.music.activity.MusicPlayerActivity中的使用示例。
## 软件截图：
![音乐播放器界面](https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/%E6%88%AA%E5%B1%8F_20190417_162126.jpg)

## 视频播放器:
* 完整的视频播放器功能，包括但不限于：</br>
    • 列表单例播放</br>
    • 列表横竖屏切换</br>
    • 常规横竖屏切换</br>
    • A activity 跳转至B activity无缝衔接播放</br>
    • 支持可切换至迷你小窗口播放，支持Activity内拖拽</br>
    • 支持可切换至全局悬浮窗播放，支持屏幕全局拖拽</br>
    • 全屏播放下手势识别调节音量、屏幕亮度、快进、快退</br>
    • 支持完全自定义视频控制器</br>
    • 支持完全自定义封面控制器</br>
    • 支持完全自定义手势识别调节器</br>
    • 支持全局悬浮窗播放器中无缝切换至播放器界面</br>
已封装成库，音频焦点等细节已处理，简单集成即可使用所有功能。
## 软件截图：
![视频播放器界面](https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/%E6%88%AA%E5%B1%8F_20190418_135654.jpg)
</br>
### 体验APK下载:
![扫描二维码下载](https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/code.png)

[Fir托管下载](https://fir.im/iMusic)
</br>
[或点此下载](https://github.com/Yuye584312311/IMusic/blob/master/Screen/apk/iMusic.apk)
### 集成步骤:
#### 音乐播放器:

#### 视频播放器:此库提供了一套默认的播放器和UI，如需自定义播放器交互UI，请继承BaseVideoPlayer、BaseVideoController、BaseCoverController，此处演示默认的播放器继承步骤，更多自定义组件和功能请阅下文。
1.在你的项目中的.xml中引入播放器布局</br>
```
      <com.video.player.lib.view.VideoPlayerTrackView
            android:id="@+id/video_track"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:video_autoSetCoverController="true"
            app:video_autoSetVideoController="true"/>
```
支持的自定义属性说明：
```
        <!--是否自动设置默认控制器-->
        <attr name="video_autoSetVideoController" format="boolean"/>
        <!--是否自动设置封面控制器-->
        <attr name="video_autoSetCoverController" format="boolean"/>
        <!--循环播放-->
        <attr name="video_loop" format="boolean"/>
```
也可以这样动态初始化：其他BaseVideoPlayer相关的API后面统一介绍。<br/>
```
        //frameLayout 你的parent布局
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.xxx);
        VideoPlayerTrackView playerTrackView=new VideoPlayerTrackView(context);
        playerTrackView.setLoop(true);
        playerTrackView.setVideoController(videoController);
        playerTrackView.setVideoCoverController(coverController);
        playerTrackView.setVideoGestureController(gestureController);
        frameLayout.addView(playerTrackView,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200dp,Gravity.CENTER));
```
2.初始化播放的宽高，默认是LayoutParams.MATCH_PARENT，设置播放器必须的基本数据
```
        //播放器控件宽高
        mVideoPlayer = (VideoDetailsPlayerTrackView) findViewById(R.id.video_player);
        int itemHeight = MusicUtils.getInstance().getScreenWidth(this) * 9 / 16;
        mVideoPlayer.getLayoutParams().height=itemHeight;
        //设置播放资源
        mVideoPlayer.setDataSource(mVideoParams.getVideoUrl(),mVideoParams.getVideoTitle(),mVideoParams.getVideoiId());
        //是否循环播放
        mVideoPlayer.setLoop(true);
        //这个可选的，如在悬浮窗中需要支持切换至播放器界面，此TAG必须绑定,假如你的播放器界面入参只需一个ID则可忽略此设置
        mVideoPlayer.setParamsTag(mVideoParams);
        //基本参数设定完毕后即可调用此方法自动开始准备播放
        mVideoPlayer.starPlaytVideo();
```
3.生命周期方法加入
```
    @Override
    protected void onResume() {
        super.onResume();
        VideoPlayerManager.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoPlayerManager.getInstance().onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if(VideoPlayerManager.getInstance().isBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoPlayerManager.getInstance().onDestroy();
    }
```
至此你的播放器具备了基础的视频播放能力,更多功能和API使用，请参阅读下文。<br/>
##### 自定义交互UI的实现
要实现自定义交互UI，需要继承BaseVideoController类，调用BaseVideoPlayer的setVideoController(V videoController);绑定交互UI，BaseVideoController设计有播放器状态必须的抽象方法需要实现外，还提供了全屏播放、小窗口播放、悬浮窗播放等拓展功能，
要实现拓展功能，请调用OnFuctionListener的抽象方法实现，拓展的功能和拓展抽象方法如下所示：
```
    /**
     * 这些方法可根据自身的操作交互按照需求实现，非必须实现的的
     */
    //设置视频标题内容
    protected void setTitle(String videoTitle){}
    //播放地址为空
    protected void pathInvalid(){}
    //切换为竖屏方向
    protected void startHorizontal(){}
    //切换为小窗口播放
    protected void startTiny(){}
    //切换为悬浮窗
    protected void startGlobalWindow(){}
    //视频总长度、播放进度、缓冲进度
    protected void onTaskRuntime(long totalDurtion, long currentDurtion,int bufferPercent){}
    //缓冲百分比
    protected void onBufferingUpdate(int percent){}
    //播放器空白位置单击事件，关注此方法实现控制器的现实和隐藏
    protected void changeControllerState(int scrrenOrientation,boolean isInterceptIntent){}

   /**
    * 自定义控制器若需要在交互布局中实现全屏、迷你窗口、全局悬浮窗、悬浮窗切换至播放器界面、弹射返回等功能，请调用BaseVideoController的setOnFuctionListener方法，按照需求实现抽象对应的抽象功能。
    */
    public abstract static class OnFuctionListener{
        /**
         * 开启全屏
         * @param videoController 继承自BaseVideoController的自定义控制器
         */
        public void onStartFullScreen(BaseVideoController videoController){}
        /**
         * 开启迷你窗口
         * @param miniWindowController 继承自BaseVideoController的自定义控制器
         */
        public void onStartMiniWindow(BaseVideoController miniWindowController){}
        /**
         * 开启全局悬浮窗
         * @param windowController 继承自BaseVideoController的自定义控制器
         */
        public void onStartGlobalWindown(BaseVideoController windowController){}
        //关闭迷你窗口
        public void onQuiteMiniWindow(){}
        //打开播放器界面
        public void onStartActivity(){}
        //弹射返回
        public void onBackPressed(){}
    }
```
除了继承BaseVideoController实现全屏、迷你窗口、全局悬浮窗、悬浮窗切换至播放器界面、弹射返回等功能外，还可以直接调用BaseVideoPlayer的公开方法实现以上功能和交互,BaseVideoPlayer的所有公开方法如下：
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
     * 开启全屏播放的原理：
     * 1：改变屏幕方向，Activity 属性必须设置为android:configChanges="orientation|screenSize"，避免Activity销毁重建
     * 2：移除常规播放器已有的TextureView组件
     * 3：向Windown ViewGroup 添加一个新的VideoPlayer组件,赋值已有的TextrueView到VideoPlayer，设置新的播放器监听，结合TextrueView onSurfaceTextureAvailable 回调事件处理
     * 4：根据自身业务，向新的播放器添加控制器
     * 5：记录全屏窗口播放器，退出全屏恢复常规播放用到
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
     */
    public void startGlobalWindown(BaseVideoController windowController) ;

    /**
     * 转向全局的悬浮窗播放
     * @param startX 屏幕X起始轴
     * @param startY 屏幕Y起始轴
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startGlobalWindown(int startX, int startY,V windowController);

    /**
     * 转向全局的悬浮窗播放
     * @param width 播放器宽
     * @param height 播放器高
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startGlobalWindownPlayerSetWH(int width,int height,V windowController);

    /**
     * 转向全局的悬浮窗播放
     * @param startX 播放器位于屏幕起始X轴
     * @param startY 播放器位于屏幕起始Y轴
     * @param width 播放器宽
     * @param height 播放器高
     * @param windowController 适用于悬浮窗的控制器，若传空，则使用内部默认的交互控制器
     */
    public void startGlobalWindown(int startX, int startY, int width, int height,V windowController);

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

    /**
     * 仅仅内部销毁，外部组件调用VideoPlayerManager 的 onDestroy()方法销毁播放器
     */
    @Override
    public void destroy();
```