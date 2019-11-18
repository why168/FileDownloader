package com.github.why168.multifiledownloader

import android.os.Environment

/**
 * 常量
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:29
 * @since JDK1.8
 */
public object Constants {

    /**
     * 数据库版本
     */
    const val DATA_BASE_VERSION = 1

    /**
     * 数据库名字
     */
    const val DATA_BASE_DOWN = "file_downloader.db"

    /**
     * 默认地址
     */
    val PATH_BASE = Environment.getExternalStorageDirectory().absolutePath + "/file_downloader/"

    // 下载表
    const val TABLE_DOWN = "table_down"

    // 自增长id
    const val ID = "id"

    // 下载id
    const val DOWN_ID = "down_id"

    // app名字
    const val DOWN_NAME = "down_name"

    // app图片
    const val DOWN_ICON = "down_icon"

    // app存放路径
    const val DOWN_FILE_PATH = "down_file_path"

    // app下载地址
    const val DOWN_URL = "down_url"

    // 下载状态
    const val DOWN_STATE = "down_state"

    // app总大小
    const val DOWN_FILE_SIZE = "down_file_size"

    // app下载进度
    const val DOWN_FILE_SIZE_ING = "down_file_size_ing"

    // aap下载是否支持断点下载
    const val DOWN_SUPPORT_RANGE = "down_support_range"


    const val CONNECT_TIME = 30 * 1000
    const val READ_TIME = 30 * 1000

    // 下载的实体类key
    const val KEY_DOWNLOAD_ENTRY = "key_download_entry"

    // 下载操作状态key
    const val KEY_OPERATING_STATE = "key_operating_state"

    const val ACTION_DOWNLOAD_BROAD_CAST = "action_download_broad_cast"

    const val action = "com.github.why168.multifiledownloader.downloadservice"
    const val packageName = "com.github.why168.multifiledownloader"
}
