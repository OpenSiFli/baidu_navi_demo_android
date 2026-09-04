package com.baidu.mapclient.liteapp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Image;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.service.MyMediaProjectionService;
import com.sifli.siflicore.log.SFLog;

public class AirPlayActivity extends AppCompatActivity
        implements View.OnClickListener,
        MyMediaProjectionService.MyMedioOnImageAvailableListener {

    private static final String TAG = "AirPlayActivity";
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 100;

    private Button airplayBtn;
    private Button stopBtn;
    private MediaProjectionManager projectionManager;

    // 服务相关
    private MyMediaProjectionService mediaService;
    private boolean isBound = false;
    // 临时保存 MediaProjection（若获取时服务尚未绑定）
    private MediaProjection pendingProjection;
    // 屏幕尺寸（从系统获取）
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    // 服务连接回调
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMediaProjectionService.LocalBinder binder = (MyMediaProjectionService.LocalBinder) service;
            mediaService = binder.getService();
            isBound = true;
            SFLog.d(TAG, "Service 绑定成功");

            // 设置图像监听（当前 Activity 实现了回调接口）
            mediaService.setOnImageAvailableListener(AirPlayActivity.this);

            // 如果之前已经获取到 MediaProjection，立即传递给 Service
            if (pendingProjection != null) {
                mediaService.initProjection(pendingProjection, screenWidth, screenHeight, screenDensity);
                pendingProjection = null;
                SFLog.d(TAG, "已传递 MediaProjection 给 Service");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaService = null;
            isBound = false;
            SFLog.d(TAG, "Service 断开连接");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_air_play);

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // 获取屏幕尺寸
        getScreenDimensions();

        initView();
        bindEvent();
    }

    private void getScreenDimensions() {
        android.view.WindowManager wm = (android.view.WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm != null) {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            screenDensity = metrics.densityDpi;
        } else {
            // 兜底值
            screenWidth = 1080;
            screenHeight = 1920;
            screenDensity = 320;
        }
    }

    private void initView() {
        airplayBtn = findViewById(R.id.navi_airplay_start_btn);
        stopBtn = findViewById(R.id.navi_airplay_stop_btn);
    }

    private void bindEvent() {
        airplayBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.navi_airplay_start_btn) {
            onAirPlayBtnTouch();
        }else if(viewId == R.id.navi_airplay_stop_btn){
            onStopAirplayBtnTouch();
        }
    }

    private void onAirPlayBtnTouch() {
        SFLog.i(TAG,"onAirPlayBtnTouch");
        // 1. 如果尚未绑定服务，则启动并绑定
        if (!isBound) {
            Intent intent = new Intent(this, MyMediaProjectionService.class);
            // 必须先 startService 才能成为前台服务
            startService(intent);
            // 绑定服务
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        // 2. 请求录屏权限（系统弹窗）
        Intent captureIntent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE);
    }

    private void onStopAirplayBtnTouch(){
        SFLog.i(TAG,"onStopAirplayBtnTouch");
        this.stopScreenMirroring();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK) {
                MediaProjection projection = projectionManager.getMediaProjection(resultCode, data);
                if (isBound && mediaService != null) {
                    // 服务已绑定，直接初始化
                    mediaService.initProjection(projection, screenWidth, screenHeight, screenDensity);
                    Toast.makeText(this, "录屏已开始", Toast.LENGTH_SHORT).show();
                } else {
                    // 尚未绑定（极少情况），暂存
                    pendingProjection = projection;
                    Toast.makeText(this, "录屏授权成功，等待服务绑定", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "用户拒绝录屏权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ------------------- 图像回调（后续集成 SDK） -------------------
    @Override
    public void onImageAvailable(Image image) {
        // 注意：此回调运行在 Service 的后台线程
        // 在这里可以集成 SifliotSDK，将 Image 转码或直接传递
        // 务必在操作完成后关闭 Image，否则内存泄漏
        SFLog.d(TAG, "收到图像帧: " + image.getWidth() + "x" + image.getHeight());

        // 【示例】直接释放（后续替换为 SDK 处理）
        // 如果 SDK 是异步处理，需要拷贝数据后再 close
        image.close();
    }

    // ------------------- 生命周期管理 -------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑服务（但一般投屏希望后台继续，因此这里仅解绑，不停止服务）
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
            SFLog.d(TAG, "已解绑服务，服务继续在后台运行");
        }

        // 如果希望 Activity 销毁时也停止录屏，可以取消注释以下代码：
        // if (mediaService != null) {
        //     mediaService.stopProjection();
        // }
    }

    // 可选：用户主动停止投屏的方法（可以加一个停止按钮调用）
    public void stopScreenMirroring() {
        if (mediaService != null) {
            mediaService.stopProjection();
        }
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}