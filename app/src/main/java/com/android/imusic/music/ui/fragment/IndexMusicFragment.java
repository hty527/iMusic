package com.android.imusic.music.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.base.MusicBaseFragment;
import com.android.imusic.music.activity.MusicAlbumActivity;
import com.android.imusic.music.activity.MusicCollectActivity;
import com.android.imusic.music.activity.MusicHistroyActivity;
import com.android.imusic.music.activity.MusicLocalActivity;
import com.android.imusic.music.activity.MusicSearchActivity;
import com.android.imusic.music.adapter.MusicIndexDataAdapter;
import com.android.imusic.music.bean.AlbumInfo;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.ui.contract.MusicListContract;
import com.android.imusic.music.ui.presenter.MusicListPersenter;
import com.android.imusic.music.utils.MediaUtils;
import com.music.player.lib.adapter.base.OnItemClickListener;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.model.MusicPlayingChannel;
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

public class IndexMusicFragment extends MusicBaseFragment<MusicListPersenter>
        implements Observer, MusicListContract.View {

    private static final String TAG = "IndexMusicFragment";
    private MusicIndexDataAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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
                            MusicPlayerManager.getInstance().setPlayingChannel(MusicPlayingChannel.CHANNEL_NET);
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
                new android.support.v7.app.AlertDialog.Builder(getActivity())
                        .setTitle("支持作者")
                        .setMessage("感谢您的支持与厚爱，您的支持是我前进的动力！点击‘赏赐’立即前往支付宝犒赏作者")
                        .setNegativeButton("赏赐",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userSing="tsx05608jpga1ccy7yeej90%3F_s";
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F"+userSing+"%3Dweb-other"));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
                            "本地音乐封面加载已开启":"本地音乐封面加载已关闭",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //关注播放器内部状态和渠道状态
        MusicPlayerManager.getInstance().addObservable(this);
        mPresenter=new MusicListPersenter();
        mPresenter.attachView(this);
        mPresenter.getIndexAudios();
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
        new AsyncTask<Void, Void, List<BaseAudioInfo>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected List<BaseAudioInfo> doInBackground(final Void... unused) {
                ArrayList<BaseAudioInfo> audioInfos = MediaUtils.getInstance().queryLocationMusics(context);
                return audioInfos;
            }

            @Override
            protected void onPostExecute(List<BaseAudioInfo> data) {
                if(null!=data&&null!=mAdapter){
                    MediaUtils.getInstance().setLocationMusic(data);
                    if(mAdapter.getData().size()>0){
                        mAdapter.getData().get(0).setDesp("("+data.size()+"首)");
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }.execute();
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
        Toast.makeText(getContext(),errorMsg,Toast.LENGTH_SHORT).show();
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