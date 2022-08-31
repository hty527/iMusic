package com.music.player.lib.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.music.player.lib.R;
import com.music.player.lib.adapter.base.BaseAdapter;
import com.music.player.lib.bean.MusicAlarmSetting;
import com.music.player.lib.util.MusicUtils;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/7
 * 闹钟设置
 */

public class MusicAlarmAdapter extends BaseAdapter<MusicAlarmSetting,MusicAlarmAdapter.MusicHolderView> {

    private int mItemWidth;

    public MusicAlarmAdapter(Context context, List<MusicAlarmSetting> data) {
        super(context,data);
        int screenWidth = MusicUtils.getInstance().getScreenWidth(context);
        mItemWidth = (screenWidth - MusicUtils.getInstance().dpToPxInt(context, 92f)) /4;
    }

    @Override
    public MusicHolderView inCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View inflate = mInflater.inflate(R.layout.music_re_item_alarm_setting, null);
        return new MusicHolderView(inflate);
    }

    @Override
    public void inBindViewHolder(MusicHolderView viewHolder, int position) {
        MusicAlarmSetting itemData = getItemData(position);
        if(null!=itemData){
            MusicAlarmSetting alarmSetting = mData.get(position);
            viewHolder.textTitle.setText(alarmSetting.getTitle());
            viewHolder.itemView.setTag(alarmSetting);
        }
    }

    public class MusicHolderView extends RecyclerView.ViewHolder{
        private TextView textTitle;
        private RelativeLayout itemRootView;

        public MusicHolderView(View itemView) {
            super(itemView);
            textTitle = (TextView) itemView.findViewById(R.id.view_item_title);
            itemRootView = (RelativeLayout) itemView.findViewById(R.id.view_item_root);
            itemRootView.getLayoutParams().height=mItemWidth;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItemWidth=0;
    }
}