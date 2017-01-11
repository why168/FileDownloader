package com.github.why168.filedownloader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

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
import java.util.concurrent.LinkedBlockingDeque;

import static com.github.why168.filedownloader.constant.DownLoadState.STATE_DELETE;
import static com.github.why168.filedownloader.constant.DownLoadState.STATE_DOWNLOADED;


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

    private ConcurrentHashMap<String, DownLoadTask2> mTaskMap2 = new ConcurrentHashMap<String, DownLoadTask2>();

    /**
     * 当下载状态发送改变的时候回调
     */
//    private ExecuteHandler handler = new ExecuteHandler();

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
                    mTaskMap2.remove(bean.id);
                    DownLoadBean poll = mWaitingQueue.poll();
                    if (poll != null) {
                        downNone2(poll);
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
            case STATE_DOWNLOADED://下载完毕
                downLoaded();
                break;
            case DownLoadState.STATE_ERROR://下载失败
                downError(loadBean);
                break;
            default:
                break;
        }
    }

    void deleteDownTask(DownLoadBean item) {
        //TODO 删除文件，删除数据库
        try {
            item.downloadState = DownLoadState.STATE_DELETE;
            DownLoadTask remove = mTaskMap.remove(item.id);
            if (remove != null)
                remove.cancel();
            DataBaseUtil.DeleteDownLoadById(item.id);
            notifyDownloadStateChanged(item, DownLoadState.STATE_DELETE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void deleteDownTask2(DownLoadBean item) {
        //TODO 删除文件，删除数据库
        try {
            DownLoadTask2 remove = mTaskMap2.remove(item.id);
            if (remove != null) {
                remove.cancle();
            } else {
                mWaitingQueue.remove(item);
            }
            item.downloadState = STATE_DELETE;
            DataBaseUtil.DeleteDownLoadById(item.id);
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
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
                case DownLoadState.STATE_DOWNLOADING:
                    Log.v("Edwin", "下载完毕");
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
                        notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING);
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
                        notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING);
                    }
                    fos.close();
                    is.close();
                } else {
                    bean.downloadState = DownLoadState.STATE_ERROR;
                    DataBaseUtil.UpdateDownLoadById(bean);
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                bean.downloadState = DownLoadState.STATE_ERROR;
                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
            }

            //TODO 判断是否下载完成
            if (bean.currentSize == bean.totalSize) {
                bean.downloadState = STATE_DOWNLOADED;
                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean, STATE_DOWNLOADED);
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
                notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION);
                Log.i("Edwin", "连接成功--isSupportRange = " + bean.isSupportRange);
                //TODO 开始下载咯
                startDownload(bean);
            } catch (IOException e) {
                isRunning = false;
                e.printStackTrace();
                bean.downloadState = DownLoadState.STATE_ERROR;
                DataBaseUtil.UpdateDownLoadById(bean);
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
        Log.i("Edwin", "删除本地文件文件 Id = " + loadBean.id);
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
        Log.i("Edwin", "安装应用");
    }


    public void download(DownLoadBean loadBean) {
        //TODO 最最最--->先判断任务数是否
        if (mTaskMap.size() >= DownloadConfig.getConfig().getMax_download_tasks()) {
            mWaitingQueue.offer(loadBean);
            loadBean.downloadState = DownLoadState.STATE_WAITING;
            //TODO 更新数据库
            DataBaseUtil.UpdateDownLoadById(loadBean);
            //TODO 每次状态发生改变，都需要回调该方法通知所有观察者
            notifyDownloadStateChanged(loadBean, DownLoadState.STATE_WAITING);
        } else {
            if (loadBean.totalSize == 0) {
                ConnectThread connectThread = new ConnectThread(loadBean);
                DownLoadExecutor.execute(connectThread);
            } else {
                //TODO 先判断是否有这个app的下载信息,更新信息
                if (DataBaseUtil.getDownLoadById(loadBean.id) != null) {
                    DataBaseUtil.UpdateDownLoadById(loadBean);
                } else {
                    //TODO 插入数据库
                    DataBaseUtil.insertDown(loadBean);
                }
                startDownload(loadBean);
            }
        }
    }

    private class DownLoadTask2 implements Runnable {
        private DownLoadBean bean;
        private volatile boolean isRunning = false;

        DownLoadTask2(DownLoadBean loadBean) {
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
            File destFile = DataBaseUtil.getDownloadFile(bean.url);
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
                        notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING);
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
                        notifyDownloadStateChanged(bean, DownLoadState.STATE_DOWNLOADING);
                    }
                    fos.close();
                    is.close();
                } else {
                    bean.downloadState = DownLoadState.STATE_ERROR;
                    DataBaseUtil.UpdateDownLoadById(bean);
                    notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                bean.downloadState = DownLoadState.STATE_ERROR;
                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean, DownLoadState.STATE_ERROR);
            }

            //TODO 判断是否下载完成
            if (bean.currentSize == bean.totalSize) {
                bean.downloadState = STATE_DOWNLOADED;
                DataBaseUtil.UpdateDownLoadById(bean);
                notifyDownloadStateChanged(bean, STATE_DOWNLOADED);
            }
        }
    }

    /**
     * 下载第二种
     *
     * @param loadBean
     */
    void download2(DownLoadBean loadBean) {
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
                downNone2(loadBean);
                break;
            case DownLoadState.STATE_WAITING://等待中
                downWaiting2(loadBean);
                break;
            case DownLoadState.STATE_PAUSED://暂停
                downPaused2(loadBean);
                break;
            case DownLoadState.STATE_DOWNLOADING://下载中
                downLoading2(loadBean);
                break;
            case DownLoadState.STATE_CONNECTION://连接中
                break;
            case DownLoadState.STATE_ERROR://下载失败
                downError2(loadBean);
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
    private class ConnectThread2 implements Runnable {
        private DownLoadBean bean;
        private volatile boolean isRunning;

        public ConnectThread2(DownLoadBean bean) {
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
                notifyDownloadStateChanged(bean, DownLoadState.STATE_CONNECTION);
                Log.i("Edwin", "连接成功--isSupportRange = " + bean.isSupportRange);

                //TODO 开始下载咯
                DownLoadTask2 downLoadTask2 = new DownLoadTask2(bean);
                mTaskMap2.put(bean.id, downLoadTask2);
                DownLoadExecutor.execute(downLoadTask2);

            } catch (IOException e) {
                isRunning = false;
                e.printStackTrace();
                bean.downloadState = DownLoadState.STATE_ERROR;
                DataBaseUtil.UpdateDownLoadById(bean);
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


    private void downNone2(DownLoadBean loadBean) {
        //TODO 最最最--->先判断任务数是否
        if (mTaskMap2.size() >= DownloadConfig.getConfig().getMax_download_tasks()) {
            mWaitingQueue.offer(loadBean);
            loadBean.downloadState = DownLoadState.STATE_WAITING;
            //TODO 更新数据库
            DataBaseUtil.UpdateDownLoadById(loadBean);
            //TODO 每次状态发生改变，都需要回调该方法通知所有观察者
            notifyDownloadStateChanged(loadBean, DownLoadState.STATE_WAITING);
        } else {
            if (loadBean.totalSize <= 0) {
                ConnectThread2 connectThread = new ConnectThread2(loadBean);
                DownLoadExecutor.execute(connectThread);
            } else {
                DownLoadTask2 downLoadTask2 = new DownLoadTask2(loadBean);
                mTaskMap2.put(loadBean.id, downLoadTask2);
                DownLoadExecutor.execute(downLoadTask2);
            }

        }
    }

    /**
     * 等待状态
     */
    private void downWaiting2(DownLoadBean loadBean) {
        //TODO 1.移出去队列
        mWaitingQueue.remove(loadBean);
        Log.e("Edwin", "mWaitingQueue size = " + mWaitingQueue.size());

        //TODO 2.TaskMap获取线程对象，移除线程;
        DownLoadTask2 downLoadTask2 = mTaskMap2.get(loadBean.id);
        if (downLoadTask2 != null)
            downLoadTask2.cancle();
        mTaskMap.remove(loadBean.id);

        //TODO 3.状态修改成STATE_PAUSED;
        loadBean.downloadState = DownLoadState.STATE_PAUSED;

        //TODO 4.更新数据库
        DataBaseUtil.UpdateDownLoadById(loadBean);

        //TODO 5.每次状态发生改变，都需要回调该方法通知所有观察者
        notifyDownloadStateChanged(loadBean, DownLoadState.STATE_PAUSED);
    }


    /**
     * 暂停状态
     */
    private void downPaused2(DownLoadBean loadBean) {
        //TODO 1.状态修改成STATE_WAITING;
        loadBean.downloadState = DownLoadState.STATE_WAITING;
        downNone2(loadBean);
    }


    /**
     * 下载状态
     */
    private void downLoading2(DownLoadBean loadBean) {
        //TODO 1.TaskMap获取线程对象，移除线程;
        DownLoadTask2 downLoadTask = mTaskMap2.get(loadBean.id);
        if (downLoadTask != null) {
            downLoadTask.cancle();
            mTaskMap2.remove(loadBean.id);
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
    private void downError2(DownLoadBean loadBean) {
        //TODO 1.删除本地文件文件
        Log.i("Edwin", "删除本地文件文件 Id = " + loadBean.id);
        //TODO 2.更新数据库数据库
        DataBaseUtil.UpdateDownLoadById(loadBean);

        loadBean.downloadState = DownLoadState.STATE_NONE;

//        /*********以下操作与默认状态一样*********/
//        //TODO 4.状态修改成STATE_WAITING；
//        loadBean.downloadState = DownLoadState.STATE_WAITING;
//        //TODO 5.创建一个线程;
//        DownLoadTask downLoadTask = new DownLoadTask(loadBean);
//        //TODO 6.放入TaskMap集合;
//        mTaskMap.put(loadBean.id, downLoadTask);
//        //TODO 7.启动执行线程execute
//        DownLoadExecutor.execute(downLoadTask);
        download2(loadBean);
    }

}
