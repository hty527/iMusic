package com.android.imusic.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.imusic.R;
import com.android.imusic.music.adapter.holder.MusicListViewHolder;
import com.android.imusic.music.bean.SearchResultInfo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.music.player.lib.adapter.base.BaseAdapter;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.util.MusicUtils;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * Search Music Adapter
 */

public class MusicSearchAdapter extends BaseAdapter<SearchResultInfo,MusicListViewHolder> {

    private MusicOnItemClickListener mListener;
    private int mCurrentPosition;
    private String mCurrentKey;

    public MusicSearchAdapter(Context context, @Nullable List<SearchResultInfo> data, MusicOnItemClickListener listener) {
        super(context,data);
        this.mListener=listener;
    }

    @Override
    public MusicListViewHolder inCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View inflate = mInflater.inflate(R.layout.music_item_music_list, null);
        return new MusicListViewHolder(inflate);
    }

    /**
     * 全量更新
     * @param viewHolder
     * @param position
     */
    @Override
    public void inBindViewHolder(final MusicListViewHolder viewHolder, int position) {
        SearchResultInfo itemData = getItemData(position);
        if(null!=itemData){
            viewHolder.textTitle.setText(Html.fromHtml(MusicUtils.getInstance().formatSearchContent(
                    TextUtils.isEmpty(itemData.getSongname())?itemData.getFilename():itemData.getSongname(),mCurrentKey)));
            String anchorContent = MusicUtils.getInstance().formatSearchContent(itemData.getSingername()+" "+itemData.getAlbum_name(),mCurrentKey);
            viewHolder.textAnchor.setText(Html.fromHtml(anchorContent));
            //封面设置
            if(!TextUtils.isEmpty(itemData.getAlbum_img())){
                Glide.with(getContext())
                        .load(itemData.getAlbum_img())
                        .asBitmap()
                        .error(R.drawable.ic_music_default_cover)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(new BitmapImageViewTarget(viewHolder.imageCover) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                super.setResource(resource);
                            }
                        });
            }else{
                viewHolder.imageCover.setImageResource(R.drawable.ic_music_default_cover);
            }
            boolean isPlaying=false;
            BaseMediaInfo currentPlayerMusic = MusicPlayerManager.getInstance().getCurrentPlayerMusic();
            if(null!=currentPlayerMusic&&!TextUtils.isEmpty(currentPlayerMusic.getHashKey())&&currentPlayerMusic.getHashKey().equals(itemData.getHash())){
                isPlaying=true;
                mCurrentPosition =viewHolder.getAdapterPosition();
            }
            itemData.setSelected(isPlaying);
            viewHolder.itemPlayingStatus.setVisibility(itemData.isSelected()?View.VISIBLE:View.GONE);
            viewHolder.itemLine.setVisibility(position==(getData().size()-1)?View.INVISIBLE:View.VISIBLE);
            viewHolder.itemRootView.setTag(itemData);
            viewHolder.itemMenu.setTag(itemData);
            //条目点击事件
            viewHolder.itemRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null!=mListener){
                        mListener.onItemClick(v,viewHolder.getAdapterPosition(),1);
                    }
                }
            });
            //菜单点击事件
            viewHolder.itemMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null!=mListener){
                        mListener.onItemClick(v,viewHolder.getAdapterPosition(),0);
                    }
                }
            });
        }
    }

    /**
     * 局部刷新
     * @param viewHolder
     * @param position
     * @param payloads
     */
    @Override
    protected void inBindViewHolder(MusicListViewHolder viewHolder, int position, List<Object> payloads) {
        super.inBindViewHolder(viewHolder, position, payloads);
        SearchResultInfo itemData = getItemData(position);
        if(null!=itemData){
            viewHolder.itemPlayingStatus.setVisibility(itemData.isSelected()?View.VISIBLE:View.GONE);
            viewHolder.itemRootView.setTag(itemData);
        }
    }

    public String getCurrentKey() {
        return mCurrentKey;
    }

    public void setCurrentKey(String currentKey) {
        mCurrentKey = currentKey;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.mCurrentPosition = currentPosition;
    }

    /**
     * 刷新单个条目
     * @param position
     */
    public void notifyDataSetChanged(int position){
        List<SearchResultInfo> data = getData();
        data.get(mCurrentPosition).setSelected(false);
        data.get(position).setSelected(true);
        notifyItemChanged(mCurrentPosition,"NITIFY_DATA");
        notifyItemChanged(position,"NITIFY_ITEM");
        mCurrentPosition=position;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener=null;mCurrentPosition=0;mCurrentKey=null;
    }
}