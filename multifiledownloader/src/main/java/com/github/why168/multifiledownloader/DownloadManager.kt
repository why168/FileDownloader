package com.github.why168.multifiledownloader

import android.content.Context
import android.content.Intent
import java.lang.ref.SoftReference

/**
 * DownloadManager
 *
 * @author Edwin.Wu
 * @version 2017/1/16 15:27
 * @since JDK1.8
 */
class DownloadManager {

    private var context: Context? = null

    companion object {

        @Volatile
        private var instance: SoftReference<DownloadManager>? = null

        fun getInstance(context: Context): DownloadManager? {
            if (instance == null && instance?.get() == null) {
                synchronized(DownloadManager::class.java) {
                    if (instance == null && instance?.get() == null) {
                        instance = SoftReference<DownloadManager>(DownloadManager(context))
                    }
                }
            }
            return instance?.get()
        }
    }

    private constructor() {}

    private constructor(context: Context) {
        this.context = context
        context.startService(Intent(context, DownLoadService::class.java))
    }

    fun addTask(item: DownLoadBean) {
        context?.let {
            DownLoadService.addTask(it, item)
        }
    }

    fun deleteTask(item: DownLoadBean) {
        context?.let {
            DownLoadService.deleteTask(it, item)
        }
    }

    fun stopAll() {
        DownLoadService.waitingQueue.clear()

        DownLoadService.connectionTaskMap.forEach {
            it.value.cancel()
        }

        DownLoadService.downTaskMap.forEach {
            it.value.cancel()
        }
    }
}
