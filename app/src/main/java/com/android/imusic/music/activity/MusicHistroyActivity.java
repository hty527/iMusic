package com.android.imusic.music.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.music.adapter.MusicCommenListAdapter;
import com.android.imusic.music.base.MusicBaseActivity;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.music.dialog.MusicMusicDetailsDialog;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.model.MusicPlayingChannel;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicCommentTitleView;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/24
 * Histroy Music
 * 由近到远保存播放记录，默认最多50条，可调用 MusicUtils.getInstance().setMaxPlayHistroyNum(maxCount);设置存储最大数
 * 使用此功能，必须先初始化：MusicUtils.getInstance().initACache(this);
 */

public class MusicHistroyActivity extends MusicBaseActivity implements MusicOnItemClickListener, Observer {

    private static final String TAG = "LocationMusicActivity";
    private MusicCommenListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private MusicCommentTitleView mTitleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowEnable(true);
        setContentView(R.layout.music_activity_music_list);
        mTitleView = (MusicCommentTitleView) findViewById(R.id.title_view);
        mTitleView.setTitle("最近播放");
        mTitleView.setOnTitleClickListener(new MusicCommentTitleView.OnTitleClickListener() {
            @Override
            public void onBack(View view) {
                finish();
            }

            @Override
            public void onSubTitleClick(View v) {
                new android.support.v7.app.AlertDialog.Builder(MusicHistroyActivity.this)
                        .setTitle("清空提示")
                        .setMessage("确定要清空最近的播放记录吗？")
                        .setNegativeButton("取消",null)
                        .setPositiveButton("清空", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean flag = MusicUtils.getInstance().removeMusicHistroys();
                                if(flag){
                                    Toast.makeText(MusicHistroyActivity.this,"已清空",Toast.LENGTH_SHORT).show();
                                    mAdapter.setNewData(null);
                                    mTitleView.setSubTitle("");
                                    MusicPlayerManager.getInstance().observerUpdata(new MusicStatus());
                                }
                            }
                        }).setCancelable(false).show();
            }
        });
        ((SwipeRefreshLayout) findViewById(R.id.swipre_layout)).setEnabled(false);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MusicCommenListAdapter(MusicHistroyActivity.this,null,this);
        recyclerView.setAdapter(mAdapter);
        MusicPlayerManager.getInstance().addObservable(this);
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<BaseMediaInfo> playListByHistroy = MusicUtils.getInstance().getMusicsByHistroy();
                mAdapter.setNewData(playListByHistroy);
                if(null==playListByHistroy||playListByHistroy.size()==0){
                    Toast.makeText(MusicHistroyActivity.this,"播放记录空空如也",Toast.LENGTH_SHORT).show();
                }else{
                    if(null!=mTitleView){
                        mTitleView.setSubTitle("清空");
                    }
                }
            }
        },100);
    }

    /**
     * @param view
     * @param position
     * @param musicID >0 为单击事件，反之为菜单点击事件
     */
    @Override
    public void onItemClick(View view, final int position, long musicID) {
        if(null!=view.getTag()){
            final BaseMediaInfo mediaInfo= (BaseMediaInfo) view.getTag();
            if(musicID>0){
                long currentPlayerID = MusicPlayerManager.getInstance().getCurrentPlayerID();
                if(currentPlayerID>0&&currentPlayerID==mediaInfo.getId()){
                    //重复点击，打开播放器
                    startToMusicPlayer(currentPlayerID);
                    return;
                }
                //重新确定选中的对象
                mAdapter.notifyDataSetChanged(position);
                MusicPlayerManager.getInstance().setPlayingChannel(MusicPlayingChannel.CHANNEL_HISTROY);
                //开始播放
                MusicPlayerManager.getInstance().startPlayMusic(mAdapter.getData(),position);
                //如果悬浮窗权限未给定
                createMiniJukeboxWindow();
            }else{
                //Menu
                MusicMusicDetailsDialog.getInstance(MusicHistroyActivity.this,mediaInfo,MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)
                        .setMusicOnItemClickListener(new MusicOnItemClickListener() {
                            /**
                             * @param view
                             * @param itemId 参考 MusicDetails 定义
                             * @param musicID
                             */
                            @Override
                            public void onItemClick(View view, int itemId, long musicID) {
                                onMusicMenuClick(position,itemId,mediaInfo);
                            }
                        }).show();
            }
        }
    }

    /**
     * 菜单处理
     * @param position
     * @param itemId
     * @param mediaInfo
     */
    @Override
    protected void onMusicMenuClick(int position, int itemId, final BaseMediaInfo mediaInfo) {
        super.onMusicMenuClick(position, itemId, mediaInfo);
        if(itemId== MusicDetails.ITEM_ID_DETELE){
            new android.support.v7.app.AlertDialog.Builder(MusicHistroyActivity.this)
                    .setTitle("删除提示")
                    .setMessage("确定要从播放记录中删除这首歌吗？")
                    .setNegativeButton("取消",null)
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean flag = MusicUtils.getInstance().removeMusicHistroyById(mediaInfo.getId());
                            if(flag){
                                Toast.makeText(MusicHistroyActivity.this,"已删除",Toast.LENGTH_SHORT).show();
                                List<BaseMediaInfo> playListByHistroy = MusicUtils.getInstance().getMusicsByHistroy();
                                mAdapter.setNewData(playListByHistroy);
                            }
                        }
                    }).setCancelable(false).show();
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
        mTitleView=null;
        MusicPlayerManager.getInstance().removeObserver(this);
    }
}