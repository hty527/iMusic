package com.android.imusic.video.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.imusic.R;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.video.adapter.holder.VideoBannerViewHolder;
import com.android.imusic.video.adapter.holder.VideoCardViewHolder;
import com.android.imusic.video.adapter.holder.VideoTitleViewHolder;
import com.android.imusic.video.adapter.holder.VideoVideoViewHolder;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.music.player.lib.adapter.base.BaseAdapter;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.model.MusicGlideCircleTransform;
import com.music.player.lib.util.MusicUtils;
import com.video.player.lib.bean.VideoParams;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.utils.VideoUtils;
import com.video.player.lib.view.VideoTextrueProvider;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * Index List Adapter
 */

public class VideoIndexVideoAdapter extends BaseAdapter<OpenEyesIndexItemBean,RecyclerView.ViewHolder> {

    private final int mScreenWidth;
    private final MusicOnItemClickListener mItemClickListener;

    public VideoIndexVideoAdapter(Context context, @Nullable List<OpenEyesIndexItemBean> data, MusicOnItemClickListener onItemClickListener) {
        super(context,data);
        this.mItemClickListener=onItemClickListener;
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(context);
    }

    @Override
    public int getItemCount() {
        return null==mData?0:mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(null!=getData()){
            return getData().get(position).getItemType();
        }
        return OpenEyesIndexItemBean.ITEM_UNKNOWN;
    }

    @Override
    public RecyclerView.ViewHolder inCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType== OpenEyesIndexItemBean.ITEM_CARD){
            View inflate = mInflater.inflate(R.layout.video_index_video_card, null);
            return new VideoCardViewHolder(inflate);
        }else if(viewType== OpenEyesIndexItemBean.ITEM_TITLE){
            View inflate = mInflater.inflate(R.layout.video_index_video_title, null);
            return new VideoTitleViewHolder(inflate);
        }else if(viewType== OpenEyesIndexItemBean.ITEM_FOLLOW){
            View inflate = mInflater.inflate(R.layout.video_index_video_video, null);
            return new VideoVideoViewHolder(inflate);
        }else if(viewType== OpenEyesIndexItemBean.ITEM_VIDEO){
            View inflate = mInflater.inflate(R.layout.video_index_video_video, null);
            return new VideoVideoViewHolder(inflate);
        }else if(viewType== OpenEyesIndexItemBean.ITEM_NOIMAL){
            View inflate = mInflater.inflate(R.layout.video_index_video_video, null);
            return new VideoVideoViewHolder(inflate);
        }else if(viewType== OpenEyesIndexItemBean.ITEM_BANNER){
            View inflate = mInflater.inflate(R.layout.video_index_video_banner, null);
            return new VideoBannerViewHolder(inflate);
        }
        return new UnKnownView(mInflater.inflate(R.layout.music_unknown_layout, null));
    }

    @Override
    public void inBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        viewHolder.itemView.setTag(null);
        int itemViewType = getItemViewType(position);
        OpenEyesIndexItemBean itemData = getItemData(position);
        if(null!=itemData){
            //精品推荐
            if(itemViewType==OpenEyesIndexItemBean.ITEM_CARD){
                VideoCardViewHolder cardViewHolder= (VideoCardViewHolder) viewHolder;
                if(null!=itemData.getData()){
                    cardViewHolder.mTransformerVideoPager.setDatas(itemData.getData().getItemList(),0);
                }
            //标题
            }else if(itemViewType==OpenEyesIndexItemBean.ITEM_TITLE){
                VideoTitleViewHolder titleViewHolder= (VideoTitleViewHolder) viewHolder;
                if(null!=itemData.getData()){
                    itemData.getData().setItemType(OpenEyesIndexItemBean.ITEM_TITLE);
                    viewHolder.itemView.setTag(itemData.getData());
                    //区别不同的标题
                    if(itemData.getData().getType().equals(VideoConstants.ITEM_TITLE_FOOTER)){
                        titleViewHolder.rootTitle.setVisibility(View.GONE);
                        titleViewHolder.rootTitle1.setVisibility(View.VISIBLE);
                        titleViewHolder.textTitle2.setText(itemData.getData().getText()+">");
                    }else{
                        titleViewHolder.rootTitle1.setVisibility(View.GONE);
                        titleViewHolder.rootTitle.setVisibility(View.VISIBLE);
                        titleViewHolder.textTitle.setText(itemData.getData().getText());
                    }
                    titleViewHolder.rootTitle1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(null!=mItemClickListener){
                                mItemClickListener.onItemClick(viewHolder.itemView,position,0);
                            }
                        }
                    });
                }
            //推荐视频
            }else if(itemViewType==OpenEyesIndexItemBean.ITEM_FOLLOW){
                VideoVideoViewHolder videoViewHolder= (VideoVideoViewHolder) viewHolder;
                if(null!=itemData.getData()&&null!=itemData.getData().getContent()){
                    OpenEyesIndexItemBean indexItemBean = itemData.getData().getContent().getData();
                    setItemVideoData(videoViewHolder,indexItemBean,position);
                }
            //视频
            }else if(itemViewType==OpenEyesIndexItemBean.ITEM_VIDEO){
                VideoVideoViewHolder videoViewHolder= (VideoVideoViewHolder) viewHolder;
                if(null!=itemData.getData()){
                    OpenEyesIndexItemBean indexItemBean = itemData.getData();
                    setItemVideoData(videoViewHolder,indexItemBean,position);
                }
            //播放器界面相关推荐
            }else if(itemViewType==OpenEyesIndexItemBean.ITEM_NOIMAL){
                VideoVideoViewHolder videoViewHolder= (VideoVideoViewHolder) viewHolder;
                setItemVideoData(videoViewHolder,itemData,position);
            //Banner
            }else if(itemViewType==OpenEyesIndexItemBean.ITEM_BANNER){
                VideoBannerViewHolder bannerViewHolder= (VideoBannerViewHolder) viewHolder;
                if(null!=itemData.getData()){
                    int itemHeight = (mScreenWidth - MusicUtils.getInstance().dpToPxInt(getContext(), 30f)) * 9 / 16;
                    bannerViewHolder.itemBannerCover.getLayoutParams().height=itemHeight;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bannerViewHolder.itemBannerRoot.setOutlineProvider(new VideoTextrueProvider(VideoUtils.getInstance().dpToPxInt(getContext(),8f)));
                    }
                    OpenEyesIndexItemBean itemDataData = itemData.getData();
                    itemDataData.setItemType(OpenEyesIndexItemBean.ITEM_BANNER);
                    bannerViewHolder.itemView.setTag(itemDataData);
                    if(itemData.getType().equals("banner2")){
                        bannerViewHolder.itemBannerTag.setText("活动");
                    }else{
                        bannerViewHolder.itemBannerTag.setText("广告");
                    }
                    Glide.with(getContext())
                            .load(MusicUtils.getInstance().formatImageUrl(itemDataData.getImage()))
                            .placeholder(R.drawable.ic_video_default_cover)
                            .error(R.drawable.ic_video_default_cover)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(bannerViewHolder.itemBannerCover);
                }
            }
        }
    }

    /**
     * 更新视频条目数据
     * @param videoViewHolder
     * @param indexItemBean
     */
    private void setItemVideoData(final VideoVideoViewHolder videoViewHolder, OpenEyesIndexItemBean indexItemBean, final int position) {
        int itemHeight = (mScreenWidth - MusicUtils.getInstance().dpToPxInt(getContext(), 30f)) * 9 / 16;
        videoViewHolder.trackVideo.setWorking(false);
        videoViewHolder.trackVideo.setScrrenOrientation(VideoConstants.SCREEN_ORIENTATION_PORTRAIT);
        videoViewHolder.trackVideo.getLayoutParams().height=itemHeight;
        videoViewHolder.trackVideo.setGlobaEnable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            videoViewHolder.itemRoomItem.setOutlineProvider(new VideoTextrueProvider(VideoUtils.getInstance().dpToPxInt(getContext(),8f)));
        }
        //这里将条目View抛出，界面处理无缝衔接播放
        videoViewHolder.itemView.setTag(indexItemBean);
        videoViewHolder.menuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mItemClickListener){
                    mItemClickListener.onItemClick( videoViewHolder.itemView,position,0);
                }
            }
        });
        videoViewHolder.trackVideo.setDataSource(indexItemBean.getPlayUrl(),indexItemBean.getTitle(),indexItemBean.getId());
        VideoParams videoParams= MediaUtils.getInstance().formatVideoParams(indexItemBean);
        videoViewHolder.trackVideo.setParamsTag(videoParams);
        if(null!=videoViewHolder.trackVideo.getCoverController()){
            //视频时长
            videoViewHolder.trackVideo.getCoverController().mPreDurtion.setText(MusicUtils.getInstance().stringForAudioTime(indexItemBean.getDuration()*1000));
            //观看人次
            if(null!=indexItemBean.getConsumption()){
                videoViewHolder.trackVideo.getCoverController().mPreCount.setText(indexItemBean.getConsumption().getReplyCount()+"人观看");
            }
        }
        videoViewHolder.itemMenu.setTag(null);
        if(!TextUtils.isEmpty(indexItemBean.getDescription())){
            videoViewHolder.itemMenu.setVisibility(View.VISIBLE);
            videoViewHolder.itemMenu.setTag(indexItemBean);
            videoViewHolder.itemMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null!=mOnMenuClickListener){
                        mOnMenuClickListener.onMenuClick(videoViewHolder.itemView,v);
                    }
                }
            });
        }else{
            videoViewHolder.itemMenu.setVisibility(View.INVISIBLE);
        }
        videoViewHolder.itemTitle.setText(indexItemBean.getTitle());
        if(null!=indexItemBean.getCover()&&null!=videoViewHolder.trackVideo.getCoverController()){
            //封面
            Glide.with(getContext())
                    .load(indexItemBean.getCover().getFeed())
                    .placeholder(R.drawable.ic_video_default_cover)
                    .error(R.drawable.ic_video_default_cover)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(videoViewHolder.trackVideo.getCoverController().mVideoCover);
        }
        if(null!=indexItemBean.getAuthor()){
            //用户头像
            Glide.with(getContext())
                    .load(indexItemBean.getAuthor().getIcon())
                    .error(R.drawable.ic_music_default_cover)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new MusicGlideCircleTransform(getContext()))
                    .into(videoViewHolder.itemUserCover);
        }else{
            videoViewHolder.itemUserCover.setImageResource(0);
        }
    }

    private class UnKnownView extends RecyclerView.ViewHolder{

        public UnKnownView(View itemView) {
            super(itemView);
        }
    }

    public interface OnMenuClickListener{
        void onMenuClick(View itemView,View view);
    }

    private OnMenuClickListener mOnMenuClickListener;

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}