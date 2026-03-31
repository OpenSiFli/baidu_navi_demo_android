package com.baidu.mapclient.liteapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.adapter.BaseRecyclerViewAdapter;
import com.baidu.mapclient.liteapp.util.FolderHelper;
import com.sifli.siflicore.log.SFLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * LogsActivity是用来干什么的
 * 展示日志文件列表并可分享
 *
 * @author wangyao
 * @email 382708580@qq.com
 * @create 2025/5/8
 */
public class LogsActivity extends Activity {
    private static final String TAG = "LogsActivity";
    private RecyclerView recyclerView;
    private BaseRecyclerViewAdapter<File> mAdapter;
    private final ArrayList<File> datas = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new BaseRecyclerViewAdapter<File>(datas, this, R.layout.item_log_file, (holder, position, context) -> {
            TextView name = holder.getView(R.id.name);
            File f = datas.get(position);
            name.setText(f.getName());
            Button share = holder.getView(R.id.share);
            share.setOnClickListener(view -> shareFile(f));
            View container = holder.getView(R.id.container);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openFile(f);
                }
            });
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        String logPath = FolderHelper.getDeviceLogPath(this);
        File logs = new File(logPath);
        SFLog.i(TAG, "文件===" + Arrays.toString(logs.listFiles()));
        File[] files = logs.listFiles();
        if (files != null) {
            Collections.addAll(datas, files);
            datas.sort((f1, f2) -> {
                long time1 = f1.lastModified();
                long time2 = f2.lastModified();
                return Long.compare(time2, time1);
            });
        }
        if (datas.isEmpty()) {
            findViewById(R.id.empty).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void shareFile(File file) {
        // 获取文件的MIME类型
        String mimeType = getMimeType(file);
        SFLog.d(TAG, "文件MIME类型: " + mimeType);
        // 使用FileProvider获取文件的Content URI
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileProvider", file);
        SFLog.d(TAG, "文件URI: " + fileUri);
        // 创建分享Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 临时授予权限
        // 启动分享Activity
        startActivity(Intent.createChooser(shareIntent, "分享文件"));
    }

    // 获取文件的MIME类型
    private String getMimeType(File file) {
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (extension.equalsIgnoreCase("txt")) {
            return "text/plain";
        } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            return "image/jpeg";
        } else if (extension.equalsIgnoreCase("png")) {
            return "image/png";
        } else if (extension.equalsIgnoreCase("mp3")) {
            return "audio/mp3";
        } else if (extension.equalsIgnoreCase("mp4")) {
            return "video/mp4";
        } else {
            return "application/octet-stream";
        }
    }

    private void openFile(File file) {
        // 使用FileProvider获取文件的Content URI
        Uri fileUri = FileProvider.getUriForFile(this, "com.sifli.siflible.fileProvider", // 确保与 AndroidManifest.xml 中的 authorities 一致
                file);
        SFLog.d(TAG, "文件URI: " + fileUri);

        // 创建打开文件的Intent
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(fileUri, "text/plain");
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // 启动活动打开文件
        startActivity(Intent.createChooser(openIntent, "选择应用打开"));
    }
}
