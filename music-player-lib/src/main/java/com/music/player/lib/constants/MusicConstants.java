package com.music.player.lib.constants;

/**
 * TinyHung@Outlook.com
 * 2019/3/5.
 * Constants
 */

public interface MusicConstants {

    //SP-KEY
    String SP_KEY_NAME ="music_player_config";
    String SP_KEY_ALARM_MODEL ="SP_ALARM_MODEL";
    String SP_KEY_PLAYER_MODEL ="SP_KEY_PLAYER_MODEL";
    //SP_VALUE
    //闹钟配置
    String SP_VALUE_ALARM_MODE_10 ="sp_value_alarm_mode_10";//10分钟
    String SP_VALUE_ALARM_MODE_15 ="sp_value_alarm_mode_15";//15分钟
    String SP_VALUE_ALARM_MODE_30 ="sp_value_alarm_mode_30";//30分钟
    String SP_VALUE_ALARM_MODE_60 ="sp_value_alarm_mode_60";//60分钟
    String SP_VALUE_ALARM_MODE_0 ="sp_value_alarm_mode_0";//无限时长
    String SP_VALUE_ALARM_MODE_CURRENT ="music_alarm_model_current";//播放完本首即自动关闭
    //播放模式配置
    String SP_VALUE_MUSIC_MODEL_SINGLE ="sp_value_music_model_single";//单曲模式
    String SP_VALUE_MUSIC_MODEL_LOOP ="sp_value_music_model_loop";//列表循环模式
    String SP_VALUE_MUSIC_MODEL_ORDER ="sp_value_music_model_order";//顺序播放
    String SP_VALUE_MUSIC_MODEL_RANDOM ="sp_value_music_model_random";//随机播放
    //ParamsKey
    String KEY_MUSIC_LIST = "KEY_MUSIC_LIST";
    String KEY_MUSIC_ID = "KEY_MUSIC_ID";
    String KEY_TAG_ID = "KEY_TAG_ID";
    String KEY_ALBUM_ANME = "KEY_ALBUM_ANME";
    String SP_FIRST_START ="sp_first_start";//第一次启动标记
    String SP_FIRST_SEARCH ="sp_first_search";//第一次使用搜索
    //Hand 起始角度
    float ROTATION_INIT_NEEDLE = -30;
    //图片截图屏幕宽高
    float BASE_SCREEN_WIDTH = (float) 1080.0;
//    float BASE_SCREEN_HEIGHT = (float) 1920.0;

    //唱针宽高、距离等比例
    float SCALE_NEEDLE_WIDTH = (float) (276.0 / BASE_SCREEN_WIDTH);
    float SCALE_NEEDLE_MARGIN_LEFT = (float) (500.0 / BASE_SCREEN_WIDTH);
    float SCALE_NEEDLE_PIVOT_X = (float) (50.0 / BASE_SCREEN_WIDTH);
    float SCALE_NEEDLE_PIVOT_Y = (float) (46.0 / BASE_SCREEN_WIDTH);
    float SCALE_NEEDLE_HEIGHT = (float) (413.0 / BASE_SCREEN_WIDTH);
    float SCALE_NEEDLE_MARGIN_TOP = (float) (46.0 / BASE_SCREEN_WIDTH);

    /**
     * 注意：SCALE_DISC_BG_MARGIN_TOP 和 SCALE_DISC_MARGIN_TOP 计算公式：
     * SCALE_DISC_BG_MARGIN_TOP= 你自己定义
     * SCALE_DISC_MARGIN_TOP = SCALE_DISC_BG_SIZE-SCALE_DISC_SIZE/2
     */
    //唱盘比例
    float SCALE_DISC_SIZE = (float) (824.0 / BASE_SCREEN_WIDTH);
    //唱盘背景比例
    float SCALE_DISC_BG_SIZE = (float) (828.0 / BASE_SCREEN_WIDTH);
    //唱盘距离顶部边距时
    float SCALE_DISC_MARGIN_TOP = (float) (190.0 / BASE_SCREEN_WIDTH);
    //唱盘背景距离顶部边距
    float SCALE_DISC_BG_MARGIN_TOP = (float) (186.0 / BASE_SCREEN_WIDTH);
    //专辑图片比例
    float SCALE_MUSIC_PIC_SIZE = (float) (544.0 / BASE_SCREEN_WIDTH);

    //锁屏唱盘比例
    float SCALE_DISC_LOCK_SIZE = (float) (680.0 / BASE_SCREEN_WIDTH);
    //锁屏专辑图片比例
    float SCALE_MUSIC_PIC_LOCK_SIZE = (float) (466.02 / BASE_SCREEN_WIDTH);

    //迷你唱盘比例
    float SCALE_DISC_MINI_SIZE = (float) (160.0 / BASE_SCREEN_WIDTH);
    //迷你专辑图片比例
    float SCALE_MUSIC_PIC_MINE_SIZE = (float) (95.0 / BASE_SCREEN_WIDTH);
    //胶盘旋转一圈耗时时长，单位秒
    int BOX_REVOLVE_MINUTE = 20;
    //迷你胶盘旋转一圈耗时时长，单位秒
    int BOX_MINI_REVOLVE_MINUTE = 20;
    //申请悬浮窗权限
    int REQUST_WINDOWN_PERMISSION = 2001;
    //唱针转动时间
    long DURATION_NEEDLE_ANIAMTOR = 500;
    //前台进程通知群组ID
    String CHANNEL_ID = "com.android.imusic.mediaplayer";
    /**
     * 通知交互ACTION
     */
    //点击通知栏
    String MUSIC_INTENT_ACTION_ROOT_VIEW = "IMUSIC_INTENT_ACTION_CLICK_ROOT_VIEW";
    //上一首
    String MUSIC_INTENT_ACTION_CLICK_LAST = "IMUSIC_INTENT_ACTION_CLICK_LAST";
    //下一首
    String MUSIC_INTENT_ACTION_CLICK_NEXT = "IMUSIC_INTENT_ACTION_CLICK_NEXT";
    //暂停、开始
    String MUSIC_INTENT_ACTION_CLICK_PAUSE = "IMUSIC_INTENT_ACTION_CLICK_PAUSE";
    //关闭通知栏
    String MUSIC_INTENT_ACTION_CLICK_CLOSE = "IMUSIC_INTENT_ACTION_CLICK_CLOSE";
    //收藏
    String MUSIC_INTENT_ACTION_CLICK_COLLECT = "IMUSIC_INTENT_ACTION_CLICK_COLLECT";
    //参数传入
    String MUSIC_KEY_MEDIA_ID = "MUSIC_KEY_MEDIA_ID";

    /**
     * 播放器内部各种状态，替换enum类型
     */
    int MUSIC_PLAYER_STOP    = 0; //已结束，或未开始
    int MUSIC_PLAYER_PREPARE = 1; //准备中
    int MUSIC_PLAYER_BUFFER  = 2; //缓冲中
    int MUSIC_PLAYER_PLAYING = 3; //播放中
    int MUSIC_PLAYER_PAUSE   = 4; //暂停
    int MUSIC_PLAYER_ERROR   = 5; //错误

    /**
     * 唱片机，替换enum类型
     */
    int JUKE_BOX_PLAY  = 0;//开始
    int JUKE_BOX_PAUSE = 1;//暂停
    int JUKE_BOX_STOP  = 2;//停止
    /**
     * 定时闹钟档次，替换enum类型
     */
    int MUSIC_ALARM_MODEL_10       = 1;//10分钟后
    int MUSIC_ALARM_MODEL_15       = 2;
    int MUSIC_ALARM_MODEL_30       = 3;
    int MUSIC_ALARM_MODEL_60       = 4;
    int MUSIC_ALARM_MODEL_CURRENT  = 5;//当前歌曲播放完成立即结束
    int MUSIC_ALARM_MODEL_0        = 0;//无限期
    /**
     * 播放器内部处理的数据来源标识，替换enum类型
     */
    //网络、专辑、推荐
    int CHANNEL_NET       = 0;
    //本地
    int CHANNEL_LOCATION  = 1;
    //历史记录
    int CHANNEL_HISTROY   = 2;
    //网络搜索
    int CHANNEL_SEARCH    = 3;
    //收藏记录
    int CHANNEL_COLLECT   = 4;

    /**
     * 播放模式，替换enum类型
     */
    int MUSIC_MODEL_LOOP    = 0;//列表循环模式
    int MUSIC_MODEL_SINGLE  = 1;//单曲模式
    int MUSIC_MODEL_ORDER   = 2;//顺序播放
    int MUSIC_MODEL_RANDOM  = 3;//随机播放
}