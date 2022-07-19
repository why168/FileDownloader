package com.github.why168.multifiledownloader

import java.io.Serializable

/**
 * 下载任务实体类
 *
 * @author Edwin.Wu
 * @version 2016/12/28 11:26
 * @since JDK1.8
 */
data class DownLoadBean(
    @JvmField
    var id: String = "", //app的id url-md5
    @JvmField
    var appName: String = "", //app的软件名称
    @JvmField
    var appIcon: String = "", //app的图片
    @JvmField
    var totalSize: Long = 0, //app的size
    @JvmField
    var currentSize: Long = 0, //当前的size
    @JvmField
    var downloadState: Int = DownLoadState.STATE_NONE.index, //下载的状态
    @JvmField
    var url: String = "", //下载地址
    @JvmField
    var path: String = "", //保存路径
    @JvmField
    var isSupportRange: Boolean = false //是否支持断点下载
) : Serializable, Cloneable {

    override fun clone(): DownLoadBean {
        return super.clone() as DownLoadBean
    }
}