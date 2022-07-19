package com.github.why168.multifiledownloader.call

import com.github.why168.multifiledownloader.db.DataBaseUtil.updateDownLoadById
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import com.github.why168.multifiledownloader.Constants
import com.github.why168.multifiledownloader.DownLoadBean
import com.github.why168.multifiledownloader.DownLoadState
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 连接的线程
 *
 * @author Edwin.Wu
 * @version 2017/1/16 14:15
 * @since JDK1.8
 */
@SuppressLint("SimpleDateFormat")
class AsyncConnectCall constructor(
    private val context: Context, private val handler: Handler,
    connectionTaskMap: ConcurrentHashMap<String, AsyncConnectCall>,
    private val downTaskMap: ConcurrentHashMap<String, AsyncDownCall>,
    private val executorService: ExecutorService,
    private val bean: DownLoadBean
) : NickRunnable(
    format = "AndroidHttp %s",
    args = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Calendar.getInstance().time)
) {
    private val connectionTaskMap: ConcurrentHashMap<String, AsyncConnectCall>
    private val isRunning: AtomicBoolean = AtomicBoolean(true)

    init {
        this.connectionTaskMap = connectionTaskMap
        this.connectionTaskMap[bean.id] = this
    }

    override fun execute() {
        bean.downloadState = DownLoadState.STATE_CONNECTION.index
        updateDownLoadById(context, bean)
        notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION.index)
        var connection: HttpURLConnection? = null
        try {
            connection = URL(bean.url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = Constants.CONNECT_TIME
            connection.readTimeout = Constants.READ_TIME
            val responseCode = connection.responseCode
            val contentLength = connection.contentLength
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val ranges = connection.getHeaderField("Accept-Ranges")
                if ("bytes".equals(ranges, ignoreCase = true)) {
                    bean.isSupportRange = true
                }
                bean.totalSize = (contentLength.toString() + "").toLong()
            } else {
                bean.downloadState = DownLoadState.STATE_ERROR.index
            }

//            UpdateDownLoadById(context, bean);
//            notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION.index);
            Log.d("Edwin", "连接成功--isSupportRange = " + bean.isSupportRange)

            // 开始下载咯
            val downLoadTask = AsyncDownCall(context, handler, bean)
            if (downTaskMap[bean.id] == null) {
                downTaskMap[bean.id] = downLoadTask
                executorService.execute(downLoadTask)

                // 移除
                connectionTaskMap.remove(bean.id)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            isRunning.set(false)
            bean.downloadState = DownLoadState.STATE_ERROR.index
            updateDownLoadById(context, bean)
            notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR.index)
            Log.d("Edwin", "连接失败")
        } finally {
            connection?.disconnect()
        }
    }

    fun cancel() {
        isRunning.set(true)
    }

    fun isCancel(): Boolean {
        return isRunning.get()
    }

    /**
     * 当下载状态发送改变的时候调用
     */
    private fun notifyDownloadStateChanged(bean: DownLoadBean, state: Int) {
        val message = handler.obtainMessage()
        message.obj = bean
        message.what = state
        handler.sendMessage(message)
    }
}