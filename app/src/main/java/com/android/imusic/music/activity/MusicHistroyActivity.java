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
import com.android.imusic.base.BaseActivity;
import com.android.imusic.music.adapter.MusicCommenListAdapter;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.music.dialog.MusicMusicDetailsDialog;
import com.music.player.lib.manager.SqlLiteCacheManager;
import com.android.imusic.music.ui.contract.MusicHistroyContract;
import com.android.imusic.music.ui.presenter.MusicHistroyPersenter;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.view.MusicCommentTitleView;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/24
 * Histroy Music
 * 始终是由近到远列出播放记录
 */

public class MusicHistroyActivity extends BaseActivity<MusicHistroyPersenter> implements
        MusicOnItemClickListener, Observer,MusicHistroyContract.View {

    private MusicCommenListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private MusicCommentTitleView mTitleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowEnable(true);
        setContentView(R.layout.music_activity_music_list);
        mTitleView = (MusicCommentTitleView) findViewById(R.id.title_view);
        mTitleView.setTitle(getString(R.string.text_chistroy_title));
        mTitleView.setOnTitleClickListener(new MusicCommentTitleView.OnTitleClickListener() {
            @Override
            public void onBack(View view) {
                finish();
            }

            @Override
            public void onSubTitleClick(View v) {
                new android.support.v7.app.AlertDialog.Builder(MusicHistroyActivity.this)
                        .setTitle(getString(R.string.text_remove_tips))
                        .setMessage(getString(R.string.text_histroy_remove_title))
                        .setNegativeButton(getString(R.string.music_text_cancel),null)
                        .setPositiveButton(getString(R.string.text_remove_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean allHistroy = SqlLiteCacheManager.getInstance().deteleAllHistroy();
                                if(allHistroy){
                                    Toast.makeText(MusicHistroyActivity.this,getString(R.string.text_remove_succ),Toast.LENGTH_SHORT).show();
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
        mPresenter.getHistroyAudios();
    }

    @Override
    protected MusicHistroyPersenter createPresenter() {
        return new MusicHistroyPersenter();
    }

    /**
     * @param view
     * @param position
     * @param musicID >0 为单击事件，反之为菜单点击事件
     */
    @Override
    public void onItemClick(View view, final int position, long musicID) {
        if(null!=view.getTag()){
            final BaseAudioInfo audioInfo= (BaseAudioInfo) view.getTag();
            if(musicID>0){
                long currentPlayerID = MusicPlayerManager.getInstance().getCurrentPlayerID();
                if(currentPlayerID>0&&currentPlayerID==audioInfo.getAudioId()){
                    //重复点击，打开播放器
                    startToMusicPlayer(currentPlayerID);
                    return;
                }
                //重新确定选中的对象
                mAdapter.notifyDataSetChanged(position);
                MusicPlayerManager.getInstance().setPlayingChannel(MusicConstants.CHANNEL_HISTROY);
                //开始播放
                MusicPlayerManager.getInstance().startPlayMusic(mAdapter.getData(),position);
                //如果悬浮窗权限未给定
                createMiniJukeboxWindow();
            }else{
                //Menu
                MusicMusicDetailsDialog.getInstance(MusicHistroyActivity.this,audioInfo,
                        MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)
                        .setMusicOnItemClickListener(new MusicOnItemClickListener() {
                            /**
                             * @param view
                             * @param itemId 参考 MusicDetails 定义
                             * @param musicID
                             */
                            @Override
                            public void onItemClick(View view, int itemId, long musicID) {
                                onMusicMenuClick(position,itemId,audioInfo);
                            }
                        }).show();
            }
        }
    }

    /**
     * 菜单处理
     * @param position
     * @param itemId
     * @param audioInfo
     */
    @Override
    protected void onMusicMenuClick(int position, int itemId, final BaseAudioInfo audioInfo) {
        super.onMusicMenuClick(position, itemId, audioInfo);
        if(itemId== MusicDetails.ITEM_ID_DETELE){
            new android.support.v7.app.AlertDialog.Builder(MusicHistroyActivity.this)
                    .setTitle(getString(R.string.text_detele_tips))
                    .setMessage(getString(R.string.text_histroy_detele_title))
                    .setNegativeButton(getString(R.string.music_text_cancel),null)
                    .setPositiveButton(getString(R.string.text_detele), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean deteleHistroy = SqlLiteCacheManager.getInstance().deteleHistroyByID(audioInfo.getAudioId());
                            if(deteleHistroy){
                                Toast.makeText(MusicHistroyActivity.this,getString(R.string.text_detele_succ),Toast.LENGTH_SHORT).show();
                                if(null!=mPresenter){
                                    mPresenter.getHistroyAudios();
                                }
                            }
                        }
                    }).setCancelable(false).show();
        }
    }

    @Override
    public void showError(int code, String errorMsg) {
        super.showError(code,errorMsg);
        if(null!=mAdapter){
            mAdapter.setNewData(null);
        }
        Toast.makeText(MusicHistroyActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示本地播放记录
     * @param data 收藏、历史播放 记录
     */
    @Override
    public void showAudios(List<BaseAudioInfo> data) {
        if(null!=mAdapter){
            mAdapter.setNewData(data);
        }
        if(null!=mTitleView){
            mTitleView.setSubTitle(getString(R.string.text_remove_title));
        }
    }

    @Override
    public void showLoading() {
        super.showLoading();
    }


    @Override
    public void update(Observable o, final Object arg) {
        if(null!=mAdapter&&o instanceof MusicSubjectObservable && null!=arg && arg instanceof MusicStatus){
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   MusicStatus musicStatus= (MusicStatus) arg;
                   //停止
                   if(MusicStatus.PLAYER_STATUS_DESTROY==musicStatus.getPlayerStatus()
                           ||MusicStatus.PLAYER_STATUS_STOP==musicStatus.getPlayerStatus()){
                       if(null!=mAdapter.getData()&&mAdapter.getData().size()>mAdapter.getCurrentPosition()){
                           mAdapter.getData().get(mAdapter.getCurrentPosition()).setSelected(false);
                           mAdapter.notifyDataSetChanged();
                       }
                       //还在继续
                   }else{
                       //播放对象发生了变化，此时列表中可能不存在此音频对象，拉取最新的播放记录
                       if(null!=mPresenter){
                           mPresenter.getHistroyAudios();
                       }
                   }
               }
           });
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