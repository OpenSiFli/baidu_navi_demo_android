package com.baidu.mapclient.liteapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyMediaProjectionService extends Service {

    private static final String TAG = "MediaProjectionService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "media_projection_channel";

    // Binder 实例，用于 Activity 调用
    private final IBinder mBinder = new LocalBinder();

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    // 屏幕尺寸（从 Activity 传入或系统获取）
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;
    private boolean isRunning =false;

    // 图像回调（供 SDK 集成）
    public interface MyMedioOnImageAvailableListener {
        void onImageAvailable(Image image);
    }
    private MyMedioOnImageAvailableListener imageListener;

    public class LocalBinder extends Binder {
        public MyMediaProjectionService getService() {
            return MyMediaProjectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建后台线程
        backgroundThread = new HandlerThread("ScreenCaptureThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        // 获取屏幕尺寸（兜底方案，也可以用传入的）
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm != null) {
            DisplayMetrics metrics = new android.util.DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            screenDensity = metrics.densityDpi;
        }

        // 创建前台通知
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 若服务被系统重启，保持存活（但 MediaProjection 会失效，需外部重新初始化）
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // ------------------- 对外公开的方法（供 Activity 调用） -------------------

    /**
     * 初始化录屏（由 Activity 在绑定成功后调用）
     * @param projection 从 onActivityResult 获取的 MediaProjection
     * @param width 屏幕宽度（建议传入，确保准确）
     * @param height 屏幕高度
     * @param dpi 屏幕密度
     */
    public void initProjection(MediaProjection projection, int width, int height, int dpi) {
        if (projection == null) {
            Log.e(TAG, "MediaProjection is null");
            return;
        }
        this.mediaProjection = projection;
        this.screenWidth = width > 0 ? width : screenWidth;
        this.screenHeight = height > 0 ? height : screenHeight;
        this.screenDensity = dpi > 0 ? dpi : screenDensity;

        startScreenCapture();
    }

    /**
     * 设置图像可用监听（用于外部集成 SDK）
     */
    public void setOnImageAvailableListener(MyMedioOnImageAvailableListener listener) {
        this.imageListener = listener;
    }

    /**
     * 停止录屏并释放资源（Activity 可以调用此方法主动停止）
     */
    public void stopProjection() {
        isRunning = false;
        if (backgroundHandler != null) {
            backgroundHandler.removeCallbacksAndMessages(null);
        }
        releaseResources();
        // 停止前台服务
        stopForeground(true);
        stopSelf();
    }

    // ------------------- 内部录屏逻辑 -------------------

    private void startScreenCapture() {
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection 未初始化");
            return;
        }
        isRunning = true;
        if (imageReader != null) {
            imageReader.close();
        }

        // 创建 ImageReader
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(reader -> {
            if(!isRunning)return;
            Image image = reader.acquireLatestImage();
            if (image != null) {
                Log.d(TAG, "捕获到图像: " + image.getWidth() + "x" + image.getHeight());
                if (imageListener != null) {
                    // 外部监听器处理，外部必须负责关闭 image
                    imageListener.onImageAvailable(image);
                } else {
                    // 无监听器则立即释放
                    image.close();
                }
            }
        }, backgroundHandler);

        // 创建虚拟显示器
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null, null
        );

        Log.i(TAG, "录屏已启动，分辨率: " + screenWidth + "x" + screenHeight);
    }

    private void releaseResources() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseResources();
        Log.i(TAG, "Service 已销毁");
    }

    // ------------------- 通知相关 -------------------

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "录屏服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("录屏投屏中")
                .setContentText("正在捕获屏幕...")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}