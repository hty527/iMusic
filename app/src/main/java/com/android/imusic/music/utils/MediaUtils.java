package com.android.imusic.music.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.android.imusic.R;
import com.android.imusic.music.bean.MediaInfo;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.video.bean.OpenEyesIndexItemBean;
import com.android.imusic.music.bean.SearchHistroy;
import com.android.imusic.music.bean.SearchResultInfo;
import com.android.imusic.video.bean.VideoParams;
import com.android.imusic.music.dialog.MusicMusicDetailsDialog;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicACache;
import com.music.player.lib.util.MusicUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/22
 */

public class MediaUtils {

    private static final String TAG = "MediaUtils";
    private static MediaUtils mInstance;
    private static int MAX_SEARCH_KEY_NUM = 30;
    //搜索历史纪录
    public static final String SEARCH_HISTORY="SEARCH_HISTORY";
    private List<BaseMediaInfo> mLocationMusic;
    private static boolean mLocalImageEnable;//本地音乐图片获取开关,默认关闭

    public static synchronized MediaUtils getInstance() {
        synchronized (MediaUtils.class) {
            if (null == mInstance) {
                mInstance = new MediaUtils();
            }
        }
        return mInstance;
    }

    /**
     * 设置最大的缓存搜索历史记录个数
     * @param maxSearchKeyNum
     */
    public void setMaxPlayHistroyNum(int maxSearchKeyNum){
        MAX_SEARCH_KEY_NUM =maxSearchKeyNum;
    }

    /**
     * 获取SD卡所有音频文件
     * @return
     */
    public ArrayList<BaseMediaInfo> queryLocationMusics(Context context) {
        ArrayList<BaseMediaInfo> mediaInfos=null;
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
                mediaInfos = new ArrayList<>();
                do {
                    if(!TextUtils.isEmpty(cursor.getString(9))){
                        BaseMediaInfo mediaInfo = new BaseMediaInfo();
                        if(!TextUtils.isEmpty(cursor.getString(0))){
                            mediaInfo.setId(Long.parseLong(cursor.getString(0)));
                        }else{
                            mediaInfo.setId(System.currentTimeMillis());
                        }
                        // 文件名
                        //mediaInfo.setVideo_desp(cursor.getString(1));
                        // 歌曲名
                        if(!TextUtils.isEmpty(cursor.getString(2))){
                            mediaInfo.setVideo_desp(cursor.getString(2));
                        }
//                song.setPinyin(Pinyin.toPinyin(title.charAt(0)).substring(0, 1).toUpperCase());
                        // 时长
                        if(!TextUtils.isEmpty(cursor.getString(3))){
                            mediaInfo.setVideo_durtion(cursor.getInt(3));
                        }
                        // 歌手名
                        if(!TextUtils.isEmpty(cursor.getString(4))){
                            mediaInfo.setNickname(cursor.getString(4));
                        }
                        // 专辑名
                        if(!TextUtils.isEmpty(cursor.getString(5))){
                            mediaInfo.setMediaAlbum(cursor.getString(5));
                        }
                        // 年代 cursor.getString(6)
                        if(!TextUtils.isEmpty(cursor.getString(7))){
                            // 歌曲格式
                            if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                                mediaInfo.setMediaType("mp3");
                            } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
                                mediaInfo.setMediaType("wma");
                            }
                        }
                        //文件大小 cursor.getString(8)
                        // 文件路径
                        //  /storage/emulated/0/Music/齐晨-咱们结婚吧.mp3
                        mediaInfo.setFile_path(cursor.getString(9));
                        mediaInfos.add(mediaInfo);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            return mediaInfos;
        }
        return null;
    }

    /**
     * 根据歌曲信息返回详细信息数组
     * @param mediaInfo
     * @param sceneMode 场景
     * @param albumName 专辑昵称
     * @return
     */
    public List<MusicDetails> getMusicDetails(BaseMediaInfo mediaInfo, MusicMusicDetailsDialog.DialogScene sceneMode, String albumName) {
        List<MusicDetails> musicDetailsList=new ArrayList<>();
        if(!sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)){
            MusicDetails musicDetails0=new MusicDetails();
            musicDetails0.setTitle("添加到我的收藏");
            musicDetails0.setIcon(R.drawable.ic_music_details_collect);
            musicDetails0.setItemID(MusicDetails.ITEM_ID_COLLECT);
            musicDetails0.setId(mediaInfo.getId());
            musicDetailsList.add(musicDetails0);
        }
        if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_LOCATION)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_ALBUM)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)){
            MusicDetails defaultDetails=new MusicDetails();
            defaultDetails.setTitle("播放下一首");
            defaultDetails.setIcon(R.drawable.ic_music_details_next);
            defaultDetails.setItemID(MusicDetails.ITEM_ID_NEXT_PLAY);
            musicDetailsList.add(defaultDetails);
        }
        MusicDetails shareDetails=new MusicDetails();
        shareDetails.setTitle("分享");
        shareDetails.setPath(mediaInfo.getFile_path());
        shareDetails.setIcon(R.drawable.ic_music_details_share);
        shareDetails.setItemID(MusicDetails.ITEM_ID_SHARE);
        musicDetailsList.add(shareDetails);

        if(!TextUtils.isEmpty(mediaInfo.getNickname())){
            MusicDetails musicDetails=new MusicDetails();
            musicDetails.setTitle("歌手：<font color='#333333'>"+mediaInfo.getNickname()+"</font>");
            musicDetails.setIcon(R.drawable.ic_music_details_anchor);
            musicDetailsList.add(musicDetails);
        }
        if(!TextUtils.isEmpty(albumName)){
            MusicDetails defaultDetails=new MusicDetails();
            defaultDetails.setTitle("专辑：<font color='#333333'>"+albumName+"</font>");
            defaultDetails.setIcon(R.drawable.ic_music_details_album);
            musicDetailsList.add(defaultDetails);
        }else{
            if(!TextUtils.isEmpty(mediaInfo.getMediaAlbum())){
                MusicDetails musicDetails=new MusicDetails();
                musicDetails.setTitle("专辑：<font color='#333333'>"+mediaInfo.getMediaAlbum()+"</font>");
                musicDetails.setIcon(R.drawable.ic_music_details_album);
                musicDetailsList.add(musicDetails);
            }
        }
        if(mediaInfo.getVideo_durtion()>0){
            MusicDetails musicDetails=new MusicDetails();
            musicDetails.setTitle("时长：<font color='#333333'>"+MusicUtils.getInstance().stringForAudioTime(mediaInfo.getVideo_durtion())+"</font>");
            musicDetails.setIcon(R.drawable.ic_music_details_durtion);
            musicDetailsList.add(musicDetails);
        }
        //删除
        if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_LOCATION)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)
                ||sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)){
            MusicDetails musicDetails=new MusicDetails();
            musicDetails.setTitle("删除");
            if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_HISTROY)){
                musicDetails.setTitle("从播放记录删除");
            }else if(sceneMode.equals(MusicMusicDetailsDialog.DialogScene.SCENE_COLLECT)){
                musicDetails.setTitle("从收藏记录删除");
            }
            musicDetails.setItemID(MusicDetails.ITEM_ID_DETELE);
            musicDetails.setIcon(R.drawable.ic_music_details_detele);
            musicDetailsList.add(musicDetails);
        }
        return musicDetailsList;
    }

    /**
     * 返回相对于此数组正在播放的位置
     * @param mediaInfos
     * @param musicID
     * @return
     */
    public int getNetCurrentPlayIndexInThis(List<SearchResultInfo> mediaInfos, long musicID) {
        if(musicID<=0){
            return 0;
        }
        if(null!=mediaInfos&&mediaInfos.size()>0){
            for (int i = 0; i < mediaInfos.size(); i++) {
                if(mediaInfos.get(i).getAudio_id()==musicID){
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
    public List<MediaInfo> createIndexData() {
        List<MediaInfo> dataList=new ArrayList<>();
        MediaInfo indexData=new MediaInfo();
        indexData.setTitle("本地音乐");
        indexData.setImage(R.drawable.ic_music_index_music);
        indexData.setTag_id(MediaInfo.TAG_LOCATION);
        indexData.setClass_enty(indexData.ITEM_CLASS_TYPE_DEFAULT);
        dataList.add(indexData);

        MediaInfo indexData1=new MediaInfo();
        indexData1.setTitle("最近播放");
        indexData1.setImage(R.drawable.ic_music_index_last_play);
        indexData1.setTag_id(MediaInfo.TAG_LAST_PLAYING);
        indexData1.setClass_enty(indexData.ITEM_CLASS_TYPE_DEFAULT);
        dataList.add(indexData1);

        MediaInfo indexData2=new MediaInfo();
        indexData2.setTitle("我的收藏");
        indexData2.setImage(R.drawable.ic_music_index_collect);
        indexData2.setTag_id(MediaInfo.TAG_COLLECT);
        indexData2.setClass_enty(indexData.ITEM_CLASS_TYPE_DEFAULT);
        dataList.add(indexData2);
        return dataList;
    }

    public void setLocationMusic(List<BaseMediaInfo> locationMusic) {
        mLocationMusic = locationMusic;
    }

    public List<BaseMediaInfo> getLocationMusic() {
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

    public void onDestroy() {
        if(null!=mLocationMusic){
            mLocationMusic.clear();
            mLocationMusic=null;
        }
        mInstance=null;
    }

    /**
     * 格式化视频入参
     * @param indexItemBean
     * @return
     */
    public VideoParams formatVideoParams(OpenEyesIndexItemBean indexItemBean) {
        if(null==indexItemBean){
            return new VideoParams();
        }
        VideoParams videoParams=new VideoParams();
        videoParams.setVideoiId(indexItemBean.getId());
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


    public interface OnTaskCallBack{
        void onFinlish();
    }

    /**
     * 保存并更新搜索记录
     * @param searchKey
     */
    public void putSearchKeyToHistroy(final String searchKey, final OnTaskCallBack callBack) {
        if(null!=MusicUtils.getInstance().getACache()){
            Logger.d(TAG,"setSearchKey-->searchKey:"+searchKey);
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    MusicACache musicACache = MusicUtils.getInstance().getACache();
                    List<SearchHistroy> searchHistroys = (List<SearchHistroy>) musicACache.getAsObject(SEARCH_HISTORY);
                    if(null!=searchHistroys){
                        SearchHistroy search=new SearchHistroy();
                        search.setKey(searchKey);
                        search.setTime(System.currentTimeMillis());
                        int index=-1;
                        for (int i = 0; i < searchHistroys.size(); i++) {
                            SearchHistroy searchHistroy = searchHistroys.get(i);
                            if(searchHistroy.getKey().equals(searchKey)){
                                index=i;
                                break;
                            }
                        }
                        if(index>-1){
                            Logger.d(TAG,"本地记录存在");
                            searchHistroys.remove(index);
                        }
                        searchHistroys.add(search);
                        //冒泡排序重新排序一遍
                        for (int i = 0; i < searchHistroys.size()-1; i++) {
                            for (int i1 = 0; i1 < searchHistroys.size()-1-i; i1++) {
                                if(searchHistroys.get(i1).getTime()<searchHistroys.get(i1+1).getTime()){
                                    SearchHistroy tempMedia=searchHistroys.get(i1);
                                    searchHistroys.set(i1,searchHistroys.get(i1+1));//和下一个交换位置
                                    searchHistroys.set(i1+1,tempMedia);
                                }
                            }
                        }
                        if(searchHistroys.size()>MAX_SEARCH_KEY_NUM){
                            Logger.d(TAG,"超出最大缓存数");
                            searchHistroys.remove(searchHistroys.size()-1);
                        }
                        musicACache.remove(SEARCH_HISTORY);
                        musicACache.put(SEARCH_HISTORY, (Serializable) searchHistroys);
                        if(null!=callBack){
                            callBack.onFinlish();
                        }
                    }else{
                        Logger.d(TAG,"缓存为空");
                        List<SearchHistroy> histroys=new ArrayList<>();
                        SearchHistroy searchHistroy=new SearchHistroy();
                        searchHistroy.setKey(searchKey);
                        searchHistroy.setTime(System.currentTimeMillis());
                        histroys.add(searchHistroy);
                        musicACache.remove(SEARCH_HISTORY);
                        musicACache.put(SEARCH_HISTORY, (Serializable) histroys);
                        if(null!=callBack){
                            callBack.onFinlish();
                        }
                    }
                }
            }.start();
        }
    }

    /**
     * 获取搜索缓存
     * @return
     */
    public List<SearchHistroy> getSearchByHistroy() {
        if(null==MusicUtils.getInstance().getACache()){
            return null;
        }
        List<SearchHistroy> searchHistroys = (List<SearchHistroy>) MusicUtils.getInstance().getACache().getAsObject(SEARCH_HISTORY);
        return searchHistroys;
    }

    /**
     * 清空缓存记录
     */
    public void removeAllHistroySearchCache() {
        if(null==MusicUtils.getInstance().getACache()){
            return;
        }
        MusicUtils.getInstance().getACache().remove(SEARCH_HISTORY);
    }
}