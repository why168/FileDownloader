package com.github.why168.filedownloader.runnable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.Constants;
import com.github.why168.filedownloader.constant.DownLoadState;
import com.github.why168.filedownloader.db.DataBaseUtil;
import com.github.why168.filedownloader.utlis.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.why168.filedownloader.constant.DownLoadState.STATE_DOWNLOADED;

/**
 * @author Edwin.Wu
 * @version 2017/1/16 14:17
 * @since JDK1.8
 */
public class DownLoadTask implements Runnable {
    private final Context context;
    private final Handler handler;
    private final ConcurrentHashMap<String, DownLoadTask> mTaskMap;
    private DownLoadBean bean;
    private volatile boolean isPaused = false;

    public DownLoadTask(Context context, Handler handler, ConcurrentHashMap<String, DownLoadTask> mTaskMap, DownLoadBean loadBean) {
        this.context = context;
        this.handler = handler;
        this.mTaskMap = mTaskMap;
        this.bean = loadBean;
    }


    @Override
    public void run() {
        File destFile = FileUtilities.getDownloadFile(bean.url);
        bean.path = destFile.getPath();
        HttpURLConnection connection = null;
        bean.downloadState = DownLoadState.STATE_DOWNLOADING;
        try {
            connection = (HttpURLConnection) new URL(bean.url).openConnection();
            connection.setRequestMethod("GET");
            if (bean.isSupportRange) {
                connection.setRequestProperty("Range", "bytes=" + bean.currentSize + "-" + bean.totalSize);
            }
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();


            RandomAccessFile raf = null;
            FileOutputStream fos = null;
            InputStream is = null;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                Log.e("Edwin", bean.appName + " code = " + HttpURLConnection.HTTP_PARTIAL);
                raf = new RandomAccessFile(destFile, "rw");
                raf.seek(bean.currentSize);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused) {
                        break;
                    }
                    raf.write(buffer, 0, len);
                    bean.currentSize += len;
                    DataBaseUtil.UpdateDownLoadById(context, bean);
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING);
                }
                raf.close();
                is.close();
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.e("Edwin", bean.appName + " code = " + HttpURLConnection.HTTP_OK);
                bean.currentSize = 0;
                fos = new FileOutputStream(destFile);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    bean.currentSize += len;
                    DataBaseUtil.UpdateDownLoadById(context, bean);
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING);
                }
                fos.close();
                is.close();
            } else {
                bean.downloadState = DownLoadState.STATE_ERROR;
                DataBaseUtil.UpdateDownLoadById(context, bean);
                notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
            }


            if (isPaused) {
                bean.downloadState = DownLoadState.STATE_PAUSED;
                DataBaseUtil.UpdateDownLoadById(context, bean);
                notifyDownloadStateChanged(bean, DownLoadState.STATE_PAUSED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            bean.downloadState = DownLoadState.STATE_ERROR;
            DataBaseUtil.UpdateDownLoadById(context, bean);
            notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
        }

        //TODO 判断是否下载完成
        if (bean.currentSize == bean.totalSize) {
            bean.downloadState = STATE_DOWNLOADED;
            DataBaseUtil.UpdateDownLoadById(context, bean);
            notifyDownloadStateChanged(bean, STATE_DOWNLOADED);
        }
    }


    public boolean isPaused() {
        return isPaused;
    }

    public void cancel() {
        isPaused = true;
    }
    
    /**
     * 当下载状态发送改变的时候调用
     */
    private void notifyDownloadStateChanged(DownLoadBean bean, int state) {
        Message message = handler.obtainMessage();
        message.obj = bean;
        message.what = state;
        handler.sendMessage(message);
    }

}
