package com.android.imusic.music.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.android.imusic.music.adapter.MusicCommenListAdapter;
import com.android.imusic.music.base.MusicBaseActivity;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.ResultData;
import com.android.imusic.music.bean.SingerInfo;
import com.android.imusic.music.dialog.MusicMusicDetailsDialog;
import com.android.imusic.music.engin.IndexPersenter;
import com.android.imusic.music.net.MusicNetUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.reflect.TypeToken;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicGlideCircleTransform;
import com.music.player.lib.model.MusicPlayModel;
import com.music.player.lib.model.MusicPlayerState;
import com.music.player.lib.model.MusicPlayingChannel;
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

public class MusicAlbumActivity extends MusicBaseActivity<IndexPersenter> implements MusicOnItemClickListener, Observer, AppBarLayout.OnOffsetChangedListener, MusicPlayerEventListener {

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

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowEnable(true);
        setContentView(R.layout.music_activity_album_center);

        mTagID = getIntent().getStringExtra(MusicConstants.KEY_TAG_ID);
        mTitle = getIntent().getStringExtra(MusicConstants.KEY_ALBUM_ANME);
        if(TextUtils.isEmpty(mTagID)){
            Toast.makeText(MusicAlbumActivity.this,"TAG为空",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MusicStatusUtils.getInstance().setStatusTextColor1(true,this);

        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        //TopTitle
        mTopBar = (LinearLayout) findViewById(R.id.root_top_bar);
        mMusicTopBar = (LinearLayout) findViewById(R.id.music_top_layout);
        mTopBar.setBackgroundColor(Color.parseColor("#FFFFFF"));
        mTopBar.getBackground().setAlpha(0);
        //HeadView
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        //测量头部、标题栏高度
        mTopBar.measure(width,width);
        //设定最小停靠距离
        collapsingToolbarLayout.setMinimumHeight(mTopBar.getMeasuredHeight()+MusicUtils.getInstance().dpToPxInt(this,7f));
        //Head样式距离顶部巨鹿
        findViewById(R.id.music_empty_view).getLayoutParams().height= mTopBar.getMeasuredHeight();

        mMusicTopBar.measure(width,width);
        //HeadView整体高度
        int topBatLayoutHeight = mMusicTopBar.getMeasuredHeight();
        mMusicTopBg = (ImageView) findViewById(R.id.music_top_bg);
        //背景封面高度最终确定
        mMusicTopBg.getLayoutParams().height=topBatLayoutHeight;
        //滚动阈值高度
        mHeaderViewHeight=(topBatLayoutHeight-mTopBar.getMeasuredHeight()-MusicUtils.getInstance().dpToPxInt(this,7f));
        mSongCover = (MusicRoundImageView) findViewById(R.id.music_song_cover);
        TextView  tvPlay = (TextView) findViewById(R.id.music_tv_play);
        tvPlay.setText("全部播放");
        mTvSubPlay = (TextView) findViewById(R.id.music_tv_sub_play);
        mTvSubPlay.setText("(共"+0+"首)");
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.music_btn_back:
                        finish();
                        break;
                    case R.id.btn_play_all:
                        if(null!=mAdapter&&null!=mAdapter.getData()&&mAdapter.getData().size()>0){
                            MusicPlayerManager.getInstance().setPlayingChannel(MusicPlayingChannel.CHANNEL_NET);
                            List<BaseMediaInfo> mediaInfos = mAdapter.getData();
                            startMusicPlayer(mediaInfos.get(0).getId(),mediaInfos);
                        }
                        break;
                    case R.id.btn_play_model:
                        MusicPlayerManager.getInstance().changedPlayerPlayFullModel();
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
        mTitleView.setText("歌单");
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
        MusicPlayerManager.getInstance().addObservable(this);
        mPresenter=new IndexPersenter();
        loadData();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int abs = Math.abs(verticalOffset);
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
            mTitleView.setText("歌单");
            mBtnFunction.setBackgroundResource(R.drawable.music_song_play_bg);
        }
        oldVerticalOffset=abs;
    }

    /**
     * 更新播放模式
     * @param playModel
     */
    private void setPlayerModel(MusicPlayModel playModel) {
        if(null!=playModel&&null!=mPlayerModel&&null!=mPlayerModelName){
            int resid=R.drawable.ic_music_model_loop;
            String content="列表循环";
            if(playModel.equals(MusicPlayModel.MUSIC_MODEL_LOOP)){
                content="列表循环";
                resid = R.drawable.ic_music_model_loop;
            }
            if(playModel.equals(MusicPlayModel.MUSIC_MODEL_SINGLE)){
                content="单曲循环";
                resid = R.drawable.ic_music_model_signle;
            }
            if(playModel.equals(MusicPlayModel.MUSIC_MODEL_RANDOM)){
                content="随机播放";
                resid = R.drawable.ic_music_model_random;
            }
            if(null!=mPlayerModel){
                mPlayerModel.setImageResource(resid);
            }
            mPlayerModel.setColorFilter(Color.parseColor("#333333"));
            if(null!=mPlayerModelName){
                mPlayerModelName.setText(content);
            }
        }
    }

    /**
     * 获取音频列表
     */
    private void loadData() {
        if(null!=mPresenter){
            mPresenter.getMusicListsByTag(mTagID,new TypeToken<ResultData<AlbumInfo>>(){}.getType(),new MusicNetUtils.OnRequstCallBack<AlbumInfo>() {
                @Override
                public void onResponse(ResultData<AlbumInfo> data) {
                    if(null!=data.getData()&&null!=data.getData().getList()&&data.getData().getList().size()>0){
                        mAdapter.setNewData(data.getData().getList());
                        if(null!=mTvSubPlay){
                            mTvSubPlay.setText(Html.fromHtml("(共"+data.getData().getList().size()+"首)"));
                        }
                    }else{
                        Toast.makeText(MusicAlbumActivity.this,data.getMsg(),Toast.LENGTH_SHORT).show();
                    }
                    if(null!=data.getData()&&null!=data.getData().getSinger()){
                        updateHead(data.getData().getSinger());
                    }
                }

                @Override
                public void onError(int code, String errorMsg) {
                    Logger.d(TAG,"onError-->code:"+code+",errorMsg:"+errorMsg);
                    Toast.makeText(MusicAlbumActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
                }
            });
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
        ((TextView) findViewById(R.id.music_preview_count)).setText(MusicUtils.getInstance().formatNumToWan(singer.getPreview_num(),true));
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
            final BaseMediaInfo mediaInfo = (BaseMediaInfo) view.getTag();
            if(musicID>0){
                long currentPlayerID = MusicPlayerManager.getInstance().getCurrentPlayerID();
                if(currentPlayerID>0&&currentPlayerID==mediaInfo.getId()){
                    //重复点击，打开播放器
                    startToMusicPlayer(currentPlayerID);
                    return;
                }
                //重新确定选中的对象
                mAdapter.notifyDataSetChanged(posotion);
                MusicPlayerManager.getInstance().setPlayingChannel(MusicPlayingChannel.CHANNEL_NET);
                //开始播放
                MusicPlayerManager.getInstance().startPlayMusic(mAdapter.getData(),posotion);
                //如果悬浮窗权限未给定
                createMiniJukeboxWindow();
            }else{
                //Menu
                MusicMusicDetailsDialog.getInstance(MusicAlbumActivity.this,mediaInfo, MusicMusicDetailsDialog.DialogScene.SCENE_ALBUM,mTitle)
                        .setMusicOnItemClickListener(new MusicOnItemClickListener() {
                            /**
                             * @param view
                             * @param itemId 参考 MusicDetails 定义
                             * @param musicID
                             */
                            @Override
                            public void onItemClick(View view, int itemId, long musicID) {
                                onMusicMenuClick(posotion,itemId,mediaInfo);
                            }
                        }).show();
            }
        }
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
                int position = MusicUtils.getInstance().getCurrentPlayIndexInThis(mAdapter.getData(), MusicPlayerManager.getInstance().getCurrentPlayerID());
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
    public void onMusicPlayerState(MusicPlayerState playerState, String message) {}
    @Override
    public void onPrepared(long totalDurtion) {}
    @Override
    public void onBufferingUpdate(int percent) {}
    @Override
    public void onInfo(int event, int extra) {}
    @Override
    public void onPlayMusiconInfo(BaseMediaInfo musicInfo, int position) {}
    @Override
    public void onEchoPlayCurrentIndex(BaseMediaInfo musicInfo, int position) {}
    @Override
    public void onMusicPathInvalid(BaseMediaInfo musicInfo, int position) {}
    @Override
    public void onTaskRuntime(long totalDurtion, long currentDurtion, long alarmResidueDurtion, int bufferProgress) {}
    @Override
    public void onPlayerConfig(MusicPlayModel playModel, MusicAlarmModel alarmModel, boolean isToast) {
        setPlayerModel(playModel);
    }
}