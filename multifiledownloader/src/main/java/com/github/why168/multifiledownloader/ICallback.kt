package com.github.why168.multifiledownloader

/**
 * @author Edwin.Wu
 * @version 2017/6/29 11:47
 * @since JDK1.8
 */
interface ICallback {
    fun onConnection()
    fun onProgress()
    fun onPaused()
    fun onCompleted()
    fun onError()
    fun onRetry()
}