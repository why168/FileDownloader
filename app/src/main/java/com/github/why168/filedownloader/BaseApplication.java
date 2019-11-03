package com.github.why168.filedownloader;

import android.app.Application;
import android.content.Context;

import com.github.why168.multifiledownloader.utlis.DownLoadConfig;

/**
 * Application
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:49
 * @since JDK1.8
 */
public class BaseApplication extends Application {
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        // 初始化DownLoad
        DownLoadConfig.getConfig().setMaxTasks(3);
    }

}
