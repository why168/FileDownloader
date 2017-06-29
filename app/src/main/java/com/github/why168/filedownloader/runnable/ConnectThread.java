package com.github.why168.filedownloader.runnable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.Constants;
import com.github.why168.filedownloader.constant.DownLoadState;
import com.github.why168.filedownloader.db.DataBaseUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 连接的线程
 *
 * @author Edwin.Wu
 * @version 2017/1/16 14:15
 * @since JDK1.8
 */
public class ConnectThread implements Runnable {
    private final Context context;
    private final Handler handler;
    private final ConcurrentHashMap<String, DownLoadTask> mTaskMap;
    private final ExecutorService executorService;
    private DownLoadBean bean;
    private volatile boolean isRunning = true;

    public ConnectThread(Context context, Handler handler, ConcurrentHashMap<String, DownLoadTask> mTaskMap, ExecutorService executorService, DownLoadBean bean) {
        this.context = context;
        this.handler = handler;
        this.mTaskMap = mTaskMap;
        this.executorService = executorService;
        this.bean = bean;
    }

    @Override
    public void run() {
        bean.downloadState = DownLoadState.STATE_CONNECTION;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(bean.url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    bean.isSupportRange = true;
                }
                bean.totalSize = contentLength;
            } else {
                bean.downloadState = DownLoadState.STATE_ERROR;
            }

            DataBaseUtil.UpdateDownLoadById(context, bean);
            notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION);
            Log.i("Edwin", "连接成功--isSupportRange = " + bean.isSupportRange);

            //TODO 开始下载咯
            DownLoadTask downLoadTask = new DownLoadTask(context, handler, mTaskMap, bean);
            mTaskMap.put(bean.id, downLoadTask);
            executorService.execute(downLoadTask);

        } catch (IOException e) {
            isRunning = false;
            e.printStackTrace();
            bean.downloadState = DownLoadState.STATE_ERROR;
            DataBaseUtil.UpdateDownLoadById(context, bean);
            notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
            Log.i("Edwin", "连接失败");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        Log.i("Edwin", "No---Thread.currentThread() = " + Thread.currentThread().getName());
        isRunning = true;
        Thread.currentThread().interrupt();
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