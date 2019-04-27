# **iMusic**

[Android音频播放器开发记录]: https://www.jianshu.com/p/9051b96c02f9 "简书音频播放器"
**音乐播放器博文:**[Android音频播放器开发记录]

[Android视频播放器开发记录]: https://www.jianshu.com/p/39d8f824c2fb "简书视频播放器"
**视频播放器博文:**[Android视频播放器开发记录]

基于系统MediaPlayer解码器封装的完整音乐播放器和视频播放器功能库，集成极简、功能较全面、体积小。欢迎Star和下载体验！
___
## 功能演示及概述:
#### 功能演示:(部分功能快照预览在最下面)
* 音乐播放器预览(如播放不流畅请点击图片查看)</br>
<div>
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/video/Music.gif" height="480" width="270">
</div>

* 视频播放器预览</br>
<div>
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/video/Video.gif" height="480" width="270">
</div>

#### 功能概述:
**1.音乐播放器**
* 网络音乐播放</br>
* 本地音乐检索播放</br>
* 搜索(歌手、专辑、歌曲名)播放</br>
* 基本常规操作示例播放器</br>
* 锁屏播放控制</br>
* 自定义唱片机</br>
* 悬浮窗播放</br>
* 状态栏通知控制</br>
* 定时关闭播放</br>
* 播放模式设置</br>
* 对音乐收藏至本地</br>
* 最近播放记录浏览</br>
* 已对音频输入焦点管理作处理<br/>
***
**2.视频播放器**
* 列表单例播放</br>
* 列表横竖屏切换</br>
* 常规横竖屏切换</br>
* 支持4种画面缩放模式设置</br>
* 支持完全自定义视频控制器</br>
* 支持完全自定义封面控制器</br>
* 支持完全自定义手势识别调节器</br>
* A activity 跳转至B activity无缝衔接播放</br>
* 支持可切换至迷你小窗口播放，支持Activity内拖拽</br>
* 支持可切换至全局悬浮窗播放，支持屏幕全局拖拽</br>
* 全屏播放下手势识别调节音量、屏幕亮度、快进、快退</br>
* 支持全局悬浮窗播放器中无缝切换至播放器界面</br>
## 一.音乐播放器集成步骤:
**1.全局初始化**
```
    //初始化首选项，用以播放器内部保存播放模式设置和定时关闭设置参数
    MusicUtils.getInstance().initSharedPreferencesConfig(getApplicationContext());
    //全局悬浮窗播放器单击事件
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
```
**2.MainActivity中初始化播放器服务组件**
```
    @Override
    protected void onCreate() {
        super.onCreate();
        //绑定MusicService
        MusicPlayerManager.getInstance().bindService(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicWindowManager.getInstance().onDestroy();
        //若使用Demo中的本地音乐功能，需要调用此方法
        MediaUtils.getInstance().onDestroy();
        //解绑服务
        MusicPlayerManager.getInstance().unBindService(MainActivity.this);
        MusicPlayerManager.getInstance().onDestroy();
    }
```
**3.开始播放任务**
```
    /**
     * mediaInfos:待播放的歌单列表
     * position：从这个数组集中的哪个位置开始播放
     * ? : 你的数据实体必须继承BaseMediaInfo，必须的字段看类中的属性注释
     */
    MusicPlayerManager.getInstance().startPlayMusic(List<?> mediaInfos,int position);
```
* 播放器自定义UI和交互说明：项目默认提供了一个播放器交互组件：MusicPlayerActivity，请参照集成。实现自己的UI请注册监听事件MusicPlayerManager.getInstance().addOnPlayerEventListener(this);<br/>

**权限声明**
```
    <!--网络状态检查-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!--锁屏防止CPU休眠，锁屏下继续缓冲。-->
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <!--以下权限非必须，若开启垃圾桶回收播放器、悬浮窗口播放、常驻内存、状态栏控制、锁屏播放控制、耳机监控 等功能，请开启已下权限-->
    <uses-permission android:name="android.permission.VIBRATE" />
    <protected-broadcast android:name="android.intent.action.MEDIA_MOUNTED" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.INSTANT_APP_FOREGROUND_SERVICE"/>
```
**添加混淆**
```
-keep class com.music.player.lib.bean.**{*;}
```
[音乐播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Screen/md/MusicPlayerReadme.md "音乐播放器API介绍"
**更多功能及使用详见Wiki文档：**[音乐播放器Wiki]
___

## 二.视频播放器集成步骤:
**1.在你的项目中的.xml中引入播放器布局</br>**
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
    <declare-styleable name="BaseVideoPlayer">
            <!--是否自动设置默认控制器-->
            <attr name="video_autoSetVideoController" format="boolean"/>
            <!--是否自动设置封面控制器-->
            <attr name="video_autoSetCoverController" format="boolean"/>
            <!--循环播放-->
            <attr name="video_loop" format="boolean"/>
    </declare-styleable>
```
也可以在java代码中动态初始化。<br/>
```
    FrameLayout frameLayout = (FrameLayout) findViewById(R.id.xxx);
    VideoPlayerTrackView playerTrackView=new VideoPlayerTrackView(context);
    playerTrackView.setVideoController(videoController);
    playerTrackView.setVideoCoverController(coverController);
    playerTrackView.setVideoGestureController(gestureController);
    frameLayout.addView(playerTrackView,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200dp,Gravity.CENTER));
```
**2.播放器初始化及基本数据设置**
```
    mVideoPlayer = (VideoPlayerTrackView) findViewById(R.id.video_player);
     //播放器控件宽高
    int itemHeight = MusicUtils.getInstance().getScreenWidth(this) * 9 / 16;
    mVideoPlayer.getLayoutParams().height=itemHeight;
    //开始准备播放
    mVideoPlayer.startPlayVideo(dataSource,title);
    //或者分两步开始播放
    //mVideoPlayer.setDataSource(dataSource,title,);
    //mVideoPlayer.startPlayVideo();

    //以下设置都是可选的，根据自己功能需求设置
    //会覆盖VideoPlayerManager的循环播放设置
    mVideoPlayer.setLoop(true);
    //如需在悬浮窗中支持点击全屏切换至播放器界面，此TAG必须绑定,假如你的播放器界面入参只需一个ID则可忽略此设置并调用setDataSource的三参方法
    mVideoPlayer.setParamsTag(mVideoParams);
    //设置画面渲染缩放模式,默认VideoConstants.VIDEO_DISPLAY_TYPE_CUT，详见VideoConstants常量定义
    mVideoPlayer.setVideoDisplayType(mode);
    //是否支持悬浮窗播放功能，这个开关只针对入口有效，不会限制对startGlobalWindown();的调用
    mVideoPlayer.setGlobaEnable(true); 或 mVideoPlayer.getVideoController().setGlobaEnable(true);
```
**3.Activity生命周期方法加入**
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
    public void onBackPressed() {
        //尝试弹射返回
        if(VideoPlayerManager.getInstance().isBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoPlayerManager.getInstance().onDestroy();
        //若你的Activity是MainActivity，则需要调用下面这两个方法，其他Activity在销毁时若需支持悬浮窗口播放，则勿需调用下面方法！！！
        VideoPlayerManager.getInstance().onDestroy();
        VideoWindowManager.getInstance().onDestroy();
    }
```
**权限声明：**
```
    <!--网络状态-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!--锁屏工作，防止休眠-->
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <!--悬浮窗-->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```
**拓展功能初始化**
```
    //根据需求初始化
    VideoPlayerManager.getInstance()
          //循环模式开关，在这里配置的循环模式，只是临时的,适合在播放过程中调用
          .setLoop(true)
          //悬浮窗播放器中跳转到Activity的绝对路径，若需要支持从悬浮窗中跳转到APP的播放器界面，则必须设置此路径
          .setVideoPlayerActivityClassName(VideoPlayerActviity.class.getCanonicalName());
```
至此你的播放器具备了基础的视频播放能力。<br/>

[视频播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Screen/md/VideoPlayerReadme.md "视频播放器API介绍"
**更多自定义和其他功能详见此Wiki文档：**[视频播放器Wiki]
## iMusic预览及下载:
**强烈建议集成前先下载体验此APP，根据APP中的功能对照对应的API集成开发！！<br/>**
* 此示例工程音乐搜索API取自《酷狗音乐》开放API，视频资源API取自《开眼视频》，在次致谢！<br/>
#### 功能快照预览:
<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162033.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162126.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162237.jpg" height="480" width="270">
</div>
<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190418_135654.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162345.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162453.jpg" height="480" width="270">
</div>

#### 下载地址:

[前往fir下载]: https://fir.im/iMusic "fir下载"
**fir托管下载：**[前往fir下载]

[仓库下载APK]: https://github.com/Yuye584312311/IMusic/blob/master/Screen/apk/iMusic.apk "apk下载"
**APK下载：**[仓库下载APK]<br/>
<br/>
**或者扫描二维码下载<br/>**
<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/code.png" height="300" width="300">
</div>
<br/>

* 集成中遇到问题请阅读Wiki,发现BUG欢迎issues。如有其他问题，欢迎邮箱：TinyHung@Outlook.com