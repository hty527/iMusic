package com.android.imusic.music.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.base.BaseActivity;
import com.android.imusic.music.adapter.MusicSearchAdapter;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.music.bean.SearchMusicAnchor;
import com.android.imusic.music.bean.SearchMusicData;
import com.android.imusic.music.bean.SearchResult;
import com.android.imusic.music.bean.SearchResultInfo;
import com.android.imusic.music.dialog.MusicMusicDetailsDialog;
import com.android.imusic.music.manager.VersionUpdateManager;
import com.android.imusic.music.ui.contract.MusicSearchContract;
import com.android.imusic.music.ui.presenter.MusicSearchPersenter;
import com.android.imusic.music.utils.MediaUtils;
import com.google.android.flexbox.FlexboxLayout;
import com.music.player.lib.adapter.base.OnLoadMoreListener;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.bean.SearchHistroy;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.manager.SqlLiteCacheManager;
import com.music.player.lib.util.MusicUtils;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/24
 * Music Search
 * 音频搜索界面，使用酷狗API做数据支持
 */

public class MusicSearchActivity extends BaseActivity<MusicSearchPersenter>
        implements MusicOnItemClickListener, Observer, MusicSearchContract.View {

    private View mBtnClean;
    private EditText mEtInput;
    private MusicSearchAdapter mAdapter;
    private int mPage,mSearchHistroyCount;
    private RecyclerView mRecyclerView;
    private View mTagsRoot;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowEnable(true);
        setContentView(R.layout.music_activity_search);
        findViewById(R.id.view_status_bar).getLayoutParams().height=MusicUtils.getInstance().getStatusBarHeight(this);
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.music_btn_back:
                        onBackPressed();
                        break;
                    case R.id.music_btn_search:
                        search(false);
                        break;
                    case R.id.music_btn_clean:
                        mEtInput.setText("");
                        break;
                    case R.id.music_btn_remove:
                        if(mSearchHistroyCount>0){
                            MusicUtils.getInstance().closeKeybord(MusicSearchActivity.this,mEtInput);
                            new android.support.v7.app.AlertDialog.Builder(MusicSearchActivity.this)
                                    .setTitle(getString(R.string.text_detele_tips))
                                    .setMessage(getString(R.string.text_detele_content))
                                    .setNegativeButton(getString(R.string.music_text_cancel),null)
                                    .setPositiveButton(getString(R.string.text_detele_continue), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            boolean deteleAllSearch = SqlLiteCacheManager.getInstance().deteleAllSearch();
                                            if(deteleAllSearch){
                                                createSearchCache();
                                            }
                                        }
                                    }).setCancelable(false).show();
                        }else{
                            Toast.makeText(MusicSearchActivity.this,getString(R.string.text_detele_empty),
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        findViewById(R.id.music_btn_back).setOnClickListener(onClickListener);
        findViewById(R.id.music_btn_search).setOnClickListener(onClickListener);
        findViewById(R.id.music_btn_remove).setOnClickListener(onClickListener);
        mBtnClean = findViewById(R.id.music_btn_clean);
        mBtnClean.setOnClickListener(onClickListener);
        mEtInput = (EditText) findViewById(R.id.music_et_input);
        mEtInput.setHint(getString(R.string.text_search_hint));
        mEtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(null!=s&&s.length()>0){
                    mBtnClean.setVisibility(View.VISIBLE);
                }else{
                    mBtnClean.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEtInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    search(false);
                }
                return false;
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recyler_view);
        mTagsRoot = findViewById(R.id.music_tags_root);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MusicSearchAdapter(MusicSearchActivity.this,null,this);
        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mPage++;
                search(true);
            }
        },mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        MusicPlayerManager.getInstance().addObservable(this);
        //搜索记录回显
        createSearchCache();

        if(MusicUtils.getInstance().getInt(MusicConstants.SP_FIRST_SEARCH,0)==0){
            AlertDialog.Builder builder = new AlertDialog.Builder(MusicSearchActivity.this)
                    .setTitle(getString(R.string.text_search_play_tips))
                    .setMessage(getString(R.string.text_search_play_content))
                    .setPositiveButton(getString(R.string.text_yse), null).setCancelable(false);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //检查版本更新
                    VersionUpdateManager.getInstance().checkAppVersion();
                }
            });
            builder.show();
            MusicUtils.getInstance().putInt(MusicConstants.SP_FIRST_SEARCH,1);
        }
    }

    @Override
    protected MusicSearchPersenter createPresenter() {
        return new MusicSearchPersenter();
    }

    /**
     * 更新缓存
     */
    private void createSearchCache() {
        List<SearchHistroy> searchByCache = SqlLiteCacheManager.getInstance().querySearchNotes();
        FlexboxLayout flexboxLayout = (FlexboxLayout) findViewById(R.id.music_search_flags);
        if(null!=searchByCache&&searchByCache.size()>0){
            mSearchHistroyCount++;
            flexboxLayout.removeAllViews();
            for (int i = 0; i < searchByCache.size(); i++) {
                SearchHistroy searchHistroy = searchByCache.get(i);
                TextView tagTextView = new TextView(this);
                tagTextView.setTextSize(13);
                tagTextView.setTextColor(Color.parseColor("#313131"));
                tagTextView.setText(searchHistroy.getKey());
                tagTextView.setGravity(Gravity.CENTER);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    tagTextView.setBackground(ContextCompat.getDrawable(this,
                            R.drawable.music_search_tag_bg));
                }
                tagTextView.setTag(searchHistroy.getKey());
                tagTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key=(String) v.getTag();
                        mEtInput.setText(key);
                        mEtInput.setSelection(key.length());
                        search(key,false);
                    }
                });
                flexboxLayout.addView(tagTextView);
            }
        }else{
            mSearchHistroyCount=0;
            flexboxLayout.removeAllViews();
        }
    }

    /**
     * 开始搜索,是否自动搜索
     * @param isAuto
     */
    private void search(boolean isAuto) {
        String key = mEtInput.getText().toString().trim();
        if(!TextUtils.isEmpty(key)){
            search(key,isAuto);
        }
    }

    /**
     * 开始搜索
     * @param key
     * @param isAuto 是否自动搜索
     */
    private void search(String key,boolean isAuto) {
        if(!isAuto){
            MusicUtils.getInstance().closeKeybord(MusicSearchActivity.this,mEtInput);
            mPage=1;
            showProgressDialog(getString(R.string.text_search_loading));
            mAdapter.setCurrentKey(key);
            //写入搜索记录
            boolean searchKey = SqlLiteCacheManager.getInstance().insertSearchKey(key);
            if(searchKey){
                createSearchCache();
            }
        }
        mPresenter.queryMusicToKey(key,mPage);
    }

    /**
     * 条目点击事件
     * @param view
     * @param posotion
     * @param musicID
     */
    @Override
    public void onItemClick(View view, final int posotion, long musicID) {
        if(null!=view.getTag()){
            final SearchResultInfo searchResultInfo = (SearchResultInfo) view.getTag();
            if(musicID>0){
                String hashKey = MusicPlayerManager.getInstance().getCurrentPlayerHashKey();
                //检测是否正在播放当前歌曲
                if(!TextUtils.isEmpty(hashKey)&&hashKey.equals(searchResultInfo.getHash())){
                    //重复点击，打开播放器
                    startToMusicPlayer(searchResultInfo.getAudio_id());
                    return;
                }
                if(null!=mPresenter&&!mPresenter.isRequsting()){
                    mPresenter.getPathBkKey(posotion,searchResultInfo,searchResultInfo.getHash());
                }
            }else{
                //Menu
                final AudioInfo audioInfo = getaudioInfo(searchResultInfo);
                MusicMusicDetailsDialog.getInstance(MusicSearchActivity.this,
                        audioInfo,MusicMusicDetailsDialog.DialogScene.SCENE_SEARCH)
                        .setMusicOnItemClickListener(new MusicOnItemClickListener() {
                            /**
                             * @param view
                             * @param itemId 参考 MusicDetails 定义
                             * @param musicID
                             */
                            @Override
                            public void onItemClick(View view, int itemId, long musicID) {
                                //只有用户试听成功后才可收藏
                                if(!TextUtils.isEmpty(searchResultInfo.getSource())){
                                    onMusicMenuClick(posotion,itemId,searchResultInfo);
                                }else{
                                    Toast.makeText(MusicSearchActivity.this,getString(R.string.text_search_play_collect),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
            }
        }
    }

    /**
     * 音乐列表菜单处理
     * @param itemId
     * @param audioInfo
     */
    protected void onMusicMenuClick(int position,int itemId, SearchResultInfo audioInfo) {
        if(itemId== MusicDetails.ITEM_ID_NEXT_PLAY){
            MusicPlayerManager.getInstance().playNextMusic();
        }else if(itemId== MusicDetails.ITEM_ID_SHARE){
            try {
                if(!TextUtils.isEmpty(audioInfo.getSource())){
                    if(audioInfo.getSource().startsWith("http:")||audioInfo.getSource().startsWith("https:")){
                        Intent sendIntent = new Intent();
                        //sendIntent.setPackage("com.tencent.mm")
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "iMusic分享");
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "我正在使用"+getResources().getString(R.string.app_name)+
                                "听:《"+audioInfo.getSongname()+"》，快来听吧~猛戳-->"+audioInfo.getSource());
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "iMusic分享"));
                    }else{
                        Intent sendIntent = new Intent();
                        //sendIntent.setPackage("com.tencent.mm")
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "来自iMusic的音乐分享:《"
                                +audioInfo.getSongname()+"》-"+audioInfo.getSingername());
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(audioInfo.getSource()));
                        sendIntent.setType("audio/*");
                        startActivity(Intent.createChooser(sendIntent, "iMusic分享"));
                    }
                }else{
                    Toast.makeText(MusicSearchActivity.this,"此歌曲已被下架",
                            Toast.LENGTH_SHORT).show();
                }
            }catch (RuntimeException e){
                e.printStackTrace();
                Toast.makeText(MusicSearchActivity.this,"分享失败："+e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }else if(itemId==MusicDetails.ITEM_ID_COLLECT){
            AudioInfo cacheAudioInfo = getaudioInfo(audioInfo);
            cacheAudioInfo.setAudioId(audioInfo.getAudio_id());
            cacheAudioInfo.setAudioCover(audioInfo.getAlbum_img());
            cacheAudioInfo.setAudioPath(audioInfo.getSource());
            if(!TextUtils.isEmpty(cacheAudioInfo.getAudioPath())){
                boolean toCollect = MusicPlayerManager.getInstance().collectMusic(cacheAudioInfo);
                if(toCollect){
                    Toast.makeText(MusicSearchActivity.this,"已添加至收藏列表",
                            Toast.LENGTH_SHORT).show();
                    MusicPlayerManager.getInstance().observerUpdata(new MusicStatus());
                }
            }else{
                Toast.makeText(MusicSearchActivity.this,"添加失败，此歌曲不支持收藏",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 转换音频对象
     * @param musicData
     * @return
     */
    private AudioInfo getaudioInfo(SearchResultInfo musicData) {
        if(null!=musicData){
            AudioInfo audioInfo=new AudioInfo();
            audioInfo.setAudioId(musicData.getAudio_id());
            audioInfo.setAudioName(musicData.getSongname());
            audioInfo.setAudioAlbumName(musicData.getAlbum_name());
            audioInfo.setNickname(musicData.getSingername());
            audioInfo.setAudioDurtion(musicData.getDuration());
            audioInfo.setAudioPath(musicData.getSource());
            audioInfo.setAudioSize(musicData.getFilesize());
            return audioInfo;
        }
        return null;
    }

    /**
     * 转换音频对象
     * @param musicData
     * @param audioID 多媒体文件ID
     * @return
     */
    private AudioInfo getaudioInfo(SearchMusicData musicData,long audioID) {
        if(null!=musicData){
            AudioInfo audioInfo=new AudioInfo();
            audioInfo.setAudioId(audioID);
            audioInfo.setAudioPath(musicData.getPlay_url());
            audioInfo.setAudioName(musicData.getAudio_name());
            audioInfo.setAudioAlbumName(musicData.getAlbum_name());
            audioInfo.setAudioCover(musicData.getImg());
            audioInfo.setNickname(musicData.getAuthor_name());
            audioInfo.setAudioDurtion(musicData.getTimelength());
            audioInfo.setAudioSize(musicData.getFilesize());
            audioInfo.setAvatar(musicData.getImg());
            audioInfo.setAudioHashKey(musicData.getHash());
            if(null!=musicData.getAuthors()&&musicData.getAuthors().size()>0){
                SearchMusicAnchor searchMusicAnchor = musicData.getAuthors().get(0);
                audioInfo.setAvatar(searchMusicAnchor.getAvatar());
            }
            return audioInfo;
        }
        return null;
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
                int position = MediaUtils.getInstance().getNetCurrentPlayIndexInThis(mAdapter.getData(),
                        MusicPlayerManager.getInstance().getCurrentPlayerID());
                mAdapter.setCurrentPosition(position);
            }
        }
    }

    /**
     * 拦截返回和菜单事件
     * @param keyCode
     * @param event
     * @return
     */
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
        if(null!=mRecyclerView&&mRecyclerView.getVisibility()!=View.GONE){
            if(null!=mAdapter){
                mAdapter.setNewData(null);
            }
            mRecyclerView.setVisibility(View.GONE);
            mTagsRoot.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null!=mEtInput){
            MusicUtils.getInstance().closeKeybord(MusicSearchActivity.this,mEtInput);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
        mEtInput=null;
        mSearchHistroyCount=0;
        MusicPlayerManager.getInstance().removeObserver(this);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showError(int code, String errorMsg) {
        if(!MusicSearchActivity.this.isFinishing()){
            closeProgressDialog();
            if(mPage>0){
                mPage--;
            }
            if(null!=mAdapter){
                mAdapter.onLoadError();
            }
            Toast.makeText(MusicSearchActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showResult(SearchResult data) {
        if(!MusicSearchActivity.this.isFinishing()){
            closeProgressDialog();
            if(null!=mRecyclerView&&mRecyclerView.getVisibility()!=View.VISIBLE){
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            if(null!=mTagsRoot&&mTagsRoot.getVisibility()!=View.GONE){
                mTagsRoot.setVisibility(View.GONE);
            }
            if(null!=mAdapter){
                if(null!=data.getInfo()){
                    mAdapter.onLoadComplete();
                    if(mPage==1){
                        mAdapter.setNewData(data.getInfo());
                    }else{
                        mAdapter.addData(data.getInfo());
                    }
                }else{
                    mAdapter.onLoadEnd();
                    Toast.makeText(MusicSearchActivity.this,"暂未搜索到相关音乐",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * 音频详细信息
     * @param position ITEM Position
     * @param item 音频相关的ITEM
     * @param data 音频信息
     */
    @Override
    public void showAudioData(int position, SearchResultInfo item, SearchMusicData data) {
        if(!MusicSearchActivity.this.isFinishing()){
            if(!TextUtils.isEmpty(data.getPlay_url())){
                if(null!=item){
                    item.setAlbum_img(data.getImg());
                    item.setSource(data.getPlay_url());
                    mAdapter.notifyDataSetChanged();
                    AudioInfo audioInfo=getaudioInfo(data,item.getAudio_id());
                    MusicPlayerManager.getInstance().setPlayingChannel(MusicConstants.CHANNEL_SEARCH);
                    MusicPlayerManager.getInstance().addPlayMusicToTop(audioInfo);
                    //如果悬浮窗权限未给定
                    createMiniJukeboxWindow();
                }
            }else{
                Toast.makeText(MusicSearchActivity.this,"此歌曲已被下架",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 获取音频信息失败
     * @param code 错误码
     * @param errorMsg 描述信息
     */
    @Override
    public void showAudioDataError(int code, String errorMsg) {
        if(!MusicSearchActivity.this.isFinishing()){
            Toast.makeText(MusicSearchActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
        }
    }
}