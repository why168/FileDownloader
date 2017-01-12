package com.github.why168.filedownloader.notify;

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.DownLoadState;

import java.util.Observable;
import java.util.Observer;

/**
 * 观察者
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:39
 * @since JDK1.8
 */
public class DownFileObserver implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        DownLoadBean bean = (DownLoadBean) arg;
        String id = bean.id;
        int downloadState = bean.downloadState;
        switch (downloadState) {
            case DownLoadState.STATE_NONE:
                break;
            case DownLoadState.STATE_WAITING:
                onPrepare(bean);
                break;
            case DownLoadState.STATE_DOWNLOADING:
                onProgress(bean);
                break;
            case DownLoadState.STATE_PAUSED:
                onStop(bean);
                break;
            case DownLoadState.STATE_DOWNLOADED:
                onFinish(bean);
                break;
            case DownLoadState.STATE_ERROR:
                onError(bean);
                break;
        }
        //TODO 通过Id，

    }

    /**
     * 准备下载
     */
    void onPrepare(DownLoadBean bean) {
    }

    /**
     * 开始下载
     */
    void onStart(DownLoadBean bean) {
    }

    /**
     * 下载中
     */
    void onProgress(DownLoadBean bean) {
    }

    /**
     * 暂停
     */
    void onStop(DownLoadBean bean) {
    }

    /**
     * 下载完成
     */
    void onFinish(DownLoadBean bean) {
    }

    /**
     * 下载失败
     */
    void onError(DownLoadBean bean) {
    }

}
