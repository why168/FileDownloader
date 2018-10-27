package com.github.why168.multifiledownloader;

/**
 * @author Edwin.Wu
 * @version 2017/6/29 11:47
 * @since JDK1.8
 */
public interface ICallback {

    void onConnection();

    void onProgress();

    void onPaused();

    void onCompleted();

    void onError();

    void onRetry();
}
