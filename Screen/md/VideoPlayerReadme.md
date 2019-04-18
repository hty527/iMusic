## BaseVideoPlayer 常用API预览：
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

```
## VideoPlayerManager 常用API预览：