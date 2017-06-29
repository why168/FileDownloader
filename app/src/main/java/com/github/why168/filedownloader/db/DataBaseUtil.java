package com.github.why168.filedownloader.db;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.Constants;

import java.util.ArrayList;

/**
 * 数据操作工具类
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:47
 * @since JDK1.8
 */
public class DataBaseUtil {

    public synchronized static boolean insertDown(Context context, DownLoadBean bean) {
        String table = Constants.TABLE_DOWN;// 表名
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
        String[] values = new String[]{
                bean.id + "",
                bean.appName + "",
                bean.appIcon + "",
                bean.url + "",
                bean.path + "",
                bean.downloadState + "",
                bean.totalSize + "",
                bean.currentSize + "",
                bean.isSupportRange ? 1 + "" : 0 + ""};

        return DBHelper.insertInfo(context, true, table, titles, values);
    }

    /**
     * 根据id获取数据
     */
    public synchronized static DownLoadBean getDownLoadById(Context context, String DownloadID) {
        DownLoadBean bean = null;
        Cursor cursor = DBHelper.selectInfo(context, Constants.TABLE_DOWN,
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
        return bean;
    }

    /**
     * 获取所有数据
     */
    public synchronized static ArrayList<DownLoadBean> getDownLoad(Context context) {
        ArrayList<DownLoadBean> list = new ArrayList<DownLoadBean>();
        Cursor cursor = DBHelper.selectInfo(context, Constants.TABLE_DOWN, new String[]{"*"}, null, null, null, null, null, null);
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
        return list;
    }

    /**
     * 修改下载数据库
     */
    public synchronized static void UpdateDownLoadById(Context context, DownLoadBean bean) {
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


        DBHelper.updateInfo(context, true, Constants.TABLE_DOWN, titles, values, Constants.DOWN_ID + " =? ", new String[]{bean.id});
    }

    /**
     * 删除下载数据库数据
     */
    public synchronized static void DeleteDownLoadById(Context context, String id) {
        if (!TextUtils.isEmpty(id)) {
            DBHelper.deleteInfo(context, true, Constants.TABLE_DOWN, Constants.DOWN_ID + " =? ", new String[]{id});
        }
    }

}
