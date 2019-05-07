package com.video.player.lib.manager;

import android.content.Context;
import android.media.AudioManager;
import com.video.player.lib.utils.Logger;

/**
 * TinyHung@Outlook.com
 * 2018/3/10.
 * AudioFocusManager
 */


public final class VideoAudioFocusManager {

    public static final String TAG = "VideoAudioFocusManager";
    private int mVolumeWhenFocusLossTransientCanDuck;
    private AudioManager mAudioManager;

    public VideoAudioFocusManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 请求音频焦点
     */
    public int requestAudioFocus(OnAudioFocusListener focusListener) {
        if(null!=focusListener){
            this.mFocusListener=focusListener;
        }
        if(null!=mAudioManager){
            int requestAudioFocus = mAudioManager.requestAudioFocus(onAudioFocusChangeListener,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            return requestAudioFocus;
        }
        return 1;
    }

    /**
     * 停止播放释放音频焦点
     */
    public void releaseAudioFocus() {
        if(null!=mAudioManager&&null!=onAudioFocusChangeListener){
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }
    }

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener=new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Logger.d(TAG,"onAudioFocusChange:focusChange:"+focusChange);
            int volume;
            switch (focusChange) {
                //重新获取到了焦点
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Logger.d(TAG,"重新获取到了焦点");
                    volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (mVolumeWhenFocusLossTransientCanDuck > 0 && volume == mVolumeWhenFocusLossTransientCanDuck / 2) {
                        // 恢复音量
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeWhenFocusLossTransientCanDuck,
                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    if(null!=mFocusListener){
                        mFocusListener.onFocusGet();
                    }
                    break;
                //被其他播放器抢占
                case AudioManager.AUDIOFOCUS_LOSS:
                    Logger.d(TAG,"被其他播放器抢占");
                    if(null!=mFocusListener){
                        mFocusListener.onFocusOut();
                    }
                    break;
                //暂时失去焦点，例如来电占用音频输出
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Logger.d(TAG,"暂时失去焦点");
                    if(null!=mFocusListener){
                        mFocusListener.onFocusOut();
                    }
                    break;
                //瞬间失去焦点，例如通知占用了音频输出
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Logger.d(TAG,"瞬间失去焦点");
                    if(null!=mFocusListener){
                        volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        if (volume > 0) {
                            mVolumeWhenFocusLossTransientCanDuck = volume;
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                    mVolumeWhenFocusLossTransientCanDuck / 2,
                                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        }
                    }
                    break;
            }
        }
    };

    public interface OnAudioFocusListener{
        /**
         * 恢复焦点
         */
        void onFocusGet();

        /**
         * 失去焦点
         */
        void onFocusOut();

        /**
         * 内部播放器是否正在播放
         * @return 为true正在播放
         */
        boolean isPlaying();
    }

    public OnAudioFocusListener mFocusListener;

    public void onDestroy(){
        mAudioManager=null;
    }
}