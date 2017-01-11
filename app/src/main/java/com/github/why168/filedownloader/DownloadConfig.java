package com.github.why168.filedownloader;

/**
 * @author Edwin.Wu
 * @version 2017/1/10 11:43
 * @since JDK1.8
 */
public class DownloadConfig {
    private int max_download_tasks = 2;

    private DownloadConfig() {

    }

    private final static class Instance {
        static final DownloadConfig instance = new DownloadConfig();
    }

    public static DownloadConfig getConfig() {
        return DownloadConfig.Instance.instance;
    }

    public int getMax_download_tasks() {
        return max_download_tasks;
    }

    public void setMax_download_tasks(int max_download_tasks) {
        this.max_download_tasks = max_download_tasks;
    }
}
