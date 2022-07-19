package com.github.why168.multifiledownloader.utlis

/**
 * 下载配置
 *
 * @author Edwin.Wu
 * @version 2017/1/10 11:43
 * @since JDK1.8
 */
object DownLoadConfig {

    /**
     * 下载的任务数
     */
    private var maxTasks = 5

    @JvmStatic
    fun getMaxTasks(): Int {
        return maxTasks
    }

    @JvmStatic
    fun setMaxTasks(maxTasks: Int) {
        if (maxTasks <= 0) {
            this.maxTasks = 5
        } else {
            this.maxTasks = maxTasks
        }
    }
}