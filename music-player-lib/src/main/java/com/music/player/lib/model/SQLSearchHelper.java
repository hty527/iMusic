package com.music.player.lib.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.music.player.lib.util.Logger;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/21
 * Search SQL
 */

public class SQLSearchHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLSearchHelper";
    //数据库名
    public static final String DB_NAME="imusic_search.db";
    //表名
    public static final String TABLE_NAME = "search";

    //创建数据库，key不能为空，不能重复
    private static final String CREATE_TABLE ="create table "+TABLE_NAME+"("
            + "id integer primary key autoincrement,"
            + "key varchar(255) not null unique,"
            + "addtime integer"
            + ");";

    public SQLSearchHelper(Context context) {
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