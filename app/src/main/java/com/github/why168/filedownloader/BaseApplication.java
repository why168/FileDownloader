package com.github.why168.filedownloader;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.github.why168.filedownloader.constant.Constants;
import com.github.why168.filedownloader.db.SQLiteDataBaseDown;

/**
 * @author Edwin.Wu
 * @version 2016/12/28 14:49
 * @since JDK1.8
 */
public class BaseApplication extends Application {
    public static Context mContext;

    /**
     * 数据库操作
     */
    public static SQLiteDataBaseDown sqLiteDataBaseDown;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

    }


    /**
     * 获取数据库对象
     */
    public static SQLiteDataBaseDown getSqLiteDataBaseDown() {
        if (sqLiteDataBaseDown == null) {
            sqLiteDataBaseDown = new SQLiteDataBaseDown(mContext, Constants.DATA_BASE_DOWN);
        }
        return sqLiteDataBaseDown;
    }
}
