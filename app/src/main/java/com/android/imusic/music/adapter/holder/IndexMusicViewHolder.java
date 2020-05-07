package com.android.imusic.music.adapter.holder;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;
import com.android.imusic.music.view.LayoutProvider;
import com.music.player.lib.util.MusicUtils;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 */

public class IndexMusicViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageCover;
    public TextView textTitle;
    public TextView textAnchor;
    public View itemRootView;

    public IndexMusicViewHolder(View itemView) {
        super(itemView);
        imageCover = (ImageView) itemView.findViewById(R.id.view_item_cover);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageCover.setOutlineProvider(new LayoutProvider(MusicUtils.getInstance().dpToPxInt(imageCover.getContext(),5f)));
        }
        textTitle = (TextView) itemView.findViewById(R.id.view_item_title);
        textAnchor = (TextView) itemView.findViewById(R.id.view_item_anchor);
        itemRootView = itemView.findViewById(R.id.item_root_view);
    }
}
