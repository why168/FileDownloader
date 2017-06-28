package com.github.why168.filedownloader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.why168.filedownloader.R;


/**
 * 多任务下载
 *
 * @author Edwin.Wu
 * @version 2016/12/25 15:55
 * @since JDK1.8
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpList(View view) {
        startActivity(new Intent(this, ListViewActivity.class));
    }

    public void jumpRec(View view) {
        startActivity(new Intent(this, RecViewActivity.class));
    }
}
