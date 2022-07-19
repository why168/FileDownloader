package com.github.why168.multifiledownloader.notify

import android.util.Log
import com.github.why168.multifiledownloader.notify.DownLoadObservable
import com.github.why168.multifiledownloader.DownLoadBean
import java.util.*

/**
 * 下载被观察者
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:25
 * @since JDK1.8
 */
object DownLoadObservable : Observable() {

    @JvmStatic
    fun dataChange(data: DownLoadBean) {
        Log.d(
            "Edwin",
            "DownLoadObservable dataChange " + data.downloadState + " , currentSize = " + data.currentSize
        )
        setChanged()
        this.notifyObservers(data)
    }
}