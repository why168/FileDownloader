package com.github.why168.filedownloader.bean;

import com.github.why168.filedownloader.constant.DownLoadState;

/**
 * 下载任务实体类
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:26
 * @since JDK1.8
 */
public class DownLoadBean {
    public String id;//app的id url-md5
    public String appName;//app的软件名称
    public String appIcon;//app的图片
    public long totalSize;//app的size
    public long currentSize = 0;//当前的size
    public int downloadState = DownLoadState.STATE_NONE;//下载的状态
    public String url;//下载地址
    public String path;//保存路径
    public boolean isSupportRange = false;//是否支持断点下载

    @Override
    public String toString() {
        return "appName='" + appName +
                ", isSupportRange=" + isSupportRange +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", downloadState=" + downloadState +
                '}';
    }
}
