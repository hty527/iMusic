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

#### 视频播放器:
1.写一个类继承BaseVideoPlayer，给定layout xml文件。或者使用默认的播放器。在你的项目中的.xml中引入播放器布局</br>
```
      <com.video.player.lib.view.VideoPlayerTrackView
            android:id="@+id/video_track"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:video_autoSetCoverController="true"
            app:video_autoSetVideoController="true"/>
```
    自定义属性 video_autoSetCoverController 和 video_autoSetVideoController可选，表示是否创建默认的封面控制器和视频播放器控制器。</br>
    支持的自定义属性：
```
        <!--是否自动设置默认控制器-->
        <attr name="video_autoSetVideoController" format="boolean"/>
        <!--是否自动设置封面控制器-->
        <attr name="video_autoSetCoverController" format="boolean"/>
        <!--循环播放-->
        <attr name="video_loop" format="boolean"/>
```
更多自定义属性请阅读video-player-lib模块下的attrs中的BaseVideoPlayer说明。</br>
</br>
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
    至此你的播放器具备了基础的视频播放能力,更多API使用，请阅读下文。



