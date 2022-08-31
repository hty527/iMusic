package com.video.player.lib.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.video.player.lib.R;
import com.video.player.lib.base.BaseVideoPlayer;
import com.video.player.lib.controller.DefaultGestureController;
import com.video.player.lib.controller.DefaultVideoController;
import com.video.player.lib.controller.DetailsCoverController;

/**
 * TinyHung@Outlook.com
 * 2019/4/11
 * Video Default Controller
 */

public class VideoDetailsPlayerTrackView extends BaseVideoPlayer<DefaultVideoController,
        DetailsCoverController,DefaultGestureController>{

    @Override
    protected int getLayoutID() {
        return R.layout.video_default_track_layout;
    }

    public VideoDetailsPlayerTrackView(@NonNull Context context) {
        this(context,null);
    }

    public VideoDetailsPlayerTrackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoDetailsPlayerTrackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}