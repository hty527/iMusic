package com.video.player.lib.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.video.player.lib.R;
import com.video.player.lib.base.BaseCoverController;
import com.video.player.lib.utils.Logger;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * Video Default Cover Controller
 */

public class DefaultCoverController extends BaseCoverController {

    private static final String TAG = "DefaultCoverController";
    public ImageView mVideoCover;
    public TextView mPreCount;
    public TextView mPreDurtion;

    public DefaultCoverController(@NonNull Context context) {
        this(context,null);
    }

    public DefaultCoverController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DefaultCoverController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.video_default_cover_controller_layout,this);
        mVideoCover = (ImageView) findViewById(R.id.video_cover_icon);
        mPreCount = (TextView) findViewById(R.id.view_cover_count);
        mPreDurtion = (TextView) findViewById(R.id.view_cover_durtion);
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
        mPreCount=null;mPreDurtion=null;
    }
}