package com.android.imusic.video.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.android.imusic.R;
import com.android.imusic.video.view.TransformerVideoPager;

/**
 * hty_Yuye@Outlook.com
 * 2019/4/8
 */

public class VideoCardViewHolder extends RecyclerView.ViewHolder {

    public TransformerVideoPager mTransformerVideoPager;

    public VideoCardViewHolder(View itemView) {
        super(itemView);
        mTransformerVideoPager = (TransformerVideoPager) itemView.findViewById(R.id.trans_video);
    }
}