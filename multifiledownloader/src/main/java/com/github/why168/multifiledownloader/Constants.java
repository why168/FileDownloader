package com.github.why168.multifiledownloader;

import android.os.Environment;

/**
 * 常量
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:29
 * @since JDK1.8
 */
public final class Constants {
    /**
     * 数据库版本
     */
    public static final int DATA_BASE_VERSION = 1;
    /**
     * 数据库名字
     */
    public static final String DATA_BASE_DOWN = "file_downloader.db";
    /**
     * 默认地址
     */
    private static final String PATH_BASE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DownLoadFile/";
    /**
     * 下载完成文件
     */
    public static final String PATH_COMPLETE = PATH_BASE + "Files/";

    // 下载表
    public static final String TABLE_DOWN = "table_down";
    // 自增长id
    public static final String ID = "id";
    // 下载id
    public static final String DOWN_ID = "down_id";
    // app名字
    public static final String DOWN_NAME = "down_name";
    // app图片
    public static final String DOWN_ICON = "down_icon";
    // app存放路径
    public static final String DOWN_FILE_PATH = "down_file_path";
    // app下载地址
    public static final String DOWN_URL = "down_url";
    // 下载状态
    public static final String DOWN_STATE = "down_state";
    // app总大小
    public static final String DOWN_FILE_SIZE = "down_file_size";
    // app下载进度
    public static final String DOWN_FILE_SIZE_ING = "down_file_size_ing";
    // aap下载是否支持断点下载
    public static final String DOWN_SUPPORT_RANGE = "down_support_range";


    public static final int CONNECT_TIME = 30 * 1000;
    public static final int READ_TIME = 30 * 1000;
    // 下载的实体类key
    public static final String KEY_DOWNLOAD_ENTRY = "key_download_entry";
    // 下载操作状态key
    public static final String KEY_OPERATING_STATE = "key_operating_state";

    public static final String ACTION_DOWNLOAD_BROAD_CAST = "action_download_broad_cast";

    public static final String action = "com.github.why168.multifiledownloader.downloadservice";
    public static final String packageName = "com.github.why168.multifiledownloader";
}
