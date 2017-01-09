package com.github.why168.filedownloader;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Edwin.Wu
 * @version 2016/12/28 15:20
 * @since JDK1.8
 */
public class DownLoadExecutor {
//        private static ThreadPoolExecutor mPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
//    private static ExecutorService mPool = Executors.newSingleThreadExecutor();
    private static ThreadPoolExecutor mPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public static synchronized void execute(Runnable task) {
        mPool.execute(task);
    }


    /**
     * 取消线程池中某个还未执行的任务
     */
    public synchronized static boolean cancel(Runnable run) {
        boolean remove = mPool.getQueue().remove(run);
        boolean remove1 = mPool.remove(run);

//        if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
//            return mPool.getQueue().remove(run) || mPool.remove(run);
//        } else {
//            return false;
//        }

        return true;
    }
//
//    /**
//     * 查看线程池中是否还有某个还未执行的任务
//     */
//    public synchronized static boolean contains(Runnable run) {
//        if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
//            return mPool.getQueue().contains(run);
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * 立刻关闭线程池，并且正在执行的任务也将会被中断
//     */
//    public static void stop() {
//        if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
//            mPool.shutdownNow();
//        }
//    }

}
