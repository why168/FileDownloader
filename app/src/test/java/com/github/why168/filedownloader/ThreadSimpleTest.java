package com.github.why168.filedownloader;

import org.junit.Test;

import java.util.Vector;

/**
 * @author Edwin.Wu
 * @version 2017/6/29 15:01
 * @since JDK1.8
 */
public class ThreadSimpleTest {
    @Test
    public void t1() {
        Runnable runnable = new Runnable() {
            Count count = new Count();

            public void run() {
                count.count();
            }
        };
        for (int i = 0; i < 10; i++) {
            new Thread(runnable).start();
        }
    }

    public class Count {
        private int num = 0;
        public void count() {
            for (int i = 1; i <= 10; i++) {
                num += i;
            }
            System.out.println(Thread.currentThread().getName() + "-" + num);
        }
    }
}
