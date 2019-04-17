package com.android.imusic.video.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.video.activity.VideoPlayerActviity;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.android.imusic.video.bean.VideoParams;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.music.player.lib.util.MusicUtils;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.view.VideoTextrueProvider;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 */

public class TransformerMoiveItem extends FrameLayout {

    private ImageView mRoundImageView;

    public TransformerMoiveItem(@NonNull Context context) {
        this(context,null);
    }

    public TransformerMoiveItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.video_pager_item,this);
    }

    public void setData(OpenEyesIndexItemBean item) {
        if(null!=item&&null!=item.getData()){
            OpenEyesIndexItemBean data = item.getData().getContent().getData();
            ((TextView) findViewById(R.id.music_tr_item_title)).setText(data.getTitle());
//            TextView tvDurtion = (TextView) findViewById(R.id.music_tr_item_durtion);
//            tvDurtion.setText(MusicUtils.getInstance().stringForAudioTime(data.getDuration()*1000));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                findViewById(R.id.view_root_item).setOutlineProvider(new VideoTextrueProvider(MusicUtils.getInstance().dpToPxInt(getContext(),8f)));
            }
            mRoundImageView = (ImageView) findViewById(R.id.music_tr_item_cover);
            Glide.with(getContext())
                    .load(data.getCover().getFeed())
                    .placeholder(R.drawable.ic_video_default_cover)
                    .error(R.drawable.ic_video_default_cover)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mRoundImageView);
            this.setTag(data);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(null!=view.getTag()){
                        OpenEyesIndexItemBean indexItemBean = (OpenEyesIndexItemBean) view.getTag();
                        Intent intent=new Intent(getContext().getApplicationContext(), VideoPlayerActviity.class);
                        VideoParams videoParams= MediaUtils.getInstance().formatVideoParams(indexItemBean);
                        intent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,videoParams);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().getApplicationContext().startActivity(intent);
                    }
                }
            });
        }
    }

    public void onDestroy() {
        if(null!=mRoundImageView){
            mRoundImageView.setImageResource(0);
            mRoundImageView=null;
        }
    }
}