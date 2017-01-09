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

import com.github.why168.filedownloader.bean.DownLoadBean;
import com.github.why168.filedownloader.constant.DownLoadState;
import com.github.why168.filedownloader.pattern.DownLoadObservable;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {
    private List<DownLoadBean> loadBeen = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter viewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        initData();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(viewAdapter = new ViewAdapter());

        DownLoadObservable.getInstance().addObserver(this);
    }

    private void initData() {
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

        loadBeen.add(bean1);
        loadBeen.add(bean2);
        loadBeen.add(bean3);
        loadBeen.add(bean4);
        loadBeen.add(bean5);
        loadBeen.add(bean6);
        loadBeen.add(bean7);
        loadBeen.add(bean8);

//        ArrayList<DownLoadBean> downLoad = DataBaseUtil.getDownLoad();
//        if (downLoad.size() > 0) {
//            for (int i = 0; i < downLoad.size(); i++) {
//                DownLoadBean bean = downLoad.get(i);
//
//                for (int j = 0; j < loadBeen.size(); j++) {
//                    DownLoadBean loadBean = loadBeen.get(i);
//
//                    if (bean.id.equals(loadBean.id)) {
//                        loadBeen.remove(j);
//                        loadBeen.add(j, bean);
//                        break;
//                    }
//                }
//            }
//        }
    }

    public String FormetFileSize(long fileSize) {// 转换文件大小
        if (fileSize <= 0) {
            return "0M";
        }
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileSize < 1024) {
            fileSizeString = df.format((double) fileSize) + "B";
        } else if (fileSize < 1048576) {
            fileSizeString = df.format((double) fileSize / 1024) + "K";
        } else if (fileSize < 1073741824) {
            fileSizeString = df.format((double) fileSize / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileSize / 1073741824) + "G";
        }
        return fileSizeString;
    }


    @Override
    public void update(Observable o, Object arg) {
        DownLoadBean bean = (DownLoadBean) arg;
        int index = loadBeen.indexOf(bean);
        Log.e("Edwin", "index = " + index + " bean = " + bean.toString());
        int downloadState = bean.downloadState;
        if (index != -1) {
            if (downloadState == DownLoadState.STATE_DELETE) {
                loadBeen.remove(index);
                viewAdapter.notifyItemRemoved(index);
                if (index != loadBeen.size())
                    viewAdapter.notifyItemChanged(index, loadBeen.size() - index);

                try {
                    File file = new File(bean.path);
                    boolean delete = file.delete();
                    Log.e("Edwin", "删除 state = " + delete);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                loadBeen.remove(index);
                loadBeen.add(index, bean);
                viewAdapter.notifyItemChanged(index, "index");
            }
        }

//        switch (downloadState) {
//            case DownLoadState.STATE_NONE:
//                onStart(bean, index);
//                break;
//            case DownLoadState.STATE_WAITING:
//                onPrepare(bean, index);
//                break;
//            case DownLoadState.STATE_DOWNLOADING:
//                onProgress(bean, index);
//                break;
//            case DownLoadState.STATE_PAUSED:
//                onStop(bean, index);
//                break;
//            case DownLoadState.STATE_DOWNLOADED:
//                onFinish(bean, index);
//                break;
//            case DownLoadState.STATE_ERROR:
//                onError(bean, index);
//                break;
//        }
    }


    /**
     * 准备下载
     */
    void onPrepare(DownLoadBean bean, int index) {
        viewAdapter.notifyItemChanged(index, "index");
    }

    /**
     * 开始下载
     */
    void onStart(DownLoadBean bean, int index) {
    }

    /**
     * 下载中
     */
    void onProgress(DownLoadBean bean, int index) {
    }

    /**
     * 暂停
     */
    void onStop(DownLoadBean bean, int index) {
    }

    /**
     * 下载完成
     */
    void onFinish(DownLoadBean bean, int index) {
    }

    /**
     * 下载失败
     */
    void onError(DownLoadBean bean, int index) {
    }

    /**
     * 删除成功
     */
    void onDelete(DownLoadBean bean, int index) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownLoadObservable.getInstance().deleteObserver(this);
    }

    /**
     *
     */
    private class ViewAdapter extends RecyclerView.Adapter<ViewHolder2> {
        @Override
        public ViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder2(mLayoutInflater.inflate(R.layout.item_down, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder2 holder, int position, List<Object> payloads) {
            if (!"index".equals(payloads)) {
                onBindViewHolder(holder, position);
            } else {
                DownLoadBean item = loadBeen.get(position);
                switch (item.downloadState) {
                    case DownLoadState.STATE_NONE:
                        holder.button_start.setText("点击下载");
                        break;
                    case DownLoadState.STATE_WAITING:
                        //TODO 等待下载 改成 排队下载
                        holder.button_start.setText("排队下载");
                        break;
                    case DownLoadState.STATE_DOWNLOADING:
                        //TODO 下载中 改成 正在下载
                        holder.button_start.setText("正在下载");
                        break;
                    case DownLoadState.STATE_PAUSED:
                        //TODO 暂停下载 换成 继续下载
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
                holder.progressBar.setMax((int) item.totalSize);
                holder.progressBar.setProgress((int) item.currentSize);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder2 holder, int position) {
            DownLoadBean item = loadBeen.get(position);
            holder.text_name.setText(item.appName);

            switch (item.downloadState) {
                case DownLoadState.STATE_NONE:
                    holder.button_start.setText("点击下载");
                    break;
                case DownLoadState.STATE_WAITING:
                    //TODO 等待下载 改成 排队下载
                    holder.button_start.setText("排队下载");
                    break;
                case DownLoadState.STATE_DOWNLOADING:
                    //TODO 下载中 改成 正在下载
                    holder.button_start.setText("正在下载");
                    break;
                case DownLoadState.STATE_PAUSED:
                    //TODO 暂停下载 换成 继续下载
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
                    // 删除下载
                    DownLoadManager.getInstance().DeleteDownTask(item);
                }
            });

            holder.button_start.setOnClickListener(v -> {
                // 开启下载
                DownLoadManager.getInstance().download(item);
            });

            holder.text_progress.setText(FormetFileSize(item.currentSize) + "/" + FormetFileSize(item.totalSize));

            holder.progressBar.setMax((int) item.totalSize);
            holder.progressBar.setProgress((int) item.currentSize);
        }

        @Override
        public int getItemCount() {
            return loadBeen.size();
        }
    }

    static class ViewHolder2 extends RecyclerView.ViewHolder {
        TextView text_name;
        Button button_start;
        Button button_delete;
        TextView text_progress;
        ProgressBar progressBar;

        ViewHolder2(View itemView) {
            super(itemView);
            text_name = (TextView) itemView.findViewById(R.id.text_name);
            button_start = (Button) itemView.findViewById(R.id.button_start);
            button_delete = (Button) itemView.findViewById(R.id.button_delete);
            text_progress = (TextView) itemView.findViewById(R.id.text_progress);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }
}
