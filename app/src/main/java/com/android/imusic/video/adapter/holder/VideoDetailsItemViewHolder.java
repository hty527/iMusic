package com.android.imusic.video.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;

/**
 * hty_Yuye@Outlook.com
 * 2019/4/10
 */

public class VideoDetailsItemViewHolder extends RecyclerView.ViewHolder {

    public ImageView itemUserCover;
    public TextView itemTitle,itemDurtion,itemUserName,itemTime;
    public ImageView itemCover;
    public FrameLayout itemCoverRoot;

    public VideoDetailsItemViewHolder(View itemView) {
        super(itemView);
        itemCover = (ImageView) itemView.findViewById(R.id.view_item_cover);
        itemUserCover = (ImageView) itemView.findViewById(R.id.video_item_user_cover);
        itemTitle = (TextView) itemView.findViewById(R.id.view_item_title);
        itemUserName = (TextView) itemView.findViewById(R.id.view_item_user_name);
        itemDurtion = (TextView) itemView.findViewById(R.id.view_item_durtion);
        itemTime = (TextView) itemView.findViewById(R.id.view_item_time);
        itemCoverRoot = (FrameLayout) itemView.findViewById(R.id.view_item_cover_root);
    }
}