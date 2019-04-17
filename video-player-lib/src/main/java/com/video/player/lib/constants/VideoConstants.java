package com.video.player.lib.constants;

/**
 * TinyHung@Outlook.com
 * 2019/4/10
 */

public interface VideoConstants {

    /**
     * 播放器方向
     */
    //常规
    int SCREEN_ORIENTATION_PORTRAIT = 0;
    //全屏
    int SCREEN_ORIENTATION_FULL = 1;
    //小窗口
    int SCREEN_ORIENTATION_TINY = 2;
    //悬浮窗口
    int SCREEN_ORIENTATION_WINDOW = 3;

    /**
     * 缩放类型
     */
    //全屏铺满
    int VIDEO_DISPLAY_TYPE_FILL_PARENT = 1;
    //全屏缩放
    int VIDEO_DISPLAY_TYPE_FILL_SCROP = 2;
    //原始状态
    int VIDEO_DISPLAY_TYPE_ORIGINAL = 3;

    /**
     * 视频入参KEY
     */
    String KEY_VIDEO_PARAMS = "video_params";
    String KEY_VIDEO_PLAYING = "video_playing";
    String KEY_VIDEO_URL = "video_url";
    String KEY_VIDEO_TITLE = "video_title";

    String VIDEO_HEADER = "video_header";
    //更多标题
    String ITEM_TITLE_FOOTER = "footer2";
    //热门所有排行
    String HOST_TOP_ALL = "ranklist";
    //播放器界面路径
    String MUSIC_PLAYER_CLASS_NAME = "com.android.imusic.video.activity.VideoPlayerActviity";
}
