package com.github.why168.filedownloader;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.Constants;
import com.github.why168.filedownloader.constant.DownLoadState;
import com.github.why168.filedownloader.pattern.DownLoadObservable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Edwin.Wu
 * @version 2016/12/28 14:47
 * @since JDK1.8
 */
public class DownLoadManager {
    /**
     * 用于记录所有下载的任务，方便在取消下载时，通过id能找到该任务进行删除
     */
    private ConcurrentHashMap<String, DownLoadTask> mTaskMap = new ConcurrentHashMap<String, DownLoadTask>();

    /**
     * 当下载状态发送改变的时候回调
     */
    private ExecuteHandler handler = new ExecuteHandler();


    /**
     * 拿到主线程Looper
     */
    @SuppressLint("HandlerLeak")
    private class ExecuteHandler extends Handler {
        private ExecuteHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            DownLoadBean bean = (DownLoadBean) msg.obj;
            DownLoadObservable.getInstance().setData(bean);
        }
    }

    private final static class Instance {
        static final DownLoadManager instance = new DownLoadManager();
    }

    public static DownLoadManager getInstance() {
        return Instance.instance;
    }

    public void download(DownLoadBean loadBean) {
        //TODO 先判断是否连接过？得到过APK的大小？支持断点下载吗？
        if (loadBean.totalSize == 0) {
            ConnectThread connectThread = new ConnectThread(loadBean);
            DownLoadExecutor.execute(connectThread);
        } else {
            startDownload(loadBean);
        }
        //TODO 更新数据库
        DataBaseUtil.UpdateDownLoadById(loadBean);
        //TODO 每次状态发生改变，都需要回调该方法通知所有观察者
        notifyDownloadStateChanged(loadBean);
    }

    /**
     * 正在意义上开启下载
     *
     * @param loadBean
     */
    private void startDownload(DownLoadBean loadBean) {
        //TODO 先判断是否有这个app的下载信息,更新信息
        if (DataBaseUtil.getDownLoadById(loadBean.id) != null) {
            DataBaseUtil.UpdateDownLoadById(loadBean);
        } else {
            //TODO 插入数据库
            DataBaseUtil.insertDown(loadBean);
        }

        int state = loadBean.downloadState;
        switch (state) {
            case DownLoadState.STATE_NONE://默认
            case DownLoadState.STATE_CONNECTION://连接中
                downNone(loadBean);
                break;
            case DownLoadState.STATE_WAITING://等待中
                downWaiting(loadBean);
                break;
            case DownLoadState.STATE_DOWNLOADING://下载中
                downLoading(loadBean);
                break;
            case DownLoadState.STATE_PAUSED://暂停
                downPaused(loadBean);
                break;
            case DownLoadState.STATE_DOWNLOADED://下载完毕
                downLoaded();
                break;
            case DownLoadState.STATE_ERROR://下载失败
                downError(loadBean);
                break;
            default:
                break;
        }
    }

    public void DeleteDownTask(DownLoadBean item) {
        //TODO 删除文件，删除数据库
        try {
            item.downloadState = DownLoadState.STATE_DELETE;
            DownLoadTask remove = mTaskMap.remove(item.id);
            if (remove != null)
                remove.cancel();
            DataBaseUtil.DeleteDownLoadById(item.id);
            notifyDownloadStateChanged(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 当下载状态发送改变的时候调用
     */
    private void notifyDownloadStateChanged(DownLoadBean bean) {
        Message message = handler.obtainMessage();
        message.obj = bean;
        handler.sendMessage(message);
    }


    public class DownLoadTask implements Runnable {
        private volatile boolean isRunning = false;
        private DownLoadBean bean;

        public DownLoadTask(DownLoadBean loadBean) {
            this.bean = loadBean;
        }

        public void cancel() {
            isRunning = true;
        }

        public void start() {
            isRunning = false;
        }

        @Override
        public void run() {
            int state = bean.downloadState;
            switch (state) {
                case DownLoadState.STATE_NONE:
                case DownLoadState.STATE_WAITING:
                case DownLoadState.STATE_CONNECTION:
                    //TODO 马上下载
                    bean.downloadState = DownLoadState.STATE_DOWNLOADING;
                    RequestHttp();
                    break;
                case DownLoadState.STATE_PAUSED:
                    //TODO 如果是暂停，马上开始
                    bean.downloadState = DownLoadState.STATE_WAITING;
                    RequestHttp();
                    break;
                case DownLoadState.STATE_ERROR:
                    bean.downloadState = DownLoadState.STATE_ERROR;
                    DataBaseUtil.UpdateDownLoadById(bean);
                    notifyDownloadStateChanged(bean);
                case DownLoadState.STATE_DOWNLOADING:
                    Log.e("Edwin", "下载完毕");
                    break;
                default:
                    break;
            }
        }

        private void RequestHttp() {
            File destFile = DataBaseUtil.getDownloadFile(bean.url);
            bean.path = destFile.getPath();
            HttpURLConnection connection = null;

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
                    raf = new RandomAccessFile(destFile, "rw");
                    raf.seek(bean.currentSize);
                    is = connection.getInputStream();
                    byte[] buffer = new byte[2048];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        if (isRunning) {
                            break;
                        }
                        raf.write(buffer, 0, len);
                        bean.currentSize += len;
                        DataBaseUtil.UpdateDownLoadById(bean);
                        notifyDownloadStateChanged(bean);
                    }
                    raf.close();
                    is.close();
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    fos = new FileOutputStream(destFile);
                    is = connection.getInputStream();
                    byte[] buffer = new byte[2048];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        if (isRunning) {
                            break;
                        }

                        fos.write(buffer, 0, len);

                        bean.currentSize += len;
                        DataBaseUtil.UpdateDownLoadById(bean);
                        notifyDownloadStateChanged(bean);
                    }
                    fos.close();
                    is.close();
                } else {
                    bean.downloadState = DownLoadState.STATE_ERROR;
                    DataBaseUtil.UpdateDownLoadById(bean);
                    notifyDownloadStateChanged(bean);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                bean.downloadState = DownLoadState.STATE_ERROR;
                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean);
            }

            //TODO 判断是否下载完成
            if (bean.currentSize == bean.totalSize) {
                bean.downloadState = DownLoadState.STATE_DOWNLOADED;
                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean);
            }
        }

        public boolean isRunning() {
            return isRunning;
        }

    }


    /**
     * 连接的线程
     */
    private class ConnectThread implements Runnable {
        private DownLoadBean bean;
        private volatile boolean isRunning;

        public ConnectThread(DownLoadBean bean) {
            this.bean = bean;
        }

        @Override
        public void run() {
            isRunning = true;
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

                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean);
                Log.e("Edwin", "连接成功--isSupportRange = " + bean.isSupportRange);
                //TODO 开始下载咯
                startDownload(bean);
            } catch (IOException e) {
                isRunning = false;
                e.printStackTrace();
                bean.downloadState = DownLoadState.STATE_ERROR;
                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean);
                Log.e("Edwin", "连接失败");
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
            Log.e("Edwin", "No---Thread.currentThread() = " + Thread.currentThread().getName());
            isRunning = false;
        }
    }


    /**
     * 默认状态
     */
    private void downNone(DownLoadBean loadBean) {
        //TODO 1.状态修改成STATE_WAITING;
        loadBean.downloadState = DownLoadState.STATE_WAITING;
        //TODO 2.创建一个线程;
        DownLoadTask downLoadTask = new DownLoadTask(loadBean);
        //TODO 3.放入TaskMap集合;
        mTaskMap.put(loadBean.id, downLoadTask);
        //TODO 4.启动执行线程execute;
        DownLoadExecutor.execute(downLoadTask);
    }


    /**
     * 等待状态
     */
    private void downWaiting(DownLoadBean loadBean) {
        //TODO 1.状态修改成STATE_PAUSED;
        loadBean.downloadState = DownLoadState.STATE_PAUSED;
        //TODO 2.TaskMap获取线程对象，移除线程;
        DownLoadTask downLoadTask = mTaskMap.get(loadBean.id);
        downLoadTask.cancel();
        mTaskMap.remove(loadBean.id);
    }


    /**
     * 下载状态
     */
    private void downLoading(DownLoadBean loadBean) {
        //TODO 1.状态修改成STATE_PAUSED;
        loadBean.downloadState = DownLoadState.STATE_PAUSED;
        //TODO 2.TaskMap获取线程对象，移除线程;
        DownLoadTask downLoadTask = mTaskMap.get(loadBean.id);
        downLoadTask.cancel();
        mTaskMap.remove(loadBean.id);
    }

    /**
     * 暂停状态
     */
    private void downPaused(DownLoadBean loadBean) {
        //TODO 1.状态修改成STATE_WAITING;
        loadBean.downloadState = DownLoadState.STATE_WAITING;
        //TODO 2.创建一个线程,http断点续传Range-(start-end),是否支持断点下载,通过线程判断;
        DownLoadTask downLoadTask = new DownLoadTask(loadBean);
        //TODO 3.加入TaskMaP集合;
        mTaskMap.put(loadBean.id, downLoadTask);
        //TODO 4.启动执行线程execute
        DownLoadExecutor.execute(downLoadTask);
    }


    /**
     * 下载失败
     */
    private void downError(DownLoadBean loadBean) {
        //TODO 1.删除本地文件文件
        Log.e("Edwin", "删除本地文件文件 Id = " + loadBean.id);
        //TODO 2.删除数据库
        DataBaseUtil.DeleteDownLoadById(loadBean.id);
        //TODO 3.重新插入数据库
        DataBaseUtil.insertDown(loadBean);
        /*********以下操作与默认状态一样*********/

        //TODO 4.状态修改成STATE_WAITING；
        loadBean.downloadState = DownLoadState.STATE_WAITING;
        //TODO 5.创建一个线程;
        DownLoadTask downLoadTask = new DownLoadTask(loadBean);
        //TODO 6.放入TaskMap集合;
        mTaskMap.put(loadBean.id, downLoadTask);
        //TODO 7.启动执行线程execute
        DownLoadExecutor.execute(downLoadTask);
    }

    /**
     * 下载完成
     */
    private void downLoaded() {
        //TODO 安装应用
        Log.e("Edwin", "安装应用");
    }
}
