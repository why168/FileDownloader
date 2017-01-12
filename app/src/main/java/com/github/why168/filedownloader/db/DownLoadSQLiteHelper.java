package com.github.why168.filedownloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.why168.filedownloader.constant.Constants;

/**
 * 数据库
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:09
 * @since JDK1.8
 */
public class DownLoadSQLiteHelper extends SQLiteOpenHelper {
    private static DownLoadSQLiteHelper instance;

    static SQLiteDatabase getInstance(Context context) {
        if (instance == null)
            synchronized (DownLoadSQLiteHelper.class) {
                if (instance == null)
                    instance = new DownLoadSQLiteHelper(context);
            }
        return instance.getReadableDatabase();
    }

    private DownLoadSQLiteHelper(Context context) {
        super(context, Constants.DATA_BASE_DOWN, null, Constants.DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(DownLoadSQLiteHelper.this.getClass().toString(), "onUpgrade ----> oldVersion = " + oldVersion + ",newVersion = " + newVersion);
    }

    private void createTable(SQLiteDatabase db) {
        String sql = "create table if not exists "
                + Constants.TABLE_DOWN + "(" // 创建下载表
                + Constants.ID + " integer PRIMARY KEY autoincrement, " // 自增长id.
                + Constants.DOWN_ID + " varchar, " // 下载id
                + Constants.DOWN_NAME + " varchar, " // app的名字
                + Constants.DOWN_ICON + " varchar, " // app的图片
                + Constants.DOWN_URL + " varchar, " // 广告下载的url
                + Constants.DOWN_FILE_PATH + " varchar, " // 文件存放路径
                + Constants.DOWN_STATE + " int, " // 下载状态
                + Constants.DOWN_FILE_SIZE + " int, " // 文件总大小
                + Constants.DOWN_FILE_SIZE_ING + " int, " // 下载进度
                + Constants.DOWN_SUPPORT_RANGE + " smallint" // 是否断点下载  0 (false) and 1 (true).
                + ");";
        db.execSQL(sql);
    }
}
