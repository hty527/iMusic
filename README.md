# **iMusic**

[Android音乐播放器封装]: https://www.jianshu.com/p/9051b96c02f9 "简书音频播放器"
**音乐播放器:**[Android音乐播放器封装]

[Android视频播放器封装]: https://www.jianshu.com/p/39d8f824c2fb "简书视频播放器"
**视频播放器:**[Android视频播放器封装]

基于MediaPlayer解码器封装的完整音乐播放器和视频播放器功能库，极简接入、功能较全面、体积小。欢迎Star！欢迎下载体验！<br/>
___
## 功能演示及概述:
#### 功能演示:(更多功能快照在结尾处)
* 音乐播放器预览(如播放不流畅请点击图片查看)</br>
<div>
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/video/Music.gif" height="480" width="270">
</div>

* 视频播放器预览</br>
<div>
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/video/Video.gif" height="480" width="270">
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
* 已对音频输出焦点管理作处理<br/>
***
**2.视频播放器**
* 自定义视频控制器</br>
* 自定义封面控制器</br>
* 自定义手势识别调节器</br>
* 支持4种画面缩放模式设置</br>
* 支持界面跳转无缝衔接播放</br>
* 迷你小窗口播放、支持屏幕中拖拽</br>
* 全局悬浮窗播放、支持屏幕全局拖拽</br>
* 全屏播放下手势识别调节音量、屏幕亮度、快进、快退</br>
* 全局悬浮窗播放器中无缝切换至Activity播放界面</br>
* 列表单例播放</br>
* 列表横竖屏切换</br>
* 常规横竖屏切换</br>
* 已对视频输出焦点管理作处理<br/>
## 一.音乐播放器集成:
**1.项目build.gradle中添加**
```
    dependencies {
        implementation 'com.imusic.player:music-player:1.0.2'
    }
```
**2.全局初始化**
```
    //Applicaion中初始化
    MusicPlayerManager.getInstance().init(getApplicationContext());
```
**3.MainActivity中初始化播放器服务组件**
```
    @Override
    protected void onCreate() {
        super.onCreate();
        //初始化内部服务组件
        MusicPlayerManager.getInstance().initialize(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //播放器反初始化
        MusicPlayerManager.getInstance().unInitialize(MainActivity.this);
        //如果你启用了内置悬浮窗口播放器，则还需要对其释放
        MusicWindowManager.getInstance().onDestroy();
    }
```
**4.开始播放你的音频任务**
```
    /**
     * audios:待播放的歌单列表,音频对象需继承BaseAudioInfo类，请阅读类中成员属性注解
     * position：开始播放的位置(位于audios中的index)
     */
    MusicPlayerManager.getInstance().startPlayMusic(List<?> audios,int position);
```
**5.权限声明**
```
    <!--网络状态检查-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!--锁屏下继续缓冲-->
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
```
**添加混淆**
```
    -keep class com.music.player.lib.bean.**{*;}
```
* Demo内置一套完整的UI交互播放器，请注册监听事件MusicPlayerManager.getInstance().addOnPlayerEventListener(this);并参照MusicPlayerActivity集成。<br/>

[音乐播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Doc/md/MusicPlayerReadme.md "音乐播放器API介绍"
**APP后台防杀死和更多功能Wiki文档：**[音乐播放器Wiki]
___

## 二.视频播放器集成:
**1.项目build.gradle中添加**
```
    dependencies {
        implementation 'com.imusic.player:video-player:1.0.1'
    }
```
**2.在你的项目中的.xml中引入播放器布局</br>**
```
    <com.video.player.lib.view.VideoPlayerTrackView
        android:id="@+id/video_track"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:video_autoSetCoverController="true"
        app:video_autoSetVideoController="true"/>
```
[视频播放器Wiki介绍]: https://github.com/Yuye584312311/IMusic/blob/master/Doc/md/VideoPlayerReadme.md "视频播放器API介绍"
你也可以在java代码中创建播放器，前往[视频播放器Wiki介绍]<br/>

**3.播放器初始化及基本数据设置**
```
    mVideoPlayer = (VideoPlayerTrackView) findViewById(R.id.video_player);
     //播放器控件高度设置，默认是match_parent
    mVideoPlayer.getLayoutParams().height=200dp;
    //开始准备播放
    mVideoPlayer.startPlayVideo(dataSource,title);
    //第二种姿势准备播放
    //mVideoPlayer.setDataSource(dataSource,title,);
    //mVideoPlayer.startPlayVideo();
```
**4.Activity生命周期方法加入**
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
        //如果你的Activity是MainActivity并且你开启过悬浮窗口播放器，则还需要对其释放
        VideoWindowManager.getInstance().onDestroy();
    }
```
**5.权限声明：**
```
    <!--网络状态-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!--锁屏工作，防止休眠-->
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <!--悬浮窗-->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```
**6.Activity Manifest文件配置：**
```
    <activity android:name="xxx.xxx.xxx.MainActivity"
        android:screenOrientation="portrait"
        <!--在你需要全屏播放的Activity中加上这个属性，告诉系统Activity在横竖屏切换时不要销毁Activity-->
        android:configChanges="orientation|screenSize">
    </activity>
```
至此基础的视频播放器项目集成完毕，更多高级功能和API请阅读文档。<br/>

[视频播放器Wiki]: https://github.com/Yuye584312311/IMusic/blob/master/Doc/md/VideoPlayerReadme.md "视频播放器API介绍"
**自定义交互和其他功能Wiki文档：**[视频播放器Wiki]
## iMusic预览及下载:
**强烈建议集成前先下载体验此APP，根据APP中的功能对照对应的API集成开发！！<br/>**
* 此示例工程音乐搜索API取自《酷狗音乐》开放API，视频资源API取自《开眼视频》。<br/>
#### 功能快照预览:
<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/screenshot/20190417_162033.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/screenshot/20190417_162126.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/screenshot/20190417_162237.jpg" height="480" width="270">
</div>
<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/screenshot/20190418_135654.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/screenshot/20190417_162345.jpg" height="480" width="270">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/screenshot/20190417_162453.jpg" height="480" width="270">
</div>

#### 下载地址:

[前往fir下载]: https://fir.im/iMusic "fir下载"
**fir托管下载：**[前往fir下载]

[仓库下载APK]: https://github.com/Yuye584312311/IMusic/blob/master/Doc/apk/iMusic-1.0.01.apk "apk下载"
**APK下载：**[仓库下载APK]<br/>
<br/>
**或者扫描二维码下载<br/>**
<div align="center">
    <img src="https://github.com/Yuye584312311/IMusic/blob/master/Doc/screenshot/code.png" height="300" width="300">
</div>
<br/>

* 集成中遇到问题请阅读Wiki,BUG提交欢迎issues。如有其他问题，联系邮箱：TinyHung@Outlook.com