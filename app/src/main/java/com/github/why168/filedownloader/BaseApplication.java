package com.github.why168.filedownloader;

import android.app.Application;
import android.content.Context;

import com.github.why168.filedownloader.utlis.DownLoadConfig;

/**
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

        //TODO 初始化DownLoad
        DownLoadConfig.getConfig().setMax_download_tasks(2);
    }

}
