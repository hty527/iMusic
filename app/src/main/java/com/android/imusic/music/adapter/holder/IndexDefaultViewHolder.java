package com.android.imusic.music.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/24
 */

public class IndexDefaultViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageCover;
    public TextView textTitle;
    public TextView textDesp;
    public View itemLine,playingStatus;

    public IndexDefaultViewHolder(View itemView) {
        super(itemView);
        imageCover = (ImageView) itemView.findViewById(R.id.view_item_icon);
        textTitle = (TextView) itemView.findViewById(R.id.view_item_title);
        textDesp = (TextView) itemView.findViewById(R.id.view_item_desp);
        itemLine =  itemView.findViewById(R.id.view_item_line);
        playingStatus =  itemView.findViewById(R.id.view_playing_status);
    }
}