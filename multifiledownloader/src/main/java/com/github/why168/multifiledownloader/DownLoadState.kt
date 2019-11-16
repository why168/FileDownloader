package com.github.why168.multifiledownloader

/**
 * 下载状态
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:27
 * @since JDK1.8
 */
enum class DownLoadState private constructor(val index: Int, val content: String) {

    STATE_NONE(0, "任务添加"),
    STATE_CONNECTION(1, "任务连接中"),
    STATE_DOWNLOADING(2, "下载中"),
    STATE_DOWNLOADED(3, "下载完毕"),
    STATE_ERROR(4, "下载失败"),
    STATE_PAUSED(5, "任务暂停"),
    STATE_WAITING(6, "任务排队Queue"),
    STATE_DELETE(7, "任务删除")
}
