package com.music.player.lib.listener;

import android.app.Notification;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicPlayModel;
import com.music.player.lib.model.MusicPlayerState;
import com.music.player.lib.model.MusicPlayingChannel;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/5
 * MusicPlayer Listener
 */

public interface MusicPlayerPresenter {

    void startPlayMusic(List<?> musicList,int index);

    void startPlayMusic(int index);

    void addPlayMusicToTop(BaseMediaInfo mediaInfo);

    void playOrPause();

    void pause();

    void play();

    void setLoop(boolean loop);

    void continuePlay(String sourcePath);

    void continuePlay(String sourcePath,int index);

    void onReset();

    void onStop();

    void updateMusicPlayerData(List<?> musicLists, int index);

    MusicPlayModel setPlayerModel(MusicPlayModel model);

    MusicPlayModel getPlayerModel();

    MusicAlarmModel setPlayerAlarmModel(MusicAlarmModel model);

    MusicAlarmModel getPlayerAlarmModel();

    void onSeekTo(long currentTime);

    void playLastMusic();

    void playNextMusic();

    int playLastIndex();

    int playNextIndex();

    boolean isPlaying();

    long getDurtion();

    long getCurrentPlayerID();

    BaseMediaInfo getCurrentPlayerMusic();

    String getCurrentPlayerHashKey();

    List<?> getCurrentPlayList();

    void setPlayingChannel(MusicPlayingChannel channel);

    MusicPlayingChannel getPlayingChannel();

    MusicPlayerState getPlayerState();

    void onCheckedPlayerConfig();

    void onCheckedCurrentPlayTask();

    void addOnPlayerEventListener(MusicPlayerEventListener listener);

    void removePlayerListener(MusicPlayerEventListener listener);

    void removeAllPlayerListener();

    void changedPlayerPlayModel();

    void changedPlayerPlayFullModel();

    void createMiniJukeboxWindow();

    void startServiceForeground();

    void startServiceForeground(Notification notification);

    void startServiceForeground(Notification notification,int notificeid);

    void stopServiceForeground();

    void stopServiceForeground(int notificeid);
}