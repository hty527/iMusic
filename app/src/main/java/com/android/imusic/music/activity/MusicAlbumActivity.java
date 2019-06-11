package com.android.imusic.music.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.base.BaseActivity;
import com.android.imusic.music.adapter.MusicCommenListAdapter;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.bean.SingerInfo;
import com.android.imusic.music.dialog.MusicMusicDetailsDialog;
import com.android.imusic.music.ui.contract.MusicListContract;
import com.android.imusic.music.ui.presenter.MusicListPersenter;
import com.android.imusic.music.utils.MediaUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.model.MusicGlideCircleTransform;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicColorUtils;
import com.music.player.lib.util.MusicStatusUtils;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicRoundImageView;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/23
 * Album-Songs
 */

public class MusicAlbumActivity extends BaseActivity<MusicListPersenter> implements
        MusicOnItemClickListener, Observer, AppBarLayout.OnOffsetChangedListener, MusicPlayerEventListener, MusicListContract.View {

    private MusicCommenListAdapter mAdapter;
    private String mTagID,mTitle;
    //TOP BG
    private ImageView mMusicTopBg,mPlayerModel;
    private LinearLayout mTopBar,mMusicTopBar;
    private AppBarLayout mAppBarLayout;
    private int oldVerticalOffset;
    private int mHeaderViewHeight=0;
    private TextView mTitleView,mPlayerModelName,mTvSubPlay;
    private MusicRoundImageView mSongCover;
    public Bitmap mCoverBitmap;
    private ImageView mBtnBack;
    private LinearLayout mBtnFunction;
    private SwipeRefreshLayout mRefreshLayout;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowEnable(true);
        setContentView(R.layout.music_activity_album_center);

        mTagID = getIntent().getStringExtra(MusicConstants.KEY_TAG_ID);
        mTitle = getIntent().getStringExtra(MusicConstants.KEY_ALBUM_ANME);
        if(TextUtils.isEmpty(mTagID)){
            Toast.makeText(MusicAlbumActivity.this,"TAG is empty",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MusicStatusUtils.getInstance().setStatusTextColor1(true,this);

        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int statusBarHeight = MusicUtils.getInstance().getStatusBarHeight(this);
        //TopTitle
        mTopBar = (LinearLayout) findViewById(R.id.root_top_bar);
        findViewById(R.id.view_status_bar).getLayoutParams().height=statusBarHeight;
        mMusicTopBar = (LinearLayout) findViewById(R.id.music_top_layout);
        mTopBar.setBackgroundColor(Color.parseColor("#FFFFFF"));
        mTopBar.getBackground().setAlpha(0);
        //HeadView
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        //测量头部、标题栏高度
        mTopBar.measure(width,width);
        //设定最小停靠距离,应该减去向上padding高度
        collapsingToolbarLayout.setMinimumHeight(mTopBar.getMeasuredHeight());
        //Head样式距离顶部巨鹿
        findViewById(R.id.music_empty_view).getLayoutParams().height= mTopBar.getMeasuredHeight()
            +MusicUtils.getInstance().dpToPxInt(this,10f);

        mMusicTopBar.measure(width,width);
        //HeadView整体高度
        int topBatLayoutHeight = mMusicTopBar.getMeasuredHeight();
        mMusicTopBg = (ImageView) findViewById(R.id.music_top_bg);
        //背景封面高度最终确定
        mMusicTopBg.getLayoutParams().height=topBatLayoutHeight;
        //滚动阈值高度
        mHeaderViewHeight=(topBatLayoutHeight-mTopBar.getMeasuredHeight());
        mSongCover = (MusicRoundImageView) findViewById(R.id.music_song_cover);
        TextView  tvPlay = (TextView) findViewById(R.id.music_tv_play);
        tvPlay.setText(getString(R.string.text_all_play));
        mTvSubPlay = (TextView) findViewById(R.id.music_tv_sub_play);
        mTvSubPlay.setText(String.format(getString(R.string.text_music_count),"0"));
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.music_btn_back:
                        finish();
                        break;
                    case R.id.btn_play_all:
                        if(null!=mAdapter&&null!=mAdapter.getData()&&mAdapter.getData().size()>0){
                            MusicPlayerManager.getInstance().setPlayingChannel(MusicConstants.CHANNEL_NET);
                            List<BaseAudioInfo> audioInfos = mAdapter.getData();
                            startMusicPlayer(audioInfos.get(0).getAudioId(),audioInfos);
                        }
                        break;
                    case R.id.btn_play_model:
                        MusicPlayerManager.getInstance().changedPlayerPlayModel();
                        break;
                }
            }
        };
        mBtnBack = findViewById(R.id.music_btn_back);
        mBtnBack.setOnClickListener(onClickListener);
        findViewById(R.id.btn_play_all).setOnClickListener(onClickListener);
        findViewById(R.id.btn_play_model).setOnClickListener(onClickListener);
        mPlayerModel = (ImageView) findViewById(R.id.music_play_model);
        mPlayerModelName = (TextView) findViewById(R.id.music_play_model_name);
        mTitleView = (TextView) findViewById(R.id.music_title);
        mTitleView.setText(getString(R.string.text_album_title));
        setPlayerModel(MusicPlayerManager.getInstance().getPlayerModel());
        mBtnFunction = (LinearLayout) findViewById(R.id.ll_btn_function);
        //主列表
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MusicCommenListAdapter(MusicAlbumActivity.this,null,this,true);
        recyclerView.setAdapter(mAdapter);
        MusicPlayerManager.getInstance().addOnPlayerEventListener(this);
        //滚动交互
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(this);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipre_layout);
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mRefreshLayout.setProgressViewOffset(false,0,200);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        MusicPlayerManager.getInstance().addObservable(this);
        loadData();
    }

    @Override
    protected MusicListPersenter createPresenter() {
        return new MusicListPersenter();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int abs = Math.abs(verticalOffset);
        Logger.d(TAG,"onOffsetChanged-->mHeaderViewHeight:"+mHeaderViewHeight+",ABS:"+abs);
        //下拉刷新是否可用
        if(null!=mRefreshLayout){
            if(abs<=0){
                mRefreshLayout.setEnabled(true);
            }else{
                mRefreshLayout.setEnabled(false);
            }
        }
        if(oldVerticalOffset==abs) return;
        float scale = (float) abs / mHeaderViewHeight;
        float alpha = (255 * scale);
        if(null!=mTopBar&&null!=mTopBar.getBackground()){
            mTopBar.getBackground().mutate().setAlpha((int) alpha);
        }
        //标题栏、返回按钮文字过渡渐变
        String currentColor = MusicColorUtils.getInstance().caculateColor("#FFFFFFFF", "#FF000000",scale);
        int parseColor = Color.parseColor(currentColor);
        mBtnBack.setColorFilter(parseColor);
        mTitleView.setTextColor(parseColor);

        if(abs>=mHeaderViewHeight){
            mTitleView.setText(mTitle);
            mBtnFunction.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }else{
            mTitleView.setText(getString(R.string.text_album_title));
            mBtnFunction.setBackgroundResource(R.drawable.music_song_play_bg);
        }
        oldVerticalOffset=abs;
    }

    /**
     * 更新播放模式
     * @param playModel
     */
    private void setPlayerModel(int playModel) {
        if(null!=mPlayerModel&&null!=mPlayerModelName){
            mPlayerModel.setImageResource(MediaUtils.getInstance().getPlayerModelToRes(playModel));
            mPlayerModelName.setText(MediaUtils.getInstance().getPlayerModelToString(MusicAlbumActivity.this,playModel));
        }
    }

    /**
     * 获取音频列表
     */
    private void loadData() {
        if(null!=mPresenter){
            mPresenter.getAudiosByTag(mTagID);
        }
    }

    /**
     * 刷新头部数据和渐变交互
     * @param singer
     */
    private void updateHead(SingerInfo singer) {
        if(null==singer) return;
        mTitle=singer.getSong_title();
        //预览人数
        ((TextView) findViewById(R.id.music_preview_count)).setText(
                MusicUtils.getInstance().formatNumToWan(singer.getPreview_num(),true));
        //歌单名称
        ((TextView) findViewById(R.id.music_song_title)).setText(singer.getSong_title());

        ((TextView) findViewById(R.id.music_song_user_name)).setText(singer.getCreate_nickname());
        ImageView userCover = (ImageView) findViewById(R.id.music_song_user_cover);
        //TOP Background
        Glide.with(MusicAlbumActivity.this)
                .load(TextUtils.isEmpty(singer.getSong_front())?singer.getSinger_avatar():singer.getSong_front())
                .thumbnail(0.1f)
                .error(R.drawable.music_default_music_bg)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .skipMemoryCache(true)
                .bitmapTransform(new BlurTransformation(MusicAlbumActivity.this, 45))
                .into(mMusicTopBg);
        //COVER
        Glide.with(MusicAlbumActivity.this)
                .load(TextUtils.isEmpty(singer.getSong_front())?singer.getSinger_avatar():singer.getSong_front())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if(null!=mSongCover&&null!=bitmap){
                            mCoverBitmap=bitmap;
                            mSongCover.setImageBitmap(bitmap);
                        }
                    }
                });
        //USER COVER
        Glide.with(MusicAlbumActivity.this)
                .load(singer.getCreate_avatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_music_juke_default_cover)
                .centerCrop()
                .transform(new MusicGlideCircleTransform(this))
                .into(userCover);
    }

    @Override
    public void onItemClick(View view, final int posotion, long musicID) {
        if(null!=view.getTag()){
            final BaseAudioInfo audioInfo = (BaseAudioInfo) view.getTag();
            if(musicID>0){
                long currentPlayerID = MusicPlayerManager.getInstance().getCurrentPlayerID();
                Logger.d(TAG,"onItemClick-->currentPlayerID:"+currentPlayerID);
                if(currentPlayerID>0&&currentPlayerID==audioInfo.getAudioId()){
                    //重复点击，打开播放器
                    startToMusicPlayer(currentPlayerID);
                    return;
                }
                //重新确定选中的对象
                mAdapter.notifyDataSetChanged(posotion);
                MusicPlayerManager.getInstance().setPlayingChannel(MusicConstants.CHANNEL_NET);
                //开始播放
                MusicPlayerManager.getInstance().startPlayMusic(mAdapter.getData(),posotion);
                //如果悬浮窗权限未给定
                createMiniJukeboxWindow();
            }else{
                //Menu
                MusicMusicDetailsDialog.getInstance(MusicAlbumActivity.this,audioInfo,
                        MusicMusicDetailsDialog.DialogScene.SCENE_ALBUM,mTitle)
                        .setMusicOnItemClickListener(new MusicOnItemClickListener() {
                            /**
                             * @param view
                             * @param itemId 参考 MusicDetails 定义
                             * @param musicID
                             */
                            @Override
                            public void onItemClick(View view, int itemId, long musicID) {
                                onMusicMenuClick(posotion,itemId,audioInfo);
                            }
                        }).show();
            }
        }
    }


    @Override
    public void showAudios(List<AudioInfo> data) {}

    /**
     * 显示专辑信息
     * @param data 专辑信息
     */
    @Override
    public void showAudiosFromTag(AlbumInfo data) {
        if(null!=mRefreshLayout){
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(false);
                }
            });
        }
        if(null!=mAdapter&&null!=data.getList()&&data.getList().size()>0){
            mAdapter.setNewData(data.getList());
            if(null!=mTvSubPlay){
                mTvSubPlay.setText(Html.fromHtml(String.format(getString(R.string.text_music_count),data.getList().size()+"")));
            }
        }
        if(null!=data.getSinger()){
            updateHead(data.getSinger());
        }
    }

    @Override
    public void showLocationAudios(List<BaseAudioInfo> data) {}

    @Override
    public void showLoading() {
        if(null!=mRefreshLayout&&!mRefreshLayout.isRefreshing()){
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    /**
     * 错误获取音频列表错误信息
     * @param code 0：为空 -1：失败
     * @param errorMsg 描述信息
     */
    @Override
    public void showError(int code, String errorMsg) {
        Logger.d(TAG,"onError-->code:"+code+",errorMsg:"+errorMsg);
        if(null!=mRefreshLayout){
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(false);
                }
            });
        }
        Toast.makeText(MusicAlbumActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void update(Observable o, Object arg) {
        if(null!=mAdapter&&o instanceof MusicSubjectObservable && null!=arg && arg instanceof MusicStatus){
            MusicStatus musicStatus= (MusicStatus) arg;
            if(MusicStatus.PLAYER_STATUS_DESTROY==musicStatus.getPlayerStatus()
                    ||MusicStatus.PLAYER_STATUS_STOP==musicStatus.getPlayerStatus()){
                if(null!=mAdapter.getData()&&mAdapter.getData().size()>mAdapter.getCurrentPosition()){
                    mAdapter.getData().get(mAdapter.getCurrentPosition()).setSelected(false);
                    mAdapter.notifyDataSetChanged();
                }
            }else{
                mAdapter.notifyDataSetChanged();
                int position = MusicUtils.getInstance().getCurrentPlayIndexInThis(mAdapter.getData(),
                        MusicPlayerManager.getInstance().getCurrentPlayerID());
                mAdapter.setCurrentPosition(position);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
        if(null!=mAppBarLayout){
            mAppBarLayout.removeOnOffsetChangedListener(this);
            mAppBarLayout=null;
        }
        mMusicTopBg=null;mTopBar=null;mCoverBitmap=null;
        mSongCover=null;mTagID=null;mTitle=null;oldVerticalOffset=0;mHeaderViewHeight=0;
        mTitleView=null;mTvSubPlay=null;mPlayerModel=null;mPlayerModelName=null;
        MusicPlayerManager.getInstance().removePlayerListener(this);
        MusicPlayerManager.getInstance().removeObserver(this);
    }

    @Override
    public void onMusicPlayerState(int playerState, String message) {}
    @Override
    public void onPrepared(long totalDurtion) {}
    @Override
    public void onBufferingUpdate(int percent) {}
    @Override
    public void onInfo(int event, int extra) {}
    @Override
    public void onPlayMusiconInfo(BaseAudioInfo musicInfo, int position) {}
    @Override
    public void onMusicPathInvalid(BaseAudioInfo musicInfo, int position) {}
    @Override
    public void onTaskRuntime(long totalDurtion, long currentDurtion, long alarmResidueDurtion, int bufferProgress) {}
    @Override
    public void onPlayerConfig(int playModel, int alarmModel, boolean isToast) {
        setPlayerModel(playModel);
    }
}