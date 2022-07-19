package com.github.why168.multifiledownloader.call

import com.github.why168.multifiledownloader.utlis.FileUtilities.getDownloadFile
import com.github.why168.multifiledownloader.db.DataBaseUtil.updateDownLoadById
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import com.github.why168.multifiledownloader.Constants
import com.github.why168.multifiledownloader.DownLoadBean
import com.github.why168.multifiledownloader.DownLoadState
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 文件下载
 *
 * @author Edwin.Wu
 * @version 2017/1/16 14:17
 * @since JDK1.8
 */
class AsyncDownCall @SuppressLint("SimpleDateFormat") constructor(
    private val context: Context,
    private val handler: Handler,
    private val bean: DownLoadBean
) : NickRunnable(
    "AndroidHttp %s", SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(
        Calendar.getInstance().time
    )
) {
    private val isPaused: AtomicBoolean = AtomicBoolean(false)

    public override fun execute() {
        var raf: RandomAccessFile? = null
        var fos: FileOutputStream? = null
        var `is`: InputStream? = null
        val destFile = getDownloadFile(bean.url)

        bean.path = destFile.path
        var connection: HttpURLConnection? = null
        bean.downloadState = DownLoadState.STATE_DOWNLOADING.index
        try {
            connection = URL(bean.url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            if (bean.isSupportRange) {
                connection.setRequestProperty(
                    "Range",
                    "bytes=" + bean.currentSize + "-" + bean.totalSize
                )
            }
            connection.connectTimeout = Constants.CONNECT_TIME
            connection.readTimeout = Constants.READ_TIME
            val responseCode = connection.responseCode
            val contentLength = connection.contentLength
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                Log.d("Edwin", bean.appName + " code = " + HttpURLConnection.HTTP_PARTIAL)
                bean.isSupportRange = true
                raf = RandomAccessFile(destFile, "rws")
                raf.seek(bean.currentSize)
                `is` = connection.inputStream

                val buffer = ByteArray(2048)
                var len: Int
                while (`is`.read(buffer).also { len = it } != -1) {
                    if (isPaused.get()) {
                        break
                    }
                    raf.write(buffer, 0, len)
                    bean.currentSize += len.toLong()
                    updateDownLoadById(context, bean)
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING.index)
                }
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                val ranges = connection.getHeaderField("Accept-Ranges")
                if ("bytes".equals(ranges, ignoreCase = true)) {
                    bean.isSupportRange = true
                }
                Log.d("Edwin", bean.appName + " code = " + HttpURLConnection.HTTP_OK)
                bean.currentSize = 0L
                fos = FileOutputStream(destFile)
                fos.channel.force(true) // 文件数据和元数据强制写到磁盘
                `is` = connection.inputStream
                val buffer = ByteArray(2048)
                var len: Int
                while (`is`.read(buffer).also { len = it } != -1) {
                    if (isPaused.get()) {
                        break
                    }
                    fos.write(buffer, 0, len)
                    fos.flush()
                    fos.fd.sync()
                    bean.currentSize += len.toLong()
                    updateDownLoadById(context, bean)
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING.index)
                }
            } else {
                bean.downloadState = DownLoadState.STATE_ERROR.index
                updateDownLoadById(context, bean)
                notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR.index)
            }
            if (isPaused.get()) {
                bean.downloadState = DownLoadState.STATE_PAUSED.index
                updateDownLoadById(context, bean)
                notifyDownloadStateChanged(bean, DownLoadState.STATE_PAUSED.index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bean.downloadState = DownLoadState.STATE_ERROR.index
            updateDownLoadById(context, bean)
            notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR.index)
        } finally {
            try {
                raf?.close()
                fos?.close()
                `is`?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
                // 判断是否下载完成
                if (bean.currentSize == bean.totalSize) {
                    bean.downloadState = DownLoadState.STATE_DOWNLOADED.index
                    updateDownLoadById(context, bean)
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADED.index)
                }
            }
        }
    }

    val isCanceled: Boolean
        get() = isPaused.get()

    fun cancel() {
        isPaused.set(true)
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