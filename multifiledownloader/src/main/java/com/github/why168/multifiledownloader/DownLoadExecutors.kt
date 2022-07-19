package com.github.why168.multifiledownloader

import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Edwin.Wu
 * @version 2017/6/28 23:46
 * @since JDK1.8
 */
class DownLoadExecutors internal constructor() : ThreadPoolExecutor(
    0, Int.MAX_VALUE, 60, TimeUnit.SECONDS, SynchronousQueue(),
    threadFactory("downLoad executors"),
    AbortPolicy()
) {
    override fun execute(command: Runnable) {
        super.execute(command)
    }

    companion object {
        private fun threadFactory(name: String): ThreadFactory {
            return ThreadFactory { runnable ->
                val result = Thread(runnable, name)
                result.isDaemon = false
                return@ThreadFactory result
            }
        }
    }

    init {
        allowCoreThreadTimeOut(true)
    }
}