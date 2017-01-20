package com.github.why168.filedownloader.utlis;

/**
 * 下载配置
 *
 * @author Edwin.Wu
 * @version 2017/1/10 11:43
 * @since JDK1.8
 */
public class DownLoadConfig {
    /**
     * 下载的任务数
     */
    private int max_download_tasks = 3;

    private DownLoadConfig() {

    }

    private final static class Instance {
        static final DownLoadConfig instance = new DownLoadConfig();
    }

    public static DownLoadConfig getConfig() {
        return DownLoadConfig.Instance.instance;
    }

    public int getMax_download_tasks() {
        return max_download_tasks;
    }

    public void setMax_download_tasks(int max_download_tasks) {
        this.max_download_tasks = max_download_tasks;
    }
}
