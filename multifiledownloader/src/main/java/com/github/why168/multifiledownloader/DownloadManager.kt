package com.github.why168.multifiledownloader

import android.content.Context
import android.content.Intent
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

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
        DownLoadService.addTask(context, item)
    }

    fun deleteTask(item: DownLoadBean) {
        DownLoadService.deleteTask(context, item)
    }



    fun down(item: DownLoadBean) {
        val intent = Intent(context, DownLoadService::class.java)
        intent.action = Constants.action
        intent.setPackage(Constants.packageName)
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, item)
        intent.putExtra(Constants.KEY_OPERATING_STATE, false)
        context?.startService(intent)
    }

    fun delete(item: DownLoadBean) {
        val intent = Intent(context, DownLoadService::class.java)
        intent.action = Constants.action
        intent.setPackage(Constants.packageName)
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, item)
        intent.putExtra(Constants.KEY_OPERATING_STATE, true)
        context?.startService(intent)
    }



}
