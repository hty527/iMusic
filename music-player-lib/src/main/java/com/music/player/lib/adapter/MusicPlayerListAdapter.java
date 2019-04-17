package com.music.player.lib.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.music.player.lib.R;
import com.music.player.lib.adapter.base.BaseAdapter;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.util.MusicUtils;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/8
 * Player List
 */

public class MusicPlayerListAdapter extends BaseAdapter<BaseMediaInfo,MusicPlayerListAdapter.MusicHolderView> {

    public MusicPlayerListAdapter(Context context, List<BaseMediaInfo> data) {
        super(context,data);
    }

    @Override
    public MusicHolderView inCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new MusicHolderView(mInflater.inflate(R.layout.music_item_player_list, null));
    }

    @Override
    public void inBindViewHolder(MusicHolderView viewHolder, int position) {
        BaseMediaInfo itemData = getItemData(position);
        if(null!=itemData){
            String subString = MusicUtils.getInstance().subString(itemData.getVideo_desp(), 16);
            viewHolder.textTitle.setText(subString);
            viewHolder.textSubTitle.setText(itemData.getNickname());
            if(itemData.isSelected()){
                viewHolder.textTitle.setTextColor(Color.parseColor("#F8E71C"));
                viewHolder.textSubTitle.setTextColor(Color.parseColor("#F8E71C"));
            }else{
                viewHolder.textTitle.setTextColor(Color.parseColor("#FFFFFF"));
                viewHolder.textSubTitle.setTextColor(Color.parseColor("#FFFFFF"));
            }
            viewHolder.itemView.setTag(itemData);
        }
    }

    @Override
    protected void inBindViewHolder(MusicHolderView viewHolder, int position, List<Object> payloads) {
        super.inBindViewHolder(viewHolder, position, payloads);
        BaseMediaInfo itemData = getItemData(position);
        if(null!=itemData){
            String subString = MusicUtils.getInstance().subString(itemData.getVideo_desp(), 16);
            viewHolder.textTitle.setText(subString);
            if(itemData.isSelected()){
                viewHolder.textTitle.setTextColor(Color.parseColor("#F8E71C"));
                viewHolder.textSubTitle.setTextColor(Color.parseColor("#F8E71C"));
            }else{
                viewHolder.textTitle.setTextColor(Color.parseColor("#FFFFFF"));
                viewHolder.textSubTitle.setTextColor(Color.parseColor("#FFFFFF"));
            }
            viewHolder.itemView.setTag(itemData);
        }
    }

    public class MusicHolderView extends RecyclerView.ViewHolder{
        private TextView textTitle;
        private TextView textSubTitle;

        public MusicHolderView(View itemView) {
            super(itemView);
            textTitle = (TextView) itemView.findViewById(R.id.view_item_title);
            textSubTitle = (TextView) itemView.findViewById(R.id.view_item_subtitle);
        }
    }
}