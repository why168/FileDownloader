package com.github.why168.multifiledownloader.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.why168.multifiledownloader.Constants;
import com.github.why168.multifiledownloader.DownLoadBean;
import com.github.why168.multifiledownloader.DownLoadState;
import com.github.why168.multifiledownloader.db.DataBaseUtil;
import com.github.why168.multifiledownloader.utlis.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 文件下载
 *
 * @author Edwin.Wu
 * @version 2017/1/16 14:17
 * @since JDK1.8
 */
public class AsyncDownCall extends NickRunnable {
    private final Context context;
    private final Handler handler;
    private DownLoadBean bean;
    private AtomicBoolean isPaused;

    @SuppressLint("SimpleDateFormat")
    public AsyncDownCall(Context context, Handler handler, DownLoadBean loadBean) {
        super("AndroidHttp %s", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Calendar.getInstance().getTime()));
        this.context = context;
        this.handler = handler;
        this.bean = loadBean;
        this.isPaused = new AtomicBoolean(false);
    }


    @Override
    public void execute() {
        RandomAccessFile raf = null;
        FileOutputStream fos = null;
        InputStream is = null;

        File destFile = FileUtilities.INSTANCE.getDownloadFile(bean.url);
        bean.path = destFile.getPath();
        HttpURLConnection connection = null;
        bean.downloadState = DownLoadState.STATE_DOWNLOADING.getIndex();
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

            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                Log.d("Edwin", bean.appName + " code = " + HttpURLConnection.HTTP_PARTIAL);
                bean.isSupportRange = true;
                raf = new RandomAccessFile(destFile, "rws");
                raf.seek(bean.currentSize);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused.get()) {
                        break;
                    }

                    raf.write(buffer, 0, len);
                    bean.currentSize += len;
                    DataBaseUtil.UpdateDownLoadById(context, bean);
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING.getIndex());
                }
            } else if (responseCode == HttpURLConnection.HTTP_OK) {

                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equalsIgnoreCase(ranges)) {
                    bean.isSupportRange = true;
                }

                Log.d("Edwin", bean.appName + " code = " + HttpURLConnection.HTTP_OK);
                bean.currentSize = 0;
                fos = new FileOutputStream(destFile);
                fos.getChannel().force(true); // 文件数据和元数据强制写到磁盘
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused.get()) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    fos.flush();
                    fos.getFD().sync();
                    bean.currentSize += len;
                    DataBaseUtil.UpdateDownLoadById(context, bean);
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING.getIndex());
                }
            } else {
                bean.downloadState = DownLoadState.STATE_ERROR.getIndex();
                DataBaseUtil.UpdateDownLoadById(context, bean);
                notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR.getIndex());
            }

            if (isPaused.get()) {
                bean.downloadState = DownLoadState.STATE_PAUSED.getIndex();
                DataBaseUtil.UpdateDownLoadById(context, bean);
                notifyDownloadStateChanged(bean, DownLoadState.STATE_PAUSED.getIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
            bean.downloadState = DownLoadState.STATE_ERROR.getIndex();
            DataBaseUtil.UpdateDownLoadById(context, bean);
            notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR.getIndex());
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                // 判断是否下载完成
                if (bean.currentSize == bean.totalSize) {
                    bean.downloadState = DownLoadState.STATE_DOWNLOADED.getIndex();
                    DataBaseUtil.UpdateDownLoadById(context, bean);
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADED.getIndex());
                }
            }
        }
    }


    public boolean isCanceled() {
        return isPaused.get();
    }

    public void cancel() {
        isPaused.set(true);
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
