package com.github.why168.multifiledownloader;

/**
 * 下载状态
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:27
 * @since JDK1.8
 */
public class DownLoadState {
    /**
     * 默认
     */
    public static final int STATE_NONE = -1;
    /**
     * 等待中
     */
    public static final int STATE_WAITING = 1;
    /**
     * 下载中
     */
    public static final int STATE_DOWNLOADING = 2;
    /**
     * 暂停
     */
    public static final int STATE_PAUSED = 3;
    /**
     * 下载完毕
     */
    public static final int STATE_DOWNLOADED = 4;
    /**
     * 下载失败
     */
    public static final int STATE_ERROR = 5;
    /**
     * 连接中
     */
    public static final int STATE_CONNECTION = 6;
    /**
     * 删除
     */
    public static final int STATE_DELETE = 7;
}
