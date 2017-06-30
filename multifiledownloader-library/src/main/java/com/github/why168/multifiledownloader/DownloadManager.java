package com.github.why168.multifiledownloader;

import android.content.Context;
import android.content.Intent;

/**
 * DownloadManager
 *
 * @author Edwin.Wu
 * @version 2017/1/16 15:27
 * @since JDK1.8
 */
public class DownloadManager {
    private static DownloadManager instance;
    private final Context context;

    private DownloadManager(Context context) {
        this.context = context;
        context.startService(new Intent(context, DownLoadService.class));
    }

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context);
                }
            }
        }
        return instance;
    }


    public void down(DownLoadBean item) {
        Intent intent = new Intent(context, DownLoadService.class);
        intent.setAction("com.github.why168.multifiledownloader.downloadservice");
        intent.setPackage("com.github.why168.multifiledownloader");
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, item);
        intent.putExtra(Constants.KEY_OPERATING_STATE, false);
        context.startService(intent);
    }

    public void delete(DownLoadBean item) {
        Intent intent = new Intent(context, DownLoadService.class);
        intent.setAction("com.github.why168.multifiledownloader.downloadservice");
        intent.setPackage("com.github.why168.multifiledownloader");
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, item);
        intent.putExtra(Constants.KEY_OPERATING_STATE, true);
        context.startService(intent);
    }
}
