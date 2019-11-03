package com.github.why168.multifiledownloader.notify;

import android.util.Log;

import com.github.why168.multifiledownloader.DownLoadBean;

import java.util.Observable;

/**
 * 下载被观察者
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:25
 * @since JDK1.8
 */
public class DownLoadObservable extends Observable {

    private DownLoadObservable() {
    }

    private final static class Instance {
        static final DownLoadObservable instance = new DownLoadObservable();
    }

    public static DownLoadObservable getInstance() {
        return Instance.instance;
    }

    public void dataChange(DownLoadBean data) {
        Log.d("Edwin", "DownLoadObservable dataChange " + data.downloadState + " , currentSize = " + data.currentSize);
        this.setChanged();
        this.notifyObservers(data);
    }

}
