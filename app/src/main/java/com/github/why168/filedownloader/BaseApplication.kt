package com.github.why168.filedownloader

import android.app.Application
import android.content.Context

import com.github.why168.multifiledownloader.utlis.DownLoadConfig

/**
 * Application
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:49
 * @since JDK1.8
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化DownLoad
        DownLoadConfig.setMaxTasks(3)
    }

}
