package com.github.why168.filedownloader

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import com.github.why168.multifiledownloader.DownLoadBean
import com.github.why168.multifiledownloader.DownLoadState
import com.github.why168.multifiledownloader.DownloadManager
import com.github.why168.multifiledownloader.db.DataBaseUtil
import com.github.why168.multifiledownloader.notify.DownLoadObservable
import com.github.why168.multifiledownloader.utlis.FileUtilities
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_rec_view.*
import kotlinx.android.synthetic.main.item_down.*

import java.io.File
import java.util.ArrayList
import java.util.Observable
import java.util.Observer

/**
 * RecViewActivity
 *
 * @author Edwin.Wu
 * @version 2017/6/28 15:34
 * @since JDK1.8
 */
class RecViewActivity : AppCompatActivity(), Observer {

    private val collections = ArrayList<DownLoadBean>()
    private var viewAdapter: RecyclerView.Adapter<ViewHolder>? = null
    private var mDownloadManager: DownloadManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rec_view)

        initData()

        recView.layoutManager = LinearLayoutManager(this)
        viewAdapter = ViewAdapter()
        recView.adapter = viewAdapter
    }

    private fun initData() {
        mDownloadManager = DownloadManager.getInstance(this)

        DataUtils.mockData(collections)

        val downLoad = DataBaseUtil.getDownLoad(this)
        for (i in downLoad.indices) {
            val beanI = downLoad[i]
            for (j in collections.indices) {
                val beanJ = collections[j]
                if (beanI.id == beanJ.id) {
                    collections[j] = beanI
                    break
                }
            }
        }

    }

    override fun update(o: Observable, arg: Any) {
        if (o !is DownLoadObservable) {
            return
        }

        val bean = arg as DownLoadBean
        val index = collections.indexOf(bean)
        Log.d("Edwin", "index = $index bean = $bean")
        val downloadState = bean.downloadState

        if (index != -1 && isCurrentListViewItemVisible(index)) {
            if (downloadState == DownLoadState.STATE_DELETE) {
                viewAdapter!!.notifyItemRemoved(index)
                collections.removeAt(index)
                if (index != collections.size) {
                    notifyChange(bean, index)
                }
                try {
                    val file = File(bean.path)
                    val delete = file.delete()
                    Log.d("Edwin", "删除 state = $delete")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                collections[index] = bean
                notifyChange(bean, index)
            }
        }
    }


    /**
     * 数据改变
     *
     * @param bean
     * @param index
     */
    @SuppressLint("SetTextI18n")
    private fun notifyChange(bean: DownLoadBean, index: Int) {
        val holder = getViewHolder(index)

        when (bean.downloadState) {
            DownLoadState.STATE_NONE -> holder.button_start.text = "点击下载"
            // 等待下载 改成 排队下载
            DownLoadState.STATE_WAITING -> holder.button_start.text = "排队下载"
            // 下载中 改成 正在下载
            DownLoadState.STATE_DOWNLOADING -> holder.button_start.text = "正在下载"
            // 暂停下载 换成 继续下载
            DownLoadState.STATE_PAUSED -> holder.button_start.text = "继续下载"
            DownLoadState.STATE_DOWNLOADED -> holder.button_start.text = "下载完毕"
            DownLoadState.STATE_ERROR -> holder.button_start.text = "下载错误"
            DownLoadState.STATE_CONNECTION -> holder.button_start.text = "连接中"
        }

        holder.button_delete.setOnClickListener {
            mDownloadManager?.delete(bean)
        }

        holder.button_start.setOnClickListener {
            mDownloadManager?.down(bean)
        }

        holder.text_name.text = bean.appName
        holder.text_range.text = bean.isSupportRange.toString()
        holder.text_progress.text =
            FileUtilities.convertFileSize(bean.currentSize) + "/" + FileUtilities.convertFileSize(
                bean.totalSize
            )
        holder.progressBar.max = bean.totalSize.toInt()
        holder.progressBar.progress = bean.currentSize.toInt()
    }

    override fun onStart() {
        super.onStart()
        DownLoadObservable.getInstance().addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        DownLoadObservable.getInstance().deleteObserver(this)
    }

    private inner class ViewAdapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_down, parent, false))
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bean = collections[position]
            holder.text_name.text = bean.appName

            when (bean.downloadState) {
                DownLoadState.STATE_NONE -> holder.button_start.text = "点击下载"
                DownLoadState.STATE_WAITING -> holder.button_start.text = "等待下载"
                // 下载中 改成 正在下载
                DownLoadState.STATE_DOWNLOADING -> holder.button_start.text = "正在下载"
                // 暂停下载 换成 继续下载
                DownLoadState.STATE_PAUSED -> holder.button_start.text = "继续下载"
                DownLoadState.STATE_DOWNLOADED -> holder.button_start.text = "下载完毕"
                DownLoadState.STATE_ERROR -> holder.button_start.text = "下载错误"
                DownLoadState.STATE_CONNECTION -> holder.button_start.text = "连接中"
            }

            holder.button_delete.setOnClickListener {
                mDownloadManager?.delete(bean)
            }

            holder.button_start.setOnClickListener {
                mDownloadManager?.down(bean)
            }

            holder.text_range.text = bean.isSupportRange.toString()
            holder.text_progress.text =
                FileUtilities.convertFileSize(bean.currentSize) + "/" + FileUtilities.convertFileSize(
                    bean.totalSize
                )
            holder.progressBar.max = bean.totalSize.toInt()
            holder.progressBar.progress = bean.currentSize.toInt()
        }

        override fun getItemCount(): Int {
            return collections.size
        }
    }

    internal class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

//        var text_name: TextView = itemView.findViewById(R.id.text_name)
//        var button_start: Button = itemView.findViewById(R.id.button_start)
//        var button_delete: Button = itemView.findViewById(R.id.button_delete)
//        var text_progress: TextView = itemView.findViewById(R.id.text_progress)
//        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
//        var text_range: TextView = itemView.findViewById(R.id.text_range)
    }

    private fun getViewHolder(position: Int): ViewHolder {
        return recView.findViewHolderForLayoutPosition(position) as ViewHolder
    }

    private fun isCurrentListViewItemVisible(position: Int): Boolean {
        val layoutManager = recView.layoutManager as LinearLayoutManager?
        val first = layoutManager!!.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        return position in first..last
//        return first <= position && position <= last
    }
}
