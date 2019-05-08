package com.android.imusic.video.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.base.BaseActivity;
import com.android.imusic.base.BaseEngin;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.video.adapter.VideoIndexVideoAdapter;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.android.imusic.video.ui.contract.IndexVideoContract;
import com.android.imusic.video.ui.presenter.IndexVideoPersenter;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicCommentTitleView;
import com.video.player.lib.bean.VideoParams;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.utils.VideoUtils;
import com.video.player.lib.view.VideoPlayerTrackView;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/11
 * Video List
 */

public class VideoListActivity extends BaseActivity<IndexVideoPersenter>
        implements MusicOnItemClickListener, IndexVideoContract.View {

    private VideoIndexVideoAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPage=0;
    private PopupWindow mPopupWindow;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private MusicCommentTitleView mTitleView;
    private String mUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity_video_list);
        initViews();
        mUrl = getIntent().getStringExtra(VideoConstants.KEY_VIDEO_URL);
        if(TextUtils.isEmpty(mUrl)){
            Toast.makeText(VideoListActivity.this,"缺少必要参数",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mTitleView.setTitle(VideoUtils.getInstance().formatTitleByTitle(
                getIntent().getStringExtra(VideoConstants.KEY_VIDEO_TITLE)));
        mPresenter.getVideosByUrl(mUrl,mPage);
    }

    @Override
    protected IndexVideoPersenter createPresenter() {
        return new IndexVideoPersenter();
    }

    /**
     * 初始化组件
     */
    private void initViews() {
        mTitleView = (MusicCommentTitleView) findViewById(R.id.title_view);
        mTitleView.setOnTitleClickListener(new MusicCommentTitleView.OnTitleClickListener() {
            @Override
            public void onBack(View view) {
                onBackPressed();
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(
                VideoListActivity.this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {}

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if(null!=view.getTag()&& view.getTag() instanceof OpenEyesIndexItemBean){
                    VideoPlayerTrackView playerTrackView = (VideoPlayerTrackView) view.findViewById(R.id.video_track);
                    if(null!=playerTrackView){
                        playerTrackView.onReset();
                    }
                }
            }
        });
        mAdapter = new VideoIndexVideoAdapter(VideoListActivity.this,null,this);
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

        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipre_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(null!=mPresenter){
                    mPage=0;
                    mPresenter.getVideosByUrl(mUrl,mPage);
                }
            }
        });
    }

    /**
     * 条目单机事件
     * @param view
     * @param posotion
     * @param musicID
     */
    @Override
    public void onItemClick(View view, int posotion, long musicID) {
        if(null!=view.getTag() && view.getTag() instanceof OpenEyesIndexItemBean){
            OpenEyesIndexItemBean indexItemBean = (OpenEyesIndexItemBean) view.getTag();
            if(null!=indexItemBean.getAuthor()){
                VideoPlayerTrackView trackView = (VideoPlayerTrackView) view.findViewById(R.id.video_track);
                VideoParams videoParams= MediaUtils.getInstance().formatVideoParams(indexItemBean);
                Intent intent=new Intent(VideoListActivity.this, VideoPlayerActviity.class);
                intent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,videoParams);
                if(null!=trackView&&trackView.isWorking()){
                    VideoPlayerManager.getInstance().setContinuePlay(true);
                    trackView.reset();
                    intent.putExtra(VideoConstants.KEY_VIDEO_PLAYING,true);
                }
                startActivity(intent);
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
        View view = View.inflate(VideoListActivity.this, R.layout.video_popup_window_layout, null);
        view.findViewById(R.id.tv_item_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                new android.support.v7.app.AlertDialog.Builder(VideoListActivity.this)
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
                    Intent intent=new Intent(VideoListActivity.this, VideoPlayerActviity.class);
                    intent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,indexItemBean);
                    if(null!=trackView&&trackView.isWorking()) {
                        VideoPlayerManager.getInstance().setContinuePlay(true);
                        trackView.reset();
                        intent.putExtra(VideoConstants.KEY_VIDEO_PLAYING, true);
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
        if(startY< MusicUtils.getInstance().getStatusBarHeight(VideoListActivity.this)){
            startY=locations[1]+(clickView.getMeasuredHeight()/2);
        }
        Logger.d(TAG,"showPropupMenu-->viewX:"+locations[0]+",viewY:"+locations[1]
                +",startX:"+startX+",startY:"+startY+",viewW:"+mMeasuredWidth+",viewH:"+mMeasuredHeight);
        if(null==mTitleView){
            mTitleView=findViewById(R.id.title_view);
        }
        mPopupWindow.showAsDropDown(mTitleView,startX ,startY);
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
        if(!VideoListActivity.this.isFinishing()){
            if(null!=mSwipeRefreshLayout){
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
            if(code==BaseEngin.API_RESULT_EMPTY){
                mAdapter.onLoadEnd();
            }else{
                if(mPage>-1){
                    mPage--;
                }
                mAdapter.onLoadError();
            }
            Toast.makeText(VideoListActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示视频列表
     * @param data 视频列表
     */
    @Override
    public void showVideos(List<OpenEyesIndexItemBean> data) {
        if(!VideoListActivity.this.isFinishing()){
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoPlayerManager.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoPlayerManager.getInstance().onPause();
    }

    /**
     * 屏幕方向变化监听
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d(TAG,"onConfigurationChanged-->newConfig:"+newConfig.orientation);
        //转到横屏
        if(2==newConfig.orientation){
            MusicWindowManager.getInstance().onInvisible();
            //转到竖屏
        }else if(1==newConfig.orientation){
            MusicWindowManager.getInstance().onVisible();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if(VideoPlayerManager.getInstance().isBackPressed()){
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
        if(null!=mSwipeRefreshLayout){
            mSwipeRefreshLayout.setRefreshing(true);
            mSwipeRefreshLayout=null;
        }
        if(null!=mPopupWindow){
            mPopupWindow.dismiss();
            mPopupWindow=null;
        }
        mMeasuredWidth=0;mMeasuredHeight=0;mUrl=null;
        VideoPlayerManager.getInstance().onDestroy();
    }
}