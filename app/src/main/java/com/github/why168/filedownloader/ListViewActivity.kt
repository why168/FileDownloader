package com.github.why168.filedownloader

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import com.github.why168.multifiledownloader.DownLoadBean
import com.github.why168.multifiledownloader.DownLoadState
import com.github.why168.multifiledownloader.DownloadManager
import com.github.why168.multifiledownloader.db.DataBaseUtil
import com.github.why168.multifiledownloader.notify.DownLoadObservable
import com.github.why168.multifiledownloader.utlis.FileUtilities
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_list_view.*
import kotlinx.android.synthetic.main.item_down.*

import java.io.File
import java.util.ArrayList
import java.util.Observable
import java.util.Observer

/**
 * ListViewActivity
 *
 * @author Edwin.Wu
 * @version 2017/6/28 16:37
 * @since JDK1.8
 */
class ListViewActivity : AppCompatActivity(), Observer {
    private var collections: ArrayList<DownLoadBean> = arrayListOf()
    private var mDownloadManager: DownloadManager? = null
    private var adapter: ViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view)

        initData()

        adapter = ViewAdapter()
        listView.adapter = adapter
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

        if (index != -1) {
            if (downloadState == DownLoadState.STATE_DELETE.index) {
                collections.removeAt(index)
                try {
                    val file = File(bean.path)
                    val delete = file.delete()
                    Log.d("Edwin", "删除 state = $delete")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                adapter!!.notifyDataSetChanged()
            } else {
                collections[index] = bean
                updateItem(index, bean)
            }

        }
    }

    override fun onStart() {
        super.onStart()
        DownLoadObservable.getInstance().addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        DownLoadObservable.getInstance().deleteObserver(this)
    }

    private fun updateItem(position: Int, bean: DownLoadBean) {
        val firstVisible = listView.firstVisiblePosition
        val lastVisible = listView.lastVisiblePosition
        if (position in firstVisible..lastVisible) {
            val view = listView.getChildAt(position - firstVisible)
            val holder = view.tag as ViewHolder
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

            holder.button_delete.setOnClickListener { v -> mDownloadManager?.deleteTask(bean) }

            holder.button_start.setOnClickListener { v -> mDownloadManager?.addTask(bean) }

            holder.text_name.text = bean.appName
            holder.text_range.text = bean.isSupportRange.toString()
            holder.text_progress.text =
                FileUtilities.convertFileSize(bean.currentSize) + "/" + FileUtilities.convertFileSize(
                    bean.totalSize
                )
            holder.progressBar.max = bean.totalSize.toInt()
            holder.progressBar.progress = bean.currentSize.toInt()
        }

    }

    @SuppressLint("SetTextI18n")
    private inner class ViewAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return collections.size
        }

        override fun getItem(position: Int): DownLoadBean {
            return collections[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: ViewHolder
            val contentView: View
            if (convertView == null) {
//                contentView = LinearLayout.inflate(parent.context, R.layout.item_down, null)
                contentView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_down, parent, false)
                viewHolder = ViewHolder(contentView)
                contentView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                contentView = convertView
            }

            val bean = collections[position]
            viewHolder.text_name.text = bean.appName

            when (bean.downloadState) {
                DownLoadState.STATE_NONE.index -> {
                    viewHolder.button_start.text = DownLoadState.STATE_NONE.content
                }
                DownLoadState.STATE_WAITING.index -> {
                    viewHolder.button_start.text = DownLoadState.STATE_WAITING.content
                }
                DownLoadState.STATE_DOWNLOADING.index -> {
                    viewHolder.button_start.text = DownLoadState.STATE_DOWNLOADING.content
                }
                DownLoadState.STATE_PAUSED.index -> {
                    viewHolder.button_start.text = DownLoadState.STATE_PAUSED.content
                }
                DownLoadState.STATE_DOWNLOADED.index -> {
                    viewHolder.button_start.text = DownLoadState.STATE_DOWNLOADED.content
                }
                DownLoadState.STATE_ERROR.index -> {
                    viewHolder.button_start.text = DownLoadState.STATE_ERROR.content
                }
                DownLoadState.STATE_CONNECTION.index -> {
                    viewHolder.button_start.text = DownLoadState.STATE_CONNECTION.content
                }
            }

            viewHolder.button_delete.setOnClickListener { v ->
                mDownloadManager?.deleteTask(bean)
            }

            viewHolder.button_start.setOnClickListener { v ->
                mDownloadManager?.addTask(bean)
            }
            viewHolder.text_range.text = bean.isSupportRange.toString()
            viewHolder.text_progress.text =
                FileUtilities.convertFileSize(bean.currentSize) + "/" + FileUtilities.convertFileSize(
                    bean.totalSize
                )
            viewHolder.progressBar.max = bean.totalSize.toInt()
            viewHolder.progressBar.progress = bean.currentSize.toInt()

            return contentView
        }
    }

    private class ViewHolder
    internal constructor(override val containerView: View) : LayoutContainer
}