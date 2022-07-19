package com.github.why168.multifiledownloader.notify

import com.github.why168.multifiledownloader.DownLoadBean
import com.github.why168.multifiledownloader.DownLoadState
import java.util.*

/**
 * 观察者
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:39
 * @since JDK1.8
 */
class DownFileObserver : Observer {

    override fun update(o: Observable?, arg: Any?) {
        if (arg !is DownLoadBean) {
            return
        }
        when (arg.downloadState) {
            DownLoadState.STATE_NONE.index -> {}
            DownLoadState.STATE_WAITING.index -> onPrepare(arg)
            DownLoadState.STATE_DOWNLOADING.index -> onProgress(arg)
            DownLoadState.STATE_PAUSED.index -> onStop(arg)
            DownLoadState.STATE_DOWNLOADED.index -> onFinish(arg)
            DownLoadState.STATE_ERROR.index -> onError(arg)
        }
        o?.notifyObservers()
    }

    /**
     * 准备下载
     */
    fun onPrepare(bean: DownLoadBean?) {}

    /**
     * 开始下载
     */
    fun onStart(bean: DownLoadBean?) {}

    /**
     * 下载中
     */
    fun onProgress(bean: DownLoadBean?) {}

    /**
     * 暂停
     */
    fun onStop(bean: DownLoadBean?) {}

    /**
     * 下载完成
     */
    fun onFinish(bean: DownLoadBean?) {}

    /**
     * 下载失败
     */
    fun onError(bean: DownLoadBean?) {}
}