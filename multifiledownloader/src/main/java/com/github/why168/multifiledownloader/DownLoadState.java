package com.github.why168.multifiledownloader;

/**
 * 下载状态
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:27
 * @since JDK1.8
 */
public enum DownLoadState {

    STATE_NONE(0, "默认:点击下载"),
    STATE_CONNECTION(6, "连接中"),
    STATE_DOWNLOADING(2, "下载中"),
    STATE_WAITING(1, "等待中 排队状态"),
    STATE_DOWNLOADED(4, "下载完毕"),
    STATE_ERROR(5, "下载失败"),
    STATE_PAUSED(3, "暂停"),
    STATE_DELETE(7, "删除");

    private final int index;
    private final String name;

    private DownLoadState(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
