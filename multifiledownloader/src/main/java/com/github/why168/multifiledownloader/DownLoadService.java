package com.github.why168.multifiledownloader;

import android.app.Service;
import android.content.Context;
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

import static com.github.why168.multifiledownloader.DownLoadState.STATE_DELETE;
import static com.github.why168.multifiledownloader.DownLoadState.STATE_DOWNLOADED;

public class DownLoadService extends Service {

    private final String TAG = DownLoadService.this.getClass().getName();
    private final static DownLoadExecutors downLoadExecutor = new DownLoadExecutors();
    private final static ConcurrentHashMap<String, AsyncConnectCall> connectionTaskMap = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, AsyncDownCall> downTaskMap = new ConcurrentHashMap<>();
    private final static LinkedBlockingDeque<DownLoadBean> mWaitingQueue = new LinkedBlockingDeque<>(); // 等待队列

    /**
     * 当下载状态发送改变的时候回调
     */
    private static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownLoadBean bean = (DownLoadBean) msg.obj;
            DownLoadObservable.getInstance().dataChange(bean);
        }
    };

    public DownLoadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DownLoadService --- onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        try {
//            if (intent.getAction() != null && Constants.action.equalsIgnoreCase(intent.getAction())) {
//                // 执行
//                DownLoadBean bean = (DownLoadBean) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
//                if (bean != null) {
//                    boolean booleanExtra = intent.getBooleanExtra(Constants.KEY_OPERATING_STATE, false);
//                    if (booleanExtra) {
//                        // 删除下载
//                        deleteTask(bean);
//                    } else {
//                        // 开始下载
//                        addTask(bean);
//                    }
//                }
//                return START_STICKY;
//            } else {
//                return super.onStartCommand(intent, flags, startId);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return super.onStartCommand(intent, flags, startId);
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    /**
     * 当下载状态发送改变的时候调用
     */
    private static void notifyDownloadStateChanged(Context context, DownLoadBean bean, int state) {
        Message message = handler.obtainMessage();
        message.obj = bean;
        message.what = state;
        handler.sendMessage(message);

        if (state == DownLoadState.STATE_ERROR.getIndex()
                || state == DownLoadState.STATE_DOWNLOADED.getIndex()
                || state == DownLoadState.STATE_DELETE.getIndex()
                || state == DownLoadState.STATE_PAUSED.getIndex()) {
            Log.d("Edwin", "notifyDownloadStateChanged---> " + bean.toString());
            downTaskMap.remove(bean.id);
            DownLoadBean poll = mWaitingQueue.poll();
            if (poll != null) {
                downNone(context, poll);
            }
        }
    }

    /**
     * 下载
     *
     * @param loadBean object
     */
    public static void addTask(Context context, DownLoadBean loadBean) {
        // 先判断是否有这个app的下载信息,更新信息
        if (DataBaseUtil.getDownLoadById(context, loadBean.id) != null) {
            DataBaseUtil.UpdateDownLoadById(context, loadBean);
        } else {
            // 插入数据库
            DataBaseUtil.insertDown(context, loadBean);
        }

        int state = loadBean.downloadState;
        if (state == DownLoadState.STATE_NONE.getIndex()) {
            //默认
            downNone(context, loadBean);
        } else if (state == DownLoadState.STATE_WAITING.getIndex()) {
            //等待中
            downWaiting(context, loadBean);
        } else if (state == DownLoadState.STATE_PAUSED.getIndex()) {
            //暂停
            downPaused(context, loadBean);
        } else if (state == DownLoadState.STATE_DOWNLOADING.getIndex()) {
            //下载中
            downLoading(context, loadBean);
        } else if (state == DownLoadState.STATE_CONNECTION.getIndex()) {
            //连接中
            downConning(loadBean);
        } else if (state == DownLoadState.STATE_ERROR.getIndex()) {
            //下载失败
            downError(context, loadBean);
        } else if (state == DownLoadState.STATE_DOWNLOADED.getIndex()) {
            //下载失败
            Log.d("Edwin", "----" + loadBean.appName + "->下载完毕");
        }
    }

    public static void deleteTask(Context context, DownLoadBean item) {
        // 删除文件，删除数据库
        try {
            mWaitingQueue.remove(item);
            AsyncDownCall downLoadTask = downTaskMap.get(item.id);
            if (downLoadTask != null) {
                downLoadTask.cancel();
                downTaskMap.remove(item.id);
            }
            item.downloadState = STATE_DELETE.getIndex();
            DataBaseUtil.DeleteDownLoadById(context, item.id);
            notifyDownloadStateChanged(context, item, DownLoadState.STATE_DELETE.getIndex());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downNone(Context context, DownLoadBean loadBean) {
        // 最最最--->先判断任务数是否
        if (downTaskMap.size() >= DownLoadConfig.getConfig().getMaxTasks()) {
            mWaitingQueue.offer(loadBean);
            loadBean.downloadState = DownLoadState.STATE_WAITING.getIndex();
            // 更新数据库
            DataBaseUtil.UpdateDownLoadById(context, loadBean);
            // 每次状态发生改变，都需要回调该方法通知所有观察者
            notifyDownloadStateChanged(context, loadBean, DownLoadState.STATE_WAITING.getIndex());
        } else {
            if (loadBean.totalSize <= 0) {
                AsyncConnectCall connectThread = new AsyncConnectCall(context, handler, connectionTaskMap, downTaskMap, downLoadExecutor, loadBean);
                downLoadExecutor.execute(connectThread);
            } else {
                AsyncDownCall downLoadTask = new AsyncDownCall(context, handler, loadBean);
                downTaskMap.put(loadBean.id, downLoadTask);
                downLoadExecutor.execute(downLoadTask);
            }
        }
    }

    /**
     * 等待状态
     */
    private static void downWaiting(Context context, DownLoadBean loadBean) {
        // 1.移出去队列
        mWaitingQueue.remove(loadBean);
        Log.d("Edwin", "mWaitingQueue size = " + mWaitingQueue.size());

        // 2.TaskMap获取线程对象，移除线程;
        AsyncDownCall downLoadTask = downTaskMap.get(loadBean.id);
        if (downLoadTask != null) {
            downLoadTask.cancel();
            downTaskMap.remove(loadBean.id);
        }

        // 3.状态修改成STATE_PAUSED;
        loadBean.downloadState = DownLoadState.STATE_PAUSED.getIndex();

        // 4.更新数据库
        DataBaseUtil.UpdateDownLoadById(context, loadBean);

        // 5.每次状态发生改变，都需要回调该方法通知所有观察者
        notifyDownloadStateChanged(context, loadBean, DownLoadState.STATE_PAUSED.getIndex());
    }

    /**
     * 暂停状态
     */
    private static void downPaused(Context context, DownLoadBean loadBean) {
        // 1.状态修改成STATE_WAITING;
        loadBean.downloadState = DownLoadState.STATE_WAITING.getIndex();
        downNone(context, loadBean);
    }


    /**
     * 下载状态
     */
    private static void downLoading(Context context, DownLoadBean loadBean) {
        // 1.TaskMap获取线程对象，移除线程;
        AsyncDownCall downLoadTask = downTaskMap.get(loadBean.id);
        if (downLoadTask != null) {
            downLoadTask.cancel();
            downTaskMap.remove(loadBean.id);
        } else {
            mWaitingQueue.remove(loadBean);
        }
        //TODO 执行下载队列中的任务
//        notifyDownloadStateChanged(c,);

        DownLoadBean poll = mWaitingQueue.poll();
        if (poll != null) {
            downNone(context, poll);
        }
    }

    /**
     * 连接中
     */
    private static void downConning(DownLoadBean loadBean) {
        AsyncConnectCall asyncConnectCall = connectionTaskMap.get(loadBean.id);
        if (asyncConnectCall != null) {
            asyncConnectCall.cancel();
            connectionTaskMap.remove(loadBean.id);
        }
    }

    /**
     * 下载失败
     */
    private static void downError(Context context, DownLoadBean loadBean) {
        // 1.删除本地文件文件
        Log.d("Edwin", "删除本地文件文件 Id = " + loadBean.id);
        // 2.更新数据库数据库
        DataBaseUtil.UpdateDownLoadById(context, loadBean);

        loadBean.downloadState = DownLoadState.STATE_NONE.getIndex();

//        /*********以下操作与默认状态一样*********/
//        // 4.状态修改成STATE_WAITING；
//        // 5.创建一个线程;
//        // 6.放入TaskMap集合;
//        // 7.启动执行线程execute
        addTask(context, loadBean);
    }

}
