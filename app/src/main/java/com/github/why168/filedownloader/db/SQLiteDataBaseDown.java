package com.github.why168.filedownloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.why168.filedownloader.constant.Constants;


/**
 * 数据库
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:09
 * @since JDK1.8
 */
public class SQLiteDataBaseDown extends SQLiteOpenHelper {
    /**
     * 数据库操作链接
     */
    private SQLiteDatabase db;
    /**
     * 游标对象
     */
    private Cursor cursor;


    public SQLiteDataBaseDown(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createTable(SQLiteDatabase db) {
        String sql_down = "create table if not exists "
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
        db.execSQL(sql_down);
    }


    /**
     * 获取数据库写入权限操作对象.
     *
     * @return 数据库写入权限操作对象.
     */
    public SQLiteDatabase getWrite() {
        if (null == db || db.isReadOnly()) {
            db = this.getWritableDatabase();
        }
        return db;
    }

    /**
     * 获取数据库读取权限操作对象.
     *
     * @return 数据库读取权限操作对象.
     */
    public SQLiteDatabase getRead() {
        if (null == db || !db.isReadOnly()) {
            db = this.getReadableDatabase();
        }
        return db;
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
        if (titles.length != values.length) // 判断传入的字段名数量与插入数据的数量是否相等
            return false;
        else {
            if (null == db || db.isReadOnly()) {
                db = this.getWritableDatabase();
            }
            if (db.isOpen()) {
                // 将插入值与对应字段放入ContentValues实例中
                ContentValues contentValues = new ContentValues();
                for (int i = 0; i < titles.length; i++) {
                    contentValues.put(titles[i], values[i]);
                }
                getWrite().insert(table, null, contentValues); // 执行插入操作
                if (needClose) {
                    close();
                }
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
        if (null == db || db.isReadOnly()) {
            db = this.getWritableDatabase();
        }
        if (db.isOpen()) {
            getWrite().delete(table, conditions, whereValues); // 执行删除操作
        }
        if (needClose) {
            close();
        }
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
            if (null == db || db.isReadOnly()) {
                db = this.getWritableDatabase();
            }
            if (db.isOpen()) {
                // 将插入值与对应字段放入ContentValues实例中
                ContentValues contentValues = new ContentValues();
                for (int i = 0; i < titles.length; i++) {
                    contentValues.put(titles[i], values[i]);
                }
                getWrite().update(table, contentValues, conditions, whereValues); // 执行修改操作
                if (needClose) {
                    close();
                }
                return true;
            } else {
                return false;
            }
        }
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
        cursor = getRead().query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit); // 执行查询操作.
        return cursor;
    }


    /**
     * 关闭打开的所有数据库对象.
     */
    public void close() {
        if (null != cursor && !cursor.isClosed()) {
            cursor.close();
        }
        if (null != db && db.isOpen()) {
            db.close();
            db = null;
        }
        super.close();
    }
}
