package com.github.why168.multifiledownloader;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.github.why168.multifiledownloader.db.DataBaseUtil;
import com.github.why168.multifiledownloader.notify.DownLoadObservable;
import com.github.why168.multifiledownloader.utlis.DownLoadConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.why168.multifiledownloader.DownLoadState.STATE_DELETE;
import static com.github.why168.multifiledownloader.DownLoadState.STATE_DOWNLOADED;

public class DownLoadService extends Service {
    private String TAG = DownLoadService.this.getClass().getName();
    private DownLoadExecutors downLoadExecutors;
    private ThreadPoolExecutor downLoadExecutor;
    private ConcurrentHashMap<String, AsyncDownCall> mTaskMap;
    private LinkedBlockingDeque<DownLoadBean> mWaitingQueue;

    public DownLoadService() {
        Log.e(TAG, "DownLoadService");
        downLoadExecutors = new DownLoadExecutors();
        downLoadExecutor = new DownLoadExecutors().executorService();
        mTaskMap = new ConcurrentHashMap<>();
        mWaitingQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "DownLoadService --- onCreate");
    }

    /**
     * 当下载状态发送改变的时候回调
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownLoadBean bean = (DownLoadBean) msg.obj;
            int what = msg.what;
            switch (what) {
                case DownLoadState.STATE_ERROR:
                case STATE_DOWNLOADED:
                case STATE_DELETE:
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

    /**
     * 当下载状态发送改变的时候调用
     */
    private void notifyDownloadStateChanged(DownLoadBean bean, int state) {
        Message message = handler.obtainMessage();
        message.obj = bean;
        message.what = state;
        handler.sendMessage(message);
    }

    /**
     * 下载
     *
     * @param loadBean object
     */
    public void download(DownLoadBean loadBean) {
        //TODO 先判断是否有这个app的下载信息,更新信息
        if (DataBaseUtil.getDownLoadById(getApplicationContext(), loadBean.id) != null) {
            DataBaseUtil.UpdateDownLoadById(getApplicationContext(), loadBean);
        } else {
            //TODO 插入数据库
            DataBaseUtil.insertDown(getApplicationContext(), loadBean);
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
                Log.e("Edwin", "----" + loadBean.appName + "->下载完毕");
                break;
            default:
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        try {
            if (intent.getAction() != null && "com.github.why168.multifiledownloader.downloadservice".equalsIgnoreCase(intent.getAction())) {
                //TODO 执行
                DownLoadBean bean = (DownLoadBean) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
                if (bean != null) {
                    boolean booleanExtra = intent.getBooleanExtra(Constants.KEY_OPERATING_STATE, false);
                    if (booleanExtra) {
                        //TODO 删除下载
                        deleteDownTask(bean);
                    } else {
                        //TODO 开始下载
                        download(bean);
                    }
                }
                return START_STICKY;
            } else {
                return super.onStartCommand(intent, flags, startId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return super.onStartCommand(intent, flags, startId);
        }
    }

    public void deleteDownTask(DownLoadBean item) {
        //TODO 删除文件，删除数据库
        try {
            AsyncDownCall remove = mTaskMap.remove(item.id);
            if (remove != null) {
                remove.cancel();
            } else {
                mWaitingQueue.remove(item);
            }
            item.downloadState = STATE_DELETE;
            DataBaseUtil.DeleteDownLoadById(getApplicationContext(), item.id);
            notifyDownloadStateChanged(item, STATE_DELETE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downNone(DownLoadBean loadBean) {
        //TODO 最最最--->先判断任务数是否
        if (mTaskMap.size() >= DownLoadConfig.getConfig().getMaxTasks()) {
            mWaitingQueue.offer(loadBean);
            loadBean.downloadState = DownLoadState.STATE_WAITING;
            //TODO 更新数据库
            DataBaseUtil.UpdateDownLoadById(this, loadBean);
            //TODO 每次状态发生改变，都需要回调该方法通知所有观察者
            notifyDownloadStateChanged(loadBean, DownLoadState.STATE_WAITING);
        } else {
            if (loadBean.totalSize <= 0) {
                AsyncConnectCall connectThread = new AsyncConnectCall(this, handler, mTaskMap, downLoadExecutor, loadBean);
                downLoadExecutor.execute(connectThread);
            } else {
                AsyncDownCall downLoadTask = new AsyncDownCall(this, handler, loadBean);
                mTaskMap.put(loadBean.id, downLoadTask);
                downLoadExecutors.execute(downLoadTask);
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
        AsyncDownCall downLoadTask = mTaskMap.get(loadBean.id);
        if (downLoadTask != null)
            downLoadTask.cancel();
        mTaskMap.remove(loadBean.id);

        //TODO 3.状态修改成STATE_PAUSED;
        loadBean.downloadState = DownLoadState.STATE_PAUSED;

        //TODO 4.更新数据库
        DataBaseUtil.UpdateDownLoadById(getApplicationContext(), loadBean);

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
        AsyncDownCall downLoadTask = mTaskMap.get(loadBean.id);
        if (downLoadTask != null) {
            downLoadTask.cancel();
            mTaskMap.remove(loadBean.id);
        } else {
            mWaitingQueue.remove(loadBean);
        }
    }

    /**
     * 下载失败
     */
    private void downError(DownLoadBean loadBean) {
        //TODO 1.删除本地文件文件
        Log.i("Edwin", "删除本地文件文件 Id = " + loadBean.id);
        //TODO 2.更新数据库数据库
        DataBaseUtil.UpdateDownLoadById(getApplicationContext(), loadBean);

        loadBean.downloadState = DownLoadState.STATE_NONE;

//        /*********以下操作与默认状态一样*********/
//        //TODO 4.状态修改成STATE_WAITING；
//        //TODO 5.创建一个线程;
//        //TODO 6.放入TaskMap集合;
//        //TODO 7.启动执行线程execute
        download(loadBean);
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
