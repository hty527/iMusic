package com.android.imusic.music.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;
import com.android.imusic.music.bean.MusicDetails;
import com.music.player.lib.adapter.base.BaseAdapter;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.util.MusicUtils;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * Music Details Adapter
 */

public class MusicDetailsAdapter extends BaseAdapter<MusicDetails,MusicDetailsAdapter.MusicHolderView> {

    public MusicDetailsAdapter(Context context, List<MusicDetails> data) {
        super(context,data);
    }

    @Override
    public MusicHolderView inCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new MusicHolderView(mInflater.inflate(R.layout.music_details_item_list, null));
    }

    @Override
    public void inBindViewHolder(MusicHolderView viewHolder, final int position) {
        MusicDetails itemData = getItemData(position);
        if(null!=itemData){
            viewHolder.textTitle.setText(Html.fromHtml(itemData.getTitle()));
            viewHolder.itemIcon.setImageResource(itemData.getIcon());
            viewHolder.itemView.setTag(itemData);
            if(itemData.getItemID()==MusicDetails.ITEM_ID_COLLECT&&itemData.getId()>0){
                //已收藏不允许点击
                boolean isExist= MusicUtils.getInstance().isExistCollectHistroy(itemData.getId());
                if(isExist){
                    viewHolder.textTitle.setTextColor(Color.parseColor("#AAAAAA"));
                    viewHolder.itemIcon.setColorFilter(Color.parseColor("#AAAAAA"));
                    viewHolder.itemView.setTag(null);
                }
            }else if(itemData.getItemID()==MusicDetails.ITEM_ID_NEXT_PLAY){
                //播放器未开始播放过不允许播放
                List<?> currentPlayList = MusicPlayerManager.getInstance().getCurrentPlayList();
                if(null==currentPlayList|currentPlayList.size()<=0){
                    viewHolder.textTitle.setTextColor(Color.parseColor("#AAAAAA"));
                    viewHolder.itemIcon.setColorFilter(Color.parseColor("#AAAAAA"));
                    viewHolder.itemView.setTag(null);
                }
            }else if(itemData.getItemID()==MusicDetails.ITEM_ID_SHARE){
                //没有地址不支持分享
                if(TextUtils.isEmpty(itemData.getPath())){
                    viewHolder.textTitle.setTextColor(Color.parseColor("#AAAAAA"));
                    viewHolder.itemIcon.setColorFilter(Color.parseColor("#AAAAAA"));
                    viewHolder.itemView.setTag(null);
                }
            }
        }
    }

    public class MusicHolderView extends RecyclerView.ViewHolder{
        private TextView textTitle;
        private ImageView itemIcon;

        public MusicHolderView(View itemView) {
            super(itemView);
            textTitle = (TextView) itemView.findViewById(R.id.view_item_title);
            itemIcon = (ImageView) itemView.findViewById(R.id.view_item_icon);
        }
    }
}