package com.github.why168.filedownloader;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.Constants;
import com.github.why168.filedownloader.constant.DownLoadState;
import com.github.why168.filedownloader.notify.DownLoadObservable;
import com.github.why168.filedownloader.db.DataBaseUtil;
import com.github.why168.filedownloader.utlis.DownLoadConfig;
import com.github.why168.filedownloader.utlis.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.why168.filedownloader.constant.DownLoadState.STATE_DELETE;
import static com.github.why168.filedownloader.constant.DownLoadState.STATE_DOWNLOADED;


/**
 * DownLoadManager
 *
 * @author Edwin.Wu
 * @version 2016/12/28 14:47
 * @since JDK1.8
 */
public class DownLoadManager {
    private Context context;
    /**
     * 用于记录所有下载的任务，方便在取消下载时，通过id能找到该任务进行删除
     */
    private ConcurrentHashMap<String, DownLoadTask> mTaskMap = new ConcurrentHashMap<String, DownLoadTask>();

    private ThreadPoolExecutor downLoadExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    /**
     * 当下载状态发送改变的时候回调
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownLoadBean bean = (DownLoadBean) msg.obj;
            int what = msg.what;
            switch (what) {
                case DownLoadState.STATE_ERROR:
                case DownLoadState.STATE_DOWNLOADED:
                case DownLoadState.STATE_DELETE:
                case DownLoadState.STATE_PAUSED:
                    Log.e("Message", "---> " + bean.toString());
                    mTaskMap.remove(bean.id);
                    DownLoadBean poll = mWaitingQueue.poll();
                    if (poll != null) {
                        downNone(poll);
                    }
                    break;
                default:
                    break;
            }

            DownLoadObservable.getInstance().setData(bean);
        }
    };

    private LinkedBlockingDeque<DownLoadBean> mWaitingQueue = new LinkedBlockingDeque<>();

    private final static class Instance {
        static final DownLoadManager instance = new DownLoadManager();
    }

    public static DownLoadManager getInstance() {
        return Instance.instance;
    }


    public void deleteDownTask(Context context, DownLoadBean item) {
        this.context = context;
        //TODO 删除文件，删除数据库
        try {
            DownLoadTask remove = mTaskMap.remove(item.id);
            if (remove != null) {
                remove.cancle();
            } else {
                mWaitingQueue.remove(item);
            }
            item.downloadState = STATE_DELETE;
            DataBaseUtil.DeleteDownLoadById(context, item.id);
            notifyDownloadStateChanged(item, STATE_DELETE);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private class DownLoadTask implements Runnable {
        private DownLoadBean bean;
        private volatile boolean isRunning = false;

        DownLoadTask(DownLoadBean loadBean) {
            this.bean = loadBean;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void cancle() {
            isRunning = true;
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
                        if (isRunning) {
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
                        if (isRunning) {
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
    }

    /**
     * 下载
     *
     * @param loadBean object
     */
    public void download(Context context, DownLoadBean loadBean) {
        //TODO 先判断是否有这个app的下载信息,更新信息
        if (DataBaseUtil.getDownLoadById(context, loadBean.id) != null) {
            DataBaseUtil.UpdateDownLoadById(context, loadBean);
        } else {
            //TODO 插入数据库
            DataBaseUtil.insertDown(context, loadBean);
        }

        int state = loadBean.downloadState;
        switch (state) {
            case DownLoadState.STATE_NONE://默认
                downNone(loadBean);
                break;
            case DownLoadState.STATE_WAITING://等待中
                downWaiting(loadBean);
                break;
            case DownLoadState.STATE_PAUSED://暂停
                downPaused(loadBean);
                break;
            case DownLoadState.STATE_DOWNLOADING://下载中
                downLoading(loadBean);
                break;
            case DownLoadState.STATE_CONNECTION://连接中
                break;
            case DownLoadState.STATE_ERROR://下载失败
                downError(loadBean);
                break;
            case DownLoadState.STATE_DOWNLOADED://下载完毕
                Toast.makeText(BaseApplication.mContext, loadBean.appName + "->下载完毕", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
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

                DataBaseUtil.UpdateDownLoadById(context, bean);
                notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION);
                Log.i("Edwin", "连接成功--isSupportRange = " + bean.isSupportRange);

                //TODO 开始下载咯
                DownLoadTask downLoadTask = new DownLoadTask(bean);
                mTaskMap.put(bean.id, downLoadTask);
                downLoadExecutor.execute(downLoadTask);

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
            isRunning = false;
        }
    }


    private void downNone(DownLoadBean loadBean) {
        //TODO 最最最--->先判断任务数是否
        if (mTaskMap.size() >= DownLoadConfig.getConfig().getMax_download_tasks()) {
            mWaitingQueue.offer(loadBean);
            loadBean.downloadState = DownLoadState.STATE_WAITING;
            //TODO 更新数据库
            DataBaseUtil.UpdateDownLoadById(context, loadBean);
            //TODO 每次状态发生改变，都需要回调该方法通知所有观察者
            notifyDownloadStateChanged(loadBean, DownLoadState.STATE_WAITING);
        } else {
            if (loadBean.totalSize <= 0) {
                ConnectThread connectThread = new ConnectThread(loadBean);
                downLoadExecutor.execute(connectThread);
            } else {
                DownLoadTask downLoadTask = new DownLoadTask(loadBean);
                mTaskMap.put(loadBean.id, downLoadTask);
                downLoadExecutor.execute(downLoadTask);
            }

        }
    }

    /**
     * 等待状态
     */
    private void downWaiting(DownLoadBean loadBean) {
        //TODO 1.移出去队列
        mWaitingQueue.remove(loadBean);
        Log.e("Edwin", "mWaitingQueue size = " + mWaitingQueue.size());

        //TODO 2.TaskMap获取线程对象，移除线程;
        DownLoadTask downLoadTask = mTaskMap.get(loadBean.id);
        if (downLoadTask != null)
            downLoadTask.cancle();
        mTaskMap.remove(loadBean.id);

        //TODO 3.状态修改成STATE_PAUSED;
        loadBean.downloadState = DownLoadState.STATE_PAUSED;

        //TODO 4.更新数据库
        DataBaseUtil.UpdateDownLoadById(context, loadBean);

        //TODO 5.每次状态发生改变，都需要回调该方法通知所有观察者
        notifyDownloadStateChanged(loadBean, DownLoadState.STATE_PAUSED);
    }


    /**
     * 暂停状态
     */
    private void downPaused(DownLoadBean loadBean) {
        //TODO 1.状态修改成STATE_WAITING;
        loadBean.downloadState = DownLoadState.STATE_WAITING;
        downNone(loadBean);
    }


    /**
     * 下载状态
     */
    private void downLoading(DownLoadBean loadBean) {
        //TODO 1.TaskMap获取线程对象，移除线程;
        DownLoadTask downLoadTask = mTaskMap.get(loadBean.id);
        if (downLoadTask != null) {
            downLoadTask.cancle();
            mTaskMap.remove(loadBean.id);
        } else {
            mWaitingQueue.remove(loadBean);
        }
        //TODO 2.状态修改成STATE_PAUSED;
        loadBean.downloadState = DownLoadState.STATE_PAUSED;
        notifyDownloadStateChanged(loadBean, DownLoadState.STATE_PAUSED);
    }


    /**
     * 下载失败2
     */
    private void downError(DownLoadBean loadBean) {
        //TODO 1.删除本地文件文件
        Log.i("Edwin", "删除本地文件文件 Id = " + loadBean.id);
        //TODO 2.更新数据库数据库
        DataBaseUtil.UpdateDownLoadById(context, loadBean);

        loadBean.downloadState = DownLoadState.STATE_NONE;

//        /*********以下操作与默认状态一样*********/
//        //TODO 4.状态修改成STATE_WAITING；
//        //TODO 5.创建一个线程;
//        //TODO 6.放入TaskMap集合;
//        //TODO 7.启动执行线程execute
        download(context, loadBean);
    }

}
