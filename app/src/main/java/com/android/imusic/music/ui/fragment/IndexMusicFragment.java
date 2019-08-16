package com.android.imusic.music.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.base.BaseEngin;
import com.android.imusic.base.BaseFragment;
import com.android.imusic.music.activity.MusicAlbumActivity;
import com.android.imusic.music.activity.MusicCollectActivity;
import com.android.imusic.music.activity.MusicHistroyActivity;
import com.android.imusic.music.activity.MusicLocalActivity;
import com.android.imusic.music.activity.MusicSearchActivity;
import com.android.imusic.music.adapter.MusicIndexDataAdapter;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.dialog.QuireDialog;
import com.android.imusic.music.ui.contract.MusicListContract;
import com.android.imusic.music.ui.presenter.MusicListPersenter;
import com.android.imusic.music.utils.MediaUtils;
import com.music.player.lib.adapter.base.OnItemClickListener;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.view.MusicCommentTitleView;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 * Index Music
 */

public class IndexMusicFragment extends BaseFragment<MusicListPersenter>
        implements Observer, MusicListContract.View {

    private MusicIndexDataAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int QUERY_LOCATION_MUSIC=0;

    @Override
    protected int getLayoutID() {
        return R.layout.music_fragment_index_music;
    }

    @Override
    protected void initViews() {
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager=new GridLayoutManager(getContext(),3,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        List<AudioInfo> dataList= MediaUtils.getInstance().createIndexData();
        mAdapter = new MusicIndexDataAdapter(getContext(),dataList);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                if(null!=view.getTag()){
                    AudioInfo audioInfo = (AudioInfo) view.getTag();
                    //本地条目类型
                    if(audioInfo.getItemType()== AudioInfo.ITEM_DEFAULT){
                        if(!TextUtils.isEmpty(audioInfo.getTag_id())){
                            //本地音乐
                            if(audioInfo.getTag_id().equals(AudioInfo.TAG_LOCATION)){
                                Intent intent=new Intent(getContext(), MusicLocalActivity.class);
                                startActivity(intent);
                                return;
                            }
                            //最近播放历史记录
                            if(audioInfo.getTag_id().equals(AudioInfo.TAG_LAST_PLAYING)){
                                Intent intent=new Intent(getContext(), MusicHistroyActivity.class);
                                startActivity(intent);
                                return;
                            }
                            //收藏列表
                            if(audioInfo.getTag_id().equals(AudioInfo.TAG_COLLECT)){
                                Intent intent=new Intent(getContext(), MusicCollectActivity.class);
                                startActivity(intent);
                                return;
                            }
                        }
                        return;
                    }
                    //专辑
                    if(audioInfo.getItemType()== AudioInfo.ITEM_ALBUM){
                        if(!TextUtils.isEmpty(audioInfo.getTag_id())){
                            Intent intent=new Intent(getContext(), MusicAlbumActivity.class);
                            intent.putExtra(MusicConstants.KEY_TAG_ID, audioInfo.getTag_id());
                            intent.putExtra(MusicConstants.KEY_ALBUM_ANME, audioInfo.getTitle());
                            startActivity(intent);
                        }
                        return;
                    }
                    //音乐
                    if(audioInfo.getItemType()== AudioInfo.ITEM_MUSIC){
                        List<AudioInfo> data = mAdapter.getData();
                        //提取音乐文件前往播放
                        List<AudioInfo> newData=new ArrayList<>();
                        for (int i = 0; i < data.size(); i++) {
                            AudioInfo dunAudioInfo = data.get(i);
                            if(!TextUtils.isEmpty(dunAudioInfo.getClass_enty())
                                    &&dunAudioInfo.getClass_enty().equals(AudioInfo.ITEM_CLASS_TYPE_MUSIC)){
                                newData.add(dunAudioInfo);
                            }
                        }
                        if(newData.size()>0){
                            MusicPlayerManager.getInstance().setPlayingChannel(MusicConstants.CHANNEL_NET);
                            startToMusicPlayer(audioInfo.getAudioId(),newData);
                        }
                        return;
                    }
                }
            }
        });
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipre_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(null!=mPresenter){
                    mPresenter.getIndexAudios();
                }
            }
        });

        MusicCommentTitleView titleView = (MusicCommentTitleView) getView().findViewById(R.id.title_view);
        titleView.setOnTitleClickListener(new MusicCommentTitleView.OnTitleClickListener() {

            @Override
            public void onBack(View view) {
                QuireDialog.getInstance(getActivity())
                        .setTitleText(getString(R.string.text_support_anchor))
                        .setContentText(getString(R.string.text_support_anchor_tips))
                        .setSubmitTitleText(getString(R.string.text_support_support))
                        .setCancelTitleText(getString(R.string.text_xiao_tips_close))
                        .setTopImageRes(R.drawable.ic_setting_tips1)
                        .setBtnClickDismiss(false)
                        .setOnQueraConsentListener(new QuireDialog.OnQueraConsentListener() {
                            @Override
                            public void onConsent(QuireDialog dialog) {
                                dialog.dismiss();
                                String userSing="tsx05608jpga1ccy7yeej90";
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F"+userSing+"%3F_s%3Dweb-other"));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onRefuse(QuireDialog dialog) {
                                dialog.dismiss();
                            }
                        }).show();


            }

            @Override
            public void onMenuClick(View v) {
                Intent intent=new Intent(getContext(), MusicSearchActivity.class);
                startActivity(intent);
            }

            @Override
            public void onTitleClick(View view, boolean doubleClick) {
                if(doubleClick){
                    boolean imageEnable = MediaUtils.getInstance().changeLocalImageEnable();
                    Toast.makeText(getContext(),imageEnable?
                            getString(R.string.text_image_open):getString(R.string.text_image_close),Toast.LENGTH_SHORT).show();
                }
            }
        });
        //关注播放器内部状态和渠道状态
        MusicPlayerManager.getInstance().addObservable(this);
        mPresenter.getIndexAudios();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //权限获取到后，此界面可能还未初始化完成，检查标记是否需要更新本地音乐库
        if(QUERY_LOCATION_MUSIC>0&&null!=mPresenter){
            mPresenter.getLocationAudios(getActivity());
        }
    }

    @Override
    protected MusicListPersenter createPresenter() {
        return new MusicListPersenter();
    }

    /**
     * 查询本地音频文件
     * @param context
     */
    @SuppressLint("StaticFieldLeak")
    public void queryLocationMusic(final Context context) {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Toast.makeText(getContext(),"SD存储卡准备中",Toast.LENGTH_SHORT).show();
            return;
        }
        if (status.equals(Environment.MEDIA_SHARED)) {
            Toast.makeText(getContext(),"您的设备没有链接到USB位挂载",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getContext(),"无法读取SD卡，请检查SD卡使用权限！",Toast.LENGTH_SHORT).show();
            return;
        }
        if(null!=mPresenter){
            mPresenter.getLocationAudios(context);
        }else{
            //标记为需要获取本机音乐，待界面初始化完成后，再查询本地音乐
            QUERY_LOCATION_MUSIC=1;
        }
    }

    /**
     * 显示音频列表
     * @param data
     */
    @Override
    public void showAudios(List<AudioInfo> data) {
        if(null!=mSwipeRefreshLayout){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        if(null!=mAdapter){
            List<AudioInfo> dataList= MediaUtils.getInstance().createIndexData();
            data.addAll(0,dataList);
            mAdapter.setNewData(data);
        }
    }

    @Override
    public void showAudiosFromTag(AlbumInfo data) {}

    /**
     * 显示本地音频列表
     * @param data 本地音频列表
     */
    @Override
    public void showLocationAudios(List<BaseAudioInfo> data) {
        if(null!=mAdapter){
            if(mAdapter.getData().size()>0){
                mAdapter.getData().get(0).setDesp("("+data.size()+"首)");
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 加载中
     */
    @Override
    public void showLoading() {
        if(null!=mSwipeRefreshLayout&&!mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    /**
     * 为空、失败
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
        if(code!= BaseEngin.API_RESULT_EMPTY){
            Toast.makeText(getContext(),errorMsg,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof MusicSubjectObservable && null!=arg && arg instanceof MusicStatus){
            MusicStatus musicStatus= (MusicStatus) arg;
            if(-2==musicStatus.getPlayerStatus()){
                if(null!=mAdapter){
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QUERY_LOCATION_MUSIC=0;
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