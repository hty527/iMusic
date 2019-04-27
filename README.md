# **iMusic**

[Android音频播放器开发记录]: https://www.jianshu.com/p/9051b96c02f9 "简书音频播放器"
**音乐播放器博文:**[Android音频播放器开发记录]

[Android视频播放器开发记录]: https://www.jianshu.com/p/39d8f824c2fb "简书视频播放器"
**视频播放器博文:**[Android视频播放器开发记录]

完整的 网络音乐播放器 和 网络视频播放器 封装库及工程演示
___
## 功能演示及概述:
#### 功能演示:(部分功能快照预览在最下面)
![image](https://github.com/Yuye584312311/IMusic/blob/master/Screen/video/video.gif)
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
* A activity 跳转至B activity无缝衔接播放</br>
* 支持可切换至迷你小窗口播放，支持Activity内拖拽</br>
* 支持可切换至全局悬浮窗播放，支持屏幕全局拖拽</br>
* 全屏播放下手势识别调节音量、屏幕亮度、快进、快退</br>
* 支持完全自定义视频控制器</br>
* 支持完全自定义封面控制器</br>
* 支持完全自定义手势识别调节器</br>
* 支持全局悬浮窗播放器中无缝切换至播放器界面</br>
## 集成步骤:
### 1.音乐播放器集成步骤:
**权限声明：**
```
    //网络状态
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    //锁屏防止CPU休眠。锁屏下继续缓冲，
    <uses-permission android:name="android.permission.WAKE_LOCK"/>

    /** 以下权限非必须，若开启垃圾桶回收播放器、悬浮窗口播放、常驻内存、状态栏控制、锁屏播放控制、耳机监控 等功能，请开启已下权限 */
    //垃圾桶回收播放器使用到了震动权限
    <uses-permission android:name="android.permission.VIBRATE" />
    <protected-broadcast android:name="android.intent.action.MEDIA_MOUNTED" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.INSTANT_APP_FOREGROUND_SERVICE"/>
```

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
    /**
     * 音乐播放器初始化设置
     */
    MusicPlayerConfig config=MusicPlayerConfig.Build()

        //是否启用前台服务、常驻进程
        .setLockForeground(true)

        //是否启用悬浮窗自动靠边吸附
        .setWindownAutoScrollToEdge(true)

        //是否启用垃圾桶回收播放器
        .setTrashEnable(true)

        //是否启用锁屏控制播放
        .setScreenOffEnable(true)

        //悬浮窗样式：垃圾桶回收样式，默认时点击悬浮窗右上角X按钮回收
        .setWindownStyle(MusicWindowStyle.TRASH);

    //设置给媒体播放管理者
    MusicPlayerManager.getInstance().setMusicPlayerConfig(config);

    //配置点击通知栏跳转至Activity的绝对路径，若支持此功能，则必须设置！
    MusicPlayerManager.getInstance().setForegroundOpenActivityClassName(MusicPlayerActivity.class.getCanonicalName());
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
        //若集成视频播放器，还需要调用以下方法，仅在MainActivity中调用即可
        VideoPlayerManager.getInstance().onDestroy();
        //悬浮窗销毁
        VideoWindowManager.getInstance().onDestroy();
    }
```
**3.开始播放任务**
```
    /**
     * mediaInfos:待播放的数组集，交给内部播放器
     * position：从这个数组集中的哪个位置开始播放
     * ? : 你的数据实体必须继承BaseMediaInfo
     */
    MusicPlayerManager.getInstance().startPlayMusic(List<?> mediaInfos,int position);
```

* 播放器自定义UI和交互说明：项目默认提供了一个播放器交互组件：MusicPlayerActivity，请参照集成。如需自定义，请注册监听事件MusicPlayerManager.getInstance().addOnPlayerEventListener(this);实现自己的逻辑。<br/>
##### 添加混淆
```
-keep class com.music.player.lib.bean.**{*;}
```
##### 播放器内部协调工作说明：<br/>
 * MusicPlayerService：内部播放器服务组件，负责音频的播放、暂停、停止、上一首、下一首、闹钟定时关闭等工作
 * MusicPlayerActivity：播放器容器，处理MusicPlayerService抛出交互事件,负责用户交互。
 * MusicPlayerManager：内部播放器代理人，组件与播放器交互需经此代理人访问播放器内部功能。
 * MusicJukeBoxView：默认唱片机
 * MusicJukeBoxBackgroundLayout：默认播放器UI背景协调工作者
 * MusicJukeBoxCoverPager：默认唱片机封面
 * MusicAlarmSettingDialog：默认定制闹钟设置
 * MusicPlayerListDialog：默认当前正在播放的列表<br/>

[音乐播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Screen/md/MusicPlayerReadme.md "MusicPlayerManagerAPI介绍"
**Wiki文档：**[音乐播放器Wiki]
___

### 2.视频播放器集成步骤:
**权限声明：**
```
    //网络状态
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    //锁屏工作，防止休眠
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    //悬浮窗
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

##### 全局初始化
```
    VideoPlayerManager.getInstance()
          //循环模式开关，在这里配置的循环模式，只是临时的，开始播放时，会被BaseVideoPlayer的属性覆盖！！！，这个循环设置适合在播放过程中调用
          .setLoop(true)
          //悬浮窗播放器中跳转到Activity的绝对路径，若需要支持从悬浮窗中跳转到APP的播放器界面，则必须设定此路径
          .setVideoPlayerActivityClassName(VideoPlayerActviity.class.getCanonicalName());
```
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
也可以在java代码中动态初始化：其他BaseVideoPlayer相关的API后面统一介绍。<br/>
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
**2.设置播放器控件的宽高及基本数据设置**
```
    //播放器控件宽高
    mVideoPlayer = (VideoPlayerTrackView) findViewById(R.id.video_player);
    int itemHeight = MusicUtils.getInstance().getScreenWidth(this) * 9 / 16;
    mVideoPlayer.getLayoutParams().height=itemHeight;
    //是否循环播放，会覆盖VideoPlayerManager的循环设置
    mVideoPlayer.setLoop(true);
    //可选的设置，如在悬浮窗中需要支持切换至播放器界面，此TAG必须绑定,假如你的播放器界面入参只需一个ID则可忽略此设置并调用setDataSource的三参方法
    mVideoPlayer.setParamsTag(mVideoParams);
    //设置画面渲染缩放模式,详见VideoConstants常量定义
    mVideoPlayer.setVideoDisplayType(mode);
    //设置资源和准备播放
    mVideoPlayer.setDataSource(dataSource,title,);
    mVideoPlayer.startPlayVideo();
    //或者直接调用startPlayVideo准备播放
    //mVideoPlayer.startPlayVideo(dataSource,title);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
• 此库提供了一套默认的播放器和UI，如需自定义播放器交互UI，请继承BaseVideoPlayer、BaseVideoController、BaseCoverController，此处演示默认的播放器继承步骤，更多自定义组件和功能请阅下文。<br/>

至此你的播放器具备了基础的视频播放能力,自定义UI和API使用，请参阅读下文。<br/>

##### 自定义交互UI的实现
**• 支持可自定义的控制器说明：**
```
    播放器交互控制器基类 作用：播放过程中的缓冲、暂停、开始、全屏、悬浮窗等功能UI交互。
    BaseVideoController
    播放器封面控制器基类 作用：播放器开始播放前的上层封面图层，如视频的封面样式、时间显示、播放次数等。
    BaseCoverController
    播放器手势调节控制器基类 作用：播放器打开全屏播放后，识别用户手势调节快进、快退、音量、屏幕亮度等功能UI回显。
    BaseGestureController
```
**• 重点：实现自定义交互UI，需继承BaseVideoController抽象类，初始化完成后调用BaseVideoPlayer的setVideoController(V videoController);绑定交互UI控制器。<br/>**
除了实现必须的抽象方法外，还有诸如迷你窗口、悬浮窗口、全屏窗口 的特有状态方法，可按照需求实现，详见BaseVideoController成员方法。
##### 视频播放所有功能和公开API介绍
除了继承BaseVideoController实现全屏、迷你窗口、全局悬浮窗、悬浮窗切换至播放器界面、弹射返回等功能外，还可以直接调用BaseVideoPlayer的公开方法实现以上功能和交互。BaseVideoPlayer的主要公开方法如下:<br/>

[视频播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Screen/md/VideoPlayerReadme.md "BaseVideoPlayer API 介绍"
**Wiki文档：**[视频播放器Wiki]
___
## iMusic预览及下载:
**强烈建议集成前先下载体验此APP，根据APP中的功能对照对应的API集成开发！！<br/>**
* 此示例工程音乐搜索API取自酷狗音乐，视频资源API取自开眼视频<br/>
#### 功能快照预览:
<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162033.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162121.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162126.jpg" height="480" width="270">
</div>

<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190417_162237.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190418_135644.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190418_135654.jpg" height="480" width="270">
</div>

<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/20190418_134651.jpg" height="480" width="270">
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