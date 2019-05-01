package com.android.imusic.video.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.music.base.MusicBaseActivity;
import com.android.imusic.music.engin.IndexPersenter;
import com.android.imusic.music.net.MusicNetUtils;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.video.adapter.VideoDetailsAdapter;
import com.android.imusic.video.bean.OpenEyesIndexInfo;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.music.player.lib.manager.MusicWindowManager;
import com.video.player.lib.bean.VideoParams;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.reflect.TypeToken;
import com.music.player.lib.adapter.base.OnItemClickListener;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.video.player.lib.base.BaseVideoPlayer;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.controller.DetailsCoverController;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.utils.VideoUtils;
import com.video.player.lib.view.VideoDetailsPlayerTrackView;
import com.video.player.lib.view.VideoTextureView;

/**
 * TinyHung@Outlook.com
 * 2019/4/10
 * VideoPlayer Activity
 * 视频播放器实例界面
 * Intent 中传递的 VideoConstants.KEY_VIDEO_PLAYING 参数作用：为 true：衔接外部播放任务无缝继续播放,false：不作处理
 * 打开迷你小窗口参见 ID btn_tiny的点击事件示例代码
 */

public class VideoPlayerActviity extends MusicBaseActivity<IndexPersenter> {

    private static final String TAG = "VideoPlayerActviity";
    private VideoDetailsPlayerTrackView  mVideoPlayer;
    private VideoDetailsAdapter mAdapter;
    //视频参数
    private VideoParams mVideoParams;
    private boolean mIsPlaying;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);
        initViews();
        getIntentParams(getIntent(),true);
    }

    private void initViews() {
        //播放器控件宽高
        mVideoPlayer = (VideoDetailsPlayerTrackView) findViewById(R.id.video_player);
        int itemHeight = MusicUtils.getInstance().getScreenWidth(this) * 9 / 16;
        mVideoPlayer.getLayoutParams().height=itemHeight;
        DetailsCoverController coverController = new DetailsCoverController(VideoPlayerActviity.this);
        mVideoPlayer.setVideoCoverController(coverController,false);
        mVideoPlayer.setGlobaEnable(true);
        mVideoPlayer.setVideoDisplayType(VideoConstants.VIDEO_DISPLAY_TYPE_CUT);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new VideoDetailsAdapter(VideoPlayerActviity.this,null);
        //条目点击事件
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId){
                if(null!=view.getTag()&& view.getTag() instanceof OpenEyesIndexItemBean){
                    OpenEyesIndexItemBean indexItemBean = (OpenEyesIndexItemBean) view.getTag();
                    VideoPlayerManager.getInstance().onReset();
                    Intent intent=new Intent(VideoPlayerActviity.this, VideoPlayerActviity.class);
                    VideoParams videoParams= MediaUtils.getInstance().formatVideoParams(indexItemBean);
                    intent.putExtra(VideoConstants.KEY_VIDEO_PARAMS,videoParams);
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
        //退出播放器
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //小窗口测试
        findViewById(R.id.btn_tiny).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mVideoPlayer){
                    int screenWidth = VideoUtils.getInstance().getScreenWidth(VideoPlayerActviity.this);
                    int width = screenWidth / 2;
                    int height = width * 9 / 16;
                    int startX = screenWidth / 2 -VideoUtils.getInstance().dpToPxInt(VideoPlayerActviity.this,10f);
                    int startY=mVideoPlayer.getMeasuredHeight()+VideoUtils.getInstance().dpToPxInt(VideoPlayerActviity.this,10f);
                    mVideoPlayer.startMiniWindow(startX,startY,width,height,null);
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getIntentParams(intent,false);
    }

    /**
     * 获取视频入参
     * @param intent
     * @param isCreate
     */
    private void getIntentParams(Intent intent,boolean isCreate) {
        if(null==intent) return;
        mVideoParams = intent.getParcelableExtra(VideoConstants.KEY_VIDEO_PARAMS);
        mIsPlaying = intent.getBooleanExtra(VideoConstants.KEY_VIDEO_PLAYING,false);
        if(null!=mAdapter&&mAdapter.getData().size()>0){
            mAdapter.getData().get(0).setVideoParams(mVideoParams);
            mAdapter.notifyDataSetChanged();
        }
        if(null==mVideoParams){
            Toast.makeText(VideoPlayerActviity.this,"缺少必要参数",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(TextUtils.isEmpty(mVideoParams.getVideoUrl())){
            Toast.makeText(VideoPlayerActviity.this,"缺少必要参数",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mPresenter=new IndexPersenter();
        initVideoParams(isCreate);
    }

    /**
     * 播放器初始化
     * @param isCreate
     */
    private void initVideoParams(boolean isCreate) {
        if(null!=mVideoParams){
            mVideoPlayer.setDataSource(mVideoParams.getVideoUrl(),mVideoParams.getVideoTitle(),mVideoParams.getVideoiId());
            mVideoPlayer.setLoop(true);
            mVideoPlayer.setWorking(true);
            mVideoPlayer.setParamsTag(mVideoParams);
            //封面
            if(null!=mVideoPlayer.getCoverController()){
                Glide.with(VideoPlayerActviity.this)
                        .load(mVideoParams.getVideoCover())
                        .placeholder(R.drawable.ic_video_default_cover)
                        .error(R.drawable.ic_video_default_cover)
                        .dontAnimate()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mVideoPlayer.getCoverController().mVideoCover);
            }
            //无缝衔接外部播放任务
            if(mIsPlaying&&null!=VideoPlayerManager.getInstance().getTextureView()){
                addTextrueViewToView(mVideoPlayer);
                VideoPlayerManager.getInstance().addOnPlayerEventListener(mVideoPlayer);
                //手动检查播放器内部状态，同步常规播放器状态至全屏播放器
                VideoPlayerManager.getInstance().checkedVidepPlayerState();
            }else{
                //开始全新播放任务
                mVideoPlayer.startPlayVideo();
            }
            if(null!=mPresenter&&!TextUtils.isEmpty(mVideoParams.getVideoiId())){
                //获取推荐视频
                mPresenter.getRecommendVideoList(mVideoParams.getVideoiId(),new TypeToken<OpenEyesIndexInfo>(){}.getType(),new MusicNetUtils.OnOtherRequstCallBack<OpenEyesIndexInfo>() {

                    @Override
                    public void onResponse(OpenEyesIndexInfo data) {
                        if(null!=mAdapter){
                            if(null!=data.getItemList()&&data.getItemList().size()>0){
                                mAdapter.onLoadComplete();
                                OpenEyesIndexItemBean openEyesIndexItemBean=new OpenEyesIndexItemBean();
                                openEyesIndexItemBean.setType(VideoConstants.VIDEO_HEADER);
                                openEyesIndexItemBean.setVideoParams(mVideoParams);
                                data.getItemList().add(0,openEyesIndexItemBean);
                                mAdapter.setNewData(data.getItemList());
                                if(null!=mLayoutManager){
                                    mLayoutManager.scrollToPositionWithOffset(0,0);
                                }
                            }else{
                                mAdapter.onLoadEnd();
                            }
                        }
                    }

                    @Override
                    public void onError(int code, String errorMsg) {
                        Logger.d(TAG,"onError-->code:"+code+",errorMsg:"+errorMsg);
                        if(null!=mAdapter){
                            mAdapter.onLoadError();
                        }
                    }
                });
            }
        }
    }


    /**
     * 添加一个视频渲染组件至View
     * @param videoPlayer
     */
    private void addTextrueViewToView(BaseVideoPlayer videoPlayer) {
        //先移除存在的TextrueView
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            VideoTextureView textureView = VideoPlayerManager.getInstance().getTextureView();
            if(null!=textureView.getParent()){
                ((ViewGroup) textureView.getParent()).removeView(textureView);
            }
        }
        if(null!=VideoPlayerManager.getInstance().getTextureView()){
            videoPlayer.mSurfaceView.addView(VideoPlayerManager.getInstance().getTextureView(),new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
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
        VideoPlayerManager.getInstance().onDestroy();
        if(null!=mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
        if(null!=mVideoPlayer){
            mVideoPlayer.destroy();
            mVideoPlayer=null;
        }
        mLayoutManager=null;mVideoParams=null;
    }
}