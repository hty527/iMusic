package com.android.imusic.video.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.imusic.R;
import com.android.imusic.video.bean.VideoParams;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicRoundImageView;


/**
 * TinyHung@Outlook.com
 * 2019/4/10
 * Video Details Header
 */

public class VideoDetailsHeaderView extends LinearLayout {

    private static final String TAG = "VideoDetailsHeaderView";
    private boolean isShowDetailsText=false;
    private TextView mDespShort;
    private TextView mDespLong;
    private ImageView mBtnDetails;

    public VideoDetailsHeaderView(Context context) {
        this(context,null);
    }

    public VideoDetailsHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.video_details_head_layout,this);
        OnClickListener onClickListener=new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_desp_short:
                    case R.id.tv_desp_long:
                    case R.id.view_btn_details:
                        if (isShowDetailsText) {
                            mDespShort.setVisibility(View.VISIBLE);
                            mDespLong.setVisibility(View.GONE);
                            mBtnDetails.setImageResource(R.drawable.ic_desp_down);
                        } else {
                            mDespShort.setVisibility(View.GONE);
                            mDespLong.setVisibility(View.VISIBLE);
                            mBtnDetails.setImageResource(R.drawable.ic_desp_up);
                        }
                        isShowDetailsText = ! isShowDetailsText;
                        break;
                }
            }
        };
        mDespShort = (TextView) findViewById(R.id.tv_desp_short);
        mDespLong = (TextView) findViewById(R.id.tv_desp_long);
        mBtnDetails = (ImageView) findViewById(R.id.view_btn_details);
        mDespShort.setOnClickListener(onClickListener);
        mDespLong.setOnClickListener(onClickListener);
        mBtnDetails.setOnClickListener(onClickListener);
    }

    public void setVideoDetailsData(VideoParams data) {
        if(null!=data){
            TextView videoTitle = (TextView) findViewById(R.id.header_video_title);
            MusicRoundImageView userIcon = (MusicRoundImageView) findViewById(R.id.header_user_icon);
            TextView videoAnchor = (TextView) findViewById(R.id.header_user_name);
            TextView userDesp = (TextView) findViewById(R.id.header_user_desp);
            //视频基本信息
            videoTitle.setText(data.getVideoTitle());
            videoAnchor.setText(data.getNickName());
            userDesp.setText("发表于"+ MusicUtils.getInstance().getTimeNow(data.getLastTime()));
            if(!TextUtils.isEmpty(data.getHeadTitle())){
                TextView headerTitle = (TextView) findViewById(R.id.view_header_title);
                headerTitle.setText(data.getHeadTitle());
            }
            if(null!=mDespShort){
                mDespShort.setText(data.getVideoDesp());
                mDespLong.setText(data.getVideoDesp());
                if(null!=mBtnDetails) mBtnDetails.setVisibility(VISIBLE);
            }
            //用户头像
            Glide.with(getContext())
                    .load(data.getUserFront())
                    .asBitmap()
                    .error(R.drawable.ic_music_default_cover)
                    .dontAnimate()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(userIcon) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            super.setResource(resource);
                        }
                    });
        }
    }

    public void onDestroy() {
        isShowDetailsText=false;mDespShort=null;mDespLong=null;mBtnDetails=null;
    }
}