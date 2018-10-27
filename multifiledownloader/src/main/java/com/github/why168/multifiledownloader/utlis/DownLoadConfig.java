package com.github.why168.multifiledownloader.utlis;

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
    private int maxTasks = 3;

    private DownLoadConfig() {

    }

    private final static class Instance {
        static final DownLoadConfig instance = new DownLoadConfig();
    }

    public static DownLoadConfig getConfig() {
        return DownLoadConfig.Instance.instance;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }
}
