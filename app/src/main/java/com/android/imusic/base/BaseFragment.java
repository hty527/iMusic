package com.android.imusic.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.imusic.music.activity.MusicPlayerActivity;
import com.android.imusic.music.bean.AudioInfo;
import com.music.player.lib.constants.MusicConstants;
import java.io.Serializable;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4/8
 */

public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements BaseContract.BaseView {

    protected P mPresenter;
    protected abstract int getLayoutID();
    protected abstract void initViews();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutID(),null,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter=createPresenter();
        if(null!=mPresenter){
            mPresenter.attachView(this);
        }
        initViews();
    }

    /**
     * 交由子类实现自己指定的Presenter,可以为空
     * @return 子类持有的继承自BasePresenter的Presenter
     */
    protected abstract P createPresenter();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=mPresenter){
            mPresenter.detachView();
            mPresenter=null;
        }
    }

    protected void onInvisible() {}

    protected void onVisible() {}

    /**
     * 打开播放器
     * @param musicID
     */
    protected void startToMusicPlayer(long musicID) {
        Intent intent=new Intent(getContext().getApplicationContext(), MusicPlayerActivity.class);
        intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //overridePendingTransition( R.anim.music_bottom_menu_enter,0);
        getContext().getApplicationContext().startActivity(intent);
    }


    /**
     * 打开播放器
     * @param musicID 要播放的音频对象
     * @param audioInfos 要播放的音频队列对象
     */
    protected void startToMusicPlayer(long musicID,List<AudioInfo> audioInfos){
        Intent intent=new Intent(getContext().getApplicationContext(), MusicPlayerActivity.class);
        intent.putExtra(MusicConstants.KEY_MUSIC_LIST, (Serializable) audioInfos);
        intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().getApplicationContext().startActivity(intent);
    }

    @Override
    public void showLoading() {}

    @Override
    public void showError(int code, String errorMsg) {}
}