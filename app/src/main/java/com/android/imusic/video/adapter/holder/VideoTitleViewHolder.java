package com.android.imusic.video.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.android.imusic.R;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/24
 */

public class VideoTitleViewHolder extends RecyclerView.ViewHolder {

    public TextView textTitle;
    public TextView textTitle2;
    public View rootTitle,rootTitle1;

    public VideoTitleViewHolder(View itemView) {
        super(itemView);
        textTitle = (TextView) itemView.findViewById(R.id.item_video_title);
        textTitle2 = (TextView) itemView.findViewById(R.id.item_video_title_2);
        rootTitle = itemView.findViewById(R.id.item_root_view);
        rootTitle1 = itemView.findViewById(R.id.item_root_view1);
    }
}