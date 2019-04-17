package com.android.imusic.video.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.imusic.R;
import com.music.player.lib.view.MusicRoundImageView;
import com.video.player.lib.view.VideoPlayerTrackView;

/**
 * hty_Yuye@Outlook.com
 * 2019/4/8
 */

public class VideoVideoViewHolder extends RecyclerView.ViewHolder {

    public MusicRoundImageView itemUserCover;
    public TextView itemTitle;
    public ImageView itemMenu;
    public VideoPlayerTrackView trackVideo;
    public View menuLayout;
    public LinearLayout itemRoomItem;

    public VideoVideoViewHolder(View itemView) {
        super(itemView);
        itemUserCover = (MusicRoundImageView) itemView.findViewById(R.id.video_item_user_cover);
        itemTitle = (TextView) itemView.findViewById(R.id.view_item_title);
        itemMenu = (ImageView) itemView.findViewById(R.id.view_item_menu);
        trackVideo = (VideoPlayerTrackView) itemView.findViewById(R.id.video_track);
        menuLayout = (View) itemView.findViewById(R.id.ll_menu_layout);
        itemRoomItem = (LinearLayout) itemView.findViewById(R.id.view_root_item);
    }
}