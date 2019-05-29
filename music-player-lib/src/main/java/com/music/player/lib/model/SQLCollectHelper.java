package com.music.player.lib.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.music.player.lib.util.Logger;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/21
 * Collect SQL
 */

public class SQLCollectHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLCollectHelper";
    //数据库名
    public static final String DB_NAME="imusic_collect.db";
    //表名
    public static final String TABLE_NAME = "collect";

    //创建数据库，audioId不能为空，不能重复
    private static final String CREATE_TABLE ="create table "+TABLE_NAME+" ("
            + "id integer primary key autoincrement,"
            + "audioId integer unique,"
            + "audioDurtion integer,"
            + "audioName varchar(255),"
            + "audioCover varchar(255),"
            + "audioPath varchar(255) not null,"
            + "nickname varchar(128),"
            + "userid varchar(128),"
            + "avatar varchar(128),"
            + "audioSize integer,"
            + "audioAlbumName varchar(128),"
            + "audioType varchar(128),"
            + "audioDescribe varchar(255),"
            + "audioHashKey varchar(255),"
            + "addtime integer"
            + " );";

    public SQLCollectHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d(TAG,"onUpgrade-->,oldVersion:"+oldVersion+",newVersion:"+newVersion);
    }
}