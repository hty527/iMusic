package com.music.player.lib.service;

import android.app.Notification;
import android.os.Binder;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.listener.MusicPlayerPresenter;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicPlayModel;
import com.music.player.lib.model.MusicPlayerState;
import com.music.player.lib.model.MusicPlayingChannel;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/8
 * Music Service Binder
 * MusicPlayerService 的中间代理人
 */

public class MusicPlayerBinder extends Binder{

    private final MusicPlayerPresenter mPresenter;

    public MusicPlayerBinder(MusicPlayerPresenter presenter){
        this.mPresenter=presenter;
    }

    public void startPlayMusic(List<?> musicList,int position){
        if(null!=mPresenter){
            mPresenter.startPlayMusic(musicList,position);
        }
    }

    public void startPlayMusic(int position){
        if(null!=mPresenter){
            mPresenter.startPlayMusic(position);
        }
    }

    public void addPlayMusicToTop(BaseMediaInfo mediaInfo){
        if(null!=mPresenter){
            mPresenter.addPlayMusicToTop(mediaInfo);
        }
    }

    public void playOrPause(){
        if(null!=mPresenter){
            mPresenter.playOrPause();
        }
    }

    public void pause(){
        if(null!=mPresenter){
            mPresenter.pause();
        }
    }

    public void play(){
        if(null!=mPresenter){
            mPresenter.play();
        }
    }

    public void setLoop(boolean loop) {
        if (null!=mPresenter) {
            mPresenter.setLoop(loop);
        }
    }

    public void continuePlay(String sourcePath) {
        if (null!=mPresenter) {
            mPresenter.continuePlay(sourcePath);
        }
    }

    public void continuePlay(String sourcePath,int position) {
        if (null!=mPresenter) {
            mPresenter.continuePlay(sourcePath,position);
        }
    }

    public void onReset(){
        if(null!=mPresenter) mPresenter.onReset();
    }

    public void onStop(){
        if(null!=mPresenter) mPresenter.onStop();
    }

    public void updateMusicPlayerData(List<?> musicLists, int index) {
        if(null!=mPresenter){
            mPresenter.updateMusicPlayerData(musicLists,index);
        }
    }

    public MusicPlayModel setPlayerModel(MusicPlayModel model) {
        if (null!=mPresenter) {
            return mPresenter.setPlayerModel(model);
        }
        return null;
    }

    public MusicPlayModel getPlayerModel(){
        if (null!=mPresenter) {
            return mPresenter.getPlayerModel();
        }
        return null;
    }

    public MusicAlarmModel setPlayerAlarmModel(MusicAlarmModel model) {
        if (null!=mPresenter) {
            return mPresenter.setPlayerAlarmModel(model);
        }
        return null;
    }

    public void onSeekTo(long currentTime){
        if(null!=mPresenter) mPresenter.seekTo(currentTime);
    }


    public void playLastMusic() {
        if(null!=mPresenter){
            mPresenter.playLastMusic();
        }
    }

    public void playNextMusic() {
        if(null!=mPresenter){
            mPresenter.playNextMusic();
        }
    }

    public int playLastIndex() {
        if(null!=mPresenter){
            return mPresenter.playLastIndex();
        }
        return -1;
    }

    public int playNextIndex() {
        if(null!=mPresenter){
            return mPresenter.playNextIndex();
        }
        return -1;
    }

    public boolean isPlaying(){
        if(null!=mPresenter){
            return mPresenter.isPlaying();
        }
        return false;
    }

    public long getDurtion(){
        if(null!=mPresenter) {
            return mPresenter.getDurtion();
        }
        return 0;
    }

    public long getCurrentPlayerID() {
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayerID();
        }
        return 0;
    }

    public BaseMediaInfo getCurrentPlayerMusic(){
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayerMusic();
        }
        return null;
    }

    public String getCurrentPlayerHashKey(){
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayerHashKey();
        }
        return "";
    }

    public List<?> getCurrentPlayList() {
        if(null!=mPresenter) {
            return mPresenter.getCurrentPlayList();
        }
        return null;
    }

    public void setPlayingChannel(MusicPlayingChannel channel) {
        if(null!=mPresenter){
            mPresenter.setPlayingChannel(channel);
        }
    }


    public MusicPlayingChannel getPlayingChannel() {
        if(null!=mPresenter){
            return mPresenter.getPlayingChannel();
        }
        return MusicPlayingChannel.CHANNEL_NET;
    }


    public void onCheckedPlayerConfig(){
        if(null!=mPresenter) mPresenter.onCheckedPlayerConfig();
    }

    public void onCheckedCurrentPlayTask(){
        if(null!=mPresenter) mPresenter.onCheckedCurrentPlayTask();
    }

    public MusicPlayerState getPlayerState() {
        if(null!=mPresenter){
            return mPresenter.getPlayerState();
        }
        return null;
    }

    public void changedPlayerPlayModel(){
        if(null!=mPresenter){
            mPresenter.changedPlayerPlayModel();
        }
    }

    public void changedPlayerPlayFullModel(){
        if(null!=mPresenter){
            mPresenter.changedPlayerPlayFullModel();
        }
    }

    public void setOnPlayerEventListener(MusicPlayerEventListener listener) {
        if(null!=mPresenter) mPresenter.addOnPlayerEventListener(listener);
    }

    public void removePlayerListener(MusicPlayerEventListener listener) {
        if(null!=mPresenter) mPresenter.removePlayerListener(listener);
    }

    public void removeAllPlayerListener() {
        if(null!=mPresenter) mPresenter.removeAllPlayerListener();
    }

    public MusicAlarmModel getPlayerAlarmModel() {
        if(null!=mPresenter){
            return mPresenter.getPlayerAlarmModel();
        }
        return null;
    }

    public void createMiniJukeboxWindow(){
        if(null!=mPresenter){
            mPresenter.createMiniJukeboxWindow();
        }
    }

    public void startServiceForeground(){
        if(null!=mPresenter){
            mPresenter.startServiceForeground();
        }
    }

    public void startServiceForeground(Notification notification){
        if(null!=mPresenter){
            mPresenter.startServiceForeground(notification);
        }
    }

    public void startServiceForeground(Notification notification,int notificeid){
        if(null!=mPresenter){
            mPresenter.startServiceForeground(notification,notificeid);
        }
    }

    public void stopServiceForeground(){
        if(null!=mPresenter){
            mPresenter.stopServiceForeground();
        }
    }

    public void stopServiceForeground(int notificeid){
        if(null!=mPresenter){
            mPresenter.stopServiceForeground(notificeid);
        }
    }
}