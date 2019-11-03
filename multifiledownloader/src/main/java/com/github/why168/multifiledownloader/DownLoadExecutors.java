package com.github.why168.multifiledownloader;

import android.support.annotation.NonNull;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Edwin.Wu
 * @version 2017/6/28 23:46
 * @since JDK1.8
 */
public class DownLoadExecutors extends ThreadPoolExecutor {

    DownLoadExecutors() {
        super(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory("downLoad executors"));
        allowCoreThreadTimeOut(true);
    }

    private static ThreadFactory threadFactory(final String name) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(false);
                return result;
            }
        };
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }
}
