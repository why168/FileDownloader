package com.github.why168.filedownloader

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
            if (downloadState == DownLoadState.STATE_DELETE.index) {
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
            DownLoadState.STATE_NONE.index -> {
                holder.button_start.text = DownLoadState.STATE_NONE.content
            }
            DownLoadState.STATE_WAITING.index -> {
                holder.button_start.text = DownLoadState.STATE_WAITING.content
            }
            DownLoadState.STATE_DOWNLOADING.index -> {
                holder.button_start.text = DownLoadState.STATE_DOWNLOADING.content
            }
            DownLoadState.STATE_PAUSED.index -> {
                holder.button_start.text = DownLoadState.STATE_PAUSED.content
            }
            DownLoadState.STATE_DOWNLOADED.index -> {
                holder.button_start.text = DownLoadState.STATE_DOWNLOADED.content
            }
            DownLoadState.STATE_ERROR.index -> {
                holder.button_start.text = DownLoadState.STATE_ERROR.content
            }
            DownLoadState.STATE_CONNECTION.index -> {
                holder.button_start.text = DownLoadState.STATE_CONNECTION.content
            }
        }

        holder.button_delete.setOnClickListener {
            mDownloadManager?.deleteTask(bean)
        }

        holder.button_start.setOnClickListener {
            mDownloadManager?.addTask(bean)
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

    @SuppressLint("SetTextI18n")
    private inner class ViewAdapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_down,
                    parent,
                    false
                )
            )
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val bean = collections[position]
            holder.text_name.text = bean.appName

            when (bean.downloadState) {
                DownLoadState.STATE_NONE.index -> {
                    holder.button_start.text = DownLoadState.STATE_NONE.content
                }
                DownLoadState.STATE_WAITING.index -> {
                    holder.button_start.text = DownLoadState.STATE_WAITING.content
                }
                DownLoadState.STATE_DOWNLOADING.index -> {
                    holder.button_start.text = DownLoadState.STATE_DOWNLOADING.content
                }
                DownLoadState.STATE_PAUSED.index -> {
                    holder.button_start.text = DownLoadState.STATE_PAUSED.content
                }
                DownLoadState.STATE_DOWNLOADED.index -> {
                    holder.button_start.text = DownLoadState.STATE_DOWNLOADED.content
                }
                DownLoadState.STATE_ERROR.index -> {
                    holder.button_start.text = DownLoadState.STATE_ERROR.content
                }
                DownLoadState.STATE_CONNECTION.index -> {
                    holder.button_start.text = DownLoadState.STATE_CONNECTION.content
                }
            }

            holder.button_delete.setOnClickListener {
                mDownloadManager?.deleteTask(bean)
            }

            holder.button_start.setOnClickListener {
                mDownloadManager?.addTask(bean)
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
        RecyclerView.ViewHolder(containerView), LayoutContainer

    private fun getViewHolder(position: Int): ViewHolder {
        return recView.findViewHolderForLayoutPosition(position) as ViewHolder
    }

    private fun isCurrentListViewItemVisible(position: Int): Boolean {
        val layoutManager = recView.layoutManager as LinearLayoutManager?
        val first = layoutManager!!.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        return position in first..last
    }
}
