package com.github.why168.multifiledownloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.why168.multifiledownloader.db.DataBaseUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 连接的线程
 *
 * @author Edwin.Wu
 * @version 2017/1/16 14:15
 * @since JDK1.8
 */
public class AsyncConnectCall extends NickRunnable {
    private final Context context;
    private final Handler handler;
    private final ConcurrentHashMap<String, AsyncDownCall> mTaskMap;
    private final ExecutorService executorService;
    private DownLoadBean bean;
    private AtomicBoolean isRunning;

    @SuppressLint("SimpleDateFormat")
    public AsyncConnectCall(Context context, Handler handler,
                            ConcurrentHashMap<String, AsyncDownCall> mTaskMap,
                            ExecutorService executorService, DownLoadBean bean) {
        super("AndroidHttp %s", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Calendar.getInstance().getTime()));
        this.context = context;
        this.handler = handler;
        this.mTaskMap = mTaskMap;
        this.executorService = executorService;
        this.bean = bean;
        this.isRunning = new AtomicBoolean(true);
    }


    @Override
    protected void execute() {
        bean.downloadState = DownLoadState.STATE_CONNECTION;
        DataBaseUtil.UpdateDownLoadById(context, bean);
        notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION);

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
                if ("bytes".equalsIgnoreCase(ranges)) {
                    bean.isSupportRange = true;
                }
                bean.totalSize = contentLength;
            } else {
                bean.downloadState = DownLoadState.STATE_ERROR;
            }

//            DataBaseUtil.UpdateDownLoadById(context, bean);
//            notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION);
            Log.d("Edwin", "连接成功--isSupportRange = " + bean.isSupportRange);

            // 开始下载咯
            AsyncDownCall downLoadTask = new AsyncDownCall(context, handler, bean);
            mTaskMap.put(bean.id, downLoadTask);
            executorService.execute(downLoadTask);
        } catch (IOException e) {
            e.printStackTrace();
            isRunning.set(false);
            bean.downloadState = DownLoadState.STATE_ERROR;
            DataBaseUtil.UpdateDownLoadById(context, bean);
            notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
            Log.d("Edwin", "连接失败");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public boolean isCanceled() {
        return isRunning.get();
    }

    public void cancel() {
        isRunning.set(true);
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