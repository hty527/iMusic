package com.video.player.lib.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.video.player.lib.R;
import com.video.player.lib.base.BaseCoverController;
import com.video.player.lib.utils.Logger;

/**
 * TinyHung@Outlook.com
 * 2019/4/11
 * Video Details Cover Controller
 */

public class DetailsCoverController extends BaseCoverController {

    private static final String TAG = "DetailsCoverController";
    public ImageView mVideoCover;

    public DetailsCoverController(@NonNull Context context) {
        this(context,null);
    }

    public DetailsCoverController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DetailsCoverController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.video_details_cover_controller_layout,this);
        mVideoCover = (ImageView) findViewById(R.id.video_cover_icon);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Logger.d(TAG,"onFinishInflate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mVideoCover){
            mVideoCover.setImageResource(0);
            mVideoCover=null;
        }
    }
}