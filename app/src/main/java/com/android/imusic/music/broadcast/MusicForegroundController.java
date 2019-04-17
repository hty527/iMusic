package com.android.imusic.music.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.imusic.music.activity.MusicPlayerActivity;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.util.Logger;

/**
 * TinyHung@Outlook.com
 * 2019/3/18
 * MusicPlayer Foreground Controller
 */

public class MusicForegroundController extends BroadcastReceiver {

    private static final String TAG = "MusicForegroundController";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logger.d(TAG,"onReceive-->action:"+action);
        if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_ROOT_VIEW)){
            if(intent.getLongExtra(MusicConstants.MUSIC_KEY_MEDIA_ID,0)>0){
                Intent startIntent=new Intent(context.getApplicationContext(), MusicPlayerActivity.class);
                startIntent.putExtra(MusicConstants.KEY_MUSIC_ID, intent.getLongExtra(MusicConstants.MUSIC_KEY_MEDIA_ID,0));
                //如果播放器组件未启用，创建新的实例
                //如果播放器组件已启用且在栈顶，复用播放器不传递任何意图
                //反之则清除播放器之上的所有栈，让播放器组件显示在最顶层
                startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startIntent);
            }
        }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_LAST)){
            MusicPlayerManager.getInstance().playLastMusic();
        }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_NEXT)){
            MusicPlayerManager.getInstance().playNextMusic();
        }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_PAUSE)){
            MusicPlayerManager.getInstance().playOrPause();
        }else if(action.equals(MusicConstants.MUSIC_INTENT_ACTION_CLICK_CLOSE)){
            MusicPlayerManager.getInstance().stopServiceForeground();
        }
    }
}