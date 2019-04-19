# **iMusic**
[BaseMediaPlayer]: https://www.jianshu.com/u/6a64162caadd "简书主页"
**博文地址**[BaseMediaPlayer]<br/>
<br/>
    完整的 网络音乐播放器 和 网络视频播放器 封装库及工程演示
<br/>
___
## 功能演示及概述:
#### 功能演示:
![image](https://github.com/Yuye584312311/IMusic/blob/master/Screen/video/video.gif)
#### 功能概述:
**1.音乐播放器**
* 完整的音乐播放器功能，包括但不限于：**</br>
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
* 完整的视频播放器功能，包括但不限于：</br>
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
### 音乐播放器集成步骤:

##### 播放器内部协调工作说明：<br/>
 * MusicPlayerService：内部播放器服务组件，负责音频的播放、暂停、停止、上一首、下一首、闹钟定时关闭等工作
 * MusicPlayerActivity：播放器容器，监听内部播放器状态，负责处理当前正在播放的任务、刷新进度、处理MusicPlayerService抛出交互事件
 * MusicPlayerManager：内部播放器代理人，所有组件与播放器交互或指派任务给播放器，需经此代理人进行
 * MusicJukeBoxView：默认唱片机
 * MusicJukeBoxBackgroundLayout：默认播放器UI背景协调工作者
 * MusicJukeBoxCoverPager：默认唱片机封面
 * MusicAlarmSettingDialog：默认定制闹钟设置
 * MusicPlayerListDialog：默认当前正在播放的列表<br/>

**1.全局初始化**
```
    //初始化首选项，播放器内部的播放模式、定时模式存储，使用的是SharedPreferences
    MusicUtils.getInstance().initSharedPreferencesConfig(getApplicationContext());
    //全局迷你悬浮窗单击事件
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
    //音乐播放器初始化设置
    MusicPlayerConfig config=MusicPlayerConfig.Build()
        //前台服务锁定开关
        .setLockForeground(true)
        //悬浮窗自动吸附开关
        .setWindownAutoScrollToEdge(true)
        //垃圾桶功能开关
        .setTrashEnable(true)
        //锁屏控制器开关
        .setScreenOffEnable(true)
        //悬浮窗播放器样式
        .setWindownStyle(MusicWindowStyle.TRASH);
    MusicPlayerManager.getInstance().setMusicPlayerConfig(config);

    //若想要点击通知栏跳转至播放器界面，则必须设置点击通知栏打开的Activity绝对路径
    MusicPlayerManager.getInstance().setForegroundOpenActivityClassName(MusicPlayerActivity.class.getCanonicalName());
```
**2.Activity中初始化MusicPlayerService组件，对应生命周期方法调用**
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
        MediaUtils.getInstance().onDestroy();
        MusicPlayerManager.getInstance().unBindService(MainActivity.this);
        MusicPlayerManager.getInstance().onDestroy();
        //若集成视频播放器，需调用以下方法
        VideoPlayerManager.getInstance().onDestroy();
        VideoWindowManager.getInstance().onDestroy();
    }
```
**3.开始播放任务**
```
    //设置播放内部正在处理的数据渠道，可用于主页回显正在“哪个”模块播放音乐，非必须的
    MusicPlayerManager.getInstance().setPlayingChannel(MusicPlayingChannel.CHANNEL_LOCATION);
    //开始播放，一个数组，数组元素徐继承BaseMediaInfo类，必须赋值字段请看成员属性注释，入参请看"MusicPlayerManager常用API"
    MusicPlayerManager.getInstance().startPlayMusic(mAdapter.getData(),position);
```
* 播放器自定义UI和交互说明：项目默认提供了一个播放器交互组件：MusicPlayerActivity，请参照集成。如需自定义，请注册监听事件MusicPlayerManager.getInstance().addOnPlayerEventListener(this);实现自己的逻辑。<br/>

[音乐播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Screen/md/MusicPlayerReadme.md "MusicPlayerManagerAPI介绍"
**Wiki文档：**[音乐播放器Wiki]

### 视频播放器集成步骤:
• 此库提供了一套默认的播放器和UI，如需自定义播放器交互UI，请继承BaseVideoPlayer、BaseVideoController、BaseCoverController，此处演示默认的播放器继承步骤，更多自定义组件和功能请阅下文。
##### 全局初始化
```
    VideoPlayerManager.getInstance()
          //循环模式
          .setLoop(true)
          //全局悬浮窗播放器中打开APP的播放器界面的绝对路径，可选的参数,若需要支持从悬浮窗中跳转到APP的播放器界面，则必须设定此参数
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
**支持的自定义属性说明：**
```
    <!--是否自动设置默认控制器-->
    <attr name="video_autoSetVideoController" format="boolean"/>
    <!--是否自动设置封面控制器-->
    <attr name="video_autoSetCoverController" format="boolean"/>
    <!--循环播放-->
    <attr name="video_loop" format="boolean"/>
```
**也可以在java代码中动态初始化：其他BaseVideoPlayer相关的API后面统一介绍。<br/>**
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
    mVideoPlayer = (VideoDetailsPlayerTrackView) findViewById(R.id.video_player);
    int itemHeight = MusicUtils.getInstance().getScreenWidth(this) * 9 / 16;
    mVideoPlayer.getLayoutParams().height=itemHeight;
    //设置播放资源,setDataSource方法为重载方法，请参阅内部方法说明
    mVideoPlayer.setDataSource(mVideoParams.getVideoUrl(),mVideoParams.getVideoTitle(),mVideoParams.getVideoiId());
    //是否循环播放，和VideoPlayerManager的setLoop是等效作用
    mVideoPlayer.setLoop(true);
    //可选的设置，如在悬浮窗中需要支持切换至播放器界面，此TAG必须绑定,假如你的播放器界面入参只需一个ID则可忽略此设置并调用setDataSource的三参方法
    mVideoPlayer.setParamsTag(mVideoParams);
    //基本参数设定完毕后即可调用此方法自动开始准备播放
    mVideoPlayer.starPlaytVideo();
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
        if(VideoPlayerManager.getInstance().isBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoPlayerManager.getInstance().onDestroy();
        //若你的Activity是MainActivity，则还需要调用这两个方法,其他Activity在销毁时若支持悬浮窗口播放，则勿需调用！
        VideoPlayerManager.getInstance().onDestroy();
        VideoWindowManager.getInstance().onDestroy();
    }
```
至此你的播放器具备了基础的视频播放能力,自定义UI和API使用，请参阅读下文。<br/>
##### 自定义交互UI的实现
**支持可自定义的控制器说明：**
```
    播放器交互控制器基类 作用：播放过程中的缓冲、暂停、开始、全屏、悬浮窗等功能UI交互。<br/>
    BaseVideoController
    播放器封面控制器基类 作用：播放器开始播放前的上层封面图层，如视频的封面样式、时间显示、播放次数等。<br/>
    BaseCoverController
    播放器手势调节控制器基类 作用：播放器打开全屏播放后，识别用户手势调节快进、快退、音量、屏幕亮度等功能UI回显。<br/>
    BaseGestureController
```
**• 重点：实现自定义交互UI，需继承BaseVideoController抽象类，初始化完成后调用BaseVideoPlayer的setVideoController(V videoController);绑定交互UI控制器。<br/>**
除了实现必须的抽象方法外，还有诸如迷你窗口、悬浮窗口、全屏窗口 的特有状态方法，可按照需求实现，详见BaseVideoController成员方法。
##### 视频播放所有功能和公开API介绍
除了继承BaseVideoController实现全屏、迷你窗口、全局悬浮窗、悬浮窗切换至播放器界面、弹射返回等功能外，还可以直接调用BaseVideoPlayer的公开方法实现以上功能和交互。BaseVideoPlayer的主要公开方法如下:<br/>

[视频播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Screen/md/VideoPlayerReadme.md "BaseVideoPlayer API 介绍"
**Wiki文档介绍**[视频播放器Wiki]
___
### 体验APK下载:
![扫描二维码下载](https://github.com/Yuye584312311/IMusic/blob/master/Screen/image/code.png)

[Fir托管下载](https://fir.im/iMusic)
</br>
[或点此下载](https://github.com/Yuye584312311/IMusic/blob/master/Screen/apk/iMusic.apk)