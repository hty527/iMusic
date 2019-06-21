package com.android.imusic.music.utils;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.android.imusic.R;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.music.bean.SearchResultInfo;
import com.android.imusic.music.dialog.MusicMusicDetailsDialog;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.util.MusicUtils;
import com.video.player.lib.bean.VideoParams;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/22
 */

public class MediaUtils {

    private static final String TAG = "MediaUtils";
    private static volatile MediaUtils mInstance;
    private List<AudioInfo> mLocationMusic=null;
    private static boolean mLocalImageEnable;//本地音乐图片获取开关,默认关闭

    public static MediaUtils getInstance() {
        if(null==mInstance){
            synchronized (MediaUtils.class) {
                if (null == mInstance) {
                    mInstance = new MediaUtils();
                }
            }
        }
        return mInstance;
    }

    private MediaUtils(){}

    public int setDialogWidth(Dialog context) {
        Window window = context.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        WindowManager systemService = (WindowManager) context.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        systemService.getDefaultDisplay().getMetrics(displayMetrics);
        int hight = LinearLayout.LayoutParams.WRAP_CONTENT;
        attributes.height = hight;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int screenWidth = systemService.getDefaultDisplay().getWidth();
        if (screenWidth <= 720) {
            attributes.width = screenWidth - 100;
        } else if (screenWidth > 720 && screenWidth < 1100) {
            attributes.width = screenWidth - 200;
        } else if (screenWidth > 1100 && screenWidth < 1500) {
            attributes.width = screenWidth - 280;
        } else {
            attributes.width = screenWidth - 200;
        }
        attributes.gravity = Gravity.CENTER;
        return attributes.width;
    }

    /**
     * 获取SD卡所有音频文件
     * @return
     */
    public ArrayList<AudioInfo> queryLocationMusics(Context context) {
        ArrayList<AudioInfo> audioInfos=new ArrayList<>();
        if(null!=context.getContentResolver()){
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.YEAR,
                            MediaStore.Audio.Media.MIME_TYPE,
                            MediaStore.Audio.Media.SIZE,
                            MediaStore.Audio.Media.DATA },
                    MediaStore.Audio.Media.MIME_TYPE + "=? or "
                            + MediaStore.Audio.Media.MIME_TYPE + "=?",
                    new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);
            if (null!=cursor&&cursor.moveToFirst()) {
                do {
                    if(!TextUtils.isEmpty(cursor.getString(9))){
                        AudioInfo audioInfo = new AudioInfo();
                        if(!TextUtils.isEmpty(cursor.getString(0))){
                            audioInfo.setAudioId(Long.parseLong(cursor.getString(0)));
                        }else{
                            audioInfo.setAudioId(System.currentTimeMillis());
                        }
                        // 文件名
                        //audioInfo.setaudioName(cursor.getString(1));
                        // 歌曲名
                        if(!TextUtils.isEmpty(cursor.getString(2))){
                            audioInfo.setAudioName(cursor.getString(2));
                        }
//                song.setPinyin(Pinyin.toPinyin(title.charAt(0)).substring(0, 1).toUpperCase());
                        // 时长
                        if(!TextUtils.isEmpty(cursor.getString(3))){
                            audioInfo.setAudioDurtion(cursor.getInt(3));
                        }
                        // 歌手名
                        if(!TextUtils.isEmpty(cursor.getString(4))){
                            audioInfo.setNickname(cursor.getString(4));
                        }
                        // 专辑名
                        if(!TextUtils.isEmpty(cursor.getString(5))){
                            audioInfo.setAudioAlbumName(cursor.getString(5));
                        }
                        // 年代 cursor.getString(6)
                        if(!TextUtils.isEmpty(cursor.getString(7))){
                            // 歌曲格式
                            if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                                audioInfo.setAudioType("mp3");
                            } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
                                audioInfo.setAudioType("wma");
                            }
                        }
                        //文件大小 cursor.getString(8)
                        // 文件路径
                        //  /storage/emulated/0/Music/齐晨-咱们结婚吧.mp3
                        audioInfo.setAudioPath(cursor.getString(9));
                        audioInfos.add(audioInfo);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            setLocationMusic(audioInfos);
            return audioInfos;
        }
        setLocationMusic(audioInfos);
        return audioInfos;
    }

    /**
     * 根据歌曲信息返回详细信息数组
     * @param context 上下文
     * @param audioInfo 音频对象
     * @param sceneMode 场景
     * @param albumName 专辑昵称
     * @return
     */
    public List<MusicDetails> getMusicDetails(Context context,BaseAudioInfo audioInfo,
                                              MusicMusicDetailsDialog.DialogScene sceneMode, String albumName) {
        List<MusicDetails> musicDetailsList=new ArrayList<>();
        if(!sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)){
            MusicDetails musicDetails0=new MusicDetails();
            musicDetails0.setTitle(context.getString(R.string.text_add_to_collect));
            musicDetails0.setIcon(R.drawable.ic_music_details_collect);
            musicDetails0.setItemID(MusicDetails.ITEM_ID_COLLECT);
            musicDetails0.setId(audioInfo.getAudioId());
            musicDetailsList.add(musicDetails0);
        }
        if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_LOCATION)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_ALBUM)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)){
            MusicDetails defaultDetails=new MusicDetails();
            defaultDetails.setTitle(context.getString(R.string.text_play_next));
            defaultDetails.setIcon(R.drawable.ic_music_details_next);
            defaultDetails.setItemID(MusicDetails.ITEM_ID_NEXT_PLAY);
            musicDetailsList.add(defaultDetails);
        }
        MusicDetails shareDetails=new MusicDetails();
        shareDetails.setTitle(context.getString(R.string.text_share));
        shareDetails.setPath(audioInfo.getAudioPath());
        shareDetails.setIcon(R.drawable.ic_music_details_share);
        shareDetails.setItemID(MusicDetails.ITEM_ID_SHARE);
        musicDetailsList.add(shareDetails);

        if(!TextUtils.isEmpty(audioInfo.getNickname())){
            MusicDetails musicDetails=new MusicDetails();
            musicDetails.setTitle(context.getString(R.string.text_anchor)+"<font color='#333333'>"+audioInfo.getNickname()+"</font>");
            musicDetails.setIcon(R.drawable.ic_music_details_anchor);
            musicDetailsList.add(musicDetails);
        }
        if(!TextUtils.isEmpty(albumName)){
            MusicDetails defaultDetails=new MusicDetails();
            defaultDetails.setTitle(context.getString(R.string.text_album)+"<font color='#333333'>"+albumName+"</font>");
            defaultDetails.setIcon(R.drawable.ic_music_details_album);
            musicDetailsList.add(defaultDetails);
        }else{
            if(!TextUtils.isEmpty(audioInfo.getAudioAlbumName())){
                MusicDetails musicDetails=new MusicDetails();
                musicDetails.setTitle(context.getString(R.string.text_album)+"<font color='#333333'>"+audioInfo.getAudioAlbumName()+"</font>");
                musicDetails.setIcon(R.drawable.ic_music_details_album);
                musicDetailsList.add(musicDetails);
            }
        }
        if(audioInfo.getAudioDurtion()>0){
            MusicDetails musicDetails=new MusicDetails();
            musicDetails.setTitle(context.getString(R.string.text_durtion)+"<font color='#333333'>"+MusicUtils.getInstance().
                    stringForAudioTime(audioInfo.getAudioDurtion())+"</font>");
            musicDetails.setIcon(R.drawable.ic_music_details_durtion);
            musicDetailsList.add(musicDetails);
        }
        //删除
        if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_LOCATION)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)){
            MusicDetails musicDetails=new MusicDetails();
            musicDetails.setTitle(context.getString(R.string.text_detele));
            if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)){
                musicDetails.setTitle(context.getString(R.string.text_detele_to_collect));
            }else if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)){
                musicDetails.setTitle(context.getString(R.string.text_detele_to_histroy));
            }
            musicDetails.setItemID(MusicDetails.ITEM_ID_DETELE);
            musicDetails.setIcon(R.drawable.ic_music_details_detele);
            musicDetailsList.add(musicDetails);
        }
        return musicDetailsList;
    }

    /**
     * 返回相对于此数组正在播放的位置
     * @param searchResultInfos
     * @param musicID
     * @return
     */
    public int getNetCurrentPlayIndexInThis(List<SearchResultInfo> searchResultInfos, long musicID) {
        if(musicID<=0){
            return 0;
        }
        if(null!=searchResultInfos&&searchResultInfos.size()>0){
            for (int i = 0; i < searchResultInfos.size(); i++) {
                if(searchResultInfos.get(i).getAudio_id()==musicID){
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 生成主页本地列表
     * @return
     */
    public List<AudioInfo> createIndexData() {
        List<AudioInfo> dataList=new ArrayList<>();
        AudioInfo indexData=new AudioInfo();
        indexData.setTitle("本地音乐");
        indexData.setImage(R.drawable.ic_music_index_music);
        indexData.setTag_id(AudioInfo.TAG_LOCATION);
        indexData.setClass_enty(indexData.ITEM_CLASS_TYPE_DEFAULT);
        dataList.add(indexData);

        AudioInfo indexData1=new AudioInfo();
        indexData1.setTitle("最近播放");
        indexData1.setImage(R.drawable.ic_music_index_last_play);
        indexData1.setTag_id(AudioInfo.TAG_LAST_PLAYING);
        indexData1.setClass_enty(indexData.ITEM_CLASS_TYPE_DEFAULT);
        dataList.add(indexData1);

        AudioInfo indexData2=new AudioInfo();
        indexData2.setTitle("我的收藏");
        indexData2.setImage(R.drawable.ic_music_index_collect);
        indexData2.setTag_id(AudioInfo.TAG_COLLECT);
        indexData2.setClass_enty(indexData.ITEM_CLASS_TYPE_DEFAULT);
        dataList.add(indexData2);
        return dataList;
    }

    public void setLocationMusic(List<AudioInfo> locationMusic) {
        mLocationMusic = locationMusic;
    }

    public List<AudioInfo> getLocationMusic() {
        return mLocationMusic;
    }

    /**
     * 改变本地图片加载开关状态
     * @return
     */
    public boolean changeLocalImageEnable() {
        this.mLocalImageEnable=!mLocalImageEnable;
        return mLocalImageEnable;
    }

    public boolean isLocalImageEnable() {
        return mLocalImageEnable;
    }

    public void setLocalImageEnable(boolean localImageEnable) {
        mLocalImageEnable = localImageEnable;
    }

    /**
     * 格式化视频入参
     * @param indexItemBean ITEM
     * @return Player参数
     */
    public VideoParams formatVideoParams(OpenEyesIndexItemBean indexItemBean) {
        if(null==indexItemBean){
            return new VideoParams();
        }
        VideoParams videoParams=new VideoParams();
        videoParams.setVideoiId(indexItemBean.getId()+"");
        if(null!=indexItemBean.getAuthor()){
            videoParams.setNickName(indexItemBean.getAuthor().getName());
            videoParams.setUserFront(indexItemBean.getAuthor().getIcon());
            videoParams.setUserSinger(indexItemBean.getAuthor().getDescription());
            videoParams.setLastTime(indexItemBean.getAuthor().getLatestReleaseTime());
        }
        if(null!=indexItemBean.getCover()){
            videoParams.setVideoCover(indexItemBean.getCover().getFeed());
        }
        if(null!=indexItemBean.getConsumption()){
            videoParams.setPreviewCount(indexItemBean.getConsumption().getReplyCount());
        }
        videoParams.setVideoDesp(indexItemBean.getDescription());
        videoParams.setVideoTitle(indexItemBean.getTitle());
        videoParams.setVideoUrl(indexItemBean.getPlayUrl());
        videoParams.setDurtion(indexItemBean.getDuration());
        return videoParams;
    }

    /**
     * 根据播放模式返回文本
     * @param context 上下文
     * @param playerModel 播放模式
     * @return 模式描述
     */
    public String getPlayerModelToString(Context context,int playerModel) {
        if(null==context){
            if(playerModel==MusicConstants.MUSIC_MODEL_SINGLE){
                return "单曲循环";
            }else if(playerModel==MusicConstants.MUSIC_MODEL_LOOP){
                return "列表循环";
            }else if(playerModel==MusicConstants.MUSIC_MODEL_RANDOM){
                return "随机播放";
            }
            return "列表循环";
        }
        if(playerModel==MusicConstants.MUSIC_MODEL_SINGLE){
            return context.getResources().getString(R.string.text_play_model_single);
        }else if(playerModel==MusicConstants.MUSIC_MODEL_LOOP){
            return context.getResources().getString(R.string.text_play_model_loop);
        }else if(playerModel==MusicConstants.MUSIC_MODEL_RANDOM){
            return context.getResources().getString(R.string.text_play_model_random);
        }
        return context.getResources().getString(R.string.text_play_model_loop);
    }

    /**
     * 根据播放模式返回资源ID
     * @param playerModel 播放模式
     * @return 资源ICON
     */
    public int getPlayerModelToRes(int playerModel) {
        if(playerModel== MusicConstants.MUSIC_MODEL_SINGLE){
            return R.drawable.ic_music_model_signle_pre;
        }else if(playerModel==MusicConstants.MUSIC_MODEL_LOOP){
            return R.drawable.ic_music_model_loop_pre;
        }else if(playerModel==MusicConstants.MUSIC_MODEL_RANDOM){
            return R.drawable.ic_music_lock_model_random_pre;
        }
        return R.drawable.ic_music_model_loop_pre;
    }


    /**
     * 根据播放模式返回资源ID
     * @param playerModel 播放模式
     * @return 资源ICON
     */
    public int getPlayerModelToWhiteRes(int playerModel) {
        if(playerModel== MusicConstants.MUSIC_MODEL_SINGLE){
            return R.drawable.ic_music_model_signle_noimal;
        }else if(playerModel==MusicConstants.MUSIC_MODEL_LOOP){
            return R.drawable.ic_music_model_loop_noimal;
        }else if(playerModel==MusicConstants.MUSIC_MODEL_RANDOM){
            return R.drawable.ic_music_lock_model_random_noimal;
        }
        return R.drawable.ic_music_model_loop_noimal;
    }

    public void onDestroy() {
        if(null!=mLocationMusic){
            mLocationMusic.clear();
            mLocationMusic=null;
        }
        mInstance=null;
    }
}