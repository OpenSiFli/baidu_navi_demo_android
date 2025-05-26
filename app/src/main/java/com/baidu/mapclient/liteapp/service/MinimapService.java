package com.baidu.mapclient.liteapp.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.baidu.mapclient.liteapp.LocationController;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.custom.MiniMapViewController;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNMiniMapViewManager;
import com.baidu.navisdk.util.common.LogUtil;

public class MinimapService extends Service {
    private WindowManager windowManager;
    private final static int miniWidth = 1200;
    private final static int miniHeight = 720;
    View minimapView;

    /**
     * 多实例底图
     */
    MiniMapViewController miniMapViewController = new MiniMapViewController();

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // 创建悬浮窗
        createFloatingWindow();

        initMiniMap();
        LogUtil.out("MinimapService", "onCreate");
        BaiduNaviManagerFactory.getBaiduNaviManager().externalLocation(true);
        LocationController.getInstance().startLocation(getApplication());
    }

    private void initMiniMap() {
        final float[] lastX = new float[1];
        final float[] lastY = new float[1];
        minimapView.findViewById(R.id.btn_move).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX[0] = x;
                        lastY[0] = y;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = x - lastX[0];
                        float deltaY = y - lastY[0];

                        if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) minimapView.getLayoutParams();
                            layoutParams.x += (int) deltaX;
                            layoutParams.y += (int) deltaY;
                            windowManager.updateViewLayout(minimapView, layoutParams);

                            lastX[0] = x;
                            lastY[0] = y;
                        }
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        minimapView.findViewById(R.id.btn_enlarge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) minimapView.getLayoutParams();
                if (layoutParams.width == 500) {
                    layoutParams.width = miniWidth;
                    layoutParams.height = miniHeight;
                    miniMapViewController.offset(0, -250);
                    ((TextView) minimapView.findViewById(R.id.btn_enlarge)).setText("缩小");
                } else {
                    layoutParams.width = 500;
                    layoutParams.height = 500;
                    ((TextView) minimapView.findViewById(R.id.btn_enlarge)).setText("放大");
                    miniMapViewController.offset(0, 0);
                }
                miniMapViewController.viewHide(layoutParams.width == miniWidth);
                windowManager.updateViewLayout(minimapView, layoutParams);
            }
        });

        minimapView.findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFloatingWindow();
                stopSelf();
            }
        });
    }

    private void createFloatingWindow() {
        minimapView = miniMapViewController.onCreate(this);
        if (minimapView != null) {
            // 设置悬浮窗的布局参数
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    500,
                    500,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            // 添加悬浮窗到 WindowManager
            windowManager.addView(minimapView, params);
            miniMapViewController.setNaviMode(IBNMiniMapViewManager.NaviMode.NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeFloatingWindow();
        miniMapViewController.onDestroy();
        // 移除悬浮窗
        if (minimapView != null) {
            windowManager.removeView(minimapView);
        }
        LogUtil.out("MinimapService", "onDestroy");
        BaiduNaviManagerFactory.getBaiduNaviManager().externalLocation(false);
        LocationController.getInstance().stopLocation();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void closeFloatingWindow() {
        if (minimapView != null && windowManager != null) {
            windowManager.removeView(minimapView);
            minimapView = null;
        }
    }

}

