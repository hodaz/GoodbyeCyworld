package com.hodaz.goodbyecyword.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hodaz.goodbyecyword.CommonLog;

/**
 * Created by hodaz on 2016. 3. 11..
 */
class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private Context mContext;

    private static final String DATABASE_NAME = "goodbycyworld.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE cyworld_post (" +
            "_id INTEGER PRIMARY KEY," +
            "folder_id TEXT," +
            "folder_title TEXT," +
            "post_id TEXT," +
            "post_title TEXT," +
            "post_img TEXT," +
            "insert_date INTEGER" +
            ");";

    private static final String SQL_CREATE_INDEX =
            "CREATE INDEX idx_folder_id ON cyworld_post(post_id)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 기본 테이블 생성
        db.execSQL(SQL_CREATE_TABLE);

        // 인덱스 생성
        db.execSQL(SQL_CREATE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        CommonLog.i(TAG, "onUpgrade " + oldVersion + " to " + currentVersion);

        while (oldVersion < currentVersion) {
            oldVersion++;

            // 루핑 돌면서 버전별 변경사항 반영
        }
    }
}
