package com.github.why168.filedownloader;

import android.content.Context;
import android.content.Intent;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.Constants;
import com.github.why168.filedownloader.service.DownLoadService;

/**
 * @author Edwin.Wu
 * @version 2017/1/16 15:27
 * @since JDK1.8
 */
public class DownloadManager {

    private static DownloadManager mInstance;
    private final Context context;

    private DownloadManager(Context context) {
        this.context = context;
        context.startService(new Intent(context, DownLoadService.class));
    }

    public synchronized static DownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
        }
        return mInstance;
    }

    public void down(DownLoadBean item) {
        Intent intent = new Intent(context, DownLoadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, item);
        intent.putExtra(Constants.KEY_OPERATING_STATE, false);
        context.startService(intent);
    }

    public void delete(DownLoadBean item){
        Intent intent = new Intent(context, DownLoadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, item);
        intent.putExtra(Constants.KEY_OPERATING_STATE, true);
        context.startService(intent);
    }
}
