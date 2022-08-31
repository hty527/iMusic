package com.music.player.lib.manager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.music.player.lib.bean.SearchHistroy;
import com.music.player.lib.model.SQLCollectHelper;
import com.music.player.lib.model.SQLHistroyHelper;
import com.music.player.lib.model.SQLSearchHelper;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.util.MusicUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/21
 * CACHE SQL Manager
 */

public class SqlLiteCacheManager {

    private static SqlLiteCacheManager mInstance;
    private WeakReference<SQLCollectHelper> mCollectDB;
    private WeakReference<SQLHistroyHelper> mHistroyDB;
    private WeakReference<SQLSearchHelper> mSearchDB;

    public static synchronized SqlLiteCacheManager getInstance(){
        synchronized (SqlLiteCacheManager.class){
            if(null==mInstance){
                mInstance=new SqlLiteCacheManager();
            }
            return mInstance;
        }
    }

    private SqlLiteCacheManager(){
        mCollectDB=createCollectDB();
        mHistroyDB=createHistroyDB();
    }

    //===============================================收藏记录========================================

    private synchronized WeakReference<SQLCollectHelper> createCollectDB() {
        if(null==mCollectDB||null==mCollectDB.get()){
            SQLCollectHelper sqlCollectHelper =new SQLCollectHelper(MusicUtils.getInstance().getApplicationContext());
            mCollectDB=new WeakReference<SQLCollectHelper>(sqlCollectHelper);
        }
        return mCollectDB;
    }

    /**
     * 插入一条收藏记录
     * @param data 音频实体对象
     * @return true:插入成功
     */
    public boolean insertCollectAudio(BaseAudioInfo data){
        if(null!=data){
            createCollectDB();
            SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
            ContentValues contentValues=createVontemtValues(data);
            //区别于普通的insert，insertWithOnConflict会在存在冲突的条时，替换条数据，前提是必须指定某个字段不能重复
            writableDatabase.insertWithOnConflict(SQLCollectHelper.TABLE_NAME,null,
                    contentValues,SQLiteDatabase.CONFLICT_REPLACE);
            contentValues.clear();
            writableDatabase.close();
            return true;
        }
        return false;
    }

    /**
     * 根据ID查询收藏记录
     * @param audioID 音频ID
     * @return true:插入成功
     */
    public BaseAudioInfo queryCollectAudioByID(long audioID){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLCollectHelper.TABLE_NAME+" where audioId=?";
        Cursor cursor = writableDatabase.rawQuery(sql,new String[]{audioID+""});
        if(null!=cursor){
            while (cursor.moveToNext()) {
                BaseAudioInfo mediaInfo = formatAudioInfoByCursor(cursor);
                cursor.close();
                writableDatabase.close();
                return mediaInfo;
            }
        }
        writableDatabase.close();
        return null;
    }

    /**
     * 查询某个音频ID是否存在于收藏记录表中
     * @param audioID 音频ID
     * @return true:存在
     */
    public boolean isExistToCollectByID(long audioID){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLCollectHelper.TABLE_NAME+" where audioId=?";
        Cursor cursor = writableDatabase.rawQuery(sql,new String[]{audioID+""});
        if(null!=cursor){
            boolean moveToNext = cursor.moveToNext();
            writableDatabase.close();
            return moveToNext;
        }
        writableDatabase.close();
        return false;
    }

    /**
     * 查询最近的一条收藏记录，数据库表现是应当获取第0条的上一条，cursor.moveToLast();
     * @return 最近的一条收藏记录
     */
    public BaseAudioInfo queryFirstCollectAudio(){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLCollectHelper.TABLE_NAME;
        Cursor cursor = writableDatabase.rawQuery(sql, null);
        if(null!=cursor&&cursor.moveToLast()){
            BaseAudioInfo mediaInfo = formatAudioInfoByCursor(cursor);
            cursor.close();
            writableDatabase.close();
            return mediaInfo;
        }
        writableDatabase.close();
        return null;
    }

    /**
     * 查询收藏表的总长度
     * @return 收藏表记录列总长度
     */
    public long queryCollectAudiosSize(){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        String sql="SELECT COUNT(*) FROM "+ SQLCollectHelper.TABLE_NAME;
        Cursor cursor = writableDatabase.rawQuery(sql, null);
        if(null!=cursor){
            cursor.moveToFirst();
            long size = cursor.getLong(0);
            cursor.close();
            writableDatabase.close();
            return size;
        }
        writableDatabase.close();
        return 0;
    }

    /**
     * 查询所有收藏记录
     * @return 收藏记录列表，为空则长度为0
     */
    public List<BaseAudioInfo> queryCollectVideos(){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLCollectHelper.TABLE_NAME;
        Cursor cursor = writableDatabase.rawQuery(sql, null);
        List<BaseAudioInfo> medias=new ArrayList<>();
        if(null!=cursor){
            while (cursor.moveToNext()) {
                BaseAudioInfo mediaInfo = formatAudioInfoByCursor(cursor);
                medias.add(mediaInfo);
            }
            cursor.close();
        }
        writableDatabase.close();
        //冒泡排序，由近到远
//        for (int i = 0; i < medias.size() - 1; i++) {
//            for (int i1 = 0; i1 < medias.size() - 1 - i; i1++) {
//                if (medias.get(i1).getLastPlayTime() < medias.get(i1 + 1).getLastPlayTime()) {
//                    BaseAudioInfo tempMedia = medias.get(i1);
//                    medias.set(i1, medias.get(i1 + 1));
//                    medias.set(i1 + 1, tempMedia);
//                }
//            }
//        }
        //由近至远排序
        Collections.sort(medias, new Comparator<BaseAudioInfo>() {
            @Override
            public int compare(BaseAudioInfo o1, BaseAudioInfo o2) {
                return o2.getAddtime()>o1.getAddtime()?1:-1;
            }
        });
        return medias;
    }

    /**
     * 更新收藏对象记录,如果存在就更新，不存在则新插入
     * @param data 视频实体对象
     * @return true:更新成功
     */
    public boolean updateCollect(BaseAudioInfo data){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        ContentValues contentValues=createVontemtValues(data);
        writableDatabase.insertWithOnConflict(SQLCollectHelper.TABLE_NAME,null,
                contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        contentValues.clear();
        writableDatabase.close();
        return true;
    }

    /**
     * 根据ID删除数据
     * @param audioID 音频ID
     * @return true:删除成功
     */
    public boolean deteleCollectByID(long audioID){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        int delete = writableDatabase.delete(SQLCollectHelper.TABLE_NAME, "audioId=?",
                new String[]{audioID+""});
        writableDatabase.close();
        return delete>0;
    }

    /**
     * 删除TAB下所有数据
     * @return true:删除成功
     */
    public boolean deteleAllCollect(){
        createCollectDB();
        SQLiteDatabase writableDatabase = mCollectDB.get().getWritableDatabase();
        int delete = writableDatabase.delete(SQLCollectHelper.TABLE_NAME, null, null);
        writableDatabase.close();
        return delete>0;
    }

    //===============================================历史记录========================================

    private synchronized WeakReference<SQLHistroyHelper> createHistroyDB() {
        if(null==mHistroyDB||null==mHistroyDB.get()){
            SQLHistroyHelper sqlHistroyHelper =new SQLHistroyHelper(MusicUtils.getInstance().getApplicationContext());
            mHistroyDB=new WeakReference<SQLHistroyHelper>(sqlHistroyHelper);
        }
        return mHistroyDB;
    }

    /**
     * 插入一条播放记录
     * @param data 音频实体对象
     * @return true:插入成功
     */
    public boolean insertHistroyAudio(BaseAudioInfo data){
        if(null!=data){
            createHistroyDB();
            SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
            ContentValues contentValues=createVontemtValues(data);
            //区别于普通的insert，insertWithOnConflict会在存在冲突的条时，替换条数据，前提是必须指定某个字段不能重复
            writableDatabase.insertWithOnConflict(SQLHistroyHelper.TABLE_NAME,null,
                    contentValues,SQLiteDatabase.CONFLICT_REPLACE);
            contentValues.clear();
            writableDatabase.close();
            return true;
        }
        return false;
    }

    /**
     * 根据ID查询播放记录
     * @param audioID 音频ID
     * @return true:插入成功
     */
    public BaseAudioInfo queryHistroyAudioByID(long audioID){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLHistroyHelper.TABLE_NAME+" where audioId=?";
        Cursor cursor = writableDatabase.rawQuery(sql,new String[]{audioID+""});
        if(null!=cursor){
            while (cursor.moveToNext()) {
                BaseAudioInfo mediaInfo = formatAudioInfoByCursor(cursor);
                cursor.close();
                writableDatabase.close();
                return mediaInfo;
            }
        }
        writableDatabase.close();
        return null;
    }

    /**
     * 根据ID查询播放记录是否存在
     * @param audioID 音频ID
     * @return true:存在
     */
    public boolean isExistToHistroyByID(long audioID){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLHistroyHelper.TABLE_NAME+" where audioId=?";
        Cursor cursor = writableDatabase.rawQuery(sql,new String[]{audioID+""});
        if(null!=cursor){
            boolean moveToNext = cursor.moveToNext();
            writableDatabase.close();
            return moveToNext;
        }
        writableDatabase.close();
        return false;
    }

    /**
     * 查询最近的一条播放记录，数据库表现是应当获取第0条的上一条，cursor.moveToLast();
     * @return 最近的一条播放记录
     */
    public BaseAudioInfo queryHistroyFirstAudio(){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLHistroyHelper.TABLE_NAME;
        Cursor cursor = writableDatabase.rawQuery(sql, null);
        if(null!=cursor&&cursor.moveToLast()){
            BaseAudioInfo mediaInfo = formatAudioInfoByCursor(cursor);
            cursor.close();
            writableDatabase.close();
            return mediaInfo;
        }
        writableDatabase.close();
        return null;
    }

    /**
     * 查询所有播放记录
     * @return 收藏记录列表，为空则长度为0
     */
    public List<BaseAudioInfo> queryHistroyAudios(){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLHistroyHelper.TABLE_NAME;
        Cursor cursor = writableDatabase.rawQuery(sql, null);
        List<BaseAudioInfo> medias=new ArrayList<>();
        if(null!=cursor){
            while (cursor.moveToNext()) {
                BaseAudioInfo mediaInfo = formatAudioInfoByCursor(cursor);
                medias.add(mediaInfo);
            }
            cursor.close();
        }
        writableDatabase.close();
        Collections.sort(medias, new Comparator<BaseAudioInfo>() {
            @Override
            public int compare(BaseAudioInfo o1, BaseAudioInfo o2) {
                return o2.getAddtime()>o1.getAddtime()?1:-1;
            }
        });
        return medias;
    }

    /**
     * 查询历史纪录表的总长度
     * @return 收藏表记录列总长度
     */
    public long queryHistroyAudiosSize(){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        String sql="SELECT COUNT(*) FROM "+ SQLHistroyHelper.TABLE_NAME;
        Cursor cursor = writableDatabase.rawQuery(sql, null);
        if(null!=cursor){
            cursor.moveToFirst();
            long size = cursor.getLong(0);
            cursor.close();
            writableDatabase.close();
            return size;
        }
        writableDatabase.close();
        return 0;
    }

    /**
     * 更新播放记录,如果存在就更新，不存在则新插入
     * @param data 视频实体对象
     * @return true:更新成功
     */
    public boolean updateHistroyAudio(BaseAudioInfo data){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        ContentValues contentValues=createVontemtValues(data);
        writableDatabase.insertWithOnConflict(SQLHistroyHelper.TABLE_NAME,null,
                contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        contentValues.clear();
        writableDatabase.close();
        return true;
    }

    /**
     * 根据ID删除数据
     * @param audioID 音频ID
     * @return true:删除成功
     */
    public boolean deteleHistroyByID(long audioID){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        int delete = writableDatabase.delete(SQLHistroyHelper.TABLE_NAME, "audioId=?",
                new String[]{String.valueOf(audioID)});
        writableDatabase.close();
        return delete>0;
    }

    /**
     * 删除TAB下所有数据
     * @return true:删除成功
     */
    public boolean deteleAllHistroy(){
        createHistroyDB();
        SQLiteDatabase writableDatabase = mHistroyDB.get().getWritableDatabase();
        int delete = writableDatabase.delete(SQLHistroyHelper.TABLE_NAME, null, null);
        writableDatabase.close();
        return delete>0;
    }

    //===============================================搜索记录========================================

    private synchronized WeakReference<SQLSearchHelper> createSearchDB() {
        if(null==mSearchDB||null==mSearchDB.get()){
            SQLSearchHelper searchHelper =new SQLSearchHelper(MusicUtils.getInstance().getApplicationContext());
            mSearchDB=new WeakReference<SQLSearchHelper>(searchHelper);
        }
        return mSearchDB;
    }

    /**
     * 插入一条搜索记录
     * @param key 搜索关键词
     * @return true:插入成功
     */
    public boolean insertSearchKey(String key){
        if(!TextUtils.isEmpty(key)){
            createSearchDB();
            SQLiteDatabase writableDatabase = mSearchDB.get().getWritableDatabase();
            ContentValues contentValues=createVontemtValues(key);
            //区别于普通的insert，insertWithOnConflict会在存在冲突的条时，替换条数据，前提是必须指定某个字段不能重复
            writableDatabase.insertWithOnConflict(SQLSearchHelper.TABLE_NAME,null,
                    contentValues,SQLiteDatabase.CONFLICT_REPLACE);
            contentValues.clear();
            writableDatabase.close();
            return true;
        }
        return false;
    }


    /**
     * 查询所有搜索记录
     * @return 搜索记录列表，为空则长度为0
     */
    public List<SearchHistroy> querySearchNotes(){
        createSearchDB();
        SQLiteDatabase writableDatabase = mSearchDB.get().getWritableDatabase();
        String sql="SELECT * FROM "+ SQLSearchHelper.TABLE_NAME;
        Cursor cursor = writableDatabase.rawQuery(sql, null);
        List<SearchHistroy> searchHistroys=new ArrayList<>();
        if(null!=cursor){
            while (cursor.moveToNext()) {
                SearchHistroy searchHistroy = formatSearchByCursor(cursor);
                searchHistroys.add(searchHistroy);
            }
            cursor.close();
        }
        writableDatabase.close();
        //冒泡排序，由近到远
//        for (int i = 0; i < searchHistroys.size()-1; i++) {
//            for (int i1 = 0; i1 < searchHistroys.size()-1-i; i1++) {
//                if(searchHistroys.get(i1).getTime()<searchHistroys.get(i1+1).getTime()){
//                    SearchHistroy tempMedia=searchHistroys.get(i1);
//                    searchHistroys.set(i1,searchHistroys.get(i1+1));//和下一个交换位置
//                    searchHistroys.set(i1+1,tempMedia);
//                }
//            }
//        }
        //由近至远排序
        Collections.sort(searchHistroys, new Comparator<SearchHistroy>() {
            @Override
            public int compare(SearchHistroy o1, SearchHistroy o2) {
                return o2.getAddtime()>o1.getAddtime()?1:-1;
            }
        });
        return searchHistroys;
    }

    /**
     * 删除搜索TABLE下所有数据
     * @return true:删除成功
     */
    public boolean deteleAllSearch(){
        createSearchDB();
        SQLiteDatabase writableDatabase = mSearchDB.get().getWritableDatabase();
        int delete = writableDatabase.delete(SQLSearchHelper.TABLE_NAME, null, null);
        writableDatabase.close();
        return delete>0;
    }


    /**
     * 生成SQL ContentValues
     * @param key 搜索关键词
     * @return ContentValues
     */
    private ContentValues createVontemtValues(String key) {
        ContentValues contentValues=new ContentValues();
        contentValues.put("key",key);
        contentValues.put("addtime",System.currentTimeMillis());
        return contentValues;
    }
    /**
     * 根据游标解析成Java bean
     * @param cursor 游标
     * @return 输出JAVA BEAN
     */
    private SearchHistroy formatSearchByCursor(Cursor cursor) {
        long id = cursor.getLong(0);
        String key = cursor.getString(1);
        long addtime = cursor.getLong(2);
        SearchHistroy searchHistroy=new SearchHistroy();
        searchHistroy.setAddtime(addtime);
        searchHistroy.setKey(key);
        return searchHistroy;
    }

    //===============================================公用方法========================================

    /**
     * 生成SQL ContentValues
     * @param data 音频对象
     * @return ContentValues
     */
    private ContentValues createVontemtValues(BaseAudioInfo data) {
        if(null!=data){
            ContentValues contentValues=new ContentValues();
            contentValues.put("audioId",data.getAudioId());
            contentValues.put("audioDurtion",data.getAudioDurtion());
            contentValues.put("audioName",data.getAudioName());
            contentValues.put("audioCover",data.getAudioCover());
            contentValues.put("audioPath",data.getAudioPath());
            contentValues.put("nickname",data.getNickname());
            contentValues.put("userid",data.getUserid());
            contentValues.put("avatar",data.getAvatar());
            contentValues.put("audioSize",data.getAudioSize());
            contentValues.put("audioAlbumName",data.getAudioAlbumName());
            contentValues.put("audioType",data.getAudioType());
            contentValues.put("audioDescribe",data.getAudioDescribe());
            contentValues.put("audioHashKey",data.getAudioHashKey());
            contentValues.put("addtime",System.currentTimeMillis());
            return contentValues;
        }
        return null;
    }

    /**
     * 根据游标解析成Java bean
     * @param cursor 游标
     * @return 输出JAVA BEAN
     */
    private BaseAudioInfo formatAudioInfoByCursor(Cursor cursor) {
        long id = cursor.getLong(0);
        long audioId = cursor.getLong(1);
        long audioDurtion = cursor.getLong(2);
        String audioName = cursor.getString(3);
        String audioCover = cursor.getString(4);
        String audioPath = cursor.getString(5);
        String nickname = cursor.getString(6);
        String userid = cursor.getString(7);
        String avatar = cursor.getString(8);
        long audioSize = cursor.getLong(9);
        String audioAlbumName = cursor.getString(10);
        String audioType = cursor.getString(11);
        String audioDescribe = cursor.getString(12);
        String audioHashKey = cursor.getString(13);
        long addtime = cursor.getLong(14);
        BaseAudioInfo audioInfo=new BaseAudioInfo();
        audioInfo.setAudioId(audioId);
        audioInfo.setAudioDurtion(audioDurtion);
        audioInfo.setAudioName(audioName);
        audioInfo.setAudioCover(audioCover);
        audioInfo.setAudioPath(audioPath);
        audioInfo.setNickname(nickname);
        audioInfo.setUserid(userid);
        audioInfo.setAvatar(avatar);
        audioInfo.setAudioSize(audioSize);
        audioInfo.setAudioAlbumName(audioAlbumName);
        audioInfo.setAudioType(audioType);
        audioInfo.setAudioDescribe(audioDescribe);
        audioInfo.setAudioHashKey(audioHashKey);
        audioInfo.setAddtime(addtime);
        return audioInfo;
    }

    /**
     * 对应生命周期调用
     */
    public void onDestroy(){
        if(null!=mCollectDB&&null!=mCollectDB.get()){
            mCollectDB.get().close();
            mCollectDB.clear();
        }
        if(null!=mHistroyDB&&null!=mHistroyDB.get()){
            mHistroyDB.get().close();
            mHistroyDB.clear();
        }
        if(null!=mSearchDB&&null!=mSearchDB.get()){
            mSearchDB.get().close();
            mSearchDB.clear();
        }
        mInstance=null;
    }
}