package com.hodaz.goodbyecyword.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.hodaz.goodbyecyword.CommonLog;
import com.hodaz.goodbyecyword.model.Post;

/**
 * Created by hodaz on 2016. 3. 11..
 */
public class DBAdapter {
    private static final String TAG = "DBAdapter";
    private static final String POST_TABLE = "cyworld_post";

    private Context context = null;
    private DBHelper dbHelper = null;
    private SQLiteDatabase db = null;

    public DBAdapter(Context context) {
        this.context = context;
    }

    public DBAdapter open() throws SQLException {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        try {
            dbHelper.close();
        } catch (Exception ex) {
            CommonLog.e(TAG, ex);
        }
    }

    /**
     * 포스트 인서트
     * @param post
     * @return
     */
    public long insertPost(Post post) {
        if (post != null) {
            ContentValues values = new ContentValues();
            values.put("folder_id", post.folderID);
            values.put("folder_title", post.folderTitle);
            values.put("post_id", post.postID);
            values.put("post_title", post.postTitle);
            values.put("post_img", post.postImg);
            values.put("date", System.currentTimeMillis());

            long rowId = db.insert(POST_TABLE, null, values);
            return rowId;
        } else {
            return -1;
        }
    }

    /**
     * 포스트 모두 지우기
     * @return
     */
    public boolean deletePost() {
        int result = -1;

        try {
            result = db.delete(POST_TABLE, null, null);
        }
        catch (Exception e) {
            CommonLog.e(TAG, e);
        }

        return result >= 1;
    }

    public static final String[] COL_MUSIC_INFO = {
        "_id", "folder_id", "folder_title", "post_id", "post_title", "post_image", "date"
    };

    /**
     * 전체 포스트 가져오기
     * @return
     */
    public Cursor fetchAll() {
        CommonLog.e(TAG, "fetchAll()");
        return db.query(POST_TABLE, COL_MUSIC_INFO, null, null, null, null, "_id desc");
    }
}
