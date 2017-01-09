package com.github.why168.filedownloader.pattern;

import com.github.why168.filedownloader.bean.DownLoadBean;

import java.util.Observable;

/**
 * 下载被观察者
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:25
 * @since JDK1.8
 */
public class DownLoadObservable extends Observable {
    private DownLoadBean data;

    private DownLoadObservable() {
    }

    private final static class Instance {
        static final DownLoadObservable instance = new DownLoadObservable();
    }

    public static DownLoadObservable getInstance() {
        return Instance.instance;
    }

    public void setData(DownLoadBean data) {
        this.data = data;
        dataChange(data);
    }

    private void dataChange(DownLoadBean data) {
        this.setChanged();
        this.notifyObservers(data);
    }
}
