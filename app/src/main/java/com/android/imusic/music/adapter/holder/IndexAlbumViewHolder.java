package com.android.imusic.music.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.android.imusic.R;
import com.music.player.lib.view.MusicRoundImageView;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 */

public class IndexAlbumViewHolder extends RecyclerView.ViewHolder {

    public MusicRoundImageView imageCover;
    public TextView textTitle;
    public TextView textAnchor;
    public View itemRootView;

    public IndexAlbumViewHolder(View itemView) {
        super(itemView);
        imageCover = (MusicRoundImageView) itemView.findViewById(R.id.view_item_cover);
        textTitle = (TextView) itemView.findViewById(R.id.view_item_title);
        textAnchor = (TextView) itemView.findViewById(R.id.view_item_anchor);
        itemRootView = itemView.findViewById(R.id.item_root_view);
    }
}
