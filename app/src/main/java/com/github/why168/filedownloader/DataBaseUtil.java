package com.github.why168.filedownloader;

import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.Constants;
import com.github.why168.filedownloader.db.SQLiteDataBaseDown;

import java.io.File;
import java.util.ArrayList;

/**
 * 数据操作工具类
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:47
 * @since JDK1.8
 */
public class DataBaseUtil {
    public synchronized static void insertDown(DownLoadBean bean) {
        String table = Constants.TABLE_DOWN;// 表名
        SQLiteDataBaseDown helper = BaseApplication.getSqLiteDataBaseDown();


        /** 字段名对应字段值 **/
        String[] titles = new String[]{
                Constants.DOWN_ID,
                Constants.DOWN_NAME,
                Constants.DOWN_ICON,
                Constants.DOWN_URL,
                Constants.DOWN_FILE_PATH,
                Constants.DOWN_STATE,
                Constants.DOWN_FILE_SIZE,
                Constants.DOWN_FILE_SIZE_ING,
                Constants.DOWN_SUPPORT_RANGE};

        /** 字段值对应字段名 **/
        String[] values = new String[]{bean.id + "",
                bean.appName + "",
                bean.appIcon + "",
                bean.url + "",
                bean.path + "",
                bean.downloadState + "",
                bean.totalSize + "",
                bean.currentSize + "",
                bean.isSupportRange + ""};

        boolean insert = helper.insert(true, table, titles, values);
        Log.e("Edwin", "insert = " + insert + " \n " + bean.toString());
    }

    /**
     * 根据id获取数据
     */
    public synchronized static DownLoadBean getDownLoadById(String DownloadID) {
        DownLoadBean bean = null;

        SQLiteDataBaseDown helper = BaseApplication.getSqLiteDataBaseDown();

        Cursor cursor = helper.select(Constants.TABLE_DOWN,
                new String[]{"*"}, Constants.DOWN_ID + " = ? ",
                new String[]{DownloadID}, null, null, null, null);
        if (cursor.moveToNext()) {
            bean = new DownLoadBean();
            bean.id = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_ID));
            bean.appName = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_NAME));
            bean.appIcon = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_ICON));
            bean.url = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_URL));
            bean.path = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_FILE_PATH));
            bean.downloadState = cursor.getInt(cursor
                    .getColumnIndex(Constants.DOWN_STATE));
            bean.totalSize = cursor.getLong(cursor
                    .getColumnIndex(Constants.DOWN_FILE_SIZE));
            bean.currentSize = cursor.getLong(cursor
                    .getColumnIndex(Constants.DOWN_FILE_SIZE_ING));
            bean.isSupportRange = cursor.getLong(cursor
                    .getColumnIndex(Constants.DOWN_SUPPORT_RANGE)) != 0;
        }
        cursor.close();
        helper.close();
        return bean;
    }

    /**
     * 获取所有数据
     */
    public synchronized static ArrayList<DownLoadBean> getDownLoad() {
        ArrayList<DownLoadBean> list = new ArrayList<DownLoadBean>();
        SQLiteDataBaseDown helper = BaseApplication.getSqLiteDataBaseDown();

        Cursor cursor = helper.select(Constants.TABLE_DOWN, new String[]{"*"}, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            DownLoadBean bean = new DownLoadBean();
            bean.id = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_ID));
            bean.appName = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_NAME));
            bean.appIcon = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_ICON));
            bean.totalSize = cursor.getLong(cursor
                    .getColumnIndex(Constants.DOWN_FILE_SIZE));
            bean.currentSize = cursor.getLong(cursor
                    .getColumnIndex(Constants.DOWN_FILE_SIZE_ING));
            bean.downloadState = cursor.getInt(cursor
                    .getColumnIndex(Constants.DOWN_STATE));
            bean.url = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_URL));
            bean.path = cursor.getString(cursor
                    .getColumnIndex(Constants.DOWN_FILE_PATH));
            bean.isSupportRange = cursor.getLong(cursor
                    .getColumnIndex(Constants.DOWN_SUPPORT_RANGE)) != 0;

            list.add(bean);
        }
        cursor.close();
        helper.close();
        return list;
    }

    /**
     * 修改下载数据库
     */
    public synchronized static void UpdateDownLoadById(DownLoadBean bean) {
        SQLiteDataBaseDown helper = BaseApplication.getSqLiteDataBaseDown();

        /** 字段名对应字段值 **/
        String[] titles = new String[]{
                Constants.DOWN_ID,
                Constants.DOWN_NAME,
                Constants.DOWN_ICON,
                Constants.DOWN_URL,
                Constants.DOWN_FILE_PATH,
                Constants.DOWN_STATE,
                Constants.DOWN_FILE_SIZE,
                Constants.DOWN_FILE_SIZE_ING,
                Constants.DOWN_SUPPORT_RANGE};

        /** 字段值对应字段名 **/
        String[] values = new String[]{bean.id + "",
                bean.appName + "",
                bean.appIcon + "",
                bean.url + "",
                bean.path + "",
                bean.downloadState + "",
                bean.totalSize + "",
                bean.currentSize + "",
                bean.isSupportRange + ""};


        boolean update = helper.update(true, Constants.TABLE_DOWN, titles, values,
                Constants.DOWN_ID + " =? ", new String[]{bean.id});
        Log.e("Edwin", "update = " + update);
    }

    /**
     * 删除下载数据库数据
     */
    public synchronized static void DeleteDownLoadById(String id) {
        SQLiteDataBaseDown helper = BaseApplication.getSqLiteDataBaseDown();
        if (!TextUtils.isEmpty(id)) {
            helper.delete(true, Constants.TABLE_DOWN, Constants.DOWN_ID + " =? ", new String[]{id});
        }
//            helper.delete(true, Constants.TABLE_DOWN, null, null);
    }

    public synchronized static File getDownloadFile(String url) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(downloadDir, FileUtilities.getMd5FileName(url));
    }
}
