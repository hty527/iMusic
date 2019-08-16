package com.android.imusic.video.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;

/**
 * hty_Yuye@Outlook.com
 * 2019/4/8
 */

public class VideoBannerViewHolder extends RecyclerView.ViewHolder {

    public ImageView itemBannerCover;
    public TextView itemBannerTag;
    public FrameLayout itemBannerRoot;

    public VideoBannerViewHolder(View itemView) {
        super(itemView);
        itemBannerCover = (ImageView) itemView.findViewById(R.id.video_item_banner);
        itemBannerTag = (TextView) itemView.findViewById(R.id.item_banner_tag);
        itemBannerRoot = (FrameLayout) itemView.findViewById(R.id.item_root_view);
    }
}