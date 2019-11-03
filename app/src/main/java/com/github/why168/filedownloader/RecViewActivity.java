package com.github.why168.filedownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.why168.multifiledownloader.DownLoadBean;
import com.github.why168.multifiledownloader.DownLoadState;
import com.github.why168.multifiledownloader.DownloadManager;
import com.github.why168.multifiledownloader.db.DataBaseUtil;
import com.github.why168.multifiledownloader.notify.DownLoadObservable;
import com.github.why168.multifiledownloader.utlis.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * RecViewActivity
 *
 * @author Edwin.Wu
 * @version 2017/6/28 15:34
 * @since JDK1.8
 */
public class RecViewActivity extends AppCompatActivity implements Observer {
    private ArrayList<DownLoadBean> collections;
    private LayoutInflater mLayoutInflater;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter viewAdapter;
    private DownloadManager mDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_view);

        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        recyclerView = (RecyclerView) findViewById(R.id.recView);
        initData();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(viewAdapter = new ViewAdapter());
    }

    private void initData() {
        mDownloadManager = DownloadManager.getInstance(this);
        collections = new ArrayList<>();
        DownLoadBean bean1 = new DownLoadBean();
        bean1.appName = "爱奇艺";
        bean1.appIcon = "http://f.hiphotos.bdimg.com/wisegame/pic/item/1fd98d1001e93901b446c6217cec54e736d1966d.jpg";
        bean1.url = "http://124.192.151.146/cdn/qiyiapp/20160912/180818/ap/qiyi.196.apk";
        bean1.id = FileUtilities.getMd5FileName(bean1.url);

        DownLoadBean bean2 = new DownLoadBean();
        bean2.appName = "微信";
        bean2.appIcon = "http://f.hiphotos.bdimg.com/wisegame/pic/item/db0e7bec54e736d17a907ba993504fc2d4626994.jpg";
        bean2.url = "http://dldir1.qq.com/weixin/android/weixin6325android861.apk";
        bean2.id = FileUtilities.getMd5FileName(bean2.url);

        DownLoadBean bean3 = new DownLoadBean();
        bean3.appName = "淘宝";
        bean3.appIcon = "http://p1.qhimg.com/dr/160_160_/t01c513232212e2d915.png";
        bean3.url = "http://m.shouji.360tpcdn.com/160317/0a2c6811b5fc9bada8e7e082fb5a9324/com.taobao.trip_3001049.apk";
        bean3.id = FileUtilities.getMd5FileName(bean3.url);

        DownLoadBean bean4 = new DownLoadBean();
        bean4.appName = "酷狗音乐";
        bean4.appIcon = "http://c.hiphotos.bdimg.com/wisegame/pic/item/252309f7905298226013ce57dfca7bcb0a46d406.jpg";
        bean4.url = "http://downmobile.kugou.com/Android/KugouPlayer/8281/KugouPlayer_219_V8.2.8.apk";
        bean4.id = FileUtilities.getMd5FileName(bean4.url);

        DownLoadBean bean5 = new DownLoadBean();
        bean5.appName = "网易云音乐";
        bean5.appIcon = "http://d.hiphotos.bdimg.com/wisegame/pic/item/354e9258d109b3decfae38fec4bf6c81800a4c17.jpg";
        bean5.url = "http://s1.music.126.net/download/android/CloudMusic_official_3.7.2_150253.apk";
        bean5.id = FileUtilities.getMd5FileName(bean5.url);

        DownLoadBean bean6 = new DownLoadBean();
        bean6.appName = "百度手机卫士";
        bean6.appIcon = "http://a.hiphotos.bdimg.com/wisegame/pic/item/6955b319ebc4b7452322b1b9c7fc1e178b8215ee.jpg";
        bean6.url = "http://gdown.baidu.com/data/wisegame/6c795b7a341e0c69/baidushoujiweishi_3263.apk";
        bean6.id = FileUtilities.getMd5FileName(bean6.url);

        DownLoadBean bean7 = new DownLoadBean();
        bean7.appName = "语玩";
        bean7.appIcon = "http://www.12nav.com/interface/res/icons/yuwan.png";
        bean7.url = "http://125.32.30.10/Yuwan-0.6.25.0-81075.apk";
        bean7.id = FileUtilities.getMd5FileName(bean7.url);

        DownLoadBean bean8 = new DownLoadBean();
        bean8.appName = "全民K歌";
        bean8.appIcon = "http://e.hiphotos.bdimg.com/wisegame/pic/item/db99a9014c086e0639999b2f0a087bf40ad1cba5.jpg";
        bean8.url = "http://d3g.qq.com/musicapp/kge/877/karaoke_3.6.8.278_android_r31018_20160725154442_release_GW_D.apk";
        bean8.id = FileUtilities.getMd5FileName(bean8.url);


        DownLoadBean bean9 = new DownLoadBean();
        bean9.appName = "魔秀桌面";
        bean9.appIcon = "http://e.hiphotos.bdimg.com/wisegame/pic/item/db99a9014c086e0639999b2f0a087bf40ad1cba5.jpg";
        bean9.url = "http://211.161.126.174/imtt.dd.qq.com/16891/41C80B55FE1051D8C09D2C2B3D17F9F3.apk?mkey=5874800846b6ee89&f=8f5d&c=0&fsname=com.moxiu.launcher_5.8.5_585.apk&csr=4d5s&p=.apk";
        bean9.id = FileUtilities.getMd5FileName(bean9.url);

        collections.clear();
        collections.add(bean1);
        collections.add(bean2);
        collections.add(bean3);
        collections.add(bean4);
        collections.add(bean5);
        collections.add(bean6);
        collections.add(bean7);
        collections.add(bean8);
        collections.add(bean9);

        ArrayList<DownLoadBean> downLoad = DataBaseUtil.getDownLoad(this);
        for (int i = 0; i < downLoad.size(); i++) {
            DownLoadBean beanI = downLoad.get(i);
            for (int j = 0; j < collections.size(); j++) {
                DownLoadBean beanJ = collections.get(j);
                if (beanI.id.equals(beanJ.id)) {
                    collections.set(j, beanI);
                    break;
                }
            }
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(o instanceof DownLoadObservable)) {
            return;
        }

        final DownLoadBean bean = (DownLoadBean) arg;
        int index = collections.indexOf(bean);
        Log.d("Edwin", "index = " + index + " bean = " + bean.toString());
        int downloadState = bean.downloadState;

        if (index != -1 && isCurrentListViewItemVisible(index)) {
            if (downloadState == DownLoadState.STATE_DELETE) {
                viewAdapter.notifyItemRemoved(index);
                collections.remove(index);
                if (index != collections.size()) {
                    notifyChange(bean, index);
                }
                try {
                    File file = new File(bean.path);
                    boolean delete = file.delete();
                    Log.d("Edwin", "删除 state = " + delete);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                collections.set(index, bean);
                notifyChange(bean, index);
            }
        }
    }


    /**
     * 数据改变
     *
     * @param bean
     * @param index
     */
    private void notifyChange(final DownLoadBean bean, int index) {
        ViewHolder holder = getViewHolder(index);

        switch (bean.downloadState) {
            case DownLoadState.STATE_NONE:
                holder.button_start.setText("点击下载");
                break;
            case DownLoadState.STATE_WAITING:
                // 等待下载 改成 排队下载
                holder.button_start.setText("排队下载");
                break;
            case DownLoadState.STATE_DOWNLOADING:
                // 下载中 改成 正在下载
                holder.button_start.setText("正在下载");
                break;
            case DownLoadState.STATE_PAUSED:
                // 暂停下载 换成 继续下载
                holder.button_start.setText("继续下载");
                break;
            case DownLoadState.STATE_DOWNLOADED:
                holder.button_start.setText("下载完毕");
                break;
            case DownLoadState.STATE_ERROR:
                holder.button_start.setText("下载错误");
                break;
            case DownLoadState.STATE_CONNECTION:
                holder.button_start.setText("连接中");
                break;
        }

        holder.button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadManager.delete(bean);
            }
        });

        holder.button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadManager.down(bean);
            }
        });

        holder.text_name.setText(bean.appName);
        holder.text_range.setText(String.valueOf(bean.isSupportRange));
        holder.text_progress.setText(FileUtilities.convertFileSize(bean.currentSize) + "/" + FileUtilities.convertFileSize(bean.totalSize));
        holder.progressBar.setMax((int) bean.totalSize);
        holder.progressBar.setProgress((int) bean.currentSize);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DownLoadObservable.getInstance().addObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownLoadObservable.getInstance().deleteObserver(this);
    }

    private class ViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mLayoutInflater.inflate(R.layout.item_down, parent, false));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final DownLoadBean bean = collections.get(position);
            holder.text_name.setText(bean.appName);

            switch (bean.downloadState) {
                case DownLoadState.STATE_NONE:
                    holder.button_start.setText("点击下载");
                    break;
                case DownLoadState.STATE_WAITING:
                    holder.button_start.setText("等待下载");
                    break;
                case DownLoadState.STATE_DOWNLOADING:
                    // 下载中 改成 正在下载
                    holder.button_start.setText("正在下载");
                    break;
                case DownLoadState.STATE_PAUSED:
                    // 暂停下载 换成 继续下载
                    holder.button_start.setText("继续下载");
                    break;
                case DownLoadState.STATE_DOWNLOADED:
                    holder.button_start.setText("下载完毕");
                    break;
                case DownLoadState.STATE_ERROR:
                    holder.button_start.setText("下载错误");
                    break;
                case DownLoadState.STATE_CONNECTION:
                    holder.button_start.setText("连接中");
                    break;
            }

            holder.button_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDownloadManager.delete(bean);
                }
            });

            holder.button_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDownloadManager.down(bean);
                }
            });

            holder.text_range.setText(String.valueOf(bean.isSupportRange));
            holder.text_progress.setText(FileUtilities.convertFileSize(bean.currentSize) + "/" + FileUtilities.convertFileSize(bean.totalSize));
            holder.progressBar.setMax((int) bean.totalSize);
            holder.progressBar.setProgress((int) bean.currentSize);
        }

        @Override
        public int getItemCount() {
            return collections.size();
        }
    }

    final static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text_name;
        Button button_start;
        Button button_delete;
        TextView text_progress;
        ProgressBar progressBar;
        TextView text_range;

        ViewHolder(View itemView) {
            super(itemView);
            text_name = itemView.findViewById(R.id.text_name);
            button_start = itemView.findViewById(R.id.button_start);
            button_delete = itemView.findViewById(R.id.button_delete);
            text_progress = itemView.findViewById(R.id.text_progress);
            progressBar = itemView.findViewById(R.id.progressBar);
            text_range = itemView.findViewById(R.id.text_range);
        }
    }

    private ViewHolder getViewHolder(int position) {
        return (ViewHolder) recyclerView.findViewHolderForLayoutPosition(position);
    }

    private boolean isCurrentListViewItemVisible(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        return first <= position && position <= last;
    }
}
