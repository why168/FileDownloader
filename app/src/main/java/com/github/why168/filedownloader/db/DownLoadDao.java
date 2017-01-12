package com.github.why168.filedownloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Edwin.Wu
 * @version 2017/1/12 14:45
 * @since JDK1.8
 */
public class DownLoadDao {
    private SQLiteDatabase db;

    public DownLoadDao(Context context) {
        this.db = DownLoadSQLiteHelper.getInstance(context);
    }


    /**
     * 查询数据库的指定表中的指定数据.
     *
     * @param table         表名.
     * @param columns       查询字段.
     * @param selection     条件字段.
     * @param selectionArgs 条件值.
     * @param groupBy       分组名称.
     * @param having        分组条件.与groupBy配合使用.
     * @param orderBy       排序字段.
     * @param limit         分页.
     * @return 查询结果游标
     */
    public Cursor select(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit); // 执行查询操作.
    }


    /**
     * 修改数据库的指定表中的指定数据.
     *
     * @param needClose   是否需要关闭数据库连接.true为关闭,否则不关闭.
     * @param table       表名.
     * @param titles      字段名.
     * @param values      数据值.
     * @param conditions  条件字段.
     * @param whereValues 条件值.
     * @return 若传入的字段名与插入值的长度不等则返回false, 否则执行成功则返回true.
     */
    public boolean update(boolean needClose, String table, String[] titles, String[] values, String conditions, String[] whereValues) {
        if (titles.length != values.length) {
            return false;
        } else {
            if (db.isOpen()) {
                // 将插入值与对应字段放入ContentValues实例中
                ContentValues contentValues = new ContentValues();
                for (int i = 0; i < titles.length; i++) {
                    contentValues.put(titles[i], values[i]);
                }
                db.update(table, contentValues, conditions, whereValues); // 执行修改操作
                return true;
            } else {
                return false;
            }
        }
    }


    /**
     * 删除数据库的指定表中的指定数据.
     *
     * @param needClose   是否需要关闭数据库连接.true为关闭,否则不关闭.
     * @param table       表名.
     * @param conditions  条件字段.
     * @param whereValues 条件值.
     */
    public void delete(boolean needClose, String table, String conditions, String[] whereValues) {
        db.delete(table, conditions, whereValues); // 执行删除操作
    }

    /**
     * 向数据库的指定表中插入数据.
     *
     * @param needClose 是否需要关闭数据库连接.true为关闭,否则不关闭.
     * @param table     表名.
     * @param titles    字段名.
     * @param values    数据值.
     * @return 若传入的字段名与插入值的长度不等则返回false, 否则执行成功则返回true.
     */
    public boolean insert(boolean needClose, String table, String[] titles, String[] values) {
        if (titles.length != values.length) { // 判断传入的字段名数量与插入数据的数量是否相等
            return false;
        } else {
            // 将插入值与对应字段放入ContentValues实例中
            ContentValues contentValues = new ContentValues();
            for (int i = 0; i < titles.length; i++) {
                contentValues.put(titles[i], values[i]);
            }
            db.insert(table, null, contentValues); // 执行插入操作
            return true;
        }
    }
}
