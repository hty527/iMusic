package com.android.imusic.music.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
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
import com.android.imusic.music.dialog.QuireDialog;
import com.android.imusic.music.ui.contract.MusicLocationContract;
import com.android.imusic.music.ui.presenter.MusicLocationPersenter;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicCommentTitleView;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/22
 * Location Music
 */

public class MusicLocalActivity extends BaseActivity<MusicLocationPersenter> implements
        MusicOnItemClickListener,Observer,MusicLocationContract.View{

    private MusicCommenListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private MusicCommentTitleView mTitleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowEnable(true);
        setContentView(R.layout.music_activity_music_list);
        mTitleView = (MusicCommentTitleView) findViewById(R.id.title_view);
        mTitleView.setTitle(getString(R.string.text_local_title));
        mTitleView.setOnTitleClickListener(new MusicCommentTitleView.OnTitleClickListener() {
            @Override
            public void onBack(View view) {
                finish();
            }

            /**
             * 随机播放
             * @param v
             */
            @Override
            public void onSubTitleClick(View v) {
                if(null!=mAdapter&&null!=mAdapter.getData()&&mAdapter.getData().size()>0){
                    MusicPlayerManager.getInstance().setPlayingChannel(MusicConstants.CHANNEL_LOCATION);
                    int index = MusicUtils.getInstance().getRandomNum(0, mAdapter.getData().size() - 1);
                    List<BaseAudioInfo> audioInfos = mAdapter.getData();
                    mAdapter.notifyDataSetChanged(index);
                    mLayoutManager.scrollToPositionWithOffset(index,0);
                    MusicPlayerManager.getInstance().startPlayMusic(audioInfos,index);
                    //如果悬浮窗权限未给定
                    createMiniJukeboxWindow();
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
    protected MusicLocationPersenter createPresenter() {
        return new MusicLocationPersenter();
    }

    @Override
    protected void onRequstPermissionResult(int resultCode) {
        super.onRequstPermissionResult(resultCode);
        if(resultCode==PREMISSION_CANCEL){
            Toast.makeText(this,getString(R.string.text_local_premiss),Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(resultCode==PREMISSION_SUCCESS){
            queryLocationMusic();
        }
    }

    /**
     * 查询本地音频文件
     */
    @SuppressLint("StaticFieldLeak")
    private void queryLocationMusic() {
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
        //查询本机音乐
        if(null!=mPresenter){
            mPresenter.getLocationAudios(MusicLocalActivity.this);
        }
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
                //冒泡排序将选中的项移动至数组第一位置
                //外层循环系数，取决于数组长度、内层循环控制每一次循环排序比较多少次
//                for (int i = 0; i < audioInfos.size()-1; i++) {
//                    for (int i1 = 0; i1 < audioInfos.size()-1-i; i1++) {
//                        if(!audioInfos.get(i1).isSelected()&&audioInfos.get(i1+1).isSelected()){
//                            audioInfo tempMedia=audioInfos.get(i1);
//                            audioInfos.set(i1,audioInfos.get(i1+1));//和下一个交换位置
//                            audioInfos.set(i1+1,tempMedia);
//                        }
//                    }
//                }
                MusicPlayerManager.getInstance().setPlayingChannel(MusicConstants.CHANNEL_LOCATION);
                //开始播放
                MusicPlayerManager.getInstance().startPlayMusic(mAdapter.getData(),position);
                //如果悬浮窗权限未给定
                createMiniJukeboxWindow();
            }else{
                //Menu
                MusicMusicDetailsDialog.getInstance(MusicLocalActivity.this,audioInfo)
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

    @Override
    protected void onMusicMenuClick(final int position, int itemId, final BaseAudioInfo audioInfo) {
        super.onMusicMenuClick(position,itemId, audioInfo);
        if(itemId== MusicDetails.ITEM_ID_DETELE){
            QuireDialog.getInstance(MusicLocalActivity.this)
                    .setTitleText(getString(R.string.text_detele_tips))
                    .setContentText(getString(R.string.text_local_detele_title))
                    .setSubmitTitleText(getString(R.string.text_detele))
                    .setCancelTitleText(getString(R.string.music_text_cancel))
                    .setTopImageRes(R.drawable.ic_setting_tips4)
                    .setOnQueraConsentListener(new QuireDialog.OnQueraConsentListener() {
                        @Override
                        public void onConsent(QuireDialog dialog) {
                            mAdapter.removeItem(position);
                            Toast.makeText(MusicLocalActivity.this,getString(R.string.text_detele_succ),Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
    }

    @Override
    public void showLoading() {
        super.showLoading();
    }

    @Override
    public void showError(int code, String errorMsg) {
        super.showError(code, errorMsg);
    }

    /**
     * 显示本地音频列表
     * @param data 本地音频列表
     */
    @Override
    public void showAudios(List<BaseAudioInfo> data) {
        if(null!=mAdapter){
            mTitleView.setSubTitle(getString(R.string.text_local_play_title));
            mAdapter.setNewData(data);
            //定位至正在播放的任务
            if(null!=mLayoutManager){
                int playIndexInThis = MusicUtils.getInstance().getCurrentPlayIndexInThis(mAdapter.getData(),
                        MusicPlayerManager.getInstance().getCurrentPlayerID());
                mLayoutManager.scrollToPositionWithOffset(playIndexInThis,0);
            }
        }
    }

    @Override
    public void update(Observable o, final Object arg) {
        if(null!=mAdapter&&o instanceof MusicSubjectObservable && null!=arg && arg instanceof MusicStatus){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
        MusicPlayerManager.getInstance().removeObserver(this);
    }
}