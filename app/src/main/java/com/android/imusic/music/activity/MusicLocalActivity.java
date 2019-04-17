package com.android.imusic.music.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.android.imusic.music.utils.MediaUtils;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.model.MusicPlayingChannel;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicCommentTitleView;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/22
 * Location Music
 */

public class MusicLocalActivity extends MusicBaseActivity implements MusicOnItemClickListener, Observer {

    private MusicCommenListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private MusicCommentTitleView mTitleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowEnable(true);
        setContentView(R.layout.music_activity_music_list);
        mTitleView = (MusicCommentTitleView) findViewById(R.id.title_view);
        mTitleView.setTitle("本地音乐");
        mTitleView.setOnTitleClickListener(new MusicCommentTitleView.OnTitleClickListener() {
            @Override
            public void onBack(View view) {
                finish();
            }

            @Override
            public void onSubTitleClick(View v) {
                if(null!=mAdapter&&null!=mAdapter.getData()&&mAdapter.getData().size()>0){
                    MusicPlayerManager.getInstance().setPlayingChannel(MusicPlayingChannel.CHANNEL_LOCATION);
                    List<BaseMediaInfo> mediaInfos = mAdapter.getData();
                    startToMusicPlayer(mediaInfos.get(0).getId(),mediaInfos);
                }
            }
        });
        ((SwipeRefreshLayout) findViewById(R.id.swipre_layout)).setEnabled(false);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MusicCommenListAdapter(MusicLocalActivity.this,null,this);
        recyclerView.setAdapter(mAdapter);
        MusicPlayerManager.getInstance().addObservable(this);
        requstPermissions();
    }

    @Override
    protected void onRequstPermissionResult(int resultCode) {
        super.onRequstPermissionResult(resultCode);
        if(resultCode==PREMISSION_CANCEL){
            Toast.makeText(this,"你拒绝了读取本地存储的权限，无法查找本地音乐",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(resultCode==PREMISSION_SUCCESS){
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    queryLocationMusic();
                }
            },100);
        }
    }

    /**
     * 查询本地音频文件
     */
    @SuppressLint("StaticFieldLeak")
    private void queryLocationMusic() {
        if(null!=MediaUtils.getInstance().getLocationMusic()&&MediaUtils.getInstance().getLocationMusic().size()>0){
            mTitleView.setSubTitle("播放全部");
            List<BaseMediaInfo> mediaInfos = MediaUtils.getInstance().getLocationMusic();
            List<BaseMediaInfo> medias=new ArrayList<>();
            medias.addAll(mediaInfos);
            mAdapter.setNewData(medias);
            //定位至正在播放的任务
            if(null!=mLayoutManager){
                int playIndexInThis = MusicUtils.getInstance().getCurrentPlayIndexInThis(mAdapter.getData(), MusicPlayerManager.getInstance().getCurrentPlayerID());
                mLayoutManager.scrollToPositionWithOffset(playIndexInThis,MusicUtils.getInstance().dpToPxInt(MusicLocalActivity.this,69f));
            }
            return;
        }
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Toast.makeText(this,"SD存储卡准备中",Toast.LENGTH_SHORT).show();
            return;
        }
        if (status.equals(Environment.MEDIA_SHARED)) {
            Toast.makeText(this,"您的设备没有链接到USB位挂载",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this,"无法读取SD卡，请检查SD卡使用权限！",Toast.LENGTH_SHORT).show();
            return;
        }
        new AsyncTask<Void, Void, List<BaseMediaInfo>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected List<BaseMediaInfo> doInBackground(final Void... unused) {
                ArrayList<BaseMediaInfo> mediaInfos = MediaUtils.getInstance().queryLocationMusics(MusicLocalActivity.this);
                return mediaInfos;
            }

            @Override
            protected void onPostExecute(List<BaseMediaInfo> data) {
                if(null!=data&&null!=mAdapter){
                    mTitleView.setSubTitle("播放全部");
                    MediaUtils.getInstance().setLocationMusic(data);
                    mAdapter.setNewData(data);
                    //定位至正在播放的任务
                    if(null!=mLayoutManager){
                        int playIndexInThis = MusicUtils.getInstance().getCurrentPlayIndexInThis(mAdapter.getData(), MusicPlayerManager.getInstance().getCurrentPlayerID());
                        mLayoutManager.scrollToPositionWithOffset(playIndexInThis,0);
                    }
                }
            }
        }.execute();
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
                //冒泡排序将选中的项移动至数组第一位置
                //外层循环系数，取决于数组长度、内层循环控制每一次循环排序比较多少次
//                for (int i = 0; i < mediaInfos.size()-1; i++) {
//                    for (int i1 = 0; i1 < mediaInfos.size()-1-i; i1++) {
//                        if(!mediaInfos.get(i1).isSelected()&&mediaInfos.get(i1+1).isSelected()){
//                            MediaInfo tempMedia=mediaInfos.get(i1);
//                            mediaInfos.set(i1,mediaInfos.get(i1+1));//和下一个交换位置
//                            mediaInfos.set(i1+1,tempMedia);
//                        }
//                    }
//                }
                MusicPlayerManager.getInstance().setPlayingChannel(MusicPlayingChannel.CHANNEL_LOCATION);
                //开始播放
                MusicPlayerManager.getInstance().startPlayMusic(mAdapter.getData(),position);
                //如果悬浮窗权限未给定
                createMiniJukeboxWindow();
            }else{
                //Menu
                MusicMusicDetailsDialog.getInstance(MusicLocalActivity.this,mediaInfo)
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

    @Override
    protected void onMusicMenuClick(final int position, int itemId, final BaseMediaInfo mediaInfo) {
        super.onMusicMenuClick(position,itemId, mediaInfo);
        if(itemId== MusicDetails.ITEM_ID_DETELE){
            new android.support.v7.app.AlertDialog.Builder(MusicLocalActivity.this)
                    .setTitle("删除提示")
                    .setMessage("确定要从列表中删除这首歌吗？")
                    .setNegativeButton("取消",null)
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.removeItem(position);
                            Toast.makeText(MusicLocalActivity.this,"已删除",Toast.LENGTH_SHORT).show();
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
        MusicPlayerManager.getInstance().removeObserver(this);
    }
}