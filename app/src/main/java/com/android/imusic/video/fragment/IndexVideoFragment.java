package com.android.imusic.video.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.base.MusicBaseFragment;
import com.android.imusic.music.net.MusicNetUtils;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.video.activity.VideoListActivity;
import com.android.imusic.video.activity.VideoPlayerActviity;
import com.android.imusic.video.adapter.VideoIndexVideoAdapter;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.android.imusic.video.ui.contract.IndexVideoContract;
import com.android.imusic.video.ui.presenter.IndexVideoPersenter;
import com.video.player.lib.bean.VideoParams;
import com.music.player.lib.adapter.base.OnLoadMoreListener;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.utils.VideoUtils;
import com.video.player.lib.view.VideoPlayerTrackView;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * Index Video
 * 主页视频列表，采用“开眼视频”的API做数据支持，演示列表播放的功能
 */

public class IndexVideoFragment extends MusicBaseFragment<IndexVideoPersenter>
        implements MusicOnItemClickListener, IndexVideoContract.View {

    private static final String TAG = "IndexVideoFragment";
    private VideoIndexVideoAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean refreshFinish=false;
    private int mPage=0;
    private PopupWindow mPopupWindow;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private View mAnchorView;

    @Override
    protected int getLayoutID() {
        return R.layout.video_fragment_index_video;
    }

    @Override
    protected void initViews() {
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {}

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if(null!=view.getTag()&& view.getTag() instanceof OpenEyesIndexItemBean){
                    OpenEyesIndexItemBean indexItemBean= (OpenEyesIndexItemBean) view.getTag();
                    if(null!=indexItemBean.getAuthor()){
                        VideoPlayerTrackView playerTrackView = (VideoPlayerTrackView) view.findViewById(R.id.video_track);
                        if(null!=playerTrackView){
                            playerTrackView.onReset();
                        }
                    }
                }
            }
        });
        mAdapter = new VideoIndexVideoAdapter(getContext(),null,this);
        //菜单事件
        mAdapter.setOnMenuClickListener(new VideoIndexVideoAdapter.OnMenuClickListener() {
            @Override
            public void onMenuClick(View itemView,View clickView) {
                if(null!=clickView.getTag() && clickView.getTag() instanceof OpenEyesIndexItemBean){
                    final OpenEyesIndexItemBean indexItemBean= (OpenEyesIndexItemBean) clickView.getTag();
                    VideoParams videoParams= MediaUtils.getInstance().formatVideoParams(indexItemBean);
                    videoParams.setHeadTitle("相关推荐");
                    showPropupMenu(itemView,clickView,videoParams);
                }
            }
        });
        //加载更多
        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if(null!=mPresenter&&!mPresenter.isRequsting()){
                    mPage++;
                    loadData();
                }
            }
        },recyclerView);
        recyclerView.setAdapter(mAdapter);

        mAnchorView = getView().findViewById(R.id.view_anchor);
        mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipre_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setProgressViewOffset(false,0,200);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                loadData();
            }
        });
    }

    @Override
    protected IndexVideoPersenter createPresenter() {
        return new IndexVideoPersenter();
    }

    /**
     * 条目单击
     * @param view
     * @param posotion
     * @param musicID
     */
    @Override
    public void onItemClick(View view, int posotion, long musicID) {
        if(null!=view.getTag() && view.getTag() instanceof OpenEyesIndexItemBean){
            OpenEyesIndexItemBean indexItemBean = (OpenEyesIndexItemBean) view.getTag();
            Logger.d(TAG,"ITEM_TYPE:"+indexItemBean.getItemType());
            if(indexItemBean.getItemType()==OpenEyesIndexItemBean.ITEM_TITLE){
                if(TextUtils.equals(VideoConstants.ITEM_TITLE_FOOTER,indexItemBean.getType())
                        &&!TextUtils.isEmpty(indexItemBean.getActionUrl())){
                    String url=VideoUtils.getInstance().formatActionUrl(indexItemBean.getActionUrl());
                    VideoPlayerManager.getInstance().onReset();
                    Intent intent=new Intent(getActivity(), VideoListActivity.class);
                    intent.putExtra(VideoConstants.KEY_VIDEO_TITLE,indexItemBean.getText());
                    intent.putExtra(VideoConstants.KEY_VIDEO_URL,url);
                    startActivity(intent);
                }
            }else if(indexItemBean.getItemType()==OpenEyesIndexItemBean.ITEM_BANNER){

            }else{
                if(null!=indexItemBean.getAuthor()){
                    VideoPlayerTrackView trackView = (VideoPlayerTrackView) view.findViewById(R.id.video_track);
                    VideoParams videoParams= MediaUtils.getInstance().formatVideoParams(indexItemBean);
                    Intent intent=new Intent(getActivity(), VideoPlayerActviity.class);
                    intent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,videoParams);
                    if(null!=trackView&&trackView.isWorking()){
                        //界面衔接播放前，一定要设置此标记，用来区分Activity的onResume();事件
                        VideoPlayerManager.getInstance().setContinuePlay(true);
                        trackView.reset();
                        intent.putExtra(VideoConstants.KEY_VIDEO_PLAYING,true);
                    }else{
                        VideoPlayerManager.getInstance().onReset();
                    }
                    startActivity(intent);
                }
            }
        }
    }

    /**
     * 在某个锚点显示弹窗
     * @param itemView ItemView
     * @param clickView 锚点View,这里使用mAnchorView做屏幕的锚点,弹窗位置出现在按钮的左侧
     * @param indexItemBean
     */
    private void showPropupMenu(final View itemView, final View clickView, final VideoParams indexItemBean) {
        View view = View.inflate(getActivity(), R.layout.video_popup_window_layout, null);
        view.findViewById(R.id.tv_item_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                new android.support.v7.app.AlertDialog.Builder(getActivity())
                        .setTitle("描述信息")
                        .setMessage(indexItemBean.getVideoDesp())
                        .setPositiveButton("关闭", null).show();
            }
        });
        view.findViewById(R.id.tv_item_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                if(null!=indexItemBean){
                    VideoPlayerTrackView trackView = (VideoPlayerTrackView) itemView.findViewById(R.id.video_track);
                    Intent intent=new Intent(getActivity(), VideoPlayerActviity.class);
                    intent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,indexItemBean);
                    if(null!=trackView&&trackView.isWorking()){
                        VideoPlayerManager.getInstance().setContinuePlay(true);
                        trackView.reset();
                        intent.putExtra(VideoConstants.KEY_VIDEO_PLAYING,true);
                    }else{
                        VideoPlayerManager.getInstance().onReset();
                    }
                    startActivity(intent);
                }
            }
        });
        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setFocusable(true);//获得焦点，才能让View里的点击事件生效
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopupWindow=null;
            }
        });
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width,width);
        mMeasuredWidth = view.getMeasuredWidth();
        mMeasuredHeight = view.getMeasuredHeight();
        int[] locations=new int[2];
        clickView.getLocationOnScreen(locations);
        //X:控件在屏幕的X轴-弹窗总宽度 Y:控件在屏幕的X轴-弹窗总高度
        int startX=locations[0]-mMeasuredWidth;
        int startY=locations[1]+clickView.getMeasuredHeight()-mMeasuredHeight;
        //如果现实之后的Y轴到达了屏幕的状态栏或者之上，反过来显示
        if(startY< MusicUtils.getInstance().getStatusBarHeight(getActivity())){
            startY=locations[1]+(clickView.getMeasuredHeight()/2);
        }
        Logger.d(TAG,"showPropupMenu-->viewX:"+locations[0]+",viewY:"+locations[1]
                +",startX:"+startX+",startY:"+startY+",viewW:"+mMeasuredWidth+",viewH:"+mMeasuredHeight);
        if(null==mAnchorView){
            mAnchorView=getView().findViewById(R.id.view_anchor);
        }
        mPopupWindow.showAsDropDown(mAnchorView,startX ,startY);
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(!refreshFinish&&null!=mAdapter&&null!=mPresenter&&!mPresenter.isRequsting()){
            mPage=0;
            loadData();
        }else{
            VideoPlayerManager.getInstance().onResume();
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        VideoPlayerManager.getInstance().onPause();
    }

    /**
     * 加载音频列表
     */
    private void loadData() {
        if(null!=mPresenter) {
            mPresenter.getIndexVideos(mPage);
        }
    }

    /**
     * 加载中
     */
    @Override
    public void showLoading() {
        if(0==mPage&&null!=mSwipeRefreshLayout&&!mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    /**
     * 异常
     * @param code 0：为空 -1：失败
     * @param errorMsg 描述信息
     */
    @Override
    public void showError(int code, String errorMsg) {
        if(null!=mSwipeRefreshLayout){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        if(code==MusicNetUtils.API_RESULT_EMPTY){
            mAdapter.onLoadEnd();
        }else{
            if(mPage>-1){
                mPage--;
            }
            mAdapter.onLoadError();
        }
        Toast.makeText(getContext(),errorMsg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示视频列表
     * @param data 视频列表
     */
    @Override
    public void showVideos(List<OpenEyesIndexItemBean> data) {
        refreshFinish=true;
        if(null!=mSwipeRefreshLayout){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        if(null!=mAdapter){
            mAdapter.onLoadComplete();
            if(mPage==0){
                mAdapter.setNewData(data);
            }else{
                mAdapter.addData(data);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mSwipeRefreshLayout){
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout=null;
        }
        if(null!=mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
    }
}