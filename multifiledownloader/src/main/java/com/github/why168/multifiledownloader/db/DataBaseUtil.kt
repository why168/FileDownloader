package com.github.why168.multifiledownloader.db

import android.content.Context
import com.github.why168.multifiledownloader.db.DBHelper.Companion.insertInfo
import com.github.why168.multifiledownloader.db.DBHelper.Companion.selectInfo
import com.github.why168.multifiledownloader.db.DBHelper.Companion.updateInfo
import com.github.why168.multifiledownloader.db.DBHelper.Companion.deleteInfo
import kotlin.jvm.Synchronized
import com.github.why168.multifiledownloader.DownLoadBean
import android.text.TextUtils
import com.github.why168.multifiledownloader.Constants
import java.util.ArrayList

/**
 * 数据操作工具类
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:47
 * @since JDK1.8
 */
object DataBaseUtil {


    @JvmStatic
    @Synchronized
    fun insertDown(context: Context, bean: DownLoadBean): Boolean {
        val table = Constants.TABLE_DOWN // 表名

        /** 字段名对应字段值  */
        val titles = arrayOf<String?>(
            Constants.DOWN_ID,
            Constants.DOWN_NAME,
            Constants.DOWN_ICON,
            Constants.DOWN_URL,
            Constants.DOWN_FILE_PATH,
            Constants.DOWN_STATE,
            Constants.DOWN_FILE_SIZE,
            Constants.DOWN_FILE_SIZE_ING,
            Constants.DOWN_SUPPORT_RANGE
        )

        /** 字段值对应字段名  */
        val values = arrayOf<String?>(
            bean.id + "",
            bean.appName + "",
            bean.appIcon + "",
            bean.url + "",
            bean.path + "",
            bean.downloadState.toString() + "",
            bean.totalSize.toString() + "",
            bean.currentSize.toString() + "",
            if (bean.isSupportRange) 1.toString() + "" else 0.toString() + ""
        )
        return insertInfo(context, true, table, titles, values)
    }

    /**
     * 根据id获取数据
     */
    @JvmStatic
    @Synchronized
    fun getDownLoadById(context: Context, DownloadID: String?): DownLoadBean? {
        var bean: DownLoadBean? = null
        val cursor = selectInfo(
            context,
            Constants.TABLE_DOWN,
            arrayOf("*"),
            Constants.DOWN_ID + " = ? ",
            arrayOf(DownloadID),
            null,
            null,
            null,
            null
        )
        if (cursor.moveToNext()) {
            bean = DownLoadBean()
            bean.id = cursor.getString(cursor.getColumnIndex(Constants.DOWN_ID))
            bean.appName = cursor.getString(cursor.getColumnIndex(Constants.DOWN_NAME))
            bean.appIcon = cursor.getString(cursor.getColumnIndex(Constants.DOWN_ICON))
            bean.url = cursor.getString(cursor.getColumnIndex(Constants.DOWN_URL))
            bean.path = cursor.getString(cursor.getColumnIndex(Constants.DOWN_FILE_PATH))
            bean.downloadState = cursor.getInt(cursor.getColumnIndex(Constants.DOWN_STATE))
            bean.totalSize = cursor.getLong(cursor.getColumnIndex(Constants.DOWN_FILE_SIZE))
            bean.currentSize = cursor.getLong(cursor.getColumnIndex(Constants.DOWN_FILE_SIZE_ING))
            bean.isSupportRange =
                cursor.getLong(cursor.getColumnIndex(Constants.DOWN_SUPPORT_RANGE)) == 1L
        }
        cursor.close()
        return bean
    }

    /**
     * 获取所有数据
     */
    @JvmStatic
    @Synchronized
    fun getDownLoad(context: Context): ArrayList<DownLoadBean> {
        val list = ArrayList<DownLoadBean>()
        val cursor = selectInfo(
            context,
            Constants.TABLE_DOWN,
            arrayOf("*"),
            null,
            null,
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            val bean = DownLoadBean()
            bean.id = cursor.getString(cursor.getColumnIndex(Constants.DOWN_ID))
            bean.appName = cursor.getString(cursor.getColumnIndex(Constants.DOWN_NAME))
            bean.appIcon = cursor.getString(cursor.getColumnIndex(Constants.DOWN_ICON))
            bean.totalSize = cursor.getLong(cursor.getColumnIndex(Constants.DOWN_FILE_SIZE))
            bean.currentSize = cursor.getLong(cursor.getColumnIndex(Constants.DOWN_FILE_SIZE_ING))
            bean.downloadState = cursor.getInt(cursor.getColumnIndex(Constants.DOWN_STATE))
            bean.url = cursor.getString(cursor.getColumnIndex(Constants.DOWN_URL))
            bean.path = cursor.getString(cursor.getColumnIndex(Constants.DOWN_FILE_PATH))
            bean.isSupportRange =
                cursor.getLong(cursor.getColumnIndex(Constants.DOWN_SUPPORT_RANGE)) == 1L
            list.add(bean)
        }
        cursor.close()
        return list
    }

    /**
     * 修改下载数据库
     */
    @JvmStatic
    @Synchronized
    fun updateDownLoadById(context: Context, bean: DownLoadBean) {
        /** 字段名对应字段值  */
        val titles = arrayOf<String?>(
            Constants.DOWN_ID,
            Constants.DOWN_NAME,
            Constants.DOWN_ICON,
            Constants.DOWN_URL,
            Constants.DOWN_FILE_PATH,
            Constants.DOWN_STATE,
            Constants.DOWN_FILE_SIZE,
            Constants.DOWN_FILE_SIZE_ING,
            Constants.DOWN_SUPPORT_RANGE
        )

        /** 字段值对应字段名  */
        val values = arrayOf<String?>(
            bean.id + "",
            bean.appName + "",
            bean.appIcon + "",
            bean.url + "",
            bean.path + "",
            bean.downloadState.toString() + "",
            bean.totalSize.toString() + "",
            bean.currentSize.toString() + "",
            if (bean.isSupportRange) 1.toString() + "" else 0.toString() + ""
        )
        updateInfo(
            context,
            true,
            Constants.TABLE_DOWN,
            titles,
            values,
            Constants.DOWN_ID + " =? ",
            arrayOf(bean.id)
        )
    }

    /**
     * 删除下载数据库数据
     */
    @JvmStatic
    @Synchronized
    fun deleteDownLoadById(context: Context, id: String?) {
        if (!TextUtils.isEmpty(id)) {
            deleteInfo(
                context,
                true,
                Constants.TABLE_DOWN,
                Constants.DOWN_ID + " =? ",
                arrayOf(id)
            )
        }
    }
}