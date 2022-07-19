package com.github.why168.multifiledownloader

import android.app.Service
import android.content.Context
import com.github.why168.multifiledownloader.notify.DownLoadObservable.dataChange
import com.github.why168.multifiledownloader.db.DataBaseUtil.getDownLoadById
import com.github.why168.multifiledownloader.db.DataBaseUtil.updateDownLoadById
import com.github.why168.multifiledownloader.db.DataBaseUtil.insertDown
import com.github.why168.multifiledownloader.db.DataBaseUtil.deleteDownLoadById
import com.github.why168.multifiledownloader.utlis.DownLoadConfig.getMaxTasks
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.github.why168.multifiledownloader.call.AsyncConnectCall
import com.github.why168.multifiledownloader.call.AsyncDownCall
import android.os.Looper
import android.os.Message
import android.util.Log
import java.lang.Exception
import java.lang.UnsupportedOperationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque

class DownLoadService : Service() {
    private val TAG = this@DownLoadService.javaClass.name

    companion object {
        private val downLoadExecutor = DownLoadExecutors()
        val connectionTaskMap = ConcurrentHashMap<String, AsyncConnectCall>()
        val downTaskMap = ConcurrentHashMap<String, AsyncDownCall>()
        val waitingQueue = LinkedBlockingDeque<DownLoadBean>()

        /**
         * 当下载状态发送改变的时候回调
         */
        private val handler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val bean = msg.obj as DownLoadBean
                dataChange(bean)
            }
        }

        /**
         * 当下载状态发送改变的时候调用
         */
        private fun notifyDownloadStateChanged(context: Context, bean: DownLoadBean, state: Int) {
            val message = handler.obtainMessage()
            message.obj = bean
            message.what = state
            handler.sendMessage(message)
            if (state == DownLoadState.STATE_ERROR.index || state == DownLoadState.STATE_DOWNLOADING.index || state == DownLoadState.STATE_DOWNLOADED.index || state == DownLoadState.STATE_DELETE.index || state == DownLoadState.STATE_PAUSED.index) {
                Log.d(
                    "Edwin",
                    String.format(
                        "notifyDownloadStateChanged---> id = %s , state = %d",
                        bean.id,
                        bean.downloadState
                    )
                )
                val downLoadTask = downTaskMap[bean.id]
                if (downLoadTask != null) {
                    downLoadTask.cancel()
                    downTaskMap.remove(bean.id)
                } else {
                    waitingQueue.remove(bean)
                }
                val poll = waitingQueue.poll()
                if (poll != null) {
                    downNone(context, poll)
                }
            }
        }

        /**
         * 下载
         *
         * @param loadBean object
         */
        fun addTask(context: Context, loadBean: DownLoadBean) {
            // 先判断是否有这个app的下载信息,更新信息
            if (getDownLoadById(context, loadBean.id) != null) {
                updateDownLoadById(context, loadBean)
            } else {
                // 插入数据库
                insertDown(context, loadBean)
            }
            val state = loadBean.downloadState
            if (state == DownLoadState.STATE_NONE.index) {
                //默认
                downNone(context, loadBean)
            } else if (state == DownLoadState.STATE_WAITING.index) {
                //等待中
                downWaiting(context, loadBean)
            } else if (state == DownLoadState.STATE_PAUSED.index) {
                //暂停
                downPaused(context, loadBean)
            } else if (state == DownLoadState.STATE_DOWNLOADING.index) {
                //下载中
                downLoading(context, loadBean)
            } else if (state == DownLoadState.STATE_CONNECTION.index) {
                //连接中
                downConning(loadBean)
            } else if (state == DownLoadState.STATE_ERROR.index) {
                //下载失败
                downError(context, loadBean)
            } else if (state == DownLoadState.STATE_DOWNLOADED.index) {
                //下载失败
                Log.d("Edwin", "----" + loadBean.appName + "->下载完毕")
            }
        }

        fun deleteTask(context: Context, item: DownLoadBean) {
            // 删除文件，删除数据库
            try {
                waitingQueue.remove(item)
                val downLoadTask = downTaskMap[item.id]
                if (downLoadTask != null) {
                    downLoadTask.cancel()
                    downTaskMap.remove(item.id)
                }
                item.downloadState = DownLoadState.STATE_DELETE.index
                deleteDownLoadById(context, item.id)
                notifyDownloadStateChanged(context, item, DownLoadState.STATE_DELETE.index)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun downNone(context: Context, loadBean: DownLoadBean) {
            // 最最最--->先判断任务数是否
            if (downTaskMap.size >= getMaxTasks()) {
                waitingQueue.offer(loadBean)
                loadBean.downloadState = DownLoadState.STATE_WAITING.index
                // 更新数据库
                updateDownLoadById(context, loadBean)
                // 每次状态发生改变，都需要回调该方法通知所有观察者
                notifyDownloadStateChanged(context, loadBean, DownLoadState.STATE_WAITING.index)
            } else {
                if (loadBean.totalSize <= 0) {
                    val connectThread = AsyncConnectCall(
                        context,
                        handler,
                        connectionTaskMap,
                        downTaskMap,
                        downLoadExecutor,
                        loadBean
                    )
                    downLoadExecutor.execute(connectThread)
                } else {
                    val downLoadTask = AsyncDownCall(context, handler, loadBean)
                    downTaskMap[loadBean.id] = downLoadTask
                    downLoadExecutor.execute(downLoadTask)
                }
            }
        }

        /**
         * 等待状态
         */
        private fun downWaiting(context: Context, loadBean: DownLoadBean) {
            // 1.移出去队列
            waitingQueue.remove(loadBean)
            Log.d("Edwin", "waitingQueue size = " + waitingQueue.size)

            // 2.TaskMap获取线程对象，移除线程;
            val downLoadTask = downTaskMap[loadBean.id]
            if (downLoadTask != null) {
                downLoadTask.cancel()
                downTaskMap.remove(loadBean.id)
            }

            // 3.状态修改成STATE_PAUSED;
            loadBean.downloadState = DownLoadState.STATE_PAUSED.index

            // 4.更新数据库
            updateDownLoadById(context, loadBean)

            // 5.每次状态发生改变，都需要回调该方法通知所有观察者
            notifyDownloadStateChanged(context, loadBean, DownLoadState.STATE_PAUSED.index)
        }

        /**
         * 暂停状态
         */
        private fun downPaused(context: Context, loadBean: DownLoadBean) {
            // 1.状态修改成STATE_WAITING;
            loadBean.downloadState = DownLoadState.STATE_WAITING.index
            downNone(context, loadBean)
        }

        /**
         * 下载中
         */
        private fun downLoading(context: Context, loadBean: DownLoadBean) {
            notifyDownloadStateChanged(context, loadBean, DownLoadState.STATE_DOWNLOADING.index)
        }

        /**
         * 连接中
         */
        private fun downConning(loadBean: DownLoadBean) {
            val asyncConnectCall = connectionTaskMap[loadBean.id]
            if (asyncConnectCall != null) {
                asyncConnectCall.cancel()
                connectionTaskMap.remove(loadBean.id)
            }
        }

        /**
         * 下载失败
         */
        private fun downError(context: Context, loadBean: DownLoadBean) {
            // 1.删除本地文件文件
            Log.d("Edwin", "删除本地文件文件 Id = " + loadBean.id)
            // 2.更新数据库数据库
            updateDownLoadById(context, loadBean)
            loadBean.downloadState = DownLoadState.STATE_NONE.index

//        /*********以下操作与默认状态一样*********/
//        // 4.状态修改成STATE_WAITING；
//        // 5.创建一个线程;
//        // 6.放入TaskMap集合;
//        // 7.启动执行线程execute
            addTask(context, loadBean)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DownLoadService --- onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}