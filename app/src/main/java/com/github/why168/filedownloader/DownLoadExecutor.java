package com.github.why168.filedownloader;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Edwin.Wu
 * @version 2016/12/28 15:20
 * @since JDK1.8
 */
public class DownLoadExecutor {
//        private static ThreadPoolExecutor mPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
//    private static ExecutorService mPool = Executors.newSingleThreadExecutor();
    private static ThreadPoolExecutor mPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

//    private static ThreadPoolExecutor mPool = new ThreadPoolExecutor(3, 3,
//            60L, TimeUnit.SECONDS,
//            new LinkedBlockingDeque<>());

    public static synchronized void execute(Runnable task) {
        mPool.execute(task);

//        Executors.newScheduledThreadPool(3);
//        Executors.newFixedThreadPool(3);
//        Executors.newWorkStealingPool();

    }


    /**
     * 取消线程池中某个还未执行的任务
     */
    public synchronized static boolean cancel(Runnable run) {
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
